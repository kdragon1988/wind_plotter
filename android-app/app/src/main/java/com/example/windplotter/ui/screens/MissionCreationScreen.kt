package com.example.windplotter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.windplotter.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionCreationScreen(
    viewModel: MainViewModel,
    onMissionStarted: () -> Unit
) {
    val sdkState by viewModel.sdkInitState.collectAsState()
    var missionName by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // Scroll state for landscape support
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create New Mission",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "SDK Status: $sdkState",
            style = MaterialTheme.typography.bodyMedium,
            color = if (sdkState.toString().contains("Registered")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = missionName,
            onValueChange = { missionName = it },
            label = { Text("Mission Name (Required)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = assignee,
            onValueChange = { assignee = it },
            label = { Text("Assignee (Required)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (missionName.isNotBlank() && assignee.isNotBlank()) {
                    viewModel.startMission(missionName, assignee, note)
                    onMissionStarted()
                }
            },
            enabled = missionName.isNotBlank() && assignee.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Start Recording")
        }
    }
}
