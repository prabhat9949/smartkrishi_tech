package com.smartkrishi.presentation.crops
import androidx.compose.ui.text.style.TextAlign
import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.database.*

import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.SoftwareKeyboardController


// ────────────────────────────────────────────────
// Colors (enhanced vibrant green theme for agriculture)
private val PrimaryGreen = Color(0xFF34D399) // Vibrant emerald green
private val SecondaryGreen = Color(0xFF059669) // Deeper green
private val LightGreen = Color(0xFFECFEF4) // Softer light green
private val AccentGreen = Color(0xFF6EE7B7) // Brighter accent
private val WarningRed = Color(0xFFEF4444)
private val BorderGray = Color(0xFFE5E7EB)
private val TextPrimaryLight = Color(0xFF111827)
private val TextSecondaryLight = Color(0xFF6B7280)
private val TextPrimaryDark = Color(0xFFF9FAFB)
private val TextSecondaryDark = Color(0xFF9CA3AF)
private val DarkCardColor = Color(0xFF1F2937)
data class CropOption(
    val name: String,
    val imageUrl: String
)

val cropOptions = listOf(
    CropOption("Wheat", "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b"),
    CropOption("Rice", "https://img.jagranjosh.com/images/2025/09/12/article/image/scientific-name-of-rice-1757656335124.jpg"),
    CropOption("Maize", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTabjkABlfcZvwF0F3wgcH-3676jfEq6gcMHQ&s"),
    CropOption("Sugarcane", "https://www.mahagro.com/cdn/shop/articles/iStock_000063947343_Medium_4e1c882b-faf0-4487-b45b-c2b557d32442.jpg?v=1541408129&width=1100"),
    CropOption("Cotton", "https://images.ctfassets.net/3s5io6mnxfqz/4TV7YTCO1DJuMhhn7RD1Ol/b5a6c12340e6529a86bc1b557ed2d8f8/AdobeStock_136921602.jpeg"),
    CropOption("Potato", "https://www.apnikheti.com/upload/crops/4727idea99293460-potato-Download.jpg"),
    CropOption("Tomato", "https://static.vecteezy.com/system/resources/previews/049/995/072/non_2x/fresh-tomatoes-in-a-basket-png.png")
)
// ────────────────────────────────────────────────
// Data Model – simplified
data class Crop(
    val id: String = "",
    val name: String = "",
    val variety: String = "",
    val type: String = "Organic",
    val areaAcres: Double = 0.0,
    val harvestingDate: String = "", // Changed from plantingDate
    val quantity: Int = 0,
    val unit: String = "KG",
    val status: String = "In Stock",
    val imageUrl: String = "",
    val lastEdited: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

// ────────────────────────────────────────────────
// Main Entry Point
// ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CropsScreen(onBack: () -> Unit = {}) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val bgColor = Color(0xFFF5FAF5)
    val textPrimary = TextPrimaryLight
    val textSecondary = TextSecondaryLight
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val cropsRef = FirebaseDatabase.getInstance().getReference("crops")

    var crops by remember { mutableStateOf<List<Crop>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Stocks") }

    var currentScreen by remember { mutableStateOf("list") }
    var editingCrop by remember { mutableStateOf<Crop?>(null) }
    var showDetailDialog by remember { mutableStateOf<Crop?>(null) }

    // Firebase listeners
    DisposableEffect(Unit) {
        val cropsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull {
                    it.getValue(Crop::class.java)
                }.sortedByDescending { it.timestamp }
                crops = list
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) {
                errorMessage = error.message
                isLoading = false
            }
        }

        cropsRef.addValueEventListener(cropsListener)

        onDispose {
            cropsRef.removeEventListener(cropsListener)
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Manage Inventory", color = textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    scrolledContainerColor = bgColor
                )
            )
        },
        floatingActionButton = {
            if (currentScreen == "list") {
                FloatingActionButton(
                    onClick = { currentScreen = "add" },
                    containerColor = PrimaryGreen,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add crop", tint = Color.White)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        when (currentScreen) {
            "list" -> {
                CropListScreen(
                    crops = crops,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    searchQuery = searchQuery,
                    selectedFilter = selectedFilter,
                    onSearchChange = { searchQuery = it },
                    onFilterChange = { selectedFilter = it },
                    onShowDetail = { crop -> showDetailDialog = crop },
                    onQuantityChange = { cropId, newQty ->
                        cropsRef.child(cropId).child("quantity").setValue(newQty)
                    },
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    padding = padding
                )
            }
            "add", "edit" -> {
                CropFormScreen(
                    isEditMode = currentScreen == "edit",
                    initialCrop = editingCrop,
                    onBack = {
                        currentScreen = "list"
                        editingCrop = null
                    },
                    onSave = { crop ->
                        val key = if (crop.id.isBlank()) cropsRef.push().key!! else crop.id
                        cropsRef.child(key).setValue(crop.copy(id = key))
                            .addOnSuccessListener {
                                Toast.makeText(context, "Crop saved", Toast.LENGTH_SHORT).show()
                                currentScreen = "list"
                                editingCrop = null
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Save failed: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    },
                    onDelete = { id ->
                        cropsRef.child(id).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Crop removed", Toast.LENGTH_SHORT).show()
                                currentScreen = "list"
                                editingCrop = null
                            }
                    },
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    keyboardController = keyboardController,
                    padding = padding
                )
            }
        }

        showDetailDialog?.let { crop ->
            Dialog(onDismissRequest = { showDetailDialog = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor =  Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Crop Details",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AsyncImage(
                            model = crop.imageUrl.ifBlank { "https://via.placeholder.com/300" },
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        DetailRow("Name", crop.name, textPrimary)
                        DetailRow("Type", crop.type, textPrimary)
                        DetailRow("Variety", crop.variety, textPrimary)
                        DetailRow("Area", "${crop.areaAcres} Acres", textPrimary)
                        DetailRow("Harvesting Date", crop.harvestingDate, textPrimary)
                        DetailRow("Quantity", "${crop.quantity} ${crop.unit}", textPrimary)
                        DetailRow("Status", crop.status, textPrimary)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    editingCrop = crop
                                    currentScreen = "edit"
                                    showDetailDialog = null
                                },
                                colors = ButtonDefaults.buttonColors(PrimaryGreen)
                            ) {
                                Text("Edit", color = Color.White)
                            }
                            Button(
                                onClick = {
                                    cropsRef.child(crop.id).removeValue()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Crop removed", Toast.LENGTH_SHORT).show()
                                            showDetailDialog = null
                                        }
                                },
                                colors = ButtonDefaults.buttonColors(WarningRed)
                            ) {
                                Text("Remove", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium,
            color = textColor
        )
        Text(
            text = value,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ────────────────────────────────────────────────
// LIST SCREEN
// ────────────────────────────────────────────────
@Composable
fun CropListScreen(
    crops: List<Crop>,
    isLoading: Boolean,
    errorMessage: String?,
    searchQuery: String,
    selectedFilter: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onShowDetail: (Crop) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    padding: PaddingValues
) {
    val filteredCrops = crops.filter {
        (it.name.contains(searchQuery, ignoreCase = true) ||
                it.variety.contains(searchQuery, ignoreCase = true)) &&
                (selectedFilter == "All Stocks" || it.type == selectedFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .imePadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Find crop in inventory...", color = textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = textSecondary) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = TextPrimaryLight,
                    unfocusedTextColor = TextPrimaryLight,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                listOf("All Stocks", "Organic", "Hybrid", "Heirloom").forEach { label ->
                    FilterChip(
                        selected = selectedFilter == label,
                        onClick = { onFilterChange(label) },
                        label = { Text(label, color = if (selectedFilter == label) Color.White else textPrimary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $errorMessage", color = WarningRed)
            }
        } else if (filteredCrops.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No crops found. Add a new one!", color = textSecondary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredCrops) { crop ->
                    CropListItem(
                        crop = crop,
                        onClick = { onShowDetail(crop) },
                        onQuantityChange = { newQty ->
                            onQuantityChange(crop.id, newQty)
                        },
                        isDark = isDark,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun CropListItem(
    crop: Crop,
    onClick: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = crop.imageUrl.ifBlank { "https://via.placeholder.com/120" },
                contentDescription = null,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = crop.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = crop.type.uppercase(),
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (crop.variety.isNotBlank()) {
                        Text(
                            text = crop.variety,
                            color = textSecondary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Harvest: ${crop.harvestingDate}",
                        color = textSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusColor = when (crop.status) {
                    "Sold Out" -> WarningRed
                    "Few Stock Remaining" -> Color(0xFFF59E0B)
                    else -> PrimaryGreen
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = crop.status.uppercase(),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { if (crop.quantity > 0) onQuantityChange(crop.quantity - 1) }) {
                        Icon(Icons.Default.RemoveCircleOutline, null, tint = textSecondary)
                    }
                    Text(
                        text = "${crop.quantity} ${crop.unit}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = textPrimary
                    )
                    IconButton(onClick = { onQuantityChange(crop.quantity + 1) }) {
                        Icon(Icons.Default.AddCircleOutline, null, tint = PrimaryGreen)
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────
// FORM SCREEN (Add + Edit)
// ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CropFormScreen(
    isEditMode: Boolean,
    initialCrop: Crop?,
    onBack: () -> Unit,
    onSave: (Crop) -> Unit,
    onDelete: (String) -> Unit,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    keyboardController: SoftwareKeyboardController?,
    padding: PaddingValues
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialCrop?.name ?: "") }
    var variety by remember { mutableStateOf(initialCrop?.variety ?: "") }
    var type by remember { mutableStateOf(initialCrop?.type ?: "Organic") }
    var area by remember { mutableStateOf(initialCrop?.areaAcres?.toString() ?: "") }
    var harvestingDate by remember { mutableStateOf(initialCrop?.harvestingDate ?: "") }
    var quantity by remember { mutableStateOf(initialCrop?.quantity?.toString() ?: "0") }
    var unit by remember { mutableStateOf(initialCrop?.unit ?: "KG") }
    var status by remember { mutableStateOf(initialCrop?.status ?: "In Stock") }
    var imageUrl by remember { mutableStateOf(initialCrop?.imageUrl ?: "") }

    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            localImageUri = it
            imageUrl = it.toString()   // store local URI string in Firebase
        }
    }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val calendar = remember { Calendar.getInstance() }

    if (harvestingDate.isNotBlank()) {
        try { calendar.time = dateFormat.parse(harvestingDate)!! } catch (_: Exception) {}
    }

    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            calendar.set(y, m, d)
            harvestingDate = dateFormat.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) DarkCardColor else LightGreen)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentGreen)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val imageSource = localImageUri ?: imageUrl.takeIf { it.isNotBlank() }

                    if (imageSource != null) {
                        AsyncImage(
                            model = imageSource,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Tap to Select Image",
                            modifier = Modifier.padding(top = 60.dp),
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                var cropExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = cropExpanded,
                    onExpandedChange = { cropExpanded = !cropExpanded }
                ) {

                    OutlinedTextField(
                        value = name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Crop", color = TextSecondaryLight) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderGray,
                            focusedTextColor = TextPrimaryLight,
                            unfocusedTextColor = TextPrimaryLight,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = cropExpanded,
                        onDismissRequest = { cropExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        cropOptions.forEach { cropOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        cropOption.name,
                                        color = TextPrimaryLight
                                    )
                                },
                                onClick = {
                                    name = cropOption.name
                                    imageUrl = cropOption.imageUrl
                                    cropExpanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = TextPrimaryLight
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Variety", color = textSecondary) },
                    placeholder = { Text("e.g. Winter Red", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Crop Type", color = textSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        listOf("Organic", "Hybrid", "Heirloom", "GMO-Free").forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, color = textPrimary) },
                                onClick = {
                                    type = opt
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = area,
                    onValueChange = { if (it.toDoubleOrNull() != null || it.isEmpty()) area = it },
                    label = { Text("Area (Acres)", color = textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = harvestingDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Harvesting Date", color = textSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.CalendarMonth, null, tint = textSecondary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                var statusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status", color = textSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        listOf("In Stock", "Few Stock Remaining", "Sold Out").forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, color = textPrimary) },
                                onClick = {
                                    status = opt
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.toIntOrNull() != null || it.isEmpty()) quantity = it },
                        label = { Text("Quantity", color = textSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit", color = textSecondary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        keyboardController?.hide()
                        val qty = quantity.toIntOrNull() ?: 0
                        val acres = area.toDoubleOrNull() ?: 0.0

                        if (name.isBlank()) {
                            Toast.makeText(context, "Crop name is required", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val newCrop = Crop(
                            id = initialCrop?.id ?: "",
                            name = name.trim(),
                            variety = variety.trim(),
                            type = type,
                            areaAcres = acres,
                            harvestingDate = harvestingDate,
                            quantity = qty,
                            unit = unit,
                            status = status,
                            imageUrl = imageUrl,
                            lastEdited = System.currentTimeMillis(),
                            timestamp = initialCrop?.timestamp ?: System.currentTimeMillis()
                        )
                        onSave(newCrop)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (isEditMode) "Update Crop" else "Save Crop",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isEditMode && initialCrop?.id?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { onDelete(initialCrop.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningRed),
                        border = BorderStroke(1.dp, WarningRed),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Remove Crop")
                    }
                }
            }
        }
    }
}