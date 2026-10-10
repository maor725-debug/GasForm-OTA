package com.example.myapplication158.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.provider.Settings
import android.util.Log
import com.example.myapplication158.TechnicianProfile
import io.github.jan.supabase.postgrest.postgrest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val androidId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_DEVICE"
        
        val fullName = inputData.getString("full_name") ?: return Result.failure()
        val licenseNumber = inputData.getString("license_number") ?: return Result.failure()
        val licenseExpiry = inputData.getString("license_expiry") ?: return Result.failure()
        val technicianLevel = inputData.getString("technician_level") ?: return Result.failure()
        val technicianCity = inputData.getString("technician_city") ?: ""

        return try {
            val currentProfile = SupabaseManager.client.postgrest["technicians_profiles"]
                .select { filter { eq("device_id", androidId) } }
                .decodeSingleOrNull<TechnicianProfile>()

            val wasBlocked = currentProfile?.is_blocked == true
            val adminReason = currentProfile?.block_reason ?: "ללא סיבה"
            val oldLog = currentProfile?.correction_log ?: ""

            var newLog = oldLog
            if (wasBlocked) {
                val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                var changes = ""
                // בהעדר המידע הישן בצורה פשוטה, נציין שעודכן פרופיל לחילוץ מחסימה
                changes += "[עדכון פרופיל מלא לשחרור חסימה]"
                val logEntry = "\n--- $timestamp ---\nנחסם בגין: $adminReason\nתיקון שבוצע: $changes\n"
                newLog += logEntry
            }

            val profile = TechnicianProfile(
                device_id = androidId,
                full_name = fullName,
                license_number = licenseNumber,
                license_expiry = licenseExpiry,
                technician_level = technicianLevel,
                city = technicianCity,
                is_blocked = false,
                block_reason = if (wasBlocked) "" else currentProfile?.block_reason,
                correction_log = newLog.trim()
            )
            
            SupabaseManager.client.postgrest["technicians_profiles"].upsert(profile)
            Log.d("ProfileSyncWorker", "Successfully synced technician profile to server.")
            Result.success()
        } catch (e: Exception) {
            Log.e("ProfileSyncWorker", "Failed to sync profile to server. Retrying later...", e)
            Result.retry() // ניסיון חוזר אוטומטי כשיחזור האינטרנט
        }
    }
}
