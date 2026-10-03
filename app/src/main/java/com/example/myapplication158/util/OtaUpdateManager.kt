package com.example.myapplication158.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val versionCode: Int, val versionName: String, val releaseNotes: String)

class OtaUpdateManager(private val context: Context) {

    suspend fun checkForUpdates(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://raw.githubusercontent.com/maor725-debug/GasForm-OTA/main/update.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val latestVersionCode = json.getInt("versionCode")
                if (latestVersionCode > currentVersionCode) {
                    return@withContext UpdateInfo(
                        versionCode = latestVersionCode,
                        versionName = json.getString("versionName"),
                        releaseNotes = json.getString("releaseNotes")
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    // פונקציה חדשה וחוקית לחנות: מפנה את המשתמש לעדכן דרך גוגל פליי
    fun openPlayStoreForUpdate() {
        val appPackageName = context.packageName
        try {
            // מנסה לפתוח את אפליקציית Google Play במכשיר
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // אם אין גוגל פליי מותקן, פותח בדפדפן
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}