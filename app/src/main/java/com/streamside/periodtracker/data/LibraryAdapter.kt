package com.streamside.periodtracker.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.streamside.periodtracker.R

class LibraryAdapter(private val data: List<Library>) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvLibraryItem: CardView
        val imageLibraryItem: ImageView
        val titleLibraryItem: TextView

        init {
            cvLibraryItem = view.findViewById(R.id.cvLibraryItem)
            imageLibraryItem = view.findViewById(R.id.imageLibraryItem)
            titleLibraryItem = view.findViewById(R.id.titleLibraryItem)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.library_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.cvLibraryItem.setOnClickListener(data[position].callback)
        viewHolder.imageLibraryItem.setImageResource(data[position].image)
        viewHolder.titleLibraryItem.text = data[position].title
    }

    override fun getItemCount() = data.size
}