package com.example.myapplication158.util

import android.content.Context
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * SupabaseManager:
 * Functions strictly as a Gatekeeper & Licensing Manager (Zero-Storage Model).
 * Does NOT store or transmit professional forms, client data, photos, or work orders.
 */
class SupabaseManager(val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val supabaseUrl = "https://ztwqnsnzyfkawxhpgjme.supabase.co"
    val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhc2FzZSIsInJlZiI6Inp0d3Fuc256eWZrYXd4aHBnam1lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg1OTA5ODA0LCJleHAiOjIwMjEwNDE2Njk0MH0.6Jf6LSsTjlfk0cxYzD1TRLJlfFvW2hXIM4-gkMSUYTc"

    fun testConnection(onResult: (Boolean, String) -> Unit) {
        val url = "$supabaseUrl/rest/v1/"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $supabaseKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, e.localizedMessage ?: "שגיאת חיבור ל-Supabase Cloud")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful || response.code == 200 || response.code == 404) {
                        onResult(true, "שומר הסף זמין ופעיל ב-Supabase Cloud! ☁️")
                    } else {
                        onResult(false, "תגובת שרת Supabase: ${response.code}")
                    }
                }
            }
        })
    }
}
