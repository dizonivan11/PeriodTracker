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

class MenstrualCycleFragment : SetupFragment() {
    private lateinit var newPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menstrual_cycle, container, false)
        val fa = requireActivity()
        PERIOD_VIEW_MODEL.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_menstrual_cycle).setOnClickListener {
                val rgMenstrualCycle = view.findViewById<RadioGroup>(R.id.rg_menstrual_cycle)
                if (rgMenstrualCycle.checkedRadioButtonId > -1) {
                    // Update reference period menstrual cycle
                    newPeriod.menstrualCycle = view.findViewById<RadioButton>(rgMenstrualCycle.checkedRadioButtonId).text.toString()
                    PERIOD_VIEW_MODEL.update(newPeriod)
                    nextPage()
                } else {
                    Toast.makeText(fa, getString(R.string.ic_menstrual), Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_menstrual_cycle).setOnClickListener {
            previousPage()
        }

        return view
    }
}