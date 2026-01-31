package com.auto_wifi_postman.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.auto_wifi_postman.components.AppEvent
import com.auto_wifi_postman.domain.model.KnownNetwork
import com.auto_wifi_postman.domain.state.AgentStatus
import com.auto_wifi_postman.domain.state.AppMode
import com.auto_wifi_postman.ui.components.StatusBanner
import com.auto_wifi_postman.ui.util.AppBackground
import com.auto_wifi_postman.ui.util.toMinutesSeconds
import com.auto_wifi_postman.ui.util.LoadingBlock



/*@Composable
fun StatusBanner(
    message: String,
    level: BannerLevel,
    modifier: Modifier = Modifier
) {
    val background = when (level) {
        BannerLevel.INFO -> Color(0xFF2E7D32)
        BannerLevel.WARNING -> Color(0xFFF9A825)
        BannerLevel.ERROR -> Color(0xFFC62828)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .padding(12.dp)
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}*/

@Composable
fun MainScreen(
    state: UiState,
    onEvent: (AppEvent) -> Unit,
    onImportClick: () -> Unit
) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {

            AnimatedVisibility(visible = state.bannerMessage != null) {
                if (state.bannerMessage != null && state.bannerLevel != null) {
                    StatusBanner(
                        message = state.bannerMessage,
                        level = state.bannerLevel
                    )
                }
            }
            if (state.requestImportJson) {
                LaunchedEffect(Unit) {
                    onImportClick()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                /* ---------------- INIT ---------------- */

                if (!state.hasNetworks) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            "Добро пожаловать 👋",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Для начала импортируйте\nсписок Wi-Fi сетей",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(onClick = onImportClick) {
                            Text("Импорт сетей (JSON)")
                        }

                        state.importError?.let {
                            Spacer(Modifier.height(12.dp))
                            Text("Ошибка: $it", color = Color.Red)
                        }
                    }
                    return@AppBackground
                }

                /* ---------------- HEADER: Режим + Меню ---------------- */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (state.mode == AppMode.AUTO)
                                onEvent(AppEvent.SwitchToManual)
                            else
                                onEvent(AppEvent.SwitchToAuto)
                        }
                    ) {
                        Text(
                            if (state.mode == AppMode.AUTO)
                                "Перейти в ручной режим"
                            else
                                "Перейти в авто режим"
                        )
                    }

                    Spacer(Modifier.width(60.dp))

                    IconButton(onClick = {
                        onEvent(AppEvent.ToggleImportMenu)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                /* ---------------- IMPORT MENU ---------------- */
                AnimatedVisibility(visible = state.isImportMenuOpen) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Импорт сетей",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onEvent(AppEvent.ImportJsonClicked)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📁 Выбрать JSON файл")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ---------------- AUTO ---------------- */

                if (state.mode == AppMode.AUTO) {
                    Text(text = "🟢 Автоматический режим")

                    Spacer(Modifier.height(12.dp))

                    val isAutoWorking = state.agentStatus?.isRunning == true

                    if (isAutoWorking) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Получение данных…",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else if (state.autoCountdownMs > 0) {
                        Text(
                            text = "⏱ До следующей проверки: ${state.autoCountdownMs.toMinutesSeconds()}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                /* ---------------- MANUAL ---------------- */

                if (state.mode == AppMode.MANUAL) {

                    Spacer(Modifier.height(16.dp))

                    Button(
                        enabled = !state.isBusy,
                        onClick = { onEvent(AppEvent.StartManualScan) }
                    ) {
                        Text("Сканировать сети")
                    }

                    Spacer(Modifier.height(12.dp))

                    if (state.isBusy) {
                        LoadingBlock("🔄 Получение данных…")
                    } else {
                        state.manualScanResults.forEach { network ->
                            Button(
                                onClick = { onEvent(AppEvent.SelectNetwork(network)) },
                                enabled = true
                            ) {
                                Text(network.ssid)
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingBlock(x0: String) {
    TODO("Not yet implemented")
}

@Preview(
    name = "INIT state (no networks)",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_Init() {
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = false,
                    mode = AppMode.INIT,
                    importError = null,
                    bannerMessage = null,
                    bannerLevel = null
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}

@Preview(
    name = "INIT with error",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_InitWithError() {
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = false,
                    mode = AppMode.INIT,
                    importError = "Неверный формат JSON",
                    bannerMessage = null,
                    bannerLevel = null
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}

@Preview(
    name = "AUTO mode idle",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_AutoMode() {
    val now = System.currentTimeMillis()
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = true,
                    mode = AppMode.AUTO,
                    agentStatus = AgentStatus(
                        lastCycleAt = now - 300_000,
                        nextCycleAt = now + 1800_000, 
                        lastSuccess = true
                    ),
                    bannerMessage = null,
                    bannerLevel = null,
                    isBusy = false
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}

@Preview(
    name = "AUTO mode with countdown",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_AutoModeWithCountdown() {
    val now = System.currentTimeMillis()
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = true,
                    mode = AppMode.AUTO,
                    agentStatus = AgentStatus(
                        lastCycleAt = now - 120_000,
                        nextCycleAt = now + 15_000,
                        lastSuccess = true
                    ),
                    autoCountdownMs = 15_000,
                    bannerMessage = null,
                    bannerLevel = null,
                    isBusy = false
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}

@Preview(
    name = "AUTO mode with failure banner",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_AutoModeWithBanner() {
    val now = System.currentTimeMillis()
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = true,
                    mode = AppMode.AUTO,
                    agentStatus = AgentStatus(
                        lastCycleAt = now - 600_000,
                        nextCycleAt = now + 3600_000,
                        lastSuccess = false
                    ),
                    bannerMessage = "⚠️ Авто-проверка не удалась. Сервер недоступен",
                    bannerLevel = BannerLevel.WARNING,
                    isBusy = false
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}

@Preview(
    name = "MANUAL mode idle",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_ManualModeIdle() {
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = true,
                    mode = AppMode.MANUAL,
                    manualScanResults = emptyList(),
                    bannerMessage = null,
                    bannerLevel = null,
                    isBusy = false
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}

@Preview(
    name = "MANUAL mode scanning",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_ManualModeScanning() {
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = true,
                    mode = AppMode.MANUAL,
                    manualScanResults = emptyList(),
                    bannerMessage = "📡 Ищу доступные Wi-Fi сети…",
                    bannerLevel = BannerLevel.INFO,
                    isBusy = true
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}

@Preview(
    name = "MANUAL mode with results",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_ManualModeWithResults() {
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = true,
                    mode = AppMode.MANUAL,
                    manualScanResults = listOf(
                        KnownNetwork(
                            id = "net_1",
                            ssid = "Office-WiFi",
                            password = "office123",
                            baseUrl = "http://192.168.1.10:3000",
                            updateEndpoint = "/api/update"
                        ),
                        KnownNetwork(
                            id = "net_2",
                            ssid = "HomeNetwork",
                            password = "homepass",
                            baseUrl = "http://192.168.0.5:3000",
                            updateEndpoint = "/update"
                        ),
                        KnownNetwork(
                            id = "net_3",
                            ssid = "Cafe_Free",
                            password = null,
                            baseUrl = "http://10.0.0.100:3000",
                            updateEndpoint = "/ping"
                        )
                    ),
                    bannerMessage = "📋 Выберите сеть для отправки запроса",
                    bannerLevel = BannerLevel.INFO,
                    isBusy = false
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}

@Preview(
    name = "MANUAL mode no networks found",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_ManualModeNoNetworks() {
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = true,
                    mode = AppMode.MANUAL,
                    manualScanResults = emptyList(),
                    bannerMessage = "⚠️ Поблизости нет сохранённых Wi-Fi сетей",
                    bannerLevel = BannerLevel.WARNING,
                    isBusy = false
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}

@Preview(
    name = "MANUAL mode Wi-Fi disabled",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun Preview_MainScreen_ManualModeWifiDisabled() {
    MaterialTheme {
        Surface {
            MainScreen(
                state = UiState(
                    hasNetworks = true,
                    mode = AppMode.MANUAL,
                    manualScanResults = emptyList(),
                    bannerMessage = "📴 Wi-Fi выключен. Включите Wi-Fi и повторите",
                    bannerLevel = BannerLevel.WARNING,
                    isBusy = false
                ),
                onEvent = {},
                onImportClick = {}
            )
        }
    }
}
