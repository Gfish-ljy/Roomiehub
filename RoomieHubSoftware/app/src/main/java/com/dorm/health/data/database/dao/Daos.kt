package com.dorm.health.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dorm.health.data.database.entities.DailyReport
import com.dorm.health.data.database.entities.DormInfo
import com.dorm.health.data.database.entities.EnvironmentalRecord
import com.dorm.health.data.database.entities.NightOwlEvent
import com.dorm.health.data.database.entities.UserInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentalRecordDao {
    @Insert
    suspend fun insert(record: EnvironmentalRecord): Long

    @Query("SELECT * FROM environmental_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<EnvironmentalRecord?>

    @Query("SELECT * FROM environmental_records WHERE timestamp >= :from ORDER BY timestamp ASC")
    fun getRecordsSince(from: Long): Flow<List<EnvironmentalRecord>>

    @Query("SELECT * FROM environmental_records WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun getRecordsBetween(from: Long, to: Long): List<EnvironmentalRecord>

    @Query("SELECT * FROM environmental_records WHERE timestamp >= :from ORDER BY timestamp ASC")
    suspend fun getRecordsSinceOnce(from: Long): List<EnvironmentalRecord>

    @Query("SELECT EXISTS(SELECT 1 FROM environmental_records WHERE timestamp = :timestamp)")
    suspend fun existsAtTimestamp(timestamp: Long): Boolean

    @Query("DELETE FROM environmental_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface DailyReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: DailyReport)

    @Query("SELECT * FROM daily_reports ORDER BY date DESC")
    fun getAllReports(): Flow<List<DailyReport>>

    @Query("SELECT * FROM daily_reports WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyReport?

    @Query("SELECT * FROM daily_reports ORDER BY date DESC LIMIT 7")
    suspend fun getRecentReports(): List<DailyReport>

    @Query("DELETE FROM daily_reports WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface DormInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: DormInfo)

    @Update
    suspend fun update(info: DormInfo)

    @Query("SELECT * FROM dorm_info WHERE id = 1")
    fun getDormInfo(): Flow<DormInfo?>

    @Query("SELECT * FROM dorm_info WHERE id = 1")
    suspend fun getDormInfoOnce(): DormInfo?
}

@Dao
interface UserInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: UserInfo)

    @Update
    suspend fun update(info: UserInfo)

    @Query("SELECT * FROM user_info WHERE id = 1")
    fun getUserInfo(): Flow<UserInfo?>

    @Query("SELECT * FROM user_info WHERE id = 1")
    suspend fun getUserInfoOnce(): UserInfo?
}

@Dao
interface NightOwlEventDao {
    @Insert
    suspend fun insert(event: NightOwlEvent)

    @Query("SELECT COUNT(*) FROM night_owl_events WHERE timestamp BETWEEN :from AND :to")
    suspend fun countBetween(from: Long, to: Long): Int

    @Query("SELECT * FROM night_owl_events WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    suspend fun getBetween(from: Long, to: Long): List<NightOwlEvent>
}
