package com.smartkrishi.presentation.crops

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartkrishi.presentation.theme.ThemeState

private val PrimaryGreen = Color(0xFF2E7D32)

// ===========================================================
// DATA MODEL
// ===========================================================
data class CropInfo(
    val name: String,
    val season: String,
    val soil: String,
    val waterNeed: String,
    val duration: String,
    val suitability: String,
    val pricePerUnit: String? = null,
    val isOrganic: Boolean? = null,
    val grade: String? = null,
    val location: String? = null,
    val certification: String? = null,
    val imageUri: Uri? = null
)

// ===========================================================
// MAIN SCREEN
// ===========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropsScreen(navController: NavController) {
    val isDark by ThemeState.isDarkTheme
    val background = if (isDark) Color(0xFF07150F) else Color(0xFFF4FFF7)
    val cardColor = if (isDark) Color(0xFF10271A) else Color.White
    val textPrimary = if (isDark) Color(0xFFE8F5E9) else Color(0xFF09140C)
    val textSecondary = if (isDark) Color(0xFFB5CBB8) else Color(0xFF5F6B63)

    // Default reference crops
    val defaultCrops = remember {
        listOf(
            CropInfo(
                name = "Wheat",
                season = "Rabi",
                soil = "Well-drained loam, pH 6.0 – 7.5",
                waterNeed = "Medium, critical at tillering & grain filling",
                duration = "120–150 days",
                suitability = "North Indian plains, irrigated regions"
            ),
            CropInfo(
                name = "Paddy (Rice)",
                season = "Kharif / Rabi (South)",
                soil = "Clay / loam, good water retention",
                waterNeed = "High, standing water at main crop stage",
                duration = "110–150 days",
                suitability = "High rainfall / irrigated lowlands"
            ),
            CropInfo(
                name = "Maize",
                season = "Kharif / Rabi / Spring",
                soil = "Well-drained fertile soil, pH 5.5–7.5",
                waterNeed = "Medium, sensitive at flowering & grain filling",
                duration = "90–120 days",
                suitability = "All India with irrigation / rainfall"
            ),
            CropInfo(
                name = "Mustard",
                season = "Rabi",
                soil = "Loam to clay loam, pH 6–7.5",
                waterNeed = "Low to medium",
                duration = "110–140 days",
                suitability = "Cool & dry climate, North & Central India"
            ),
            CropInfo(
                name = "Sugarcane",
                season = "Annual (10–18 months)",
                soil = "Deep fertile loam, high organic matter",
                waterNeed = "High, regular irrigation required",
                duration = "10–18 months",
                suitability = "Sub-tropical & tropical regions with irrigation"
            )
        )
    }

    // User-added crops (in this session)
    var userCrops by remember { mutableStateOf<List<CropInfo>>(emptyList()) }

    // Dialog state
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crops & Listings",
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF10271A) else Color(0xFFC8E6C9)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Crop") },
                containerColor = PrimaryGreen,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (userCrops.isNotEmpty()) {
                    item {
                        Text(
                            "Your Listings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(userCrops) { crop ->
                        CropCard(
                            crop = crop,
                            cardColor = cardColor,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            showListingInfo = true
                        )
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                item {
                    Text(
                        "Reference Crops",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(defaultCrops) { crop ->
                    CropCard(
                        crop = crop,
                        cardColor = cardColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        showListingInfo = false
                    )
                }
            }
        }

        if (showAddDialog) {
            AddCropDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { newCrop ->
                    userCrops = userCrops + newCrop
                    showAddDialog = false
                }
            )
        }
    }
}

// ===========================================================
// CROP CARD
// ===========================================================
@Composable
private fun CropCard(
    crop: CropInfo,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    showListingInfo: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = PrimaryGreen
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(crop.name, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    Text(
                        "${crop.season} • ${crop.duration}",
                        fontSize = 12.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text("Soil: ${crop.soil}", fontSize = 12.sp, color = textSecondary)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF42A5F5),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(crop.waterNeed, fontSize = 12.sp, color = textSecondary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Suitability: ${crop.suitability}", fontSize = 12.sp, color = textPrimary)
            }

            if (showListingInfo) {
                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    crop.pricePerUnit?.let {
                        AssistChip(
                            onClick = {},
                            label = { Text("Price: $it") },
                            leadingIcon = {
                                Icon(Icons.Default.CurrencyRupee, null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }

                    crop.isOrganic?.let {
                        AssistChip(
                            onClick = {},
                            label = { Text(if (it) "Organic" else "Non-organic") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (it) Icons.Default.Eco else Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    crop.grade?.let {
                        AssistChip(
                            onClick = {},
                            label = { Text("Grade: $it") }
                        )
                    }
                }

                crop.location?.let {
                    Text("Location: $it", fontSize = 12.sp, color = textSecondary)
                }

                crop.certification?.takeIf { it.isNotBlank() }?.let {
                    Text("Certification/Remarks: $it", fontSize = 11.sp, color = textSecondary)
                }

                crop.imageUri?.let {
                    Text("Photo attached ✓", fontSize = 11.sp, color = Color(0xFF80CBC4))
                }
            }
        }
    }
}

// ===========================================================
// ADD CROP DIALOG
// ===========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCropDialog(
    onDismiss: () -> Unit,
    onAdd: (CropInfo) -> Unit
) {
    val isDark by ThemeState.isDarkTheme
    val bgColor = if (isDark) Color(0xFF0B1A12) else Color.White
    val textPrimary = if (isDark) Color(0xFFE8F5E9) else Color(0xFF09140C)
    val textSecondary = if (isDark) Color(0xFFB5CBB8) else Color(0xFF5F6B63)

    var name by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("Kharif") }
    var soilType by remember { mutableStateOf("Loam") }
    var waterNeed by remember { mutableStateOf("Medium") }
    var duration by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var organicChoice by remember { mutableStateOf("Organic") }
    var grade by remember { mutableStateOf("A") }
    var certification by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val seasonOptions = listOf("Kharif", "Rabi", "Zaid", "Any")
    val soilOptions = listOf("Loam", "Clay", "Sandy loam", "Black soil", "Alluvial")
    val waterOptions = listOf("Low", "Medium", "High")
    val organicOptions = listOf("Organic", "Non-organic")
    val gradeOptions = listOf("A", "B", "C")

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val crop = CropInfo(
                            name = name.trim(),
                            season = season,
                            soil = soilType,
                            waterNeed = waterNeed,
                            duration = if (duration.isNotBlank()) duration else "N/A",
                            suitability = location.ifBlank { "Farmer provided" },
                            pricePerUnit = price.ifBlank { null },
                            isOrganic = organicChoice == "Organic",
                            grade = grade,
                            location = location.ifBlank { null },
                            certification = certification.ifBlank { null },
                            imageUri = imageUri
                        )
                        onAdd(crop)
                    }
                }
            ) {
                Text("Add", color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Add Crop Listing", color = textPrimary, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(bgColor)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Crop name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Season dropdown
                DropdownField(
                    label = "Season",
                    options = seasonOptions,
                    selected = season,
                    onSelectedChange = { season = it }
                )

                Spacer(Modifier.height(8.dp))

                // Soil dropdown
                DropdownField(
                    label = "Soil type",
                    options = soilOptions,
                    selected = soilType,
                    onSelectedChange = { soilType = it }
                )

                Spacer(Modifier.height(8.dp))

                // Water need dropdown
                DropdownField(
                    label = "Water requirement",
                    options = waterOptions,
                    selected = waterNeed,
                    onSelectedChange = { waterNeed = it }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (e.g. 110–120 days)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (district / state)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (e.g. ₹2200/quintal)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Organic dropdown
                DropdownField(
                    label = "Organic / non-organic",
                    options = organicOptions,
                    selected = organicChoice,
                    onSelectedChange = { organicChoice = it }
                )

                Spacer(Modifier.height(8.dp))

                // Grade dropdown
                DropdownField(
                    label = "Quality grade",
                    options = gradeOptions,
                    selected = grade,
                    onSelectedChange = { grade = it }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = certification,
                    onValueChange = { certification = it },
                    label = { Text("Certification / remarks (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Photo", color = textPrimary, fontSize = 13.sp)
                        Text(
                            if (imageUri != null) "Photo attached" else "No photo selected",
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                    }
                    TextButton(
                        onClick = { imagePickerLauncher.launch("image/*") }
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = PrimaryGreen
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Choose", color = PrimaryGreen)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Buyers will see these details to verify crop authenticity, price and quality.",
                    fontSize = 11.sp,
                    color = textSecondary
                )
            }
        },
        containerColor = bgColor
    )
}

// ===========================================================
// REUSABLE DROPDOWN FIELD
// ===========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectedChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
