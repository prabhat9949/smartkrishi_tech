package com.smartkrishi.presentation.disease

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.smartkrishi.ml.PlantDiseaseClassifier
import com.smartkrishi.presentation.navigation.Screen
import com.smartkrishi.presentation.theme.ThemeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseProcessingScreen(
    navController: NavController,
    imageUri: Uri?
) {
    if (imageUri == null) {
        navController.popBackStack()
        return
    }

    val context = LocalContext.current
    val isDark = ThemeState.isDarkTheme.value
    val backgroundColor = if (isDark) Color(0xFF0F2418) else Color(0xFFF4FBF5)
    val textPrimary = if (isDark) Color(0xFFE8F5E9) else Color.Black

    var progress by remember { mutableStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageUri) {
        try {
            repeat(20) {
                progress = (it + 1) * 0.05f
                delay(60)
            }

            val result = withContext(Dispatchers.Default) {
                val bitmap = loadBitmapFromUri(context, imageUri)
                val classifier = PlantDiseaseClassifier(context.applicationContext)

                try {
                    classifier.classify(bitmap)
                } finally {
                    classifier.close()
                }
            }

            navController.navigate(
                Screen.DiseaseResult.pass(result.crop, result.disease, result.confidence)
            )

        } catch (e: Exception) {
            errorMessage = "Local model error: ${e.message}"
        }
    }

    if (errorMessage != null) {
        ErrorDialog(
            message = errorMessage!!,
            onDismiss = { navController.popBackStack() }
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
                title = { Text("Analyzing Image…", color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(26.dp))

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50.dp)),
            )

            Spacer(Modifier.height(12.dp))

            Text(
                when {
                    progress < 0.2f -> "Identifying leaf pixels…"
                    progress < 0.4f -> "Analyzing color patterns…"
                    progress < 0.6f -> "Scanning texture map…"
                    progress < 0.8f -> "Matching with trained dataset…"
                    else -> "Finalizing diagnosis…"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = textPrimary
            )
        }
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            .copy(Bitmap.Config.ARGB_8888, true)
    }
}

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text("OK") } },
        title = { Text("Error") },
        text = { Text(message) }
    )
}
