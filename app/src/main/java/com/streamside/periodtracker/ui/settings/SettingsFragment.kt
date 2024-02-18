package com.streamside.periodtracker.ui.settings

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.streamside.periodtracker.DARK_MODE
import com.streamside.periodtracker.FIRST_TIME
import com.streamside.periodtracker.MainActivity.Companion.getCheckUpResultViewModel
import com.streamside.periodtracker.MainActivity.Companion.getHealthViewModel
import com.streamside.periodtracker.MainActivity.Companion.getPeriodViewModel
import com.streamside.periodtracker.MainActivity.Companion.getStepViewModel
import com.streamside.periodtracker.MainActivity.Companion.restart
import com.streamside.periodtracker.MainActivity.Companion.toDateString
import com.streamside.periodtracker.R
import com.streamside.periodtracker.notification.NotificationItem
import com.streamside.periodtracker.notification.NotificationScheduler
import java.util.Calendar

class SettingsFragment : PreferenceFragmentCompat() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Get editor
        val fa = requireActivity()
        val pref = PreferenceManager.getDefaultSharedPreferences(fa)
        val editor = pref.edit()
        val scheduler = NotificationScheduler(fa)

        // Set icon colors
        val typedValue = TypedValue()
        val theme = requireActivity().theme
        theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true)
        val iconColor = Paint()
        iconColor.color = typedValue.data
        iconColor.style = Paint.Style.FILL

        val darkModePref = findPreference<SwitchPreference>(getString(R.string.dark_mode_key))

        val randomTipPref = findPreference<SwitchPreference>(getString(R.string.random_tip_key))
        val randomTipTriggerPref = findPreference<Preference>(getString(R.string.random_tip_trigger_key))
        val randomTipTriggerTestPref = findPreference<Preference>(getString(R.string.random_tip_trigger_test_key))

        val periodStatusPref = findPreference<SwitchPreference>(getString(R.string.period_status_key))
        val periodStatusTriggerPref = findPreference<Preference>(getString(R.string.period_status_trigger_key))
        val periodStatusTriggerTestPref = findPreference<Preference>(getString(R.string.period_status_trigger_test_key))

        val firstTimePref = findPreference<Preference>(getString(R.string.first_time_key))
        val simulationPref = findPreference<SwitchPreference>(getString(R.string.simulation_key))

        darkModePref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        randomTipPref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        randomTipTriggerPref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        periodStatusPref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        periodStatusTriggerPref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        simulationPref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        randomTipTriggerTestPref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        periodStatusTriggerTestPref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        firstTimePref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)

        darkModePref?.setOnPreferenceChangeListener { _, newValue ->
            DARK_MODE = newValue as Boolean
            if (DARK_MODE) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            true
        }
        randomTipTriggerPref?.setOnPreferenceClickListener {
            confirmNotificationPermissionFirst(fa) {
                val time = Calendar.getInstance()
                val timePickerListener = OnTimeSetListener { _, h, m ->
                    time.set(Calendar.HOUR_OF_DAY, h)
                    time.set(Calendar.MINUTE, m)
                    time.set(Calendar.SECOND, 0)
                    time.set(Calendar.MILLISECOND, 0)

                    editor.putString(getString(R.string.random_tip_trigger_key), toDateString(time.time)).apply()
                    scheduler.schedule(NotificationItem(
                        getString(R.string.random_tip_key),
                        getString(R.string.random_tip_trigger_key),
                        getString(R.string.random_tip_store_key)))
                }
                TimePickerDialog(fa, timePickerListener, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), true).show()
            }
            true
        }
        periodStatusTriggerPref?.setOnPreferenceClickListener {
            confirmNotificationPermissionFirst(fa) {
                val time = Calendar.getInstance()
                val timePickerListener = OnTimeSetListener { _, h, m ->
                    time.set(Calendar.HOUR_OF_DAY, h)
                    time.set(Calendar.MINUTE, m)
                    time.set(Calendar.SECOND, 0)
                    time.set(Calendar.MILLISECOND, 0)

                    editor.putString(getString(R.string.period_status_trigger_key), toDateString(time.time)).apply()
                    scheduler.schedule(NotificationItem(
                        getString(R.string.period_status_key),
                        getString(R.string.period_status_trigger_key),
                        getString(R.string.period_status_store_key)))
                }
                TimePickerDialog(fa, timePickerListener, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), true).show()
            }
            true
        }
        simulationPref?.setOnPreferenceChangeListener { _, _ ->
            // Restart app
            restart(fa, viewLifecycleOwner)
            true
        }
        randomTipTriggerTestPref?.setOnPreferenceClickListener {
            confirmNotificationPermissionFirst(fa) {
                val trigger = Calendar.getInstance().apply { add(Calendar.SECOND, 10) }
                editor.putString(getString(R.string.random_tip_trigger_key), toDateString(trigger.time)).apply()
                scheduler.schedule(NotificationItem(
                    getString(R.string.random_tip_key),
                    getString(R.string.random_tip_trigger_key),
                    getString(R.string.random_tip_store_key)))
            }
            true
        }
        periodStatusTriggerTestPref?.setOnPreferenceClickListener {
            confirmNotificationPermissionFirst(fa) {
                val trigger = Calendar.getInstance().apply { add(Calendar.SECOND, 10) }
                editor.putString(getString(R.string.period_status_trigger_key), toDateString(trigger.time)).apply()
                scheduler.schedule(NotificationItem(
                    getString(R.string.period_status_key),
                    getString(R.string.period_status_trigger_key),
                    getString(R.string.period_status_store_key)))
            }
            true
        }
        firstTimePref?.setOnPreferenceClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
            builder.setCancelable(true)
            builder.setTitle("Confirm Log out")
            builder.setMessage("This will wipe all your data!")
            builder.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
                run {
                    val periodViewModel = getPeriodViewModel(fa)
                    val healthViewModel = getHealthViewModel(fa)
                    val stepViewModel = getStepViewModel(fa)
                    val checkUpResultViewModel = getCheckUpResultViewModel(fa)

                    // Delete all data
                    periodViewModel.deleteAll()
                    healthViewModel.deleteAll()
                    stepViewModel.deleteAll()
                    checkUpResultViewModel.deleteAll()
                    editor.clear().commit()

                    // Restart app
                    restart(fa, viewLifecycleOwner)
                }
            }
            builder.setOnCancelListener {
                FIRST_TIME = false
                editor.putBoolean(getString(R.string.first_time_key), FIRST_TIME).apply()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
            true
        }
    }

    private fun confirmNotificationPermissionFirst(fa: FragmentActivity, callback: () -> Unit) {
        if(ContextCompat.checkSelfPermission(fa, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        } else callback()
    }
}