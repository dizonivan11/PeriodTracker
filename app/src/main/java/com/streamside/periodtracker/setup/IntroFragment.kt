package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.R

class IntroFragment : SetupFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro, container, false)
        view.findViewById<Button>(R.id.submit_intro).setOnClickListener {
            goTo(R.id.navigation_health_setup)
        }
        return view
    }
}