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
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val todayCalendar = Calendar.getInstance().apply { time = Date() }
    private val currentCalendar = Calendar.getInstance()
    private var currentMonth = 0
    private var simulated : Boolean = false
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
        val fa = requireActivity()
        val preferences = PreferenceManager.getDefaultSharedPreferences(fa)
        val prefEditor = preferences.edit()
        simulated = preferences.getBoolean(getString(R.string.simulation_key), false)

        smallCalendar = root.findViewById(R.id.main_single_row_calendar)
        tvDate = root.findViewById(R.id.tvDate)
        tvDay = root.findViewById(R.id.tvDay)
        btnRight = root.findViewById(R.id.btnRight)
        btnLeft = root.findViewById(R.id.btnLeft)
        circleFillView = root.findViewById(R.id.circleFillView)
        circleFillBackText = root.findViewById(R.id.circleFillBackText)
        circleFillForeText = root.findViewById(R.id.circleFillForeText)
        circleFillBackText.setCounterValue(fa, 0, circleFillView.getCircleFillValue(), 1000)
        circleFillForeText.setCounterValue(fa, 0, circleFillView.getCircleFillValue(), 1000)

        root.findViewById<Button>(R.id.btnLog).setOnClickListener {
            // val oldValue = circleFillView.getCircleFillValue()
            // circleFillView.setCircleFillValue((0..100).random(), 1000)
            // circleFillBackText.setCounterValue(requireActivity(), oldValue, circleFillView.getCircleFillValue(), 1000)
            // circleFillForeText.setCounterValue(requireActivity(), oldValue, circleFillView.getCircleFillValue(), 1000)

            // Log period, collect cycle data and initialize new cycle setup
            val builder : AlertDialog.Builder = AlertDialog.Builder(fa)
            builder.setCancelable(true)
            builder.setTitle("Continue Log Period")
            builder.setMessage("This will end this month's cycle and collect cycle data for you")
            builder.setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
                run {
                    prefEditor.putBoolean(getString(R.string.log_period_key), true)
                    prefEditor.apply()
                    fa.recreate()
                }
            }
            val dialog : AlertDialog = builder.create()
            dialog.show()
        }

        // set current date to calendar and current month to currentMonth variable
        currentCalendar.time = Date()
        currentMonth = currentCalendar[Calendar.MONTH]

        initSmallCalendar(getFutureDatesOfCurrentMonth())
        btnRight.setOnClickListener {
            initSmallCalendar(getDatesOfNextMonth())
        }
        btnLeft.setOnClickListener {
            initSmallCalendar(getDatesOfPreviousMonth())
        }

        if (simulated)
            root.findViewById<Button>(R.id.btnSimulatedDate).visibility = View.VISIBLE

        return root
    }

    private fun initSmallCalendar(dates: List<Date>) {
        val smallCalendarViewManager = object : CalendarViewManager {
            override fun setCalendarViewResourceId(
                position: Int,
                date: Date,
                isSelected: Boolean
            ): Int {
                val cal = Calendar.getInstance()
                cal.time = date
                return if (isSelected) when (isToday(cal)) {
                    true -> R.layout.period_selected_calendar_item
                    false -> R.layout.selected_calendar_item
                } else when (isToday(cal)) {
                    true -> R.layout.period_calendar_item
                    false -> R.layout.calendar_item
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
                tvDate.text = getString(R.string.text_month_day, DateUtils.getMonthName(date), DateUtils.getDayNumber(date), DateUtils.getYear(date))
                tvDay.text = DateUtils.getDay3LettersName(date)
                super.whenSelectionChanged(isSelected, position, date)
            }
        }

        // selection manager is responsible for managing selection
        val smallSelectionManager = object : CalendarSelectionManager {
            override fun canBeItemSelected(position: Int, date: Date): Boolean {
                return if (simulated) {
                    todayCalendar.time = date
                    true
                } else isToday(Calendar.getInstance().apply { time = date })
            }
        }

        // here we init our calendar, also you can set more properties if you need them
        smallCalendar.calendarViewManager = smallCalendarViewManager
        smallCalendar.calendarChangesObserver = smallCalendarChangesObserver
        smallCalendar.calendarSelectionManager = smallSelectionManager
        smallCalendar.setDates(dates)
        smallCalendar.init()

        if (isSameYearAndMonth(currentCalendar)) {
            var scrollPos = todayCalendar.get(Calendar.DAY_OF_MONTH) - 3
            if (scrollPos < 0) scrollPos = 0
            smallCalendar.select(todayCalendar.get(Calendar.DAY_OF_MONTH) - 1)
            smallCalendar.scrollToPosition(scrollPos)
        } else {
            if (simulated) smallCalendar.select(0)
            smallCalendar.scrollToPosition(0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dayDistance(date1: Date, date2: Date): Int {
        val cal1 = Calendar.getInstance().apply {
            time = date1
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            time = date2
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return abs(TimeUnit.MILLISECONDS.toDays(cal1.time.time - cal2.time.time).toInt())
    }

    private fun isSameYear(date: Calendar): Boolean {
        val today = Calendar.getInstance()
        today.time = Date()

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR)
    }

    private fun isSameYearAndMonth(date: Calendar): Boolean {
        val today = Calendar.getInstance()
        today.time = Date()

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == date.get(Calendar.MONTH)
    }

    private fun isToday(date: Calendar): Boolean {
        val today = Calendar.getInstance()
        today.time = Date()

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
    }

    private fun getDatesOfNextMonth(): List<Date> {
        currentMonth++ // + because we want next month
        if (currentMonth == 12) {
            // we will switch to january of next year, when we reach last month of year
            currentCalendar.set(Calendar.YEAR, currentCalendar[Calendar.YEAR] + 1)
            currentMonth = 0 // 0 == january
        }
        return getDates(mutableListOf())
    }

    private fun getDatesOfPreviousMonth(): List<Date> {
        currentMonth-- // - because we want previous month
        if (currentMonth == -1) {
            // we will switch to december of previous year, when we reach first month of year
            currentCalendar.set(Calendar.YEAR, currentCalendar[Calendar.YEAR] - 1)
            currentMonth = 11 // 11 == december
        }
        return getDates(mutableListOf())
    }

    private fun getFutureDatesOfCurrentMonth(): List<Date> {
        // get all next dates of current month
        currentMonth = currentCalendar[Calendar.MONTH]
        return getDates(mutableListOf())
    }

    private fun getDates(list: MutableList<Date>): List<Date> {
        // load dates of whole month
        currentCalendar.set(Calendar.MONTH, currentMonth)
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1)
        list.add(currentCalendar.time)
        while (currentMonth == currentCalendar[Calendar.MONTH]) {
            currentCalendar.add(Calendar.DATE, +1)
            if (currentCalendar[Calendar.MONTH] == currentMonth)
                list.add(currentCalendar.time)
        }
        currentCalendar.add(Calendar.DATE, -1)
        return list
    }
}