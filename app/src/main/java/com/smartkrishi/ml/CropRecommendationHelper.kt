package com.smartkrishi.ml

import android.content.Context

class CropRecommendationHelper(context: Context) {

    private val model = try {
        TFLiteCropModelHelper(context)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun predict(
        n: Float,
        p: Float,
        k: Float,
        temp: Float,
        humidity: Float,
        ph: Float,
        rainfall: Float
    ): Pair<String, Float> {

        // ✅ Step 1: Prepare input
        val input = floatArrayOf(n, p, k, temp, humidity, ph, rainfall)

        // ✅ Step 2: Apply scaling
        val scaled = Scaler.transform(input)

        // ✅ Step 3: Run model safely
        val (index, probs) = model?.predict(scaled)
            ?: Pair(0, FloatArray(LabelEncoder.size()) { 0f })

        // ✅ Step 4: Decode crop
        val crop = LabelEncoder.decode(index)

        // ✅ Step 5: Confidence
        val confidence = probs.getOrElse(index) { 0f }

        return Pair(crop, confidence)
    }
}