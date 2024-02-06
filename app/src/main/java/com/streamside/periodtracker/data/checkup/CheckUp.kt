package com.streamside.periodtracker.data.checkup

data class CheckUp(
    val question: String,
    val choices: Choices,
    val parentIndex: Int,
    val parentCondition: String,
    val children: CheckUpList = CheckUpList(),
    var answer: String = ""
)
