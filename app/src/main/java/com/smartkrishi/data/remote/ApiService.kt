package com.smartkrishi.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 🩺 Check ML server status (Flask/FastAPI)
    @GET("health")
    suspend fun checkServer(): Response<String>

    // 🌾 ML Disease Prediction
    @Multipart
    @POST("disease/predict")
    suspend fun predictDisease(
        @Part image: MultipartBody.Part
    ): Response<DiseaseResponse>
}
