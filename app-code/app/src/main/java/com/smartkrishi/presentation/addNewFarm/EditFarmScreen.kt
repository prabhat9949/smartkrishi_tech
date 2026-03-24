package com.smartkrishi.presentation.addNewFarm

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.storage.FirebaseStorage
import com.smartkrishi.R
import com.smartkrishi.presentation.home.FarmViewModel
import com.smartkrishi.presentation.model.Farm
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val TAG = "EditFarmScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFarmScreen(
    farm: Farm,
    viewModel: FarmViewModel,
    onFarmUpdated: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val primaryGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFFE8F5E9)
    val darkText = Color(0xFF212121)

    // ✅ Log incoming farm data
    LaunchedEffect(farm) {
        Log.d(TAG, "=== EDIT FARM DATA ===")
        Log.d(TAG, "Farm ID: ${farm.id}")
        Log.d(TAG, "Name: ${farm.name}")
        Log.d(TAG, "Crop: ${farm.cropType}")
        Log.d(TAG, "Soil: ${farm.soilType}")
        Log.d(TAG, "Acres: ${farm.acres}")
        Log.d(TAG, "Location: ${farm.location}")
        Log.d(TAG, "Image URL: ${farm.imageUrl}")
        Log.d(TAG, "Water Source: ${farm.waterSource}")
        Log.d(TAG, "Irrigation: ${farm.irrigationType}")
        Log.d(TAG, "Description: ${farm.description}")
    }

    // ✅ Pre-fill with existing farm data
    var name by remember { mutableStateOf(farm.name) }
    var cropType by remember { mutableStateOf(farm.cropType) }
    var soilType by remember { mutableStateOf(farm.soilType) }
    var customSoilType by remember { mutableStateOf("") }
    var acres by remember { mutableStateOf(farm.acres.toString()) }
    var locationText by remember { mutableStateOf(farm.location) }
    var lat by remember { mutableStateOf<Double?>(farm.lat) }
    var lon by remember { mutableStateOf<Double?>(farm.lon) }
    var farmImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf(farm.imageUrl) }
    var waterSource by remember { mutableStateOf(farm.waterSource) }
    var irrigationType by remember { mutableStateOf(farm.irrigationType) }
    var description by remember { mutableStateOf(farm.description) }

    var isSaving by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Dropdown states
    var cropTypeExpanded by remember { mutableStateOf(false) }
    var soilTypeExpanded by remember { mutableStateOf(false) }
    var waterSourceExpanded by remember { mutableStateOf(false) }
    var irrigationExpanded by remember { mutableStateOf(false) }

    // Options
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
        "🌧️ Rainwater", "🚰 Municipal Supply", "⛲ Tube Well", "✏️ Other"
    )

    val irrigationTypes = listOf(
        "💧 Drip Irrigation", "🌊 Sprinkler", "🚰 Flood Irrigation",
        "🎋 Manual Watering", "💦 Rain-fed", "🔷 Mixed", "✏️ Other"
    )

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        farmImageUri = uri
        Log.d(TAG, "New image selected: $uri")
    }

    // Upload Image Function
    suspend fun uploadImage(uri: Uri): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("farm_images/${UUID.randomUUID()}.jpg")
            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "Image uploaded successfully: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Image upload failed: ${e.message}", e)
            null
        }
    }

    // Update Farm Function
    fun updateFarm() {
        // Validation
        if (name.isBlank() || cropType.isBlank() || soilType.isBlank() ||
            acres.isBlank() || locationText.isBlank()) {
            Toast.makeText(context, "⚠️ Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true

        scope.launch {
            try {
                var imageUrl = existingImageUrl

                // Upload new image if selected
                if (farmImageUri != null) {
                    isUploadingImage = true
                    imageUrl = uploadImage(farmImageUri!!)
                    isUploadingImage = false

                    if (imageUrl == null) {
                        Toast.makeText(context, "⚠️ Image upload failed, keeping existing image", Toast.LENGTH_SHORT).show()
                        imageUrl = existingImageUrl
                    }
                }

                val finalSoilType = if (soilType.contains("Custom")) customSoilType else soilType

                val updatedFarm = farm.copy(
                    name = name.trim(),
                    cropType = cropType.trim(),
                    soilType = finalSoilType.trim(),
                    acres = acres.toDoubleOrNull()?.toInt() ?: farm.acres,
                    location = locationText.trim(),
                    lat = lat ?: farm.lat,
                    lon = lon ?: farm.lon,
                    imageUrl = imageUrl,
                    waterSource = waterSource.trim(),
                    irrigationType = irrigationType.trim(),
                    description = description.trim()
                )

                Log.d(TAG, "Updating farm with data: $updatedFarm")

                // Use ViewModel to update farm
                viewModel.updateFarm(
                    farm = updatedFarm,
                    onSuccess = {
                        isSaving = false
                        Log.d(TAG, "✅ Farm updated successfully")
                        Toast.makeText(context, "✅ Farm updated successfully!", Toast.LENGTH_SHORT).show()
                        onFarmUpdated()
                    },
                    onFailure = { error ->
                        isSaving = false
                        Log.e(TAG, "❌ Update failed: $error")
                        Toast.makeText(context, "❌ Failed to update: $error", Toast.LENGTH_LONG).show()
                    }
                )

            } catch (e: Exception) {
                isSaving = false
                isUploadingImage = false
                Log.e(TAG, "❌ Exception during update", e)
                Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Edit Farm", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            farm.name,
                            fontSize = 12.sp,
                            color = Color.White.copy(0.9f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Cancel", tint = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section Header
            Text(
                "Farm Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryGreen
            )

            // Farm Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Farm Name *", fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Agriculture, null, tint = primaryGreen) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = primaryGreen,
                    unfocusedLabelColor = Color.Gray,
                    focusedLeadingIconColor = primaryGreen,
                    unfocusedLeadingIconColor = Color.Gray,
                    cursorColor = primaryGreen,
                    focusedTextColor = darkText,
                    unfocusedTextColor = darkText
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = darkText
                )
            )

            // Crop Type
            CustomDropdown(
                value = cropType,
                expanded = cropTypeExpanded,
                onExpandedChange = { cropTypeExpanded = it },
                label = "Crop Type *",
                placeholder = "Select crop type",
                icon = Icons.Default.Eco,
                options = cropTypes,
                onSelect = { cropType = it },
                primaryGreen = primaryGreen,
                darkText = darkText
            )

            // Soil Type
            CustomDropdown(
                value = soilType,
                expanded = soilTypeExpanded,
                onExpandedChange = { soilTypeExpanded = it },
                label = "Soil Type *",
                placeholder = "Select soil type",
                icon = Icons.Default.Terrain,
                options = soilTypes,
                onSelect = { soilType = it },
                primaryGreen = primaryGreen,
                darkText = darkText
            )

            // Custom Soil Type
            AnimatedVisibility(visible = soilType.contains("Custom")) {
                OutlinedTextField(
                    value = customSoilType,
                    onValueChange = { customSoilType = it },
                    label = { Text("Specify Soil Type *", fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                    placeholder = { Text("Enter custom soil type", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = primaryGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = primaryGreen,
                        cursorColor = primaryGreen,
                        focusedTextColor = darkText,
                        unfocusedTextColor = darkText
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = darkText
                    )
                )
            }

            // Area
            OutlinedTextField(
                value = acres,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) acres = it },
                label = { Text("Area (Acres) *", fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                placeholder = { Text("e.g., 25.5", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Landscape, null, tint = primaryGreen) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = primaryGreen,
                    cursorColor = primaryGreen,
                    focusedTextColor = darkText,
                    unfocusedTextColor = darkText
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = darkText
                )
            )

            // Location
            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
                label = { Text("Location *", fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                placeholder = { Text("Farm location", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = primaryGreen) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = primaryGreen,
                    cursorColor = primaryGreen,
                    focusedTextColor = darkText,
                    unfocusedTextColor = darkText
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = darkText
                )
            )

            HorizontalDivider(thickness = 2.dp, color = primaryGreen.copy(alpha = 0.2f))

            Text(
                "Additional Details (Optional)",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryGreen
            )

            // Water Source
            CustomDropdown(
                value = waterSource,
                expanded = waterSourceExpanded,
                onExpandedChange = { waterSourceExpanded = it },
                label = "Water Source",
                placeholder = "Select water source",
                icon = Icons.Default.WaterDrop,
                options = waterSources,
                onSelect = { waterSource = it },
                primaryGreen = primaryGreen,
                darkText = darkText
            )

            // Irrigation Type
            CustomDropdown(
                value = irrigationType,
                expanded = irrigationExpanded,
                onExpandedChange = { irrigationExpanded = it },
                label = "Irrigation Type",
                placeholder = "Select irrigation method",
                icon = Icons.Default.Shower,
                options = irrigationTypes,
                onSelect = { irrigationType = it },
                primaryGreen = primaryGreen,
                darkText = darkText
            )

            // ✅ Description - Fixed icon alignment
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                placeholder = { Text("Add farm details...", fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    // ✅ Fixed: Simple icon without alignment issues
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = primaryGreen
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = primaryGreen,
                    cursorColor = primaryGreen,
                    focusedTextColor = darkText,
                    unfocusedTextColor = darkText
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = darkText,
                    lineHeight = 24.sp
                )
            )

            Spacer(Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, primaryGreen),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryGreen
                    ),
                    enabled = !isSaving && !isUploadingImage
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Button(
                    onClick = { updateFarm() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    enabled = !isSaving && !isUploadingImage,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isSaving || isUploadingImage) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (isUploadingImage) "Uploading..." else "Saving...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    } else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Update Farm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// Custom Dropdown
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
    darkText: Color
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.Gray) },
            leadingIcon = { Icon(icon, null, tint = primaryGreen) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryGreen,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = primaryGreen,
                unfocusedLabelColor = Color.Gray,
                focusedLeadingIconColor = primaryGreen,
                unfocusedLeadingIconColor = Color.Gray,
                focusedTextColor = darkText,
                unfocusedTextColor = darkText
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = darkText
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = darkText
                        )
                    },
                    onClick = {
                        onSelect(option)
                        onExpandedChange(false)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
