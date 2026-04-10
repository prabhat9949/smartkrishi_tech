package com.smartkrishi.presentation.disease

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.smartkrishi.presentation.dashboard.ChatPopup
import com.smartkrishi.presentation.theme.ThemeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseResultScreen(
    navController: NavController,
    crop: String,
    disease: String,
    confidence: Float
) {
    val isDark = ThemeState.isDarkTheme.value

    val bgGradient = Brush.verticalGradient(
        colors = if (isDark)
            listOf(Color(0xFF001219), Color(0xFF00353F), Color(0xFF004149))
        else
            listOf(Color(0xFFE8FFF6), Color(0xFFD4FAF1), Color(0xFFC8F9FF))
    )

    val cardColor = if (isDark) Color(0xFF022229) else Color.White
    val highlight = if (isDark) Color(0xFF36E1B5) else Color(0xFF007F5F)

    val imagePath = DiseaseMemory.data.scannedImage
    val ai = remember { DiseaseExpertAI.getAdvice(crop, disease) }

    var showPopup by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = highlight
                ),
                title = {
                    Text("Disease Diagnosis", fontWeight = FontWeight.Black)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = highlight)
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ==================== Scanned Image ====================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (!imagePath.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(imagePath),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No Image", color = Color.Gray)
                        }
                    }
                }

                // ==================== Crop / Disease ====================
                DiagnosisCard("Crop Detected", crop, cardColor, highlight)
                DiagnosisCard("Detected Disease", disease, cardColor, highlight)

                // ==================== Confidence ====================
                val normalized = if (confidence > 1f) confidence / 100f else confidence
                ConfidenceCard(normalized, cardColor, highlight)

                // ==================== AI Medical Advice ====================
                AdviceCard("Spread Assessment", ai.spread, cardColor, highlight)
                AdviceCard("Treatment (इलाज)", ai.treatment, cardColor, highlight)
                AdviceCard("Prevention Tips (बचाव)", ai.prevention, cardColor, highlight)

                Spacer(modifier = Modifier.height(60.dp)) // Space for button
            }

            // ==================== Ask SmartKrishi Button ====================
            Button(
                onClick = { showPopup = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = highlight),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(10.dp))
                Text("Ask Smart Krishi Assistant", color = Color.Black, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showPopup) ChatPopup(navController) { showPopup = false }
}

// ================= REUSABLE UI BLOCKS =================

@Composable
fun DiagnosisCard(title: String, value: String, cardColor: Color, highlight: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(cardColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = highlight)
            Text(value, fontWeight = FontWeight.SemiBold, color = if (ThemeState.isDarkTheme.value) Color.White else Color.Black)
        }
    }
}

@Composable
fun ConfidenceCard(conf: Float, cardColor: Color, highlight: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(cardColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Model Confidence", fontWeight = FontWeight.Bold, color = highlight)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = conf,
                color = highlight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(100.dp))
            )
            Spacer(Modifier.height(6.dp))
            Text("${(conf * 100).toInt()}%", color = if (ThemeState.isDarkTheme.value) Color.White else Color.Black, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AdviceCard(title: String, value: String, cardColor: Color, highlight: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(cardColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = highlight)
            Spacer(Modifier.height(6.dp))
            Text(value, color = if (ThemeState.isDarkTheme.value) Color.White else Color.Black)
        }
    }
}
