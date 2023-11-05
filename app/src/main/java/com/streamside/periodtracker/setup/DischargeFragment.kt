package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.Period
import com.streamside.periodtracker.data.PeriodViewModel
import java.util.Calendar
import java.util.Date

class DischargeFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var newPeriod: Period
    private lateinit var lastPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_discharge, container, false)
        val fa = requireActivity()
        val preferences = PreferenceManager.getDefaultSharedPreferences(fa)
        val logPeriod : Boolean = preferences.getBoolean(getString(R.string.log_period_key), false)

        periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            if (logPeriod) {
                // Hide back button, this will be the first page for logging period
                view.findViewById<Button>(R.id.back_discharge).visibility = View.INVISIBLE

                periodViewModel.all.observe(viewLifecycleOwner) { periods ->
                    // Get last period
                    for (i in 0..periods.size) {
                        if (periods[i].id == newPeriod.lastPeriodId) {
                            lastPeriod = periods[i]
                            break
                        }
                    }

                    // Get last period date
                    val lastPeriodDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, lastPeriod.periodYear)
                        set(Calendar.MONTH, lastPeriod.periodMonth)
                        set(Calendar.DAY_OF_MONTH, lastPeriod.periodDay)
                    }

                    // Record menstrual cycle condition
                    val today = Calendar.getInstance().apply { Date() }
                    val periodGap = dayDistance(today.time, lastPeriodDate.time)
                    if (periodGap in 21..35)
                        newPeriod.menstrualCycle = getString(R.string.menstrual_cycle_regular)
                    else
                        newPeriod.menstrualCycle = getString(R.string.menstrual_cycle_irregular)
                }
            } else {
                view.findViewById<Button>(R.id.back_discharge).setOnClickListener {
                    previousPage()
                }
            }

            view.findViewById<Button>(R.id.submit_discharge).setOnClickListener {
                val rgDischarge = view.findViewById<RadioGroup>(R.id.rg_discharge)
                if (rgDischarge.checkedRadioButtonId > -1) {
                    // Record discharge color
                    newPeriod.discharge = view.findViewById<RadioButton>(rgDischarge.checkedRadioButtonId).text.toString()
                    periodViewModel.update(newPeriod)
                    nextPage()
                } else {
                    Toast.makeText(fa, getString(R.string.ic_discharge), Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}