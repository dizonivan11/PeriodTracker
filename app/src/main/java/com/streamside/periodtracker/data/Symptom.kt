package com.streamside.periodtracker.data

data class Symptom(
    val id: String,
    val visible: Boolean = true,
    var value: Boolean = false
)