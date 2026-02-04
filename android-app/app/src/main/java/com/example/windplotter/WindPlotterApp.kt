package com.example.windplotter

import androidx.multidex.MultiDexApplication
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.windplotter.data.AppDatabase
import com.example.windplotter.workers.InfoUploadWorker
import com.secneo.sdk.Helper
import java.util.concurrent.TimeUnit

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
        
        setupWorker()
    }

    private fun setupWorker() {
        val uploadWorkRequest = PeriodicWorkRequestBuilder<InfoUploadWorker>(
            repeatInterval = 15, // Minimum 15 minutes
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            InfoUploadWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            uploadWorkRequest
        )
    }
}
