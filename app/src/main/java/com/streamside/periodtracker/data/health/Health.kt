package com.streamside.periodtracker.data.health

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Health(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    var name: String,
    var age: Int,
    var weight: Int, // In kilograms
    var height: Int // In inches, can be converted to centimeter by function, saved from input feet+inches
)