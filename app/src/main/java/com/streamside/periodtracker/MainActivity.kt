package com.streamside.periodtracker

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.streamside.periodtracker.databinding.ActivityMainBinding
import com.streamside.periodtracker.setup.IntroFragment

private const val SETUP_PAGES = 1

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val firstTime : Boolean = preferences.getBoolean(getString(R.string.first_time_key), true)

        if (firstTime) {
            // TODO: First time setup wizard viewpager
            setContentView(R.layout.activity_setup)

            val viewPager : ViewPager2 = findViewById(R.id.setup_vp)
            val pagerAdapter = SetupPagerAdapter(this)
            viewPager.adapter = pagerAdapter

            // Set First Time settings to false
            // preferences.edit().putBoolean(getString(R.string.first_time_key), false).apply()
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
                else -> IntroFragment()
            }
        }

    }
}