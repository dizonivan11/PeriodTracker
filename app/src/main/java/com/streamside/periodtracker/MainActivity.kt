package com.streamside.periodtracker

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streamside.periodtracker.data.PeriodViewModel
import com.streamside.periodtracker.setup.SETUP_CURRENT_PAGE
import com.streamside.periodtracker.setup.SETUP_FRAME
import com.streamside.periodtracker.setup.SetupFragment
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

lateinit var START: Intent
lateinit var FA: FragmentActivity
lateinit var NAVVIEW: BottomNavigationView
var DARK_MODE : Boolean = false
var FIRST_TIME : Boolean = false
var LOG_PERIOD : Boolean = false
const val MAX_HISTORY = 3
const val SAFE_PERIOD_MIN = 2
const val SAFE_PERIOD_MAX = 7
const val SAFE_MIN = 23
const val SAFE_MAX = 35
const val OVULATION = 14
const val PREGNANCY_WINDOW = 9 // Possible days to get pregnant; before and up to Ovulation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        START = intent
        FA = this
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        DARK_MODE = preferences.getBoolean(getString(R.string.dark_mode_key), false)
        FIRST_TIME = preferences.getBoolean(getString(R.string.first_time_key), true)
        LOG_PERIOD = preferences.getBoolean(getString(R.string.log_period_key), false)

        if (DARK_MODE) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (FIRST_TIME || LOG_PERIOD) showSetup() else showHome()
        supportActionBar?.hide()
    }

    private fun showSetup() {
        setContentView(R.layout.activity_setup)
        SETUP_FRAME = findViewById(R.id.fcvSetup)
        replaceFragment(SetupFragment.initFragment(), R.id.fcvSetup)
    }

    private fun showHome() {
        setContentView(R.layout.activity_main)
        NAVVIEW = findViewById(R.id.nav_view)
        val nc = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_library, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(nc, appBarConfiguration)
        NAVVIEW.setupWithNavController(nc)
    }

    companion object {
        fun restart(fa: FragmentActivity, owner: LifecycleOwner) {
            val fm = fa.supportFragmentManager
            for (fragment in fm.fragments) fm.beginTransaction().remove(fragment).commit()
            SETUP_CURRENT_PAGE = 0
            clearObservers(fa, owner)
            fa.finish()
            fa.startActivity(START)
        }

        fun getPeriodViewModel(fa: FragmentActivity): PeriodViewModel {
            return ViewModelProvider(fa)[PeriodViewModel::class.java]
        }
        fun toCalendar(year: Int, month: Int, day: Int): Calendar {
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }
        }

        fun replaceFragment(newFragment: Fragment, parentViewId: Int) {
            FA.supportFragmentManager.beginTransaction().replace(parentViewId, newFragment).commit()
        }

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

        fun clearObservers(fa: FragmentActivity, owner: LifecycleOwner) {
            val pvm = getPeriodViewModel(fa)
            pvm.all.removeObservers(owner)
            pvm.lastPeriod.removeObservers(owner)
            pvm.currentPeriod.removeObservers(owner)
        }

        fun isSameYearAndMonth(date1: Calendar, date2: Calendar): Boolean {
            return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                    date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH)
        }

        fun isSameDay(date1: Calendar, date2: Calendar): Boolean {
            return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                    date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                    date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
        }

        fun isToday(date: Calendar): Boolean {
            val today = Calendar.getInstance()
            today.time = Date()

            return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
    }
}