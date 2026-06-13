package com.dorm.health.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dorm.health.data.database.dao.DailyReportDao
import com.dorm.health.data.database.dao.DormInfoDao
import com.dorm.health.data.database.dao.EnvironmentalRecordDao
import com.dorm.health.data.database.dao.NightOwlEventDao
import com.dorm.health.data.database.dao.UserInfoDao
import com.dorm.health.data.database.entities.DailyReport
import com.dorm.health.data.database.entities.DormInfo
import com.dorm.health.data.database.entities.EnvironmentalRecord
import com.dorm.health.data.database.entities.NightOwlEvent
import com.dorm.health.data.database.entities.UserInfo

@Database(
    entities = [
        EnvironmentalRecord::class,
        DailyReport::class,
        DormInfo::class,
        UserInfo::class,
        NightOwlEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun environmentalRecordDao(): EnvironmentalRecordDao
    abstract fun dailyReportDao(): DailyReportDao
    abstract fun dormInfoDao(): DormInfoDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun nightOwlEventDao(): NightOwlEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dorm_health.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
