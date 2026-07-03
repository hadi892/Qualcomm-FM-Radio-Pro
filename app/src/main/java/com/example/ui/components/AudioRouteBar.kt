package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioRoute
import com.example.ui.theme.*

@Composable
fun AudioRouteBar(
    currentRoute: AudioRoute,
    isHeadsetAntennaConnected: Boolean,
    isMuted: Boolean,
    volume: Float,
    isStereo: Boolean,
    isRecording: Boolean,
    recordingDurationSec: Int,
    onRouteSelect: (AudioRoute) -> Unit,
    onToggleMute: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleStereo: () -> Unit,
    onToggleRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRouteMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlateSurfaceCard)
            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        // Top status line: Antenna & Routing Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Antenna connection badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isHeadsetAntennaConnected) Icons.Default.Headphones else Icons.Default.Warning,
                    contentDescription = "Antenna Status",
                    tint = if (isHeadsetAntennaConnected) QcomGreen else QcomAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isHeadsetAntennaConnected) "ANTENNA: WIRED HEADSET CONNECTED" else "ANTENNA: INTERNAL SPEAKER ROUTED",
                    color = if (isHeadsetAntennaConnected) QcomGreen else QcomAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Audio route selector dropdown trigger
            Box {
                Surface(
                    onClick = { showRouteMenu = true },
                    color = SlateSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, QcomCyan)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = when (currentRoute) {
                                AudioRoute.WIRED_HEADSET -> Icons.Default.Headphones
                                AudioRoute.BLUETOOTH -> Icons.Default.Bluetooth
                                AudioRoute.USB_AUDIO -> Icons.Default.Usb
                                AudioRoute.DSP_INTERNAL -> Icons.Default.Memory
                                else -> Icons.Default.Speaker
                            },
                            contentDescription = "Current Audio Route",
                            tint = QcomCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentRoute.displayName.take(16),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                DropdownMenu(
                    expanded = showRouteMenu,
                    onDismissRequest = { showRouteMenu = false },
                    modifier = Modifier.background(SlateSurfaceCard).border(1.dp, SlateBorder)
                ) {
                    AudioRoute.entries.forEach { route ->
                        DropdownMenuItem(
                            text = { Text(route.displayName, color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                onRouteSelect(route)
                                showRouteMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Volume slider & controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) QcomRed else SlateSurface)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Toggle Mute",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Slider(
                value = if (isMuted) 0f else volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = QcomCyan,
                    activeTrackColor = QcomCyan,
                    inactiveTrackColor = SlateBg
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Stereo / Mono button
            TextButton(
                onClick = onToggleStereo,
                colors = ButtonDefaults.textButtonColors(contentColor = if (isStereo) QcomCyan else TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(if (isStereo) "2CH ST" else "1CH MO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Record button
            IconButton(
                onClick = onToggleRecord,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) QcomRed else SlateSurface)
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    contentDescription = "Record Audio",
                    tint = Color.White
                )
            }
        }

        if (isRecording) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(QcomRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val min = recordingDurationSec / 60
                val sec = recordingDurationSec % 60
                Text(
                    text = String.format("CAPTURING V4L2 AUDIO STREAM: %02d:%02d", min, sec),
                    color = QcomRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
