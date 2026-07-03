package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.FrequencyRegion
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    autoHeadsetDetection: Boolean,
    autoSpeakerSwitching: Boolean,
    debugLoggingEnabled: Boolean,
    currentRegion: FrequencyRegion,
    onUpdateSettings: (autoHeadset: Boolean, autoSpeaker: Boolean, debugLog: Boolean) -> Unit,
    onSelectRegion: (FrequencyRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "QUALCOMM FM ENGINE PREFERENCES",
                color = QcomCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Audio Routing Settings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = QcomCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Audio & Antenna Detection", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatic Headset Detection", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Monitor 3.5mm/USB audio jacks in real-time to verify RF ground/antenna state.", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = autoHeadsetDetection,
                            onCheckedChange = { onUpdateSettings(it, autoSpeakerSwitching, debugLoggingEnabled) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = QcomCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SlateBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatic Speaker Switching", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Route FM audio automatically to built-in stereo speakers when headset antenna is unplugged.", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = autoSpeakerSwitching,
                            onCheckedChange = { onUpdateSettings(autoHeadsetDetection, it, debugLoggingEnabled) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = QcomCyan)
                        )
                    }
                }
            }
        }

        // Region settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = QcomOrange)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Global Frequency Regions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FrequencyRegion.entries.forEach { reg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentRegion == reg) SlateSurface else Color.Transparent)
                                .clickable { onSelectRegion(reg) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(reg.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(reg.label, color = TextSecondary, fontSize = 12.sp)
                            }
                            RadioButton(
                                selected = currentRegion == reg,
                                onClick = { onSelectRegion(reg) },
                                colors = RadioButtonDefaults.colors(selectedColor = QcomOrange)
                            )
                        }
                    }
                }
            }
        }

        // System diagnostics logging setting
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Debug Telemetry & Runtime Logs", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Record V4L2 ioctl responses and Binder IPC traces for export.", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = debugLoggingEnabled,
                        onCheckedChange = { onUpdateSettings(autoHeadsetDetection, autoSpeakerSwitching, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = QcomCyan)
                    )
                }
            }
        }
    }
}
