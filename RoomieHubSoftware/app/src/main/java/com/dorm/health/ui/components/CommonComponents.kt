package com.dorm.health.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.dorm.health.ui.theme.CardDark
import com.dorm.health.ui.theme.CardLight
import com.dorm.health.ui.theme.DividerDark
import com.dorm.health.ui.theme.DividerLight
import com.dorm.health.ui.theme.isAppInDarkTheme

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme(),
    isAlert: Boolean = false,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alert")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAlert) 0.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val bgColor = if (isDark) CardDark else CardLight
    val gloss = if (isDark) {
        Brush.verticalGradient(listOf(Color.White.copy(0.05f), Color.Transparent))
    } else {
        Brush.verticalGradient(listOf(Color.White.copy(0.3f), Color.Transparent))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .background(gloss)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "offset"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.LightGray.copy(0.3f),
                        Color.LightGray.copy(0.6f),
                        Color.LightGray.copy(0.3f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(offset, 0f),
                    end = androidx.compose.ui.geometry.Offset(offset + 300f, 300f)
                )
            )
    )
}

@Composable
fun EmptyState(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(text = "📭", style = MaterialTheme.typography.displayLarge)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        onRetry?.let {
            Button(onClick = it) { Text("重试") }
        }
    }
}

@Composable
fun dividerColor(isDark: Boolean): Color = if (isDark) DividerDark else DividerLight
