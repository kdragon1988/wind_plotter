package com.example.windplotter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.windplotter.data.Sample
import com.example.windplotter.ui.components.TacticalPanel
import com.example.windplotter.ui.components.TacticalUi
import com.example.windplotter.ui.components.WindSpeedGraph
import com.example.windplotter.ui.components.tacticalBackground
import com.example.windplotter.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MissionDetailScreen(
    missionId: String,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onResumeMission: (String) -> Unit
) {
    val samples by viewModel.getMissionSamples(missionId).collectAsState(initial = emptyList())
    val allMissions by viewModel.allMissions.collectAsState()
    val mission = allMissions.find { it.missionId == missionId }

    var selectedSession by remember(missionId) { mutableStateOf<Int?>(null) } // null = ALL
    val sessionIndexes = remember(samples) { samples.map { it.sessionIndex }.distinct().sorted() }

    LaunchedEffect(sessionIndexes, selectedSession) {
        if (selectedSession != null && selectedSession !in sessionIndexes) {
            selectedSession = null
        }
    }

    val filteredSamples = remember(samples, selectedSession) {
        if (selectedSession == null) samples else samples.filter { it.sessionIndex == selectedSession }
    }
    val stats = remember(filteredSamples) { buildAnalysisStats(filteredSamples) }
    val sessionLabel = selectedSession?.let { "S$it" } ?: "ALL"

    if (mission == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .tacticalBackground(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = TacticalUi.accent)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .tacticalBackground()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TacticalPanel(
                title = "BACK",
                modifier = Modifier.weight(0.42f),
                bodyPadding = 6.dp,
                titleBottomSpacing = 1.dp
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalUi.panelTint.copy(alpha = 0.45f))
                ) {
                    Text(
                        "HOME",
                        color = TacticalUi.text,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            TacticalPanel(
                title = "REPORT",
                modifier = Modifier.weight(1.58f),
                bodyPadding = 8.dp,
                titleBottomSpacing = 1.dp
            ) {
                Text(
                    text = mission.name,
                    color = TacticalUi.text,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TacticalPanel(
                title = "TARGET",
                modifier = Modifier.weight(1f),
                bodyPadding = 8.dp,
                titleBottomSpacing = 1.dp
            ) {
                Text(
                    text = sessionLabel,
                    color = TacticalUi.accent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.44f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalPanel(
                    title = "SESSION SELECT",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.22f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SessionButton(
                            label = "ALL",
                            selected = selectedSession == null,
                            onClick = { selectedSession = null }
                        )
                        sessionIndexes.forEach { session ->
                            SessionButton(
                                label = "S$session",
                                selected = selectedSession == session,
                                onClick = { selectedSession = session }
                            )
                        }
                    }
                }

                TacticalPanel(
                    title = "ANALYSIS",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.44f)
                ) {
                    InfoLine("SAMPLES", stats.count.toString())
                    InfoLine("MAX WIND", String.format(Locale.US, "%.1f m/s", stats.maxWind))
                    InfoLine("AVG WIND", String.format(Locale.US, "%.1f m/s", stats.avgWind))
                    InfoLine("AVG ALT", String.format(Locale.US, "%.1f m", stats.avgAlt))
                    InfoLine("MAIN DIR", stats.mainDirection)
                    InfoLine("RISK COUNT", "${stats.cautionCount + stats.dangerCount}")
                }

                TacticalPanel(
                    title = "MISSION INFO",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.34f)
                ) {
                    InfoLine("ASSIGNEE", mission.assignee)
                    InfoLine("CREATED", formatDateTime(mission.createdAt))
                    if (!mission.note.isNullOrBlank()) {
                        InfoLine("NOTE", mission.note)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { onResumeMission(missionId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .border(1.dp, TacticalUi.border, RoundedCornerShape(10.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalUi.panelTint.copy(alpha = 0.55f))
                    ) {
                        Text(
                            "RESUME OPERATION UI",
                            color = TacticalUi.text,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.56f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalPanel(
                    title = "WIND TREND [$sessionLabel]",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.76f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TacticalUi.panelTint.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    ) {
                        if (filteredSamples.isEmpty()) {
                            Text(
                                "No data for selected session",
                                color = TacticalUi.muted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            WindSpeedGraph(
                                samples = filteredSamples,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                TacticalPanel(
                    title = "TIME WINDOW",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.24f)
                ) {
                    InfoLine("START", stats.startTimeText)
                    InfoLine("END", stats.endTimeText)
                    InfoLine("DURATION", stats.durationText)
                    InfoLine("DANGER(>=10)", stats.dangerCount.toString(), TacticalUi.danger)
                }
            }
        }
    }
}

@Composable
private fun SessionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) TacticalUi.accent.copy(alpha = 0.25f) else TacticalUi.panelTint.copy(alpha = 0.5f)
        ),
        modifier = Modifier.height(34.dp)
    ) {
        Text(
            text = label,
            color = if (selected) TacticalUi.accent else TacticalUi.text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = TacticalUi.text) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TacticalUi.muted,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = valueColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    Spacer(modifier = Modifier.height(3.dp))
}

private data class AnalysisStats(
    val count: Int,
    val maxWind: Float,
    val avgWind: Float,
    val avgAlt: Double,
    val mainDirection: String,
    val cautionCount: Int,
    val dangerCount: Int,
    val startTimeText: String,
    val endTimeText: String,
    val durationText: String
)

private fun buildAnalysisStats(samples: List<Sample>): AnalysisStats {
    if (samples.isEmpty()) {
        return AnalysisStats(
            count = 0,
            maxWind = 0f,
            avgWind = 0f,
            avgAlt = 0.0,
            mainDirection = "--",
            cautionCount = 0,
            dangerCount = 0,
            startTimeText = "--",
            endTimeText = "--",
            durationText = "00:00"
        )
    }

    val maxWind = samples.maxOf { it.windSpeed }
    val avgWind = samples.map { it.windSpeed }.average().toFloat()
    val avgAlt = samples.map { it.altitude }.average()
    val cautionCount = samples.count { it.windSpeed >= 5f && it.windSpeed < 10f }
    val dangerCount = samples.count { it.windSpeed >= 10f }
    val start = samples.minOf { it.timestamp }
    val end = samples.maxOf { it.timestamp }
    val durationMs = (end - start).coerceAtLeast(0L)
    val min = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val sec = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    val direction = samples
        .groupingBy { toDirectionText(it.windDirection) }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key ?: "--"

    return AnalysisStats(
        count = samples.size,
        maxWind = maxWind,
        avgWind = avgWind,
        avgAlt = avgAlt,
        mainDirection = direction,
        cautionCount = cautionCount,
        dangerCount = dangerCount,
        startTimeText = formatDateTime(start),
        endTimeText = formatDateTime(end),
        durationText = String.format(Locale.getDefault(), "%02d:%02d", min, sec)
    )
}

private fun formatDateTime(ts: Long): String {
    return SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(ts))
}

private fun toDirectionText(directionDeg: Float): String {
    val normalized = ((directionDeg % 360f) + 360f) % 360f
    val index = (((normalized + 22.5f) % 360f) / 45f).toInt()
    return when (index) {
        0 -> "北"
        1 -> "北東"
        2 -> "東"
        3 -> "南東"
        4 -> "南"
        5 -> "南西"
        6 -> "西"
        else -> "北西"
    }
}
