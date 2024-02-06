package com.streamside.periodtracker.data.step

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Step(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var date: Date? = null,
    var goal: Int = 0, // In step
    var progress: Int = 0 // In step
)
