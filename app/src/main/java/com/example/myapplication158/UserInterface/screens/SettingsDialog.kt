package com.example.myapplication158.UserInterface.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.documentfile.provider.DocumentFile
import com.example.myapplication158.TechnicianProfile
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.UserInterface.components.SignaturePad
import com.example.myapplication158.util.SettingsManager
import com.example.myapplication158.util.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val categories = listOf(
        Triple("עיצוב", Icons.Default.Palette, 0),
        Triple("אחסון", Icons.Default.Cloud, 1),
        Triple("קבלן", Icons.Default.Badge, 2),
        Triple("רישיון", Icons.Default.VerifiedUser, 3),
        Triple("חשבון", Icons.Default.AccountBox, 4),
        Triple("אבטחה", Icons.Default.Security, 5)
    )

    var isAutoSaveEnabled by remember { mutableStateOf(settingsManager.isAutoSavePdfEnabled) }

    var savedLicenseUri by remember { mutableStateOf(settingsManager.technicianLicenseUri ?: "") }
    var contractorHeader by remember { mutableStateOf(settingsManager.contractorHeader) }
    var contractorPhone by remember { mutableStateOf(settingsManager.contractorPhone) }
    var defaultTechnicianName by remember { mutableStateOf(settingsManager.defaultTechnicianName) }
    var currentFormNumberInput by remember { mutableStateOf(if (settingsManager.currentFormNumber > 0) settingsManager.currentFormNumber.toString() else "") }

    var technicianLicenseNumber by remember { mutableStateOf(settingsManager.technicianLicenseNumber) }
    var technicianLicenseExpiry by remember { mutableStateOf(settingsManager.technicianLicenseExpiry) }
    var technicianLevel by remember { mutableStateOf(settingsManager.technicianLevel) }
    var technicianCity by remember { mutableStateOf(settingsManager.technicianCity) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val dateInteractionSource = remember { MutableInteractionSource() }
    if (dateInteractionSource.collectIsPressedAsState().value) { showDatePicker = true }

    var isSaving by remember { mutableStateOf(false) }

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

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let {
            viewModel?.exportBackup(context, it) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            viewModel?.importBackup(context, it) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
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
                                            OutlinedButton(onClick = { folderPickerLauncher.launch(null) }, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), border = BorderStroke(1.dp, primaryColor)) {
                                                Icon(Icons.Default.FolderOpen, null, tint = primaryColor); Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = if (customStorageFolderName.isEmpty()) "לחץ לבחירת תיקיית שמירה" else "תיקייה: $customStorageFolderName", color = primaryColor, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))
                                        HorizontalDivider(color = borderColor)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Backup, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("גיבוי ושחזור נתונים", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("גיבוי מלא של כל הדוחות והנתונים השמורים במכשיר. מומלץ לבצע גיבוי תקופתי ולשמור בטוח (למשל ב-Google Drive).", fontSize = 12.sp, color = textGray)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Button(
                                                onClick = {
                                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                    val fileName = "Normativi_Backup_${dateFormat.format(Date())}.db"
                                                    exportLauncher.launch(fileName)
                                                },
                                                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                            ) {
                                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("גבה עכשיו", fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = {
                                                    importLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*"))
                                                },
                                                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                            ) {
                                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("שחזר מגיבוי", fontWeight = FontWeight.Bold)
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
                                        Text("תמונת חתימה וחותמת טכנאי (חובה)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        SignaturePad(
                                            title = "בחר תמונה מהגלריה:",
                                            initialSignatureUri = savedLicenseUri,
                                            onSignatureSaved = { licUri -> val newUri = licUri.ifEmpty { null }; savedLicenseUri = newUri ?: ""; settingsManager.technicianLicenseUri = newUri }
                                        )
                                    }
                                }
                            }
                            3 -> {
                                Card(colors = CardDefaults.cardColors(containerColor = cardBg), shape = RoundedCornerShape(12.dp)) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VerifiedUser, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ניהול רישיון טכנאי גפ\"מ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textWhite)
                                        }
                                        Text("כאן תוכל לעדכן את פרטי הרישיון ועיר המגורים. העדכון יישמר בשרת באופן אוטומטי בעת לחיצה על לחצן השמירה למטה.", fontSize = 12.sp, color = textGray)

                                        Spacer(modifier = Modifier.height(4.dp))

                                        OutlinedTextField(
                                            value = technicianLicenseNumber,
                                            onValueChange = { technicianLicenseNumber = it },
                                            label = { Text("מספר רישיון טכנאי גז") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )

                                        OutlinedTextField(
                                            value = technicianCity,
                                            onValueChange = { technicianCity = it },
                                            label = { Text("עיר מגורים") },
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
                                                RadioButton(selected = technicianLevel == "רמה 1", onClick = { technicianLevel = "רמה 1" })
                                                Text("רמה 1", fontSize = 14.sp, color = textWhite)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(selected = technicianLevel == "רמה 2", onClick = { technicianLevel = "רמה 2" })
                                                Text("רמה 2", fontSize = 14.sp, color = textWhite)
                                            }
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
                                                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
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
                            val hasContractorName = contractorHeader.isNotBlank()
                            val hasContractorPhone = contractorPhone.isNotBlank()
                            val hasTechName = defaultTechnicianName.isNotBlank()
                            val hasFormNumber = (currentFormNumberInput.toIntOrNull() ?: 0) > 0
                            val hasLicensePhoto = savedLicenseUri.isNotBlank()
                            val hasLicenseDetails = technicianLicenseNumber.isNotBlank() && technicianLicenseExpiry.isNotBlank() && technicianCity.isNotBlank()

                            val isAllValid = hasFolder && hasContractorName && hasContractorPhone && hasTechName && hasFormNumber && hasLicensePhoto && hasLicenseDetails

                            if (!isAllValid) {
                                val missing = mutableListOf<String>()
                                if (!hasFolder) missing.add("• תיקיית שמירה לדוחות (בלשונית אחסון)")
                                if (!hasContractorName) missing.add("• שם קבלן / חברה (בלשונית קבלן)")
                                if (!hasContractorPhone) missing.add("• מספר טלפון נייד (בלשונית קבלן)")
                                if (!hasTechName) missing.add("• שם טכנאי (בלשונית קבלן)")
                                if (!hasFormNumber) missing.add("• מספר טופס התחלתי (בלשונית קבלן)")
                                if (!hasLicensePhoto) missing.add("• תמונת חתימה וחותמת (בלשונית קבלן)")
                                if (!hasLicenseDetails) missing.add("• מספר רישיון, תוקף ועיר (בלשונית רישיון)")
                                Toast.makeText(context, "חובה להגדיר את השדות הבאים להתחלת עבודה:\n" + missing.joinToString("\n"), Toast.LENGTH_LONG).show()
                            } else {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val currentProfile = SupabaseManager.client.postgrest["technicians_profiles"]
                                            .select { filter { eq("device_id", androidId) } }
                                            .decodeSingleOrNull<TechnicianProfile>()

                                        val wasBlocked = currentProfile?.is_blocked == true
                                        val adminReason = currentProfile?.block_reason ?: "ללא סיבה"
                                        val oldLog = currentProfile?.correction_log ?: ""

                                        val oldLic = settingsManager.technicianLicenseNumber
                                        val oldExp = settingsManager.technicianLicenseExpiry
                                        val oldLvl = settingsManager.technicianLevel
                                        val oldCity = settingsManager.technicianCity

                                        var newLog = oldLog
                                        if (wasBlocked) {
                                            val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                            var changes = ""
                                            if (oldLic != technicianLicenseNumber) changes += "[רישיון שונה] "
                                            if (oldExp != technicianLicenseExpiry) changes += "[תוקף שונה] "
                                            if (oldLvl != technicianLevel) changes += "[רמה שונתה] "
                                            if (oldCity != technicianCity) changes += "[עיר שונתה] "
                                            if (changes.isBlank()) changes = "[שמירה ללא שינוי נתונים]"

                                            val logEntry = "\n--- $timestamp ---\nנחסם בגין: $adminReason\nתיקון שבוצע: $changes\n"
                                            newLog += logEntry
                                        }

                                        val profile = TechnicianProfile(
                                            device_id = androidId,
                                            full_name = defaultTechnicianName,
                                            license_number = technicianLicenseNumber,
                                            license_expiry = technicianLicenseExpiry,
                                            technician_level = technicianLevel,
                                            city = technicianCity,
                                            is_blocked = false,
                                            block_reason = if (wasBlocked) "" else currentProfile?.block_reason,
                                            correction_log = newLog.trim()
                                        )
                                        SupabaseManager.client.postgrest["technicians_profiles"].upsert(profile)

                                        settingsManager.contractorHeader = contractorHeader
                                        settingsManager.contractorPhone = contractorPhone
                                        settingsManager.defaultTechnicianName = defaultTechnicianName
                                        settingsManager.technicianLicenseNumber = technicianLicenseNumber
                                        settingsManager.technicianLicenseExpiry = technicianLicenseExpiry
                                        settingsManager.technicianLevel = technicianLevel
                                        settingsManager.technicianCity = technicianCity

                                        Toast.makeText(context, "ההגדרות נשמרו וסונכרנו בהצלחה", Toast.LENGTH_SHORT).show()
                                        onDismissRequest()
                                        onDismiss()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "ההגדרות נשמרו מקומית, אך השמירה לענן נכשלה. בדוק חיבור לאינטרנט.", Toast.LENGTH_LONG).show()
                                        onDismissRequest()
                                        onDismiss()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "שמור", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("שמירת הגדרות וסגירה", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            technicianLicenseExpiry = sdf.format(Date(millis))
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