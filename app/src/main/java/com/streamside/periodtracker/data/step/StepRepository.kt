package com.streamside.periodtracker.data.step

import androidx.lifecycle.LiveData
import java.util.Date

class StepRepository(private val dao: StepDao) {
    fun get(id: Long): LiveData<Step> = dao.get(id)
    fun getFromDate(date: Date): LiveData<Step> = dao.getFromDate(date)
    fun getFromDateBetween(date1: Date, date2: Date): LiveData<List<Step>> = dao.getFromDateBetween(date1, date2)

    val all: LiveData<List<Step>> = dao.getAll()

    suspend fun add(step: Step): Long {
        return dao.add(step)
    }

    suspend fun update(step: Step) {
        dao.update(step)
    }

    suspend fun delete(step: Step) {
        dao.delete(step)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}