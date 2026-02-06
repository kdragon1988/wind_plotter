package com.example.windplotter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.windplotter.data.Mission
import com.example.windplotter.ui.components.TacticalPanel
import com.example.windplotter.ui.components.TacticalUi
import com.example.windplotter.ui.components.tacticalBackground
import com.example.windplotter.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MissionListScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onMissionClick: (String) -> Unit
) {
    val allMissions by viewModel.allMissions.collectAsState()
    var missionToDelete by remember { mutableStateOf<Mission?>(null) }

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
                        fontSize = 10.sp
                    )
                }
            }

            TacticalPanel(
                title = "MISSION HISTORY",
                modifier = Modifier.weight(1.58f),
                bodyPadding = 8.dp,
                titleBottomSpacing = 1.dp
            ) {
                Text(
                    text = "WIND PLOTTER REPORT ARCHIVE",
                    color = TacticalUi.text,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TacticalPanel(
                title = "TOTAL",
                modifier = Modifier.weight(1f),
                bodyPadding = 8.dp,
                titleBottomSpacing = 1.dp
            ) {
                Text(
                    text = "${allMissions.size}",
                    color = TacticalUi.accent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TacticalPanel(
            title = "MISSIONS",
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (allMissions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No missions recorded.",
                        color = TacticalUi.muted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(items = allMissions, key = { it.missionId }) { mission ->
                        MissionCompactRow(
                            mission = mission,
                            onClick = { onMissionClick(mission.missionId) },
                            onDelete = { missionToDelete = mission }
                        )
                    }
                }
            }
        }
    }

    if (missionToDelete != null) {
        AlertDialog(
            onDismissRequest = { missionToDelete = null },
            title = { Text("Delete Mission") },
            text = {
                Text("Delete '${missionToDelete?.name}'? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        missionToDelete?.let { viewModel.deleteMission(it) }
                        missionToDelete = null
                    }
                ) {
                    Text("Delete", color = TacticalUi.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { missionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MissionCompactRow(
    mission: Mission,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(mission.createdAt))
    val statusColor = when (mission.status) {
        "RECORDING" -> TacticalUi.good
        "FINISHED" -> TacticalUi.warn
        "SYNCED" -> TacticalUi.accent
        else -> TacticalUi.muted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .border(1.dp, TacticalUi.border.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .background(TacticalUi.panelTint.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$dateStr | ${mission.assignee} | S:${mission.sessionCount}",
                color = TacticalUi.muted,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = mission.status,
            color = statusColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 9.sp
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .height(26.dp)
                .width(26.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = TacticalUi.muted
            )
        }
    }
}
