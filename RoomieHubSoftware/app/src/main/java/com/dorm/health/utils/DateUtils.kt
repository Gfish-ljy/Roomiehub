package com.dorm.health.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())

    fun today(): String = dateFormat.format(Date())

    fun yesterday(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun formatShortDate(timestamp: Long): String =
        SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))

    fun formatDisplayDate(date: String): String {
        return try {
            displayFormat.format(dateFormat.parse(date)!!)
        } catch (_: Exception) {
            date
        }
    }

    fun startOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun daysAgo(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.timeInMillis
    }

    fun rangeStart(range: com.dorm.health.data.model.TimeRange): Long {
        val cal = Calendar.getInstance()
        return when (range) {
            com.dorm.health.data.model.TimeRange.DAY -> startOfDay()
            com.dorm.health.data.model.TimeRange.WEEK -> daysAgo(7)
            com.dorm.health.data.model.TimeRange.MONTH -> daysAgo(30)
        }.also { cal.timeInMillis = it }
    }
}
