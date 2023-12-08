package com.streamside.periodtracker.data.health

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface HealthDao {
    @Query("SELECT * FROM health WHERE id = :id")
    fun get(id: Long): LiveData<Health>
    @Query("SELECT * FROM health")
    fun getAll(): LiveData<List<Health>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(health: Health): Long
    @Update
    suspend fun update(health: Health)
    @Delete
    suspend fun delete(health: Health)
    @Query("DELETE FROM health")
    suspend fun deleteAll()
}