package com.streamside.periodtracker.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import com.streamside.periodtracker.R
import com.streamside.periodtracker.databinding.FragmentHomeBinding
import com.streamside.periodtracker.views.CircleFillView
import com.streamside.periodtracker.views.CounterView
import java.util.Calendar
import java.util.Date

lateinit var SIMULATED_DATE : Date

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val currentCalendar = Calendar.getInstance().apply { time = Date() }
    private val selectedCalendar = Calendar.getInstance()
    private var selectedMonth = 0
    private lateinit var smallCalendar : SingleRowCalendar
    private lateinit var tvDate : TextView
    private lateinit var tvDay : TextView
    private lateinit var btnRight : View
    private lateinit var btnLeft : View
    private lateinit var circleFillView: CircleFillView
    private lateinit var circleFillBackText : CounterView
    private lateinit var circleFillForeText : CounterView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val prefEditor = preferences.edit()
        val simulated = preferences.getBoolean(getString(R.string.simulation_key), false)

        smallCalendar = root.findViewById(R.id.main_single_row_calendar)
        tvDate = root.findViewById(R.id.tvDate)
        tvDay = root.findViewById(R.id.tvDay)
        btnRight = root.findViewById(R.id.btnRight)
        btnLeft = root.findViewById(R.id.btnLeft)
        circleFillView = root.findViewById(R.id.circleFillView)
        circleFillBackText = root.findViewById(R.id.circleFillBackText)
        circleFillForeText = root.findViewById(R.id.circleFillForeText)
        circleFillBackText.setCounterValue(requireActivity(), 0, circleFillView.getCircleFillValue(), 1000)
        circleFillForeText.setCounterValue(requireActivity(), 0, circleFillView.getCircleFillValue(), 1000)

        root.findViewById<Button>(R.id.btnLog).setOnClickListener {
            // val oldValue = circleFillView.getCircleFillValue()
            // circleFillView.setCircleFillValue((0..100).random(), 1000)
            // circleFillBackText.setCounterValue(requireActivity(), oldValue, circleFillView.getCircleFillValue(), 1000)
            // circleFillForeText.setCounterValue(requireActivity(), oldValue, circleFillView.getCircleFillValue(), 1000)

            // Log period, collect cycle data and initialize new cycle setup
            val builder : AlertDialog.Builder = AlertDialog.Builder(requireActivity())
            builder.setCancelable(true)
            builder.setTitle("Continue Log Period")
            builder.setMessage("This will end this month's cycle and collect cycle data for you")
            builder.setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
                run {
                    prefEditor.putBoolean(getString(R.string.log_period_key), true)
                    prefEditor.apply()
                    requireActivity().recreate()
                }
            }
            val dialog : AlertDialog = builder.create()
            dialog.show()
        }

        // set current date to calendar and current month to currentMonth variable
        selectedCalendar.time = Date()
        selectedMonth = selectedCalendar[Calendar.MONTH]

        // calendar view manager is responsible for our displaying logic
        val smallCalendarViewManager = object : CalendarViewManager {
            override fun setCalendarViewResourceId(
                position: Int,
                date: Date,
                isSelected: Boolean
            ): Int {
                // set date to calendar according to position where we are
                val cal = Calendar.getInstance()
                cal.time = date
                // if item is selected we return this layout items
                // in this example monday, wednesday and friday will have special item views and other days
                // will be using basic item view
                return if (isSelected)
                    when (cal[Calendar.DAY_OF_WEEK]) {
                        Calendar.MONDAY -> R.layout.first_special_selected_calendar_item
                        Calendar.WEDNESDAY -> R.layout.second_special_selected_calendar_item
                        Calendar.FRIDAY -> R.layout.third_special_selected_calendar_item
                        else -> R.layout.selected_calendar_item
                    }
                else
                // here we return items which are not selected
                    when (cal[Calendar.DAY_OF_WEEK]) {
                        Calendar.MONDAY -> R.layout.first_special_calendar_item
                        Calendar.WEDNESDAY -> R.layout.second_special_calendar_item
                        Calendar.FRIDAY -> R.layout.third_special_calendar_item
                        else -> R.layout.calendar_item
                    }
            }

            override fun bindDataToCalendarView(
                holder: SingleRowCalendarAdapter.CalendarViewHolder,
                date: Date,
                position: Int,
                isSelected: Boolean
            ) {
                // using this method we can bind data to calendar view
                // good practice is if all views in layout have same IDs in all item views
                holder.itemView.findViewById<TextView>(R.id.tv_date_calendar_item).text = DateUtils.getDayNumber(date)
                holder.itemView.findViewById<TextView>(R.id.tv_day_calendar_item).text = DateUtils.getDay3LettersName(date)
            }
        }

        // using calendar changes observer we can track changes in calendar
        val smallCalendarChangesObserver = object : CalendarChangesObserver {
            // you can override more methods, in this example we need only this one
            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
                tvDate.text = getString(R.string.text_month_day, DateUtils.getMonthName(date), DateUtils.getDayNumber(date))
                tvDay.text = DateUtils.getDay3LettersName(date)
                super.whenSelectionChanged(isSelected, position, date)
            }
        }

        // selection manager is responsible for managing selection
        val smallSelectionManager = object : CalendarSelectionManager {
            override fun canBeItemSelected(position: Int, date: Date): Boolean {
                return true
            }
        }

        // here we init our calendar, also you can set more properties if you need them
        val singleRowCalendar = smallCalendar.apply {
            calendarViewManager = smallCalendarViewManager
            calendarChangesObserver = smallCalendarChangesObserver
            calendarSelectionManager = smallSelectionManager
            setDates(getFutureDatesOfCurrentMonth())
            init()
        }
        var scrollPos = currentCalendar.get(Calendar.DAY_OF_MONTH) - 3
        if (scrollPos < 0) scrollPos = 0
        singleRowCalendar.select(currentCalendar.get(Calendar.DAY_OF_MONTH) - 1)
        singleRowCalendar.scrollToPosition(scrollPos)

        if (simulated) {
            root.findViewById<Button>(R.id.btnSimulatedDate).visibility = View.VISIBLE
            btnRight.visibility = View.GONE
            btnLeft.visibility = View.GONE
        } else {
            btnRight.setOnClickListener { singleRowCalendar.setDates(getDatesOfNextMonth()) }
            btnLeft.setOnClickListener { singleRowCalendar.setDates(getDatesOfPreviousMonth()) }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDatesOfNextMonth(): List<Date> {
        selectedMonth++ // + because we want next month
        if (selectedMonth == 12) {
            // we will switch to january of next year, when we reach last month of year
            selectedCalendar.set(Calendar.YEAR, selectedCalendar[Calendar.YEAR] + 1)
            selectedMonth = 0 // 0 == january
        }
        return getDates(mutableListOf())
    }

    private fun getDatesOfPreviousMonth(): List<Date> {
        selectedMonth-- // - because we want previous month
        if (selectedMonth == -1) {
            // we will switch to december of previous year, when we reach first month of year
            selectedCalendar.set(Calendar.YEAR, selectedCalendar[Calendar.YEAR] - 1)
            selectedMonth = 11 // 11 == december
        }
        return getDates(mutableListOf())
    }

    private fun getFutureDatesOfCurrentMonth(): List<Date> {
        // get all next dates of current month
        selectedMonth = selectedCalendar[Calendar.MONTH]
        return getDates(mutableListOf())
    }

    private fun getDates(list: MutableList<Date>): List<Date> {
        // load dates of whole month
        selectedCalendar.set(Calendar.MONTH, selectedMonth)
        selectedCalendar.set(Calendar.DAY_OF_MONTH, 1)
        list.add(selectedCalendar.time)
        while (selectedMonth == selectedCalendar[Calendar.MONTH]) {
            selectedCalendar.add(Calendar.DATE, +1)
            if (selectedCalendar[Calendar.MONTH] == selectedMonth)
                list.add(selectedCalendar.time)
        }
        selectedCalendar.add(Calendar.DATE, -1)
        return list
    }
}