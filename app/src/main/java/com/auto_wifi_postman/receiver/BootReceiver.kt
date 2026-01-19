package com.auto_wifi_postman.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.auto_wifi_postman.service.AgentForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AgentForegroundService.start(context)
        }
    }
}
