package com.streamside.periodtracker.data

data class Category(
    val id: Int,
    val symptoms: List<Symptom>,
    val singleSelection: Boolean = false
)