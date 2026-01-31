package com.auto_wifi_postman.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GreenColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF66BB6A),
    tertiary = Color(0xFF81C784)
)

@Composable
fun AutoWifiPostmanTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GreenColorScheme,
        content = content
    )
}
