package com.streamside.periodtracker.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.streamside.periodtracker.R

class ChatAdapter(private val data: List<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatContent: TextView

        init {
            chatContent = view.findViewById(R.id.chatContent)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(viewType, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.chatContent.text = data[position].content
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position].role) {
            ChatRole.System -> R.layout.chat_system
            ChatRole.Assistant -> R.layout.chat_assistant
            ChatRole.User -> R.layout.chat_user
        }
    }

    override fun getItemCount() = data.size
}