package com.example.myapplication158.UserInterface.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PinLockScreen(
    correctPin: String,
    onUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pin_security_prefs", Context.MODE_PRIVATE) }

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // משיכת סטטוס הניסיונות והנעילה מזיכרון המכשיר (חסין בפני הפעלה מחדש)
    var failedAttempts by remember { mutableStateOf(prefs.getInt("failed_attempts", 0)) }
    var lockoutEndTime by remember { mutableStateOf(prefs.getLong("lockout_end_time", 0L)) }
    var remainingSeconds by remember { mutableStateOf(0L) }

    // טיימר לאחור שרץ ברקע אם קיימת נעילה
    LaunchedEffect(lockoutEndTime) {
        while (true) {
            val current = System.currentTimeMillis()
            if (lockoutEndTime > current) {
                remainingSeconds = (lockoutEndTime - current) / 1000
                delay(1000L) // עדכון כל שנייה
            } else {
                remainingSeconds = 0L
                if (lockoutEndTime != 0L) {
                    // הזמן חלף - מאפסים את הניסיונות
                    failedAttempts = 0
                    lockoutEndTime = 0L
                    prefs.edit().putInt("failed_attempts", 0).putLong("lockout_end_time", 0L).apply()
                }
                break
            }
        }
    }

    val isLocked = remainingSeconds > 0

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "מנעול",
            modifier = Modifier.size(64.dp),
            // הצבע משתנה לאדום בזמן הנעילה
            tint = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "הזן קוד אישי (PIN)",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = enteredPin,
            onValueChange = {
                if (!isLocked && it.length <= 4) {
                    enteredPin = it
                    isError = false
                    if (it == correctPin) {
                        // הצלחה - איפוס מונים ושחרור
                        prefs.edit().putInt("failed_attempts", 0).putLong("lockout_end_time", 0L).apply()
                        onUnlockSuccess()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            isError = isError,
            singleLine = true,
            enabled = !isLocked, // חוסם הקלדה בזמן נעילה
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        if (isError && !isLocked) {
            Text(
                text = "קוד שגוי. נותרו ${5 - failedAttempts} ניסיונות.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (isLocked) {
            Text(
                text = "ננעל עקב ניסיונות שגויים.\nנסה שוב בעוד $remainingSeconds שניות.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (!isLocked) {
                    if (enteredPin == correctPin) {
                        prefs.edit().putInt("failed_attempts", 0).putLong("lockout_end_time", 0L).apply()
                        onUnlockSuccess()
                    } else {
                        isError = true
                        enteredPin = ""
                        failedAttempts++
                        if (failedAttempts >= 5) {
                            // נועל ל-30 שניות ושומר בזיכרון
                            lockoutEndTime = System.currentTimeMillis() + 30_000L
                            prefs.edit()
                                .putInt("failed_attempts", failedAttempts)
                                .putLong("lockout_end_time", lockoutEndTime)
                                .apply()
                        } else {
                            prefs.edit().putInt("failed_attempts", failedAttempts).apply()
                        }
                    }
                }
            },
            enabled = !isLocked && enteredPin.length == 4, // מאפשר לחיצה רק אם הוזנו 4 ספרות
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("כניסה")
        }
    }
}