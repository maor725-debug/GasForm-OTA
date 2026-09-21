package com.example.myapplication158.UserInterface.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import com.example.myapplication158.UserInterface.components.TechnicianSignatureTouchPad
import com.example.myapplication158.UserInterface.components.FormCard
import com.example.myapplication158.UserInterface.components.CheckboxWithLabel
import com.example.myapplication158.data.GasFormD2
import com.example.myapplication158.util.SettingsManager
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodicFormEditScreenD2(
    viewModel: GasFormViewModel,
    form: Any? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

    val initialForm = form as? GasFormD2

    var sequentialNumber by remember { mutableStateOf(if (initialForm != null && initialForm.sequentialNumber > 0) initialForm.sequentialNumber else settingsManager.currentFormNumber) }

    val isDark = settingsManager.isDarkMode
    val primaryColor = Color(0xFF2196F3) // כחול לד-2
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

    var businessName by remember { mutableStateOf(initialForm?.businessName ?: "") }
    var city by remember { mutableStateOf(initialForm?.city ?: "") }
    var street by remember { mutableStateOf(initialForm?.street ?: "") }
    var building by remember { mutableStateOf(initialForm?.building ?: "") }
    var clientName by remember { mutableStateOf(initialForm?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(initialForm?.clientPhone ?: "") }
    var isUnaddressedSite by remember { mutableStateOf(initialForm?.isUnaddressedSite ?: false) }
    var gpsCoordinates by remember { mutableStateOf(initialForm?.gpsCoordinates ?: "") }

    var capacityPerTank by remember { mutableStateOf(initialForm?.capacityPerTank ?: "") }
    var totalCapacity by remember { mutableStateOf(initialForm?.totalCapacity ?: "") }
    var manufactureOrTestYear by remember { mutableStateOf(initialForm?.manufactureOrTestYear ?: "") }
    var tankType by remember { mutableStateOf(initialForm?.tankType ?: "על-קרקעי") }
    var usageType by remember { mutableStateOf(initialForm?.usageType ?: "מגורים") }
    var manifoldNumber by remember { mutableStateOf(initialForm?.manifoldNumber ?: "") }
    var suppliesToBuildings by remember { mutableStateOf(initialForm?.suppliesToBuildings ?: "") }

    var checkSiteSignage by remember { mutableStateOf(initialForm?.checkSiteSignage ?: "") }
    var checkSiteClean by remember { mutableStateOf(initialForm?.checkSiteClean ?: "") }
    var checkTankPlate by remember { mutableStateOf(initialForm?.checkTankPlate ?: "") }
    var checkTankCover by remember { mutableStateOf(initialForm?.checkTankCover ?: "") }
    var checkTankFittings by remember { mutableStateOf(initialForm?.checkTankFittings ?: "") }
    var checkSafeAccess by remember { mutableStateOf(initialForm?.checkSafeAccess ?: "") }
    var checkFittingsHeight by remember { mutableStateOf(initialForm?.checkFittingsHeight ?: "") }
    var checkSafetyDistances by remember { mutableStateOf(initialForm?.checkSafetyDistances ?: "") }
    var checkElecDistances by remember { mutableStateOf(initialForm?.checkElecDistances ?: "") }
    var checkFillPipe by remember { mutableStateOf(initialForm?.checkFillPipe ?: "") }

    var checkEarthquakeValve by remember { mutableStateOf(initialForm?.checkEarthquakeValve ?: "") }
    var checkValveLevel by remember { mutableStateOf(initialForm?.checkValveLevel ?: "") }
    var checkMainValve by remember { mutableStateOf(initialForm?.checkMainValve ?: "") }
    var checkDischargeValve by remember { mutableStateOf(initialForm?.checkDischargeValve ?: "") }
    var checkPressure1_4 by remember { mutableStateOf(initialForm?.checkPressure1_4 ?: "") }
    var checkPipingSecured by remember { mutableStateOf(initialForm?.checkPipingSecured ?: "") }
    var checkOutletsPlugged by remember { mutableStateOf(initialForm?.checkOutletsPlugged ?: "") }

    val failedReasonsMap = remember { mutableStateMapOf<String, String>() }

    var isLeakFoundPrimary by remember { mutableStateOf(initialForm?.isLeakFoundPrimary ?: false) }
    var leakLocationDetails by remember { mutableStateOf(initialForm?.leakLocationDetails ?: "") }
    var intermediatePressureValue by remember { mutableStateOf(initialForm?.intermediatePressureValue ?: "") }
    var isIntermediatePressureKept by remember { mutableStateOf(initialForm?.isIntermediatePressureKept ?: true) }
    var finalStatus by remember { mutableStateOf(initialForm?.finalStatus ?: "OK") }
    var defectsFixByDate by remember { mutableStateOf(initialForm?.defectsFixByDate ?: "") }
    var executionRemarks by remember { mutableStateOf(initialForm?.executionRemarks ?: "") }

    var technicianName by remember { mutableStateOf(initialForm?.technicianName?.takeIf { it.isNotBlank() } ?: settingsManager.defaultTechnicianName) }
    var customerId by remember { mutableStateOf(initialForm?.clientNameConfirm ?: "") }
    var clientSignatureUri by remember { mutableStateOf(initialForm?.clientSignatureUri ?: "") }
    var selectedExtraUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var showCriticalWarningDialog by remember { mutableStateOf(false) }

    fun buildCurrentForm(): GasFormD2 {
        val jsonReasons = JSONObject(failedReasonsMap.toMap()).toString()
        return GasFormD2(
            id = initialForm?.id ?: 0, sequentialNumber = sequentialNumber, date = currentDate,
            businessName = businessName, city = city, street = street, building = building,
            clientName = clientName, clientPhone = clientPhone, isUnaddressedSite = isUnaddressedSite, gpsCoordinates = gpsCoordinates,
            capacityPerTank = capacityPerTank, totalCapacity = totalCapacity, manufactureOrTestYear = manufactureOrTestYear,
            tankType = tankType, usageType = usageType, manifoldNumber = manifoldNumber, suppliesToBuildings = suppliesToBuildings,
            checkSiteSignage = checkSiteSignage, checkSiteClean = checkSiteClean, checkTankPlate = checkTankPlate,
            checkTankCover = checkTankCover, checkTankFittings = checkTankFittings, checkSafeAccess = checkSafeAccess,
            checkFittingsHeight = checkFittingsHeight, checkSafetyDistances = checkSafetyDistances, checkElecDistances = checkElecDistances,
            checkFillPipe = checkFillPipe, checkEarthquakeValve = checkEarthquakeValve, checkValveLevel = checkValveLevel,
            checkMainValve = checkMainValve, checkDischargeValve = checkDischargeValve, checkPressure1_4 = checkPressure1_4,
            checkPipingSecured = checkPipingSecured, checkOutletsPlugged = checkOutletsPlugged,
            isLeakFoundPrimary = isLeakFoundPrimary, leakLocationDetails = leakLocationDetails,
            intermediatePressureValue = intermediatePressureValue, isIntermediatePressureKept = isIntermediatePressureKept,
            finalStatus = finalStatus, defectsFixByDate = defectsFixByDate, executionRemarks = executionRemarks,
            technicianName = technicianName, clientNameConfirm = customerId, clientSignatureUri = clientSignatureUri,
            extraImagesUris = selectedExtraUris.joinToString(",") { it.toString() }, failedReasonsJson = jsonReasons
        )
    }

    val saveToDatabase = { viewModel.autoSaveFormD2(buildCurrentForm()) }

    fun triggerCriticalFailure() {
        finalStatus = "DISCONNECTED"
        showCriticalWarningDialog = true
        saveToDatabase()
    }

    fun copyGalleryUriToInternal(sourceUri: Uri, prefix: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val file = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream -> inputStream.copyTo(outputStream) }
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) { null }
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
    fun ThreeStateRow(title: String, currentState: String, isCritical: Boolean = false, onStateChange: (String) -> Unit) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(title, color = textWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(onClick = {
                    onStateChange(if(currentState == "PASS") "" else "PASS")
                    failedReasonsMap.remove(title)
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = if(currentState == "PASS") successGreen else cardBg, border = BorderStroke(1.dp, if(currentState == "PASS") successGreen else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("מתאים ✓", color = if(currentState == "PASS") Color.White else successGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(onClick = {
                    val newState = if(currentState == "FAIL") "" else "FAIL"
                    onStateChange(newState)
                    if (newState == "FAIL") {
                        failedReasonsMap[title] = failedReasonsMap[title] ?: ""
                        if (isCritical) triggerCriticalFailure()
                    } else failedReasonsMap.remove(title)
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = if(currentState == "FAIL") errorRed else cardBg, border = BorderStroke(1.dp, if(currentState == "FAIL") errorRed else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("לא מתאים ✗", color = if(currentState == "FAIL") Color.White else errorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(onClick = {
                    onStateChange(if(currentState == "NA") "" else "NA")
                    failedReasonsMap.remove(title)
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
                text = { Text("יש להפסיק הספקת גז למתקן!\n\nסימנת 'לא מתאים' בסעיף קריטי. הסטטוס בסוף הדוח עודכן אוטומטית למצב של ניתוק.", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                confirmButton = { Button(onClick = { showCriticalWarningDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = errorRed)) { Text("הבנתי, המערכת נותקה") } }
            )
        }

        Scaffold(
            containerColor = bgScreenColor,
            topBar = {
                Surface(color = headerBg, shadowElevation = 4.dp) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onNavigateBack() }) { Icon(Icons.Default.ArrowForward, "חזור", tint = textWhite) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("דוח בדיקה תקופתית: מאגר גפ\"מ (מכלים נייחים)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(modifier = Modifier.height(4.dp))

                FormCard("פרטי האתר והמערכת", cardBg, borderColor, primaryColor) {
                    OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("שם העסק/הבניין") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("איש קשר") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = clientPhone, onValueChange = { clientPhone = it }, label = { Text("טלפון נייד") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("יישוב") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("רחוב/בית") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                }

                FormCard("נתוני המאגר", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = capacityPerTank, onValueChange = { capacityPerTank = it }, label = { Text("קיבול מכל (ק\"ג)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors)
                        OutlinedTextField(value = totalCapacity, onValueChange = { totalCapacity = it }, label = { Text("סה\"כ קיבול (ק\"ג)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = manufactureOrTestYear, onValueChange = { manufactureOrTestYear = it }, label = { Text("שנת ייצור/בדיקה") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = manifoldNumber, onValueChange = { manifoldNumber = it }, label = { Text("מס' מרכזייה") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("סוג המכלים:", color = textWhite, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = tankType == "על-קרקעי", onClick = { tankType = "על-קרקעי" }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("על-קרקעי", color = textWhite)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = tankType == "תת-קרקעי", onClick = { tankType = "תת-קרקעי" }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("תת-קרקעי", color = textWhite)
                    }
                    Text("משמש ל:", color = textWhite, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = usageType == "מגורים", onClick = { usageType = "מגורים" }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("מגורים", color = textWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = usageType == "מסחרי", onClick = { usageType = "מסחרי" }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("מסחרי", color = textWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = usageType == "אחר", onClick = { usageType = "אחר" }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                        Text("אחר", color = textWhite)
                    }
                }

                FormCard("1. אתר ההתקנה", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("1.1 יש שילוט בטיחות ומחסום יציב בפני רכבים", checkSiteSignage) { checkSiteSignage = it }
                    ThreeStateRow("1.2 אתר ההתקנה תקין (שלמות כיסוי וניקיון)", checkSiteClean) { checkSiteClean = it }
                }

                FormCard("2. המכלים (בדיקה חזותית)", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("2.1 למכל יש לוחית זיהוי קריאה", checkTankPlate) { checkTankPlate = it }
                    ThreeStateRow("2.2 ברכת האביזרים/המכסה תקין", checkTankCover) { checkTankCover = it }
                    ThreeStateRow("2.3 האביזרים שלמים ונקיים", checkTankFittings) { checkTankFittings = it }
                    ThreeStateRow("2.4 יש סידור לגישה בטוחה לאביזרי המכל", checkSafeAccess) { checkSafeAccess = it }
                    ThreeStateRow("2.5 חיבורים ומוצאים גבוהים ממפלס המים", checkFittingsHeight) { checkFittingsHeight = it }
                    ThreeStateRow("2.6 נשמרים מרחקי הבטיחות (טבלה 2)", checkSafetyDistances, isCritical = true) { checkSafetyDistances = it }
                    ThreeStateRow("2.7 נשמרים מרחקי בטיחות לציוד חשמלי", checkElecDistances, isCritical = true) { checkElecDistances = it }
                    ThreeStateRow("2.8 פתח צינור מילוי מתאים לדרישות", checkFillPipe) { checkFillPipe = it }
                }

                FormCard("3. מערכת הצינורות המשותפת", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("3.1 שסתום לרעידת אדמה בקו לחץ ביניים", checkEarthquakeValve) { checkEarthquakeValve = it }
                    ThreeStateRow("3.2 השסתום מפולס והתקנתו תקינה", checkValveLevel) { checkValveLevel = it }
                    ThreeStateRow("3.3 ברז ניתוק ראשי נגיש ומשולט בכניסה לבניין", checkMainValve) { checkMainValve = it }
                    ThreeStateRow("3.4 שסתומי פריקה מחוברים לאוויר חוץ", checkDischargeValve) { checkDischargeValve = it }
                    ThreeStateRow("3.5 לחץ הגז בצנרת פנים אינו גדול מ-1.4 בר", checkPressure1_4) { checkPressure1_4 = it }
                    ThreeStateRow("3.6 הצנרת ומרכיביה מקובעים", checkPipingSecured) { checkPipingSecured = it }
                    ThreeStateRow("3.7 כל מוצא שאינו בשימוש קבוע סגור בפקק/ברז תקין", checkOutletsPlugged, isCritical = true) { checkOutletsPlugged = it }
                }

                FormCard("4. בדיקת אטימות ולחצים", cardBg, borderColor, primaryColor) {
                    Text("4.1 אטימות לחץ ראשוני (בדיקת נוזל בלחץ מכל):", fontWeight = FontWeight.Bold, color = textWhite, fontSize = 13.sp)
                    CheckboxWithLabel("נמצאה דליפה (קריטי!)", isLeakFoundPrimary, {
                        isLeakFoundPrimary = it
                        if (it) triggerCriticalFailure()
                    }, CheckboxDefaults.colors(checkedColor = errorRed), textWhite, Modifier.fillMaxWidth())
                    AnimatedVisibility(visible = isLeakFoundPrimary) {
                        OutlinedTextField(value = leakLocationDetails, onValueChange = { leakLocationDetails = it }, label = { Text("ציין את מקום הדליפה") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = errorFieldColors)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = borderColor)
                    Text("4.2 אטימות מערכת ללחץ ביניים (15 דק'):", fontWeight = FontWeight.Bold, color = textWhite, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = intermediatePressureValue, onValueChange = { intermediatePressureValue = it }, label = { Text("לחץ הבדיקה (mbar/bar)") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("האם הלחץ נשמר?", color = textWhite, modifier = Modifier.weight(1f))
                        RadioButton(selected = isIntermediatePressureKept, onClick = { isIntermediatePressureKept = true }, colors = RadioButtonDefaults.colors(selectedColor = successGreen))
                        Text("כן", color = textWhite)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = !isIntermediatePressureKept, onClick = {
                            isIntermediatePressureKept = false
                            triggerCriticalFailure()
                        }, colors = RadioButtonDefaults.colors(selectedColor = errorRed))
                        Text("לא", color = textWhite)
                    }
                }

                FormCard("5. סיכום מבצע הבדיקה (סטטוס)", cardBg, borderColor, primaryColor) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { finalStatus = "OK" }) {
                        RadioButton(selected = finalStatus == "OK", onClick = { finalStatus = "OK" }, colors = RadioButtonDefaults.colors(selectedColor = successGreen))
                        Text("המתקן נמצא תקין בהתאם לדרישות", color = successGreen, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { finalStatus = "DEFECTS" }) {
                        RadioButton(selected = finalStatus == "DEFECTS", onClick = { finalStatus = "DEFECTS" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF9800)))
                        Text("נמצאו ליקויים ויש לתקנם עד תאריך:", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                    AnimatedVisibility(visible = finalStatus == "DEFECTS") {
                        OutlinedTextField(value = defectsFixByDate, onValueChange = { defectsFixByDate = it }, label = { Text("תאריך יעד לתיקון (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth().padding(start = 40.dp, bottom = 8.dp), colors = textFieldColors)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { finalStatus = "DISCONNECTED" }) {
                        RadioButton(selected = finalStatus == "DISCONNECTED", onClick = { finalStatus = "DISCONNECTED" }, colors = RadioButtonDefaults.colors(selectedColor = errorRed))
                        Text("הספקת הגז נותקה עקב ליקויים חמורים", color = errorRed, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = executionRemarks, onValueChange = { executionRemarks = it }, label = { Text("הערות נוספות וסיכום הליקויים") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = textFieldColors)
                }

                AnimatedVisibility(visible = failedReasonsMap.isNotEmpty()) {
                    FormCard("פירוט ליקויים שנמצאו בבדיקה (חובה למלא)", cardBg, errorRed, errorRed) {
                        failedReasonsMap.keys.forEach { sectionTitle ->
                            OutlinedTextField(value = failedReasonsMap[sectionTitle] ?: "", onValueChange = { failedReasonsMap[sectionTitle] = it }, label = { Text("פרט מדוע '$sectionTitle' אינו תקין") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = errorFieldColors, minLines = 2)
                        }
                    }
                }

                FormCard("5. תמונות ומסמכים מצורפים (נספחים)", cardBg, borderColor, primaryColor) {
                    Text("ניתן להוסיף צילומים של המאגר, תקלות או מסמכים נלווים. הם יצורפו בסוף הדוח.", color = textGray, fontSize = 12.sp, lineHeight = 16.sp)
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
                    OutlinedTextField(value = technicianName, onValueChange = { technicianName = it; saveToDatabase() }, label = { Text("שם מבצע הבדיקה") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("חתימת הלקוח (נציג ועד הבית / אחראי):", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    TechnicianSignatureTouchPad(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp), initialSignatureUri = if (clientSignatureUri.isNotEmpty()) clientSignatureUri else null, onSignatureSaved = { uri -> clientSignatureUri = uri; saveToDatabase() })
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = customerId, onValueChange = { customerId = it; saveToDatabase() }, label = { Text("שם החותם / ת.ז") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                }

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { saveToDatabase(); viewModel.previewPdfD2(context, buildCurrentForm()) },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(10.dp)
                        ) { Text("תצוגה מקדימה", fontWeight = FontWeight.Bold, fontSize = 13.sp) }

                        Button(
                            onClick = {
                                saveToDatabase()
                                Toast.makeText(context, "נשמר כטיוטה בזיכרון", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = successGreen), shape = RoundedCornerShape(10.dp)
                        ) { Text("שמור כטיוטה", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigateBack() },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isDark) Color(0xFF333333) else Color(0xFFE0E0E0)), shape = RoundedCornerShape(10.dp)
                        ) { Text("בטל דוח", fontWeight = FontWeight.Bold, color = errorRed, fontSize = 13.sp) }

                        Button(
                            onClick = {
                                if (clientName.isBlank() || clientPhone.isBlank()) {
                                    Toast.makeText(context, "שגיאה: חובה למלא שם איש קשר וטלפון.", Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.sharePdfD2(context, buildCurrentForm()) { onNavigateBack() }
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