package com.auto_wifi_postman.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object AgentNotification {

    private const val CHANNEL_ID = "agent_channel"

    fun create(context: Context): Notification {
        val manager = context.getSystemService(NotificationManager::class.java)

        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Agent",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        return Notification.Builder(context, CHANNEL_ID)
            .setContentTitle("Мониторинг локальных сетей активен")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
    }
}
