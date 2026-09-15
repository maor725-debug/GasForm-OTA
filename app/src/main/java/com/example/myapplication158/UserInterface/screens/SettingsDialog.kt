package com.example.myapplication158.UserInterface.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.documentfile.provider.DocumentFile
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.UserInterface.components.SignaturePad
import com.example.myapplication158.util.SettingsManager
import com.example.myapplication158.util.SupabaseManager
import com.example.myapplication158.util.UserProfile
import com.example.myapplication158.util.ProfileUpdate
import com.example.myapplication158.TechnicianProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.provider.Settings

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onAppThemeChange: ((String) -> Unit)? = null,
    viewModel: GasFormViewModel? = null,
    initialCategoryIndex: Int = 0
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val scope = rememberCoroutineScope()
    val androidId = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_DEVICE" }

    val appSecurityPrefs = remember { context.getSharedPreferences("app_security_prefs", Context.MODE_PRIVATE) }
    var isLicensed by remember { mutableStateOf(appSecurityPrefs.getBoolean("is_licensed_user", false)) }
    val trialFormsCount by remember { mutableIntStateOf(appSecurityPrefs.getInt("trial_forms_count", 0)) }
    var showRegistrationDialog by remember { mutableStateOf(false) }

    val isDark = settingsManager.isDarkMode
    val darkBg = if (isDark) Color(0xFF0D0D0D) else Color(0xFFF4F6F8)
    val cardBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val primaryColor = MaterialTheme.colorScheme.primary
    val textWhite = if (isDark) Color(0xFFF5F5F5) else Color(0xFF212121)
    val textGray = if (isDark) Color(0xFFAAAAAA) else Color(0xFF757575)
    val borderColor = if (isDark) Color.DarkGray else Color.LightGray
    val selectedSurfaceBg = if (isDark) Color(0xFF2A2A2A) else Color(0xFFE3F2FD)
    val unselectedSurfaceBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)

    val warningBg = if (isDark) Color(0xFF4E342E) else Color(0xFFFFF3E0)
    val warningIcon = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
    val warningText = if (isDark) Color(0xFFFFE0B2) else Color(0xFFEF6C00)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primaryColor,
        unfocusedBorderColor = borderColor,
        focusedTextColor = textWhite,
        unfocusedTextColor = textWhite,
        cursorColor = primaryColor,
        focusedLabelColor = primaryColor,
        unfocusedLabelColor = textGray,
        focusedContainerColor = cardBg,
        unfocusedContainerColor = cardBg
    )

    var currentAppTheme by remember { mutableStateOf(settingsManager.appTheme) }
    var currentTemplateStyle by remember { mutableStateOf(settingsManager.pdfTemplateStyle) }

    var isPinEnabled by remember { mutableStateOf(settingsManager.isPinEnabled) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showPinWarningAlert by remember { mutableStateOf(false) }

    var selectedCategoryIndex by remember { mutableIntStateOf(initialCategoryIndex) }

    // הוספת קטגוריית "רישיון" ייעודית
    val categories = listOf(
        Triple("עיצוב", Icons.Default.Palette, 0),
        Triple("אחסון", Icons.Default.Cloud, 1),
        Triple("קבלן", Icons.Default.Badge, 2),
        Triple("רישיון", Icons.Default.VerifiedUser, 3), // לשונית חדשה לעדכון הרישיון
        Triple("חשבון", Icons.Default.AccountBox, 4),
        Triple("אבטחה", Icons.Default.Security, 5)
    )

    var isAutoSaveEnabled by remember { mutableStateOf(settingsManager.isAutoSavePdfEnabled) }

    var savedLicenseUri by remember { mutableStateOf(settingsManager.technicianLicenseUri ?: "") }
    var contractorHeader by remember { mutableStateOf(settingsManager.contractorHeader) }
    var contractorPhone by remember { mutableStateOf(settingsManager.contractorPhone) }
    var defaultTechnicianName by remember { mutableStateOf(settingsManager.defaultTechnicianName) }
    var currentFormNumberInput by remember { mutableStateOf(if (settingsManager.currentFormNumber > 0) settingsManager.currentFormNumber.toString() else "") }

    // שדות רישיון טכנאי לעדכון במסך ההגדרות
    var technicianLicenseNumber by remember { mutableStateOf(settingsManager.technicianLicenseNumber) }
    var technicianLicenseExpiry by remember { mutableStateOf(settingsManager.technicianLicenseExpiry) }
    var technicianLevel by remember { mutableStateOf(settingsManager.technicianLevel) }

    // תאריכון לבחירת תוקף הרישיון מחדש
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val dateInteractionSource = remember { MutableInteractionSource() }
    if (dateInteractionSource.collectIsPressedAsState().value) {
        showDatePicker = true
    }

    var customStorageTreeUri by remember { mutableStateOf(settingsManager.customStorageTreeUri) }
    var customStorageFolderName by remember { mutableStateOf(settingsManager.customStorageFolderName) }

    val folderPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                val docFolder = DocumentFile.fromTreeUri(context, uri)
                val folderDisplay = docFolder?.name ?: uri.lastPathSegment ?: "תיקייה נבחרת"
                customStorageTreeUri = uri.toString()
                customStorageFolderName = folderDisplay
                settingsManager.customStorageTreeUri = uri.toString()
                settingsManager.customStorageFolderName = folderDisplay
                Toast.makeText(context, "✓ תיקייה חוברה בהצלחה:\n$folderDisplay", Toast.LENGTH_LONG).show()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(onDismissRequest = { onDismissRequest(); onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Card(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f).padding(8.dp).border(1.dp, borderColor, RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = darkBg)) {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("הגדרות", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                        }
                        IconButton(onClick = { onDismissRequest(); onDismiss() }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "סגור", tint = textWhite)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = borderColor)

                    ScrollableTabRow(
                        selectedTabIndex = selectedCategoryIndex, edgePadding = 0.dp, containerColor = darkBg, contentColor = primaryColor,
                        divider = { HorizontalDivider(color = borderColor) },
                        indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]), color = primaryColor, height = 3.dp) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEachIndexed { index, (label, icon, _) ->
                            val isSelected = selectedCategoryIndex == index
                            Tab(
                                selected = isSelected, onClick = { selectedCategoryIndex = index },
                                text = { Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) primaryColor else textGray, fontSize = 12.sp, maxLines = 1) },
                                icon = { Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) primaryColor else textGray, modifier = Modifier.size(20.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        when (selectedCategoryIndex) {
                            0 -> {
                                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Palette, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("עיצוב וערכת נושא", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("בחר ערכת נושא לאפליקציה:", style = MaterialTheme.typography.bodySmall, color = textGray)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        val themesList = listOf(Triple(SettingsManager.THEME_ORANGE, "כתום גז", Color(0xFFFF8C00)), Triple(SettingsManager.THEME_BLUE, "כחול יוקרתי", Color(0xFF1565C0)), Triple(SettingsManager.THEME_GREEN, "ירוק רענן", Color(0xFF2E7D32)), Triple(SettingsManager.THEME_YELLOW, "צהוב זורח", Color(0xFFFFD600)))
                                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            themesList.forEach { (themeKey, themeLabel, colorDot) ->
                                                val isSelected = currentAppTheme == themeKey
                                                Surface(
                                                    onClick = { currentAppTheme = themeKey; settingsManager.appTheme = themeKey; onAppThemeChange?.invoke(themeKey) },
                                                    shape = RoundedCornerShape(12.dp), color = if (isSelected) selectedSurfaceBg else unselectedSurfaceBg,
                                                    border = if (isSelected) BorderStroke(1.5.dp, primaryColor) else BorderStroke(1.dp, borderColor), modifier = Modifier.width(75.dp).height(75.dp)
                                                ) {
                                                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(colorDot), contentAlignment = Alignment.Center) { if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(text = themeLabel, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) primaryColor else textGray, textAlign = TextAlign.Center, maxLines = 1)
                                                    }
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { settingsManager.isDarkMode = !settingsManager.isDarkMode; onAppThemeChange?.invoke(currentAppTheme) }.padding(vertical = 8.dp)) {
                                            Switch(checked = settingsManager.isDarkMode, onCheckedChange = { settingsManager.isDarkMode = it; onAppThemeChange?.invoke(currentAppTheme) })
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("מצב לילה (Dark Mode)", color = textWhite)
                                        }
                                    }
                                }

                                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PictureAsPdf, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("עיצוב תבנית PDF", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("בחר את סגנון המסמך שיופק ללקוח:", style = MaterialTheme.typography.bodySmall, color = textGray)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            val isClassic = currentTemplateStyle == SettingsManager.TEMPLATE_CLASSIC
                                            Surface(
                                                onClick = { currentTemplateStyle = SettingsManager.TEMPLATE_CLASSIC; settingsManager.pdfTemplateStyle = SettingsManager.TEMPLATE_CLASSIC },
                                                shape = RoundedCornerShape(12.dp), color = if (isClassic) selectedSurfaceBg else unselectedSurfaceBg,
                                                border = if (isClassic) BorderStroke(1.5.dp, primaryColor) else BorderStroke(1.dp, borderColor), modifier = Modifier.weight(1f).height(65.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(text = "קלאסי (מסורתי)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isClassic) primaryColor else textGray) }
                                            }
                                            val isModern = currentTemplateStyle == SettingsManager.TEMPLATE_MODERN
                                            Surface(
                                                onClick = { currentTemplateStyle = SettingsManager.TEMPLATE_MODERN; settingsManager.pdfTemplateStyle = SettingsManager.TEMPLATE_MODERN },
                                                shape = RoundedCornerShape(12.dp), color = if (isModern) selectedSurfaceBg else unselectedSurfaceBg,
                                                border = if (isModern) BorderStroke(1.5.dp, primaryColor) else BorderStroke(1.dp, borderColor), modifier = Modifier.weight(1f).height(65.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(text = "מודרני (חדש)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isModern) primaryColor else textGray) }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.FolderSpecial, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("תיקיית שמירה לדוחות", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "שמירה אוטומטית פעילה", fontWeight = FontWeight.Medium, color = textWhite, modifier = Modifier.weight(1f))
                                            Switch(checked = isAutoSaveEnabled, onCheckedChange = { isAutoSaveEnabled = it; settingsManager.isAutoSavePdfEnabled = it })
                                        }
                                        if (isAutoSaveEnabled) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            OutlinedButton(onClick = { folderPickerLauncher.launch(null) }, modifier = Modifier.fillMaxWidth().height(54.dp), border = BorderStroke(1.dp, primaryColor)) {
                                                Icon(Icons.Default.FolderOpen, null, tint = primaryColor); Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = if (customStorageFolderName.isEmpty()) "לחץ לבחירת תיקיית שמירה" else "תיקייה: $customStorageFolderName", color = primaryColor, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Badge, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("פרטי קבלן וטכנאי", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))

                                        OutlinedTextField(value = contractorHeader, onValueChange = { contractorHeader = it; settingsManager.contractorHeader = it }, label = { Text("שם הקבלן / חברה (חובה)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedTextField(value = contractorPhone, onValueChange = { contractorPhone = it; settingsManager.contractorPhone = it }, label = { Text("טלפון נייד ליצירת קשר (חובה)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedTextField(value = defaultTechnicianName, onValueChange = { defaultTechnicianName = it; settingsManager.defaultTechnicianName = it }, label = { Text("שם טכנאי גז מבצע (חובה)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors)

                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(color = borderColor)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Pin, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("מספר טופס שוטף", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("המספר ממנו המערכת תתחיל לרוץ אוטומטית. (חובה)", style = MaterialTheme.typography.bodySmall, color = textGray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = currentFormNumberInput,
                                            onValueChange = { newValue ->
                                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                                    currentFormNumberInput = newValue
                                                    val num = newValue.toIntOrNull() ?: 0
                                                    settingsManager.currentFormNumber = num
                                                }
                                            },
                                            label = { Text("מספר התחלתי (לדוגמה: 1000)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors
                                        )
                                    }
                                }
                                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("צילום רישיון טכנאי מהגלריה (חובה)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        SignaturePad(title = "בחר צילום רישיון (יופיע כחתימה בדוחות):", initialSignatureUri = savedLicenseUri, onSignatureSaved = { licUri -> val newUri = licUri.ifEmpty { null }; savedLicenseUri = newUri ?: ""; settingsManager.technicianLicenseUri = newUri })
                                    }
                                }
                            }
                            3 -> {
                                // כרטיסיית רישיון גפ"מ - עדכון מס' רישיון, תוקף ורמה וסנכרון לשרת!
                                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VerifiedUser, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ניהול רישיון טכנאי גפ\"מ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        }
                                        Text("כאן תוכל לעדכן את מספר הרישיון ותוקפו. העדכון יסונכרן אוטומטית למערכת הניהול.", fontSize = 12.sp, color = textGray)

                                        Spacer(modifier = Modifier.height(4.dp))

                                        OutlinedTextField(
                                            value = technicianLicenseNumber,
                                            onValueChange = { technicianLicenseNumber = it; settingsManager.technicianLicenseNumber = it },
                                            label = { Text("מספר רישיון טכנאי גז") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )

                                        OutlinedTextField(
                                            value = technicianLicenseExpiry,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("תוקף הרישיון (לחץ לעדכון)") },
                                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = primaryColor) },
                                            interactionSource = dateInteractionSource,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = textFieldColors
                                        )

                                        Text("רמת טכנאי:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textWhite, modifier = Modifier.padding(top = 8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(selected = technicianLevel == "רמה 1", onClick = { technicianLevel = "רמה 1"; settingsManager.technicianLevel = "רמה 1" })
                                                Text("רמה 1", fontSize = 14.sp, color = textWhite)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(selected = technicianLevel == "רמה 2", onClick = { technicianLevel = "רמה 2"; settingsManager.technicianLevel = "רמה 2" })
                                                Text("רמה 2", fontSize = 14.sp, color = textWhite)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = {
                                                if (technicianLicenseNumber.isBlank() || technicianLicenseExpiry.isBlank()) {
                                                    Toast.makeText(context, "נא למלא מספר רישיון ותוקף", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    scope.launch {
                                                        try {
                                                            val profile = TechnicianProfile(
                                                                device_id = androidId,
                                                                full_name = defaultTechnicianName,
                                                                license_number = technicianLicenseNumber,
                                                                license_expiry = technicianLicenseExpiry,
                                                                technician_level = technicianLevel,
                                                                is_blocked = false
                                                            )
                                                            SupabaseManager.client.postgrest["technicians_profiles"].upsert(profile)
                                                            Toast.makeText(context, "✓ פרטי הרישיון עודכנו וסונכרנו לשרת בהצלחה!", Toast.LENGTH_LONG).show()
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                            Toast.makeText(context, "נשמר מקומית, אך אירעה שגיאה בסנכרון לשרת.", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().height(44.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                        ) {
                                            Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("סנכרן רישיון מול השרת", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            4 -> {
                                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("המנוי שלי \uD83D\uDC51", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryColor, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                        Spacer(modifier = Modifier.height(16.dp))

                                        if (isLicensed) {
                                            Surface(color = Color(0xFF1B5E20), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text("משתמש רשום במערכת", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                                        Text("תקופת הניסיון בוטלה לצמיתות. המערכת פתוחה לשימוש מלא.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFA5D6A7), fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        } else {
                                            val formsLeft = 30 - trialFormsCount
                                            val displayFormsLeft = if (formsLeft < 0) 0 else formsLeft

                                            Surface(color = warningBg, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Info, null, tint = warningIcon, modifier = Modifier.size(32.dp))
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text("סטטוס: חשבון ניסיון", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = warningIcon)
                                                        Text("נותרו לך $displayFormsLeft טפסים ליצירה (מתוך 30)", style = MaterialTheme.typography.bodySmall, color = warningText, fontSize = 12.sp)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(24.dp))

                                            Button(
                                                onClick = { showRegistrationDialog = true },
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White)
                                            ) {
                                                Text("התחבר / הרשם להסרת הגבלות", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            5 -> {
                                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("נעילת אפליקציה (PIN)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text("נעילה בקוד PIN פעילה", fontWeight = FontWeight.Medium, color = textWhite, modifier = Modifier.weight(1f))
                                            Switch(checked = isPinEnabled, onCheckedChange = { checked -> if (checked) { if (settingsManager.pinCode.isNullOrEmpty()) showSetPinDialog = true else { isPinEnabled = true; settingsManager.isPinEnabled = true } } else showPinWarningAlert = true }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val hasFolder = !settingsManager.customStorageTreeUri.isNullOrBlank()
                            val hasContractorName = settingsManager.contractorHeader.isNotBlank()
                            val hasContractorPhone = settingsManager.contractorPhone.isNotBlank()
                            val hasTechName = settingsManager.defaultTechnicianName.isNotBlank()
                            val hasFormNumber = settingsManager.currentFormNumber > 0
                            val hasLicensePhoto = !settingsManager.technicianLicenseUri.isNullOrBlank()

                            val isAllValid = hasFolder && hasContractorName && hasContractorPhone && hasTechName && hasFormNumber && hasLicensePhoto

                            if (!isAllValid) {
                                val missing = mutableListOf<String>()
                                if (!hasFolder) missing.add("• תיקיית שמירה לדוחות (בלשונית אחסון)")
                                if (!hasContractorName) missing.add("• שם קבלן / חברה (בלשונית קבלן)")
                                if (!hasContractorPhone) missing.add("• מספר טלפון נייד (בלשונית קבלן)")
                                if (!hasTechName) missing.add("• שם טכנאי (בלשונית קבלן)")
                                if (!hasFormNumber) missing.add("• מספר טופס התחלתי (בלשונית קבלן)")
                                if (!hasLicensePhoto) missing.add("• צילום רישיון מהגלריה (בלשונית קבלן)")
                                Toast.makeText(context, "חובה להגדיר את השדות הבאים להתחלת עבודה:\n" + missing.joinToString("\n"), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "ההגדרות נשמרו בהצלחה", Toast.LENGTH_SHORT).show()
                                onDismissRequest()
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "שמור", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("שמירת הגדרות וסגירה", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        if (showRegistrationDialog) {
            SettingsAuthDialog(
                onDismiss = { showRegistrationDialog = false },
                onSuccess = {
                    isLicensed = true
                    appSecurityPrefs.edit().putBoolean("is_licensed_user", true).apply()
                    showRegistrationDialog = false
                    Toast.makeText(context, "נרשמת למערכת בהצלחה! ההגבלה הוסרה.", Toast.LENGTH_LONG).show()
                }
            )
        }

        // תאריכון לעדכון תוקף הרישיון מתוך חלון ההגדרות
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            technicianLicenseExpiry = sdf.format(Date(millis))
                            settingsManager.technicianLicenseExpiry = technicianLicenseExpiry
                        }
                    }) { Text("אישור") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("ביטול") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Suppress("UnusedPrivateMember", "UNUSED_PARAMETER")
@Composable
private fun SupabaseCloudCard(
    context: Context,
    cardBg: Color,
    primaryColor: Color,
    textWhite: Color,
    textGray: Color
) {
    val supabaseManager = remember { SupabaseManager(context) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("שומר סף ורישיונות Supabase (SaaS)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("מודל Zero-Storage: הנתונים והטפסים נשמרים מקומית בבטחה במכשיר בלבד.", fontSize = 11.sp, color = textGray)

            if (statusText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(statusText!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    isChecking = true
                    statusText = "בודק חיבור ל-Supabase Cloud..."
                    supabaseManager.testConnection { _, message ->
                        isChecking = false
                        statusText = message
                    }
                },
                enabled = !isChecking,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White)
            ) {
                if (isChecking) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("בדוק חיבור ל-Supabase Cloud ☁️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAuthDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE) }
    var isSignUpMode by remember { mutableStateOf(false) }

    val androidId = remember { android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "UNKNOWN_DEVICE" }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(prefs.getBoolean("remember_me", false)) }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSignUpMode) "הרשמה למערכת" else "התחברות למערכת",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (isSignUpMode) {
                    OutlinedTextField(
                        value = firstName, onValueChange = { firstName = it },
                        label = { Text("שם פרטי") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = lastName, onValueChange = { lastName = it },
                        label = { Text("שם משפחה") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("אימייל") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("סיסמה (לפחות 6 תווים)") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End
                ) {
                    Text("שמור אימייל להתחברות", fontSize = 14.sp)
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank() || (isSignUpMode && (firstName.isBlank() || lastName.isBlank()))) {
                                errorMessage = "נא למלא את כל השדות"
                                return@Button
                            }
                            if (password.length < 6) {
                                errorMessage = "הסיסמה חייבת להכיל לפחות 6 תווים"
                                return@Button
                            }
                            scope.launch {
                                isLoading = true; errorMessage = null
                                try {
                                    if (isSignUpMode) {
                                        SupabaseManager.client.auth.signUpWith(Email) { this.email = email.trim(); this.password = password }
                                        val user = SupabaseManager.client.auth.currentUserOrNull()
                                        if (user != null) {
                                            SupabaseManager.client.postgrest["profiles"].update(ProfileUpdate(first_name = firstName.trim(), last_name = lastName.trim(), device_id = androidId)) { filter { eq("id", user.id) } }
                                            val profile = SupabaseManager.client.postgrest["profiles"].select { filter { eq("id", user.id) } }.decodeSingleOrNull<UserProfile>()

                                            if (profile?.status == "active") {
                                                if (rememberMe) prefs.edit().putBoolean("remember_me", true).putString("email", email.trim()).apply()
                                                else prefs.edit().clear().apply()
                                                onSuccess()
                                            } else errorMessage = "הרישיון אינו פעיל."
                                        } else errorMessage = "שגיאה ביצירת המשתמש."
                                    } else {
                                        SupabaseManager.client.auth.signInWith(Email) { this.email = email.trim(); this.password = password }
                                        val user = SupabaseManager.client.auth.currentUserOrNull()
                                        if (user != null) {
                                            val profile = SupabaseManager.client.postgrest["profiles"].select { filter { eq("id", user.id) } }.decodeSingleOrNull<UserProfile>()

                                            if (profile != null && profile.status == "active") {
                                                if (profile.device_id.isNullOrEmpty()) {
                                                    SupabaseManager.client.postgrest["profiles"].update(com.example.myapplication158.util.DeviceUpdate(device_id = androidId)) { filter { eq("id", user.id) } }
                                                    if (rememberMe) prefs.edit().putBoolean("remember_me", true).putString("email", email.trim()).apply()
                                                    else prefs.edit().clear().apply()
                                                    onSuccess()
                                                } else if (profile.device_id == androidId) {
                                                    if (rememberMe) prefs.edit().putBoolean("remember_me", true).putString("email", email.trim()).apply()
                                                    else prefs.edit().clear().apply()
                                                    onSuccess()
                                                } else {
                                                    SupabaseManager.client.auth.signOut()
                                                    errorMessage = "חשבון זה משויך למכשיר אחר. אנא פנה להנהלה."
                                                }
                                            } else {
                                                errorMessage = "הרישיון שלך אינו פעיל."
                                            }
                                        } else errorMessage = "שגיאה בזיהוי המשתמש."
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorMessage = "שגיאה: ${e.message ?: "לא ידועה"}"
                                } finally { isLoading = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isSignUpMode) "הרשם" else "התחבר", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { isSignUpMode = !isSignUpMode; errorMessage = null }) {
                    Text(if (isSignUpMode) "יש לך כבר חשבון? התחבר" else "אין לך חשבון? הירשם כאן")
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("ביטול", color = Color.Gray) }
            }
        }
    }
}