package com.streamside.periodtracker.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PeriodViewModel(app: Application): AndroidViewModel(app) {
    val all: LiveData<List<Period>>
    val lastPeriod: LiveData<Period>
    val currentPeriod: LiveData<Period>
    private val repository: PeriodRepository

    init {
        val dao = PeriodDatabase.getDatabase(app).periodDao()
        repository = PeriodRepository(dao)
        all = repository.all
        lastPeriod = repository.lastPeriod
        currentPeriod = repository.currentPeriod
    }

    suspend fun get(id: Long): Period {
        val deferred: Deferred<Period> = viewModelScope.async { repository.get(id) }
        return deferred.await()
    }

    fun add(period: Period): LiveData<Long> {
        val result = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(repository.add(period))
        }
        return result
    }

    fun init(lastPeriodId: Long, year: Int, month: Int, day: Int): LiveData<Long> {
        val result = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(repository.init(lastPeriodId, year, month, day))
        }
        return result
    }

    fun update(period: Period) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(period)
        }
    }

    fun delete(period: Period) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(period)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}