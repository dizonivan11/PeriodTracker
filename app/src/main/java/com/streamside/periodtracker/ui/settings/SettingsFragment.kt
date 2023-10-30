package com.streamside.periodtracker.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.streamside.periodtracker.DARK_MODE
import com.streamside.periodtracker.FIRST_TIME
import com.streamside.periodtracker.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<SwitchPreference>(getString(R.string.dark_mode_key))?.setOnPreferenceChangeListener { _, newValue ->
            DARK_MODE = newValue as Boolean
            if (DARK_MODE) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            true
        }
        findPreference<SwitchPreference>(getString(R.string.first_time_key))?.setOnPreferenceChangeListener { _, newValue ->
            FIRST_TIME = newValue as Boolean
            true
        }
    }
}