package com.streamside.periodtracker.setup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.streamside.periodtracker.R
import com.streamside.periodtracker.SETUP_PAGER

open class SetupFragment : Fragment() {
    fun previousPage() {
        SETUP_PAGER.setCurrentItem(SETUP_PAGER.currentItem - 1, true)
    }

    fun nextPage() {
        SETUP_PAGER.setCurrentItem(SETUP_PAGER.currentItem + 1, true)
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