package com.example.myapplication158.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodicGasFormDao {
    @Query("SELECT * FROM periodic_gas_forms ORDER BY createdAt DESC")
    fun getAllForms(): Flow<List<PeriodicGasForm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: PeriodicGasForm): Long

    @Update
    suspend fun updateForm(form: PeriodicGasForm)

    @Delete
    suspend fun deleteForm(form: PeriodicGasForm)

    @Query("SELECT * FROM periodic_gas_forms WHERE id = :id")
    suspend fun getFormById(id: Int): PeriodicGasForm?
}