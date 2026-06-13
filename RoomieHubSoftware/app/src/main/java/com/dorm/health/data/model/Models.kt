package com.dorm.health.data.model

enum class SensorMode {
    PHONE,
    /** 通过 Spring Boot 从本地电脑读取 ESP32 数据库 */
    SERVER
}

/** 主题偏好：null=跟随系统，false=浅色，true=深色 */
enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色")
}

enum class HealthLevel(val label: String) {
    EXCELLENT("优秀"),
    GOOD("良好"),
    FAIR("一般"),
    POOR("需改善");

    companion object {
        fun fromScore(score: Int): HealthLevel = when {
            score >= 85 -> EXCELLENT
            score >= 70 -> GOOD
            score >= 55 -> FAIR
            else -> POOR
        }
    }
}

enum class TimeRange(val label: String) {
    DAY("日"),
    WEEK("周"),
    MONTH("月")
}

enum class AirQualityLevel(val label: String) {
    EXCELLENT("优"),
    GOOD("良"),
    LIGHT("轻度污染"),
    HEAVY("重度污染");

    companion object {
        fun fromAqi(aqi: Int): AirQualityLevel = when {
            aqi <= 50 -> EXCELLENT
            aqi <= 100 -> GOOD
            aqi <= 150 -> LIGHT
            else -> HEAVY
        }
    }
}

data class EnvironmentSnapshot(
    val temp: Float = 24f,
    val humi: Float = 55f,
    val noise: Float = 35f,
    val light: Float = 100f,
    val aqi: Int = 50,
    val isSimulated: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class HealthScore(
    val total: Int = 80,
    val sleep: Int = 80,
    val comfort: Int = 80,
    val noise: Int = 80,
    val air: Int = 80,
    val level: HealthLevel = HealthLevel.GOOD
)

data class SmartSuggestion(
    val message: String,
    val isWarning: Boolean = false
)

data class DeviceStatus(
    val mode: SensorMode,
    val serverUrl: String? = null,
    val isConnected: Boolean = false,
    val lastError: String? = null
)

/** Spring Boot 连接测试结果 */
data class ServerConnectionTestResult(
    val success: Boolean,
    val message: String,
    val testedUrl: String? = null,
    val responseTimeMs: Long = 0,
    val sampleData: EnvironmentSnapshot? = null
)

/** 演示用用户账号（假用户端） */
data class MockUserAccount(
    val id: String,
    val username: String,
    val password: String,
    val nickname: String,
    val studentId: String,
    val dormName: String,
    val dormBuilding: String,
    val bedNumber: String
)

data class TrendPoint(
    val timestamp: Long,
    val value: Float
)

data class RadarDimension(
    val label: String,
    val value: Float
)

data class ReportDetail(
    val date: String,
    val avgScore: Int,
    val avgRestTime: String,
    val nightNoiseCount: Int,
    val airQualityAvg: Float,
    val trend: String,
    val sleepScore: Int = 0,
    val comfortScore: Int = 0,
    val noiseScore: Int = 0,
    val airScore: Int = 0
)
