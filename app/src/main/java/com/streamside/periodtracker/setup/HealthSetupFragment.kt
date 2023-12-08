package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import com.streamside.periodtracker.FIRST_PERIOD_START_MIN
import com.streamside.periodtracker.FIRST_TIME
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.R

class HealthSetupFragment : SetupFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_health_setup, container, false)
        val fa = requireActivity()
        val hsName = view.findViewById<EditText>(R.id.hsName)
        val hsAge = view.findViewById<EditText>(R.id.hsAge)
        val hsHeightFeet = view.findViewById<AutoCompleteTextView>(R.id.hsHeightFeet)
        val hsHeightInch = view.findViewById<AutoCompleteTextView>(R.id.hsHeightInch)
        val hsBack = view.findViewById<Button>(R.id.hsBack)
        val hsNext = view.findViewById<Button>(R.id.hsNext)

        if (FIRST_TIME) {
            hsBack.setOnClickListener { goTo(R.id.navigation_intro) }
        } else {
            hsBack.visibility = View.GONE
        }

        hsHeightFeet.threshold = 1
        hsHeightFeet.setAdapter(ArrayAdapter(fa, androidx.appcompat.R.layout.select_dialog_singlechoice_material,
            listOf( 4, 5, 6, 7 )))

        hsHeightInch.threshold = 1
        hsHeightInch.setAdapter(ArrayAdapter(fa, androidx.appcompat.R.layout.select_dialog_singlechoice_material,
            listOf( 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 )))

        hsNext.setOnClickListener {
            // TODO: save health profile here....

            if (hsAge.text.toString().toInt() >= FIRST_PERIOD_START_MIN) {
                goTo(R.id.navigation_period_date)
            } else {
                finalizeSetup(fa)
            }
        }

        return view
    }
}