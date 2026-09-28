package com.example.myapplication158.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GasFormD3Dao {
    @Query("SELECT * FROM GasFormD3 ORDER BY id DESC")
    fun getAllForms(): Flow<List<GasFormD3>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: GasFormD3): Long

    @Update
    suspend fun updateForm(form: GasFormD3)

    @Delete
    suspend fun deleteForm(form: GasFormD3)
}