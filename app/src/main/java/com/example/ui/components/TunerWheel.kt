package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FrequencyRegion
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun TunerWheel(
    frequency: Float,
    region: FrequencyRegion,
    onFrequencyChange: (Float) -> Unit,
    onSeek: (Boolean) -> Unit,
    onStep: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDirectEntryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SlateSurfaceCard)
            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: Direct keypad icon and region label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = SlateSurface,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Text(
                    text = region.label.split(" ").first().uppercase(),
                    color = QcomCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            IconButton(
                onClick = { showDirectEntryDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Direct Frequency Keypad Entry",
                    tint = QcomOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Giant glowing frequency readout
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.clickable { showDirectEntryDialog = true }
        ) {
            Text(
                text = String.format("%.1f", frequency),
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "MHz",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = QcomCyan,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive frequency dial scale
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SlateBg)
                .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                .pointerInput(region, frequency) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaFreq = -dragAmount / 150f
                        onFrequencyChange(frequency + deltaFreq)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerFreq = frequency
                val span = 2.5f // +/- 2.5 MHz visible
                val width = size.width
                val height = size.height

                val startFreq = ((centerFreq - span) * 10).roundToInt()
                val endFreq = ((centerFreq + span) * 10).roundToInt()

                for (f10 in startFreq..endFreq) {
                    val f = f10 / 10f
                    val x = width / 2f + ((f - centerFreq) / span) * (width / 2f)
                    val isMajor = f10 % 10 == 0
                    val isMedium = f10 % 5 == 0

                    val lineH = when {
                        isMajor -> height * 0.6f
                        isMedium -> height * 0.4f
                        else -> height * 0.25f
                    }
                    val color = when {
                        isMajor -> QcomCyan
                        isMedium -> Color.LightGray
                        else -> Color.DarkGray
                    }

                    drawLine(
                        color = color,
                        start = Offset(x, height),
                        end = Offset(x, height - lineH),
                        strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                    )
                }
            }

            // Center red/orange needle indicator
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(QcomOrange)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tuner controls row: Seek Down, Step Down, Step Up, Seek Up
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onSeek(false) },
                colors = ButtonDefaults.buttonColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.FastRewind, contentDescription = "Seek Down", tint = QcomCyan)
                Spacer(modifier = Modifier.width(4.dp))
                Text("SEEK", color = QcomCyan, fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = { onStep(false) },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(42.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Text("-0.1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { onStep(true) },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(42.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Text("+0.1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onSeek(true) },
                colors = ButtonDefaults.buttonColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("SEEK", color = QcomCyan, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.FastForward, contentDescription = "Seek Up", tint = QcomCyan)
            }
        }
    }

    if (showDirectEntryDialog) {
        DirectFrequencyEntryDialog(
            currentFrequency = frequency,
            region = region,
            onDismiss = { showDirectEntryDialog = false },
            onConfirm = { newF ->
                onFrequencyChange(newF)
                showDirectEntryDialog = false
            }
        )
    }
}

@Composable
fun DirectFrequencyEntryDialog(
    currentFrequency: Float,
    region: FrequencyRegion,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var inputStr by remember { mutableStateOf(currentFrequency.toString()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SlateSurfaceCard,
        titleContentColor = QcomCyan,
        title = { Text("Direct Frequency Tuning") },
        text = {
            Column {
                Text(
                    text = "Enter target frequency (${region.minFreq} - ${region.maxFreq} MHz):",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = inputStr,
                    onValueChange = { valStr ->
                        inputStr = valStr
                        errorMsg = null
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = QcomCyan,
                        unfocusedBorderColor = SlateBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMsg!!, color = QcomRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = inputStr.toFloatOrNull()
                    if (parsed == null || parsed < region.minFreq || parsed > region.maxFreq) {
                        errorMsg = "Invalid frequency for ${region.name}"
                    } else {
                        onConfirm(parsed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = QcomCyan)
            ) {
                Text("TUNE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        }
    )
}
