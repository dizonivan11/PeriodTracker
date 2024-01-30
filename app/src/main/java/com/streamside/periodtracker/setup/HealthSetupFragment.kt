package com.streamside.periodtracker.setup

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.streamside.periodtracker.FIRST_TIME
import com.streamside.periodtracker.MainActivity.Companion.getHealthViewModel
import com.streamside.periodtracker.MainActivity.Companion.getPeriodViewModel
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.health.Health
import com.streamside.periodtracker.data.health.HealthViewModel
import com.streamside.periodtracker.data.period.PeriodViewModel
import java.util.Calendar
import java.util.Date

class HealthSetupFragment : SetupFragment() {
    private val FEET_START = 3
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var healthViewModel: HealthViewModel
    private lateinit var feetDropDownList: List<String>
    private lateinit var inchesDropDownList: List<String>
    private lateinit var hsHeightFeet: Spinner
    private lateinit var hsHeightInch: Spinner

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_health_setup, container, false)
        val fa = requireActivity()
        periodViewModel = getPeriodViewModel(fa)
        healthViewModel = getHealthViewModel(fa)
        val hsTitle = view.findViewById<TextView>(R.id.hsTitle)
        val hsName = view.findViewById<EditText>(R.id.hsName)
        val birthMonthDropDownList: List<String> = listOf("mm", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
        val birthDayDropDownList = listOf("dd", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
            "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31")
        val hsBirthMonth = view.findViewById<Spinner>(R.id.hsBirthMonth)
        val hsBirthDay = view.findViewById<Spinner>(R.id.hsBirthDay)
        val hsBirthYear = view.findViewById<EditText>(R.id.hsBirthYear)
        feetDropDownList = listOf("Select", "4ft", "5ft", "6ft", "7ft")
        inchesDropDownList = listOf("Select", "0in", "1in", "2in", "3in", "4in", "5in", "6in", "7in", "8in", "9in", "10in", "11in")
        hsHeightFeet = view.findViewById(R.id.hsHeightFeet)
        hsHeightInch = view.findViewById(R.id.hsHeightInch)
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
                    var mString = (m + 1).toString()
                    if (m + 1 < 10) mString = "0${m + 1}"
                    var dString = d.toString()
                    if (d < 10) dString = "0${d}"
                    hsBirthMonth.setSelection(birthMonthDropDownList.indexOf(mString))
                    hsBirthDay.setSelection(birthDayDropDownList.indexOf(dString))
                    hsBirthYear.setText(y.toString())
                }
                if (existingProfile.height > 0) {
                    hsHeightFeet.setSelection(feetDropDownList.indexOf("${feetInches[0]}ft"))
                    hsHeightInch.setSelection(inchesDropDownList.indexOf("${feetInches[1]}in"))
                }
                hsWeight.setText(existingProfile.weight.toString())
            }

            hsNext.setOnClickListener {
                var success = true
                val today = Calendar.getInstance().apply { time = Date() }
                val name = if (hsName.text.isNotEmpty()) hsName.text.toString() else ""
                var birthDate: Date? = null
                if (hsBirthMonth.selectedItem.toString() != "mm" &&
                    hsBirthDay.selectedItem.toString() != "dd" &&
                    hsBirthYear.text.isNotEmpty()) {
                    val c = Calendar.getInstance()
                    c.set(Calendar.MONTH, hsBirthMonth.selectedItem.toString().toInt() - 1)
                    c.set(Calendar.DAY_OF_MONTH, hsBirthDay.selectedItem.toString().toInt())
                    c.set(Calendar.YEAR, hsBirthYear.text.toString().toInt())
                    birthDate = c.time

                    // Birth year must not be a year above the current year
                    if (c.get(Calendar.YEAR) > today.get(Calendar.YEAR)) {
                        Toast.makeText(fa, "Birth year cannot be above the current year", Toast.LENGTH_SHORT).show()
                        success = false
                    }
                }
                val weight = if (hsWeight.text.isNotEmpty()) hsWeight.text.toString().toInt() else 0
                val feet = getFeetFromDropDown()
                val inches = getInchesFromDropDown()
                val height = if (feet > 0 && inches > -1) getInches(feet, inches) else 0

                if (success) {
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

                    // Set First Time settings to false
                    val preferences = PreferenceManager.getDefaultSharedPreferences(fa)
                    preferences.edit().putBoolean(getString(R.string.first_time_key), false).apply()
                    FIRST_TIME = false

                    goTo(R.id.navigation_home)
                }
            }
        }

        return view
    }

    private fun getFeetFromDropDown(): Int {
        if (hsHeightFeet.selectedItemPosition > 0) {
            return hsHeightFeet.selectedItemPosition + FEET_START
        }
        return 0
    }

    private fun getInchesFromDropDown(): Int {
        if (hsHeightInch.selectedItemPosition > 0) {
            return hsHeightInch.selectedItemPosition - 1
        }
        return -1
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