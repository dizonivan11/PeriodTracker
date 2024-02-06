package com.streamside.periodtracker.data.checkup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class CheckUpResultViewModel(app: Application): AndroidViewModel(app) {
    fun get(date: Date): LiveData<CheckUpResult> = repository.get(date)
    val all: LiveData<List<CheckUpResult>>
    private val repository: CheckUpResultRepository

    init {
        val dao = CheckUpResultDatabase.getDatabase(app).checkUpResultDao()
        repository = CheckUpResultRepository(dao)
        all = repository.all
    }

    fun add(checkUpResult: CheckUpResult): LiveData<Long> {
        val result = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(repository.add(checkUpResult))
        }
        return result
    }

    fun update(checkUpResult: CheckUpResult) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(checkUpResult)
        }
    }

    fun delete(checkUpResult: CheckUpResult) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(checkUpResult)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}