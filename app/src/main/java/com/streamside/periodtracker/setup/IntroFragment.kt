package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.streamside.periodtracker.MainActivity.Companion.getPeriodViewModel
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.period.PeriodViewModel

class IntroFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro, container, false)
        val fa = requireActivity()
        periodViewModel = getPeriodViewModel(fa)

        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { referencePeriod ->
            view.findViewById<Button>(R.id.submit_intro).setOnClickListener {
                if (referencePeriod == null) {
                    // Initialize reference period
                    periodViewModel.init(-1, 0, 0, 0).observe(viewLifecycleOwner) {
                        goTo(R.id.navigation_health_setup)
                    }
                } else {
                    goTo(R.id.navigation_health_setup)
                }
            }
        }
        return view
    }
}