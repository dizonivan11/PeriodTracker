package com.streamside.periodtracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streamside.periodtracker.databinding.ActivityMainBinding
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

lateinit var SETUP_PAGER : ViewPager2
private const val SETUP_PAGES = 11

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode : Boolean = preferences.getBoolean(getString(R.string.dark_mode_key), false)
        val firstTime : Boolean = preferences.getBoolean(getString(R.string.first_time_key), true)
        val logPeriod : Boolean = preferences.getBoolean(getString(R.string.log_period_key), false)

        if (darkMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (firstTime || logPeriod) {
            // TODO: First time setup wizard viewpager
            setContentView(R.layout.activity_setup)

            SETUP_PAGER = findViewById(R.id.setup_vp)
            val pagerAdapter = SetupPagerAdapter(this)
            SETUP_PAGER.adapter = pagerAdapter
            SETUP_PAGER.isUserInputEnabled = false

            // Set 4th page as starting page for logging period
            if (logPeriod)
                SETUP_PAGER.setCurrentItem(3, true)
        } else {
            setContentView(binding.root)
            val navView: BottomNavigationView = binding.navView
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
            return SETUP_PAGES
        }

        override fun createFragment(position: Int): Fragment {
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