// File: presentation/chat/KrishiMitriChatViewModel.kt
package com.smartkrishi.presentation.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkrishi.data.models.ChatMessage
import com.smartkrishi.data.repository.ChatRepository
import kotlinx.coroutines.launch

class KrishiMitriChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {  // ✅ Changed from AndroidViewModel to ViewModel

    var uiState by mutableStateOf(ChatUiState())
        private set

    private val conversationHistory = mutableListOf<ChatMessage>()

    var userName: String = "Farmer"
        private set

    init {
        sendWelcomeMessage()
    }

    /**
     * Set username from navigation or session
     */
    fun setUserName(name: String) {
        userName = name.ifBlank { "Farmer" }
        if (uiState.messages.isEmpty()) {
            sendWelcomeMessage()
        }
    }

    private fun sendWelcomeMessage() {
        val welcomeMessage = ChatMessageUi(
            text = "Namaste $userName! 🙏\n\nI'm KrishiMitri, your AI-powered farming assistant. " +
                    "I can help you with crop diseases, weather information, market prices, and farming best practices.\n\n" +
                    "How can I assist you today?",
            isUser = false,
            quickReplies = listOf(
                QuickReply("Identify crop disease", "🦠"),
                QuickReply("Weather forecast", "☀️"),
                QuickReply("Market prices", "💰"),
                QuickReply("Best farming tips", "🌱"),
                QuickReply("Fertilizer advice", "🧪")
            )
        )

        uiState = uiState.copy(messages = listOf(welcomeMessage))
    }

    fun onUserSend(message: String) {
        if (message.isBlank() || uiState.isLoading) return

        val userMessage = ChatMessageUi(text = message, isUser = true)

        uiState = uiState.copy(
            messages = uiState.messages + userMessage,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val result = repository.sendMessage(
                    userMessage = message,
                    conversationHistory = conversationHistory,
                    userName = userName
                )

                result.onSuccess { botReply ->
                    handleSuccessfulResponse(message, botReply)
                }.onFailure { error ->
                    handleErrorResponse(error)
                }
            } catch (e: Exception) {
                handleErrorResponse(e)
            }
        }
    }

    private fun handleSuccessfulResponse(userMessage: String, botReply: String) {
        conversationHistory.add(ChatMessage(role = "user", content = userMessage))
        conversationHistory.add(ChatMessage(role = "assistant", content = botReply))

        if (conversationHistory.size > 20) {
            repeat(conversationHistory.size - 20) {
                conversationHistory.removeAt(0)
            }
        }

        val quickReplies = generateContextualQuickReplies(botReply)

        val botMessage = ChatMessageUi(
            text = botReply,
            isUser = false,
            quickReplies = quickReplies
        )

        uiState = uiState.copy(
            messages = uiState.messages + botMessage,
            isLoading = false
        )
    }

    private fun generateContextualQuickReplies(botReply: String): List<QuickReply> {
        val lowerReply = botReply.lowercase()

        return when {
            lowerReply.contains("disease") || lowerReply.contains("pest") -> listOf(
                QuickReply("Prevention tips", "🛡️"),
                QuickReply("Treatment options", "💊")
            )
            lowerReply.contains("weather") || lowerReply.contains("rain") -> listOf(
                QuickReply("7-day forecast", "📅"),
                QuickReply("Irrigation advice", "💧")
            )
            lowerReply.contains("price") || lowerReply.contains("market") -> listOf(
                QuickReply("Nearby mandis", "🏪"),
                QuickReply("Best time to sell", "⏰")
            )
            else -> listOf(
                QuickReply("More info", "ℹ️"),
                QuickReply("Ask something else", "💬")
            )
        }
    }

    private fun handleErrorResponse(error: Throwable) {
        val errorText = when {
            error.message?.contains("Unable to resolve host") == true -> {
                "Unable to connect. Please check your internet connection."
            }
            error.message?.contains("401") == true -> {
                "Authentication failed. Please check API key configuration."
            }
            error.message?.contains("429") == true -> {
                "Too many requests. Please wait a moment."
            }
            else -> {
                "Sorry, I encountered an error: ${error.message}. Please try again."
            }
        }

        val errorMessage = ChatMessageUi(
            text = errorText,
            isUser = false,
            quickReplies = listOf(QuickReply("Retry", "🔄"))
        )

        uiState = uiState.copy(
            messages = uiState.messages + errorMessage,
            isLoading = false,
            error = error.message
        )
    }

    fun onQuickReplyClick(reply: QuickReply) {
        when {
            reply.text.equals("Retry", ignoreCase = true) -> {
                val lastUserMessage = uiState.messages.lastOrNull { it.isUser }?.text
                if (lastUserMessage != null) {
                    onUserSend(lastUserMessage)
                }
            }
            else -> onUserSend(reply.text)
        }
    }

    fun resetChat() {
        conversationHistory.clear()
        uiState = ChatUiState()
        sendWelcomeMessage()
    }
}
