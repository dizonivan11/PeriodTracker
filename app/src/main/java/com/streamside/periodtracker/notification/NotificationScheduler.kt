package com.streamside.periodtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.streamside.periodtracker.MainActivity.Companion.fromDateString
import com.streamside.periodtracker.MainActivity.Companion.timeDifference
import com.streamside.periodtracker.MainActivity.Companion.toDateString
import com.streamside.periodtracker.R
import java.util.Calendar
import java.util.Date

class NotificationScheduler(private var context: Context): INotificationScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    @RequiresApi(Build.VERSION_CODES.S)
    override fun schedule(item: NotificationItem) {
        val pref = getDefaultSharedPreferences(context)
        val editor = pref.edit()
        val today = Date()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("key", item.key)
            putExtra("timeKey", item.timeKey)
            putExtra("contentKey", item.contentKey)
        }

        var trigger = defaultTrigger()
        val storedTime = pref.getString(item.timeKey, "")
        if (!storedTime.isNullOrEmpty()) {
            trigger = Calendar.getInstance().apply { time = fromDateString(storedTime)!! }
        }

        while (timeDifference(today, trigger.time) >= 0) {
            trigger.add(Calendar.HOUR_OF_DAY, 24)
        }

        val i = NotificationItem(item.key, item.timeKey, item.contentKey)
        if (hasNotificationSet(context, item.key.hashCode())) cancel(i)
        editor.putString(item.timeKey, toDateString(trigger.time)).apply()

        alarmManager.canScheduleExactAlarms()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger.time.time,
            PendingIntent.getBroadcast(context, i.key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

        val title = if (i.key == context.getString(R.string.random_tip_key)) "Tip of the day" else "Period status"
        Log.i("Notification", "Set $title notification at ${trigger.time}")
    }

    override fun cancel(item: NotificationItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                item.key.hashCode(),
                Intent(context, NotificationReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        val title = if (item.key == context.getString(R.string.random_tip_key)) "Tip of the day" else "Period status"
        Log.i("Notification", "Cancelled $title notification")
    }

    companion object {
        fun defaultTrigger(): Calendar {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.HOUR_OF_DAY, 24)
            }
        }

        fun hasNotificationSet(context: Context, requestCode: Int): Boolean {
            return PendingIntent.getBroadcast(context, requestCode,
                Intent(context, NotificationReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE) != null
        }
    }
}