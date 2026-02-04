package com.example.windplotter

import android.content.Context
import android.util.Log
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DJIConnectionManager {
    private const val TAG = "DJIConnectionManager"

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _sdkInitState = MutableStateFlow("Uninitialized")
    val sdkInitState: StateFlow<String> = _sdkInitState.asStateFlow()

    fun initSDK(context: Context) {
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
            }

            override fun onRegisterFailure(error: IDJIError?) {
                Log.e(TAG, "onRegisterFailure: ${error?.errorCode()}")
                _isRegistered.value = false
                _sdkInitState.value = "Register Failed: ${error?.errorCode()}"
            }

            override fun onProductDisconnect(productId: Int) {
                Log.d(TAG, "onProductDisconnect: $productId")
            }

            override fun onProductConnect(productId: Int) {
                Log.d(TAG, "onProductConnect: $productId")
            }
            
            override fun onProductChanged(productId: Int) {
                 Log.d(TAG, "onProductChanged: $productId")
            }

            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                // Ignore
            }
        })
    }
}
