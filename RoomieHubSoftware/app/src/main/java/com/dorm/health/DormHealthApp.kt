package com.dorm.health

import android.app.Application
import com.dorm.health.di.AppModule
import kotlinx.coroutines.launch

class DormHealthApp : Application() {
    lateinit var appModule: AppModule
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        appModule = AppModule(this)
        appScope.launch {
            appModule.userRepository.ensureDefaultUser()
            appModule.reportRepository.ensureYesterdayReport()
            appModule.environmentRepository.startDataCollection()
        }
    }

    val environmentRepository get() = appModule.environmentRepository
    val reportRepository get() = appModule.reportRepository
    val userRepository get() = appModule.userRepository
    val preferencesManager get() = appModule.preferencesManager
    val notificationHelper get() = appModule.notificationHelper
    val networkMonitor get() = appModule.networkMonitor
    val serverDataSource get() = appModule.serverDataSource
    val authRepository get() = appModule.authRepository
    val appScope get() = appModule.appScope

    companion object {
        lateinit var instance: DormHealthApp
            private set
    }
}
