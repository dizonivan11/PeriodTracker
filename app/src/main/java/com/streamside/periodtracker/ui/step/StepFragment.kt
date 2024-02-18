package com.streamside.periodtracker.ui.step

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.michalsvec.singlerowcalendar.utils.DateUtils
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.views.chart.ChartView
import com.streamside.periodtracker.MainActivity.Companion.getDataViewModel
import com.streamside.periodtracker.MainActivity.Companion.getStepViewModel
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.DataViewModel
import com.streamside.periodtracker.data.library.Library
import com.streamside.periodtracker.data.step.Step
import com.streamside.periodtracker.data.step.StepViewModel
import com.streamside.periodtracker.data.step.WeeklyStepAdapter
import com.streamside.periodtracker.ui.home.AutoRecommendAI
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

const val ANIMATION_DURATION: Long = 1000
const val STEPS_IN_KM = 1312.3359580052
const val DAILY_GOAL = 10000
private const val MAX_HISTORY = 7

class StepFragment : Fragment(), SensorEventListener {
    private lateinit var dataViewModel: DataViewModel
    private lateinit var stepViewModel: StepViewModel
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialStepCounterSensorValue: Float = -1f
    private var initialStepProgressValue: Int = -1
    private lateinit var currentStep: Step
    private lateinit var tvDailyGoal: TextView
    private lateinit var tvLastDailyGoal: TextView
    private lateinit var stepProgress: CircularProgressIndicator
    private lateinit var tvStepProgress: TextView
    private lateinit var tvStepProgressInStep: TextView
    private lateinit var btnStepToggle: Button
    private var stepToggle: Boolean = false
    private lateinit var rvRecommendedTips: RecyclerView
    private lateinit var tvWeekProgress: TextView
    private lateinit var rvWeekProgress: RecyclerView
    private val weekSteps: MutableList<Step> = mutableListOf()
    private lateinit var tvStepTrend: TextView
    private lateinit var chartStepTrend: ChartView
    private val entries: MutableList<FloatEntry> = mutableListOf()

    private var isInitialized = false
    private var weeklyInitialized = false
    private var monthlyInitialized = false

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_step, container, false)
        val fa = requireActivity()
        dataViewModel = getDataViewModel(fa)
        stepViewModel = getStepViewModel(fa)
        sensorManager = fa.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        tvDailyGoal = view.findViewById(R.id.tvDailyGoal)
        tvLastDailyGoal = view.findViewById(R.id.tvLastDailyGoal)
        stepProgress = view.findViewById(R.id.stepProgress)
        tvStepProgress = view.findViewById(R.id.tvStepProgress)
        tvStepProgressInStep = view.findViewById(R.id.tvStepProgressInStep)
        btnStepToggle = view.findViewById(R.id.btnStepToggle)
        rvRecommendedTips = view.findViewById(R.id.rvRecommendedTips)
        tvWeekProgress = view.findViewById(R.id.tvWeekProgress)
        rvWeekProgress = view.findViewById(R.id.rvWeekProgress)
        tvStepTrend = view.findViewById(R.id.tvStepTrend)
        chartStepTrend = view.findViewById(R.id.chartStepTrend)
        val todayDate = Calendar.getInstance()

        stepViewModel.all.observe(viewLifecycleOwner) {
            stepViewModel.getFromDate(todayDate.time).observe(viewLifecycleOwner) { stepToday ->
                if (!isInitialized) {
                    if (stepToday != null) {
                        currentStep = stepToday
                        initialStepProgressValue = currentStep.progress
                        updateDataToday()
                        val yesterdayDate: Calendar = Calendar.getInstance().apply {
                            add(Calendar.DATE, -1)
                        }
                        stepViewModel.getFromDate(yesterdayDate.time).observe(viewLifecycleOwner) { stepYesterday ->
                            if (stepYesterday != null) updateDataYesterday(stepYesterday)
                        }
                    } else {
                        // Create New Data for Today
                        currentStep = Step(0, todayDate.time, DAILY_GOAL, 0)
                        stepViewModel.add(currentStep).observe(viewLifecycleOwner) { newStepId ->
                            if (newStepId != null) {
                                currentStep.id = newStepId
                            }
                            initialStepProgressValue = currentStep.progress
                            updateDataToday()
                        }
                    }

                    dataViewModel.getLibraryData().observe(viewLifecycleOwner) { articles ->
                        val filteredArticles: MutableList<Library> = articles.filter { it.visible }.toMutableList()
                        filteredArticles.removeAt(0)

                        dataViewModel.newSymptomsData().observe(viewLifecycleOwner) { newSymptomList ->
                            val firstDayOfMonth = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
                            stepViewModel.getFromDateBetween(firstDayOfMonth.time, todayDate.time).observe(viewLifecycleOwner) { firstToCurrentSteps ->
                                val ai = AutoRecommendAI(filteredArticles, newSymptomList)
                                ai.firstToCurrentSteps = firstToCurrentSteps
                                ai.execute(this, rvRecommendedTips, ai::step, ai::stepFocused)
                            }
                        }
                    }

                    tvWeekProgress.text = "Past ${MAX_HISTORY} Days Progress"
                    rvWeekProgress.layoutManager = LinearLayoutManager(fa, LinearLayoutManager.HORIZONTAL, false)

                    // Fill in blanks with dummy Steps first
                    for (i in 0..<MAX_HISTORY) {
                        weekSteps.add(Step(0, todayDate.time, DAILY_GOAL, 0))
                    }

                    // Check for missing days since the last step record and fill in with existing one
                    for (i in MAX_HISTORY - 1 downTo 0) {
                        val currentDate = Date.from(LocalDate.now().minusDays(i.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant())
                        val si = (MAX_HISTORY - 1) - i
                        fillStepFrom(si, currentDate)
                    }

                    tvStepTrend.text = "Monthly Steps Trend (${DateUtils.getMonthName(todayDate.time)})"
                    val currentMonthDayCount = YearMonth.of(todayDate.get(Calendar.YEAR), todayDate.get(Calendar.MONTH) + 1).lengthOfMonth()
                    // Fill in blanks with dummy data first
                    for (i in 0..<currentMonthDayCount) {
                        entries.add(FloatEntry(i + 1f, 0f))
                    }
                    // Fill in existing data within the max trend range
                    for (i in 0..<currentMonthDayCount) {
                        val currentDate = Calendar.getInstance()
                        currentDate.set(Calendar.DAY_OF_MONTH, i + 1)
                        fillEntryFrom(i, currentDate.time, currentMonthDayCount)
                    }

                    btnStepToggle.setOnClickListener {
                        if(ContextCompat.checkSelfPermission(fa, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
                            requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 0)
                        } else {
                            initialStepCounterSensorValue = -1f
                            stepToggle = !stepToggle

                            if (stepSensor == null) {
                                Toast.makeText(fa, "No step sensor detected", Toast.LENGTH_SHORT).show()
                            } else if (stepToggle) {
                                btnStepToggle.text = "Stop"
                            } else {
                                btnStepToggle.text = "Start"
                                stepViewModel.update(currentStep)
                            }
                        }
                    }
                    isInitialized = true
                }
            }
        }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillStepFrom(index: Int, currentDate: Date) {
        val o = stepViewModel.getFromDate(currentDate)
        if (!o.hasActiveObservers() || !weeklyInitialized) {
            o.observe(viewLifecycleOwner) { s ->
                if (s != null)
                    weekSteps[index] = s
                else
                    weekSteps[index].date = currentDate

                if (index > 0 && index < MAX_HISTORY - 1) {
                    // Check next
                    val nextDate = Date.from(LocalDate.now().minusDays((index - 1).toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant())
                    val si = (MAX_HISTORY - 1) - (index - 1)
                    fillStepFrom(si, nextDate)
                } else {
                    rvWeekProgress.adapter = WeeklyStepAdapter(weekSteps)
                    rvWeekProgress.scrollToPosition((rvWeekProgress.adapter as WeeklyStepAdapter).itemCount - 1)
                }
                weeklyInitialized = true
                o.removeObservers(viewLifecycleOwner)
            }
        }
    }

    private fun fillEntryFrom(index: Int, currentDate: Date, currentMonthDayCount: Int) {
        val o = stepViewModel.getFromDate(currentDate)
        if (!o.hasActiveObservers() || !monthlyInitialized) {
            o.observe(viewLifecycleOwner) { s ->
                if (s != null) {
                    entries[index] = FloatEntry(entries[index].x, s.progress.toFloat())
                }

                if (index < currentMonthDayCount - 1) {
                    // Check next
                    val nextDate = Calendar.getInstance()
                    nextDate.set(Calendar.DAY_OF_MONTH, index + 2)
                    fillEntryFrom(index + 1, nextDate.time, currentMonthDayCount)
                } else chartStepTrend.setModel(entryModelOf(entries))
                monthlyInitialized = true
                o.removeObservers(viewLifecycleOwner)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onStop() {
        super.onStop()

        if (stepSensor != null) {
            sensorManager.unregisterListener(this)
            stepViewModel.update(currentStep)
        }
    }

    override fun onSensorChanged(se: SensorEvent?) {
        if (se != null && stepToggle) {
            if (se.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                if (initialStepCounterSensorValue == -1f)
                    initialStepCounterSensorValue = se.values[0]

                val step = se.values[0] - initialStepCounterSensorValue
                currentStep.progress = initialStepProgressValue + step.toInt()
                updateDataToday()
            }
        }
    }

    override fun onAccuracyChanged(s: Sensor?, i: Int) { }

    private fun updateDataToday() {
        tvDailyGoal.text = "${String.format("%.2f", getKmFromStep(currentStep.goal))}km"
        setProgress(stepProgress, ((currentStep.progress.toDouble() / currentStep.goal.toDouble()) * 100.0).toInt().coerceAtMost(100))
        tvStepProgress.text = "${String.format("%.2f", getKmFromStep(currentStep.progress))}km"
        tvStepProgressInStep.text = "${currentStep.progress} steps"
        // stepViewModel.update(currentStep)
    }

    private fun updateDataYesterday(stepYesterday: Step) {
        tvLastDailyGoal.text = "${String.format("%.2f", getKmFromStep(stepYesterday.progress))}km"
    }

    private fun setProgress(progressBar: CircularProgressIndicator, newValue: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, newValue).apply {
            this.duration = ANIMATION_DURATION
            start()
        }
    }

    private fun getStepFromKm(km: Double): Int {
        return (km * STEPS_IN_KM).toInt()
    }

    private fun getKmFromStep(step: Int): Double {
        return step.toDouble() / STEPS_IN_KM
    }
}