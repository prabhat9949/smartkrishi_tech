package com.smartkrishi.presentation.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smartkrishi.ml.FertilizerRecommendationViewModel
import com.smartkrishi.ml.FertilizerUiState
import com.smartkrishi.presentation.home.FarmViewModel

private val PrimaryGreen = Color(0xFF2E7D32)
private val PrimaryGreenDark = Color(0xFF1B5E20)
private val AccentGreenSoft = Color(0xFFC8E6C9)
private val LightBackground = Color(0xFFF4FBF6)
private val CardLight = Color(0xFFFFFFFF)
private val BorderSoft = Color(0xFFD9E7DB)
private val TextSoft = Color(0xFF5F6F64)

data class FertilizerPlan(
    val name: String,
    val quantity: String,
    val timing: String,
    val purpose: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerRecommendationScreen(
    navController: NavController,
    farmViewModel: FarmViewModel = hiltViewModel(),
    recommendationViewModel: FertilizerRecommendationViewModel = hiltViewModel()
) {
    val selectedFarm by farmViewModel.selectedFarm.collectAsState()
    val uiState by recommendationViewModel.uiState.collectAsStateWithLifecycle()
    val plans = parseStructuredPlans(uiState.resultText)

    var showRecalculateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFarm) {
        selectedFarm?.let { farm ->
            recommendationViewModel.prefillFromDashboard(
                crop = getFieldValue(farm, listOf("cropType", "crop"), ""),
                location = getFieldValue(farm, listOf("location", "village", "district"), ""),
                area = getFieldValue(farm, listOf("area", "farmArea"), ""),
                month = getFieldValue(farm, listOf("month", "sowingMonth"), ""),
                cropGrowthStage = getFieldValue(farm, listOf("cropGrowthStage", "growthStage"), ""),
                season = getFieldValue(farm, listOf("season"), ""),
                irrigationType = getFieldValue(farm, listOf("irrigationType", "irrigation"), ""),
                previousCrop = getFieldValue(farm, listOf("previousCrop"), ""),
                previousFertilizerUsed = getFieldValue(farm, listOf("previousFertilizerUsed", "fertilizerUsed"), ""),
                totalFarm = getFieldValue(farm, listOf("totalFarm", "farmSize"), ""),
                soil = getFieldValue(farm, listOf("soilType", "soil"), "Loamy"),
                pH = getFieldValue(farm, listOf("soilPH", "soilPh", "ph", "pH"), "6.5"),
                moisture = getFieldValue(farm, listOf("soilMoisture", "moisture"), "45"),
                temp = getFieldValue(farm, listOf("temperature", "temp"), "30"),
                humidity = getFieldValue(farm, listOf("humidity"), "60"),
                nitrogen = getFieldValue(farm, listOf("nitrogen", "n"), "40"),
                phosphorus = getFieldValue(farm, listOf("phosphorus", "p"), "30"),
                potassium = getFieldValue(farm, listOf("potassium", "k"), "20")
            )
        }
    }

    if (showRecalculateDialog) {
        AlertDialog(
            onDismissRequest = { showRecalculateDialog = false },
            title = {
                Text(
                    text = "Recalculate recommendation",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Previous recommendation will be removed and a new recommendation will be generated.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRecalculateDialog = false
                        recommendationViewModel.clearRecommendation()
                        recommendationViewModel.generateRecommendation()
                    }
                ) {
                    Text("Continue", color = PrimaryGreenDark)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRecalculateDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Scaffold(
            containerColor = LightBackground,
            topBar = {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        Text(
                            text = "Fertilizer Advisor",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryGreen,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                BottomActionBar(
                    uiState = uiState,
                    onBack = { recommendationViewModel.goPreviousStep() },
                    onNext = { recommendationViewModel.goNextStep() },
                    onCalculate = { recommendationViewModel.generateRecommendation() }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(padding)
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                HeaderCard(
                    crop = uiState.crop.ifBlank { "Select crop from dashboard" },
                    location = uiState.location.ifBlank { "Enter location" },
                    hasRecommendation = uiState.resultText.isNotBlank(),
                    onRecalculate = { showRecalculateDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                StepIndicator(currentStep = uiState.currentStep)

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.currentStep == 1) {
                    FarmContextSection(
                        crop = uiState.crop,
                        location = uiState.location,
                        area = uiState.area,
                        month = uiState.month,
                        cropGrowthStage = uiState.cropGrowthStage,
                        season = uiState.season,
                        irrigationType = uiState.irrigationType,
                        previousCrop = uiState.previousCrop,
                        previousFertilizerUsed = uiState.previousFertilizerUsed,
                        totalFarm = uiState.totalFarm,
                        onCropChange = { recommendationViewModel.updateFarmContext(crop = it) },
                        onLocationChange = { recommendationViewModel.updateFarmContext(location = it) },
                        onAreaChange = { recommendationViewModel.updateFarmContext(area = it) },
                        onMonthChange = { recommendationViewModel.updateFarmContext(month = it) },
                        onCropGrowthStageChange = { recommendationViewModel.updateFarmContext(cropGrowthStage = it) },
                        onSeasonChange = { recommendationViewModel.updateFarmContext(season = it) },
                        onIrrigationTypeChange = { recommendationViewModel.updateFarmContext(irrigationType = it) },
                        onPreviousCropChange = { recommendationViewModel.updateFarmContext(previousCrop = it) },
                        onPreviousFertilizerUsedChange = { recommendationViewModel.updateFarmContext(previousFertilizerUsed = it) },
                        onTotalFarmChange = { recommendationViewModel.updateFarmContext(totalFarm = it) }
                    )
                } else {
                    MlInputsSection(
                        soil = uiState.soil,
                        pH = uiState.pH,
                        moisture = uiState.moisture,
                        temp = uiState.temp,
                        humidity = uiState.humidity,
                        nitrogen = uiState.nitrogen,
                        phosphorus = uiState.phosphorus,
                        potassium = uiState.potassium,
                        onSoilChange = { recommendationViewModel.updateMlInputs(soil = it) },
                        onPHChange = { recommendationViewModel.updateMlInputs(pH = it) },
                        onMoistureChange = { recommendationViewModel.updateMlInputs(moisture = it) },
                        onTempChange = { recommendationViewModel.updateMlInputs(temp = it) },
                        onHumidityChange = { recommendationViewModel.updateMlInputs(humidity = it) },
                        onNitrogenChange = { recommendationViewModel.updateMlInputs(nitrogen = it) },
                        onPhosphorusChange = { recommendationViewModel.updateMlInputs(phosphorus = it) },
                        onPotassiumChange = { recommendationViewModel.updateMlInputs(potassium = it) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedVisibility(
                    visible = uiState.predictedFertilizer.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    PredictedFertilizerCard(
                        fertilizerName = uiState.predictedFertilizer
                    )
                }

                if (uiState.predictedFertilizer.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ErrorCard(
                        message = uiState.error ?: "Something went wrong. Please try again."
                    )
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AnimatedVisibility(
                    visible = plans.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    RecommendationSection(
                        crop = uiState.crop,
                        location = uiState.location,
                        plans = plans,
                        onRecalculate = { showRecalculateDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        if (uiState.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun HeaderCard(
    crop: String,
    location: String,
    hasRecommendation: Boolean,
    onRecalculate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AccentGreenSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = crop,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreenDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = location,
                    fontSize = 14.sp,
                    color = PrimaryGreenDark.copy(alpha = 0.75f)
                )
            }

            if (hasRecommendation) {
                IconButton(onClick = onRecalculate) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recalculate recommendation",
                        tint = PrimaryGreenDark
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepChip(
                number = 1,
                title = "Farm Details",
                active = currentStep == 1
            )
            Text(
                text = "—",
                color = TextSoft
            )
            StepChip(
                number = 2,
                title = "Soil Inputs",
                active = currentStep == 2
            )
        }
    }
}

@Composable
private fun StepChip(
    number: Int,
    title: String,
    active: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = if (active) PrimaryGreen else AccentGreenSoft,
            modifier = Modifier.size(30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    color = if (active) Color.White else PrimaryGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = if (active) PrimaryGreenDark else TextSoft,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun FarmContextSection(
    crop: String,
    location: String,
    area: String,
    month: String,
    cropGrowthStage: String,
    season: String,
    irrigationType: String,
    previousCrop: String,
    previousFertilizerUsed: String,
    totalFarm: String,
    onCropChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onAreaChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onCropGrowthStageChange: (String) -> Unit,
    onSeasonChange: (String) -> Unit,
    onIrrigationTypeChange: (String) -> Unit,
    onPreviousCropChange: (String) -> Unit,
    onPreviousFertilizerUsedChange: (String) -> Unit,
    onTotalFarmChange: (String) -> Unit
) {
    FormCard(
        title = "Farm Information",
        subtitle = "Review auto-filled data and complete missing details"
    ) {
        StyledInputField(value = crop, onValueChange = onCropChange, label = "Crop")
        Spacer(modifier = Modifier.height(12.dp))
        StyledInputField(value = location, onValueChange = onLocationChange, label = "Location")
        Spacer(modifier = Modifier.height(12.dp))
        StyledInputField(value = area, onValueChange = onAreaChange, label = "Area")
        Spacer(modifier = Modifier.height(12.dp))
        StyledInputField(value = month, onValueChange = onMonthChange, label = "Month")
        Spacer(modifier = Modifier.height(12.dp))
        StyledInputField(value = cropGrowthStage, onValueChange = onCropGrowthStageChange, label = "Crop growth stage")
        Spacer(modifier = Modifier.height(12.dp))
        StyledInputField(value = season, onValueChange = onSeasonChange, label = "Season")
        Spacer(modifier = Modifier.height(12.dp))
        StyledInputField(value = irrigationType, onValueChange = onIrrigationTypeChange, label = "Irrigation type")
        Spacer(modifier = Modifier.height(12.dp))
        StyledInputField(value = previousCrop, onValueChange = onPreviousCropChange, label = "Previous crop")
        Spacer(modifier = Modifier.height(12.dp))
        StyledInputField(
            value = previousFertilizerUsed,
            onValueChange = onPreviousFertilizerUsedChange,
            label = "Previous fertilizer used"
        )
        Spacer(modifier = Modifier.height(12.dp))
        StyledInputField(value = totalFarm, onValueChange = onTotalFarmChange, label = "Total farm")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MlInputsSection(
    soil: String,
    pH: String,
    moisture: String,
    temp: String,
    humidity: String,
    nitrogen: String,
    phosphorus: String,
    potassium: String,
    onSoilChange: (String) -> Unit,
    onPHChange: (String) -> Unit,
    onMoistureChange: (String) -> Unit,
    onTempChange: (String) -> Unit,
    onHumidityChange: (String) -> Unit,
    onNitrogenChange: (String) -> Unit,
    onPhosphorusChange: (String) -> Unit,
    onPotassiumChange: (String) -> Unit
) {
    val soilOptions = listOf("Loamy", "Sandy", "Clay", "Black", "Red", "Clayey")
    var expanded by remember { mutableStateOf(false) }

    FormCard(
        title = "Soil & Nutrient Inputs",
        subtitle = "These values are used by the ML model"
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = soil,
                onValueChange = {},
                readOnly = true,
                label = { Text("Soil type") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = outlinedFieldColors()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                soilOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSoilChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        NumericInputField(value = pH, onValueChange = onPHChange, label = "Soil pH")
        Spacer(modifier = Modifier.height(12.dp))
        NumericInputField(value = moisture, onValueChange = onMoistureChange, label = "Soil moisture")
        Spacer(modifier = Modifier.height(12.dp))
        NumericInputField(value = temp, onValueChange = onTempChange, label = "Temperature")
        Spacer(modifier = Modifier.height(12.dp))
        NumericInputField(value = humidity, onValueChange = onHumidityChange, label = "Humidity")
        Spacer(modifier = Modifier.height(12.dp))
        NumericInputField(value = nitrogen, onValueChange = onNitrogenChange, label = "Nitrogen (N)")
        Spacer(modifier = Modifier.height(12.dp))
        NumericInputField(value = phosphorus, onValueChange = onPhosphorusChange, label = "Phosphorus (P)")
        Spacer(modifier = Modifier.height(12.dp))
        NumericInputField(value = potassium, onValueChange = onPotassiumChange, label = "Potassium (K)")
    }
}

@Composable
private fun FormCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = PrimaryGreenDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextSoft,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun StyledInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        minLines = 1,
        colors = outlinedFieldColors()
    )
}

@Composable
private fun NumericInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = outlinedFieldColors()
    )
}

@Composable
private fun PredictedFertilizerCard(
    fertilizerName: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F6EA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "ML Predicted Fertilizer",
                fontSize = 14.sp,
                color = PrimaryGreenDark,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = fertilizerName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreenDark
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun RecommendationSection(
    crop: String,
    location: String,
    plans: List<FertilizerPlan>,
    onRecalculate: () -> Unit
) {
    Column {
        Text(
            text = "Recommended Fertilizer Plan",
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryGreenDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tailored for $crop cultivation in $location",
            fontSize = 13.sp,
            color = TextSoft
        )

        Spacer(modifier = Modifier.height(18.dp))

        plans.forEachIndexed { index, plan ->
            ProfessionalFertilizerCard(plan = plan, index = index + 1)
            if (index < plans.lastIndex) {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onRecalculate,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Recalculate Recommendation",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    uiState: FertilizerUiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onCalculate: () -> Unit
) {
    Surface(
        tonalElevation = 10.dp,
        shadowElevation = 10.dp,
        color = CardLight
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.currentStep == 2) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreenSoft,
                        contentColor = PrimaryGreenDark
                    )
                ) {
                    Text("Back", fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = {
                    if (uiState.currentStep == 1) onNext() else onCalculate()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = if (uiState.currentStep == 1) "Next" else "Calculate Recommendation",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = PrimaryGreen,
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Generating recommendation...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreenDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Using ML and AI to prepare your fertilizer plan",
                    fontSize = 13.sp,
                    color = TextSoft
                )
            }
        }
    }
}

@Composable
private fun ProfessionalFertilizerCard(
    plan: FertilizerPlan,
    index: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryGreen.copy(alpha = 0.10f),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = index.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreenDark
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = plan.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PrimaryGreenDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(title = "Quantity", value = plan.quantity)
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(title = "Timing", value = plan.timing)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Purpose",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextSoft
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = plan.purpose,
                fontSize = 14.sp,
                color = Color(0xFF37473B),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = TextSoft,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = PrimaryGreenDark,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryGreen,
    unfocusedBorderColor = BorderSoft,
    focusedLabelColor = PrimaryGreen,
    cursorColor = PrimaryGreen
)

private fun getFieldValue(farm: Any, possibleNames: List<String>, fallback: String): String {
    return try {
        val methods = farm.javaClass.methods
        possibleNames.forEach { field ->
            val getterNames = listOf(
                "get" + field.replaceFirstChar { it.uppercase() },
                field
            )
            getterNames.forEach { getter ->
                val method = methods.firstOrNull { it.name == getter }
                val value = method?.invoke(farm)?.toString()
                if (!value.isNullOrBlank() && value != "null") return value
            }
        }
        fallback
    } catch (_: Exception) {
        fallback
    }
}

private fun parseStructuredPlans(text: String): List<FertilizerPlan> {
    val normalized = text.replace("\\n", "\n")
    val plans = mutableListOf<FertilizerPlan>()
    val blocks = normalized.split("Fertilizer:").drop(1)

    for (block in blocks) {
        val name = block.substringBefore("\n").trim()
        val quantity = Regex("Quantity:(.*?)(\\n|$)").find(block)?.groupValues?.get(1)?.trim() ?: ""
        val timing = Regex("Timing:(.*?)(\\n|$)").find(block)?.groupValues?.get(1)?.trim() ?: ""
        val purpose = Regex("Purpose:(.*?)(\\n|$)").find(block)?.groupValues?.get(1)?.trim() ?: ""

        if (name.isNotBlank()) {
            plans.add(
                FertilizerPlan(
                    name = name,
                    quantity = quantity,
                    timing = timing,
                    purpose = purpose
                )
            )
        }
    }

    return plans
}