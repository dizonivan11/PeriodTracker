package com.streamside.periodtracker.views

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.FragmentActivity
import com.streamside.periodtracker.R

class CounterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    private var counterValue = 0

    fun setCounterValue(fragment : FragmentActivity, oldValue: Int, newValue: Int, duration: Long) {
        val animator = ValueAnimator.ofInt(oldValue, newValue)
        animator.duration = duration
        animator.addUpdateListener { animation -> text = fragment.getString(R.string.text_percent, animation.animatedValue.toString()) }
        animator.start()
        setCounterValue(newValue)
    }

    fun setCounterValue(value: Int) {
        this.counterValue = value
    }
}