package com.streamside.periodtracker.ui.settings

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.streamside.periodtracker.DARK_MODE
import com.streamside.periodtracker.FIRST_TIME
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.PeriodViewModel

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Set icon colors
        val typedValue = TypedValue()
        val theme = requireActivity().theme
        theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true)
        val iconColor = Paint()
        iconColor.color = typedValue.data
        iconColor.style = Paint.Style.FILL

        val darkModePref = findPreference<SwitchPreference>(getString(R.string.dark_mode_key))
        val firstTimePref = findPreference<SwitchPreference>(getString(R.string.first_time_key))
        val simulationPref = findPreference<SwitchPreference>(getString(R.string.simulation_key))

        darkModePref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        firstTimePref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)
        simulationPref?.icon?.setColorFilter(iconColor.color, PorterDuff.Mode.MULTIPLY)

        darkModePref?.setOnPreferenceChangeListener { _, newValue ->
            DARK_MODE = newValue as Boolean
            if (DARK_MODE) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            true
        }
        firstTimePref?.setOnPreferenceChangeListener { _, newValue ->
            FIRST_TIME = newValue as Boolean
            if (FIRST_TIME) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
                builder.setCancelable(true)
                builder.setTitle("Confirm Data Reset")
                builder.setMessage("This will wipe all your data!")
                builder.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
                    run {
                        val periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]

                        // Clear all observers
                        periodViewModel.all.removeObservers(viewLifecycleOwner)
                        periodViewModel.lastPeriod.removeObservers(viewLifecycleOwner)
                        periodViewModel.currentPeriod.removeObservers(viewLifecycleOwner)

                        // Delete all data
                        periodViewModel.deleteAll()

                        // Restart app
                        requireActivity().recreate()
                    }
                }
                builder.setOnCancelListener {
                    FIRST_TIME = false
                    PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit()
                        .putBoolean(getString(R.string.first_time_key), FIRST_TIME).apply()
                    findPreference<SwitchPreference>(getString(R.string.first_time_key))?.isChecked = false
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
            true
        }
    }
}