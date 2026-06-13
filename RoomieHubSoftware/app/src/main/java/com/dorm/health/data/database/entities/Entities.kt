package com.dorm.health.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "environmental_records")
data class EnvironmentalRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val temp: Float,
    val humi: Float,
    val noise: Float,
    val light: Float,
    val aqi: Int,
    val isNightOwl: Boolean = false
)

@Entity(tableName = "daily_reports")
data class DailyReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

@Entity(tableName = "dorm_info")
data class DormInfo(
    @PrimaryKey val id: Int = 1,
    val dormName: String = "我的宿舍",
    val memberCount: Int = 1,
    val deviceMac: String? = null,
    val sensorMode: String = "PHONE"
)

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey val id: Int = 1,
    val nickname: String = "宿舍用户",
    val avatarUri: String? = null,
    val joinTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "night_owl_events")
data class NightOwlEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val durationMinutes: Int
)
