package com.streamside.periodtracker.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.streamside.periodtracker.FA
import com.streamside.periodtracker.FIRST_TIME_TRACKER
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.checkup.CheckUp
import com.streamside.periodtracker.data.checkup.CheckUpList
import com.streamside.periodtracker.data.checkup.Choices
import com.streamside.periodtracker.data.library.Library
import com.streamside.periodtracker.data.period.Category
import com.streamside.periodtracker.data.period.Subject
import com.streamside.periodtracker.data.period.Symptom
import com.streamside.periodtracker.data.period.SymptomList
import com.streamside.periodtracker.ui.library.WEB_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException

const val SPREADSHEET_URL = "https://sheets.googleapis.com/v4/spreadsheets/%s/values/%s?alt=json&key=%s"
const val SHEET_ID = "1LznrVMSxYMLfVgCM4Jguv8axvP3LBdPWGOrW-b7YT08"
const val LIBRARY_TAB_NAME = "List"
const val SYMPTOMS_TAB_NAME = "Symptoms"
const val CHECKUP_TAB_NAME = "Checkup"
const val API_KEY = "AIzaSyBU5VZ7bb1L6Z9rJnO91CYzJVeTahkx3xQ"

class DataViewModel(app: Application): AndroidViewModel(app) {
    fun newSymptomsData(): LiveData<SymptomList> {
        val result = MutableLiveData<SymptomList>()
        viewModelScope.launch(Dispatchers.IO) {
            var lastCategory: Category? = null
            val list = SymptomList(mutableListOf())
            val queue = Volley.newRequestQueue(FA)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                String.format(SPREADSHEET_URL, SHEET_ID, SYMPTOMS_TAB_NAME, API_KEY),
                null,
                { response ->
                    try {
                        val root = response.getJSONArray("values")
                        for (i in 0 until root.length()) {
                            val symptom = root.getJSONArray(i)
                            val id = symptom.getString(0)
                            val type = symptom.getString(1)
                            val visible = if (symptom.length() > 2 && symptom.getString(2).isNotEmpty()) symptom.getBoolean(2) else true
                            if (type == "Category") {
                                val newCategory = Category(symptom.getString(0), mutableListOf(), visible)
                                list.categories.add(newCategory)
                                lastCategory = newCategory
                            } else if (type == "Symptom") {
                                lastCategory?.symptoms?.add(Symptom(id, visible))
                            }
                        }
                        result.postValue(list)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, {
                    throw it
                }
            )
            queue.add(jsonObjectRequest)
        }
        return result
    }

    fun getSymptomsData(): LiveData<Map<String, Subject>> {
        val result = MutableLiveData<Map<String, Subject>>()
        viewModelScope.launch(Dispatchers.IO) {
            var lastCategory = ""
            val list: MutableMap<String, Subject> = mutableMapOf()
            val queue = Volley.newRequestQueue(FA)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                String.format(SPREADSHEET_URL, SHEET_ID, SYMPTOMS_TAB_NAME, API_KEY),
                null,
                { response ->
                    try {
                        val root = response.getJSONArray("values")
                        for (i in 0 until root.length()) {
                            val symptom = root.getJSONArray(i)
                            val id = symptom.getString(0)
                            val type = symptom.getString(1)
                            if (type == "Category") {
                                list[id] = Subject()
                                lastCategory = id
                            } else if (type == "Symptom") {
                                if (lastCategory.isNotEmpty())
                                    list[lastCategory]?.children?.set(id, Subject(lastCategory))
                            }
                        }
                        result.postValue(list)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, {
                    throw it
                }
            )
            queue.add(jsonObjectRequest)
        }
        return result
    }

    fun getLibraryData(): LiveData<List<Library>> {
        val result = MutableLiveData<List<Library>>()
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf(
                Library("Search for word or term on dictionary",
                    "https://images.pexels.com/photos/159581/dictionary-reference-book-learning-meaning-159581.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"
                ) {
                    goTo(R.id.navigation_dictionary)
                },
                Library("Log your symptoms",
                    "https://images.pexels.com/photos/6942026/pexels-photo-6942026.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
                    false, listOf(), R.layout.insight_symptoms_item
                ) {
                    FIRST_TIME_TRACKER = false
                    goTo(R.id.navigation_period_symptoms)
                },
            )
            val queue = Volley.newRequestQueue(FA)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                String.format(SPREADSHEET_URL, SHEET_ID, LIBRARY_TAB_NAME, API_KEY),
                null,
                { response ->
                    try {
                        val root = response.getJSONArray("values")
                        for (i in 1 until root.length()) {
                            val library = root.getJSONArray(i)
                            val symptoms = library.getString(0).split(",")
                            val title = if (library.length() > 1) library.getString(1) else ""
                            val url = if (library.length() > 2) library.getString(2) else ""
                            val image = if (library.length() > 3) library.getString(3) else ""
                            list.add(Library(title, image, true, symptoms) {
                                WEB_URL = url
                                goTo(R.id.navigation_web)
                            })
                        }
                        result.postValue(list)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, {
                    throw it
                }
            )
            queue.add(jsonObjectRequest)
        }
        return result
    }

    fun getCheckUpData(): LiveData<CheckUpList> {
        val result = MutableLiveData<CheckUpList>()
        viewModelScope.launch(Dispatchers.IO) {
            val list = CheckUpList()
            val parentReferences: MutableMap<Int, CheckUp> = mutableMapOf()
            val queue = Volley.newRequestQueue(FA)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                String.format(SPREADSHEET_URL, SHEET_ID, CHECKUP_TAB_NAME, API_KEY),
                null,
                { response ->
                    try {
                        val root = response.getJSONArray("values")
                        for (i in 0 until root.length()) {
                            val checkup = root.getJSONArray(i)
                            val parentIndex = if (checkup.length() > 0 && checkup.getString(0).isNotEmpty()) checkup.getInt(0) - 1 else -1
                            val parentCondition = if (checkup.length() > 1) checkup.getString(1) else ""
                            val question = if (checkup.length() > 2) checkup.getString(2) else ""

                            val choices = Choices()
                            if (checkup.length() > 3)
                                for (c in 3..<checkup.length())
                                    choices.list.add(checkup.getString(c))

                            val item = CheckUp(question, choices, parentIndex, parentCondition)
                            parentReferences[i] = item
                            if (parentIndex > -1) parentReferences[parentIndex]?.children?.list?.add(item)
                            else list.list.add(item)
                        }
                        result.postValue(list)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, {
                    throw it
                }
            )
            queue.add(jsonObjectRequest)
        }
        return result
    }
}