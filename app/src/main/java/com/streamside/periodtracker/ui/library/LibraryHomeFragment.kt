package com.streamside.periodtracker.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.LibraryAdapter
import com.streamside.periodtracker.data.AppDataBuilder

var LIBRARY_CALLBACK: (() -> Unit)? = null

class LibraryHomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_library_home, container, false)
        val fa = requireActivity()
        val rvLibrary = view.findViewById<RecyclerView>(R.id.rvLibrary)

        rvLibrary.layoutManager = GridLayoutManager(fa, 2)
        rvLibrary.adapter = LibraryAdapter(AppDataBuilder.getLibraryData().filter { it.visible })

        // Call stored callback (if any)
        LIBRARY_CALLBACK?.invoke()

        return view
    }
}