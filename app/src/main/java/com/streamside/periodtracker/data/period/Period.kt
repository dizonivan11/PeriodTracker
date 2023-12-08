package com.streamside.periodtracker.data.period

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Period(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    var lastPeriodId: Long,
    var nextPeriodId: Long,
    var periodYear: Int,
    var periodMonth: Int,
    var periodDay: Int,
    var periodEndYear: Int,
    var periodEndMonth: Int,
    var periodEndDay: Int,
    var menstrualCycle: String,
    var symptoms: SymptomList = SymptomList(mutableListOf())
)