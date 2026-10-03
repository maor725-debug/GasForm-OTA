package com.example.myapplication158.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WaiverFormDao {
    @Query("SELECT * FROM waiver_forms ORDER BY id DESC")
    fun getAllForms(): Flow<List<WaiverForm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: WaiverForm): Long

    @Update
    suspend fun updateForm(form: WaiverForm)

    @Delete
    suspend fun deleteForm(form: WaiverForm)
}