package com.example.myapplication158.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_orders")
data class WorkOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val targetDate: String = "", // e.g. "dd/MM/yyyy" or "yyyy-MM-dd"
    val targetTime: String = "", // e.g. "10:00"
    val location: String = "",
    val clientPhone: String = "",
    val jobDescription: String = "",
    val quotedPrice: String = "",
    val status: String = STATUS_PENDING,
    val isMuted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_CANCELED = "CANCELED"
        const val STATUS_RESCHEDULED = "RESCHEDULED"
    }
}
