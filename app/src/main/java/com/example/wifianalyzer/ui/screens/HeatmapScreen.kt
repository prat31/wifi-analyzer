package com.example.wifianalyzer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wifianalyzer.viewmodel.HeatmapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

// Cyberpunk / Dark Aesthetics Colors
val DarkBackground = Color(0xFF050510)
val NeonGrid = Color(0xFF00FFCC).copy(alpha = 0.15f)
val NeonPath = Color(0xFF00E5FF)
val UserIndicatorColor = Color(0xFFFF00FF)
val PanelBackground = Color(0xFF1E1E2E).copy(alpha = 0.85f)
val TextPrimary = Color(0xFFE0E0E0)
val TextSecondary = Color(0xFFAAAAAA)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HeatmapScreen(
    bssid: String,
    ssid: String,
    navController: NavController,
) {
    val viewModel: HeatmapViewModel = viewModel()
    val points by viewModel.heatmapPoints.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    val permissions = mutableListOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        permissions.add(android.Manifest.permission.ACTIVITY_RECOGNITION)
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    var zoomScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // --- Interactive Heatmap Canvas ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale *= zoom
                        offset += pan
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasCenter = Offset(size.width / 2, size.height / 2)
                
                translate(left = offset.x + canvasCenter.x, top = offset.y + canvasCenter.y) {
                    scale(zoomScale) {
                        // 1. Draw Infinite Grid
                        val gridSize = 100f
                        // Calculate viewport bounds to optimize drawing (optional, but good for perf)
                        // For now simply draw a large enough area relative to current view or just fixed large area
                        val gridRange = 20 // 20x20 grid around center
                        
                        for (i in -gridRange..gridRange) {
                            // Vertical
                            drawLine(
                                color = NeonGrid,
                                start = Offset(i * gridSize, -gridRange * gridSize),
                                end = Offset(i * gridSize, gridRange * gridSize),
                                strokeWidth = 1f
                            )
                            // Horizontal
                            drawLine(
                                color = NeonGrid,
                                start = Offset(-gridRange * gridSize, i * gridSize),
                                end = Offset(gridRange * gridSize, i * gridSize),
                                strokeWidth = 1f
                            )
                        }

                        // 2. Draw Heatmap Points (Glow Effect)
                        // Use a radial gradient for each point to simulate "glow"
                        points.forEach { point ->
                            val color = getNeonHeatmapColor(point.rssi)
                            val radius = 60f 
                            
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0.0f)),
                                    center = Offset(point.x, point.y),
                                    radius = radius
                                ),
                                radius = radius,
                                center = Offset(point.x, point.y),
                            )
                        }

                        // 3. Draw Path Line
                        if (points.isNotEmpty()) {
                            val path = Path()
                            path.moveTo(points.first().x, points.first().y)
                            points.forEach { path.lineTo(it.x, it.y) }
                            
                            drawPath(
                                path = path,
                                color = NeonPath.copy(alpha = 0.5f),
                                style = Stroke(
                                    width = 3f,
                                    cap = StrokeCap.Round
                                )
                            )
                        }

                        // 4. Draw User Current Position Indicator
                        if (points.isNotEmpty()) {
                            val last = points.last()
                            drawCircle(
                                color = UserIndicatorColor,
                                radius = 8f,
                                center = Offset(last.x, last.y)
                            )
                            drawCircle(
                                color = UserIndicatorColor.copy(alpha = 0.3f),
                                radius = 16f,
                                center = Offset(last.x, last.y)
                            )
                        }
                    }
                }
            }
        }

        // --- Glassmorphic UI Overlays ---

        // Top Bar
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                colors = IconButtonDefaults.iconButtonColors(containerColor = PanelBackground)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(PanelBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = ssid,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            // Spacer to balance layout if needed or Settings icon
             Spacer(modifier = Modifier.size(48.dp)) 
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Legend / Info
            if (isRecording) {
                 Text(
                    "Recording path...",
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                 Text(
                    "Walk to map signal",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Main Action Button
            Button(
                onClick = {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        if (permissionState.allPermissionsGranted) {
                            viewModel.startRecording(bssid)
                        } else {
                            permissionState.launchMultiplePermissionRequest()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color(0xFFFF3333) else Color(0xFF00E5FF),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.size(width = 160.dp, height = 56.dp)
            ) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRecording) "STOP" else "START",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

fun getNeonHeatmapColor(rssi: Int): Color {
    // Brighter neon colors for dark mode
    return when {
         rssi >= -50 -> Color(0xFFFF0055) // Neon Red/Pink
         rssi >= -60 -> Color(0xFFFF9900) // Neon Orange
         rssi >= -70 -> Color(0xFFFFEE00) // Neon Yellow
         rssi >= -80 -> Color(0xFF00FFCC) // Neon Cyan
         else -> Color(0xFF0066FF) // Neon Blue
    }
}
