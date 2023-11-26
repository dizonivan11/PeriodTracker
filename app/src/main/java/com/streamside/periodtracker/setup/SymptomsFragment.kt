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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.streamside.periodtracker.FIRST_TIME
import com.streamside.periodtracker.LOG_PERIOD
import com.streamside.periodtracker.MainActivity.Companion.clearObservers
import com.streamside.periodtracker.MainActivity.Companion.getPeriodViewModel
import com.streamside.periodtracker.NAVVIEW
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.Period
import com.streamside.periodtracker.data.PeriodViewModel

class SymptomsFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var selectedPeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_symptoms, container, false)
        val fa =  requireActivity()
        periodViewModel = getPeriodViewModel(fa)

        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { currentPeriod ->
            periodViewModel.lastPeriod.observe(viewLifecycleOwner) { lastPeriod ->
                if (LOG_PERIOD) {
                    // Set selected period as the last period to review and finalize it after moving to next period
                    selectedPeriod = lastPeriod
                } else {
                    selectedPeriod = if (lastPeriod != null) {
                        // Check first if there's a last period without period end date yet
                        if (lastPeriod.periodEndYear == 0 &&
                            lastPeriod.periodEndMonth == 0 &&
                            lastPeriod.periodEndDay == 0) {

                            // Set selected period as the last period if there's no period end date yet
                            lastPeriod
                        } else {
                            currentPeriod
                        }
                    } else {
                        currentPeriod
                    }
                }
                populateSymptomChips(view, fa)
            }
        }

        if (!FIRST_TIME) {
            if (LOG_PERIOD) {
                // Hide back button if this isn't a first time setup
                view.findViewById<Button>(R.id.back_symptoms).visibility = View.INVISIBLE

                // Make save button click navigate to Home
                view.findViewById<Button>(R.id.submit_symptoms).setOnClickListener {
                    periodViewModel.update(selectedPeriod)
                    finalizeSetup(fa)
                }
            } else {
                // Make back button click navigate to Home
                view.findViewById<Button>(R.id.back_symptoms).setOnClickListener {
                    NAVVIEW.selectedItemId = R.id.navigation_home
                }
                // Make save button click navigate to Home
                view.findViewById<Button>(R.id.submit_symptoms).setOnClickListener {
                    periodViewModel.update(selectedPeriod)
                    NAVVIEW.selectedItemId = R.id.navigation_home
                }
            }
        } else {
            view.findViewById<Button>(R.id.back_symptoms).setOnClickListener {
                previousPage(fa)
            }
            // Make save button click navigate to Home
            view.findViewById<Button>(R.id.submit_symptoms).setOnClickListener {
                periodViewModel.update(selectedPeriod)
                finalizeSetup(fa)
            }
        }
        return view
    }

    override fun onStop() {
        super.onStop()
        clearObservers(requireActivity(), viewLifecycleOwner)
    }

    private fun populateSymptomChips(view: View, fa: FragmentActivity) {
        val llSymptoms = view.findViewById<LinearLayout>(R.id.llSymptoms)
        llSymptoms.removeAllViews()

        val tvSymptomsTitle = TextView(fa)
        tvSymptomsTitle.layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
        (tvSymptomsTitle.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 80)
        tvSymptomsTitle.textSize = 24F
        tvSymptomsTitle.isAllCaps = true
        tvSymptomsTitle.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        if (FIRST_TIME) tvSymptomsTitle.text = "Select symptoms you remembered from your last cycle"
        else if (LOG_PERIOD) tvSymptomsTitle.text = "Review your symptoms"
        else tvSymptomsTitle.text = "Log your symptoms"
        llSymptoms.addView(tvSymptomsTitle)

        for (category in selectedPeriod.symptoms.categories) {
            val tvCategory = TextView(fa)
            tvCategory.layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
            (tvCategory.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 20)
            tvCategory.textSize = 20F
            tvCategory.text = getString(category.id)
            llSymptoms.addView(tvCategory)

            val cgSymptom = ChipGroup(fa)
            cgSymptom.layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
            (cgSymptom.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 80)
            cgSymptom.isSingleSelection = category.singleSelection
            llSymptoms.addView(cgSymptom)

            for (symptom in category.symptoms) {
                val chipSymptom = Chip(fa)
                chipSymptom.layoutParams = MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT)
                chipSymptom.text = getString(symptom.id)
                chipSymptom.elevation = 10f
                chipSymptom.isChecked = symptom.value
                chipSymptom.setOnClickListener { it.isSelected = !it.isSelected }
                chipSymptom.setOnCheckedChangeListener { _: CompoundButton, value: Boolean ->
                    symptom.value = value
                }
                if (symptom.icon > 0)
                    chipSymptom.chipIcon = ResourcesCompat.getDrawable(resources, symptom.icon, fa.theme)
                else
                    chipSymptom.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.baseline_info_24, fa.theme)
                cgSymptom.addView(chipSymptom)
            }
        }
    }
}