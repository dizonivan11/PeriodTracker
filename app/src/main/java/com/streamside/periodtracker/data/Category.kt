package com.streamside.periodtracker.data

data class Category(
    val id: String,
    var symptoms: MutableList<Symptom>,
    val visible: Boolean = true
)