package com.auto_wifi_postman.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.auto_wifi_postman.domain.state.AppMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppModeRepository @Inject constructor(
    private val prefs: DataStore<Preferences>
) {

    private val MODE_KEY = stringPreferencesKey("app_mode")

    val mode: Flow<AppMode> =
        prefs.data.map { prefs ->
            when (prefs[MODE_KEY]) {
                AppMode.MANUAL.name -> AppMode.MANUAL
                else -> AppMode.AUTO
            }
        }

    suspend fun setAuto() {
        prefs.edit { it[MODE_KEY] = AppMode.AUTO.name }
    }

    suspend fun setManual() {
        prefs.edit { it[MODE_KEY] = AppMode.MANUAL.name }
    }
}













































