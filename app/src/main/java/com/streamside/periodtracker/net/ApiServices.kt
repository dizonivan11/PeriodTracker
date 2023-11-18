package com.streamside.periodtracker.net

import com.streamside.periodtracker.data.DictionaryEntry
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiServices {
    @GET("entries/en/{word}")
    fun getDictionaryEntry(@Path("word") word: String?): Call<List<DictionaryEntry>>
}