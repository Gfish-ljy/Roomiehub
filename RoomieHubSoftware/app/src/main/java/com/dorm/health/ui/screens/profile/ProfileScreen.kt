package com.dorm.health.ui.screens.profile

import com.dorm.health.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dorm.health.data.model.SensorMode
import com.dorm.health.data.model.ThemeMode
import com.dorm.health.ui.components.AppBackground
import com.dorm.health.ui.components.GlassCard
import com.dorm.health.ui.components.dividerColor
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun ProfileScreen(
    onNavigateServerConnection: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isAppInDarkTheme()
    val swipeState = rememberSwipeRefreshState(uiState.isRefreshing)
    var nickname by remember(uiState.nickname) { mutableStateOf(uiState.nickname) }
    var dormName by remember(uiState.dormName) { mutableStateOf(uiState.dormName) }

    AppBackground(isDark = isDark) {
        SwipeRefresh(state = swipeState, onRefresh = { viewModel.refresh() }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "我的",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                GlassCard(isDark = isDark) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "用户端账号",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        uiState.studentId?.let { ProfileInfoLine("学号：$it") }
                        uiState.username?.let { ProfileInfoLine("用户名：$it") }
                        uiState.bedNumber?.let { ProfileInfoLine("床位：$it") }
                        ProfileInfoLine("宿舍：${uiState.dormName}")
                        TextButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("退出登录", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                ModeSwitchCard(
                    currentMode = uiState.sensorMode,
                    onModeChange = { viewModel.switchSensorMode(it) },
                    isDark = isDark
                )

                GlassCard(isDark = isDark) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.avatarUri != null) {
                            AsyncImage(
                                model = uiState.avatarUri,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp)
                            )
                        }
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("昵称", maxLines = 1) },
                            placeholder = { Text("请输入昵称", maxLines = 1) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextButton(
                            onClick = { viewModel.updateNickname(nickname) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("保存昵称", style = MaterialTheme.typography.labelLarge)
                        }
                        Text(
                            "加入时间：${uiState.joinTime}",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                GlassCard(isDark = isDark) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("宿舍设置", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(
                            value = dormName,
                            onValueChange = { dormName = it },
                            label = { Text("宿舍名称", maxLines = 1) },
                            placeholder = { Text("请输入宿舍名称", maxLines = 1) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextButton(
                            onClick = { viewModel.updateDormName(dormName) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("保存宿舍名称", style = MaterialTheme.typography.labelLarge)
                        }
                        HorizontalDivider(color = dividerColor(isDark))
                        ProfileActionButton("添加成员（开发中）", enabled = false, onClick = {})
                        ProfileActionButton("服务器连接", onClick = onNavigateServerConnection)
                        val status = uiState.deviceStatus
                        ProfileInfoLine(
                            "当前模式：${if (status.mode == SensorMode.PHONE) "手机传感器" else "Spring Boot 服务器"}"
                        )
                        status.serverUrl?.let {
                            ProfileInfoLine("服务器：$it")
                        }
                        status.lastError?.let {
                            ProfileInfoLine("连接错误：$it")
                        }
                    }
                }

                GlassCard(isDark = isDark) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("系统设置", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        ThemeModeSelector(
                            currentMode = uiState.themeMode,
                            onModeChange = { viewModel.setThemeMode(it) }
                        )
                        SettingRow("消息提醒", Icons.Outlined.Notifications) {
                            Switch(
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = { viewModel.setNotifications(it) }
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "刷新频率",
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 10, 30).forEach { sec ->
                                    FilterChip(
                                        selected = uiState.refreshInterval == sec,
                                        onClick = { viewModel.setRefreshInterval(sec) },
                                        label = {
                                            Text(
                                                "${sec}s",
                                                modifier = Modifier.fillMaxWidth(),
                                                maxLines = 1,
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = dividerColor(isDark))
                        Text(
                            "关于项目：RoomieHub 宿舍健康管理 APP，支持手机传感器与 Spring Boot 服务器双模式。",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ModeSwitchCard(
    currentMode: SensorMode,
    onModeChange: (SensorMode) -> Unit,
    isDark: Boolean
) {
    GlassCard(isDark = isDark) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("数据源模式", style = MaterialTheme.typography.titleLarge)
            FilterChip(
                selected = currentMode == SensorMode.PHONE,
                onClick = { onModeChange(SensorMode.PHONE) },
                label = {
                    Text(
                        "手机传感器",
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            FilterChip(
                selected = currentMode == SensorMode.SERVER,
                onClick = { onModeChange(SensorMode.SERVER) },
                label = {
                    Text(
                        "Spring Boot 服务器",
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProfileInfoLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
    )
}

@Composable
private fun ProfileActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ThemeModeSelector(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.DarkMode, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                "外观主题",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = currentMode == mode,
                    onClick = { onModeChange(mode) },
                    label = {
                        Text(
                            mode.label,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (currentMode == ThemeMode.SYSTEM) {
            Text(
                "已跟随系统设置，修改手机深浅色模式后自动切换",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Text(
            title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.widthIn(min = 52.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            trailing()
        }
    }
}
