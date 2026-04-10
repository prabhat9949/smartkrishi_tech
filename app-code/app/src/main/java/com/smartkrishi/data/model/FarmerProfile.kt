package com.smartkrishi.data.model

import com.google.firebase.Timestamp

data class FarmerProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val state: String = "",
    val district: String = "",
    val pincode: String = "",
    val farmingExperience: Int = 0,
    val totalLand: Double = 0.0,
    val language: String = "English",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val profileImageUrl: String = "",
    val isVerified: Boolean = false
)
