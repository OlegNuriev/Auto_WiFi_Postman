package com.auto_wifi_postman

import android.app.Application
import android.util.Log
import com.auto_wifi_postman.util.LogTags
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.e("APP", "🔥 App.onCreate reached")
        Log.i(LogTags.APP, "Application started")
    }
}