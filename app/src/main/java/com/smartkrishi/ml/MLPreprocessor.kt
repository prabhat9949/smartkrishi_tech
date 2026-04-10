package com.smartkrishi.ml

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class MLPreprocessor(context: Context) {

    private val soilClasses: List<String>
    private val fertClasses: List<String>
    private val meanValues: FloatArray
    private val scaleValues: FloatArray

    companion object {
        private const val TAG = "ML_PREPROCESSOR"
        private const val EXPECTED_INPUT_SIZE = 8
        private const val SOIL_ENCODER_FILE = "soil_encoder.json"
        private const val FERT_ENCODER_FILE = "fert_encoder.json"
        private const val SCALER_FILE = "scaler.json"
    }

    init {
        try {
            val soilJson = readAssetText(context, SOIL_ENCODER_FILE)
            soilClasses = parseStringArray(soilJson)

            val fertJson = readAssetText(context, FERT_ENCODER_FILE)
            fertClasses = parseStringArray(fertJson)

            val scalerJson = JSONObject(readAssetText(context, SCALER_FILE))

            meanValues = parseFloatArray(scalerJson.getJSONArray("mean"))
            scaleValues = parseFloatArray(scalerJson.getJSONArray("scale"))

            require(meanValues.size == EXPECTED_INPUT_SIZE) {
                "Scaler mean size must be $EXPECTED_INPUT_SIZE but was ${meanValues.size}"
            }

            require(scaleValues.size == EXPECTED_INPUT_SIZE) {
                "Scaler scale size must be $EXPECTED_INPUT_SIZE but was ${scaleValues.size}"
            }

            require(fertClasses.isNotEmpty()) {
                "fert_encoder.json is empty"
            }

            require(soilClasses.isNotEmpty()) {
                "soil_encoder.json is empty"
            }

            Log.d(TAG, "Initialization successful")
            Log.d(TAG, "Soil classes count: ${soilClasses.size}")
            Log.d(TAG, "Fertilizer classes count: ${fertClasses.size}")
            Log.d(TAG, "Scaler mean size: ${meanValues.size}")
            Log.d(TAG, "Scaler scale size: ${scaleValues.size}")
            Log.d(TAG, "Soil classes: $soilClasses")
            Log.d(TAG, "Fertilizer classes: $fertClasses")

        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed: ${e.message}", e)
            throw RuntimeException("MLPreprocessor failed to initialize", e)
        }
    }

    private fun readAssetText(context: Context, fileName: String): String {
        return try {
            context.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read asset file: $fileName", e)
            throw RuntimeException("Could not read asset file: $fileName", e)
        }
    }

    private fun parseStringArray(json: String): List<String> {
        return try {
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { index ->
                jsonArray.getString(index).trim()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse string array JSON", e)
            throw RuntimeException("Invalid string array JSON format", e)
        }
    }

    private fun parseFloatArray(jsonArray: JSONArray): FloatArray {
        return try {
            FloatArray(jsonArray.length()) { index ->
                jsonArray.getDouble(index).toFloat()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse float array JSON", e)
            throw RuntimeException("Invalid float array JSON format", e)
        }
    }

    fun encodeSoil(soil: String): Float {
        val normalizedSoil = soil.trim()
        val index = soilClasses.indexOfFirst {
            it.equals(normalizedSoil, ignoreCase = true)
        }

        return if (index >= 0) {
            Log.d(TAG, "Encoded soil '$normalizedSoil' to index $index")
            index.toFloat()
        } else {
            Log.w(TAG, "Unknown soil type '$normalizedSoil', defaulting to index 0")
            0f
        }
    }

    fun scale(input: FloatArray): FloatArray {
        if (input.size != EXPECTED_INPUT_SIZE) {
            Log.e(
                TAG,
                "Input size mismatch: got ${input.size}, expected $EXPECTED_INPUT_SIZE"
            )
            throw IllegalArgumentException(
                "Input size must be $EXPECTED_INPUT_SIZE but was ${input.size}"
            )
        }

        val scaled = FloatArray(input.size) { index ->
            val mean = meanValues[index]
            val scale = scaleValues[index]
            val divisor = if (scale == 0f) {
                Log.w(TAG, "Scale value at index $index is 0, using 1 instead")
                1f
            } else {
                scale
            }

            (input[index] - mean) / divisor
        }

        Log.d(TAG, "Raw input: ${input.contentToString()}")
        Log.d(TAG, "Scaled input: ${scaled.contentToString()}")

        return scaled
    }

    fun getFertilizerCount(): Int = fertClasses.size

    fun getSoilCount(): Int = soilClasses.size

    fun getFertilizerLabels(): List<String> = fertClasses.toList()

    fun getSoilLabels(): List<String> = soilClasses.toList()

    fun decodeFertilizer(index: Int): String {
        return if (index in fertClasses.indices) {
            val fertilizer = fertClasses[index]
            Log.d(TAG, "Decoded fertilizer index $index to '$fertilizer'")
            fertilizer
        } else {
            Log.e(TAG, "Invalid fertilizer index: $index")
            "Unknown Fertilizer"
        }
    }
}