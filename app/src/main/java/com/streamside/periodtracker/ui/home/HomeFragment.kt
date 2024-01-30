package com.streamside.periodtracker.ui.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.streamside.periodtracker.MainActivity.Companion.getDataViewModel
import com.streamside.periodtracker.MainActivity.Companion.getHealthViewModel
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.MainActivity.Companion.isNotEmptyHealthProfile
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.health.Health
import com.streamside.periodtracker.data.health.HealthViewModel
import com.streamside.periodtracker.data.library.Library
import com.streamside.periodtracker.data.library.SearchAdapter
import com.streamside.periodtracker.data.period.Category
import com.streamside.periodtracker.data.period.DataViewModel
import com.streamside.periodtracker.data.period.InsightsAdapter
import com.streamside.periodtracker.data.period.Subject
import com.streamside.periodtracker.data.period.Symptom
import com.streamside.periodtracker.ui.library.FILTER
import com.streamside.periodtracker.ui.library.LibraryFragment.Companion.isChild
import com.streamside.periodtracker.views.CardView2
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.random.Random

private var CREATE_PROFILE_INITIAL_VISIBILITY = View.INVISIBLE

class HomeFragment : Fragment() {
    private lateinit var dataViewModel: DataViewModel
    private lateinit var healthViewModel: HealthViewModel
    private lateinit var symptoms: Map<String, Subject>
    private var libraryList: List<Library> = listOf()
    private lateinit var rvSearch: RecyclerView
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val fa = requireActivity()
        dataViewModel = getDataViewModel(fa)
        healthViewModel = getHealthViewModel(fa)

        val cv2Header = root.findViewById<CardView2>(R.id.cv2Header)
        val btnUpdateProfile = root.findViewById<Button>(R.id.btnUpdateProfile)
        val svSearchBox = root.findViewById<SearchView>(R.id.svSearchBox)
        rvSearch = root.findViewById(R.id.rvSearch)
        val cv2CreateProfile = root.findViewById<CardView>(R.id.cv2CreateProfile)
        val tvCreateProfile = root.findViewById<TextView>(R.id.tvCreateProfile)
        val tvCreateProfileContent = root.findViewById<TextView>(R.id.tvCreateProfileContent)
        val btnCreateProfile = root.findViewById<Button>(R.id.btnCreateProfile)
        val llMainCards = root.findViewById<GridLayout>(R.id.llMainCards)
        val cvWeight = root.findViewById<CardView>(R.id.cvWeight)
        val tvWeight = root.findViewById<TextView>(R.id.tvWeight)
        val cvHeight = root.findViewById<CardView>(R.id.cvHeight)
        val tvHeight = root.findViewById<TextView>(R.id.tvHeight)
        val cvBMI = root.findViewById<CardView>(R.id.cvBMI)
        val llBMI = root.findViewById<LinearLayout>(R.id.llBMI)
        val tvBMI = root.findViewById<TextView>(R.id.tvBMI)
        val tvBMIStatus = root.findViewById<TextView>(R.id.tvBMIStatus)
        val tvBMIRange = root.findViewById<TextView>(R.id.tvBMIRange)
        val tvBMITips = root.findViewById<TextView>(R.id.tvBMITips)
        val cv2Step = root.findViewById<CardView2>(R.id.cv2Step)
        val tvRecommendedTip = root.findViewById<TextView>(R.id.tvRecommendedTip)
        val rvRecommendedTips = root.findViewById<RecyclerView>(R.id.rvRecommendedTips)
        val cv2Tips = root.findViewById<CardView2>(R.id.cv2Tips)
        val tvRandomTip = root.findViewById<TextView>(R.id.tvRandomTip)
        val cv2RandomTip = root.findViewById<CardView2>(R.id.cv2RandomTip)
        val llCategories = root.findViewById<LinearLayout>(R.id.llCategories)
        val cv2Menstruation = root.findViewById<CardView2>(R.id.cv2Menstruation)
        val cv2Hair = root.findViewById<CardView2>(R.id.cv2Hair)
        val cv2Skin = root.findViewById<CardView2>(R.id.cv2Skin)
        val cv2Eyes = root.findViewById<CardView2>(R.id.cv2Eyes)
        val cv2Upper = root.findViewById<CardView2>(R.id.cv2Upper)
        val cv2DentalOral = root.findViewById<CardView2>(R.id.cv2DentalOral)
        val cv2Lower = root.findViewById<CardView2>(R.id.cv2Lower)

        searchAdapter = SearchAdapter(this, listOf())
        rvSearch.layoutManager = LinearLayoutManager(fa, LinearLayoutManager.VERTICAL, false)
        rvSearch.adapter = searchAdapter

        dataViewModel.getSymptomsData().observe(viewLifecycleOwner) { symptomsList ->
            symptoms = symptomsList
        }

        dataViewModel.getLibraryData().observe(viewLifecycleOwner) { articles ->
            val filteredArticles: MutableList<Library> = articles.filter { it.visible }.toMutableList()
            filteredArticles.removeAt(0)
            libraryList = filteredArticles

            healthViewModel.all.observe(viewLifecycleOwner) { healthProfiles ->
                if (healthProfiles.isNotEmpty()) {
                    val healthProfile = healthProfiles[0]

                    if (isNotEmptyHealthProfile(healthProfile)) {
                        CREATE_PROFILE_INITIAL_VISIBILITY = View.GONE
                        cv2CreateProfile.visibility = View.GONE
                        cv2Header.setCardText("Welcome Back ${healthProfile.name}!")

                        // WEIGHT
                        tvWeight.text = "${healthProfile.weight}kg"

                        // HEIGHT
                        val heightFeet = healthProfile.height / 12
                        val heightInches = healthProfile.height % 12
                        tvHeight.text = "${heightFeet}ft ${heightInches}in"

                        // BMI
                        val bmi = getBMI(healthProfile.weight, toCM(healthProfile.height))
                        val df = DecimalFormat("#.#")
                        df.roundingMode = RoundingMode.HALF_DOWN
                        tvBMI.text = df.format(bmi)
                        updateBMIInfo(bmi, llBMI, tvBMIStatus, tvBMIRange, tvBMITips)

                        // Recommended tips section
                        rvRecommendedTips.layoutManager = LinearLayoutManager(fa, LinearLayoutManager.HORIZONTAL, false)
                        val recommendedTips: MutableList<Library> = mutableListOf()
                        for (library in libraryList) {
                            var include = false
                            for (c in gatherRecommendedTips(healthProfile)) {
                                if (!include) {
                                    for (s in c.symptoms) {
                                        // Check for individual symptom check value
                                        for (symptom in library.symptoms) {
                                            if (s.id == symptom) {
                                                include = s.value
                                                break
                                            }
                                        }
                                        if (include) break
                                    }
                                }
                            }
                            if (include) recommendedTips.add(library)
                        }
                        rvRecommendedTips.adapter = InsightsAdapter(this, recommendedTips)
                    } else {
                        btnUpdateProfile.visibility = View.GONE
                        CREATE_PROFILE_INITIAL_VISIBILITY = View.VISIBLE
                        cvWeight.visibility = View.GONE
                        cvHeight.visibility = View.GONE
                        cvBMI.visibility = View.GONE
                        tvRecommendedTip.visibility = View.GONE
                        rvRecommendedTips.visibility = View.GONE
                        tvCreateProfile.text = "Update Your Health Profile"
                        tvCreateProfileContent.text = "Health profile incomplete, app features may be limited"
                        btnCreateProfile.text = getString(R.string.button_update)
                    }
                }
            }

            // Random tips of the day section
            val randomArticle = filteredArticles[Random.nextInt(0, filteredArticles.size)]
            Glide.with(fa).load(randomArticle.image).centerCrop().listener(object :
                RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    cv2RandomTip.setCardImage(ResourcesCompat.getDrawable(fa.resources, R.drawable.default_library_image, fa.theme))
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    cv2RandomTip.setCardImage(resource)
                    return false
                }
            }).preload()
            cv2RandomTip.setCardText(randomArticle.title)
            cv2RandomTip.setOnClickListener {
                randomArticle.callback.invoke(it)
            }

            cv2Step.setOnClickListener { goTo(fa, viewLifecycleOwner, R.id.navigation_step) }

            cv2Tips.setOnClickListener {
                FILTER.clear()
                goTo(fa, viewLifecycleOwner, R.id.navigation_library)
            }

            btnUpdateProfile.setOnClickListener {
                goTo(fa, viewLifecycleOwner, R.id.navigation_health_setup)
            }

            btnCreateProfile.setOnClickListener {
                goTo(fa, viewLifecycleOwner, R.id.navigation_health_setup)
            }

            cv2Menstruation.setOnClickListener {
                FILTER.clear()
                FILTER.add("Menstruation")
                goTo(fa, viewLifecycleOwner, R.id.navigation_library)
            }

            cv2Hair.setOnClickListener {
                FILTER.clear()
                FILTER.add("Hair")
                goTo(fa, viewLifecycleOwner, R.id.navigation_library)
            }

            cv2Skin.setOnClickListener {
                FILTER.clear()
                FILTER.add("Skin")
                goTo(fa, viewLifecycleOwner, R.id.navigation_library)
            }

            cv2Eyes.setOnClickListener {
                FILTER.clear()
                FILTER.add("Eyes")
                goTo(fa, viewLifecycleOwner, R.id.navigation_library)
            }

            cv2DentalOral.setOnClickListener {
                FILTER.clear()
                FILTER.add("Dental and Oral")
                goTo(fa, viewLifecycleOwner, R.id.navigation_library)
            }

            cv2Upper.setOnClickListener {
                FILTER.clear()
                FILTER.add("Upper Body")
                goTo(fa, viewLifecycleOwner, R.id.navigation_library)
            }

            cv2Lower.setOnClickListener {
                FILTER.clear()
                FILTER.add("Lower Body")
                goTo(fa, viewLifecycleOwner, R.id.navigation_library)
            }
        }

        svSearchBox.setOnQueryTextListener(object: OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return if (libraryList.isEmpty() || CREATE_PROFILE_INITIAL_VISIBILITY == View.INVISIBLE) false
                else {
                    if (!query.isNullOrEmpty()) {
                        cv2Header.visibility = View.GONE
                        rvSearch.visibility = View.VISIBLE
                        cv2CreateProfile.visibility = View.GONE
                        llMainCards.visibility = View.GONE
                        tvRandomTip.visibility = View.GONE
                        cv2Tips.visibility = View.GONE
                        cv2RandomTip.visibility = View.GONE
                        llCategories.visibility = View.GONE
                        filterSearchQuery(query)
                    } else {
                        cv2Header.visibility = View.VISIBLE
                        rvSearch.visibility = View.GONE
                        cv2CreateProfile.visibility = CREATE_PROFILE_INITIAL_VISIBILITY
                        llMainCards.visibility = View.VISIBLE
                        tvRandomTip.visibility = View.VISIBLE
                        cv2Tips.visibility = View.VISIBLE
                        cv2RandomTip.visibility = View.VISIBLE
                        llCategories.visibility = View.VISIBLE
                    }
                    true
                }
            }
        })

        return root
    }

    private fun gatherRecommendedTips(healthProfile: Health): List<Category> {
        val result = mutableListOf<Category>()

        val heightInCM = toCM(healthProfile.height)
        val bmi = getBMI(healthProfile.weight, heightInCM)
        val fitness = Category("Fitness", mutableListOf())
        if (bmi < 18.5) {
            // Underweight
            fitness.symptoms.add(Symptom("Underweight", visible = false, value = true))
        } else if (bmi >= 18.5 && bmi < 25) {
            // Healthy
            fitness.symptoms.add(Symptom("Healthy", visible = false, value = true))
        } else if (bmi >= 25 && bmi < 30) {
            // Overweight
            fitness.symptoms.add(Symptom("Overweight", visible = false, value = true))
        } else {
            // Obesity
            fitness.symptoms.add(Symptom("Obesity", visible = false, value = true))
        }
        result.add(fitness)

        return result
    }

    private fun updateBMIInfo(bmi: Float, llBMI: LinearLayout, tvBMIStatus: TextView, tvBMIRange: TextView, tvBMITips: TextView) {
        if (bmi < 18.5) {
            // Underweight
            tvBMIStatus.text = "Underweight"
            tvBMIRange.text = getString(R.string.range_underweight)
            tvBMITips.text = getString(R.string.tips_underweight)
        } else if (bmi >= 18.5 && bmi < 25) {
            // Healthy
            tvBMIStatus.text = "Healthy"
            tvBMIRange.text = getString(R.string.range_healthy)
            tvBMITips.text = getString(R.string.tips_healthy)
            llBMI.setBackgroundResource(R.drawable.bmi_good_bg)
        } else if (bmi >= 25 && bmi < 30) {
            // Overweight
            tvBMIStatus.text = "Overweight"
            tvBMIRange.text = getString(R.string.range_overweight)
            tvBMITips.text = getString(R.string.tips_overweight)
        } else {
            // Obesity
            tvBMIStatus.text = "Obesity"
            tvBMIRange.text = getString(R.string.range_obesity)
            tvBMITips.text = getString(R.string.tips_obesity)
            llBMI.setBackgroundResource(R.drawable.bmi_critical_bg)
        }
    }

    private fun toCM(inches: Int) = inches * 2.54f
    private fun getBMI(weight: Int, height: Float) = weight / (height * 0.01f).pow(2)

    private fun filterSearchQuery(query: String) {
        val trimmedQuery = query.trim()
        val filteredList = mutableListOf<Library>()
        for (library in libraryList) {
            if (library.title.contains(trimmedQuery, true)) {
                filteredList.add(library)
                continue
            }

            for (symptom in library.symptoms) {
                if (symptom.contains(trimmedQuery, true) ||
                    isChild(symptoms, symptom, trimmedQuery, true)) {
                    filteredList.add(library)
                    break
                }
            }
        }
        searchAdapter.updateData(filteredList)
    }
}