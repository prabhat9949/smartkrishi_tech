package com.smartkrishi.presentation.model

data class SensorNode(
    val id: String = "",
    val moisture: Float? = null,
    val temp: Float? = null,
    val nitrogen: Float? = null,
    val phosphorus: Float? = null,
    val potassium: Float? = null,
    val ph: Float? = null,
    val ec: Float? = null
)
