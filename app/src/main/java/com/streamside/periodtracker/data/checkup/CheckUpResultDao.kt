package com.streamside.periodtracker.data.checkup

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.Date

@Dao
interface CheckUpResultDao {
    @Query("SELECT * FROM checkupresult WHERE date = :date")
    fun get(date: Date): LiveData<CheckUpResult>
    @Query("SELECT * FROM checkupresult")
    fun getAll(): LiveData<List<CheckUpResult>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(checkUpResult: CheckUpResult): Long
    @Update
    suspend fun update(checkUpResult: CheckUpResult)
    @Delete
    suspend fun delete(checkUpResult: CheckUpResult)
    @Query("DELETE FROM checkupresult")
    suspend fun deleteAll()
}