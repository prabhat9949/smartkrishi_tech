package com.smartkrishi.ml

object Scaler {

    private val mean = floatArrayOf(
        50.551818f,
        53.362728f,
        48.14909f,
        25.616243f,
        71.48178f,
        6.46948f,
        103.46365f
    )

    private val std = floatArrayOf(
        36.908943f,
        32.978386f,
        50.63642f,
        5.062598f,
        22.25875f,
        0.7737618f,
        54.945896f
    )

    fun transform(input: FloatArray): FloatArray {
        require(input.size == 7) { "Scaler input must have exactly 7 values." }

        return FloatArray(7) { index ->
            val divisor = if (std[index] == 0f) 1f else std[index]
            (input[index] - mean[index]) / divisor
        }
    }
}