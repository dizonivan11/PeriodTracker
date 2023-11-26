package com.streamside.periodtracker.data

data class Symptom(
    val id: Int,
    val icon: Int = -1,
    var value: Boolean = false
)