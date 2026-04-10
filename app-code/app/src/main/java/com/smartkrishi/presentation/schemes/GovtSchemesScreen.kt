package com.smartkrishi.presentation.schemes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartkrishi.presentation.theme.ThemeState
import java.util.Locale

private val PrimaryGreen = Color(0xFF2E7D32)

data class GovtScheme(
    val name: String,
    val shortDescription: String,
    val fullDescription: String,
    val ministry: String,
    val eligibility: String,
    val benefits: String,
    val documents: String,
    val applyUrl: String,
    val guide: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBgColor: Color,
    val location: String,
    val crop: String,
    val season: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovtSchemesScreen(navController: NavController) {

    val isDark by ThemeState.isDarkTheme
    val background = if (isDark) Color(0xFF08160F) else Color(0xFFFFFFFF)
    val cardColor = if (isDark) Color(0xFF10261A) else Color(0xFFF7FFF9)
    val textPrimary = if (isDark) Color(0xFF6FB672) else Color(0xFF000000)
    val textSecondary = if (isDark) Color(0xFFB5CBB8) else Color(0xFF000000)

    val context = LocalContext.current

    // Search + voice
    var searchQuery by remember { mutableStateOf("") }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                searchQuery = matches[0]
            }
        }
    }

    // Filters
    var selectedLocation by remember { mutableStateOf("All") }
    var selectedCrop by remember { mutableStateOf("All") }
    var selectedSeason by remember { mutableStateOf("All") }

    // Track "My Schemes" (where user clicked Apply Now)
    val appliedSchemes = remember { mutableStateListOf<GovtScheme>() }
    var showMySchemes by remember { mutableStateOf(false) }

    // === All schemes with real links and short guides ===
    val schemes = remember {
        listOf(
            GovtScheme(
                name = "PM Kisan Samman Nidhi",
                shortDescription = "₹6,000 per year income support",
                fullDescription = "A central sector scheme with 100% funding from Govt. of India to provide ₹6,000 per year to landholding farmer families.",
                ministry = "Ministry of Agriculture & Farmers Welfare",
                eligibility = "All landholding farmer families in the country are eligible, subject to exclusion criteria (institutional landholders, income tax payers, etc.).",
                benefits = "• ₹6,000 per year in three instalments of ₹2,000.\n• Direct benefit transfer (DBT) into bank account.\n• Helps in purchasing seeds, fertilisers and other inputs.\n• Fully funded by Central Government.",
                documents = "• Aadhaar card\n• Bank account details\n• Land records / Khatauni\n• Address details / mobile number",
                applyUrl = "https://pmkisan.gov.in/",
                guide = "1. Visit pmkisan.gov.in and click on 'New Farmer Registration'.\n" +
                        "2. Enter Aadhaar number, select state and fill basic details.\n" +
                        "3. Add land details as per revenue records and submit.\n" +
                        "4. Complete e-KYC online or at nearest CSC centre.\n" +
                        "5. Track status under 'Beneficiary Status' on the PM-KISAN portal.",
                icon = Icons.Default.AccountBalance,
                iconBgColor = Color(0xFFE4F6EA),
                location = "India",
                crop = "All",
                season = "All"
            ),
            GovtScheme(
                name = "Pradhan Mantri Fasal Bima Yojana",
                shortDescription = "Insurance protection for crop loss",
                fullDescription = "Provides insurance coverage and financial support to farmers in the event of crop failure due to natural calamities, pests and diseases.",
                ministry = "Ministry of Agriculture & Farmers Welfare",
                eligibility = "All farmers cultivating notified crops in notified areas can enrol, whether loanee or non-loanee, as per season guidelines.",
                benefits = "• Risk coverage from pre-sowing to post-harvest.\n" +
                        "• Very low farmer premium (2% for Kharif, 1.5% for Rabi – approx).\n" +
                        "• Remaining premium shared by Centre and States.\n" +
                        "• Claims directly credited to farmer’s bank account.",
                documents = "• Aadhaar card\n• Bank passbook\n• Land records / tenancy documents\n• Crop sowing certificate if required\n• Recent photograph (some states)",
                applyUrl = "https://pmfby.gov.in/",
                guide = "1. Go to pmfby.gov.in and choose your state and season.\n" +
                        "2. Register as a farmer with Aadhaar, mobile and bank details.\n" +
                        "3. Select notified crop and insured area / land parcel.\n" +
                        "4. Pay the farmer’s share of premium online or through bank/CSC.\n" +
                        "5. Keep receipt and application number for claim reference.",
                icon = Icons.Default.Security,
                iconBgColor = Color(0xFFE0F3F4),
                location = "India",
                crop = "Cereals",
                season = "Kharif"
            ),
            GovtScheme(
                name = "PMKSY – Krishi Sinchai Yojana",
                shortDescription = "Har Khet Ko Pani, Per Drop More Crop",
                fullDescription = "Improves on-farm water use efficiency and expands micro-irrigation (drip and sprinkler) so every field gets assured irrigation.",
                ministry = "Ministry of Jal Shakti & Ministry of Agriculture",
                eligibility = "All farmers willing to install micro-irrigation systems (drip / sprinkler) on their fields as per state guidelines.",
                benefits = "• Capital subsidy on cost of drip / sprinkler system.\n" +
                        "• Saves 30–50% irrigation water.\n" +
                        "• Increases productivity and quality of crops.\n" +
                        "• Reduces fertiliser and labour cost.",
                documents = "• Land ownership proof / lease agreement\n• Aadhaar card\n• Bank account details\n• Quotation from approved micro-irrigation supplier",
                applyUrl = "https://pmksy.gov.in/microirrigation/index.aspx",
                guide = "1. Contact your district agriculture / horticulture office or visit pmksy.gov.in.\n" +
                        "2. Choose approved supplier and system design for drip/sprinkler.\n" +
                        "3. Submit application with land records and bank details.\n" +
                        "4. After field verification, install the system on field.\n" +
                        "5. Subsidy is released to supplier / farmer as per state norms.",
                icon = Icons.Default.WaterDrop,
                iconBgColor = Color(0xFFE3F2FD),
                location = "India",
                crop = "Horticulture",
                season = "All"
            ),
            GovtScheme(
                name = "Soil Health Card Scheme",
                shortDescription = "Know your soil, use fertiliser correctly",
                fullDescription = "Provides Soil Health Cards to farmers with nutrient status and fertiliser recommendations for better and balanced use of inputs.",
                ministry = "Ministry of Agriculture & Farmers Welfare",
                eligibility = "All farmers in India are eligible to get their fields tested and receive a Soil Health Card, usually once in a 2–3 year cycle.",
                benefits = "• Free / low-cost soil testing.\n" +
                        "• Nutrient and fertiliser recommendation for each plot.\n" +
                        "• Improves yield and reduces fertiliser cost.\n" +
                        "• Promotes balanced use of NPK and micronutrients.",
                documents = "• Basic farmer details (name, address, mobile)\n" +
                        "• Land details (survey number, area)\n" +
                        "• Sample IDs generated by soil testing lab / KVK / agriculture department.",
                applyUrl = "https://soilhealth.dac.gov.in/",
                guide = "1. Visit nearest Krishi Vigyan Kendra, Agriculture Office or Soil Testing Lab.\n" +
                        "2. Take guidance to collect soil samples from your field.\n" +
                        "3. Submit samples with farmer and land details.\n" +
                        "4. Check Soil Health Card status on soilhealth.dac.gov.in or via mobile app.\n" +
                        "5. Follow nutrient and fertiliser recommendations given in the card.",
                icon = Icons.Default.HealthAndSafety,
                iconBgColor = Color(0xFFFFF3E0),
                location = "India",
                crop = "All",
                season = "All"
            ),
            GovtScheme(
                name = "e-NAM – National Agriculture Market",
                shortDescription = "Online mandi for better prices",
                fullDescription = "Pan-India electronic trading platform linking APMC mandis to create a unified national market for agricultural commodities.",
                ministry = "Ministry of Agriculture & Farmers Welfare",
                eligibility = "Farmers, traders and FPOs registered with participating APMC mandis can trade on e-NAM as per mandi rules.",
                benefits = "• Transparent price discovery through online bidding.\n" +
                        "• Option to sell produce to buyers from other mandis / states.\n" +
                        "• Online payment and electronic weighing integration.\n" +
                        "• Better market access and reduced information gap.",
                documents = "• Aadhaar card\n• Bank account\n• Mobile number\n• Mandi registration documents (as required by local APMC).",
                applyUrl = "https://enam.gov.in/web",
                guide = "1. Visit enam.gov.in and go to Farmer Registration section.\n" +
                        "2. Select your state and nearest e-NAM enabled mandi.\n" +
                        "3. Fill farmer profile with Aadhaar and bank details.\n" +
                        "4. Get farmer ID approved at mandi / through e-NAM helpdesk.\n" +
                        "5. Use e-NAM mobile app / portal to view prices and sell produce through online bidding.",
                icon = Icons.Default.Store,
                iconBgColor = Color(0xFFF3E5F5),
                location = "India",
                crop = "All",
                season = "All"
            )
        )
    }

    // Apply filters & search
    val filteredSchemes = schemes.filter { scheme ->
        (searchQuery.isBlank() ||
                scheme.name.contains(searchQuery, ignoreCase = true) ||
                scheme.shortDescription.contains(searchQuery, ignoreCase = true) ||
                scheme.fullDescription.contains(searchQuery, ignoreCase = true)) &&
                (selectedLocation == "All" || scheme.location == selectedLocation) &&
                (selectedCrop == "All" || scheme.crop == selectedCrop) &&
                (selectedSeason == "All" || scheme.season == selectedSeason)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Government Schemes",
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
                actions = {
                    TextButton(onClick = { showMySchemes = true }) {
                        Text("My Schemes", color = textPrimary, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF10261A) else Color.White
                )
            )
        }
    ) { padding ->

        // My Schemes dialog
        if (showMySchemes) {
            AlertDialog(
                onDismissRequest = { showMySchemes = false },
                title = { Text("My Schemes", fontWeight = FontWeight.Bold) },
                text = {
                    if (appliedSchemes.isEmpty()) {
                        Text("You have not applied to any scheme yet.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            appliedSchemes.forEach { s ->
                                Text("• ${s.name}", fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMySchemes = false }) {
                        Text("OK")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // SEARCH BAR (with mic)
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search for schemes...",
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Black
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                        )
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE,
                                            Locale.getDefault()
                                        )
                                        putExtra(
                                            RecognizerIntent.EXTRA_PROMPT,
                                            "Speak scheme name"
                                        )
                                    }
                                    voiceLauncher.launch(intent)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = Color.Black
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // FILTER ROW (Location, Crop, Season)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterDropdown(
                            label = "Location",
                            selected = selectedLocation,
                            options = listOf("All", "India")
                        ) { selectedLocation = it }

                        FilterDropdown(
                            label = "Crop",
                            selected = selectedCrop,
                            options = listOf("All", "All", "Cereals", "Horticulture")
                        ) { selectedCrop = it }

                        FilterDropdown(
                            label = "Season",
                            selected = selectedSeason,
                            options = listOf("All", "Kharif", "Rabi", "All")
                        ) { selectedSeason = it }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                // SCHEME CARDS
                items(filteredSchemes) { scheme ->
                    SchemeCard(
                        scheme = scheme,
                        cardColor = cardColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onApplied = { applied ->
                            if (appliedSchemes.none { it.name == applied.name }) {
                                appliedSchemes.add(applied)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = "$label: $selected",
                    fontSize = 11.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            shape = RoundedCornerShape(16.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.distinct().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SchemeCard(
    scheme: GovtScheme,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    onApplied: (GovtScheme) -> Unit
) {
    val context = LocalContext.current

    var showEligibility by remember { mutableStateOf(false) }
    var showBenefits by remember { mutableStateOf(false) }
    var showDocuments by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(false) }

    // Clear big dialogs for each section
    if (showEligibility) {
        InfoDialog(
            title = "Eligibility – ${scheme.name}",
            content = scheme.eligibility,
            onDismiss = { showEligibility = false }
        )
    }
    if (showBenefits) {
        InfoDialog(
            title = "Benefits – ${scheme.name}",
            content = scheme.benefits,
            onDismiss = { showBenefits = false }
        )
    }
    if (showDocuments) {
        InfoDialog(
            title = "Documents – ${scheme.name}",
            content = scheme.documents,
            onDismiss = { showDocuments = false }
        )
    }
    if (showGuide) {
        InfoDialog(
            title = "How to Apply – ${scheme.name}",
            content = scheme.guide,
            onDismiss = { showGuide = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(scheme.iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = scheme.icon,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        scheme.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = textPrimary
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        scheme.fullDescription,
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            }

            // Eligibility / Benefits / Documents row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconTextPill(
                    icon = Icons.Default.People,
                    label = "Eligibility",
                    onClick = { showEligibility = true }
                )
                IconTextPill(
                    icon = Icons.Default.VerifiedUser,
                    label = "Benefits",
                    onClick = { showBenefits = true }
                )
                IconTextPill(
                    icon = Icons.Default.Description,
                    label = "Documents",
                    onClick = { showDocuments = true }
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        onApplied(scheme)
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(scheme.applyUrl)
                            )
                            context.startActivity(intent)
                        } catch (_: Exception) {
                        }
                    },
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text("Apply Now")
                }

                TextButton(onClick = { showGuide = true }) {
                    Text(
                        text = "View Guide",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF4B000),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun IconTextPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2F1)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = PrimaryGreen
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InfoDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Text(content, fontSize = 15.sp, lineHeight = 20.sp)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewGovtSchemesScreen_NoNav() {
    GovtSchemesScreen(navController = NavController(LocalContext.current))
}
