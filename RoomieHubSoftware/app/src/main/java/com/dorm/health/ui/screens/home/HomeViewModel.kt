package com.dorm.health.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dorm.health.DormHealthApp
import com.dorm.health.data.database.entities.DormInfo
import kotlinx.coroutines.flow.combine
import com.dorm.health.data.model.EnvironmentSnapshot
import com.dorm.health.data.model.HealthLevel
import com.dorm.health.data.model.HealthScore
import com.dorm.health.data.model.SensorMode
import com.dorm.health.utils.ScoreCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val dormName: String = "我的宿舍",
    val memberCount: Int = 1,
    val currentTime: String = "",
    val healthLevel: HealthLevel = HealthLevel.GOOD,
    val snapshot: EnvironmentSnapshot = EnvironmentSnapshot(),
    val healthScore: HealthScore = HealthScore(),
    val suggestions: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val rankingPercent: Int = 75,
    val isNightOwl: Boolean = false,
    val sensorModeLabel: String = "手机传感器",
    val welcomeMessage: String? = null
)

class HomeViewModel : ViewModel() {
    private val repo = DormHealthApp.instance.environmentRepository
    private val prefs = DormHealthApp.instance.preferencesManager
    private val notifications = DormHealthApp.instance.notificationHelper
    private val authRepo = DormHealthApp.instance.authRepository

    private val _isRefreshing = MutableStateFlow(false)
    private val _currentTime = MutableStateFlow(formatTime())

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            repo.currentSnapshot,
            repo.healthScore,
            repo.suggestions,
            repo.dormInfo
        ) { snapshot, score, suggestions, dorm ->
            listOf(snapshot, score, suggestions, dorm)
        },
        combine(repo.isNightOwl, repo.sensorMode, _isRefreshing, _currentTime, authRepo.currentUser) {
                isNightOwl, mode, refreshing, time, user ->
            listOf(isNightOwl, mode, refreshing, time, user)
        }
    ) { a, b ->
        @Suppress("UNCHECKED_CAST")
        val snapshot = a[0] as EnvironmentSnapshot
        val score = a[1] as HealthScore
        val suggestions = a[2] as List<String>
        val dorm = a[3] as DormInfo?
        val isNightOwl = b[0] as Boolean
        val mode = b[1] as SensorMode
        val refreshing = b[2] as Boolean
        val time = b[3] as String
        val user = b[4] as com.dorm.health.data.model.MockUserAccount?
        HomeUiState(
            dormName = user?.dormName ?: dorm?.dormName ?: "我的宿舍",
            memberCount = dorm?.memberCount ?: 1,
            currentTime = time,
            healthLevel = score.level,
            snapshot = snapshot,
            healthScore = score,
            suggestions = suggestions,
            isLoading = false,
            isRefreshing = refreshing,
            rankingPercent = ScoreCalculator.fakeRankingPercent(score.total),
            isNightOwl = isNightOwl,
            sensorModeLabel = if (mode == SensorMode.PHONE) "手机传感器" else "Spring Boot 服务器",
            welcomeMessage = user?.let { "你好，${it.nickname}（${it.dormName}）" }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        viewModelScope.launch {
            repo.alertEvents.collect { event ->
                if (prefs.notificationsEnabled) {
                    notifications.sendAlert("环境提醒", event.message)
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTime.value = formatTime()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repo.refresh()
            _isRefreshing.value = false
        }
    }

    private fun formatTime(): String =
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}
