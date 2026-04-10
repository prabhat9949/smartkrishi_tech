package com.smartkrishi.presentation.disease

object DiseaseExpertAI {

    data class Advice(
        val spread: String,
        val treatment: String,
        val prevention: String
    )

    fun getAdvice(crop: String, disease: String): Advice {

        val c = crop.lowercase()
        val d = disease.lowercase()

        return when {
            c.contains("tomato") && d.contains("blight") -> Advice(
                spread = "High 🔥 (जल्दी फैलता है, पत्तियों और फल दोनों को नुकसान)",
                treatment = "Copper Oxychloride @ 2–3g/L या Mancozeb 2.5g/L का छिड़काव। Organic: Neem Oil 5ml/L.",
                prevention = "धूप व वेंटिलेशन रखें, संक्रमित पत्तियां हटा दें, हर 10–14 दिन में स्प्रे दोहराएँ।"
            )

            c.contains("apple") && d.contains("scab") -> Advice(
                spread = "Medium 🌧 (नमी में तेजी से फैलता है)",
                treatment = "Carbendazim 1g/L या Captan 2g/L स्प्रे। Organic: Sulphur Dust.",
                prevention = "पेड़ों की छंटाई करें, गिरे हुए पत्ते हटाएँ, पानी पत्तियों पर न रुकने दें।"
            )

            c.contains("maize") && d.contains("rust") -> Advice(
                spread = "Medium 🔶",
                treatment = "Propiconazole 1ml/L या Mancozeb 2g/L। Organic: Neem Oil.",
                prevention = "नमी कम रखें, बीज उपचार करें, फसल चक्र अपनाएँ।"
            )

            c.contains("corn") && d.contains("leaf spot") -> Advice(
                spread = "High 🌡",
                treatment = "Chlorothalonil 2g/L या Copper Fungicide।",
                prevention = "साफ बीज, फसल चक्र, संक्रमित पत्तों को नष्ट करें।"
            )

            else -> Advice(
                spread = "Data Limited — But likely moderate. 📉",
                treatment = "Use Neem Oil (5ml/L) + Copper fungicide (2g/L).",
                prevention = "Avoid overwatering, remove infected leaves, ensure sunlight."
            )
        }
    }
}
