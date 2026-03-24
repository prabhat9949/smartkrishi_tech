package com.smartkrishi.presentation.disease
import androidx.compose.ui.window.Dialog
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import com.smartkrishi.presentation.navigation.Screen

/* ---------------- THEME COLORS (consistent and professional palette) ---------------- */
private val PrimaryGreen = Color(0xFF388E3C)
private val SecondaryGreen = Color(0xFF2E7D32)
private val BackgroundGray = Color(0xFFF5F5F5)
private val CardBackground = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF212121)
private val TextSecondary = Color(0xFF757575)
private val WarningRed = Color(0xFFE53935)
private val SuccessGreen = Color(0xFF4CAF50)
private val MediumOrange = Color(0xFFFF9800)

/* ---------------- DATA MODELS ---------------- */
data class TreatmentData(
    val cause: String,
    val symptoms: List<String>,
    val chemicalTreatment: List<ApplicationStep>,
    val organicTreatment: List<ApplicationStep>,
    val prevention: List<String>,
    val safety: List<String>
)

data class ApplicationStep(
    val product: String,
    val dosage: String,
    val method: String,
    val frequency: String,
    val notes: String = ""  // Added for extra elaboration
)

/* ---------------- MAIN SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseResultScreen(
    navController: NavController,
    crop: String,
    disease: String,
    confidence: Float,
    imageUriString: String?
){
    val scrollState = rememberScrollState()
    val displayPercentage = (confidence * 100).toInt()
    val imageUri = imageUriString?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }

    val confidenceText = when {
        confidence >= 0.60f -> "High confidence detection"
        confidence >= 0.40f -> "Medium confidence – consider retaking photo for confirmation"
        else -> "Low confidence – please capture a clearer image of the affected area"
    }

    val confidenceColor = when {
        confidence >= 0.60f -> SuccessGreen
        confidence >= 0.40f -> MediumOrange
        else -> WarningRed
    }
    val isWrongCrop = disease.contains("wrong crop", ignoreCase = true)
    var showWrongCropDialog by remember { mutableStateOf(isWrongCrop) }
    val cleanDisease = disease
        .replace("___", " ")
        .replace("_", " ")
        .replace(crop, "", ignoreCase = true)
        .ifBlank { "Unknown disease" }
        .trim()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    if (showWrongCropDialog) {

        Dialog(onDismissRequest = { }) {



            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                WarningRed.copy(alpha = 0.12f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = WarningRed,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Wrong Crop Selected",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningRed
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "The uploaded leaf does not match the selected crop.\n\nPlease select the correct crop and try again.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    Button(
                        onClick = {
                            showWrongCropDialog = false
                            navController.popBackStack()
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Correct Crop")
                    }
                }
            }
        }

        // 🔴 STOP everything else from rendering
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Disease Detection Result",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = crop.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // TODO: Implement sharing functionality (e.g., share result as text or image)
                    }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share result",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(Screen.KrishiMitri.route) {
                        launchSingleTop = true
                    }
                },
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Chat, contentDescription = "Ask Smart Krishi") },
                text = { Text("Ask Smart Krishi", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(bottom = 80.dp)  // Extra padding for FAB
        ) {

            /* --------- Uploaded Image Card --------- */
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                if (imageUri != null) {
                    SubcomposeAsyncImage(
                        model = imageUri,
                        contentDescription = "Uploaded crop image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (painter.state) {
                            is coil.compose.AsyncImagePainter.State.Loading -> {
                                Box(
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }

                            is coil.compose.AsyncImagePainter.State.Error -> {
                                Box(
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.BrokenImage,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Could not load image",
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            else -> {
                                SubcomposeAsyncImageContent()
                            }
                        }
                    }
                } else {
                    // Fallback if URI is null/empty
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No image available",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            /* --------- Summary Strip --------- */
            SummaryStrip(
                crop = crop,
                disease = cleanDisease,
                confidence = displayPercentage,
                riskLevel = getRiskLevel(disease),
                riskColor = getRiskColor(disease),
                confidenceColor = confidenceColor
            )

            /* --------- Main Disease Info Card --------- */
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Crop Name Badge + Risk chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PrimaryGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = crop,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = getRiskLevel(disease),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = getRiskColor(disease).copy(alpha = 0.12f),
                                labelColor = getRiskColor(disease),
                                leadingIconContentColor = getRiskColor(disease)
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Disease Name
                    Text(
                        text = cleanDisease,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = confidenceText,
                        fontSize = 13.sp,
                        color = confidenceColor,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(18.dp))

                    // Confidence Score
                    Column {
                        Text(
                            text = "Confidence score",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = confidence.coerceIn(0f, 1f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = confidenceColor,
                                trackColor = Color(0xFFE0E0E0)
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$displayPercentage%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (confidence >= 0.4f) "Model is reasonably sure"
                                    else "Image may be unclear",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tabs
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("Treatment Plan", "Prevention Plan")

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardBackground,
                contentColor = PrimaryGreen,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (selectedTab == 0) {
                TreatmentPlanContent(crop, disease)
            } else {
                PreventionPlanContent(crop)
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

/* ---------------- SUMMARY STRIP ---------------- */

@Composable
private fun SummaryStrip(
    crop: String,
    disease: String,
    confidence: Int,
    riskLevel: String,
    riskColor: Color,
    confidenceColor: Color
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$crop – $disease",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Risk: $riskLevel",
                    fontSize = 12.sp,
                    color = riskColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$confidence%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = confidenceColor
                )
                Text(
                    text = "Model confidence",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))
}

/* ---------------- TREATMENT SECTION ---------------- */
@Composable
private fun TreatmentPlanContent(
    crop: String,
    disease: String
) {
    val treatmentData = getTreatmentData(crop, disease)

    ExpandableSection(
        title = "Cause",
        icon = Icons.Default.BugReport
    ) {
        Text(
            treatmentData.cause,
            fontSize = 14.sp,
            color = TextPrimary,
            lineHeight = 20.sp
        )
    }

    ExpandableSection(
        title = "Symptoms",
        icon = Icons.Default.Visibility
    ) {
        treatmentData.symptoms.forEach {
            TreatmentBullet(it)
        }
    }

    if (treatmentData.chemicalTreatment.isNotEmpty()) {
        ExpandableSection(
            title = "Chemical Treatment (Application Guide)",
            icon = Icons.Default.Science
        ) {
            treatmentData.chemicalTreatment.forEach {
                ApplicationCard(it)
            }
        }
    }

    if (treatmentData.organicTreatment.isNotEmpty()) {
        ExpandableSection(
            title = "Organic Treatment (Application Guide)",
            icon = Icons.Default.Eco
        ) {
            treatmentData.organicTreatment.forEach {
                ApplicationCard(it)
            }
        }
    }

    ExpandableSection(
        title = "Safety Instructions",
        icon = Icons.Default.HealthAndSafety
    ) {
        treatmentData.safety.forEach {
            TreatmentBullet(it)
        }
    }
}

/* ---------------- PREVENTION SECTION ---------------- */

@Composable
private fun PreventionPlanContent(crop: String) {
    val preventionData = getPreventionData(crop)

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Prevention Plan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            preventionData.forEach { (title, description) ->
                PreventionItem(title, description)
            }
        }
    }
}

/* ---------------- UI HELPERS ---------------- */

@Composable
private fun TreatmentBullet(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .background(PrimaryGreen, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextPrimary,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ApplicationCard(step: ApplicationStep) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                step.product,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(6.dp))
            Text("Dosage: ${step.dosage}", fontSize = 13.sp, color = TextPrimary)
            Text("Method: ${step.method}", fontSize = 13.sp, color = TextPrimary)
            Text("Frequency: ${step.frequency}", fontSize = 13.sp, color = TextPrimary)
            if (step.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Notes: ${step.notes}", fontSize = 13.sp, color = TextSecondary, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
private fun PreventionItem(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(6.dp)
                    .background(PrimaryGreen, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    defaultExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            if (expanded) {
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Column(
                    modifier = Modifier.padding(16.dp),
                    content = content
                )
            }
        }
    }
}

/* ---------------- DATA & LOGIC ---------------- */

private fun getTreatmentData(
    crop: String,
    disease: String
): TreatmentData {

    val c = crop.lowercase()
    val d = disease.lowercase()

    /* ---------------- HEALTHY ---------------- */
    if (d.contains("healthy")) {
        return TreatmentData(
            cause = "The plant appears to be in good health with no signs of disease or pest infestation. This indicates proper care, suitable environmental conditions, and absence of pathogens.",
            symptoms = listOf(
                "Vibrant, uniform green coloration across leaves and stems",
                "No visible spots, lesions, or discolorations",
                "Healthy leaf structure without wilting, curling, or deformation",
                "Strong stem support and normal growth patterns"
            ),
            chemicalTreatment = emptyList(),
            organicTreatment = listOf(
                ApplicationStep(
                    product = "Neem Oil (Preventive Maintenance)",
                    dosage = "3-5 ml per liter of water",
                    method = "Prepare a dilute solution and spray evenly on both sides of leaves, focusing on undersides where pests may hide",
                    frequency = "Apply once every 15-20 days during growing season",
                    notes = "Best applied in early morning or evening to avoid leaf burn. Test on a small area first to check for sensitivity."
                )
            ),
            prevention = listOf(
                "Implement regular soil testing to maintain optimal nutrient levels",
                "Ensure consistent but not excessive irrigation to prevent root rot",
                "Promote biodiversity by planting companion crops",
                "Conduct weekly visual inspections for early signs of issues",
                "Use mulch to retain soil moisture and suppress weeds"
            ),
            safety = listOf(
                "Always wear protective gloves when handling any sprays",
                "Avoid application during peak sunlight hours to prevent phytotoxicity",
                "Store preventive products in cool, dry places away from children and pets",
                "Follow organic certification guidelines if applicable"
            )
        )
    }

    /* ---------------- TOMATO ---------------- */
    if (c.contains("tomato")) {

        if (d.contains("early")) {
            return TreatmentData(
                cause = "Early blight in tomatoes is primarily caused by the fungal pathogen Alternaria solani. It thrives in warm, humid conditions and spreads through infected plant debris, soil, wind, and water splash. Poor air circulation and overhead irrigation exacerbate the spread.",
                symptoms = listOf(
                    "Small, dark brown spots with concentric rings on lower leaves, resembling a bull's-eye pattern",
                    "Yellow halo surrounding the lesions, leading to chlorosis",
                    "Premature defoliation starting from older leaves upward",
                    "Stem lesions that can girdle young plants",
                    "Fruit spots near the stem end, causing cracking and secondary infections"
                ),
                chemicalTreatment = listOf(
                    ApplicationStep(
                        product = "Mancozeb 75% WP (Broad-spectrum fungicide)",
                        dosage = "2-2.5 g per liter of water",
                        method = "Mix thoroughly and apply as a foliar spray covering all plant surfaces, especially lower leaves",
                        frequency = "Apply every 7-10 days, starting at first sign of symptoms; maximum 3-4 applications per season",
                        notes = "Alternate with other fungicides to prevent resistance. Apply in early morning for best absorption."
                    ),
                    ApplicationStep(
                        product = "Chlorothalonil 50% WP",
                        dosage = "2 g per liter of water",
                        method = "Spray uniformly on foliage, ensuring coverage of both upper and lower leaf surfaces",
                        frequency = "Alternate with Mancozeb every 7 days after initial application",
                        notes = "Do not apply when temperatures exceed 30°C. Wash equipment thoroughly after use."
                    )
                ),
                organicTreatment = listOf(
                    ApplicationStep(
                        product = "Neem Oil Extract (Azadirachtin-based)",
                        dosage = "3-5 ml per liter of water with emulsifier",
                        method = "Shake well and spray on affected areas, focusing on leaf undersides",
                        frequency = "Apply weekly or after rainfall",
                        notes = "Combine with soap for better adhesion. Effective as both fungicide and insecticide."
                    ),
                    ApplicationStep(
                        product = "Trichoderma viride Bio-fungicide",
                        dosage = "5-10 g per liter of water",
                        method = "Dissolve in water and apply as soil drench or foliar spray",
                        frequency = "Apply once every 15 days as preventive measure",
                        notes = "Store in refrigerator. Best used in conjunction with compost for soil health."
                    ),
                    ApplicationStep(
                        product = "Baking Soda Solution (Sodium Bicarbonate)",
                        dosage = "1 tablespoon per gallon of water with 1 tsp vegetable oil",
                        method = "Mix and spray weekly on leaves",
                        frequency = "Weekly preventive application",
                        notes = "pH neutral; test on small area to avoid leaf burn."
                    )
                ),
                prevention = listOf(
                    "Practice crop rotation with non-solanaceous plants for at least 2-3 years",
                    "Maintain proper plant spacing (60-90 cm) for good air circulation",
                    "Use drip irrigation to keep foliage dry and reduce splash",
                    "Remove and destroy infected plant material promptly",
                    "Apply mulch to prevent soil splash and maintain moisture",
                    "Select resistant tomato varieties like 'Mountain Merit' or 'Iron Lady'",
                    "Monitor weather for humid conditions and apply preventives accordingly"
                ),
                safety = listOf(
                    "Wear protective gear including gloves, masks, and eye protection during application",
                    "Avoid spraying during windy conditions to prevent drift",
                    "Observe pre-harvest interval: 5-7 days for chemicals",
                    "Store products in original containers away from food and children",
                    "Wash hands and exposed skin thoroughly after handling",
                    "Dispose of empty containers responsibly according to local regulations",
                    "In case of ingestion or skin irritation, seek medical attention immediately"
                )
            )
        }
    }

    /* ---------------- POTATO ---------------- */
    if (c.contains("potato")) {

        if (d.contains("late")) {
            return TreatmentData(
                cause = "Late blight in potatoes is caused by the oomycete pathogen Phytophthora infestans. It spreads rapidly in cool (15-20°C), moist conditions through spores carried by wind, rain, or infected tubers. Historical note: This pathogen caused the Irish Potato Famine in the 1840s.",
                symptoms = listOf(
                    "Irregular dark brown to black water-soaked lesions on leaves and stems",
                    "White fuzzy mycelial growth on leaf undersides in humid conditions",
                    "Rapid wilting and collapse of entire plants within days",
                    "Brown, firm rot on tubers with reddish-brown discoloration under skin",
                    "Foul odor from secondary bacterial infections in advanced stages"
                ),
                chemicalTreatment = listOf(
                    ApplicationStep(
                        product = "Metalaxyl + Mancozeb (Systemic + Contact Fungicide)",
                        dosage = "2.5 g per liter of water",
                        method = "Apply as thorough foliar spray covering all plant parts; repeat if rain occurs within 24 hours",
                        frequency = "Every 5-7 days during susceptible periods; maximum 4 applications",
                        notes = "Use protectant fungicides before infection; systemic after symptoms appear. Rotate modes of action."
                    ),
                    ApplicationStep(
                        product = "Dimethomorph 50% WP",
                        dosage = "1-1.5 g per liter",
                        method = "Foliar application focusing on lower canopy",
                        frequency = "Alternate with other fungicides every 7-10 days",
                        notes = "Effective against oomycetes; do not use alone to prevent resistance."
                    )
                ),
                organicTreatment = listOf(
                    ApplicationStep(
                        product = "Copper-based Fungicide (e.g., Bordeaux Mixture)",
                        dosage = "3-4% solution (copper sulfate + lime)",
                        method = "Prepare fresh and apply as preventive foliar spray",
                        frequency = "Weekly during wet weather",
                        notes = "Traditional organic method; may accumulate in soil with overuse."
                    ),
                    ApplicationStep(
                        product = "Bacillus subtilis Bio-fungicide",
                        dosage = "As per product label (typically 5-10 ml/liter)",
                        method = "Spray on foliage and soil around plants",
                        frequency = "Every 10-14 days",
                        notes = "Promotes plant immunity; combine with compost tea for better results."
                    ),
                    ApplicationStep(
                        product = "Potassium Bicarbonate Solution",
                        dosage = "1-2% solution with spreader",
                        method = "Foliar spray in early stages",
                        frequency = "Every 5-7 days",
                        notes = "Alkaline pH disrupts fungal growth; environmentally friendly."
                    )
                ),
                prevention = listOf(
                    "Use certified disease-free seed potatoes from reputable sources",
                    "Implement wide row spacing (75-90 cm) for better ventilation",
                    "Avoid irrigation late in the day to allow foliage to dry",
                    "Hill up soil around plants to protect tubers",
                    "Destroy volunteer plants and cull piles",
                    "Choose resistant varieties like 'Kennebec' or 'Defender'",
                    "Monitor disease forecasts and apply protectants preemptively",
                    "Harvest during dry weather and cure tubers properly"
                ),
                safety = listOf(
                    "Use full protective equipment: respirator, gloves, long sleeves, and eye protection",
                    "Apply during calm weather to minimize exposure and drift",
                    "Respect re-entry interval: 24-48 hours after application",
                    "Adhere to pre-harvest interval: 7-14 days depending on product",
                    "Avoid application near water bodies to prevent contamination",
                    "Store in secure, ventilated area away from living spaces",
                    "In emergency, rinse exposed areas with water and consult poison control"
                )
            )
        }
    }

    /* ---------------- APPLE ---------------- */
    if (c.contains("apple")) {

        if (d.contains("scab")) {
            return TreatmentData(
                cause = "Apple scab is caused by the ascomycete fungus Venturia inaequalis. It overwinters on fallen leaves and infects new growth in spring during wet weather. Primary infections occur at green tip stage, with secondary spread throughout the season.",
                symptoms = listOf(
                    "Velvety olive-green to black spots on leaves, often on undersides initially",
                    "Lesions on fruit starting as small spots, developing into corky scabs",
                    "Distorted leaf growth and premature defoliation in severe cases",
                    "Cracked, deformed fruits with reduced market value",
                    "Twig lesions in chronic infections, leading to dieback"
                ),
                chemicalTreatment = listOf(
                    ApplicationStep(
                        product = "Captan 50% WP (Contact Fungicide)",
                        dosage = "2-3 g per liter of water",
                        method = "High-volume spray ensuring thorough coverage of canopy",
                        frequency = "Every 7-10 days from green tip to petal fall, then as needed",
                        notes = "Protectant only; apply before rain events. Tank mix with other fungicides for spectrum."
                    ),
                    ApplicationStep(
                        product = "Difenoconazole (Systemic)",
                        dosage = "0.5 ml per liter",
                        method = "Foliar application during high-risk periods",
                        frequency = "Alternate every 10-14 days",
                        notes = "Curative properties; limit to 3 applications per season to manage resistance."
                    )
                ),
                organicTreatment = listOf(
                    ApplicationStep(
                        product = "Sulfur 80% WG (Elemental Sulfur)",
                        dosage = "3-5 g per liter",
                        method = "Apply as fine mist on dry foliage",
                        frequency = "Every 7-10 days during susceptible periods",
                        notes = "Avoid temperatures above 30°C to prevent phytotoxicity. Not for sulfur-sensitive varieties."
                    ),
                    ApplicationStep(
                        product = "Potassium Bicarbonate",
                        dosage = "5-10 g per liter with surfactant",
                        method = "Foliar spray covering all surfaces",
                        frequency = "Weekly preventive",
                        notes = "Raises pH to inhibit spore germination; rinse sprayer after use."
                    ),
                    ApplicationStep(
                        product = "Bacillus amyloliquefaciens Bio-agent",
                        dosage = "As per label (typically 2-5 ml/liter)",
                        method = "Apply to foliage and fruit",
                        frequency = "Every 10-14 days",
                        notes = "Enhances natural resistance; best in integrated programs."
                    )
                ),
                prevention = listOf(
                    "Select scab-resistant varieties like 'Liberty' or 'Enterprise'",
                    "Prune trees annually to improve air flow and light penetration",
                    "Rake and destroy fallen leaves in autumn to reduce overwintering spores",
                    "Use urea treatment on leaf litter to accelerate decomposition",
                    "Install weather monitoring for infection periods (Mills table)",
                    "Maintain balanced nutrition, avoiding excess nitrogen",
                    "Space trees adequately (4-6 m) for ventilation",
                    "Use mulch but keep trunk area clear"
                ),
                safety = listOf(
                    "Employ proper PPE: chemical-resistant gloves, goggles, and coveralls",
                    "Calibrate sprayers for accurate application rates",
                    "Observe bee safety: avoid blooming period applications",
                    "Follow label PHI: 0-7 days for fruit harvest",
                    "Prevent runoff into waterways",
                    "Train applicators on safe handling",
                    "Maintain equipment to avoid leaks"
                )
            )
        }
    }

    /* ---------------- ADDITIONAL CROPS (Added for completeness) ---------------- */

    // Rice Blast
    if (c.contains("rice") && d.contains("blast")) {
        return TreatmentData(
            cause = "Rice blast is caused by the fungus Magnaporthe oryzae (formerly Pyricularia oryzae). It spreads via airborne spores, favored by high humidity, moderate temperatures (24-28°C), and nitrogen-rich conditions. Infection can occur at all growth stages.",
            symptoms = listOf(
                "Elliptical lesions with gray centers and dark borders on leaves",
                "Neck rot leading to panicle blanking",
                "White to grayish spots on sheaths and stems",
                "Reduced grain filling and quality",
                "Seedling blight in severe cases"
            ),
            chemicalTreatment = listOf(
                ApplicationStep(
                    product = "Tricyclazole 75% WP",
                    dosage = "0.6 g per liter",
                    method = "Foliar spray at tillering and panicle initiation",
                    frequency = "2-3 applications at 10-15 day intervals",
                    notes = "Systemic; absorbs quickly. Avoid mixing with alkaline compounds."
                ),
                ApplicationStep(
                    product = "Isoprothiolane 40% EC",
                    dosage = "1.5 ml per liter",
                    method = "Uniform coverage spray",
                    frequency = "Apply at first symptoms",
                    notes = "Effective on neck blast; use stickers for better adhesion."
                )
            ),
            organicTreatment = listOf(
                ApplicationStep(
                    product = "Pseudomonas fluorescens Bio-control",
                    dosage = "10 g per liter",
                    method = "Seed treatment and foliar spray",
                    frequency = "Multiple applications from seedling to heading",
                    notes = "Antagonistic bacteria; apply in evening for viability."
                ),
                ApplicationStep(
                    product = "Neem Seed Kernel Extract",
                    dosage = "5% solution",
                    method = "Soak seeds and spray foliage",
                    frequency = "Every 10 days",
                    notes = "Natural antifeedant; combine with bio-agents."
                )
            ),
            prevention = listOf(
                "Use blast-resistant varieties like IR64 or Swarna Sub1",
                "Balance nitrogen fertilization (split doses)",
                "Maintain water depth of 5-10 cm to suppress spores",
                "Avoid dense planting; use 20x15 cm spacing",
                "Burn infected stubble after harvest",
                "Seed treatment with hot water (52°C for 10 min)",
                "Monitor silicon levels in soil for plant strength"
            ),
            safety = listOf(
                "Use calibrated equipment for precise application",
                "Wear full protective suit during spraying",
                "Avoid eating/drinking in field during operations",
                "Wash thoroughly after handling chemicals",
                "Store in locked facilities",
                "Follow integrated pest management to minimize chemical use"
            )
        )
    }

    // Corn Smut (Example addition)
    if (c.contains("corn") || c.contains("maize") && d.contains("smut")) {
        return TreatmentData(
            cause = "Corn smut is caused by the basidiomycete fungus Ustilago maydis. It infects through wounds or silks, stimulated by high moisture and temperatures around 26-34°C. Often considered a delicacy (huitlacoche) in some cultures.",
            symptoms = listOf(
                "Large, swollen galls on ears, tassels, or stalks filled with black spores",
                "Silvery-gray tumors that burst to release powdery spores",
                "Deformed plant parts",
                "Reduced yield in severe infections",
                "White to gray initial galls turning black"
            ),
            chemicalTreatment = listOf(
                ApplicationStep(
                    product = "Tebuconazole 25% WG",
                    dosage = "1 g per liter",
                    method = "Preventive foliar spray at V6-V8 stage",
                    frequency = "1-2 applications",
                    notes = "Limited efficacy; focus on prevention."
                )
            ),
            organicTreatment = listOf(
                ApplicationStep(
                    product = "Trichoderma harzianum",
                    dosage = "5 g per kg seed",
                    method = "Seed treatment",
                    frequency = "Pre-planting",
                    notes = "Soil inoculant for antagonism."
                ),
                ApplicationStep(
                    product = "Garlic Extract",
                    dosage = "10% solution",
                    method = "Foliar spray",
                    frequency = "Weekly",
                    notes = "Natural antifungal properties."
                )
            ),
            prevention = listOf(
                "Use resistant hybrids",
                "Crop rotation with non-hosts",
                "Remove and destroy galls before spore release",
                "Balance soil fertility",
                "Avoid mechanical injury to plants",
                "Plant at optimal density"
            ),
            safety = listOf(
                "Minimal chemical use recommended",
                "Handle infected material with care to avoid spore inhalation",
                "Use gloves when removing galls"
            )
        )
    }

    /* ---------------- DEFAULT ---------------- */
    return TreatmentData(
        cause = "The exact cause may vary based on crop type and environmental factors. Common causes include fungal, bacterial, or viral pathogens, often exacerbated by poor management practices. Consult local agricultural experts for precise diagnosis.",
        symptoms = listOf(
            "General leaf discoloration or spotting",
            "Wilting or stunted growth",
            "Presence of unusual growths or lesions",
            "Reduced yield and plant vigor",
            "Secondary infections from weakened plants"
        ),
        chemicalTreatment = listOf(
            ApplicationStep(
                product = "Broad-spectrum Fungicide (e.g., Carbendazim)",
                dosage = "1-2 g per liter",
                method = "Foliar spray or soil drench as appropriate",
                frequency = "As per local recommendations",
                notes = "Use only after confirmation; rotate chemicals."
            )
        ),
        organicTreatment = listOf(
            ApplicationStep(
                product = "Neem-based Products",
                dosage = "3-5 ml per liter",
                method = "Regular foliar application",
                frequency = "Weekly preventive",
                notes = "Safe for beneficial insects."
            )
        ),
        prevention = listOf(
            "Adopt integrated pest management (IPM) practices",
            "Use certified disease-free seeds/seedlings",
            "Maintain optimal plant nutrition and soil health",
            "Implement proper sanitation and field hygiene",
            "Monitor and scout fields regularly for early detection"
        ),
        safety = listOf(
            "Always read and follow product labels carefully",
            "Use appropriate personal protective equipment (PPE)",
            "Avoid application during adverse weather conditions",
            "Observe waiting periods before harvest",
            "Store materials safely and dispose responsibly"
        )
    )
}

private fun getPreventionData(crop: String): List<Pair<String, String>> {
    return listOf(
        "Crop Rotation" to "Rotate $crop with non-host crops for 2-3 years to break disease cycles and deplete soil pathogens.",
        "Plant Spacing" to "Maintain adequate spacing between plants to promote air circulation and reduce humidity buildup in the canopy.",
        "Tool Sanitation" to "Clean and disinfect all farming tools and equipment after use to prevent cross-contamination between fields.",
        "Nutrient Management" to "Apply balanced fertilizers based on soil tests; avoid excess nitrogen which can promote foliar diseases.",
        "Irrigation Practices" to "Use drip or furrow irrigation to keep foliage dry; irrigate in morning to allow drying.",
        "Resistant Varieties" to "Select and plant disease-resistant varieties of $crop suitable for your region.",
        "Field Monitoring" to "Conduct regular scouting (weekly) to detect early signs of disease for timely intervention.",
        "Weed Control" to "Maintain weed-free fields as weeds can harbor pathogens and compete for resources.",
        "Soil Health" to "Incorporate organic matter and beneficial microbes to improve soil structure and disease suppression.",
        "Post-Harvest Handling" to "Properly clean and store produce to prevent spread to next season's crops."
    )
}

private fun getRiskLevel(disease: String): String {
    return when {
        disease.contains("Healthy", ignoreCase = true) -> "None"
        disease.contains("Late_blight", ignoreCase = true) -> "Very High"
        disease.contains("Early_blight", ignoreCase = true) -> "High"
        disease.contains("Bacterial", ignoreCase = true) -> "High"
        disease.contains("scab", ignoreCase = true) -> "High"
        disease.contains("blast", ignoreCase = true) -> "High"
        disease.contains("smut", ignoreCase = true) -> "Medium"
        else -> "Moderate"
    }
}

private fun getRiskColor(disease: String): Color {
    return when {
        disease.contains("Healthy", ignoreCase = true) -> SuccessGreen
        disease.contains("Late_blight", ignoreCase = true) -> WarningRed
        disease.contains("Early_blight", ignoreCase = true) -> WarningRed
        disease.contains("Bacterial", ignoreCase = true) -> WarningRed
        disease.contains("scab", ignoreCase = true) -> WarningRed
        disease.contains("blast", ignoreCase = true) -> MediumOrange
        disease.contains("smut", ignoreCase = true) -> MediumOrange
        else -> MediumOrange
    }
}