package com.example.windplotter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.windplotter.DJIConnectionManager
import com.example.windplotter.data.Mission
import com.example.windplotter.data.MissionDao
import com.example.windplotter.data.Sample
import com.example.windplotter.data.SampleDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val missionDao: MissionDao,
    private val sampleDao: SampleDao
) : ViewModel() {

    // Current active mission (if any)
    private val _currentMission = MutableStateFlow<Mission?>(null)
    val currentMission: StateFlow<Mission?> = _currentMission.asStateFlow()

    // Status of SDK registration
    val sdkRegistered = DJIConnectionManager.isRegistered
    val sdkInitState = DJIConnectionManager.sdkInitState

    // Mock Live Data (Placeholder until SDK Listener implementation)
    private val _currentWindSpeed = MutableStateFlow(0.0f)
    val currentWindSpeed: StateFlow<Float> = _currentWindSpeed.asStateFlow()

    private val _currentWindDirection = MutableStateFlow(0.0f)
    val currentWindDirection: StateFlow<Float> = _currentWindDirection.asStateFlow()
    
    // Sample count for current mission
    val sampleCount = _currentMission.combine(sampleDao.getSampleCount("")) { mission, _ ->
         if (mission != null) {
             // Re-query with actual ID would be better, but for flow logic we might need a flat map
             // Simplified for now: we will just observe DB count when mission is active
             0 // Placeholder, logic below is better
         } else {
             0
         }
    }

    init {
        checkActiveMission()
        startMockDataUpdates() // For UI testing
    }

    private fun checkActiveMission() {
        viewModelScope.launch {
            val active = missionDao.getActiveMission()
            _currentMission.value = active
        }
    }

    fun startMission(name: String, assignee: String, note: String?) {
        viewModelScope.launch {
            val newMission = Mission(
                name = name,
                assignee = assignee,
                note = note,
                status = "RECORDING"
            )
            missionDao.insert(newMission)
            _currentMission.value = newMission
        }
    }

    fun stopMission() {
        viewModelScope.launch {
            _currentMission.value?.let { mission ->
                val finishedMission = mission.copy(status = "FINISHED")
                missionDao.update(finishedMission)
                _currentMission.value = null
            }
        }
    }
    
    // Preliminary method to simulate data coming in
    private fun startMockDataUpdates() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (_currentMission.value != null) {
                    // Random fluctuation
                    _currentWindSpeed.value = (Math.random() * 10).toFloat()
                    _currentWindDirection.value = (Math.random() * 360).toFloat()
                    
                    // Save sample
                    val sample = Sample(
                        missionId = _currentMission.value!!.missionId,
                        timestamp = System.currentTimeMillis(),
                        seq = (System.currentTimeMillis() / 1000).toInt(), // Simplified seq
                        latitude = 35.6895,
                        longitude = 139.6917,
                        altitude = 100.0,
                        windSpeed = _currentWindSpeed.value,
                        windDirection = _currentWindDirection.value
                    )
                    sampleDao.insert(sample)
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val missionDao: MissionDao,
    private val sampleDao: SampleDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(missionDao, sampleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
