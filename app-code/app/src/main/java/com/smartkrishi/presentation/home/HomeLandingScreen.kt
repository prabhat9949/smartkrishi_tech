package com.smartkrishi.presentation.home

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.smartkrishi.R
import com.smartkrishi.presentation.model.Farm
import kotlinx.coroutines.delay

// 🎨 PROFESSIONAL COLOR SCHEME
private val PrimaryGreen = Color(0xFF2E7D32)
private val SecondaryGreen = Color(0xFF388E3C)
private val AccentGreen = Color(0xFF66BB6A)
private val LightBackground = Color(0xFFF5F5F5)
private val CardBackground = Color(0xFFFFFFFF)
private val ErrorRed = Color(0xFFD32F2F)

// 📊 Statistic Type Enum
private enum class StatType {
    FARMS, ACRES, CROPS
}

// Helper function to clean crop type
private fun String.cleanCropType(): String {
    return this
        .removePrefix("🌾 ")
        .removePrefix("🍚 ")
        .removePrefix("🌽 ")
        .removePrefix("🌿 ")
        .removePrefix("🎋 ")
        .removePrefix("🫘 ")
        .removePrefix("🥔 ")
        .removePrefix("🍅 ")
        .removePrefix("🥬 ")
        .removePrefix("🌻 ")
        .removePrefix("🫑 ")
        .removePrefix("🧅 ")
        .removePrefix("🥕 ")
        .removePrefix("🫛 ")
        .removePrefix("🌱 ")
        .trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLandingScreen(
    onOpenFarm: (Farm) -> Unit,
    onAddFarm: () -> Unit,
    onEditFarm: (Farm) -> Unit,
    onDeleteFarm: (Farm) -> Unit,
    onSkip: () -> Unit,
    viewModel: FarmViewModel = hiltViewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val currentUserEmail = remember { auth.currentUser?.email ?: "" }
    val currentUserName = remember {
        auth.currentUser?.displayName ?: currentUserEmail.substringBefore("@").capitalize()
    }

    // ✅ OBSERVE VIEWMODEL STATE
    val farms by viewModel.farms.collectAsState()
    val selectedFarm by viewModel.selectedFarm.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 🔥 FILTER FARMS BY CURRENT USER
    val userFarms = remember(farms, currentUserEmail) {
        if (currentUserEmail.isBlank()) {
            emptyList()
        } else {
            farms.filter { it.userEmail == currentUserEmail }
        }
    }

    // ✅ ENHANCED STATISTICS
    val totalAcres = remember(userFarms) {
        userFarms.sumOf { it.acres }
    }

    val uniqueCrops = remember(userFarms) {
        userFarms.map { it.cropType }.distinct()
    }

    val cropDistribution = remember(userFarms) {
        userFarms.groupBy { it.cropType }.mapValues { it.value.size }
    }

    // ✅ DEBUG LOGGING
    LaunchedEffect(userFarms.size) {
        Log.d("HomeLandingScreen", "🔄 Farms changed - Count: ${userFarms.size}")
        userFarms.forEach { farm ->
            Log.d("HomeLandingScreen", "   📍 ${farm.name} (${farm.userEmail})")
        }
    }

    var bottomSheetFarm by remember { mutableStateOf<Farm?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var farmToDelete by remember { mutableStateOf<Farm?>(null) }

    // ✅ NEW: Statistics Dialog State
    var showStatsDialog by remember { mutableStateOf(false) }
    var selectedStatType by remember { mutableStateOf<StatType?>(null) }

    // ✅ REFRESH ON LOAD
    LaunchedEffect(Unit) {
        viewModel.refreshFarms()
    }

    // 🔽 STATISTICS DETAIL DIALOG
    if (showStatsDialog && selectedStatType != null) {
        StatisticsDetailDialog(
            statType = selectedStatType!!,
            farms = userFarms,
            totalAcres = totalAcres,
            cropDistribution = cropDistribution,
            onDismiss = { showStatsDialog = false },
            onFarmClick = { farm ->
                showStatsDialog = false
                bottomSheetFarm = farm
                showSheet = true
            }
        )
    }

    // 🔽 BOTTOM SHEET FOR FARM ACTIONS
    if (showSheet && bottomSheetFarm != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            FarmActionSheet(
                farm = bottomSheetFarm!!,
                onOpenFarm = {
                    onOpenFarm(bottomSheetFarm!!)
                    showSheet = false
                },
                onEditFarm = {
                    onEditFarm(bottomSheetFarm!!)
                    showSheet = false
                },
                onDeleteFarmClick = {
                    farmToDelete = bottomSheetFarm
                    showDeleteDialog = true
                    showSheet = false
                }
            )
        }
    }

    // ✅ DELETE CONFIRMATION DIALOG
    if (showDeleteDialog && farmToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Delete Farm?",
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to delete",
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        "\"${farmToDelete!!.name}\"?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        "This action cannot be undone. All farm data, sensors, and history will be permanently deleted.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFarm(farmToDelete!!)
                        farmToDelete = null
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Farm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        farmToDelete = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, PrimaryGreen)
                ) {
                    Text("Cancel", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = CardBackground
        )
    }

    // 📌 MAIN CONTENT
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // ✅ ENHANCED HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello, $currentUserName! 👋",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "My Farms",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryGreen,
                        fontSize = 32.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshFarms() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(CardBackground, CircleShape)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = "Refresh Farms",
                        tint = SecondaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ✅ CLICKABLE STATISTICS CARDS
            if (userFarms.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EnhancedStatCard(
                        icon = Icons.Outlined.Agriculture,
                        value = userFarms.size.toString(),
                        label = if (userFarms.size == 1) "Farm" else "Farms",
                        subtitle = "Total Farms",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedStatType = StatType.FARMS
                            showStatsDialog = true
                        }
                    )
                    EnhancedStatCard(
                        icon = Icons.Outlined.Landscape,
                        value = totalAcres.toString(),
                        label = "Acres",
                        subtitle = "Total Land",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedStatType = StatType.ACRES
                            showStatsDialog = true
                        }
                    )
                    EnhancedStatCard(
                        icon = Icons.Outlined.Grass,
                        value = uniqueCrops.size.toString(),
                        label = if (uniqueCrops.size == 1) "Crop" else "Crops",
                        subtitle = "Crop Types",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedStatType = StatType.CROPS
                            showStatsDialog = true
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))
            }

            // ✅ LOADING STATE
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SecondaryGreen,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Loading your farms...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                if (userFarms.isEmpty()) {
                    EmptyFarmState(onAddFarm = onAddFarm)
                } else {
                    // 🌽 FARM LIST HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Farms",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            fontSize = 18.sp
                        )

                        TextButton(
                            onClick = onSkip,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = SecondaryGreen
                            )
                        ) {
                            Text("Skip", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 🌽 FARMS LIST
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(userFarms, key = { it.id }) { farm ->
                            AnimatedFarmCard(
                                farm = farm,
                                isSelected = farm.id == selectedFarm?.id,
                                onClick = {
                                    bottomSheetFarm = farm
                                    showSheet = true
                                }
                            )
                        }

                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        // ✅ FLOATING ACTION BUTTON
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AnimatedFAB(onClick = onAddFarm)
        }
    }
}

// 📊 ENHANCED STATISTICS CARD WITH SUBTITLE & CLICK
@Composable
private fun EnhancedStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(105.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = subtitle,
                tint = SecondaryGreen,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = PrimaryGreen
            )
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                subtitle,
                fontSize = 10.sp,
                color = Color.Gray.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

// 📋 STATISTICS DETAIL DIALOG
@Composable
private fun StatisticsDetailDialog(
    statType: StatType,
    farms: List<Farm>,
    totalAcres: Int,
    cropDistribution: Map<String, Int>,
    onDismiss: () -> Unit,
    onFarmClick: (Farm) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 550.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (statType) {
                                StatType.FARMS -> "All Farms"
                                StatType.ACRES -> "Land Distribution"
                                StatType.CROPS -> "Crop Distribution"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PrimaryGreen
                        )
                        Text(
                            text = when (statType) {
                                StatType.FARMS -> "${farms.size} farm${if (farms.size > 1) "s" else ""} total"
                                StatType.ACRES -> "$totalAcres acres total"
                                StatType.CROPS -> "${cropDistribution.size} crop type${if (cropDistribution.size > 1) "s" else ""}"
                            },
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                Spacer(Modifier.height(16.dp))

                // Content
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (statType) {
                        StatType.FARMS -> {
                            items(farms) { farm ->
                                FarmListItem(
                                    farm = farm,
                                    onClick = { onFarmClick(farm) }
                                )
                            }
                        }

                        StatType.ACRES -> {
                            items(farms) { farm ->
                                AcresListItem(farm = farm, onClick = { onFarmClick(farm) })
                            }
                        }

                        StatType.CROPS -> {
                            items(cropDistribution.entries.toList()) { (crop, count) ->
                                CropDistributionItem(
                                    cropType = crop,
                                    count = count,
                                    farms = farms.filter { it.cropType == crop }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Farm List Item
@Composable
private fun FarmListItem(
    farm: Farm,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
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
                    .background(SecondaryGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Agriculture,
                    contentDescription = null,
                    tint = SecondaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    farm.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${farm.acres} acres",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    farm.cropType.cleanCropType(),
                    fontSize = 12.sp,
                    color = SecondaryGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// Acres List Item
@Composable
private fun AcresListItem(
    farm: Farm,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
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
                    .background(Color(0xFFFF9800).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Landscape,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    farm.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "📍 ${farm.location}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${farm.acres}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color(0xFFFF9800)
                )
                Text(
                    "acres",
                    fontSize = 11.sp,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Crop Distribution Item
@Composable
private fun CropDistributionItem(
    cropType: String,
    count: Int,
    farms: List<Farm>
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
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
                        .background(SecondaryGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Grass,
                        contentDescription = null,
                        tint = SecondaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        cropType.cleanCropType(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryGreen,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "$count farm${if (count > 1) "s" else ""} growing this crop",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = SecondaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Farms with ${cropType.cleanCropType()}:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    farms.forEach { farm ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "• ${farm.name}",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${farm.acres} acres",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// 🌾 EMPTY STATE
@Composable
private fun EmptyFarmState(onAddFarm: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(600)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Agriculture,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = PrimaryGreen.copy(alpha = 0.3f)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "No Farms Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryGreen,
                    fontSize = 28.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Start your smart farming journey!\nAdd your first farm to unlock AI-powered insights.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    lineHeight = 24.sp
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onAddFarm,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryGreen
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Add Your First Farm",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 📋 FARM ACTION SHEET
@Composable
private fun FarmActionSheet(
    farm: Farm,
    onOpenFarm: () -> Unit,
    onEditFarm: () -> Unit,
    onDeleteFarmClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // FARM HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F5E9))
            ) {
                if (!farm.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(farm.imageUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.farm_placeholder)
                            .error(R.drawable.farm_placeholder)
                            .build(),
                        contentDescription = farm.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Agriculture,
                        contentDescription = null,
                        tint = SecondaryGreen,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    farm.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(
                        Icons.Outlined.Landscape,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${farm.acres} acres",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Grass,
                        contentDescription = null,
                        tint = SecondaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        farm.cropType.cleanCropType(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        farm.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
        Spacer(Modifier.height(20.dp))

        Text(
            "Quick Actions",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(16.dp))

        FarmActionRow(
            icon = Icons.Outlined.Dashboard,
            label = "Open Farm Dashboard",
            description = "View real-time data & insights",
            onClick = onOpenFarm
        )

        Spacer(Modifier.height(8.dp))

        FarmActionRow(
            icon = Icons.Outlined.Edit,
            label = "Edit Farm Details",
            description = "Update farm information",
            onClick = onEditFarm
        )

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
        Spacer(Modifier.height(12.dp))

        FarmActionRow(
            icon = Icons.Outlined.Delete,
            label = "Delete Farm",
            description = "Permanently remove this farm",
            onClick = onDeleteFarmClick,
            isDestructive = true
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun FarmActionRow(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val backgroundColor = if (isDestructive)
        Color(0xFFFFEBEE)
    else
        Color(0xFFF1F8E9)

    val iconTint = if (isDestructive)
        ErrorRed
    else
        SecondaryGreen

    val textColor = if (isDestructive)
        ErrorRed
    else
        Color.DarkGray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDestructive) ErrorRed.copy(alpha = 0.15f) else SecondaryGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    description,
                    color = textColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// 🎴 ANIMATED FARM CARD
@Composable
private fun AnimatedFarmCard(
    farm: Farm,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(400)
        )
    ) {
        FarmCardItem(
            farm = farm,
            isSelected = isSelected,
            onClick = onClick
        )
    }
}

@Composable
private fun FarmCardItem(
    farm: Farm,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val borderColor by animateColorAsState(
        if (isSelected) SecondaryGreen else Color.Transparent,
        label = "border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 0.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            Box {
                if (!farm.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(farm.imageUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.farm_placeholder)
                            .error(R.drawable.farm_placeholder)
                            .build(),
                        contentDescription = farm.name,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(140.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = farm.backgroundRes),
                        contentDescription = farm.name,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(140.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(32.dp)
                            .background(SecondaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        farm.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = if (isSelected) SecondaryGreen else PrimaryGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Landscape,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${farm.acres} acres",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            farm.location,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) SecondaryGreen else Color(0xFFF1F8E9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Grass,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else SecondaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            farm.cropType.cleanCropType(),
                            color = if (isSelected) Color.White else SecondaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ✨ ANIMATED FAB
@Composable
private fun AnimatedFAB(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabScale"
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = SecondaryGreen,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .size(width = 160.dp, height = 56.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Farm",
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Add Farm",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// ✅ HELPER
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
