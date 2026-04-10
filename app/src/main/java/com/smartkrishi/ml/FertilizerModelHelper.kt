package com.smartkrishi.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

class FertilizerModelHelper(context: Context) {

    private var interpreter: Interpreter? = null

    companion object {
        private const val TAG = "FertilizerModel"
        private const val INPUT_SIZE = 8          // 8 features
        private const val OUTPUT_CLASSES = 7      // 7 fertilizer classes
        private const val MODEL_NAME = "fertilizer_model.tflite"
    }

    init {
        try {
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(false) // set true only if you know NNAPI works
            }

            val assetList = context.assets.list("") ?: emptyArray()
            require(assetList.contains(MODEL_NAME)) {
                "$MODEL_NAME not found in assets"
            }

            val modelBuffer = FileUtil.loadMappedFile(context, MODEL_NAME)
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "Model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Model loading failed: ${e.message}", e)
            interpreter = null
        }
    }

    fun predict(input: FloatArray): FloatArray {
        if (input.size != INPUT_SIZE) {
            Log.e(TAG, "Invalid input size: ${input.size}, expected: $INPUT_SIZE")
            return FloatArray(OUTPUT_CLASSES) { 0f }
        }

        val interp = interpreter ?: run {
            Log.e(TAG, "Interpreter is null (not initialized)")
            return FloatArray(OUTPUT_CLASSES) { 0f }
        }

        return try {
            val modelInput = arrayOf(input)                // [1, 8]
            val output = Array(1) { FloatArray(OUTPUT_CLASSES) } // [1, 7]
            interp.run(modelInput, output)
            Log.d(TAG, "Prediction: ${output[0].contentToString()}")
            output[0]
        } catch (e: Exception) {
            Log.e(TAG, "Prediction failed: ${e.message}", e)
            FloatArray(OUTPUT_CLASSES) { 0f }
        }
    }

    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            Log.d(TAG, "Interpreter closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter: ${e.message}", e)
        }
    }
}