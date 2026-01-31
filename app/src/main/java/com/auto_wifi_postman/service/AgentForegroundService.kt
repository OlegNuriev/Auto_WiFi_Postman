package com.auto_wifi_postman.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.auto_wifi_postman.data.repository.AppModeRepository
import com.auto_wifi_postman.data.repository.KnownNetworksRepository
import com.auto_wifi_postman.domain.orchestrator.Orchestrator
import com.auto_wifi_postman.domain.state.AppMode
import com.auto_wifi_postman.util.LogTags
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AgentForegroundService : LifecycleService() {

    @Inject lateinit var orchestrator: Orchestrator
    @Inject lateinit var modeRepo: AppModeRepository
    @Inject lateinit var networksRepo: KnownNetworksRepository

    private var autoJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(LogTags.SERVICE, "ForegroundService created")

        startForeground(
            1,
            AgentNotification.create(this)
        )

        lifecycleScope.launch {
            modeRepo.mode.collect { mode ->
                when (mode) {
                    AppMode.AUTO -> tryStartAuto()
                    AppMode.MANUAL -> stopAuto()
                    AppMode.INIT -> stopAuto()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(LogTags.SERVICE, "onStartCommand called")
        return START_STICKY
    }

    private suspend fun tryStartAuto() {
        val networks = networksRepo.getAll()

        if (networks.isEmpty()) {
            Log.w(LogTags.SERVICE, "AUTO requested but no networks")
            return
        }

        startAuto()
    }

    private fun startAuto() {
        if (autoJob != null) return

        Log.i(LogTags.SERVICE, "AUTO mode active")

        autoJob = lifecycleScope.launch {
            while (isActive) {
                try {
                    Log.i(LogTags.SERVICE, "Starting orchestrator cycle")
                    orchestrator.startCycle()
                } catch (t: Throwable) {
                    Log.e(LogTags.SERVICE, "Service loop error", t)
                }

                Log.i(LogTags.SERVICE, "Sleeping 1 hour")
                delay(60 * 60 * 1000L)
            }
        }
    }

    private fun stopAuto() {
        if (autoJob == null) return

        Log.i(LogTags.SERVICE, "AUTO stopped")
        autoJob?.cancel()
        autoJob = null
    }

    override fun onDestroy() {
        stopAuto()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, AgentForegroundService::class.java)
            )
        }
    }
}
