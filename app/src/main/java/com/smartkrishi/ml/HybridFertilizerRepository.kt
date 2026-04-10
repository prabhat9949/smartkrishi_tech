package com.smartkrishi.ml

import android.content.Context
import android.util.Log
import com.smartkrishi.data.ai.OpenAIService
import com.smartkrishi.data.firebase.FirebaseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class HybridResult(
    val predictedFertilizer: String,
    val confidence: Float,
    val finalText: String
)

class HybridFertilizerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseRepository: FirebaseRepository
) {

    private val model = FertilizerModelHelper(context)
    private val preprocessor = MLPreprocessor(context)
    private val openAI = OpenAIService()

    companion object {
        private const val TAG = "HybridFertilizerRepo"
    }

    suspend fun getFullPrediction(
        soil: String,
        pH: Float,
        moisture: Float,
        temp: Float,
        humidity: Float,
        n: Float,
        p: Float,
        k: Float,
        crop: String,
        location: String
    ): HybridResult {

        return try {
            val encodedSoil = preprocessor.encodeSoil(soil)

            val rawInput = floatArrayOf(
                encodedSoil,
                pH,
                moisture,
                temp,
                humidity,
                n,
                p,
                k
            )

            val scaledInput = preprocessor.scale(rawInput)

            val output = model.predict(scaledInput)
            Log.d(TAG, "Model output: ${output.contentToString()}")

            val maxIndex = output.indices.maxByOrNull { output[it] } ?: 0
            val safeIndex = maxIndex.coerceIn(0, preprocessor.getFertilizerCount() - 1)

            val predictedFertilizer = preprocessor.decodeFertilizer(safeIndex)
            val confidence = output.getOrElse(maxIndex) { 0f }.coerceIn(0f, 1f)

            Log.d(TAG, "Predicted: $predictedFertilizer, confidence: $confidence")

            val prompt = buildPrompt(
                predictedFertilizer = predictedFertilizer,
                confidence = confidence,
                soil = soil,
                pH = pH,
                moisture = moisture,
                temp = temp,
                humidity = humidity,
                n = n,
                p = p,
                k = k,
                crop = crop,
                location = location
            )

            val finalText = try {
                openAI.getRecommendation(prompt)
            } catch (e: Exception) {
                Log.e(TAG, "LLM failed: ${e.message}", e)
                """
Fertilizer: $predictedFertilizer
Quantity: As per soil test
Timing: Early stage
Purpose: Improve nutrients
""".trimIndent()
            }

            // 🔄 Save training sample to Firestore
            saveFertilizerSample(
                soil = soil,
                pH = pH,
                moisture = moisture,
                temp = temp,
                humidity = humidity,
                n = n,
                p = p,
                k = k,
                crop = crop,
                location = location,
                predicted = predictedFertilizer,
                confidence = confidence,
                finalText = finalText
            )

            HybridResult(
                predictedFertilizer = predictedFertilizer,
                confidence = confidence,
                finalText = finalText
            )

        } catch (e: Exception) {
            Log.e(TAG, "Prediction failed: ${e.message}", e)
            HybridResult(
                predictedFertilizer = "Unknown",
                confidence = 0f,
                finalText = "Unable to generate recommendation. Please try again."
            )
        }
    }

    private fun buildPrompt(
        predictedFertilizer: String,
        confidence: Float,
        soil: String,
        pH: Float,
        moisture: Float,
        temp: Float,
        humidity: Float,
        n: Float,
        p: Float,
        k: Float,
        crop: String,
        location: String
    ): String {
        return """
You are an expert Indian agronomist.

A machine learning model predicted:
Fertilizer: $predictedFertilizer
Confidence: ${"%.2f".format(confidence)}

Farm details:
Crop: $crop
Location: $location
Soil Type: $soil
Soil pH: $pH
Soil Moisture: $moisture
Temperature: $temp
Humidity: $humidity
Nitrogen (N): $n
Phosphorus (P): $p
Potassium (K): $k

Use the predicted fertilizer as the main recommendation.
Then create a practical fertilizer plan for Indian farmers.

Return EXACTLY this structure with 3 fertilizers:

Fertilizer:
Quantity:
Timing:
Purpose:

Fertilizer:
Quantity:
Timing:
Purpose:

Fertilizer:
Quantity:
Timing:
Purpose:
""".trimIndent()
    }

    private suspend fun saveFertilizerSample(
        soil: String,
        pH: Float,
        moisture: Float,
        temp: Float,
        humidity: Float,
        n: Float,
        p: Float,
        k: Float,
        crop: String,
        location: String,
        predicted: String,
        confidence: Float,
        finalText: String
    ) {
        val data = mapOf(
            "soil" to soil,
            "pH" to pH,
            "moisture" to moisture,
            "temperature" to temp,
            "humidity" to humidity,
            "N" to n,
            "P" to p,
            "K" to k,
            "crop" to crop,
            "location" to location,
            "predictedFertilizer" to predicted,
            "confidence" to confidence,
            "llmRecommendation" to finalText,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            firebaseRepository.saveFertilizerData(data)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save fertilizer sample: ${e.message}", e)
        }
    }

    fun close() {
        model.close()
    }
}