package com.streamside.periodtracker.ui.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
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
import com.streamside.periodtracker.MainActivity.Companion.clearObservers
import com.streamside.periodtracker.MainActivity.Companion.getDataViewModel
import com.streamside.periodtracker.MainActivity.Companion.getHealthViewModel
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.MainActivity.Companion.isNotEmptyHealthProfile
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.health.HealthViewModel
import com.streamside.periodtracker.data.library.Library
import com.streamside.periodtracker.data.library.SearchAdapter
import com.streamside.periodtracker.data.period.DataViewModel
import com.streamside.periodtracker.data.period.Subject
import com.streamside.periodtracker.views.CardView2
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
        clearObservers(fa, viewLifecycleOwner)

        val cv2Header = root.findViewById<CardView2>(R.id.cv2Header)
        val svSearchBox = root.findViewById<SearchView>(R.id.svSearchBox)
        rvSearch = root.findViewById(R.id.rvSearch)
        val cv2CreateProfile = root.findViewById<CardView>(R.id.cv2CreateProfile)
        val tvCreateProfile = root.findViewById<TextView>(R.id.tvCreateProfile)
        val tvCreateProfileContent = root.findViewById<TextView>(R.id.tvCreateProfileContent)
        val btnCreateProfile = root.findViewById<Button>(R.id.btnCreateProfile)
        val llMainCards = root.findViewById<GridLayout>(R.id.llMainCards)
        val tvRandomTip = root.findViewById<TextView>(R.id.tvRandomTip)
        val cv2RandomTip = root.findViewById<CardView2>(R.id.cv2RandomTip)
        val cv2Tracker = root.findViewById<CardView2>(R.id.cv2Tracker)

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
            cv2RandomTip.setOnClickListener { it2 ->
                randomArticle.callback.invoke(it2)
            }
        }

        healthViewModel.all.observe(viewLifecycleOwner) { healthProfiles ->
            if (healthProfiles.isNotEmpty()) {
                val healthProfile = healthProfiles[0]

                if (isNotEmptyHealthProfile(healthProfile)) {
                    CREATE_PROFILE_INITIAL_VISIBILITY = View.GONE
                    cv2CreateProfile.visibility = View.GONE
                    cv2Header.setCardText("Welcome Back ${healthProfile.name}!")
                } else {
                    CREATE_PROFILE_INITIAL_VISIBILITY = View.VISIBLE
                    tvCreateProfile.text = "Update Your Health Profile"
                    tvCreateProfileContent.text = "Health profile incomplete, app features may be limited"
                    btnCreateProfile.text = getString(R.string.button_update)
                }
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
                        cv2Tracker.visibility = View.GONE
                        cv2RandomTip.visibility = View.GONE
                        filterSearchQuery(query)
                    } else {
                        cv2Header.visibility = View.VISIBLE
                        rvSearch.visibility = View.GONE
                        cv2CreateProfile.visibility = CREATE_PROFILE_INITIAL_VISIBILITY
                        llMainCards.visibility = View.VISIBLE
                        tvRandomTip.visibility = View.VISIBLE
                        cv2Tracker.visibility = View.VISIBLE
                        cv2RandomTip.visibility = View.VISIBLE
                    }
                    true
                }
            }
        })

        cv2Tracker.setOnClickListener { goTo(R.id.navigation_tracker) }

        btnCreateProfile.setOnClickListener {
            goTo(R.id.navigation_health_setup)
        }

        return root
    }

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
                    isChild(symptom, trimmedQuery)) {
                    filteredList.add(library)
                    break
                }
            }
        }
        searchAdapter.updateData(filteredList)
    }

    private fun isChild(symptom: String, trimmedQuery: String): Boolean {
        var parent = findParentOf(symptom, symptoms)

        while (true) {
            if (parent == "") return false
            else {
                if (parent.contains(trimmedQuery, true)) {
                    return true
                } else {
                    parent = findParentOf(parent, symptoms)
                }
            }
        }
    }

    private fun findParentOf(child: String, list: Map<String, Subject>): String {
        var parent = ""
        for (symptom in list.keys) {
            val l = list[symptom] ?: continue
            if (child == symptom) {
                parent = l.parent
                break
            } else {
                parent = findParentOf(child, l.children)
                if (parent.isNotEmpty()) break
            }
        }
        return parent
    }
}