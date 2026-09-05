package com.example.myapplication158.UserInterface.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.myapplication158.data.GasForm
import com.example.myapplication158.util.SettingsManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormEditScreen(
    viewModel: GasFormViewModel? = null,
    form: Any? = null,
    onNavigateBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settingsManager = remember { SettingsManager(context) }
    val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

    val initialForm = form as? GasForm

    var sequentialNumber by remember {
        mutableStateOf(
            if (initialForm != null && initialForm.sequentialNumber > 0) initialForm.sequentialNumber
            else settingsManager.currentFormNumber
        )
    }

    val isDark = settingsManager.isDarkMode
    val primaryColor = MaterialTheme.colorScheme.primary

    val bgScreenColor = if (isDark) Color(0xFF0D0D0D) else Color(0xFFF4F6F8)
    val cardBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)

    val headerBg = if (isDark) {
        when (settingsManager.appTheme) {
            SettingsManager.THEME_BLUE -> Color(0xFF04132B)
            SettingsManager.THEME_GREEN -> Color(0xFF0A2711)
            SettingsManager.THEME_PURPLE -> Color(0xFF260930)
            else -> Color(0xFF381504)
        }
    } else Color.White

    val textWhite = if (isDark) Color(0xFFF5F5F5) else Color(0xFF212121)
    val textGray = if (isDark) Color(0xFFAAAAAA) else Color(0xFF757575)
    val borderColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFE0E0E0)

    val successGreen = Color(0xFF4CAF50)
    val errorRed = Color(0xFFFF5252)

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

    val checkboxColors = CheckboxDefaults.colors(checkedColor = primaryColor, uncheckedColor = textGray, checkmarkColor = Color.White)
    val radioColors = RadioButtonDefaults.colors(selectedColor = primaryColor, unselectedColor = textGray)

    var currentFormId by remember { mutableStateOf(initialForm?.id ?: 0) }
    var date by remember { mutableStateOf(initialForm?.date?.takeIf { it.isNotBlank() } ?: currentDate) }
    var clientName by remember { mutableStateOf(initialForm?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(initialForm?.clientPhone ?: "") }

    var isUnaddressedSite by remember { mutableStateOf(initialForm?.isUnaddressedSite ?: false) }
    var city by remember { mutableStateOf(initialForm?.clientCity ?: "") }
    var street by remember { mutableStateOf(initialForm?.clientStreet ?: "") }
    var apartment by remember { mutableStateOf(initialForm?.clientBuilding ?: "") }
    var gpsCoordinates by remember { mutableStateOf(initialForm?.gpsCoordinates ?: "") }
    var sitePhotoUri by remember { mutableStateOf(initialForm?.sitePhotoUri ?: "") }

    var isSharedBuilding by remember { mutableStateOf(initialForm?.isSharedBuilding ?: false) }
    var isCommercial by remember { mutableStateOf(initialForm?.isCommercial ?: false) }
    var isCylinders12x2 by remember { mutableStateOf(initialForm?.isCylinders12x2 ?: false) }
    var isCylinders48x2 by remember { mutableStateOf(initialForm?.isCylinders48x2 ?: false) }
    var isOtherInstallation by remember { mutableStateOf(initialForm?.isOtherType ?: false) }
    var otherInstallationDetails by remember { mutableStateOf(initialForm?.otherTypeText ?: "") }

    var isNewInstallation by remember { mutableStateOf(initialForm?.isWorkNew ?: false) }
    var isRepair by remember { mutableStateOf(initialForm?.isWorkRepair ?: false) }
    var isAddition by remember { mutableStateOf(initialForm?.isWorkAddition ?: false) }
    var isApplianceConnection by remember { mutableStateOf(initialForm?.isWorkConnection ?: false) }

    var applianceType by remember { mutableStateOf(initialForm?.connectionDeviceType ?: "") }
    var applianceBrand by remember { mutableStateOf(initialForm?.connectionDeviceBrand ?: "") }
    var applianceModel by remember { mutableStateOf(initialForm?.connectionDeviceModel ?: "") }
    var applianceSerial by remember { mutableStateOf(initialForm?.connectionDeviceSerial ?: "") }

    var hasAdditionalAppliance by remember { mutableStateOf(initialForm?.hasAdditionalDevice ?: false) }
    var additionalDeviceType by remember { mutableStateOf(initialForm?.additionalDeviceType ?: "") }
    var additionalDeviceBrand by remember { mutableStateOf(initialForm?.additionalDeviceBrand ?: "") }
    var additionalDeviceModel by remember { mutableStateOf(initialForm?.additionalDeviceModel ?: "") }
    var additionalDeviceSerial by remember { mutableStateOf(initialForm?.additionalDeviceSerial ?: "") }

    var pipeType by remember { mutableStateOf(initialForm?.hoseType ?: "") }
    var pipeYear by remember { mutableStateOf(initialForm?.hoseProductionYear ?: "") }
    var clampsNumber by remember { mutableStateOf(initialForm?.clampsCount ?: "") }

    var isTightnessTest by remember { mutableStateOf(initialForm?.isLeakTestChecked ?: false) }
    var tightnessDate by remember { mutableStateOf(initialForm?.leakTestDate?.takeIf { it.isNotBlank() } ?: currentDate) }
    var tightnessPressure by remember { mutableStateOf(initialForm?.leakTestPressure ?: "") }

    var isPressureTest by remember { mutableStateOf(initialForm?.isPressureTestChecked ?: false) }
    var pressureDate by remember { mutableStateOf(initialForm?.pressureTestDate?.takeIf { it.isNotBlank() } ?: currentDate) }
    var pressureValue by remember { mutableStateOf(initialForm?.pressureTestValue ?: "") }

    var isRegulatorTest by remember { mutableStateOf(initialForm?.isRegulatorTestChecked ?: false) }
    var regulatorPressure by remember { mutableStateOf(initialForm?.regulatorPressure ?: "") }

    var isStandard1 by remember { mutableStateOf(initialForm?.complianceRoutePlan ?: false) }
    var isStandard2 by remember { mutableStateOf(initialForm?.complianceMaterials ?: false) }
    var isPipelineNotDoneBy by remember { mutableStateOf(initialForm?.isRouteNotDoneByChecked ?: false) }
    var isPipelineDoneBy by remember { mutableStateOf(initialForm?.isRouteDoneByChecked ?: false) }
    var pipelineDetails by remember { mutableStateOf(if (isPipelineDoneBy) initialForm?.routeDoneBy ?: "" else initialForm?.routeNotDoneBy ?: "") }

    var notes by remember { mutableStateOf(initialForm?.executionRemarks ?: "") }
    var isCompliant by remember { mutableStateOf(initialForm?.isStatusConforming ?: true) }

    var nonCompliantReason by remember { mutableStateOf(initialForm?.nonCompliantReason ?: "") }

    var technicianName by remember { mutableStateOf(initialForm?.technicianStamp?.takeIf { it.isNotBlank() } ?: settingsManager.defaultTechnicianName) }
    var customerId by remember { mutableStateOf(initialForm?.clientIdConfirm ?: "") }
    var clientSignatureUri by remember { mutableStateOf(initialForm?.clientSignatureUri ?: "") }

    var selectedPipelineUris by remember { mutableStateOf<List<Uri>>(initialForm?.extraRouteImageUris?.split(",")?.filter { it.isNotBlank() }?.map { Uri.parse(it) } ?: emptyList()) }
    var selectedNotesUris by remember { mutableStateOf<List<Uri>>(initialForm?.remarksImageUris?.split(",")?.filter { it.isNotBlank() }?.map { Uri.parse(it) } ?: emptyList()) }

    var isFormSavedToTarget by remember { mutableStateOf(initialForm?.isSavedToTarget == true) }
    var savedTargetLocationState by remember { mutableStateOf(initialForm?.savedTargetLocation) }

    fun buildCurrentForm(): GasForm {
        val targetLocation = if (isFormSavedToTarget) savedTargetLocationState else "מכשיר"
        return GasForm(
            id = currentFormId, date = date, clientName = clientName, clientPhone = clientPhone,
            sequentialNumber = sequentialNumber,
            isUnaddressedSite = isUnaddressedSite, gpsCoordinates = gpsCoordinates, sitePhotoUri = sitePhotoUri,
            clientCity = city, clientStreet = street, clientBuilding = apartment,
            isSharedBuilding = isSharedBuilding, isCommercial = isCommercial,
            isCylinders12x2 = isCylinders12x2, isCylinders48x2 = isCylinders48x2,
            isOtherType = isOtherInstallation, otherTypeText = otherInstallationDetails,
            isWorkNew = isNewInstallation, isWorkAddition = isAddition, isWorkRepair = isRepair,
            isWorkConnection = isApplianceConnection, connectionDeviceType = applianceType,
            connectionDeviceBrand = applianceBrand, connectionDeviceModel = applianceModel,
            connectionDeviceSerial = applianceSerial, hasAdditionalDevice = hasAdditionalAppliance,
            additionalDeviceType = additionalDeviceType, additionalDeviceBrand = additionalDeviceBrand,
            additionalDeviceModel = additionalDeviceModel, additionalDeviceSerial = additionalDeviceSerial,
            hoseType = pipeType, hoseProductionYear = pipeYear, clampsCount = clampsNumber,
            isLeakTestChecked = isTightnessTest, leakTestDate = tightnessDate, leakTestPressure = tightnessPressure,
            isPressureTestChecked = isPressureTest, pressureTestDate = pressureDate, pressureTestValue = pressureValue,
            isRegulatorTestChecked = isRegulatorTest, regulatorPressure = regulatorPressure,
            complianceRoutePlan = isStandard1, complianceMaterials = isStandard2,
            isRouteNotDoneByChecked = isPipelineNotDoneBy, routeNotDoneBy = if (isPipelineNotDoneBy) pipelineDetails else "",
            isRouteDoneByChecked = isPipelineDoneBy, routeDoneBy = if (isPipelineDoneBy) pipelineDetails else "",
            executionRemarks = notes, isStatusConforming = isCompliant, isStatusNonConforming = !isCompliant,
            pluggedAt = "",
            nonCompliantReason = if (!isCompliant) nonCompliantReason else "",
            technicianStamp = technicianName, clientIdConfirm = customerId,
            clientSignatureUri = clientSignatureUri,
            extraRouteImageUris = selectedPipelineUris.joinToString(",") { it.toString() },
            remarksImageUris = selectedNotesUris.joinToString(",") { it.toString() },
            createdAt = initialForm?.createdAt ?: System.currentTimeMillis(),
            savedTargetLocation = targetLocation,
            isSavedToTarget = isFormSavedToTarget
        )
    }

    val saveToDatabase = { viewModel?.autoSaveForm(buildCurrentForm()) { newId -> currentFormId = newId } }

    fun copyGalleryUriToInternal(sourceUri: Uri, prefix: String): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val file = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream -> inputStream.copyTo(outputStream) }
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    var tempSitePhotoUri by remember { mutableStateOf<Uri?>(null) }
    val sitePhotoCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { tempSitePhotoUri?.let { sitePhotoUri = it.toString() }; Toast.makeText(context, "תמונת שטח נשמרה!", Toast.LENGTH_SHORT).show(); saveToDatabase() }
    }
    fun launchSitePhotoCamera() {
        try {
            val file = File(context.filesDir, "site_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempSitePhotoUri = uri
            sitePhotoCameraLauncher.launch(uri)
        } catch (e: Exception) { Toast.makeText(context, "שגיאת מצלמה.", Toast.LENGTH_LONG).show() }
    }
    val sitePhotoGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { copyGalleryUriToInternal(uri, "site_photo")?.let { sitePhotoUri = it.toString(); saveToDatabase() } }
    }

    // --- שדרוג לאונצ'ר המיקום: קבלת ופענוח הכתובת ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchLocationAndDecode(context) { coords, address ->
                gpsCoordinates = if (address.isNotEmpty()) "$coords | משוער: $address" else coords
                saveToDatabase()
            }
        }
        else { Toast.makeText(context, "חובה לאשר הרשאות מיקום כדי לדגום נ.צ", Toast.LENGTH_LONG).show() }
    }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { tempCameraUri?.let { selectedPipelineUris = selectedPipelineUris + it }; saveToDatabase() }
    }
    fun launchCameraSafely() {
        try {
            val file = File(context.filesDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempCameraUri = uri; cameraLauncher.launch(uri)
        } catch (e: Exception) {}
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) { val copied = uris.mapNotNull { copyGalleryUriToInternal(it, "gallery_route") }; selectedPipelineUris = selectedPipelineUris + copied; saveToDatabase() }
    }

    var tempNotesUri by remember { mutableStateOf<Uri?>(null) }
    val notesCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { tempNotesUri?.let { selectedNotesUris = selectedNotesUris + it }; saveToDatabase() }
    }
    fun launchNotesCamera() {
        try {
            val file = File(context.filesDir, "notes_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempNotesUri = uri; notesCameraLauncher.launch(uri)
        } catch (e: Exception) {}
    }
    val notesGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) { val copied = uris.mapNotNull { copyGalleryUriToInternal(it, "gallery_notes") }; selectedNotesUris = selectedNotesUris + copied; saveToDatabase() }
    }

    fun validateForShare(): Boolean {
        if (clientPhone.isBlank()) {
            Toast.makeText(context, "שגיאה: חובה להזין מספר טלפון של הלקוח.", Toast.LENGTH_LONG).show()
            return false
        }
        if (isUnaddressedSite) {
            if (gpsCoordinates.isBlank() || sitePhotoUri.isBlank()) {
                Toast.makeText(context, "שגיאה: הגדרת אתר ללא כתובת. חובה לדגום נ.צ ולצרף צילום שטח.", Toast.LENGTH_LONG).show()
                return false
            }
        } else {
            if (city.isBlank() || street.isBlank()) {
                Toast.makeText(context, "שגיאה: חובה להזין עיר ורחוב, או לסמן 'אתר ללא כתובת'.", Toast.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) { saveToDatabase() } }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer); saveToDatabase() }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = bgScreenColor,
            topBar = {
                Surface(color = headerBg, shadowElevation = 4.dp) {
                    Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { saveToDatabase(); onNavigateBack() }) { Icon(Icons.Default.ArrowForward, "חזור", tint = textWhite) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (currentFormId == 0) "מילוי טופס (מס' $sequentialNumber)" else "עריכת טופס (מס' $sequentialNumber)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textWhite)
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(modifier = Modifier.height(4.dp))

                FormCard("פרטי הצהרה כלליים", cardBg, borderColor, primaryColor) {
                    OutlinedTextField(value = date, onValueChange = { date = it; saveToDatabase() }, label = { Text("תאריך") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = textGray) }, colors = textFieldColors)
                }

                FormCard("פרטי הלקוח והמיקום", cardBg, borderColor, primaryColor) {
                    OutlinedTextField(value = clientName, onValueChange = { clientName = it; saveToDatabase() }, label = { Text("שם הלקוח") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = clientPhone, onValueChange = { clientPhone = it; saveToDatabase() }, label = { Text("מספר טלפון (חובה לשיתוף)") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = borderColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    CheckboxWithLabel("אתר בבנייה / ללא כתובת מוסדרת", isUnaddressedSite, { isUnaddressedSite = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.fillMaxWidth(), "ידרוש דגימת GPS וצילום שטח במקום עיר ורחוב")

                    AnimatedVisibility(visible = !isUnaddressedSite) {
                        Column {
                            OutlinedTextField(value = city, onValueChange = { city = it; saveToDatabase() }, label = { Text("יישוב") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(value = street, onValueChange = { street = it; saveToDatabase() }, label = { Text("רחוב") }, modifier = Modifier.weight(2f), colors = textFieldColors)
                                OutlinedTextField(value = apartment, onValueChange = { apartment = it; saveToDatabase() }, label = { Text("בניין/דירה") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                            }
                        }
                    }

                    // --- הבלוק החדש: חלון פרטי לפענוח הכתובת ---
                    AnimatedVisibility(visible = isUnaddressedSite) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).border(1.dp, borderColor, RoundedCornerShape(8.dp)).padding(12.dp)) {

                            val coordsPart = if (gpsCoordinates.contains(" | ")) gpsCoordinates.substringBefore(" | ") else gpsCoordinates
                            val addressPart = if (gpsCoordinates.contains(" משוער: ")) gpsCoordinates.substringAfter(" משוער: ") else ""

                            Text("נ.צ (GPS): ${if (coordsPart.isEmpty()) "עדיין לא נדגם" else coordsPart}", color = textWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                            // חלון פענוח אזור
                            if (addressPart.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(color = if(isDark) Color(0xFF2A2A2A) else Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Map, null, tint = primaryColor, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("אזור משוער (פוענח אוטומטית):", fontSize = 11.sp, color = textGray)
                                            Text(addressPart, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textWhite)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
                                Text(if (coordsPart.isEmpty()) "דגום מיקום עכשיו" else "עדכן מיקום נוכחי", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = borderColor)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("צילום שטח / מפת התמצאות:", color = textWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { launchSitePhotoCamera() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(isDark) Color(0xFF333333) else Color(0xFFE0E0E0))) { Text("מצלמה", color = if(isDark) Color.White else Color.Black) }
                                Button(onClick = { sitePhotoGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(isDark) Color(0xFF333333) else Color(0xFFE0E0E0))) { Text("גלריה", color = if(isDark) Color.White else Color.Black) }
                            }
                            if (sitePhotoUri.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                                    AsyncImage(model = Uri.parse(sitePhotoUri), contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick = { sitePhotoUri = ""; saveToDatabase() },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape).size(24.dp)
                                    ) { Icon(Icons.Default.Close, contentDescription = "מחק תמונה", tint = Color.White, modifier = Modifier.size(16.dp)) }
                                }
                            }
                        }
                    }
                }

                FormCard("פרטי המתקן ומיקומו", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CheckboxWithLabel("בית משותף", isSharedBuilding, { isSharedBuilding = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                        CheckboxWithLabel("מסחרי", isCommercial, { isCommercial = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CheckboxWithLabel("מכלים 2*12", isCylinders12x2, { isCylinders12x2 = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                        CheckboxWithLabel("מכלים 2*48", isCylinders48x2, { isCylinders48x2 = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CheckboxWithLabel("אחר", isOtherInstallation, { isOtherInstallation = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                    }
                    AnimatedVisibility(visible = isOtherInstallation, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        OutlinedTextField(value = otherInstallationDetails, onValueChange = { otherInstallationDetails = it; saveToDatabase() }, label = { Text("פרט מתקן אחר") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = textFieldColors)
                    }
                }

                FormCard("תיאור העבודה", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CheckboxWithLabel("מתקן חדש", isNewInstallation, { isNewInstallation = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                        CheckboxWithLabel("תוספת למתקן קיים", isAddition, { isAddition = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CheckboxWithLabel("תיקון נזק / שינוי", isRepair, { isRepair = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                        CheckboxWithLabel("חיבור מכשיר:", isApplianceConnection, { isApplianceConnection = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                    }

                    AnimatedVisibility(visible = isApplianceConnection, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).border(1.dp, borderColor, RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = applianceType, onValueChange = { applianceType = it; saveToDatabase() }, label = { Text("סוג המכשיר") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                                OutlinedTextField(value = applianceBrand, onValueChange = { applianceBrand = it; saveToDatabase() }, label = { Text("מותג המכשיר") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = applianceModel, onValueChange = { applianceModel = it; saveToDatabase() }, label = { Text("דגם המכשיר") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                                OutlinedTextField(value = applianceSerial, onValueChange = { applianceSerial = it; saveToDatabase() }, label = { Text("מס' סריאלי") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CheckboxWithLabel("הוספת מכשיר נוסף", hasAdditionalAppliance, { hasAdditionalAppliance = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.fillMaxWidth())

                            AnimatedVisibility(visible = hasAdditionalAppliance) {
                                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                    Text("פרטי מכשיר שני:", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = additionalDeviceType, onValueChange = { additionalDeviceType = it; saveToDatabase() }, label = { Text("סוג מכשיר 2") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                                        OutlinedTextField(value = additionalDeviceBrand, onValueChange = { additionalDeviceBrand = it; saveToDatabase() }, label = { Text("מותג מכשיר 2") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = additionalDeviceModel, onValueChange = { additionalDeviceModel = it; saveToDatabase() }, label = { Text("דגם מכשיר 2") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                                        OutlinedTextField(value = additionalDeviceSerial, onValueChange = { additionalDeviceSerial = it; saveToDatabase() }, label = { Text("מס' סריאלי 2") }, modifier = Modifier.weight(1f), colors = textFieldColors)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("המכשירים חוברו עם צינור גומי תקני עד 3 מטר למכשיר. סה\"כ חבקים:", color = textWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = pipeType, onValueChange = { pipeType = it; saveToDatabase() }, label = { Text("סוג צינור") }, modifier = Modifier.weight(1f), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = pipeYear, onValueChange = { pipeYear = it; saveToDatabase() }, label = { Text("שנת ייצור") }, modifier = Modifier.weight(1f), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = clampsNumber, onValueChange = { clampsNumber = it; saveToDatabase() }, label = { Text("מס' חבקים") }, modifier = Modifier.weight(1f), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }

                FormCard("בדיקות לחצים לפני הפעלה", cardBg, borderColor, primaryColor) {
                    CheckboxWithLabel("בדיקת אטימות ללחץ שימוש", isTightnessTest, { isTightnessTest = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.fillMaxWidth(), "נערכה בתאריך, לחץ בדיקה mbar, במשך 15 דק'")
                    AnimatedVisibility(visible = isTightnessTest, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = tightnessDate, onValueChange = { tightnessDate = it; saveToDatabase() }, label = { Text("תאריך אטימות") }, leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = textGray, modifier = Modifier.size(18.dp)) }, modifier = Modifier.weight(1f), colors = textFieldColors)
                            OutlinedTextField(value = tightnessPressure, onValueChange = { tightnessPressure = it; saveToDatabase() }, label = { Text("לחץ בדיקה (mbar)") }, modifier = Modifier.weight(1f), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = borderColor)

                    CheckboxWithLabel("בדיקת לחץ", isPressureTest, { isPressureTest = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.fillMaxWidth(), "נערכה בתאריך, לחץ בדיקה 0.250 BAR או אחר")
                    AnimatedVisibility(visible = isPressureTest, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = pressureDate, onValueChange = { pressureDate = it; saveToDatabase() }, label = { Text("תאריך בדיקת לחץ") }, leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = textGray, modifier = Modifier.size(18.dp)) }, modifier = Modifier.weight(1f), colors = textFieldColors)
                            OutlinedTextField(value = pressureValue, onValueChange = { pressureValue = it; saveToDatabase() }, label = { Text("לחץ בדיקה (BAR)") }, modifier = Modifier.weight(1f), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = borderColor)

                    CheckboxWithLabel("בדיקת לחץ ווסת", isRegulatorTest, { isRegulatorTest = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.fillMaxWidth(), "ערך לחץ הווסת (mbar)")
                    AnimatedVisibility(visible = isRegulatorTest, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        val pressureValNum = regulatorPressure.toDoubleOrNull()
                        val isPressureValid = pressureValNum != null && pressureValNum in 25.0..39.0
                        val isPressureInvalid = pressureValNum != null && (pressureValNum < 25.0 || pressureValNum > 39.0)

                        val currentColors = if (isPressureValid) {
                            OutlinedTextFieldDefaults.colors(focusedBorderColor = successGreen, unfocusedBorderColor = successGreen, focusedTextColor = successGreen, unfocusedTextColor = successGreen, focusedLabelColor = successGreen, unfocusedLabelColor = successGreen, focusedContainerColor = cardBg, unfocusedContainerColor = cardBg)
                        } else if (isPressureInvalid) {
                            OutlinedTextFieldDefaults.colors(focusedBorderColor = errorRed, unfocusedBorderColor = errorRed, focusedTextColor = errorRed, unfocusedTextColor = errorRed, focusedLabelColor = errorRed, unfocusedLabelColor = errorRed, focusedContainerColor = cardBg, unfocusedContainerColor = cardBg)
                        } else { textFieldColors }

                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            OutlinedTextField(
                                value = regulatorPressure, onValueChange = { regulatorPressure = it; saveToDatabase() }, label = { Text("לחץ ווסת (mbar)") }, modifier = Modifier.fillMaxWidth(), colors = currentColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    if (isPressureValid) Text("תקין ✓", color = successGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                                    else if (isPressureInvalid) Text("לא תקין ✗", color = errorRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                                }
                            )
                        }
                    }
                }

                FormCard("בדיקת תוואי וחומרי מבנה", cardBg, borderColor, primaryColor) {
                    CheckboxWithLabel("התוואי בהתאם לתוכניות ולתקן ישראלי 158", isStandard1, { isStandard1 = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.fillMaxWidth())
                    CheckboxWithLabel("חומרי המבנה מתאימים לדרישות תקן 158", isStandard2, { isStandard2 = it; saveToDatabase() }, checkboxColors, textWhite, Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CheckboxWithLabel("התוואי לא נעשה ע\"י", isPipelineNotDoneBy, { isPipelineNotDoneBy = it; if(it) isPipelineDoneBy = false; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                        CheckboxWithLabel("התוואי נעשה ע\"י", isPipelineDoneBy, { isPipelineDoneBy = it; if(it) isPipelineNotDoneBy = false; saveToDatabase() }, checkboxColors, textWhite, Modifier.weight(1f))
                    }
                    AnimatedVisibility(visible = isPipelineDoneBy || isPipelineNotDoneBy, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        OutlinedTextField(value = pipelineDetails, onValueChange = { pipelineDetails = it; saveToDatabase() }, label = { Text("פרטים נוספים") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = textFieldColors)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("צילומי התוואי (הוספת תמונות):", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { launchCameraSafely() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("צלם במצלמה")
                        }
                        Button(onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("בחר מהגלריה")
                        }
                    }
                    if (selectedPipelineUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedPipelineUris) { uri ->
                                Box(modifier = Modifier.size(70.dp)) {
                                    AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick = { selectedPipelineUris = selectedPipelineUris - uri; saveToDatabase() },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape).size(20.dp)
                                    ) { Icon(Icons.Default.Close, contentDescription = "מחק", tint = Color.White, modifier = Modifier.size(14.dp)) }
                                }
                            }
                        }
                    }
                }

                FormCard("הערות ביצוע", cardBg, borderColor, primaryColor) {
                    OutlinedTextField(value = notes, onValueChange = { notes = it; saveToDatabase() }, label = { Text("הקלד הערות ביצוע כאן...") }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), colors = textFieldColors, minLines = 3)
                }

                FormCard("תמונות נוספות (הערות ביצוע)", cardBg, borderColor, primaryColor) {
                    Text("הוספת תמונות נוספות לדוח:", color = textGray, fontSize = 12.sp, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val secondaryButtonBg = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
                        val secondaryButtonText = if (isDark) Color.White else Color.Black
                        Button(onClick = { launchNotesCamera() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = secondaryButtonBg)) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp), tint = secondaryButtonText); Spacer(modifier = Modifier.width(4.dp)); Text("צלם להערות", color = secondaryButtonText)
                        }
                        Button(onClick = { notesGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = secondaryButtonBg)) {
                            Icon(Icons.Default.Collections, null, modifier = Modifier.size(16.dp), tint = secondaryButtonText); Spacer(modifier = Modifier.width(4.dp)); Text("בחר מהגלריה", color = secondaryButtonText)
                        }
                    }
                    if (selectedNotesUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(selectedNotesUris) { uri ->
                                Box(modifier = Modifier.size(70.dp)) {
                                    AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick = { selectedNotesUris = selectedNotesUris - uri; saveToDatabase() },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape).size(20.dp)
                                    ) { Icon(Icons.Default.Close, contentDescription = "מחק", tint = Color.White, modifier = Modifier.size(14.dp)) }
                                }
                            }
                        }
                    }
                }

                FormCard("סטטוס התאמה והצהרה", cardBg, borderColor, primaryColor) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isCompliant, onClick = { isCompliant = true; saveToDatabase() }, colors = radioColors)
                            Text("מתאים לתקן ✓", color = successGreen, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !isCompliant, onClick = { isCompliant = false; saveToDatabase() }, colors = radioColors)
                            Text("אינו מתאים לתקן ✗", color = errorRed, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = !isCompliant, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        OutlinedTextField(
                            value = nonCompliantReason,
                            onValueChange = { nonCompliantReason = it; saveToDatabase() },
                            label = { Text("סיבת למה המערכת לא תקינה:") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = errorFieldColors,
                            minLines = 2
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = technicianName, onValueChange = { technicianName = it; saveToDatabase() }, label = { Text("חותמת / שם הטכנאי המאשר") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, singleLine = true)
                }

                FormCard("חתימת הלקוח (נא לצייר כאן):", cardBg, borderColor, primaryColor) {
                    TechnicianSignatureTouchPad(modifier = Modifier.fillMaxWidth().height(200.dp), initialSignatureUri = if (clientSignatureUri.isNotEmpty()) clientSignatureUri else null, onSignatureSaved = { uri -> clientSignatureUri = uri; saveToDatabase() })
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = customerId, onValueChange = { customerId = it; saveToDatabase() }, label = { Text("ת.ז / ח.פ של הלקוח") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { saveToDatabase(); viewModel?.previewPdf(context, buildCurrentForm()) },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("תצוגה מקדימה", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Button(
                            onClick = {
                                isFormSavedToTarget = false
                                savedTargetLocationState = "מכשיר"
                                val formToSave = buildCurrentForm().copy(isSavedToTarget = false, savedTargetLocation = "מכשיר")
                                viewModel?.saveCurrentForm(formToSave) { Toast.makeText(context, "הטופס נשמר כטיוטה!", Toast.LENGTH_SHORT).show(); onNavigateBack() }
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = successGreen), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("שמור כטיוטה", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (currentFormId != 0) viewModel?.deleteForm(GasForm(id = currentFormId))
                                Toast.makeText(context, "הטופס נמחק בהצלחה", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isDark) Color(0xFF333333) else Color(0xFFE0E0E0)), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF5252))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("מחק טופס", fontWeight = FontWeight.Bold, color = Color(0xFFFF5252), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Button(
                            onClick = {
                                if (validateForShare()) {
                                    isFormSavedToTarget = true
                                    savedTargetLocationState = "Shared"
                                    val formToShare = buildCurrentForm().copy(isSavedToTarget = true, savedTargetLocation = "Shared")

                                    if (currentFormId == 0) {
                                        settingsManager.currentFormNumber = sequentialNumber + 1
                                    }

                                    viewModel?.sharePdf(context, formToShare) { savedForm ->
                                        currentFormId = savedForm.id
                                        onNavigateBack()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("שתף וסיים", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// פונקציית פענוח מיקום (מריצה הליך ברקע שלא תוקע את האפליקציה)
fun decodeLocationAndReturn(context: Context, lat: Double, lon: Double, onResult: (String, String) -> Unit) {
    Thread {
        var addressText = ""
        try {
            val geocoder = Geocoder(context, Locale("he", "IL"))
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val street = address.thoroughfare
                val city = address.locality ?: address.subAdminArea
                if (street != null && city != null) {
                    addressText = "$street, $city"
                } else if (city != null) {
                    addressText = city
                } else {
                    addressText = address.getAddressLine(0) ?: ""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        android.os.Handler(Looper.getMainLooper()).post {
            onResult("$lat, $lon", addressText)
        }
    }.start()
}

@SuppressLint("MissingPermission")
fun fetchLocationAndDecode(context: Context, onResult: (String, String) -> Unit) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(context, "נא להפעיל שירותי מיקום (GPS) במכשיר", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(context, "דוגם מיקום ומפענח כתובת...", Toast.LENGTH_SHORT).show()

        var locationFound = false

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!locationFound) {
                    locationFound = true
                    decodeLocationAndReturn(context, location.latitude, location.longitude, onResult)
                    Toast.makeText(context, "המיקום נדגם בהצלחה!", Toast.LENGTH_SHORT).show()
                    locationManager.removeUpdates(this)
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener, Looper.getMainLooper())
        }
        if (isGpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener, Looper.getMainLooper())
        }

        android.os.Handler(Looper.getMainLooper()).postDelayed({
            if (!locationFound) {
                locationManager.removeUpdates(locationListener)

                val lastGps = if (isGpsEnabled) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
                val lastNetwork = if (isNetworkEnabled) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) else null

                val bestLast = when {
                    lastGps != null && lastNetwork != null -> if (lastGps.accuracy < lastNetwork.accuracy) lastGps else lastNetwork
                    else -> lastGps ?: lastNetwork
                }

                if (bestLast != null) {
                    locationFound = true
                    decodeLocationAndReturn(context, bestLast.latitude, bestLast.longitude, onResult)
                    Toast.makeText(context, "מיקום נדגם (לפי אחרון ידוע)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "לא הצלחנו לדגום מיקום במדויק. נסה לצאת למקום פתוח.", Toast.LENGTH_LONG).show()
                }
            }
        }, 8000)

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "שגיאה בדגימת המיקום: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}