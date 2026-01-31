package com.auto_wifi_postman.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportStateRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val IMPORT_DONE_KEY = booleanPreferencesKey("import_done")

    val hasImportedOnce: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[IMPORT_DONE_KEY] == true
        }

    suspend fun markImported() {
        dataStore.edit { prefs ->
            prefs[IMPORT_DONE_KEY] = true
        }
    }
}
