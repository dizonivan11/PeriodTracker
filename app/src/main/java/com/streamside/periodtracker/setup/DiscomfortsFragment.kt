package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.Period
import com.streamside.periodtracker.data.PeriodViewModel

class DiscomfortsFragment : SetupFragment() {
    private var discomforts: MutableList<CheckBox> = mutableListOf()
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var newPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_discomforts, container, false)
        val fa = requireActivity()
        discomforts.add(view.findViewById(R.id.check_discomforts_painful_menstrual_cramps))
        discomforts.add(view.findViewById(R.id.check_discomforts_pms_symptoms))
        discomforts.add(view.findViewById(R.id.check_discomforts_unusual_discharge))
        discomforts.add(view.findViewById(R.id.check_discomforts_heavy_menstrual_flow))
        discomforts.add(view.findViewById(R.id.check_discomforts_mood_swings))
        discomforts.add(view.findViewById(R.id.check_discomforts_other))
        discomforts.add(view.findViewById(R.id.check_discomforts_none))

        periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_discomforts).setOnClickListener {
                if (hasCheck(discomforts)) {
                    // Record discomforts
                    newPeriod.discomforts = getLongCheckValues(discomforts)
                    periodViewModel.update(newPeriod)
                    nextPage()
                } else {
                    Toast.makeText(fa, getString(R.string.ic_discomforts), Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_discomforts).setOnClickListener {
            previousPage()
        }

        return view
    }
}