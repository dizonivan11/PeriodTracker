package com.streamside.periodtracker.data.period

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.streamside.periodtracker.data.checkup.CheckUpList
import com.streamside.periodtracker.data.checkup.Choices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataConverters {
    @TypeConverter
    fun toDateString(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
    @TypeConverter
    fun dateFromString(date: String): Date? {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)
    }
    @TypeConverter
    fun toSymptomsString(data: SymptomList): String {
        return Gson().toJson(data)
    }
    @TypeConverter
    fun symptomsFromString(data: String): SymptomList {
        return Gson().fromJson(data, SymptomList::class.java)
    }
    @TypeConverter
    fun toCheckUpListString(checkUpList: CheckUpList): String {
        return Gson().toJson(checkUpList)
    }
    @TypeConverter
    fun checkUpListFromString(checkUpList: String): CheckUpList {
        return Gson().fromJson(checkUpList, CheckUpList::class.java)
    }
    @TypeConverter
    fun toChoicesString(choices: Choices): String {
        return Gson().toJson(choices)
    }
    @TypeConverter
    fun choicesListFromString(choices: String): Choices {
        return Gson().fromJson(choices, Choices::class.java)
    }
}