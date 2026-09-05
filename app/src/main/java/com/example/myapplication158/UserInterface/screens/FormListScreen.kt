package com.example.myapplication158.UserInterface.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication158.data.GasForm
import com.example.myapplication158.data.PeriodicGasForm
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.UserInterface.components.FinancialReportDialog
import com.example.myapplication158.UserInterface.components.FormListItemAiStyle
import com.example.myapplication158.UserInterface.components.PricingDialog
import com.example.myapplication158.util.SettingsManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormListScreen(
    viewModel: GasFormViewModel,
    onAddNormativeForm: () -> Unit,
    onAddOtherForms: () -> Unit,
    onEditForm: (GasForm) -> Unit,
    onEditPeriodicForm: (PeriodicGasForm) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val forms by viewModel.allForms.collectAsState()
    val periodicForms by viewModel.allPeriodicForms.collectAsState()

    val combinedForms = remember(forms, periodicForms) {
        val list = mutableListOf<Any>()
        list.addAll(forms)
        list.addAll(periodicForms)
        list.sortedByDescending {
            when (it) {
                is GasForm -> it.createdAt
                is PeriodicGasForm -> it.createdAt
                else -> 0L
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf<Any?>(null) }
    var showPricingDialog by remember { mutableStateOf<GasForm?>(null) }

    var showReportDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var settingsInitialTab by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val activity = LocalActivity.current

    var isSetupComplete by remember { mutableStateOf(true) }
    var missingFieldsText by remember { mutableStateOf("") }
    var suppressOnboarding by remember { mutableStateOf(false) }

    fun checkSetup() {
        val hasName = !settingsManager.contractorHeader.isNullOrBlank()
        val hasSig = !settingsManager.savedSignatureUri.isNullOrBlank()
        val hasFolder = !settingsManager.customStorageTreeUri.isNullOrBlank()
        val hasFormNumber = settingsManager.currentFormNumber > 0

        isSetupComplete = hasName && hasSig && hasFolder && hasFormNumber

        val missing = mutableListOf<String>()
        if (!hasName) missing.add("• שם טכנאי/קבלן")
        if (!hasSig) missing.add("• חתימה קבועה")
        if (!hasFolder) missing.add("• תיקיית שמירה / גיבוי")
        if (!hasFormNumber) missing.add("• מספר טופס שוטף התחלתי")
        missingFieldsText = missing.joinToString("\n")
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) checkSetup()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(showSettingsDialog) {
        if (!showSettingsDialog) { checkSetup(); suppressOnboarding = false }
    }

    val isDark = settingsManager.isDarkMode
    val primaryColor = MaterialTheme.colorScheme.primary
    val aiBgColor = if (isDark) Color(0xFF0D0D0D) else Color(0xFFF4F6F8)
    val aiHeaderBg = if (isDark) {
        when (settingsManager.appTheme) {
            SettingsManager.THEME_BLUE -> Color(0xFF04132B)
            SettingsManager.THEME_GREEN -> Color(0xFF0A2711)
            SettingsManager.THEME_PURPLE -> Color(0xFF260930)
            else -> Color(0xFF381504)
        }
    } else Color.White

    val aiHeaderTextColor = if (isDark) Color.White else Color(0xFF212121)
    val aiCardBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val aiBorderColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFE0E0E0)
    val aiStatTotalBg = if (isDark) primaryColor.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.1f)
    val aiStatMonthBg = if (isDark) primaryColor.copy(alpha = 0.25f) else primaryColor.copy(alpha = 0.2f)
    val aiTextColor = if (isDark) Color.White else Color(0xFF212121)
    val aiTextGray = if (isDark) Color(0xFFAAAAAA) else Color(0xFF757575)
    val statLabelColor = if (isDark) Color.LightGray else Color.DarkGray

    val currentMonth = SimpleDateFormat("-MM-yyyy", Locale.getDefault()).format(Date())

    val totalThisMonth = combinedForms.count {
        when (it) {
            is GasForm -> it.date.contains(currentMonth)
            is PeriodicGasForm -> it.date.contains(currentMonth)
            else -> false
        }
    }

    val filteredForms = combinedForms.filter { item ->
        when (item) {
            is GasForm -> {
                item.clientName.contains(searchQuery, ignoreCase = true) ||
                        item.clientCity.contains(searchQuery, ignoreCase = true) ||
                        item.date.contains(searchQuery, ignoreCase = true) ||
                        item.partnerNumber.contains(searchQuery, ignoreCase = true)
            }
            is PeriodicGasForm -> {
                item.businessName.contains(searchQuery, ignoreCase = true) ||
                        item.clientName.contains(searchQuery, ignoreCase = true) ||
                        item.city.contains(searchQuery, ignoreCase = true) ||
                        item.date.contains(searchQuery, ignoreCase = true) ||
                        item.sequentialNumber.toString().contains(searchQuery, ignoreCase = true)
            }
            else -> false
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = aiBgColor,
            topBar = {
                Surface(color = aiHeaderBg, modifier = Modifier.fillMaxWidth(), shadowElevation = 4.dp) {
                    Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Settings, "הגדרות", tint = primaryColor, modifier = Modifier.clickable { settingsInitialTab = 0; showSettingsDialog = true }.size(22.dp))
                            Icon(if (isDark) Icons.Default.Brightness7 else Icons.Default.Brightness4, "מצב לילה/יום", tint = primaryColor, modifier = Modifier.size(22.dp).clickable { settingsManager.isDarkMode = !isDark; activity?.recreate() })
                            Icon(Icons.Default.BarChart, "דוחות", tint = primaryColor, modifier = Modifier.size(22.dp).clickable { showReportDialog = true })
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("מערכת מילוי טפסים", fontWeight = FontWeight.ExtraBold, color = aiHeaderTextColor, fontSize = 15.sp, maxLines = 1)
                            Text("מאור מנחם - קבלן עבודות גז", color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExtendedFloatingActionButton(
                        onClick = { onAddOtherForms() },
                        containerColor = aiCardBg,
                        contentColor = primaryColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp),
                        elevation = FloatingActionButtonDefaults.elevation(2.dp)
                    ) {
                        Icon(Icons.Default.ListAlt, "טפסים נוספים", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("טפסים נוספים", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    ExtendedFloatingActionButton(
                        onClick = { onAddNormativeForm() },
                        containerColor = primaryColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, "הוסף", modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("טופס נורמטיבי", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(aiBgColor)) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("חפש לפי שם לקוח, ישוב, מס' טופס...", textAlign = TextAlign.Right, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = aiTextGray, modifier = Modifier.size(20.dp)) },
                    trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null, tint = aiTextGray, modifier = Modifier.size(20.dp)) } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(24.dp), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = aiCardBg, unfocusedContainerColor = aiCardBg, focusedBorderColor = primaryColor, unfocusedBorderColor = aiBorderColor, focusedTextColor = aiTextColor, unfocusedTextColor = aiTextColor, cursorColor = primaryColor)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = aiStatTotalBg), shape = RoundedCornerShape(10.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            Text("סה\"כ טפסים", color = statLabelColor, fontSize = 11.sp); Spacer(Modifier.height(2.dp))
                            Text(combinedForms.size.toString(), color = aiTextColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = aiStatMonthBg), shape = RoundedCornerShape(10.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            Text("טפסים החודש", color = statLabelColor, fontSize = 11.sp); Spacer(Modifier.height(2.dp))
                            Text(totalThisMonth.toString(), color = aiTextColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text("מסמכים שמורים (${filteredForms.size})", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Description, null, tint = primaryColor, modifier = Modifier.size(14.dp))
                }

                if (filteredForms.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.Description, null, tint = aiBorderColor, modifier = Modifier.size(48.dp)); Spacer(Modifier.height(12.dp))
                            Text("אין טפסים להצגה", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = aiTextColor)
                        }
                    }
                    Text(
                        text = "פותח ע\"י מאור מנחם ©",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = aiTextGray
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(bottom = 80.dp, top = 4.dp)) {
                        items(filteredForms) { form ->
                            when (form) {
                                is GasForm -> {
                                    FormListItemAiStyle(form = form, onEdit = { onEditForm(form) }, onPreview = { viewModel.previewPdf(context, form) }, onShare = { viewModel.sharePdf(context, form) }, onDelete = { showDeleteConfirmDialog = form }, onPricingClick = { showPricingDialog = form }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, aiTextGray = aiTextGray, primaryColor = primaryColor, aiBorderColor = aiBorderColor)
                                }
                                is PeriodicGasForm -> {
                                    PeriodicFormListItemAiStyle(form = form, onEdit = { onEditPeriodicForm(form) }, onPreview = { viewModel.previewPeriodicPdf(context, form) }, onShare = { viewModel.sharePeriodicPdf(context, form) }, onDelete = { showDeleteConfirmDialog = form }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, aiTextGray = aiTextGray, primaryColor = Color(0xFF4CAF50), aiBorderColor = aiBorderColor)
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "פותח ע\"י מאור מנחם ©",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                color = aiTextGray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }

            if (!isSetupComplete && !suppressOnboarding) {
                AlertDialog(
                    onDismissRequest = { /* לא ניתן לסגור בלחיצה בחוץ */ },
                    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                    containerColor = aiCardBg, titleContentColor = primaryColor, textContentColor = aiTextColor,
                    icon = { Icon(Icons.Default.Warning, null, tint = primaryColor, modifier = Modifier.size(36.dp)) },
                    title = { Text("הגדרות חובה חסרות", fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("כדי להתחיל לעבוד עם המערכת עליך להגדיר:", textAlign = TextAlign.Center, fontSize = 14.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(missingFieldsText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFF5252), textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                suppressOnboarding = true
                                settingsInitialTab = 2 // שולח ישירות ללשונית "קבלן" שבה מגדירים את המספר הרץ
                                showSettingsDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("הגדר עכשיו", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                )
            }

            if (showReportDialog) { FinancialReportDialog(forms = forms, onDismiss = { showReportDialog = false }, onGenerate = { showReportDialog = false }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, primaryColor = primaryColor, aiBorderColor = aiBorderColor, aiTextGray = aiTextGray) }
            showPricingDialog?.let { form -> PricingDialog(form = form, onDismiss = { showPricingDialog = null }, onSave = { updatedForm -> viewModel.saveCurrentForm(updatedForm) { showPricingDialog = null } }, surfaceColor = aiCardBg, primaryColor = primaryColor, textColor = aiTextColor, borderColor = aiBorderColor) }

            showDeleteConfirmDialog?.let { form ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null },
                    containerColor = aiCardBg,
                    confirmButton = {
                        Button(onClick = {
                            if (form is GasForm) viewModel.deleteForm(form)
                            else if (form is PeriodicGasForm) viewModel.deletePeriodicForm(form)
                            showDeleteConfirmDialog = null
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))) { Text("מחק") }
                    },
                    dismissButton = { TextButton(onClick = { showDeleteConfirmDialog = null }) { Text("ביטול", color = aiTextGray) } },
                    title = { Text("מחיקה", color = aiTextColor, fontWeight = FontWeight.Bold) },
                    text = { Text("האם אתה בטוח שברצונך למחוק את הטופס?", color = aiTextColor) }
                )
            }

            if (showSettingsDialog) {
                val act = LocalActivity.current
                SettingsDialog(onDismissRequest = { showSettingsDialog = false; settingsInitialTab = 0 }, onDismiss = { showSettingsDialog = false; settingsInitialTab = 0 }, onAppThemeChange = { act?.recreate() }, viewModel = viewModel, initialCategoryIndex = settingsInitialTab)
            }
        }
    }
}

@Composable
private fun PeriodicFormListItemAiStyle(
    form: PeriodicGasForm,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    aiCardBg: Color,
    aiTextColor: Color,
    aiTextGray: Color,
    primaryColor: Color,
    aiBorderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = aiCardBg),
        border = BorderStroke(1.dp, aiBorderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, tint = primaryColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val title = form.businessName.takeIf { it.isNotBlank() } ?: form.clientName.takeIf { it.isNotBlank() } ?: "טופס תקופתי חדש"
                Text(title, color = aiTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text("ד-1 | תקופתי | מס' ${form.sequentialNumber} | ${form.date}", color = aiTextGray, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Share, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Visibility, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha=0.7f), modifier = Modifier.size(18.dp)) }
            }
        }
    }
}