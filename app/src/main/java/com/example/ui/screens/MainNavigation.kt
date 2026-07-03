package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DiagnosticStatus
import com.example.ui.theme.*
import com.example.viewmodel.FmRadioViewModel

@Composable
fun MainNavigation(
    viewModel: FmRadioViewModel,
    modifier: Modifier = Modifier
) {
    val report by viewModel.capabilityReport.collectAsStateWithLifecycle()
    val isOperationalMode by viewModel.isOperationalMode.collectAsStateWithLifecycle()
    val isSimulatedPrivilegedMode by viewModel.isSimulatedPrivilegedMode.collectAsStateWithLifecycle()
    val currentFrequency by viewModel.currentFrequency.collectAsStateWithLifecycle()
    val frequencyRegion by viewModel.frequencyRegion.collectAsStateWithLifecycle()
    val signalStrength by viewModel.signalStrength.collectAsStateWithLifecycle()
    val isStereo by viewModel.isStereo.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val rdsData by viewModel.rdsData.collectAsStateWithLifecycle()
    val presets by viewModel.allPresets.collectAsStateWithLifecycle()
    val recordings by viewModel.allRecordings.collectAsStateWithLifecycle()
    val currentRoute by viewModel.audioEngine.currentRoute.collectAsStateWithLifecycle()
    val isHeadsetConnected by viewModel.audioEngine.isHeadsetAntennaConnected.collectAsStateWithLifecycle()
    val sleepTimerRemainingSec by viewModel.sleepTimerRemainingSec.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingDurationSec by viewModel.recordingDurationSec.collectAsStateWithLifecycle()
    val runtimeLogs by viewModel.runtimeLogs.collectAsStateWithLifecycle()

    val autoHeadset by viewModel.autoHeadsetDetection.collectAsStateWithLifecycle()
    val autoSpeaker by viewModel.autoSpeakerSwitching.collectAsStateWithLifecycle()
    val debugLog by viewModel.debugLoggingEnabled.collectAsStateWithLifecycle()

    // 0: Tuner Mode 1, 1: Engineering Diagnostics Mode 2, 2: Favorites & Recordings, 3: Scan Band, 4: Settings
    var currentScreenIndex by remember { mutableIntStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }

    // If hardware is sandboxed and not simulated, default initial tab to Engineering Diagnostics Mode 2 so user sees the hardware audit explanation immediately
    LaunchedEffect(report, isOperationalMode) {
        if (report != null && !isOperationalMode && currentScreenIndex == 0) {
            currentScreenIndex = 1
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = SlateSurface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Brand & Target Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = QcomCyan,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Radio, contentDescription = null, tint = Color.Black)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "QUALCOMM FM PRO",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = SlateSurfaceCard,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, QcomCyanDark)
                                ) {
                                    Text(
                                        text = "SM-X216B • SM6375",
                                        color = QcomCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isOperationalMode) "MODE 1: FULLY OPERATIONAL TUNER" else "MODE 2: ENGINEERING DIAGNOSTICS",
                                color = if (isOperationalMode) QcomGreen else QcomOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Top Action Bar buttons
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (isSimulatedPrivilegedMode) {
                            Surface(color = QcomOrange.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp), border = androidx.compose.foundation.BorderStroke(1.dp, QcomOrange)) {
                                Text("SIMULATED PRIVILEGE ON", color = QcomOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        Button(
                            onClick = { showExportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceCard),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = "Export Report", tint = QcomCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EXPORT DIAGNOSTICS", color = QcomCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tablet Navigation Rail (Side bar)
            NavigationRail(
                containerColor = SlateSurface,
                contentColor = TextPrimary,
                modifier = Modifier.border(width = 1.dp, color = SlateBorder)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                NavigationRailItem(
                    selected = currentScreenIndex == 0,
                    onClick = { currentScreenIndex = 0 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Mode 1 Tuner") },
                    label = { Text("Tuner", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = QcomCyan,
                        indicatorColor = QcomCyanDark
                    )
                )

                NavigationRailItem(
                    selected = currentScreenIndex == 1,
                    onClick = { currentScreenIndex = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (!isOperationalMode) {
                                Badge(containerColor = QcomOrange) { Text("!") }
                            }
                        }) {
                            Icon(Icons.Default.Memory, contentDescription = "Mode 2 Diagnostics")
                        }
                    },
                    label = { Text("Diagnostics", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = QcomCyan,
                        indicatorColor = QcomCyanDark
                    )
                )

                NavigationRailItem(
                    selected = currentScreenIndex == 2,
                    onClick = { currentScreenIndex = 2 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites & Clips") },
                    label = { Text("Favorites", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = QcomCyan,
                        indicatorColor = QcomCyanDark
                    )
                )

                NavigationRailItem(
                    selected = currentScreenIndex == 3,
                    onClick = { currentScreenIndex = 3 },
                    icon = { Icon(Icons.Default.Radar, contentDescription = "Scan Spectrum") },
                    label = { Text("Scan Band", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = QcomCyan,
                        indicatorColor = QcomCyanDark
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                NavigationRailItem(
                    selected = currentScreenIndex == 4,
                    onClick = { currentScreenIndex = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = QcomCyan,
                        indicatorColor = QcomCyanDark
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Screen Content
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                when (currentScreenIndex) {
                    0 -> OperationalTunerScreen(
                        currentFrequency = currentFrequency,
                        frequencyRegion = frequencyRegion,
                        signalStrength = signalStrength,
                        isStereo = isStereo,
                        isMuted = isMuted,
                        volume = volume,
                        rdsData = rdsData,
                        presets = presets,
                        currentRoute = currentRoute,
                        isHeadsetAntennaConnected = isHeadsetConnected,
                        isRecording = isRecording,
                        recordingDurationSec = recordingDurationSec,
                        sleepTimerRemainingSec = sleepTimerRemainingSec,
                        onFrequencyChange = { viewModel.tuneFrequency(it) },
                        onSeek = { viewModel.seekStation(it) },
                        onStep = { viewModel.stepFrequency(it) },
                        onToggleFavorite = { viewModel.toggleFavoriteCurrentStation() },
                        onToggleStereo = { viewModel.toggleStereoMono() },
                        onToggleMute = { viewModel.toggleMute() },
                        onVolumeChange = { viewModel.setVolumeLevel(it) },
                        onRouteSelect = { viewModel.setAudioRoute(it) },
                        onToggleRecord = { viewModel.toggleRecording() },
                        onStartSleepTimer = { viewModel.startSleepTimer(it) },
                        onCancelSleepTimer = { viewModel.cancelSleepTimer() }
                    )

                    1 -> EngineeringDiagnosticsScreen(
                        report = report,
                        isSimulatedPrivilegedMode = isSimulatedPrivilegedMode,
                        runtimeLogs = runtimeLogs,
                        onToggleSimulation = { viewModel.setSimulatedPrivilegedMode(it) },
                        onRunReaudit = { viewModel.runCapabilityVerification() },
                        onExportReport = { showExportDialog = true }
                    )

                    2 -> FavoritesScreen(
                        presets = presets,
                        recordings = recordings,
                        currentFrequency = currentFrequency,
                        onSelectFrequency = {
                            viewModel.tuneFrequency(it)
                            currentScreenIndex = 0
                        },
                        onDeletePreset = { viewModel.deletePreset(it) },
                        onDeleteRecording = { viewModel.deleteRecording(it) }
                    )

                    3 -> SearchScanScreen(
                        currentRegion = frequencyRegion,
                        currentFrequency = currentFrequency,
                        presets = presets,
                        onSelectFrequency = {
                            viewModel.tuneFrequency(it)
                            currentScreenIndex = 0
                        },
                        onSelectRegion = { viewModel.setRegion(it) }
                    )

                    4 -> SettingsScreen(
                        autoHeadsetDetection = autoHeadset,
                        autoSpeakerSwitching = autoSpeaker,
                        debugLoggingEnabled = debugLog,
                        currentRegion = frequencyRegion,
                        onUpdateSettings = { h, s, d -> viewModel.updateSettings(h, s, d) },
                        onSelectRegion = { viewModel.setRegion(it) }
                    )
                }
            }
        }
    }

    if (showExportDialog && report != null) {
        ReportExportDialog(
            report = report!!,
            onDismiss = { showExportDialog = false }
        )
    }
}
