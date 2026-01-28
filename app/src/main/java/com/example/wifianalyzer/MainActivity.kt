package com.example.wifianalyzer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wifianalyzer.ui.screens.DetailScreen
import com.example.wifianalyzer.ui.screens.HomeScreen
import com.example.wifianalyzer.ui.theme.WiFiAnalyzerTheme
import com.example.wifianalyzer.viewmodel.WifiViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    private val viewModel: WifiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            var isDarkTheme by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(isSystemDark) }

            WiFiAnalyzerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(viewModel, isDarkTheme) { isDarkTheme = !isDarkTheme }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppContent(
    viewModel: WifiViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    
    // Permission Handling
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            // Activity Recognition is needed for Step Detector on Android 10+
            // Note: In real app, check version code before adding
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        } else {
            viewModel.startScan()
        }
    }
    
    // Auto-scan when permissions granted
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.startScan()
        }
    }
    
    if (permissionsState.allPermissionsGranted) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onToggleTheme
                )
            }
            composable("detail/{bssid}") { backStackEntry ->
                val bssid = backStackEntry.arguments?.getString("bssid") ?: return@composable
                DetailScreen(bssid = bssid, viewModel = viewModel, navController = navController)
            }
            composable("heatmap/{bssid}/{ssid}") { backStackEntry ->
                val bssid = backStackEntry.arguments?.getString("bssid") ?: return@composable
                val ssid = backStackEntry.arguments?.getString("ssid") ?: "Unknown"
                com.example.wifianalyzer.ui.screens.HeatmapScreen(bssid, ssid, navController)
            }
        }
    } else {
       // Ideally show a rationale UI here
       androidx.compose.material3.Text("Permissions required to scan WiFi.")
    }
}
