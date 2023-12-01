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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.michalsvec.singlerowcalendar.calendar.CalendarChangesObserver
import com.michalsvec.singlerowcalendar.calendar.CalendarViewManager
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendar
import com.michalsvec.singlerowcalendar.calendar.SingleRowCalendarAdapter
import com.michalsvec.singlerowcalendar.selection.CalendarSelectionManager
import com.michalsvec.singlerowcalendar.utils.DateUtils
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.views.chart.ChartView
import com.streamside.periodtracker.MainActivity.Companion.clearObservers
import com.streamside.periodtracker.MainActivity.Companion.dayDistance
import com.streamside.periodtracker.MainActivity.Companion.getPeriodViewModel
import com.streamside.periodtracker.MainActivity.Companion.isSameDay
import com.streamside.periodtracker.MainActivity.Companion.isSameYearAndMonth
import com.streamside.periodtracker.MainActivity.Companion.isToday
import com.streamside.periodtracker.MainActivity.Companion.restart
import com.streamside.periodtracker.MainActivity.Companion.toCalendar
import com.streamside.periodtracker.OVULATION
import com.streamside.periodtracker.PREGNANCY_WINDOW
import com.streamside.periodtracker.R
import com.streamside.periodtracker.SAFE_MAX
import com.streamside.periodtracker.SAFE_MIN
import com.streamside.periodtracker.SAFE_PERIOD_MAX
import com.streamside.periodtracker.SAFE_PERIOD_MIN
import com.streamside.periodtracker.data.InsightsAdapter
import com.streamside.periodtracker.data.AppDataBuilder
import com.streamside.periodtracker.data.Library
import com.streamside.periodtracker.data.Period
import com.streamside.periodtracker.data.PeriodViewModel
import com.streamside.periodtracker.setup.SymptomsFragment.Companion.hasSymptomsOn
import com.streamside.periodtracker.setup.SymptomsFragment.Companion.symptomsPeriod
import com.streamside.periodtracker.views.CircleFillView
import com.streamside.periodtracker.views.CounterView
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Date

const val CIRCLE_FILL_DURATION = 1000L
const val MAX_HISTORY = 3
const val MAX_TREND = 9

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
    private lateinit var rvInsights : RecyclerView
    private lateinit var tvPrompt : TextView
    private lateinit var tvMyCycleStatus : TextView
    private lateinit var tvLastCycleLength : TextView
    private lateinit var tvLastCycleLengthStatus : TextView
    private lateinit var tvLastPeriodLength : TextView
    private lateinit var tvLastPeriodLengthStatus : TextView
    private lateinit var linearCycleHistory : LinearLayout
    private lateinit var chartCycleTrend: ChartView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val fa = requireActivity()
        clearObservers(fa, viewLifecycleOwner)
        periodViewModel = getPeriodViewModel(fa)
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
        btnLog = root.findViewById(R.id.btnLog)
        rvInsights = root.findViewById(R.id.rvInsights)
        tvPrompt = root.findViewById(R.id.tvPrompt)
        tvMyCycleStatus = root.findViewById(R.id.tvMyCycleStatus)
        tvLastCycleLength = root.findViewById(R.id.tvLastCycleLength)
        tvLastCycleLengthStatus = root.findViewById(R.id.tvLastCycleLengthStatus)
        tvLastPeriodLength = root.findViewById(R.id.tvLastPeriodLength)
        tvLastPeriodLengthStatus = root.findViewById(R.id.tvLastPeriodLengthStatus)
        linearCycleHistory = root.findViewById(R.id.linearCycleHistory)
        chartCycleTrend = root.findViewById(R.id.chartCycleTrend)

        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            currentPeriod = period

            val currentPeriodDate = toCalendar(currentPeriod.periodYear, currentPeriod.periodMonth, currentPeriod.periodDay)
            val currentPeriodDays = dayDistance(currentPeriodDate.time, Date())

            if (!simulated) {
                currentCalendar.time = Date()
            } else {
                currentCalendar.time = currentPeriodDate.time
            }
            currentYear = currentCalendar[Calendar.YEAR]
            currentMonth = currentCalendar[Calendar.MONTH]

            circleFillBackText.setCounterValue(fa, currentPeriodDays, CIRCLE_FILL_DURATION)
            circleFillForeText.setCounterValue(fa, currentPeriodDays, CIRCLE_FILL_DURATION)

            // Display last period statistics (part 1)
            tvLastCycleLengthStatus.text = currentPeriod.menstrualCycle

            periodViewModel.lastPeriod.observe(viewLifecycleOwner) { lastPeriod ->
                if (lastPeriod != null) {
                    // Insights section
                    rvInsights.layoutManager = LinearLayoutManager(fa, LinearLayoutManager.HORIZONTAL, false)
                    val insightsData: MutableList<Library> = mutableListOf()
                    val selectedPeriodSymptoms = symptomsPeriod(currentPeriod, lastPeriod)
                    for (insight in AppDataBuilder.getLibraryData()) {
                        // Invisible library data will be displayed as Insight item
                        if (!insight.visible) insightsData.add(insight)
                        var include = false
                        for (c in selectedPeriodSymptoms.symptoms.categories) {
                            // Check if at least one was checked on this category
                            for (symptom in insight.symptoms) {
                                if (c.id == symptom) {
                                    include = hasSymptomsOn(c)
                                    break
                                }
                            }
                            if (!include) {
                                for (s in c.symptoms) {
                                    // Check for individual symptom check value
                                    for (symptom in insight.symptoms) {
                                        if (s.id == symptom) {
                                            include = s.value
                                            break
                                        }
                                    }
                                    if (include) break
                                }
                            }
                        }
                        if (include) insightsData.add(insight)
                    }
                    rvInsights.adapter = InsightsAdapter(insightsData)

                    val lastPeriodDate = toCalendar(lastPeriod.periodYear, lastPeriod.periodMonth, lastPeriod.periodDay)
                    val lastPeriodEndDate = toCalendar(lastPeriod.periodEndYear, lastPeriod.periodEndMonth, lastPeriod.periodEndDay)

                    // Check if the last cycle period is on going
                    if (lastPeriod.periodEndYear == 0 &&
                        lastPeriod.periodEndMonth == 0 &&
                        lastPeriod.periodEndDay == 0) {

                        // Change circle fill view mode to period phase
                        circleFillView.setPeriodMode(true)

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
            periodViewModel.all.observe(viewLifecycleOwner) { periods ->
                linearCycleHistory.removeAllViews()
                var n = 0
                for (i in periods.size - 1 downTo 0) {
                    val historyPeriod = periods[i]
                    val hasLastCycle = historyPeriod.lastPeriodId > -1
                    val hasNextCycle = historyPeriod.nextPeriodId > -1
                    if (!hasLastCycle || !hasNextCycle) continue
                    if (n++ > MAX_HISTORY - 1) break

                    val periodDate = toCalendar(historyPeriod.periodYear, historyPeriod.periodMonth, historyPeriod.periodDay)

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
                    periodViewModel.get(historyPeriod.lastPeriodId).observe(viewLifecycleOwner) { lastPeriod ->
                        if (lastPeriod != null) {
                            val lastPeriodDate = toCalendar(lastPeriod.periodYear, lastPeriod.periodMonth, lastPeriod.periodDay)
                            llInnerCycleGap.text = "Cycle length: ${dayDistance(periodDate.time, lastPeriodDate.time)}"
                            val lastPeriodEndDate = toCalendar(lastPeriod.periodEndYear, lastPeriod.periodEndMonth, lastPeriod.periodEndDay)
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

                n = 0
                val entries: MutableList<FloatEntry> = mutableListOf()
                val periodEntries: MutableList<FloatEntry> = mutableListOf()
                for (p in periods.size - 1 downTo 0) {
                    if (n++ > MAX_TREND - 1) break

                    val entryPeriodDate = toCalendar(periods[p].periodYear, periods[p].periodMonth, periods[p].periodDay)
                    var lastEntryPeriodDate = entryPeriodDate
                    if (p > 0) lastEntryPeriodDate = toCalendar(periods[p - 1].periodYear, periods[p - 1].periodMonth, periods[p - 1].periodDay)
                    val gap = dayDistance(entryPeriodDate.time, lastEntryPeriodDate.time).toFloat()
                    entries.add(FloatEntry(p + 1f, gap))

                    var periodLength = 0f
                    if (p > 0) {
                        val entryPeriodEndDate = toCalendar(periods[p - 1].periodEndYear, periods[p - 1].periodEndMonth, periods[p - 1].periodEndDay)
                        if (periods[p - 1].periodEndYear != 0 &&
                            periods[p - 1].periodEndDay != 0) {
                            periodLength = dayDistance(entryPeriodDate.time, entryPeriodEndDate.time).toFloat()
                        }
                    }
                    periodEntries.add(FloatEntry(p + 1f, periodLength))
                }
                try {
                    chartCycleTrend.setModel(entryModelOf(entries, periodEntries))
                } catch (ex: Exception) {
                    chartCycleTrend.setModel(entryModelOf(entriesOf(0f)))
                }
            }
        }
        return root
    }

    private fun getCurrentPrompt(today: Calendar) {
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { currentPeriod ->
            val currentPeriodDate = toCalendar(currentPeriod.periodYear, currentPeriod.periodMonth, currentPeriod.periodDay)

            // Update current cycle status
            when (dayDistance(today.time, currentPeriodDate.time)) {
                in 0..SAFE_PERIOD_MAX ->
                    tvPrompt.text = getString(R.string.prompt_period)
                in SAFE_PERIOD_MAX..<PREGNANCY_WINDOW ->
                    tvPrompt.text = getString(R.string.prompt_follicular)
                in PREGNANCY_WINDOW..<OVULATION ->
                    tvPrompt.text = getString(R.string.prompt_pregnant)
                OVULATION ->
                    tvPrompt.text = getString(R.string.prompt_ovulation)
                in OVULATION..<SAFE_MIN ->
                    tvPrompt.text = getString(R.string.prompt_luteal)
                in SAFE_MIN..SAFE_MAX ->
                    tvPrompt.text = getString(R.string.prompt_regular)
                else ->
                    tvPrompt.text = getString(R.string.prompt_irregular)
            }
        }
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

                        // Prepare new cycle with starting period date as today's small calendar
                        periodViewModel.init(
                            currentPeriod.id,
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH)
                        ).observe(viewLifecycleOwner) { newPeriodId ->
                            // Update foreign keys of the current cycle to finalize it
                            currentPeriod.nextPeriodId = newPeriodId
                            periodViewModel.update(currentPeriod)

                            // Update new period's menstrual cycle condition
                            periodViewModel.get(newPeriodId).observe(viewLifecycleOwner) { newPeriod ->
                                val newPeriodDate = toCalendar(newPeriod.periodYear, newPeriod.periodMonth, newPeriod.periodDay)

                                periodViewModel.get(newPeriod.lastPeriodId).observe(viewLifecycleOwner) { lastPeriod ->
                                    val lastPeriodDate = toCalendar(lastPeriod.periodYear, lastPeriod.periodMonth, lastPeriod.periodDay)

                                    // Check if the period gap happened between SAFE_MIN and SAFE_MAX and mark it as regular
                                    if (dayDistance(newPeriodDate.time, lastPeriodDate.time) in SAFE_MIN..SAFE_MAX)
                                        newPeriod.menstrualCycle = getString(R.string.menstrual_cycle_regular)
                                    else
                                        newPeriod.menstrualCycle = getString(R.string.menstrual_cycle_irregular)

                                    // Update menstrual cycle condition
                                    periodViewModel.update(newPeriod)

                                    // Restart app
                                    restart(fa, viewLifecycleOwner)
                                }
                            }
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
                        periodViewModel.lastPeriod.observe(viewLifecycleOwner) { lastPeriod ->
                            if (lastPeriod != null) {
                                // Update last cycle's period end date
                                val today = Calendar.getInstance().apply { time = smallCalendar.getSelectedDates()[0] }
                                lastPeriod.periodEndYear = today.get(Calendar.YEAR)
                                lastPeriod.periodEndMonth = today.get(Calendar.MONTH)
                                lastPeriod.periodEndDay = today.get(Calendar.DAY_OF_MONTH)
                                periodViewModel.update(lastPeriod)
                            }

                            // Set log period flag to true then restart app
                            prefEditor.putBoolean(getString(R.string.log_period_key), true)
                            prefEditor.apply()
                            restart(fa, viewLifecycleOwner)
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
                val periodDate = toCalendar(currentPeriod.periodYear, currentPeriod.periodMonth, currentPeriod.periodDay)
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
                holder.itemView.findViewById<TextView>(R.id.tv_date_calendar_item).text = DateUtils.getDayNumber(date)
                holder.itemView.findViewById<TextView>(R.id.tv_day_calendar_item).text = DateUtils.getDay3LettersName(date)
            }
        }

        val smallCalendarChangesObserver = object : CalendarChangesObserver {
            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
                tvDate.text = getString(R.string.text_month_day, DateUtils.getMonthName(date), DateUtils.getDayNumber(date), DateUtils.getYear(date))
                tvDay.text = DateUtils.getDay3LettersName(date)
                super.whenSelectionChanged(isSelected, position, date)
            }
        }

        val smallSelectionManager = object : CalendarSelectionManager {
            override fun canBeItemSelected(position: Int, date: Date): Boolean {
                val today = Calendar.getInstance().apply { time = date }

                // Switch prompt messages based on today date on small calendar
                getCurrentPrompt(today)

                // Select today's date
                return if (simulated) {
                    todayCalendar.time = date
                    updateCircleFill(todayCalendar)
                    true
                } else {
                    if (isToday(today)) {
                        updateCircleFill(today)
                        true
                    } else {
                        false
                    }
                }
            }
        }

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

        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { period ->
            var scrollPos: Int
            if (simulated) {
                val periodDate = toCalendar(period.periodYear, period.periodMonth, period.periodDay)
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
        periodViewModel.currentPeriod.observe(viewLifecycleOwner) { currentPeriod ->
            val fa = requireActivity()
            val currentPeriodDate = toCalendar(currentPeriod.periodYear, currentPeriod.periodMonth, currentPeriod.periodDay)
            val gap = dayDistance(today.time, currentPeriodDate.time)
            var maxFillValue = SAFE_MAX

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

            if (circleFillView.getPeriodMode()) maxFillValue = SAFE_PERIOD_MAX
            val percentage: Float = (gap.toFloat() / maxFillValue) * 100f
            circleFillView.setCircleFillValue(percentage.toInt(), CIRCLE_FILL_DURATION)
            circleFillBackText.setCounterValue(fa, gap, CIRCLE_FILL_DURATION)
            circleFillForeText.setCounterValue(fa, gap, CIRCLE_FILL_DURATION)
        }
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