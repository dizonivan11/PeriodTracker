package com.streamside.periodtracker.setup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.streamside.periodtracker.MainActivity.Companion.restart
import com.streamside.periodtracker.R

open class SetupFragment : Fragment() {
    fun finalizeSetup(fa: FragmentActivity) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(fa)

        // Set First Time settings to false
        preferences.edit().putBoolean(getString(R.string.first_time_key), false).apply()

        // Set Log Period settings to false
        preferences.edit().putBoolean(getString(R.string.log_period_key), false).apply()

        // Restart activity
        restart(fa, viewLifecycleOwner)
    }
}