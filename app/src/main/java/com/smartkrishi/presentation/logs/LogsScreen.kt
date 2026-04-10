package com.smartkrishi.presentation.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smartkrishi.domain.model.LogEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val AppWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val Green = Color(0xFF1A7430)
private val Red = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    navController: NavController,
    viewModel: LogsViewModel = viewModel()
) {

    val logs by viewModel.logs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = AppWhite,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Activity Log",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppWhite,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppWhite)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green)
            }
        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppWhite)
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                val grouped = groupLogsByDate(logs)

                grouped.forEach { (date, entries) ->

                    item {
                        Text(
                            text = date,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }

                    items(entries) { log ->
                        ActivityLogCard(log)
                    }
                }

                item { Spacer(Modifier.height(60.dp)) }
            }
        }
    }
}

@Composable
private fun ActivityLogCard(log: LogEntry) {

    val green = Color(0xFF1A7430)
    val red = Color(0xFFC62828)
    val orange = Color(0xFFF57C00)
    val white = Color.White

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = white),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = when (log.type) {
                    "irrigation" -> Icons.Default.PowerSettingsNew
                    "tds" -> Icons.Default.Science
                    "moisture" -> Icons.Default.Grass
                    "weather" -> Icons.Default.Cloud
                    else -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = when {
                    log.isAlert -> red
                    log.type == "irrigation" && log.pumpStatus == 1 -> green
                    else -> orange
                },
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = log.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = log.description,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = log.time,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            if (log.isAlert) {
                Text(
                    text = "ALERT",
                    color = red,
                    fontWeight = FontWeight.Bold
                )
            } else if (log.type == "irrigation") {
                Text(
                    text = if (log.pumpStatus == 1) "ON" else "OFF",
                    color = if (log.pumpStatus == 1) green else orange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
private fun groupLogsByDate(logs: List<LogEntry>): Map<String, List<LogEntry>> {

    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return logs.groupBy { log ->
        val date = log.timestamp.toLocalDate()
        when {
            date == today -> "TODAY"
            date == yesterday -> "YESTERDAY"
            else -> date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        }
    }
}