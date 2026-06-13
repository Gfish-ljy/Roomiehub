package com.dorm.health.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dorm.health.data.model.RadarDimension
import com.dorm.health.data.model.TimeRange
import com.dorm.health.ui.components.AppBackground
import com.dorm.health.ui.components.EmptyState
import com.dorm.health.ui.components.GlassCard
import com.dorm.health.ui.components.dividerColor
import com.dorm.health.ui.theme.AppMetricValue
import com.dorm.health.ui.theme.isAppInDarkTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isAppInDarkTheme()
    val swipeState = rememberSwipeRefreshState(uiState.isRefreshing)

    AppBackground(isDark = isDark) {
        SwipeRefresh(state = swipeState, onRefresh = { viewModel.refresh() }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "数据分析",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeRange.entries.forEach { range ->
                        FilterChip(
                            selected = uiState.timeRange == range,
                            onClick = { viewModel.setTimeRange(range) },
                            label = {
                                Text(
                                    range.label,
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
                Spacer(Modifier.height(12.dp))

                if (uiState.tempTrend.isEmpty()) {
                    EmptyState("暂无历史数据，请先采集环境数据", onRetry = { viewModel.refresh() })
                } else {
                    EnvironmentTrendChartCard(TempTrendConfig, uiState.tempTrend, isDark)
                    Spacer(Modifier.height(12.dp))
                    EnvironmentTrendChartCard(HumiTrendConfig, uiState.humiTrend, isDark)
                    Spacer(Modifier.height(12.dp))
                    EnvironmentTrendChartCard(NoiseTrendConfig, uiState.noiseTrend, isDark)
                    Spacer(Modifier.height(12.dp))
                    HealthScoreTrendChartCard(uiState.healthTrend, isDark)
                    Spacer(Modifier.height(12.dp))
                    RadarChartCard(uiState.radarData, isDark)
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun RadarChartCard(dimensions: List<RadarDimension>, isDark: Boolean) {
    GlassCard(isDark = isDark) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "近一周健康雷达",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "四维健康指标综合表现",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = dividerColor(isDark))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val radius = size.minDimension / 3.2f
                    val count = dimensions.size.coerceAtLeast(3)
                    val angleStep = (2 * Math.PI / count).toFloat()

                    for (level in 1..4) {
                        val r = radius * level / 4
                        val path = androidx.compose.ui.graphics.Path()
                        for (i in 0 until count) {
                            val angle = i * angleStep - Math.PI.toFloat() / 2
                            val x = cx + r * cos(angle)
                            val y = cy + r * sin(angle)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(path, Color.Gray.copy(0.2f))
                    }

                    val dataPath = androidx.compose.ui.graphics.Path()
                    dimensions.forEachIndexed { i, dim ->
                        val angle = i * angleStep - Math.PI.toFloat() / 2
                        val r = radius * (dim.value / 100f)
                        val x = cx + r * cos(angle)
                        val y = cy + r * sin(angle)
                        if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                    }
                    dataPath.close()
                    drawPath(dataPath, Color(0xFF6C5CE7).copy(0.35f))
                    drawPath(
                        dataPath,
                        Color(0xFF6C5CE7),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                    )
                }
            }

            HorizontalDivider(color = dividerColor(isDark))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dimensions.forEach { dim ->
                    RadarLegendRow(label = dim.label, value = dim.value.toInt())
                }
            }
        }
    }
}

@Composable
private fun RadarLegendRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6C5CE7))
            )
            Text(
                label,
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            "${value}分",
            style = AppMetricValue.copy(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
