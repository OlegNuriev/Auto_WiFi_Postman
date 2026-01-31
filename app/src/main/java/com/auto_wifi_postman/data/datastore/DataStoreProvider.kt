package com.auto_wifi_postman.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.networksDataStore by preferencesDataStore(
    name = "known_networks"
)
