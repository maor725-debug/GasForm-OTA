package com.example.myapplication158.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GasFormDao {
    @Query("SELECT * FROM gas_forms ORDER BY createdAt DESC")
    fun getAllForms(): Flow<List<GasForm>>

    @Query("SELECT * FROM gas_forms WHERE id = :id")
    suspend fun getFormById(id: Int): GasForm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: GasForm): Long

    @Update
    suspend fun updateForm(form: GasForm)

    @Delete
    suspend fun deleteForm(form: GasForm)
}