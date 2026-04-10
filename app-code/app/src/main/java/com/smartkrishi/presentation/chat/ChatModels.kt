package com.smartkrishi.presentation.chat

import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val quickReplies: List<QuickReply> = emptyList(),
    val imageUrl: String? = null
)

data class QuickReply(
    val id: String,
    val text: String,
    val emoji: String = ""
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val showQuickReplies: Boolean = false
)

fun Long.toTimeString(): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))
}
