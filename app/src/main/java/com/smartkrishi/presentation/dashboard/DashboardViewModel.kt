package com.smartkrishi.presentation.dashboard

import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.smartkrishi.presentation.model.SensorNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {

    private val dbRef = FirebaseDatabase.getInstance().reference

    // 🔥 FIXED FOR NOW (matching your Firebase screenshot)
    private val farmerId = "farmer_SK001"
    private val farmId = "farm_alpha01"

    private var irrigationRef: DatabaseReference? = null
    private var nodesRef: DatabaseReference? = null

    private var irrigationListener: ValueEventListener? = null
    private var nodesListener: ValueEventListener? = null

    private val _nodes = MutableStateFlow<List<SensorNode>>(emptyList())
    val nodes: StateFlow<List<SensorNode>> = _nodes

    private val _tds = MutableStateFlow(0)
    val tds: StateFlow<Int> = _tds

    private val _tank = MutableStateFlow(0)
    val tank: StateFlow<Int> = _tank

    private val _rain = MutableStateFlow(0)
    val rain: StateFlow<Int> = _rain

    private val _pump = MutableStateFlow(0)
    val pump: StateFlow<Int> = _pump

    fun startListening() {

        removeListeners()

        val liveRef = dbRef
            .child("dashboard")
            .child(farmerId)
            .child(farmId)
            .child("live")

        irrigationRef = liveRef.child("irrigation")
        nodesRef = liveRef.child("nodes")

        // ---------------- IRRIGATION ----------------
        irrigationListener = irrigationRef
            ?.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    _pump.value = snapshot.child("pump")
                        .getValue(Int::class.java) ?: 0

                    _rain.value = snapshot.child("rain")
                        .getValue(Int::class.java) ?: 0

                    _tds.value = snapshot.child("tds")
                        .getValue(Int::class.java) ?: 0

                    _tank.value = snapshot.child("tank")
                        .getValue(Double::class.java)?.toInt() ?: 0
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // ---------------- NODES ----------------
        nodesListener = nodesRef
            ?.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val list = mutableListOf<SensorNode>()

                    for (zoneSnap in snapshot.children) {

                        val node = zoneSnap.getValue(SensorNode::class.java)

                        node?.let {
                            it.id = zoneSnap.key ?: ""
                            list.add(it)
                        }
                    }

                    _nodes.value = list
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun removeListeners() {
        irrigationListener?.let { irrigationRef?.removeEventListener(it) }
        nodesListener?.let { nodesRef?.removeEventListener(it) }

        irrigationListener = null
        nodesListener = null
    }
    fun reloadAll() {
        startListening()
    }
    override fun onCleared() {
        super.onCleared()
        removeListeners()
    }
}