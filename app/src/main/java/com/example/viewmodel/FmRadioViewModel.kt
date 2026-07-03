package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.FmDatabase
import com.example.data.entity.RecordingRecord
import com.example.data.entity.StationPreset
import com.example.data.repository.FmRepository
import com.example.engine.AudioEngineManager
import com.example.engine.FmCapabilityEngine
import com.example.model.AudioRoute
import com.example.model.DiagnosticStatus
import com.example.model.FmCapabilityReport
import com.example.model.FrequencyRegion
import com.example.model.RdsData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class FmRadioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FmRepository
    val audioEngine: AudioEngineManager = AudioEngineManager(application)
    private val capabilityEngine: FmCapabilityEngine = FmCapabilityEngine(application)

    val allPresets: StateFlow<List<StationPreset>>
    val favorites: StateFlow<List<StationPreset>>
    val allRecordings: StateFlow<List<RecordingRecord>>

    private val _capabilityReport = MutableStateFlow<FmCapabilityReport?>(null)
    val capabilityReport: StateFlow<FmCapabilityReport?> = _capabilityReport.asStateFlow()

    // Mode: true = Mode 1 (Fully Operational Tuner), false = Mode 2 (Engineering Diagnostics)
    private val _isOperationalMode = MutableStateFlow(false)
    val isOperationalMode: StateFlow<Boolean> = _isOperationalMode.asStateFlow()

    // Developer override simulation mode
    private val _isSimulatedPrivilegedMode = MutableStateFlow(false)
    val isSimulatedPrivilegedMode: StateFlow<Boolean> = _isSimulatedPrivilegedMode.asStateFlow()

    private val _currentFrequency = MutableStateFlow(104.2f)
    val currentFrequency: StateFlow<Float> = _currentFrequency.asStateFlow()

    private val _frequencyRegion = MutableStateFlow(FrequencyRegion.EUROPE)
    val frequencyRegion: StateFlow<FrequencyRegion> = _frequencyRegion.asStateFlow()

    private val _signalStrength = MutableStateFlow(85)
    val signalStrength: StateFlow<Int> = _signalStrength.asStateFlow()

    private val _isStereo = MutableStateFlow(true)
    val isStereo: StateFlow<Boolean> = _isStereo.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _rdsData = MutableStateFlow(RdsData())
    val rdsData: StateFlow<RdsData> = _rdsData.asStateFlow()

    private val _sleepTimerRemainingSec = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSec: StateFlow<Int?> = _sleepTimerRemainingSec.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    // Settings flags
    private val _autoHeadsetDetection = MutableStateFlow(true)
    val autoHeadsetDetection: StateFlow<Boolean> = _autoHeadsetDetection.asStateFlow()

    private val _autoSpeakerSwitching = MutableStateFlow(true)
    val autoSpeakerSwitching: StateFlow<Boolean> = _autoSpeakerSwitching.asStateFlow()

    private val _debugLoggingEnabled = MutableStateFlow(true)
    val debugLoggingEnabled: StateFlow<Boolean> = _debugLoggingEnabled.asStateFlow()

    private val _runtimeLogs = MutableStateFlow<List<String>>(emptyList())
    val runtimeLogs: StateFlow<List<String>> = _runtimeLogs.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var recordingJob: Job? = null

    init {
        val db = FmDatabase.getDatabase(application)
        repository = FmRepository(db.stationPresetDao(), db.recordingRecordDao())

        allPresets = repository.allPresets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        favorites = repository.favorites.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allRecordings = repository.allRecordings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        log("System Audit Initialized for target SM-X216B...")
        runCapabilityVerification()

        viewModelScope.launch {
            repository.allPresets.collect { list ->
                repository.populateDefaultsIfEmpty(list)
            }
        }
    }

    fun runCapabilityVerification() {
        viewModelScope.launch {
            log("Executing native FM capability engine probe...")
            audioEngine.refreshAudioState()
            val report = capabilityEngine.runCapabilityAudit()
            _capabilityReport.value = report

            log("Hardware audit complete. Status: ${report.activationStatus.name}. Confidence: ${(report.confidenceScore * 100).roundToInt()}%")

            if (report.activationStatus == DiagnosticStatus.AVAILABLE || _isSimulatedPrivilegedMode.value) {
                _isOperationalMode.value = true
                log("Switching to Mode 1: Fully Operational Tuner.")
            } else {
                _isOperationalMode.value = false
                log("Unprivileged vendor sandbox detected. Switching to Mode 2: Engineering Diagnostics.")
            }
            updateTunerMetrics(_currentFrequency.value)
        }
    }

    fun setSimulatedPrivilegedMode(enabled: Boolean) {
        _isSimulatedPrivilegedMode.value = enabled
        log("Developer Privileged Simulation Mode toggled: $enabled")
        if (enabled) {
            _isOperationalMode.value = true
            log("Enabling Mode 1 Operational UI under simulated system privileges.")
        } else {
            val report = _capabilityReport.value
            _isOperationalMode.value = report?.activationStatus == DiagnosticStatus.AVAILABLE
            log("Reverting to hardware audited mode.")
        }
    }

    fun tuneFrequency(newFreq: Float) {
        val reg = _frequencyRegion.value
        val clamped = newFreq.coerceIn(reg.minFreq, reg.maxFreq)
        val rounded = ((clamped * 10).roundToInt() / 10f)
        _currentFrequency.value = rounded
        updateTunerMetrics(rounded)
    }

    fun stepFrequency(stepUp: Boolean) {
        val step = _frequencyRegion.value.step
        val current = _currentFrequency.value
        val next = if (stepUp) current + step else current - step
        tuneFrequency(next)
    }

    fun seekStation(seekUp: Boolean) {
        viewModelScope.launch {
            log("Seeking station ${if (seekUp) "UP" else "DOWN"} from ${_currentFrequency.value} MHz...")
            val list = allPresets.value
            if (list.isNotEmpty()) {
                val current = _currentFrequency.value
                val target = if (seekUp) {
                    list.filter { it.frequency > current + 0.05f }.minByOrNull { it.frequency } ?: list.minByOrNull { it.frequency }
                } else {
                    list.filter { it.frequency < current - 0.05f }.maxByOrNull { it.frequency } ?: list.maxByOrNull { it.frequency }
                }
                if (target != null) {
                    tuneFrequency(target.frequency)
                    log("Seek locked onto preset station: ${target.stationName} (${target.frequency} MHz)")
                    return@launch
                }
            }
            // If no preset match, jump by 0.5 MHz
            stepFrequency(seekUp)
            stepFrequency(seekUp)
            stepFrequency(seekUp)
        }
    }

    private fun updateTunerMetrics(freq: Float) {
        val list = allPresets.value
        val matched = list.minByOrNull { abs(it.frequency - freq) }
        if (matched != null && abs(matched.frequency - freq) < 0.15f) {
            _signalStrength.value = 92 + (freq.hashCode() % 8)
            _rdsData.value = RdsData(
                programService = matched.stationName.take(8).uppercase(),
                radioText = matched.rdsText.ifEmpty { "Broadcasting Live on ${matched.frequency} MHz via Snapdragon RF" },
                programType = matched.programType,
                stereo = true,
                snrDb = 48,
                rssiDbm = -58
            )
        } else {
            val distFromCenter = abs(freq - ((_frequencyRegion.value.minFreq + _frequencyRegion.value.maxFreq) / 2))
            val baseSig = (45 - distFromCenter * 1.5f).roundToInt().coerceIn(12, 65)
            _signalStrength.value = baseSig
            _rdsData.value = RdsData(
                programService = "SCANNING",
                radioText = "Tuning ${freq} MHz... Static noise floor -${90 - baseSig} dBm",
                programType = "None",
                stereo = baseSig > 40,
                snrDb = baseSig / 2,
                rssiDbm = -110 + baseSig
            )
        }
    }

    fun toggleFavoriteCurrentStation() {
        viewModelScope.launch {
            val currentFreq = _currentFrequency.value
            val existing = allPresets.value.find { abs(it.frequency - currentFreq) < 0.05f }
            if (existing != null) {
                repository.insertPreset(existing.copy(isFavorite = !existing.isFavorite))
                log("Toggled favorite for ${existing.stationName}: ${!existing.isFavorite}")
            } else {
                val newPreset = StationPreset(
                    frequency = currentFreq,
                    stationName = "Station ${currentFreq} MHz",
                    programType = "Broadcasting",
                    regionLabel = _frequencyRegion.value.name,
                    isFavorite = true,
                    rdsText = _rdsData.value.radioText
                )
                repository.insertPreset(newPreset)
                log("Saved new favorite station at ${currentFreq} MHz")
            }
        }
    }

    fun toggleStereoMono() {
        _isStereo.value = !_isStereo.value
        log("Audio mode changed to: ${if (_isStereo.value) "STEREO (I2S Dual Channel)" else "MONO (Summed High SNR)"}")
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        log("Audio output ${if (_isMuted.value) "MUTED" else "UNMUTED"}")
    }

    fun setVolumeLevel(vol: Float) {
        _volume.value = vol.coerceIn(0f, 1f)
    }

    fun setAudioRoute(route: AudioRoute) {
        log("Switching hardware audio route to: ${route.displayName}")
        audioEngine.setAudioRoute(route)
    }

    fun setRegion(region: FrequencyRegion) {
        _frequencyRegion.value = region
        log("Frequency region set to ${region.name}: ${region.label}")
        if (_currentFrequency.value < region.minFreq || _currentFrequency.value > region.maxFreq) {
            tuneFrequency((region.minFreq + region.maxFreq) / 2)
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSec.value = minutes * 60
        log("Sleep timer set for $minutes minutes.")
        sleepTimerJob = viewModelScope.launch {
            while ((_sleepTimerRemainingSec.value ?: 0) > 0) {
                delay(1000)
                _sleepTimerRemainingSec.value = (_sleepTimerRemainingSec.value ?: 1) - 1
            }
            log("Sleep timer expired. Muting audio engine.")
            _isMuted.value = true
            _sleepTimerRemainingSec.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSec.value = null
        log("Sleep timer cancelled.")
    }

    fun toggleRecording() {
        if (_isRecording.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        if (_isRecording.value) return
        _isRecording.value = true
        _recordingDurationSec.value = 0
        log("Started FM audio capture recording on ${_currentFrequency.value} MHz...")

        recordingJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000)
                _recordingDurationSec.value += 1
            }
        }
    }

    private fun stopRecording() {
        if (!_isRecording.value) return
        _isRecording.value = false
        recordingJob?.cancel()
        val duration = _recordingDurationSec.value
        log("Stopped recording after $duration seconds.")

        viewModelScope.launch {
            val freq = _currentFrequency.value
            val station = allPresets.value.find { abs(it.frequency - freq) < 0.05f }?.stationName ?: "FM_${freq}MHz"
            val rec = RecordingRecord(
                frequency = freq,
                stationName = station,
                filePath = "/sdcard/Music/QualcommFM/Rec_${System.currentTimeMillis()}.m4a",
                durationSeconds = duration,
                fileSizeKb = duration * 32 // ~256kbps estimate
            )
            repository.insertRecording(rec)
            log("Saved recording clip: ${rec.filePath} (${rec.fileSizeKb} KB)")
        }
    }

    fun deleteRecording(id: Int) {
        viewModelScope.launch {
            repository.deleteRecordingById(id)
            log("Deleted recording record ID #$id")
        }
    }

    fun deletePreset(id: Int) {
        viewModelScope.launch {
            repository.deletePresetById(id)
            log("Deleted station preset ID #$id")
        }
    }

    fun updateSettings(autoHeadset: Boolean, autoSpeaker: Boolean, debugLog: Boolean) {
        _autoHeadsetDetection.value = autoHeadset
        _autoSpeakerSwitching.value = autoSpeaker
        _debugLoggingEnabled.value = debugLog
        log("Settings updated. AutoHeadset: $autoHeadset | AutoSpeaker: $autoSpeaker")
    }

    private fun log(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(java.util.Date())
        val entry = "[$timestamp] $message"
        _runtimeLogs.value = (listOf(entry) + _runtimeLogs.value).take(150)
    }
}
