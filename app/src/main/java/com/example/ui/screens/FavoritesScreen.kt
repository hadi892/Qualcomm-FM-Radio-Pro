package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RecordingRecord
import com.example.data.entity.StationPreset
import com.example.ui.theme.*

@Composable
fun FavoritesScreen(
    presets: List<StationPreset>,
    recordings: List<RecordingRecord>,
    currentFrequency: Float,
    onSelectFrequency: (Float) -> Unit,
    onDeletePreset: (Int) -> Unit,
    onDeleteRecording: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Presets, 1 = Recorded Clips

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg)
            .padding(16.dp)
    ) {
        // Tab header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SlateSurfaceCard)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TabButton(
                text = "SAVED PRESETS (${presets.size})",
                icon = Icons.Default.Favorite,
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )

            TabButton(
                text = "CAPTURED RECORDINGS (${recordings.size})",
                icon = Icons.Default.Audiotrack,
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            if (presets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No saved favorites. Click 'SAVE PRESET' on the Tuner screen.", color = TextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(presets) { preset ->
                        PresetCard(
                            preset = preset,
                            isActive = kotlin.math.abs(preset.frequency - currentFrequency) < 0.05f,
                            onTune = { onSelectFrequency(preset.frequency) },
                            onDelete = { onDeletePreset(preset.id) }
                        )
                    }
                }
            }
        } else {
            if (recordings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No V4L2 audio recordings captured yet. Use the Record button on the Tuner screen.", color = TextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recordings) { rec ->
                        RecordingCard(
                            recording = rec,
                            onPlay = { onSelectFrequency(rec.frequency) },
                            onDelete = { onDeleteRecording(rec.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        color = if (isSelected) QcomCyanDark else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PresetCard(
    preset: StationPreset,
    isActive: Boolean,
    onTune: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTune() },
        colors = CardDefaults.cardColors(containerColor = if (isActive) SlateSurfaceCard else SlateSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) QcomCyan else SlateBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (isActive) QcomCyan else SlateBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${preset.frequency}",
                            color = if (isActive) Color.Black else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = preset.stationName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = QcomCyanDark, shape = RoundedCornerShape(4.dp)) {
                                Text("TUNED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text(
                        text = "${preset.programType} • ${preset.rdsText.take(45)}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Preset", tint = QcomRed.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun RecordingCard(
    recording: RecordingRecord,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(QcomOrange)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Re-tune", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "${recording.stationName} (${recording.frequency} MHz)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val min = recording.durationSeconds / 60
                    val sec = recording.durationSeconds % 60
                    Text(
                        text = String.format("Duration: %02d:%02d • Size: %d KB • %s", min, sec, recording.fileSizeKb, java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.US).format(java.util.Date(recording.timestamp))),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Clip", tint = QcomRed)
            }
        }
    }
}
