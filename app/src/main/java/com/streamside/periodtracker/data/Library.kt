package com.streamside.periodtracker.data

import android.view.View
import com.streamside.periodtracker.R

data class Library(
    val title: String,
    val image: Int,
    val visible: Boolean = true,
    val symptoms: List<Int> = listOf(),
    val insightView: Int = R.layout.insight_item,
    val callback: (View) -> Unit
)
