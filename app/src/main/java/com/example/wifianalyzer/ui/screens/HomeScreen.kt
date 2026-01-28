package com.example.wifianalyzer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifi0Bar
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wifianalyzer.model.WifiNetwork
import com.example.wifianalyzer.viewmodel.WifiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WifiViewModel,
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val wifiNetworks by viewModel.wifiNetworks.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WiFi Analyzer") },
                actions = {
                    androidx.compose.material3.IconButton(onClick = onThemeToggle) {
                         // Simple toggle logic for icon: if currently dark, show Sun (to switch to light), else Moon
                         val icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode
                         Icon(icon, contentDescription = "Toggle Theme")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.startScan() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Scan")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (wifiNetworks.isEmpty() && !isScanning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No WiFi networks found. Pull/Tap refresh to scan.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(wifiNetworks) { network ->
                        WifiNetworkItem(network = network, onClick = {
                            // Use encoded SSID/BSSID to pass safely, or just pass BSSID as ID
                            val encodedBssid = android.net.Uri.encode(network.bssid)
                            navController.navigate("detail/$encodedBssid")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun WifiNetworkItem(
    network: WifiNetwork,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getSignalIcon(network.level),
                contentDescription = "Signal Strength",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (network.ssid.isBlank()) "(Hidden Network)" else network.ssid,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${network.frequency} MHz | ${network.level} dBm",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "${network.signalQuality}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

fun getSignalIcon(level: Int): ImageVector {
    return if (level > -50) Icons.Default.SignalWifi4Bar
    else if (level > -80) Icons.Default.SignalWifi4Bar // Simplified for now, can add more logic
    else Icons.Default.SignalWifi0Bar
}
