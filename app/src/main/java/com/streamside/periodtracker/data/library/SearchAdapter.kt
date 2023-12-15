package com.streamside.periodtracker.data.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.streamside.periodtracker.R

class SearchAdapter(private val fragment: Fragment, private var data: List<Library>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val searchQuery: LinearLayout
        val searchQueryImage: ImageView
        val searchQueryTitle: TextView

        init {
            searchQuery = view.findViewById(R.id.searchQuery)
            searchQueryImage = view.findViewById(R.id.searchQueryImage)
            searchQueryTitle = view.findViewById(R.id.searchQueryTitle)
        }
    }

    fun updateData(newData: List<Library>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.search_query_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.searchQuery.setOnClickListener(data[position].callback)

        if (data[position].image != "")
            Glide.with(fragment).load(data[position].image).centerCrop().into(viewHolder.searchQueryImage)

        viewHolder.searchQueryTitle.text = data[position].title
    }

    override fun getItemCount() = data.size
}