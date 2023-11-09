package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import com.streamside.periodtracker.PERIOD_VIEW_MODEL
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.Period

class MentalHealthFragment : SetupFragment() {
    private val mentalHealths: MutableList<CheckBox> = mutableListOf()
    private lateinit var newPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mental_health, container, false)
        val fa = requireActivity()
        mentalHealths.add(view.findViewById(R.id.check_mental_health_fine))
        mentalHealths.add(view.findViewById(R.id.check_mental_health_stressed))
        mentalHealths.add(view.findViewById(R.id.check_mental_health_mood_fluctuations))
        mentalHealths.add(view.findViewById(R.id.check_mental_health_anxiety))
        mentalHealths.add(view.findViewById(R.id.check_mental_health_depressed_mood))
        mentalHealths.add(view.findViewById(R.id.check_mental_health_low_energy))
        mentalHealths.add(view.findViewById(R.id.check_mental_health_poor_self_image))
        mentalHealths.add(view.findViewById(R.id.check_mental_health_other))

        PERIOD_VIEW_MODEL.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_mental_health).setOnClickListener {
                if (hasCheck(mentalHealths)) {
                    // Record mental health conditions
                    newPeriod.mental = getLongCheckValues(mentalHealths)
                    PERIOD_VIEW_MODEL.update(newPeriod)
                    nextPage()
                } else {
                    Toast.makeText(fa, getString(R.string.ic_mental), Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_mental_health).setOnClickListener {
            previousPage()
        }

        return view
    }
}