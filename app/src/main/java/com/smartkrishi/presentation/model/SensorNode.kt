package com.smartkrishi.presentation.model

data class SensorNode(
    var id: String = "",

    val moisture: Float? = null,
    val humidity: Float? = null,
    val temperature: Float? = null,

    // NPK (matching Firebase keys)
    val n: Float? = null,
    val p: Float? = null,
    val k: Float? = null,

    val ph: Float? = null,
    val ec: Float? = null
)