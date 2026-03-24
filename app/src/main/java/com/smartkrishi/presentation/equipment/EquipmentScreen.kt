package com.smartkrishi.presentation.equipment

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import java.util.UUID

// ────────────────────────────────────────────────
// Colors (matched to crop theme, adjusted for visibility)
private val PrimaryGreen = Color(0xFF2E7D32)
private val SecondaryGreen = Color(0xFF388E3C)
private val LightGreen = Color(0xFFE8F5E9)
private val AccentGreen = Color(0xFFA5D6A7)

private val DarkBg = Color(0xFF0F2F1D)
private val DarkCardColor = Color(0xFF578571)

private val TextPrimaryLight = Color(0xFF1B1B1B)
private val TextSecondaryLight = Color(0xFF555555)

private val TextPrimaryDark = Color(0xFFFFFFFF)
private val TextSecondaryDark = Color(0xFFB7E4C7)

private val BorderGray = Color(0xFFDADADA)
private val WarningRed = Color(0xFFD32F2F)
// ────────────────────────────────────────────────
// Data Model
data class Equipment(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val idCode: String = "",
    val status: String = "IN USE",
    val nextService: String = "",
    val operatingHours: Int = 0,
    val isMarketplace: Boolean = false,
    val tag: String = "NEW", // NEW/USED
    val brand: String = "",
    val price: String = "",
    val rentPerHour: Double = 0.0,
    val totalCost: Double = 0.0,
    val ownerName: String = "",
    val ownerPhone: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val isLocal: Boolean = false, // New flag for local items
    val lastEdited: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
data class EquipmentTemplate(
    val name: String,
    val type: String,
    val imageUrl: String
)

val predefinedEquipment = listOf(
    EquipmentTemplate(
        "Tractor",
        "Tractors",
        "https://cdn.tractorsdekho.com/in/mahindra/yuvraj-215-nxt/mahindra-yuvraj-215-nxt-21206.jpg?impolicy=resize&imwidth=480"
    ),
    EquipmentTemplate(
        "Harvester",
        "Harvesters",
        "https://media.istockphoto.com/id/458307297/photo/john-deere-combine.jpg?s=612x612&w=0&k=20&c=SUIosnDYn7CW2gsgPFffU2MeZJ8BYsmcZJjQg4-UR18="
    ),
    EquipmentTemplate(
        "Sprayer",
        "Sprayers",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRs1pxkot3Vrqwk7z_v-XRiSYs9k7wrEsgEJA&s"
    ),
    EquipmentTemplate(
        "Irrigation Pump",
        "Irrigation",
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTXnpKC5n9eAw3VG_cAVP3sLFYuZtKK9cyQuA&s"
    ),
    EquipmentTemplate(
        "Rotavator",
        "Other",
        "https://www.mahindratractor.com/sites/default/files/styles/1532x912/public/2025-02/what-is-a-rotavator-uses-and-benefits-explained-detail.webp?itok=cYs8bgbI"
    )
)
// ────────────────────────────────────────────────
// Main Entry Point
// ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EquipmentScreen(onBack: () -> Unit = {}) {
    val isDark = false
    val bgColor = Color(0xFFF4FBF6)
    val textPrimary = TextPrimaryLight
    val textSecondary = TextSecondaryLight

    var showCart by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val equipmentRef = FirebaseDatabase.getInstance().getReference("equipment")
    val marketplaceRef = FirebaseDatabase.getInstance().getReference("marketplace")
    var cartItems = remember { mutableStateListOf<Equipment>() }
    var equipmentList by remember { mutableStateOf<List<Equipment>>(emptyList()) } // From Firebase my equipment
    var marketplaceList by remember { mutableStateOf<List<Equipment>>(emptyList()) } // From Firebase marketplace
    var localEquipment = remember { mutableStateListOf<Equipment>() } // Local

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Marketplace, 1: My Equipment
    var selectedFilter by remember { mutableStateOf("All") }

    var currentScreen by remember { mutableStateOf("list") }
    var editingEquipment by remember { mutableStateOf<Equipment?>(null) }
    var showDetailDialog by remember { mutableStateOf<Equipment?>(null) }

    // Firebase listeners for my equipment
    DisposableEffect(Unit) {
        val equipmentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull {
                    it.getValue(Equipment::class.java)
                }.sortedByDescending { it.timestamp }
                equipmentList = list
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) {
                errorMessage = error.message
                isLoading = false
            }
        }

        equipmentRef.addValueEventListener(equipmentListener)

        onDispose {
            equipmentRef.removeEventListener(equipmentListener)
        }
    }

    // Firebase listeners for marketplace (all users)
    DisposableEffect(Unit) {
        val marketplaceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull {
                    it.getValue(Equipment::class.java)
                }.sortedByDescending { it.timestamp }
                marketplaceList = list
                isLoading = false
            }
            override fun onCancelled(error: DatabaseError) {
                errorMessage = error.message
                isLoading = false
            }
        }

        marketplaceRef.addValueEventListener(marketplaceListener)

        onDispose {
            marketplaceRef.removeEventListener(marketplaceListener)
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Equipment", color = textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                    }
                },
                actions = {
                    IconButton(onClick = { showCart = true }){
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = textPrimary)
                    }
                    if (showCart) {
                        Dialog(onDismissRequest = { showCart = false }) {
                            val totalAmount = cartItems.sumOf {
                                it.price.replace("₹", "").toDoubleOrNull() ?: it.totalCost
                            }
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {

                                    Text(
                                        "My Cart",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = textPrimary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (cartItems.isEmpty()) {
                                        Text("Cart is empty", color = textSecondary)
                                    } else {
                                        cartItems.forEach { item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(item.name, fontWeight = FontWeight.SemiBold, color = textPrimary)
                                                    Text(item.price, color = PrimaryGreen)
                                                }
                                                IconButton(onClick = { cartItems.remove(item) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = WarningRed)
                                                }
                                            }
                                            Divider()
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Total: ₹ $totalAmount",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = textPrimary
                                    )
                                    Button(
                                        onClick = { showCart = false },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                    ) {
                                        Text("Checkout", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Bell", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    scrolledContainerColor = bgColor
                )
            )
        },
        floatingActionButton = {
            if (currentScreen == "list" && selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = { currentScreen = "add" },
                    containerColor = PrimaryGreen,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add equipment", tint = Color.White)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0; selectedFilter = "All" },
                    text = { Text("Marketplace") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1; selectedFilter = "All" },
                    text = { Text("My Equipment") }
                )
            }
            showDetailDialog?.let { equipment ->
                EquipmentDetailDialog(
                    equipment = equipment,
                    isMarketplace = selectedTabIndex == 0,
                    onDismiss = { showDetailDialog = null },
                    onAddToCart = {
                        cartItems.add(equipment)
                        Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show()
                    },
                    onEdit = {
                        editingEquipment = equipment
                        currentScreen = "edit"
                        showDetailDialog = null
                    },
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    isDark = isDark
                )
            }
            if (currentScreen == "list") {
                val isMarketplaceTab = selectedTabIndex == 0
                val filteredList = if (isMarketplaceTab) {
                    marketplaceList
                } else {
                    (localEquipment + equipmentList).distinctBy { it.id }
                }.filter {
                    (it.name.contains(searchQuery, ignoreCase = true) ||
                            it.brand.contains(searchQuery, ignoreCase = true)) &&
                            (selectedFilter == "All" || it.type == selectedFilter)
                }

                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        placeholder = { Text("Search equipment, brands...", color = textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = textSecondary) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryGreen,
                            unfocusedBorderColor = BorderGray,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = if (isMarketplaceTab) listOf("All", "Tractors", "Irrigation", "Harvesting") else listOf("All", "Tractors", "Harvesters", "Sprayers")
                        filters.forEach { label ->
                            FilterChip(
                                selected = selectedFilter == label,
                                onClick = { selectedFilter = label },
                                label = { Text(label, color = if (selectedFilter == label) Color.White else textPrimary) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
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
                    } else if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No equipment found. Add a new one!", color = textSecondary)
                        }
                    } else {
                        if (isMarketplaceTab) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 160.dp),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredList) { equipment ->
                                    MarketplaceCard(
                                        equipment = equipment,
                                        onClick = { showDetailDialog = equipment },
                                        textPrimary = textPrimary,
                                        textSecondary = textSecondary,
                                        isDark = isDark
                                    )
                                }
                            }
                        } else {
                            val total = filteredList.size
                            val active = filteredList.count { it.status == "IN USE" || it.status == "IDLE" }
                            val service = filteredList.count { it.status == "MAINTENANCE" }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SummaryCard("TOTAL", total.toString(), textPrimary, textSecondary, Modifier.weight(1f))
                                SummaryCard("ACTIVE", active.toString(), textPrimary, textSecondary, Modifier.weight(1f))
                                SummaryCard("SERVICE", service.toString(), textPrimary, textSecondary, Modifier.weight(1f))
                            }

                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredList) { equipment ->
                                    MyEquipmentCard(
                                        equipment = equipment,
                                        onClick = { showDetailDialog = equipment },
                                        textPrimary = textPrimary,
                                        textSecondary = textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                EquipmentFormScreen(
                    isEditMode = currentScreen == "edit",
                    initialEquipment = editingEquipment,
                    onBack = {
                        currentScreen = "list"
                        editingEquipment = null
                    },
                    onSave = { equipment ->

                        val key = if (equipment.id.isBlank())
                            UUID.randomUUID().toString()
                        else equipment.id

                        if (equipment.isMarketplace) {

                            // Save to Firebase Marketplace
                            marketplaceRef.child(key)
                                .setValue(equipment.copy(id = key, isLocal = false))
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Saved to Marketplace", Toast.LENGTH_SHORT).show()
                                    currentScreen = "list"
                                    editingEquipment = null
                                }

                        } else {

                            // Save Locally ONLY
                            val updated = equipment.copy(
                                id = key,
                                isLocal = true
                            )

                            localEquipment.removeAll { it.id == key }
                            localEquipment.add(updated)

                            Toast.makeText(context, "Saved Locally", Toast.LENGTH_SHORT).show()
                            currentScreen = "list"
                            editingEquipment = null
                        }
                    },
                    onDelete = { id ->
                        if (editingEquipment?.isMarketplace == true) {
                            marketplaceRef.child(id).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Removed from Marketplace", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            localEquipment.removeAll { it.id == id }
                            Toast.makeText(context, "Removed locally", Toast.LENGTH_SHORT).show()
                        }
                        currentScreen = "list"
                        editingEquipment = null
                    },
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    keyboardController = keyboardController,
                    padding = PaddingValues(0.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, textPrimary: Color, textSecondary: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightGreen

        ),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(value, color = textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MarketplaceCard(
    equipment: Equipment,
    onClick: () -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean
) {val formattedDate = SimpleDateFormat(
    "dd MMM yyyy",
    Locale.getDefault()
).format(Date(equipment.timestamp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =  Color.White
        ),
        border = BorderStroke(1.dp, BorderGray),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            AsyncImage(
                model = equipment.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(12.dp)
            ){

                Text(
                    equipment.name,
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Brand: ${equipment.brand}",
                    color = textSecondary,
                    fontSize = 12.sp
                )

                Text(
                    "Listed: $formattedDate",
                    color = textSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    equipment.price.ifBlank { "₹ ${equipment.totalCost}" },
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
@Composable
fun MyEquipmentCard(
    equipment: Equipment,
    onClick: () -> Unit,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGray),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = equipment.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(equipment.name, color = textPrimary, fontWeight = FontWeight.Bold)
                Text("ID: ${equipment.idCode}", color = textSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                val statusColor = when (equipment.status) {
                    "IN USE" -> PrimaryGreen
                    "MAINTENANCE" -> WarningRed
                    "IDLE" -> Color.Gray
                    else -> Color.Gray
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        equipment.status,
                        color = statusColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Next Service: ${equipment.nextService}", color = textSecondary, fontSize = 12.sp)
                }
                Text("${equipment.operatingHours} Operating Hours", color = textSecondary, fontSize = 12.sp)
            }
            Text("Manage >", color = PrimaryGreen, modifier = Modifier.align(Alignment.CenterVertically ))
        }
    }
}
@Composable
fun EquipmentDetailDialog(
    equipment: Equipment,
    isMarketplace: Boolean,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
    onEdit: () -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) DarkCardColor else LightGreen
            ),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                AsyncImage(
                    model = equipment.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    equipment.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Text(
                    equipment.brand,
                    color = textSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow("Type", equipment.type, textPrimary, textSecondary)
                DetailRow("Status", equipment.status, textPrimary, textSecondary)
                DetailRow("Next Service", equipment.nextService, textPrimary, textSecondary)
                DetailRow("Operating Hours", "${equipment.operatingHours}", textPrimary, textSecondary)
                DetailRow("Owner", equipment.ownerName, textPrimary, textSecondary)
                DetailRow("Phone", equipment.ownerPhone, textPrimary, textSecondary)
                DetailRow("Location", equipment.location, textPrimary, textSecondary)
                DetailRow("Price", equipment.price, textPrimary, textSecondary)

                Spacer(modifier = Modifier.height(20.dp))

                if (isMarketplace) {
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Cart", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Equipment", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
@Composable
fun DetailRow(
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            color = textSecondary
        )
        Text(
            value,
            color = textPrimary
        )
    }
}
// ────────────────────────────────────────────────
// FORM SCREEN (Add + Edit)
// ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EquipmentFormScreen(
    isEditMode: Boolean,
    initialEquipment: Equipment?,
    onBack: () -> Unit,
    onSave: (Equipment) -> Unit,
    onDelete: (String) -> Unit,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    keyboardController: SoftwareKeyboardController?,
    padding: PaddingValues
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialEquipment?.name ?: "") }
    var type by remember { mutableStateOf(initialEquipment?.type ?: "") }
    var idCode by remember { mutableStateOf(initialEquipment?.idCode ?: "") }
    var status by remember { mutableStateOf(initialEquipment?.status ?: "IN USE") }
    var nextService by remember { mutableStateOf(initialEquipment?.nextService ?: "") }
    var operatingHours by remember { mutableStateOf(initialEquipment?.operatingHours?.toString() ?: "0") }
    var isMarketplace by remember { mutableStateOf(initialEquipment?.isMarketplace ?: false) }
    var tag by remember { mutableStateOf(initialEquipment?.tag ?: "NEW") }
    var brand by remember { mutableStateOf(initialEquipment?.brand ?: "") }
    var price by remember { mutableStateOf(initialEquipment?.price ?: "") }
    var rentPerHour by remember { mutableStateOf(initialEquipment?.rentPerHour?.toString() ?: "") }
    var totalCost by remember { mutableStateOf(initialEquipment?.totalCost?.toString() ?: "") }
    var ownerName by remember { mutableStateOf(initialEquipment?.ownerName ?: "") }
    var ownerPhone by remember { mutableStateOf(initialEquipment?.ownerPhone ?: "") }
    var location by remember { mutableStateOf(initialEquipment?.location ?: "") }
    var imageUrl by remember { mutableStateOf(initialEquipment?.imageUrl ?: "") }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }



    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val calendar = remember { Calendar.getInstance() }

    if (nextService.isNotBlank()) {
        try { calendar.time = dateFormat.parse(nextService)!! } catch (e: Exception) {}
    }

    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            calendar.set(y, m, d)
            nextService = dateFormat.format(calendar.time)
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
                        ,
                    contentAlignment = Alignment.Center
                ) {
                    val imageSource = localImageUri ?: imageUrl.takeIf { it.isNotBlank() }
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                    } else {
                        Icon(Icons.Default.CameraAlt, null, tint = PrimaryGreen, modifier = Modifier.size(48.dp))
                        Text(
                            "Tap to Upload Image",
                            modifier = Modifier.padding(top = 60.dp),
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                var equipmentExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = equipmentExpanded,
                    onExpandedChange = { equipmentExpanded = !equipmentExpanded }
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Equipment", color = textSecondary) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipmentExpanded)
                        },
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
                        expanded = equipmentExpanded,
                        onDismissRequest = { equipmentExpanded = false }
                    ) {
                        predefinedEquipment.forEach { template ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        template.name,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    name = template.name
                                    type = template.type
                                    imageUrl = template.imageUrl
                                    equipmentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))



                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = idCode,
                    onValueChange = { idCode = it },
                    label = { Text("ID Code", color = textSecondary) },
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
                        listOf("IN USE", "IDLE", "MAINTENANCE").forEach { opt ->
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nextService,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Next Service", color = textSecondary) },
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

                OutlinedTextField(
                    value = operatingHours,
                    onValueChange = { if (it.toIntOrNull() != null || it.isEmpty()) operatingHours = it },
                    label = { Text("Operating Hours", color = textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                CheckboxRow("List in Marketplace", isMarketplace, onCheckedChange = { isMarketplace = it }, textPrimary)

                if (isMarketplace) {
                    Spacer(modifier = Modifier.height(16.dp))

                    var tagExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = tagExpanded,
                        onExpandedChange = { tagExpanded = !tagExpanded }
                    ) {
                        OutlinedTextField(
                            value = tag,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tag", color = textSecondary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagExpanded) },
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
                            expanded = tagExpanded,
                            onDismissRequest = { tagExpanded = false }
                        ) {
                            listOf("NEW", "USED").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, color = textPrimary) },
                                    onClick = {
                                        tag = opt
                                        tagExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = rentPerHour,
                    onValueChange = { if (it.toDoubleOrNull() != null || it.isEmpty()) rentPerHour = it },
                    label = { Text("Rent Per Hour", color = textSecondary) },
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
                    value = totalCost,
                    onValueChange = { if (it.toDoubleOrNull() != null || it.isEmpty()) totalCost = it },
                    label = { Text("Total Cost", color = textSecondary) },
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
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner Name", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = ownerPhone,
                    onValueChange = { ownerPhone = it },
                    label = { Text("Owner Phone", color = textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location", color = textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        keyboardController?.hide()
                        val rentHour = rentPerHour.toDoubleOrNull() ?: 0.0
                        val cost = totalCost.toDoubleOrNull() ?: 0.0
                        val hours = operatingHours.toIntOrNull() ?: 0

                        if (name.isBlank()) {
                            Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val newEquipment = Equipment(
                            id = initialEquipment?.id ?: "",
                            name = name.trim(),
                            type = type,
                            idCode = idCode.trim(),
                            status = status,
                            nextService = nextService,
                            operatingHours = hours,
                            isMarketplace = isMarketplace,
                            tag = tag,
                            brand = brand.trim(),
                            price = price.trim(),
                            rentPerHour = rentHour,
                            totalCost = cost,
                            ownerName = ownerName.trim(),
                            ownerPhone = ownerPhone.trim(),
                            location = location.trim(),
                            imageUrl = imageUrl,
                            isLocal = !isMarketplace,
                            lastEdited = System.currentTimeMillis(),
                            timestamp = initialEquipment?.timestamp ?: System.currentTimeMillis()
                        )

                        onSave(newEquipment)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save", fontSize = 16.sp, color = Color.White)
                }

                if (isEditMode && initialEquipment?.id?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { onDelete(initialEquipment.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningRed),
                        border = BorderStroke(1.dp, WarningRed),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(label, color = textColor)
    }
}