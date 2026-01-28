package com.example.wifianalyzer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifianalyzer.model.HeatmapPoint
import com.example.wifianalyzer.util.SensorManagerHelper
import com.example.wifianalyzer.util.WifiScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class HeatmapViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorHelper = SensorManagerHelper(application)
    private val wifiScanner = WifiScanner(application)
    
    // We might need to listen to WifiScanner's real-time updates or trigger them manually
    // For heatmap, we care about the RSSI of a *specific* BSSID relative to current location.
    
    // Current user position on the relative grid (0,0 start)
    private var currentX = 0f
    private var currentY = 0f
    
    private val _heatmapPoints = MutableStateFlow<List<HeatmapPoint>>(emptyList())
    val heatmapPoints: StateFlow<List<HeatmapPoint>> = _heatmapPoints.asStateFlow()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var targetBssid: String? = null
    
    init {
        sensorHelper.onStepDetected = { headingRadians ->
            if (_isRecording.value) {
                // Assume step length approx 0.7m or 1 unit
                val stepSize = 50f // Arbitrary pixels for drawing
                
                // Calculate new position
                // Heading is azimuth: 0=North (-y), PI/2=East (+x), but depends on device orientation mapping
                // Let's assume standard math: 0 is +x, PI/2 is +y for canvas convenience, mapping sensor azimuth to it
                // Actually sensor azimuth: 0=North, PI/2=East.
                // Screen Y is down (North), Screen X is right (East).
                val dx = (sin(headingRadians) * stepSize).toFloat()
                val dy = -(cos(headingRadians) * stepSize).toFloat() 
                
                currentX += dx
                currentY += dy
                
                // Record point
                recordPoint()
            }
        }
    }
    
    fun startRecording(bssid: String) {
        targetBssid = bssid
        _heatmapPoints.value = emptyList()
        currentX = 500f // Start, center-ish of a canvas
        currentY = 1000f // Start 
        _isRecording.value = true
        sensorHelper.startListening()
        
        // Also start periodic Wifi Sampling
        viewModelScope.launch {
            while (_isRecording.value) {
                // Trigger a scan or just get latest
                wifiScanner.startScan() 
                // We rely on the getScanResults to pull latest data
                recordPoint()
                kotlinx.coroutines.delay(2000) // Sample every 2s
            }
        }
    }
    
    fun stopRecording() {
        _isRecording.value = false
        sensorHelper.stopListening()
    }
    
    private fun recordPoint() {
        targetBssid?.let { bssid ->
            val results = wifiScanner.getScanResults()
            val match = results.find { it.bssid == bssid }
            match?.let {
                val newPoint = HeatmapPoint(currentX, currentY, it.level)
                _heatmapPoints.value = _heatmapPoints.value + newPoint
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorHelper.stopListening()
    }
}
