package com.smartkrishi.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.smartkrishi.R
import com.smartkrishi.presentation.model.Farm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "FarmViewModel"

@HiltViewModel
class FarmViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val realtimeDb = FirebaseDatabase.getInstance()

    private val _farms = MutableStateFlow<List<Farm>>(emptyList())
    val farms: StateFlow<List<Farm>> = _farms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedFarm = MutableStateFlow<Farm?>(null)
    val selectedFarm: StateFlow<Farm?> = _selectedFarm.asStateFlow()

    // ✅ KEEP REFERENCE TO LISTENER TO PREVENT DUPLICATE LISTENERS
    private var farmsListener: ListenerRegistration? = null

    init {
        loadFarms()
    }

    // -------------------------------------------
    // 🔁 LOAD FARMS OF CURRENT USER (BY EMAIL)
    // -------------------------------------------
    fun loadFarms() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Log.w(TAG, "❌ No authenticated user found")
            _farms.value = emptyList()
            _errorMessage.value = "Please login to view your farms"
            return
        }

        val userEmail = currentUser.email

        if (userEmail.isNullOrBlank()) {
            Log.w(TAG, "❌ User email is null or blank")
            _farms.value = emptyList()
            return
        }

        Log.d(TAG, "🔍 Loading farms for user: $userEmail")

        // ✅ REMOVE OLD LISTENER BEFORE CREATING NEW ONE
        farmsListener?.remove()

        _isLoading.value = true
        _errorMessage.value = null

        // 🔥 QUERY BY USER EMAIL
        farmsListener = firestore.collection("farms")
            .whereEqualTo("userEmail", userEmail)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    Log.e(TAG, "❌ Error loading farms: ${error.message}", error)

                    if (error.message?.contains("index", ignoreCase = true) == true) {
                        Log.e(TAG, "⚠️ FIRESTORE INDEX REQUIRED!")
                        _errorMessage.value = "Database index required. Check logs."
                    } else {
                        _errorMessage.value = "Failed to load farms: ${error.message}"
                    }

                    // ✅ DON'T CLEAR FARMS ON ERROR - KEEP EXISTING DATA
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "⚠️ Snapshot is null")
                    return@addSnapshotListener
                }

                Log.d(TAG, "📊 Snapshot received - Document count: ${snapshot.size()}")
                Log.d(TAG, "📊 Snapshot metadata - fromCache: ${snapshot.metadata.isFromCache}")

                if (snapshot.isEmpty) {
                    Log.d(TAG, "⚠️ No farms found for user: $userEmail")
                    _farms.value = emptyList()
                    return@addSnapshotListener
                }

                val farmsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val farm = doc.toObject(Farm::class.java)?.copy(id = doc.id)
                        Log.d(TAG, "✅ Parsed farm: ${farm?.name} (ID: ${doc.id}, userEmail: ${doc.getString("userEmail")})")
                        farm
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error parsing farm document: ${doc.id}", e)
                        null
                    }
                }.sortedBy { it.name }

                _farms.value = farmsList
                Log.d(TAG, "✅ Successfully loaded ${farmsList.size} farms")
                farmsList.forEach { farm ->
                    Log.d(TAG, "   📍 ${farm.name} (${farm.location})")
                }

                // Auto-select first farm if none selected
                if (_selectedFarm.value == null && farmsList.isNotEmpty()) {
                    _selectedFarm.value = farmsList.first()
                    Log.d(TAG, "🎯 Auto-selected first farm: ${farmsList.first().name}")
                }
            }
    }

    // -------------------------------------------
    // ➕ ADD NEW FARM (ENHANCED WITH ALL FIELDS)
    // -------------------------------------------
    fun addFarm(
        farm: Farm,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "❌ Cannot add farm - User not authenticated")
            onFailure("User not authenticated")
            return
        }

        val userEmail = currentUser.email
        val userId = currentUser.uid

        if (userEmail.isNullOrBlank()) {
            Log.e(TAG, "❌ Cannot add farm - User email not found")
            onFailure("User email not found")
            return
        }

        viewModelScope.launch {
            try {
                // Create farm with user info
                val farmToAdd = farm.copy(
                    userEmail = userEmail,
                    ownerId = userId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                Log.d(TAG, "➕ Adding farm: ${farm.name} for user: $userEmail")

                // Add to Firestore
                val docRef = firestore.collection("farms")
                    .add(farmToAdd)
                    .await()

                val farmId = docRef.id
                val farmWithId = farmToAdd.copy(id = farmId)

                // Update Firestore with ID
                firestore.collection("farms")
                    .document(farmId)
                    .set(farmWithId)
                    .await()

                // Add to Realtime Database
                realtimeDb.getReference("farms")
                    .child(userId)
                    .child(farmId)
                    .setValue(farmWithId)
                    .await()

                Log.d(TAG, "✅ Farm added successfully with ID: $farmId")
                onSuccess()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to add farm: ${e.message}", e)
                onFailure(e.message ?: "Failed to add farm")
            }
        }
    }

    // -------------------------------------------
    // ✏️ UPDATE FARM (ENHANCED WITH ALL FIELDS)
    // -------------------------------------------
    fun updateFarm(
        farm: Farm,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "❌ Cannot update farm - User not authenticated")
            onFailure("User not authenticated")
            return
        }

        viewModelScope.launch {
            try {
                val updatedFarm = farm.copy(
                    updatedAt = System.currentTimeMillis()
                )

                Log.d(TAG, "✏️ Updating farm: ${farm.id}")

                // Update Firestore
                firestore.collection("farms")
                    .document(farm.id)
                    .set(updatedFarm)
                    .await()

                // Update Realtime Database
                realtimeDb.getReference("farms")
                    .child(currentUser.uid)
                    .child(farm.id)
                    .setValue(updatedFarm)
                    .await()

                Log.d(TAG, "✅ Farm updated successfully: ${farm.id}")

                // Update selected farm if it's the one being edited
                if (_selectedFarm.value?.id == farm.id) {
                    _selectedFarm.value = updatedFarm
                }

                onSuccess()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to update farm: ${e.message}", e)
                onFailure(e.message ?: "Failed to update farm")
            }
        }
    }

    // -------------------------------------------
    // 🗑️ DELETE FARM (WITH REALTIME DB)
    // -------------------------------------------
    fun deleteFarm(
        farmId: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "❌ Cannot delete farm - User not authenticated")
            onFailure("User not authenticated")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "🗑️ Deleting farm: $farmId")

                // Delete from Firestore
                firestore.collection("farms")
                    .document(farmId)
                    .delete()
                    .await()

                // Delete from Realtime Database
                realtimeDb.getReference("farms")
                    .child(currentUser.uid)
                    .child(farmId)
                    .removeValue()
                    .await()

                Log.d(TAG, "✅ Farm deleted successfully: $farmId")

                // Clear selected farm if it was deleted
                if (_selectedFarm.value?.id == farmId) {
                    _selectedFarm.value = null
                }

                onSuccess()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to delete farm: ${e.message}", e)
                onFailure(e.message ?: "Failed to delete farm")
            }
        }
    }

    // -------------------------------------------
    // 🎯 SELECT FARM
    // -------------------------------------------
    fun selectFarm(farm: Farm) {
        Log.d(TAG, "🎯 Farm selected: ${farm.name} (${farm.id})")
        _selectedFarm.value = farm
    }

    // -------------------------------------------
    // 🔄 CLEAR SELECTED FARM
    // -------------------------------------------
    fun clearSelectedFarm() {
        _selectedFarm.value = null
    }

    // -------------------------------------------
    // 🔁 MANUAL REFRESH
    // -------------------------------------------
    fun refreshFarms() {
        Log.d(TAG, "🔄 Manual refresh triggered")
        loadFarms()
    }

    // -------------------------------------------
    // ❌ CLEAR ERROR MESSAGE
    // -------------------------------------------
    fun clearError() {
        _errorMessage.value = null
    }

    // -------------------------------------------
    // 📊 GET FARM COUNT
    // -------------------------------------------
    fun getFarmCount(): Int = _farms.value.size

    // -------------------------------------------
    // ✅ HAS FARMS
    // -------------------------------------------
    fun hasFarms(): Boolean = _farms.value.isNotEmpty()

    // -------------------------------------------
    // 🔍 GET FARM BY ID
    // -------------------------------------------
    fun getFarmById(farmId: String): Farm? {
        return _farms.value.find { it.id == farmId }
    }

    // -------------------------------------------
    // 🔐 GET CURRENT USER EMAIL
    // -------------------------------------------
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    // -------------------------------------------
    // 👤 GET CURRENT USER ID
    // -------------------------------------------
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // -------------------------------------------
    // 📈 GET FARMS STATISTICS
    // -------------------------------------------
    fun getFarmsStatistics(): FarmStatistics {
        val farmsList = _farms.value
        return FarmStatistics(
            totalFarms = farmsList.size,
            totalAcres = farmsList.sumOf { it.acres },
            cropTypes = farmsList.map { it.cropType }.distinct(),
            soilTypes = farmsList.map { it.soilType }.distinct()
        )
    }

    // -------------------------------------------
    // 🚪 LOGOUT (CLEAR DATA)
    // -------------------------------------------
    fun logout() {
        Log.d(TAG, "🚪 Logging out - clearing farm data")

        // ✅ REMOVE LISTENER
        farmsListener?.remove()
        farmsListener = null

        _farms.value = emptyList()
        _selectedFarm.value = null
        _errorMessage.value = null
    }

    // ✅ CLEANUP WHEN VIEWMODEL IS DESTROYED
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 ViewModel cleared - removing listener")
        farmsListener?.remove()
    }
}

// -------------------------------------------
// 📊 FARM STATISTICS DATA CLASS
// -------------------------------------------
data class FarmStatistics(
    val totalFarms: Int,
    val totalAcres: Int,
    val cropTypes: List<String>,
    val soilTypes: List<String>
)
