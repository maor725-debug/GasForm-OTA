package com.example.myapplication158.UserInterface

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication158.data.AppDatabase
import com.example.myapplication158.data.GasForm
import com.example.myapplication158.data.PeriodicGasForm
import com.example.myapplication158.data.GasFormD2
import com.example.myapplication158.data.GasFormD3
import com.example.myapplication158.data.GasFormD4
import com.example.myapplication158.data.WaiverForm
import com.example.myapplication158.data.WorkOrder
import com.example.myapplication158.data.isNotEmptyOrBlank
import com.example.myapplication158.util.AppLogger
import com.example.myapplication158.util.PdfGenerator
import com.example.myapplication158.util.SupabaseManager
import com.example.myapplication158.util.WorkOrderReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class GasFormViewModel(application: Application) : AndroidViewModel(application) {

    private val gasFormDao = AppDatabase.getDatabase(application).gasFormDao()
    private val periodicGasFormDao = AppDatabase.getDatabase(application).periodicGasFormDao()
    private val gasFormD2Dao = AppDatabase.getDatabase(application).gasFormD2Dao()
    private val gasFormD3Dao = AppDatabase.getDatabase(application).gasFormD3Dao()
    private val gasFormD4Dao = AppDatabase.getDatabase(application).gasFormD4Dao()
    private val workOrderDao = AppDatabase.getDatabase(application).workOrderDao()
    private val waiverFormDao = AppDatabase.getDatabase(application).waiverFormDao()
    private val workOrderReminderManager = WorkOrderReminderManager(application)

    val allForms: StateFlow<List<GasForm>> = gasFormDao.getAllForms().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currentForm = MutableStateFlow<GasForm?>(null)

    val allPeriodicForms: StateFlow<List<PeriodicGasForm>> = periodicGasFormDao.getAllForms().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currentPeriodicForm = MutableStateFlow<PeriodicGasForm?>(null)

    val allD2Forms: StateFlow<List<GasFormD2>> = gasFormD2Dao.getAllForms().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allD3Forms: StateFlow<List<GasFormD3>> = gasFormD3Dao.getAllForms().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allD4Forms: StateFlow<List<GasFormD4>> = gasFormD4Dao.getAllForms().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkOrders: StateFlow<List<WorkOrder>> = workOrderDao.getAllWorkOrders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allWaiverForms: StateFlow<List<WaiverForm>> = waiverFormDao.getAllForms().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allForms.collect { forms -> forms.filter { !it.isNotEmptyOrBlank() }.forEach { gasFormDao.deleteForm(it) } }
        }
        viewModelScope.launch(Dispatchers.IO) {
            allPeriodicForms.collect { forms -> forms.filter { !it.isNotEmptyOrBlank() }.forEach { periodicGasFormDao.deleteForm(it) } }
        }
        viewModelScope.launch(Dispatchers.IO) {
            allD2Forms.collect { forms ->
                forms.filter { form ->
                    form.businessName.isBlank() && form.clientName.isBlank() && form.clientPhone.isBlank()
                }.forEach { gasFormD2Dao.deleteForm(it) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            allD3Forms.collect { forms ->
                forms.filter { form ->
                    form.businessName.isBlank() && form.clientName.isBlank() && form.clientPhone.isBlank()
                }.forEach { gasFormD3Dao.deleteForm(it) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            allD4Forms.collect { forms ->
                forms.filter { form ->
                    form.businessName.isBlank() && form.clientName.isBlank() && form.clientPhone.isBlank()
                }.forEach { gasFormD4Dao.deleteForm(it) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            allWaiverForms.collect { forms ->
                forms.filter { !it.isNotEmptyOrBlank() }.forEach { waiverFormDao.deleteForm(it) }
            }
        }
    }

    fun saveWorkOrder(workOrder: WorkOrder, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = if (workOrder.id == 0) workOrderDao.insertWorkOrder(workOrder).toInt() else { workOrderDao.updateWorkOrder(workOrder); workOrder.id }
            val updatedOrder = workOrder.copy(id = id)
            workOrderReminderManager.scheduleReminders(updatedOrder)
            withContext(Dispatchers.Main) { onComplete?.invoke() }
        }
    }
    fun updateWorkOrder(workOrder: WorkOrder) { viewModelScope.launch(Dispatchers.IO) { workOrderDao.updateWorkOrder(workOrder); workOrderReminderManager.scheduleReminders(workOrder) } }
    fun deleteWorkOrder(workOrder: WorkOrder) { viewModelScope.launch(Dispatchers.IO) { workOrderDao.deleteWorkOrder(workOrder); workOrderReminderManager.cancelReminders(workOrder.id) } }
    fun toggleMuteWorkOrder(workOrder: WorkOrder) { updateWorkOrder(workOrder.copy(isMuted = !workOrder.isMuted)) }
    fun addToNativeCalendar(workOrder: WorkOrder) { workOrderReminderManager.addToNativeCalendar(workOrder) }

    fun autoSaveWaiverForm(form: WaiverForm, onIdAssigned: ((Int) -> Unit)? = null) {
        if (!form.isNotEmptyOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (form.id == 0) {
                    val newId = waiverFormDao.insertForm(form)
                    withContext(Dispatchers.Main) { onIdAssigned?.invoke(newId.toInt()) }
                } else {
                    waiverFormDao.updateForm(form)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun saveCurrentWaiverForm(form: WaiverForm, onComplete: () -> Unit) {
        if (!form.isNotEmptyOrBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                if (form.id != 0) waiverFormDao.deleteForm(form)
                withContext(Dispatchers.Main) { onComplete() }
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (form.id == 0) waiverFormDao.insertForm(form) else waiverFormDao.updateForm(form)
            } catch (e: Exception) { e.printStackTrace() }
            finally { withContext(Dispatchers.Main) { onComplete() } }
        }
    }

    fun deleteWaiverForm(form: WaiverForm) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (form.id != 0) waiverFormDao.deleteForm(form)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun previewWaiverPdf(context: Context, form: WaiverForm) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFile = PdfGenerator.generateWaiverFormPdf(context, form)
            if (pdfFile != null && pdfFile.exists()) {
                withContext(Dispatchers.Main) {
                    try {
                        val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                        context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(contentUri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun shareWaiverPdf(context: Context, form: WaiverForm, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFile = PdfGenerator.generateWaiverFormPdf(context, form)
            if (pdfFile != null && pdfFile.exists()) {
                val updatedForm = form.copy(isSavedToTarget = true, savedTargetLocation = "Shared")
                val savedId = if (form.id == 0) waiverFormDao.insertForm(updatedForm).toInt() else { waiverFormDao.updateForm(updatedForm); form.id }
                withContext(Dispatchers.Main) {
                    try {
                        val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, contentUri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                        context.startActivity(Intent.createChooser(shareIntent, "שתף").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                        onComplete()
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun getNextPartnerNumber(): String {
        val maxNum = allForms.value.mapNotNull { it.partnerNumber.toIntOrNull() }.maxOrNull()
        return if (maxNum != null) (maxNum + 1).toString() else "1"
    }

    fun selectForm(form: GasForm?) { currentForm.value = form }
    fun saveCurrentForm(form: GasForm, onComplete: () -> Unit) {
        if (!form.isNotEmptyOrBlank()) { viewModelScope.launch(Dispatchers.IO) { if (form.id != 0) gasFormDao.deleteForm(form); withContext(Dispatchers.Main) { onComplete() } }; return }
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) gasFormDao.insertForm(form) else gasFormDao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } finally { withContext(Dispatchers.Main) { onComplete() } } }
    }
    fun autoSaveForm(form: GasForm, onIdAssigned: ((Int) -> Unit)? = null) {
        if (!form.isNotEmptyOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) { val newId = gasFormDao.insertForm(form); withContext(Dispatchers.Main) { onIdAssigned?.invoke(newId.toInt()) } } else gasFormDao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } }
    }
    fun deleteForm(form: GasForm) { viewModelScope.launch(Dispatchers.IO) { try { if (form.id != 0) gasFormDao.deleteForm(form); if (!form.savedPdfFilePath.isNullOrEmpty()) { val file = File(form.savedPdfFilePath); if (file.exists()) file.delete() } } catch (e: Exception) { e.printStackTrace() } } }

    fun previewPdf(context: Context, form: GasForm) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var pdfFile: File? = if (!form.savedPdfFilePath.isNullOrEmpty() && form.isSavedToTarget) { File(form.savedPdfFilePath) } else null
                if (pdfFile == null || !pdfFile.exists()) { pdfFile = PdfGenerator.generateFormPdf(context, form) }
                if (pdfFile != null && pdfFile.exists()) {
                    withContext(Dispatchers.Main) {
                        try {
                            val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                            context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(contentUri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                        } catch (e: Exception) { Toast.makeText(context, "שגיאה: לא מותקנת אפליקציה להצגת PDF.", Toast.LENGTH_LONG).show() }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    fun sharePdf(context: Context, form: GasForm, onFormSaved: ((GasForm) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfFile = PdfGenerator.generateFormPdf(context, form)
                if (pdfFile != null && pdfFile.exists()) {
                    val updatedForm = form.copy(isSavedToTarget = true, savedTargetLocation = "Shared", savedPdfFilePath = pdfFile.absolutePath)
                    val savedFormId = if (form.id == 0) gasFormDao.insertForm(updatedForm).toInt() else { gasFormDao.updateForm(updatedForm); form.id }
                    val finalSavedForm = updatedForm.copy(id = savedFormId)
                    withContext(Dispatchers.Main) { onFormSaved?.invoke(finalSavedForm); shareFileSafe(context, finalSavedForm, pdfFile, false) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    private suspend fun shareFileSafe(context: Context, form: GasForm, pdfFile: File, isPeriodic: Boolean) {
        withContext(Dispatchers.Main) {
            try {
                val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                val clientName = form.clientName.takeIf { it.isNotBlank() } ?: "לקוח יקר"
                val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, contentUri); putExtra(Intent.EXTRA_SUBJECT, "טופס נורמטיבי - $clientName"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                context.startActivity(Intent.createChooser(shareIntent, "שתף טופס באמצעות").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            } catch (e: Exception) { Toast.makeText(context, "שגיאה בהפעלת שיתוף.", Toast.LENGTH_LONG).show() }
        }
    }

    fun selectPeriodicForm(form: PeriodicGasForm?) { currentPeriodicForm.value = form }
    fun saveCurrentPeriodicForm(form: PeriodicGasForm, onComplete: () -> Unit) {
        if (!form.isNotEmptyOrBlank()) { viewModelScope.launch(Dispatchers.IO) { if (form.id != 0) periodicGasFormDao.deleteForm(form); withContext(Dispatchers.Main) { onComplete() } }; return }
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) periodicGasFormDao.insertForm(form) else periodicGasFormDao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } finally { withContext(Dispatchers.Main) { onComplete() } } }
    }
    fun autoSavePeriodicForm(form: PeriodicGasForm, onIdAssigned: ((Int) -> Unit)? = null) {
        if (!form.isNotEmptyOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) { val newId = periodicGasFormDao.insertForm(form); withContext(Dispatchers.Main) { onIdAssigned?.invoke(newId.toInt()) } } else periodicGasFormDao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } }
    }
    fun deletePeriodicForm(form: PeriodicGasForm) { viewModelScope.launch(Dispatchers.IO) { try { if (form.id != 0) periodicGasFormDao.deleteForm(form) } catch (e: Exception) { e.printStackTrace() } } }

    fun previewPeriodicPdf(context: Context, form: PeriodicGasForm) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfFile = PdfGenerator.generatePeriodicFormPdf(context, form)
                if (pdfFile != null && pdfFile.exists()) {
                    withContext(Dispatchers.Main) {
                        try {
                            val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                            context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(contentUri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                        } catch (e: Exception) {}
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    fun sharePeriodicPdf(context: Context, form: PeriodicGasForm, onFormSaved: ((PeriodicGasForm) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfFile = PdfGenerator.generatePeriodicFormPdf(context, form)
                if (pdfFile != null && pdfFile.exists() && pdfFile.length() > 0L) {
                    val updatedForm = form.copy(isSavedToTarget = true, savedTargetLocation = "Shared")
                    val savedFormId = if (form.id == 0) periodicGasFormDao.insertForm(updatedForm).toInt() else { periodicGasFormDao.updateForm(updatedForm); form.id }
                    val finalSavedForm = updatedForm.copy(id = savedFormId)
                    withContext(Dispatchers.Main) { onFormSaved?.invoke(finalSavedForm); sharePeriodicFileSafe(context, finalSavedForm, pdfFile) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    private suspend fun sharePeriodicFileSafe(context: Context, form: PeriodicGasForm, pdfFile: File) {
        withContext(Dispatchers.Main) {
            try {
                val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, contentUri); putExtra(Intent.EXTRA_SUBJECT, "דוח בדיקה תקופתית"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                context.startActivity(Intent.createChooser(shareIntent, "שתף טופס באמצעות").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            } catch (e: Exception) {}
        }
    }

    fun scanAndRestoreFromPdfFolder(context: Context, onResult: (Int) -> Unit) { onResult(0) }

    // --- גיבוי ושחזור אמיתי של מסד הנתונים ---
    fun exportBackup(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                // התיקון של קלוד: שימוש ב-use וקריאה ממשית כדי להכריח את ה-Checkpoint לרוץ!
                db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                    if (cursor.moveToFirst()) {
                        AppLogger.d("Backup", "WAL Checkpoint executed successfully")
                    }
                }

                val dbFile = context.getDatabasePath("gas_forms_database")
                if (!dbFile.exists()) {
                    withContext(Dispatchers.Main) { onResult(false, "שגיאה: קובץ מסד הנתונים לא קיים במכשיר.") }
                    return@launch
                }

                // העתקת מסד הנתונים ליעד שהמשתמש בחר
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    dbFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                withContext(Dispatchers.Main) { onResult(true, "הגיבוי נשמר בהצלחה! שמור עליו במקום בטוח.") }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false, "שגיאה בביצוע הגיבוי: ${e.message}") }
            }
        }
    }

    fun importBackup(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath("gas_forms_database")
                val walFile = File(dbFile.path + "-wal")
                val shmFile = File(dbFile.path + "-shm")

                // חובה לסגור את החיבור למסד הנתונים לפני שדורסים אותו
                AppDatabase.getDatabase(context).close()

                // דריסת מסד הנתונים הקיים בקובץ הגיבוי
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    dbFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                // מחיקת קובצי מטמון זמניים של מסד הנתונים הישן כדי למנוע השחתה (Corruption)
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()

                withContext(Dispatchers.Main) {
                    onResult(true, "הנתונים שוחזרו! האפליקציה תיסגר כעת כדי להחיל את השינויים. פתח אותה מחדש.")
                    // סגירה בטוחה של האפליקציה לאחר 2.5 שניות כדי לאפשר להודעה להופיע
                    kotlinx.coroutines.delay(2500)
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false, "שגיאה בשחזור: ${e.message}") }
            }
        }
    }

    fun autoSaveFormD2(form: GasFormD2, onIdAssigned: ((Int) -> Unit)? = null) {
        val isBlank = form.businessName.isBlank() && form.clientName.isBlank() && form.clientPhone.isBlank()
        if (isBlank) return
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) { val newId = gasFormD2Dao.insertForm(form); withContext(Dispatchers.Main) { onIdAssigned?.invoke(newId.toInt()) } } else gasFormD2Dao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } }
    }
    fun saveCurrentFormD2(form: GasFormD2, onComplete: () -> Unit) {
        val isBlank = form.businessName.isBlank() && form.clientName.isBlank() && form.clientPhone.isBlank()
        if (isBlank) { viewModelScope.launch(Dispatchers.IO) { if (form.id != 0) gasFormD2Dao.deleteForm(form); withContext(Dispatchers.Main) { onComplete() } }; return }
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) gasFormD2Dao.insertForm(form) else gasFormD2Dao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } finally { withContext(Dispatchers.Main) { onComplete() } } }
    }
    fun deleteFormD2(form: GasFormD2) { viewModelScope.launch(Dispatchers.IO) { try { if (form.id != 0) gasFormD2Dao.deleteForm(form) } catch (e: Exception) { e.printStackTrace() } } }
    fun previewPdfD2(context: Context, form: GasFormD2) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFile = PdfGenerator.generateFormD2Pdf(context, form)
            if (pdfFile != null && pdfFile.exists()) {
                withContext(Dispatchers.Main) {
                    try {
                        val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                        context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(contentUri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } catch (e: Exception) {}
                }
            }
        }
    }
    fun sharePdfD2(context: Context, form: GasFormD2, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFile = PdfGenerator.generateFormD2Pdf(context, form)
            if (pdfFile != null && pdfFile.exists()) {
                val updatedForm = form.copy(isSavedToTarget = true, savedTargetLocation = "Shared")
                val savedId = if (form.id == 0) gasFormD2Dao.insertForm(updatedForm).toInt() else { gasFormD2Dao.updateForm(updatedForm); form.id }
                withContext(Dispatchers.Main) {
                    try {
                        val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, contentUri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                        context.startActivity(Intent.createChooser(shareIntent, "שתף").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                        onComplete()
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun autoSaveFormD3(form: GasFormD3, onIdAssigned: ((Int) -> Unit)? = null) {
        val isBlank = form.businessName.isBlank() && form.clientName.isBlank() && form.clientPhone.isBlank()
        if (isBlank) return
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) { val newId = gasFormD3Dao.insertForm(form); withContext(Dispatchers.Main) { onIdAssigned?.invoke(newId.toInt()) } } else gasFormD3Dao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } }
    }
    fun saveCurrentFormD3(form: GasFormD3, onComplete: () -> Unit) {
        val isBlank = form.businessName.isBlank() && form.clientName.isBlank() && form.clientPhone.isBlank()
        if (isBlank) { viewModelScope.launch(Dispatchers.IO) { if (form.id != 0) gasFormD3Dao.deleteForm(form); withContext(Dispatchers.Main) { onComplete() } }; return }
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) gasFormD3Dao.insertForm(form) else gasFormD3Dao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } finally { withContext(Dispatchers.Main) { onComplete() } } }
    }
    fun deleteFormD3(form: GasFormD3) { viewModelScope.launch(Dispatchers.IO) { try { if (form.id != 0) gasFormD3Dao.deleteForm(form) } catch (e: Exception) { e.printStackTrace() } } }
    fun previewPdfD3(context: Context, form: GasFormD3) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFile = PdfGenerator.generateFormD3Pdf(context, form)
            if (pdfFile != null && pdfFile.exists()) {
                withContext(Dispatchers.Main) {
                    try {
                        val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                        context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(contentUri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } catch (e: Exception) {}
                }
            }
        }
    }
    fun sharePdfD3(context: Context, form: GasFormD3, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFile = PdfGenerator.generateFormD3Pdf(context, form)
            if (pdfFile != null && pdfFile.exists()) {
                val updatedForm = form.copy(isSavedToTarget = true, savedTargetLocation = "Shared")
                val savedId = if (form.id == 0) gasFormD3Dao.insertForm(updatedForm).toInt() else { gasFormD3Dao.updateForm(updatedForm); form.id }
                withContext(Dispatchers.Main) {
                    try {
                        val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, contentUri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                        context.startActivity(Intent.createChooser(shareIntent, "שתף").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                        onComplete()
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun autoSaveFormD4(form: GasFormD4, onIdAssigned: ((Int) -> Unit)? = null) {
        val isBlank = form.businessName.isBlank() && form.clientName.isBlank() && form.clientPhone.isBlank()
        if (isBlank) return
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) { val newId = gasFormD4Dao.insertForm(form); withContext(Dispatchers.Main) { onIdAssigned?.invoke(newId.toInt()) } } else gasFormD4Dao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } }
    }
    fun saveCurrentFormD4(form: GasFormD4, onComplete: () -> Unit) {
        val isBlank = form.businessName.isBlank() && form.clientName.isBlank() && form.clientPhone.isBlank()
        if (isBlank) { viewModelScope.launch(Dispatchers.IO) { if (form.id != 0) gasFormD4Dao.deleteForm(form); withContext(Dispatchers.Main) { onComplete() } }; return }
        viewModelScope.launch(Dispatchers.IO) { try { if (form.id == 0) gasFormD4Dao.insertForm(form) else gasFormD4Dao.updateForm(form) } catch (e: Exception) { e.printStackTrace() } finally { withContext(Dispatchers.Main) { onComplete() } } }
    }
    fun deleteFormD4(form: GasFormD4) { viewModelScope.launch(Dispatchers.IO) { try { if (form.id != 0) gasFormD4Dao.deleteForm(form) } catch (e: Exception) { e.printStackTrace() } } }
    fun previewPdfD4(context: Context, form: GasFormD4) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFile = PdfGenerator.generateFormD4Pdf(context, form)
            if (pdfFile != null && pdfFile.exists()) {
                withContext(Dispatchers.Main) {
                    try {
                        val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                        context.startActivity(Intent(Intent.ACTION_VIEW).apply { setDataAndType(contentUri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } catch (e: Exception) {}
                }
            }
        }
    }
    fun sharePdfD4(context: Context, form: GasFormD4, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFile = PdfGenerator.generateFormD4Pdf(context, form)
            if (pdfFile != null && pdfFile.exists()) {
                val updatedForm = form.copy(isSavedToTarget = true, savedTargetLocation = "Shared")
                val savedId = if (form.id == 0) gasFormD4Dao.insertForm(updatedForm).toInt() else { gasFormD4Dao.updateForm(updatedForm); form.id }
                withContext(Dispatchers.Main) {
                    try {
                        val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, contentUri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                        context.startActivity(Intent.createChooser(shareIntent, "שתף").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                        onComplete()
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun mergeAndSharePdfs(context: Context, forms: List<Any>, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfFiles = mutableListOf<File>()
            for (form in forms) {
                val file = when(form) {
                    is GasForm -> PdfGenerator.generateFormPdf(context, form)
                    is PeriodicGasForm -> PdfGenerator.generatePeriodicFormPdf(context, form)
                    is GasFormD2 -> PdfGenerator.generateFormD2Pdf(context, form)
                    is GasFormD3 -> PdfGenerator.generateFormD3Pdf(context, form)
                    is GasFormD4 -> PdfGenerator.generateFormD4Pdf(context, form)
                    is WaiverForm -> PdfGenerator.generateWaiverFormPdf(context, form)
                    else -> null
                }
                if (file != null && file.exists()) {
                    pdfFiles.add(file)
                }
            }

            if (pdfFiles.isNotEmpty()) {
                val mergedFile = PdfGenerator.mergePdfFiles(context, pdfFiles)
                if (mergedFile != null) {
                    withContext(Dispatchers.Main) {
                        try {
                            val contentUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", mergedFile)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, contentUri)
                                putExtra(Intent.EXTRA_SUBJECT, "מסמכים ממוזגים - קבלן גז")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "שתף מסמך ממוזג").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                            onComplete()
                        } catch (e: Exception) {
                            Toast.makeText(context, "שגיאה בשיתוף המסמך הממוזג", Toast.LENGTH_SHORT).show()
                            onComplete()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) { Toast.makeText(context, "שגיאה במיזוג הקבצים", Toast.LENGTH_SHORT).show(); onComplete() }
                }
            } else {
                withContext(Dispatchers.Main) { Toast.makeText(context, "לא נוצרו קבצים למיזוג", Toast.LENGTH_SHORT).show(); onComplete() }
            }
        }
    }
}