package com.streamside.periodtracker.ui.home

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamside.periodtracker.data.checkup.CheckUpList
import com.streamside.periodtracker.data.checkup.CheckUpResult
import com.streamside.periodtracker.data.health.Health
import com.streamside.periodtracker.data.library.Library
import com.streamside.periodtracker.data.period.InsightsAdapter
import com.streamside.periodtracker.data.period.SymptomList
import com.streamside.periodtracker.data.step.Step
import com.streamside.periodtracker.ui.step.DAILY_GOAL
import kotlin.math.pow

class AutoRecommendAI(
    private var library: List<Library>,
    private var symptoms: SymptomList) {
    private var selects: MutableList<String> = mutableListOf()

    fun execute(f: Fragment, rv: RecyclerView, vararg modules: () -> Unit) {
        // Build result from sub-modules
        for (subModule in modules) subModule()

        // Display result to RecyclerView
        rv.layoutManager = LinearLayoutManager(f.requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        val recommendedTips: MutableList<Library> = mutableListOf()

        for (l in library) {
            // Check for selected titles
            if (selects.contains(l.title)) {
                recommendedTips.add(l)
            } else {
                // Check if symptom is included
                var include = false
                for (c in symptoms.categories) {
                    if (!include) {
                        for (s in c.symptoms) {
                            // Check for individual symptom check value
                            for (symptom in l.symptoms) {
                                if (s.id == symptom) {
                                    include = s.value
                                    break
                                }
                            }
                            if (include) break
                        }
                    }
                }
                if (include) recommendedTips.add(l)
            }
        }
        recommendedTips.shuffle()
        rv.adapter = InsightsAdapter(f, recommendedTips)
    }

    // SUB-MODULES
    lateinit var healthProfile: Health
    fun bmi() {
        val heightInCM = toCM(healthProfile.height)
        val bmi = getBMI(healthProfile.weight, heightInCM)
        if (bmi < 18.5) {
            include("Underweight")
        } else if (bmi >= 18.5 && bmi < 25) {
            include("Healthy")
        } else if (bmi >= 25 && bmi < 30) {
            include("Overweight")
        } else {
            include("Obesity")
        }
    }
    lateinit var firstToCurrentSteps: List<Step>
    fun step() {
        var average = 0.00
        for (step in firstToCurrentSteps) { average += step.progress }
        average /= firstToCurrentSteps.size

        when {
            average == 0.00 -> include("Cardio Exercises")
            average < DAILY_GOAL * 0.25 -> include("Walking")
            average < DAILY_GOAL * 0.50 -> include("Brisk Walking")
            average < DAILY_GOAL * 0.75 -> include("Running")
            else -> include("Regular Running")
        }
    }
    fun stepFocused()  {
        include("Biking")
        include("Hiking")
        select("5 Health Tips for Women to Follow This Year for Better Health")
        select("Women's Health Tips for Heart, Mind, and Body")
    }
    var todayCheckUp: CheckUpResult? = null
    fun checkup() {
        todayCheckUp?.let { ask(it.list) }
    }
    private fun ask(list: CheckUpList) {
        for (checkUp in list.list) {
            when (checkUp.question) {
                "Do you have any cold?" -> {
                    if (isYes(checkUp.answer)) {
                        select("The Do’s and Don’ts of Easing Cold Symptoms")
                    }
                }
                "How many days is the fever now?" -> {
                    try {
                        val n = checkUp.answer.toInt()
                        when {
                            n < 2 -> {
                                // 1 day
                                select("How to Manage a Fever")
                            }
                            n < 3 -> {
                                // 2 days
                                select("10 Ways to Reduce Fever from a Cold or Flu")
                            }
                            else -> {
                                // 3 days+
                                select("Fever: Home Treatment and When to See a Doctor")
                            }
                        }
                    } catch (ex: Exception) { Log.i("Fever", ex.message.toString()) }
                }
                "Did you sleep well last night?" -> {
                    if (!isYes(checkUp.answer)) {
                        select("What are sleep disorders?")
                    }
                }
                "Are you physically active?" -> {
                    when(checkUp.answer) {
                        "Rarely" -> select("How to Start Exercising: A Beginner’s Guide to Working Out")
                        "Everyday" -> select("Is It OK to Work Out Every Day?")
                        "No" -> select("7 Tips To Motivate Yourself To Exercise")
                    }
                }
                "What are you trying to achieve?" -> {
                    when(checkUp.answer) {
                        "Gain weight" -> select("10 Best Exercise to Gain Weight at Home with Proper Diet Plan")
                        "Lose weight" -> select("7 Best Exercises To Lose Weight At Home")
                        "Maintain weight" -> select("Physical Activity for a Healthy Weight")
                        "Increase overall health" -> select("Women’s health: 5 things women must do to stay fit")
                    }
                }
                "When is the last time you exercise?" -> {
                    when(checkUp.answer) {
                        "A week ago" -> select("17 Women Share Their Best Tips For Getting Motivated To Work Out")
                        "A month ago" -> select("This 4-Week Workout Plan Will Have You Feeling Strong and Fit")
                        "6 months ago" -> select("How to Start Exercising Again When It’s Been…a While")
                    }
                }
                "What type of medication?" -> {
                    when(checkUp.answer) {
                        "Supplements" -> select("The Truth About Supplements: 5 Things You Should Know")
                        "Prescribed" -> select("Just What the Doctor Ordered: The Importance of Taking Your Medication as Prescribed")
                    }
                }
                "Do you smoke?" -> {
                    when(checkUp.answer) {
                        "Rarely" -> select("Light and social smoking carry cardiovascular risks")
                        "Always" -> select("Health Effects of Cigarette Smoking")
                    }
                }
                "Do you drink alcohol?" -> {
                    when(checkUp.answer) {
                        "Rarely" -> select("Alcohol: Balancing Risks and Benefits")
                        "Always" -> select("Excessive Alcohol Use is a Risk to Women’s Health")
                    }
                }
            }
            ask(checkUp.children)
        }
    }

    // HELPERS
    private fun include(category: String) {
        for (c in symptoms.categories)
            for (s in c.symptoms)
                if (s.id == category)
                    s.value = true
    }
    private fun select(title: String) { if (!selects.contains(title)) selects.add(title) }
    private fun isYes(yn: String) = yn == "Yes"
    private fun toCM(inches: Int) = inches * 2.54f
    private fun getBMI(weight: Int, height: Float) = weight / (height * 0.01f).pow(2)
}