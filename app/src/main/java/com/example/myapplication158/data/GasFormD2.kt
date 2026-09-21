package com.example.myapplication158.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "GasFormD2")
@Serializable
data class GasFormD2(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sequentialNumber: Int = 0,
    val date: String = "",
    val businessName: String = "",
    val businessType: String = "",
    val businessId: String = "",
    val fireDeptFileNumber: String = "",
    val city: String = "",
    val street: String = "",
    val building: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val gasProvider: String = "",

    val isUnaddressedSite: Boolean = false,
    val gpsCoordinates: String = "",
    val sitePhotoUri: String = "",

    // פרטים ייעודיים לד-2
    val manufactureOrTestYear: String = "",
    val capacityPerTank: String = "",
    val totalCapacity: String = "",
    val tankType: String = "על-קרקעי",
    val usageType: String = "מגורים",
    val manifoldNumber: String = "",
    val suppliesToBuildings: String = "",

    // 1. אתר ההתקנה
    val checkSiteSignage: String = "",
    val checkSiteClean: String = "",

    // 2. המכלים
    val checkTankPlate: String = "",
    val checkTankCover: String = "",
    val checkTankFittings: String = "",
    val checkSafeAccess: String = "",
    val checkFittingsHeight: String = "",
    val checkSafetyDistances: String = "", // קריטי
    val checkElecDistances: String = "", // קריטי
    val checkFillPipe: String = "",

    // 3. צנרת משותפת
    val checkEarthquakeValve: String = "",
    val checkValveLevel: String = "",
    val checkMainValve: String = "",
    val checkDischargeValve: String = "",
    val checkPressure1_4: String = "",
    val checkPipingSecured: String = "",
    val checkOutletsPlugged: String = "", // קריטי

    // 4. אטימות ולחץ (קריטי)
    val isLeakFoundPrimary: Boolean = false,
    val leakLocationDetails: String = "",
    val intermediatePressureValue: String = "",
    val isIntermediatePressureKept: Boolean = true,

    // סיכום וחתימות
    val finalStatus: String = "OK",
    val defectsFixByDate: String = "",
    val executionRemarks: String = "",
    val technicianName: String = "",
    val technicianLicense: String = "",
    val clientNameConfirm: String = "",
    val clientSignatureUri: String = "",
    val extraImagesUris: String = "",
    val failedReasonsJson: String = "",

    val savedTargetLocation: String = "מכשיר",
    val isSavedToTarget: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)