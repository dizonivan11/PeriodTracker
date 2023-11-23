package com.streamside.periodtracker.data

import android.view.View

data class Library(
    val title: String,
    val image: Int,
    val symptoms: List<Int> = listOf(),
    val callback: (View) -> Unit
)
