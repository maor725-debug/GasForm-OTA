package com.example.myapplication158.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val versionCode: Int, val versionName: String, val apkUrl: String, val releaseNotes: String)

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

                // התיקון כאן: התאמנו את השמות בדיוק למה שכתוב ב-JSON בגיטהאב
                val latestVersionCode = json.getInt("versionCode")
                if (latestVersionCode > currentVersionCode) {
                    return@withContext UpdateInfo(
                        versionCode = latestVersionCode,
                        versionName = json.getString("versionName"),
                        apkUrl = json.getString("apkUrl"),
                        releaseNotes = json.getString("releaseNotes")
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    fun downloadAndInstallApk(apkUrl: String) {
        val appContext = context.applicationContext

        try {
            val request = DownloadManager.Request(Uri.parse(apkUrl))
                .setTitle("עדכון אפליקציה 158")
                .setDescription("מוריד ומכין להתקנה...")
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update_158_${System.currentTimeMillis()}.apk")

            val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            Toast.makeText(appContext, "ההורדה החלה... ההתקנה תחל אוטומטית בסיום.", Toast.LENGTH_LONG).show()

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        try {
                            val uri = downloadManager.getUriForDownloadedFile(downloadId)
                            if (uri != null) {
                                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/vnd.android.package-archive")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                appContext.startActivity(installIntent)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(appContext, "לחץ על ההתראה שהסתיימה כדי להתקין", Toast.LENGTH_LONG).show()
                        }
                        appContext.unregisterReceiver(this)
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
            } else {
                appContext.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }

        } catch (e: Exception) {
            Toast.makeText(appContext, "שגיאה בהורדה: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}