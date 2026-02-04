package com.example.windplotter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.windplotter.DJIConnectionManager
import com.example.windplotter.data.Mission
import com.example.windplotter.data.MissionDao
import com.example.windplotter.data.Sample
import com.example.windplotter.data.SampleDao
import com.example.windplotter.services.DataCollectionService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val missionDao: MissionDao,
    private val sampleDao: SampleDao
) : AndroidViewModel(application) {

    // Current active mission (if any)
    private val _currentMission = MutableStateFlow<Mission?>(null)
    val currentMission: StateFlow<Mission?> = _currentMission.asStateFlow()

    // Status of SDK registration
    val sdkRegistered = DJIConnectionManager.isRegistered
    val sdkInitState = DJIConnectionManager.isRegistered

    // Live Data observed from DB
    @OptIn(ExperimentalCoroutinesApi::class)
    val latestSample: StateFlow<Sample?> = _currentMission.flatMapLatest { mission ->
        if (mission != null) {
            sampleDao.getLatestSample(mission.missionId)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentWindSpeed: StateFlow<Float> = latestSample.map { it?.windSpeed ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val currentWindDirection: StateFlow<Float> = latestSample.map { it?.windDirection ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val currentAltitude: StateFlow<Double> = latestSample.map { it?.altitude ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sampleCount = _currentMission.flatMapLatest { mission ->
        if (mission != null) {
            sampleDao.getSampleCount(mission.missionId)
        } else {
            flowOf(0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentSamples: StateFlow<List<Sample>> = _currentMission.flatMapLatest { mission ->
        if (mission != null) {
            sampleDao.getRecentSamples(mission.missionId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsyncedCount: StateFlow<Int> = sampleDao.getUnsyncedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val missionSamples: StateFlow<List<Sample>> = _currentMission.flatMapLatest { mission ->
        if (mission != null) {
            sampleDao.getSamplesForMission(mission.missionId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkActiveMission()
    }

    private fun checkActiveMission() {
        viewModelScope.launch {
            val active = missionDao.getActiveMission()
            _currentMission.value = active
            // If there is an active mission on startup, restart the service?
            // Ideally yes, to resume recording if app crashed.
            if (active != null && active.status == "RECORDING") {
                DataCollectionService.start(getApplication(), active.missionId)
            }
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
            
            // Start Service
            DataCollectionService.start(getApplication(), newMission.missionId)
        }
    }

    fun stopMission() {
        viewModelScope.launch {
            _currentMission.value?.let { mission ->
                val finishedMission = mission.copy(status = "FINISHED")
                missionDao.update(finishedMission)
                _currentMission.value = null
                
                // Stop Service
                DataCollectionService.stop(getApplication())
            }
        }
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val missionDao: MissionDao,
    private val sampleDao: SampleDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, missionDao, sampleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
