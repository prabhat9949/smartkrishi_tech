package com.smartkrishi.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartkrishi.data.model.FarmerProfile  // ✅ ADD THIS IMPORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "FarmerViewModel"

@HiltViewModel
class FarmerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _farmerProfile = MutableStateFlow<FarmerProfile?>(null)
    val farmerProfile: StateFlow<FarmerProfile?> = _farmerProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val uid: String? get() = auth.currentUser?.uid
    private val userEmail: String? get() = auth.currentUser?.email

    init {
        loadFarmerProfile()
    }

    // -------------------------------------------
    // 📥 LOAD FARMER PROFILE
    // -------------------------------------------
    fun loadFarmerProfile() {
        val userId = uid
        if (userId == null) {
            Log.w(TAG, "Cannot load profile - User not logged in")
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val snapshot = firestore.collection("farmers")
                    .document(userId)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val profile = snapshot.toObject(FarmerProfile::class.java)
                    _farmerProfile.value = profile
                    Log.d(TAG, "✅ Farmer profile loaded: ${profile?.name}")
                } else {
                    Log.d(TAG, "⚠️ No farmer profile found for user: $userId")
                    _farmerProfile.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading farmer profile: ${e.message}", e)
                _errorMessage.value = "Failed to load profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -------------------------------------------
    // 💾 SAVE FARMER PROFILE
    // -------------------------------------------
    suspend fun saveFarmerProfile(profile: FarmerProfile): Boolean {
        val userId = uid
        val email = userEmail

        if (userId == null) {
            Log.e(TAG, "Cannot save profile - User not logged in")
            _errorMessage.value = "User not authenticated"
            return false
        }

        if (email.isNullOrBlank()) {
            Log.e(TAG, "Cannot save profile - User email not found")
            _errorMessage.value = "User email not found"
            return false
        }

        _isLoading.value = true
        _errorMessage.value = null

        return try {
            // Add uid and email to profile
            val profileWithMetadata = hashMapOf<String, Any>(
                "uid" to userId,
                "email" to email,
                "name" to profile.name,
                "phone" to profile.phone,
                "address" to profile.address,
                "state" to profile.state,
                "district" to profile.district,
                "pincode" to profile.pincode,
                "farmingExperience" to profile.farmingExperience,
                "totalLand" to profile.totalLand,
                "language" to profile.language,
                "profileImageUrl" to profile.profileImageUrl,
                "isVerified" to profile.isVerified,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            Log.d(TAG, "Saving farmer profile for user: $email")

            firestore.collection("farmers")
                .document(userId)
                .set(profileWithMetadata)
                .await()

            // Reload to get saved data with proper types
            loadFarmerProfile()
            Log.d(TAG, "✅ Farmer profile saved successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving farmer profile: ${e.message}", e)
            _errorMessage.value = "Failed to save profile: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    // -------------------------------------------
    // ✏️ UPDATE FARMER PROFILE
    // -------------------------------------------
    suspend fun updateFarmerProfile(
        name: String? = null,
        phone: String? = null,
        address: String? = null,
        state: String? = null,
        district: String? = null,
        pincode: String? = null,
        farmingExperience: Int? = null,
        totalLand: Double? = null,
        language: String? = null
    ): Boolean {
        val userId = uid ?: return false

        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val updates = hashMapOf<String, Any>(
                "updatedAt" to Timestamp.now()
            )

            name?.let { updates["name"] = it }
            phone?.let { updates["phone"] = it }
            address?.let { updates["address"] = it }
            state?.let { updates["state"] = it }
            district?.let { updates["district"] = it }
            pincode?.let { updates["pincode"] = it }
            farmingExperience?.let { updates["farmingExperience"] = it }
            totalLand?.let { updates["totalLand"] = it }
            language?.let { updates["language"] = it }

            Log.d(TAG, "Updating farmer profile for user: $userId")

            firestore.collection("farmers")
                .document(userId)
                .update(updates)
                .await()

            loadFarmerProfile() // Reload to get updated data
            Log.d(TAG, "✅ Farmer profile updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating farmer profile: ${e.message}", e)
            _errorMessage.value = "Failed to update profile: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    // -------------------------------------------
    // 🗑️ DELETE FARMER PROFILE
    // -------------------------------------------
    suspend fun deleteFarmerProfile(): Boolean {
        val userId = uid ?: return false

        _isLoading.value = true
        _errorMessage.value = null

        return try {
            Log.d(TAG, "Deleting farmer profile for user: $userId")

            firestore.collection("farmers")
                .document(userId)
                .delete()
                .await()

            _farmerProfile.value = null
            Log.d(TAG, "✅ Farmer profile deleted successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting farmer profile: ${e.message}", e)
            _errorMessage.value = "Failed to delete profile: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    // -------------------------------------------
    // ✅ CHECK IF PROFILE EXISTS
    // -------------------------------------------
    suspend fun hasProfile(): Boolean {
        val userId = uid ?: return false

        return try {
            val snapshot = firestore.collection("farmers")
                .document(userId)
                .get()
                .await()

            snapshot.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking profile existence: ${e.message}", e)
            false
        }
    }

    // -------------------------------------------
    // 🔍 GET PROFILE BY USER ID
    // -------------------------------------------
    suspend fun getFarmerProfileByUserId(userId: String): FarmerProfile? {
        return try {
            val snapshot = firestore.collection("farmers")
                .document(userId)
                .get()
                .await()

            snapshot.toObject(FarmerProfile::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile by userId: ${e.message}", e)
            null
        }
    }

    // -------------------------------------------
    // 🔍 GET PROFILE BY EMAIL
    // -------------------------------------------
    suspend fun getFarmerProfileByEmail(email: String): FarmerProfile? {
        return try {
            val snapshot = firestore.collection("farmers")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                snapshot.documents.first().toObject(FarmerProfile::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile by email: ${e.message}", e)
            null
        }
    }

    // -------------------------------------------
    // ❌ CLEAR ERROR MESSAGE
    // -------------------------------------------
    fun clearError() {
        _errorMessage.value = null
    }

    // -------------------------------------------
    // 🔄 REFRESH PROFILE
    // -------------------------------------------
    fun refreshProfile() {
        loadFarmerProfile()
    }

    // -------------------------------------------
    // 🔐 GET CURRENT USER EMAIL
    // -------------------------------------------
    fun getCurrentUserEmail(): String? = userEmail

    // -------------------------------------------
    // 🔐 GET CURRENT USER ID
    // -------------------------------------------
    fun getCurrentUserId(): String? = uid

    // -------------------------------------------
    // 🚪 LOGOUT (CLEAR DATA)
    // -------------------------------------------
    fun logout() {
        Log.d(TAG, "Logging out - clearing farmer profile")
        _farmerProfile.value = null
        _errorMessage.value = null
    }
}

// ❌ REMOVE THE DATA CLASS FROM HERE - IT'S NOW IN data/model/FarmerProfile.kt
