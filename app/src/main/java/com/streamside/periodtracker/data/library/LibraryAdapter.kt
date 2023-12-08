package com.streamside.periodtracker.data.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.streamside.periodtracker.R

class LibraryAdapter(private val fragment: Fragment, private val data: List<Library>) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {
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

        if (data[position].image != "")
            Glide.with(fragment).load(data[position].image).centerCrop().into(viewHolder.imageLibraryItem)
        else
            viewHolder.imageLibraryItem.setImageResource(R.drawable.default_library_image)

        viewHolder.titleLibraryItem.text = data[position].title
    }

    override fun getItemCount() = data.size
}