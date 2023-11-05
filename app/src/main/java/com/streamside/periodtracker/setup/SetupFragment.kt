package com.streamside.periodtracker.setup

import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.streamside.periodtracker.R
import com.streamside.periodtracker.SETUP_PAGER
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

open class SetupFragment : Fragment() {
    fun dayDistance(date1: Date, date2: Date): Int {
        val cal1 = Calendar.getInstance().apply {
            time = date1
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            time = date2
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return abs(TimeUnit.MILLISECONDS.toDays(cal1.time.time - cal2.time.time).toInt())
    }

    fun hasCheck(checkboxes: MutableList<CheckBox>): Boolean {
        var hasChecked = false
        for (i in 0..<checkboxes.size) {
            if (checkboxes[i].isChecked) {
                hasChecked = true
                break
            }
        }
        return hasChecked
    }

    fun getLongCheckValues(checkboxes: MutableList<CheckBox>): Long {
        var value: Long = 0
        var bitVal: Long = 1
        for (i in 0..<checkboxes.size) {
            if (checkboxes[i].isChecked) value += bitVal
            bitVal *= 2
        }
        return value
    }

    fun previousPage() {
        SETUP_PAGER.currentItem = SETUP_PAGER.currentItem - 1
    }

    fun nextPage() {
        SETUP_PAGER.currentItem = SETUP_PAGER.currentItem + 1
    }

    fun finalizeSetup(fa: FragmentActivity) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(fa)

        // Set First Time settings to false
        preferences.edit().putBoolean(getString(R.string.first_time_key), false).apply()

        // Set Log Period settings to false
        preferences.edit().putBoolean(getString(R.string.log_period_key), false).apply()

        // Restart activity
        fa.recreate()
    }
}