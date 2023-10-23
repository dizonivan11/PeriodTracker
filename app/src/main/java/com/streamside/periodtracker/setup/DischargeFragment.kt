package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.preference.PreferenceManager
import com.streamside.periodtracker.R

class DischargeFragment : SetupFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_discharge, container, false)
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val logPeriod : Boolean = preferences.getBoolean(getString(R.string.log_period_key), false)

        if (logPeriod) {
            // Hide back button, this will be the first page for logging period
            view.findViewById<Button>(R.id.back_discharge).visibility = View.INVISIBLE
        } else {
            view.findViewById<Button>(R.id.back_discharge).setOnClickListener {
                previousPage()
            }
        }

        view.findViewById<Button>(R.id.submit_discharge).setOnClickListener {
            nextPage()
        }

        return view
    }
}