package com.streamside.periodtracker.data.period

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.library.Library

class InsightsAdapter(private val fragment: Fragment, private val data: List<Library>) : RecyclerView.Adapter<InsightsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cvInsightItem: CardView
        val imageInsightItem: ImageView
        val titleInsightItem: TextView

        init {
            cvInsightItem = view.findViewById(R.id.cvInsightItem)
            imageInsightItem = view.findViewById(R.id.imageInsightItem)
            titleInsightItem = view.findViewById(R.id.titleInsightItem)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(viewType, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.cvInsightItem.setOnClickListener(data[position].callback)

        if (data[position].image != "")
            Glide.with(viewHolder.imageInsightItem.context)
                .asBitmap()
                .load(data[position].image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(ResourcesCompat.getDrawable(fragment.resources, R.drawable.default_library_image, fragment.activity?.theme))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        viewHolder.imageInsightItem.setImageDrawable(resource.toDrawable(fragment.resources))
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        viewHolder.imageInsightItem.setImageDrawable(placeholder)
                    }
                })

        viewHolder.titleInsightItem.text = data[position].title
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].insightView
    }

    override fun getItemCount() = data.size
}