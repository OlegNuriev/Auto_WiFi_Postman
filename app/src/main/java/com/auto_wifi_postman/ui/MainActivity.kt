package com.auto_wifi_postman.ui

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import com.auto_wifi_postman.service.AgentForegroundService
import dagger.hilt.android.AndroidEntryPoint


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(TextView(this).apply {
            text = "Агент работает в фоне"
            gravity = Gravity.CENTER
            textSize = 18f
        })

        AgentForegroundService.start(this)
    }
}
