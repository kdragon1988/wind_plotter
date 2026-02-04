package com.example.windplotter

import androidx.multidex.MultiDexApplication
import android.content.Context
import com.example.windplotter.data.AppDatabase
import com.secneo.sdk.Helper

class WindPlotterApp : MultiDexApplication() {
    val database by lazy { AppDatabase.getDatabase(this) }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        try {
            android.util.Log.d("WindPlotterApp", "Attempting to install DJI SDK Helper")
            System.loadLibrary("c++_shared")
            Helper.install(this)
            android.util.Log.d("WindPlotterApp", "DJI SDK Helper installed successfully")
        } catch (e: Exception) {
            android.util.Log.e("WindPlotterApp", "Failed to install DJI SDK Helper", e)
        } catch (t: Throwable) {
            android.util.Log.e("WindPlotterApp", "Fatal error installing DJI SDK Helper", t)
        }
    }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("WindPlotterApp", "onCreate called")
    }
}
