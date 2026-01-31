package com.auto_wifi_postman.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.auto_wifi_postman.components.AppEvent
import com.auto_wifi_postman.service.AgentForegroundService
import com.auto_wifi_postman.ui.theme.AutoWifiPostmanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val pickFile =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri ?: return@registerForActivityResult

            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val json = contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: return@registerForActivityResult

            viewModel.onEvent(AppEvent.ImportNetworks(json))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            AutoWifiPostmanTheme {
                val state by viewModel.state.collectAsState()


                MainScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onImportClick = {
                        pickFile.launch(arrayOf("application/json"))
                    }
                )
            }
        }
    }
}
