package com.darkndev.everkeep

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.darkndev.everkeep.utils.sdkVersion26AndAbove
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EverKeep : Application() {

    companion object {
        const val REMINDER_NOTIFICATION_CHANNEL_1 =
            "com.darkNDev.everKeep.REMINDER_NOTIFICATION_CHANNEL_1"
        const val SYSTEM_NOTIFICATION_CHANNEL_2 =
            "com.darkNDev.everKeep.SYSTEM_NOTIFICATION_CHANNEL_2"
    }

    override fun onCreate() {
        super.onCreate()
        sdkVersion26AndAbove {
            val channel1 = NotificationChannel(
                REMINDER_NOTIFICATION_CHANNEL_1,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val channel2 = NotificationChannel(
                SYSTEM_NOTIFICATION_CHANNEL_2,
                "General notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel1)
            notificationManager.createNotificationChannel(channel2)
        }
    }
}