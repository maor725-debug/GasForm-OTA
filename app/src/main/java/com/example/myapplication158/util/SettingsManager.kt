package com.example.myapplication158.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import java.io.File

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    var appTheme: String
        get() = prefs.getString(KEY_APP_THEME, THEME_ORANGE) ?: THEME_ORANGE
        set(value) = prefs.edit().putString(KEY_APP_THEME, value).apply()

    var textSizeLevel: String
        get() = prefs.getString(KEY_TEXT_SIZE_LEVEL, TEXT_SIZE_NORMAL) ?: TEXT_SIZE_NORMAL
        set(value) = prefs.edit().putString(KEY_TEXT_SIZE_LEVEL, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_DARK_MODE, value).apply()

    var pinCode: String?
        get() = prefs.getString(KEY_PIN_CODE, null)
        set(value) = prefs.edit().putString(KEY_PIN_CODE, value).apply()

    var isPinEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_PIN_ENABLED, false) && !pinCode.isNullOrEmpty()
        set(value) = prefs.edit().putBoolean(KEY_IS_PIN_ENABLED, value).apply()

    var isAutoSavePdfEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SAVE_PDF, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SAVE_PDF, value).apply()

    var storageType: String
        get() = prefs.getString(KEY_STORAGE_TYPE, STORAGE_TYPE_LOCAL) ?: STORAGE_TYPE_LOCAL
        set(value) = prefs.edit().putString(KEY_STORAGE_TYPE, value).apply()

    var autoSaveFolderName: String
        get() = prefs.getString(KEY_AUTO_SAVE_FOLDER, "טופס_158_גיבוי") ?: "טופס_158_גיבוי"
        set(value) = prefs.edit().putString(KEY_AUTO_SAVE_FOLDER, value).apply()

    var googleDriveFolderName: String
        get() = prefs.getString(KEY_GOOGLE_DRIVE_FOLDER, "טופס_158_גיבוי") ?: "טופס_158_גיבוי"
        set(value) = prefs.edit().putString(KEY_GOOGLE_DRIVE_FOLDER, value).apply()

    var googleDriveAccount: String
        get() = prefs.getString(KEY_GOOGLE_DRIVE_ACCOUNT, "maor725@gmail.com") ?: "maor725@gmail.com"
        set(value) = prefs.edit().putString(KEY_GOOGLE_DRIVE_ACCOUNT, value).apply()

    var savedSignatureUri: String?
        get() = prefs.getString(KEY_SAVED_SIGNATURE_URI, null)
        set(value) = prefs.edit().putString(KEY_SAVED_SIGNATURE_URI, value).apply()

    var contractorHeader: String
        get() = prefs.getString(KEY_CONTRACTOR_HEADER, "מ.מ מערכות גז") ?: "מ.מ מערכות גז"
        set(value) = prefs.edit().putString(KEY_CONTRACTOR_HEADER, value).apply()

    var contractorPhone: String
        get() = prefs.getString(KEY_CONTRACTOR_PHONE, "054-6096487") ?: "054-6096487"
        set(value) = prefs.edit().putString(KEY_CONTRACTOR_PHONE, value).apply()

    var defaultTechnicianName: String
        get() = prefs.getString(KEY_DEFAULT_TECHNICIAN_NAME, "מאור מנחם") ?: "מאור מנחם"
        set(value) = prefs.edit().putString(KEY_DEFAULT_TECHNICIAN_NAME, value).apply()

    var customStorageTreeUri: String?
        get() = prefs.getString(KEY_CUSTOM_STORAGE_TREE_URI, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_STORAGE_TREE_URI, value).apply()

    var customStorageFolderName: String
        get() = prefs.getString(KEY_CUSTOM_STORAGE_FOLDER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_STORAGE_FOLDER_NAME, value).apply()

    var lastPromptedYear: Int
        get() = prefs.getInt(KEY_LAST_PROMPTED_YEAR, 0)
        set(value) = prefs.edit().putInt(KEY_LAST_PROMPTED_YEAR, value).apply()

    var currentFormNumber: Int
        get() = prefs.getInt(KEY_CURRENT_FORM_NUMBER, 0)
        set(value) = prefs.edit().putInt(KEY_CURRENT_FORM_NUMBER, value).apply()

    // --- בחירת עיצוב לטופס ---
    var pdfTemplateStyle: String
        get() = prefs.getString(KEY_PDF_TEMPLATE, TEMPLATE_MODERN) ?: TEMPLATE_MODERN
        set(value) = prefs.edit().putString(KEY_PDF_TEMPLATE, value).apply()

    fun getAutoSaveDir(context: Context): File {
        val folderName = when (storageType) {
            STORAGE_TYPE_GOOGLE_DRIVE -> googleDriveFolderName.ifEmpty { "טופס_158_גיבוי" }
            STORAGE_TYPE_DROPBOX -> "טופס_158_גיבוי"
            else -> autoSaveFolderName.ifEmpty { "טופס_158_גיבוי" }
        }

        val baseDir = when (storageType) {
            STORAGE_TYPE_GOOGLE_DRIVE -> {
                val driveDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GoogleDrive")
                if (!driveDir.exists()) driveDir.mkdirs()
                driveDir
            }
            STORAGE_TYPE_DROPBOX -> {
                val dropboxDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Dropbox")
                if (!dropboxDir.exists()) dropboxDir.mkdirs()
                dropboxDir
            }
            else -> {
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    ?: File(context.filesDir, "Documents")
            }
        }

        val targetDir = File(baseDir, folderName)
        if (!targetDir.exists()) targetDir.mkdirs()
        return targetDir
    }

    companion object {
        const val THEME_ORANGE = "ORANGE"
        const val THEME_BLUE = "BLUE"
        const val THEME_GREEN = "GREEN"
        const val THEME_PURPLE = "PURPLE"

        const val TEXT_SIZE_TINY = "TINY"
        const val TEXT_SIZE_SMALL = "SMALL"
        const val TEXT_SIZE_NORMAL = "NORMAL"
        const val TEXT_SIZE_LARGE = "LARGE"
        const val TEXT_SIZE_HUGE = "HUGE"

        const val STORAGE_TYPE_LOCAL = "LOCAL"
        const val STORAGE_TYPE_GOOGLE_DRIVE = "GOOGLE_DRIVE"
        const val STORAGE_TYPE_DROPBOX = "DROPBOX"
        const val STORAGE_TYPE_SAF_CUSTOM = "SAF_CUSTOM"

        const val TEMPLATE_CLASSIC = "CLASSIC"
        const val TEMPLATE_MODERN = "MODERN"

        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_TEXT_SIZE_LEVEL = "text_size_level"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_PIN_CODE = "pin_code"
        private const val KEY_IS_PIN_ENABLED = "is_pin_enabled"
        private const val KEY_AUTO_SAVE_PDF = "auto_save_pdf"
        private const val KEY_STORAGE_TYPE = "storage_type"
        private const val KEY_AUTO_SAVE_FOLDER = "auto_save_folder"
        private const val KEY_GOOGLE_DRIVE_FOLDER = "google_drive_folder"
        private const val KEY_GOOGLE_DRIVE_ACCOUNT = "google_drive_account"
        private const val KEY_SAVED_SIGNATURE_URI = "saved_signature_uri"
        private const val KEY_CONTRACTOR_HEADER = "contractor_header"
        private const val KEY_CONTRACTOR_PHONE = "contractor_phone"
        private const val KEY_DEFAULT_TECHNICIAN_NAME = "default_technician_name"
        private const val KEY_CUSTOM_STORAGE_TREE_URI = "custom_storage_tree_uri"
        private const val KEY_CUSTOM_STORAGE_FOLDER_NAME = "custom_storage_folder_name"
        private const val KEY_LAST_PROMPTED_YEAR = "last_prompted_year"
        private const val KEY_CURRENT_FORM_NUMBER = "current_form_number"
        private const val KEY_PDF_TEMPLATE = "pdf_template"
    }
}