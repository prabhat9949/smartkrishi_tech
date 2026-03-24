package com.smartkrishi.presentation.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smartkrishi.data.ai.OpenAIService
import com.smartkrishi.presentation.home.FarmViewModel
import kotlinx.coroutines.launch

// ================= THEME COLORS =================

private val PrimaryGreen = Color(0xFF2E7D32)
private val AccentGreenSoft = Color(0xFFC8E6C9)
private val LightBackground = Color(0xFFF3FBF5)
private val DarkBackground = Color(0xFF0A1C12)
private val CardLight = Color(0xFFFFFFFF)
private val CardDark = Color(0xFF153525)

// ================= DATA MODEL =================

data class FertilizerPlan(
    val name: String,
    val quantity: String,
    val timing: String,
    val purpose: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerRecommendationScreen(
    navController: NavController,
    farmViewModel: FarmViewModel = hiltViewModel()
) {

    val selectedFarm by farmViewModel.selectedFarm.collectAsState()
    val farmType = selectedFarm?.cropType ?: "Sugarcane"
    val farmLocation = selectedFarm?.location ?: "Uttar Pradesh"

    val openAI = remember { OpenAIService() }
    val scope = rememberCoroutineScope()

    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val plans = remember(resultText) {
        if (resultText.isNotBlank()) parseStructuredPlans(resultText)
        else emptyList()
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            TopAppBar(
                title = { Text("Fertilizer Advisor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // ===== HEADER SECTION =====

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AccentGreenSoft)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = farmType,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Location: $farmLocation",
                        color = PrimaryGreen.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ===== GENERATE BUTTON =====

            Button(
                onClick = {
                    isLoading = true
                    error = null
                    resultText = ""

                    scope.launch {
                        try {
                            val prompt = buildSugarcanePrompt(farmLocation)
                            resultText = openAI.getRecommendation(prompt)
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                       // Icon(Icons.Default.Science, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Fertilizer Plan", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // ===== RESULTS SECTION =====

            AnimatedVisibility(
                visible = plans.isNotEmpty(),
                enter = fadeIn()
            ) {
                Column {

                    Text(
                        text = "Recommended Plan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    plans.forEach { plan ->
                        CompactFertilizerCard(plan)
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

// ================= COMPACT CARD =================

@Composable
private fun CompactFertilizerCard(plan: FertilizerPlan) {

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = plan.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PrimaryGreen
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Quantity: ${plan.quantity}")
            Text("Timing: ${plan.timing}")

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = plan.purpose,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

// ================= PROMPT =================

private fun buildSugarcanePrompt(location: String): String {
    return """
You are a professional Indian agronomist.

Provide a concise fertilizer plan for Sugarcane farming in $location.

Return EXACTLY in this format:

Fertilizer:
Quantity:
Timing:
Purpose:

Provide 3 fertilizers only.
Keep explanation short.
No stars.
No bullet points.
No extra formatting.
""".trimIndent()
}

// ================= PARSER =================

private fun parseStructuredPlans(text: String): List<FertilizerPlan> {

    val plans = mutableListOf<FertilizerPlan>()
    val blocks = text.split("Fertilizer:").drop(1)

    for (block in blocks) {

        val name = block.substringBefore("\n").trim()
        val quantity = Regex("Quantity:(.*)")
            .find(block)?.groupValues?.get(1)?.trim() ?: ""

        val timing = Regex("Timing:(.*)")
            .find(block)?.groupValues?.get(1)?.trim() ?: ""

        val purpose = Regex("Purpose:(.*)")
            .find(block)?.groupValues?.get(1)?.trim() ?: ""

        plans.add(
            FertilizerPlan(
                name = name,
                quantity = quantity,
                timing = timing,
                purpose = purpose
            )
        )
    }

    return plans
}