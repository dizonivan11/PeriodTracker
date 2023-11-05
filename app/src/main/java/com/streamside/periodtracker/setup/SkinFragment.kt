package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.Period
import com.streamside.periodtracker.data.PeriodViewModel

class SkinFragment : SetupFragment() {
    private val skinChanges: MutableList<CheckBox> = mutableListOf()
    private lateinit var periodViewModel: PeriodViewModel
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

        periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_skin).setOnClickListener {
                if (hasCheck(skinChanges)) {
                    // Record skin changes
                    newPeriod.skin = getLongCheckValues(skinChanges)
                    periodViewModel.update(newPeriod)
                    finalizeSetup(requireActivity())
                } else {
                    Toast.makeText(fa, "Please select at least one sex changes", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_skin).setOnClickListener {
            previousPage()
        }

        return view
    }
}