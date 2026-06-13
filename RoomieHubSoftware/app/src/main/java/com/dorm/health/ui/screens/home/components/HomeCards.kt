package com.dorm.health.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dorm.health.data.model.AirQualityLevel
import com.dorm.health.data.model.EnvironmentSnapshot
import com.dorm.health.data.model.HealthLevel
import com.dorm.health.data.model.HealthScore
import com.dorm.health.ui.components.AnimatedIntNumber
import com.dorm.health.ui.components.AnimatedNumber
import com.dorm.health.ui.components.GlassCard
import com.dorm.health.ui.components.HealthScoreRing
import com.dorm.health.ui.theme.Danger
import com.dorm.health.ui.theme.SuccessLight
import com.dorm.health.ui.theme.WarningLight

@Composable
fun DormInfoCard(
    dormName: String,
    memberCount: Int,
    currentTime: String,
    healthLevel: HealthLevel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val levelColor = when (healthLevel) {
        HealthLevel.EXCELLENT -> SuccessLight
        HealthLevel.GOOD -> SuccessLight
        HealthLevel.FAIR -> WarningLight
        HealthLevel.POOR -> Danger
    }
    GlassCard(modifier = modifier, isDark = isDark) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Home,
                contentDescription = null,
                tint = levelColor,
                modifier = Modifier.size(40.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    dormName,
                    style = MaterialTheme.typography.headlineLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "在线成员 $memberCount 人 · $currentTime",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "整体状态：${healthLevel.label}",
                    color = levelColor,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EnvironmentMetricCard(
    icon: ImageVector,
    label: String,
    value: Float,
    unit: String,
    isAbnormal: Boolean,
    suggestion: String?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, isDark = isDark, isAlert = isAbnormal) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isAbnormal) Danger else SuccessLight,
                modifier = Modifier.size(28.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            AnimatedNumber(
                value = value,
                suffix = unit,
                decimals = if (unit == "dB" || unit == "lux") 0 else 1
            )
            AnimatedVisibility(visible = suggestion != null, enter = fadeIn() + scaleIn()) {
                suggestion?.let {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun EnvironmentGrid(snapshot: EnvironmentSnapshot, isDark: Boolean) {
    val airLevel = AirQualityLevel.fromAqi(snapshot.aqi)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            EnvironmentMetricCard(
                Icons.Outlined.Thermostat, "温度", snapshot.temp, "°C",
                snapshot.temp > 30 || snapshot.temp < 18,
                if (snapshot.temp > 30) "注意降温" else null,
                isDark, Modifier.weight(1f).fillMaxHeight()
            )
            EnvironmentMetricCard(
                Icons.Outlined.WaterDrop, "湿度", snapshot.humi, "%",
                snapshot.humi > 70 || snapshot.humi < 35,
                if (snapshot.humi > 70) "建议除湿" else null,
                isDark, Modifier.weight(1f).fillMaxHeight()
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            EnvironmentMetricCard(
                Icons.Outlined.VolumeUp, "噪音", snapshot.noise, "dB",
                snapshot.noise > 70,
                if (snapshot.noise > 70) "噪音过高" else null,
                isDark, Modifier.weight(1f).fillMaxHeight()
            )
            EnvironmentMetricCard(
                Icons.Outlined.LightMode, "光照", snapshot.light, "lux",
                snapshot.light > 500,
                null, isDark, Modifier.weight(1f).fillMaxHeight()
            )
        }
        GlassCard(isDark = isDark, isAlert = snapshot.aqi > 150) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Air,
                    contentDescription = null,
                    tint = if (snapshot.aqi > 150) Danger else SuccessLight,
                    modifier = Modifier.size(28.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    Text(
                        "空气质量",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        airLevel.label,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AnimatedIntNumber(snapshot.aqi, suffix = " AQI")
            }
        }
    }
}

@Composable
fun HealthScoreCard(score: HealthScore, rankingPercent: Int, isDark: Boolean) {
    GlassCard(isDark = isDark) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("健康评分", style = MaterialTheme.typography.titleLarge)
            HealthScoreRing(score.total)
            Text(
                "状态：${score.level.label}",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                "本周超越 $rankingPercent% 宿舍",
                color = Color(0xFF6C5CE7),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SuggestionCard(suggestions: List<String>, isDark: Boolean) {
    val items = suggestions.ifEmpty { listOf("环境状态良好，继续保持健康作息") }
    GlassCard(isDark = isDark) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "智能建议",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.forEach { suggestion ->
                    SuggestionRow(suggestion)
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(text: String) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = textStyle,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 1.dp)
        )
        Text(
            text = text,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            lineHeight = textStyle.lineHeight
        )
    }
}

@Composable
fun HomeActionButtons(
    onRefresh: () -> Unit,
    onAnalytics: () -> Unit,
    onReport: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val buttonPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minWidth = 0.dp),
            contentPadding = buttonPadding
        ) {
            Text("刷新", maxLines = 1, style = MaterialTheme.typography.labelLarge)
        }
        Button(
            onClick = onAnalytics,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minWidth = 0.dp),
            contentPadding = buttonPadding
        ) {
            Text("查看分析", maxLines = 1, style = MaterialTheme.typography.labelLarge)
        }
        Button(
            onClick = onReport,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minWidth = 0.dp),
            contentPadding = buttonPadding
        ) {
            Text("健康报告", maxLines = 1, style = MaterialTheme.typography.labelLarge)
        }
    }
}
