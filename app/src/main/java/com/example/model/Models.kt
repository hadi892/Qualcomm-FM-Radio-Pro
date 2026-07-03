package com.example.model

enum class FrequencyRegion(
    val label: String,
    val minFreq: Float,
    val maxFreq: Float,
    val step: Float
) {
    EUROPE("Europe (87.5 - 108.0 MHz)", 87.5f, 108.0f, 0.1f),
    US("United States (87.9 - 107.9 MHz)", 87.9f, 107.9f, 0.2f),
    JAPAN("Japan (76.0 - 95.0 MHz)", 76.0f, 95.0f, 0.1f),
    CUSTOM("Custom Wideband (65.0 - 108.0 MHz)", 65.0f, 108.0f, 0.05f)
}

enum class AudioRoute(val displayName: String, val iconName: String) {
    WIRED_HEADSET("Wired Headset (Antenna Active)", "headset"),
    SPEAKER("Built-in Stereo Speakers", "speaker"),
    BLUETOOTH("Bluetooth A2DP / LE Audio", "bluetooth"),
    USB_AUDIO("USB-C Digital Audio DAC", "usb"),
    DSP_INTERNAL("Qualcomm Hexagon DSP Direct Passthrough", "chip")
}

data class RdsData(
    val programService: String = "QCOM-FM",
    val radioText: String = "Qualcomm Snapdragon RF Explorer - High Fidelity Broadcast Reception",
    val programType: String = "Pop Music",
    val trafficAnnouncement: Boolean = false,
    val stereo: Boolean = true,
    val snrDb: Int = 42,
    val rssiDbm: Int = -68
)

enum class DiagnosticStatus {
    AVAILABLE,
    UNAVAILABLE,
    BLOCKED_BY_PERMISSIONS,
    BLOCKED_BY_SELINUX,
    MISSING_HARDWARE_NODE
}

data class HardwareItemStatus(
    val componentName: String,
    val expectedTarget: String,
    val detectedValue: String,
    val status: DiagnosticStatus,
    val details: String
)

data class FmCapabilityReport(
    val targetDeviceModel: String = "SM-X216B (Samsung Galaxy Tab A9+ 5G)",
    val cpuSoC: String = "Qualcomm Snapdragon 695 5G (SM6375 Blair ARM64)",
    val androidVersion: String = "Android 16 (API Level 36)",
    val fmHardware: HardwareItemStatus,
    val fmDriver: HardwareItemStatus,
    val fmHal: HardwareItemStatus,
    val fmBinder: HardwareItemStatus,
    val audioRouteStatus: HardwareItemStatus,
    val permissionStatus: HardwareItemStatus,
    val activationStatus: DiagnosticStatus,
    val blockingComponent: String?,
    val confidenceScore: Float,
    val engineeringExplanation: String,
    val suggestedNextAction: String,
    val timestampMs: Long = System.currentTimeMillis()
) {
    fun toMarkdown(): String {
        return """
            # Qualcomm FM Radio Professional - Engineering Diagnostic Report
            **Generated:** ${java.util.Date(timestampMs)}
            
            ## Device Profile
            - **Target Model:** $targetDeviceModel
            - **SoC Architecture:** $cpuSoC
            - **OS Version:** $androidVersion
            - **Overall Activation:** ${activationStatus.name}
            - **Confidence Score:** ${String.format("%.1f", confidenceScore * 100)}%
            
            ## Subsystem Verification Matrix
            | Subsystem | Expected Target | Detected Value | Status |
            | :--- | :--- | :--- | :--- |
            | **Hardware Engine** | ${fmHardware.expectedTarget} | ${fmHardware.detectedValue} | ${fmHardware.status.name} |
            | **V4L2 / SMD Driver** | ${fmDriver.expectedTarget} | ${fmDriver.detectedValue} | ${fmDriver.status.name} |
            | **HIDL / AIDL HAL** | ${fmHal.expectedTarget} | ${fmHal.detectedValue} | ${fmHal.status.name} |
            | **System Binder** | ${fmBinder.expectedTarget} | ${fmBinder.detectedValue} | ${fmBinder.status.name} |
            | **Audio Routing** | ${audioRouteStatus.expectedTarget} | ${audioRouteStatus.detectedValue} | ${audioRouteStatus.status.name} |
            | **Security & Perms** | ${permissionStatus.expectedTarget} | ${permissionStatus.detectedValue} | ${permissionStatus.status.name} |
            
            ## Analysis & Blocking Factors
            - **Primary Blocking Component:** ${blockingComponent ?: "None (Fully Unlocked)"}
            - **Engineering Explanation:** $engineeringExplanation
            - **Recommended Remediation:** $suggestedNextAction
        """.trimIndent()
    }

    fun toJson(): String {
        return """
            {
              "reportVersion": "2.4.0-QCOM",
              "timestamp": $timestampMs,
              "device": "$targetDeviceModel",
              "soc": "$cpuSoC",
              "androidVersion": "$androidVersion",
              "activationStatus": "${activationStatus.name}",
              "confidence": ${confidenceScore},
              "blockingComponent": "${blockingComponent ?: "null"}",
              "subsystems": {
                "hardware": { "status": "${fmHardware.status.name}", "detected": "${fmHardware.detectedValue}" },
                "driver": { "status": "${fmDriver.status.name}", "detected": "${fmDriver.detectedValue}" },
                "hal": { "status": "${fmHal.status.name}", "detected": "${fmHal.detectedValue}" },
                "binder": { "status": "${fmBinder.status.name}", "detected": "${fmBinder.detectedValue}" }
              },
              "engineeringExplanation": "${engineeringExplanation.replace("\"", "\\\"")}",
              "suggestedNextAction": "${suggestedNextAction.replace("\"", "\\\"")}"
            }
        """.trimIndent()
    }

    fun toHtml(): String {
        return """
            <!DOCTYPE html>
            <html>
            <head><title>Qualcomm FM Diagnostic Report</title>
            <style>
              body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #0B131E; color: #E2E8F0; padding: 24px; }
              h1 { color: #38BDF8; border-bottom: 2px solid #1E293B; padding-bottom: 8px; }
              .card { background: #1E293B; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
              table { width: 100%; border-collapse: collapse; margin-top: 12px; }
              th, td { padding: 10px; border: 1px solid #334155; text-align: left; }
              th { background: #0F172A; color: #38BDF8; }
              .status-ok { color: #4ADE80; font-weight: bold; }
              .status-err { color: #F87171; font-weight: bold; }
            </style>
            </head>
            <body>
              <h1>Qualcomm FM Radio Professional - Diagnostic Report</h1>
              <div class="card">
                <h3>Device Profile</h3>
                <p><b>Model:</b> $targetDeviceModel<br/><b>SoC:</b> $cpuSoC<br/><b>Confidence:</b> ${String.format("%.1f", confidenceScore * 100)}%</p>
              </div>
              <div class="card">
                <h3>Subsystem Verification Matrix</h3>
                <table>
                  <tr><th>Subsystem</th><th>Target</th><th>Detected</th><th>Status</th></tr>
                  <tr><td>Hardware Engine</td><td>${fmHardware.expectedTarget}</td><td>${fmHardware.detectedValue}</td><td class="${if (fmHardware.status == DiagnosticStatus.AVAILABLE) "status-ok" else "status-err"}">${fmHardware.status.name}</td></tr>
                  <tr><td>Driver Nodes</td><td>${fmDriver.expectedTarget}</td><td>${fmDriver.detectedValue}</td><td class="${if (fmDriver.status == DiagnosticStatus.AVAILABLE) "status-ok" else "status-err"}">${fmDriver.status.name}</td></tr>
                  <tr><td>HAL Interface</td><td>${fmHal.expectedTarget}</td><td>${fmHal.detectedValue}</td><td class="${if (fmHal.status == DiagnosticStatus.AVAILABLE) "status-ok" else "status-err"}">${fmHal.status.name}</td></tr>
                  <tr><td>Binder Service</td><td>${fmBinder.expectedTarget}</td><td>${fmBinder.detectedValue}</td><td class="${if (fmBinder.status == DiagnosticStatus.AVAILABLE) "status-ok" else "status-err"}">${fmBinder.status.name}</td></tr>
                </table>
              </div>
              <div class="card">
                <h3>Engineering Analysis</h3>
                <p><b>Blocking Component:</b> ${blockingComponent ?: "None"}</p>
                <p><b>Explanation:</b> $engineeringExplanation</p>
                <p><b>Recommended Action:</b> $suggestedNextAction</p>
              </div>
            </body>
            </html>
        """.trimIndent()
    }
}
