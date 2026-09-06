package com.example.myapplication158.UserInterface.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.myapplication158.UserInterface.components.TechnicianSignatureTouchPad
import com.example.myapplication158.util.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onCompleteOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }

    var currentStep by remember { mutableIntStateOf(1) }

    // Profile State Variables
    var contractorHeader by remember { mutableStateOf(settingsManager.contractorHeader.ifEmpty { "מאור מנחם - קבלן עבודות גז" }) }
    var contractorPhone by remember { mutableStateOf(settingsManager.contractorPhone.ifEmpty { "054-6096487" }) }
    var technicianName by remember { mutableStateOf(settingsManager.defaultTechnicianName.ifEmpty { "מאור מנחם" }) }
    var startingFormNumber by remember { mutableIntStateOf(if (settingsManager.currentFormNumber > 0) settingsManager.currentFormNumber else 100) }

    // Signature State
    var savedSignatureUri by remember { mutableStateOf(settingsManager.savedSignatureUri) }

    // Permission check
    var locationGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var cameraGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: locationGranted
        cameraGranted = permissions[Manifest.permission.CAMERA] ?: cameraGranted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "הגדרות ראשוניות - קבלת פנים",
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
            // Step Progress Indicator Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepBadge(stepNumber = 1, title = "הרשאות", isActive = currentStep == 1, isDone = currentStep > 1)
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                StepBadge(stepNumber = 2, title = "פרופיל", isActive = currentStep == 2, isDone = currentStep > 2)
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                StepBadge(stepNumber = 3, title = "חתימה", isActive = currentStep == 3, isDone = currentStep > 3)
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (currentStep) {
                // STEP 1: PERMISSIONS
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
                                text = "ברוך הבא למערכת טפסי גז 158!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "לפני שנתחיל, האפליקציה זקוקה לאישור הרשאות עבודה קריטיות:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                PermissionItem(
                                    icon = Icons.Default.LocationOn,
                                    title = "גישה למיקום (GPS)",
                                    description = "לדגימת קואורדינטות מדויקות לאתרי בנייה וללא כתובת",
                                    isGranted = locationGranted
                                )

                                PermissionItem(
                                    icon = Icons.Default.PhotoCamera,
                                    title = "גישה למצלמה",
                                    description = "לצילום תוואי, ליקויים וצילום פני לקוח בעת חתימה",
                                    isGranted = cameraGranted
                                )
                            }

                            Button(
                                onClick = {
                                    permissionsLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.CAMERA
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("אשר הרשאות גישה כעת", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // STEP 2: PROFILE & CONTRACTOR DETAILS
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
                                text = "הגדרת פרופיל קבלן וטכנאי",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "פרטים אלו יופיעו אוטומטית בראש כל הדוחות והטפסים שתפיק:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                value = technicianName,
                                onValueChange = { technicianName = it },
                                label = { Text("שם הטכנאי המבצע / מס' רישיון") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = startingFormNumber.toString(),
                                onValueChange = {
                                    startingFormNumber = it.toIntOrNull() ?: 1
                                },
                                label = { Text("מספר טופס שוטף התחלתי") },
                                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // STEP 3: PERMANENT SIGNATURE
                3 -> {
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
                                text = "יצירת חתימה קבועה לטכנאי",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "חתום כעת בלוח. חתימה זו תישמר במכשיר ותוטבע אוטומטית על כל דוח עתידי:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            TechnicianSignatureTouchPad(
                                initialSignatureUri = savedSignatureUri,
                                onSignatureSaved = { uri ->
                                    val newUri = uri.ifEmpty { null }
                                    savedSignatureUri = newUri
                                    settingsManager.savedSignatureUri = newUri
                                    Toast.makeText(context, "החתימה נשמרה בהצלחה!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // STEP 4: SUMMARY & START WORK
                4 -> {
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
                                text = "האפליקציה מוכנה לפעולה. כל הפרטים והחתימה נשמרו וישולבו אוטומטית בטפסים שתפיק.",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF2E7D32)
                            )

                            Button(
                                onClick = {
                                    settingsManager.contractorHeader = contractorHeader
                                    settingsManager.contractorPhone = contractorPhone
                                    settingsManager.defaultTechnicianName = technicianName
                                    settingsManager.currentFormNumber = startingFormNumber
                                    if (settingsManager.savedSignatureUri.isNullOrEmpty()) {
                                        settingsManager.savedSignatureUri = savedSignatureUri
                                    }

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
            if (currentStep < 4) {
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
                            if (currentStep == 2 && (contractorHeader.isBlank() || technicianName.isBlank())) {
                                Toast.makeText(context, "אנא הזן שם קבלן ושם טכנאי", Toast.LENGTH_SHORT).show()
                            } else if (currentStep == 3 && savedSignatureUri.isNullOrEmpty()) {
                                Toast.makeText(context, "אנא חתום בלוח לפני ההמשך", Toast.LENGTH_SHORT).show()
                            } else {
                                settingsManager.contractorHeader = contractorHeader
                                settingsManager.contractorPhone = contractorPhone
                                settingsManager.defaultTechnicianName = technicianName
                                settingsManager.currentFormNumber = startingFormNumber
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
