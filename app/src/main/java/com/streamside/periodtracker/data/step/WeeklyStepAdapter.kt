package com.streamside.periodtracker.data.step

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.streamside.periodtracker.R
import com.streamside.periodtracker.ui.step.ANIMATION_DURATION
import java.util.Calendar
import java.util.Date

class WeeklyStepAdapter(private val data: List<Step>) : RecyclerView.Adapter<WeeklyStepAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvWeeklyProgress: CircularProgressIndicator
        val tvWeeklyNumber: TextView

        init {
            tvWeeklyProgress = view.findViewById(R.id.tvWeeklyProgress)
            tvWeeklyNumber = view.findViewById(R.id.tvWeeklyNumber)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val c = Calendar.getInstance().apply { time = data[position].date!! }
        val t = Calendar.getInstance().apply { time = Date() }

        return if (c.get(Calendar.YEAR) == t.get(Calendar.YEAR) &&
            c.get(Calendar.MONTH) == t.get(Calendar.MONTH) &&
            c.get(Calendar.DAY_OF_MONTH) == t.get(Calendar.DAY_OF_MONTH))
            R.layout.step_weekly_today_item
        else
            R.layout.step_weekly_item
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(viewType, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        setProgress(viewHolder.tvWeeklyProgress, ((data[position].progress.toDouble() / data[position].goal.toDouble()) * 100).toInt())
        val c = Calendar.getInstance().apply { time = data[position].date!! }
        viewHolder.tvWeeklyNumber.text = c.get(Calendar.DAY_OF_MONTH).toString()
    }

    override fun getItemCount() = data.size

    private fun setProgress(progressBar: CircularProgressIndicator, newValue: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, newValue).apply {
            this.duration = ANIMATION_DURATION
            start()
        }
    }
}