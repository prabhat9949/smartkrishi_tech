// File: data/api/OpenAIApiService.kt
package com.smartkrishi.data.api

import com.smartkrishi.data.models.ChatCompletionRequest
import com.smartkrishi.data.models.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
