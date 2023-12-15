package com.streamside.periodtracker.data.health

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Health(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String = "",
    var age: Int = 0,
    var weight: Int = 0, // In kilograms
    var height: Int = 0 // In inches, can be converted to centimeter by function, saved from input feet+inches
)