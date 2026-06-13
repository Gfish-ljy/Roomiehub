package com.dorm.health.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorm.health.data.model.HealthLevel
import com.dorm.health.data.model.TrendPoint
import com.dorm.health.ui.components.GlassCard
import com.dorm.health.ui.components.dividerColor
import com.dorm.health.ui.theme.AppMetricValue
import com.dorm.health.ui.theme.Danger
import com.dorm.health.ui.theme.PrimaryDark
import com.dorm.health.ui.theme.PrimaryLight
import com.dorm.health.ui.theme.SuccessDark
import com.dorm.health.ui.theme.SuccessLight
import com.dorm.health.ui.theme.WarningDark
import com.dorm.health.ui.theme.WarningLight
import com.dorm.health.utils.DateUtils
import kotlin.math.roundToInt
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlin.math.abs
import kotlin.math.max

internal data class EnvTrendConfig(
    val title: String,
    val unit: String,
    val lineColor: Color,
    val decimals: Int = 1,
    val minFloor: Float = 0f
)

internal val TempTrendConfig = EnvTrendConfig(
    title = "温度趋势",
    unit = "°C",
    lineColor = Color(0xFFFF6B6B),
    decimals = 1,
    minFloor = 0f
)

internal val HumiTrendConfig = EnvTrendConfig(
    title = "湿度趋势",
    unit = "%",
    lineColor = Color(0xFF4ECDC4),
    decimals = 0,
    minFloor = 0f
)

internal val NoiseTrendConfig = EnvTrendConfig(
    title = "噪音趋势",
    unit = "dB",
    lineColor = Color(0xFF6C5CE7),
    decimals = 0,
    minFloor = 0f
)

@Composable
internal fun HealthScoreTrendChartCard(
    data: List<TrendPoint>,
    isDark: Boolean
) {
    val fontScale = LocalConfiguration.current.fontScale
    val useStackedLayout = fontScale >= 1.15f
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    val lineColor = if (isDark) PrimaryDark else PrimaryLight
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.18f else 0.12f)

    val values = data.map { it.value }
    val current = values.lastOrNull()?.roundToInt()
    val average = values.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
    val minValue = values.minOrNull()?.roundToInt()
    val maxValue = values.maxOrNull()?.roundToInt()
    val currentLevel = current?.let { HealthLevel.fromScore(it) }
    val delta = if (data.size >= 2) {
        (values.last() - values.first()).roundToInt()
    } else {
        null
    }

    GlassCard(isDark = isDark) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "健康评分趋势",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = if (data.isEmpty()) {
                        "基于每日健康报告汇总"
                    } else {
                        "近 ${data.size} 日评分 · 满分 100"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (data.isNotEmpty() && current != null && average != null && minValue != null && maxValue != null) {
                if (useStackedLayout) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HealthScoreStatBlock(
                            label = "当前评分",
                            value = "$current",
                            suffix = "分",
                            accent = currentLevel?.let { healthLevelColor(it, isDark) } ?: lineColor,
                            badge = currentLevel?.label
                        )
                        HealthScoreStatBlock(
                            label = "平均分",
                            value = "$average",
                            suffix = "分",
                            accent = MaterialTheme.colorScheme.onSurface
                        )
                        HealthScoreStatBlock(
                            label = "区间",
                            value = "$minValue ~ $maxValue",
                            suffix = "分",
                            accent = MaterialTheme.colorScheme.onSurface
                        )
                        delta?.let { HealthScoreTrendHint(delta = it, labelColor = labelColor) }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HealthScoreStatCell(
                            label = "当前",
                            value = "$current",
                            suffix = "分",
                            accent = currentLevel?.let { healthLevelColor(it, isDark) } ?: lineColor,
                            badge = currentLevel?.label,
                            modifier = Modifier.weight(1f)
                        )
                        HealthScoreStatCell(
                            label = "平均",
                            value = "$average",
                            suffix = "分",
                            accent = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        HealthScoreStatCell(
                            label = "区间",
                            value = "$minValue~$maxValue",
                            suffix = "分",
                            accent = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            compactValue = true
                        )
                    }
                    delta?.let { HealthScoreTrendHint(delta = it, labelColor = labelColor) }
                }
            }

            HorizontalDivider(color = dividerColor(isDark))

            if (data.isEmpty()) {
                Text(
                    text = "暂无报告数据，使用 APP 后将自动生成每日健康评分",
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelColor,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                HealthScoreTrendChart(
                    data = data,
                    lineColor = lineColor,
                    labelColor = labelColor,
                    gridColor = gridColor,
                    fontScale = fontScale
                )
                HealthScoreLevelLegend(useStackedLayout = useStackedLayout, labelColor = labelColor)
            }
        }
    }
}

@Composable
private fun HealthScoreStatCell(
    label: String,
    value: String,
    suffix: String,
    accent: Color,
    modifier: Modifier = Modifier,
    badge: String? = null,
    compactValue: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = value + suffix,
            style = AppMetricValue.copy(
                fontSize = if (compactValue) 13.sp else 15.sp,
                lineHeight = if (compactValue) 16.sp else 18.sp,
                color = accent
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        badge?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HealthScoreStatBlock(
    label: String,
    value: String,
    suffix: String,
    accent: Color,
    badge: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.widthIn(min = 72.dp)
        ) {
            Text(
                text = value + suffix,
                style = AppMetricValue.copy(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    lineHeight = MaterialTheme.typography.titleMedium.lineHeight,
                    color = accent
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            badge?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HealthScoreTrendHint(delta: Int, labelColor: Color) {
    val text = when {
        delta > 0 -> "较首期 +$delta 分，整体上升"
        delta < 0 -> "较首期 $delta 分，整体下降"
        else -> "较首期持平"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = labelColor,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HealthScoreLevelLegend(useStackedLayout: Boolean, labelColor: Color) {
    val items = listOf("优秀 ≥85", "良好 ≥70", "一般 ≥55", "需改善 <55")
    if (useStackedLayout) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        Text(
            text = items.joinToString("  ·  "),
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = MaterialTheme.typography.labelSmall.lineHeight,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HealthScoreTrendChart(
    data: List<TrendPoint>,
    lineColor: Color,
    labelColor: Color,
    gridColor: Color,
    fontScale: Float
) {
    val timestamps = remember(data) { data.map { it.timestamp } }
    val axisTextSize = if (fontScale >= 1.15f) 8.sp else 9.sp
    val axisWidth = if (fontScale >= 1.15f) 52f else 44f
    val showPoints = data.size <= 14

    val axisLabel = axisLabelComponent(color = labelColor, textSize = axisTextSize)

    val startAxis = rememberStartAxis(
        label = axisLabel,
        valueFormatter = AxisValueFormatter { value, _ ->
            value.roundToInt().toString()
        },
        tickLength = 4.dp,
        guideline = axisGuidelineComponent(color = gridColor),
        itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 5) },
        sizeConstraint = Axis.SizeConstraint.Exact(axisWidth)
    )

    val bottomAxis = rememberBottomAxis(
        label = axisLabel,
        valueFormatter = AxisValueFormatter { value, _ ->
            val index = value.toInt()
            if (index in timestamps.indices) DateUtils.formatShortDate(timestamps[index]) else ""
        },
        tickLength = 4.dp,
        guideline = null,
        itemPlacer = remember(data.size) {
            AxisItemPlacer.Horizontal.default(
                spacing = 1,
                addExtremeLabelPadding = true
            )
        },
        sizeConstraint = Axis.SizeConstraint.Exact(if (fontScale >= 1.15f) 28f else 22f)
    )

    val yOverrider = remember {
        object : AxisValuesOverrider<ChartEntryModel> {
            override fun getMinY(model: ChartEntryModel): Float = 0f
            override fun getMaxY(model: ChartEntryModel): Float = 100f
        }
    }

    val line = lineSpec(
        lineColor = lineColor,
        lineThickness = 2.5.dp,
        lineBackgroundShader = DynamicShaders.fromBrush(
            Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.30f),
                    lineColor.copy(alpha = 0.06f),
                    Color.Transparent
                )
            )
        ),
        point = if (showPoints) {
            shapeComponent(shape = Shapes.pillShape, color = lineColor)
        } else {
            null
        },
        pointSize = if (showPoints) 6.dp else 0.dp,
        pointConnector = DefaultPointConnector(cubicStrength = 0.18f)
    )

    val producer = remember { ChartEntryModelProducer() }
    LaunchedEffect(data) {
        producer.setEntries(data.mapIndexed { index, point -> entryOf(index.toFloat(), point.value) })
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (fontScale >= 1.15f) 248.dp else 228.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        ProvideChartStyle(m3ChartStyle()) {
            Chart(
                chart = lineChart(
                    lines = listOf(line),
                    axisValuesOverrider = yOverrider,
                    spacing = when {
                        data.size <= 4 -> 24.dp
                        data.size <= 8 -> 16.dp
                        else -> 8.dp
                    }
                ),
                chartModelProducer = producer,
                startAxis = startAxis,
                bottomAxis = bottomAxis,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 2.dp, top = 8.dp, end = 10.dp, bottom = 4.dp)
            )
        }
    }
}

private fun healthLevelColor(level: HealthLevel, isDark: Boolean): Color = when (level) {
    HealthLevel.EXCELLENT -> if (isDark) SuccessDark else SuccessLight
    HealthLevel.GOOD -> if (isDark) PrimaryDark else PrimaryLight
    HealthLevel.FAIR -> if (isDark) WarningDark else WarningLight
    HealthLevel.POOR -> Danger
}

@Composable
internal fun EnvironmentTrendChartCard(
    config: EnvTrendConfig,
    data: List<TrendPoint>,
    isDark: Boolean
) {
    val values = data.map { it.value }
    val current = values.lastOrNull()
    val average = values.takeIf { it.isNotEmpty() }?.average()?.toFloat()
    val minValue = values.minOrNull()
    val maxValue = values.maxOrNull()
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.18f else 0.12f)

    GlassCard(isDark = isDark) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    config.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "单位：${config.unit.trim()} · 共 ${data.size} 个采样点",
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelColor,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (data.isNotEmpty() && current != null && average != null && minValue != null && maxValue != null) {
                val useStackedStats = LocalConfiguration.current.fontScale >= 1.15f
                if (useStackedStats) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrendStatBlock("当前", formatTrendValue(current, config.decimals), config.unit, config.lineColor)
                        TrendStatBlock("平均", formatTrendValue(average, config.decimals), config.unit, MaterialTheme.colorScheme.onSurface)
                        TrendStatBlock(
                            "区间",
                            "${formatTrendValue(minValue, config.decimals)} ~ ${formatTrendValue(maxValue, config.decimals)}",
                            config.unit,
                            MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrendStatCell(
                            label = "当前",
                            value = formatTrendValue(current, config.decimals),
                            unit = config.unit,
                            accent = config.lineColor,
                            modifier = Modifier.weight(1f)
                        )
                        TrendStatCell(
                            label = "平均",
                            value = formatTrendValue(average, config.decimals),
                            unit = config.unit,
                            accent = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        TrendRangeStatCell(
                            label = "区间",
                            minValue = formatTrendValue(minValue, config.decimals),
                            maxValue = formatTrendValue(maxValue, config.decimals),
                            unit = config.unit,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            HorizontalDivider(color = dividerColor(isDark))

            if (data.isEmpty()) {
                Text("无数据", style = MaterialTheme.typography.bodyLarge)
            } else {
                RefinementTrendChart(
                    config = config,
                    data = data,
                    labelColor = labelColor,
                    gridColor = gridColor
                )
            }
        }
    }
}

@Composable
private fun TrendStatBlock(
    label: String,
    value: String,
    unit: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (unit.isBlank()) value else "$value$unit",
            style = AppMetricValue.copy(
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                lineHeight = MaterialTheme.typography.titleMedium.lineHeight,
                color = accent
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(min = 72.dp)
        )
    }
}

@Composable
private fun TrendStatCell(
    label: String,
    value: String,
    unit: String,
    accent: Color,
    modifier: Modifier = Modifier,
    valueFontSize: androidx.compose.ui.unit.TextUnit = 15.sp
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = if (unit.isBlank()) value else "$value$unit",
            style = AppMetricValue.copy(
                fontSize = valueFontSize,
                lineHeight = 18.sp,
                color = accent
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TrendRangeStatCell(
    label: String,
    minValue: String,
    maxValue: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            minValue + unit,
            style = AppMetricValue.copy(
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "~",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            textAlign = TextAlign.Center
        )
        Text(
            maxValue + unit,
            style = AppMetricValue.copy(
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RefinementTrendChart(
    config: EnvTrendConfig,
    data: List<TrendPoint>,
    labelColor: Color,
    gridColor: Color
) {
    val fontScale = LocalConfiguration.current.fontScale
    val timestamps = remember(data) { data.map { it.timestamp } }
    val minVal = remember(data) { data.minOf { it.value } }
    val maxVal = remember(data) { data.maxOf { it.value } }
    val yPadding = remember(data) {
        val span = (maxVal - minVal).takeIf { it > 0.001f } ?: 1f
        span * 0.12f
    }
    val showPoints = data.size <= 20
    val axisTextSize = if (fontScale >= 1.15f) 8.sp else 9.sp
    val axisWidth = if (fontScale >= 1.15f) 48f else 38f

    val axisLabel = axisLabelComponent(
        color = labelColor,
        textSize = axisTextSize
    )

    val startAxis = rememberStartAxis(
        label = axisLabel,
        valueFormatter = AxisValueFormatter { value, _ ->
            formatAxisTick(value, config.decimals)
        },
        tickLength = 4.dp,
        guideline = axisGuidelineComponent(color = gridColor),
        itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 4) },
        sizeConstraint = Axis.SizeConstraint.Exact(axisWidth)
    )

    val bottomAxis = rememberBottomAxis(
        label = axisLabel,
        valueFormatter = AxisValueFormatter { _, _ -> "" },
        tickLength = 0.dp,
        guideline = null,
        itemPlacer = remember { AxisItemPlacer.Horizontal.default(spacing = 1, addExtremeLabelPadding = false) },
        sizeConstraint = Axis.SizeConstraint.Exact(4f)
    )

    val yOverrider = remember(minVal, maxVal, yPadding, config.minFloor) {
        object : AxisValuesOverrider<ChartEntryModel> {
            override fun getMinY(model: ChartEntryModel): Float? =
                max(config.minFloor, minVal - yPadding)

            override fun getMaxY(model: ChartEntryModel): Float? =
                maxVal + yPadding
        }
    }

    val line = lineSpec(
        lineColor = config.lineColor,
        lineThickness = 2.5.dp,
        lineBackgroundShader = DynamicShaders.fromBrush(
            Brush.verticalGradient(
                colors = listOf(
                    config.lineColor.copy(alpha = 0.28f),
                    config.lineColor.copy(alpha = 0.04f),
                    Color.Transparent
                )
            )
        ),
        point = if (showPoints) {
            shapeComponent(shape = Shapes.pillShape, color = config.lineColor)
        } else {
            null
        },
        pointSize = if (showPoints) 5.dp else 0.dp,
        pointConnector = DefaultPointConnector(cubicStrength = 0.22f)
    )

    val producer = remember { ChartEntryModelProducer() }
    LaunchedEffect(data) {
        producer.setEntries(data.mapIndexed { index, point -> entryOf(index.toFloat(), point.value) })
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(236.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        ProvideChartStyle(m3ChartStyle()) {
            Chart(
                chart = lineChart(
                    lines = listOf(line),
                    axisValuesOverrider = yOverrider,
                    spacing = if (data.size <= 8) 16.dp else 4.dp
                ),
                chartModelProducer = producer,
                startAxis = startAxis,
                bottomAxis = bottomAxis,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 2.dp)
            )
        }
    }

    if (data.size >= 2) {
        if (fontScale >= 1.15f) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "起始 ${DateUtils.formatTime(timestamps.first())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "最新 ${DateUtils.formatTime(timestamps.last())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    DateUtils.formatTime(timestamps.first()),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    "→",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    DateUtils.formatTime(timestamps.last()),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}

private fun formatAxisTick(value: Float, decimals: Int): String {
    if (abs(value - value.toInt().toFloat()) < 0.05f) return "${value.toInt()}"
    return "%.${decimals}f".format(value)
}

internal fun formatTrendValue(value: Float, decimals: Int = 1): String =
    if (decimals == 0 || abs(value - value.toInt().toFloat()) < 0.05f) {
        "${value.toInt()}"
    } else {
        "%.${decimals}f".format(value)
    }
