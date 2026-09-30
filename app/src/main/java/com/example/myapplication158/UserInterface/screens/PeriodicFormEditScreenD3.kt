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
import com.example.myapplication158.data.GasFormD3
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
fun PeriodicFormEditScreenD3(
    viewModel: GasFormViewModel,
    form: Any? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    val initialForm = form as? GasFormD3

    var currentFormId by remember { mutableIntStateOf(initialForm?.id ?: 0) }
    var sequentialNumber by remember { mutableIntStateOf(if (initialForm != null && initialForm.sequentialNumber > 0) initialForm.sequentialNumber else settingsManager.currentFormNumber) }

    val isDark = settingsManager.isDarkMode
    val primaryColor = Color(0xFFFF9800) // כתום לד-3
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
    var consumerMeterNumber by remember { mutableStateOf(initialForm?.consumerMeterNumber ?: "") }
    var meterManufactureYear by remember { mutableStateOf(initialForm?.meterManufactureYear ?: "") }

    var clientName by remember { mutableStateOf(initialForm?.clientName ?: "") }
    var city by remember { mutableStateOf(initialForm?.city ?: "") }
    var street by remember { mutableStateOf(initialForm?.street ?: "") }
    var gasProvider by remember { mutableStateOf(initialForm?.gasProvider ?: "") }

    var facilityTypeSelection by remember { mutableStateOf(
        when {
            initialForm?.facilityType?.startsWith("פרטי") == true -> "פרטי"
            initialForm?.facilityType?.startsWith("מסחרי") == true -> "מסחרי"
            initialForm?.facilityType?.startsWith("אחר") == true -> "אחר"
            else -> ""
        }
    ) }
    var facilityDetailsText by remember { mutableStateOf(
        if (initialForm?.facilityType?.contains(":") == true) {
            initialForm.facilityType.substringAfter(":").trim()
        } else ""
    ) }
    var facilityPhotoUri by remember { mutableStateOf(initialForm?.facilityPhotoUri ?: "") }

    var businessId by remember { mutableStateOf(initialForm?.businessId ?: "") }
    var businessName by remember { mutableStateOf(initialForm?.businessName ?: "") }
    var businessType by remember { mutableStateOf(initialForm?.businessType ?: "") }
    var fireDeptFileNumber by remember { mutableStateOf(initialForm?.fireDeptFileNumber ?: "") }
    var poBox by remember { mutableStateOf(initialForm?.poBox ?: "") }
    var zip by remember { mutableStateOf(initialForm?.zip ?: "") }
    var building by remember { mutableStateOf(initialForm?.building ?: "") }

    var mainContactName by remember { mutableStateOf(initialForm?.mainContactName ?: "") }
    var contactRole by remember { mutableStateOf(initialForm?.contactRole ?: "") }
    var clientPhone by remember { mutableStateOf(initialForm?.clientPhone ?: "") }
    var email by remember { mutableStateOf(initialForm?.email ?: "") }

    var check1_1 by remember { mutableStateOf(initialForm?.check1_1 ?: "") }
    var check1_2 by remember { mutableStateOf(initialForm?.check1_2 ?: "") }
    var check1_3 by remember { mutableStateOf(initialForm?.check1_3 ?: "") }
    var check1_5 by remember { mutableStateOf(initialForm?.check1_5 ?: "") }
    var check1_6 by remember { mutableStateOf(initialForm?.check1_6 ?: "") }
    var check1_7 by remember { mutableStateOf(initialForm?.check1_7 ?: "") }
    var check1_8 by remember { mutableStateOf(initialForm?.check1_8 ?: "") }
    var check1_9 by remember { mutableStateOf(initialForm?.check1_9 ?: "") }

    var devicesList by remember { mutableStateOf(initialForm?.devicesList ?: "") }
    var check2_1 by remember { mutableStateOf(initialForm?.check2_1 ?: "") }
    var check2_2 by remember { mutableStateOf(initialForm?.check2_2 ?: "") }
    var check2_3 by remember { mutableStateOf(initialForm?.check2_3 ?: "") }
    var check2_5 by remember { mutableStateOf(initialForm?.check2_5 ?: "") }
    var check2_6_1 by remember { mutableStateOf(initialForm?.check2_6_1 ?: "") }
    var check2_6_2 by remember { mutableStateOf(initialForm?.check2_6_2 ?: "") }
    var check2_6_3 by remember { mutableStateOf(initialForm?.check2_6_3 ?: "") }
    var check2_6_4 by remember { mutableStateOf(initialForm?.check2_6_4 ?: "") }
    var check2_6_5 by remember { mutableStateOf(initialForm?.check2_6_5 ?: "") }
    var check2_7_1 by remember { mutableStateOf(initialForm?.check2_7_1 ?: "") }
    var check2_7_2 by remember { mutableStateOf(initialForm?.check2_7_2 ?: "") }
    var check2_7_3 by remember { mutableStateOf(initialForm?.check2_7_3 ?: "") }
    var check2_8_1 by remember { mutableStateOf(initialForm?.check2_8_1 ?: "") }
    var check2_8_2 by remember { mutableStateOf(initialForm?.check2_8_2 ?: "") }
    var check2_9 by remember { mutableStateOf(initialForm?.check2_9 ?: "") }

    var check3_1 by remember { mutableStateOf(initialForm?.check3_1 ?: "") }
    var testPressure by remember { mutableStateOf(initialForm?.testPressure ?: "") }
    var check3_2 by remember { mutableStateOf(initialForm?.check3_2 ?: "") }

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

    var technicianName by remember { mutableStateOf(initialForm?.clientName?.takeIf { it.isNotBlank() } ?: settingsManager.defaultTechnicianName) }
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

    fun buildCurrentForm(): GasFormD3 {
        val jsonReasons = JSONObject(failedReasonsMap.toMap()).toString()
        val currentFacilityType = if (facilityTypeSelection.isNotBlank()) "$facilityTypeSelection: $facilityDetailsText" else ""

        return GasFormD3(
            id = currentFormId, sequentialNumber = sequentialNumber, date = currentDate,
            consumerNumber = consumerNumber, consumerMeterNumber = consumerMeterNumber, meterManufactureYear = meterManufactureYear,
            clientName = clientName, city = city, street = street, gasProvider = gasProvider, facilityType = currentFacilityType,
            facilityPhotoUri = facilityPhotoUri, businessId = businessId, businessName = businessName, businessType = businessType,
            fireDeptFileNumber = fireDeptFileNumber, poBox = poBox, zip = zip, building = building, mainContactName = mainContactName,
            contactRole = contactRole, clientPhone = clientPhone, email = email, check1_1 = check1_1, check1_2 = check1_2,
            check1_3 = check1_3, check1_4 = "NA", check1_5 = check1_5, check1_6 = check1_6, check1_7 = check1_7,
            check1_8 = check1_8, check1_9 = check1_9, devicesList = devicesList, check2_1 = check2_1, check2_2 = check2_2,
            check2_3 = check2_3, check2_4 = "NA", check2_5 = check2_5, check2_6_1 = check2_6_1, check2_6_2 = check2_6_2,
            check2_6_3 = check2_6_3, check2_6_4 = check2_6_4, check2_6_5 = check2_6_5, check2_7_1 = check2_7_1,
            check2_7_2 = check2_7_2, check2_7_3 = check2_7_3, check2_8_1 = check2_8_1, check2_8_2 = check2_8_2,
            check2_9 = check2_9, check3_1 = check3_1, testPressure = testPressure, check3_2 = check3_2,
            isFacilityValid = isFacilityValid, requiresFixes = requiresFixes, fixByDate = fixByDate, isDisconnected = isDisconnected,
            disconnectReason = disconnectReason, additionalNotes = additionalNotes, failedReasonsJson = jsonReasons,
            extraImagesUris = selectedExtraUris.joinToString(",") { it.toString() }, technicianSignatureUri = "",
            clientSignatureUri = clientSignatureUri, clientNameConfirm = clientNameConfirm
        )
    }

    fun validateForm(): String? {
        if (clientName.isBlank() && businessName.isBlank()) return "חובה למלא את שם הלקוח או העסק."
        if (clientPhone.isBlank()) return "חובה למלא מס' טלפון איש קשר."
        if (city.isBlank() || street.isBlank()) return "חובה למלא את הכתובת (יישוב ורחוב)."
        if (facilityTypeSelection.isBlank() || facilityDetailsText.isBlank()) return "חובה לבחור ולפרט בתיבת הטקסט את 'סוג המתקן'."

        val requiredChecks = listOf(
            check1_1 to "1.1", check1_2 to "1.2", check1_3 to "1.3",
            check1_5 to "1.4", check1_6 to "1.5", check1_7 to "1.6", check1_8 to "1.7", check1_9 to "1.8",
            check2_2 to "2.2", check2_3 to "2.3", check2_5 to "2.4",
            check2_6_1 to "2.5.1", check2_6_2 to "2.5.2", check2_6_3 to "2.5.3", check2_6_4 to "2.5.4", check2_6_5 to "2.5.5",
            check2_7_1 to "2.6.1", check2_7_2 to "2.6.2", check2_7_3 to "2.6.3",
            check2_8_1 to "2.7.1", check2_8_2 to "2.7.2", check2_9 to "2.8",
            check3_1 to "3.1", check3_2 to "3.2"
        )

        val missingChecks = requiredChecks.filter { it.first.isBlank() }.map { it.second }
        if (missingChecks.isNotEmpty()) {
            return "חובה לסמן את כל סעיפי הבדיקה.\nחסר סימון בסעיפים: ${missingChecks.joinToString(", ")}"
        }

        return null // הכל תקין
    }

    val saveToDatabase = {
        viewModel.autoSaveFormD3(buildCurrentForm()) { newId ->
            if (currentFormId == 0) { currentFormId = newId }
        }
    }

    var tempFacilityUri by remember { mutableStateOf<Uri?>(null) }
    val facilityCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { tempFacilityUri?.let { facilityPhotoUri = it.toString() }; saveToDatabase() }
    }
    fun launchFacilityCamera() {
        try {
            val file = File(context.filesDir, "facility_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempFacilityUri = uri; facilityCameraLauncher.launch(uri)
        } catch (e: Exception) {}
    }
    val facilityGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { copyGalleryUriToInternal(uri, "facility_gal")?.let { facilityPhotoUri = it.toString(); saveToDatabase() } }
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
    fun ThreeStateRow(number: String, title: String, currentState: String, isCritical: Boolean = false, isSubItem: Boolean = false, customFontSize: Int? = null, onStateChange: (String) -> Unit) {
        val rowModifier = if (isSubItem) {
            Modifier.fillMaxWidth().padding(start = 16.dp, top = 6.dp, bottom = 6.dp).background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFFAFAFA), RoundedCornerShape(10.dp)).border(1.dp, borderColor, RoundedCornerShape(10.dp)).padding(12.dp)
        } else {
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
        }

        Column(modifier = rowModifier) {
            // עיצוב אחיד עם קו מפריד
            Text(
                text = "$number - $title",
                color = textWhite,
                fontSize = customFontSize?.sp ?: 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(onClick = {
                    onStateChange(if(currentState == "PASS") "" else "PASS")
                    failedReasonsMap.remove(number)
                    saveToDatabase()
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = if(currentState == "PASS") successGreen else cardBg, border = BorderStroke(1.dp, if(currentState == "PASS") successGreen else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("מתאים ✓", color = if(currentState == "PASS") Color.White else successGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(onClick = {
                    val newState = if(currentState == "FAIL") "" else "FAIL"
                    onStateChange(newState)
                    if (newState == "FAIL") {
                        failedReasonsMap[number] = failedReasonsMap[number] ?: ""
                        if (isCritical) {
                            isDisconnected = true
                            isFacilityValid = false
                            requiresFixes = false
                            showCriticalWarningDialog = true
                        }
                    } else {
                        failedReasonsMap.remove(number)
                    }
                    saveToDatabase()
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = if(currentState == "FAIL") errorRed else cardBg, border = BorderStroke(1.dp, if(currentState == "FAIL") errorRed else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("לא מתאים ✗", color = if(currentState == "FAIL") Color.White else errorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(onClick = {
                    onStateChange(if(currentState == "NA") "" else "NA")
                    failedReasonsMap.remove(number)
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
                text = { Text("יש להפסיק מיד את הספקת הגז!\n\nסימנת 'לא מתאים' בסעיף קריטי (⊕). הסטטוס בסוף הדוח עודכן אוטומטית למצב של ניתוק.", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
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
                            Text("דוח בדיקה ד-3 (מאגר משותף)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(modifier = Modifier.height(4.dp))

                FormCard("פרטי הלקוח והנכס", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = clientName, onValueChange = { clientName = it; saveToDatabase() }, label = { Text("שם לקוח / עסק") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = clientPhone, onValueChange = { clientPhone = it; saveToDatabase() }, label = { Text("טלפון") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it; saveToDatabase() }, label = { Text("יישוב") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = street, onValueChange = { street = it; saveToDatabase() }, label = { Text("רחוב/בית") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                }

                FormCard("נתוני המונה והמתקן", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = consumerNumber, onValueChange = { consumerNumber = it; saveToDatabase() }, label = { Text("מספר צרכן") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = gasProvider, onValueChange = { gasProvider = it; saveToDatabase() }, label = { Text("ספק הגז") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = consumerMeterNumber, onValueChange = { consumerMeterNumber = it; saveToDatabase() }, label = { Text("מספר מונה") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = meterManufactureYear, onValueChange = { meterManufactureYear = it; saveToDatabase() }, label = { Text("שנת ייצור מונה") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("סוג המתקן (חובה לפרט):", color = textWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = facilityTypeSelection == "פרטי", onClick = { facilityTypeSelection = "פרטי"; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("פרטי", color = textWhite)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = facilityTypeSelection == "מסחרי", onClick = { facilityTypeSelection = "מסחרי"; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("מסחרי", color = textWhite)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = facilityTypeSelection == "אחר", onClick = { facilityTypeSelection = "אחר"; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("אחר", color = textWhite)
                    }

                    AnimatedVisibility(visible = facilityTypeSelection.isNotBlank()) {
                        Column {
                            OutlinedTextField(
                                value = facilityDetailsText,
                                onValueChange = { facilityDetailsText = it; saveToDatabase() },
                                label = { Text("פרט סוג מתקן ($facilityTypeSelection)") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = textFieldColors
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                val secondaryButtonBg = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
                                val secondaryButtonText = if (isDark) Color.White else Color.Black
                                Button(onClick = { launchFacilityCamera() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = secondaryButtonBg)) {
                                    Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp), tint = secondaryButtonText); Spacer(modifier = Modifier.width(4.dp)); Text("צלם מתקן", color = secondaryButtonText, fontSize = 12.sp)
                                }
                                Button(onClick = { facilityGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = secondaryButtonBg)) {
                                    Icon(Icons.Default.Collections, null, modifier = Modifier.size(16.dp), tint = secondaryButtonText); Spacer(modifier = Modifier.width(4.dp)); Text("גלריה", color = secondaryButtonText, fontSize = 12.sp)
                                }
                            }
                            if (facilityPhotoUri.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.size(100.dp)) {
                                    AsyncImage(model = Uri.parse(facilityPhotoUri), contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick = { facilityPhotoUri = ""; saveToDatabase() },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape).size(24.dp)
                                    ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                                }
                            }
                        }
                    }
                }

                FormCard("1. קווי צינורות", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("1.1", "קיים ברז ניתוק נגיש ליחידת הצריכה (דירה או בית עסק) מזוהה בשם הצרכן או במספר הדירה", check1_1) { check1_1 = it }
                    ThreeStateRow("1.2", "המונה מקובע", check1_2) { check1_2 = it }
                    ThreeStateRow("1.3", "פרק הזמן ממועד ייצור המונה אינו גדול מ-18 שנה, ובהתקנה במגורים - סך הנפח המצטבר אינו גדול מ-2000 מ\"ק (מונה שאינו עומד בתנאים יוחלף)", check1_3) { check1_3 = it }
                    ThreeStateRow("1.4", "בשסתומי פריקה המורכבים בווסת או לאחריו, והנמצאים בתוך הבניין, מוצא שסתום הפריקה מחובר אל אוויר החוץ, וקצה הצינור מרוחק 1 מ' מכל פתח בבניין שמתחתיו", check1_5) { check1_5 = it }
                    ThreeStateRow("1.5 (⊕)", "בווסתים ללא שסתום פריקה יש אמצעים המגבילים את הלחץ לצרכן, ואם הם מותקנים בתוך מבנה- יש אמצעים המגבילים את פתח האוויר", check1_6, isCritical = true) { check1_6 = it }
                    ThreeStateRow("1.6", "יש ברז ניתוק בקרבת כל מכשיר צורך גפ\"מ", check1_7) { check1_7 = it }
                    ThreeStateRow("1.7", "הצנרת ומרכיביה מקובעים", check1_8) { check1_8 = it }
                    ThreeStateRow("1.8 (⊕)", "כל מוצא של מתקן, שאינו מחובר באופן קבוע למכשיר, סגור בפקק או באבזר ניתוק מהיר או בשסתום חד-כיווני, ונמנע שחרור גפ\"מ לאוויר", check1_9, isCritical = true) { check1_9 = it }
                }

                FormCard("2. חיבור המכשירים", cardBg, borderColor, primaryColor) {
                    OutlinedTextField(value = devicesList, onValueChange = { devicesList = it; saveToDatabase() }, label = { Text("2.1 פרט את המכשירים המחוברים למתקן בזמן הבדיקה") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, minLines = 2)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("שלמות המכשירים בבחינה חזותית:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("2.2", "מכשירים קבועים מחוברים בצינור קשיח", check2_2, isSubItem = true) { check2_2 = it }
                    ThreeStateRow("2.3", "הוחלף צינור אלסטומרי לחיבור מכשיר בצינור תקני, למעט צינור גמיש מפלדה לא מחלידה וצינור אלסטומרי שקוטרו הפנימי גדול מ-8 מ\"מ עם קצוות מתוברגים, שיוחלפו רק לפי הצורך בעקבות הבחינה החזותית. קצוות הזרנוק המחוברים לניפלים מחוזקים בחבקים", check2_3, isSubItem = true) { check2_3 = it }
                    ThreeStateRow("2.4", "אורך הצינורות האלסטומריים אינו גדול מ-3 מ', למעט זרנוק ממין שכינויו 18 בהתקנות תעשייתיות וחקלאיות", check2_5, isSubItem = true) { check2_5 = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("מכשירים צורכי גפ\"מ עם ארובה אטמוספרית:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("2.5.1", "למכשיר צורך גפ\"מ עם ארובה אטמוספרית, המותקן בתוך דירת מגורים, יש תווית אישור בדיקה שנתית תקפה לפי טופס ד-5", check2_6_1, isSubItem = true) { check2_6_1 = it }
                    ThreeStateRow("2.5.2", "מכשיר חימום מים להסקה עם ארובה אטמוספרית אינו מותקן בחדרי שינה, שירותים או רחצה (לחדרי רחצה ושירותים בתוקף עד 31.12.11)", check2_6_2, isSubItem = true) { check2_6_2 = it }
                    ThreeStateRow("2.5.3", "למכשיר חימום מים להסקה עם ארובה אטמוספרית המותקן בתוך דירת מגורים, יש תווית אישור בדיקה שנתית תקפה לפי טופס ד-5, ולא עברו 3 שנים מיום פרסום גיליון התיקון מס' 1 לת\"י 158 חלק 3", check2_6_3, isSubItem = true) { check2_6_3 = it }
                    ThreeStateRow("2.5.4", "למכשיר חימום מים לצריכה עם ארובה אטמוספרית שהספקו גדול מ-0.5 ק\"ג גז לשעה, המותקן בתוך דירת מגורים, יש תווית אישור בדיקה שנתית תקפה לפי טופס ד-5, ולא עברו 5 שנים מיום פרסום גיליון התיקון מס' 1 לת\"י 158 חלק 3", check2_6_4, isSubItem = true) { check2_6_4 = it }
                    ThreeStateRow("2.5.5", "מכשיר צורך גפ\"מ ללא ארובה אינו מותקן באמבטיה, בשירותים או בחדר שינה, למעט מכשיר בישול ואפייה ובתנאי שמתקיימים תנאי אוורור לפי ת\"י 158 חלק 3 טבלה 2", check2_6_5, isSubItem = true) { check2_6_5 = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("תקינות ארובות למכשירים צורכי גפ\"מ:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("2.6.1", "הארובה שלמה ומחוזקת באופן המונע אפשרות לשינוי ממצב ההתקנה", check2_7_1, isSubItem = true) { check2_7_1 = it }
                    ThreeStateRow("2.6.2", "מוצא ארובה אטמוספרית מרוחק 0.5 מ' מכל פתח בבניין", check2_7_2, isSubItem = true) { check2_7_2 = it }
                    ThreeStateRow("2.6.3", "מוצא ארובה כפולה מרוחק 0.4 מ' מכל פתח בבניין", check2_7_3, isSubItem = true) { check2_7_3 = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("מכשירים לשימוש ציבורי, מסחרי, חקלאי או תעשייתי:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("2.7.1", "מכשירים לחימום חלל במקומות ציבוריים מצוידים בהתקן לסגירת זרימת הגז כשהלהבה כבה", check2_8_1, isSubItem = true) { check2_8_1 = it }
                    ThreeStateRow("2.7.2", "במטבחים של מבני ציבור יש פתח אוורור קבוע אל אוויר החוץ או אוורור מאולץ שקיל", check2_8_2, isSubItem = true) { check2_8_2 = it }
                    ThreeStateRow("2.8", "למכשירים המותקנים במקום נמוך יש תווית אישור בדיקה שנתית תקפה לפי טופס ד-6", check2_9) { check2_9 = it }
                }

                FormCard("3. בדיקת אטימות ולחץ", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("3.1", "בדיקת אטימות המערכת ללחץ השימוש כוללת את הצינורות האלסטומריים (אחרי החלפתם), ונעשית כשברזי המכשירים סגורים. בודקים באמצעות מד לחץ בעל טווח מדידה שאינו גדול מפי 3 מהלחץ הנמדד. בודקים בלחץ הגז השורר בקו. מחזיקים את הלחץ במשך 15 דקות ומוודאים שאין ירידת לחץ.", check3_1) { check3_1 = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = testPressure, onValueChange = { testPressure = it; saveToDatabase() }, label = { Text("ציין לחץ הבדיקה במיליבר") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = borderColor)

                    ThreeStateRow("3.2", "בדיקת וסת הלחץ - תנאי הבדיקה: מקור לחץ פתוח, ברזי המכשירים סגורים ומד-הלחץ בקו מותקן אחרי מוצא הווסת. משך הבדיקה חמש דקות. ודא שהלחץ אינו גדול ב-30% מהלחץ הנומינלי.", check3_2) { check3_2 = it }
                }

                FormCard("4. סיכום מבצע הבדיקה", cardBg, borderColor, primaryColor) {
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
                            OutlinedTextField(value = failedReasonsMap[sectionTitle] ?: "", onValueChange = { failedReasonsMap[sectionTitle] = it; saveToDatabase() }, label = { Text("פרט מדוע סעיף $sectionTitle אינו תקין") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = errorFieldColors, minLines = 2)
                        }
                    }
                }

                FormCard("תמונות ומסמכים מצורפים", cardBg, borderColor, primaryColor) {
                    Text("ניתן להוסיף צילומים של המתקן. הם יצורפו בסוף הדוח.", color = textGray, fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val secondaryButtonBg = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
                        val secondaryButtonText = if (isDark) Color.White else Color.Black
                        Button(onClick = { launchExtraCamera() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = secondaryButtonBg)) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp), tint = secondaryButtonText); Spacer(modifier = Modifier.width(4.dp)); Text("צלם במצלמה", color = secondaryButtonText)
                        }
                        Button(onClick = { extraGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = secondaryButtonBg)) {
                            Icon(Icons.Default.Collections, null, modifier = Modifier.size(16.dp), tint = secondaryButtonText); Spacer(modifier = Modifier.width(4.dp)); Text("בחר מהגלריה", color = secondaryButtonText)
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

                FormCard("אישור לקוח וחתימות", cardBg, borderColor, primaryColor) {
                    Text("הבהרה: מכשירי צריכת הגז אינם נבדקים ואינם נכללים בטופס בדיקה זה.", color = errorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = technicianName, onValueChange = { technicianName = it; saveToDatabase() }, label = { Text("שם מבצע הבדיקה") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("אני מאשר שנערכה בדיקה ונמסר לי עותק של טופס הבדיקה", color = textWhite, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("חתימת הלקוח:", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    TechnicianSignatureTouchPad(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp), initialSignatureUri = if (clientSignatureUri.isNotEmpty()) clientSignatureUri else null, onSignatureSaved = { uri -> clientSignatureUri = uri; saveToDatabase() })

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = clientNameConfirm,
                        onValueChange = { clientNameConfirm = it ; saveToDatabase() },
                        label = { Text("שם החותם / ת.ז") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                }

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val error = validateForm()
                                if (error != null) {
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                } else {
                                    saveToDatabase()
                                    viewModel.previewPdfD3(context, buildCurrentForm())
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(10.dp)
                        ) { Text("תצוגה מקדימה", fontWeight = FontWeight.Bold, fontSize = 13.sp) }

                        Button(
                            onClick = {
                                saveToDatabase()
                                Toast.makeText(context, "נשמר כטיוטה", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = successGreen), shape = RoundedCornerShape(10.dp)
                        ) { Text("שמור כטיוטה", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (initialForm != null) viewModel.deleteFormD3(initialForm)
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isDark) Color(0xFF333333) else Color(0xFFE0E0E0)), shape = RoundedCornerShape(10.dp)
                        ) { Text("מחק טופס", fontWeight = FontWeight.Bold, color = errorRed, fontSize = 13.sp) }

                        Button(
                            onClick = {
                                val error = validateForm()
                                if (error != null) {
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                } else {
                                    saveToDatabase()
                                    viewModel.sharePdfD3(context, buildCurrentForm()) { onNavigateBack() }
                                }
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