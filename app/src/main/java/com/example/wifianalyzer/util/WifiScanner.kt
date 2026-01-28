package com.example.wifianalyzer.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import com.example.wifianalyzer.model.WifiNetwork

class WifiScanner(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun startScan(): Boolean {
        return try {
            wifiManager.startScan()
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun getScanResults(): List<WifiNetwork> {
        val results: List<ScanResult> = try {
            wifiManager.scanResults
        } catch (e: Exception) {
            emptyList()
        }

        return results.map { scanResult ->
            WifiNetwork(
                ssid = scanResult.SSID ?: "Unknown SSID",
                bssid = scanResult.BSSID ?: "Unknown BSSID",
                level = scanResult.level,
                frequency = scanResult.frequency,
                capabilities = scanResult.capabilities ?: "",
                timestamp = scanResult.timestamp,
                channelWidth = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) scanResult.channelWidth else -1,
                centerFreq0 = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) scanResult.centerFreq0 else -1,
                centerFreq1 = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) scanResult.centerFreq1 else -1
            )
        }
    }
}
