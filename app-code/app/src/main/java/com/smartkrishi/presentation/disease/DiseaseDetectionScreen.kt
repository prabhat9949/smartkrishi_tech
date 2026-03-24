package com.smartkrishi.presentation.disease

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    // ================= GALLERY PICKER =================
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            lastImageUri = uri
            navController.navigate(Screen.DiseaseProcessing.pass(uri.toString()))
        }
    }

    // ================= CAMERA CAPTURE =================
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uri = saveImage(bitmap, context)
            lastImageUri = uri
            navController.navigate(Screen.DiseaseProcessing.pass(uri.toString()))
        }
    }

    val askCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
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
                    Text("Detect Disease", fontWeight = FontWeight.Bold, color = textPrimary)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                "How would you like to scan for disease?",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = textPrimary
            )

            // ======================= ROW 1 =======================
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
                    } else askCameraPermission.launch(Manifest.permission.CAMERA)
                }
            }

            // ======================= ROW 2 (Disabled) =======================
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
                    icon = Icons.Default.VideoCameraFront,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }
        }
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
        if (enabled) cardColor else cardColor.copy(alpha = 0.4f)
    )

    ElevatedCard(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        onClick = { if (enabled) onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) Color(0xFF2E7D32) else Color.Gray,
                modifier = Modifier.size(36.dp)
            )

            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = textPrimary)
                Text(subtitle, color = textSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
