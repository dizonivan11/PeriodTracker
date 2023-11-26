package com.streamside.periodtracker.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.streamside.periodtracker.R

class InsightsAdapter(private val data: List<Library>) : RecyclerView.Adapter<InsightsAdapter.ViewHolder>() {
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
        viewHolder.imageInsightItem.setImageResource(data[position].image)
        viewHolder.titleInsightItem.text = data[position].title
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].insightView
    }

    override fun getItemCount() = data.size
}