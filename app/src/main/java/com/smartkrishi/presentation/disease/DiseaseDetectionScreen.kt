package com.smartkrishi.presentation.disease

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.smartkrishi.presentation.navigation.Screen
import com.smartkrishi.presentation.theme.ThemeState
import com.smartkrishi.utils.SaveImageUtil.saveImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetectionScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val isDark = ThemeState.isDarkTheme.value

    val backgroundColor = if (isDark) Color(0xFF0A1C12) else Color(0xFFF5FAF5)
    val cardColor = if (isDark) Color(0xFF153525) else Color.White
    val textPrimary = if (isDark) Color(0xFFE8F5E9) else Color.Black
    val textSecondary = if (isDark) Color(0xFFB8D0C1) else Color.Gray

    var lastImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCrop by remember { mutableStateOf("Tomato") }
    var showCropDialog by remember { mutableStateOf(false) }

    // Updated crop options based on your model structure
    val cropOptions = listOf(
        "Apple",
        "Blueberry",
        "Corn",
        "Grape",
        "Pepper",
        "Potato",
        "Strawberry",
        "Tomato"
    )

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            lastImageUri = uri
            showCropDialog = true
        }
    }

    // Camera Capture
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = saveImage(bitmap, context)
            lastImageUri = uri
            showCropDialog = true
        }
    }

    val askCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
    }

    // Crop Selection Dialog
    if (showCropDialog) {
        AlertDialog(
            onDismissRequest = {
                showCropDialog = false
                lastImageUri = null
            },
            containerColor = if (isDark) Color(0xFF153525) else Color.White,
            title = {
                Text(
                    "Select Crop Type",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Choose the crop type for accurate disease detection:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )

                    Spacer(Modifier.height(16.dp))

                    cropOptions.forEach { crop ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedCrop = crop },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCrop == crop)
                                Color(0xFF2E7D32).copy(alpha = 0.15f)
                            else
                                Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCrop == crop,
                                    onClick = { selectedCrop = crop },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF2E7D32),
                                        unselectedColor = textSecondary
                                    )
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = crop,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = textPrimary,
                                        fontWeight = if (selectedCrop == crop)
                                            FontWeight.SemiBold
                                        else
                                            FontWeight.Normal
                                    )
                                    if (selectedCrop == crop) {
                                        Text(
                                            text = "Selected",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCropDialog = false
                        lastImageUri?.let { uri ->
                            navController.navigate(
                                Screen.DiseaseProcessing.pass(uri.toString(), selectedCrop)
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Analyze Disease", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCropDialog = false
                        lastImageUri = null
                    }
                ) {
                    Text("Cancel", color = textSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textPrimary
                ),
                title = {
                    Text(
                        "Disease Detection",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "How would you like to scan?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    color = textPrimary
                )

                Text(
                    "Upload or capture a clear image of the affected plant leaf",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
                )
            }

            Spacer(Modifier.height(8.dp))

            // Active Options Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiseaseOptionCard(
                    title = "Upload Image",
                    subtitle = "Choose from gallery",
                    enabled = true,
                    icon = Icons.Default.Image,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                ) {
                    galleryLauncher.launch("image/*")
                }

                DiseaseOptionCard(
                    title = "Take Photo",
                    subtitle = "Capture using camera",
                    enabled = true,
                    icon = Icons.Default.CameraAlt,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                ) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraLauncher.launch(null)
                    } else {
                        askCameraPermission.launch(Manifest.permission.CAMERA)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Coming Soon Section
            Text(
                "Coming Soon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary
            )

            // Disabled Options Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiseaseOptionCard(
                    title = "Rover Camera",
                    subtitle = "Remote rover mode",
                    enabled = false,
                    icon = Icons.Default.VideoCameraFront,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )

                DiseaseOptionCard(
                    title = "Auto Scan",
                    subtitle = "Scan entire plant row",
                    enabled = false,
                    icon = Icons.Default.Scanner,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }

            Spacer(Modifier.height(16.dp))

            // Tips Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Tips for Better Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        )
                    }

                    TipItem("📸", "Take clear, well-lit photos of affected leaves", textSecondary)
                    TipItem("🔍", "Focus on the diseased area", textSecondary)
                    TipItem("☀️", "Use natural daylight when possible", textSecondary)
                    TipItem("📏", "Fill the frame with the leaf", textSecondary)
                }
            }
        }
    }
}

@Composable
private fun TipItem(emoji: String, text: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            emoji,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
fun DiseaseOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (enabled) cardColor else cardColor.copy(alpha = 0.4f),
        label = "cardColor"
    )

    val iconTint by animateColorAsState(
        targetValue = if (enabled) Color(0xFF2E7D32) else Color.Gray,
        label = "iconTint"
    )

    ElevatedCard(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        onClick = { if (enabled) onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (enabled) 4.dp else 1.dp,
            pressedElevation = if (enabled) 8.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(36.dp)
                )

                Column {
                    Text(
                        title,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) textPrimary else textPrimary.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        subtitle,
                        color = if (enabled) textSecondary else textSecondary.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // "Coming Soon" badge for disabled cards
            if (!enabled) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                ) {
                    Text(
                        "Soon",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
