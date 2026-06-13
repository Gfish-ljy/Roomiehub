package com.dorm.health.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dorm.health.DormHealthApp
import com.dorm.health.R
import com.dorm.health.utils.NotificationHelper

/**
 * 前台服务：后台持续采集环境数据
 */
class SensorService : Service() {

    override fun onCreate() {
        super.onCreate()
        DormHealthApp.instance.environmentRepository.startDataCollection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setContentTitle("RoomieHub 监测中")
            .setContentText("正在采集环境数据")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        DormHealthApp.instance.environmentRepository.stopDataCollection()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
