package com.example.myapplication158.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "GasFormD4")
@Serializable
data class GasFormD4(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sequentialNumber: Int = 0,
    val date: String = "",
    val consumerNumber: String = "",
    val gasProvider: String = "",
    val facilityType: String = "פרטי",
    val activationDate: String = "",

    val businessId: String = "",
    val businessName: String = "",
    val businessType: String = "",
    val fireDeptFileNumber: String = "",
    val city: String = "",
    val street: String = "",
    val building: String = "",
    val zip: String = "",
    val poBox: String = "",

    val mainContactName: String = "",
    val contactRole: String = "",
    val clientPhone: String = "",
    val email: String = "",
    val clientName: String = "",

    // 1. בחינה חזותית - מכלים מיטלטלים
    val check1_1_1: String = "",
    val check1_1_2_1: String = "",
    val check1_1_2_2: String = "",
    val check1_1_2_3: String = "",
    val check1_1_2_4: String = "",
    val check1_1_2_5: String = "",
    val check1_1_2_6: String = "",
    val check1_2: String = "",
    val check1_3: String = "",
    val check1_4: String = "",
    val check1_5_1: String = "",
    val check1_5_2: String = "",
    val check1_5_3: String = "",
    val check1_6_1: String = "",
    val check1_6_2: String = "",
    val check1_7: String = "",

    // 2. מאגר גפ"מ - מכלים נייחים
    val check2_1_1: String = "",
    val check2_2_1: String = "",

    // 3. מערכת הצינורות
    val check3_1: String = "",
    val check3_2: String = "",
    val check3_3: String = "",
    val check3_4: String = "",
    val check3_5: String = "",
    val check3_6: String = "",
    val check3_7: String = "",
    val check3_8: String = "",
    val check3_9: String = "",

    // 4. מכשירים
    val devicesList: String = "",
    val check4_2: String = "",
    val check4_3: String = "",
    val check4_4: String = "",
    val check4_5_1: String = "",
    val check4_5_2: String = "",
    val check4_5_3: String = "",
    val check4_5_4: String = "",
    val check4_6: String = "",
    val check4_7_1: String = "",
    val check4_7_2: String = "",
    val check4_7_3: String = "",
    val check4_8_1: String = "",
    val check4_8_2: String = "",
    val check4_9: String = "",

    // 5. אטימות
    val testPressure: String = "",
    val check5_1: String = "",
    val check5_2: String = "",

    // 6. סיכום
    val isFacilityValid: Boolean = true,
    val requiresFixes: Boolean = false,
    val fixByDate: String = "",
    val isDisconnected: Boolean = false,
    val disconnectReason: String = "",
    val additionalNotes: String = "",

    val failedReasonsJson: String = "",
    val extraImagesUris: String = "",
    val technicianSignatureUri: String = "",
    val clientSignatureUri: String = "",
    val clientNameConfirm: String = "",

    val savedTargetLocation: String = "מכשיר",
    val isSavedToTarget: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)