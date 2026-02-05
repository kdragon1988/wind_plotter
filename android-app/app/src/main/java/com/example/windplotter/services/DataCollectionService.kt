package com.example.windplotter.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.windplotter.R
import com.example.windplotter.data.AppDatabase
import com.example.windplotter.data.Sample
// MSDK Imports
import dji.v5.manager.KeyManager
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.value.flightcontroller.FlightMode
import dji.sdk.keyvalue.value.common.LocationCoordinate3D
import dji.v5.common.callback.CommonCallbacks
import dji.v5.manager.interfaces.SDKManagerCallback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class DataCollectionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var collectionJob: Job? = null
    
    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    
    private var currentMissionId: String? = null
    private val sequenceCounter = AtomicInteger(0)

    // Data Cache
    @Volatile private var currentWindSpeed: Int = 0 
    @Volatile private var currentWindDirection: Int = 0 
    @Volatile private var currentLatitude: Double = 0.0
    @Volatile private var currentLongitude: Double = 0.0
    @Volatile private var currentAltitude: Double = 0.0
    
    // Debug
    @Volatile private var currentFlightMode: String = "Unknown"

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_MISSION_ID = "EXTRA_MISSION_ID"
        
        const val CHANNEL_ID = "DataCollectionChannel"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "DataCollectionService"

        fun start(context: Context, missionId: String) {
            val intent = Intent(context, DataCollectionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MISSION_ID, missionId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, DataCollectionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val missionId = intent.getStringExtra(EXTRA_MISSION_ID)
                if (missionId != null) {
                    startCollection(missionId)
                } else {
                    Log.e(TAG, "Mission ID missing")
                    stopSelf()
                }
            }
            ACTION_STOP -> stopCollection()
        }
        return START_NOT_STICKY
    }

    private fun startCollection(missionId: String) {
        Log.d(TAG, "Starting Data Collection Service for Mission: $missionId")
        currentMissionId = missionId
        sequenceCounter.set(0)
        
        createNotificationChannel()
        val notification = createNotification(missionId)
        startForeground(NOTIFICATION_ID, notification)

        initSDKListeners()

        if (collectionJob?.isActive == true) return

        collectionJob = serviceScope.launch {
            while (isActive) {
                collectAndSaveData()
                delay(1000) // 1Hz sampling
            }
        }
    }

    private fun stopCollection() {
        Log.d(TAG, "Stopping Data Collection Service")
        collectionJob?.cancel()
        removeSDKListeners()
        stopForeground(true)
        stopSelf()
    }
    
    // --- SDK Listeners ---

    // Valid Key confirmed from decompiled code
    private val flightModeKey = KeyTools.createKey(FlightControllerKey.KeyFlightMode)
    // Inherited keys from co_v (obfuscated parent class)
    private val windSpeedKey = KeyTools.createKey(FlightControllerKey.KeyWindSpeed)
    private val windDirectionKey = KeyTools.createKey(FlightControllerKey.KeyWindDirection)
    // Optimistic guess for Location key
    private val locationKey = KeyTools.createKey(FlightControllerKey.KeyAircraftLocation3D)

    private val flightModeListener = CommonCallbacks.KeyListener<FlightMode> { oldValue, newValue ->
        if (newValue != null) {
            currentFlightMode = newValue.name
            Log.d(TAG, "FlightMode changed: $currentFlightMode")
        }
    }

    private val windSpeedListener = CommonCallbacks.KeyListener<Int> { oldValue, newValue ->
        if (newValue != null) {
            currentWindSpeed = newValue
        }
    }

    private val windDirectionListener = CommonCallbacks.KeyListener<dji.sdk.keyvalue.value.flightcontroller.WindDirection> { oldValue, newValue ->
        if (newValue != null) {
            currentWindDirection = newValue.ordinal
        }
    }

    private val locationListener = CommonCallbacks.KeyListener<LocationCoordinate3D> { oldValue, newValue ->
        if (newValue != null) {
            currentLatitude = newValue.latitude
            currentLongitude = newValue.longitude
            currentAltitude = newValue.altitude
        }
    }

    private fun initSDKListeners() {
        val keyManager = KeyManager.getInstance()
        
        if (flightModeKey != null) keyManager.listen(flightModeKey, this, flightModeListener)
        if (windSpeedKey != null) keyManager.listen(windSpeedKey, this, windSpeedListener)
        if (windDirectionKey != null) keyManager.listen(windDirectionKey, this, windDirectionListener)
        if (locationKey != null) keyManager.listen(locationKey, this, locationListener)
    }

    private fun removeSDKListeners() {
        val keyManager = KeyManager.getInstance()
        keyManager.cancelListen(this)
    }

    // --- Data Saving ---

    private suspend fun collectAndSaveData() {
        val missionId = currentMissionId ?: return
        
        // Mocking wind/location data for now until keys are resolved
        // currentLatitude, currentWindSpeed etc. remain 0/default
        
        val sample = Sample(
            missionId = missionId,
            timestamp = System.currentTimeMillis(),
            seq = sequenceCounter.getAndIncrement(),
            latitude = currentLatitude,
            longitude = currentLongitude,
            altitude = currentAltitude,
            windSpeed = currentWindSpeed.toFloat() / 10.0f, // SDK returns decimeters/sec, convert to m/s
            windDirection = currentWindDirection.toFloat(),
            windWarningLevel = currentWindSpeed 
        )
        
        try {
            database.sampleDao().insert(sample)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving sample (Mode: $currentFlightMode)", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Data Collection Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(missionId: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WindPlotter")
            .setContentText("Mission: $missionId - Recording... (Mode: $currentFlightMode)")
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Standard icon to avoid resource error
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        collectionJob?.cancel()
        removeSDKListeners()
    }
}
