package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.model.DiagnosticStatus
import com.example.model.FmCapabilityReport
import com.example.model.HardwareItemStatus
import com.example.ui.theme.*

@Composable
fun EngineeringDiagnosticsScreen(
    report: FmCapabilityReport?,
    isSimulatedPrivilegedMode: Boolean,
    runtimeLogs: List<String>,
    onToggleSimulation: (Boolean) -> Unit,
    onRunReaudit: () -> Unit,
    onExportReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (report == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = QcomCyan)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner card: Target Device Profile & Overall Status
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateSurfaceCard)
                    .border(1.dp, QcomCyanDark, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = QcomCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "QUALCOMM RF HARDWARE DIAGNOSTICS",
                                color = QcomCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = report.targetDeviceModel,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Confidence Pill
                    Surface(
                        color = SlateSurface,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, QcomCyan)
                    ) {
                        Text(
                            text = String.format("CONFIDENCE: %.1f%%", report.confidenceScore * 100),
                            color = QcomCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SoC: ${report.cpuSoC}", color = TextSecondary, fontSize = 13.sp)
                    Text("OS: ${report.androidVersion}", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }

        // Developer Simulation Override Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSimulatedPrivilegedMode) QcomOrange else SlateBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = QcomOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Developer Privileged Simulation Mode",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bypasses vendor unprivileged sandbox restrictions in UI space to allow full exploration of Mode 1 (Operational Tuner, Presets, RDS) on non-system test tablets.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Switch(
                        checked = isSimulatedPrivilegedMode,
                        onCheckedChange = onToggleSimulation,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = QcomOrange,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SlateBg
                        )
                    )
                }
            }
        }

        // Engineering Analysis Explanation Box
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateSurfaceCard)
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "ENGINEERING AUDIT ANALYSIS & LIMITATIONS",
                    color = QcomOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (report.blockingComponent != null) {
                    Text(
                        text = "Primary Sandbox Constraint: ${report.blockingComponent}",
                        color = QcomRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = report.engineeringExplanation,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = SlateBorder)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Recommended Remediation Action:",
                    color = QcomCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = report.suggestedNextAction,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Subsystem Verification Matrix Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SUBSYSTEM VERIFICATION MATRIX",
                    color = QcomCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onRunReaudit,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateSurface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = QcomCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RE-AUDIT", color = QcomCyan, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onExportReport,
                        colors = ButtonDefaults.buttonColors(containerColor = QcomCyanDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXPORT REPORT", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Subsystem items
        val itemsList = listOf(
            report.fmHardware,
            report.fmDriver,
            report.fmHal,
            report.fmBinder,
            report.audioRouteStatus,
            report.permissionStatus
        )

        items(itemsList) { itemStatus ->
            SubsystemItemCard(itemStatus)
        }

        // Live Runtime Audit Logs
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlateSurfaceCard)
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "RUNTIME KERNEL & JNI AUDIT LOGS",
                    color = QcomCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateBg)
                        .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(runtimeLogs) { logLine ->
                            Text(
                                text = logLine,
                                color = if (logLine.contains("Switching") || logLine.contains("Audit")) QcomCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubsystemItemCard(itemStatus: HardwareItemStatus) {
    val statusColor = when (itemStatus.status) {
        DiagnosticStatus.AVAILABLE -> QcomGreen
        DiagnosticStatus.BLOCKED_BY_PERMISSIONS -> QcomAmber
        else -> QcomRed
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itemStatus.componentName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = itemStatus.status.name,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Target Specification: ${itemStatus.expectedTarget}", color = TextSecondary, fontSize = 12.sp)
            Text("Detected Runtime Value: ${itemStatus.detectedValue}", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)

            if (itemStatus.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(itemStatus.details, color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}
