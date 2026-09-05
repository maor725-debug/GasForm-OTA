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
import com.example.myapplication158.data.isNotEmptyOrBlank
import com.example.myapplication158.util.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class GasFormViewModel(application: Application) : AndroidViewModel(application) {

    // חיבור לשני מסדי הנתונים בנפרד
    private val gasFormDao = AppDatabase.getDatabase(application).gasFormDao()
    private val periodicGasFormDao = AppDatabase.getDatabase(application).periodicGasFormDao()

    // --- 1. זרמי נתונים (StateFlows) לטפסים נורמטיביים ---
    val allForms: StateFlow<List<GasForm>> = gasFormDao.getAllForms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val currentForm = MutableStateFlow<GasForm?>(null)

    // --- 2. זרמי נתונים (StateFlows) לטפסים תקופתיים (ד-1) ---
    val allPeriodicForms: StateFlow<List<PeriodicGasForm>> = periodicGasFormDao.getAllForms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val currentPeriodicForm = MutableStateFlow<PeriodicGasForm?>(null)

    init {
        // מנקה אוטומטית טפסים ריקים (נורמטיבי)
        viewModelScope.launch(Dispatchers.IO) {
            allForms.collect { forms ->
                val emptyForms = forms.filter { !it.isNotEmptyOrBlank() }
                for (emptyForm in emptyForms) {
                    gasFormDao.deleteForm(emptyForm)
                }
            }
        }
        // מנקה אוטומטית טפסים ריקים (תקופתי ד-1)
        viewModelScope.launch(Dispatchers.IO) {
            allPeriodicForms.collect { forms ->
                val emptyForms = forms.filter { !it.isNotEmptyOrBlank() }
                for (emptyForm in emptyForms) {
                    periodicGasFormDao.deleteForm(emptyForm)
                }
            }
        }
    }

    // ==========================================
    // לוגיקה לטופס נורמטיבי (לא השתנה בכלל)
    // ==========================================
    fun getNextPartnerNumber(): String {
        val forms = allForms.value
        val maxNum = forms.mapNotNull { it.partnerNumber.toIntOrNull() }.maxOrNull()
        return if (maxNum != null) (maxNum + 1).toString() else "1"
    }

    fun selectForm(form: GasForm?) { currentForm.value = form }

    fun saveCurrentForm(form: GasForm, onComplete: () -> Unit) {
        if (!form.isNotEmptyOrBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                if (form.id != 0) gasFormDao.deleteForm(form)
                withContext(Dispatchers.Main) { onComplete() }
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (form.id == 0) gasFormDao.insertForm(form) else gasFormDao.updateForm(form)
            } catch (e: Exception) { e.printStackTrace() }
            finally { withContext(Dispatchers.Main) { onComplete() } }
        }
    }

    fun autoSaveForm(form: GasForm, onIdAssigned: ((Int) -> Unit)? = null) {
        if (!form.isNotEmptyOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (form.id == 0) {
                    val newId = gasFormDao.insertForm(form)
                    withContext(Dispatchers.Main) { onIdAssigned?.invoke(newId.toInt()) }
                } else gasFormDao.updateForm(form)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deleteForm(form: GasForm) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (form.id != 0) gasFormDao.deleteForm(form)
                if (!form.savedPdfFilePath.isNullOrEmpty()) {
                    val file = File(form.savedPdfFilePath)
                    if (file.exists()) file.delete()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun previewPdf(context: Context, form: GasForm) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var pdfFile: File? = if (!form.savedPdfFilePath.isNullOrEmpty() && form.isSavedToTarget) { File(form.savedPdfFilePath) } else null
                if (pdfFile == null || !pdfFile.exists()) { pdfFile = PdfGenerator.generateFormPdf(context, form) }
                if (pdfFile != null && pdfFile.exists()) {
                    if (pdfFile.length() == 0L) { pdfFile.delete(); throw Exception("הקובץ שנוצר פגום או ריק.") }
                    withContext(Dispatchers.Main) {
                        try {
                            val authority = context.packageName + ".fileprovider"
                            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(viewIntent)
                        } catch (e: ActivityNotFoundException) { Toast.makeText(context, "שגיאה: לא מותקנת אפליקציה להצגת PDF.", Toast.LENGTH_LONG).show() }
                    }
                } else { withContext(Dispatchers.Main) { Toast.makeText(context, "שגיאה ביצירת ה-PDF.", Toast.LENGTH_SHORT).show() } }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun sharePdf(context: Context, form: GasForm, onFormSaved: ((GasForm) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfFile = PdfGenerator.generateFormPdf(context, form)
                if (pdfFile != null && pdfFile.exists() && pdfFile.length() > 0L) {
                    val updatedForm = form.copy(isSavedToTarget = true, savedTargetLocation = "Shared", savedPdfFilePath = pdfFile.absolutePath)
                    val savedFormId = if (form.id == 0) gasFormDao.insertForm(updatedForm).toInt() else { gasFormDao.updateForm(updatedForm); form.id }
                    val finalSavedForm = updatedForm.copy(id = savedFormId)
                    withContext(Dispatchers.Main) {
                        onFormSaved?.invoke(finalSavedForm)
                        shareFileSafe(context, finalSavedForm, pdfFile, false)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun shareFileSafe(context: Context, form: GasForm, pdfFile: File, isPeriodic: Boolean) {
        withContext(Dispatchers.Main) {
            try {
                val authority = context.packageName + ".fileprovider"
                val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)
                val clientName = form.clientName.takeIf { it.isNotBlank() } ?: "לקוח יקר"
                val shareMessage = "שלום $clientName,\nמצורף דוח התאמת מתקן גז (טופס נורמטיבי) מתאריך ${form.date}.\nהמשך יום נעים!"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_SUBJECT, "טופס נורמטיבי - $clientName")
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "שתף טופס באמצעות")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) { Toast.makeText(context, "שגיאה בהפעלת שיתוף.", Toast.LENGTH_LONG).show() }
        }
    }


    // ==========================================
    // הלוגיקה החדשה והמקבילה - טופס תקופתי ד-1
    // ==========================================
    fun selectPeriodicForm(form: PeriodicGasForm?) { currentPeriodicForm.value = form }

    fun saveCurrentPeriodicForm(form: PeriodicGasForm, onComplete: () -> Unit) {
        if (!form.isNotEmptyOrBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                if (form.id != 0) periodicGasFormDao.deleteForm(form)
                withContext(Dispatchers.Main) { onComplete() }
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (form.id == 0) periodicGasFormDao.insertForm(form) else periodicGasFormDao.updateForm(form)
            } catch (e: Exception) { e.printStackTrace() }
            finally { withContext(Dispatchers.Main) { onComplete() } }
        }
    }

    fun autoSavePeriodicForm(form: PeriodicGasForm, onIdAssigned: ((Int) -> Unit)? = null) {
        if (!form.isNotEmptyOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (form.id == 0) {
                    val newId = periodicGasFormDao.insertForm(form)
                    withContext(Dispatchers.Main) { onIdAssigned?.invoke(newId.toInt()) }
                } else periodicGasFormDao.updateForm(form)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deletePeriodicForm(form: PeriodicGasForm) {
        viewModelScope.launch(Dispatchers.IO) {
            try { if (form.id != 0) periodicGasFormDao.deleteForm(form) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun previewPeriodicPdf(context: Context, form: PeriodicGasForm) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // מפעיל את הפונקציה החדשה ב-PdfGenerator (שניצור בשלב הבא)
                val pdfFile = PdfGenerator.generatePeriodicFormPdf(context, form)
                if (pdfFile != null && pdfFile.exists()) {
                    withContext(Dispatchers.Main) {
                        try {
                            val authority = context.packageName + ".fileprovider"
                            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(contentUri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(viewIntent)
                        } catch (e: ActivityNotFoundException) { Toast.makeText(context, "שגיאה: לא מותקנת אפליקציה להצגת PDF.", Toast.LENGTH_LONG).show() }
                    }
                } else { withContext(Dispatchers.Main) { Toast.makeText(context, "שגיאה ביצירת ה-PDF.", Toast.LENGTH_SHORT).show() } }
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

                    withContext(Dispatchers.Main) {
                        onFormSaved?.invoke(finalSavedForm)
                        sharePeriodicFileSafe(context, finalSavedForm, pdfFile)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun sharePeriodicFileSafe(context: Context, form: PeriodicGasForm, pdfFile: File) {
        withContext(Dispatchers.Main) {
            try {
                val authority = context.packageName + ".fileprovider"
                val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)
                val clientName = form.businessName.takeIf { it.isNotBlank() } ?: "לקוח יקר"
                val shareMessage = "שלום $clientName,\nמצורף דוח בדיקה תקופתית (טופס ד-1) למאגר גפ\"מ מתאריך ${form.date}.\nהמשך יום נעים!"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_SUBJECT, "דוח בדיקה תקופתית ד-1 - $clientName")
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "שתף טופס באמצעות")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) { Toast.makeText(context, "שגיאה בהפעלת שיתוף.", Toast.LENGTH_LONG).show() }
        }
    }

    fun scanAndRestoreFromPdfFolder(context: Context, onResult: (Int) -> Unit) { onResult(0) }
    fun exportBackup(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) { onResult(false, "ייצוא יופעל בעדכון הבא") }
    fun importBackup(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) { onResult(false, "שחזור יופעל בעדכון הבא") }
}