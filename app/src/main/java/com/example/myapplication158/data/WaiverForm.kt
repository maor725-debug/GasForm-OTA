package com.example.myapplication158.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "waiver_forms")
@Serializable
data class WaiverForm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String = "",
    val clientName: String = "",
    val clientId: String = "", // תעודת זהות / ח.פ
    val address: String = "",
    val phone: String = "",
    val workDescription: String = "", // תיאור העבודה

    // תמונות של מצב השטח לפני העבודה
    val image1Uri: String? = null,
    val image2Uri: String? = null,
    val image3Uri: String? = null,

    // אימות זהות וחתימות
    val clientPhotoUri: String? = null, // הפיצ'ר שביקשת: תמונת הלקוח במעמד החתימה
    val clientSignatureUri: String? = null,
    val technicianSignatureUri: String? = null,

    // ניהול קבצים
    val savedPdfFilePath: String? = null,
    val isSavedToTarget: Boolean = false,
    val savedTargetLocation: String? = null
) {
    // פונקציה שבודקת אם הטופס ריק לחלוטין (כדי לא לשמור סתם טפסים ריקים)
    fun isNotEmptyOrBlank(): Boolean {
        return clientName.isNotBlank() || clientId.isNotBlank() || phone.isNotBlank() || workDescription.isNotBlank()
    }
}