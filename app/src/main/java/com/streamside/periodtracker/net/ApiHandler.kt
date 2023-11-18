package com.streamside.periodtracker.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/"

class ApiHandler {
    companion object {
        @Volatile
        private var INSTANCE: ApiServices? = null

        fun getApiServices(): ApiServices {
            val temp = INSTANCE
            if (temp != null) return temp
            synchronized(this) {
                val instance = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiServices::class.java)

                INSTANCE = instance
                return instance
            }
        }
    }
}