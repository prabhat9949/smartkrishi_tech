package com.smartkrishi.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.MappedByteBuffer

private const val TAG = "PlantDiseaseClassifier"

data class DiseaseResult(
    val crop: String,
    val disease: String,
    val confidence: Float
)

class PlantDiseaseClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null

    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f)) // pixel / 255.0
        .build()

    fun classify(bitmap: Bitmap, selectedCrop: String): DiseaseResult {
        try {
            Log.d(TAG, "🔄 Starting classification for $selectedCrop")

            validateModelExists(selectedCrop)

            val modelPath = "models/$selectedCrop/$selectedCrop.tflite"
            val modelBuffer: MappedByteBuffer =
                FileUtil.loadMappedFile(context, modelPath)

            interpreter = Interpreter(modelBuffer)

            val labels = loadClassLabels(selectedCrop)

            // 🔹 Preprocess image
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            val processedImage = imageProcessor.process(tensorImage)

            // 🔹 Read output tensor shape FROM MODEL (CRITICAL FIX)
            val outputTensor = interpreter!!.getOutputTensor(0)
            val outputShape = outputTensor.shape()   // [1, numClasses]
            val outputSize = outputShape[1]

            val output = Array(1) { FloatArray(outputSize) }

            interpreter!!.run(processedImage.buffer, output)

            val probabilities = output[0]
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] }
                ?: throw Exception("Invalid model output")

            val confidence = probabilities[maxIndex]
            val diseaseName = labels[maxIndex] ?: "Unknown Disease"

            Log.d(
                TAG,
                "✅ SUCCESS: $selectedCrop → $diseaseName (${String.format("%.2f", confidence * 100)}%)"
            )

            return DiseaseResult(
                crop = selectedCrop,
                disease = diseaseName,
                confidence = confidence
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Classification failed for $selectedCrop", e)
            throw Exception("Model classification failed for $selectedCrop: ${e.message}", e)
        }
    }

    // -------------------------------------------------------

    private fun validateModelExists(crop: String) {
        val modelPath = "models/$crop/$crop.tflite"
        val labelPath = "models/$crop/class_labels.json"

        try {
            context.assets.open(modelPath).close()
            context.assets.open(labelPath).close()
            Log.d(TAG, "✅ Model files verified for $crop")
        } catch (e: Exception) {
            throw Exception("Model files missing for $crop. Expected at assets/models/$crop/")
        }
    }

    // -------------------------------------------------------

    private fun loadClassLabels(crop: String): Map<Int, String> {
        val labelPath = "models/$crop/class_labels.json"

        val jsonString = context.assets.open(labelPath)
            .bufferedReader()
            .use { it.readText() }

        val jsonObject = JSONObject(jsonString)

        val labelMap = mutableMapOf<Int, String>()
        jsonObject.keys().forEach { key ->
            labelMap[key.toInt()] = jsonObject.getString(key)
        }

        Log.d(TAG, "📄 Loaded labels: $labelMap")
        return labelMap
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
