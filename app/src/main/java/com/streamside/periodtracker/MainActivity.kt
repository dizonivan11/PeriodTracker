package com.streamside.periodtracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streamside.periodtracker.data.PeriodViewModel
import com.streamside.periodtracker.setup.DischargeFragment
import com.streamside.periodtracker.setup.DiscomfortsFragment
import com.streamside.periodtracker.setup.FitnessFragment
import com.streamside.periodtracker.setup.IntroFragment
import com.streamside.periodtracker.setup.MenstrualCycleFragment
import com.streamside.periodtracker.setup.MentalHealthFragment
import com.streamside.periodtracker.setup.PeriodDateFragment
import com.streamside.periodtracker.setup.RHDFragment
import com.streamside.periodtracker.setup.SexLifeFragment
import com.streamside.periodtracker.setup.SkinFragment
import com.streamside.periodtracker.setup.SleepFragment

lateinit var PERIOD_VIEW_MODEL : PeriodViewModel
lateinit var SETUP_PAGER : ViewPager2
var DARK_MODE : Boolean = false
var FIRST_TIME : Boolean = false
var LOG_PERIOD : Boolean = false
private const val SETUP_PAGES = 11
const val SAFE_MIN = 23
const val SAFE_MAX = 35

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        DARK_MODE = preferences.getBoolean(getString(R.string.dark_mode_key), false)
        FIRST_TIME = preferences.getBoolean(getString(R.string.first_time_key), true)
        LOG_PERIOD = preferences.getBoolean(getString(R.string.log_period_key), false)
        PERIOD_VIEW_MODEL = ViewModelProvider(this)[PeriodViewModel::class.java]

        if (DARK_MODE) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (FIRST_TIME || LOG_PERIOD) {
            setContentView(R.layout.activity_setup)

            SETUP_PAGER = findViewById(R.id.setup_vp)
            val pagerAdapter = SetupPagerAdapter(this)
            SETUP_PAGER.adapter = pagerAdapter
            SETUP_PAGER.isUserInputEnabled = false
        } else {
            setContentView(R.layout.activity_main)
            val navView: BottomNavigationView = findViewById(R.id.nav_view)
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home, R.id.navigation_library, R.id.navigation_settings
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }
        supportActionBar?.hide()
    }

    inner class SetupPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return if (LOG_PERIOD)
                SETUP_PAGES - 3
            else
                SETUP_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            if (LOG_PERIOD) {
                return when (position) {
                    // Log period starts here
                    0 -> DischargeFragment()
                    1 -> DiscomfortsFragment()
                    2 -> SleepFragment()
                    3 -> RHDFragment()
                    4 -> MentalHealthFragment()
                    5 -> SexLifeFragment()
                    6 -> FitnessFragment()
                    7 -> SkinFragment()
                    else -> IntroFragment()
                }
            } else {
                return when (position) {
                    // Clean setup starts here
                    0 -> IntroFragment()
                    1 -> PeriodDateFragment()
                    2 -> MenstrualCycleFragment()
                    // Log period starts here
                    3 -> DischargeFragment()
                    4 -> DiscomfortsFragment()
                    5 -> SleepFragment()
                    6 -> RHDFragment()
                    7 -> MentalHealthFragment()
                    8 -> SexLifeFragment()
                    9 -> FitnessFragment()
                    10 -> SkinFragment()
                    else -> IntroFragment()
                }
            }
        }
    }
}