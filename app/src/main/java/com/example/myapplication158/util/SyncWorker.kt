package com.example.myapplication158.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.provider.Settings
import android.util.Log
import io.github.jan.supabase.postgrest.postgrest

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val count = inputData.getInt("forms_created", -1)
        if (count == -1) {
            return Result.failure()
        }

        val androidId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_DEVICE"
        
        return try {
            SupabaseManager.client.postgrest["trials_tracker"].update(
                mapOf("forms_created" to count)
            ) {
                filter { eq("device_id", androidId) }
            }
            Log.d("SyncWorker", "Successfully synced $count forms to server.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Failed to sync to server. Retrying later...", e)
            Result.retry() // אומר למערכת לנסות שוב כשיהיה אינטרנט או מאוחר יותר
        }
    }
}
