package com.dorm.health.ui.screens.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dorm.health.data.model.ReportDetail
import com.dorm.health.ui.components.AppBackground
import com.dorm.health.ui.components.EmptyState
import com.dorm.health.ui.components.GlassCard
import com.dorm.health.ui.components.dividerColor
import com.dorm.health.ui.theme.isAppInDarkTheme
import com.dorm.health.utils.DateUtils
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isAppInDarkTheme()
    val swipeState = rememberSwipeRefreshState(uiState.isRefreshing)

    AppBackground(isDark = isDark) {
        SwipeRefresh(state = swipeState, onRefresh = { viewModel.refresh() }) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("健康报告", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))

                uiState.selectedReport?.let { ReportDetailCard(it, isDark) }
                Spacer(Modifier.height(12.dp))

                if (uiState.reports.isEmpty()) {
                    EmptyState("暂无报告，采集数据后将自动生成", onRetry = { viewModel.refresh() })
                } else {
                    Text("近一周报告", style = MaterialTheme.typography.titleLarge)
                    LazyColumn {
                        items(uiState.reports, key = { it.date }) { report ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteReport(report.date)
                                        true
                                    } else false
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {},
                                enableDismissFromStartToEnd = false
                            ) {
                                ReportListItem(
                                    report = report,
                                    isSelected = uiState.selectedReport?.date == report.date,
                                    isDark = isDark,
                                    onClick = { viewModel.selectReport(report) }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun ReportDetailCard(report: ReportDetail, isDark: Boolean) {
    GlassCard(isDark = isDark) {
        Text(DateUtils.formatDisplayDate(report.date), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("综合评分：${report.avgScore} 分", style = MaterialTheme.typography.titleLarge)
        Text("平均休息时间：${report.avgRestTime}", style = MaterialTheme.typography.bodyLarge)
        Text("深夜噪音次数：${report.nightNoiseCount}", style = MaterialTheme.typography.bodyLarge)
        Text(
            "空气质量优良占比：${"%.1f".format(report.airQualityAvg)}%",
            style = MaterialTheme.typography.bodyLarge
        )
        Text("趋势：${report.trend}", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "作息 ${report.sleepScore} · 舒适 ${report.comfortScore} · 噪音 ${report.noiseScore} · 空气 ${report.airScore}",
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}

@Composable
private fun ReportListItem(
    report: ReportDetail,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            "${DateUtils.formatDisplayDate(report.date)} - ${report.avgScore}分",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(report.trend, style = MaterialTheme.typography.labelSmall)
        HorizontalDivider(color = dividerColor(isDark), modifier = Modifier.padding(top = 8.dp))
    }
}
