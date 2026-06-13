package com.dorm.health.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dorm.health.DormHealthApp
import com.dorm.health.data.datasource.ServerDataSource
import com.dorm.health.data.model.DeviceStatus
import com.dorm.health.data.model.SensorMode
import com.dorm.health.data.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProfileUiState(
    val nickname: String = "宿舍用户",
    val avatarUri: String? = null,
    val dormName: String = "我的宿舍",
    val joinTime: String = "",
    val studentId: String? = null,
    val username: String? = null,
    val bedNumber: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val refreshInterval: Int = 5,
    val sensorMode: SensorMode = SensorMode.PHONE,
    val deviceStatus: DeviceStatus = DeviceStatus(SensorMode.PHONE),
    val isRefreshing: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val userRepo = DormHealthApp.instance.userRepository
    private val envRepo = DormHealthApp.instance.environmentRepository
    private val prefs = DormHealthApp.instance.preferencesManager
    private val server = DormHealthApp.instance.serverDataSource
    private val authRepo = DormHealthApp.instance.authRepository

    private val _refreshInterval = MutableStateFlow(prefs.refreshIntervalSeconds)
    private val _darkMode = MutableStateFlow(prefs.darkModeOverride)
    private val _notifications = MutableStateFlow(prefs.notificationsEnabled)
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(
        combine(userRepo.userInfo, envRepo.dormInfo, envRepo.sensorMode) { user, dorm, mode ->
            Triple(user, dorm, mode)
        },
        combine(server.connectionState, server.lastError, authRepo.currentUser) { connState, error, mockUser ->
            Triple(connState, error, mockUser)
        },
        combine(_refreshInterval, _darkMode, _notifications, _isRefreshing) { interval, dark, notif, refreshing ->
            listOf(interval, dark, notif, refreshing)
        }
    ) { userDormMode, serverInfo, settings ->
        val (user, dorm, mode) = userDormMode
        val (connState, error, mockUser) = serverInfo
        val interval = settings[0] as Int
        val dark = settings[1] as Boolean?
        val notif = settings[2] as Boolean
        val refreshing = settings[3] as Boolean
        val joinFormatted = user?.joinTime?.let {
            SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(Date(it))
        } ?: ""

        ProfileUiState(
            nickname = mockUser?.nickname ?: user?.nickname ?: "宿舍用户",
            avatarUri = user?.avatarUri,
            dormName = mockUser?.dormName ?: dorm?.dormName ?: "我的宿舍",
            joinTime = joinFormatted,
            studentId = mockUser?.studentId,
            username = mockUser?.username,
            bedNumber = mockUser?.bedNumber,
            themeMode = dark.toThemeMode(),
            notificationsEnabled = notif,
            refreshInterval = interval,
            sensorMode = mode,
            deviceStatus = DeviceStatus(
                mode = mode,
                serverUrl = if (mode == SensorMode.SERVER) prefs.serverBaseUrl else null,
                isConnected = connState == ServerDataSource.ConnectionState.CONNECTED,
                lastError = error
            ),
            isRefreshing = refreshing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun updateNickname(name: String) {
        viewModelScope.launch { userRepo.updateNickname(name) }
    }

    fun updateDormName(name: String) {
        viewModelScope.launch { envRepo.updateDormName(name) }
    }

    fun setThemeMode(mode: ThemeMode) {
        val override = mode.toDarkOverride()
        _darkMode.value = override
        prefs.darkModeOverride = override
    }

    fun setNotifications(enabled: Boolean) {
        _notifications.value = enabled
        prefs.notificationsEnabled = enabled
    }

    fun setRefreshInterval(seconds: Int) {
        _refreshInterval.value = seconds
        prefs.refreshIntervalSeconds = seconds
        envRepo.stopDataCollection()
        envRepo.startDataCollection()
    }

    fun switchSensorMode(mode: SensorMode) {
        viewModelScope.launch { envRepo.switchMode(mode) }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            envRepo.refresh()
            _isRefreshing.value = false
        }
    }

    fun logout() {
        authRepo.logout()
    }
}

private fun Boolean?.toThemeMode(): ThemeMode = when (this) {
    null -> ThemeMode.SYSTEM
    false -> ThemeMode.LIGHT
    true -> ThemeMode.DARK
}

private fun ThemeMode.toDarkOverride(): Boolean? = when (this) {
    ThemeMode.SYSTEM -> null
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
