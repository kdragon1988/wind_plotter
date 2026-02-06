package com.example.windplotter

import android.content.Context
import android.util.Log
import android.view.Surface
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.sdk.keyvalue.key.BatteryKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.common.callback.CommonCallbacks
import dji.v5.manager.KeyManager
import dji.v5.manager.SDKManager
import dji.v5.manager.datacenter.MediaDataCenter
import dji.v5.manager.interfaces.ICameraStreamManager
import dji.v5.manager.interfaces.SDKManagerCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DJIConnectionManager {
    private const val TAG = "DJIConnectionManager"
    private val listenerOwner = Any()

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _sdkInitState = MutableStateFlow("Uninitialized")
    val sdkInitState: StateFlow<String> = _sdkInitState.asStateFlow()

    private val _isProductConnected = MutableStateFlow(false)
    val isProductConnected: StateFlow<Boolean> = _isProductConnected.asStateFlow()

    private val _batteryPercent = MutableStateFlow<Int?>(null)
    val batteryPercent: StateFlow<Int?> = _batteryPercent.asStateFlow()

    private val _availableCameras = MutableStateFlow<List<ComponentIndexType>>(emptyList())
    val availableCameras: StateFlow<List<ComponentIndexType>> = _availableCameras.asStateFlow()

    private val _boundCamera = MutableStateFlow<ComponentIndexType?>(null)
    val boundCamera: StateFlow<ComponentIndexType?> = _boundCamera.asStateFlow()

    private val batteryKey = KeyTools.createKey(BatteryKey.KeyChargeRemainingInPercent, 0)
    private val batteryKeyListener = CommonCallbacks.KeyListener<Int> { _, newValue ->
        _batteryPercent.value = newValue
    }

    private val availableCameraListener = ICameraStreamManager.AvailableCameraUpdatedListener { cameras ->
        _availableCameras.value = cameras ?: emptyList()
    }

    private var cameraStreamManager: ICameraStreamManager? = null
    private var initialized = false

    fun initSDK(context: Context) {
        if (initialized) {
            return
        }
        initialized = true

        SDKManager.getInstance().init(context, object : SDKManagerCallback {
            override fun onInitProcess(event: DJISDKInitEvent?, totalProcess: Int) {
                Log.d(TAG, "onInitProcess: ${event?.name}")
                _sdkInitState.value = "Initializing: ${event?.name}"
                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                     SDKManager.getInstance().registerApp()
                }
            }

            override fun onRegisterSuccess() {
                Log.i(TAG, "onRegisterSuccess")
                _isRegistered.value = true
                _sdkInitState.value = "Registered"
                startRuntimeListeners()
            }

            override fun onRegisterFailure(error: IDJIError?) {
                Log.e(TAG, "onRegisterFailure: ${error?.errorCode()}")
                _isRegistered.value = false
                _sdkInitState.value = "Register Failed: ${error?.errorCode()}"
                _isProductConnected.value = false
            }

            override fun onProductDisconnect(productId: Int) {
                Log.d(TAG, "onProductDisconnect: $productId")
                _isProductConnected.value = false
                _availableCameras.value = emptyList()
                _boundCamera.value = null
                _batteryPercent.value = null
            }

            override fun onProductConnect(productId: Int) {
                Log.d(TAG, "onProductConnect: $productId")
                _isProductConnected.value = true
                _sdkInitState.value = "Product Connected"
                startRuntimeListeners()
            }
            
            override fun onProductChanged(productId: Int) {
                 Log.d(TAG, "onProductChanged: $productId")
                 _isProductConnected.value = true
                 startRuntimeListeners()
            }

            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                // Ignore
            }
        })
    }

    private fun startRuntimeListeners() {
        try {
            KeyManager.getInstance().listen(batteryKey, listenerOwner, true, batteryKeyListener)
        } catch (e: Exception) {
            Log.w(TAG, "Battery listener setup failed", e)
        }

        try {
            cameraStreamManager = MediaDataCenter.getInstance().cameraStreamManager
            cameraStreamManager?.addAvailableCameraUpdatedListener(availableCameraListener)
        } catch (e: Exception) {
            Log.w(TAG, "Camera stream listener setup failed", e)
        }
    }

    private fun selectCamera(preferred: ComponentIndexType?): ComponentIndexType {
        val cameras = _availableCameras.value
        if (preferred != null && cameras.contains(preferred)) {
            return preferred
        }
        return when {
            cameras.contains(ComponentIndexType.LEFT_OR_MAIN) -> ComponentIndexType.LEFT_OR_MAIN
            cameras.isNotEmpty() -> cameras.first()
            else -> ComponentIndexType.LEFT_OR_MAIN
        }
    }

    fun bindCameraStream(
        surface: Surface,
        width: Int,
        height: Int,
        preferredCamera: ComponentIndexType? = null
    ): ComponentIndexType {
        val manager = cameraStreamManager ?: MediaDataCenter.getInstance().cameraStreamManager.also {
            cameraStreamManager = it
        }
        val camera = selectCamera(preferredCamera)
        manager.putCameraStreamSurface(
            camera,
            surface,
            width,
            height,
            ICameraStreamManager.ScaleType.CENTER_CROP
        )
        _boundCamera.value = camera
        return camera
    }

    fun unbindCameraStream(surface: Surface) {
        try {
            cameraStreamManager?.removeCameraStreamSurface(surface)
        } catch (e: Exception) {
            Log.w(TAG, "removeCameraStreamSurface failed", e)
        }
        _boundCamera.value = null
    }
}
