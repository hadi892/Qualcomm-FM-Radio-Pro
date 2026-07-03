package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.StationPreset
import com.example.model.AudioRoute
import com.example.model.FrequencyRegion
import com.example.model.RdsData
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun OperationalTunerScreen(
    currentFrequency: Float,
    frequencyRegion: FrequencyRegion,
    signalStrength: Int,
    isStereo: Boolean,
    isMuted: Boolean,
    volume: Float,
    rdsData: RdsData,
    presets: List<StationPreset>,
    currentRoute: AudioRoute,
    isHeadsetAntennaConnected: Boolean,
    isRecording: Boolean,
    recordingDurationSec: Int,
    sleepTimerRemainingSec: Int?,
    onFrequencyChange: (Float) -> Unit,
    onSeek: (Boolean) -> Unit,
    onStep: (Boolean) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleStereo: () -> Unit,
    onToggleMute: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onRouteSelect: (AudioRoute) -> Unit,
    onToggleRecord: () -> Unit,
    onStartSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val isCurrentFavorite = presets.any { abs(it.frequency - currentFrequency) < 0.05f && it.isFavorite }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column (On wide tablets takes 55% width): Tuner Wheel & Audio Routing
        Column(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Audio routing bar
            AudioRouteBar(
                currentRoute = currentRoute,
                isHeadsetAntennaConnected = isHeadsetAntennaConnected,
                isMuted = isMuted,
                volume = volume,
                isStereo = isStereo,
                isRecording = isRecording,
                recordingDurationSec = recordingDurationSec,
                onRouteSelect = onRouteSelect,
                onToggleMute = onToggleMute,
                onVolumeChange = onVolumeChange,
                onToggleStereo = onToggleStereo,
                onToggleRecord = onToggleRecord
            )

            // Animated Tuner Wheel
            TunerWheel(
                frequency = currentFrequency,
                region = frequencyRegion,
                onFrequencyChange = onFrequencyChange,
                onSeek = onSeek,
                onStep = onStep
            )

            // Sleep Timer Pill / Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateSurfaceCard)
                    .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepTimerRemainingSec != null) QcomAmber else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (sleepTimerRemainingSec != null) {
                            val min = sleepTimerRemainingSec / 60
                            val sec = sleepTimerRemainingSec % 60
                            String.format("SLEEP TIMER: %02d:%02d REMAINING", min, sec)
                        } else {
                            "SLEEP TIMER: OFF"
                        },
                        color = if (sleepTimerRemainingSec != null) QcomAmber else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (sleepTimerRemainingSec != null) {
                    TextButton(onClick = onCancelSleepTimer) {
                        Text("CANCEL", color = QcomRed, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { showSleepTimerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateSurface)
                    ) {
                        Text("SET TIMER", color = QcomCyan, fontSize = 12.sp)
                    }
                }
            }
        }

        // Right Column (Takes 45% width): RDS Panel, Signal Meter, Quick Presets
        Column(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Favorite toggle header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECEPTION & METRICS",
                    color = QcomCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Button(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentFavorite) QcomOrange else SlateSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isCurrentFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCurrentFavorite) "FAVORITED" else "SAVE PRESET",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // RDS Display Panel
            RdsDisplayPanel(
                rdsData = rdsData,
                frequency = currentFrequency,
                isFavorite = isCurrentFavorite,
                onToggleFavorite = onToggleFavorite
            )

            // Signal Strength Meter
            SignalStrengthMeter(
                signalStrength = signalStrength,
                isStereo = isStereo,
                snrDb = rdsData.snrDb,
                rssiDbm = rdsData.rssiDbm
            )

            // Quick Station Presets Horizontal Strip
            Text(
                text = "QUICK STATION PRESETS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    Surface(
                        onClick = { onFrequencyChange(preset.frequency) },
                        color = if (abs(preset.frequency - currentFrequency) < 0.05f) QcomCyanDark else SlateSurfaceCard,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (abs(preset.frequency - currentFrequency) < 0.05f) QcomCyan else SlateBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${preset.frequency}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = preset.stationName.take(10),
                                color = if (abs(preset.frequency - currentFrequency) < 0.05f) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            containerColor = SlateSurfaceCard,
            titleContentColor = QcomCyan,
            title = { Text("Auto Sleep Timer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select broadcast shutdown interval:", color = TextSecondary)
                    listOf(15, 30, 45, 60, 90).forEach { min ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onStartSleepTimer(min)
                                    showSleepTimerDialog = false
                                },
                            color = SlateSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$min Minutes", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Default.Timer, contentDescription = null, tint = QcomCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}
