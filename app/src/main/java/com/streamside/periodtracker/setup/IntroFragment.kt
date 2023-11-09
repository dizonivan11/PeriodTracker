package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.streamside.periodtracker.PERIOD_VIEW_MODEL
import com.streamside.periodtracker.R

class IntroFragment : SetupFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_intro, container, false)
        PERIOD_VIEW_MODEL.all.observe(viewLifecycleOwner) { periods ->
            view.findViewById<Button>(R.id.submit_intro).setOnClickListener {
                if (periods.isEmpty()) {
                    // Initialize reference cycle
                    PERIOD_VIEW_MODEL.init(-1, 0, 0, 0)
                }
                nextPage()
            }
        }

        return view
    }
}