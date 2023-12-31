package com.streamside.periodtracker.data.period

import androidx.lifecycle.LiveData

class PeriodRepository(private val dao: PeriodDao) {
    fun get(id: Long): LiveData<Period> = dao.get(id)
    val all: LiveData<List<Period>> = dao.getAll()
    val lastPeriod: LiveData<Period> = dao.getLastPeriod()
    val currentPeriod: LiveData<Period> = dao.getCurrentPeriod()

    suspend fun add(period: Period): Long {
        return dao.add(period)
    }

    suspend fun init(lastPeriodId: Long, year: Int, month: Int, day: Int): Long {
        return dao.add(
            Period(0,
            lastPeriodId,
            -1,
            year, month, day,
            0,
            0,
            0,
            ""
        )
        )
    }

    suspend fun update(period: Period) {
        dao.update(period)
    }

    suspend fun delete(period: Period) {
        dao.delete(period)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}