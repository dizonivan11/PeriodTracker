package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import com.streamside.periodtracker.MainActivity.Companion.getPeriodViewModel
import com.streamside.periodtracker.MainActivity.Companion.toCalendar
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.PeriodViewModel
import java.util.Calendar

class PeriodDateFragment : SetupFragment() {
    private lateinit var periodViewModel: PeriodViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_period_date, container, false)
        val fa = requireActivity()
        periodViewModel = getPeriodViewModel(fa)

        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { referencePeriod ->
            view.findViewById<Button>(R.id.submit_period_date).setOnClickListener {
                val dpPeriodDate = view.findViewById<DatePicker>(R.id.dp_period_date)
                val periodDate = toCalendar(dpPeriodDate.year, dpPeriodDate.month, dpPeriodDate.dayOfMonth)
                // Update reference period date
                referencePeriod.periodYear = periodDate.get(Calendar.YEAR)
                referencePeriod.periodMonth = periodDate.get(Calendar.MONTH)
                referencePeriod.periodDay = periodDate.get(Calendar.DAY_OF_MONTH)
                periodViewModel.update(referencePeriod)
                nextPage(fa)
            }
        }

        view.findViewById<Button>(R.id.back_period_date).setOnClickListener {
            previousPage(fa)
        }
        return view
    }
}