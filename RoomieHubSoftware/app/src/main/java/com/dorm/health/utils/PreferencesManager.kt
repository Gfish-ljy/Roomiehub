package com.dorm.health.utils

import android.content.Context
import android.content.SharedPreferences
import com.dorm.health.data.model.SensorMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dorm_health_prefs", Context.MODE_PRIVATE)

    private val _darkModeOverride = MutableStateFlow(readDarkModeOverride())

    /** 深色模式偏好变更流，供 MainActivity 实时响应手动切换 */
    val darkModeOverrideFlow: StateFlow<Boolean?> = _darkModeOverride.asStateFlow()

    private fun readDarkModeOverride(): Boolean? =
        if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null

    var refreshIntervalSeconds: Int
        get() = prefs.getInt(KEY_REFRESH_INTERVAL, 5)
        set(value) = prefs.edit().putInt(KEY_REFRESH_INTERVAL, value).apply()

    var darkModeOverride: Boolean?
        get() = _darkModeOverride.value
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_DARK_MODE).apply()
            } else {
                prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
            }
            _darkModeOverride.value = value
        }

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var serverHost: String
        get() = prefs.getString(KEY_SERVER_HOST, DEFAULT_SERVER_HOST) ?: DEFAULT_SERVER_HOST
        set(value) = prefs.edit().putString(KEY_SERVER_HOST, value.trim()).apply()

    var serverPort: Int
        get() = prefs.getInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)
        set(value) = prefs.edit().putInt(KEY_SERVER_PORT, value).apply()

    val serverBaseUrl: String
        get() {
            val host = serverHost
                .removePrefix("http://")
                .removePrefix("https://")
                .substringBefore("/")
                .substringBefore(":")
            val port = serverPort.coerceIn(1, 65535)
            return "http://$host:$port"
        }

    var sensorMode: SensorMode
        get() = when (prefs.getString(KEY_SENSOR_MODE, "PHONE")) {
            "SERVER", "ESP32" -> SensorMode.SERVER
            else -> SensorMode.PHONE
        }
        set(value) = prefs.edit().putString(KEY_SENSOR_MODE, value.name).apply()

    var lastReportCheckDate: String?
        get() = prefs.getString(KEY_LAST_REPORT_CHECK, null)
        set(value) = prefs.edit().putString(KEY_LAST_REPORT_CHECK, value).apply()

    var loggedInUserId: String?
        get() = prefs.getString(KEY_LOGGED_IN_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_LOGGED_IN_USER_ID, value).apply()

    companion object {
        private const val KEY_REFRESH_INTERVAL = "refresh_interval"
        private const val KEY_DARK_MODE = "dark_mode_override"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_SERVER_HOST = "server_host"
        private const val KEY_SERVER_PORT = "server_port"
        private const val KEY_SENSOR_MODE = "sensor_mode"
        private const val KEY_LAST_REPORT_CHECK = "last_report_check"
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"

        private const val DEFAULT_SERVER_HOST = "192.168.1.100"
        private const val DEFAULT_SERVER_PORT = 8080
    }
}
