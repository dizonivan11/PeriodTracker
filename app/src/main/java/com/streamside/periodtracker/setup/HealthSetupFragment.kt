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
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.health.Health
import com.streamside.periodtracker.data.health.HealthViewModel

class HealthSetupFragment : SetupFragment() {
    private lateinit var healthViewModel: HealthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_health_setup, container, false)
        val fa = requireActivity()
        healthViewModel = getHealthViewModel(fa)
        val feetDropDownList = listOf( 4, 5, 6, 7 )
        val inchesDropDownList = listOf( 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 )
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
                hsHeightFeet.setSelection(feetDropDownList.indexOf(feetInches[0]))
                hsHeightInch.setSelection(inchesDropDownList.indexOf(feetInches[1]))
                hsWeight.setText(existingProfile.weight.toString())
            }

            hsNext.setOnClickListener {
                if (healthProfiles.isEmpty()) {
                    val newHealthProfile = Health(
                        0,
                        hsName.text.toString(),
                        hsAge.text.toString().toInt(),
                        hsWeight.text.toString().toInt(),
                        getInches(
                            hsHeightFeet.selectedItem.toString().toInt(),
                            hsHeightInch.selectedItem.toString().toInt()))

                    healthViewModel.add(newHealthProfile)
                } else {
                    val existingProfile = healthProfiles[0]
                    existingProfile.name = hsName.text.toString()
                    existingProfile.age = hsAge.text.toString().toInt()
                    existingProfile.weight = hsWeight.text.toString().toInt()
                    existingProfile.height = getInches(
                        hsHeightFeet.selectedItem.toString().toInt(),
                        hsHeightInch.selectedItem.toString().toInt())

                    healthViewModel.update(existingProfile)
                }

                if (hsAge.text.toString().toInt() >= FIRST_PERIOD_START_MIN) {
                    if (FIRST_TIME)
                        goTo(R.id.navigation_period_date)
                    else
                        goTo(R.id.navigation_home)
                } else {
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