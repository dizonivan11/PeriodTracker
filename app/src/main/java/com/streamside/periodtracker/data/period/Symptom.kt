package com.streamside.periodtracker.data.period

data class Symptom(
    val id: String,
    val visible: Boolean = true,
    var value: Boolean = false
)