package com.example.myapplication158.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object StorageCleaner {
    // מנקה קבצים שנוצרו לפני יותר ממספר הימים שהוגדר (ברירת מחדל: 14 ימים)
    suspend fun cleanOldTemporaryFiles(context: Context, olderThanDays: Int = 14) {
        withContext(Dispatchers.IO) {
            try {
                val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24L * 60L * 60L * 1000L)
                var deletedFilesCount = 0
                var freedSpaceBytes = 0L

                // רשימת התיקיות שצריך לסרוק
                val directoriesToClean = listOf(
                    context.cacheDir,
                    File(context.cacheDir, "merged_pdfs"),
                    File(context.filesDir, "pdfs"),
                    context.filesDir // התיקייה הראשית - בה שומרים חתימות ותמונות מצלמה
                )

                for (dir in directoriesToClean) {
                    if (dir.exists() && dir.isDirectory) {
                        dir.listFiles()?.forEach { file ->
                            if (file.isFile) {
                                // מוחק רק קבצי תמונות ו-PDF שהם ישנים מ-14 יום.
                                val fileName = file.name.lowercase()
                                val isTempFile = fileName.endsWith(".jpg") || 
                                                 fileName.endsWith(".png") || 
                                                 fileName.endsWith(".pdf")
                                                 
                                if (isTempFile && file.lastModified() < cutoffTime) {
                                    val size = file.length()
                                    if (file.delete()) {
                                        deletedFilesCount++
                                        freedSpaceBytes += size
                                    }
                                }
                            }
                        }
                    }
                }
                Log.d("StorageCleaner", "Cleaned up $deletedFilesCount old temporary files. Freed ${freedSpaceBytes / 1024 / 1024} MB.")
            } catch (e: Exception) {
                Log.e("StorageCleaner", "Error during storage cleanup", e)
            }
        }
    }
}
