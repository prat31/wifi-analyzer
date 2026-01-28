package com.example.wifianalyzer.model

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val level: Int, // Signal strength in dBm
    val frequency: Int, // Frequency in MHz
    val capabilities: String,
    val timestamp: Long,
    val channelWidth: Int,
    val centerFreq0: Int,
    val centerFreq1: Int
) {
    // Helper to calculate signal quality percentage
    val signalQuality: Int
        get() {
            return if (level <= -100) 0
            else if (level >= -50) 100
            else 2 * (level + 100)
        }

    val signalZone: SignalZone
        get() {
            return when {
                level >= -50 -> SignalZone.HOT
                level >= -60 -> SignalZone.WARM
                level >= -70 -> SignalZone.FAIR
                else -> SignalZone.COLD
            }
        }
}

enum class SignalZone(val label: String, val colorHex: Long, val emoji: String) {
    HOT("Hot Zone", 0xFFFF4500, "🔥"),   // OrangeRed
    WARM("Warm Zone", 0xFFFFA500, "☀️"),  // Orange
    FAIR("Fair Zone", 0xFF87CEEB, "☁️"),  // SkyBlue
    COLD("Cold Zone", 0xFF0000FF, "❄️")   // Blue
}
