package com.smartkrishi.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class DiseasePrediction(
    val crop: String,
    val disease: String,
    val confidence: Float
)

class PlantDiseaseClassifier(private val context: Context) : AutoCloseable {

    private val interpreter: Interpreter
    private val labels: List<String>

    private val inputShape: IntArray
    private val inputImageSize: Int

    init {
        // Load TFLite model
        interpreter = Interpreter(loadModelFile("plant_disease.tflite"))

        // Load labels
        labels = loadLabels("labels.txt")

        // Read model input shape: [1, height, width, 3]
        inputShape = interpreter.getInputTensor(0).shape()
        inputImageSize = inputShape[1]   // usually 224
    }

    private fun loadModelFile(fileName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(fileName)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    private fun loadLabels(fileName: String): List<String> {
        val list = mutableListOf<String>()
        BufferedReader(InputStreamReader(context.assets.open(fileName))).useLines { lines ->
            lines.forEach { list.add(it.trim()) }
        }
        return list
    }

    fun classify(bitmap: Bitmap): DiseasePrediction {

        val inputTensor = interpreter.getInputTensor(0)
        val shape = inputTensor.shape() // e.g., [1, 224, 224, 3]
        val inputHeight = shape[1]
        val inputWidth = shape[2]
        val inputChannels = shape[3]

        val resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)

        val inputBytes = 4 * inputHeight * inputWidth * inputChannels
        val inputBuffer = java.nio.ByteBuffer.allocateDirect(inputBytes)
        inputBuffer.order(java.nio.ByteOrder.nativeOrder())

        val pixels = IntArray(inputWidth * inputHeight)
        resized.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255f
            val g = (pixel shr 8 and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        val output = TensorBuffer.createFixedSize(
            intArrayOf(1, labels.size),
            DataType.FLOAT32
        )

        interpreter.run(inputBuffer, output.buffer)

        val scores = output.floatArray
        var maxIdx = scores.indices.maxByOrNull { scores[it] } ?: 0
        val maxScore = scores[maxIdx]

        val parts = labels[maxIdx].split("___")
        val crop = parts.getOrNull(0) ?: "Unknown"
        val disease = parts.getOrNull(1) ?: "Healthy"

        return DiseasePrediction(
            crop = crop,
            disease = disease,
            confidence = maxScore * 100f
        )
    }

override fun close() {
    interpreter.close()
}
}

