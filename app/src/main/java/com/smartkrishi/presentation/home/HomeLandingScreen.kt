package com.smartkrishi.presentation.home

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.smartkrishi.R
import com.smartkrishi.presentation.model.Farm
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════
//  COLORS
// ═══════════════════════════════════════════════════════════
private val PrimaryGreen   = Color(0xFF1B5E20)
private val AccentGreen    = Color(0xFF1A411D)
private val DarkGreen      = Color(0xFF1B4332)
private val PageBg         = Color(0xFFF4F1EB)
private val CardBackground = Color(0xFFFFFFFF)
private val ErrorRed       = Color(0xFFD32F2F)
private val AlertOrange    = Color(0xFFE65100)
private val IrrigatingTeal = Color(0xFF00796B)
private val HealthyGreen   = Color(0xFF2E7D32)
private val WarningAmber   = Color(0xFFFF8F00)

// ═══════════════════════════════════════════════════════════
//  STATUS
// ═══════════════════════════════════════════════════════════
private fun statusColor(status: String): Color = when (status.uppercase()) {
    "HEALTHY"    -> HealthyGreen
    "IRRIGATING" -> IrrigatingTeal
    "ALERT"      -> AlertOrange
    "WARNING"    -> WarningAmber
    "FERTILISER",
    "FERTILIZER" -> Color(0xFF6A1B9A)
    else         -> Color(0xFF616161)
}

private fun statusEmoji(status: String): String = when (status.uppercase()) {
    "HEALTHY"              -> "✅"
    "IRRIGATING"           -> "💧"
    "ALERT"                -> "🚨"
    "WARNING"              -> "⚠️"
    "FERTILISER","FERTILIZER" -> "🌿"
    else                   -> "ℹ️"
}

private fun farmStatusLabel(farm: Farm): String =
    farm.status?.uppercase()?.trim()?.ifBlank { null } ?: "HEALTHY"

// ═══════════════════════════════════════════════════════════
//  ENUMS
// ═══════════════════════════════════════════════════════════
enum class StatType { FARMS, ACRES, CROPS }

private enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val route: String
) {
    HOME("Home",       Icons.Outlined.Home,          Icons.Filled.Home,          "home_landing"),
    DISEASE("Detect",  Icons.Outlined.LocalFlorist,  Icons.Filled.LocalFlorist,  "disease_detection"),
    CHAT("Chat",       Icons.Outlined.Message,       Icons.Filled.Message,       "faq"),
    PROFILE("Profile", Icons.Outlined.Person,        Icons.Filled.Person,        "profile")
}

private data class DrawerMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val route: String,
    val color: Color = PrimaryGreen,
    val badge: String? = null
)

// ═══════════════════════════════════════════════════════════
//  HELPERS
// ═══════════════════════════════════════════════════════════
private fun String.cleanCropType(): String =
    this.removePrefix("🌾 ").removePrefix("🍚 ").removePrefix("🌽 ")
        .removePrefix("🌿 ").removePrefix("🎋 ").removePrefix("🫘 ")
        .removePrefix("🥔 ").removePrefix("🍅 ").removePrefix("🥬 ")
        .removePrefix("🌻 ").removePrefix("🫑 ").removePrefix("🧅 ")
        .removePrefix("🥕 ").removePrefix("🫛 ").removePrefix("🌱 ").trim()

private fun getCropEmoji(cropType: String): String = when {
    cropType.contains("wheat",     ignoreCase = true) -> "🌾"
    cropType.contains("rice",      ignoreCase = true) -> "🍚"
    cropType.contains("corn",      ignoreCase = true) ||
            cropType.contains("maize",     ignoreCase = true) -> "🌽"
    cropType.contains("sugarcane", ignoreCase = true) -> "🎋"
    cropType.contains("cotton",    ignoreCase = true) -> "🌿"
    cropType.contains("potato",    ignoreCase = true) -> "🥔"
    cropType.contains("tomato",    ignoreCase = true) -> "🍅"
    cropType.contains("cabbage",   ignoreCase = true) -> "🥬"
    cropType.contains("sunflower", ignoreCase = true) -> "🌻"
    cropType.contains("onion",     ignoreCase = true) -> "🧅"
    cropType.contains("carrot",    ignoreCase = true) -> "🥕"
    cropType.contains("pea",       ignoreCase = true) -> "🫛"
    cropType.contains("bean",      ignoreCase = true) -> "🫘"
    else                                              -> "🌱"
}

private fun String.capitalizeFirst(): String =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

// ═══════════════════════════════════════════════════════════
//  MAIN SCREEN
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLandingScreen(
    navController: NavController,
    onOpenFarm: (Farm) -> Unit,
    onAddFarm: () -> Unit,
    onEditFarm: (Farm) -> Unit,
    onDeleteFarm: (Farm) -> Unit,
    onSkip: () -> Unit,
    viewModel: FarmViewModel = hiltViewModel()
) {
    val auth             = FirebaseAuth.getInstance()
    val currentUserEmail = remember { auth.currentUser?.email ?: "" }
    val currentUserName  = remember {
        auth.currentUser?.displayName?.ifBlank { null }
            ?: currentUserEmail.substringBefore("@").capitalizeFirst()
    }

    val farms        by viewModel.farms.collectAsState()
    val selectedFarm by viewModel.selectedFarm.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()

    val userFarms = remember(farms, currentUserEmail) {
        if (currentUserEmail.isBlank()) emptyList()
        else farms.filter { it.userEmail == currentUserEmail }
    }

    val totalAcres       = remember(userFarms) { userFarms.sumOf { it.acres } }
    val uniqueCrops      = remember(userFarms) { userFarms.map { it.cropType }.distinct() }
    val cropDistribution = remember(userFarms) {
        userFarms.groupBy { it.cropType }.mapValues { it.value.size }
    }

    LaunchedEffect(userFarms.size) {
        Log.d("HomeLandingScreen", "Farms loaded: ${userFarms.size}")
    }

    // ─── State ───────────────────────────────────────────────────────
    var activeFarm         by remember { mutableStateOf<Farm?>(null) }
    var showFarmSheet      by remember { mutableStateOf(false) }
    var showDeleteDialog   by remember { mutableStateOf(false) }
    var farmToDelete       by remember { mutableStateOf<Farm?>(null) }
    var showStatsDialog    by remember { mutableStateOf(false) }
    var selectedStatType   by remember { mutableStateOf<StatType?>(null) }
    var selectedBottomItem by remember { mutableStateOf(BottomNavItem.HOME) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    // KEY FIX: Create sheetState at screen level with confirmValueChange = { true }
    // This allows the sheet to always close — no stuck state
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Helper: safely hide sheet then clear state — CRASH FIX
    // We use a simple boolean flag approach instead of sheetState.hide()
    // to avoid the "already disposed" crash
    fun dismissSheet() {
        showFarmSheet = false
        activeFarm    = null
    }

    val drawerMenuItems = listOf(
        DrawerMenuItem(Icons.Outlined.TrendingUp,     "Market Prices",   "Live commodity rates",      "market_price",      Color(0xFF1565C0)),
        DrawerMenuItem(Icons.Outlined.Message,        "Krishi Mitri AI", "Smart farming assistant",   "krishimitri_chat",  Color(0xFF00897B), badge = "AI"),
        DrawerMenuItem(Icons.Outlined.AccountBalance, "Govt Schemes",    "Agricultural programs",     "govt_schemes",      Color(0xFFD84315)),
        DrawerMenuItem(Icons.Outlined.Grass,          "Crop Library",    "Detailed crop information", "crop_listing",      Color(0xFF558B2F)),
        DrawerMenuItem(Icons.Outlined.Build,          "Equipment",       "Farm tools & machinery",    "equipment_listing", Color(0xFF6A1B9A))
    )

    LaunchedEffect(Unit) { viewModel.refreshFarms() }

    // ─── Stats dialog ─────────────────────────────────────────────────
    if (showStatsDialog && selectedStatType != null) {
        StatisticsDetailDialog(
            statType         = selectedStatType!!,
            farms            = userFarms,
            totalAcres       = totalAcres,
            cropDistribution = cropDistribution,
            onDismiss        = { showStatsDialog = false },
            onFarmClick      = { farm ->
                showStatsDialog = false
                activeFarm      = farm
                showFarmSheet   = true
            }
        )
    }

    // ─── Farm action bottom sheet ─────────────────────────────────────
    // CRASH FIX: Only render ModalBottomSheet when showFarmSheet=true AND activeFarm!=null
    // Use key() so Compose creates a brand-new sheet node each time we open a farm
    // This avoids the "Sheet is still animating" / disposed-scope crash entirely
    if (showFarmSheet && activeFarm != null) {
        val currentFarm = activeFarm!! // snapshot into local val — safe to use in callbacks

        ModalBottomSheet(
            onDismissRequest = {
                // CRASH FIX: just flip booleans — do NOT call sheetState.hide() here
                showFarmSheet = false
                activeFarm    = null
            },
            sheetState     = sheetState,
            containerColor = CardBackground,
            shape          = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle     = {
                // Custom drag handle for cleaner look
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp))
                )
            }
        ) {
            // Inline action sheet — no FarmActionSheet composable needed
            // This avoids any mismatch between external sheetState and internal hide() calls
            FarmBottomSheetContent(
                farm = currentFarm,
                onOpenFarm = {
                    showFarmSheet = false
                    activeFarm    = null
                    onOpenFarm(currentFarm)
                },
                onEditFarm = {
                    showFarmSheet = false
                    activeFarm    = null
                    onEditFarm(currentFarm)
                },
                onDeleteFarm = {
                    farmToDelete   = currentFarm
                    showFarmSheet  = false
                    activeFarm     = null
                    showDeleteDialog = true
                }
            )
        }
    }

    // ─── Delete confirmation dialog ───────────────────────────────────
    if (showDeleteDialog && farmToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; farmToDelete = null },
            icon = {
                Box(
                    modifier         = Modifier
                        .size(64.dp)
                        .background(ErrorRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Warning,
                        contentDescription = null,
                        tint               = ErrorRed,
                        modifier           = Modifier.size(34.dp)
                    )
                }
            },
            title = {
                Text("Delete Farm?", fontWeight = FontWeight.ExtraBold, color = Color(0xFF212121), fontSize = 20.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("You are about to permanently delete:", fontSize = 14.sp, color = Color(0xFF616161))
                    Surface(shape = RoundedCornerShape(12.dp), color = PrimaryGreen.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "\"${farmToDelete!!.name}\"",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            color      = PrimaryGreen,
                            modifier   = Modifier.padding(14.dp)
                        )
                    }
                    Text(
                        "⚠️ This action cannot be undone.",
                        fontSize   = 13.sp,
                        color      = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFarm(farmToDelete!!)
                        farmToDelete     = null
                        showDeleteDialog = false
                    },
                    colors   = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick  = { showDeleteDialog = false; farmToDelete = null },
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(1.5.dp, PrimaryGreen),
                    modifier = Modifier.height(46.dp)
                ) {
                    Text("Cancel", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            shape          = RoundedCornerShape(20.dp),
            containerColor = CardBackground
        )
    }

    // ─── Root layout ──────────────────────────────────────────────────
    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            DrawerContent(
                navController    = navController,
                drawerState      = drawerState,
                currentUserName  = currentUserName,
                currentUserEmail = currentUserEmail,
                drawerMenuItems  = drawerMenuItems,
                scope            = scope
            )
        }
    ) {
        Scaffold(
            containerColor = PageBg,
            topBar = {
                TopBar(
                    drawerState = drawerState,
                    scope       = scope,
                    onRefresh   = { viewModel.refreshFarms() }
                )
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(PageBg)
            ) {
                // ── Main scroll content ──────────────────────────
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {

                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Welcome back, ", fontSize = 18.sp, color = Color(0xFF555555))
                            Text(currentUserName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkGreen)
                            Text(" 👋", fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(18.dp))
                    }

                    if (userFarms.isNotEmpty()) {
                        item {
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatPillChip("🌿", "${userFarms.size} ${if (userFarms.size == 1) "Farm" else "Farms"}", Color(0xFFDEF0DE), DarkGreen, Modifier.weight(1f)) {
                                    selectedStatType = StatType.FARMS; showStatsDialog = true
                                }
                                StatPillChip("📐", "$totalAcres acres", Color(0xFFFFF3DC), Color(0xFF5D4037), Modifier.weight(1f)) {
                                    selectedStatType = StatType.ACRES; showStatsDialog = true
                                }
                                StatPillChip("🌱", "Crops ${uniqueCrops.size}", Color(0xFFDEF0DE), DarkGreen, Modifier.weight(1f)) {
                                    selectedStatType = StatType.CROPS; showStatsDialog = true
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    when {
                        isLoading -> item {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(top = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = AccentGreen, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(14.dp))
                                    Text("Loading your farms...", fontSize = 14.sp, color = Color(0xFF888888))
                                }
                            }
                        }

                        userFarms.isEmpty() -> item {
                            EmptyFarmState(onAddFarm = onAddFarm)
                        }

                        else -> {
                            item {
                                Row(
                                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("My Farms", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A), letterSpacing = (-0.5).sp)
                                        Text("${userFarms.size} active ${if (userFarms.size == 1) "farm" else "farms"}", fontSize = 12.sp, color = Color(0xFF888888))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(DarkGreen.copy(alpha = 0.1f), CircleShape)
                                            .clickable(remember { MutableInteractionSource() }, null) { onSkip() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(17.dp))
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                            }

                            items(userFarms, key = { it.id }) { farm ->
                                FarmCard(
                                    farm       = farm,
                                    isSelected = farm.id == selectedFarm?.id,
                                    onClick    = {
                                        activeFarm    = farm
                                        showFarmSheet = true
                                    }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // ── Floating pill nav bar ─────────────────────────
                PillNavBar(
                    modifier           = Modifier.align(Alignment.BottomCenter),
                    selectedItem       = selectedBottomItem,
                    onItemSelected     = { item ->
                        selectedBottomItem = item
                        if (item != BottomNavItem.HOME) {
                            navController.navigate(item.route) { launchSingleTop = true }
                        }
                    },
                    onAddFarm          = onAddFarm
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  FARM BOTTOM SHEET CONTENT
//  (standalone composable — no external FarmActionSheet dependency
//   that could call sheetState.hide() and cause crashes)
// ═══════════════════════════════════════════════════════════
@Composable
private fun FarmBottomSheetContent(
    farm: Farm,
    onOpenFarm: () -> Unit,
    onEditFarm: () -> Unit,
    onDeleteFarm: () -> Unit
) {
    val status = farmStatusLabel(farm)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        // Farm info header
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(getCropEmoji(farm.cropType), fontSize = 28.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(farm.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color(0xFFAAAAAA), modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(farm.location, fontSize = 13.sp, color = Color(0xFF888888))
                }
            }
            Surface(shape = RoundedCornerShape(8.dp), color = statusColor(status)) {
                Text(
                    text     = "${statusEmoji(status)} $status",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color    = Color.White,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Farm details chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF0F0F0)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(getCropEmoji(farm.cropType), fontSize = 13.sp)
                    Spacer(Modifier.width(5.dp))
                    Text(farm.cropType.cleanCropType(), fontSize = 12.sp, color = Color(0xFF444444), fontWeight = FontWeight.SemiBold)
                }
            }
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF0F0F0)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Landscape, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${farm.acres} acres", fontSize = 12.sp, color = Color(0xFF444444), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Status alert card (if not healthy)
        if (status != "HEALTHY") {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = statusColor(status).copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(statusEmoji(status), fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text       = when (status) {
                                "IRRIGATING" -> "Irrigation Active"
                                "ALERT"      -> "Farm Alert"
                                "WARNING"    -> "Attention Needed"
                                "FERTILISER","FERTILIZER" -> "Fertilisation Due"
                                else         -> "Farm Status"
                            },
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = statusColor(status)
                        )
                        Text(
                            text     = when (status) {
                                "IRRIGATING" -> "Irrigation is currently running on this farm."
                                "ALERT"      -> "This farm requires immediate attention."
                                "WARNING"    -> "Please inspect this farm soon."
                                "FERTILISER","FERTILIZER" -> "This farm is due for fertilisation."
                                else         -> "Check farm for more details."
                            },
                            fontSize = 12.sp,
                            color    = Color(0xFF555555),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
        Spacer(Modifier.height(16.dp))

        // Action buttons
        Button(
            onClick  = onOpenFarm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = DarkGreen)
        ) {
            Icon(Icons.Filled.Agriculture, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Open Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick  = onEditFarm,
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                border   = BorderStroke(1.5.dp, DarkGreen)
            ) {
                Text("Edit Farm", color = DarkGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            OutlinedButton(
                onClick  = onDeleteFarm,
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                border   = BorderStroke(1.5.dp, ErrorRed)
            ) {
                Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ═══════════════════════════════════════════════════════════
//  TOP BAR
// ═══════════════════════════════════════════════════════════
@Composable
private fun TopBar(
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PageBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(42.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clickable(remember { MutableInteractionSource() }, null) {
                    scope.launch { drawerState.open() }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = DarkGreen, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SmartKrishi", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DarkGreen, letterSpacing = (-0.5).sp)
            Text("AI-Powered Farming", fontSize = 10.sp, color = Color(0xFF999999), fontWeight = FontWeight.Medium)
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(42.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clickable(remember { MutableInteractionSource() }, null) { onRefresh() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = DarkGreen, modifier = Modifier.size(20.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  FLOATING PILL NAV BAR
// ═══════════════════════════════════════════════════════════
@Composable
private fun PillNavBar(
    modifier: Modifier,
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    onAddFarm: () -> Unit
) {
    val items = BottomNavItem.values()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Pill background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(50.dp), spotColor = Color.Black.copy(alpha = 0.18f))
                .background(Color.White, RoundedCornerShape(50.dp))
        ) {
            Row(
                modifier              = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    if (index == 2) {
                        // Gap for FAB
                        Spacer(Modifier.width(58.dp))
                    }
                    val isSelected = selectedItem == item
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier            = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(remember { MutableInteractionSource() }, null) {
                                onItemSelected(item)
                            }
                    ) {
                        Icon(
                            imageVector        = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.label,
                            tint               = if (isSelected) DarkGreen else Color(0xFFAAAAAA),
                            modifier           = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text       = item.label,
                            fontSize   = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color      = if (isSelected) DarkGreen else Color(0xFFAAAAAA)
                        )
                    }
                }
            }
        }

        // FAB centred, lifted above pill
        FloatingActionButton(
            onClick        = onAddFarm,
            containerColor = DarkGreen,
            contentColor   = Color.White,
            shape          = CircleShape,
            modifier       = Modifier
                .size(54.dp)
                .align(Alignment.Center)
                .offset(y = (-8).dp)
                .shadow(14.dp, CircleShape)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Farm", modifier = Modifier.size(26.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  STAT PILL CHIP
// ═══════════════════════════════════════════════════════════
@Composable
private fun StatPillChip(
    emoji: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier        = modifier.height(46.dp).clickable(remember { MutableInteractionSource() }, null, onClick = onClick),
        shape           = RoundedCornerShape(23.dp),
        color           = bgColor,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier              = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 13.sp)
            Spacer(Modifier.width(5.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  FARM CARD — uniform size for all farms
// ═══════════════════════════════════════════════════════════
@Composable
private fun FarmCard(
    farm: Farm,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val status  = farmStatusLabel(farm)
    val hasAlert = status != "HEALTHY"

    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) DarkGreen else if (hasAlert) statusColor(status).copy(alpha = 0.4f) else Color(0xFFEEEEEE),
        animationSpec = tween(300),
        label         = "border"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(if (hasAlert) 6.dp else 3.dp, RoundedCornerShape(20.dp))
            .clickable(remember { MutableInteractionSource() }, null, onClick = onClick),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (hasAlert) statusColor(status).copy(alpha = 0.04f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(width = if (isSelected || hasAlert) 1.5.dp else 1.dp, color = borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                Box(
                    modifier         = Modifier.size(78.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!farm.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(farm.imageUrl).crossfade(true)
                                .placeholder(R.drawable.farm_placeholder)
                                .error(R.drawable.farm_placeholder).build(),
                            contentDescription = farm.name,
                            modifier           = Modifier.fillMaxSize(),
                            contentScale       = ContentScale.Crop
                        )
                    } else {
                        Text(getCropEmoji(farm.cropType), fontSize = 36.sp)
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp).background(DarkGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = farm.name,
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color(0xFF1A1A1A),
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            modifier   = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(7.dp), color = statusColor(status)) {
                            Text(
                                text          = "${statusEmoji(status)} $status",
                                fontSize      = 8.sp,
                                fontWeight    = FontWeight.ExtraBold,
                                color         = Color.White,
                                letterSpacing = 0.3.sp,
                                modifier      = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(5.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color(0xFFBBBBBB), modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(farm.location, fontSize = 12.sp, color = Color(0xFF888888), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF0F0F0)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(getCropEmoji(farm.cropType), fontSize = 11.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(farm.cropType.cleanCropType(), fontSize = 11.sp, color = Color(0xFF444444), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF0F0F0)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Landscape, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(11.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("${farm.acres} acres", fontSize = 11.sp, color = Color(0xFF444444), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Alert banner at bottom of card (only when not healthy)
            if (hasAlert) {
                Surface(
                    color    = statusColor(status).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(statusEmoji(status), fontSize = 13.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (status) {
                                "IRRIGATING"             -> "Irrigation in progress"
                                "ALERT"                  -> "Immediate attention required"
                                "WARNING"                -> "Inspection recommended"
                                "FERTILISER","FERTILIZER"-> "Fertilisation scheduled"
                                else                     -> "Check farm status"
                            },
                            fontSize   = 12.sp,
                            color      = statusColor(status),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  DRAWER CONTENT — compact user header, no app banner
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    currentUserName: String,
    currentUserEmail: String,
    drawerMenuItems: List<DrawerMenuItem>,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val auth = FirebaseAuth.getInstance()

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFFF9F9F9),
        modifier             = Modifier.width(295.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())
        ) {
            // ── Compact user profile header ───────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1B4332), Color(0xFF2D6A4F), Color(0xFF40916C))
                        )
                    )
                    // Reduced top padding so header is smaller
                    .padding(top = 36.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(54.dp)                               // smaller than before
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF74C69D), Color(0xFF52B788))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = currentUserName.firstOrNull()?.uppercase() ?: "F",
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color.White
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text          = currentUserName,
                            fontSize      = 18.sp,                    // slightly smaller
                            fontWeight    = FontWeight.ExtraBold,
                            color         = Color.White,
                            maxLines      = 1,
                            overflow      = TextOverflow.Ellipsis,
                            letterSpacing = (-0.2).sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text     = currentUserEmail,
                            fontSize = 11.sp,
                            color    = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.18f)) {
                            Row(
                                modifier          = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌾", fontSize = 10.sp)
                                Spacer(Modifier.width(4.dp))
                                Text("Smart Farmer", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "EXPLORE",
                fontSize      = 10.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = Color(0xFFBBBBBB),
                letterSpacing = 1.5.sp,
                modifier      = Modifier.padding(horizontal = 22.dp, vertical = 6.dp)
            )

            drawerMenuItems.forEach { item ->
                DrawerItem(
                    item    = item,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(item.route) { launchSingleTop = true }
                        }
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(12.dp),
                    color           = Color.White,
                    shadowElevation = 2.dp,
                    onClick         = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("profile") { launchSingleTop = true }
                        }
                    }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Settings, contentDescription = null, tint = Color(0xFF666666), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Settings & Profile", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF222222))
                    }
                }

                Spacer(Modifier.height(8.dp))

                Surface(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(12.dp),
                    color     = ErrorRed.copy(alpha = 0.07f),
                    onClick   = {
                        auth.signOut()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Logout, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Logout", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("SmartKrishi v1.0.0 • Made with ❤️", fontSize = 10.sp, color = Color(0xFFCCCCCC), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  DRAWER ITEM
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerItem(item: DrawerMenuItem, onClick: () -> Unit) {
    Surface(
        modifier        = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        shape           = RoundedCornerShape(14.dp),
        color           = Color.White,
        shadowElevation = 1.dp,
        onClick         = onClick
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier         = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A1A))
                Text(item.subtitle, fontSize = 11.sp, color = Color(0xFF999999), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            item.badge?.let {
                Surface(shape = RoundedCornerShape(6.dp), color = item.color) {
                    Text(it, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  EMPTY STATE
// ═══════════════════════════════════════════════════════════
@Composable
private fun EmptyFarmState(onAddFarm: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(top = 60.dp, start = 40.dp, end = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(140.dp).background(
                Brush.radialGradient(listOf(DarkGreen.copy(alpha = 0.15f), DarkGreen.copy(alpha = 0.02f))),
                CircleShape
            ),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(100.dp).background(DarkGreen.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Agriculture, contentDescription = null, modifier = Modifier.size(58.dp), tint = DarkGreen.copy(alpha = 0.45f))
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("No Farms Yet", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A), letterSpacing = (-0.5).sp)
        Spacer(Modifier.height(10.dp))
        Text(
            "Add your first farm to get started with AI-powered insights, crop tracking, and smart recommendations.",
            textAlign = TextAlign.Center, fontSize = 14.sp, color = Color(0xFF888888), lineHeight = 21.sp
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick   = onAddFarm,
            modifier  = Modifier.fillMaxWidth(0.78f).height(52.dp),
            shape     = RoundedCornerShape(16.dp),
            colors    = ButtonDefaults.buttonColors(containerColor = DarkGreen),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add Your First Farm", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}