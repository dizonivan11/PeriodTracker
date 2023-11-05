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
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.Period
import com.streamside.periodtracker.data.PeriodViewModel

class RHDFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var newPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rhd, container, false)
        val fa = requireActivity()

        periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_rhd).setOnClickListener {
                val rgRHD = view.findViewById<RadioGroup>(R.id.rg_rhd)
                if (rgRHD.checkedRadioButtonId > -1) {
                    // Record RHD conditions
                    newPeriod.rhd = view.findViewById<RadioButton>(rgRHD.checkedRadioButtonId).text.toString()
                    periodViewModel.update(newPeriod)
                    movePage(fa, 7)
                } else {
                    Toast.makeText(fa, "Please select if you have any reproductive health disorders", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_rhd).setOnClickListener {
            movePage(fa, 5)
        }

        return view
    }
}