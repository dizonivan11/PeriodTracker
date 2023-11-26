package com.streamside.periodtracker.setup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.preference.PreferenceManager
import com.streamside.periodtracker.LOG_PERIOD
import com.streamside.periodtracker.MainActivity.Companion.clearObservers
import com.streamside.periodtracker.MainActivity.Companion.replaceFragment
import com.streamside.periodtracker.MainActivity.Companion.restart
import com.streamside.periodtracker.R

lateinit var SETUP_FRAME : FragmentContainerView
var SETUP_CURRENT_PAGE = 0

open class SetupFragment : Fragment() {
    fun previousPage(fa: FragmentActivity) {
        clearObservers(fa, viewLifecycleOwner)
        SETUP_CURRENT_PAGE--
        replaceFragment(createFragment(), R.id.fcvSetup)
    }

    fun nextPage(fa: FragmentActivity) {
        clearObservers(fa, viewLifecycleOwner)
        SETUP_CURRENT_PAGE++
        replaceFragment(createFragment(), R.id.fcvSetup)
    }

    fun finalizeSetup(fa: FragmentActivity) {
        clearObservers(fa, viewLifecycleOwner)
        val preferences = PreferenceManager.getDefaultSharedPreferences(fa)

        // Set First Time settings to false
        preferences.edit().putBoolean(getString(R.string.first_time_key), false).apply()

        // Set Log Period settings to false
        preferences.edit().putBoolean(getString(R.string.log_period_key), false).apply()

        // Restart activity
        restart(fa, viewLifecycleOwner)
    }

    companion object {
        fun initFragment(): Fragment {
            return if (LOG_PERIOD) SymptomsFragment() else IntroFragment()
        }

        fun createFragment(): Fragment {
            if (LOG_PERIOD) {
                return when (SETUP_CURRENT_PAGE) {
                    // Log period starts here
                    0 -> SymptomsFragment()
                    else -> IntroFragment()
                }
            } else {
                return when (SETUP_CURRENT_PAGE) {
                    // Clean setup starts here
                    0 -> IntroFragment()
                    1 -> PeriodDateFragment()
                    2 -> MenstrualCycleFragment()
                    // Log period starts here
                    3 -> SymptomsFragment()
                    else -> IntroFragment()
                }
            }
        }
    }
}