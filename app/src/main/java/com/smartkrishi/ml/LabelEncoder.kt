package com.smartkrishi.ml

object LabelEncoder {

    val crops = listOf(
        "rice", "maize", "chickpea", "kidneybeans", "pigeonpeas",
        "mothbeans", "mungbean", "blackgram", "lentil", "pomegranate",
        "banana", "mango", "grapes", "watermelon", "muskmelon",
        "apple", "orange", "papaya", "coconut", "cotton", "jute", "coffee"
    )

    fun decode(index: Int): String {
        return crops.getOrElse(index) { "Unknown Crop" }
    }

    fun size(): Int = crops.size
}