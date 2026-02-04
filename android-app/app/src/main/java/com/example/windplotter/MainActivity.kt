package com.example.windplotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.windplotter.ui.screens.DashboardScreen
import com.example.windplotter.ui.screens.MissionCreationScreen
import com.example.windplotter.viewmodel.MainViewModel
import com.example.windplotter.viewmodel.MainViewModelFactory

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    
    // Permissions required by DJI SDK
    private val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.VIBRATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as WindPlotterApp
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel(
                        factory = MainViewModelFactory(
                            app.database.missionDao(),
                            app.database.sampleDao()
                        )
                    )
                    
                    var permissionsGranted by remember { mutableStateOf(checkPermissions()) }
                    
                    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        permissionsGranted = permissions.entries.all { it.value }
                        if (permissionsGranted) {
                            DJIConnectionManager.initSDK(this@MainActivity)
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                         if (!permissionsGranted) {
                             launcher.launch(requiredPermissions)
                         } else {
                             DJIConnectionManager.initSDK(this@MainActivity)
                         }
                    }

                    if (!permissionsGranted) {
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             Text("Requesting Permissions...")
                         }
                    } else {
                        val navController = rememberNavController()
                        val currentMission by viewModel.currentMission.collectAsState()
                        
                        // Determine start destination
                        val startDestination = if (currentMission != null) "dashboard" else "start"
                        
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("start") {
                                MissionCreationScreen(
                                    viewModel = viewModel,
                                    onMissionStarted = {
                                        navController.navigate("dashboard") {
                                            popUpTo("start") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onMissionStopped = {
                                        // For MVP, just go back to start screen when stopped
                                        navController.navigate("start") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun checkPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
