package com.smartkrishi.presentation.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
private val CardLight = Color(0xFFFFFFFF)

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
                title = {
                    Text(
                        text = "Fertilizer Advisor",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {

            // ===== FARM HEADER SECTION =====

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AccentGreenSoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = farmType,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Location: $farmLocation",
                            fontSize = 15.sp,
                            color = PrimaryGreen.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== GENERATE BUTTON =====

            Button(
                onClick = {
                    isLoading = true
                    error = null
                    resultText = ""

                    scope.launch {
                        try {
                            val prompt = buildFertilizerPrompt(farmType, farmLocation)
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
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Generate Personalized Fertilizer Plan",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== ERROR DISPLAY =====

            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                error?.let {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science, // Reuse for visual consistency
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== RESULTS SECTION =====

            AnimatedVisibility(
                visible = plans.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Text(
                        text = "Recommended Fertilizer Plan",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tailored for  cultivation in $farmLocation ",
                        fontSize = 13.sp,
                        color = PrimaryGreen.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    plans.forEachIndexed { index, plan ->
                        ProfessionalFertilizerCard(plan = plan, index = index + 1)
                        if (index < plans.lastIndex) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Professional disclaimer

                }
            }
        }
    }
}

// ================= PROFESSIONAL FERTILIZER CARD =================

@Composable
private fun ProfessionalFertilizerCard(plan: FertilizerPlan, index: Int) {

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Card header with index
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PrimaryGreen.copy(alpha = 0.1f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = index.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = plan.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PrimaryGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity and Timing in structured rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quantity",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.width(100.dp)
                )
                Text(
                    text = plan.quantity,
                    fontSize = 15.sp,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timing",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.width(100.dp)
                )
                Text(
                    text = plan.timing,
                    fontSize = 15.sp,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Purpose
            Text(
                text = "Purpose",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = plan.purpose,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
        }
    }
}

// ================= PROMPT (DYNAMIC) =================

private fun buildFertilizerPrompt(cropType: String, location: String): String {
    return """
You are a professional Indian agronomist specializing in sustainable farming practices.

Provide a concise fertilizer plan for $cropType farming in $location.

Return EXACTLY in this format (provide exactly 3 fertilizers):

Fertilizer:
Quantity:
Timing:
Purpose:

Keep each explanation short and precise.
No stars, no bullet points, no extra formatting or introductory text.
""".trimIndent()
}

// ================= PARSER =================

private fun parseStructuredPlans(text: String): List<FertilizerPlan> {

    val plans = mutableListOf<FertilizerPlan>()
    val blocks = text.split("Fertilizer:").drop(1)

    for (block in blocks) {

        val name = block.substringBefore("\n").trim()

        val quantity = Regex("Quantity:(.*?)(\n|$)").find(block)?.groupValues?.get(1)?.trim() ?: ""
        val timing = Regex("Timing:(.*?)(\n|$)").find(block)?.groupValues?.get(1)?.trim() ?: ""
        val purpose = Regex("Purpose:(.*?)(\n|$)").find(block)?.groupValues?.get(1)?.trim() ?: ""

        if (name.isNotBlank()) {
            plans.add(
                FertilizerPlan(
                    name = name,
                    quantity = quantity,
                    timing = timing,
                    purpose = purpose
                )
            )
        }
    }

    return plans
}