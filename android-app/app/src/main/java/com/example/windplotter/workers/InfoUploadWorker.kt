package com.example.windplotter.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.windplotter.WindPlotterApp
import com.example.windplotter.data.Sample
import kotlinx.coroutines.delay

class InfoUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val sampleDao = (context.applicationContext as WindPlotterApp).database.sampleDao()

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting batch upload work...")

        return try {
            // 1. Fetch unsynced samples (batch size 50)
            val unsyncedSamples = sampleDao.getUnsyncedSamples(limit = 50)
            
            if (unsyncedSamples.isEmpty()) {
                Log.d(TAG, "No unsynced samples found.")
                return Result.success()
            }

            Log.d(TAG, "Found ${unsyncedSamples.size} unsynced samples. Uploading...")

            // 2. Upload (Mocking network call for now)
            val success = uploadData(unsyncedSamples)

            if (success) {
                // 3. Mark as synced
                val ids = unsyncedSamples.map { it.id }
                sampleDao.markAsSynced(ids)
                Log.d(TAG, "Successfully synced ${ids.size} samples.")
                
                // If we had a full batch, there might be more. 
                // We could return Result.retry() or just wait for next period.
                // For battery saving, we return success and wait for next schedule.
                Result.success()
            } else {
                Log.w(TAG, "Upload failed.")
                Result.retry()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during upload work", e)
            Result.retry()
        }
    }

    private suspend fun uploadData(samples: List<Sample>): Boolean {
        // TODO: Replace with actual Retrofit call
        // val response = ApiClient.service.uploadSamples(samples)
        // return response.isSuccessful
        
        delay(1000) // Simulate network latency
        Log.d(TAG, "Uploaded batch: $samples")
        return true
    }

    companion object {
        const val TAG = "InfoUploadWorker"
        const val WORK_NAME = "info_upload_work"
    }
}
