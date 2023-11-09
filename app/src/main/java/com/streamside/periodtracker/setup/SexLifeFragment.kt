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

class SexLifeFragment : SetupFragment() {
    private val sexLifeChanges: MutableList<CheckBox> = mutableListOf()
    private lateinit var newPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sex_life, container, false)
        val fa = requireActivity()
        sexLifeChanges.add(view.findViewById(R.id.check_sex_life_none))
        sexLifeChanges.add(view.findViewById(R.id.check_sex_life_painful_sex))
        sexLifeChanges.add(view.findViewById(R.id.check_sex_life_difficulty_orgasm))
        sexLifeChanges.add(view.findViewById(R.id.check_sex_life_low_libido))
        sexLifeChanges.add(view.findViewById(R.id.check_sex_life_communication))
        sexLifeChanges.add(view.findViewById(R.id.check_sex_life_poor_body_image))
        sexLifeChanges.add(view.findViewById(R.id.check_sex_life_other))

        PERIOD_VIEW_MODEL.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_sex_life).setOnClickListener {
                if (hasCheck(sexLifeChanges)) {
                    // Record sex life changes
                    newPeriod.sex = getLongCheckValues(sexLifeChanges)
                    PERIOD_VIEW_MODEL.update(newPeriod)
                    nextPage()
                } else {
                    Toast.makeText(fa, getString(R.string.ic_sex), Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_sex_life).setOnClickListener {
            previousPage()
        }

        return view
    }
}