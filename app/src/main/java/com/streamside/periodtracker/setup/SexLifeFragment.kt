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

class SexLifeFragment : SetupFragment() {
    private val sexLifeChanges: MutableList<CheckBox> = mutableListOf()
    private lateinit var periodViewModel: PeriodViewModel
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

        periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            newPeriod = period

            view.findViewById<Button>(R.id.submit_sex_life).setOnClickListener {
                if (hasCheck(sexLifeChanges)) {
                    // Record sex life changes
                    newPeriod.sex = getLongCheckValues(sexLifeChanges)
                    periodViewModel.update(newPeriod)
                    movePage(fa, 9)
                } else {
                    Toast.makeText(fa, "Please select at least one sex changes", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.back_sex_life).setOnClickListener {
            movePage(fa, 7)
        }

        return view
    }
}