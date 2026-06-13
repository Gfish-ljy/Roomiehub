package com.dorm.health.utils

import com.dorm.health.data.database.dao.NightOwlEventDao
import com.dorm.health.data.database.entities.NightOwlEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 熬夜检测：22:00-05:00 光照>30lux 且 噪音>40dB 持续超过15分钟记录一次熬夜事件
 */
class NightOwlDetector(
    private val nightOwlEventDao: NightOwlEventDao
) {
    private val mutex = Mutex()
    private var conditionStartTime: Long? = null
    private var lastEventTime: Long = 0
    private var alertStartTime: Long? = null

    data class DetectionResult(
        val isNightOwl: Boolean,
        val shouldAlert: Boolean,
        val alertMessage: String? = null
    )

    suspend fun evaluate(light: Float, noise: Float, timestamp: Long): DetectionResult {
        return mutex.withLock {
            val isLateNight = ScoreCalculator.isLateNight(timestamp)
            val meetsCondition = isLateNight && light > 30 && noise > 40

            if (meetsCondition) {
                if (conditionStartTime == null) {
                    conditionStartTime = timestamp
                }
                if (alertStartTime == null) {
                    alertStartTime = timestamp
                }
            } else {
                conditionStartTime = null
                alertStartTime = null
            }

            val durationMinutes = conditionStartTime?.let {
                ((timestamp - it) / 60_000).toInt()
            } ?: 0

            val alertDurationMinutes = alertStartTime?.let {
                ((timestamp - it) / 60_000).toInt()
            } ?: 0

            var isNightOwl = false
            if (durationMinutes >= 15 && timestamp - lastEventTime > 30 * 60_000) {
                nightOwlEventDao.insert(
                    NightOwlEvent(timestamp = timestamp, durationMinutes = durationMinutes)
                )
                lastEventTime = timestamp
                isNightOwl = true
            }

            val shouldAlert = alertDurationMinutes >= 30
            val alertMessage = if (shouldAlert) "熬夜提醒，建议尽快休息" else null

            DetectionResult(
                isNightOwl = isNightOwl || meetsCondition,
                shouldAlert = shouldAlert,
                alertMessage = alertMessage
            )
        }
    }
}
