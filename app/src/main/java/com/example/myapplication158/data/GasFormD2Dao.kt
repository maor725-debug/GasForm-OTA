package com.example.myapplication158.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GasFormD2Dao {
    @Insert
    suspend fun insertForm(form: GasFormD2): Long

    @Update
    suspend fun updateForm(form: GasFormD2)

    @Delete
    suspend fun deleteForm(form: GasFormD2)

    @Query("SELECT * FROM GasFormD2 ORDER BY id DESC")
    fun getAllForms(): Flow<List<GasFormD2>>
}