package com.smartkrishi.presentation.disease

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.smartkrishi.ml.PlantDiseaseClassifier
import com.smartkrishi.presentation.navigation.Screen
import com.smartkrishi.presentation.theme.ThemeState
import com.smartkrishi.utils.OpenAIHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "DiseaseProcessing"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseProcessingScreen(
    navController: NavController,
    imageUri: Uri?,
    selectedCrop: String
) {

    if (imageUri == null) {
        navController.popBackStack()
        return
    }

    val context = LocalContext.current
    val isDark = ThemeState.isDarkTheme.value

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0xFF0A1C12), Color(0xFF0F2418), Color(0xFF0A1C12))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFF5FAF5), Color(0xFFE8F5E9), Color(0xFFF5FAF5))
        )
    }

    val surfaceColor = if (isDark) Color(0xFF153525) else Color.White
    val textPrimary = if (isDark) Color(0xFFE8F5E9) else Color(0xFF1A3C34)
    val accentGreen = Color(0xFF2E7D32)

    var progress by remember { mutableStateOf(0f) }
    var currentStatus by remember { mutableStateOf("Preparing image...") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val animatedProgress by animateFloatAsState(progress, label = "")

    LaunchedEffect(imageUri, selectedCrop) {
        try {

            // ---------------- STATUS STEP 1 ----------------
            currentStatus = "Loading image..."
            delay(500)
            progress = 0.15f

            val originalBitmap = loadBitmapFromUri(context, imageUri)

            // ---------------- STATUS STEP 2 ----------------
            currentStatus = "Optimizing image..."
            delay(600)
            progress = 0.30f

            val resizedBitmap = resizeBitmap(originalBitmap)

            // ---------------- STATUS STEP 3 ----------------
            currentStatus = "Running TensorFlow model..."
            delay(600)
            progress = 0.50f

            val tfResult = withContext(Dispatchers.Default) {
                val classifier = PlantDiseaseClassifier(context)
                try {
                    classifier.classify(resizedBitmap, selectedCrop)
                } finally {
                    classifier.close()
                }
            }

            Log.d(TAG, "TF Disease: ${tfResult.disease}")
            Log.d(TAG, "TF Confidence: ${tfResult.confidence}")

            val tfFormatted =
                "${tfResult.disease} (${(tfResult.confidence * 100).toInt()}%)"

            // ---------------- STATUS STEP 4 ----------------
            currentStatus = "Validating with AI vision..."
            delay(700)
            progress = 0.75f

            val openAIResponse = try {
                OpenAIHelper.analyzeLeaf(
                    bitmap = resizedBitmap,
                    crop = selectedCrop,
                    tfPrediction = tfFormatted
                )
            } catch (e: Exception) {
                Log.e(TAG, "OpenAI Error: ${e.message}")
                null
            }

            Log.d(TAG, "OpenAI Response: $openAIResponse")

            // ---------------- STATUS STEP 5 ----------------
            currentStatus = "Cross-checking results..."
            delay(600)
            progress = 0.90f

            val finalDisease = determineFinalResult(
                tfDisease = tfResult.disease,
                tfConfidence = tfResult.confidence,
                aiResponse = openAIResponse
            )

            currentStatus = "Diagnosis complete"
            progress = 1f
            delay(800)

            navController.navigate(
                Screen.DiseaseResult.pass(
                    crop = selectedCrop,
                    disease = finalDisease,
                    confidence = tfResult.confidence,
                    imageUri = imageUri.toString()
                )
            ) {
                popUpTo(Screen.DiseaseDetection.route)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Processing Failed: ${e.message}")
            errorMessage = "Analysis failed: ${e.localizedMessage}"
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { navController.popBackStack() },
            confirmButton = {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Return")
                }
            },
            title = { Text("Analysis Error") },
            text = { Text(errorMessage!!) },
            containerColor = surfaceColor
        )
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Analyzing $selectedCrop") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .shadow(10.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(surfaceColor)
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    strokeWidth = 8.dp,
                    color = accentGreen
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "${(animatedProgress * 100).toInt()}%",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(true, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        currentStatus,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "SmartKrishi AI Diagnosis",
                    fontSize = 12.sp,
                    color = textPrimary.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun determineFinalResult(
    tfDisease: String,
    tfConfidence: Float,
    aiResponse: String?
): String {

    val VERY_LOW = 0.20f
    val LOW = 0.40f

    if (tfConfidence < VERY_LOW)
        return "Wrong crop selected"

    if (aiResponse.isNullOrBlank())
        return tfDisease

    if (aiResponse.contains("wrong crop", true))
        return "Wrong crop selected"

    if (tfConfidence < LOW &&
        !aiResponse.lowercase().contains(tfDisease.lowercase())
    )
        return "Wrong crop selected"

    return aiResponse
}

private fun resizeBitmap(bitmap: Bitmap): Bitmap {
    val maxSize = 1024
    val ratio = minOf(
        maxSize.toFloat() / bitmap.width,
        maxSize.toFloat() / bitmap.height
    )

    return if (ratio < 1f) {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )
    } else bitmap
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    return if (bitmap.config != Bitmap.Config.ARGB_8888) {
        bitmap.copy(Bitmap.Config.ARGB_8888, true)
    } else {
        bitmap
    }
}