package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.streamside.periodtracker.MainActivity.Companion.getPeriodViewModel
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.PeriodViewModel

class MenstrualCycleFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menstrual_cycle, container, false)
        val fa = requireActivity()
        periodViewModel = getPeriodViewModel(fa)

        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { newPeriod ->
            view.findViewById<Button>(R.id.submit_menstrual_cycle).setOnClickListener {
                val rgMenstrualCycle = view.findViewById<RadioGroup>(R.id.rg_menstrual_cycle)
                if (rgMenstrualCycle.checkedRadioButtonId > -1) {
                    // Update reference period menstrual cycle
                    newPeriod.menstrualCycle = view.findViewById<RadioButton>(rgMenstrualCycle.checkedRadioButtonId).text.toString()
                    periodViewModel.update(newPeriod)
                    nextPage(fa)
                } else {
                    Toast.makeText(fa, getString(R.string.ic_menstrual), Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_menstrual_cycle).setOnClickListener {
            previousPage(fa)
        }
        return view
    }
}