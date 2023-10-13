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
import com.streamside.periodtracker.setup.DiscomfortsFragment
import com.streamside.periodtracker.setup.IntroFragment
import com.streamside.periodtracker.setup.MenstrualCycleFragment

lateinit var SETUP_PAGER : ViewPager2
private const val SETUP_PAGES = 3

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode : Boolean = preferences.getBoolean(getString(R.string.dark_mode_key), false)
        val firstTime : Boolean = preferences.getBoolean(getString(R.string.first_time_key), true)

        if (darkMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (firstTime) {
            // TODO: First time setup wizard viewpager
            setContentView(R.layout.activity_setup)

            SETUP_PAGER = findViewById(R.id.setup_vp)
            val pagerAdapter = SetupPagerAdapter(this)
            SETUP_PAGER.adapter = pagerAdapter
        } else {
            setContentView(binding.root)
            val navView: BottomNavigationView = binding.navView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_settings
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
                0 -> IntroFragment()
                1 -> MenstrualCycleFragment()
                2 -> DiscomfortsFragment()
                else -> IntroFragment()
            }
        }
    }
}