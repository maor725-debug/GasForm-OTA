package com.example.myapplication158.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GasFormD4Dao {
    @Query("SELECT * FROM GasFormD4 ORDER BY id DESC")
    fun getAllForms(): Flow<List<GasFormD4>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: GasFormD4): Long

    @Update
    suspend fun updateForm(form: GasFormD4)

    @Delete
    suspend fun deleteForm(form: GasFormD4)
}