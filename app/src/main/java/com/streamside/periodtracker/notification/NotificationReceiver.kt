package com.streamside.periodtracker.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.streamside.periodtracker.MainActivity.Companion.fromDateString
import com.streamside.periodtracker.OVULATION
import com.streamside.periodtracker.PREGNANCY_WINDOW
import com.streamside.periodtracker.R
import com.streamside.periodtracker.SAFE_MAX
import com.streamside.periodtracker.SAFE_MIN
import com.streamside.periodtracker.SAFE_PERIOD_MAX
import com.streamside.periodtracker.data.API_KEY
import com.streamside.periodtracker.data.LIBRARY_TAB_NAME
import com.streamside.periodtracker.data.SHEET_ID
import com.streamside.periodtracker.data.SPREADSHEET_URL
import org.json.JSONException
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class NotificationReceiver: BroadcastReceiver() {
    private lateinit var scheduler: NotificationScheduler
    private val titles: MutableList<String> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (intent != null) {
                scheduler = NotificationScheduler(context)
                val item = NotificationItem(
                    intent.getStringExtra("key")!!,
                    intent.getStringExtra("timeKey")!!,
                    intent.getStringExtra("contentKey")!!)

                val pref = getDefaultSharedPreferences(context)
                val editor = pref.edit()
                if (pref.getBoolean(item.key, false)) {
                    when (item.key) {
                        context.getString(R.string.random_tip_key) -> {
                            getLibraryTitles(context) {
                                // Notify with the new title
                                val newTitle = titles[Random.nextInt(0, titles.size)]
                                createNotification(context, "Tip of the day", newTitle)
                                editor.putString(item.contentKey, newTitle).apply()
                                // Schedule new tip of the day notification
                                scheduler.schedule(NotificationItem(item.key, item.timeKey, item.contentKey))
                            }
                        }
                        context.getString(R.string.period_status_key) -> {
                            val lastPeriod = pref.getString(context.getString(R.string.period_status_last_period_key), "")
                            if (!lastPeriod.isNullOrEmpty()) {
                                val periodGap = TimeUnit.MILLISECONDS.toDays(timeDifference(Date(), fromDateString(lastPeriod)!!)).toInt()
                                var message = "Day $periodGap: "
                                when (periodGap) {
                                    in 0..SAFE_PERIOD_MAX ->
                                        message += context.getString(R.string.prompt_period)
                                    in SAFE_PERIOD_MAX..<PREGNANCY_WINDOW ->
                                        message += context.getString(R.string.prompt_follicular)
                                    in PREGNANCY_WINDOW..<OVULATION ->
                                        message += context.getString(R.string.prompt_pregnant)
                                    OVULATION ->
                                        message += context.getString(R.string.prompt_ovulation)
                                    in OVULATION..<SAFE_MIN ->
                                        message += context.getString(R.string.prompt_luteal)
                                    in SAFE_MIN..SAFE_MAX ->
                                        message += context.getString(R.string.prompt_regular)
                                    else ->
                                        message += context.getString(R.string.prompt_irregular)
                                }
                                // Notify with the new status
                                createNotification(context, "Period status", message)
                                editor.putString(item.contentKey, message).apply()
                                // Schedule new period status
                                scheduler.schedule(NotificationItem(item.key, item.timeKey, item.contentKey))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createNotification(context: Context, title: String, content: String) {
        val channelId = context.getString(R.string.notification_channel)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_water_drop_24)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager.notify(Date().time.toInt(), builder.build())
    }

    private fun getLibraryTitles(context: Context, callback: () -> Unit) {
        val queue = Volley.newRequestQueue(context)
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
                            titles.add(library.getString(1))
                    }
                    callback()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, {
                throw it
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun timeDifference(d1: Date, d2: Date): Long {
        return TimeUnit.MILLISECONDS.toMillis(d1.time - d2.time)
    }
}