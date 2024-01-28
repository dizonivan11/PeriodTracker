package com.streamside.periodtracker.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamside.periodtracker.MainActivity.Companion.getDataViewModel
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.library.LibraryAdapter
import com.streamside.periodtracker.data.period.Subject

val FILTER: MutableList<String> = mutableListOf()

class LibraryFragment : Fragment() {
    private lateinit var symptoms: Map<String, Subject>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_library, container, false)
        val fa = requireActivity()
        val dataViewModel = getDataViewModel(fa)
        val rvLibrary = view.findViewById<RecyclerView>(R.id.rvLibrary)

        dataViewModel.getSymptomsData().observe(viewLifecycleOwner) { symptomsList ->
            symptoms = symptomsList

            dataViewModel.getLibraryData().observe(viewLifecycleOwner) { libraryData ->
                rvLibrary.layoutManager = GridLayoutManager(fa, 2)
                var filteredLibraryData = libraryData.filter { it.visible }

                if (FILTER.isNotEmpty()) {
                    filteredLibraryData = filteredLibraryData.filter {
                        var filterResult = false
                        for (symptom in it.symptoms) {
                            if (FILTER.contains(symptom)) {
                                filterResult = true
                                break
                            } else {
                                for (filterSymptom in FILTER) {
                                    if (isChild(symptoms, symptom, filterSymptom)) {
                                        filterResult = true
                                        break
                                    }
                                }
                            }
                        }
                        filterResult
                    }
                    FILTER.clear()
                }

                rvLibrary.adapter = LibraryAdapter(this, filteredLibraryData)
            }
        }

        return view
    }

    companion object {


        fun isChild(symptoms: Map<String, Subject>, symptom: String, filter: String, filterContains: Boolean = false): Boolean {
            var parent = findParentOf(symptom, symptoms)

            while (true) {
                if (parent == "") return false
                else {
                    parent = if (filterContains) {
                        if (parent.contains(filter, true)) {
                            return true
                        } else {
                            findParentOf(parent, symptoms)
                        }
                    } else {
                        if (parent == filter) {
                            return true
                        } else {
                            findParentOf(parent, symptoms)
                        }
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
}