package com.example.windplotter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.windplotter.DJIConnectionManager
import com.example.windplotter.data.Mission
import com.example.windplotter.data.MissionDao
import com.example.windplotter.data.MissionStats
import com.example.windplotter.data.Sample
import com.example.windplotter.data.SampleDao
import com.example.windplotter.services.DataCollectionService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

    private val _currentMission = MutableStateFlow<Mission?>(null)
    val currentMission: StateFlow<Mission?> = _currentMission.asStateFlow()

    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    private val _activeSessionIndex = MutableStateFlow<Int?>(null)
    val activeSessionIndex: StateFlow<Int?> = _activeSessionIndex.asStateFlow()

    val sdkRegistered = DJIConnectionManager.isRegistered
    val sdkInitState = DJIConnectionManager.sdkInitState

    @OptIn(ExperimentalCoroutinesApi::class)
    val latestSample: StateFlow<Sample?> = _currentMission.flatMapLatest { mission ->
        if (mission != null) sampleDao.getLatestSample(mission.missionId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentWindSpeed: StateFlow<Float> = latestSample.map { it?.windSpeed ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val currentWindDirection: StateFlow<Float> = latestSample.map { it?.windDirection ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val currentAltitude: StateFlow<Double> = latestSample.map { it?.altitude ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sampleCount: StateFlow<Int> = _currentMission.flatMapLatest { mission ->
        if (mission != null) sampleDao.getSampleCount(mission.missionId) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentSamples: StateFlow<List<Sample>> = _currentMission.flatMapLatest { mission ->
        if (mission != null) sampleDao.getRecentSamples(mission.missionId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val missionSamples: StateFlow<List<Sample>> = _currentMission.flatMapLatest { mission ->
        if (mission != null) sampleDao.getSamplesForMission(mission.missionId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessionIndexes: StateFlow<List<Int>> = _currentMission.flatMapLatest { mission ->
        if (mission != null) sampleDao.getSessionIndexesForMission(mission.missionId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsyncedCount: StateFlow<Int> = sampleDao.getUnsyncedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        checkActiveMission()
    }

    private fun checkActiveMission() {
        viewModelScope.launch {
            _currentMission.value = missionDao.getActiveMission()
            _isMeasuring.value = false
            _activeSessionIndex.value = null
        }
    }

    fun startMission(name: String, assignee: String, note: String?) {
        viewModelScope.launch {
            val newMission = Mission(
                name = name,
                assignee = assignee,
                note = note,
                status = "RECORDING",
                sessionCount = 0
            )
            missionDao.insert(newMission)
            _currentMission.value = newMission
            _isMeasuring.value = false
            _activeSessionIndex.value = null
        }
    }

    fun resumeMission(missionId: String) {
        viewModelScope.launch {
            val mission = missionDao.getMissionById(missionId) ?: return@launch
            val resumed = if (mission.status == "FINISHED") mission.copy(status = "RECORDING") else mission
            if (resumed != mission) {
                missionDao.update(resumed)
            }
            _currentMission.value = resumed
            _isMeasuring.value = false
            _activeSessionIndex.value = null
        }
    }

    fun startMeasurement() {
        viewModelScope.launch {
            val mission = _currentMission.value ?: return@launch
            if (_isMeasuring.value) return@launch

            val nextSession = mission.sessionCount + 1
            val updatedMission = mission.copy(
                status = "RECORDING",
                sessionCount = nextSession,
                lastMeasuredAt = System.currentTimeMillis()
            )
            missionDao.update(updatedMission)
            _currentMission.value = updatedMission

            DataCollectionService.start(getApplication(), updatedMission.missionId, nextSession)
            _isMeasuring.value = true
            _activeSessionIndex.value = nextSession
        }
    }

    fun stopMeasurement() {
        if (!_isMeasuring.value) return
        DataCollectionService.stop(getApplication())
        _isMeasuring.value = false
        _activeSessionIndex.value = null
    }

    fun stopMission() {
        viewModelScope.launch {
            stopMeasurement()
            _currentMission.value?.let { mission ->
                val finishedMission = mission.copy(status = "FINISHED")
                missionDao.update(finishedMission)
                _currentMission.value = null
            }
        }
    }

    val allMissions: StateFlow<List<Mission>> = missionDao.getAllMissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteMission(mission: Mission) {
        viewModelScope.launch {
            missionDao.delete(mission)
        }
    }

    fun getMissionStats(missionId: String): Flow<MissionStats?> {
        return sampleDao.getMissionStats(missionId)
    }

    fun getMissionSamples(missionId: String): Flow<List<Sample>> {
        return sampleDao.getSamplesForMission(missionId)
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
