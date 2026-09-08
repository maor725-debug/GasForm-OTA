package com.example.myapplication158.UserInterface.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE) }

    // משיכת הפרטים השמורים (אם ישנם)
    var email by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("password", "") ?: "") }
    var rememberMe by remember { mutableStateOf(prefs.getBoolean("remember_me", false)) }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // פונקציית ההתחברות (הוצאנו אותה החוצה כדי שנוכל להפעיל אותה גם אוטומטית)
    val performLogin: () -> Unit = {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "נא להזין אימייל וסיסמה"
        } else {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
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
                            // שומרים או מוחקים את הפרטים בזיכרון המכשיר לפי בחירת המשתמש
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
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorMessage = "שגיאת התחברות: פרטים שגויים או שגיאת רשת."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // מנגנון הפעלה אוטומטית בעת טעינת המסך
    LaunchedEffect(Unit) {
        if (rememberMe && email.isNotBlank() && password.isNotBlank()) {
            performLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("אימייל מורשה") },
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
            label = { Text("סיסמה") },
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

        // תיבת הסימון לשמירת הפרטים
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
                onClick = performLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("התחבר למערכת", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
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