package com.auto_wifi_postman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.auto_wifi_postman.ui.BannerLevel

@Composable
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
}
