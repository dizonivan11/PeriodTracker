package com.streamside.periodtracker.data

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
    var menstrualCycle: String,
    var discharge: String,
    var discomforts: Long,
    var fitness: String,
    var mental: Long,
    var rhd: String,
    var sex: Long,
    var skin: Long,
    var sleep: String
)