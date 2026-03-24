package com.smartkrishi.presentation.dashboard
import kotlinx.coroutines.isActive
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.smartkrishi.R
import com.smartkrishi.presentation.chat.KrishiMitriChatScreen
import com.smartkrishi.presentation.home.FarmViewModel
import com.smartkrishi.presentation.model.Farm
import com.smartkrishi.presentation.model.SensorNode
import com.smartkrishi.presentation.navigation.Screen
import com.smartkrishi.presentation.theme.ThemeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// ===============================================================
// THEME COLORS FOR DASHBOARD
// ===============================================================
private val PrimaryGreen = Color(0xFF2E7D32)
private val AccentGreenSoft = Color(0xFFC8E6C9)
private val LightBackground = Color(0xFFF3FBF5)
private val DarkBackground = Color(0xFF0A1C12)
private val CardLight = Color(0xFFFFFFFF)
private val CardDark = Color(0xFF153525)

// ===============================================================
// WEATHER DATA MODELS
// ===============================================================
// Replace with your actual OpenWeather API key
private const val OPEN_WEATHER_API_KEY = "64961347ba9d05d6b1a486037c5142c1"

data class HourlyForecast(
    val time: String,
    val tempC: Int,
    val condition: String,
    val rainChance: Int
)

data class DailyForecast(
    val label: String,
    val minTempC: Int,
    val maxTempC: Int,
    val condition: String,
    val rainChance: Int,
    val iconCode: String = ""
)

data class WeatherInfo(
    val condition: String,
    val tempC: Int,
    val humidity: Int,
    val windKph: Int,
    val pressure: Int = 1013,
    val visibility: Double = 10.0,
    val uvIndex: Int = 0,
    val dewPoint: Int = 0,
    val feelsLike: Int = 0,
    val daily: List<DailyForecast>,
    val hourly: List<HourlyForecast> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

// ===============================================================
// AI RECOMMENDATIONS
// ===============================================================
data class AiRecommendations(
    val alertLine: String?,
    val cropSuggestions: List<String>,
    val fertiliserPlanLines: List<String>
)

// ===============================================================
// SENSOR UI MODELS
// ===============================================================
data class SoilParam(
    val name: String,
    val displayValue: String,
    val numericValue: Float
)

private data class SoilNodeUi(
    val id: String,
    val title: String,
    val params: List<SoilParam>
)

private fun demoNodeUi(): SoilNodeUi = SoilNodeUi(
    id = "DEMO",
    title = "Demo node",
    params = listOf(
        SoilParam("Moisture", "55%", 55f),
        SoilParam("Soil Temp", "26°C", 26f),
        SoilParam("Nitrogen", "45 mg/kg", 45f),
        SoilParam("Phosphorus", "20 mg/kg", 20f),
        SoilParam("Potassium", "50 mg/kg", 50f),
        SoilParam("pH", "6.8", 6.8f),
        SoilParam("EC", "1.2", 1.2f)
    )
)

// ===============================================================
// DASHBOARD MAIN SCREEN
// ===============================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    drawerState: DrawerState,
    farm: Farm
) {
    val scope = rememberCoroutineScope()
    val farmViewModel: FarmViewModel = hiltViewModel()
    val farms by farmViewModel.farms.collectAsState()

    var selectedFarm by remember { mutableStateOf(farm) }
    val isDarkTheme = ThemeState.isDarkTheme.value
    var showChatPopup by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("EN") }
    var showFarmDialog by remember { mutableStateOf(false) }

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firebaseDatabase = remember { FirebaseDatabase.getInstance() }

    LaunchedEffect(farms, farm) {
        selectedFarm = when {
            farm.id.isNotBlank() -> farm
            farms.isNotEmpty() -> farms.first()
            else -> Farm()
        }
    }


    var isPumpUpdating by remember { mutableStateOf(false) }

    val backgroundColor = if (isDarkTheme) DarkBackground else LightBackground
    val cardColor = if (isDarkTheme) CardDark else CardLight
    val textPrimary = if (isDarkTheme) Color(0xFFE8F5E9) else Color(0xFF0A0F0B)
    val textSecondary = if (isDarkTheme) Color(0xFFB5CBB8) else Color.Gray

    val userName = remember {
        firebaseAuth.currentUser?.displayName ?: "Farmer"
    }

    val dashboardViewModel: DashboardViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        dashboardViewModel.startListening()
    }

    DisposableEffect(selectedFarm.id) {
        onDispose {
            dashboardViewModel.stopListening()
        }
    }

    val sensorNodes by dashboardViewModel.nodes.collectAsState()
    val tds by dashboardViewModel.tds.collectAsState()
    val tank by dashboardViewModel.tank.collectAsState()
    val rain by dashboardViewModel.rain.collectAsState()
    val pumpValue by dashboardViewModel.pump.collectAsState()

    var weatherInfo by remember { mutableStateOf<WeatherInfo?>(null) }
    var weatherLoading by remember { mutableStateOf(false) }
    var weatherError by remember { mutableStateOf<String?>(null) }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var isPumpOn by remember { mutableStateOf(false) }
    val tds by dashboardViewModel.tds.collectAsState()
    val tank by dashboardViewModel.tank.collectAsState()
    val rain by dashboardViewModel.rain.collectAsState()
    val pumpValue by dashboardViewModel.pump.collectAsState()

    LaunchedEffect(pumpValue) {
        isPumpOn = pumpValue == 1
    }
    var isPumpOn by remember { mutableStateOf(false) }

    LaunchedEffect(pumpValue) {
        isPumpOn = pumpValue == 1
    }

    // Auto-refresh weather
    LaunchedEffect(selectedFarm.id, selectedFarm.location, selectedFarm.lat, selectedFarm.lon) {
        val locName = selectedFarm.location
        val lat = selectedFarm.lat
        val lon = selectedFarm.lon

        if ((lat == 0.0 && lon == 0.0) && locName.isBlank()) {
            weatherInfo = null
            weatherError = "No location set for this farm"
            return@LaunchedEffect
        }

        weatherLoading = true
        weatherError = null
        weatherInfo = fetchWeatherForLocation(
            locationName = locName,
            lat = lat,
            lon = lon
        ) ?: run {
            weatherError = "Unable to fetch weather"
            null
        }
        weatherLoading = false

        // Auto refresh every 10 minutes
        while (isActive) {
            delay(600000)
            val freshWeather = fetchWeatherForLocation(locName, lat, lon)
            if (freshWeather != null) {
                weatherInfo = freshWeather
                weatherError = null
            }
        }
    }

    val aiRecommendations = remember(sensorNodes, weatherInfo) {
        computeAiRecommendations(sensorNodes, weatherInfo)
    }

    if (showFarmDialog) {
        FarmSelectionDialog(
            farms = farms,
            selectedFarm = selectedFarm,
            onDismiss = { showFarmDialog = false },
            onFarmSelected = { farm ->
                selectedFarm = farm
                showFarmDialog = false
            },
            navController = navController,
            cardColor = cardColor,
            textPrimary = textPrimary,
            textSecondary = textSecondary
        )
    }

    if (showWeatherDialog && weatherInfo != null) {
        WeatherDetailDialog(
            weather = weatherInfo!!,
            location = selectedFarm.location,
            onDismiss = { showWeatherDialog = false },
            cardColor = cardColor,
            textPrimary = textPrimary,
            textSecondary = textSecondary
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContent(navController) }
    ) {
        Scaffold(
            containerColor = backgroundColor,
            floatingActionButton = {
                if (!showChatPopup) {
                    AnimatedChatbotFab {
                        showChatPopup = true
                    }
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = textPrimary
                    ),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "SMART KRISHI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                "Bringing intelligence to every leaf",
                                fontSize = 11.sp,
                                color = textSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = textPrimary
                            )
                        }
                    },
                    actions = {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .height(30.dp)
                                .clickable {
                                    language = if (language == "EN") "HI" else "EN"
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    language,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        IconButton(onClick = {
                            ThemeState.isDarkTheme.value = !ThemeState.isDarkTheme.value
                        }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle theme",
                                tint = PrimaryGreen
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(backgroundColor)
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                GreetingSection(
                    userName = userName,
                    farmName = selectedFarm.name,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )


                ProfessionalFarmSelector(
                    selectedFarm = selectedFarm,
                    onClick = { showFarmDialog = true },
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
                CompactWeatherCard(
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    farmLocation = selectedFarm.location,
                    weather = weatherInfo,
                    isLoading = weatherLoading,
                    error = weatherError,
                    onClick = { showWeatherDialog = true }
                )
                NodeSensorsSection(
                    nodes = sensorNodes,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onSyncAll = { dashboardViewModel.reloadAll() },
                    onSyncNode = { nodeId -> dashboardViewModel.reloadNode(nodeId) },
                    onDemoSoilUpdate = { updated ->
                        demoSoilParams = updated
                    },onDemoVersionBump = {
                        demoVersion++
                    }
                )

                SystemStatusGrid(
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isPumpOn = isPumpOn,
                    isPumpUpdating = isPumpUpdating,
                    tds = tds,
                    tank = tank,
                    rain = rain,
                    onPumpToggleRequest = { desired ->
                        if (isPumpUpdating) return@SystemStatusGrid
                        isPumpUpdating = true
                        scope.launch {
                            try {val uid = firebaseAuth.currentUser?.uid
                                val farmId = selectedFarm.id

                                if (uid != null && farmId.isNotBlank()) {
                                    firebaseDatabase.reference
                                        .child("dashboard")
                                        .child(uid)
                                        .child(farmId)
                                        .child("live")
                                        .child("irrigation")
                                        .child("pump")
                                        .setValue(if (desired) 1 else 0)
                                }
                            } finally {
                                delay(800)
                                isPumpOn = desired
                                isPumpUpdating = false
                            }
                        }
                    }
                )

                EnhancedAiRecommendations(
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    recommendations = aiRecommendations,
                    navController = navController,
                    weatherInfo = weatherInfo
                )

                LogHistoryBarGraph(
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    navController = navController
                )

                QuickModuleButtons(
                    navController = navController,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showChatPopup) {
        ChatPopup(
            navController = navController,
            onClosePopup = { showChatPopup = false }
        )
    }
}

// ===============================================================
// DRAWER - SCROLLABLE
// ===============================================================
@Composable
fun DrawerContent(navController: NavController) {
    val isDark by ThemeState.isDarkTheme
    val textColor = if (isDark) Color(0xFFE8F5E9) else Color(0xFF0A140D)
    val bgColor = if (isDark) Color(0xFF10261A) else Color(0xFFE8F5E9)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(270.dp)
            .background(bgColor)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Navigation", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
            Spacer(Modifier.height(18.dp))

            DrawerItem("Dashboard", Icons.Default.Home) {
                navController.navigate(Screen.Dashboard.route)
            }
            DrawerItem("Disease Detection", Icons.Default.BugReport) {
                navController.navigate(Screen.DiseaseDetection.route)
            }
            DrawerItem("Market Prices", Icons.Default.AttachMoney) {
                navController.navigate(Screen.MarketPrice.route)
            }
            DrawerItem("Rover Control", Icons.Default.Videocam) {
                navController.navigate(Screen.Rover.route)
            }
            DrawerItem("Logs & History", Icons.Default.History) {
                navController.navigate(Screen.Logs.route)
            }
            DrawerItem("KrishiMitri Chat", Icons.Default.SupportAgent) {
                navController.navigate(Screen.KrishiMitri.route)
            }
            Spacer(Modifier.height(6.dp))

            DrawerItem("Govt Schemes", Icons.Default.Assignment) {
                navController.navigate(Screen.GovtSchemes.route)
            }
            DrawerItem("Crops", Icons.Default.Agriculture) {
                navController.navigate(Screen.Crops.route)
            }
            DrawerItem("Equipment", Icons.Default.Build) {
                navController.navigate(Screen.Equipment.route)
            }

            Spacer(Modifier.height(12.dp))
        }

        Column {
            Divider(color = Color.Gray.copy(alpha = 0.3f))
            Spacer(Modifier.height(6.dp))

            DrawerItem("Profile", Icons.Default.Person) {
                navController.navigate(Screen.Profile.route)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    val isDark by ThemeState.isDarkTheme
    val textColor = if (isDark) Color(0xFFE8F5E9) else Color(0xFF0A140D)
    val itemBg = if (isDark) Color(0xFF173822) else Color(0xFFFFFFFF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(itemBg)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryGreen)
        }
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = textColor)
    }
}

// ===============================================================
// GREETING SECTION
// ===============================================================
@Composable
fun GreetingSection(
    userName: String,
    farmName: String,
    textPrimary: Color,
    textSecondary: Color
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
    val dateString = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())

    Column {
        Text(
            "Namaste, $userName 🙏",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "$greeting • $dateString",
            color = textSecondary,
            fontSize = 13.sp
        )
        Text(
            if (farmName.isBlank()) "No farm selected" else "Managing $farmName",
            color = textSecondary,
            fontSize = 13.sp
        )
    }
}

// Continue in next message due to length...
// ===============================================================
// ENHANCED WEATHER CARD WITH TIME-BASED ICON
// ===============================================================
// COMPACT PROFESSIONAL WEATHER CARD - Like Google Weather
@Composable
private fun CompactWeatherCard(
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    farmLocation: String,
    weather: WeatherInfo?,
    isLoading: Boolean,
    error: String?,
    onClick: () -> Unit
) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    // Time-based icon
    val timeIcon = when (hour) {
        in 6..17 -> "☀️"
        in 18..19 -> "🌆"
        else -> "🌙"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryGreen,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        error,
                        fontSize = 13.sp,
                        color = Color(0xFFE53935)
                    )
                }
            }
            weather != null -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Background gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PrimaryGreen.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        // Location header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    farmLocation.ifBlank { "Unknown" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                timeIcon,
                                fontSize = 28.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Main temperature display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${weather.tempC}°",
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    lineHeight = 64.sp
                                )
                                Text(
                                    weather.condition,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary
                                )
                            }

                            // Weather illustration placeholder
                            Text(
                                weatherEmojiFor(weather.condition),
                                fontSize = 72.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Temperature range and feels like
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Thermostat,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Feels like ${weather.feelsLike}°",
                                    fontSize = 13.sp,
                                    color = textSecondary
                                )
                            }

                            // Today's high/low from forecast
                            weather.daily.firstOrNull()?.let { today ->
                                Text(
                                    "↑ ${today.maxTempC}° / ↓ ${today.minTempC}°",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textSecondary
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Bottom metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CompactMetric(
                                icon = Icons.Outlined.WaterDrop,
                                value = "${weather.humidity}%",
                                textColor = textSecondary
                            )
                            CompactMetric(
                                icon = Icons.Outlined.Air,
                                value = "${weather.windKph} km/h",
                                textColor = textSecondary
                            )
                            CompactMetric(
                                icon = Icons.Outlined.Visibility,
                                value = "${weather.visibility.roundToInt()} km",
                                textColor = textSecondary
                            )
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Weather not available",
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactMetric(
    icon: ImageVector,
    value: String,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

private fun weatherEmojiFor(condition: String): String {
    val c = condition.lowercase(Locale.getDefault())
    return when {
        "thunder" in c -> "⛈️"
        "rain" in c || "drizzle" in c -> "🌧️"
        "snow" in c -> "❄️"
        "cloud" in c -> "☁️"
        "clear" in c -> "☀️"
        else -> "🌤️"
    }
}

// ===============================================================
// PROFESSIONAL FARM SELECTOR
// ===============================================================
@Composable
private fun ProfessionalFarmSelector(
    selectedFarm: Farm,
    onClick: () -> Unit,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PrimaryGreen,
                                    PrimaryGreen.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Current Farm",
                        fontSize = 11.sp,
                        color = textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        selectedFarm.name.ifBlank { "Select Farm" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (selectedFarm.location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                selectedFarm.location,
                                fontSize = 12.sp,
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Change farm",
                tint = PrimaryGreen,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ===============================================================
// FARM SELECTION DIALOG
// ===============================================================
@Composable
private fun FarmSelectionDialog(
    farms: List<Farm>,
    selectedFarm: Farm,
    onDismiss: () -> Unit,
    onFarmSelected: (Farm) -> Unit,
    navController: NavController,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Select Farm",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = textPrimary
                        )
                        Text(
                            "${farms.size} farm${if (farms.size != 1) "s" else ""} available",
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = textSecondary.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        onDismiss()
                        navController.navigate(Screen.AddFarm.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Farm", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    farms.forEach { farm ->
                        val isSelected = farm.id == selectedFarm.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFarmSelected(farm) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    PrimaryGreen.copy(alpha = 0.15f)
                                else
                                    cardColor
                            ),
                            border = if (isSelected) BorderStroke(2.dp, PrimaryGreen) else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Agriculture,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        farm.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${farm.acres} acres • ${farm.cropType.take(20)}",
                                        fontSize = 13.sp,
                                        color = textSecondary,
                                        modifier = Modifier.padding(top = 2.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (farm.location.isNotBlank()) {
                                        Text(
                                            farm.location,
                                            fontSize = 12.sp,
                                            color = textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(26.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===============================================================
// WEATHER DETAIL DIALOG
// ===============================================================
@Composable
private fun WeatherDetailDialog(
    weather: WeatherInfo,
    location: String,
    onDismiss: () -> Unit,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PrimaryGreen.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textPrimary
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            weatherEmojiFor(weather.condition),
                            fontSize = 72.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "${weather.tempC}°C",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            weather.condition,
                            fontSize = 20.sp,
                            color = textSecondary
                        )
                        Text(
                            location,
                            fontSize = 14.sp,
                            color = textSecondary.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        "Current Conditions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textPrimary
                    )
                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherDetailCard(
                                icon = Icons.Outlined.Thermostat,
                                title = "Feels Like",
                                value = "${weather.feelsLike}°C",
                                modifier = Modifier.weight(1f),
                                cardColor = cardColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            WeatherDetailCard(
                                icon = Icons.Outlined.WaterDrop,
                                title = "Humidity",
                                value = "${weather.humidity}%",
                                modifier = Modifier.weight(1f),
                                cardColor = cardColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherDetailCard(
                                icon = Icons.Outlined.Air,
                                title = "Wind",
                                value = "${weather.windKph} km/h",
                                modifier = Modifier.weight(1f),
                                cardColor = cardColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            WeatherDetailCard(
                                icon = Icons.Outlined.Visibility,
                                title = "Visibility",
                                value = "${weather.visibility} km",
                                modifier = Modifier.weight(1f),
                                cardColor = cardColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherDetailCard(
                                icon = Icons.Outlined.Compress,
                                title = "Pressure",
                                value = "${weather.pressure} mb",
                                modifier = Modifier.weight(1f),
                                cardColor = cardColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            WeatherDetailCard(
                                icon = Icons.Outlined.WaterDrop,
                                title = "Dew Point",
                                value = "${weather.dewPoint}°C",
                                modifier = Modifier.weight(1f),
                                cardColor = cardColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WeatherDetailCard(
                                icon = Icons.Outlined.WbSunny,
                                title = "UV Index",
                                value = weather.uvIndex.toString(),
                                modifier = Modifier.weight(1f),
                                cardColor = cardColor,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                            Box(modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (weather.daily.isNotEmpty()) {
                        Text(
                            "7-Day Forecast",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textPrimary
                        )
                        Spacer(Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            weather.daily.forEach { day ->
                                DailyForecastRow(
                                    day = day,
                                    cardColor = cardColor,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )

            Column {
                Text(
                    value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    title,
                    fontSize = 11.sp,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    day: DailyForecast,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    day.label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${day.rainChance}%",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                }
            }

            Text(
                weatherEmojiFor(day.condition),
                fontSize = 32.sp
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${day.maxTempC}°",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    "${day.minTempC}°",
                    fontSize = 14.sp,
                    color = textSecondary
                )
            }
        }
    }
}

// Continue with remaining components in next message...
// ===============================================================
// NODE SENSORS SECTION - FROM FIREBASE
// ===============================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
NodeSensorsSection(
nodes = sensorNodes,
cardColor = cardColor,
textPrimary = textPrimary,
textSecondary = textSecondary,
onSyncAll = { dashboardViewModel.reloadAll() },
onSyncNode = { dashboardViewModel.reloadAll() },
onDemoSoilUpdate = { updatedMap ->
    demoSoilParams = updatedMap
},onDemoVersionBump = {
    demoVersion++
}
)
 {
    val scope = rememberCoroutineScope()
    val syncingAll = remember { mutableStateOf(false) }
    val syncingById = remember { mutableStateMapOf<String, Boolean>() }

    val uiNodesRaw = remember(nodes) {
        nodes.map { it.toSoilNodeUi() }
    }

    // If no nodes from DB, show a demo node so UI is never empty
    val demoNodeState = remember {
        mutableStateOf(randomDemoNodeUi())
    }

    val uiNodes = remember(uiNodesRaw, demoNodeState.value) {
        if (uiNodesRaw.isEmpty()) listOf(demoNodeState.value) else uiNodesRaw
    }

    val averageParams = remember(uiNodes) { computeAverageParams(uiNodes) }
    val pageCount = 1 + uiNodes.size
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Column {
        Text("Soil Nodes", fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))

        if (nodes.isEmpty()) {
            Text(
                "No live nodes found. Showing demo values until ESP sends data.",
                color = textSecondary,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(6.dp))
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->

            val isAveragePage = page == 0

            val title: String
            val params: List<SoilParam>
            val isSyncing: Boolean
            val onSync: () -> Unit

            if (isAveragePage) {
                title = "All nodes • Average"
                params = averageParams
                isSyncing = syncingAll.value
                onSync = {
                    if (!syncingAll.value) {
                        syncingAll.value = true
                        scope.launch {
                            if (nodes.isEmpty()) {
                                demoNodeState.value = randomDemoNodeUi()
                            } else {
                                onSyncAll()
                            }
                            delay(700)
                            syncingAll.value = false
                        }
                    }
                }

            }
            } else {
                val node = uiNodes[page - 1]
                title = node.title
                params = node.params
                val nodeSyncing = syncingById[node.id] == true
                isSyncing = nodeSyncing
                onSync = {
                    if (syncingById[node.id] != true) {
                        syncingById[node.id] = true
                        scope.launch {
                            onSyncNode(node.id)
                            delay(700)
                            syncingById[node.id] = false
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                NodeSensorCardWithCircles(
                    title = title,
                    params = params,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isSyncing = isSyncing,
                    onSync = onSync
                )
            }
        }
    }
}

private fun SensorNode.toSoilNodeUi(): SoilNodeUi {

    val params = mutableListOf<SoilParam>()

    moisture?.let {
        params.add(
            SoilParam("Moisture", "${it.roundToInt()}%", it.toFloat())
        )
    }

    temperature?.let {
        params.add(
            SoilParam("Soil Temp", "${it.roundToInt()}°C", it.toFloat())
        )
    }

    n?.let {
        params.add(
            SoilParam("Nitrogen", "${it.roundToInt()} mg/kg", it.toFloat())
        )
    }

    p?.let {
        params.add(
            SoilParam("Phosphorus", "${it.roundToInt()} mg/kg", it.toFloat())
        )
    }

    k?.let {
        params.add(
            SoilParam("Potassium", "${it.roundToInt()} mg/kg", it.toFloat())
        )
    }

    ph?.let {
        params.add(
            SoilParam("pH", String.format("%.1f", it), it.toFloat())
        )
    }

    ec?.let {
        params.add(
            SoilParam("EC", String.format("%.1f", it), it.toFloat())
        )
    }

    return SoilNodeUi(
        id = id,
        title = "Node $id",
        params = params
    )
}
@Composable
fun NodeSensorCardWithCircles(
    title: String,
    params: List<SoilParam>,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    isSyncing: Boolean,
    onSync: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Live from field", fontSize = 11.sp, color = textSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryGreen
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Syncing...", fontSize = 11.sp, color = textSecondary)
                    } else {
                        Text(
                            "Sync",
                            fontSize = 12.sp,
                            color = PrimaryGreen,
                            modifier = Modifier
                                .clickable { onSync() }
                                .padding(4.dp)
                        )
                        IconButton(onClick = onSync, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = "Sync node",
                                tint = PrimaryGreen
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (params.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No sensor values available for this node.",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                val rows = params.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { rowParams ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowParams.forEach { param ->
                                SingleSensorCircle(param, selectedCrop)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SingleSensorCircle(param: SoilParam) {
    val progress = when (param.name) {
        "Moisture" -> param.numericValue / 100f
        "pH" -> param.numericValue / 14f
        "EC" -> param.numericValue / 5f
        "Soil Temp" -> param.numericValue / 40f
        else -> param.numericValue / 100f
    }.coerceIn(0f, 1f)

    val color = soilStatusColor(param.name, param.numericValue)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = color.copy(alpha = 0.18f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 11f)
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 11f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    param.displayValue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = color
                )
            }
        }
        Text(param.name, fontSize = 10.sp, color = Color.Gray)
    }
}

private fun computeAverageParams(nodes: List<SoilNodeUi>): List<SoilParam> {
    if (nodes.isEmpty()) return emptyList()

    val grouped = LinkedHashMap<String, MutableList<SoilParam>>()
    nodes.forEach { node ->
        node.params.forEach { param ->
            val list = grouped.getOrPut(param.name) { mutableListOf() }
            list.add(param)
        }
    }

    return grouped.map { (name, list) ->
        val avgValue = list.map { it.numericValue }.average().toFloat()
        val display = when (name) {
            "Moisture" -> "${avgValue.roundToInt()}%"
            "pH" -> String.format(Locale.getDefault(), "%.1f", avgValue)
            "EC" -> String.format(Locale.getDefault(), "%.1f", avgValue)
            "Nitrogen" -> "${avgValue.roundToInt()} mg/kg"
            "Phosphorus" -> "${avgValue.roundToInt()} mg/kg"
            "Potassium" -> "${avgValue.roundToInt()} mg/kg"
            "Soil Temp" -> "${avgValue.roundToInt()}°C"
            else -> list.firstOrNull()?.displayValue ?: ""
        }
        SoilParam(name = name, displayValue = display, numericValue = avgValue)
    }
}

private fun soilStatusColor(name: String, value: Float): Color {
    return when (name) {
        "Moisture" -> when {
            value < 30 -> Color(0xFFE53935)
            value < 50 -> Color(0xFFFFA000)
            else -> PrimaryGreen
        }
        "pH" -> when {
            value < 5.5f || value > 8f -> Color(0xFFE53935)
            value < 6f || value > 7.5f -> Color(0xFFFFA000)
            else -> PrimaryGreen
        }
        "EC" -> when {
            value > 4f -> Color(0xFFE53935)
            value > 2f -> Color(0xFFFFA000)
            else -> PrimaryGreen
        }
        "Nitrogen", "Phosphorus", "Potassium" -> Color(0xFFFFA000)
        "Soil Temp" -> when {
            value < 10 || value > 35 -> Color(0xFFE53935)
            value < 15 || value > 30 -> Color(0xFFFFA000)
            else -> PrimaryGreen
        }
        else -> PrimaryGreen
    }
}

// ===============================================================
// SYSTEM STATUS GRID
// ===============================================================
@Composable
@Composable
fun SystemStatusGrid(
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    isPumpOn: Boolean,
    isPumpUpdating: Boolean,
    tds: Int,
    tank: Int,
    rain: Int,
    onPumpToggleRequest: (Boolean) -> Unit
)
{
    Column {
        Text("Farm Systems", fontWeight = FontWeight.Bold, color = textPrimary)
        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SystemTile(
                    title = "TDS",
                    subtitle = "$tds ppm",
                            status = "Good",
                    icon = Icons.Default.InvertColors,
                    tint = PrimaryGreen,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                )
                SystemTile(
                    title = "Rain sensor",
                    subtitle = if (rain == 1) "Rain detected" else "No rain",
                    status = "Dry",
                    icon = Icons.Default.Umbrella,
                    tint = Color(0xFF0288D1),
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PumpStatusTile(
                    title = "Pump status",
                    isOn = isPumpOn,
                    isUpdating = isPumpUpdating,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onToggle = { desired -> onPumpToggleRequest(desired) },
                    modifier = Modifier.weight(1f)
                )
                SystemTile(
                    title = "Tank level",
                    subtitle = "$tank%",
                    status = "Enough water",
                    icon = Icons.Default.Opacity,
                    tint = PrimaryGreen,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SystemTile(
                    title = "Solar charging",
                    subtitle = "Charging",
                    status = "1.4 kW",
                    icon = Icons.Default.Bolt,
                    tint = Color(0xFFFFC107),
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                )
                SystemTile(
                    title = "Irrigation",
                    subtitle = "Next in 2 hrs",
                    status = "Auto mode",
                    icon = Icons.Default.Water,
                    tint = PrimaryGreen,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SystemTile(
    title: String,
    subtitle: String,
    status: String,
    icon: ImageVector,
    tint: Color,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    Text(subtitle, fontSize = 11.sp, color = textSecondary)
                }
            }
            Text(status, fontSize = 11.sp, color = tint, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PumpStatusTile(
    title: String,
    isOn: Boolean,
    isUpdating: Boolean,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = when {
        isUpdating -> Color(0xFFFFA000)
        isOn -> PrimaryGreen
        else -> Color(0xFFE53935)
    }

    val subtitle = when {
        isUpdating -> "Updating..."
        isOn -> "ON"
        else -> "OFF"
    }

    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(enabled = !isUpdating) { onToggle(!isOn) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.WaterDamage, contentDescription = null, tint = tint)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    Text(subtitle, fontSize = 11.sp, color = textSecondary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = tint
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Updating", fontSize = 11.sp, color = tint)
                } else {
                    Switch(
                        checked = isOn,
                        onCheckedChange = { desired -> onToggle(desired) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE53935)
                        )
                    )
                }
            }
        }
    }
}

// ===============================================================
// ENHANCED AI RECOMMENDATIONS
// ===============================================================
@Composable
private fun EnhancedAiRecommendations(
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    recommendations: AiRecommendations,
    navController: NavController,
    weatherInfo: WeatherInfo?
) {
    Column {
        Text("AI Insights & Recommendations", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // AI Alert
            if (recommendations.alertLine != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "AI Alert",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0A0F0B)
                            )
                            Text(
                                recommendations.alertLine,
                                fontSize = 13.sp,
                                color = Color(0xFF424242),
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Crop Suggestions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Grass,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Crop Suggestions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    if (recommendations.cropSuggestions.isEmpty()) {
                        Text(
                            "Waiting for sensor data to provide crop recommendations",
                            fontSize = 13.sp,
                            color = textSecondary,
                            lineHeight = 18.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recommendations.cropSuggestions.forEach { crop ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryGreen)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        crop,
                                        fontSize = 14.sp,
                                        color = textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "✓ Best suited for current soil & weather conditions",
                            fontSize = 12.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Fertilizer Recommendation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Eco,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Fertilizer Recommendation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    if (recommendations.fertiliserPlanLines.isEmpty()) {
                        Text(
                            "Soil nutrients are balanced. Continue regular monitoring.",
                            fontSize = 13.sp,
                            color = textSecondary,
                            lineHeight = 18.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            recommendations.fertiliserPlanLines.forEach { line ->
                                Text(
                                    "• $line",
                                    fontSize = 13.sp,
                                    color = textPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Weather-based Farming Tips
            if (weatherInfo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "Today's Farming Tips",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0A0F0B)
                            )
                            Spacer(Modifier.height(6.dp))

                            val tips = getWeatherBasedTips(weatherInfo)
                            tips.forEach { tip ->
                                Text(
                                    "• $tip",
                                    fontSize = 13.sp,
                                    color = Color(0xFF424242),
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getWeatherBasedTips(weather: WeatherInfo): List<String> {
    val tips = mutableListOf<String>()

    if (weather.tempC > 35) {
        tips.add("High temperature detected. Schedule irrigation for evening hours.")
    }
    if (weather.humidity < 40) {
        tips.add("Low humidity. Increase irrigation frequency to prevent soil drying.")
    } else if (weather.humidity > 80) {
        tips.add("High humidity. Monitor for fungal diseases in crops.")
    }
    if (weather.windKph > 30) {
        tips.add("Strong winds expected. Secure loose items and check crop support systems.")
    }

    weather.daily.firstOrNull()?.let { today ->
        if (today.rainChance > 70) {
            tips.add("High chance of rain (${today.rainChance}%). Delay fertilizer application.")
        }
    }

    if (tips.isEmpty()) {
        tips.add("Weather conditions are favorable for normal farming operations.")
    }

    return tips
}

// ===============================================================
// LOG HISTORY BAR GRAPH
// ===============================================================
@Composable
fun LogHistoryBarGraph(
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    navController: NavController
) {
    val values = listOf(45, 68, 55, 72, 60, 78, 65)
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.Logs.route) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Logs & history", fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Last 7 days moisture events", fontSize = 11.sp, color = textSecondary)
                }
                Icon(Icons.Default.History, contentDescription = null, tint = PrimaryGreen)
            }
            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                values.forEachIndexed { index, v ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .height((v * 1.2).dp)
                                .width(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryGreen)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(days[index], fontSize = 10.sp, color = textSecondary)
                    }
                }
            }
        }
    }
}

// ===============================================================
// QUICK MODULE BUTTONS
// ===============================================================
@Composable
fun QuickModuleButtons(
    navController: NavController,
    textPrimary: Color,
    textSecondary: Color
) {
    Column {
        Text("Quick modules", fontWeight = FontWeight.Bold, color = textPrimary)
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickButton("Disease", Icons.Default.BugReport, textSecondary) {
                navController.navigate(Screen.DiseaseDetection.route)
            }
            QuickButton("Rover", Icons.Default.Videocam, textSecondary) {
                navController.navigate(Screen.Rover.route)
            }
            QuickButton("Market", Icons.Default.AttachMoney, textSecondary) {
                navController.navigate(Screen.MarketPrice.route)
            }
            QuickButton("Logs", Icons.Default.History, textSecondary) {
                navController.navigate(Screen.Logs.route)
            }
            QuickButton("Chat", Icons.Default.SupportAgent, textSecondary) {
                navController.navigate(Screen.KrishiMitri.route)
            }
        }
    }
}

@Composable
fun QuickButton(
    label: String,
    icon: ImageVector,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(AccentGreenSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryGreen)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = textColor)
    }
}

// ===============================================================
// CHAT POPUP & ANIMATED FAB
// ===============================================================
@Composable
fun ChatPopup(
    navController: NavController,
    onClosePopup: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Dialog(
        onDismissRequest = {
            visible = false
            onClosePopup()
        },
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            color = Color(0xFF050908).copy(alpha = 0.92f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.96f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Krishi AI Assistant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row {
                        IconButton(onClick = {
                            visible = false
                            onClosePopup()
                            navController.navigate(Screen.KrishiMitri.route)
                        }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White)
                        }
                        IconButton(onClick = {
                            visible = false
                            onClosePopup()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.2f))

                Box(modifier = Modifier.fillMaxSize()) {
                    KrishiMitriChatScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp)
                    )

                    AnimatedChatbotAvatar(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedChatbotFab(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "chatbotFab")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabScale"
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = Color.White,
        modifier = Modifier.scale(scale)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bot),
            contentDescription = "Chatbot",
            modifier = Modifier.size(52.dp)
        )
    }
}

@Composable
fun AnimatedChatbotAvatar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "chatbotAvatar")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarBob"
    )

    Box(modifier = modifier.offset(y = bobOffset.dp)) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF1B5E20),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Ask Krishi AI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ===============================================================
// WEATHER FETCH - CURRENT + 5-DAY FORECAST
// ===============================================================
private suspend fun fetchWeatherForLocation(
    locationName: String,
    lat: Double,
    lon: Double
): WeatherInfo? = withContext(Dispatchers.IO) {
    try {
        val useLatLon = lat != 0.0 && lon != 0.0
        val safeLocation = if (locationName.isBlank()) "Delhi,IN" else locationName
        val encodedLocation = URLEncoder.encode(safeLocation, "UTF-8")

        val currentUrl = if (useLatLon) {
            URL("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&appid=$OPEN_WEATHER_API_KEY")
        } else {
            URL("https://api.openweathermap.org/data/2.5/weather?q=$encodedLocation&units=metric&appid=$OPEN_WEATHER_API_KEY")
        }

        val currentConn = currentUrl.openConnection() as HttpURLConnection
        currentConn.requestMethod = "GET"
        currentConn.connectTimeout = 10000
        currentConn.readTimeout = 10000

        if (currentConn.responseCode != HttpURLConnection.HTTP_OK) {
            currentConn.disconnect()
            return@withContext null
        }

        val currentBody = currentConn.inputStream.bufferedReader().use { it.readText() }
        currentConn.disconnect()

        val currentJson = JSONObject(currentBody)
        val main = currentJson.getJSONObject("main")
        val weatherArray = currentJson.getJSONArray("weather")
        val weatherObj = weatherArray.getJSONObject(0)
        val windObj = currentJson.optJSONObject("wind") ?: JSONObject()

        val temp = main.getDouble("temp").toFloat().roundToInt()
        val feelsLike = main.getDouble("feels_like").toFloat().roundToInt()
        val humidity = main.getInt("humidity")
        val pressure = main.getInt("pressure")
        val windMs = windObj.optDouble("speed", 0.0)
        val windKph = (windMs * 3.6).roundToInt()
        val condition = weatherObj.getString("main")
        val visibility = currentJson.optDouble("visibility", 10000.0) / 1000.0

        // ---- 5-day 3-hour forecast ----
        val forecastUrl = if (useLatLon) {
            URL("https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&units=metric&appid=$OPEN_WEATHER_API_KEY")
        } else {
            URL("https://api.openweathermap.org/data/2.5/forecast?q=$encodedLocation&units=metric&appid=$OPEN_WEATHER_API_KEY")
        }

        val forecastConn = forecastUrl.openConnection() as HttpURLConnection
        forecastConn.requestMethod = "GET"
        forecastConn.connectTimeout = 10000
        forecastConn.readTimeout = 10000

        if (forecastConn.responseCode != HttpURLConnection.HTTP_OK) {
            forecastConn.disconnect()
            return@withContext WeatherInfo(
                condition = condition,
                tempC = temp,
                humidity = humidity,
                windKph = windKph,
                pressure = pressure,
                visibility = visibility,
                feelsLike = feelsLike,
                dewPoint = temp - ((100 - humidity) / 5),
                daily = emptyList()
            )
        }

        val forecastBody = forecastConn.inputStream.bufferedReader().use { it.readText() }
        forecastConn.disconnect()

        val forecastJson = JSONObject(forecastBody)
        val listArray = forecastJson.getJSONArray("list")

        val byDate = linkedMapOf<String, MutableList<JSONObject>>()
        for (i in 0 until listArray.length()) {
            val item = listArray.getJSONObject(i)
            val dtText = item.getString("dt_txt") // "2025-02-19 12:00:00"
            val dateKey = dtText.substring(0, 10) // "2025-02-19"
            val list = byDate.getOrPut(dateKey) { mutableListOf() }
            list.add(item)
        }

        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("EEE", Locale.getDefault())

        val dailyForecasts = mutableListOf<DailyForecast>()
        byDate.entries.take(5).forEachIndexed { index, (dateKey, items) ->
            var minT = Float.MAX_VALUE
            var maxT = -Float.MAX_VALUE
            var rainSum = 0.0
            var rainCount = 0
            var conditionName = "Clear"

            items.forEach { obj ->
                val mainObj = obj.getJSONObject("main")
                val t = mainObj.getDouble("temp").toFloat()
                if (t < minT) minT = t
                if (t > maxT) maxT = t

                val weatherArr = obj.getJSONArray("weather")
                val wObj = weatherArr.getJSONObject(0)
                conditionName = wObj.getString("main")

                val pop = obj.optDouble("pop", 0.0)
                rainSum += pop
                rainCount++
            }

            val rainChance = if (rainCount > 0) ((rainSum / rainCount) * 100).roundToInt() else 0

            val label = when (index) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> {
                    try {
                        val date = sdfIn.parse(dateKey)
                        if (date != null) sdfOut.format(date) else dateKey
                    } catch (e: Exception) {
                        dateKey
                    }
                }
            }

            dailyForecasts += DailyForecast(
                label = label,
                minTempC = minT.roundToInt(),
                maxTempC = maxT.roundToInt(),
                condition = conditionName,
                rainChance = rainChance
            )
        }

        WeatherInfo(
            condition = condition,
            tempC = temp,
            humidity = humidity,
            windKph = windKph,
            pressure = pressure,
            visibility = visibility,
            uvIndex = 0,
            dewPoint = temp - ((100 - humidity) / 5),
            feelsLike = feelsLike,
            daily = dailyForecasts
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ===============================================================
// AI LOGIC - CROPS & FERTILISER BASED ON NODES + WEATHER
// ===============================================================


    // Average values
    var moistureSum = 0f
    var moistureCount = 0
    var nitrogenSum = 0f
    var nitrogenCount = 0
    var phSum = 0f
    var phCount = 0
    var tempSoilSum = 0f
    var tempSoilCount = 0

    nodes.forEach { n ->
        n.moisture?.let { moistureSum += it; moistureCount++ }
        n.nitrogen?.let { nitrogenSum += it; nitrogenCount++ }
        n.ph?.let { phSum += it; phCount++ }
        n.temp?.let { tempSoilSum += it; tempSoilCount++ }
    }

    val avgMoisture = if (moistureCount > 0) moistureSum / moistureCount else null
    val avgNitrogen = if (nitrogenCount > 0) nitrogenSum / nitrogenCount else null
    val avgPh = if (phCount > 0) phSum / phCount else null
    val avgSoilTemp = if (tempSoilCount > 0) tempSoilSum / tempSoilCount else null
    val airTemp = weather?.tempC?.toFloat()
    val tempForCrop = airTemp ?: avgSoilTemp ?: 25f

    // ------- Crop Suggestions (Indian style) -------
    val crops = mutableListOf<String>()
    if (avgMoisture != null && tempForCrop > 26f && avgMoisture > 60f) {
        crops += "Paddy"
        crops += "Sugarcane"
    }
    if (tempForCrop in 18f..28f && (avgMoisture ?: 40f) in 35f..60f) {
        crops += "Wheat"
        crops += "Maize"
        crops += "Mustard"
    }
    if (avgPh != null && avgPh in 6f..7.5f && (avgMoisture ?: 40f) in 30f..60f) {
        crops += "Potato"
        crops += "Vegetable mix"
    }
    if (crops.isEmpty()) {
        crops += "Pulses"
        crops += "Millets"
    }

    // ------- Fertiliser Plan (kg/acre) -------
    val fertLines = mutableListOf<String>()
    if (avgNitrogen != null) {
        when {
            avgNitrogen < 40f -> fertLines += "Nitrogen low. Apply Urea 40–50 kg/acre (2 splits: 50% basal, 50% at 25–30 days)."
            avgNitrogen in 40f..80f -> fertLines += "Nitrogen moderate. Maintenance dose: Urea 20–25 kg/acre at active tillering/vegetative stage."
            else -> fertLines += "Nitrogen high. Avoid extra Urea now; focus on organic manures only."
        }
    }

    avgPh?.let {
        if (it < 6f) {
            fertLines += "Soil slightly acidic. Add 1–1.5 qtl/acre agricultural lime once in 2–3 years."
        } else if (it > 7.8f) {
            fertLines += "Soil alkaline. Prefer gypsum + organic compost; avoid sodic/alkaline irrigation water."
        }
    }

    avgMoisture?.let {
        when {
            it < 30f -> fertLines += "Soil moisture low. Light irrigation before fertiliser improves uptake."
            it > 70f -> fertLines += "Soil moisture high. Avoid heavy nitrogen dose in near-waterlogged condition."
        }
    }

    if (fertLines.isEmpty()) {
        fertLines += "Current soil status looks balanced. Maintain 1–2 ton/acre FYM/compost every year."
    }

    // ------- Alert line -------
    val alert = when {
        avgMoisture != null && avgMoisture < 25f -> "Soil moisture is very low. Plan irrigation within next 12–24 hours with light to moderate watering."
        avgMoisture != null && avgMoisture > 75f -> "Soil is near waterlogged. Avoid irrigation and nitrogen fertiliser until moisture reduces."
        avgNitrogen != null && avgNitrogen < 40f -> "Nitrogen is slightly low. Schedule Urea application together with next irrigation."
        else -> "Conditions are generally stable. Continue normal field operations and monitor sensors regularly."
    }

    return AiRecommendations(
        alertLine = alert,
        cropSuggestions = crops.distinct(),
        fertiliserPlanLines = fertLines
    )
}
