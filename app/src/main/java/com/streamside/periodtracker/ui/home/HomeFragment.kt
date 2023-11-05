package com.streamside.periodtracker.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import com.streamside.periodtracker.R
import com.streamside.periodtracker.SAFE_MAX
import com.streamside.periodtracker.data.Period
import com.streamside.periodtracker.data.PeriodViewModel
import com.streamside.periodtracker.views.CircleFillView
import com.streamside.periodtracker.views.CounterView
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

const val CIRCLE_FILL_DURATION = 1000L

class HomeFragment : Fragment() {
    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var currentPeriod: Period
    private val todayCalendar = Calendar.getInstance().apply { time = Date() }
    private val currentCalendar = Calendar.getInstance()
    private var currentYear = 0
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
    private lateinit var btnLog : Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val fa = requireActivity()
        periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            currentPeriod = period

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
            circleFillBackText.setCounterValue(fa, 0, circleFillView.getCircleFillValue(), CIRCLE_FILL_DURATION)
            circleFillForeText.setCounterValue(fa, 0, circleFillView.getCircleFillValue(), CIRCLE_FILL_DURATION)
            btnLog = root.findViewById(R.id.btnLog)

            periodViewModel.all.observe(viewLifecycleOwner) { periods ->
                if (currentPeriod.lastPeriodId > -1) {
                    // Get last period
                    for (i in 0..periods.size) {
                        if (periods[i].id == currentPeriod.lastPeriodId) {
                            val lastPeriod = periods[i]

                            // Check if the last cycle period is on going
                            if (lastPeriod.periodEndYear == 0 &&
                                lastPeriod.periodEndMonth == 0 &&
                                lastPeriod.periodEndDay == 0) {

                                // Make the button for ending period and starting setup
                                addEndEvent(fa, prefEditor)
                            } else {
                                // Make the button for logging period
                                addLogEvent(fa)
                            }
                            break
                        }
                    }
                } else {
                    // Make the button for logging period
                    addLogEvent(fa)
                }
            }

            // set current date to calendar and current month to currentMonth variable
            currentCalendar.time = Date()
            currentYear = currentCalendar[Calendar.YEAR]
            currentMonth = currentCalendar[Calendar.MONTH]

            initSmallCalendar(root, getFutureDatesOfCurrentMonth())
            btnRight.setOnClickListener {
                initSmallCalendar(root, getDatesOfNextMonth())
                // initSmallCalendar(root, getDatesOfNextMonth(), false)
            }
            btnLeft.setOnClickListener {
                initSmallCalendar(root, getDatesOfPreviousMonth())
                // initSmallCalendar(root, getDatesOfPreviousMonth(), false)
            }
        }

        return root
    }

    private fun addLogEvent(fa: FragmentActivity) {
        btnLog.setOnClickListener {
            // Log period and initialize new cycle while tracking period length
            val builder : AlertDialog.Builder = AlertDialog.Builder(fa)
            builder.setCancelable(true)
            builder.setTitle("Confirm Log Cycle")
            builder.setMessage("This will start your period phase")
            builder.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
                run {
                    if (smallCalendar.getSelectedDates().isNotEmpty()) {
                        val today = Calendar.getInstance().apply { time = smallCalendar.getSelectedDates()[0] }

                        // Prepare new cycle
                        periodViewModel.init(
                            currentPeriod.id,
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH)
                        ).observe(viewLifecycleOwner) { newPeriodId ->

                            // Update foreign keys of the current cycle to finalize it
                            currentPeriod.nextPeriodId = newPeriodId
                            periodViewModel.update(currentPeriod)

                            // Refresh app
                            fa.recreate()
                        }
                    } else {
                        Toast.makeText(fa, "Please select a date on small calendar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            val dialog : AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun addEndEvent(fa: FragmentActivity, prefEditor: Editor) {
        // Update button text to period phase
        btnLog.text = "End Period"

        btnLog.setOnClickListener {
            // End period, collect cycle data and initialize new cycle setup
            val builder : AlertDialog.Builder = AlertDialog.Builder(fa)
            builder.setCancelable(true)
            builder.setTitle("Confirm End Cycle")
            builder.setMessage("This will end your period phase")
            builder.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
                run {
                    if (smallCalendar.getSelectedDates().isNotEmpty()) {
                        periodViewModel.all.observe(viewLifecycleOwner) { periods ->
                            // Update last cycle's period end date
                            for (i in 0..periods.size) {
                                if (periods[i].id == currentPeriod.lastPeriodId) {
                                    val today = Calendar.getInstance().apply { time = smallCalendar.getSelectedDates()[0] }
                                    val lastPeriod = periods[i]
                                    lastPeriod.periodEndYear = today.get(Calendar.YEAR)
                                    lastPeriod.periodEndMonth = today.get(Calendar.MONTH)
                                    lastPeriod.periodEndDay = today.get(Calendar.DAY_OF_MONTH)
                                    periodViewModel.update(lastPeriod)
                                    break
                                }
                            }

                            // Set log period flag to true then restart app
                            prefEditor.putBoolean(getString(R.string.log_period_key), true)
                            prefEditor.apply()
                            fa.recreate()
                        }
                    } else {
                        Toast.makeText(fa, "Please select a date on small calendar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            val dialog : AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun initSmallCalendar(root: View, dates: List<Date>, firstTime: Boolean = true) {
        val smallCalendarViewManager = object : CalendarViewManager {
            override fun setCalendarViewResourceId(
                position: Int,
                date: Date,
                isSelected: Boolean
            ): Int {
                val cal = Calendar.getInstance()
                cal.time = date
                val periodDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentPeriod.periodYear)
                    set(Calendar.MONTH, currentPeriod.periodMonth)
                    set(Calendar.DAY_OF_MONTH, currentPeriod.periodDay)
                }
                return if (isSelected) when (isSameDay(cal, periodDate)) {
                    true -> R.layout.period_selected_calendar_item
                    false -> R.layout.selected_calendar_item
                } else when (isSameDay(cal, periodDate)) {
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
                // Select today's date
                return if (simulated) {
                    todayCalendar.time = date
                    updateCircleFill(todayCalendar)
                    true
                } else {
                    val today = Calendar.getInstance().apply { time = date }
                    if (isToday(today)) {
                        updateCircleFill(today)
                        true
                    } else {
                        false
                    }
                }
            }
        }

        // here we init our calendar, also you can set more properties if you need them
        smallCalendar.calendarViewManager = smallCalendarViewManager
        smallCalendar.calendarChangesObserver = smallCalendarChangesObserver
        smallCalendar.calendarSelectionManager = smallSelectionManager
        smallCalendar.setDates(dates)
        if (firstTime) smallCalendar.init()
        root.findViewById<TextView>(R.id.tvSelectedMonth).text = getString(R.string.text_month_year, DateFormatSymbols().months[currentMonth], currentYear.toString())

        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            if (isSameYearAndMonth(currentCalendar)) {
                var scrollPos: Int
                if (simulated) {
                    scrollPos = period.periodDay - 3
                    if (scrollPos < 0) scrollPos = 0
                    smallCalendar.select(period.periodDay - 1)
                    smallCalendar.scrollToPosition(scrollPos)
                } else {
                    scrollPos = todayCalendar.get(Calendar.DAY_OF_MONTH) - 3
                    if (scrollPos < 0) scrollPos = 0
                    smallCalendar.select(todayCalendar.get(Calendar.DAY_OF_MONTH) - 1)
                    smallCalendar.scrollToPosition(scrollPos)
                }
            } else {
                if (simulated) smallCalendar.select(0)
                smallCalendar.scrollToPosition(0)
            }
        }
    }

    private fun updateCircleFill(today: Calendar) {
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { currentPeriod ->
            val fa = requireActivity()
            val currentPeriodDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentPeriod.periodYear)
                set(Calendar.MONTH, currentPeriod.periodMonth)
                set(Calendar.DAY_OF_MONTH, currentPeriod.periodDay)
            }
            val gap = dayDistance(today.time, currentPeriodDate.time)
            val percentage: Float = (gap.toFloat() / SAFE_MAX) * 100f
            val oldValue = circleFillView.getCircleFillValue()
            circleFillView.setCircleFillValue(percentage.toInt(), CIRCLE_FILL_DURATION)
            circleFillBackText.setCounterValue(fa, oldValue, circleFillView.getCircleFillValue(), CIRCLE_FILL_DURATION)
            circleFillForeText.setCounterValue(fa, oldValue, circleFillView.getCircleFillValue(), CIRCLE_FILL_DURATION)
        }
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

    private fun isSameDay(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
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
            currentYear = currentCalendar[Calendar.YEAR] + 1
            currentCalendar.set(Calendar.YEAR, currentYear)
            currentMonth = 0 // 0 == january
        }
        return getDates(mutableListOf())
    }

    private fun getDatesOfPreviousMonth(): List<Date> {
        currentMonth-- // - because we want previous month
        if (currentMonth == -1) {
            // we will switch to december of previous year, when we reach first month of year
            currentYear = currentCalendar[Calendar.YEAR] - 1
            currentCalendar.set(Calendar.YEAR, currentYear)
            currentMonth = 11 // 11 == december
        }
        return getDates(mutableListOf())
    }

    private fun getFutureDatesOfCurrentMonth(): List<Date> {
        // get all next dates of current month
        currentYear = currentCalendar[Calendar.YEAR]
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