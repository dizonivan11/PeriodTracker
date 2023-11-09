package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.streamside.periodtracker.PERIOD_VIEW_MODEL
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.Period

class SleepFragment : SetupFragment() {
    private lateinit var newPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)
        val fa = requireActivity()
        PERIOD_VIEW_MODEL.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_sleep).setOnClickListener {
                val rgSleep = view.findViewById<RadioGroup>(R.id.rg_sleep)
                if (rgSleep.checkedRadioButtonId > -1) {
                    // Record sleep issue
                    newPeriod.sleep = view.findViewById<RadioButton>(rgSleep.checkedRadioButtonId).text.toString()
                    PERIOD_VIEW_MODEL.update(newPeriod)
                    nextPage()
                } else {
                    Toast.makeText(fa, getString(R.string.ic_sleep), Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_sleep).setOnClickListener {
            previousPage()
        }

        return view
    }
}