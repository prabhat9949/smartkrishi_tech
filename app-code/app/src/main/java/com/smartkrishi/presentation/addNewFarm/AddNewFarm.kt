package com.smartkrishi.presentation.addNewFarm

import android.Manifest
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.smartkrishi.R
import com.smartkrishi.presentation.model.Farm
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewFarm(onFarmSaved: () -> Unit) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Clean Theme Colors
    val primaryGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFF66BB6A)
    val veryLightGreen = Color(0xFFE8F5E9)

    // ---------------- Fields ----------------
    var name by remember { mutableStateOf("") }
    var cropType by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("") }
    var customSoilType by remember { mutableStateOf("") }
    var acres by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }
    var farmImageUri by remember { mutableStateOf<Uri?>(null) }
    var waterSource by remember { mutableStateOf("") }
    var customWaterSource by remember { mutableStateOf("") }
    var irrigationType by remember { mutableStateOf("") }
    var customIrrigationType by remember { mutableStateOf("") }
    var farmDescription by remember { mutableStateOf("") }

    // Validation States
    var nameError by remember { mutableStateOf<String?>(null) }
    var cropTypeError by remember { mutableStateOf<String?>(null) }
    var soilTypeError by remember { mutableStateOf<String?>(null) }
    var acresError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    // Loading States
    var isSaving by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var soilAutoDetected by remember { mutableStateOf(false) }

    // Dropdown Options
    val cropTypes = listOf(
        "🌾 Wheat", "🍚 Rice", "🌽 Corn", "🌿 Cotton", "🎋 Sugarcane",
        "🌾 Barley", "🫘 Soybean", "🥔 Potato", "🍅 Tomato",
        "🥬 Vegetables", "🌻 Sunflower", "🫑 Chili", "🧅 Onion",
        "🥕 Carrot", "🫛 Pulses", "🌱 Other"
    )

    val soilTypes = listOf(
        "🟤 Clay Soil", "🟡 Sandy Soil", "🟢 Loamy Soil",
        "🔵 Silt Soil", "🟫 Peat Soil", "⚪ Chalky Soil",
        "⚫ Black Soil", "🔴 Red Soil", "🟠 Alluvial Soil",
        "🌾 Laterite Soil", "✏️ Custom (Type Below)"
    )

    val waterSources = listOf(
        "💧 Borewell", "🏞️ Canal", "🌊 River", "💦 Pond/Lake",
        "🌧️ Rainwater", "🚰 Municipal Supply", "⛲ Tube Well", "✏️ Other (Specify Below)"
    )

    val irrigationTypes = listOf(
        "💧 Drip Irrigation", "🌊 Sprinkler", "🚰 Flood Irrigation",
        "🎋 Manual Watering", "💦 Rain-fed", "🔷 Mixed", "✏️ Other (Specify Below)"
    )

    var cropTypeExpanded by remember { mutableStateOf(false) }
    var soilTypeExpanded by remember { mutableStateOf(false) }
    var waterSourceExpanded by remember { mutableStateOf(false) }
    var irrigationExpanded by remember { mutableStateOf(false) }

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        farmImageUri = uri
    }

    // Location
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (!granted) {
            Toast.makeText(context, "📍 Location permission denied", Toast.LENGTH_SHORT).show()
            locationError = "Location permission required"
            return@rememberLauncherForActivityResult
        }

        isFetchingLocation = true
        scope.launch {
            fetchLocationAndSoil(context, fusedLocationClient) { lt, ln, address, detectedSoil ->
                lat = lt
                lon = ln
                locationText = address ?: "$lt, $ln"

                // Auto-detect soil type based on location
                if (detectedSoil != null) {
                    soilType = detectedSoil
                    soilAutoDetected = true
                }

                locationError = null
                isFetchingLocation = false

                if (detectedSoil != null) {
                    Toast.makeText(context, "✅ Location & soil auto-detected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "✅ Location fetched", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Validation Function
    fun validateFields(): Boolean {
        var isValid = true

        if (name.isBlank()) {
            nameError = "Farm name is required"
            isValid = false
        } else {
            nameError = null
        }

        if (cropType.isBlank()) {
            cropTypeError = "Crop type is required"
            isValid = false
        } else {
            cropTypeError = null
        }

        if (soilType.isBlank()) {
            soilTypeError = "Soil type is required"
            isValid = false
        } else if (soilType.contains("Custom") && customSoilType.isBlank()) {
            soilTypeError = "Please specify custom soil type"
            isValid = false
        } else {
            soilTypeError = null
        }

        if (acres.isBlank()) {
            acresError = "Area is required"
            isValid = false
        } else if (acres.toDoubleOrNull() == null || acres.toDouble() <= 0) {
            acresError = "Enter valid area"
            isValid = false
        } else {
            acresError = null
        }

        if (locationText.isBlank()) {
            locationError = "Location is required"
            isValid = false
        } else {
            locationError = null
        }

        return isValid
    }

    // Upload Image to Firebase Storage
    suspend fun uploadImage(uri: Uri): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("farm_images/${UUID.randomUUID()}.jpg")
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    // Save Function
    fun saveFarm() {
        if (!validateFields()) {
            Toast.makeText(context, "⚠️ Please fix all errors", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        if (userId == null || userEmail.isNullOrBlank()) {
            Toast.makeText(context, "❌ Error: User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true

        scope.launch {
            try {
                // Upload image if selected
                var imageUrl: String? = null
                if (farmImageUri != null) {
                    isUploadingImage = true
                    imageUrl = uploadImage(farmImageUri!!)
                    isUploadingImage = false
                }

                val firestore = FirebaseFirestore.getInstance().collection("farms")
                val realtime = FirebaseDatabase.getInstance()

                val key = firestore.document().id

                // Get final values (use custom if "Other" selected)
                val finalSoilType = if (soilType.contains("Custom")) customSoilType else soilType
                val finalWaterSource = if (waterSource.contains("Other")) customWaterSource else waterSource
                val finalIrrigationType = if (irrigationType.contains("Other")) customIrrigationType else irrigationType

                val farm = Farm(
                    id = key,
                    name = name.trim(),
                    location = locationText.trim(),
                    lat = lat ?: 0.0,
                    lon = lon ?: 0.0,
                    acres = acres.toDouble().toInt(),
                    cropType = cropType.trim(),
                    soilType = finalSoilType.trim(),
                    backgroundRes = R.drawable.farm_placeholder,
                    ownerId = userId,
                    userEmail = userEmail,
                    imageUrl = imageUrl,
                    waterSource = finalWaterSource.trim(),
                    irrigationType = finalIrrigationType.trim(),
                    description = farmDescription.trim()
                )

                // Save to Firestore
                firestore.document(key).set(farm).await()

                // Save to Realtime Database
                realtime.getReference("farms")
                    .child(userId)
                    .child(key)
                    .setValue(farm)
                    .await()

                isSaving = false
                Toast.makeText(context, "🌾 Farm Added Successfully!", Toast.LENGTH_SHORT).show()
                onFarmSaved()

            } catch (e: Exception) {
                isSaving = false
                Toast.makeText(context, "❌ Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ------------ UI ------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Add New Farm", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onFarmSaved() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryGreen
                )
            )
        },
        containerColor = Color.White
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            // Farm Information Section
            SectionHeader("🌾 Farm Information", primaryGreen)

            CustomTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = "Farm Name *",
                placeholder = "e.g., Green Valley Farm",
                icon = Icons.Default.Agriculture,
                primaryGreen = primaryGreen,
                error = nameError,
                focusManager = focusManager
            )

            CustomDropdown(
                value = cropType,
                expanded = cropTypeExpanded,
                onExpandedChange = { cropTypeExpanded = it },
                label = "Crop Type *",
                placeholder = "Select crop type",
                icon = Icons.Default.Eco,
                options = cropTypes,
                onSelect = { cropType = it; cropTypeError = null },
                primaryGreen = primaryGreen,
                error = cropTypeError
            )

            // Soil Type with Auto-detect indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Soil Type *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (soilAutoDetected) primaryGreen else Color.Black
                )
                if (soilAutoDetected) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Auto-detected",
                            fontSize = 12.sp,
                            color = primaryGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            CustomDropdown(
                value = soilType,
                expanded = soilTypeExpanded,
                onExpandedChange = {
                    soilTypeExpanded = it
                    if (it) soilAutoDetected = false // Clear auto-detect flag when manually changed
                },
                label = "",
                placeholder = "Auto-detect or select manually",
                icon = Icons.Default.Terrain,
                options = soilTypes,
                onSelect = {
                    soilType = it
                    soilTypeError = null
                    soilAutoDetected = false
                },
                primaryGreen = primaryGreen,
                error = soilTypeError
            )

            // Custom Soil Type Input
            AnimatedVisibility(visible = soilType.contains("Custom")) {
                CustomTextField(
                    value = customSoilType,
                    onValueChange = { customSoilType = it; soilTypeError = null },
                    label = "Specify Soil Type *",
                    placeholder = "Enter custom soil type",
                    icon = Icons.Default.Edit,
                    primaryGreen = primaryGreen,
                    error = null,
                    focusManager = focusManager
                )
            }

            CustomTextField(
                value = acres,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        acres = it
                        acresError = null
                    }
                },
                label = "Area (Acres) *",
                placeholder = "e.g., 25.5",
                icon = Icons.Default.Landscape,
                primaryGreen = primaryGreen,
                keyboardType = KeyboardType.Decimal,
                error = acresError,
                focusManager = focusManager
            )

            Divider(color = Color.LightGray, thickness = 1.dp)

            // Location Section
            SectionHeader("📍 Location Details", primaryGreen)

            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it; locationError = null },
                label = { Text("Farm Location *", fontSize = 14.sp) },
                placeholder = { Text("Enter or auto-detect location", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = primaryGreen)
                },
                trailingIcon = {
                    Row {
                        // Auto-detect button
                        IconButton(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            enabled = !isFetchingLocation
                        ) {
                            if (isFetchingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = primaryGreen
                                )
                            } else {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = "Auto-detect",
                                    tint = primaryGreen
                                )
                            }
                        }

                        // Map pin button
                        IconButton(onClick = {
                            Toast.makeText(context, "🗺️ Map view coming soon!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = "Pin on map",
                                tint = primaryGreen
                            )
                        }
                    }
                },
                isError = locationError != null,
                supportingText = {
                    locationError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(primaryGreen),
                minLines = 2,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Coordinates Display
            AnimatedVisibility(visible = lat != null && lon != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = veryLightGreen
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "GPS Coordinates",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Lat: ${String.format("%.6f", lat)} | Lon: ${String.format("%.6f", lon)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryGreen,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Divider(color = Color.LightGray, thickness = 1.dp)

            // Water & Irrigation Section
            SectionHeader("💧 Water & Irrigation (Optional)", primaryGreen)

            CustomDropdown(
                value = waterSource,
                expanded = waterSourceExpanded,
                onExpandedChange = { waterSourceExpanded = it },
                label = "Water Source",
                placeholder = "Select water source",
                icon = Icons.Default.WaterDrop,
                options = waterSources,
                onSelect = { waterSource = it },
                primaryGreen = primaryGreen
            )

            AnimatedVisibility(visible = waterSource.contains("Other")) {
                CustomTextField(
                    value = customWaterSource,
                    onValueChange = { customWaterSource = it },
                    label = "Specify Water Source",
                    placeholder = "Enter water source",
                    icon = Icons.Default.Edit,
                    primaryGreen = primaryGreen,
                    error = null,
                    focusManager = focusManager
                )
            }

            CustomDropdown(
                value = irrigationType,
                expanded = irrigationExpanded,
                onExpandedChange = { irrigationExpanded = it },
                label = "Irrigation Type",
                placeholder = "Select irrigation method",
                icon = Icons.Default.Shower,
                options = irrigationTypes,
                onSelect = { irrigationType = it },
                primaryGreen = primaryGreen
            )

            AnimatedVisibility(visible = irrigationType.contains("Other")) {
                CustomTextField(
                    value = customIrrigationType,
                    onValueChange = { customIrrigationType = it },
                    label = "Specify Irrigation Type",
                    placeholder = "Enter irrigation method",
                    icon = Icons.Default.Edit,
                    primaryGreen = primaryGreen,
                    error = null,
                    focusManager = focusManager
                )
            }

            // Description
            OutlinedTextField(
                value = farmDescription,
                onValueChange = { farmDescription = it },
                label = { Text("Farm Description (Optional)", fontSize = 14.sp) },
                placeholder = { Text("Add any additional details...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null, tint = primaryGreen)
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(primaryGreen),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = { saveFarm() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSaving && !isUploadingImage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryGreen,
                    disabledContainerColor = primaryGreen.copy(0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                if (isSaving || isUploadingImage) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isUploadingImage) "Uploading Image..." else "Saving Farm...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Save Farm", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// Helper Composables
@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    primaryGreen: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 14.sp) },
        placeholder = { Text(placeholder, fontSize = 13.sp, color = Color.Gray) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = primaryGreen)
        },
        isError = error != null,
        supportingText = {
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = textFieldColors(primaryGreen),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDropdown(
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    options: List<String>,
    onSelect: (String) -> Unit,
    primaryGreen: Color,
    error: String? = null
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = if (label.isNotEmpty()) ({ Text(label, fontSize = 14.sp) }) else null,
            placeholder = { Text(placeholder, fontSize = 13.sp, color = Color.Gray) },
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = primaryGreen)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = error != null,
            supportingText = {
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors(primaryGreen)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp) },
                    onClick = {
                        onSelect(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun textFieldColors(primaryGreen: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = primaryGreen,
    unfocusedBorderColor = Color.LightGray,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    cursorColor = primaryGreen,
    focusedLabelColor = primaryGreen,
    unfocusedLabelColor = Color.Gray,
    focusedLeadingIconColor = primaryGreen,
    unfocusedLeadingIconColor = Color.Gray
)

// -------------- ENHANCED LOCATION FETCH WITH SOIL DETECTION --------------
private fun fetchLocationAndSoil(
    context: android.content.Context,
    fused: com.google.android.gms.location.FusedLocationProviderClient,
    onLoc: (Double, Double, String?, String?) -> Unit
) {
    try {
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    val address = addresses?.firstOrNull()?.getAddressLine(0)

                    // Soil detection based on region
                    val state = addresses?.firstOrNull()?.adminArea?.lowercase() ?: ""
                    val detectedSoil = when {
                        state.contains("maharashtra") || state.contains("madhya pradesh") -> "⚫ Black Soil"
                        state.contains("punjab") || state.contains("uttar pradesh") || state.contains("haryana") -> "🟠 Alluvial Soil"
                        state.contains("tamil nadu") || state.contains("kerala") || state.contains("karnataka") -> "🔴 Red Soil"
                        state.contains("rajasthan") || state.contains("gujarat") -> "🟡 Sandy Soil"
                        state.contains("west bengal") || state.contains("assam") -> "🟢 Loamy Soil"
                        state.contains("odisha") || state.contains("jharkhand") -> "🌾 Laterite Soil"
                        else -> null
                    }

                    onLoc(loc.latitude, loc.longitude, address, detectedSoil)
                } catch (e: Exception) {
                    onLoc(loc.latitude, loc.longitude, null, null)
                }
            } else {
                Toast.makeText(context, "❌ Un" +
                        "" +
                        "" +
                        "able to fetch location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "❌ Location fetch failed", Toast.LENGTH_SHORT).show()
        }
    } catch (e: SecurityException) {
        Toast.makeText(context, "❌ Location permission required", Toast.LENGTH_SHORT).show()
    }
}



