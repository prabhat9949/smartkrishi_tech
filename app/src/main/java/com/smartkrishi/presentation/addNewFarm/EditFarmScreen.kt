package com.smartkrishi.presentation.addNewFarm

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.google.firebase.storage.FirebaseStorage
import com.smartkrishi.presentation.home.FarmViewModel
import com.smartkrishi.presentation.model.Farm
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/* ---------- FIXED LIGHT COLORS ---------- */

private val PrimaryGreen = Color(0xFF2E7D32)
private val LightGreenBg = Color(0xFFF4FBF6)
private val BorderGray = Color(0xFFBDBDBD)
private val TextDark = Color(0xFF212121)

/* ---------- SCREEN ---------- */

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

    var name by remember { mutableStateOf(farm.name) }
    var cropType by remember { mutableStateOf(farm.cropType) }
    var soilType by remember { mutableStateOf(farm.soilType) }
    var customSoilType by remember { mutableStateOf("") }
    var acres by remember { mutableStateOf(farm.acres.toString()) }
    var location by remember { mutableStateOf(farm.location) }
    var waterSource by remember { mutableStateOf(farm.waterSource) }
    var irrigationType by remember { mutableStateOf(farm.irrigationType) }
    var description by remember { mutableStateOf(farm.description) }

    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf(farm.imageUrl) }

    var saving by remember { mutableStateOf(false) }

    var cropExpanded by remember { mutableStateOf(false) }
    var soilExpanded by remember { mutableStateOf(false) }
    var waterExpanded by remember { mutableStateOf(false) }
    var irrigationExpanded by remember { mutableStateOf(false) }

    val cropTypes = listOf("🌾 Wheat","🍚 Rice","🌽 Corn","🌿 Cotton","🎋 Sugarcane","🌱 Other")
    val soilTypes = listOf("🟤 Clay","🟡 Sandy","🟢 Loamy","⚫ Black","✏️ Custom")
    val waterSources = listOf("💧 Borewell","🏞️ Canal","🌊 River","✏️ Other")
    val irrigationTypes = listOf("💧 Drip","🌊 Sprinkler","🚰 Flood","✏️ Other")

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> newImageUri = uri }

    suspend fun uploadImage(uri: Uri): String? {
        return try {
            val ref = FirebaseStorage.getInstance()
                .reference.child("farm_images/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) { null }
    }

    fun updateFarm() {
        if (name.isBlank() || cropType.isBlank() || soilType.isBlank() ||
            acres.isBlank() || location.isBlank()
        ) {
            Toast.makeText(context, "Fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        saving = true

        scope.launch {
            var imageUrl = existingImageUrl
            if (newImageUri != null) {
                imageUrl = uploadImage(newImageUri!!) ?: existingImageUrl
            }

            val updatedFarm = farm.copy(
                name = name.trim(),
                cropType = cropType.trim(),
                soilType = if (soilType.contains("Custom")) customSoilType else soilType,
                acres = acres.toIntOrNull() ?: farm.acres,
                location = location.trim(),
                imageUrl = imageUrl,
                waterSource = waterSource,
                irrigationType = irrigationType,
                description = description
            )

            viewModel.updateFarm(
                updatedFarm,
                onSuccess = {
                    saving = false
                    Toast.makeText(context, "Farm updated", Toast.LENGTH_SHORT).show()
                    onFarmUpdated()
                },
                onFailure = {
                    saving = false
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PrimaryGreen,
            background = LightGreenBg,
            surface = Color.White,
            onSurface = TextDark
        )
    ) {
        Scaffold(
            containerColor = LightGreenBg,
            topBar = {
                TopAppBar(
                    title = { Text("Edit Farm", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                ImagePickerSection(existingImageUrl, newImageUri) {
                    imagePicker.launch("image/*")
                }

                LightTextField(name, "Farm Name *") { name = it }

                LightDropdown(
                    label = "Crop Type *",
                    value = cropType,
                    expanded = cropExpanded,
                    options = cropTypes,
                    onSelect = {
                        cropType = it
                        cropExpanded = false
                    },
                    onExpandChange = { cropExpanded = it }
                )

                LightDropdown(
                    label = "Soil Type *",
                    value = soilType,
                    expanded = soilExpanded,
                    options = soilTypes,
                    onSelect = {
                        soilType = it
                        soilExpanded = false
                    },
                    onExpandChange = { soilExpanded = it }
                )

                AnimatedVisibility(soilType.contains("Custom")) {
                    LightTextField(customSoilType, "Custom Soil Type") {
                        customSoilType = it
                    }
                }

                LightTextField(acres, "Area (Acres) *", KeyboardType.Number) { acres = it }
                LightTextField(location, "Location *") { location = it }

                Divider()

                LightDropdown(
                    label = "Water Source",
                    value = waterSource,
                    expanded = waterExpanded,
                    options = waterSources,
                    onSelect = {
                        waterSource = it
                        waterExpanded = false
                    },
                    onExpandChange = { waterExpanded = it }
                )

                LightDropdown(
                    label = "Irrigation Type",
                    value = irrigationType,
                    expanded = irrigationExpanded,
                    options = irrigationTypes,
                    onSelect = {
                        irrigationType = it
                        irrigationExpanded = false
                    },
                    onExpandChange = { irrigationExpanded = it }
                )

                LightTextField(description, "Description", KeyboardType.Text, 4) {
                    description = it
                }

                Button(
                    onClick = { updateFarm() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = !saving
                ) {
                    if (saving) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    else Text("Update Farm", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ---------- HELPERS ---------- */

@Composable
private fun ImagePickerSection(
    imageUrl: String?,
    newImage: Uri?,
    onPick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEAF6EF))
            .clickable { onPick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            newImage != null ->
                AsyncImage(newImage, null, contentScale = ContentScale.Crop)
            !imageUrl.isNullOrBlank() ->
                AsyncImage(imageUrl, null, contentScale = ContentScale.Crop)
            else -> Icon(Icons.Default.CameraAlt, null, tint = Color.Gray)
        }
    }
}

@Composable
private fun LightTextField(
    value: String,
    label: String,
    keyboard: KeyboardType = KeyboardType.Text,
    lines: Int = 1,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        maxLines = lines,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = BorderGray,
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LightDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    options: List<String>,
    onSelect: (String) -> Unit,
    onExpandChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(expanded, onExpandChange) {

        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(it.take(2))
                            Spacer(Modifier.width(10.dp))
                            Text(it.drop(2).trim(), color = TextDark)
                        }
                    },
                    onClick = { onSelect(it) }
                )
            }
        }
    }
}
