package com.streamside.periodtracker.data.period

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataConverters {
    @TypeConverter
    fun toBirthDateString(birthdate: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(birthdate)
    }
    @TypeConverter
    fun birthDateFromString(birthdate: String): Date? {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(birthdate)
    }
    @TypeConverter
    fun toSymptomsString(data: SymptomList): String {
        return Gson().toJson(data)
    }
    @TypeConverter
    fun symptomsFromString(data: String): SymptomList {
        return Gson().fromJson(data, SymptomList::class.java)
    }
}