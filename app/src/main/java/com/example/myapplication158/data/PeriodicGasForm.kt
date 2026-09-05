package com.example.myapplication158.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "periodic_gas_forms")
data class PeriodicGasForm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sequentialNumber: Int = 0,
    val date: String = "",

    // פרטי איש קשר חובה
    val clientName: String = "",
    val clientPhone: String = "",

    // פרטי עסק ומאגר
    val businessName: String = "",
    val businessType: String = "",
    val businessId: String = "", // ח.פ / ת.ז
    val fireDeptFileNumber: String = "",

    // כתובת ו-GPS
    val isUnaddressedSite: Boolean = false,
    val gpsCoordinates: String = "",
    val sitePhotoUri: String = "",
    val city: String = "",
    val street: String = "",
    val building: String = "",
    val zipCode: String = "",
    val poBox: String = "",

    // פרטי איש קשר ראשי בעסק
    val contactName: String = "",
    val contactRole: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",

    // פרטי המאגר
    val gasProvider: String = "",
    val consumersCount: String = "",
    val cylindersCount: String = "",
    val manifoldNumber: String = "", // מספר מרכזייה

    // 1. בחינה חזותית - שומרים סטטוסים כ: "PASS" (מתאים), "FAIL" (לא מתאים), "NA" (לא ישים), או ריק.
    val checkLocationOpen: String = "",
    val checkSafetyDistances: String = "",
    val checkSafetyDistances07Heat: String = "",
    val checkSafetyDistances17Fire: String = "",
    val checkSafetyDistances05Pits: String = "",
    val checkSafetyDistances3Drainage: String = "",
    val checkSafetyDistances12Building: String = "",
    val checkSafetyDistances3LowLevel: String = "",
    val checkRegulatorSecured: String = "",
    val checkWarningSigns: String = "",
    val checkWaterSprinklers: String = "",
    val checkGasRoomMax20: String = "",
    val checkGasRoomLighting: String = "",
    val checkGasRoomNoFlammables: String = "",
    val checkCageMax20: String = "",
    val checkCageVentilated: String = "",
    val checkRampsSecured: String = "",

    // 2. מערכת הצינורות המשותפת
    val checkEarthquakeValve: String = "",
    val checkEarthquakeValveSecured: String = "",
    val checkMainValveAccessible: String = "",
    val checkDischargeValves: String = "",
    val checkPressureUpTo1_4: String = "",
    val checkPipingSecured: String = "",
    val checkUnusedOutletsPlugged: String = "",

    // פירוט ליקויים דינמי (נשמר כטקסט עם מזהה של הסעיף שנכשל)
    val failedReasonsJson: String = "",

    // 3. בדיקות אטימות ולחץ
    val isLeakFoundPrimary: Boolean = false,
    val leakLocationDetails: String = "",
    val intermediatePressureValue: String = "",
    val isIntermediatePressureKept: Boolean = true, // האם הלחץ נשמר

    // 4. סיכום מבצע הבדיקה
    // Status can be: "OK" (תקין), "DEFECTS" (ליקויים לתיקון), "DISCONNECTED" (נותק)
    val finalStatus: String = "OK",
    val defectsFixByDate: String = "",
    val executionRemarks: String = "",

    // 5. אישורים וחתימות
    val technicianName: String = "",
    val technicianLicense: String = "",
    val technicianSignatureUri: String = "",
    val clientNameConfirm: String = "",
    val clientSignatureUri: String = "",

    // נספחים כלליים
    val extraImagesUris: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val savedTargetLocation: String? = "מכשיר",
    val isSavedToTarget: Boolean = false
)

fun PeriodicGasForm.isNotEmptyOrBlank(): Boolean {
    return businessName.isNotBlank() || clientPhone.isNotBlank() || city.isNotBlank() ||
            gpsCoordinates.isNotBlank() || clientSignatureUri.isNotBlank()
}