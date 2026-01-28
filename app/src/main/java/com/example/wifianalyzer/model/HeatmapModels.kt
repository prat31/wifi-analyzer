package com.example.wifianalyzer.model

data class HeatmapPoint(
    val x: Float,
    val y: Float,
    val rssi: Int
)

data class HeatmapSession(
    val bssid: String,
    val points: List<HeatmapPoint> = emptyList(),
    val startTime: Long = System.currentTimeMillis()
)
