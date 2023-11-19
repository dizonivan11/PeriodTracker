package com.streamside.periodtracker.ui.library

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import com.streamside.periodtracker.R

class WebFragment(url: String) : Fragment() {
    private val url: String

    init {
        this.url = url
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_web, container, false)
        val fa = requireActivity()
        val btnWebBack = view.findViewById<Button>(R.id.btnWebBack)
        val wvWeb = view.findViewById<WebView>(R.id.wvWeb)

        btnWebBack.setOnClickListener {
            fa.supportFragmentManager.beginTransaction()
                .replace(R.id.frameLibrary, LibraryHomeFragment()).commit()
        }

        if (url != "")
            wvWeb.loadUrl(url)

        return view
    }
}