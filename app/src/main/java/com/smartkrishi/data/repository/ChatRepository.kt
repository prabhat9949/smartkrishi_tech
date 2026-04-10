// File: data/repository/ChatRepository.kt
package com.smartkrishi.data.repository

import com.smartkrishi.data.api.OpenAIApiService
import com.smartkrishi.data.api.RetrofitInstance
import com.smartkrishi.data.models.ChatCompletionRequest
import com.smartkrishi.data.models.ChatMessage

class ChatRepository(
    private val apiService: OpenAIApiService = RetrofitInstance.api
) {
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        userName: String
    ): Result<String> {
        return try {
            // Build conversation with system context
            val messages = mutableListOf<ChatMessage>()

            // Add system message with user context
            messages.add(
                ChatMessage(
                    role = "system",
                    content = """You are KrishiMitri, an intelligent agricultural assistant helping farmer $userName. 
                        |You provide expert advice on crops, diseases, weather, farming techniques, and market information. 
                        |Always address the user as $userName and be helpful, friendly, and professional.
                        |Keep responses concise and actionable.""".trimMargin()
                )
            )

            // Add conversation history
            messages.addAll(conversationHistory)

            // Add current user message
            messages.add(ChatMessage(role = "user", content = userMessage))

            val request = ChatCompletionRequest(
                model = "gpt-3.5-turbo",
                messages = messages,
                maxTokens = 500,
                temperature = 0.7
            )

            val response = apiService.getChatCompletion(
                authorization = "Bearer ${RetrofitInstance.OPENAI_API_KEY}",
                request = request
            )

            val botReply = response.choices.firstOrNull()?.message?.content
                ?: "Sorry, I couldn't generate a response."

            Result.success(botReply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
