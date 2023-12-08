package com.streamside.periodtracker.data.library

import android.view.View
import com.streamside.periodtracker.R

data class Library(
    val title: String,
    val image: String,
    val visible: Boolean = true,
    val symptoms: List<String> = listOf(),
    val insightView: Int = R.layout.insight_item,
    val callback: (View) -> Unit
)
