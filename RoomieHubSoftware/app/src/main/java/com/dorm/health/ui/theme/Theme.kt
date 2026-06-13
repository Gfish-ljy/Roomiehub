package com.dorm.health.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** 当前应用实际使用的深浅色状态（含手动切换） */
val LocalAppDarkTheme = staticCompositionLocalOf { false }

@Composable
fun isAppInDarkTheme(): Boolean = LocalAppDarkTheme.current

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    secondary = SuccessLight,
    onSecondary = Color.White,
    tertiary = WarningLight,
    onTertiary = TextPrimaryLight,
    error = Danger,
    onError = Color.White,
    background = BgGradientStartLight,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = BgGradientEndLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0x33000000),
    inverseSurface = Color(0xFF2D3436),
    inverseOnSurface = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF1E1E1E),
    secondary = SuccessDark,
    onSecondary = Color(0xFF1E1E1E),
    tertiary = WarningDark,
    onTertiary = Color(0xFF1E1E1E),
    error = Danger,
    onError = Color(0xFF1E1E1E),
    background = BgGradientStartDark,
    onBackground = TextPrimaryDark,
    surface = Color(0xFF1F1F1F),
    onSurface = TextPrimaryDark,
    surfaceVariant = BgGradientEndDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0x33FFFFFF),
    inverseSurface = Color(0xFFECEFF4),
    inverseOnSurface = Color(0xFF2D3436)
)

@Composable
fun DormHealthTheme(
    darkThemeOverride: Boolean? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = darkThemeOverride ?: isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
