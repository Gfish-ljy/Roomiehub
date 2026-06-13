package com.dorm.health.ui.screens.serverconnection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dorm.health.DormHealthApp
import com.dorm.health.data.datasource.ServerDataSource
import com.dorm.health.data.model.SensorMode
import com.dorm.health.data.model.ServerConnectionTestResult
import com.dorm.health.ui.components.AppBackground
import com.dorm.health.ui.components.GlassCard
import com.dorm.health.ui.theme.SuccessDark
import com.dorm.health.ui.theme.SuccessLight
import com.dorm.health.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConnectionScreen(onBack: () -> Unit) {
    val prefs = DormHealthApp.instance.preferencesManager
    val envRepo = DormHealthApp.instance.environmentRepository
    val server = DormHealthApp.instance.serverDataSource
    val scope = rememberCoroutineScope()
    val isDark = isAppInDarkTheme()
    val fontScale = LocalConfiguration.current.fontScale
    val useCompactButtons = fontScale >= 1.15f

    val connectionState by server.connectionState.collectAsState()
    val lastError by server.lastError.collectAsState()
    val lastTestResult by server.lastTestResult.collectAsState()

    var host by remember { mutableStateOf(prefs.serverHost) }
    var portText by remember { mutableStateOf(prefs.serverPort.toString()) }
    var isBusy by remember { mutableStateOf(false) }
    var localTestResult by remember { mutableStateOf<ServerConnectionTestResult?>(null) }

    val displayResult = localTestResult ?: lastTestResult

    val previewUrl = remember(host, portText) {
        val cleanHost = host.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")
            .substringBefore(":")
        val port = portText.toIntOrNull()?.coerceIn(1, 65535) ?: prefs.serverPort
        if (cleanHost.isBlank()) "http://<IP>:$port" else "http://$cleanHost:$port"
    }

    AppBackground(isDark = isDark) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Text(
                            text = "服务器连接",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            }
        ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(isDark = isDark) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "请确保手机与电脑处于同一局域网，并在电脑上启动 Spring Boot 服务。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("电脑 IP 地址", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        placeholder = { Text("例如 192.168.1.100", maxLines = 1) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it.filter { ch -> ch.isDigit() }.take(5) },
                        label = { Text("端口", maxLines = 1) },
                        placeholder = { Text("8080", maxLines = 1) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ServerActionButton(
                            text = when {
                                isBusy -> "测试中..."
                                else -> "测试 Spring Boot 连接"
                            },
                            enabled = !isBusy && host.isNotBlank(),
                            compact = useCompactButtons,
                            onClick = {
                                val port = portText.toIntOrNull() ?: 8080
                                prefs.serverHost = host
                                prefs.serverPort = port
                                isBusy = true
                                localTestResult = null
                                scope.launch {
                                    val result = envRepo.testServerConnection()
                                    localTestResult = result
                                    isBusy = false
                                }
                            }
                        )
                        OutlinedButton(
                            onClick = {
                                val port = portText.toIntOrNull() ?: 8080
                                prefs.serverHost = host
                                prefs.serverPort = port
                                isBusy = true
                                scope.launch {
                                    envRepo.switchMode(SensorMode.SERVER)
                                    envRepo.syncHistoryFromServer()
                                    isBusy = false
                                }
                            },
                            enabled = !isBusy && host.isNotBlank() &&
                                (displayResult?.success == true || connectionState == ServerDataSource.ConnectionState.CONNECTED),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (useCompactButtons) "保存并启用" else "保存并启用服务器模式",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge,
                                lineHeight = MaterialTheme.typography.labelLarge.lineHeight
                            )
                        }
                    }

                    if (isBusy) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("正在测试 Spring Boot 连接...", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    displayResult?.let { result ->
                        ConnectionTestResultCard(result = result, isDark = isDark)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusLine("状态", connectionStateLabel(connectionState))
                        StatusLine("地址", previewUrl)
                        if (displayResult == null) {
                            lastError?.let { StatusLine("错误", it, isError = true) }
                        }
                    }
                }
            }

            GlassCard(isDark = isDark) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Spring Boot 接口约定",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = MaterialTheme.typography.titleLarge.lineHeight
                    )
                    ApiPathLine("GET /api/health")
                    ApiPathLine("GET /api/latest")
                    ApiPathLine("GET /api/environment/latest")
                    ApiPathLine("GET /api/history?from=&to=")
                    Text(
                        text = "latest 返回示例：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                    Text(
                        text = "{\"temp\":23.5,\"humi\":58,\"noise\":45,\"light\":120,\"aqi\":85,\"timestamp\":1710000000000}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = MaterialTheme.typography.labelMedium.lineHeight
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
        }
    }
}

@Composable
private fun ConnectionTestResultCard(
    result: ServerConnectionTestResult,
    isDark: Boolean
) {
    val accent = if (result.success) {
        if (isDark) SuccessDark else SuccessLight
    } else {
        MaterialTheme.colorScheme.error
    }
    val bgColor = accent.copy(alpha = if (isDark) 0.22f else 0.12f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (result.success) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (result.success) "Spring Boot 连接成功" else "Spring Boot 连接失败",
                style = MaterialTheme.typography.titleLarge,
                color = accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = result.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        result.testedUrl?.let { StatusLine("测试接口", it) }
        if (result.responseTimeMs > 0) {
            StatusLine("响应时间", "${result.responseTimeMs} ms")
        }
        result.sampleData?.let { data ->
            StatusLine(
                "数据预览",
                "温度 ${data.temp}°C · 湿度 ${data.humi}% · 噪音 ${data.noise}dB · 光照 ${data.light}lux · AQI ${data.aqi}"
            )
        }
    }
}

@Composable
private fun ServerActionButton(
    text: String,
    enabled: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (compact) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    Icons.Outlined.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                    lineHeight = MaterialTheme.typography.labelLarge.lineHeight
                )
            }
        } else {
            Icon(
                Icons.Outlined.CloudSync,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun StatusLine(
    label: String,
    value: String,
    isError: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Composable
private fun ApiPathLine(path: String) {
    Text(
        text = path,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
    )
}

private fun connectionStateLabel(state: ServerDataSource.ConnectionState): String = when (state) {
    ServerDataSource.ConnectionState.DISCONNECTED -> "未连接"
    ServerDataSource.ConnectionState.CONNECTING -> "连接中"
    ServerDataSource.ConnectionState.CONNECTED -> "已连接"
    ServerDataSource.ConnectionState.ERROR -> "连接失败"
}
