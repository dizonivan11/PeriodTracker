package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.streamside.periodtracker.R

class SexLifeFragment : SetupFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sex_life, container, false)

        view.findViewById<Button>(R.id.back_sex_life).setOnClickListener {
            previousPage()
        }

        view.findViewById<Button>(R.id.submit_sex_life).setOnClickListener {
            nextPage()
        }

        return view
    }
}