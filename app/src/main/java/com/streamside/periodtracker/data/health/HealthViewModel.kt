package com.streamside.periodtracker.data.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HealthViewModel(app: Application): AndroidViewModel(app) {
    fun get(id: Long): LiveData<Health> = repository.get(id)
    val all: LiveData<List<Health>>
    private val repository: HealthRepository

    init {
        val dao = HealthDatabase.getDatabase(app).healthDao()
        repository = HealthRepository(dao)
        all = repository.all
    }

    fun add(health: Health): LiveData<Long> {
        val result = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(repository.add(health))
        }
        return result
    }

    fun update(health: Health) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(health)
        }
    }

    fun delete(health: Health) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(health)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }
}