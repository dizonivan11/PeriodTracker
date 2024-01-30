package com.streamside.periodtracker.setup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.streamside.periodtracker.MainActivity.Companion.restart
import com.streamside.periodtracker.R

open class SetupFragment : Fragment() {
    fun finalizeSetup(fa: FragmentActivity) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(fa)

        // Set Log Period settings to false
        preferences.edit().putBoolean(getString(R.string.log_period_key), false).apply()

        // Set Tracker Redirect settings to true
        preferences.edit().putBoolean(getString(R.string.tracker_redirect_key), true).apply()

        // Restart activity
        restart(fa, viewLifecycleOwner)
    }
}