package com.smartkrishi.presentation.ai

import com.smartkrishi.ml.*
import com.smartkrishi.data.firebase.FirebaseRepository
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.EditLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smartkrishi.data.ai.OpenAIService
import com.smartkrishi.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BgScreen = Color(0xFFF6FAF6)
private val BgCard = Color(0xFFFFFFFF)
private val BgSoft = Color(0xFFF7FBF7)
private val BgLiveTag = Color(0xFFBBEFBB)
private val ScoreTrack = Color(0xFFE1EDE1)

private val GreenDark = Color(0xFF234A27)
private val GreenMid = Color(0xFF48894A)
private val GreenTrack = Color(0xFFB6D9B6)
private val GreenPale = Color(0xFFE4F4E4)

private val TextPri = Color(0xFF151F15)
private val TextSec = Color(0xFF3F533F)
private val TextMut = Color(0xFF859985)
private val TextOnDark = Color(0xFFFFFFFF)

private val BorderCard = Color(0xFFDCEBDC)
private val DividerCol = Color(0xFFE8F0E8)
private val ErrBg = Color(0xFFFDECEC)
private val ErrText = Color(0xFFC62828)

private enum class ScreenMode {
    ENTRY,
    RESULT
}

data class CropFull(
    val name: String,
    val scientific: String,
    val match: Int,
    val duration: String,
    val sowingTime: String,
    val harvestTime: String,
    val yield: String,
    val soilFit: String,
    val marketDemand: String,
    val waterNeeds: Int,
    val pestRisk: Int,
    val climateSuitability: Int,
    val whySuitable: String,
    val recommendedBecause: List<String>,
    val changedParameters: List<String>,
    val notRecommendedIf: String
)

data class RecommendationInput(
    val sowingMonth: String,
    val lastCrop: String,
    val location: String,
    val state: String,
    val farmSizeText: String,
    val farmSizeAcres: Float
)

data class FarmSnapshot(
    val location: String,
    val state: String,
    val soilType: String,
    val rainfall: String,
    val avgN: Float,
    val avgP: Float,
    val avgK: Float,
    val avgPh: Float,
    val avgTemp: Float,
    val avgHumidity: Float,
    val irrigationSummary: String,
    val weatherSummary: String,
    val nodeCount: Int,
    val farmSizeAcres: Float
)

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun CropRecommendationScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        dashboardViewModel.startListening()
    }

    val nodes by dashboardViewModel.nodes.collectAsState(initial = emptyList())
    val openAI = remember { OpenAIService() }
    val scope = rememberCoroutineScope()

    var screenMode by remember { mutableStateOf(ScreenMode.ENTRY) }
    var resultText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showRefreshDialog by remember { mutableStateOf(false) }
    var lastUpdatedAt by remember { mutableLongStateOf(0L) }

    val loadingMessages = remember { mutableStateListOf<String>() }
    var visibleLoadingCount by remember { mutableIntStateOf(0) }

    val realNodes = nodes.filter {
        it.n != null || it.p != null || it.k != null || it.ph != null || it.temperature != null || it.humidity != null
    }

    val avgN = realNodes.mapNotNull { it.n }.averageOrZero()
    val avgP = realNodes.mapNotNull { it.p }.averageOrZero()
    val avgK = realNodes.mapNotNull { it.k }.averageOrZero()
    val avgPh = realNodes.mapNotNull { it.ph }.averageOrZero()
    val avgTemp = realNodes.mapNotNull { it.temperature }.averageOrZero()
    val avgHumidity = realNodes.mapNotNull { it.humidity }.averageOrZero()

    val detectedLocation = remember(nodes, dashboardViewModel) {
        resolveFarmLocation(dashboardViewModel, nodes)
    }

    val rainfallDisplay = remember(nodes) {
        resolveRainfall(nodes)
    }

    val soilTypeDisplay = remember(nodes, dashboardViewModel) {
        resolveSoilType(dashboardViewModel, nodes)
    }

    val irrigationSummary = remember(nodes) {
        resolveIrrigationSummary(nodes)
    }

    val weatherSummary = remember(avgTemp, avgHumidity, rainfallDisplay) {
        buildWeatherSummary(avgTemp, avgHumidity, rainfallDisplay)
    }

    var sowingMonth by remember { mutableStateOf(currentMonthName()) }
    var lastCrop by remember { mutableStateOf("") }
    var userLocationInput by remember { mutableStateOf("") }
    var farmSizeText by remember { mutableStateOf("2.5") }
    var mlInfo by remember { mutableStateOf("") }

    LaunchedEffect(detectedLocation) {
        if (userLocationInput.isBlank() && detectedLocation.isNotBlank()) {
            userLocationInput = detectedLocation
        }
    }

    val finalLocation = userLocationInput.trim().ifBlank { detectedLocation.trim() }
    val detectedState = remember(finalLocation) { inferIndianState(finalLocation) }
    val farmSizeAcres = farmSizeText.trim().toFloatOrNull() ?: 0f

    val farmSnapshot = remember(
        finalLocation,
        detectedState,
        soilTypeDisplay,
        rainfallDisplay,
        avgN,
        avgP,
        avgK,
        avgPh,
        avgTemp,
        avgHumidity,
        irrigationSummary,
        weatherSummary,
        realNodes.size,
        farmSizeAcres
    ) {
        FarmSnapshot(
            location = finalLocation,
            state = detectedState,
            soilType = soilTypeDisplay.ifBlank { "Loamy" },
            rainfall = rainfallDisplay,
            avgN = avgN,
            avgP = avgP,
            avgK = avgK,
            avgPh = avgPh,
            avgTemp = avgTemp,
            avgHumidity = avgHumidity,
            irrigationSummary = irrigationSummary,
            weatherSummary = weatherSummary,
            nodeCount = realNodes.size,
            farmSizeAcres = farmSizeAcres
        )
    }

    val context = LocalContext.current
    val modelHelper = remember {
        try {
            TFLiteCropModelHelper(context)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val crops: List<CropFull> = remember(resultText) {
        if (resultText.isNotBlank()) parseCropsFull(resultText) else emptyList()
    }

    suspend fun runBufferedLoading(input: RecommendationInput, snapshot: FarmSnapshot) {
        loadingMessages.clear()
        visibleLoadingCount = 0

        val staged = listOf(
            "Initializing agronomic recommendation engine",
            "Validating selected farm location: ${snapshot.location.ifBlank { "Pending selection" }}",
            "Identifying regional crop zone for ${snapshot.state.ifBlank { "Unknown state" }}",
            "Reading soil sensor network from ${snapshot.nodeCount} active field node(s)",
            "Aggregating nitrogen, phosphorus, potassium and pH values",
            "Reviewing field moisture, irrigation pattern and atmospheric trend",
            "Standardizing farm size input: ${formatAcres(snapshot.farmSizeAcres)} acres",
            "Estimating land-capacity suitability for small and medium plot planning",
            "Checking seasonal alignment for sowing month: ${input.sowingMonth}",
            "Reviewing crop rotation impact from previous crop: ${input.lastCrop.ifBlank { "Not specified" }}",
            "Evaluating weather and rainfall profile for ${snapshot.location.ifBlank { "selected farm" }}",
            "Computing state-specific crop suitability scores",
            "Adjusting recommendations for land area, field practicality and expected output",
            "Ranking top crop options for final recommendation report"
        )

        staged.forEachIndexed { index, line ->
            loadingMessages.add(line)
            delay(if (index < 5) 220 else 320)
            visibleLoadingCount = loadingMessages.size
        }
    }

    fun resetToEntryForRecalculate() {
        sowingMonth = currentMonthName()
        lastCrop = "Wheat"
        userLocationInput = detectedLocation
        farmSizeText = "2.5"
        screenMode = ScreenMode.ENTRY
        resultText = ""
        error = null
        mlInfo = ""
    }

    suspend fun saveRecommendationToFirebase(
        snapshot: FarmSnapshot,
        input: RecommendationInput,
        mlCrop: String,
        confidence: Float,
        finalJson: String
    ) {
        try {
            val repo = FirebaseRepository()
            repo.saveTrainingData(snapshot, mlCrop, confidence)
        } catch (e: Exception) {
            println("Firebase saveTrainingData Error: ${e.message}")
        }

        try {
            val ref = FirebaseDatabase.getInstance()
                .getReference("crop_recommendation_history")
                .push()

            val payload = hashMapOf<String, Any?>(
                "timestamp" to System.currentTimeMillis(),
                "location" to snapshot.location,
                "state" to snapshot.state,
                "farmSizeAcres" to snapshot.farmSizeAcres,
                "sowingMonth" to input.sowingMonth,
                "lastCrop" to input.lastCrop,
                "soilType" to snapshot.soilType,
                "rainfall" to snapshot.rainfall,
                "avgN" to snapshot.avgN,
                "avgP" to snapshot.avgP,
                "avgK" to snapshot.avgK,
                "avgPh" to snapshot.avgPh,
                "avgTemp" to snapshot.avgTemp,
                "avgHumidity" to snapshot.avgHumidity,
                "irrigationSummary" to snapshot.irrigationSummary,
                "weatherSummary" to snapshot.weatherSummary,
                "nodeCount" to snapshot.nodeCount,
                "mlPrediction" to mlCrop,
                "mlConfidence" to confidence,
                "finalRecommendationJson" to finalJson
            )

            ref.setValue(payload)
        } catch (e: Exception) {
            println("Firebase recommendation history Error: ${e.message}")
        }
    }

    fun launchRecommendation(reason: String = "manual") {
        if (realNodes.isEmpty()) {
            error = "No live farm node data available yet."
            return
        }

        if (sowingMonth.isBlank()) {
            error = "Please enter sowing month."
            return
        }

        if (finalLocation.isBlank()) {
            error = "Please enter location manually or use dashboard location."
            return
        }

        if (farmSizeAcres <= 0f) {
            error = "Please enter a valid farm size in acres."
            return
        }

        val input = RecommendationInput(
            sowingMonth = sowingMonth.trim(),
            lastCrop = lastCrop.trim(),
            location = finalLocation,
            state = detectedState,
            farmSizeText = farmSizeText.trim(),
            farmSizeAcres = farmSizeAcres
        )

        loading = true
        error = null
        resultText = ""

        scope.launch {
            try {
                runBufferedLoading(input, farmSnapshot)

                val (mlCrop, confidence, probs) = try {
                    if (modelHelper != null) {
                        runHybridModel(modelHelper, farmSnapshot)
                    } else {
                        Triple("Wheat", 0.5f, safeFallbackProbabilities())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Triple("Wheat", 0.5f, safeFallbackProbabilities())
                }

                val prompt = """
You are an expert agronomist for Indian agriculture.

ML Prediction:
Crop: $mlCrop
Confidence: ${(confidence * 100).toInt()}%

Farm Data:
Location: ${farmSnapshot.location}
State: ${farmSnapshot.state}
Farm Size: ${formatAcres(farmSnapshot.farmSizeAcres)} acres

Soil:
N=${farmSnapshot.avgN}, P=${farmSnapshot.avgP}, K=${farmSnapshot.avgK}
pH=${farmSnapshot.avgPh}
SoilType=${farmSnapshot.soilType}

Weather:
Temp=${farmSnapshot.avgTemp}
Humidity=${farmSnapshot.avgHumidity}
Rainfall=${farmSnapshot.rainfall}
Irrigation=${farmSnapshot.irrigationSummary}

Task:
1. Verify ML prediction
2. Improve it if needed
3. Suggest best 3 crops for this Indian farm
4. Return valid JSON array only
5. No markdown, no explanation outside JSON

Use practical Indian farming relevance.
""".trimIndent()

                println("CROP_REC_REASON => $reason")
                println("CROP_REC_PROMPT => $prompt")
                println("ML_PROBS_SIZE => ${probs.size}")

                val response = openAI.getRecommendation(prompt)
                println("CROP_REC_RESPONSE => $response")

                if (response.isNullOrBlank()) {
                    error = "Recommendation service returned empty response."
                    return@launch
                }

                val parsed = parseCropsFull(response)

                val finalParsed = if (parsed.isNotEmpty()) {
                    when {
                        confidence > 0.9f -> boostMlCrop(parsed, mlCrop)
                        confidence > 0.75f -> mergeMlAndLlm(parsed, mlCrop)
                        else -> parsed
                    }.take(3)
                } else {
                    fallbackCropsByState(
                        state = farmSnapshot.state,
                        location = farmSnapshot.location,
                        soilType = farmSnapshot.soilType
                    )
                }

                if (finalParsed.isEmpty()) {
                    error = "Response came, but no valid crop data was generated."
                } else {
                    val finalJson = cropsToJson(finalParsed.take(3))
                    mlInfo = "ML Prediction: $mlCrop (${(confidence * 100).toInt()}%)"
                    resultText = finalJson
                    lastUpdatedAt = System.currentTimeMillis()
                    screenMode = ScreenMode.RESULT

                    saveRecommendationToFirebase(
                        snapshot = farmSnapshot,
                        input = input,
                        mlCrop = mlCrop,
                        confidence = confidence,
                        finalJson = finalJson
                    )
                }
            } catch (e: Exception) {
                println("CROP_REC_ERROR => ${e.stackTraceToString()}")
                error = e.message ?: "Something went wrong."
            } finally {
                loading = false
            }
        }
    }

    if (showRefreshDialog) {
        AlertDialog(
            onDismissRequest = { showRefreshDialog = false },
            containerColor = BgCard,
            shape = RoundedCornerShape(22.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(GreenPale, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = GreenDark)
                }
            },
            title = {
                Text(
                    "Recalculate Recommendation?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPri,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "The entry form will open again with your current values. You can edit location manually and generate a fresh result.",
                    fontSize = 14.sp,
                    color = TextSec,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRefreshDialog = false
                        resetToEntryForRecalculate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenDark),
                    shape = RoundedCornerShape(40.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Entry Again", color = TextOnDark, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showRefreshDialog = false },
                    shape = RoundedCornerShape(40.dp),
                    border = BorderStroke(1.dp, BorderCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = TextMut, fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    Scaffold(
        containerColor = BgScreen,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPri)
                    }
                },
                title = {
                    Column {
                        Text(
                            "Crop Recommendation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPri
                        )
                        Text(
                            finalLocation.ifBlank { "Location not selected" },
                            fontSize = 11.sp,
                            color = GreenMid,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    AnimatedVisibility(visible = crops.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .background(BgLiveTag, RoundedCornerShape(20.dp))
                                .border(1.dp, GreenTrack, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "RESULT",
                                fontSize = 11.sp,
                                color = GreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (lastUpdatedAt > 0L && !loading && screenMode == ScreenMode.RESULT) {
                Text(
                    "Last updated: ${formatTime(lastUpdatedAt)}",
                    fontSize = 11.sp,
                    color = TextMut,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            AnimatedVisibility(
                visible = screenMode == ScreenMode.ENTRY && !loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    EntryInputSection(
                        sowingMonth = sowingMonth,
                        onSowingMonthChange = { sowingMonth = it },
                        lastCrop = lastCrop,
                        onLastCropChange = { lastCrop = it },
                        locationInput = userLocationInput,
                        onLocationInputChange = { userLocationInput = it },
                        detectedLocation = detectedLocation,
                        state = detectedState,
                        farmSizeText = farmSizeText,
                        onFarmSizeChange = { farmSizeText = it }
                    )

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { launchRecommendation("manual_first") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenDark),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            "Generate Crop Recommendation",
                            color = TextOnDark,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            AnimatedVisibility(visible = loading, enter = fadeIn(), exit = fadeOut()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BufferedLoadingCard(
                        location = finalLocation.ifBlank { "selected farm" },
                        visibleLines = loadingMessages.take(visibleLoadingCount)
                    )
                    Spacer(Modifier.height(18.dp))
                }
            }

            AnimatedVisibility(visible = error != null && !loading, enter = fadeIn(), exit = fadeOut()) {
                error?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ErrBg)
                    ) {
                        Text(
                            it,
                            color = ErrText,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = crops.isNotEmpty() && !loading && screenMode == ScreenMode.RESULT,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 }),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ResultHeaderCard(
                        location = farmSnapshot.location,
                        state = farmSnapshot.state,
                        sowingMonth = sowingMonth,
                        lastCrop = lastCrop,
                        farmSizeAcres = farmSnapshot.farmSizeAcres
                    )

                    Spacer(Modifier.height(14.dp))

                    if (mlInfo.isNotBlank()) {
                        Text(
                            mlInfo,
                            fontSize = 13.sp,
                            color = GreenMid,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        "Final Crop Result",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPri
                    )

                    Spacer(Modifier.height(10.dp))

                    crops.take(3).forEachIndexed { index, crop ->
                        FinalCropCardProfessional(crop = crop, rank = index + 1)
                        Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { showRefreshDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenDark),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            "Recalculate Recommendation",
                            color = TextOnDark,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun EntryInputSection(
    sowingMonth: String,
    onSowingMonthChange: (String) -> Unit,
    lastCrop: String,
    onLastCropChange: (String) -> Unit,
    locationInput: String,
    onLocationInputChange: (String) -> Unit,
    detectedLocation: String,
    state: String,
    farmSizeText: String,
    onFarmSizeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Input Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPri
            )

            Spacer(Modifier.height(12.dp))

            LocationInputCard(
                locationInput = locationInput,
                onLocationInputChange = onLocationInputChange,
                detectedLocation = detectedLocation,
                state = state
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = farmSizeText,
                onValueChange = { value ->
                    onFarmSizeChange(value.filter { it.isDigit() || it == '.' })
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Farm Size (Acres)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = sowingMonth,
                onValueChange = onSowingMonthChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Month of Sowing") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = lastCrop,
                onValueChange = onLastCropChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Last Crop Grown") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
        }
    }
}

@Composable
private fun LocationInputCard(
    locationInput: String,
    onLocationInputChange: (String) -> Unit,
    detectedLocation: String,
    state: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSoft),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(GreenPale, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.EditLocation, contentDescription = null, tint = GreenDark)
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        "Enter Location Manually",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPri
                    )
                    Text(
                        if (detectedLocation.isBlank()) {
                            "Dashboard location not available"
                        } else {
                            "Dashboard detected: $detectedLocation"
                        },
                        fontSize = 11.sp,
                        color = TextMut
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = locationInput,
                onValueChange = onLocationInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Farm Location") },
                placeholder = { Text("Enter village, city, district, state") },
                singleLine = false,
                minLines = 2,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            if (state.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Detected state: $state",
                    fontSize = 11.sp,
                    color = GreenMid,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun BufferedLoadingCard(
    location: String,
    visibleLines: List<String>
) {
    val inf = rememberInfiniteTransition(label = "spin")
    val sweep by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "sw"
    )
    val pulse by inf.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(54.dp)) {
                        drawArc(
                            GreenTrack,
                            0f,
                            360f,
                            false,
                            style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            GreenDark.copy(alpha = pulse),
                            sweep,
                            110f,
                            false,
                            style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Professional Calculation in Progress",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPri
                    )
                    Text(
                        "Analyzing farm intelligence for $location",
                        fontSize = 12.sp,
                        color = TextMut
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = DividerCol)
            Spacer(Modifier.height(14.dp))

            if (visibleLines.isEmpty()) {
                Text("Preparing recommendation workflow", fontSize = 13.sp, color = TextSec)
            } else {
                visibleLines.forEachIndexed { index, line ->
                    Text(
                        text = "${index + 1}. $line",
                        fontSize = 13.sp,
                        color = TextSec,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultHeaderCard(
    location: String,
    state: String,
    sowingMonth: String,
    lastCrop: String,
    farmSizeAcres: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Recommendation Context",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPri
            )
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryTile("Location", location, Modifier.weight(1f))
                SummaryTile("State", state, Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryTile("Sowing Month", sowingMonth, Modifier.weight(1f))
                SummaryTile("Last Crop", if (lastCrop.isBlank()) "Not Provided" else lastCrop, Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryTile("Farm Size", "${formatAcres(farmSizeAcres)} Acres", Modifier.weight(1f))
                SummaryTile("Analysis Type", "Professional Agronomic Match", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryTile(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(BgSoft, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(title, fontSize = 10.sp, color = TextMut, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value.ifBlank { "-" }, fontSize = 13.sp, color = TextPri, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FinalCropCardProfessional(crop: CropFull, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, BorderCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Recommendation $rank",
                        fontSize = 10.sp,
                        color = TextMut,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        crop.name,
                        fontSize = 20.sp,
                        color = TextPri,
                        fontWeight = FontWeight.Bold
                    )
                    if (crop.scientific.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            crop.scientific,
                            fontSize = 11.sp,
                            color = TextMut
                        )
                    }
                }

                ScoreBadge(match = crop.match)
            }

            Spacer(Modifier.height(14.dp))
            ProfessionalScoreBar(score = crop.match)
            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryTile("Sowing", crop.sowingTime, Modifier.weight(1f))
                SummaryTile("Harvest", crop.harvestTime, Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryTile("Duration", crop.duration, Modifier.weight(1f))
                SummaryTile("Expected Yield", crop.yield, Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryTile("Soil", crop.soilFit, Modifier.weight(1f))
                SummaryTile("Demand", crop.marketDemand, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ScoreBadge(match: Int) {
    Box(
        modifier = Modifier
            .background(GreenPale, RoundedCornerShape(14.dp))
            .border(1.dp, GreenTrack, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$match%",
                fontSize = 18.sp,
                color = GreenDark,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Score",
                fontSize = 10.sp,
                color = GreenMid,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfessionalScoreBar(score: Int) {
    var progressTarget by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "progressBar"
    )

    LaunchedEffect(score) {
        delay(100)
        progressTarget = score.coerceIn(0, 100) / 100f
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Suitability", fontSize = 11.sp, color = TextMut, fontWeight = FontWeight.SemiBold)
            Text("$score%", fontSize = 11.sp, color = GreenDark, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(ScoreTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(GreenDark)
            )
        }
    }
}

private fun resolveFarmLocation(dashboardViewModel: DashboardViewModel, nodes: List<Any>): String {
    return runCatching {
        val vm = dashboardViewModel
        val clazz = vm.javaClass

        val farmGetter = clazz.methods.firstOrNull {
            it.name in listOf("getSelectedFarm", "getFarm", "getCurrentFarm")
        }

        if (farmGetter != null) {
            val flow = farmGetter.invoke(vm)
            val valueMethod = flow?.javaClass?.methods?.firstOrNull { it.name == "getValue" }
            val farmObj = valueMethod?.invoke(flow)
            if (farmObj != null) {
                val locMethod = farmObj.javaClass.methods.firstOrNull {
                    it.name in listOf("getLocation", "location", "getAddress", "getVillage", "getFarmLocation")
                }
                val loc = locMethod?.invoke(farmObj)?.toString()?.trim()
                if (!loc.isNullOrBlank()) return@runCatching loc
            }
        }

        nodes.firstOrNull()?.let { node ->
            val locMethod = node.javaClass.methods.firstOrNull {
                it.name in listOf("getLocation", "getCity", "getRegion", "getAddress", "getVillage")
            }
            locMethod?.invoke(node)?.toString()?.trim()?.ifBlank { null }
        }
    }.getOrNull().orEmpty()
}

private fun resolveRainfall(nodes: List<Any>): String {
    return nodes.firstOrNull()?.let { node ->
        runCatching {
            val m = node.javaClass.methods.firstOrNull {
                it.name in listOf("getRainfall", "getRain", "getPrecipitation")
            }
            val v = m?.invoke(node)
            if (v != null) "${formatNodeVal(v)} mm" else null
        }.getOrNull()
    } ?: "-"
}

private fun resolveSoilType(dashboardViewModel: DashboardViewModel, nodes: List<Any>): String {
    return runCatching {
        val vm = dashboardViewModel
        val flowField = vm.javaClass.methods.firstOrNull {
            it.name in listOf("getSelectedFarm", "getFarm", "getCurrentFarm")
        }
        val flow = flowField?.invoke(vm)
        val farmObj = flow?.javaClass?.getMethod("getValue")?.invoke(flow)
        if (farmObj != null) {
            val m = farmObj.javaClass.methods.firstOrNull {
                it.name in listOf("getSoilType", "getSoil")
            }
            m?.invoke(farmObj)?.toString()?.trim()?.ifBlank { null }
        } else null
    }.getOrNull()
        ?: nodes.firstOrNull()?.let { node ->
            runCatching {
                val m = node.javaClass.methods.firstOrNull {
                    it.name in listOf("getSoilType", "getSoil")
                }
                m?.invoke(node)?.toString()?.trim()?.ifBlank { null }
            }.getOrNull()
        }
        ?: "Loamy"
}

private fun resolveIrrigationSummary(nodes: List<Any>): String {
    val signal = nodes.firstOrNull()?.let { node ->
        runCatching {
            val m = node.javaClass.methods.firstOrNull {
                it.name in listOf("getRain", "getRainStatus", "getIrrigationStatus", "getPumpStatus")
            }
            m?.invoke(node)?.toString()
        }.getOrNull()
    }

    return when {
        signal.isNullOrBlank() -> "Based on live field records"
        else -> signal
    }
}

private fun buildWeatherSummary(temp: Float, humidity: Float, rainfall: String): String {
    val t = if (temp > 0f) "${temp.toInt()} C" else "-"
    val h = if (humidity > 0f) "${humidity.toInt()}%" else "-"
    return "Temp $t, Humidity $h, Rain $rainfall"
}

private fun inferIndianState(location: String): String {
    if (location.isBlank()) return ""
    val value = location.lowercase(Locale.getDefault())
    return when {
        "punjab" in value || "ludhiana" in value || "amritsar" in value -> "Punjab"
        "haryana" in value || "karnal" in value || "hisar" in value -> "Haryana"
        "uttar pradesh" in value || "ghaziabad" in value || "lucknow" in value || "kanpur" in value -> "Uttar Pradesh"
        "bihar" in value || "patna" in value -> "Bihar"
        "rajasthan" in value || "jaipur" in value -> "Rajasthan"
        "madhya pradesh" in value || "bhopal" in value || "indore" in value -> "Madhya Pradesh"
        "maharashtra" in value || "pune" in value || "nagpur" in value -> "Maharashtra"
        "gujarat" in value || "ahmedabad" in value || "rajkot" in value -> "Gujarat"
        "west bengal" in value || "kolkata" in value -> "West Bengal"
        "odisha" in value || "bhubaneswar" in value -> "Odisha"
        "chhattisgarh" in value || "raipur" in value -> "Chhattisgarh"
        "telangana" in value || "hyderabad" in value -> "Telangana"
        "andhra pradesh" in value || "vijayawada" in value -> "Andhra Pradesh"
        "karnataka" in value || "bengaluru" in value || "mysuru" in value -> "Karnataka"
        "tamil nadu" in value || "coimbatore" in value || "chennai" in value -> "Tamil Nadu"
        "kerala" in value || "kochi" in value || "thrissur" in value -> "Kerala"
        "assam" in value || "guwahati" in value -> "Assam"
        else -> ""
    }
}

private fun parseCropsFull(text: String): List<CropFull> {
    val cleaned = sanitizeAiResponse(text)
    if (cleaned.isBlank()) return emptyList()

    parseAsArray(cleaned)?.let { if (it.isNotEmpty()) return it }

    extractLikelyJsonArray(cleaned)?.let { candidate ->
        parseAsArray(candidate)?.let { if (it.isNotEmpty()) return it }
    }

    val objects = extractJsonObjects(cleaned)
    if (objects.isNotEmpty()) {
        val crops = objects.mapNotNull { parseCropObject(it) }
            .distinctBy { it.name.lowercase(Locale.getDefault()).trim() }
        if (crops.isNotEmpty()) return crops.take(3)
    }

    return emptyList()
}

private fun sanitizeAiResponse(text: String): String {
    return text
        .replace("```json", "", ignoreCase = true)
        .replace("```JSON", "", ignoreCase = true)
        .replace("```", "")
        .replace("\uFEFF", "")
        .trim()
}

private fun extractLikelyJsonArray(text: String): String? {
    val start = text.indexOf('[')
    if (start < 0) return null

    var depth = 0
    var inString = false
    var escaped = false

    for (i in start until text.length) {
        val c = text[i]

        if (escaped) {
            escaped = false
            continue
        }

        if (c == '\\') {
            escaped = true
            continue
        }

        if (c == '"') {
            inString = !inString
            continue
        }

        if (!inString) {
            if (c == '[') depth++
            if (c == ']') {
                depth--
                if (depth == 0) {
                    return text.substring(start, i + 1)
                }
            }
        }
    }
    return null
}

private fun extractJsonObjects(text: String): List<String> {
    val result = mutableListOf<String>()
    var start = -1
    var depth = 0
    var inString = false
    var escaped = false

    text.forEachIndexed { index, c ->
        if (escaped) {
            escaped = false
            return@forEachIndexed
        }

        if (c == '\\') {
            escaped = true
            return@forEachIndexed
        }

        if (c == '"') {
            inString = !inString
            return@forEachIndexed
        }

        if (!inString) {
            if (c == '{') {
                if (depth == 0) start = index
                depth++
            } else if (c == '}') {
                depth--
                if (depth == 0 && start >= 0) {
                    result += text.substring(start, index + 1)
                    start = -1
                }
            }
        }
    }

    return result
}

private fun parseAsArray(raw: String): List<CropFull>? {
    return runCatching {
        val arr = JSONArray(raw)
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                parseCropObject(obj.toString())?.let { add(it) }
            }
        }.distinctBy { it.name.lowercase(Locale.getDefault()).trim() }
    }.getOrNull()
}

private fun parseCropObject(raw: String): CropFull? {
    return runCatching {
        val obj = JSONObject(raw)
        val name = obj.strField("name", "crop", "cropName", "recommendedCrop")
        if (name.isBlank()) return@runCatching null

        CropFull(
            name = name,
            scientific = obj.strField("scientific", "scientificName"),
            match = obj.intField("match", "score", "suitability", default = 80).coerceIn(1, 100),
            duration = obj.strField("duration", "cropDuration"),
            sowingTime = obj.strField("sowingTime", "sowing", "bestSowingTime"),
            harvestTime = obj.strField("harvestTime", "harvest", "harvestingTime"),
            yield = obj.strField("yield", "expectedYield"),
            soilFit = obj.strField("soilFit", "soilTypeFit", "soilSuitability"),
            marketDemand = obj.strField("marketDemand", "demand", "market"),
            waterNeeds = obj.intField("waterNeeds", "waterNeed", default = 50).coerceIn(0, 100),
            pestRisk = obj.intField("pestRisk", "risk", default = 30).coerceIn(0, 100),
            climateSuitability = obj.intField("climateSuitability", "climateScore", default = 80).coerceIn(0, 100),
            whySuitable = obj.strField("whySuitable", "reason", "why"),
            recommendedBecause = obj.arrayField("recommendedBecause", "reasons", "recommendedReasons"),
            changedParameters = obj.arrayField("changedParameters", "parameterChanges", "changes"),
            notRecommendedIf = obj.strField("notRecommendedIf", "avoidIf", "warning")
        )
    }.getOrNull()
}

private fun JSONObject.strField(vararg keys: String): String {
    for (key in keys) {
        if (has(key) && !isNull(key)) {
            val value = optString(key).trim()
            if (value.isNotBlank() && value.lowercase(Locale.getDefault()) != "null") return value
        }
    }
    return ""
}

private fun JSONObject.intField(vararg keys: String, default: Int): Int {
    for (key in keys) {
        if (!has(key) || isNull(key)) continue
        val any = opt(key)
        when (any) {
            is Number -> return any.toInt()
            is String -> any.trim().toIntOrNull()?.let { return it }
        }
    }
    return default
}

private fun JSONObject.arrayField(vararg keys: String): List<String> {
    for (key in keys) {
        if (!has(key) || isNull(key)) continue
        val arr = optJSONArray(key)
        if (arr != null) return arr.toStringList()
        val stringValue = optString(key).trim()
        if (stringValue.isNotBlank()) {
            return stringValue
                .split("\n", ";", "•")
                .map { it.trim().trimStart('-', '•').trim() }
                .filter { it.isNotBlank() }
        }
    }
    return emptyList()
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    val out = mutableListOf<String>()
    for (i in 0 until length()) {
        val item = optString(i).trim()
        if (item.isNotBlank() && item.lowercase(Locale.getDefault()) != "null") out += item
    }
    return out
}

private fun fallbackCropsByState(
    state: String,
    location: String,
    soilType: String
): List<CropFull> {
    val s = state.lowercase(Locale.getDefault())
    return when {
        "punjab" in s || "haryana" in s -> listOf(
            CropFull("Wheat", "Triticum aestivum", 93, "110-120 Days", "Nov", "Mar-Apr", "4.0-4.8 t/ha", "$soilType Soil", "High Demand", 45, 28, 90, "", emptyList(), emptyList(), ""),
            CropFull("Mustard", "Brassica juncea", 86, "95-110 Days", "Oct-Nov", "Feb-Mar", "1.4-1.8 t/ha", "$soilType Soil", "Good Demand", 32, 35, 84, "", emptyList(), emptyList(), ""),
            CropFull("Gram", "Cicer arietinum", 79, "100-115 Days", "Oct-Nov", "Feb-Mar", "1.8-2.4 t/ha", "$soilType Soil", "Stable Demand", 30, 33, 78, "", emptyList(), emptyList(), "")
        )
        "uttar pradesh" in s || "bihar" in s -> listOf(
            CropFull("Wheat", "Triticum aestivum", 91, "110-125 Days", "Nov", "Mar-Apr", "3.8-4.6 t/ha", "$soilType Soil", "High Demand", 46, 30, 88, "", emptyList(), emptyList(), ""),
            CropFull("Potato", "Solanum tuberosum", 84, "90-110 Days", "Oct-Nov", "Jan-Feb", "22-28 t/ha", "$soilType Soil", "High Demand", 54, 38, 81, "", emptyList(), emptyList(), ""),
            CropFull("Gram", "Cicer arietinum", 78, "100-115 Days", "Oct-Nov", "Feb-Mar", "1.7-2.2 t/ha", "$soilType Soil", "Stable Demand", 30, 34, 77, "", emptyList(), emptyList(), "")
        )
        "maharashtra" in s || "madhya pradesh" in s -> listOf(
            CropFull("Soybean", "Glycine max", 90, "90-110 Days", "Jun-Jul", "Sep-Oct", "1.8-2.6 t/ha", "$soilType Soil", "High Demand", 44, 36, 88, "", emptyList(), emptyList(), ""),
            CropFull("Gram", "Cicer arietinum", 85, "100-120 Days", "Oct-Nov", "Feb-Mar", "1.8-2.3 t/ha", "$soilType Soil", "Stable Demand", 31, 33, 82, "", emptyList(), emptyList(), ""),
            CropFull("Wheat", "Triticum aestivum", 77, "110-125 Days", "Nov", "Mar-Apr", "3.4-4.2 t/ha", "$soilType Soil", "High Demand", 49, 31, 75, "", emptyList(), emptyList(), "")
        )
        "gujarat" in s || "rajasthan" in s -> listOf(
            CropFull("Mustard", "Brassica juncea", 89, "95-110 Days", "Oct-Nov", "Feb-Mar", "1.3-1.8 t/ha", "$soilType Soil", "Good Demand", 30, 34, 86, "", emptyList(), emptyList(), ""),
            CropFull("Cumin", "Cuminum cyminum", 83, "100-110 Days", "Nov", "Feb-Mar", "0.7-1.0 t/ha", "$soilType Soil", "Premium Demand", 26, 41, 80, "", emptyList(), emptyList(), ""),
            CropFull("Gram", "Cicer arietinum", 78, "100-115 Days", "Oct-Nov", "Feb-Mar", "1.5-2.0 t/ha", "$soilType Soil", "Stable Demand", 29, 35, 76, "", emptyList(), emptyList(), "")
        )
        "karnataka" in s || "telangana" in s || "andhra pradesh" in s -> listOf(
            CropFull("Maize", "Zea mays", 88, "95-110 Days", "Jun-Jul", "Sep-Oct", "4.5-6.2 t/ha", "$soilType Soil", "High Demand", 50, 37, 86, "", emptyList(), emptyList(), ""),
            CropFull("Groundnut", "Arachis hypogaea", 84, "105-120 Days", "Jun-Jul", "Oct-Nov", "1.8-2.6 t/ha", "$soilType Soil", "Good Demand", 40, 36, 82, "", emptyList(), emptyList(), ""),
            CropFull("Red Gram", "Cajanus cajan", 79, "150-180 Days", "Jun-Jul", "Dec-Jan", "1.3-1.8 t/ha", "$soilType Soil", "Stable Demand", 34, 33, 77, "", emptyList(), emptyList(), "")
        )
        "tamil nadu" in s || "kerala" in s -> listOf(
            CropFull("Rice", "Oryza sativa", 90, "110-135 Days", "Jun-Jul", "Oct-Nov", "4.0-5.5 t/ha", "$soilType Soil", "High Demand", 62, 39, 88, "", emptyList(), emptyList(), ""),
            CropFull("Groundnut", "Arachis hypogaea", 81, "105-120 Days", "Jun-Jul", "Oct-Nov", "1.7-2.4 t/ha", "$soilType Soil", "Good Demand", 40, 35, 79, "", emptyList(), emptyList(), ""),
            CropFull("Black Gram", "Vigna mungo", 76, "75-90 Days", "Jul-Aug", "Oct-Nov", "0.8-1.2 t/ha", "$soilType Soil", "Stable Demand", 28, 31, 75, "", emptyList(), emptyList(), "")
        )
        else -> listOf(
            CropFull("Wheat", "Triticum aestivum", 91, "110-120 Days", "Nov", "Mar-Apr", "4.0-4.8 t/ha", "$soilType Soil", "High Demand", 45, 28, 89, "", emptyList(), emptyList(), ""),
            CropFull("Mustard", "Brassica juncea", 83, "95-110 Days", "Oct-Nov", "Feb-Mar", "1.4-1.8 t/ha", "$soilType Soil", "Good Demand", 32, 35, 80, "", emptyList(), emptyList(), ""),
            CropFull("Gram", "Cicer arietinum", 74, "100-115 Days", "Oct-Nov", "Feb-Mar", "1.8-2.4 t/ha", "$soilType Soil", "Stable Demand", 30, 33, 76, "", emptyList(), emptyList(), "")
        )
    }.distinctBy { it.name.lowercase(Locale.getDefault()) }.take(3)
}

private fun cropsToJson(crops: List<CropFull>): String {
    val arr = JSONArray()
    crops.forEach { crop ->
        val obj = JSONObject().apply {
            put("name", crop.name)
            put("scientific", crop.scientific)
            put("match", crop.match)
            put("duration", crop.duration)
            put("sowingTime", crop.sowingTime)
            put("harvestTime", crop.harvestTime)
            put("yield", crop.yield)
            put("soilFit", crop.soilFit)
            put("marketDemand", crop.marketDemand)
            put("waterNeeds", crop.waterNeeds)
            put("pestRisk", crop.pestRisk)
            put("climateSuitability", crop.climateSuitability)
            put("whySuitable", crop.whySuitable)
            put("recommendedBecause", JSONArray(crop.recommendedBecause))
            put("changedParameters", JSONArray(crop.changedParameters))
            put("notRecommendedIf", crop.notRecommendedIf)
        }
        arr.put(obj)
    }
    return arr.toString()
}

private fun formatNodeVal(v: Any): String = when (v) {
    is Float -> if (v == v.toLong().toFloat()) v.toLong().toString() else "%.1f".format(v)
    is Double -> if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)
    else -> v.toString()
}

private fun List<Float>.averageOrZero(): Float =
    if (isNotEmpty()) average().toFloat() else 0f

private fun currentMonthName(): String =
    SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())

private fun formatTime(time: Long): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(time))

private fun formatAcres(acres: Float): String {
    return if (acres == acres.toInt().toFloat()) acres.toInt().toString() else "%.1f".format(acres)
}

fun mergeMlAndLlm(crops: List<CropFull>, mlCrop: String): List<CropFull> {
    val exists = crops.any { it.name.equals(mlCrop, true) }
    return if (exists) {
        crops
    } else {
        val ml = crops.firstOrNull()?.copy(name = mlCrop, match = 85)
        if (ml != null) (listOf(ml) + crops).take(3) else crops.take(3)
    }
}

private fun boostMlCrop(crops: List<CropFull>, mlCrop: String): List<CropFull> {
    return crops.sortedByDescending {
        if (it.name.equals(mlCrop, true)) 100 else it.match
    }
}

private fun safeFallbackProbabilities(): FloatArray {
    val labelCount = try {
        val possibleFields = LabelEncoder::class.java.declaredFields
        val labelField = possibleFields.firstOrNull {
            it.name.equals("labels", true) || it.name.equals("classes", true)
        }
        labelField?.isAccessible = true
        val value = labelField?.get(null)
        when (value) {
            is List<*> -> FloatArray(value.size.coerceAtLeast(1)) { if (it == 0) 1f else 0f }
            is Array<*> -> FloatArray(value.size.coerceAtLeast(1)) { if (it == 0) 1f else 0f }
            else -> floatArrayOf(1f)
        }
    } catch (e: Exception) {
        floatArrayOf(1f)
    }
    return labelCount
}

private fun decodeLabelSafely(index: Int): String {
    return try {
        LabelEncoder.decode(index)
    } catch (e: Exception) {
        "Wheat"
    }
}

private fun runHybridModel(
    model: TFLiteCropModelHelper,
    snapshot: FarmSnapshot
): Triple<String, Float, FloatArray> {

    val rainfall = snapshot.rainfall
        .replace("[^0-9.]".toRegex(), "")
        .toFloatOrNull()
        ?: 0f

    val rawInput = floatArrayOf(
        snapshot.avgN.coerceAtLeast(0f),
        snapshot.avgP.coerceAtLeast(0f),
        snapshot.avgK.coerceAtLeast(0f),
        snapshot.avgTemp.coerceAtLeast(0f),
        snapshot.avgHumidity.coerceAtLeast(0f),
        snapshot.avgPh.coerceIn(0f, 14f),
        rainfall.coerceAtLeast(0f)
    )

    val scaled = try {
        Scaler.transform(rawInput)
    } catch (e: Exception) {
        throw IllegalStateException("Scaler transform failed: ${e.message}", e)
    }

    val prediction = try {
        model.predict(scaled)
    } catch (e: Exception) {
        throw IllegalStateException("TFLite predict failed: ${e.message}", e)
    }

    val index = prediction.first
    val rawProbs = prediction.second

    val safeRawProbs = if (rawProbs.isNotEmpty()) rawProbs else safeFallbackProbabilities()
    val sum = safeRawProbs.sum().takeIf { it > 0f } ?: 1f
    val probs = safeRawProbs.map { it / sum }.toFloatArray()

    val safeIndex = when {
        probs.isEmpty() -> 0
        index < 0 -> 0
        index >= probs.size -> probs.indices.maxByOrNull { probs[it] } ?: 0
        else -> index
    }

    val crop = decodeLabelSafely(safeIndex)
    val confidence = probs.getOrNull(safeIndex) ?: 0.5f

    return Triple(crop, confidence, probs)
}