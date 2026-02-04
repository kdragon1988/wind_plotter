package com.example.windplotter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.windplotter.viewmodel.MainViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onMissionStopped: () -> Unit
) {
    val currentMission by viewModel.currentMission.collectAsState()
    val windSpeed by viewModel.currentWindSpeed.collectAsState()
    val windDirection by viewModel.currentWindDirection.collectAsState()
    val sdkRegistered by viewModel.sdkRegistered.collectAsState()
    
    // Status color
    val statusColor = if (currentMission != null) Color(0xFF4CAF50) else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, shape = RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentMission != null) "RECORDING: ${currentMission?.name}" else "IDLE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (sdkRegistered) "SDK: OK" else "SDK: Disconnected",
                style = MaterialTheme.typography.bodySmall,
                color = if (sdkRegistered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Metric: Wind Speed
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Wind Speed",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = String.format(Locale.US, "%.1f", windSpeed),
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "m/s",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Metrics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Direction", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${windDirection.toInt()}°",
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Altitude", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "100m", // Dummy for now
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        if (currentMission != null) {
            Button(
                onClick = {
                    viewModel.stopMission()
                    onMissionStopped()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("STOP RECORDING")
            }
        } else {
             // In case we land here without active mission (e.g. just finished)
             Text(
                text = "Mission Finished",
                modifier = Modifier.align(Alignment.CenterHorizontally)
             )
        }
    }
}
