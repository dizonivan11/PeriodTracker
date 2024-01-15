package com.streamside.periodtracker.setup

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
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
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class HealthSetupFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var healthViewModel: HealthViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_health_setup, container, false)
        val fa = requireActivity()
        periodViewModel = getPeriodViewModel(fa)
        healthViewModel = getHealthViewModel(fa)
        val hsTitle = view.findViewById<TextView>(R.id.hsTitle)
        val hsName = view.findViewById<EditText>(R.id.hsName)
        val birthMonthDropDownList = listOf("mm", 1, 2, 3, 4, 5, 6, 7, 8, 9,10, 11, 12)
        val birthDayDropDownList = listOf("dd", 1, 2, 3, 4, 5, 6, 7, 8, 9,10, 11, 12,
            13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31)
        val hsBirthMonth = view.findViewById<Spinner>(R.id.hsBirthMonth)
        val hsBirthDay = view.findViewById<Spinner>(R.id.hsBirthDay)
        val hsBirthYear = view.findViewById<EditText>(R.id.hsBirthYear)
        val feetDropDownList = listOf( "Select", 4, 5, 6, 7 )
        val inchesDropDownList = listOf( "Select", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 )
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

        hsBirthMonth.adapter = ArrayAdapter(fa, androidx.appcompat.R.layout.select_dialog_item_material, birthMonthDropDownList)
        hsBirthDay.adapter = ArrayAdapter(fa, androidx.appcompat.R.layout.select_dialog_item_material, birthDayDropDownList)
        hsHeightFeet.adapter = ArrayAdapter(fa, androidx.appcompat.R.layout.select_dialog_item_material, feetDropDownList)
        hsHeightInch.adapter = ArrayAdapter(fa, androidx.appcompat.R.layout.select_dialog_item_material, inchesDropDownList)

        healthViewModel.all.observe(viewLifecycleOwner) { healthProfiles ->
            if (healthProfiles.isNotEmpty()) {
                val existingProfile = healthProfiles[0]
                val feetInches = getFeetInches(existingProfile.height)
                hsTitle.text = "Update Health Profile"

                hsName.setText(existingProfile.name)
                if (existingProfile.birthdate != null) {
                    val c = Calendar.getInstance().apply { time = existingProfile.birthdate!! }
                    val m = c.get(Calendar.MONTH)
                    val d = c.get(Calendar.DAY_OF_MONTH)
                    val y = c.get(Calendar.YEAR)
                    hsBirthMonth.setSelection(birthMonthDropDownList.indexOf(m + 1))
                    hsBirthDay.setSelection(birthDayDropDownList.indexOf(d))
                    hsBirthYear.setText(y.toString())
                }
                if (existingProfile.height > 0) {
                    hsHeightFeet.setSelection(feetDropDownList.indexOf(feetInches[0]))
                    hsHeightInch.setSelection(inchesDropDownList.indexOf(feetInches[1]))
                }
                hsWeight.setText(existingProfile.weight.toString())
            }

            hsNext.setOnClickListener {
                val name = if (hsName.text.isNotEmpty()) hsName.text.toString() else ""
                var birthDate: Date? = null
                if (hsBirthMonth.selectedItem.toString() != "mm" &&
                    hsBirthDay.selectedItem.toString() != "dd" &&
                    hsBirthYear.text.isNotEmpty()) {
                    val c = Calendar.getInstance()
                    c.set(Calendar.MONTH, hsBirthMonth.selectedItem.toString().toInt() - 1)
                    c.set(Calendar.DAY_OF_MONTH, hsBirthDay.selectedItem.toString().toInt())
                    c.set(Calendar.YEAR, hsBirthYear.text.toString().toInt())
                    Log.i("Date", "${c.get(Calendar.MONTH)}-${c.get(Calendar.DAY_OF_MONTH)}-${c.get(Calendar.YEAR)}")
                    birthDate = c.time
                }
                val weight = if (hsWeight.text.isNotEmpty()) hsWeight.text.toString().toInt() else 0
                val feet = if (hsHeightFeet.selectedItem.toString() != "Select") hsHeightFeet.selectedItem.toString().toInt() else 0
                val inches = if (hsHeightInch.selectedItem.toString() != "Select") hsHeightInch.selectedItem.toString().toInt() else -1
                val height = if (feet > 0 && inches > -1) getInches(feet, inches) else 0

                if (healthProfiles.isEmpty()) {
                    healthViewModel.add(Health(0, name, birthDate, weight, height))
                } else {
                    val existingProfile = healthProfiles[0]
                    existingProfile.name = name
                    existingProfile.birthdate = birthDate
                    existingProfile.weight = weight
                    existingProfile.height = height
                    healthViewModel.update(existingProfile)
                }

                if (birthDate != null) {
                    Log.i("Age", "${getAge(birthDate)}")
                    if (getAge(birthDate) >= FIRST_PERIOD_START_MIN) {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAge(birthDate: Date): Int {
        val c = Calendar.getInstance().apply { time = birthDate }
        return java.time.Period.between(
            LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)),
            LocalDate.now()
        ).years
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