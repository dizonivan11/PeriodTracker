package com.streamside.periodtracker.data.health

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Health(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val age: Int,
    var weight: Int,
    var height: Float
)
