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

class SleepFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var newPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)
        val fa = requireActivity()

        periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_sleep).setOnClickListener {
                val rgSleep = view.findViewById<RadioGroup>(R.id.rg_sleep)
                if (rgSleep.checkedRadioButtonId > -1) {
                    // Record sleep issue
                    newPeriod.sleep = view.findViewById<RadioButton>(rgSleep.checkedRadioButtonId).text.toString()
                    periodViewModel.update(newPeriod)
                    movePage(fa, 6)
                } else {
                    Toast.makeText(fa, "Please select if you have sleeping issue", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_sleep).setOnClickListener {
            movePage(fa, 4)
        }

        return view
    }
}