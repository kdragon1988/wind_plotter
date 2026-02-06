package com.example.windplotter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.windplotter.ui.components.WindSpeedGraph
import com.example.windplotter.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionDetailScreen(
    missionId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onResumeMission: (String) -> Unit
) {
    val stats by viewModel.getMissionStats(missionId).collectAsState(initial = null)
    val samples by viewModel.getMissionSamples(missionId).collectAsState(initial = emptyList())
    val allMissions by viewModel.allMissions.collectAsState()
    val mission = allMissions.find { it.missionId == missionId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mission?.name ?: "Mission Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (mission == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // --- Left Column: Stats Cards (30%) ---
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text("Basic Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Assignee", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(mission.assignee, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            
                            if (!mission.note.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Note", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Text(mission.note, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onResumeMission(missionId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Resume In Operation UI")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (stats != null) {
                        val s = stats!!
                        val durationMs = s.endTime - s.startTime
                        val durationStr = if (durationMs > 0) {
                            val min = TimeUnit.MILLISECONDS.toMinutes(durationMs)
                            val sec = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
                            String.format("%02d:%02d", min, sec)
                        } else "00:00"
                        
                        StatsCard("Duration & Time") {
                            StatsRow("Duration", durationStr)
                            val df = SimpleDateFormat("HH:mm", Locale.getDefault())
                            StatsRow("Start", df.format(Date(s.startTime)))
                            StatsRow("End", df.format(Date(s.endTime)))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        StatsCard("Wind & Altitude") {
                            StatsRow("Max Wind", String.format("%.1f m/s", s.maxWindSpeed))
                            StatsRow("Avg Wind", String.format("%.1f m/s", s.avgWindSpeed))
                            StatsRow("Avg Alt", String.format("%.1f m", s.avgAltitude))
                        }
                    } else {
                        Text("No statistics available", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // --- Right Column: Graph (70%) ---
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                ) {
                    Text("Wind Speed Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF222222), RoundedCornerShape(8.dp))
                    ) {
                        if (samples.isEmpty()) {
                            Text("No graph data", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                        } else {
                            WindSpeedGraph(
                                samples = samples,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun StatsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
