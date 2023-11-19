package com.streamside.periodtracker.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences.Editor
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import com.streamside.periodtracker.MAX_HISTORY
import com.streamside.periodtracker.OVULATION
import com.streamside.periodtracker.PERIOD_VIEW_MODEL
import com.streamside.periodtracker.R
import com.streamside.periodtracker.SAFE_MAX
import com.streamside.periodtracker.SAFE_MIN
import com.streamside.periodtracker.SAFE_PERIOD_MAX
import com.streamside.periodtracker.SAFE_PERIOD_MIN
import com.streamside.periodtracker.data.InsightsAdapter
import com.streamside.periodtracker.data.LibraryDataBuilder
import com.streamside.periodtracker.data.Period
import com.streamside.periodtracker.data.PeriodViewModel
import com.streamside.periodtracker.views.CircleFillView
import com.streamside.periodtracker.views.CounterView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

const val CIRCLE_FILL_DURATION = 1000L

class HomeFragment : Fragment() {
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
    private lateinit var rvInsights : RecyclerView
    private lateinit var tvMyCycleStatus : TextView
    private lateinit var tvLastCycleLength : TextView
    private lateinit var tvLastCycleLengthStatus : TextView
    private lateinit var tvLastPeriodLength : TextView
    private lateinit var tvLastPeriodLengthStatus : TextView
    private lateinit var linearCycleHistory : LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val fa = requireActivity()
        clearObservers()

        PERIOD_VIEW_MODEL.currentPeriod.observe(viewLifecycleOwner) { period ->
            currentPeriod = period

            val currentPeriodDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentPeriod.periodYear)
                set(Calendar.MONTH, currentPeriod.periodMonth)
                set(Calendar.DAY_OF_MONTH, currentPeriod.periodDay)
            }
            val currentPeriodDays = dayDistance(currentPeriodDate.time, Date())

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
            circleFillBackText.setCounterValue(fa, currentPeriodDays, CIRCLE_FILL_DURATION)
            circleFillForeText.setCounterValue(fa, currentPeriodDays, CIRCLE_FILL_DURATION)
            btnLog = root.findViewById(R.id.btnLog)
            rvInsights = root.findViewById(R.id.rvInsights)
            tvMyCycleStatus = root.findViewById(R.id.tvMyCycleStatus)
            tvLastCycleLength = root.findViewById(R.id.tvLastCycleLength)
            tvLastCycleLengthStatus = root.findViewById(R.id.tvLastCycleLengthStatus)
            tvLastPeriodLength = root.findViewById(R.id.tvLastPeriodLength)
            tvLastPeriodLengthStatus = root.findViewById(R.id.tvLastPeriodLengthStatus)
            linearCycleHistory = root.findViewById(R.id.linearCycleHistory)

            // Insights section
            rvInsights.layoutManager = LinearLayoutManager(fa, LinearLayoutManager.HORIZONTAL, false)
            rvInsights.adapter = InsightsAdapter(LibraryDataBuilder.getLibraryData(fa))

            // Display last period statistics (part 1)
            tvLastCycleLengthStatus.text = currentPeriod.menstrualCycle

            PERIOD_VIEW_MODEL.lastPeriod.observe(viewLifecycleOwner) { lastPeriod ->
                if (lastPeriod != null) {
                    val lastPeriodDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, lastPeriod.periodYear)
                        set(Calendar.MONTH, lastPeriod.periodMonth)
                        set(Calendar.DAY_OF_MONTH, lastPeriod.periodDay)
                    }
                    val lastPeriodEndDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, lastPeriod.periodEndYear)
                        set(Calendar.MONTH, lastPeriod.periodEndMonth)
                        set(Calendar.DAY_OF_MONTH, lastPeriod.periodEndDay)
                    }

                    // Check if the last cycle period is on going
                    if (lastPeriod.periodEndYear == 0 &&
                        lastPeriod.periodEndMonth == 0 &&
                        lastPeriod.periodEndDay == 0) {

                        // Make the button for ending period and starting setup
                        addEndEvent(fa, prefEditor)
                    } else {
                        // Display last period statistics (part 2)
                        val lastPeriodLength = dayDistance(lastPeriodEndDate.time, currentPeriodDate.time)
                        if (lastPeriodLength > 1) tvLastPeriodLength.text = "$lastPeriodLength days"
                        else tvLastPeriodLength.text = "$lastPeriodLength day"

                        if (lastPeriodLength in SAFE_PERIOD_MIN..SAFE_PERIOD_MAX) {
                            tvLastPeriodLengthStatus.text = "Normal"
                        } else {
                            tvLastPeriodLengthStatus.text = "Abnormal"
                        }

                        // Make the button for logging period
                        addLogEvent(fa)
                    }

                    // Display last cycle statistics
                    val lastCycleLength = dayDistance(currentPeriodDate.time, lastPeriodDate.time)
                    if (lastCycleLength > 1) tvLastCycleLength.text = "$lastCycleLength days"
                    else tvLastCycleLength.text = "$lastCycleLength day"
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
                // smallCalendar.setDates(getDatesOfNextMonth())
                initSmallCalendar(root, getDatesOfNextMonth())
            }
            btnLeft.setOnClickListener {
                // smallCalendar.setDates(getDatesOfPreviousMonth())
                initSmallCalendar(root, getDatesOfPreviousMonth())
            }

            // Cycle History section
            PERIOD_VIEW_MODEL.all.observe(viewLifecycleOwner) { periods ->
                linearCycleHistory.removeAllViews()
                var n = 0
                for (i in periods.size - 1 downTo 0) {
                    val historyPeriod = periods[i]
                    val hasLastCycle = historyPeriod.lastPeriodId > -1
                    val hasNextCycle = historyPeriod.nextPeriodId > -1
                    if (!hasLastCycle || !hasNextCycle) continue
                    if (n++ > MAX_HISTORY - 1) break

                    val periodDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, historyPeriod.periodYear)
                        set(Calendar.MONTH, historyPeriod.periodMonth)
                        set(Calendar.DAY_OF_MONTH, historyPeriod.periodDay)
                    }

                    val ll = LinearLayout(fa).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        orientation = LinearLayout.HORIZONTAL
                    }
                    if (n > 1) (ll.layoutParams as LinearLayout.LayoutParams).setMargins(0, 50, 0, 0)
                    val llInner = LinearLayout(fa).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        orientation = LinearLayout.VERTICAL
                    }
                    (llInner.layoutParams as LinearLayout.LayoutParams).weight = 1f
                    val llInnerMonthYear = TextView(fa).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT)
                        text = "${DateUtils.getMonthName(periodDate.time)} ${DateUtils.getDayNumber(periodDate.time)}, ${historyPeriod.periodYear}"
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    val llInnerCycleGap = TextView(fa).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT)
                        text = "--"
                    }
                    (llInnerCycleGap.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 8, 0, 0)
                    val llInnerPeriodGap = TextView(fa).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT)
                        text = "--"
                    }
                    (llInnerPeriodGap.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 8, 0, 0)
                    val llSStatus = TextView(fa).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        text = historyPeriod.menstrualCycle
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }

                    var isPeriodEnded = true
                    if (hasLastCycle) {
                        CoroutineScope(MainScope().coroutineContext).launch {
                            val lastPeriod = PERIOD_VIEW_MODEL.get(historyPeriod.lastPeriodId)
                            val lastPeriodDate = Calendar.getInstance().apply {
                                set(Calendar.YEAR, lastPeriod.periodYear)
                                set(Calendar.MONTH, lastPeriod.periodMonth)
                                set(Calendar.DAY_OF_MONTH, lastPeriod.periodDay)
                            }
                            llInnerCycleGap.text = "Cycle length: ${dayDistance(periodDate.time, lastPeriodDate.time)}"
                            val lastPeriodEndDate = Calendar.getInstance().apply {
                                set(Calendar.YEAR, lastPeriod.periodEndYear)
                                set(Calendar.MONTH, lastPeriod.periodEndMonth)
                                set(Calendar.DAY_OF_MONTH, lastPeriod.periodEndDay)
                            }
                            llInnerPeriodGap.text = "Period length: ${dayDistance(periodDate.time, lastPeriodEndDate.time)}"
                            if (lastPeriod.periodEndYear == 0 &&
                                lastPeriod.periodEndMonth == 0 &&
                                lastPeriod.periodEndDay == 0) isPeriodEnded = false
                        }
                    }
                    if (!isPeriodEnded) continue

                    // HIERARCHY
                    // ll
                    //  - ll_inner
                    //      - ll_inner_month_year
                    //      - ll_inner_cycle_gap
                    //      - ll_inner_period_gap
                    //  - ll_status

                    ll.addView(llInner)
                    llInner.addView(llInnerMonthYear)
                    llInner.addView(llInnerCycleGap)
                    llInner.addView(llInnerPeriodGap)
                    ll.addView(llSStatus)
                    linearCycleHistory.addView(ll)
                }
                if (n < 1) {
                    val tvNoHistory = TextView(fa).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT)
                        text = "No history yet"
                    }
                    linearCycleHistory.addView(tvNoHistory)
                }
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
                        PERIOD_VIEW_MODEL.init(
                            currentPeriod.id,
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH)
                        ).observe(viewLifecycleOwner) { newPeriodId ->

                            // Update foreign keys of the current cycle to finalize it
                            currentPeriod.nextPeriodId = newPeriodId
                            PERIOD_VIEW_MODEL.update(currentPeriod)

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
                        PERIOD_VIEW_MODEL.lastPeriod.observe(viewLifecycleOwner) { lastPeriod ->
                            if (lastPeriod != null) {
                                // Update last cycle's period end date
                                val today = Calendar.getInstance().apply { time = smallCalendar.getSelectedDates()[0] }
                                lastPeriod.periodEndYear = today.get(Calendar.YEAR)
                                lastPeriod.periodEndMonth = today.get(Calendar.MONTH)
                                lastPeriod.periodEndDay = today.get(Calendar.DAY_OF_MONTH)
                                PERIOD_VIEW_MODEL.update(lastPeriod)
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

    private fun initSmallCalendar(root: View, dates: List<Date>) {
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
        val sc = smallCalendar.apply {
            calendarViewManager = smallCalendarViewManager
            calendarChangesObserver = smallCalendarChangesObserver
            calendarSelectionManager = smallSelectionManager
            futureDaysCount = 30
            includeCurrentDate = true
            setDates(dates)
            init()
        }
        root.findViewById<TextView>(R.id.tvSelectedMonth).text = getString(R.string.text_month_year, DateFormatSymbols().months[currentMonth], currentYear.toString())

        PERIOD_VIEW_MODEL.currentPeriod.observe(viewLifecycleOwner) { period ->
            var scrollPos: Int
            if (simulated) {
                val periodDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, period.periodYear)
                    set(Calendar.MONTH, period.periodMonth)
                }
                if (isSameYearAndMonth(periodDate, currentCalendar)) {
                    scrollPos = period.periodDay - 3
                    if (scrollPos < 0) scrollPos = 0
                    sc.select(period.periodDay - 1)
                    sc.scrollToPosition(scrollPos)
                } else {
                    sc.select(0)
                    sc.scrollToPosition(0)
                }
            } else {
                if (isSameYearAndMonth(Calendar.getInstance().apply { time = Date() }, currentCalendar)) {
                    scrollPos = todayCalendar.get(Calendar.DAY_OF_MONTH) - 3
                    if (scrollPos < 0) scrollPos = 0
                    sc.select(todayCalendar.get(Calendar.DAY_OF_MONTH) - 1)
                    sc.scrollToPosition(scrollPos)
                } else {
                    sc.scrollToPosition(0)
                }
            }
        }
    }

    private fun updateCircleFill(today: Calendar) {
        PERIOD_VIEW_MODEL.currentPeriod.observe(viewLifecycleOwner) { currentPeriod ->
            val fa = requireActivity()
            val currentPeriodDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentPeriod.periodYear)
                set(Calendar.MONTH, currentPeriod.periodMonth)
                set(Calendar.DAY_OF_MONTH, currentPeriod.periodDay)
            }
            val gap = dayDistance(today.time, currentPeriodDate.time)

            // Update current cycle status
            if (gap <= SAFE_PERIOD_MAX)
                tvMyCycleStatus.text = getString(R.string.cs_period)
            else if (gap < OVULATION)
                tvMyCycleStatus.text = getString(R.string.cs_follicular_phase)
            else if (gap == OVULATION)
                tvMyCycleStatus.text = getString(R.string.cs_ovulation)
            else if (gap < SAFE_MIN)
                tvMyCycleStatus.text = getString(R.string.cs_luteal_phase)
            else
                tvMyCycleStatus.text = getString(R.string.cs_safe_period)

            val percentage: Float = (gap.toFloat() / SAFE_MAX) * 100f
            circleFillView.setCircleFillValue(percentage.toInt(), CIRCLE_FILL_DURATION)
            circleFillBackText.setCounterValue(fa, gap, CIRCLE_FILL_DURATION)
            circleFillForeText.setCounterValue(fa, gap, CIRCLE_FILL_DURATION)
        }
    }

    private fun clearObservers() {
        val periodViewModel = ViewModelProvider(this)[PeriodViewModel::class.java]
        periodViewModel.all.removeObservers(viewLifecycleOwner)
        periodViewModel.lastPeriod.removeObservers(viewLifecycleOwner)
        periodViewModel.currentPeriod.removeObservers(viewLifecycleOwner)
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

    private fun isSameYearAndMonth(date1: Calendar, date2: Calendar): Boolean {
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH)
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