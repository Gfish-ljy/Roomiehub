package com.dorm.health.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val RobotoMono = FontFamily.Monospace

/** 环境指标卡片内的数值 */
val AppMetricValue = TextStyle(
    fontFamily = RobotoMono,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 26.sp
)

/** 单行横向布局中的大号数值（如 AQI） */
val AppMetricValueLarge = TextStyle(
    fontFamily = RobotoMono,
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    lineHeight = 30.sp
)

/** 健康评分圆环中心数值 */
val AppScoreRingValue = TextStyle(
    fontFamily = RobotoMono,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 28.sp
)

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 28.8.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 21.6.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    displayLarge = TextStyle(
        fontFamily = RobotoMono,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 52.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoMono,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    )
)
