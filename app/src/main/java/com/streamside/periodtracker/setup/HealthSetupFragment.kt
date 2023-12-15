package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.streamside.periodtracker.FIRST_PERIOD_START_MIN
import com.streamside.periodtracker.FIRST_TIME
import com.streamside.periodtracker.MainActivity.Companion.getHealthViewModel
import com.streamside.periodtracker.MainActivity.Companion.getPeriodViewModel
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.MainActivity.Companion.isNotEmptyPeriod
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.health.Health
import com.streamside.periodtracker.data.health.HealthViewModel
import com.streamside.periodtracker.data.period.PeriodViewModel

class HealthSetupFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var healthViewModel: HealthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_health_setup, container, false)
        val fa = requireActivity()
        periodViewModel = getPeriodViewModel(fa)
        healthViewModel = getHealthViewModel(fa)
        val feetDropDownList = listOf( "Select", 4, 5, 6, 7 )
        val inchesDropDownList = listOf( "Select", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 )
        val hsTitle = view.findViewById<TextView>(R.id.hsTitle)
        val hsName = view.findViewById<EditText>(R.id.hsName)
        val hsAge = view.findViewById<EditText>(R.id.hsAge)
        val hsHeightFeet = view.findViewById<Spinner>(R.id.hsHeightFeet)
        val hsHeightInch = view.findViewById<Spinner>(R.id.hsHeightInch)
        val hsWeight = view.findViewById<EditText>(R.id.hsWeight)
        val hsBack = view.findViewById<Button>(R.id.hsBack)
        val hsNext = view.findViewById<Button>(R.id.hsNext)

        if (FIRST_TIME) {
            hsBack.setOnClickListener { goTo(R.id.navigation_intro) }
        } else {
            hsBack.visibility = View.GONE
            hsNext.text = getString(R.string.button_save)
        }

        hsHeightFeet.adapter = ArrayAdapter(fa, androidx.appcompat.R.layout.select_dialog_item_material, feetDropDownList)
        hsHeightInch.adapter = ArrayAdapter(fa, androidx.appcompat.R.layout.select_dialog_item_material, inchesDropDownList)

        healthViewModel.all.observe(viewLifecycleOwner) { healthProfiles ->
            if (healthProfiles.isNotEmpty()) {
                val existingProfile = healthProfiles[0]
                val feetInches = getFeetInches(existingProfile.height)
                hsTitle.text = "Update Health Profile"

                hsName.setText(existingProfile.name)
                hsAge.setText(existingProfile.age.toString())
                if (existingProfile.height > 0) {
                    hsHeightFeet.setSelection(feetDropDownList.indexOf(feetInches[0]))
                    hsHeightInch.setSelection(inchesDropDownList.indexOf(feetInches[1]))
                }
                hsWeight.setText(existingProfile.weight.toString())
            }

            hsNext.setOnClickListener {
                val name = if (hsName.text.isNotEmpty()) hsName.text.toString() else ""
                val age = if (hsAge.text.isNotEmpty()) hsAge.text.toString().toInt() else 0
                val weight = if (hsWeight.text.isNotEmpty()) hsWeight.text.toString().toInt() else 0
                val feet = if (hsHeightFeet.selectedItem.toString() != "Select") hsHeightFeet.selectedItem.toString().toInt() else 0
                val inches = if (hsHeightInch.selectedItem.toString() != "Select") hsHeightInch.selectedItem.toString().toInt() else -1
                val height = if (feet > 0 && inches > -1) getInches(feet, inches) else 0

                if (healthProfiles.isEmpty()) {
                    healthViewModel.add(Health(0, name, age, weight, height))
                } else {
                    val existingProfile = healthProfiles[0]
                    existingProfile.name = name
                    existingProfile.age = age
                    existingProfile.weight = weight
                    existingProfile.height = height
                    healthViewModel.update(existingProfile)
                }

                if (hsAge.text.toString().isNotEmpty()) {
                    if (hsAge.text.toString().toInt() >= FIRST_PERIOD_START_MIN) {
                        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { referencePeriod ->
                            if (referencePeriod == null) {
                                // Initialize reference period
                                periodViewModel.init(-1, 0, 0, 0).observe(viewLifecycleOwner) {
                                    goTo(R.id.navigation_period_date)
                                }
                            } else {
                                if (isNotEmptyPeriod(referencePeriod)) {
                                    goTo(R.id.navigation_home)
                                    finalizeSetup(fa)
                                } else {
                                    goTo(R.id.navigation_period_date)
                                }
                            }
                        }
                    } else {
                        goTo(R.id.navigation_home)
                        finalizeSetup(fa)
                    }
                } else {
                    goTo(R.id.navigation_home)
                    finalizeSetup(fa)
                }
            }
        }

        return view
    }

    private fun getInches(feet: Int, inches: Int): Int {
        val inchMultiplier = 12
        return (feet * inchMultiplier) + inches
    }

    private fun getFeetInches(inches: Int): List<Int> {
        val inchMultiplier = 12
        val f = inches / inchMultiplier
        val i = inches % inchMultiplier
        return listOf(f, i)
    }
}