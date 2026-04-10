package com.smartkrishi.presentation.equipment

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartkrishi.presentation.theme.ThemeState

// =======================================================
// COLORS
// =======================================================
private val PrimaryGreen = Color(0xFF2E7D32)
private val SecondaryGreen = Color(0xFF80CBC4)

// =======================================================
// DATA MODEL
// =======================================================
data class EquipmentItem(
    val name: String,
    val type: String,
    val useCase: String,
    val approxPrice: String,
    val power: String,
    val isRentalListing: Boolean = false,
    val rentPerDay: String? = null,
    val ownerName: String? = null,
    val ownerPhoneMasked: String? = null,
    val location: String? = null,
    val imageUri: Uri? = null,
    val icon: @Composable () -> Unit
)

// =======================================================
// MAIN SCREEN
// =======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(navController: NavController) {
    val isDark by ThemeState.isDarkTheme
    val background = if (isDark) Color(0xFF07130F) else Color(0xFFF3FFF7)
    val cardColor = if (isDark) Color(0xFF10251A) else Color.White
    val textPrimary = if (isDark) Color(0xFFE8F5E9) else Color(0xFF0A130C)
    val textSecondary = if (isDark) Color(0xFFB5CBB8) else Color(0xFF5D6B63)

    // Default equipment
    val defaultEquipment = listOf(
        EquipmentItem(
            name = "3 HP Submersible Pump",
            type = "Irrigation",
            useCase = "Lift water from borewell / tube well for drip & flood irrigation.",
            approxPrice = "₹18,000 – ₹28,000",
            power = "3 HP",
            icon = { Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF42A5F5)) }
        ),
        EquipmentItem(
            name = "Solar Pump Set",
            type = "Solar Irrigation",
            useCase = "Run pump using solar panels — reduce diesel cost.",
            approxPrice = "₹1.5 – 3.0 Lakh",
            power = "7.5 HP",
            icon = { Icon(Icons.Default.Bolt, null, tint = Color(0xFFFFB300)) }
        ),
        EquipmentItem(
            name = "Rotavator",
            type = "Tillage",
            useCase = "Primary & secondary tillage, seed-bed preparation.",
            approxPrice = "₹80,000 – ₹1.4 Lakh",
            power = "Requires 35+ HP tractor",
            icon = { Icon(Icons.Default.Agriculture, null, tint = PrimaryGreen) }
        ),
        EquipmentItem(
            name = "Sprayer",
            type = "Plant Protection",
            useCase = "Spraying fertiliser & pesticide on crops.",
            approxPrice = "₹3,000 – ₹15,000",
            power = "Power/Battery",
            icon = { Icon(Icons.Default.Build, null, tint = Color(0xFF8E24AA)) }
        )
    )

    // User rental listings
    var rentalListings by remember { mutableStateOf<List<EquipmentItem>>(emptyList()) }

    // Dialog trigger
    var showAddRentalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Equipment & Tools", color = textPrimary, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF10251A) else Color(0xFFC8E6C9)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddRentalDialog = true },
                icon = { Icon(Icons.Default.AddBusiness, null) },
                text = { Text("Add equipment for rent") },
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

                // SECTION — EXISTING RENTAL LISTING
                if (rentalListings.isNotEmpty()) {
                    item {
                        Text("Available for Rent", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = textPrimary)
                        Spacer(Modifier.height(6.dp))
                    }
                    items(rentalListings) { eq ->
                        EquipmentCard(eq, cardColor, textPrimary, textSecondary, isRental = true) {
                            // Rent request click event
                        }
                    }
                    item { Spacer(Modifier.height(10.dp)) }
                }

                // SECTION — PRODUCT CATALOG
                item {
                    Text("Equipment Catalog", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = textPrimary)
                    Spacer(Modifier.height(6.dp))
                }

                items(defaultEquipment) { eq ->
                    EquipmentCard(eq, cardColor, textPrimary, textSecondary, isRental = false)
                }
            }
        }
    }

    if (showAddRentalDialog) {
        AddRentalDialog(
            onDismiss = { showAddRentalDialog = false },
            onAdd = { newItem ->
                rentalListings = rentalListings + newItem
                showAddRentalDialog = false
            }
        )
    }
}

// =======================================================
// CARD UI
// =======================================================
@Composable
private fun EquipmentCard(
    item: EquipmentItem,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    isRental: Boolean,
    onRentClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(cardColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    item.icon()
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(item.name, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    Text(item.type, fontSize = 12.sp, color = textSecondary)
                }
            }

            Text("Use: ${item.useCase}", fontSize = 12.sp, color = textPrimary)
            Text("Power: ${item.power}", fontSize = 12.sp, color = textSecondary)

            if (!isRental) {
                Text("Approx price: ${item.approxPrice}", fontSize = 12.sp, color = PrimaryGreen)
            } else {
                Divider(color = textSecondary.copy(alpha = 0.3f))
                Text("Rental price: ${item.rentPerDay}", fontSize = 12.sp, color = SecondaryGreen)
                Text("Owner: ${item.ownerName}", fontSize = 12.sp, color = textPrimary)
                Text("Location: ${item.location}", fontSize = 12.sp, color = textPrimary)
                Text("Contact: ${item.ownerPhoneMasked}", fontSize = 12.sp, color = textSecondary)

                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onRentClick?.invoke() },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Request Rent", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// =======================================================
// ADD RENT FORM
// =======================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRentalDialog(
    onDismiss: () -> Unit,
    onAdd: (EquipmentItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rentPrice by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List Equipment for Rent") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Equipment name") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(value = rentPrice, onValueChange = { rentPrice = it },
                    label = { Text("Rental Price (per day)") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(value = ownerName, onValueChange = { ownerName = it },
                    label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text("Owner Phone") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(value = location, onValueChange = { location = it },
                    label = { Text("Location") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (imageUri != null) "Image selected ✓" else "No image selected")

                    TextButton(onClick = { picker.launch("image/*") }) {
                        Icon(Icons.Default.Image, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Upload")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(
                    EquipmentItem(
                        name = name,
                        type = "Farmer Machine",
                        useCase = "User-defined equipment",
                        approxPrice = "",
                        power = "",
                        isRentalListing = true,
                        rentPerDay = rentPrice,
                        ownerName = ownerName,
                        location = location,
                        ownerPhoneMasked = if (phone.length >= 10) phone.replaceRange(3, 7, "****") else "Hidden",
                        icon = { Icon(Icons.Default.Build, null, tint = PrimaryGreen) }
                    )
                )
            }) {
                Text("Add Listing")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
