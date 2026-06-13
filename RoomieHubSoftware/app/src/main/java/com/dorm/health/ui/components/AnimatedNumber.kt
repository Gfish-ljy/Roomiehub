package com.dorm.health.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.dorm.health.ui.theme.AppMetricValue
import com.dorm.health.ui.theme.AppMetricValueLarge
import com.dorm.health.ui.theme.RobotoMono

@Composable
fun AnimatedNumber(
    value: Float,
    modifier: Modifier = Modifier,
    suffix: String = "",
    decimals: Int = 1,
    style: TextStyle = AppMetricValue
) {
    val animatable = remember { Animatable(value) }
    LaunchedEffect(value) {
        animatable.animateTo(value, animationSpec = tween(500))
    }
    Text(
        text = "%.${decimals}f".format(animatable.value) + suffix,
        style = style.copy(fontFamily = RobotoMono),
        modifier = modifier.fillMaxWidth(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}

@Composable
fun AnimatedIntNumber(
    value: Int,
    modifier: Modifier = Modifier,
    suffix: String = "",
    style: TextStyle = AppMetricValueLarge
) {
    val animatable = remember { Animatable(value.toFloat()) }
    LaunchedEffect(value) {
        animatable.animateTo(value.toFloat(), animationSpec = tween(500))
    }
    Text(
        text = "${animatable.value.toInt()}$suffix",
        style = style.copy(fontFamily = RobotoMono),
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.End
    )
}
