package com.example.windplotter.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.windplotter.ui.components.TacticalPanel
import com.example.windplotter.ui.components.TacticalUi
import com.example.windplotter.ui.components.tacticalBackground
import com.example.windplotter.viewmodel.MainViewModel

@Composable
fun MissionCreationScreen(
    viewModel: MainViewModel,
    onMissionStarted: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val sdkState by viewModel.sdkInitState.collectAsState()
    var missionName by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val canStart = missionName.isNotBlank() && assignee.isNotBlank()
    val sdkText = sdkState.toString()
    val sdkReady = sdkText.contains("Registered", ignoreCase = true) || sdkText.contains("Ready", ignoreCase = true)

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
                title = "WIND PLOTTER",
                modifier = Modifier.weight(1.2f),
                bodyPadding = 8.dp,
                titleBottomSpacing = 2.dp
            ) {
                Text(
                    text = "TACTICAL MISSION CONSOLE",
                    color = TacticalUi.text,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            TacticalPanel(
                title = "SDK STATUS",
                modifier = Modifier.weight(1.8f),
                bodyPadding = 8.dp,
                titleBottomSpacing = 2.dp
            ) {
                Text(
                    text = sdkText,
                    color = if (sdkReady) TacticalUi.good else TacticalUi.warn,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TacticalPanel(
                title = "MISSION FORM",
                modifier = Modifier
                    .weight(1.28f)
                    .fillMaxHeight(),
                bodyPadding = 12.dp
            ) {
                FormField(
                    label = "MISSION NAME",
                    value = missionName,
                    onValueChange = { missionName = it },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                FormField(
                    label = "ASSIGNEE",
                    value = assignee,
                    onValueChange = { assignee = it },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                FormField(
                    label = "NOTE",
                    value = note,
                    onValueChange = { note = it },
                    singleLine = false,
                    minLines = 3,
                    maxLines = 4
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.72f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalPanel(
                    title = "START",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.68f),
                    bodyPadding = 12.dp
                ) {
                    Button(
                        onClick = {
                            viewModel.startMission(missionName, assignee, note.ifBlank { null })
                            onMissionStarted()
                        },
                        enabled = canStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .border(1.dp, TacticalUi.border, RoundedCornerShape(10.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canStart) TacticalUi.accent.copy(alpha = 0.26f) else TacticalUi.panelTint,
                            disabledContainerColor = TacticalUi.panelTint.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            "OPEN OPERATION UI",
                            color = TacticalUi.text,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (canStart) "Ready for deployment" else "Enter mission name and assignee",
                        color = if (canStart) TacticalUi.good else TacticalUi.muted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                TacticalPanel(
                    title = "REPORT",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.32f),
                    bodyPadding = 10.dp
                ) {
                    Button(
                        onClick = onNavigateToHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, TacticalUi.border, RoundedCornerShape(10.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalUi.panelTint.copy(alpha = 0.55f))
                    ) {
                        Text(
                            "VIEW HISTORY / REPORT",
                            color = TacticalUi.text,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                color = TacticalUi.muted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = TacticalUi.text,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TacticalUi.accent,
            unfocusedBorderColor = TacticalUi.border,
            focusedTextColor = TacticalUi.text,
            unfocusedTextColor = TacticalUi.text,
            focusedContainerColor = TacticalUi.panelTint.copy(alpha = 0.45f),
            unfocusedContainerColor = TacticalUi.panelTint.copy(alpha = 0.35f)
        )
    )
}
