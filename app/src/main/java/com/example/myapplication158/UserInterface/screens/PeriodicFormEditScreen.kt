package com.example.myapplication158.UserInterface.screens

import android.Manifest
import android.content.Context
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.UserInterface.components.TechnicianSignatureTouchPad
import com.example.myapplication158.UserInterface.components.FormCard
import com.example.myapplication158.UserInterface.components.CheckboxWithLabel
import com.example.myapplication158.data.PeriodicGasForm
import com.example.myapplication158.util.SettingsManager
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodicFormEditScreen(
    viewModel: GasFormViewModel,
    form: Any? = null,
    onNavigateBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settingsManager = remember { SettingsManager(context) }
    val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

    val initialForm = form as? PeriodicGasForm

    var sequentialNumber by remember { mutableStateOf(if (initialForm != null && initialForm.sequentialNumber > 0) initialForm.sequentialNumber else settingsManager.currentFormNumber) }

    val isDark = settingsManager.isDarkMode
    val primaryColor = MaterialTheme.colorScheme.primary
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

    var currentFormId by remember { mutableStateOf(initialForm?.id ?: 0) }
    var date by remember { mutableStateOf(initialForm?.date?.takeIf { it.isNotBlank() } ?: currentDate) }

    var businessName by remember { mutableStateOf(initialForm?.businessName ?: "") }
    var businessType by remember { mutableStateOf(initialForm?.businessType ?: "") }
    var businessId by remember { mutableStateOf(initialForm?.businessId ?: "") }
    var fireDeptFileNumber by remember { mutableStateOf(initialForm?.fireDeptFileNumber ?: "") }
    var isUnaddressedSite by remember { mutableStateOf(initialForm?.isUnaddressedSite ?: false) }
    var gpsCoordinates by remember { mutableStateOf(initialForm?.gpsCoordinates ?: "") }
    var sitePhotoUri by remember { mutableStateOf(initialForm?.sitePhotoUri ?: "") }
    var city by remember { mutableStateOf(initialForm?.city ?: "") }
    var street by remember { mutableStateOf(initialForm?.street ?: "") }
    var building by remember { mutableStateOf(initialForm?.building ?: "") }

    var clientName by remember { mutableStateOf(initialForm?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(initialForm?.clientPhone ?: "") }
    var contactRole by remember { mutableStateOf(initialForm?.contactRole ?: "") }
    var gasProvider by remember { mutableStateOf(initialForm?.gasProvider ?: "") }
    var consumersCount by remember { mutableStateOf(initialForm?.consumersCount ?: "") }
    var cylindersCount by remember { mutableStateOf(initialForm?.cylindersCount ?: "") }
    var manifoldNumber by remember { mutableStateOf(initialForm?.manifoldNumber ?: "") }

    var checkLocationOpen by remember { mutableStateOf(initialForm?.checkLocationOpen ?: "") }
    var checkSafetyDistances07Heat by remember { mutableStateOf(initialForm?.checkSafetyDistances07Heat ?: "") }
    var checkSafetyDistances17Fire by remember { mutableStateOf(initialForm?.checkSafetyDistances17Fire ?: "") }
    var checkSafetyDistances05Pits by remember { mutableStateOf(initialForm?.checkSafetyDistances05Pits ?: "") }
    var checkSafetyDistances3Drainage by remember { mutableStateOf(initialForm?.checkSafetyDistances3Drainage ?: "") }
    var checkSafetyDistances12Building by remember { mutableStateOf(initialForm?.checkSafetyDistances12Building ?: "") }
    var checkSafetyDistances3LowLevel by remember { mutableStateOf(initialForm?.checkSafetyDistances3LowLevel ?: "") }

    var checkRegulatorSecured by remember { mutableStateOf(initialForm?.checkRegulatorSecured ?: "") }
    var checkWarningSigns by remember { mutableStateOf(initialForm?.checkWarningSigns ?: "") }
    var checkWaterSprinklers by remember { mutableStateOf(initialForm?.checkWaterSprinklers ?: "") }
    var checkGasRoomMax20 by remember { mutableStateOf(initialForm?.checkGasRoomMax20 ?: "") }
    var checkGasRoomLighting by remember { mutableStateOf(initialForm?.checkGasRoomLighting ?: "") }
    var checkGasRoomNoFlammables by remember { mutableStateOf(initialForm?.checkGasRoomNoFlammables ?: "") }
    var checkCageMax20 by remember { mutableStateOf(initialForm?.checkCageMax20 ?: "") }
    var checkCageVentilated by remember { mutableStateOf(initialForm?.checkCageVentilated ?: "") }
    var checkRampsSecured by remember { mutableStateOf(initialForm?.checkRampsSecured ?: "") }
    var checkEarthquakeValve by remember { mutableStateOf(initialForm?.checkEarthquakeValve ?: "") }
    var checkEarthquakeValveSecured by remember { mutableStateOf(initialForm?.checkEarthquakeValveSecured ?: "") }
    var checkMainValveAccessible by remember { mutableStateOf(initialForm?.checkMainValveAccessible ?: "") }
    var checkDischargeValves by remember { mutableStateOf(initialForm?.checkDischargeValves ?: "") }
    var checkPressureUpTo1_4 by remember { mutableStateOf(initialForm?.checkPressureUpTo1_4 ?: "") }
    var checkPipingSecured by remember { mutableStateOf(initialForm?.checkPipingSecured ?: "") }
    var checkUnusedOutletsPlugged by remember { mutableStateOf(initialForm?.checkUnusedOutletsPlugged ?: "") }

    val failedReasonsMap = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(initialForm?.failedReasonsJson) {
        if (!initialForm?.failedReasonsJson.isNullOrEmpty()) {
            try {
                val json = JSONObject(initialForm!!.failedReasonsJson)
                json.keys().forEach { key -> failedReasonsMap[key] = json.getString(key) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    var isLeakFoundPrimary by remember { mutableStateOf(initialForm?.isLeakFoundPrimary ?: false) }
    var leakLocationDetails by remember { mutableStateOf(initialForm?.leakLocationDetails ?: "") }
    var intermediatePressureValue by remember { mutableStateOf(initialForm?.intermediatePressureValue ?: "") }
    var isIntermediatePressureKept by remember { mutableStateOf(initialForm?.isIntermediatePressureKept ?: true) }
    var finalStatus by remember { mutableStateOf(initialForm?.finalStatus ?: "OK") }
    var defectsFixByDate by remember { mutableStateOf(initialForm?.defectsFixByDate ?: "") }
    var executionRemarks by remember { mutableStateOf(initialForm?.executionRemarks ?: "") }
    var technicianName by remember { mutableStateOf(initialForm?.technicianName?.takeIf { it.isNotBlank() } ?: settingsManager.defaultTechnicianName) }
    var technicianLicense by remember { mutableStateOf(initialForm?.technicianLicense ?: "") }
    var customerId by remember { mutableStateOf(initialForm?.clientNameConfirm ?: "") }
    var clientSignatureUri by remember { mutableStateOf(initialForm?.clientSignatureUri ?: "") }
    var selectedExtraUris by remember { mutableStateOf<List<Uri>>(initialForm?.extraImagesUris?.split(",")?.filter { it.isNotBlank() }?.map { Uri.parse(it) } ?: emptyList()) }
    var isFormSavedToTarget by remember { mutableStateOf(initialForm?.isSavedToTarget == true) }

    fun buildCurrentForm(): PeriodicGasForm {
        val jsonReasons = JSONObject(failedReasonsMap.toMap()).toString()
        return PeriodicGasForm(
            id = currentFormId, sequentialNumber = sequentialNumber, date = date,
            businessName = businessName, businessType = businessType, businessId = businessId, fireDeptFileNumber = fireDeptFileNumber,
            isUnaddressedSite = isUnaddressedSite, gpsCoordinates = gpsCoordinates, sitePhotoUri = sitePhotoUri,
            city = city, street = street, building = building, zipCode = "", poBox = "",
            clientName = clientName, contactRole = contactRole, clientPhone = clientPhone, contactEmail = "",
            gasProvider = gasProvider, consumersCount = consumersCount, cylindersCount = cylindersCount, manifoldNumber = manifoldNumber,

            checkLocationOpen = checkLocationOpen, checkSafetyDistances = "",
            checkSafetyDistances07Heat = checkSafetyDistances07Heat, checkSafetyDistances17Fire = checkSafetyDistances17Fire,
            checkSafetyDistances05Pits = checkSafetyDistances05Pits, checkSafetyDistances3Drainage = checkSafetyDistances3Drainage,
            checkSafetyDistances12Building = checkSafetyDistances12Building, checkSafetyDistances3LowLevel = checkSafetyDistances3LowLevel,
            checkRegulatorSecured = checkRegulatorSecured, checkWarningSigns = checkWarningSigns, checkWaterSprinklers = checkWaterSprinklers, checkGasRoomMax20 = checkGasRoomMax20,
            checkGasRoomLighting = checkGasRoomLighting, checkGasRoomNoFlammables = checkGasRoomNoFlammables, checkCageMax20 = checkCageMax20,
            checkCageVentilated = checkCageVentilated, checkRampsSecured = checkRampsSecured, checkEarthquakeValve = checkEarthquakeValve, checkEarthquakeValveSecured = checkEarthquakeValveSecured, checkMainValveAccessible = checkMainValveAccessible,
            checkDischargeValves = checkDischargeValves, checkPressureUpTo1_4 = checkPressureUpTo1_4, checkPipingSecured = checkPipingSecured, checkUnusedOutletsPlugged = checkUnusedOutletsPlugged,
            failedReasonsJson = jsonReasons, isLeakFoundPrimary = isLeakFoundPrimary, leakLocationDetails = leakLocationDetails,
            intermediatePressureValue = intermediatePressureValue, isIntermediatePressureKept = isIntermediatePressureKept,
            finalStatus = finalStatus, defectsFixByDate = defectsFixByDate, executionRemarks = executionRemarks,
            technicianName = technicianName, technicianLicense = technicianLicense, technicianSignatureUri = "",
            clientNameConfirm = customerId, clientSignatureUri = clientSignatureUri, extraImagesUris = selectedExtraUris.joinToString(",") { it.toString() },
            createdAt = initialForm?.createdAt ?: System.currentTimeMillis(), savedTargetLocation = if (isFormSavedToTarget) "Shared" else "מכשיר",
            isSavedToTarget = isFormSavedToTarget
        )
    }

    val saveToDatabase = { viewModel.autoSavePeriodicForm(buildCurrentForm()) { newId -> currentFormId = newId } }

    fun copyGalleryUriToInternal(sourceUri: Uri, prefix: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val file = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream -> inputStream.copyTo(outputStream) }
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchLocationAndDecode(context) { coords, address -> gpsCoordinates = if (address.isNotEmpty()) "$coords | משוער: $address" else coords; saveToDatabase() }
        } else { Toast.makeText(context, "חובה לאשר הרשאות מיקום כדי לדגום נ.צ", Toast.LENGTH_LONG).show() }
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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) saveToDatabase() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer); saveToDatabase() }
    }

    @Composable
    fun ThreeStateRow(title: String, currentState: String, onStateChange: (String) -> Unit, isSubItem: Boolean = false) {
        val rowModifier = if (isSubItem) {
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 6.dp, bottom = 6.dp)
                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFFAFAFA), RoundedCornerShape(10.dp))
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .padding(12.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
        }

        Column(modifier = rowModifier) {
            Text(title, color = textWhite, fontSize = 13.sp, fontWeight = if(isSubItem) FontWeight.Normal else FontWeight.Bold, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val passColor = if (currentState == "PASS") successGreen else cardBg
                val failColor = if (currentState == "FAIL") errorRed else cardBg
                val naColor = if (currentState == "NA") Color.Gray else cardBg

                Surface(onClick = {
                    val newState = if (currentState == "PASS") "" else "PASS"
                    onStateChange(newState)
                    failedReasonsMap.remove(title)
                    saveToDatabase()
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = passColor, border = BorderStroke(1.dp, if(currentState == "PASS") successGreen else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("מתאים ✓", color = if(currentState == "PASS") Color.White else successGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(onClick = {
                    val newState = if (currentState == "FAIL") "" else "FAIL"
                    onStateChange(newState)
                    if (newState == "FAIL") failedReasonsMap[title] = failedReasonsMap[title] ?: "" else failedReasonsMap.remove(title)
                    saveToDatabase()
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = failColor, border = BorderStroke(1.dp, if(currentState == "FAIL") errorRed else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("לא מתאים ✗", color = if(currentState == "FAIL") Color.White else errorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(onClick = {
                    val newState = if (currentState == "NA") "" else "NA"
                    onStateChange(newState)
                    failedReasonsMap.remove(title)
                    saveToDatabase()
                }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), color = naColor, border = BorderStroke(1.dp, if(currentState == "NA") Color.Gray else borderColor)) {
                    Box(contentAlignment = Alignment.Center) { Text("לא ישים ⚪", color = if(currentState == "NA") Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = bgScreenColor,
            topBar = {
                Surface(color = headerBg, shadowElevation = 4.dp) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { saveToDatabase(); onNavigateBack() }) { Icon(Icons.Default.ArrowForward, "חזור", tint = textWhite) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("דוח בדיקה תקופתית (ד-1) מס' $sequentialNumber", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(modifier = Modifier.height(4.dp))

                FormCard("פרטי העסק והמיקום", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = businessName, onValueChange = { businessName = it; saveToDatabase() }, label = { Text("שם העסק/הבניין") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = businessType, onValueChange = { businessType = it; saveToDatabase() }, label = { Text("מהות העסק") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = businessId, onValueChange = { businessId = it; saveToDatabase() }, label = { Text("ח.פ / ת.ז") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = fireDeptFileNumber, onValueChange = { fireDeptFileNumber = it; saveToDatabase() }, label = { Text("מס' תיק כבאות") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("כתובת האתר:", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    CheckboxWithLabel("אתר בבנייה / ללא כתובת", isUnaddressedSite, { isUnaddressedSite = it; saveToDatabase() }, CheckboxDefaults.colors(checkedColor = primaryColor), textWhite, Modifier.fillMaxWidth())

                    AnimatedVisibility(visible = !isUnaddressedSite) {
                        Column {
                            OutlinedTextField(value = city, onValueChange = { city = it; saveToDatabase() }, label = { Text("יישוב") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = street, onValueChange = { street = it; saveToDatabase() }, label = { Text("רחוב") }, modifier = Modifier.weight(2f), colors = textFieldColors)
                                OutlinedTextField(value = building, onValueChange = { building = it; saveToDatabase() }, label = { Text("מספר/בית") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                            }
                        }
                    }
                    AnimatedVisibility(visible = isUnaddressedSite) {
                        Column(modifier = Modifier.fillMaxWidth().background(if(isDark) Color(0xFF222222) else Color(0xFFF0F0F0), RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Text("נ.צ: ${if(gpsCoordinates.isEmpty()) "טרם נדגם" else gpsCoordinates}", color = textWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) { Text("דגום מיקום עכשיו") }
                        }
                    }
                }

                FormCard("איש קשר ופרטי המאגר", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = clientName, onValueChange = { clientName = it; saveToDatabase() }, label = { Text("שם איש קשר (חובה)") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = clientPhone, onValueChange = { clientPhone = it; saveToDatabase() }, label = { Text("טלפון נייד (חובה)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = gasProvider, onValueChange = { gasProvider = it; saveToDatabase() }, label = { Text("שם חברת הגז (ספק)") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = consumersCount, onValueChange = { consumersCount = it; saveToDatabase() }, label = { Text("מס' צרכנים") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors)
                        OutlinedTextField(value = cylindersCount, onValueChange = { cylindersCount = it; saveToDatabase() }, label = { Text("מס' מכלים") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors)
                        OutlinedTextField(value = manifoldNumber, onValueChange = { manifoldNumber = it; saveToDatabase() }, label = { Text("מס' מרכזייה") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                }

                FormCard("1. בחינה חזותית של המאגר", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("1.1.1 במקום פתוח ומאוורר. לא במפלס נמוך ולא למגורים", checkLocationOpen, { checkLocationOpen = it })

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(if(isDark) Color(0xFF263238) else Color(0xFFECEFF1), RoundedCornerShape(4.dp)).padding(8.dp)) {
                        Text("1.1.2 מרחקי בטיחות:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    }
                    ThreeStateRow("1.1.2.1 - 0.7 מ' ממקור חום וניצוצות (קריטי)", checkSafetyDistances07Heat, { checkSafetyDistances07Heat = it }, isSubItem = true)
                    ThreeStateRow("1.1.2.2 - 1.7 מ' מאש גלויה (קריטי)", checkSafetyDistances17Fire, { checkSafetyDistances17Fire = it }, isSubItem = true)
                    ThreeStateRow("1.1.2.3 - 0.5 מ' מבורות ומתאי בקרה הסגורים במכסה קבוע", checkSafetyDistances05Pits, { checkSafetyDistances05Pits = it }, isSubItem = true)
                    ThreeStateRow("1.1.2.4 - 3 מ' מבורות ופתחי ניקוז פתוחים", checkSafetyDistances3Drainage, { checkSafetyDistances3Drainage = it }, isSubItem = true)
                    ThreeStateRow("1.1.2.5 - 1.2 מ' מפתח בניין", checkSafetyDistances12Building, { checkSafetyDistances12Building = it }, isSubItem = true)
                    ThreeStateRow("1.1.2.6 - 3 מ' מפתחי מפלס נמוך", checkSafetyDistances3LowLevel, { checkSafetyDistances3LowLevel = it }, isSubItem = true)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = borderColor)
                    ThreeStateRow("1.2 הווסת והסעפת מקובעים כראוי", checkRegulatorSecured, { checkRegulatorSecured = it })
                    HorizontalDivider(color = borderColor)
                    ThreeStateRow("1.3 יש שילוט אזהרה (גז מתלקח, סמל דליקות, פרטי ספק)", checkWarningSigns, { checkWarningSigns = it })
                    HorizontalDivider(color = borderColor)
                    ThreeStateRow("1.4 אם יש מתקן מים, מובטחת התזה על כל המכלים", checkWaterSprinklers, { checkWaterSprinklers = it })

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(if(isDark) Color(0xFF263238) else Color(0xFFECEFF1), RoundedCornerShape(4.dp)).padding(8.dp)) {
                        Text("1.5 אם המאגר בחדר גז, בדוק גם:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    }
                    ThreeStateRow("1.5.1 בחדר יש עד 20 מכלים ק\"ג (1000 ק\"ג)", checkGasRoomMax20, { checkGasRoomMax20 = it }, isSubItem = true)
                    ThreeStateRow("1.5.2 גוף התאורה בתקרה והמפסק מחוץ לחדר", checkGasRoomLighting, { checkGasRoomLighting = it }, isSubItem = true)
                    ThreeStateRow("1.5.3 בחדר לא מוחזקים חומרים דליקים (קריטי)", checkGasRoomNoFlammables, { checkGasRoomNoFlammables = it }, isSubItem = true)

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(if(isDark) Color(0xFF263238) else Color(0xFFECEFF1), RoundedCornerShape(4.dp)).padding(8.dp)) {
                        Text("1.6 אם המאגר במכלאה, בדוק גם:", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp)
                    }
                    ThreeStateRow("1.6.1 במכלאה יש עד 20 מכלים", checkCageMax20, { checkCageMax20 = it }, isSubItem = true)
                    ThreeStateRow("1.6.2 המכלאה מגודרת ומאווררת", checkCageVentilated, { checkCageVentilated = it }, isSubItem = true)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = borderColor)
                    ThreeStateRow("1.7 המאספים (רמפות) יציבים ולכל אחד ברז ניתוק", checkRampsSecured, { checkRampsSecured = it })
                }

                FormCard("2. מערכת הצינורות המשותפת", cardBg, borderColor, primaryColor) {
                    ThreeStateRow("2.1 שסתום לרעידת אדמה בקו לחץ ביניים (אחרי פבר' 2012)", checkEarthquakeValve, { checkEarthquakeValve = it })
                    ThreeStateRow("2.2 השסתום מפולס והתקנתו תקינה", checkEarthquakeValveSecured, { checkEarthquakeValveSecured = it })
                    ThreeStateRow("2.3 ברז ניתוק ראשי נגיש ומשולט בכניסה לבניין", checkMainValveAccessible, { checkMainValveAccessible = it })
                    ThreeStateRow("2.4 שסתומי פריקה מחוברים לאוויר חוץ כחוק", checkDischargeValves, { checkDischargeValves = it })
                    ThreeStateRow("2.5 לחץ הגז בצנרת פנים המבנה אינו גדול מ-1.4 בר", checkPressureUpTo1_4, { checkPressureUpTo1_4 = it })
                    ThreeStateRow("2.6 הצנרת ומרכיביה מקובעים", checkPipingSecured, { checkPipingSecured = it })
                    ThreeStateRow("2.7 כל מוצא שאינו בשימוש קבוע, סגור בפקק/ברז תקין", checkUnusedOutletsPlugged, { checkUnusedOutletsPlugged = it })
                }

                FormCard("3. בדיקת אטימות ולחצים", cardBg, borderColor, primaryColor) {
                    Text("3.1 אטימות לחץ ראשוני (בדיקת נוזל בלחץ מכל):", fontWeight = FontWeight.Bold, color = textWhite, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    CheckboxWithLabel("נמצאה דליפה (קריטי!)", isLeakFoundPrimary, { isLeakFoundPrimary = it; saveToDatabase() }, CheckboxDefaults.colors(checkedColor = errorRed), textWhite, Modifier.fillMaxWidth())
                    AnimatedVisibility(visible = isLeakFoundPrimary) {
                        OutlinedTextField(value = leakLocationDetails, onValueChange = { leakLocationDetails = it; saveToDatabase() }, label = { Text("ציין את מקום הדליפה") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = errorFieldColors)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = borderColor)

                    Text("3.2 אטימות מערכת ללחץ ביניים (למשך 15 דקות):", fontWeight = FontWeight.Bold, color = textWhite, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = intermediatePressureValue, onValueChange = { intermediatePressureValue = it; saveToDatabase() }, label = { Text("לחץ הבדיקה (mbar/bar)") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("האם הלחץ נשמר?", color = textWhite, modifier = Modifier.weight(1f))
                        RadioButton(selected = isIntermediatePressureKept, onClick = { isIntermediatePressureKept = true; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = successGreen))
                        Text("כן", color = textWhite)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = !isIntermediatePressureKept, onClick = { isIntermediatePressureKept = false; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = errorRed))
                        Text("לא", color = textWhite)
                    }
                }

                FormCard("4. סיכום מבצע הבדיקה (סטטוס)", cardBg, borderColor, primaryColor) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { finalStatus = "OK"; saveToDatabase() }) {
                        RadioButton(selected = finalStatus == "OK", onClick = { finalStatus = "OK"; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = successGreen))
                        Text("המתקן נמצא תקין בהתאם לדרישות", color = successGreen, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { finalStatus = "DEFECTS"; saveToDatabase() }) {
                        RadioButton(selected = finalStatus == "DEFECTS", onClick = { finalStatus = "DEFECTS"; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF9800)))
                        Text("נמצאו ליקויים ויש לתקנם עד תאריך:", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                    AnimatedVisibility(visible = finalStatus == "DEFECTS") {
                        OutlinedTextField(value = defectsFixByDate, onValueChange = { defectsFixByDate = it; saveToDatabase() }, label = { Text("תאריך יעד לתיקון (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth().padding(start = 40.dp, bottom = 8.dp), colors = textFieldColors)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { finalStatus = "DISCONNECTED"; saveToDatabase() }) {
                        RadioButton(selected = finalStatus == "DISCONNECTED", onClick = { finalStatus = "DISCONNECTED"; saveToDatabase() }, colors = RadioButtonDefaults.colors(selectedColor = errorRed))
                        Text("הספקת הגז נותקה עקב ליקויים חמורים", color = errorRed, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = executionRemarks, onValueChange = { executionRemarks = it; saveToDatabase() }, label = { Text("הערות נוספות וסיכום הליקויים") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = textFieldColors)
                }

                // תיבות פירוט ליקויים (הועבר לסוף הדוח לפני תמונות וחתימות)
                AnimatedVisibility(visible = failedReasonsMap.isNotEmpty()) {
                    FormCard("פירוט ליקויים שנמצאו בבדיקה (חובה למלא)", cardBg, errorRed, errorRed) {
                        failedReasonsMap.keys.forEach { sectionTitle ->
                            OutlinedTextField(
                                value = failedReasonsMap[sectionTitle] ?: "",
                                onValueChange = { failedReasonsMap[sectionTitle] = it; saveToDatabase() },
                                label = { Text("פרט מדוע '$sectionTitle' אינו תקין") },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = errorFieldColors, minLines = 2
                            )
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = technicianName, onValueChange = { technicianName = it; saveToDatabase() }, label = { Text("שם מבצע הבדיקה") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                        OutlinedTextField(value = technicianLicense, onValueChange = { technicianLicense = it; saveToDatabase() }, label = { Text("מס' רישיון מתקין") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("חתימת הלקוח (נציג ועד הבית / אחראי):", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    TechnicianSignatureTouchPad(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp), initialSignatureUri = if (clientSignatureUri.isNotEmpty()) clientSignatureUri else null, onSignatureSaved = { uri -> clientSignatureUri = uri; saveToDatabase() })
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = customerId, onValueChange = { customerId = it; saveToDatabase() }, label = { Text("שם החותם / ת.ז") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                }

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { saveToDatabase(); viewModel.previewPeriodicPdf(context, buildCurrentForm()) },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(10.dp)
                        ) { Text("תצוגה מקדימה", fontWeight = FontWeight.Bold, fontSize = 13.sp) }

                        Button(
                            onClick = {
                                isFormSavedToTarget = false
                                viewModel.saveCurrentPeriodicForm(buildCurrentForm().copy(isSavedToTarget = false, savedTargetLocation = "מכשיר")) { Toast.makeText(context, "נשמר כטיוטה!", Toast.LENGTH_SHORT).show(); onNavigateBack() }
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = successGreen), shape = RoundedCornerShape(10.dp)
                        ) { Text("שמור כטיוטה", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { if (currentFormId != 0) viewModel.deletePeriodicForm(PeriodicGasForm(id = currentFormId)); onNavigateBack() },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isDark) Color(0xFF333333) else Color(0xFFE0E0E0)), shape = RoundedCornerShape(10.dp)
                        ) { Text("מחק טופס", fontWeight = FontWeight.Bold, color = errorRed, fontSize = 13.sp) }

                        Button(
                            onClick = {
                                val allStates = listOf(
                                    checkLocationOpen, checkSafetyDistances07Heat, checkSafetyDistances17Fire, checkSafetyDistances05Pits,
                                    checkSafetyDistances3Drainage, checkSafetyDistances12Building, checkSafetyDistances3LowLevel,
                                    checkRegulatorSecured, checkWarningSigns, checkWaterSprinklers, checkGasRoomMax20, checkGasRoomLighting,
                                    checkGasRoomNoFlammables, checkCageMax20, checkCageVentilated, checkRampsSecured, checkEarthquakeValve,
                                    checkEarthquakeValveSecured, checkMainValveAccessible, checkDischargeValves, checkPressureUpTo1_4,
                                    checkPipingSecured, checkUnusedOutletsPlugged
                                )
                                val isAllAnswered = allStates.all { it.isNotEmpty() }

                                if (clientName.isBlank() || clientPhone.isBlank()) {
                                    Toast.makeText(context, "שגיאה: חובה למלא שם איש קשר וטלפון.", Toast.LENGTH_LONG).show()
                                } else if (!isAllAnswered) {
                                    Toast.makeText(context, "שגיאה: חובה לענות על כל סעיפי הבדיקה בטופס (סמן 'לא ישים' היכן שצריך).", Toast.LENGTH_LONG).show()
                                } else {
                                    isFormSavedToTarget = true
                                    if (currentFormId == 0) settingsManager.currentFormNumber = sequentialNumber + 1
                                    viewModel.sharePeriodicPdf(context, buildCurrentForm().copy(isSavedToTarget = true, savedTargetLocation = "Shared")) { onNavigateBack() }
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