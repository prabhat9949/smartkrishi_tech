package com.smartkrishi.presentation.model

import com.smartkrishi.R

data class Farm(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val acres: Int = 0,
    val cropType: String = "",
    val soilType: String = "",
    val roverId: String = "",
    val backgroundRes: Int = R.drawable.farm_placeholder,
    val ownerId: String = "",
    val userEmail: String = "",
    val status: String = "HEALTHY",// ✅ User email for filtering

    // ✅ NEW FIELDS
    val imageUrl: String? = null,  // Firebase Storage image URL
    val waterSource: String = "",  // Water source type
    val irrigationType: String = "",  // Irrigation method
    val description: String = "",  // Farm description
    val createdAt: Long = System.currentTimeMillis(),  // Timestamp
    val updatedAt: Long = System.currentTimeMillis(),  // Last update
    val isActive: Boolean = true,  // Farm active status
    val harvestDate: String? = null,  // Expected harvest date
    val plantingDate: String? = null,  // Planting date
    val lastIrrigated: String? = null,  // Last irrigation date
    val soilHealth: String = "Good",  // Soil health status
    val cropHealth: String = "Healthy",  // Crop health status
    val fertilizersUsed: List<String> = emptyList(),  // List of fertilizers
    val pesticidesUsed: List<String> = emptyList(),  // List of pesticides
    val weatherAlerts: Boolean = true,  // Enable weather alerts
    val notes: String = ""  // Additional notes
)
