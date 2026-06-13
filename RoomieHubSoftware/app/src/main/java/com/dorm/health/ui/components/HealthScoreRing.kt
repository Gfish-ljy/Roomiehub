package com.dorm.health.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dorm.health.ui.theme.AppScoreRingValue
import com.dorm.health.ui.theme.Danger
import com.dorm.health.ui.theme.SuccessLight
import com.dorm.health.ui.theme.WarningLight

@Composable
fun HealthScoreRing(
    score: Int,
    modifier: Modifier = Modifier
) {
    val sweep = score / 100f * 360f
    val gradient = Brush.sweepGradient(
        colors = listOf(SuccessLight, WarningLight, Danger, SuccessLight)
    )

    Box(modifier = modifier.size(140.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(140.dp)) {
            drawArc(
                color = androidx.compose.ui.graphics.Color.LightGray.copy(0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                brush = gradient,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        AnimatedIntNumber(value = score, suffix = "分", style = AppScoreRingValue)
    }
}
