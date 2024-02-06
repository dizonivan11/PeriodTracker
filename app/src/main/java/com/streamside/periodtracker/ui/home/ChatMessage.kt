package com.streamside.periodtracker.ui.home

data class ChatMessage (
    val role: ChatRole = ChatRole.System,
    val content: String = ""
)