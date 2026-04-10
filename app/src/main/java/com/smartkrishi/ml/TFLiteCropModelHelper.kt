package com.smartkrishi.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import kotlin.math.exp

class TFLiteCropModelHelper(context: Context) {

    private val interpreter: Interpreter

    init {
        val file = FileUtil.loadMappedFile(context, "crop_model.tflite")
        interpreter = Interpreter(file)
    }

    fun predict(input: FloatArray): Pair<Int, FloatArray> {
        require(input.size == 7) { "Model input must contain exactly 7 features." }

        val inputBuffer = arrayOf(input.copyOf())
        val outputBuffer = Array(1) { FloatArray(LabelEncoder.size()) }

        interpreter.run(inputBuffer, outputBuffer)

        val raw = outputBuffer[0]
        val probs = softmax(raw)
        val maxIndex = probs.indices.maxByOrNull { probs[it] } ?: 0

        return Pair(maxIndex, probs)
    }

    private fun softmax(logits: FloatArray): FloatArray {
        if (logits.isEmpty()) return floatArrayOf(1f)

        val maxLogit = logits.maxOrNull() ?: 0f
        val expVals = logits.map { exp((it - maxLogit).toDouble()).toFloat() }
        val sum = expVals.sum().takeIf { it > 0f } ?: 1f

        return expVals.map { it / sum }.toFloatArray()
    }

    fun close() {
        interpreter.close()
    }
}