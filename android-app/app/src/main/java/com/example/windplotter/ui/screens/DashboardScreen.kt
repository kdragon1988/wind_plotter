package com.example.windplotter.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.windplotter.data.Sample
import com.example.windplotter.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onMissionStopped: () -> Unit
) {
    val currentMission by viewModel.currentMission.collectAsState()
    val windSpeed by viewModel.currentWindSpeed.collectAsState()
    val windDirection by viewModel.currentWindDirection.collectAsState()
    val sdkRegistered by viewModel.sdkRegistered.collectAsState()
    val altitude by viewModel.currentAltitude.collectAsState()
    
    val sampleCount by viewModel.sampleCount.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()
    val recentSamples by viewModel.recentSamples.collectAsState()
    val missionSamples by viewModel.missionSamples.collectAsState()
    
    val statusColor = if (currentMission != null) Color(0xFF4CAF50) else Color.Gray

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp) // Reduced padding
    ) {
        // --- Top Bar (Header) ---
        Row(
            modifier = Modifier.fillMaxWidth().height(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(10.dp).background(statusColor, shape = RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentMission != null) "REC: ${currentMission?.name}" else "IDLE",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (sdkRegistered) "SDK: OK" else "SDK: Disconnected",
                style = MaterialTheme.typography.labelSmall,
                color = if (sdkRegistered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        // --- UPPER SECTION: Graph + Metrics (Weight 6) ---
        // Giving more height to the graph/metrics section
        Row(
            modifier = Modifier.weight(6f).fillMaxWidth()
        ) {
            // GRAPH (Left, 2.5/3.5 width)
            Card(
                modifier = Modifier.weight(2.5f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Wind Speed History", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    WindSpeedGraph(
                        samples = missionSamples,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // METRICS (Right, 1/3.5 width, Stacked)
            // User requested smaller windows, so we keep them compact
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Speed
                Card(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    MetricContent(label = "Speed", value = String.format(Locale.US, "%.1f", windSpeed), unit = "m/s", isLarge = true)
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Direction
                Card(modifier = Modifier.weight(0.8f).fillMaxWidth()) {
                    MetricContent(label = "Direction", value = "${windDirection.toInt()}°", unit = "")
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Altitude
                Card(modifier = Modifier.weight(0.8f).fillMaxWidth()) {
                    MetricContent(label = "Altitude", value = String.format(Locale.US, "%.1f", altitude), unit = "m")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // --- LOWER SECTION: Status & Logs (Weight 4) ---
        Row(
           modifier = Modifier.weight(4f).fillMaxWidth() 
        ) {
            // System Status & Controls (Left, 1/3)
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                 Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                     // Changed to ROW to prevent vertical overflow
                     Row(
                         modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                         verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.SpaceAround
                     ) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("RECORDS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                             Text("$sampleCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                         }
                         
                         // Divider
                         Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.6f).background(Color.Gray))

                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("PENDING", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
                             Text("$unsyncedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if(unsyncedCount > 0) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface)
                         }
                     }
                 }
                 
                 Spacer(modifier = Modifier.height(8.dp))
                 
                if (currentMission != null) {
                    Button(
                        onClick = {
                            viewModel.stopMission()
                            onMissionStopped()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().height(40.dp) // Reduced height
                    ) {
                        Text("STOP", fontWeight = FontWeight.Bold)
                    }
                } else {
                     Card(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("FINISHED", style = MaterialTheme.typography.labelLarge)
                         }
                     }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            
            // Recent Logs (Right, 2/3)
            Column(modifier = Modifier.weight(2.5f).fillMaxHeight()) {
                 Text("Recent Logs", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                 Spacer(modifier = Modifier.height(2.dp))
                 
                 // Header
                 Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                     Text("Time", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f), fontSize = 10.sp)
                     Text("Speed", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f), fontSize = 10.sp)
                     Text("Dir", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(0.6f), fontSize = 10.sp)
                     Text("Alt", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(0.8f), fontSize = 10.sp)
                 }
                 Divider()

                 Card(modifier = Modifier.fillMaxSize()) {
                     LazyColumn(
                         modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)
                     ) {
                         items(recentSamples) { sample ->
                             CompactLogRow(sample)
                             Divider(color = Color.LightGray, thickness = 0.5.dp)
                         }
                     }
                 }
            }
        }
    }
}

@Composable
fun MetricContent(label: String, value: String, unit: String, isLarge: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(
            text = value,
            // Adjusted font sizes
            style = if (isLarge) MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            fontWeight = FontWeight.Bold
        )
        if (unit.isNotEmpty()) {
            Text(unit, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun WindSpeedGraph(
    samples: List<Sample>,
    modifier: Modifier = Modifier
) {
    if (samples.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No Data", color = Color.Gray)
        }
        return
    }

    // Graph Configuration
    val maxSpeed = max(samples.maxOfOrNull { it.windSpeed } ?: 5f, 5f) // Minimum 5m/s scale
    val lineColor = MaterialTheme.colorScheme.primary
    val areaColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 10f

        val path = Path()
        
        // Reverse samples to be chronological (Oldest -> Newest) for drawing
        // *Assuming getSamplesForMission returns ordered by seq ASC. 
        // If it's the full list, it usually is ASC.
        // Let's assume samples is ASC.
        
        val count = samples.size
        if (count < 2) return@Canvas

        val stepX = width / (count - 1).coerceAtLeast(1)
        
        samples.forEachIndexed { index, sample ->
            val x = index * stepX
            // Y is inverted (0 at top). Map speed 0..max to height..0
            val ratio = (sample.windSpeed / maxSpeed).coerceIn(0f, 1f)
            val y = height - (ratio * height)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw Line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Fill Area
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()
        
        drawPath(
            path = fillPath,
            color = areaColor
        )
        
        // Draw Reference Line for Max Speed (optional)
        drawLine(
            color = Color.Gray.copy(alpha=0.5f),
            start = Offset(0f, 0f),
            end = Offset(width, 0f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
fun CompactLogRow(sample: Sample) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val time = dateFormat.format(Date(sample.timestamp))
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Using matching weights for table-like alignment
        Text(time, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text("${String.format("%.1f", sample.windSpeed)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("${sample.windDirection.toInt()}°", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.6f))
        Text("${String.format("%.1f", sample.altitude)}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.8f))
    }
}
