package com.streamside.periodtracker.data.period

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
            Glide.with(fragment).load(data[position].image).centerCrop().into(viewHolder.imageInsightItem)
        else
            viewHolder.imageInsightItem.setImageResource(R.drawable.default_library_image)

        viewHolder.titleInsightItem.text = data[position].title
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].insightView
    }

    override fun getItemCount() = data.size
}