package com.streamside.periodtracker.data.health

import androidx.lifecycle.LiveData

class HealthRepository(private val dao: HealthDao) {
    fun get(id: Long): LiveData<Health> = dao.get(id)
    val all: LiveData<List<Health>> = dao.getAll()

    suspend fun add(health: Health): Long {
        return dao.add(health)
    }

    suspend fun update(health: Health) {
        dao.update(health)
    }

    suspend fun delete(health: Health) {
        dao.delete(health)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}