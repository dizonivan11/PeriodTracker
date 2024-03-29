package com.streamside.periodtracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streamside.periodtracker.data.DataViewModel
import com.streamside.periodtracker.data.checkup.CheckUpResultViewModel
import com.streamside.periodtracker.data.health.Health
import com.streamside.periodtracker.data.health.HealthViewModel
import com.streamside.periodtracker.data.period.Period
import com.streamside.periodtracker.data.period.PeriodViewModel
import com.streamside.periodtracker.data.step.StepViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

lateinit var START: Intent
lateinit var FA: FragmentActivity
lateinit var NAVVIEW: BottomNavigationView
private var NAVIGATED_FROM_GOTO = false
var DARK_MODE : Boolean = false
var FIRST_TIME : Boolean = false
var FIRST_TIME_TRACKER : Boolean = false
var TRACKER_REDIRECT : Boolean = false
var LOG_PERIOD : Boolean = false
const val FIRST_PERIOD_START_MIN = 11
const val FIRST_PERIOD_START_MAX = 15
const val SAFE_PERIOD_MIN = 2
const val SAFE_PERIOD_MAX = 7
const val SAFE_MIN = 23
const val SAFE_MAX = 35
const val OVULATION = 14
const val PREGNANCY_WINDOW = 9 // Possible days to get pregnant; before and up to Ovulation

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        START = intent
        START.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        FA = this
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        DARK_MODE = preferences.getBoolean(getString(R.string.dark_mode_key), false)
        FIRST_TIME = preferences.getBoolean(getString(R.string.first_time_key), true)
        TRACKER_REDIRECT = preferences.getBoolean(getString(R.string.tracker_redirect_key), false)
        LOG_PERIOD = preferences.getBoolean(getString(R.string.log_period_key), false)

        if (DARK_MODE) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.activity_main)
        NAVVIEW = findViewById(R.id.nav_view)
        val nc = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home, R.id.navigation_library, R.id.navigation_settings))
        setupActionBarWithNavController(nc, appBarConfiguration)
        NAVVIEW.setupWithNavController(nc)

        NAVVIEW.setOnItemSelectedListener {
            if (NAVIGATED_FROM_GOTO) {
                NAVIGATED_FROM_GOTO = false
                nc.navigate(it.itemId)
            } else if (NAVVIEW.selectedItemId != it.itemId) {
                nc.navigate(it.itemId)
            }
            true
        }

        if (FIRST_TIME) {
            NAVVIEW.visibility = View.GONE
            goTo(R.id.navigation_intro)
        }

        else if (LOG_PERIOD)
            goTo(R.id.navigation_period_symptoms)

        else if (TRACKER_REDIRECT)
            goTo(R.id.navigation_tracker)

        else
            goTo(R.id.navigation_home)

        supportActionBar?.hide()
        createNotificationChannel(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val channelId = context.getString(R.string.notification_channel)
        val channelName = context.getString(R.string.notification_channel)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        fun restart(fa: FragmentActivity, owner: LifecycleOwner) {
            val fm = fa.supportFragmentManager
            for (fragment in fm.fragments) fm.beginTransaction().remove(fragment).commit()
            clearObservers(fa, owner)
            fa.finish()
            fa.startActivity(START)
        }

        fun getHealthViewModel(fa: FragmentActivity) = ViewModelProvider(fa)[HealthViewModel::class.java]
        fun getPeriodViewModel(fa: FragmentActivity) = ViewModelProvider(fa)[PeriodViewModel::class.java]
        fun getDataViewModel(fa: FragmentActivity) = ViewModelProvider(fa)[DataViewModel::class.java]
        fun getStepViewModel(fa: FragmentActivity) = ViewModelProvider(fa)[StepViewModel::class.java]
        fun getCheckUpResultViewModel(fa: FragmentActivity) = ViewModelProvider(fa)[CheckUpResultViewModel::class.java]

        fun toCalendar(year: Int, month: Int, day: Int): Calendar {
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }
        }

        fun goTo(selectedPage: Int) {
            NAVIGATED_FROM_GOTO = true
            FA.findNavController(R.id.nav_host_fragment_activity_main).navigate(selectedPage)
        }

        fun goTo(fa: FragmentActivity, owner: LifecycleOwner, selectedPage: Int) {
            clearObservers(fa, owner)
            NAVIGATED_FROM_GOTO = true
            FA.findNavController(R.id.nav_host_fragment_activity_main).navigate(selectedPage)
        }

        fun isNotEmptyPeriod(period: Period): Boolean {
            return period.periodYear > 0 && period.periodDay > 0 && period.menstrualCycle.isNotEmpty()
        }

        fun isNotEmptyHealthProfile(healthProfile: Health): Boolean {
            return healthProfile.name.isNotEmpty() && healthProfile.birthdate != null && healthProfile.weight > 0 && healthProfile.height > 0
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

        private fun clearObservers(fa: FragmentActivity, owner: LifecycleOwner) {
            val pvm = getPeriodViewModel(fa)
            val hvm = getHealthViewModel(fa)
            pvm.all.removeObservers(owner)
            pvm.lastPeriod.removeObservers(owner)
            pvm.currentPeriod.removeObservers(owner)
            hvm.all.removeObservers(owner)
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

        fun toDateString(date: Date): String = SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss", Locale.getDefault()).format(date)
        fun fromDateString(date: String): Date? = SimpleDateFormat("dd/MM/yyyy'T'HH:mm:ss", Locale.getDefault()).parse(date)
        fun timeDifference(d1: Date, d2: Date): Long {
            return TimeUnit.MILLISECONDS.toMillis(d1.time - d2.time)
        }
    }
}