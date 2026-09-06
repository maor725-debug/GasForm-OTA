package com.example.myapplication158.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object NavigationUtils {
    fun navigateToAddress(context: Context, address: String) {
        if (address.isBlank()) {
            Toast.makeText(context, "לא הוזנה כתובת לניווט", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val wazeUrl = "https://waze.com/ul?q=${Uri.encode(address)}&navigate=yes"
            val wazeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(wazeUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(wazeIntent)
        } catch (e: Exception) {
            try {
                val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(mapIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "לא ניתן לפתוח אפליקציית ניווט", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
