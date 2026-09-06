package com.example.myapplication158.util

import android.content.Context
import com.example.myapplication158.data.WorkOrder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

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
                        onResult(true, "חיבור תקין ל-Supabase Cloud! ☁️")
                    } else {
                        onResult(false, "תגובת שרת Supabase: ${response.code}")
                    }
                }
            }
        })
    }

    fun syncWorkOrderToCloud(workOrder: WorkOrder, onResult: ((Boolean, String) -> Unit)? = null) {
        val url = "$supabaseUrl/rest/v1/work_orders"
        val json = JSONObject().apply {
            put("id", workOrder.id)
            put("target_date", workOrder.targetDate)
            put("target_time", workOrder.targetTime)
            put("location", workOrder.location)
            put("client_phone", workOrder.clientPhone)
            put("job_description", workOrder.jobDescription)
            put("quoted_price", workOrder.quotedPrice)
            put("status", workOrder.status)
            put("is_muted", workOrder.isMuted)
            put("created_at", workOrder.createdAt)
        }

        sendSupabasePost(url, json.toString(), onResult)
    }

    private fun sendSupabasePost(url: String, jsonBody: String, onResult: ((Boolean, String) -> Unit)?) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $supabaseKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "resolution=merge-duplicates")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult?.invoke(false, e.localizedMessage ?: "שגיאת תקשורת עם Supabase")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        onResult?.invoke(true, "סונכרן בהצלחה ל-Supabase Cloud ✅")
                    } else {
                        onResult?.invoke(false, "תגובת שרת Supabase: ${response.code}")
                    }
                }
            }
        })
    }
}
