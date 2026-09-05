package com.example.myapplication158.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gas_forms")
data class GasForm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerNumber: String = "",
    val sequentialNumber: Int = 0, // התוספת החדשה: מס' טופס רץ
    val date: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientCity: String = "",
    val clientStreet: String = "",
    val clientBuilding: String = "",

    val isUnaddressedSite: Boolean = false,
    val gpsCoordinates: String = "",
    val sitePhotoUri: String = "",

    val isSharedBuilding: Boolean = false,
    val isCommercial: Boolean = false,
    val isCylinders12x2: Boolean = false,
    val isCylinders48x2: Boolean = false,
    val isOtherType: Boolean = false,
    val otherTypeText: String = "",

    val isWorkNew: Boolean = false,
    val isWorkAddition: Boolean = false,
    val isWorkRepair: Boolean = false,
    val isWorkConnection: Boolean = false,

    val connectionDeviceType: String = "",
    val connectionDeviceBrand: String = "",
    val connectionDeviceModel: String = "",
    val connectionDeviceSerial: String = "",

    val hasAdditionalDevice: Boolean = false,
    val additionalDeviceType: String = "",
    val additionalDeviceBrand: String = "",
    val additionalDeviceModel: String = "",
    val additionalDeviceSerial: String = "",

    val hoseType: String = "",
    val hoseProductionYear: String = "",
    val clampsCount: String = "",

    val isLeakTestChecked: Boolean = false,
    val leakTestDate: String = "",
    val leakTestPressure: String = "",

    val isPressureTestChecked: Boolean = false,
    val pressureTestDate: String = "",
    val pressureTestValue: String = "",

    val isRegulatorTestChecked: Boolean = false,
    val regulatorPressure: String = "",

    val complianceRoutePlan: Boolean = false,
    val complianceMaterials: Boolean = false,
    val isRouteNotDoneByChecked: Boolean = false,
    val routeNotDoneBy: String = "",
    val isRouteDoneByChecked: Boolean = false,
    val routeDoneBy: String = "",

    val executionRemarks: String = "",
    val isStatusConforming: Boolean = true,
    val isStatusNonConforming: Boolean = false,
    val pluggedAt: String = "", // הושאר כדי למנוע קריסת מסד נתונים, לא נשתמש בו יותר ב-UI
    val nonCompliantReason: String = "", // התוספת החדשה: סיבת אי תקינות

    val technicianStamp: String = "",
    val clientIdConfirm: String = "",
    val clientSignatureUri: String = "",

    val extraRouteImageUris: String = "",
    val remarksImageUris: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val savedTargetLocation: String? = "מכשיר",
    val isSavedToTarget: Boolean = false,
    val savedPdfFilePath: String? = null,

    val customerPrice: String? = "",
    val technicianCost: String? = "",
    val internalWorkDescription: String? = ""
)

fun GasForm.isNotEmptyOrBlank(): Boolean {
    return clientName.isNotBlank() || clientPhone.isNotBlank() || clientCity.isNotBlank() ||
            clientStreet.isNotBlank() || clientSignatureUri.isNotBlank() || gpsCoordinates.isNotBlank()
}