package com.streamside.periodtracker.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.streamside.periodtracker.R

class LibraryHomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_library_home, container, false)
        val fa = requireActivity()
        val cvDictionary = view.findViewById<CardView>(R.id.cvDictionary)

        cvDictionary.setOnClickListener {
            fa.supportFragmentManager.beginTransaction()
                .replace(R.id.frameLibrary, DictionaryFragment()).commit()
        }

        return view
    }
}