// File: data/api/RetrofitInstance.kt
package com.smartkrishi.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://api.openai.com/"

    // ⚠️ OPENAI API KEY - Fixed (removed OpenWeather key)
    const val OPENAI_API_KEY = "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"

    // OpenWeather API key (separate - use this for weather features)
    const val OPENWEATHER_API_KEY = "2d83729a95f53171fdef4447b4373c1a"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: OpenAIApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApiService::class.java)
    }
}
