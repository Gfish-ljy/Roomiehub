package com.dorm.health.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.dorm.health.ui.theme.BgGradientEndDark
import com.dorm.health.ui.theme.BgGradientEndLight
import com.dorm.health.ui.theme.BgGradientStartDark
import com.dorm.health.ui.theme.BgGradientStartLight

@Composable
fun AppBackground(isDark: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val gradient = if (isDark) {
        Brush.verticalGradient(listOf(BgGradientStartDark, BgGradientEndDark))
    } else {
        Brush.verticalGradient(listOf(BgGradientStartLight, BgGradientEndLight))
    }
    Box(modifier = modifier.fillMaxSize().background(gradient)) {
        content()
    }
}
