package com.smartkrishi.presentation.disease

object DiseaseMemory {

    data class DiseaseData(
        val crop_name: String = "",
        val disease_name: String = "",
        val confidence: Float = 0f,
        val spread_assessment: String = "",
        val treatment_plan: String = "",
        val prevention_guidelines: String = "",
        val scannedImage: String = "",   // 👈 MUST BE STRING
        val ai_generated: Boolean = false
    )

    var data: DiseaseData = DiseaseData()
}
