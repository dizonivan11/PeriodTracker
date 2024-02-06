package com.streamside.periodtracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.streamside.periodtracker.MainActivity.Companion.fromDateString
import com.streamside.periodtracker.MainActivity.Companion.toDateString
import com.streamside.periodtracker.data.API_KEY
import com.streamside.periodtracker.data.LIBRARY_TAB_NAME
import com.streamside.periodtracker.data.SHEET_ID
import com.streamside.periodtracker.data.SPREADSHEET_URL
import org.json.JSONException
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.random.Random

private const val TAG = "Notification"
private const val NOTIFICATION_UPDATE_RATE = 10000L //  update rate in milliseconds (1 sec = 1000 milliseconds)

private const val DEFAULT_HOUR_INTERVAL = 24 // next trigger hour component
private const val DEFAULT_MINUTE_INTERVAL = 0 // next trigger minute component
private const val DEFAULT_SECOND_INTERVAL = 0 // next trigger second component

class NotificationService : Service() {
    private lateinit var handler: Handler
    private lateinit var timer: Timer
    private lateinit var task: TimerTask
    private val libraryTitles: MutableList<String> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        getLibraryTitles()
        handler = Handler(applicationContext.mainLooper)
        timer = Timer()
        task = object : TimerTask() { override fun run() { handler.post { main() } } }
        timer.schedule(task, 0, NOTIFICATION_UPDATE_RATE)
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun main() {
        val sharedPreferences = getDefaultSharedPreferences(applicationContext)
        val sharedPreferencesEdit = sharedPreferences.edit()
        val channel = NotificationChannel(
            getString(R.string.notification_channel),
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        if (sharedPreferences.getBoolean(getString(R.string.random_tip_key), false)) {
            Log.i(TAG, "Evaluating notification for random tip of the day...")
            val todayDate = Calendar.getInstance()
            val randomTipTrigger = sharedPreferences.getString(getString(R.string.random_tip_trigger_key), "")
            val title = sharedPreferences.getString(getString(R.string.random_tip_title_key), "")

            if (!title.isNullOrEmpty()) {
                if (randomTipTrigger.isNullOrEmpty()) {
                    // Create default trigger date and time if not set on settings
                    val triggerDate = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }
                    Log.i(TAG, "Trigger date: ${triggerDate.time}")
                    while (timeDifference(todayDate.time, triggerDate.time) >= 0) {
                        Log.i(TAG, "First trigger was past, adding intervals...")
                        // if today is past the default trigger hour, add a default hour interval and trigger it at next interval
                        triggerDate.add(Calendar.HOUR_OF_DAY, DEFAULT_HOUR_INTERVAL)
                        triggerDate.add(Calendar.MINUTE, DEFAULT_MINUTE_INTERVAL)
                        triggerDate.add(Calendar.SECOND, DEFAULT_SECOND_INTERVAL)
                    }
                    Log.i(TAG, "First trigger created")
                    sharedPreferencesEdit.putString(getString(R.string.random_tip_trigger_key), toDateString(triggerDate.time)).apply()
                } else {
                    val bypass = sharedPreferences.getBoolean(getString(R.string.random_tip_trigger_test_key), false)
                    val triggerDate = Calendar.getInstance().apply { time = fromDateString(randomTipTrigger)!! }
                    val triggerGap = timeDifference(todayDate.time, triggerDate.time)
                    Log.i(TAG, "Tip of the day: ${TimeUnit.MILLISECONDS.toSeconds(abs(triggerGap))} seconds left for the next trigger")

                    if (triggerGap >= 0 || bypass) {
                        val newTitle = libraryTitles[Random.nextInt(0, libraryTitles.size)]
                        sharedPreferencesEdit.putString(getString(R.string.random_tip_title_key), newTitle).apply()
                        notify(notificationManager, "Tip of the day", newTitle)

                        // Set the next trigger date
                        val nextTriggerDate = triggerDate.apply {
                            add(Calendar.HOUR_OF_DAY, DEFAULT_HOUR_INTERVAL)
                            add(Calendar.MINUTE, DEFAULT_MINUTE_INTERVAL)
                            add(Calendar.SECOND, DEFAULT_SECOND_INTERVAL)
                        }
                        sharedPreferencesEdit.putBoolean(getString(R.string.random_tip_trigger_test_key), false).apply()
                        sharedPreferencesEdit.putString(getString(R.string.random_tip_trigger_key), toDateString(nextTriggerDate.time)).apply()
                        Log.i(TAG, "Tip of the day: Bypassed? $bypass")
                        Log.i(TAG, "Tip of the day: Next trigger date: ${nextTriggerDate.time}")
                    }
                }
            } else {
                // If the app hasn't load to Home yet, pick a random tip now
                sharedPreferencesEdit.putString(
                    getString(R.string.random_tip_title_key),
                    libraryTitles[Random.nextInt(0, libraryTitles.size)]).apply()
            }
        }

        if (sharedPreferences.getBoolean(getString(R.string.period_status_key), false)) {
            Log.i(TAG, "Evaluating notification for period status...")
            val todayDate = Calendar.getInstance()
            val periodStatusTrigger = sharedPreferences.getString(getString(R.string.period_status_trigger_key), "")
            val lastPeriod = sharedPreferences.getString(getString(R.string.period_status_last_period_key), "")

            if (!lastPeriod.isNullOrEmpty()) {
                if (periodStatusTrigger.isNullOrEmpty()) {
                    Log.i(TAG, "Creating first trigger...")
                    // Create default trigger date and time if not set on settings
                    val triggerDate = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }
                    Log.i(TAG, "Trigger date: ${triggerDate.time}")
                    while (timeDifference(todayDate.time, triggerDate.time) >= 0) {
                        Log.i(TAG, "First trigger was past, adding intervals...")
                        // if today is past the default trigger hour, add a default hour interval and trigger it at next interval
                        triggerDate.add(Calendar.HOUR_OF_DAY, DEFAULT_HOUR_INTERVAL)
                        triggerDate.add(Calendar.MINUTE, DEFAULT_MINUTE_INTERVAL)
                        triggerDate.add(Calendar.SECOND, DEFAULT_SECOND_INTERVAL)
                    }
                    Log.i(TAG, "First trigger created")
                    sharedPreferencesEdit.putString(getString(R.string.period_status_trigger_key), toDateString(triggerDate.time)).apply()
                } else {
                    val bypass = sharedPreferences.getBoolean(getString(R.string.period_status_trigger_test_key), false)
                    val triggerDate = Calendar.getInstance().apply { time = fromDateString(periodStatusTrigger)!! }
                    val triggerGap = timeDifference(todayDate.time, triggerDate.time)
                    Log.i(TAG, "Period status: ${TimeUnit.MILLISECONDS.toSeconds(abs(triggerGap))} seconds left for the next trigger")

                    if (triggerGap >= 0 || bypass) {
                        val lastPeriodDate = Calendar.getInstance().apply { time = fromDateString(lastPeriod)!! }
                        val periodGap = TimeUnit.MILLISECONDS.toDays(timeDifference(todayDate.time, lastPeriodDate.time)).toInt()
                        var message = "Day $periodGap: "
                        when (periodGap) {
                            in 0..SAFE_PERIOD_MAX ->
                                message += getString(R.string.prompt_period)
                            in SAFE_PERIOD_MAX..<PREGNANCY_WINDOW ->
                                message += getString(R.string.prompt_follicular)
                            in PREGNANCY_WINDOW..<OVULATION ->
                                message += getString(R.string.prompt_pregnant)
                            OVULATION ->
                                message += getString(R.string.prompt_ovulation)
                            in OVULATION..<SAFE_MIN ->
                                message += getString(R.string.prompt_luteal)
                            in SAFE_MIN..SAFE_MAX ->
                                message += getString(R.string.prompt_regular)
                            else ->
                                message += getString(R.string.prompt_irregular)
                        }
                        if (message.isNotEmpty()) {
                            notify(notificationManager, "Period Status", message)
                            // Set the next trigger date
                            val nextTriggerDate = triggerDate.apply {
                                add(Calendar.HOUR_OF_DAY, DEFAULT_HOUR_INTERVAL)
                                add(Calendar.MINUTE, DEFAULT_MINUTE_INTERVAL)
                                add(Calendar.SECOND, DEFAULT_SECOND_INTERVAL)
                            }
                            sharedPreferencesEdit.putBoolean(getString(R.string.period_status_trigger_test_key), false).apply()
                            sharedPreferencesEdit.putString(getString(R.string.period_status_trigger_key), toDateString(nextTriggerDate.time)).apply()
                            Log.i(TAG, "Period status: Bypassed? $bypass")
                            Log.i(TAG, "Period status: Next trigger date: ${nextTriggerDate.time}")
                        }
                    }
                }
            }
        }
    }

    private fun getLibraryTitles() {
        val queue = Volley.newRequestQueue(FA)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            String.format(SPREADSHEET_URL, SHEET_ID, LIBRARY_TAB_NAME, API_KEY),
            null,
            { response ->
                try {
                    val root = response.getJSONArray("values")
                    for (i in 1 until root.length()) {
                        val library = root.getJSONArray(i)
                        if (library.length() > 1)
                            libraryTitles.add(library.getString(1))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, {
                throw it
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun notify(notificationManager: NotificationManager, title: String, message: String, intent: PendingIntent? = null) {
        val notificationBuilder = NotificationCompat.Builder(applicationContext, getString(R.string.notification_channel))
            .setSmallIcon(R.drawable.baseline_water_drop_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
        if (intent != null) {
            notificationBuilder.setContentIntent(intent)
        }
        notificationManager.notify(Date().time.toInt(), notificationBuilder.build())
    }

    private fun timeDifference(d1: Date, d2: Date): Long {
        return TimeUnit.MILLISECONDS.toMillis(d1.time - d2.time)
    }
}