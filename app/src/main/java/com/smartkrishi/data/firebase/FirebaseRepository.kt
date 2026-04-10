package com.smartkrishi.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.smartkrishi.presentation.ai.FarmSnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepository @Inject constructor() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // 🌱 Save crop training data
    suspend fun saveTrainingData(
        snapshot: FarmSnapshot,
        predictedCrop: String,
        confidence: Float
    ) {
        try {
            val data = hashMapOf(
                "N" to snapshot.avgN,
                "P" to snapshot.avgP,
                "K" to snapshot.avgK,
                "temperature" to snapshot.avgTemp,
                "humidity" to snapshot.avgHumidity,
                "ph" to snapshot.avgPh,
                "rainfall" to snapshot.rainfall,
                "prediction" to predictedCrop,
                "confidence" to confidence,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection(CROP_COLLECTION)
                .add(data)
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
            // ❗ Do NOT crash app
        }
    }

    // 🌾 Save fertilizer training data
    suspend fun saveFertilizerData(data: Map<String, Any>) {
        try {
            db.collection(FERTILIZER_COLLECTION)
                .add(data)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val CROP_COLLECTION = "crop_training_data"
        private const val FERTILIZER_COLLECTION = "fertilizer_training_data"
    }
}