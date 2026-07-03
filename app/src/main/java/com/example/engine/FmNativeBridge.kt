package com.example.engine

import java.io.File

object FmNativeBridge {
    /**
     * Probes V4L2 and Qualcomm SMD radio device nodes on the device filesystem.
     */
    fun probeRadioDeviceNodes(): Map<String, Boolean> {
        val candidates = listOf(
            "/dev/radio0",
            "/dev/fm",
            "/dev/smd7",
            "/dev/qmi0",
            "/sys/class/video4linux/radio0",
            "/sys/devices/soc0/soc_id"
        )
        return candidates.associateWith { path ->
            try {
                File(path).exists()
            } catch (e: SecurityException) {
                // If SEPolicy blocks existence check, note true with permission denial
                false
            }
        }
    }

    /**
     * Checks if standard Qualcomm or Android broadcast radio shared libraries exist
     */
    fun probeNativeLibraries(): Map<String, Boolean> {
        val libs = listOf(
            "/vendor/lib64/libqcomfm_jni.so",
            "/system/lib64/libqcomfm_jni.so",
            "/vendor/lib64/libqradio.so",
            "/vendor/lib64/hw/broadcastradio.qti.default.so",
            "/vendor/lib64/libfm_jni.so"
        )
        return libs.associateWith { path ->
            try {
                File(path).exists()
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Checks kernel /proc/devices or /proc/modules for radio drivers if readable
     */
    fun checkKernelDriverInfo(): String {
        return try {
            val procDevices = File("/proc/devices")
            if (procDevices.exists() && procDevices.canRead()) {
                val content = procDevices.readText()
                if (content.contains("video4linux") || content.contains("radio")) {
                    "V4L2 Radio Kernel Module Detected (Char 81)"
                } else {
                    "Standard Kernel Char Devices (No dedicated /dev/radio0 registered)"
                }
            } else {
                "Kernel /proc/devices restricted by SELinux sandbox"
            }
        } catch (e: Exception) {
            "SELinux Access Restriction (${e.javaClass.simpleName})"
        }
    }
}
