package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.PeriodViewModel

class IntroFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_intro, container, false)
        val fa = requireActivity()
        periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]

        // Initialize reference cycle
        view.findViewById<Button>(R.id.submit_intro).setOnClickListener {
            periodViewModel.all.observe(viewLifecycleOwner) { periods ->
                if (periods.isEmpty()) {
                    periodViewModel.init(-1, 0, 0, 0)
                }
                movePage(fa, 1)
            }
        }

        return view
    }
}