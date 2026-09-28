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

    var sequentialNumber by remember { mutableIntStateOf(if (initialForm != null && initialForm.sequentialNumber > 0) initialForm.sequentialNumber else settingsManager.currentFormNumber) }

    val isDark = settingsManager.isDarkMode
    val primaryColor = Color(0xFFFF9800)
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
    var facilityType by remember { mutableStateOf(initialForm?.facilityType ?: "פרטי") }

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
    var check1_4 by remember { mutableStateOf(initialForm?.check1_4 ?: "") }
    var check1_5 by remember { mutableStateOf(initialForm?.check1_5 ?: "") }
    var check1_6 by remember { mutableStateOf(initialForm?.check1_6 ?: "") }
    var check1_7 by remember { mutableStateOf(initialForm?.check1_7 ?: "") }
    var check1_8 by remember { mutableStateOf(initialForm?.check1_8 ?: "") }
    var check1_9 by remember { mutableStateOf(initialForm?.check1_9 ?: "") }

    var devicesList by remember { mutableStateOf(initialForm?.devicesList ?: "") }
    var check2_1 by remember { mutableStateOf(initialForm?.check2_1 ?: "") }
    var check2_2 by remember { mutableStateOf(initialForm?.check2_2 ?: "") }
    var check2_3 by remember { mutableStateOf(initialForm?.check2_3 ?: "") }
    var check2_4 by remember { mutableStateOf(initialForm?.check2_4 ?: "") }
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
        return GasFormD3(
            id = initialForm?.id ?: 0, sequentialNumber = sequentialNumber, date = currentDate,
            consumerNumber = consumerNumber, consumerMeterNumber = consumerMeterNumber, meterManufactureYear = meterManufactureYear,
            clientName = clientName, city = city, street = street, gasProvider = gasProvider, facilityType = facilityType,
            businessId = businessId, businessName = businessName, businessType = businessType, fireDeptFileNumber = fireDeptFileNumber,
            poBox = poBox, zip = zip, building = building, mainContactName = mainContactName, contactRole = contactRole,
            clientPhone = clientPhone, email = email, check1_1 = check1_1, check1_2 = check1_2, check1_3 = check1_3, check1_4 = check1_4,
            check1_5 = check1_5, check1_6 = check1_6, check1_7 = check1_7, check1_8 = check1_8, check1_9 = check1_9,
            devicesList = devicesList, check2_1 = check2_1, check2_2 = check2_2, check2_3 = check2_3, check2_4 = check2_4,
            check2_5 = check2_5, check2_6_1 = check2_6_1, check2_6_2 = check2_6_2, check2_6_3 = check2_6_3, check2_6_4 = check2_6_4,
            check2_6_5 = check2_6_5, check2_7_1 = check2_7_1, check2_7_2 = check2_7_2, check2_7_3 = check2_7_3, check2_8_1 = check2_8_1,
            check2_8_2 = check2_8_2, check2_9 = check2_9, check3_1 = check3_1, testPressure = testPressure, check3_2 = check3_2,
            isFacilityValid = isFacilityValid, requiresFixes = requiresFixes, fixByDate = fixByDate, isDisconnected = isDisconnected,
            disconnectReason = disconnectReason, additionalNotes = additionalNotes, failedReasonsJson = jsonReasons, extraImagesUris = selectedExtraUris.joinToString(",") { it.toString() },
            technicianSignatureUri = "", clientSignatureUri = clientSignatureUri
        )
    }

    val saveToDatabase = {
        viewModel.autoSaveFormD3(buildCurrentForm())
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

                FormCard("1. קווי צינורות", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("1.1 ליחידת הצריכה (דירה או בית עסק) קיים ברז ניתוק נגיש", check1_1) { check1_1 = it }
                    ThreeStateRow("1.2 המונה מקובע", check1_2) { check1_2 = it }
                    ThreeStateRow("1.3 פרק הזמן ממועד ייצור המונה אינו גדול מ-18 שנה", check1_3) { check1_3 = it }
                    ThreeStateRow("1.4 סך הנפח המצטבר אינו גדול מ-2000 ק\"מ...", check1_4) { check1_4 = it }
                    ThreeStateRow("1.5 בשסתומי פריקה (בתוך בניין)... מוצא השסתום מחובר אל אוויר החוץ", check1_5) { check1_5 = it }
                    ThreeStateRow("1.6 בווסתים ללא שסתום פריקה יש אמצעים המגבילים את הלחץ", check1_6, isCritical = true) { check1_6 = it }
                    ThreeStateRow("1.7 יש ברז ניתוק בקרבת כל מכשיר צורך גפ\"מ", check1_7) { check1_7 = it }
                    ThreeStateRow("1.8 הצנרת ומרכיביה מקובעים", check1_8) { check1_8 = it }
                    ThreeStateRow("1.9 (⊕) כל מוצא של מתקן, שאינו מחובר למכשיר, סגור בפקק...", check1_9, isCritical = true) { check1_9 = it }
                }

                FormCard("2. חיבור המכשירים", cardBg, borderColor, primaryColor) {
                    OutlinedTextField(value = devicesList, onValueChange = { devicesList = it; saveToDatabase() }, label = { Text("2.1 פרט את המכשירים המחוברים למִתקן") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, minLines = 2)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("שלמות המכשירים בבחינה חזותית:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("2.2 מכשירים קבועים מחוברים בצינור קשיח", check2_2, isSubItem = true) { check2_2 = it }
                    ThreeStateRow("2.3 צינור אלסטומרי... הוחלף בעקבות הבחינה החזותית", check2_3, isSubItem = true) { check2_3 = it }
                    ThreeStateRow("2.4 קצוות הזרנוק המחוברים לניפלים מחוזקים בחבקים", check2_4, isSubItem = true) { check2_4 = it }
                    ThreeStateRow("2.5 אורך הצינורות האלסטומריים אינו גדול מ-3 מ'", check2_5, isSubItem = true) { check2_5 = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("מכשירים צורכי גפ\"מ עם ארובה אטמוספרית:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("2.6.1 יש תווית אישור בדיקה שנתית תקפה (ד-5)", check2_6_1, isSubItem = true) { check2_6_1 = it }
                    ThreeStateRow("2.6.2 מכשיר חימום מים להסקה אינו מותקן בחדרי שינה/רחצה", check2_6_2, isSubItem = true) { check2_6_2 = it }
                    ThreeStateRow("2.6.3 עברו פחות מ-3 שנים מתיקון 1 לת\"י 158...", check2_6_3, isSubItem = true) { check2_6_3 = it }
                    ThreeStateRow("2.6.4 מכשיר חימום לצריכה (>0.5 ק\"ג/ש) יש תווית ד-5", check2_6_4, isSubItem = true) { check2_6_4 = it }
                    ThreeStateRow("2.6.5 מכשיר ללא ארובה אינו באמבטיה/שירותים/שינה", check2_6_5, isSubItem = true) { check2_6_5 = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("תקינות ארובות למכשירים צורכי גפ\"מ:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("2.7.1 הארובה שלמה ומחוזקת באופן המונע שינוי", check2_7_1, isSubItem = true) { check2_7_1 = it }
                    ThreeStateRow("2.7.2 מוצא ארובה אטמוספרית מרוחק 0.5 מ' מכל פתח", check2_7_2, isSubItem = true) { check2_7_2 = it }
                    ThreeStateRow("2.7.3 מוצא ארובה כפולה מרוחק 0.4 מ' מכל פתח", check2_7_3, isSubItem = true) { check2_7_3 = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("מכשירים ציבורי / מסחרי / חקלאי / תעשייתי:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    ThreeStateRow("2.8.1 מכשירים לחימום חלל ציבורי מצוידים בהתקן סגירת גז", check2_8_1, isSubItem = true) { check2_8_1 = it }
                    ThreeStateRow("2.8.2 יש פתח אוורור קבוע במטבחים של מבני ציבור", check2_8_2, isSubItem = true) { check2_8_2 = it }
                    ThreeStateRow("2.9 למכשירים במקום נמוך יש תווית אישור בדיקה ד-6", check2_9) { check2_9 = it }
                }

                FormCard("3. בדיקת אטימות", cardBg, borderColor, primaryColor) {
                    Text("3.1 בדיקת אטימות המערכת ללחץ שימוש", fontWeight = FontWeight.Bold, color = textWhite, fontSize = 13.sp)
                    Text("הבדיקה בלחץ השורר בקו למשך 15 דק'.", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = testPressure, onValueChange = { testPressure = it; saveToDatabase() }, label = { Text("לחץ בדיקה במיליבר") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    ThreeStateRow("הלחץ נשמר ללא ירידה למשך 15 דקות?", check3_1, isCritical = true) { check3_1 = it }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = borderColor)

                    Text("3.2 בדיקת וסת הלחץ", fontWeight = FontWeight.Bold, color = textWhite, fontSize = 13.sp)
                    Text("משך 5 דק'. ודא שהלחץ אינו גדול ב-30% מהנומינלי.", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    ThreeStateRow("הלחץ לא חרג מהמותר", check3_2, isCritical = true) { check3_2 = it }
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
                            OutlinedTextField(value = failedReasonsMap[sectionTitle] ?: "", onValueChange = { failedReasonsMap[sectionTitle] = it; saveToDatabase() }, label = { Text("פרט מדוע '$sectionTitle' אינו תקין") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = errorFieldColors, minLines = 2)
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
                        value = mainContactName,
                        onValueChange = { mainContactName = it; saveToDatabase() },
                        label = { Text("שם החותם / ת.ז") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                }

                // 4 כפתורי הפעולה המבצעיים כמו בטופס ד-1
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.previewPdfD3(context, buildCurrentForm()) },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(10.dp)
                        ) { Text("תצוגה מקדימה", fontWeight = FontWeight.Bold, fontSize = 13.sp) }

                        Button(
                            onClick = { viewModel.saveCurrentFormD3(buildCurrentForm()) { Toast.makeText(context, "נשמר כטיוטה", Toast.LENGTH_SHORT).show(); onNavigateBack() } },
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
                                viewModel.sharePdfD3(context, buildCurrentForm()) { onNavigateBack() }
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