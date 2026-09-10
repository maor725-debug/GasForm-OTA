package com.example.myapplication158.util

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val status: String,
    val first_name: String? = null,
    val last_name: String? = null,
    val device_id: String? = null // הוספנו את המעקב אחר המכשיר!
)

@Serializable
data class ProfileUpdate(
    val first_name: String,
    val last_name: String,
    val device_id: String? = null // מאפשר שמירת המכשיר בעת ההרשמה
)

@Serializable
data class DeviceUpdate(
    val device_id: String
)

@Serializable
data class TrialTracker(
    val device_id: String,
    val forms_created: Int
)

class SupabaseManager(private val context: Context) {

    fun testConnection(onResult: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.postgrest["profiles"].select { limit(1) }
                withContext(Dispatchers.Main) {
                    onResult(true, "החיבור לשרת הרישיונות (Supabase) תקין ופעיל!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "שגיאה בחיבור לשרת: ${e.localizedMessage}")
                }
            }
        }
    }

    companion object {
        private const val SUPABASE_URL = "https://ztwqnsnzyfkawxhpgjme.supabase.co"
        private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp0d3Fuc256eWZrYXd4aHBnam1lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg1OTA5ODAsImV4cCI6MjEwNDE2Njk4MH0.6Jf6LSsTjlfk0cxYzD1TRLJlfFvW2hXIM4-gkMSUYTc"

        val client: SupabaseClient by lazy {
            createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Postgrest)
                install(Auth)
            }
        }
    }
}