package com.streamside.periodtracker.data.checkup

import androidx.lifecycle.LiveData
import java.util.Date

class CheckUpResultRepository(private val dao: CheckUpResultDao) {
    fun get(date: Date): LiveData<CheckUpResult> = dao.get(date)
    val all: LiveData<List<CheckUpResult>> = dao.getAll()

    suspend fun add(checkUpResult: CheckUpResult): Long {
        return dao.add(checkUpResult)
    }

    suspend fun update(checkUpResult: CheckUpResult) {
        dao.update(checkUpResult)
    }

    suspend fun delete(checkUpResult: CheckUpResult) {
        dao.delete(checkUpResult)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}