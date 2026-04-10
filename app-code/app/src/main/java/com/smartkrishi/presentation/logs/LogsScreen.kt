package com.smartkrishi.presentation.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartkrishi.presentation.theme.ThemeState

val demoLogs = listOf(
    "Pump turned ON",
    "Soil nitrogen low alert",
    "Rain detected",
    "Market price checked",
    "Disease detection run",
    "Fertiliser recommendation generated",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(navController: NavController) {

    val isDark = ThemeState.isDarkTheme.value
    val bgColor = if (isDark) Color(0xFF0A1C12) else Color(0xFFF4F6F5)
    val cardColor = if (isDark) Color(0xFF153525) else Color.White
    val textPrimary = if (isDark) Color(0xFFE8F5E9) else Color(0xFF0A0F0B)
    val textSecondary = if (isDark) Color(0xFFB5CBB8) else Color.Gray

    Scaffold(
        containerColor = bgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Logs & History", color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(bgColor)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {

            Text(
                "Activity & Sensor Logs",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                color = textPrimary
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(demoLogs) { log ->
                    LogItemCard(
                        message = log,
                        cardColor = cardColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun LogItemCard(
    message: String,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {

    val timestamp = remember {
        val hour = (6..22).random()
        val min = (10..59).random()
        "$hour:$min"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(message, color = textPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("⏱ $timestamp", fontSize = 11.sp, color = textSecondary)
        }
    }
}
