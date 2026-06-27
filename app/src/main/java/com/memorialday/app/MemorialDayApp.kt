// 纪念日 - 主应用入口
// 对应 iOS: MemorialDayApp.swift

package com.memorialday.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.memorialday.app.mooncalendar.MoonCalendarStorage
import com.memorialday.app.services.NotificationService
import com.memorialday.app.services.StorageService

class MemorialDayApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化核心服务（对应 iOS AppDelegate）
        createNotificationChannels()
        StorageService.init(this)
        MoonCalendarStorage.init(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationService.CHANNEL_ID,
                "纪念日提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "纪念日到期提醒"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        lateinit var instance: MemorialDayApp
            private set
    }
}
