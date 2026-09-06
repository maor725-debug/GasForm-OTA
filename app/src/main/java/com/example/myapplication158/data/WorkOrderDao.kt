package com.example.myapplication158.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkOrderDao {
    @Query("SELECT * FROM work_orders ORDER BY id DESC")
    fun getAllWorkOrders(): Flow<List<WorkOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrder(workOrder: WorkOrder): Long

    @Update
    suspend fun updateWorkOrder(workOrder: WorkOrder)

    @Delete
    suspend fun deleteWorkOrder(workOrder: WorkOrder)

    @Query("SELECT * FROM work_orders WHERE id = :id")
    suspend fun getWorkOrderById(id: Int): WorkOrder?
}
