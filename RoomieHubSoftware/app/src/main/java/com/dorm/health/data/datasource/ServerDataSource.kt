package com.dorm.health.data.datasource

import com.dorm.health.data.model.EnvironmentSnapshot
import com.dorm.health.data.model.ServerConnectionTestResult
import com.dorm.health.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 通过 Spring Boot REST API 从本地电脑读取 ESP32 环境数据。
 */
class ServerDataSource(
    private val preferencesManager: PreferencesManager
) {
    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _lastTestResult = MutableStateFlow<ServerConnectionTestResult?>(null)
    val lastTestResult: StateFlow<ServerConnectionTestResult?> = _lastTestResult.asStateFlow()

    fun baseUrl(): String = preferencesManager.serverBaseUrl

    suspend fun testConnection(): ServerConnectionTestResult = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.CONNECTING
        val startMs = System.currentTimeMillis()
        try {
            val healthPaths = listOf("/api/health", "/actuator/health")
            for (path in healthPaths) {
                request(path)?.let { (url, body) ->
                    if (body.contains("ok", ignoreCase = true) || body.contains("UP", ignoreCase = true)) {
                        return@withContext successResult(
                            url = url,
                            elapsedMs = System.currentTimeMillis() - startMs,
                            message = "Spring Boot 服务连接成功（健康检查通过）"
                        )
                    }
                }
            }

            val latestPaths = listOf("/api/latest", "/api/environment/latest")
            for (path in latestPaths) {
                request(path)?.let { (url, body) ->
                    parseSnapshot(body)?.let { snapshot ->
                        return@withContext successResult(
                            url = url,
                            elapsedMs = System.currentTimeMillis() - startMs,
                            message = "Spring Boot 连接成功，已获取最新环境数据",
                            snapshot = snapshot
                        )
                    }
                }
            }

            failureResult(
                message = lastRequestError ?: "无法连接 Spring Boot，请检查 IP、端口与接口路径",
                elapsedMs = System.currentTimeMillis() - startMs
            )
        } catch (e: Exception) {
            failureResult(
                message = e.message ?: "连接失败：${e.javaClass.simpleName}",
                elapsedMs = System.currentTimeMillis() - startMs
            )
        }
    }

    suspend fun fetchLatest(): EnvironmentSnapshot? = withContext(Dispatchers.IO) {
        try {
            val snapshot = fetchLatestInternal()
            if (snapshot != null) {
                markConnected()
            } else {
                markError(lastRequestError ?: "服务器未返回有效环境数据")
            }
            snapshot
        } catch (e: Exception) {
            markError(e.message ?: "获取数据失败")
            null
        }
    }

    suspend fun fetchHistory(from: Long, to: Long): List<EnvironmentSnapshot> =
        withContext(Dispatchers.IO) {
            try {
                val paths = listOf(
                    "/api/history?from=$from&to=$to",
                    "/api/environment/history?from=$from&to=$to"
                )
                for (path in paths) {
                    val body = request(path)?.second ?: continue
                    val array = extractArray(body) ?: continue
                    val result = mutableListOf<EnvironmentSnapshot>()
                    for (i in 0 until array.length()) {
                        parseSnapshot(array.getJSONObject(i).toString())?.let { result.add(it) }
                    }
                    if (result.isNotEmpty()) {
                        markConnected()
                        return@withContext result
                    }
                }
                emptyList()
            } catch (e: Exception) {
                markError(e.message ?: "同步历史数据失败")
                emptyList()
            }
        }

    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
        _lastError.value = null
        _lastTestResult.value = null
    }

    private var lastRequestError: String? = null

    private fun successResult(
        url: String,
        elapsedMs: Long,
        message: String,
        snapshot: EnvironmentSnapshot? = null
    ): ServerConnectionTestResult {
        markConnected()
        val result = ServerConnectionTestResult(
            success = true,
            message = message,
            testedUrl = url,
            responseTimeMs = elapsedMs,
            sampleData = snapshot
        )
        _lastTestResult.value = result
        return result
    }

    private fun failureResult(message: String, elapsedMs: Long): ServerConnectionTestResult {
        markError(message)
        val result = ServerConnectionTestResult(
            success = false,
            message = message,
            testedUrl = lastRequestError?.substringAfter("："),
            responseTimeMs = elapsedMs
        )
        _lastTestResult.value = result
        return result
    }

    private fun fetchLatestInternal(): EnvironmentSnapshot? {
        for (path in listOf("/api/latest", "/api/environment/latest")) {
            request(path)?.second?.let { body ->
                parseSnapshot(body)?.let { return it }
            }
        }
        return null
    }

    private fun request(path: String): Pair<String, String>? {
        val url = "${baseUrl()}$path"
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                lastRequestError = "HTTP ${response.code}：$url"
                return null
            }
            lastRequestError = null
            val body = response.body?.string() ?: return null
            return url to body
        }
    }

    private fun markConnected() {
        _connectionState.value = ConnectionState.CONNECTED
        _lastError.value = null
    }

    private fun markError(message: String) {
        _connectionState.value = ConnectionState.ERROR
        _lastError.value = message
    }

    private fun extractArray(body: String): JSONArray? {
        val trimmed = body.trim()
        if (trimmed.startsWith("[")) return JSONArray(trimmed)
        val obj = JSONObject(trimmed)
        listOf("data", "content", "records", "items", "result").forEach { key ->
            if (obj.has(key)) {
                when (val value = obj.get(key)) {
                    is JSONArray -> return value
                    is JSONObject -> return JSONArray().put(value)
                }
            }
        }
        return null
    }

    private fun extractObject(body: String): JSONObject? {
        val trimmed = body.trim()
        if (trimmed.startsWith("{")) {
            val obj = JSONObject(trimmed)
            listOf("data", "content", "record", "result").forEach { key ->
                if (obj.has(key) && obj.get(key) is JSONObject) {
                    return obj.getJSONObject(key)
                }
            }
            if (obj.has("temp") || obj.has("temperature")) return obj
            return obj
        }
        return null
    }

    private fun parseSnapshot(body: String): EnvironmentSnapshot? {
        return try {
            val obj = extractObject(body) ?: return null
            EnvironmentSnapshot(
                temp = obj.readFloat("temp", "temperature", default = 24f),
                humi = obj.readFloat("humi", "humidity", default = 55f),
                noise = obj.readFloat("noise", "decibel", default = 40f),
                light = obj.readFloat("light", "lux", default = 100f),
                aqi = obj.readInt("aqi", default = 50),
                isSimulated = false,
                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun JSONObject.readFloat(vararg keys: String, default: Float): Float {
        keys.forEach { key ->
            if (has(key) && !isNull(key)) return optDouble(key).toFloat()
        }
        return default
    }

    private fun JSONObject.readInt(vararg keys: String, default: Int): Int {
        keys.forEach { key ->
            if (has(key) && !isNull(key)) return optInt(key)
        }
        return default
    }
}
