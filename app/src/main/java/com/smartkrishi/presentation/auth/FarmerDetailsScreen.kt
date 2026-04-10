package com.smartkrishi.presentation.auth

import com.smartkrishi.data.model.FarmerProfile
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Locale

private const val TAG = "FarmerDetailsScreen"

// ✅ AADHAAR AUTO-SPACING TRANSFORMATION
class AadhaarVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(12)
        val formatted = buildString {
            for (i in trimmed.indices) {
                append(trimmed[i])
                if (i == 3 || i == 7) append(' ')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                if (offset <= 12) return offset + 2
                return 14
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                return 12
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDetailsScreen(
    email: String,
    onSubmitSuccess: () -> Unit,
    viewModel: FarmerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }

    // NEW FIELDS
    var aadhaarNumber by remember { mutableStateOf("") }
    var farmSize by remember { mutableStateOf("") }
    var farmingExperience by remember { mutableStateOf("") }
    var primaryCrop by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("English") }

    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var fetchingLocation by remember { mutableStateOf(false) }

    // Dropdowns
    var stateExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }

    // Indian States
    val indianStates = listOf(
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
        "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
        "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
        "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
        "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal"
    )

    val languages = listOf("English", "Hindi", "Punjabi", "Bengali", "Tamil", "Telugu", "Marathi", "Gujarati", "Kannada", "Malayalam", "Odia")

    // Light theme colors
    val primaryGreen = Color(0xFF2E7D32)
    val lightGreen = Color(0xFF66BB6A)
    val veryLightGreen = Color(0xFFE8F5E9)
    val backgroundGreen = Color(0xFFF1F8E9)

    val backgroundGradient = Brush.verticalGradient(
        listOf(
            Color.White,
            backgroundGreen.copy(0.3f),
            Color.White
        )
    )

    LaunchedEffect(Unit) {
        Log.d(TAG, "FarmerDetailsScreen loaded for email: $email")
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                fetchCurrentLocation(
                    context = context,
                    onLocationFetched = { addr, st, dist, pin ->
                        if (address.isBlank()) address = addr
                        if (state.isBlank()) state = st
                        if (district.isBlank()) district = dist
                        if (pincode.isBlank()) pincode = pin
                        fetchingLocation = false
                        Toast.makeText(context, "✅ Location fetched", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        fetchingLocation = false
                        Toast.makeText(context, "❌ $it", Toast.LENGTH_LONG).show()
                    }
                )
            }
            else -> {
                fetchingLocation = false
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun requestLocation() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchingLocation = true
                fetchCurrentLocation(
                    context = context,
                    onLocationFetched = { addr, st, dist, pin ->
                        if (address.isBlank()) address = addr
                        if (state.isBlank()) state = st
                        if (district.isBlank()) district = dist
                        if (pincode.isBlank()) pincode = pin
                        fetchingLocation = false
                        Toast.makeText(context, "✅ Location fetched", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        fetchingLocation = false
                        Toast.makeText(context, "❌ $it", Toast.LENGTH_LONG).show()
                    }
                )
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    fun validatePhone(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }

    fun validatePincode(pincode: String): Boolean {
        return pincode.length == 6 && pincode.all { it.isDigit() }
    }

    fun validateAadhaar(aadhaar: String): Boolean {
        return aadhaar.length == 12 && aadhaar.all { it.isDigit() }
    }

    fun submit() {
        Log.d(TAG, "Submit button clicked")

        // Validation
        if (name.isBlank()) {
            errorMessage = "⚠️ Please enter your full name"
            return
        }
        if (!validatePhone(phone)) {
            errorMessage = "⚠️ Please enter a valid 10-digit mobile number"
            return
        }
        if (address.isBlank()) {
            errorMessage = "⚠️ Please enter your address"
            return
        }
        if (state.isBlank()) {
            errorMessage = "⚠️ Please select your state"
            return
        }
        if (district.isBlank()) {
            errorMessage = "⚠️ Please enter your district"
            return
        }
        if (!validatePincode(pincode)) {
            errorMessage = "⚠️ Please enter a valid 6-digit PIN code"
            return
        }
        if (aadhaarNumber.isNotBlank() && !validateAadhaar(aadhaarNumber)) {
            errorMessage = "⚠️ Please enter a valid 12-digit Aadhaar number"
            return
        }

        loading = true
        errorMessage = ""

        val profile = FarmerProfile(
            uid = "",
            email = email.trim(),
            name = name.trim(),
            phone = phone.trim(),
            address = address.trim(),
            state = state.trim(),
            district = district.trim(),
            pincode = pincode.trim(),
            farmingExperience = farmingExperience.toIntOrNull() ?: 0,
            totalLand = farmSize.toDoubleOrNull() ?: 0.0,
            language = language,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            profileImageUrl = "",
            isVerified = false
        )

        Log.d(TAG, "Saving profile via ViewModel for: ${email.trim()}")

        scope.launch {
            val success = viewModel.saveFarmerProfile(profile)
            loading = false

            if (success) {
                Log.d(TAG, "Profile saved successfully")
                Toast.makeText(context, "✅ Profile saved successfully!", Toast.LENGTH_SHORT).show()
                onSubmitSuccess()
            } else {
                Log.e(TAG, "Failed to save profile")
                errorMessage = "❌ Failed to save profile. Please try again."
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Complete Your Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = primaryGreen,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = veryLightGreen
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Column {
                            Text(
                                "Farmer Registration",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen
                            )
                            Text(
                                "Please provide your details",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Error Message
                if (errorMessage.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F)
                            )
                            Text(
                                errorMessage,
                                color = Color(0xFFD32F2F),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Personal Information Section
                SectionHeader("Personal Information", primaryGreen)

                CustomTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = "" },
                    label = "Full Name *",
                    placeholder = "Enter your full name",
                    icon = Icons.Default.Person,
                    primaryGreen = primaryGreen,
                    focusManager = focusManager
                )

                CustomTextField(
                    value = phone,
                    onValueChange = {
                        phone = it.filter(Char::isDigit).take(10)
                        errorMessage = ""
                    },
                    label = "Mobile Number *",
                    placeholder = "10-digit number",
                    icon = Icons.Default.Phone,
                    primaryGreen = primaryGreen,
                    keyboardType = KeyboardType.Phone,
                    focusManager = focusManager,
                    prefix = "+91 "
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Email (Verified)") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = primaryGreen)
                    },
                    trailingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = primaryGreen)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = Color.Gray,
                        disabledLeadingIconColor = primaryGreen,
                        disabledTrailingIconColor = primaryGreen
                    ),
                    enabled = false
                )

                // ID Proof Section
                SectionHeader("ID Proof (Optional)", primaryGreen)

                OutlinedTextField(
                    value = aadhaarNumber,
                    onValueChange = {
                        aadhaarNumber = it.filter(Char::isDigit).take(12)
                        errorMessage = ""
                    },
                    label = { Text("Aadhaar Number") },
                    placeholder = { Text("12-digit Aadhaar number") },
                    leadingIcon = {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = primaryGreen)
                    },
                    visualTransformation = AadhaarVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors(primaryGreen),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Address Section
                SectionHeader("Address Details", primaryGreen)

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; errorMessage = "" },
                    label = { Text("Full Address *") },
                    placeholder = { Text("House no., Street, Landmark") },
                    leadingIcon = {
                        Icon(Icons.Default.Home, contentDescription = null, tint = primaryGreen)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { requestLocation() },
                            enabled = !fetchingLocation
                        ) {
                            if (fetchingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = primaryGreen
                                )
                            } else {
                                Icon(Icons.Default.LocationOn, contentDescription = "Get location", tint = primaryGreen)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors(primaryGreen),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // State Dropdown
                ExposedDropdownMenuBox(
                    expanded = stateExpanded,
                    onExpandedChange = { stateExpanded = !stateExpanded }
                ) {
                    OutlinedTextField(
                        value = state,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("State *") },
                        placeholder = { Text("Select your state") },
                        leadingIcon = {
                            Icon(Icons.Default.Map, contentDescription = null, tint = primaryGreen)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(primaryGreen)
                    )
                    ExposedDropdownMenu(
                        expanded = stateExpanded,
                        onDismissRequest = { stateExpanded = false }
                    ) {
                        indianStates.forEach { stateName ->
                            DropdownMenuItem(
                                text = { Text(stateName) },
                                onClick = {
                                    state = stateName
                                    stateExpanded = false
                                    errorMessage = ""
                                }
                            )
                        }
                    }
                }

                CustomTextField(
                    value = district,
                    onValueChange = { district = it; errorMessage = "" },
                    label = "District *",
                    placeholder = "Enter your district",
                    icon = Icons.Default.LocationCity,
                    primaryGreen = primaryGreen,
                    focusManager = focusManager
                )

                CustomTextField(
                    value = pincode,
                    onValueChange = {
                        pincode = it.filter(Char::isDigit).take(6)
                        errorMessage = ""
                    },
                    label = "PIN Code *",
                    placeholder = "6-digit PIN code",
                    icon = Icons.Default.Pin,
                    primaryGreen = primaryGreen,
                    keyboardType = KeyboardType.Number,
                    focusManager = focusManager
                )

                // Farming Details Section
                SectionHeader("Farming Details (Optional)", primaryGreen)

                CustomTextField(
                    value = farmSize,
                    onValueChange = { farmSize = it.filter { c -> c.isDigit() || c == '.' } },
                    label = "Farm Size (Acres)",
                    placeholder = "Enter farm size",
                    icon = Icons.Default.Terrain,
                    primaryGreen = primaryGreen,
                    keyboardType = KeyboardType.Decimal,
                    focusManager = focusManager
                )

                CustomTextField(
                    value = farmingExperience,
                    onValueChange = { farmingExperience = it.filter(Char::isDigit).take(2) },
                    label = "Farming Experience (Years)",
                    placeholder = "Years of experience",
                    icon = Icons.Default.Timer,
                    primaryGreen = primaryGreen,
                    keyboardType = KeyboardType.Number,
                    focusManager = focusManager
                )

                CustomTextField(
                    value = primaryCrop,
                    onValueChange = { primaryCrop = it },
                    label = "Primary Crop",
                    placeholder = "e.g., Wheat, Rice, Cotton",
                    icon = Icons.Default.Grass,
                    primaryGreen = primaryGreen,
                    focusManager = focusManager
                )

                // Language Dropdown
                ExposedDropdownMenuBox(
                    expanded = languageExpanded,
                    onExpandedChange = { languageExpanded = !languageExpanded }
                ) {
                    OutlinedTextField(
                        value = language,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Preferred Language") },
                        leadingIcon = {
                            Icon(Icons.Default.Language, contentDescription = null, tint = primaryGreen)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors(primaryGreen)
                    )
                    ExposedDropdownMenu(
                        expanded = languageExpanded,
                        onDismissRequest = { languageExpanded = false }
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    language = lang
                                    languageExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = { submit() },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    elevation = ButtonDefaults.buttonElevation(6.dp)
                ) {
                    if (loading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Saving Profile...", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("Save & Continue", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Security Notice
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Your data is securely encrypted",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// Helper Composables
@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 15.sp,
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
    focusManager: androidx.compose.ui.focus.FocusManager,
    prefix: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = primaryGreen)
        },
        prefix = prefix?.let { { Text(it, fontWeight = FontWeight.SemiBold) } },
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

@Composable
private fun textFieldColors(primaryGreen: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = primaryGreen,
    unfocusedBorderColor = Color.LightGray,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    cursorColor = primaryGreen,
    focusedLabelColor = primaryGreen,
    unfocusedLabelColor = Color.Gray
)

// Location Helper
private fun fetchCurrentLocation(
    context: android.content.Context,
    onLocationFetched: (String, String, String, String) -> Unit,
    onError: (String) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        onLocationFetched(
                            addr.getAddressLine(0) ?: "",
                            addr.adminArea ?: "",
                            addr.subAdminArea ?: addr.locality ?: "",
                            addr.postalCode ?: ""
                        )
                        Log.d(TAG, "Location fetched: ${location.latitude}, ${location.longitude}")
                    } else {
                        onError("Unable to fetch address details")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Geocoder error: ${e.message}", e)
                    onError("Geocoder error: ${e.message}")
                }
            } else {
                onError("Unable to fetch location. Please enable GPS.")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Location fetch failed: ${e.message}", e)
            onError("Failed to fetch location: ${e.message}")
        }
    } catch (e: SecurityException) {
        Log.e(TAG, "Security exception: ${e.message}", e)
        onError("Location permission required")
    }
}
