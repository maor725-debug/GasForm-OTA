package com.example.myapplication158

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication158.data.GasForm
import com.example.myapplication158.data.PeriodicGasForm
import com.example.myapplication158.data.GasFormD2
import com.example.myapplication158.UserInterface.GasFormViewModel
import com.example.myapplication158.UserInterface.screens.FormEditScreen
import com.example.myapplication158.UserInterface.screens.FormListScreen
import com.example.myapplication158.UserInterface.screens.OnboardingScreen
import com.example.myapplication158.UserInterface.screens.PeriodicFormEditScreen
import com.example.myapplication158.UserInterface.screens.PeriodicFormEditScreenD2
import com.example.myapplication158.UserInterface.screens.LoginScreen
import com.example.myapplication158.UserInterface.screens.SettingsDialog
import com.example.myapplication158.UserInterface.screens.WelcomeScreen
import com.example.myapplication158.util.OtaUpdateManager
import com.example.myapplication158.util.SettingsManager
import com.example.myapplication158.util.SupabaseManager
import com.example.myapplication158.util.TrialTracker
import com.example.myapplication158.util.UpdateInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class TechnicianProfile(val device_id: String, val full_name: String, val license_number: String, val license_expiry: String, val technician_level: String, val is_blocked: Boolean = false, val block_reason: String? = null, val correction_log: String? = null)

@Serializable
data class SystemMessage(val id: String, val title: String, val content: String, val target_device_id: String? = null, val created_at: String? = null)

@Serializable
data class MessageRead(val message_id: String, val device_id: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val isDarkTheme = settingsManager.isDarkMode

            val colorScheme = when (settingsManager.appTheme) {
                SettingsManager.THEME_BLUE -> if (isDarkTheme) { darkColorScheme(primary = Color(0xFF90CAF9), primaryContainer = Color(0xFF0D47A1), secondary = Color(0xFF64B5F6), background = Color(0xFF121212), surface = Color(0xFF1E1E1E)) } else { lightColorScheme(primary = Color(0xFF1565C0), primaryContainer = Color(0xFFBBDEFB), secondary = Color(0xFF1E88E5), surfaceVariant = Color(0xFFE3F2FD)) }
                SettingsManager.THEME_GREEN -> if (isDarkTheme) { darkColorScheme(primary = Color(0xFFA5D6A7), primaryContainer = Color(0xFF1B5E20), secondary = Color(0xFF81C784), background = Color(0xFF121212), surface = Color(0xFF1E1E1E)) } else { lightColorScheme(primary = Color(0xFF2E7D32), primaryContainer = Color(0xFFC8E6C9), secondary = Color(0xFF43A047), surfaceVariant = Color(0xFFE8F5E9)) }
                SettingsManager.THEME_YELLOW -> if (isDarkTheme) { darkColorScheme(primary = Color(0xFFFFD600), primaryContainer = Color(0xFFFBC02D), secondary = Color(0xFFFFEA00), background = Color(0xFF121212), surface = Color(0xFF1E1E1E)) } else { lightColorScheme(primary = Color(0xFFF57F17), primaryContainer = Color(0xFFFFF9C4), secondary = Color(0xFFFFD600), surfaceVariant = Color(0xFFFFFDE7)) }
                else -> if (isDarkTheme) { darkColorScheme(primary = Color(0xFFFFB74D), primaryContainer = Color(0xFFE65100), secondary = Color(0xFFFF9800), background = Color(0xFF121212), surface = Color(0xFF1E1E1E)) } else { lightColorScheme(primary = Color(0xFFFF6D00), primaryContainer = Color(0xFFFFE0B2), secondary = Color(0xFFFF9100), surfaceVariant = Color(0xFFFFF3E0)) }
            }

            val scale = when (settingsManager.textSizeLevel) {
                SettingsManager.TEXT_SIZE_TINY -> 0.7f
                SettingsManager.TEXT_SIZE_SMALL -> 0.85f
                SettingsManager.TEXT_SIZE_LARGE -> 1.15f
                SettingsManager.TEXT_SIZE_HUGE -> 1.30f
                else -> 1.0f
            }

            val baseTypography = Typography()
            val scaledTypography = Typography(
                displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * scale),
                displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * scale),
                displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * scale),
                headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * scale),
                headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * scale),
                headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * scale),
                titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * scale),
                titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * scale),
                titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * scale),
                bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * scale),
                bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * scale),
                bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * scale),
                labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * scale),
                labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * scale),
                labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * scale)
            )

            MaterialTheme(colorScheme = colorScheme, typography = scaledTypography) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val otaManager = remember { OtaUpdateManager(context) }
    var updateAvailable by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val currentVersionCode = try { context.packageManager.getPackageInfo(context.packageName, 0).versionCode } catch (e: Exception) { 1 }
        val update = otaManager.checkForUpdates(currentVersionCode)
        if (update != null) { updateAvailable = update }
    }

    MainNavigation()

    updateAvailable?.let { update ->
        AlertDialog(
            onDismissRequest = { updateAvailable = null },
            title = { Text("עדכון גרסה זמין (${update.versionName})", textAlign = TextAlign.Right) },
            text = { Text(update.releaseNotes, textAlign = TextAlign.Right) },
            confirmButton = { Button(onClick = { otaManager.downloadAndInstallApk(update.apkUrl); updateAvailable = null }) { Text("הורד ועדכן") } },
            dismissButton = { TextButton(onClick = { updateAvailable = null }) { Text("מאוחר יותר") } }
        )
    }
}

sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object Onboarding : Screen()
    object List : Screen()
    data class Edit(val form: GasForm) : Screen()
    data class EditPeriodic(val form: PeriodicGasForm) : Screen()
    data class EditD2(val form: GasFormD2) : Screen()
}

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val viewModel: GasFormViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val androidId = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_DEVICE" }

    val appPrefs = remember { context.getSharedPreferences("app_security_prefs", Context.MODE_PRIVATE) }
    var isLicensed by remember { mutableStateOf(appPrefs.getBoolean("is_licensed_user", false)) }
    var trialFormsCount by remember { mutableIntStateOf(appPrefs.getInt("trial_forms_count", 0)) }
    var hasSeenWelcome by remember { mutableStateOf(appPrefs.getBoolean("has_seen_welcome", false)) }

    var isUserBlocked by remember { mutableStateOf(false) }
    var blockReason by remember { mutableStateOf("") }
    var profileRefreshTrigger by remember { mutableIntStateOf(0) }
    var showSettingsFromBlock by remember { mutableStateOf(false) }

    var unreadMessages by remember { mutableStateOf<List<SystemMessage>>(emptyList()) }
    var currentDisplayMessage by remember { mutableStateOf<SystemMessage?>(null) }
    var isConfirmingMessage by remember { mutableStateOf(false) }

    LaunchedEffect(profileRefreshTrigger) {
        if (androidId != "UNKNOWN_DEVICE") {
            try {
                val profile = SupabaseManager.client.postgrest["technicians_profiles"].select { filter { eq("device_id", androidId) } }.decodeSingleOrNull<TechnicianProfile>()
                if (profile?.is_blocked == true) { isUserBlocked = true; blockReason = profile.block_reason ?: "לא צוינה סיבת חסימה." } else { isUserBlocked = false }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    LaunchedEffect(androidId) {
        if (androidId == "UNKNOWN_DEVICE") return@LaunchedEffect
        while (true) {
            try {
                val allMessages = SupabaseManager.client.postgrest["system_messages"].select().decodeList<SystemMessage>()
                val relevantMessages = allMessages.filter { it.target_device_id.isNullOrEmpty() || it.target_device_id == androidId }
                if (relevantMessages.isNotEmpty()) {
                    val myReads = SupabaseManager.client.postgrest["message_reads"].select { filter { eq("device_id", androidId) } }.decodeList<MessageRead>()
                    val readMessageIds = myReads.map { it.message_id }.toSet()
                    val newUnread = relevantMessages.filter { it.id !in readMessageIds }.sortedBy { it.created_at }
                    unreadMessages = newUnread
                    if (currentDisplayMessage == null && newUnread.isNotEmpty()) { currentDisplayMessage = newUnread.first() }
                }
            } catch (e: Exception) { e.printStackTrace() }
            delay(60 * 60 * 1000L)
        }
    }

    LaunchedEffect(isLicensed) {
        if (!isLicensed && androidId != "UNKNOWN_DEVICE") {
            try {
                val remoteTracker = SupabaseManager.client.postgrest["trials_tracker"].select { filter { eq("device_id", androidId) } }.decodeSingleOrNull<TrialTracker>()
                if (remoteTracker != null) {
                    if (remoteTracker.forms_created > trialFormsCount) { trialFormsCount = remoteTracker.forms_created; appPrefs.edit().putInt("trial_forms_count", trialFormsCount).apply() }
                } else {
                    SupabaseManager.client.postgrest["trials_tracker"].insert(TrialTracker(device_id = androidId, forms_created = trialFormsCount))
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    val isOnboardingComplete = remember(settingsManager.technicianLicenseNumber) {
        settingsManager.contractorHeader.isNotBlank() && settingsManager.defaultTechnicianName.isNotBlank() && settingsManager.technicianLicenseNumber.isNotBlank() && settingsManager.currentFormNumber > 0
    }

    var currentScreen by remember { mutableStateOf<Screen>(when { !hasSeenWelcome -> Screen.Welcome !isOnboardingComplete -> Screen.Onboarding else -> Screen.List }) }

    var showFormTypeDialog by remember { mutableStateOf(false) }

    val handleNewFormAttempt: (createAction: () -> Unit) -> Unit = { createAction ->
        if (isLicensed) {
            createAction()
        } else if (trialFormsCount < 30) {
            trialFormsCount++
            appPrefs.edit().putInt("trial_forms_count", trialFormsCount).apply()
            scope.launch { try { SupabaseManager.client.postgrest["trials_tracker"].update(mapOf("forms_created" to trialFormsCount)) { filter { eq("device_id", androidId) } } } catch (e: Exception) { e.printStackTrace() } }
            val formsLeft = 30 - trialFormsCount
            if (formsLeft in 1..10) { Toast.makeText(context, "שים לב: נותרו לך עוד $formsLeft טפסים בתקופת הניסיון", Toast.LENGTH_LONG).show() }
            createAction()
        } else {
            Toast.makeText(context, "תקופת הניסיון הסתיימה (30 טפסים). אנא היכנס ל'הגדרות' -> 'חשבון' כדי להירשם.", Toast.LENGTH_LONG).show()
        }
    }

    if (isUserBlocked) {
        if (showSettingsFromBlock) {
            SettingsDialog(onDismissRequest = { showSettingsFromBlock = false; profileRefreshTrigger++ }, onDismiss = { showSettingsFromBlock = false; profileRefreshTrigger++ }, viewModel = viewModel, initialCategoryIndex = 3)
        } else {
            AlertDialog(
                onDismissRequest = { }, title = { Text("החשבון נחסם לשימוש", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth(), color = Color.Red, fontWeight = FontWeight.Bold) },
                text = { Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) { Text("הגישה שלך למערכת נחסמה עקב אי-תאימות בפרטים. הודעת המנהל:", textAlign = TextAlign.Right); Spacer(modifier = Modifier.height(12.dp)); Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) { Text(blockReason, color = Color(0xFFC62828), fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Right) }; Spacer(modifier = Modifier.height(12.dp)); Text("באפשרותך לתקן את הפרטים שלך כעת. המערכת תשחרר את החסימה אוטומטית לאחר שמירה וסנכרון מוצלח.", textAlign = TextAlign.Right) } },
                confirmButton = { Button(onClick = { showSettingsFromBlock = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("ערוך ותקן פרטים") } },
                dismissButton = { TextButton(onClick = { (context as? ComponentActivity)?.finish() }) { Text("סגור אפליקציה", color = Color.Gray) } }
            )
        }
    } else {
        currentDisplayMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { }, title = { Text(msg.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
                text = { Text(msg.content, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth(), fontSize = 15.sp, lineHeight = 22.sp) },
                confirmButton = {
                    Button(onClick = { isConfirmingMessage = true; scope.launch { try { SupabaseManager.client.postgrest["message_reads"].insert(MessageRead(message_id = msg.id, device_id = androidId)); val updatedUnread = unreadMessages.filter { it.id != msg.id }; unreadMessages = updatedUnread; currentDisplayMessage = updatedUnread.firstOrNull() } catch (e: Exception) { e.printStackTrace(); Toast.makeText(context, "שגיאה באישור ההודעה מול השרת.", Toast.LENGTH_SHORT).show() } finally { isConfirmingMessage = false } } }, enabled = !isConfirmingMessage, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { if (isConfirmingMessage) { CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp)) } else { Text("קראתי והבנתי") } }
                }
            )
        }

        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                is Screen.Welcome -> { WelcomeScreen(onNavigateNext = { hasSeenWelcome = true; appPrefs.edit().putBoolean("has_seen_welcome", true).apply(); currentScreen = if (!isOnboardingComplete) Screen.Onboarding else Screen.List }) }
                is Screen.Login -> { LoginScreen(onLoginSuccess = { isLicensed = true; appPrefs.edit().putBoolean("is_licensed_user", true).apply(); currentScreen = if (!isOnboardingComplete) Screen.Onboarding else Screen.List }) }
                is Screen.Onboarding -> { OnboardingScreen(onCompleteOnboarding = { scope.launch { try { val profile = TechnicianProfile(device_id = androidId, full_name = settingsManager.defaultTechnicianName, license_number = settingsManager.technicianLicenseNumber, license_expiry = settingsManager.technicianLicenseExpiry, technician_level = settingsManager.technicianLevel, is_blocked = false); SupabaseManager.client.postgrest["technicians_profiles"].upsert(profile) } catch (e: Exception) { e.printStackTrace() } }; currentScreen = Screen.List }) }
                is Screen.List -> {
                    FormListScreen(
                        viewModel = viewModel,
                        onAddNormativeForm = { handleNewFormAttempt { currentScreen = Screen.Edit(GasForm(partnerNumber = viewModel.getNextPartnerNumber())) } },
                        onAddOtherForms = { showFormTypeDialog = true },
                        onEditForm = { form -> currentScreen = Screen.Edit(form) },
                        onEditPeriodicForm = { form -> currentScreen = Screen.EditPeriodic(form) }
                    )
                }
                is Screen.Edit -> { FormEditScreen(viewModel = viewModel, form = screen.form, onNavigateBack = { currentScreen = Screen.List }) }
                is Screen.EditPeriodic -> { PeriodicFormEditScreen(viewModel = viewModel, form = screen.form, onNavigateBack = { currentScreen = Screen.List }) }
                is Screen.EditD2 -> { PeriodicFormEditScreenD2(viewModel = viewModel, form = screen.form, onNavigateBack = { currentScreen = Screen.List }) }
            }
        }
    }

    if (showFormTypeDialog) {
        AlertDialog(
            onDismissRequest = { showFormTypeDialog = false },
            title = { Text("טפסים נוספים", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = { Text("בחר איזה טופס למלא:", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showFormTypeDialog = false; handleNewFormAttempt { currentScreen = Screen.EditPeriodic(PeriodicGasForm()) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()
                    ) { Text("דוח בדיקה תקופתית למערכת גז מרכזית (מכלים מיטלטלים)", textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp)) }

                    Button(
                        onClick = { showFormTypeDialog = false; handleNewFormAttempt { currentScreen = Screen.EditD2(GasFormD2()) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), modifier = Modifier.fillMaxWidth()
                    ) { Text("דוח בדיקה תקופתית: מאגר גפ\"מ (מכלים נייחים)", textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp)) }
                }
            },
            dismissButton = { TextButton(onClick = { showFormTypeDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("ביטול", color = Color.Gray, textAlign = TextAlign.Center) } }
        )
    }
}