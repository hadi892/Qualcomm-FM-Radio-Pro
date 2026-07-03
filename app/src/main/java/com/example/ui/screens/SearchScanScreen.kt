package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
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
import com.example.data.entity.StationPreset
import com.example.model.FrequencyRegion
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun SearchScanScreen(
    currentRegion: FrequencyRegion,
    currentFrequency: Float,
    presets: List<StationPreset>,
    onSelectFrequency: (Float) -> Unit,
    onSelectRegion: (FrequencyRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var scannedStations by remember { mutableStateOf<List<Float>>(emptyList()) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            scannedStations = emptyList()
            val step = currentRegion.step * 5
            var f = currentRegion.minFreq
            while (f <= currentRegion.maxFreq && isScanning) {
                delay(80)
                scanProgress = (f - currentRegion.minFreq) / (currentRegion.maxFreq - currentRegion.minFreq)
                // Simulate detecting stations around presets or major intervals
                if (presets.any { kotlin.math.abs(it.frequency - f) < 0.2f } || (f.hashCode() % 11 == 0)) {
                    val rounded = ((f * 10).roundToInt() / 10f)
                    if (!scannedStations.contains(rounded)) {
                        scannedStations = scannedStations + rounded
                    }
                }
                f += step
            }
            isScanning = false
            scanProgress = 1f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Region Picker Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BROADCAST FREQUENCY REGION BAND", color = QcomCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FrequencyRegion.entries.forEach { region ->
                        Surface(
                            onClick = { onSelectRegion(region) },
                            modifier = Modifier.weight(1f),
                            color = if (currentRegion == region) QcomCyanDark else SlateSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (currentRegion == region) QcomCyan else SlateBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(region.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${region.minFreq}-${region.maxFreq}", color = if (currentRegion == region) Color.White else TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Scan Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Radar, contentDescription = null, tint = if (isScanning) QcomAmber else QcomCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isScanning) "AUTOMATIC SPECTRUM SCAN IN PROGRESS..." else "WIDEBAND SPECTRUM SCANNER (${scannedStations.size} FOUND)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = { isScanning = !isScanning },
                colors = ButtonDefaults.buttonColors(containerColor = if (isScanning) QcomRed else QcomCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = if (isScanning) Color.White else Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isScanning) "STOP SCAN" else "START SCAN", color = if (isScanning) Color.White else Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        if (isScanning || scanProgress > 0f) {
            LinearProgressIndicator(
                progress = { scanProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = QcomCyan,
                trackColor = SlateSurface
            )
        }

        // Scanned Stations Grid
        val stationsToShow = if (scannedStations.isNotEmpty()) scannedStations else presets.map { it.frequency }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(stationsToShow) { freq ->
                val matchedPreset = presets.find { kotlin.math.abs(it.frequency - freq) < 0.05f }
                val isCurrent = kotlin.math.abs(currentFrequency - freq) < 0.05f

                Card(
                    onClick = { onSelectFrequency(freq) },
                    colors = CardDefaults.cardColors(containerColor = if (isCurrent) QcomCyanDark else SlateSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) QcomCyan else SlateBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%.1f", freq),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "MHz",
                            color = if (isCurrent) Color.White else QcomCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = matchedPreset?.stationName ?: "FM $freq",
                            color = if (isCurrent) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
