package com.dorm.health.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dorm.health.DormHealthApp
import com.dorm.health.data.model.ReportDetail
import com.dorm.health.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val reports: List<ReportDetail> = emptyList(),
    val selectedReport: ReportDetail? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

class ReportViewModel : ViewModel() {
    private val reportRepo = DormHealthApp.instance.reportRepository

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            reportRepo.ensureYesterdayReport()
            val reports = reportRepo.getRecentReports()
            _uiState.value = _uiState.value.copy(
                reports = reports,
                isLoading = false,
                selectedReport = reports.firstOrNull()
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val yesterday = DateUtils.yesterday()
            val start = DateUtils.startOfDay(
                java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, -1)
                }.timeInMillis
            )
            reportRepo.generateReportForDay(yesterday, start, start + 86400000 - 1)
            loadReports()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun selectReport(report: ReportDetail) {
        _uiState.value = _uiState.value.copy(selectedReport = report)
    }

    fun deleteReport(date: String) {
        viewModelScope.launch {
            val report = _uiState.value.reports.find { it.date == date } ?: return@launch
            val dbReport = DormHealthApp.instance.appModule.database.dailyReportDao()
                .getByDate(date) ?: return@launch
            reportRepo.deleteReport(dbReport.id)
            loadReports()
        }
    }
}
