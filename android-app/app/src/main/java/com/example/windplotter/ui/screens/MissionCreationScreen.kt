package com.example.windplotter.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.windplotter.viewmodel.MainViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.windplotter.R

@OptIn(ExperimentalMaterial3Api::class)
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

    // Scroll state for landscape support (failsafe)
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Left Column: Inputs (Weight 1.2) ---
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.visionoid_logo),
                contentDescription = "Logo",
                modifier = Modifier.width(180.dp), // Slightly wider for text logo
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Create New Mission",
                style = MaterialTheme.typography.titleLarge, // Smaller headline
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "SDK Status: $sdkState",
                style = MaterialTheme.typography.bodySmall,
                color = if (sdkState.toString().contains("Registered")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = missionName,
                onValueChange = { missionName = it },
                label = { Text("Mission Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = assignee,
                onValueChange = { assignee = it },
                label = { Text("Assignee") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp), // Fixed height for note
                minLines = 2,
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        // --- Right Column: Actions (Weight 0.8) ---
        Column(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (missionName.isNotBlank() && assignee.isNotBlank()) {
                        viewModel.startMission(missionName, assignee, note)
                        onMissionStarted()
                    }
                },
                enabled = missionName.isNotBlank() && assignee.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text("Start Recording", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onNavigateToHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View History")
            }
        }
    }
}
