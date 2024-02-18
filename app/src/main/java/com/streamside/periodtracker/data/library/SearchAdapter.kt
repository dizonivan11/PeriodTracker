package com.streamside.periodtracker.data.library

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.streamside.periodtracker.R

class SearchAdapter(private val fragment: Fragment, private var data: List<Library>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val searchQuery: LinearLayout
        val searchQueryImage: ImageView
        val searchQueryTitle: TextView
        val searchQueryCategory: TextView

        init {
            searchQuery = view.findViewById(R.id.searchQuery)
            searchQueryImage = view.findViewById(R.id.searchQueryImage)
            searchQueryTitle = view.findViewById(R.id.searchQueryTitle)
            searchQueryCategory = view.findViewById(R.id.searchQueryCategory)
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
            Glide.with(viewHolder.searchQueryImage.context)
                .asBitmap()
                .load(data[position].image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(ResourcesCompat.getDrawable(fragment.resources, R.drawable.default_library_image, fragment.activity?.theme))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        viewHolder.searchQueryImage.setImageDrawable(resource.toDrawable(fragment.resources))
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        viewHolder.searchQueryImage.setImageDrawable(placeholder)
                    }
                })

        viewHolder.searchQueryTitle.text = data[position].title

        var categories = ""
        for (symptom in data[position].symptoms) {
            categories = "${categories}, $symptom"
        }
        categories = "Category: ${categories.substring(2)}"

        viewHolder.searchQueryCategory.text = categories
    }

    override fun getItemCount() = data.size
}