package com.streamside.periodtracker.data.step

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class StepViewModel(app: Application): AndroidViewModel(app) {
    fun get(id: Long): LiveData<Step> = repository.get(id)
    fun getFromDate(date: Date): LiveData<Step> = repository.getFromDate(date)
    fun getFromDateBetween(date1: Date, date2: Date): LiveData<List<Step>> = repository.getFromDateBetween(date1, date2)

    val all: LiveData<List<Step>>
    private val repository: StepRepository

    init {
        val dao = StepDatabase.getDatabase(app).stepDao()
        repository = StepRepository(dao)
        all = repository.all
    }

    fun add(step: Step): LiveData<Long> {
        val result = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(repository.add(step))
        }
        return result
    }

    fun update(step: Step) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(step)
        }
    }

    fun delete(step: Step) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(step)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}