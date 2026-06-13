package com.dorm.health.data.repository

import com.dorm.health.data.database.dao.DailyReportDao
import com.dorm.health.data.database.dao.EnvironmentalRecordDao
import com.dorm.health.data.database.dao.NightOwlEventDao
import com.dorm.health.data.database.entities.DailyReport
import com.dorm.health.data.model.ReportDetail
import com.dorm.health.utils.DateUtils
import com.dorm.health.utils.PreferencesManager
import com.dorm.health.utils.ScoreCalculator
import kotlinx.coroutines.flow.Flow

class ReportRepository(
    private val dailyReportDao: DailyReportDao,
    private val recordDao: EnvironmentalRecordDao,
    private val nightOwlEventDao: NightOwlEventDao,
    private val preferencesManager: PreferencesManager
) {
    fun getAllReports(): Flow<List<DailyReport>> = dailyReportDao.getAllReports()

    suspend fun ensureYesterdayReport() {
        val yesterday = DateUtils.yesterday()
        if (dailyReportDao.getByDate(yesterday) != null) return

        val start = DateUtils.startOfDay(
            java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, -1)
            }.timeInMillis
        )
        val end = start + 24 * 3600_000 - 1
        generateReportForDay(yesterday, start, end)
        preferencesManager.lastReportCheckDate = DateUtils.today()
    }

    suspend fun generateReportForDay(date: String, start: Long, end: Long) {
        val records = recordDao.getRecordsBetween(start, end)
        if (records.isEmpty()) return

        val nightOwlCount = nightOwlEventDao.countBetween(start, end)
        val score = ScoreCalculator.calculateDailyScore(records, nightOwlCount)
        val nightNoiseCount = records.count { it.noise > 50 && ScoreCalculator.isNightHour(it.timestamp) }
        val goodAirRatio = records.count { it.aqi <= 100 }.toFloat() / records.size * 100

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val previousDate = try {
            val parsed = dateFormat.parse(date)!!
            val c = java.util.Calendar.getInstance().apply {
                time = parsed
                add(java.util.Calendar.DAY_OF_YEAR, -1)
            }
            dateFormat.format(c.time)
        } catch (_: Exception) {
            null
        }

        val previousReport = previousDate?.let { dailyReportDao.getByDate(it) }
        val trend = when {
            previousReport == null -> "首次记录"
            score.total > previousReport.avgScore -> "较昨日上升 ${score.total - previousReport.avgScore} 分"
            score.total < previousReport.avgScore -> "较昨日下降 ${previousReport.avgScore - score.total} 分"
            else -> "与昨日持平"
        }

        dailyReportDao.insert(
            DailyReport(
                date = date,
                avgScore = score.total,
                avgRestTime = ScoreCalculator.estimateRestTime(nightOwlCount),
                nightNoiseCount = nightNoiseCount,
                airQualityAvg = goodAirRatio,
                trend = trend,
                sleepScore = score.sleep,
                comfortScore = score.comfort,
                noiseScore = score.noise,
                airScore = score.air
            )
        )
    }

    suspend fun getRecentReports(): List<ReportDetail> {
        return dailyReportDao.getRecentReports().map { report ->
            ReportDetail(
                date = report.date,
                avgScore = report.avgScore,
                avgRestTime = report.avgRestTime,
                nightNoiseCount = report.nightNoiseCount,
                airQualityAvg = report.airQualityAvg,
                trend = report.trend,
                sleepScore = report.sleepScore,
                comfortScore = report.comfortScore,
                noiseScore = report.noiseScore,
                airScore = report.airScore
            )
        }
    }

    suspend fun deleteReport(id: Long) {
        dailyReportDao.deleteById(id)
    }
}
