package com.streamside.periodtracker.data.step

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.Date

@Dao
interface StepDao {
    @Query("SELECT * FROM step WHERE id = :id")
    fun get(id: Long): LiveData<Step>
    @Query("SELECT * FROM step WHERE date = :date")
    fun getFromDate(date: Date): LiveData<Step>
    @Query("SELECT * FROM step WHERE date BETWEEN :date1 AND :date2")
    fun getFromDateBetween(date1: Date, date2: Date): LiveData<List<Step>>
    @Query("SELECT * FROM step")
    fun getAll(): LiveData<List<Step>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(step: Step): Long
    @Update
    suspend fun update(step: Step)
    @Delete
    suspend fun delete(step: Step)
    @Query("DELETE FROM step")
    suspend fun deleteAll()
}