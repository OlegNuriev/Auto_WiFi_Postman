package com.auto_wifi_postman.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.auto_wifi_postman.domain.orchestrator.Orchestrator
import com.auto_wifi_postman.util.LogTags
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgentForegroundService : LifecycleService() {

    @Inject
    lateinit var orchestrator: Orchestrator

    override fun onCreate() {
        super.onCreate()

        Log.i(LogTags.SERVICE, "ForegroundService created")

        startForeground(
            1,
            AgentNotification.create(this)
        )

        lifecycleScope.launch {
            Log.i(LogTags.SERVICE, "Service loop started")

            while (true) {
                try {
                    Log.i(LogTags.SERVICE, "Starting orchestrator cycle")
                    orchestrator.startCycle()
                } catch (t: Throwable) {
                    Log.e(LogTags.SERVICE, "Service loop error", t)
                }

                Log.i(
                    LogTags.SERVICE,
                    "Sleeping for 1 hour before next cycle"
                )
                delay(60 * 60 * 1000L)
            }
        }
    }

    override fun onDestroy() {
        Log.w(LogTags.SERVICE, "ForegroundService destroyed")
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            Log.i(LogTags.SERVICE, "Request to start ForegroundService")

            ContextCompat.startForegroundService(
                context,
                Intent(context, AgentForegroundService::class.java)
            )
        }
    }
}
