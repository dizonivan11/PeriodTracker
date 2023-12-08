package com.streamside.periodtracker.ui.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.streamside.periodtracker.MainActivity.Companion.clearObservers
import com.streamside.periodtracker.MainActivity.Companion.getDataViewModel
import com.streamside.periodtracker.MainActivity.Companion.getHealthViewModel
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.health.HealthViewModel
import com.streamside.periodtracker.data.period.DataViewModel
import com.streamside.periodtracker.views.CardView2
import kotlin.random.Random

class HomeFragment : Fragment() {
    private lateinit var dataViewModel: DataViewModel
    private lateinit var healthViewModel: HealthViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val fa = requireActivity()
        dataViewModel = getDataViewModel(fa)
        healthViewModel = getHealthViewModel(fa)
        clearObservers(fa, viewLifecycleOwner)

        val cv2Header = root.findViewById<CardView2>(R.id.cv2Header)
        val cv2RandomTip = root.findViewById<CardView2>(R.id.cv2RandomTip)
        val cv2Tracker = root.findViewById<CardView2>(R.id.cv2Tracker)
        val btnCreateProfile = root.findViewById<Button>(R.id.btnCreateProfile)

        dataViewModel.getLibraryData().observe(viewLifecycleOwner) { articles ->
            val filteredArticles = articles.filter { it.visible }
            val randomArticle = filteredArticles[Random.nextInt(1, filteredArticles.size)]
            Glide.with(fa).load(randomArticle.image).centerCrop().listener(object :
                RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    cv2RandomTip.setCardImage(ResourcesCompat.getDrawable(fa.resources, R.drawable.default_library_image, fa.theme))
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
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
                cv2Header.setCardText("Welcome Back ${healthProfile.name}!")
            }
        }

        cv2Tracker.setOnClickListener { goTo(R.id.navigation_tracker) }

        btnCreateProfile.setOnClickListener {
            goTo(R.id.navigation_health_setup)
        }

        return root
    }
}