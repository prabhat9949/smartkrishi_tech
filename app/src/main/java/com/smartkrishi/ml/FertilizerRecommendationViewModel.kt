package com.smartkrishi.ml

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FertilizerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val predictedFertilizer: String = "",
    val resultText: String = "",
    val currentStep: Int = 1,

    val crop: String = "",
    val location: String = "",
    val area: String = "",
    val month: String = "",
    val cropGrowthStage: String = "",
    val season: String = "",
    val irrigationType: String = "",
    val previousCrop: String = "",
    val previousFertilizerUsed: String = "",
    val totalFarm: String = "",

    val soil: String = "Loamy",
    val pH: String = "6.5",
    val moisture: String = "45",
    val temp: String = "30",
    val humidity: String = "60",
    val nitrogen: String = "40",
    val phosphorus: String = "30",
    val potassium: String = "20"
)

data class FertilizerRecommendationRequest(
    val crop: String,
    val location: String,
    val area: String,
    val month: String,
    val cropGrowthStage: String,
    val season: String,
    val irrigationType: String,
    val previousCrop: String,
    val previousFertilizerUsed: String,
    val totalFarm: String,
    val soil: String,
    val pH: Float,
    val moisture: Float,
    val temp: Float,
    val humidity: Float,
    val n: Float,
    val p: Float,
    val k: Float
)

@HiltViewModel
class FertilizerRecommendationViewModel @Inject constructor(
    private val repository: HybridFertilizerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FertilizerUiState())
    val uiState: StateFlow<FertilizerUiState> = _uiState.asStateFlow()

    // ====== STEP CONTROL ======

    fun setStep(step: Int) {
        _uiState.value = _uiState.value.copy(currentStep = step.coerceIn(1, 2))
    }

    fun goNextStep() {
        val next = (_uiState.value.currentStep + 1).coerceAtMost(2)
        _uiState.value = _uiState.value.copy(currentStep = next)
    }

    fun goPreviousStep() {
        val prev = (_uiState.value.currentStep - 1).coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(currentStep = prev)
    }

    // ====== FARM CONTEXT ======

    fun updateFarmContext(
        crop: String? = null,
        location: String? = null,
        area: String? = null,
        month: String? = null,
        cropGrowthStage: String? = null,
        season: String? = null,
        irrigationType: String? = null,
        previousCrop: String? = null,
        previousFertilizerUsed: String? = null,
        totalFarm: String? = null
    ) {
        val state = _uiState.value
        _uiState.value = state.copy(
            crop = crop ?: state.crop,
            location = location ?: state.location,
            area = area ?: state.area,
            month = month ?: state.month,
            cropGrowthStage = cropGrowthStage ?: state.cropGrowthStage,
            season = season ?: state.season,
            irrigationType = irrigationType ?: state.irrigationType,
            previousCrop = previousCrop ?: state.previousCrop,
            previousFertilizerUsed = previousFertilizerUsed ?: state.previousFertilizerUsed,
            totalFarm = totalFarm ?: state.totalFarm
        )
    }

    // ====== ML INPUTS ======

    fun updateMlInputs(
        soil: String? = null,
        pH: String? = null,
        moisture: String? = null,
        temp: String? = null,
        humidity: String? = null,
        nitrogen: String? = null,
        phosphorus: String? = null,
        potassium: String? = null
    ) {
        val state = _uiState.value
        _uiState.value = state.copy(
            soil = soil ?: state.soil,
            pH = pH ?: state.pH,
            moisture = moisture ?: state.moisture,
            temp = temp ?: state.temp,
            humidity = humidity ?: state.humidity,
            nitrogen = nitrogen ?: state.nitrogen,
            phosphorus = phosphorus ?: state.phosphorus,
            potassium = potassium ?: state.potassium
        )
    }

    // Prefill from dashboard node
    fun prefillFromDashboard(
        crop: String?,
        location: String?,
        area: String? = null,
        month: String? = null,
        cropGrowthStage: String? = null,
        season: String? = null,
        irrigationType: String? = null,
        previousCrop: String? = null,
        previousFertilizerUsed: String? = null,
        totalFarm: String? = null,
        soil: String? = null,
        pH: String? = null,
        moisture: String? = null,
        temp: String? = null,
        humidity: String? = null,
        nitrogen: String? = null,
        phosphorus: String? = null,
        potassium: String? = null
    ) {
        val state = _uiState.value
        _uiState.value = state.copy(
            crop = crop ?: state.crop,
            location = location ?: state.location,
            area = area ?: state.area,
            month = month ?: state.month,
            cropGrowthStage = cropGrowthStage ?: state.cropGrowthStage,
            season = season ?: state.season,
            irrigationType = irrigationType ?: state.irrigationType,
            previousCrop = previousCrop ?: state.previousCrop,
            previousFertilizerUsed = previousFertilizerUsed ?: state.previousFertilizerUsed,
            totalFarm = totalFarm ?: state.totalFarm,
            soil = soil ?: state.soil,
            pH = pH ?: state.pH,
            moisture = moisture ?: state.moisture,
            temp = temp ?: state.temp,
            humidity = humidity ?: state.humidity,
            nitrogen = nitrogen ?: state.nitrogen,
            phosphorus = phosphorus ?: state.phosphorus,
            potassium = potassium ?: state.potassium
        )
    }

    // ====== RECALCULATE DIALOG ======

    private val _showRecalculateDialog = MutableStateFlow(false)
    val showRecalculateDialog: StateFlow<Boolean> = _showRecalculateDialog.asStateFlow()

    fun showRecalculateDialog() {
        _showRecalculateDialog.value = true
    }

    fun hideRecalculateDialog() {
        _showRecalculateDialog.value = false
    }

    // ====== CLEAR ======

    fun clearRecommendation() {
        val state = _uiState.value
        _uiState.value = state.copy(
            error = null,
            predictedFertilizer = "",
            resultText = ""
        )
    }

    fun clearAll() {
        _uiState.value = FertilizerUiState()
    }

    // ====== MAIN PREDICTION ======

    fun generateRecommendation() {
        val state = _uiState.value

        val pHValue = state.pH.toFloatOrNull()
        val moistureValue = state.moisture.toFloatOrNull()
        val tempValue = state.temp.toFloatOrNull()
        val humidityValue = state.humidity.toFloatOrNull()
        val nValue = state.nitrogen.toFloatOrNull()
        val pValue = state.phosphorus.toFloatOrNull()
        val kValue = state.potassium.toFloatOrNull()

        if (
            state.crop.isBlank() ||
            state.location.isBlank() ||
            state.area.isBlank() ||
            state.month.isBlank() ||
            state.cropGrowthStage.isBlank() ||
            state.season.isBlank() ||
            state.irrigationType.isBlank() ||
            state.previousCrop.isBlank() ||
            state.previousFertilizerUsed.isBlank() ||
            state.totalFarm.isBlank() ||
            pHValue == null ||
            moistureValue == null ||
            tempValue == null ||
            humidityValue == null ||
            nValue == null ||
            pValue == null ||
            kValue == null
        ) {
            _uiState.value = state.copy(
                error = "Please fill all required fields correctly before calculating."
            )
            return
        }

        val request = FertilizerRecommendationRequest(
            crop = state.crop,
            location = state.location,
            area = state.area,
            month = state.month,
            cropGrowthStage = state.cropGrowthStage,
            season = state.season,
            irrigationType = state.irrigationType,
            previousCrop = state.previousCrop,
            previousFertilizerUsed = state.previousFertilizerUsed,
            totalFarm = state.totalFarm,
            soil = state.soil,
            pH = pHValue,
            moisture = moistureValue,
            temp = tempValue,
            humidity = humidityValue,
            n = nValue,
            p = pValue,
            k = kValue
        )

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                error = null,
                predictedFertilizer = "",
                resultText = ""
            )

            try {
                val result = repository.getFullPrediction(
                    soil = request.soil,
                    pH = request.pH,
                    moisture = request.moisture,
                    temp = request.temp,
                    humidity = request.humidity,
                    n = request.n,
                    p = request.p,
                    k = request.k,
                    crop = request.crop,
                    location = request.location
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = if (result.predictedFertilizer == "Unknown" && result.finalText.isBlank()) {
                        "Could not generate a reliable recommendation."
                    } else null,
                    predictedFertilizer = result.predictedFertilizer,
                    resultText = result.finalText
                )

                // TODO: when your repository is ready, call a save method here to push
                // request + result to Firebase.
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Something went wrong while generating recommendation."
                )
            }
        }
    }

    override fun onCleared() {
        repository.close()
        super.onCleared()
    }
}