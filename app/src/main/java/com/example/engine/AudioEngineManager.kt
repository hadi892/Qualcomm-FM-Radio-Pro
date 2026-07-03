package com.example.engine

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.example.model.AudioRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEngineManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _currentRoute = MutableStateFlow(detectBestAudioRoute())
    val currentRoute: StateFlow<AudioRoute> = _currentRoute.asStateFlow()

    private val _isHeadsetAntennaConnected = MutableStateFlow(isWiredHeadsetConnected())
    val isHeadsetAntennaConnected: StateFlow<Boolean> = _isHeadsetAntennaConnected.asStateFlow()

    private val _dspPassthroughEnabled = MutableStateFlow(true)
    val dspPassthroughEnabled: StateFlow<Boolean> = _dspPassthroughEnabled.asStateFlow()

    fun refreshAudioState() {
        val headset = isWiredHeadsetConnected()
        _isHeadsetAntennaConnected.value = headset
        _currentRoute.value = detectBestAudioRoute()
    }

    private fun isWiredHeadsetConnected(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            return devices.any { 
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || 
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_USB_HEADSET
            }
        } else {
            @Suppress("DEPRECATION")
            return audioManager.isWiredHeadsetOn
        }
    }

    private fun detectBestAudioRoute(): AudioRoute {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            if (devices.any { it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET }) {
                return AudioRoute.WIRED_HEADSET
            }
            if (devices.any { it.type == AudioDeviceInfo.TYPE_USB_HEADSET || it.type == AudioDeviceInfo.TYPE_USB_DEVICE }) {
                return AudioRoute.USB_AUDIO
            }
            if (devices.any { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || it.type == AudioDeviceInfo.TYPE_BLE_HEADSET }) {
                return AudioRoute.BLUETOOTH
            }
        }
        return AudioRoute.SPEAKER
    }

    fun setAudioRoute(route: AudioRoute): Boolean {
        _currentRoute.value = route
        try {
            when (route) {
                AudioRoute.SPEAKER -> {
                    audioManager.isSpeakerphoneOn = true
                }
                AudioRoute.WIRED_HEADSET -> {
                    audioManager.isSpeakerphoneOn = false
                }
                AudioRoute.BLUETOOTH -> {
                    audioManager.isSpeakerphoneOn = false
                    if (audioManager.isBluetoothA2dpOn) {
                        // Keep A2DP routing
                    }
                }
                AudioRoute.USB_AUDIO -> {
                    audioManager.isSpeakerphoneOn = false
                }
                AudioRoute.DSP_INTERNAL -> {
                    _dspPassthroughEnabled.value = true
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun toggleDspPassthrough(enabled: Boolean) {
        _dspPassthroughEnabled.value = enabled
    }
}
