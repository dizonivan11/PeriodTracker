package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.streamside.periodtracker.R

class SkinFragment : SetupFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_skin, container, false)

        view.findViewById<Button>(R.id.submit_skin).setOnClickListener {
            finalizeSetup(requireActivity())
        }

        return view
    }
}