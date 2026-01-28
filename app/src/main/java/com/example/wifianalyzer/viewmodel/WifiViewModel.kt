package com.example.wifianalyzer.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifianalyzer.model.WifiNetwork
import com.example.wifianalyzer.util.WifiScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WifiViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = WifiScanner(application)
    private val _wifiNetworks = MutableStateFlow<List<WifiNetwork>>(emptyList())
    val wifiNetworks: StateFlow<List<WifiNetwork>> = _wifiNetworks.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        application.registerReceiver(wifiScanReceiver, intentFilter)
    }

    fun startScan() {
        viewModelScope.launch {
            if (_isScanning.value) return@launch
            
            while (true) {
                _isScanning.value = true
                val success = scanner.startScan()
                if (!success) {
                    // Scan failed (likely throttled), just get old results
                    scanFailure()
                }
                // Wait 10 seconds before next scan
                kotlinx.coroutines.delay(10000L)
            }
        }
    }

    private fun scanSuccess() {
        _wifiNetworks.value = scanner.getScanResults().sortedByDescending { it.level }
        _isScanning.value = false
    }

    private fun scanFailure() {
        // Even if scan fails, we can try to retrieve older results
        val results = scanner.getScanResults()
        if (results.isNotEmpty()) {
            _wifiNetworks.value = results.sortedByDescending { it.level }
        }
        _isScanning.value = false
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(wifiScanReceiver)
    }
}
