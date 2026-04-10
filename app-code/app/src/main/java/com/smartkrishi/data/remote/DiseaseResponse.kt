package com.smartkrishi.data.remote

data class DiseaseResponse(
    val disease: String,
    val confidence: Float,
    val advice: String? = null,

    val result: String,        // disease name

)
