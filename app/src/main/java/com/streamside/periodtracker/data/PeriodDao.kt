package com.streamside.periodtracker.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PeriodDao {
    @Query("SELECT * FROM period WHERE id = :id")
    suspend fun get(id: Long): Period
    @Query("SELECT * FROM period")
    fun getAll(): LiveData<List<Period>>
    @Query("SELECT * FROM period WHERE id = (SELECT lastPeriodId FROM period WHERE nextPeriodId = -1)")
    fun getLastPeriod(): LiveData<Period>
    @Query("SELECT * FROM period WHERE nextPeriodId = -1")
    fun getCurrentPeriod(): LiveData<Period>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(period: Period): Long

    @Update
    suspend fun update(period: Period)

    @Delete
    suspend fun delete(period: Period)
    @Query("DELETE FROM period")
    suspend fun deleteAll()
}