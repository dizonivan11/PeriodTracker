package com.streamside.periodtracker.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import com.streamside.periodtracker.PERIOD_VIEW_MODEL
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.Period
import java.util.Calendar

class PeriodDateFragment : SetupFragment() {
    private lateinit var referencePeriod: Period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_period_date, container, false)
        PERIOD_VIEW_MODEL.currentPeriod.observe(viewLifecycleOwner) { period ->
            referencePeriod = period

            view.findViewById<Button>(R.id.submit_period_date).setOnClickListener {
                val dpPeriodDate = view.findViewById<DatePicker>(R.id.dp_period_date)
                val periodDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, dpPeriodDate.year)
                    set(Calendar.MONTH, dpPeriodDate.month)
                    set(Calendar.DAY_OF_MONTH, dpPeriodDate.dayOfMonth)
                }
                // Update reference period date
                referencePeriod.periodYear = periodDate.get(Calendar.YEAR)
                referencePeriod.periodMonth = periodDate.get(Calendar.MONTH)
                referencePeriod.periodDay = periodDate.get(Calendar.DAY_OF_MONTH)
                PERIOD_VIEW_MODEL.update(referencePeriod)
                nextPage()
            }
        }

        view.findViewById<Button>(R.id.back_period_date).setOnClickListener {
            previousPage()
        }

        return view
    }
}