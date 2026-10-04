package com.example.myapplication158.util

import android.content.Context
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val LOG_DIR_NAME = "app_logs"
    private const val MAX_LOG_AGE_MS = 7L * 24 * 60 * 60 * 1000 // 7 ימים

    fun d(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }

    // פונקציה לרשום תקלה חדשה
    fun logError(context: Context, tag: String, message: String, throwable: Throwable? = null) {
        try {
            val logDir = File(context.filesDir, LOG_DIR_NAME)
            if (!logDir.exists()) logDir.mkdirs()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val today = dateFormat.format(Date())

            val logFile = File(logDir, "log_$today.txt")

            val logEntry = buildString {
                append("[${timeFormat.format(Date())}] [$tag] $message\n")
                if (throwable != null) {
                    append("Exception: ${throwable.message}\n")
                    append(throwable.stackTraceToString())
                    append("\n")
                }
                append("----------------------------------------\n")
            }

            logFile.appendText(logEntry)
            clearOldLogs(logDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // פונקציה לשאוב את כל הלוגים של השבוע האחרון
    fun getLogsForLastWeek(context: Context): String {
        try {
            val logDir = File(context.filesDir, LOG_DIR_NAME)
            if (!logDir.exists()) return "לא נמצאו לוגים במערכת."

            val logs = StringBuilder()
            logs.append("=== פרטי מכשיר ===\n")
            logs.append("דגם: ${Build.MANUFACTURER} ${Build.MODEL}\n")
            logs.append("גרסת אנדרואיד: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
            logs.append("==================== LOGS ====================\n\n")

            val cutoffTime = System.currentTimeMillis() - MAX_LOG_AGE_MS
            val files = logDir.listFiles()?.filter { it.isFile && it.name.startsWith("log_") && it.name.endsWith(".txt") }
                ?.sortedBy { it.name } ?: emptyList()

            for (file in files) {
                if (file.lastModified() > cutoffTime) {
                    logs.append("--- ${file.name} ---\n")
                    logs.append(file.readText())
                    logs.append("\n")
                }
            }

            return if (logs.toString().contains("--- log_")) logs.toString() else "לא נרשמו שגיאות ב-7 הימים האחרונים."
        } catch (e: Exception) {
            return "שגיאה בקריאת קובצי הלוג: ${e.message}"
        }
    }

    // מחיקת לוגים ישנים
    private fun clearOldLogs(logDir: File) {
        val cutoffTime = System.currentTimeMillis() - MAX_LOG_AGE_MS
        logDir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }
}