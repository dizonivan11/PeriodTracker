package com.streamside.periodtracker.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.preference.PreferenceManager
import com.streamside.periodtracker.R

class MenstrualCycleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menstrual_cycle, container, false)
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        view.findViewById<Button>(R.id.submit_menstrual_cycle).setOnClickListener {
            // Set First Time settings to false
            preferences.edit().putBoolean(getString(R.string.first_time_key), false).apply()

            // Restart activity
            requireActivity().recreate()
        }

        return view
    }
}