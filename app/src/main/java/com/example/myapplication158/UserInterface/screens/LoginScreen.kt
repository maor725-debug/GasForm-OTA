package com.example.myapplication158.UserInterface.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.myapplication158.util.SupabaseManager
import com.example.myapplication158.util.UserProfile
import com.example.myapplication158.util.ProfileUpdate
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE) }

    var isSignUpMode by remember { mutableStateOf(false) } // שולט אם אנחנו בהתחברות או הרשמה

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("password", "") ?: "") }
    var rememberMe by remember { mutableStateOf(prefs.getBoolean("remember_me", false)) }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val performAction: () -> Unit = {
        if (email.isBlank() || password.isBlank() || (isSignUpMode && (firstName.isBlank() || lastName.isBlank()))) {
            errorMessage = "נא למלא את כל השדות"
        } else {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    if (isSignUpMode) {
                        // תהליך הרשמה
                        SupabaseManager.client.auth.signUpWith(Email) {
                            this.email = email.trim()
                            this.password = password
                        }

                        val user = SupabaseManager.client.auth.currentUserOrNull()
                        if (user != null) {
                            // מעדכנים את השמות בטבלה
                            val profileUpdate = ProfileUpdate(
                                first_name = firstName.trim(),
                                last_name = lastName.trim()
                            )
                            SupabaseManager.client.postgrest["profiles"]
                                .update(profileUpdate) { filter { eq("id", user.id) } }

                            val profile = SupabaseManager.client.postgrest["profiles"]
                                .select { filter { eq("id", user.id) } }
                                .decodeSingleOrNull<UserProfile>()

                            if (profile?.status == "active") {
                                if (rememberMe) {
                                    prefs.edit()
                                        .putBoolean("remember_me", true)
                                        .putString("email", email.trim())
                                        .putString("password", password)
                                        .apply()
                                } else {
                                    prefs.edit().clear().apply()
                                }
                                onLoginSuccess()
                            } else {
                                errorMessage = "הרישיון שלך אינו פעיל. פנה להנהלה."
                            }
                        } else {
                            errorMessage = "שגיאה ביצירת המשתמש."
                        }
                    } else {
                        // תהליך התחברות רגיל
                        SupabaseManager.client.auth.signInWith(Email) {
                            this.email = email.trim()
                            this.password = password
                        }

                        val user = SupabaseManager.client.auth.currentUserOrNull()
                        if (user != null) {
                            val profile = SupabaseManager.client.postgrest["profiles"]
                                .select { filter { eq("id", user.id) } }
                                .decodeSingleOrNull<UserProfile>()

                            if (profile?.status == "active") {
                                if (rememberMe) {
                                    prefs.edit()
                                        .putBoolean("remember_me", true)
                                        .putString("email", email.trim())
                                        .putString("password", password)
                                        .apply()
                                } else {
                                    prefs.edit().clear().apply()
                                }
                                onLoginSuccess()
                            } else {
                                errorMessage = "הרישיון שלך אינו פעיל. פנה להנהלה."
                            }
                        } else {
                            errorMessage = "שגיאה בזיהוי המשתמש. נסה שנית."
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorMessage = if (isSignUpMode) "שגיאה בהרשמה: ייתכן שהאימייל תפוס או סיסמה קצרה מדי."
                    else "שגיאת התחברות: פרטים שגויים או שגיאת רשת."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!isSignUpMode && rememberMe && email.isNotBlank() && password.isNotBlank()) {
            performAction()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // מאפשר גלילה אם המסך קטן מדי לשדות החדשים
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "נורמטיבי",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "מערכת בדיקות גז ורישיונות",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (isSignUpMode) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("שם פרטי") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("שם משפחה") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("אימייל") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("סיסמה (לפחות 6 תווים)") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "הסתר סיסמה" else "הצג סיסמה"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text("שמור פרטי התחברות", fontSize = 14.sp)
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
            Button(
                onClick = performAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isSignUpMode) "הרשם למערכת" else "התחבר למערכת", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                isSignUpMode = !isSignUpMode
                errorMessage = null // מנקה שגיאות כשעוברים מסך
            }
        ) {
            Text(if (isSignUpMode) "יש לך כבר חשבון? התחבר כאן" else "אין לך חשבון? הירשם עכשיו")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}