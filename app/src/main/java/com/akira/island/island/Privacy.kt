package com.akira.island.island

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.PrivacyState
import com.akira.island.core.Tints
import com.akira.island.core.tr

/** Раскрытый индикатор приватности: что именно сейчас включено. */
@Composable
fun PrivacyExpanded(p: PrivacyState) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (p.camera) PrivacyRow(true, tr("Камера используется", "Camera is in use"))
        if (p.mic) PrivacyRow(false, tr("Микрофон используется", "Microphone is in use"))
    }
}

@Composable
private fun PrivacyRow(camera: Boolean, text: String) {
    val tint = if (camera) Tints.Green else Tints.Orange
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(if (camera) Icons.Rounded.Videocam else Icons.Rounded.Mic, null, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
