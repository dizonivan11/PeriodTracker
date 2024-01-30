package com.streamside.periodtracker.setup

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.streamside.periodtracker.FIRST_TIME_TRACKER
import com.streamside.periodtracker.LOG_PERIOD
import com.streamside.periodtracker.MainActivity.Companion.getDataViewModel
import com.streamside.periodtracker.MainActivity.Companion.getPeriodViewModel
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.period.Category
import com.streamside.periodtracker.data.period.DataViewModel
import com.streamside.periodtracker.data.period.Period
import com.streamside.periodtracker.data.period.PeriodViewModel
import com.streamside.periodtracker.data.period.Symptom
import com.streamside.periodtracker.data.period.SymptomList

class SymptomsFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var dataViewModel: DataViewModel
    private lateinit var selectedPeriod: Period
    private lateinit var symptomList: SymptomList

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_symptoms, container, false)
        val fa =  requireActivity()
        periodViewModel = getPeriodViewModel(fa)
        dataViewModel = getDataViewModel(fa)

        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { currentPeriod ->
            periodViewModel.lastPeriod.observe(viewLifecycleOwner) { lastPeriod ->
                selectedPeriod = if (lastPeriod != null) symptomsPeriod(currentPeriod, lastPeriod) else currentPeriod

                dataViewModel.newSymptomsData().observe(viewLifecycleOwner) {
                    symptomList = it
                    populateSymptomChips(view, fa)
                }

                if (!FIRST_TIME_TRACKER) {
                    if (LOG_PERIOD) {
                        // Hide back button if this isn't a first time setup
                        view.findViewById<Button>(R.id.back_symptoms).visibility = View.INVISIBLE

                        // Make save button click
                        setSubmitOnClick(view, fa)
                    } else {
                        // Make back button click navigate to Home
                        view.findViewById<Button>(R.id.back_symptoms).setOnClickListener {
                            goTo(R.id.navigation_tracker)
                        }
                        // Make save button click navigate to Home
                        setSubmitOnClick(view, fa, true)
                    }
                } else {
                    view.findViewById<Button>(R.id.back_symptoms).setOnClickListener {
                        goTo(R.id.navigation_menstrual_cycle)
                    }
                    // Make save button click
                    setSubmitOnClick(view, fa)
                }
            }
        }

        return view
    }

    private fun setSubmitOnClick(root: View, fa: FragmentActivity, logOnly: Boolean = false) {
        root.findViewById<Button>(R.id.submit_symptoms).setOnClickListener {
            val submittedList = SymptomList(mutableListOf())
            for (category in symptomList.categories) {
                val checkedSymptoms = category.symptoms.filter { it.value }.toMutableList()
                if (checkedSymptoms.size != 0) {
                    submittedList.categories.add(category)
                    category.symptoms = checkedSymptoms
                }
            }
            selectedPeriod.symptoms = submittedList
            periodViewModel.update(selectedPeriod)
            if (logOnly) goTo(R.id.navigation_tracker)
            else finalizeSetup(fa)
        }
    }

    private fun populateSymptomChips(view: View, fa: FragmentActivity) {
        val typeface = Typeface.createFromAsset(fa.assets, "Poppins-Regular.ttf")
        val llSymptoms = view.findViewById<LinearLayout>(R.id.llSymptoms)
        llSymptoms.removeAllViews()

        val tvSymptomsTitle = TextView(fa)
        tvSymptomsTitle.layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
        (tvSymptomsTitle.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 80)
        tvSymptomsTitle.textSize = 24F
        tvSymptomsTitle.isAllCaps = true
        tvSymptomsTitle.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        if (FIRST_TIME_TRACKER) tvSymptomsTitle.text = "Select symptoms you remembered from your last cycle"
        else if (LOG_PERIOD) tvSymptomsTitle.text = "Review your symptoms"
        else tvSymptomsTitle.text = "Log your symptoms"
        llSymptoms.addView(tvSymptomsTitle)

        for (category in symptomList.categories) {
            if (!category.visible) continue

            val tvCategory = TextView(fa)
            tvCategory.layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
            (tvCategory.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 20)
            tvCategory.textSize = 20F
            tvCategory.text = category.id
            llSymptoms.addView(tvCategory)

            val cgSymptom = ChipGroup(fa)
            cgSymptom.layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
            (cgSymptom.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 80)
            llSymptoms.addView(cgSymptom)

            for (symptom in category.symptoms) {
                if (!symptom.visible) continue
                symptom.value = hasSymptom(selectedPeriod.symptoms, symptom)

                val chipSymptom = Chip(fa)
                chipSymptom.layoutParams = MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT)
                chipSymptom.typeface = typeface
                chipSymptom.text = symptom.id
                chipSymptom.elevation = 10f
                chipSymptom.isChecked = symptom.value
                chipSymptom.setOnClickListener { it.isSelected = !it.isSelected }
                chipSymptom.setOnCheckedChangeListener { _: CompoundButton, value: Boolean -> symptom.value = value }
                cgSymptom.addView(chipSymptom)
            }
        }
    }

    companion object {
        fun symptomsPeriod(currentPeriod: Period, lastPeriod: Period): Period {
            return if (LOG_PERIOD) {
                // Set selected period as the last period to review and finalize it after moving to next period
                lastPeriod
            } else {
                // Check first if there's a last period without period end date yet
                if (lastPeriod.periodEndYear == 0 &&
                    lastPeriod.periodEndMonth == 0 &&
                    lastPeriod.periodEndDay == 0) {

                    // Set selected period as the last period if there's no period end date yet
                    lastPeriod
                } else {
                    currentPeriod
                }
            }
        }

        fun hasSymptomsOn(category: Category): Boolean {
            for (symptom in category.symptoms) if (symptom.value) return true
            return false
        }

        fun hasSymptom(list: SymptomList, symptom: Symptom): Boolean {
            for (c in list.categories) {
                for (s in c.symptoms) {
                    if (s.id == symptom.id) return s.value
                }
            }
            return false
        }
    }
}