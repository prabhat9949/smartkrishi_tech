// File: presentation/chat/ChatModels.kt
package com.smartkrishi.presentation.chat

import java.text.SimpleDateFormat
import java.util.*

data class ChatUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatMessageUi(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val quickReplies: List<QuickReply> = emptyList()
)

data class QuickReply(
    val text: String,
    val emoji: String = ""
)

fun Long.toTimeString(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}
