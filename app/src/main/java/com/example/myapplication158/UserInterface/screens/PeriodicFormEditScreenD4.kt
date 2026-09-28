package com.example.myapplication158.UserInterface.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.UserInterface.components.FormCard
import com.example.myapplication158.UserInterface.components.TechnicianSignatureTouchPad
import com.example.myapplication158.data.GasFormD4
import com.example.myapplication158.util.SettingsManager
import androidx.activity.result.PickVisualMediaRequest
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodicFormEditScreenD4(
    viewModel: GasFormViewModel,
    form: Any? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    val initialForm = form as? GasFormD4

    var sequentialNumber by remember { mutableIntStateOf(if (initialForm != null && initialForm.sequentialNumber > 0) initialForm.sequentialNumber else settingsManager.currentFormNumber) }

    val isDark = settingsManager.isDarkMode
    val primaryColor = Color(0xFF673AB7) // סגול עמוק לד-4
    val bgScreenColor = if (isDark) Color(0xFF0D0D0D) else Color(0xFFF4F6F8)
    val cardBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val headerBg = if (isDark) Color(0xFF111827) else Color.White
    val textWhite = if (isDark) Color(0xFFF5F5F5) else Color(0xFF212121)
    val textGray = if (isDark) Color(0xFFAAAAAA) else Color(0xFF757575)
    val borderColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFE0E0E0)
    val errorRed = Color(0xFFFF5252)
    val successGreen = Color(0xFF4CAF50)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primaryColor, unfocusedBorderColor = borderColor,
        focusedTextColor = textWhite, unfocusedTextColor = textWhite, cursorColor = primaryColor,
        focusedLabelColor = primaryColor, unfocusedLabelColor = textGray,
        focusedContainerColor = cardBg, unfocusedContainerColor = cardBg
    )

    val errorFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = errorRed, unfocusedBorderColor = errorRed,
        focusedTextColor = textWhite, unfocusedTextColor = textWhite, cursorColor = errorRed,
        focusedLabelColor = errorRed, unfocusedLabelColor = errorRed,
        focusedContainerColor = cardBg, unfocusedContainerColor = cardBg
    )

    var consumerNumber by remember { mutableStateOf(initialForm?.consumerNumber ?: "") }
    var gasProvider by remember { mutableStateOf(initialForm?.gasProvider ?: "") }
    var facilityType by remember { mutableStateOf(initialForm?.facilityType ?: "פרטי") }
    var activationDate by remember { mutableStateOf(initialForm?.activationDate ?: "") }

    var businessId by remember { mutableStateOf(initialForm?.businessId ?: "") }
    var businessName by remember { mutableStateOf(initialForm?.businessName ?: "") }
    var businessType by remember { mutableStateOf(initialForm?.businessType ?: "") }
    var fireDeptFileNumber by remember { mutableStateOf(initialForm?.fireDeptFileNumber ?: "") }
    var city by remember { mutableStateOf(initialForm?.city ?: "") }
    var street by remember { mutableStateOf(initialForm?.street ?: "") }
    var building by remember { mutableStateOf(initialForm?.building ?: "") }
    var zip by remember { mutableStateOf(initialForm?.zip ?: "") }
    var poBox by remember { mutableStateOf(initialForm?.poBox ?: "") }

    var mainContactName by remember { mutableStateOf(initialForm?.mainContactName ?: "") }
    var contactRole by remember { mutableStateOf(initialForm?.contactRole ?: "") }
    var clientPhone by remember { mutableStateOf(initialForm?.clientPhone ?: "") }
    var email by remember { mutableStateOf(initialForm?.email ?: "") }
    var clientName by remember { mutableStateOf(initialForm?.clientName ?: "") }

    var check1_1_1 by remember { mutableStateOf(initialForm?.check1_1_1 ?: "") }
    var check1_1_2_1 by remember { mutableStateOf(initialForm?.check1_1_2_1 ?: "") }
    var check1_1_2_2 by remember { mutableStateOf(initialForm?.check1_1_2_2 ?: "") }
    var check1_1_2_3 by remember { mutableStateOf(initialForm?.check1_1_2_3 ?: "") }
    var check1_1_2_4 by remember { mutableStateOf(initialForm?.check1_1_2_4 ?: "") }
    var check1_1_2_5 by remember { mutableStateOf(initialForm?.check1_1_2_5 ?: "") }
    var check1_1_2_6 by remember { mutableStateOf(initialForm?.check1_1_2_6 ?: "") }
    var check1_2 by remember { mutableStateOf(initialForm?.check1_2 ?: "") }
    var check1_3 by remember { mutableStateOf(initialForm?.check1_3 ?: "") }
    var check1_4 by remember { mutableStateOf(initialForm?.check1_4 ?: "") }
    var check1_5_1 by remember { mutableStateOf(initialForm?.check1_5_1 ?: "") }
    var check1_5_2 by remember { mutableStateOf(initialForm?.check1_5_2 ?: "") }
    var check1_5_3 by remember { mutableStateOf(initialForm?.check1_5_3 ?: "") }
    var check1_6_1 by remember { mutableStateOf(initialForm?.check1_6_1 ?: "") }
    var check1_6_2 by remember { mutableStateOf(initialForm?.check1_6_2 ?: "") }
    var check1_7 by remember { mutableStateOf(initialForm?.check1_7 ?: "") }

    var check2_1_1 by remember { mutableStateOf(initialForm?.check2_1_1 ?: "") }
    var check2_2_1 by remember { mutableStateOf(initialForm?.check2_2_1 ?: "") }

    var check3_1 by remember { mutableStateOf(initialForm?.check3_1 ?: "") }
    var check3_2 by remember { mutableStateOf(initialForm?.check3_2 ?: "") }
    var check3_3 by remember { mutableStateOf(initialForm?.check3_3 ?: "") }
    var check3_4 by remember { mutableStateOf(initialForm?.check3_4 ?: "") }
    var check3_5 by remember { mutableStateOf(initialForm?.check3_5 ?: "") }
    var check3_6 by remember { mutableStateOf(initialForm?.check3_6 ?: "") }
    var check3_7 by remember { mutableStateOf(initialForm?.check3_7 ?: "") }
    var check3_8 by remember { mutableStateOf(initialForm?.check3_8 ?: "") }
    var check3_9 by remember { mutableStateOf(initialForm?.check3_9 ?: "") }

    var devicesList by remember { mutableStateOf(initialForm?.devicesList ?: "") }
    var check4_2 by remember { mutableStateOf(initialForm?.check4_2 ?: "") }
    var check4_3 by remember { mutableStateOf(initialForm?.check4_3 ?: "") }
    var check4_4 by remember { mutableStateOf(initialForm?.check4_4 ?: "") }
    var check4_5_1 by remember { mutableStateOf(initialForm?.check4_5_1 ?: "") }
    var check4_5_2 by remember { mutableStateOf(initialForm?.check4_5_2 ?: "") }
    var check4_5_3 by remember { mutableStateOf(initialForm?.check4_5_3 ?: "") }
    var check4_5_4 by remember { mutableStateOf(initialForm?.check4_5_4 ?: "") }
    var check4_6 by remember { mutableStateOf(initialForm?.check4_6 ?: "") }
    var check4_7_1 by remember { mutableStateOf(initialForm?.check4_7_1 ?: "") }
    var check4_7_2 by remember { mutableStateOf(initialForm?.check4_7_2 ?: "") }
    var check4_7_3 by remember { mutableStateOf(initialForm?.check4_7_3 ?: "") }
    var check4_8_1 by remember { mutableStateOf(initialForm?.check4_8_1 ?: "") }
    var check4_8_2 by remember { mutableStateOf(initialForm?.check4_8_2 ?: "") }
    var check4_9 by remember { mutableStateOf(initialForm?.check4_9 ?: "") }

    var testPressure by remember { mutableStateOf(initialForm?.testPressure ?: "") }
    var check5_1 by remember { mutableStateOf(initialForm?.check5_1 ?: "") }
    var check5_2 by remember { mutableStateOf(initialForm?.check5_2 ?: "") }

    var isFacilityValid by remember { mutableStateOf(initialForm?.isFacilityValid ?: true) }
    var requiresFixes by remember { mutableStateOf(initialForm?.requiresFixes ?: false) }
    var fixByDate by remember { mutableStateOf(initialForm?.fixByDate ?: "") }
    var isDisconnected by remember { mutableStateOf(initialForm?.isDisconnected ?: false) }
    var disconnectReason by remember { mutableStateOf(initialForm?.disconnectReason ?: "") }
    var additionalNotes by remember { mutableStateOf(initialForm?.additionalNotes ?: "") }

    val failedReasonsMap = remember { mutableStateMapOf<String, String>().apply {
        try {
            val existingJson = initialForm?.failedReasonsJson
            if (!existingJson.isNullOrBlank() && existingJson != "{}") {
                val jsonObject = JSONObject(existingJson)
                jsonObject.keys().forEach { key -> this[key] = jsonObject.getString(key) }
            }
        } catch (e: Exception) {}
    } }

    var technicianName by remember { mutableStateOf(initialForm?.technicianSignatureUri.takeIf { !it.isNullOrBlank() } ?: settingsManager.defaultTechnicianName) }
    var clientSignatureUri by remember { mutableStateOf(initialForm?.clientSignatureUri ?: "") }
    var clientNameConfirm by remember { mutableStateOf(initialForm?.clientNameConfirm ?: "") }
    var selectedExtraUris by remember { mutableStateOf<List<Uri>>(if (!initialForm?.extraImagesUris.isNullOrBlank()) initialForm!!.extraImagesUris.split(",").map { Uri.parse(it) } else emptyList()) }

    var showCriticalWarningDialog by remember { mutableStateOf(false) }

    fun copyGalleryUriToInternal(sourceUri: Uri, prefix: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val file = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream -> inputStream.copyTo(outputStream) }
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) { null }
    }

    // הוספת פונקציית בניית האובייקט (הייתה חסרה בשלד המקורי)
    fun buildCurrentForm(): GasFormD4 {
        val jsonReasons = JSONObject(failedReasonsMap.toMap()).toString()
        return GasFormD4(
            id = initialForm?.id ?: 0, sequentialNumber = sequentialNumber, date = currentDate,
            consumerNumber = consumerNumber, gasProvider = gasProvider, facilityType = facilityType,
            activationDate = activationDate, businessId = businessId, businessName = businessName,
            businessType = businessType, fireDeptFileNumber = fireDeptFileNumber, city = city,
            street = street, building = building, zip = zip, poBox = poBox,
            mainContactName = mainContactName, contactRole = contactRole, clientPhone = clientPhone,
            email = email, clientName = clientName, check1_1_1 = check1_1_1, check1_1_2_1 = check1_1_2_1,
            check1_1_2_2 = check1_1_2_2, check1_1_2_3 = check1_1_2_3, check1_1_2_4 = check1_1_2_4,
            check1_1_2_5 = check1_1_2_5, check1_1_2_6 = check1_1_2_6, check1_2 = check1_2,
            check1_3 = check1_3, check1_4 = check1_4, check1_5_1 = check1_5_1, check1_5_2 = check1_5_2,
            check1_5_3 = check1_5_3, check1_6_1 = check1_6_1, check1_6_2 = check1_6_2, check1_7 = check1_7,
            check2_1_1 = check2_1_1, check2_2_1 = check2_2_1, check3_1 = check3_1, check3_2 = check3_2,
            check3_3 = check3_3, check3_4 = check3_4, check3_5 = check3_5, check3_6 = check3_6,
            check3_7 = check3_7, check3_8 = check3_8, check3_9 = check3_9, devicesList = devicesList,
            check4_2 = check4_2, check4_3 = check4_3, check4_4 = check4_4, check4_5_1 = check4_5_1,
            check4_5_2 = check4_5_2, check4_5_3 = check4_5_3, check4_5_4 = check4_5_4, check4_6 = check4_6,
            check4_7_1 = check4_7_1, check4_7_2 = check4_7_2, check4_7_3 = check4_7_3, check4_8_1 = check4_8_1,
            check4_8_2 = check4_8_2, check4_9 = check4_9, testPressure = testPressure, check5_1 = check5_1,
            check5_2 = check5_2, isFacilityValid = isFacilityValid, requiresFixes = requiresFixes,
            fixByDate = fixByDate, isDisconnected = isDisconnected, disconnectReason = disconnectReason,
            additionalNotes = additionalNotes, failedReasonsJson = jsonReasons,
            extraImagesUris = selectedExtraUris.joinToString(",") { it.toString() },
            technicianSignatureUri = "", clientSignatureUri = clientSignatureUri,
            clientNameConfirm = clientNameConfirm
        )
    }

    // חיבור שמירה אמיתית ל-Room
    val saveToDatabase = {
        viewModel.autoSaveFormD4(buildCurrentForm())
    }

    var tempExtraUri by remember { mutableStateOf<Uri?>(null) }
    val extraCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success) { tempExtraUri?.let { selectedExtraUris = selectedExtraUris + it }; saveToDatabase() } }
    fun launchExtraCamera() {
        try {
            val file = File(context.filesDir, "extra_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempExtraUri = uri; extraCameraLauncher.launch(uri)
        } catch (e: Exception) {}
    }

    val extraGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) { val copied = uris.mapNotNull { copyGalleryUriToInternal(it, "gallery_extra") }; selectedExtraUris = selectedExtraUris + copied; saveToDatabase() }
    }

    @Composable
    fun ThreeStateRow(title: String, currentState: String, isCritical: Boolean = false, isSubItem: Boolean = false, onStateChange: (String) -> Unit) {
        val rowModifier = if (isSubItem) {
            Modifier.fillMaxWidth().padding(start = 16.dp, top = 6.dp, bottom = 6.dp).background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFFAFAFA), RoundedCornerShape(10.dp)).border(1.dp, borderColor, RoundedCornerShape(10.dp)).padding(12.dp)
        } else {
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
        }

        Column(modifier = rowModifier) {
            Text(title, color = textWhite, fontSize = 13.sp, fontWeight = if(isSubItem) FontWeight.Normal else FontWeight.Bold, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(onClick = {
                    onStateChange(if(currentState == "PASS") "" else "PASS")
                    failedReasonsMap.remove(title)
                    saveToDatabase()
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = if(currentState == "PASS") successGreen else cardBg, border = BorderStroke(1.dp, if(currentState == "PASS") successGreen else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("מתאים ✓", color = if(currentState == "PASS") Color.White else successGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(onClick = {
                    val newState = if(currentState == "FAIL") "" else "FAIL"
                    onStateChange(newState)
                    if (newState == "FAIL") {
                        failedReasonsMap[title] = failedReasonsMap[title] ?: ""
                        if (isCritical) {
                            isDisconnected = true
                            isFacilityValid = false
                            requiresFixes = false
                            showCriticalWarningDialog = true
                        }
                    } else {
                        failedReasonsMap.remove(title)
                    }
                    saveToDatabase()
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = if(currentState == "FAIL") errorRed else cardBg, border = BorderStroke(1.dp, if(currentState == "FAIL") errorRed else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("לא מתאים ✗", color = if(currentState == "FAIL") Color.White else errorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(onClick = {
                    onStateChange(if(currentState == "NA") "" else "NA")
                    failedReasonsMap.remove(title)
                    saveToDatabase()
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = if(currentState == "NA") Color.Gray else cardBg, border = BorderStroke(1.dp, if(currentState == "NA") Color.Gray else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("לא ישים ⚪", color = if(currentState == "NA") Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (showCriticalWarningDialog) {
            AlertDialog(
                onDismissRequest = { showCriticalWarningDialog = false },
                title = { Text("סכנה - ליקוי חמור", color = errorRed, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                text = { Text("יש להפסיק מיד את הספקת הגז!\n\nסימנת 'לא מתאים' בסעיף קריטי (⊕) או בבדיקת אטימות. הסטטוס בסוף הדוח עודכן אוטומטית למצב של ניתוק.", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                confirmButton = { Button(onClick = { showCriticalWarningDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = errorRed)) { Text("הבנתי, המערכת נותקה") } }
            )
        }

        Scaffold(
            containerColor = bgScreenColor,
            topBar = {
                Surface(color = headerBg, shadowElevation = 4.dp) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onNavigateBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "חזור", tint = textWhite) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("דוח בדיקה ד-4 (מאגר נפרד)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(modifier = Modifier.height(4.dp))

                FormCard("פרטי בית העסק / הלקוח", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = businessName, onValueChange = { businessName = it; saveToDatabase() }, label = { Text("שם העסק / הלקוח") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = businessId, onValueChange = { businessId = it; saveToDatabase() }, label = { Text("ח.פ/ע.מ/ת.ז") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = businessType, onValueChange = { businessType = it; saveToDatabase() }, label = { Text("מהות העסק") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = fireDeptFileNumber, onValueChange = { fireDeptFileNumber = it; saveToDatabase() }, label = { Text("מס' תיק כבאות") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it; saveToDatabase() }, label = { Text("יישוב") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = street, onValueChange = { street = it; saveToDatabase() }, label = { Text("רחוב/בית") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = clientName, onValueChange = { clientName = it; saveToDatabase() }, label = { Text("שם איש קשר") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = clientPhone, onValueChange = { clientPhone = it; saveToDatabase() }, label = { Text("טלפון איש קשר") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = textFieldColors)
                }

                FormCard("נתוני המערכת", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = consumerNumber, onValueChange = { consumerNumber = it; saveToDatabase() }, label = { Text("מספר צרכן") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = gasProvider, onValueChange = { gasProvider = it; saveToDatabase() }, label = { Text("ספק הגז") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = activationDate, onValueChange = { activationDate = it; saveToDatabase() }, label = { Text("תאריך הפעלת מתקן") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("סוג המתקן:", color = textWhite, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = facilityType == "פרטי", onClick = { facilityType = "פרטי"; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("פרטי", color = textWhite)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = facilityType == "מסחרי", onClick = { facilityType = "מסחרי"; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("מסחרי", color = textWhite)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = facilityType == "אחר", onClick = { facilityType = "אחר"; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("אחר", color = textWhite)
                    }
                }

                FormCard("1. מאגר במכלים מיטלטלים", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("1.1.1 מכלים במקום פתוח ומאוורר (לא במפלס נמוך/מגורים)", check1_1_1, isCritical = true) { check1_1_1 = it }
                    Text("1.1.2 מרחקי בטיחות (⊕):", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("0.7 מ' ממקור חום וניצוצות", check1_1_2_1, isCritical = true, isSubItem = true) { check1_1_2_1 = it }
                    ThreeStateRow("1.7 מ' מאש גלויה", check1_1_2_2, isCritical = true, isSubItem = true) { check1_1_2_2 = it }
                    ThreeStateRow("0.5 מ' מבורות/תאים סגורים", check1_1_2_3, isCritical = true, isSubItem = true) { check1_1_2_3 = it }
                    ThreeStateRow("3 מ' מבורות ופתחי ניקוז פתוחים", check1_1_2_4, isCritical = true, isSubItem = true) { check1_1_2_4 = it }
                    ThreeStateRow("1.2 מ' מפתחי בניין", check1_1_2_5, isCritical = true, isSubItem = true) { check1_1_2_5 = it }
                    ThreeStateRow("3 מ' מפתחים במפלס נמוך", check1_1_2_6, isCritical = true, isSubItem = true) { check1_1_2_6 = it }

                    ThreeStateRow("1.2 יש שילוט אזהרה עם שם ספק הגז וטלפון לחירום", check1_2) { check1_2 = it }
                    ThreeStateRow("1.3 הווסת והסעפת מקובעים", check1_3) { check1_3 = it }
                    ThreeStateRow("1.4 במתקן התזת מים, מובטחת התזה על כל המכלים", check1_4) { check1_4 = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("1.5 אם המאגר בחדר גז:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("בחדר יש עד 20 מכלים", check1_5_1, isSubItem = true) { check1_5_1 = it }
                    ThreeStateRow("גוף תאורה בתקרה והמפסק בחוץ", check1_5_2, isSubItem = true) { check1_5_2 = it }
                    ThreeStateRow("בחדר לא מוחזקים חומרים דליקים", check1_5_3, isSubItem = true) { check1_5_3 = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("1.6 אם המאגר במכלאה:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("במכלאה יש עד 20 מכלים", check1_6_1, isSubItem = true) { check1_6_1 = it }
                    ThreeStateRow("המכלאה מגודרת ומאווררת", check1_6_2, isSubItem = true) { check1_6_2 = it }

                    ThreeStateRow("1.7 המאספים (רמפות) מותקנים יציב ולכל אחד ברז ניתוק", check1_7) { check1_7 = it }
                }

                FormCard("2. מאגר במכלים נייחים", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("2.1.1 יש מחסום בפני התקרבות כלי רכב ושילוט בטיחות", check2_1_1) { check2_1_1 = it }
                    ThreeStateRow("2.2.1 למכל יש לוחית זיהוי קריאה והנתונים תואמים", check2_2_1) { check2_2_1 = it }
                }

                FormCard("3. מערכת הצינורות", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("3.1 יש שסתום סגירה לרעידת אדמה (התקנה אחרי 2012)", check3_1) { check3_1 = it }
                    ThreeStateRow("3.2 השסתום מפולס והחיבורים תקינים", check3_2) { check3_2 = it }
                    ThreeStateRow("3.3 יש ברז ניתוק נגיש ומשולט בכניסה לבניין", check3_3) { check3_3 = it }
                    ThreeStateRow("3.4 מוצא שסתום הפריקה מחובר לאוויר חוץ", check3_4) { check3_4 = it }
                    ThreeStateRow("3.5 הלחץ בצנרת בתוך המבנה אינו גדול מ-1.4 בר", check3_5, isCritical = true) { check3_5 = it }
                    ThreeStateRow("3.6 יש אמצעים להגבלת לחץ בווסתים ללא שסתום פריקה", check3_6, isCritical = true) { check3_6 = it }
                    ThreeStateRow("3.7 יש ברז ניתוק בקרבת כל מכשיר צורך גפ\"מ", check3_7) { check3_7 = it }
                    ThreeStateRow("3.8 הצנרת ומרכיביה מקובעים", check3_8) { check3_8 = it }
                    ThreeStateRow("3.9 (⊕) כל מוצא פתוח סגור בפקק/ברז תקין", check3_9, isCritical = true) { check3_9 = it }
                }

                FormCard("4. חיבור המכשירים", cardBg, borderColor, primaryColor) {
                    OutlinedTextField(value = devicesList, onValueChange = { devicesList = it; saveToDatabase() }, label = { Text("4.1 פרט את המכשירים המחוברים") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, minLines = 2)
                    Spacer(modifier = Modifier.height(12.dp))
                    ThreeStateRow("4.2 שלמות המכשירים בבחינה חזותית", check4_2) { check4_2 = it }
                    ThreeStateRow("4.3 צינור אלסטומרי... הוחלף בעקבות הבחינה החזותית", check4_3) { check4_3 = it }
                    ThreeStateRow("4.4 קצות הזרנוק לניפלים מחוזקים בחבקים / אורך תקין", check4_4) { check4_4 = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("4.5 מכשירים צורכי גפ\"מ עם ארובה אטמוספרית:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("יש תווית בדיקה שנתית ד-5 בדירת מגורים", check4_5_1, isSubItem = true) { check4_5_1 = it }
                    ThreeStateRow("מכשיר חימום מים אינו מותקן בחדרי שינה/רחצה", check4_5_2, isSubItem = true) { check4_5_2 = it }
                    ThreeStateRow("מכשיר להסקה בדירה - לא עברו 3 שנים מתקן 158", check4_5_3, isSubItem = true) { check4_5_3 = it }
                    ThreeStateRow("מכשיר חימום מים לצריכה (>0.5) - לא עברו 5 שנים", check4_5_4, isSubItem = true) { check4_5_4 = it }
                    ThreeStateRow("4.6 מכשיר ללא ארובה אינו מותקן בשינה/רחצה", check4_6) { check4_6 = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("4.7 תקינות ארובות:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("הארובה שלמה ומחוזקת למניעת שינוי", check4_7_1, isSubItem = true) { check4_7_1 = it }
                    ThreeStateRow("מוצא ארובה אטמוספרית מרוחק 0.5 מ' מכל פתח", check4_7_2, isSubItem = true) { check4_7_2 = it }
                    ThreeStateRow("מוצא ארובה כפולה מרוחק 0.4 מ' מכל פתח", check4_7_3, isSubItem = true) { check4_7_3 = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("4.8 מכשירים לשימוש מסחרי / תעשייתי:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("התקן לסגירת גז במכשירי חימום חלל", check4_8_1, isSubItem = true) { check4_8_1 = it }
                    ThreeStateRow("פתח אוורור קבוע במטבחי ציבור", check4_8_2, isSubItem = true) { check4_8_2 = it }
                    ThreeStateRow("4.9 למכשירים במקום נמוך יש תווית ד-6 שנתית", check4_9) { check4_9 = it }
                }

                FormCard("5. בדיקת אטימות ולחץ", cardBg, borderColor, primaryColor) {
                    Text("5.1 בדיקת אטימות (⊕)", fontWeight = FontWeight.Bold, color = textWhite, fontSize = 13.sp)
                    Text("הבדיקה בלחץ השורר בקו למשך 15 דק'.", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = testPressure, onValueChange = { testPressure = it; saveToDatabase() }, label = { Text("לחץ בדיקה במיליבר") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    ThreeStateRow("הלחץ נשמר ללא ירידה", check5_1, isCritical = true) { check5_1 = it }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = borderColor)

                    Text("5.2 בדיקת וסת הלחץ (⊕)", fontWeight = FontWeight.Bold, color = textWhite, fontSize = 13.sp)
                    Text("משך 5 דק'. הלחץ אינו חורג מ-30% מעל הנומינלי.", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    ThreeStateRow("הלחץ לא חרג מהמותר", check5_2, isCritical = true) { check5_2 = it }
                }

                FormCard("6. סיכום מבצע הבדיקה", cardBg, borderColor, primaryColor) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isFacilityValid = true; requiresFixes = false; isDisconnected = false; saveToDatabase() }) {
                        RadioButton(selected = isFacilityValid && !requiresFixes && !isDisconnected, onClick = { isFacilityValid = true; requiresFixes = false; isDisconnected = false; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = successGreen))
                        Text("המתקן נמצא תקין בהתאם לדרישות", color = successGreen, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isFacilityValid = false; requiresFixes = true; isDisconnected = false; saveToDatabase() }) {
                        RadioButton(selected = requiresFixes, onClick = { isFacilityValid = false; requiresFixes = true; isDisconnected = false; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF9800)))
                        Text("נמצאו ליקויים לתיקון עד תאריך:", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                    AnimatedVisibility(visible = requiresFixes) {
                        OutlinedTextField(value = fixByDate, onValueChange = { fixByDate = it; saveToDatabase() }, label = { Text("תאריך יעד לתיקון") }, modifier = Modifier.fillMaxWidth().padding(start = 40.dp, bottom = 8.dp), colors = textFieldColors)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isFacilityValid = false; requiresFixes = false; isDisconnected = true; saveToDatabase() }) {
                        RadioButton(selected = isDisconnected, onClick = { isFacilityValid = false; requiresFixes = false; isDisconnected = true; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = errorRed))
                        Text("הספקת הגז נותקה עקב ליקויים", color = errorRed, fontWeight = FontWeight.Bold)
                    }
                    AnimatedVisibility(visible = isDisconnected) {
                        OutlinedTextField(value = disconnectReason, onValueChange = { disconnectReason = it; saveToDatabase() }, label = { Text("סיבת הניתוק") }, modifier = Modifier.fillMaxWidth().padding(start = 40.dp, bottom = 8.dp), colors = textFieldColors)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = additionalNotes, onValueChange = { additionalNotes = it; saveToDatabase() }, label = { Text("הערות נוספות") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = textFieldColors)
                }

                AnimatedVisibility(visible = failedReasonsMap.isNotEmpty()) {
                    FormCard("פירוט ליקויים שנמצאו בבדיקה (חובה למלא)", cardBg, errorRed, errorRed) {
                        failedReasonsMap.keys.forEach { sectionTitle ->
                            OutlinedTextField(value = failedReasonsMap[sectionTitle] ?: "", onValueChange = { failedReasonsMap[sectionTitle] = it; saveToDatabase() }, label = { Text("פרט מדוע '$sectionTitle' אינו תקין") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = errorFieldColors, minLines = 2)
                        }
                    }
                }

                FormCard("תמונות מצורפים", cardBg, borderColor, primaryColor) {
                    Text("התמונות יצורפו בסוף הדוח.", color = textGray, fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val secondaryButtonBg = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
                        val secondaryButtonText = if (isDark) Color.White else Color.Black
                        Button(onClick = { launchExtraCamera() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = secondaryButtonBg)) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp), tint = secondaryButtonText); Spacer(modifier = Modifier.width(4.dp)); Text("מצלמה", color = secondaryButtonText)
                        }
                        Button(onClick = { extraGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = secondaryButtonBg)) {
                            Icon(Icons.Default.Collections, null, modifier = Modifier.size(16.dp), tint = secondaryButtonText); Spacer(modifier = Modifier.width(4.dp)); Text("גלריה", color = secondaryButtonText)
                        }
                    }
                    if (selectedExtraUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedExtraUris) { uri ->
                                Box(modifier = Modifier.size(80.dp)) {
                                    AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick = { selectedExtraUris = selectedExtraUris - uri; saveToDatabase() },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape).size(20.dp)
                                    ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                                }
                            }
                        }
                    }
                }

                FormCard("7. אישור הלקוח וחתימות", cardBg, borderColor, primaryColor) {
                    Text("הבהרה: מכשירי צריכת הגז אינם נבדקים ואינם נכללים בטופס בדיקה זה.", color = errorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("שם מבצע הבדיקה: ${technicianName}", color = textWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("אני מאשר שנערכה בדיקה של מתקן הגז שאיננה כוללת בדיקת תקינות של מכשירי צריכת הגז. נמסר לי עותק של טופס הבדיקה על ידי הבודק.", color = textWhite, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("חתימת הלקוח:", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    TechnicianSignatureTouchPad(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp), initialSignatureUri = if (clientSignatureUri.isNotEmpty()) clientSignatureUri else null, onSignatureSaved = { uri -> clientSignatureUri = uri; saveToDatabase() })

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = clientNameConfirm, onValueChange = { clientNameConfirm = it ; saveToDatabase() }, label = { Text("שם החותם / ת.ז") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                }

                // החלפת הכפתור הזמני ב-4 כפתורי הפעולה
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { saveToDatabase(); viewModel.previewPdfD4(context, buildCurrentForm()) },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(10.dp)
                        ) { Text("תצוגה מקדימה", fontWeight = FontWeight.Bold, fontSize = 13.sp) }

                        Button(
                            onClick = { saveToDatabase(); Toast.makeText(context, "נשמר כטיוטה", Toast.LENGTH_SHORT).show(); onNavigateBack() },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = successGreen), shape = RoundedCornerShape(10.dp)
                        ) { Text("שמור כטיוטה", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (initialForm != null) viewModel.deleteFormD4(initialForm)
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isDark) Color(0xFF333333) else Color(0xFFE0E0E0)), shape = RoundedCornerShape(10.dp)
                        ) { Text("מחק טופס", fontWeight = FontWeight.Bold, color = errorRed, fontSize = 13.sp) }

                        Button(
                            onClick = {
                                saveToDatabase()
                                viewModel.sharePdfD4(context, buildCurrentForm()) { onNavigateBack() }
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(10.dp)
                        ) { Text("שתף וסיים", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}