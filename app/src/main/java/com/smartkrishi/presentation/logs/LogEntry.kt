package com.smartkrishi.domain.model

import java.time.LocalDateTime

data class LogEntry(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "",          // irrigation, tds, alert, moisture
    val time: String = "",
    val pumpStatus: Int = 0,
    val isAlert: Boolean = false,
    val timestamp: LocalDateTime = LocalDateTime.now()
)