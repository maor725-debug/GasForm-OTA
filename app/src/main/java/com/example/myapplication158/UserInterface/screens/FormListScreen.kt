package com.example.myapplication158.UserInterface.screens

import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.example.myapplication158.data.GasFormD2
import com.example.myapplication158.data.GasFormD3
import com.example.myapplication158.data.GasFormD4
import com.example.myapplication158.data.WorkOrder
import com.example.myapplication158.data.WaiverForm
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.UserInterface.components.FinancialReportDialog
import com.example.myapplication158.UserInterface.components.FormListItemAiStyle
import com.example.myapplication158.UserInterface.components.PricingDialog
import com.example.myapplication158.UserInterface.components.WorkOrderDialog
import com.example.myapplication158.UserInterface.components.WorkOrdersListDialog
import com.example.myapplication158.util.AppLogger
import com.example.myapplication158.util.SettingsManager
import com.example.myapplication158.util.SupabaseManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class SupportTicket(
    val device_id: String,
    val category: String,
    val message: String,
    val system_logs: String? = null
)

@Composable
fun FormListScreen(
    viewModel: GasFormViewModel,
    onAddNormativeForm: () -> Unit,
    onAddOtherForms: () -> Unit,
    onEditForm: (GasForm) -> Unit,
    onEditPeriodicForm: (PeriodicGasForm) -> Unit = {},
    onEditD2Form: (GasFormD2) -> Unit = {},
    onEditD3Form: (GasFormD3) -> Unit = {},
    onEditD4Form: (GasFormD4) -> Unit = {},
    onEditWaiverForm: (WaiverForm) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val forms by viewModel.allForms.collectAsState()
    val periodicForms by viewModel.allPeriodicForms.collectAsState()
    val d2Forms by viewModel.allD2Forms.collectAsState()
    val d3Forms by viewModel.allD3Forms.collectAsState()
    val d4Forms by viewModel.allD4Forms.collectAsState()
    val waiverForms by viewModel.allWaiverForms.collectAsState()

    val combinedForms = remember(forms, periodicForms, d2Forms, d3Forms, d4Forms, waiverForms) {
        val list = mutableListOf<Any>()
        list.addAll(forms)
        list.addAll(periodicForms)
        list.addAll(d2Forms)
        list.addAll(d3Forms)
        list.addAll(d4Forms)
        list.addAll(waiverForms)
        list.sortedByDescending {
            when (it) {
                is GasForm -> it.createdAt
                is PeriodicGasForm -> it.createdAt
                is GasFormD2 -> it.createdAt
                is GasFormD3 -> it.createdAt
                is GasFormD4 -> it.createdAt
                is WaiverForm -> it.id.toLong()
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

    var showWorkOrdersListDialog by remember { mutableStateOf(false) }
    var showWorkOrderCreateDialog by remember { mutableStateOf(false) }
    var editingWorkOrder by remember { mutableStateOf<WorkOrder?>(null) }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedForms by remember { mutableStateOf(setOf<Any>()) }

    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val androidId = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_DEVICE" }

    var showSupportDialog by remember { mutableStateOf(false) }
    var supportCategory by remember { mutableStateOf("דיווח על באג / תקלה") }
    var supportMessage by remember { mutableStateOf("") }
    var isSubmittingSupport by remember { mutableStateOf(false) }

    var isSetupComplete by remember { mutableStateOf(true) }
    var missingFieldsText by remember { mutableStateOf("") }
    var suppressOnboarding by remember { mutableStateOf(false) }

    fun checkSetup() {
        val hasFolder = !settingsManager.customStorageTreeUri.isNullOrBlank()
        val hasContractorName = !settingsManager.contractorHeader.isNullOrBlank()
        val hasContractorPhone = !settingsManager.contractorPhone.isNullOrBlank()
        val hasTechName = !settingsManager.defaultTechnicianName.isNullOrBlank()
        val hasFormNumber = settingsManager.currentFormNumber > 0
        val hasLicensePhoto = !settingsManager.technicianLicenseUri.isNullOrBlank()

        isSetupComplete = hasFolder && hasContractorName && hasContractorPhone && hasTechName && hasFormNumber && hasLicensePhoto

        val missing = mutableListOf<String>()
        if (!hasFolder) missing.add("• תיקיית שמירה לדוחות")
        if (!hasContractorName) missing.add("• שם קבלן / חברה")
        if (!hasContractorPhone) missing.add("• מס' טלפון נייד קבלן")
        if (!hasTechName) missing.add("• שם טכנאי גז מבצע")
        if (!hasFormNumber) missing.add("• מספר טופס שוטף התחלתי")
        if (!hasLicensePhoto) missing.add("• צילום רישיון טכנאי מהגלריה")
        missingFieldsText = missing.joinToString("\n")
    }

    val lifecycleOwner = LocalLifecycleOwner.current
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
    val d2PrimaryColor = Color(0xFF2196F3)
    val d3PrimaryColor = Color(0xFFFF9800)
    val d4PrimaryColor = Color(0xFF673AB7)
    val waiverPrimaryColor = Color(0xFFE65100)

    val aiBgColor = if (isDark) Color(0xFF121212) else Color(0xFFFAFAFA)
    val aiHeaderBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val aiHeaderTextColor = if (isDark) Color.White else Color(0xFF1E1E1E)
    val aiCardBg = if (isDark) Color(0xFF242424) else Color.White
    val aiBorderColor = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
    val aiTextColor = if (isDark) Color.White else Color(0xFF212121)
    val aiTextGray = if (isDark) Color(0xFFAAAAAA) else Color(0xFF757575)

    // חישוב טפסים
    val approvedForms = combinedForms.count {
        when(it) {
            is GasForm -> it.isSavedToTarget
            is PeriodicGasForm -> it.isSavedToTarget
            is GasFormD2 -> it.isSavedToTarget
            is GasFormD3 -> it.isSavedToTarget
            is GasFormD4 -> it.isSavedToTarget
            is WaiverForm -> it.isSavedToTarget
            else -> false
        }
    }
    val pendingForms = combinedForms.size - approvedForms


    val filteredForms = combinedForms.filter { item ->
        when (item) {
            is GasForm -> item.clientName.contains(searchQuery, ignoreCase = true) || item.clientCity.contains(searchQuery, ignoreCase = true) || item.date.contains(searchQuery, ignoreCase = true) || item.partnerNumber.contains(searchQuery, ignoreCase = true)
            is PeriodicGasForm -> item.businessName.contains(searchQuery, ignoreCase = true) || item.clientName.contains(searchQuery, ignoreCase = true) || item.sequentialNumber.toString().contains(searchQuery, ignoreCase = true)
            is GasFormD2 -> item.businessName.contains(searchQuery, ignoreCase = true) || item.clientName.contains(searchQuery, ignoreCase = true) || item.sequentialNumber.toString().contains(searchQuery, ignoreCase = true)
            is GasFormD3 -> item.businessName.contains(searchQuery, ignoreCase = true) || item.clientName.contains(searchQuery, ignoreCase = true) || item.sequentialNumber.toString().contains(searchQuery, ignoreCase = true)
            is GasFormD4 -> item.businessName.contains(searchQuery, ignoreCase = true) || item.clientName.contains(searchQuery, ignoreCase = true) || item.sequentialNumber.toString().contains(searchQuery, ignoreCase = true)
            is WaiverForm -> item.clientName.contains(searchQuery, ignoreCase = true) || item.address.contains(searchQuery, ignoreCase = true)
            else -> false
        }
    }

    val handleItemClick: (Any, () -> Unit) -> Unit = { item, defaultAction ->
        if (isSelectionMode) {
            selectedForms = if (selectedForms.contains(item)) selectedForms - item else selectedForms + item
            if (selectedForms.isEmpty()) isSelectionMode = false
        } else {
            defaultAction()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = aiBgColor,
            topBar = {
                if (isSelectionMode) {
                    Surface(color = primaryColor, modifier = Modifier.fillMaxWidth(), shadowElevation = 4.dp) {
                        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isSelectionMode = false; selectedForms = emptySet() }) {
                                Icon(Icons.Default.Close, "בטל", tint = Color.White)
                            }
                            Text("${selectedForms.size} טפסים נבחרו", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = {
                                if (selectedForms.size < 2) {
                                    Toast.makeText(context, "יש לבחור לפחות 2 טפסים למיזוג", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "ממזג ${selectedForms.size} טפסים, אנא המתן...", Toast.LENGTH_LONG).show()
                                    viewModel.mergeAndSharePdfs(context, selectedForms.toList()) {
                                        isSelectionMode = false
                                        selectedForms = emptySet()
                                    }
                                }
                            }) { Text("מזג ל-PDF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                        }
                    }
                } else {
                    Surface(color = aiHeaderBg, modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Settings, "הגדרות", tint = primaryColor, modifier = Modifier.size(24.dp).clickable { settingsInitialTab = 0; showSettingsDialog = true })
                                Icon(Icons.Default.LibraryAddCheck, "בחירה למיזוג", tint = primaryColor, modifier = Modifier.size(24.dp).clickable { isSelectionMode = true })

                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showSupportDialog = true }) {
                                    Icon(Icons.Default.SupportAgent, "תמיכה", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                                    Text("תמיכה טכנית", color = Color(0xFF4CAF50), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // ---> תיקון משימה 1: הצגת שם קבלן דינמי במקום טקסט קבוע <---
                            Column(horizontalAlignment = Alignment.End) {
                                Text("מערכת מילוי טפסים", fontWeight = FontWeight.ExtraBold, color = aiHeaderTextColor, fontSize = 16.sp)
                                val headerText = settingsManager.contractorHeader.takeIf { !it.isNullOrBlank() } ?: "שם קבלן / חברה"
                                Text(headerText, color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            modifier = modifier.fillMaxSize().navigationBarsPadding()
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).background(aiBgColor),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = searchQuery, onValueChange = { searchQuery = it },
                            placeholder = { Text("חפש לפי שם לקוח, ישוב, מס' טופס...", textAlign = TextAlign.Right, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = aiTextGray, modifier = Modifier.size(20.dp)) },
                            trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null, tint = aiTextGray, modifier = Modifier.size(20.dp)) } },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 50.dp),
                            shape = RoundedCornerShape(24.dp), singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = aiCardBg, unfocusedContainerColor = aiCardBg, focusedBorderColor = primaryColor, unfocusedBorderColor = aiBorderColor, focusedTextColor = aiTextColor, unfocusedTextColor = aiTextColor, cursorColor = primaryColor)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).height(85.dp),
                                colors = CardDefaults.cardColors(containerColor = if(isDark) Color(0xFF3E2723) else Color(0xFFFFF3E0)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha=0.5f))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.height(2.dp))
                                    Text("טפסים בהמתנה", color = if(isDark) Color.LightGray else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(pendingForms.toString(), color = Color(0xFFE65100), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f).height(85.dp),
                                colors = CardDefaults.cardColors(containerColor = if(isDark) Color(0xFF1B5E20) else Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha=0.5f))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.height(2.dp))
                                    Text("טפסים מאושרים", color = if(isDark) Color.LightGray else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(approvedForms.toString(), color = Color(0xFF2E7D32), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onAddNormativeForm() },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))).padding(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("טופס חדש", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("התחל למלא טופס בדיקה נורמטיבי\n(ת\"י 158)", color = Color.White.copy(alpha=0.9f), fontSize = 14.sp, lineHeight = 20.sp)
                                    }

                                    Button(
                                        onClick = { onAddNormativeForm() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1976D2)),
                                        shape = RoundedCornerShape(24.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text("התחל", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, null, tint = aiTextColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("טפסים אחרונים", color = aiTextColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (filteredForms.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF5F7F9)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, aiBorderColor)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Description, tint = Color.LightGray, modifier = Modifier.size(56.dp), contentDescription = null)
                                    Spacer(Modifier.height(16.dp))
                                    Text("אין טפסים להצגה", fontWeight = FontWeight.Bold, color = aiHeaderTextColor, fontSize = 18.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text("טפסים שיופיעו כאן לאחר מילוי", color = aiTextGray, fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        items(filteredForms) { form ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(start = if (isSelectionMode) 16.dp else 0.dp)) {
                                if (isSelectionMode) {
                                    Checkbox(
                                        checked = selectedForms.contains(form),
                                        onCheckedChange = { checked -> selectedForms = if (checked) selectedForms + form else selectedForms - form },
                                        colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    when (form) {
                                        is GasForm -> FormListItemAiStyle(form = form, onEdit = { handleItemClick(form) { onEditForm(form) } }, onPreview = { viewModel.previewPdf(context, form) }, onShare = { viewModel.sharePdf(context, form) }, onDelete = { showDeleteConfirmDialog = form }, onPricingClick = { showPricingDialog = form }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, aiTextGray = aiTextGray, primaryColor = primaryColor, aiBorderColor = aiBorderColor)
                                        is PeriodicGasForm -> PeriodicFormListItemAiStyle(form = form, onEdit = { handleItemClick(form) { onEditPeriodicForm(form) } }, onPreview = { viewModel.previewPeriodicPdf(context, form) }, onShare = { viewModel.sharePeriodicPdf(context, form) }, onDelete = { showDeleteConfirmDialog = form }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, aiTextGray = aiTextGray, primaryColor = Color(0xFF4CAF50), aiBorderColor = aiBorderColor)
                                        is GasFormD2 -> D2FormListItemAiStyle(form = form, onEdit = { handleItemClick(form) { onEditD2Form(form) } }, onPreview = { viewModel.previewPdfD2(context, form) }, onShare = { viewModel.sharePdfD2(context, form) {} }, onDelete = { showDeleteConfirmDialog = form }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, aiTextGray = aiTextGray, primaryColor = d2PrimaryColor, aiBorderColor = aiBorderColor)
                                        is GasFormD3 -> D3FormListItemAiStyle(form = form, onEdit = { handleItemClick(form) { onEditD3Form(form) } }, onPreview = { viewModel.previewPdfD3(context, form) }, onShare = { viewModel.sharePdfD3(context, form) {} }, onDelete = { showDeleteConfirmDialog = form }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, aiTextGray = aiTextGray, primaryColor = d3PrimaryColor, aiBorderColor = aiBorderColor)
                                        is GasFormD4 -> D4FormListItemAiStyle(form = form, onEdit = { handleItemClick(form) { onEditD4Form(form) } }, onPreview = { viewModel.previewPdfD4(context, form) }, onShare = { viewModel.sharePdfD4(context, form) {} }, onDelete = { showDeleteConfirmDialog = form }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, aiTextGray = aiTextGray, primaryColor = d4PrimaryColor, aiBorderColor = aiBorderColor)
                                        is WaiverForm -> WaiverFormListItemAiStyle(form = form, onEdit = { handleItemClick(form) { onEditWaiverForm(form) } }, onPreview = { viewModel.previewWaiverPdf(context, form) }, onShare = { viewModel.shareWaiverPdf(context, form) {} }, onDelete = { showDeleteConfirmDialog = form }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, aiTextGray = aiTextGray, primaryColor = waiverPrimaryColor, aiBorderColor = aiBorderColor)
                                    }
                                }
                            }
                        }
                    }
                }

                if (!isSelectionMode) {
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(
                            Brush.verticalGradient(colors = listOf(Color.Transparent, aiBgColor.copy(alpha = 0.9f), aiBgColor))
                        ).padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onAddOtherForms() },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("טפסים נוספים", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, modifier = Modifier.size(12.dp))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                FloatingActionButton(
                                    onClick = { showReportDialog = true },
                                    containerColor = Color.White,
                                    contentColor = primaryColor,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp),
                                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                                ) {
                                    Icon(Icons.Default.BarChart, "הפקת דוח", modifier = Modifier.size(24.dp))
                                }

                                FloatingActionButton(
                                    onClick = { showWorkOrdersListDialog = true },
                                    containerColor = primaryColor,
                                    contentColor = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.size(56.dp),
                                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                                ) {
                                    Icon(Icons.Default.EditCalendar, "יומן עבודה", modifier = Modifier.size(28.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "פותח ע\"י", fontSize = 12.sp, color = aiTextGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "A.S.I", fontSize = 12.sp, color = aiTextGray, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Dialogs
            if (showSupportDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isSubmittingSupport) showSupportDialog = false },
                    containerColor = aiCardBg,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text("פנייה למפתח המערכת", color = aiTextColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = primaryColor)
                        }
                    },
                    text = {
                        Column {
                            Text("במה נוכל לעזור?", color = aiTextColor, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Right)
                            val categories = listOf("דיווח על באג / תקלה", "הצעת ייעול או פיצ'ר חדש", "שאלה כללית")
                            categories.forEach { cat ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { supportCategory = cat }.padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(cat, color = aiTextColor)
                                    Spacer(Modifier.width(8.dp))
                                    RadioButton(selected = supportCategory == cat, onClick = { supportCategory = cat }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = supportMessage, onValueChange = { supportMessage = it },
                                label = { Text("פרט ככל הניתן את פנייתך כאן...", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = aiBorderColor, focusedTextColor = aiTextColor, unfocusedTextColor = aiTextColor),
                                maxLines = 5, textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (supportMessage.isBlank()) { Toast.makeText(context, "אנא הזן את תוכן הפנייה", Toast.LENGTH_SHORT).show(); return@Button }
                                isSubmittingSupport = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val logsToAttach = if (supportCategory == "דיווח על באג / תקלה") AppLogger.getLogsForLastWeek(context) else null
                                        val ticket = SupportTicket(device_id = androidId, category = supportCategory, message = supportMessage, system_logs = logsToAttach)
                                        SupabaseManager.client.postgrest["support_tickets"].insert(ticket)
                                        withContext(Dispatchers.Main) { Toast.makeText(context, "הפנייה נשלחה בהצלחה!", Toast.LENGTH_LONG).show(); showSupportDialog = false; supportMessage = ""; isSubmittingSupport = false }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        withContext(Dispatchers.Main) { Toast.makeText(context, "שגיאה בשליחת הפנייה. נסה שוב.", Toast.LENGTH_SHORT).show(); isSubmittingSupport = false }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor), enabled = !isSubmittingSupport
                        ) {
                            if (isSubmittingSupport) { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp) }
                            else { Row(verticalAlignment = Alignment.CenterVertically) { Text("שלח פנייה", fontWeight = FontWeight.Bold); Spacer(Modifier.width(4.dp)); Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp)) } }
                        }
                    },
                    dismissButton = { TextButton(onClick = { showSupportDialog = false }, enabled = !isSubmittingSupport) { Text("ביטול", color = aiTextGray) } }
                )
            }

            if (!isSetupComplete && !suppressOnboarding) {
                AlertDialog(
                    onDismissRequest = { }, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                    containerColor = aiCardBg, titleContentColor = primaryColor, textContentColor = aiTextColor,
                    icon = { Icon(Icons.Default.Warning, null, tint = primaryColor, modifier = Modifier.size(36.dp)) },
                    title = { Text("הגדרות חובה חסרות", fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { Text("כדי להתחיל לעבוד עם המערכת עליך להגדיר:", textAlign = TextAlign.Center, fontSize = 14.sp); Spacer(Modifier.height(12.dp)); Text(missingFieldsText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFF5252), textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) } },
                    confirmButton = { Button(onClick = { suppressOnboarding = true; settingsInitialTab = 2; showSettingsDialog = true }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) { Text("הגדר עכשיו", fontWeight = FontWeight.Bold, color = Color.White) } }
                )
            }

            if (showReportDialog) { FinancialReportDialog(forms = forms, onDismiss = { showReportDialog = false }, onGenerate = { showReportDialog = false }, aiCardBg = aiCardBg, aiTextColor = aiTextColor, primaryColor = primaryColor, aiBorderColor = aiBorderColor, aiTextGray = aiTextGray) }
            showPricingDialog?.let { form -> PricingDialog(form = form, onDismiss = { showPricingDialog = null }, onSave = { updatedForm -> viewModel.saveCurrentForm(updatedForm) { showPricingDialog = null } }, surfaceColor = aiCardBg, primaryColor = primaryColor, textColor = aiTextColor, borderColor = aiBorderColor) }

            showDeleteConfirmDialog?.let { form ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null }, containerColor = aiCardBg,
                    confirmButton = { Button(onClick = { when (form) { is GasForm -> viewModel.deleteForm(form); is PeriodicGasForm -> viewModel.deletePeriodicForm(form); is GasFormD2 -> viewModel.deleteFormD2(form); is GasFormD3 -> viewModel.deleteFormD3(form); is GasFormD4 -> viewModel.deleteFormD4(form); is WaiverForm -> viewModel.deleteWaiverForm(form) }; showDeleteConfirmDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))) { Text("מחק") } },
                    dismissButton = { TextButton(onClick = { showDeleteConfirmDialog = null }) { Text("ביטול", color = aiTextGray) } },
                    title = { Text("מחיקה", color = aiTextColor, fontWeight = FontWeight.Bold) },
                    text = { Text("האם אתה בטוח שברצונך למחוק את הטופס?", color = aiTextColor) }
                )
            }

            if (showSettingsDialog) {
                val act = LocalActivity.current
                SettingsDialog(onDismissRequest = { showSettingsDialog = false; settingsInitialTab = 0 }, onDismiss = { showSettingsDialog = false; settingsInitialTab = 0 }, onAppThemeChange = { act?.recreate() }, viewModel = viewModel, initialCategoryIndex = settingsInitialTab)
            }

            if (showWorkOrdersListDialog) {
                WorkOrdersListDialog(
                    viewModel = viewModel, onDismiss = { showWorkOrdersListDialog = false },
                    onAddNewWorkOrder = { editingWorkOrder = null; showWorkOrderCreateDialog = true },
                    onEditWorkOrder = { item -> editingWorkOrder = item; showWorkOrderCreateDialog = true },
                    onGenerateNormativeForm = { prefilledForm -> showWorkOrdersListDialog = false; onEditForm(prefilledForm) },
                    onGeneratePeriodicForm = { prefilledPeriodic -> showWorkOrdersListDialog = false; onEditPeriodicForm(prefilledPeriodic) }
                )
            }

            if (showWorkOrderCreateDialog) {
                WorkOrderDialog(initialWorkOrder = editingWorkOrder, onDismiss = { showWorkOrderCreateDialog = false; editingWorkOrder = null }, onSave = { order -> viewModel.saveWorkOrder(order) { showWorkOrderCreateDialog = false; editingWorkOrder = null; Toast.makeText(context, "העבודה נשמרה בהצלחה ביומן", Toast.LENGTH_SHORT).show() } })
            }
        }
    }
}

@Composable
private fun WaiverFormListItemAiStyle(
    form: WaiverForm, onEdit: () -> Unit, onPreview: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit, aiCardBg: Color, aiTextColor: Color, aiTextGray: Color, primaryColor: Color, aiBorderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onEdit() }, colors = CardDefaults.cardColors(containerColor = aiCardBg), border = BorderStroke(1.dp, aiBorderColor), shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Gavel, contentDescription = null, tint = primaryColor) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val title = form.clientName.takeIf { it.isNotBlank() } ?: "הסרת אחריות חדשה"
                Text(title, color = aiTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                val draftText = if (!form.isSavedToTarget) " • טיוטה" else ""
                Text("הסרת אחריות וחציבה$draftText | ${form.date}", color = aiTextGray, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Share, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Visibility, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha=0.7f), modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

@Composable
private fun PeriodicFormListItemAiStyle(
    form: PeriodicGasForm, onEdit: () -> Unit, onPreview: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit, aiCardBg: Color, aiTextColor: Color, aiTextGray: Color, primaryColor: Color, aiBorderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onEdit() }, colors = CardDefaults.cardColors(containerColor = aiCardBg), border = BorderStroke(1.dp, aiBorderColor), shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, tint = primaryColor) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val title = form.businessName.takeIf { it.isNotBlank() } ?: form.clientName.takeIf { it.isNotBlank() } ?: "טופס תקופתי חדש"
                Text(title, color = aiTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                val isDraft = !form.isSavedToTarget
                val draftText = if (isDraft) " • טיוטה" else ""
                Text("ד-1 | מרכזיה$draftText | מס' ${form.sequentialNumber} | ${form.date}", color = aiTextGray, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Share, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Visibility, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha=0.7f), modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

@Composable
private fun D2FormListItemAiStyle(
    form: GasFormD2, onEdit: () -> Unit, onPreview: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit, aiCardBg: Color, aiTextColor: Color, aiTextGray: Color, primaryColor: Color, aiBorderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onEdit() }, colors = CardDefaults.cardColors(containerColor = aiCardBg), border = BorderStroke(1.dp, aiBorderColor), shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.PropaneTank, contentDescription = null, tint = primaryColor) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val title = form.businessName.takeIf { it.isNotBlank() } ?: form.clientName.takeIf { it.isNotBlank() } ?: "דוח ד-2 חדש"
                Text(title, color = aiTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                val isDraft = !form.isSavedToTarget
                val draftText = if (isDraft) " • טיוטה" else ""
                Text("ד-2 | מכלים נייחים$draftText | מס' ${form.sequentialNumber} | ${form.date}", color = aiTextGray, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Share, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Visibility, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha=0.7f), modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

@Composable
private fun D3FormListItemAiStyle(
    form: GasFormD3, onEdit: () -> Unit, onPreview: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit, aiCardBg: Color, aiTextColor: Color, aiTextGray: Color, primaryColor: Color, aiBorderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onEdit() }, colors = CardDefaults.cardColors(containerColor = aiCardBg), border = BorderStroke(1.dp, aiBorderColor), shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Domain, contentDescription = null, tint = primaryColor) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val title = form.businessName.takeIf { it.isNotBlank() } ?: form.clientName.takeIf { it.isNotBlank() } ?: "דוח ד-3 חדש"
                Text(title, color = aiTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                val isDraft = !form.isSavedToTarget
                val draftText = if (isDraft) " • טיוטה" else ""
                Text("ד-3 | מאגר משותף$draftText | מס' ${form.sequentialNumber} | ${form.date}", color = aiTextGray, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Share, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Visibility, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha=0.7f), modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

@Composable
private fun D4FormListItemAiStyle(
    form: GasFormD4, onEdit: () -> Unit, onPreview: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit, aiCardBg: Color, aiTextColor: Color, aiTextGray: Color, primaryColor: Color, aiBorderColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onEdit() }, colors = CardDefaults.cardColors(containerColor = aiCardBg), border = BorderStroke(1.dp, aiBorderColor), shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Storage, contentDescription = null, tint = primaryColor) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val title = form.businessName.takeIf { it.isNotBlank() } ?: form.clientName.takeIf { it.isNotBlank() } ?: "דוח ד-4 חדש"
                Text(title, color = aiTextColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                val isDraft = !form.isSavedToTarget
                val draftText = if (isDraft) " • טיוטה" else ""
                Text("ד-4 | מאגר נפרד$draftText | מס' ${form.sequentialNumber} | ${form.date}", color = aiTextGray, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Share, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Visibility, null, tint = primaryColor, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha=0.7f), modifier = Modifier.size(18.dp)) }
            }
        }
    }
}