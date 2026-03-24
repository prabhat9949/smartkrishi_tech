package com.smartkrishi.presentation.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smartkrishi.data.ai.OpenAIService
import com.smartkrishi.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════
// PALETTE — clean, light-greenish, professional
// ═══════════════════════════════════════════════════════════════════
private val BgScreen    = Color(0xFFF6FAF6)
private val BgCard      = Color(0xFFFFFFFF)
private val BgMuted     = Color(0xFFF0F6F0)
private val BgChip      = Color(0xFFDEF0DE)
private val BgLiveTag   = Color(0xFFBBEFBB)
private val BgNodeHead  = Color(0xFFE6F4E6)
private val BgNodeAlt   = Color(0xFFF7FBF7)

private val GreenDark   = Color(0xFF2A5C2A)   // arc, button, strong text
private val GreenMid    = Color(0xFF48894A)   // chips, accents
private val GreenTrack  = Color(0xFFB6D9B6)   // arc background track
private val GreenPale   = Color(0xFFE4F4E4)   // cell tint

private val TextPri     = Color(0xFF151F15)
private val TextSec     = Color(0xFF3F533F)
private val TextMut     = Color(0xFF859985)
private val TextOnDark  = Color(0xFFFFFFFF)

private val BorderCard  = Color(0xFFDCEBDC)
private val DividerCol  = Color(0xFFE8F0E8)

private val ErrBg       = Color(0xFFFDECEC)
private val ErrText     = Color(0xFFC62828)

// ═══════════════════════════════════════════════════════════════════
// DATA MODELS
// ═══════════════════════════════════════════════════════════════════
data class CropRec(
    val name: String,
    val scientific: String,
    val suitability: String,
    val expectedYield: String,
    val reason: String
)

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
    val climateSuitability: Int
)

// ═══════════════════════════════════════════════════════════════════
// SCREEN
// ═══════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRecommendationScreen(
    navController: NavController,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { dashboardViewModel.startListening() }

    val nodes    by dashboardViewModel.nodes.collectAsState(initial = emptyList())
    val openAI    = remember { OpenAIService() }
    val scope     = rememberCoroutineScope()

    var resultText    by remember { mutableStateOf("") }
    var loading       by remember { mutableStateOf(false) }
    var loadingPhase  by remember { mutableStateOf(0) }
    var error         by remember { mutableStateOf<String?>(null) }
    var autoTriggered by remember { mutableStateOf(false) }
    var showRefreshDialog by remember { mutableStateOf(false) }

    // ── Sensor averages (across all nodes) ──
    val realNodes   = nodes.filter { it.n != null || it.p != null || it.k != null || it.ph != null }
    val avgN        = realNodes.mapNotNull { it.n }.averageOrZero()
    val avgP        = realNodes.mapNotNull { it.p }.averageOrZero()
    val avgK        = realNodes.mapNotNull { it.k }.averageOrZero()
    val avgPh       = realNodes.mapNotNull { it.ph }.averageOrZero()
    val avgTemp     = realNodes.mapNotNull { it.temperature }.averageOrZero()
    val avgHumidity = realNodes.mapNotNull { it.humidity }.averageOrZero()

    // ── Location: pull from ViewModel/farm, fallback gracefully ──
    // dashboardViewModel should expose the current farm's location.
    // We access it via reflection to stay compatible with all ViewModel variants.
    val farmLocation: String = remember(dashboardViewModel) {
        runCatching {
            // Try common property names: selectedFarm, farm, currentFarm
            val vm = dashboardViewModel
            val clazz = vm.javaClass
            val flowField = clazz.methods
                .firstOrNull { it.name in listOf("getSelectedFarm", "getFarm", "getCurrentFarm") }
            if (flowField != null) {
                val flow = flowField.invoke(vm)
                // StateFlow<Farm?> — call .value
                val valueMethod = flow?.javaClass?.getMethod("getValue")
                val farmObj     = valueMethod?.invoke(flow)
                if (farmObj != null) {
                    val locMethod = farmObj.javaClass.methods
                        .firstOrNull { it.name in listOf("getLocation", "location") }
                    val loc = locMethod?.invoke(farmObj)?.toString()?.trim()
                    if (!loc.isNullOrBlank()) return@runCatching loc
                }
            }
            // Also try nodes themselves if they carry a location field
            nodes.firstOrNull()?.let { node ->
                val locMethod = node.javaClass.methods
                    .firstOrNull { it.name in listOf("getLocation", "getCity", "getRegion") }
                locMethod?.invoke(node)?.toString()?.trim()?.ifBlank { null }
            } ?: ""
        }.getOrDefault("")
    }

    // Fallback display — if blank, show "Your Farm"
    val displayLocation = farmLocation.ifBlank { "Your Farm" }

    // Rainfall — try to read from node, else use "–"
    val rainfallDisplay: String = remember(nodes) {
        nodes.firstOrNull()?.let { node ->
            runCatching {
                val m = node.javaClass.methods.firstOrNull {
                    it.name in listOf("getRainfall", "getRain", "getPrecipitation")
                }
                val v = m?.invoke(node)
                if (v != null) "${formatNodeVal(v)} mm" else null
            }.getOrNull()
        } ?: "–"
    }

    // Soil type — try to read from node/farm, else "Loamy"
    val soilTypeDisplay: String = remember(nodes, dashboardViewModel) {
        runCatching {
            val vm = dashboardViewModel
            val flowField = vm.javaClass.methods
                .firstOrNull { it.name in listOf("getSelectedFarm", "getFarm", "getCurrentFarm") }
            val flow    = flowField?.invoke(vm)
            val farmObj = flow?.javaClass?.getMethod("getValue")?.invoke(flow)
            if (farmObj != null) {
                val m = farmObj.javaClass.methods
                    .firstOrNull { it.name in listOf("getSoilType", "getSoil") }
                m?.invoke(farmObj)?.toString()?.trim()?.ifBlank { null }
            } else null
        }.getOrNull()
            ?: nodes.firstOrNull()?.let { node ->
                runCatching {
                    val m = node.javaClass.methods
                        .firstOrNull { it.name in listOf("getSoilType", "getSoil") }
                    m?.invoke(node)?.toString()?.trim()?.ifBlank { null }
                }.getOrNull()
            }
            ?: "Loamy"
    }

    // All nodes with at least one sensor value
    val sensorNodes = nodes.filter {
        it.n != null || it.p != null || it.k != null ||
                it.ph != null || it.temperature != null || it.humidity != null
    }

    val crops: List<CropFull> = remember(resultText) {
        if (resultText.isNotBlank()) parseCropsFull(resultText) else emptyList()
    }

    // ── Key that tracks the actual sensor values so we re-generate when they change ──
    val sensorKey = remember(avgN, avgP, avgK, avgPh, avgTemp, avgHumidity, farmLocation) {
        "$avgN|$avgP|$avgK|$avgPh|$avgTemp|$avgHumidity|$farmLocation"
    }

    // ── Auto-generate once data arrives; re-generate if sensor values change significantly ──
    LaunchedEffect(sensorKey) {
        if (realNodes.isEmpty()) return@LaunchedEffect
        if (autoTriggered && resultText.isNotBlank() && !loading) {
            // Data changed after first load — silently refresh
            loading = true; loadingPhase = 1; error = null
            scope.launch {
                try {
                    delay(400); loadingPhase = 2
                    val prompt = buildPrompt(avgN, avgP, avgK, avgPh, avgTemp, avgHumidity, farmLocation, soilTypeDisplay)
                    loadingPhase = 3
                    val response = openAI.getRecommendation(prompt)
                    if (!response.isNullOrBlank()) resultText = response
                } catch (_: Exception) {
                } finally { loading = false; loadingPhase = 0 }
            }
            return@LaunchedEffect
        }
        if (!autoTriggered && resultText.isBlank() && !loading) {
            autoTriggered = true
            delay(700)
            loading = true; loadingPhase = 1; error = null
            scope.launch {
                try {
                    delay(500); loadingPhase = 2
                    val prompt = buildPrompt(avgN, avgP, avgK, avgPh, avgTemp, avgHumidity, farmLocation, soilTypeDisplay)
                    loadingPhase = 3
                    val response = openAI.getRecommendation(prompt)
                    if (response.isNullOrBlank()) {
                        error = "Could not fetch recommendation. Tap Refresh to try again."
                    } else {
                        resultText = response
                    }
                } catch (e: Exception) {
                    error = e.message ?: "Something went wrong."
                } finally { loading = false; loadingPhase = 0 }
            }
        }
    }

    fun launchRefresh() {
        if (realNodes.isEmpty()) { error = "No sensor data yet."; return }
        loading = true; loadingPhase = 1; error = null; resultText = ""
        scope.launch {
            try {
                delay(400); loadingPhase = 2
                val prompt = buildPrompt(avgN, avgP, avgK, avgPh, avgTemp, avgHumidity, farmLocation, soilTypeDisplay)
                loadingPhase = 3
                val response = openAI.getRecommendation(prompt)
                if (response.isNullOrBlank()) {
                    error = "Could not fetch recommendation. Try again."
                } else {
                    resultText = response
                }
            } catch (e: Exception) {
                error = e.message ?: "Something went wrong."
            } finally { loading = false; loadingPhase = 0 }
        }
    }

    // ── Refresh confirmation dialog ──
    if (showRefreshDialog) {
        AlertDialog(
            onDismissRequest = { showRefreshDialog = false },
            containerColor   = BgCard,
            shape            = RoundedCornerShape(22.dp),
            icon = {
                Box(
                    Modifier.size(52.dp).background(GreenPale, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("🔄", fontSize = 24.sp) }
            },
            title = {
                Text(
                    "Refresh Recommendations?",
                    fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    color = TextPri, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "New recommendations will be generated using your current live sensor readings from $displayLocation. Previous results will be replaced.",
                    fontSize = 14.sp, color = TextSec, lineHeight = 21.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick  = { showRefreshDialog = false; launchRefresh() },
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenDark),
                    shape    = RoundedCornerShape(40.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Yes, Refresh Now", color = TextOnDark, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { showRefreshDialog = false },
                    shape    = RoundedCornerShape(40.dp),
                    border   = BorderStroke(1.dp, BorderCard),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel", color = TextMut, fontWeight = FontWeight.Medium) }
            }
        )
    }

    // ── Scaffold ──
    Scaffold(
        containerColor = BgScreen,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPri)
                    }
                },
                title = {
                    Column {
                        Text(
                            "Crop Recommendation",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPri
                        )
                        if (displayLocation != "Your Farm") {
                            Text(
                                "📍 $displayLocation",
                                fontSize = 11.sp, color = GreenMid, fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {

                    AnimatedVisibility(visible = crops.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                        Box(
                            Modifier
                                .padding(end = 12.dp)
                                .background(BgLiveTag, RoundedCornerShape(20.dp))
                                .border(1.dp, GreenTrack, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )

                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Loading widget ──────────────────────────────────────
            AnimatedVisibility(visible = loading, enter = fadeIn(), exit = fadeOut()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FetchingWidget(loadingPhase, displayLocation)
                    Spacer(Modifier.height(24.dp))
                }
            }

            // ── Error card ──────────────────────────────────────────
            AnimatedVisibility(visible = error != null && !loading, enter = fadeIn()) {
                error?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = CardDefaults.cardColors(containerColor = ErrBg)
                    ) {
                        Text("⚠  $it", color = ErrText, fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp))
                    }
                }
            }

            // ── Top crop hero ───────────────────────────────────────
            AnimatedVisibility(
                visible = crops.isNotEmpty() && !loading,
                enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 6 })
            ) {
                crops.firstOrNull()?.let { top ->
                    TopCropArcCard(crop = top, location = displayLocation)
                    Spacer(Modifier.height(22.dp))
                }
            }

            // ── Other crops ─────────────────────────────────────────
            AnimatedVisibility(
                visible = crops.size > 1 && !loading,
                enter   = fadeIn() + slideInVertically(initialOffsetY = { it / 6 })
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        "Other Suitable Crops",
                        fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPri
                    )
                    Spacer(Modifier.height(10.dp))
                    crops.drop(1).forEachIndexed { idx, crop ->
                        OtherCropRow(crop = crop, index = idx)
                        Spacer(Modifier.height(10.dp))
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── Farm Profile ─────────────────────────────────────────
            FarmProfileSection(
                n = avgN, p = avgP, k = avgK,
                ph = avgPh, temp = avgTemp, humidity = avgHumidity,
                rainfall = rainfallDisplay,
                soilType = soilTypeDisplay,
                location = displayLocation,
                sensorNodes = sensorNodes,
                nodeCount   = realNodes.size
            )

            Spacer(Modifier.height(22.dp))

            // ── Main action button ───────────────────────────────────
            Button(
                onClick  = {
                    if (crops.isEmpty()) launchRefresh() else showRefreshDialog = true
                },
                enabled  = !loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (loading) GreenTrack else GreenDark
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = TextOnDark, strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Analysing…", color = TextOnDark, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                } else {
                    Text(
                        if (crops.isEmpty()) "🌱  Get Recommendation"
                        else "🔄  Refresh Recommendation",
                        color = TextOnDark, fontWeight = FontWeight.SemiBold, fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(36.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// FETCHING WIDGET
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun FetchingWidget(phase: Int, location: String) {
    val labels = listOf(
        "Reading $location sensors…",
        "Analysing soil profile…",
        "Building your recommendation…"
    )
    val label = labels.getOrElse(phase - 1) { "Please wait…" }

    val inf = rememberInfiniteTransition(label = "spin")
    val sweep by inf.animateFloat(
        0f, 360f, infiniteRepeatable(tween(1000, easing = LinearEasing)), label = "sw"
    )
    val pulse by inf.animateFloat(
        0.35f, 1f,
        infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(
        Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(20.dp))
            .border(1.dp, BorderCard, RoundedCornerShape(20.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(74.dp), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(Modifier.size(74.dp)) {
                drawArc(GreenTrack, 0f, 360f, false, style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
                drawArc(GreenDark.copy(alpha = pulse), sweep, 110f, false,
                    style = Stroke(7.dp.toPx(), cap = StrokeCap.Round))
            }
            Text("🌿", fontSize = 28.sp)
        }

        Spacer(Modifier.height(14.dp))
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPri)
        Spacer(Modifier.height(4.dp))
        Text(
            "SmartKrishi AI is analysing your farm",
            fontSize = 12.sp, color = TextMut
        )
        Spacer(Modifier.height(16.dp))

        // 3-step phase bar
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { step ->
                val filled = step < phase
                val active = step == phase - 1
                val animPct by animateFloatAsState(
                    if (filled || active) 1f else 0f, tween(500), label = "step_$step"
                )
                Box(
                    Modifier.weight(1f).height(5.dp)
                        .clip(RoundedCornerShape(3.dp)).background(GreenTrack)
                ) {
                    Box(
                        Modifier.fillMaxWidth(animPct).fillMaxHeight()
                            .background(if (active) GreenMid else GreenDark)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Step $phase of 3",
            fontSize = 11.sp, color = TextMut
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// TOP CROP ARC CARD
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun TopCropArcCard(crop: CropFull, location: String) {
    var arcTarget by remember { mutableStateOf(0f) }
    val arcAnim   by animateFloatAsState(
        arcTarget, tween(1600, easing = FastOutSlowInEasing), label = "topArc"
    )
    LaunchedEffect(crop.match) { delay(250); arcTarget = crop.match.toFloat() }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = BgCard),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, BorderCard)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header label
            Text(
                "TOP RECOMMENDED CROP",
                fontSize = 10.sp, color = TextMut,
                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
            )

            Spacer(Modifier.height(18.dp))

            // Big arc
            Box(Modifier.size(195.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(Modifier.size(195.dp)) {
                    val sw = Stroke(15.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(GreenTrack, -90f, 360f, false, style = sw)
                    drawArc(GreenDark, -90f, (arcAnim / 100f) * 360f, false, style = sw)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                        Text(
                            "${crop.match}",
                            fontWeight = FontWeight.ExtraBold, fontSize = 52.sp,
                            color = GreenDark, lineHeight = 52.sp
                        )
                        Text(
                            "%",
                            fontWeight = FontWeight.Bold, fontSize = 20.sp,
                            color = GreenDark, modifier = Modifier.padding(bottom = 10.dp, start = 1.dp)
                        )
                    }
                    Text(
                        crop.name,
                        fontWeight = FontWeight.Bold, fontSize = 17.sp,
                        color = TextPri, textAlign = TextAlign.Center
                    )
                    if (crop.scientific.isNotBlank()) {
                        Text(
                            crop.scientific,
                            fontStyle = FontStyle.Italic, fontSize = 11.sp,
                            color = TextMut
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── 3 stat tiles: Duration | Sowing | Harvest ──
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(BgMuted, RoundedCornerShape(14.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatTile("📅", "DURATION", crop.duration, Modifier.weight(1f))

                QuickStatTile("🌱", "SOWING",   crop.sowingTime, Modifier.weight(1f))

                QuickStatTile("🚜", "HARVEST",  crop.harvestTime, Modifier.weight(1f))
            }

            // ── Yield ──
            if (crop.yield.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(GreenPale, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("📈  Expected Yield", fontSize = 13.sp, color = TextSec, fontWeight = FontWeight.Medium)
                    Text(crop.yield, fontSize = 14.sp, color = GreenDark, fontWeight = FontWeight.Bold)
                }
            }

            // ── Chips ──
            val chips = listOf(crop.soilFit, crop.marketDemand).filter { it.isNotBlank() }
            if (chips.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chips.forEach { chip ->
                        Box(
                            Modifier
                                .background(BgChip, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(chip, fontSize = 12.sp, color = GreenMid, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun RowScope.align(alignment: Alignment.Vertical) = this

@Composable
private fun QuickStatTile(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.height(5.dp))
        Text(
            label, fontSize = 9.sp, color = TextMut,
            fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(3.dp))
        Text(
            value.ifBlank { "–" },
            fontSize = 12.sp, color = TextPri,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// OTHER CROP ROW
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun OtherCropRow(crop: CropFull, index: Int) {
    var arcTarget by remember { mutableStateOf(0f) }
    val arcAnim   by animateFloatAsState(
        arcTarget, tween(1000, easing = FastOutSlowInEasing), label = "otherArc_$index"
    )
    LaunchedEffect(crop.match) { delay((200 + index * 150).toLong()); arcTarget = crop.match.toFloat() }

    val icons = listOf("🌿", "🍃", "🌾")
    val icon  = icons.getOrElse(index) { "🌱" }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = BgCard),
        border    = BorderStroke(1.dp, BorderCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(44.dp).background(GreenPale, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(icon, fontSize = 20.sp) }

            Column(Modifier.weight(1f)) {
                Text(crop.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPri)
                Spacer(Modifier.height(3.dp))
                Text(
                    buildString {
                        if (crop.duration.isNotBlank()) { append(crop.duration) }
                        if (crop.sowingTime.isNotBlank()) {
                            if (isNotEmpty()) append("  •  ")
                            append("Sow: ${crop.sowingTime}")
                        }
                    }.ifBlank { "–" },
                    fontSize = 12.sp, color = TextMut
                )
            }

            // Mini arc
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Canvas(Modifier.size(48.dp)) {
                        val sw = Stroke(4.5.dp.toPx(), cap = StrokeCap.Round)
                        drawArc(GreenTrack, -90f, 360f, false, style = sw)
                        drawArc(GreenDark, -90f, (arcAnim / 100f) * 360f, false, style = sw)
                    }
                    Text("${crop.match}%", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GreenDark)
                }
                Text(
                    "MATCH", fontSize = 8.sp, color = TextMut,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// FARM PROFILE SECTION — fully dynamic location + soil data
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun FarmProfileSection(
    n: Float, p: Float, k: Float,
    ph: Float, temp: Float, humidity: Float,
    rainfall: String,
    soilType: String,
    location: String,
    sensorNodes: List<*>,
    nodeCount: Int
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = BgCard),
        border    = BorderStroke(1.dp, BorderCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Farm Profile", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPri)
                Box(
                    Modifier
                        .background(BgLiveTag, RoundedCornerShape(20.dp))
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(Modifier.size(6.dp).background(GreenMid, CircleShape))
                        Text("Live Conditions", fontSize = 11.sp, color = GreenDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Location row
            Row(
                Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("📍", fontSize = 13.sp)
                Text(location, fontSize = 13.sp, color = GreenMid, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(14.dp))

            if (nodeCount == 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(BgMuted, RoundedCornerShape(12.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🌐  Waiting for sensor data from Firebase…",
                        fontSize = 13.sp, color = TextMut, textAlign = TextAlign.Center
                    )
                }
            } else {
                // ── Row 1: Soil | pH | Moisture ──
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileTile("SOIL TYPE", soilType, Modifier.weight(1f))
                    ProfileTile("PH LEVEL", "${ph.let { if (it == it.toLong().toFloat()) it.toLong().toString() else "%.1f".format(it) }}", Modifier.weight(1f))
                    ProfileTile("MOISTURE", "${humidity.toInt()}%", Modifier.weight(1f))
                }

                Spacer(Modifier.height(8.dp))

                // ── Row 2: Temp | Rainfall ──
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileTile("TEMP", "${temp.toInt()}°C", Modifier.weight(1f))
                    ProfileTile("RAINFALL", rainfall, Modifier.weight(1f))
                    // Filler to match grid
                    Box(Modifier.weight(1f))
                }

                Spacer(Modifier.height(8.dp))

                // ── NPK block — full width, no overflow ──
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(BgMuted, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column {
                        Text(
                            "NPK VALUE",
                            fontSize = 10.sp, color = TextMut,
                            fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NpkCell("N", n.toInt().toString(), GreenDark)
                            // Divider
                            Box(Modifier.width(1.dp).height(40.dp).background(DividerCol))
                            NpkCell("P", p.toInt().toString(), GreenMid)
                            Box(Modifier.width(1.dp).height(40.dp).background(DividerCol))
                            NpkCell("K", k.toInt().toString(), TextSec)
                        }
                    }
                }

                // ── Per-node table ──
                if (sensorNodes.size > 1) {
                    Spacer(Modifier.height(16.dp))
                    NodeSensorTable(sensorNodes)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// PROFILE TILE — individual stat cell
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun ProfileTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(BgMuted, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 11.dp)
    ) {
        Text(
            label, fontSize = 9.sp, color = TextMut,
            fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, maxLines = 1
        )
        Spacer(Modifier.height(5.dp))
        Text(
            value.ifBlank { "–" },
            fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPri,
            maxLines = 1
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// NPK CELL
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun NpkCell(element: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            element, fontSize = 11.sp, color = TextMut,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color
        )
        Text(
            "mg/kg", fontSize = 9.sp, color = TextMut
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// NODE SENSOR TABLE
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun NodeSensorTable(sensorNodes: List<*>) {
    Column(Modifier.fillMaxWidth()) {

        Text("Live Sensor Nodes", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPri)
        Spacer(Modifier.height(10.dp))

        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .background(BgNodeHead, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            NTH("Node", 0.17f)
            NTH("N",    0.13f, TextAlign.Center)
            NTH("P",    0.13f, TextAlign.Center)
            NTH("K",    0.13f, TextAlign.Center)
            NTH("pH",   0.13f, TextAlign.Center)
            NTH("T°C",  0.15f, TextAlign.Center)
            NTH("Hum%", 0.16f, TextAlign.Center)
        }

        HorizontalDivider(color = DividerCol, thickness = 1.dp)

        sensorNodes.forEachIndexed { i, node ->
            val any  = node ?: return@forEachIndexed
            val nVal = nodeGet(any, "getN")
            val pVal = nodeGet(any, "getP")
            val kVal = nodeGet(any, "getK")
            val phV  = nodeGet(any, "getPh")
            val tVal = nodeGet(any, "getTemperature")
            val hVal = nodeGet(any, "getHumidity")
            val bg   = if (i % 2 == 0) BgCard else BgNodeAlt

            Row(
                Modifier.fillMaxWidth().background(bg)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                NTD("Node ${i + 1}", 0.17f, fontWeight = FontWeight.SemiBold)
                NTD(nVal, 0.13f, align = TextAlign.Center)
                NTD(pVal, 0.13f, align = TextAlign.Center)
                NTD(kVal, 0.13f, align = TextAlign.Center)
                NTD(phV,  0.13f, align = TextAlign.Center)
                NTD(tVal, 0.15f, align = TextAlign.Center)
                NTD(hVal, 0.16f, align = TextAlign.Center)
            }

            if (i < sensorNodes.lastIndex)
                HorizontalDivider(color = DividerCol, thickness = 0.5.dp)
        }

        Box(
            Modifier.fillMaxWidth()
                .background(BgNodeHead, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .height(8.dp)
        )
    }
}

@Composable
private fun RowScope.NTH(text: String, weight: Float, align: TextAlign = TextAlign.Start) {
    Text(
        text, Modifier.weight(weight), fontSize = 11.sp, color = GreenDark,
        fontWeight = FontWeight.Bold, textAlign = align, maxLines = 1
    )
}

@Composable
private fun RowScope.NTD(
    text: String, weight: Float,
    fontWeight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Start
) {
    Text(
        text, Modifier.weight(weight), fontSize = 12.sp, color = TextSec,
        fontWeight = fontWeight, textAlign = align, maxLines = 1
    )
}

// ═══════════════════════════════════════════════════════════════════
// ARC CANVAS
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun ArcProgress(percent: Float, size: Dp, trackColor: Color, arcColor: Color, strokeWidth: Dp = 10.dp) {
    androidx.compose.foundation.Canvas(Modifier.size(size)) {
        val sw = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
        drawArc(trackColor, -90f, 360f, false, style = sw)
        drawArc(arcColor, -90f, (percent / 100f) * 360f, false, style = sw)
    }
}

// ═══════════════════════════════════════════════════════════════════
// PROMPT — fully dynamic: uses real location, soil type, sensor values
// ═══════════════════════════════════════════════════════════════════
private fun buildPrompt(
    n: Float, p: Float, k: Float,
    ph: Float, temp: Float, humidity: Float,
    location: String,
    soilType: String
): String {
    val loc = location.ifBlank { "Bihar, India" }
    val soil = soilType.ifBlank { "Loamy" }
    return """
You are an expert agronomist specialising in ${loc}, India.

Live sensor readings from the farmer's field in $loc:
- Nitrogen (N): ${n.toInt()} mg/kg
- Phosphorus (P): ${p.toInt()} mg/kg
- Potassium (K): ${k.toInt()} mg/kg
- Soil pH: $ph
- Temperature: ${temp.toInt()}°C
- Humidity/Moisture: ${humidity.toInt()}%
- Soil Type: $soil
- Location: $loc

Based on these exact readings, recommend 3 crops that are most suitable for this specific location and soil profile.
Consider the local climate, typical growing seasons, and market conditions of $loc.

Return ONLY a raw JSON array with exactly 3 crop objects. No markdown, no explanation, no code fences.

Schema:
{
  "name": "Paddy",
  "scientific": "Oryza sativa",
  "match": 92,
  "duration": "120 Days",
  "sowingTime": "July-Aug",
  "harvestTime": "Nov-Dec",
  "yield": "4.5 t/ha",
  "soilFit": "$soil Soil",
  "marketDemand": "High Demand",
  "waterNeeds": 85,
  "pestRisk": 25,
  "climateSuitability": 93
}

Rules:
- Recommend crops actually grown in $loc or nearby regions with similar agro-climatic conditions.
- First crop: best match score 88–96%.
- Second crop: 76–87%.
- Third crop: 65–78%.
- match, waterNeeds, pestRisk, climateSuitability = integers 0–100.
- sowingTime / harvestTime: short strings like "July-Aug" or "Nov-Dec" — local season timing for $loc.
- soilFit: short like "$soil Soil" or "Well-Drained".
- marketDemand: short like "High Demand" or "Stable Price".
- Reasons in your choice should reflect the actual sensor values (N=${n.toInt()}, P=${p.toInt()}, K=${k.toInt()}, pH=$ph).
- Return raw JSON array ONLY. Nothing else.
""".trimIndent()
}

// ═══════════════════════════════════════════════════════════════════
// PARSER
// ═══════════════════════════════════════════════════════════════════
private fun parseCropsFull(text: String): List<CropFull> {
    return try {
        val s = text.indexOf('[')
        val e = text.lastIndexOf(']')
        if (s < 0 || e < 0) return emptyList()
        splitJsonObjects(text.substring(s, e + 1)).mapNotNull { obj ->
            val name    = strField(obj, "name")                  ?: return@mapNotNull null
            val sci     = strField(obj, "scientific")            ?: ""
            val match   = intField(obj, "match")                 ?: 80
            val dur     = strField(obj, "duration")              ?: ""
            val sowing  = strField(obj, "sowingTime")            ?: ""
            val harvest = strField(obj, "harvestTime")           ?: ""
            val yld     = strField(obj, "yield")                 ?: ""
            val soilFit = strField(obj, "soilFit")               ?: ""
            val mktDem  = strField(obj, "marketDemand")          ?: ""
            val water   = intField(obj, "waterNeeds")            ?: 65
            val pest    = intField(obj, "pestRisk")              ?: 30
            val climate = intField(obj, "climateSuitability")    ?: 80
            CropFull(
                name = name, scientific = sci, match = match,
                duration = dur, sowingTime = sowing, harvestTime = harvest,
                yield = yld, soilFit = soilFit, marketDemand = mktDem,
                waterNeeds = water, pestRisk = pest, climateSuitability = climate
            )
        }
    } catch (_: Exception) { emptyList() }
}

private fun splitJsonObjects(json: String): List<String> {
    val out = mutableListOf<String>()
    var depth = 0; var start = -1
    for (i in json.indices) when (json[i]) {
        '{' -> { if (depth++ == 0) start = i }
        '}' -> { if (--depth == 0 && start >= 0) { out.add(json.substring(start, i + 1)); start = -1 } }
    }
    return out
}

private fun strField(json: String, key: String): String? =
    Regex(""""$key"\s*:\s*"([^"\\]*(?:\\.[^"\\]*)*)"""").find(json)?.groupValues?.get(1)

private fun intField(json: String, key: String): Int? =
    Regex(""""$key"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toIntOrNull()

// ═══════════════════════════════════════════════════════════════════
// NODE REFLECTION HELPER
// ═══════════════════════════════════════════════════════════════════
private fun nodeGet(obj: Any, method: String): String =
    runCatching {
        val v = obj.javaClass.getMethod(method).invoke(obj) ?: return@runCatching "—"
        formatNodeVal(v)
    }.getOrDefault("—")

private fun formatNodeVal(v: Any): String = when (v) {
    is Float  -> if (v == v.toLong().toFloat()) v.toLong().toString() else "%.1f".format(v)
    is Double -> if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)
    else      -> v.toString()
}

// ═══════════════════════════════════════════════════════════════════
// UTIL
// ═══════════════════════════════════════════════════════════════════
private fun List<Float>.averageOrZero(): Float =
    if (isNotEmpty()) average().toFloat() else 0f