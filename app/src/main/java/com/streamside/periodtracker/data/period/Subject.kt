package com.streamside.periodtracker.data.period

data class Subject(
    val parent: String = "",
    val children: MutableMap<String, Subject> = mutableMapOf()
)
