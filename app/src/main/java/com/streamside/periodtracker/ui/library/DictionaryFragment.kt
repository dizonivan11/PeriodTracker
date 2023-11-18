package com.streamside.periodtracker.ui.library

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.DictionaryEntry
import com.streamside.periodtracker.net.ApiHandler
import com.streamside.periodtracker.net.ApiServices
import retrofit2.Call
import retrofit2.Response

class DictionaryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dictionary, container, false)
        val fa = requireActivity()
        val mainDictionary = view.findViewById<LinearLayout>(R.id.mainDictionary)
        val btnDictionaryBack = view.findViewById<Button>(R.id.btnDictionaryBack)
        val editSearchDictionary = view.findViewById<EditText>(R.id.editSearchDictionary)
        val btnSearchDictionary = view.findViewById<Button>(R.id.btnSearchDictionary)
        mainDictionary.visibility = View.GONE

        btnDictionaryBack.setOnClickListener {
            fa.supportFragmentManager.beginTransaction()
                .replace(R.id.frameLibrary, LibraryHomeFragment()).commit()
        }

        btnSearchDictionary.setOnClickListener {
            getDictionaryEntry(view, fa, ApiHandler.getApiServices(), editSearchDictionary.text.toString())
            mainDictionary.visibility = View.VISIBLE
        }

        return view
    }

    private fun getDictionaryEntry(root: View, fa: FragmentActivity, api: ApiServices, word: String) {
        api.getDictionaryEntry(word).enqueue(object: retrofit2.Callback<List<DictionaryEntry>> {
            override fun onResponse(
                call: Call<List<DictionaryEntry>>,
                response: Response<List<DictionaryEntry>>
            ) {
                val wordDictionary = root.findViewById<TextView>(R.id.wordDictionary)
                val meaningsDictionary = root.findViewById<LinearLayout>(R.id.meaningsDictionary)
                wordDictionary.text = getString(R.string.label_no_result)
                meaningsDictionary.removeAllViews()

                if (response.isSuccessful) {
                    response.body()?.let {
                        for (entry in it) {
                            wordDictionary.text = entry.word

                            for (meaning in entry.meanings) {
                                val llMeaning = LinearLayout(fa)
                                val llMeaningParams: LinearLayout.LayoutParams =
                                    LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT)
                                llMeaningParams.setMargins(0, 80, 0, 0)
                                llMeaning.setPadding(40, 40, 40, 40)
                                llMeaning.layoutParams = llMeaningParams
                                llMeaning.orientation = LinearLayout.VERTICAL
                                llMeaning.background = ContextCompat.getDrawable(fa, R.drawable.card2_background)
                                meaningsDictionary.addView(llMeaning)

                                val partOfSpeech = TextView(fa)
                                val partOfSpeechParams: ViewGroup.MarginLayoutParams =
                                    ViewGroup.MarginLayoutParams(
                                        ViewGroup.MarginLayoutParams.MATCH_PARENT,
                                        ViewGroup.MarginLayoutParams.WRAP_CONTENT)
                                partOfSpeech.layoutParams = partOfSpeechParams
                                partOfSpeech.text = meaning.partOfSpeech
                                llMeaning.addView(partOfSpeech)

                                for (definition in meaning.definitions) {
                                    val llDefinition = LinearLayout(fa)
                                    val llDefinitionParams: LinearLayout.LayoutParams =
                                        LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT)
                                    llDefinitionParams.setMargins(0, 20, 0, 0)
                                    llDefinition.setPadding(40, 40, 40, 40)
                                    llDefinition.layoutParams = llDefinitionParams
                                    llDefinition.orientation = LinearLayout.VERTICAL
                                    llDefinition.background = ContextCompat.getDrawable(fa, R.drawable.card3_background)
                                    llMeaning.addView(llDefinition)

                                    val tvDefinition = TextView(fa)
                                    val tvDefinitionParams: ViewGroup.MarginLayoutParams =
                                        ViewGroup.MarginLayoutParams(
                                            ViewGroup.MarginLayoutParams.MATCH_PARENT,
                                            ViewGroup.MarginLayoutParams.WRAP_CONTENT)
                                    tvDefinition.layoutParams = tvDefinitionParams
                                    tvDefinition.text = definition.definition
                                    llDefinition.addView(tvDefinition)

                                    if (definition.example != null) {
                                        val llExample = LinearLayout(fa)
                                        val llExampleParams: LinearLayout.LayoutParams =
                                            LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT)
                                        llExampleParams.setMargins(0, 20, 0, 0)
                                        llExample.setPadding(40, 40, 40, 40)
                                        llExample.layoutParams = llExampleParams
                                        llExample.orientation = LinearLayout.VERTICAL
                                        llExample.background = ContextCompat.getDrawable(fa, R.drawable.card_background)
                                        llDefinition.addView(llExample)

                                        val tvExample = TextView(fa)
                                        val tvExampleParams: ViewGroup.MarginLayoutParams =
                                            ViewGroup.MarginLayoutParams(
                                                ViewGroup.MarginLayoutParams.MATCH_PARENT,
                                                ViewGroup.MarginLayoutParams.WRAP_CONTENT)
                                        tvExample.layoutParams = tvExampleParams
                                        tvExample.text = getString(R.string.label_example, definition.example)
                                        llExample.addView(tvExample)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.i("API Error", "${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<List<DictionaryEntry>>, t: Throwable) {
                Log.i("API Error", "${t.message}")
            }
        })
    }
}