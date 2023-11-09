package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.streamside.periodtracker.PERIOD_VIEW_MODEL
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.Period

class SkinFragment : SetupFragment() {
    private val skinChanges: MutableList<CheckBox> = mutableListOf()
    private lateinit var newPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_skin, container, false)
        val fa = requireActivity()
        skinChanges.add(view.findViewById(R.id.check_skin_no_changes))
        skinChanges.add(view.findViewById(R.id.check_skin_acne_blemishes))
        skinChanges.add(view.findViewById(R.id.check_skin_dark_spots_pores))
        skinChanges.add(view.findViewById(R.id.check_skin_dryness))
        skinChanges.add(view.findViewById(R.id.check_skin_fine_lines_wrinkles))
        skinChanges.add(view.findViewById(R.id.check_skin_dullness_texture))
        skinChanges.add(view.findViewById(R.id.check_skin_other))

        PERIOD_VIEW_MODEL.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_skin).setOnClickListener {
                if (hasCheck(skinChanges)) {
                    // Record skin changes
                    newPeriod.skin = getLongCheckValues(skinChanges)
                    PERIOD_VIEW_MODEL.update(newPeriod)

                    if (inputCheck(newPeriod)) {
                        finalizeSetup(requireActivity())
                    } else {
                        displayMissingInput(fa, newPeriod)
                    }
                } else {
                    Toast.makeText(fa, getString(R.string.ic_skin), Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_skin).setOnClickListener {
            previousPage()
        }

        return view
    }

    private fun inputCheck(period: Period): Boolean {
        if (period.periodYear == 0 ||
            period.periodMonth == 0 ||
            period.periodDay == 0
        ) return false

        if (period.menstrualCycle == "") return false
        if (period.discharge == "") return false
        if (period.discomforts == 0L) return false
        if (period.fitness == "") return false
        if (period.mental == 0L) return false
        if (period.rhd == "") return false
        if (period.sex == 0L) return false
        if (period.skin == 0L) return false
        if (period.sleep == "") return false

        return true
    }

    private fun displayMissingInput(fa: FragmentActivity, period: Period) {
        var message = "Unknown error"

        if (period.periodYear == 0 ||
            period.periodMonth == 0 ||
            period.periodDay == 0
        ) message = getString(R.string.ic_period)

        if (period.menstrualCycle == "") message = getString(R.string.ic_menstrual)
        if (period.discharge == "") message = getString(R.string.ic_discharge)
        if (period.discomforts == 0L) message = getString(R.string.ic_discomforts)
        if (period.fitness == "") message = getString(R.string.ic_fitness)
        if (period.mental == 0L) message = getString(R.string.ic_mental)
        if (period.rhd == "") message = getString(R.string.ic_rhd)
        if (period.sex == 0L) message = getString(R.string.ic_sex)
        if (period.skin == 0L) message = getString(R.string.ic_skin)
        if (period.sleep == "") message = getString(R.string.ic_sleep)

        Toast.makeText(fa, message, Toast.LENGTH_SHORT).show()
    }
}