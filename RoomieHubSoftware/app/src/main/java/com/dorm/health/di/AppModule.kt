package com.dorm.health.di

import android.content.Context
import com.dorm.health.data.database.AppDatabase
import com.dorm.health.data.datasource.PhoneSensorDataSource
import com.dorm.health.data.datasource.ServerDataSource
import com.dorm.health.data.repository.EnvironmentRepository
import com.dorm.health.data.repository.MockAuthRepository
import com.dorm.health.data.repository.ReportRepository
import com.dorm.health.data.repository.UserRepository
import com.dorm.health.utils.NetworkMonitor
import com.dorm.health.utils.NightOwlDetector
import com.dorm.health.utils.NotificationHelper
import com.dorm.health.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppModule(context: Context) {
    private val appContext = context.applicationContext
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val database = AppDatabase.getInstance(appContext)
    val preferencesManager = PreferencesManager(appContext)
    val notificationHelper = NotificationHelper(appContext)
    val networkMonitor = NetworkMonitor(appContext)

    val phoneSensorDataSource = PhoneSensorDataSource(appContext)
    val serverDataSource = ServerDataSource(preferencesManager)
    val nightOwlDetector = NightOwlDetector(database.nightOwlEventDao())

    val environmentRepository = EnvironmentRepository(
        recordDao = database.environmentalRecordDao(),
        dormInfoDao = database.dormInfoDao(),
        nightOwlEventDao = database.nightOwlEventDao(),
        phoneSensorDataSource = phoneSensorDataSource,
        serverDataSource = serverDataSource,
        preferencesManager = preferencesManager,
        nightOwlDetector = nightOwlDetector,
        scope = appScope
    )

    val reportRepository = ReportRepository(
        dailyReportDao = database.dailyReportDao(),
        recordDao = database.environmentalRecordDao(),
        nightOwlEventDao = database.nightOwlEventDao(),
        preferencesManager = preferencesManager
    )

    val userRepository = UserRepository(database.userInfoDao())

    val authRepository = MockAuthRepository(
        preferencesManager = preferencesManager,
        userRepository = userRepository,
        environmentRepository = environmentRepository
    )
}
