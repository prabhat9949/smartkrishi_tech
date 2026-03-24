package com.smartkrishi.presentation.dashboard

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.smartkrishi.presentation.model.SensorNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _nodes = MutableStateFlow<List<SensorNode>>(emptyList())
    val nodes: StateFlow<List<SensorNode>> = _nodes

    private var listener: ValueEventListener? = null
    private var currentRef: DatabaseReference? = null

    // 🔥 START LISTENING FROM "sensorData/userId/farmId/"
    fun startListening(farmId: String) {
        val uid = auth.currentUser?.uid ?: return

        // Remove previous listener
        listener?.let { currentRef?.removeEventListener(it) }

        val ref = database.reference
            .child("sensorData") // 👈 Updated path
            .child(uid)
            .child(farmId)

        currentRef = ref

        listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { node ->
                    val id = node.key ?: return@mapNotNull null

                    SensorNode(
                        id = id,
                        moisture = node.child("moisture").getValue(Float::class.java),
                        temp = node.child("temp").getValue(Float::class.java),
                        nitrogen = node.child("nitrogen").getValue(Float::class.java),
                        phosphorus = node.child("phosphorus").getValue(Float::class.java),
                        potassium = node.child("potassium").getValue(Float::class.java),
                        ph = node.child("ph").getValue(Float::class.java),
                        ec = node.child("ec").getValue(Float::class.java)
                    )
                }
                _nodes.value = list
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 🔄 REFRESH ALL
    fun reloadAll() {
        currentRef?.get()
    }

    // 🔄 REFRESH SPECIFIC NODE
    fun reloadNode(nodeId: String) {
        currentRef?.child(nodeId)?.get()
    }

    override fun onCleared() {
        super.onCleared()
        listener?.let { currentRef?.removeEventListener(it) }
    }
}
