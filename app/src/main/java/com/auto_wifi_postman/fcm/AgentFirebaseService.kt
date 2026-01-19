package com.auto_wifi_postman.fcm

import android.util.Log
import com.auto_wifi_postman.service.AgentForegroundService
import com.auto_wifi_postman.util.LogTags
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgentFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.i(LogTags.FCM, "FCM received: ${message.data}")
        if (message.data["action"] == "START_SCAN") {
            AgentForegroundService.start(this)
        }
    }
}

