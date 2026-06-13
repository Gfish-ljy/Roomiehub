package com.dorm.health.utils

import com.dorm.health.data.database.entities.EnvironmentalRecord
import com.dorm.health.data.model.HealthLevel
import com.dorm.health.data.model.HealthScore
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 健康评分计算器
 * 维度：作息、环境舒适度、噪音控制、空气质量
 */
object ScoreCalculator {

    private const val IDEAL_TEMP = 24f
    private const val IDEAL_HUMI = 55f

    fun calculateRealtimeScore(records: List<EnvironmentalRecord>): HealthScore {
        if (records.isEmpty()) return HealthScore()

        val sleep = calculateSleepScore(records)
        val comfort = calculateComfortScore(records)
        val noise = calculateNoiseScore(records)
        val air = calculateAirScore(records)

        val nightOwlPenalty = if (records.any { it.isNightOwl }) 0.8f else 1f
        val total = ((sleep + comfort + noise + air) / 4f * nightOwlPenalty).toInt().coerceIn(0, 100)

        return HealthScore(
            total = total,
            sleep = sleep,
            comfort = comfort,
            noise = noise,
            air = air,
            level = HealthLevel.fromScore(total)
        )
    }

    fun calculateDailyScore(records: List<EnvironmentalRecord>, nightOwlCount: Int): HealthScore {
        val base = calculateRealtimeScore(records)
        val sleepPenalty = max(0, 100 - nightOwlCount * 15)
        val sleep = min(base.sleep, sleepPenalty)
        val total = ((sleep + base.comfort + base.noise + base.air) / 4f).toInt().coerceIn(0, 100)
        return base.copy(total = total, sleep = sleep, level = HealthLevel.fromScore(total))
    }

    private fun calculateSleepScore(records: List<EnvironmentalRecord>): Int {
        val nightRecords = records.filter { isNightHour(it.timestamp) }
        if (nightRecords.isEmpty()) return 85
        val nightOwlRatio = nightRecords.count { it.isNightOwl }.toFloat() / nightRecords.size
        return (100 - nightOwlRatio * 60).toInt().coerceIn(0, 100)
    }

    private fun calculateComfortScore(records: List<EnvironmentalRecord>): Int {
        val avgTemp = records.map { it.temp }.average().toFloat()
        val avgHumi = records.map { it.humi }.average().toFloat()
        val tempDev = abs(avgTemp - IDEAL_TEMP)
        val humiDev = abs(avgHumi - IDEAL_HUMI)
        val penalty = tempDev * 3 + humiDev * 0.8f
        return (100 - penalty).toInt().coerceIn(0, 100)
    }

    private fun calculateNoiseScore(records: List<EnvironmentalRecord>): Int {
        val avgNoise = records.map { it.noise }.average().toFloat()
        return when {
            avgNoise <= 40 -> 95
            avgNoise <= 55 -> 80
            avgNoise <= 70 -> 60
            else -> 40
        }
    }

    private fun calculateAirScore(records: List<EnvironmentalRecord>): Int {
        val avgAqi = records.map { it.aqi }.average().toFloat()
        return when {
            avgAqi <= 50 -> 95
            avgAqi <= 100 -> 80
            avgAqi <= 150 -> 60
            else -> 35
        }
    }

    fun isNightHour(timestamp: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 6
    }

    fun isLateNight(timestamp: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 5
    }

    fun calculateVirtualAqi(noise: Float, humi: Float, light: Float): Int {
        var score = 30f
        score += (noise - 30) * 0.5f
        score += abs(humi - 50) * 0.3f
        if (light < 10) score += 10
        return score.toInt().coerceIn(0, 300)
    }

    fun generateSuggestions(
        temp: Float,
        humi: Float,
        noise: Float,
        light: Float,
        aqi: Int,
        isNightOwl: Boolean
    ): List<String> {
        val suggestions = mutableListOf<String>()
        if (noise > 70) suggestions.add("噪音过高，建议降低音量或使用耳塞")
        if (aqi > 150) suggestions.add("空气质量较差，建议开窗通风")
        if (temp > 30) suggestions.add("温度过高，注意防暑降温")
        if (humi > 70) suggestions.add("湿度偏高，建议使用除湿设备")
        if (humi < 35) suggestions.add("空气干燥，建议适当加湿")
        if (isNightOwl) suggestions.add("熬夜警告：深夜环境活跃，建议尽快休息")
        if (light > 300 && isLateNight(System.currentTimeMillis())) {
            suggestions.add("深夜光照较强，可能影响睡眠质量")
        }
        if (suggestions.isEmpty()) {
            suggestions.add("环境状态良好，继续保持健康作息")
        }
        return suggestions
    }

    fun estimateRestTime(nightOwlEvents: Int): String {
        return when {
            nightOwlEvents == 0 -> "23:30"
            nightOwlEvents <= 2 -> "00:15"
            else -> "01:00"
        }
    }

    fun fakeRankingPercent(score: Int): Int {
        return (55 + score * 0.35).toInt().coerceIn(60, 98)
    }
}
