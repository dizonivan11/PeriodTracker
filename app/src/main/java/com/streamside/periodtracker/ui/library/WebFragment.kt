package com.streamside.periodtracker.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import androidx.fragment.app.Fragment
import com.streamside.periodtracker.MainActivity.Companion.goTo
import com.streamside.periodtracker.R

var WEB_URL: String = ""

class WebFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_web, container, false)
        val btnWebBack = view.findViewById<Button>(R.id.btnWebBack)
        val wvWeb = view.findViewById<WebView>(R.id.wvWeb)
        val wvWebSettings = wvWeb.settings
        wvWebSettings.javaScriptEnabled = true

        btnWebBack.setOnClickListener {
            goTo(R.id.navigation_library)
        }

        if (WEB_URL != "")
            wvWeb.loadUrl(WEB_URL)

        return view
    }
}