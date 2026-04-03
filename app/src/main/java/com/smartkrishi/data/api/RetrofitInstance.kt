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
