package com.dorm.health.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dorm.health.DormHealthApp
import com.dorm.health.data.model.RadarDimension
import com.dorm.health.data.model.TimeRange
import com.dorm.health.data.model.TrendPoint
import com.dorm.health.utils.DateUtils
import com.dorm.health.utils.ScoreCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val timeRange: TimeRange = TimeRange.DAY,
    val tempTrend: List<TrendPoint> = emptyList(),
    val humiTrend: List<TrendPoint> = emptyList(),
    val noiseTrend: List<TrendPoint> = emptyList(),
    val lightTrend: List<TrendPoint> = emptyList(),
    val aqiTrend: List<TrendPoint> = emptyList(),
    val healthTrend: List<TrendPoint> = emptyList(),
    val radarData: List<RadarDimension> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedPoint: String? = null
)

class AnalyticsViewModel : ViewModel() {
    private val envRepo = DormHealthApp.instance.environmentRepository
    private val reportRepo = DormHealthApp.instance.reportRepository

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setTimeRange(range: TimeRange) {
        _uiState.value = _uiState.value.copy(timeRange = range)
        loadData()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun onChartPointSelected(label: String) {
        _uiState.value = _uiState.value.copy(selectedPoint = label)
    }

    private fun loadData() {
        viewModelScope.launch {
            val range = _uiState.value.timeRange
            val from = DateUtils.rangeStart(range)
            val records = envRepo.getRecordsForRange(from)

            val tempTrend = records.map { TrendPoint(it.timestamp, it.temp) }
            val humiTrend = records.map { TrendPoint(it.timestamp, it.humi) }
            val noiseTrend = records.map { TrendPoint(it.timestamp, it.noise) }
            val lightTrend = records.map { TrendPoint(it.timestamp, it.light) }
            val aqiTrend = records.map { TrendPoint(it.timestamp, it.aqi.toFloat()) }

            val weekRecords = envRepo.getRecordsForRange(DateUtils.daysAgo(7))
            val score = ScoreCalculator.calculateRealtimeScore(weekRecords)
            val radar = listOf(
                RadarDimension("作息", score.sleep.toFloat()),
                RadarDimension("舒适", score.comfort.toFloat()),
                RadarDimension("噪音", score.noise.toFloat()),
                RadarDimension("空气", score.air.toFloat())
            )

            val reports = reportRepo.getRecentReports()
            val healthTrend = reports.reversed().map {
                TrendPoint(
                    DateUtils.startOfDay(
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .parse(it.date)?.time ?: 0
                    ),
                    it.avgScore.toFloat()
                )
            }

            _uiState.value = _uiState.value.copy(
                tempTrend = tempTrend,
                humiTrend = humiTrend,
                noiseTrend = noiseTrend,
                lightTrend = lightTrend,
                aqiTrend = aqiTrend,
                healthTrend = healthTrend,
                radarData = radar,
                isLoading = false
            )
        }
    }
}
