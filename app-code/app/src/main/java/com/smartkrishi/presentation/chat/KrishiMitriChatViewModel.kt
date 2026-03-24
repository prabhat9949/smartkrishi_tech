package com.smartkrishi.presentation.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiConfig {
    const val GEMINI_API_KEY = "AIzaSyCRL2SZCh1fDwRb6S9Il_KuZWTO7a29r9U"
    const val GEMINI_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$GEMINI_API_KEY"
}

class KrishiMitriChatViewModel : ViewModel() {

    var uiState by mutableStateOf(ChatUiState())
        private set

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var userName: String = ""

    init {
        showWelcomeSequence()
    }

    private fun showWelcomeSequence() {
        viewModelScope.launch {
            // Message 1: Greeting
            addBotMessage("Hi! 👋")
            delay(1000)

            // Message 2: Welcome
            addBotMessage("Welcome to Smart Krishi! 🌾")
            delay(1200)

            // Message 3: Ask for name
            addBotMessage("I'm KrishiMitri, your smart farming assistant. What's your name?")
        }
    }

    private fun showOptionsMenu() {
        viewModelScope.launch {
            delay(800)
            addBotMessage("Nice to meet you, $userName! 😊")
            delay(1000)

            addBotMessage(
                text = "I can help you with crop management, weather insights, pest control, market prices, and much more!",
            )
            delay(1200)

            addBotMessage(
                text = "What would you like to explore today?",
                quickReplies = listOf(
                    QuickReply("weather", "Weather Updates", "🌤️"),
                    QuickReply("pest", "Pest Control", "🐛"),
                    QuickReply("crop", "Crop Advice", "🌾"),
                    QuickReply("market", "Market Prices", "💰"),
                    QuickReply("fertilizer", "Fertilizer Guide", "🧪"),
                    QuickReply("scheme", "Govt Schemes", "📋")
                )
            )
        }
    }

    private fun addBotMessage(text: String, quickReplies: List<QuickReply> = emptyList()) {
        uiState = uiState.copy(
            messages = uiState.messages + ChatMessage(
                text = text,
                isUser = false,
                quickReplies = quickReplies
            ),
            showQuickReplies = quickReplies.isNotEmpty()
        )
    }

    fun onQuickReplyClick(reply: QuickReply) {
        val responseMap = mapOf(
            "weather" to "Getting latest weather updates for your farm location... 🌦️\n\nPlease tell me your location or pin code for accurate weather data.",
            "pest" to "I'll help you identify and control pests. Which crop are you growing? 🌱",
            "crop" to "I can provide crop-specific advice. What crop do you need help with? 🌾",
            "market" to "I'll fetch latest market prices for agricultural produce. Which commodity are you interested in? 💹",
            "fertilizer" to "I'll recommend the best fertilizers. Tell me about your soil type and crop.",
            "scheme" to "Here are some beneficial government schemes:\n\n✅ PM-KISAN\n✅ Pradhan Mantri Fasal Bima Yojana\n✅ Soil Health Card Scheme\n✅ National Agriculture Market (e-NAM)\n\nWhich one would you like to know more about?"
        )

        onUserSend("${reply.emoji} ${reply.text}")

        viewModelScope.launch {
            delay(800)
            val botResponse = ChatMessage(
                text = responseMap[reply.id] ?: "Let me help you with that!",
                isUser = false
            )
            uiState = uiState.copy(
                messages = uiState.messages + botResponse,
                showQuickReplies = false
            )
        }
    }

    fun onUserSend(message: String) {
        if (message.isBlank() || uiState.isLoading) return

        val trimmed = message.trim()

        // Check if we're waiting for the user's name
        if (userName.isEmpty() && !trimmed.startsWith("🌤️") && !trimmed.startsWith("🐛")
            && !trimmed.startsWith("🌾") && !trimmed.startsWith("💰")
            && !trimmed.startsWith("🧪") && !trimmed.startsWith("📋")) {
            userName = trimmed
            val userMsg = ChatMessage(text = trimmed, isUser = true)
            uiState = uiState.copy(messages = uiState.messages + userMsg)
            showOptionsMenu()
            return
        }

        val userMsg = ChatMessage(text = trimmed, isUser = true)

        uiState = uiState.copy(
            messages = uiState.messages + userMsg,
            isLoading = true,
            showQuickReplies = false
        )

        val conversation = uiState.messages

        viewModelScope.launch {
            val reply = withContext(Dispatchers.IO) {
                askGemini(conversation)
            }

            delay(500) // Small delay before showing response

            val botMsg = ChatMessage(
                text = reply ?: "⚠️ Sorry, I couldn't process that. Please try again or check your internet connection.",
                isUser = false
            )

            uiState = uiState.copy(
                messages = uiState.messages + botMsg,
                isLoading = false
            )
        }
    }

    private fun askGemini(conversation: List<ChatMessage>): String? {
        return try {
            val systemPrompt = """
                You are KrishiMitri, a friendly and knowledgeable AI assistant for Indian farmers.
                The user's name is $userName. Use their name occasionally in conversation.
                Provide helpful advice on farming, crops, weather, pests, fertilizers, and government schemes.
                Keep responses concise (2-3 sentences), friendly, and practical. Use emojis occasionally.
                Always respond in a warm, supportive tone.
            """.trimIndent()

            val arr = JSONArray()

            arr.put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                }
            )

            conversation.takeLast(8).filter { it.isUser || !it.text.contains("👋") }
                .forEach { msg ->
                    arr.put(
                        JSONObject().apply {
                            put("role", if (msg.isUser) "user" else "model")
                            put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                        }
                    )
                }

            val root = JSONObject().apply {
                put("contents", arr)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 256)
                })
            }

            val body = root.toString().toRequestBody("application/json".toMediaType())
            val req = Request.Builder()
                .url(GeminiConfig.GEMINI_ENDPOINT)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(req).execute().use { res ->
                val data = res.body?.string() ?: return "Network error. Please check your connection."

                if (!res.isSuccessful) {
                    return when (res.code) {
                        400 -> "Invalid request. Please try rephrasing your question."
                        401 -> "API authentication failed. Please contact support."
                        429 -> "Too many requests. Please wait a moment and try again."
                        500, 503 -> "Service temporarily unavailable. Please try again later."
                        else -> "Connection error (${res.code}). Please check your internet."
                    }
                }

                val obj = JSONObject(data)
                val candidates = obj.optJSONArray("candidates")

                if (candidates == null || candidates.length() == 0) {
                    return "I couldn't generate a response. Please try again."
                }

                candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            }
        } catch (e: java.net.UnknownHostException) {
            "No internet connection. Please check your network."
        } catch (e: java.net.SocketTimeoutException) {
            "Request timeout. Please check your internet connection."
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.message ?: "Something went wrong. Please try again."}"
        }
    }

    fun resetChat() {
        userName = ""
        uiState = ChatUiState()
        showWelcomeSequence()
    }
}
