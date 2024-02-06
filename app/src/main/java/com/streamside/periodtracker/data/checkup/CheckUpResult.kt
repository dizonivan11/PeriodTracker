package com.streamside.periodtracker.data.checkup

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class CheckUpResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val date: Date,
    val list: CheckUpList
)
