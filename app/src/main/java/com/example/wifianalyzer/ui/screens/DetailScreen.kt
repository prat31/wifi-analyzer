package com.example.wifianalyzer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wifianalyzer.viewmodel.WifiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    bssid: String,
    viewModel: WifiViewModel,
    navController: NavController
) {
    val wifiNetworks by viewModel.wifiNetworks.collectAsState()
    val network = wifiNetworks.find { it.bssid == bssid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(network?.ssid ?: "Network Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (network == null) {
            Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                Text("Network not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                DetailItem(label = "SSID", value = network.ssid)
                DetailItem(label = "BSSID", value = network.bssid)

                // Visual Signal Indicator
                SignalZoneIndicator(network.signalZone)
                
                // Heatmap Entry
                androidx.compose.material3.Button(
                    onClick = { 
                        val encodedBssid = android.net.Uri.encode(network.bssid)
                        val encodedSsid = android.net.Uri.encode(network.ssid)
                        navController.navigate("heatmap/$encodedBssid/$encodedSsid") 
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Open Signal Heatmap")
                }
                
                DetailItem(label = "Signal Strength", value = "${network.level} dBm")
                DetailItem(label = "Signal Quality", value = "${network.signalQuality}%")
                DetailItem(label = "Frequency", value = "${network.frequency} MHz")
                DetailItem(label = "Channel Width", value = "${network.channelWidth} MHz")
                DetailItem(label = "Capabilities", value = network.capabilities)
                DetailItem(label = "Center Freq 0", value = "${network.centerFreq0}")
                DetailItem(label = "Center Freq 1", value = "${network.centerFreq1}")
                DetailItem(label = "Timestamp", value = "${network.timestamp}")
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun SignalZoneIndicator(zone: com.example.wifianalyzer.model.SignalZone) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(zone.colorHex).copy(alpha = 0.2f)
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(text = zone.emoji, style = MaterialTheme.typography.displayMedium)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Text(
                    text = zone.label,
                    style = MaterialTheme.typography.headlineSmall,
                    color = androidx.compose.ui.graphics.Color(zone.colorHex)
                )
                Text(
                    text = "Signal Quality Indicator",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
