package com.example.myapplication158.UserInterface.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication158.util.SettingsManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onCompleteOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    var currentStep by remember { mutableIntStateOf(1) }

    // Profile State Variables
    var contractorHeader by remember { mutableStateOf(settingsManager.contractorHeader.ifEmpty { "מ.מ מערכות גז" }) }
    var contractorPhone by remember { mutableStateOf(settingsManager.contractorPhone.ifEmpty { "054-6096487" }) }

    // Form Number State - Starts empty to force user input
    var startingFormNumberStr by remember { mutableStateOf(if (settingsManager.currentFormNumber > 0) settingsManager.currentFormNumber.toString() else "") }

    // License State Variables
    var technicianName by remember { mutableStateOf(settingsManager.defaultTechnicianName.ifEmpty { "מאור מנחם" }) }
    var technicianLicenseNumber by remember { mutableStateOf(settingsManager.technicianLicenseNumber) }
    var technicianLicenseExpiry by remember { mutableStateOf(settingsManager.technicianLicenseExpiry) }
    var technicianLevel by remember { mutableStateOf(settingsManager.technicianLevel.ifEmpty { "רמה 1" }) }

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    val dateInteractionSource = remember { MutableInteractionSource() }
    if (dateInteractionSource.collectIsPressedAsState().value) {
        showDatePicker = true
    }

    // Permission check
    var locationGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: locationGranted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "הגדרות ראשוניות למערכת",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Progress Indicator Bar (Updated to 3 steps)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepBadge(stepNumber = 1, title = "הרשאות", isActive = currentStep == 1, isDone = currentStep > 1)
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                StepBadge(stepNumber = 2, title = "פרופיל ורישיון", isActive = currentStep == 2, isDone = currentStep > 2)
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                StepBadge(stepNumber = 3, title = "סיום", isActive = currentStep == 3, isDone = currentStep > 3)
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (currentStep) {
                // STEP 1: PERMISSIONS & WELCOME
                1 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )

                            Text(
                                text = "ברוך הבא למערכת הוצאת דוחות בדיקה לפי תקן 158 חלק 4!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "לפני שנתחיל, האפליקציה זקוקה לאישור הרשאה קריטית:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            PermissionItem(
                                icon = Icons.Default.LocationOn,
                                title = "גישה למיקום (GPS)",
                                description = "לדגימת קואורדינטות מדויקות לאתרי בנייה וללא כתובת",
                                isGranted = locationGranted
                            )

                            Button(
                                onClick = {
                                    permissionsLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("אשר הרשאת גישה כעת", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // STEP 2: PROFILE & LICENSE DETAILS
                2 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "פרטי הקבלן / העסק",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = contractorHeader,
                                onValueChange = { contractorHeader = it },
                                label = { Text("שם הקבלן / כותרת העסק בטופס") },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = contractorPhone,
                                onValueChange = { contractorPhone = it },
                                label = { Text("מספר טלפון ליצירת קשר") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = startingFormNumberStr,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        startingFormNumberStr = newValue
                                    }
                                },
                                label = { Text("מספר טופס התחלתי (לדוגמה: 1)") },
                                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "פרטי טכנאי ורישיון גפ\"מ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = technicianName,
                                onValueChange = { technicianName = it },
                                label = { Text("שם מלא של הטכנאי") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = technicianLicenseNumber,
                                onValueChange = { technicianLicenseNumber = it },
                                label = { Text("מספר רישיון טכנאי גז") },
                                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = technicianLicenseExpiry,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("תוקף הרישיון (לחץ לבחירה)") },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                interactionSource = dateInteractionSource,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("רמת טכנאי:", fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = technicianLevel == "רמה 1",
                                        onClick = { technicianLevel = "רמה 1" }
                                    )
                                    Text("רמה 1", fontSize = 14.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = technicianLevel == "רמה 2",
                                        onClick = { technicianLevel = "רמה 2" }
                                    )
                                    Text("רמה 2", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                // STEP 3: SUMMARY & START WORK
                3 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(56.dp)
                            )

                            Text(
                                text = "הגדרת הפרופיל הושלמה בהצלחה!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF1B5E20)
                            )

                            Text(
                                text = "האפליקציה מוכנה לפעולה. כל הפרטים נשמרו וישולבו אוטומטית בדוחות שתפיק.",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF2E7D32)
                            )

                            Button(
                                onClick = {
                                    onCompleteOnboarding()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("התחל לעבוד במערכת 🚀", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons (Next / Back)
            if (currentStep < 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("הקודם")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep == 2) {
                                val formNum = startingFormNumberStr.toIntOrNull() ?: 0
                                if (contractorHeader.isBlank() || technicianName.isBlank() || technicianLicenseNumber.isBlank() || technicianLicenseExpiry.isBlank()) {
                                    Toast.makeText(context, "אנא הזן את כל שדות החובה של העסק והרישיון", Toast.LENGTH_SHORT).show()
                                } else if (formNum <= 0) {
                                    Toast.makeText(context, "אנא הזן מספר טופס התחלתי תקין (1 ומעלה)", Toast.LENGTH_SHORT).show()
                                } else {
                                    settingsManager.contractorHeader = contractorHeader
                                    settingsManager.contractorPhone = contractorPhone
                                    settingsManager.defaultTechnicianName = technicianName
                                    settingsManager.technicianLicenseNumber = technicianLicenseNumber
                                    settingsManager.technicianLicenseExpiry = technicianLicenseExpiry
                                    settingsManager.technicianLevel = technicianLevel
                                    settingsManager.currentFormNumber = formNum
                                    currentStep++
                                }
                            } else {
                                currentStep++
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("המשך")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
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
                }) {
                    Text("אישור")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("ביטול")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun StepBadge(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isDone: Boolean
) {
    val bg = when {
        isDone -> Color(0xFF2E7D32)
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isDone || isActive -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            } else {
                Text(text = stepNumber.toString(), color = contentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 11.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean
) {
    Surface(
        color = if (isGranted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isGranted) Color(0xFF81C784) else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}