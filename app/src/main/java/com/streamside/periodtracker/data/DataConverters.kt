package com.streamside.periodtracker.data

import androidx.room.TypeConverter
import com.google.gson.Gson

class DataConverters {
    @TypeConverter
    fun toSymptomsString(data: SymptomList): String {
        return Gson().toJson(data)
    }
    @TypeConverter
    fun symptomsFromString(data: String): SymptomList {
        return Gson().fromJson(data, SymptomList::class.java)
    }
}