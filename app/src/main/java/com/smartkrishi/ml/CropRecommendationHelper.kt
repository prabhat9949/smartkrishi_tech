package com.smartkrishi.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

class CropRecommendationHelper(private val context: Context) {

    private var interpreter: Interpreter? = null

    // SAFE fallback labels (must match model output size ideally)
    private var cropLabels: List<String> = listOf(
        "Rice",
        "Wheat",
        "Maize",
        "Cotton",
        "Sugarcane"
    )

    // Identity scaler (NO normalization)
    private val meanValues = FloatArray(7) { 0f }
    private val stdValues = FloatArray(7) { 1f }

    init {
        loadModel()
        Log.w("ML", "⚠️ Using SAFE fallback labels & scaler (no JSON files)")
    }

    // ---------------------------------------------------------
    // MODEL LOADING
    // ---------------------------------------------------------
    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile("SoilSuitabilityModel.tflite")
            interpreter = Interpreter(modelBuffer)
            Log.d("ML", "✅ TFLite model loaded successfully")
        } catch (e: Exception) {
            Log.e("ML", "❌ Failed to load TFLite model", e)
            interpreter = null
        }
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val afd = context.assets.openFd(filename)
        FileInputStream(afd.fileDescriptor).use { fis ->
            return fis.channel.map(
                FileChannel.MapMode.READ_ONLY,
                afd.startOffset,
                afd.declaredLength
            )
        }
    }

    // ---------------------------------------------------------
    // PREDICTION
    // ---------------------------------------------------------
    fun predictCrops(
        nitrogen: Float,
        phosphorus: Float,
        potassium: Float,
        temperature: Float,
        humidity: Float,
        ph: Float,
        rainfall: Float
    ): List<CropPrediction> {

        val model = interpreter ?: return emptyList()

        // Normalize (identity – safe mode)
        val normalizedInput = floatArrayOf(
            (nitrogen - meanValues[0]) / stdValues[0],
            (phosphorus - meanValues[1]) / stdValues[1],
            (potassium - meanValues[2]) / stdValues[2],
            (temperature - meanValues[3]) / stdValues[3],
            (humidity - meanValues[4]) / stdValues[4],
            (ph - meanValues[5]) / stdValues[5],
            (rainfall - meanValues[6]) / stdValues[6]
        )

        // Input shape: [1,7]
        val input = arrayOf(normalizedInput)

        // Output shape: [1, numCrops]
        val output = Array(1) { FloatArray(cropLabels.size) }

        return try {
            model.run(input, output)

            Log.d("ML_INPUT", normalizedInput.joinToString())
            Log.d("ML_OUTPUT", output[0].joinToString())

            cropLabels.indices
                .map { i ->
                    CropPrediction(
                        cropName = cropLabels[i].replaceFirstChar {
                            if (it.isLowerCase())
                                it.titlecase(Locale.getDefault())
                            else it.toString()
                        },
                        probability = output[0].getOrElse(i) { 0f }
                    )
                }
                .sortedByDescending { it.probability }
                .take(3)

        } catch (e: Exception) {
            Log.e("ML", "❌ Inference failed", e)
            emptyList()
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

// ---------------------------------------------------------
// DATA MODEL
// ---------------------------------------------------------
data class CropPrediction(
    val cropName: String,
    val probability: Float
) {
    val confidencePercentage: Int
        get() = (probability * 100f).coerceIn(0f, 100f).toInt()
}
