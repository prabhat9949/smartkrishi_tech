package com.smartkrishi.presentation.logs

import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.smartkrishi.domain.model.LogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LogsViewModel : ViewModel() {

    private val dbRef = FirebaseDatabase.getInstance()
        .reference
        .child("dashboard")
        .child("farmer_SK001")
        .child("farm_alpha01")
        .child("history")

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadLogs()
    }

    private fun loadLogs() {

        dbRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val demoList = demoLogs()
                val firebaseList = mutableListOf<LogEntry>()

                snapshot.children.forEach { snap ->

                    val id = snap.key ?: return@forEach
                    val pump = snap.child("pump").getValue(Int::class.java) ?: 0
                    val tds = snap.child("tds").getValue(Int::class.java) ?: 0
                    val tank = snap.child("tank").getValue(Int::class.java) ?: 0

                    val timestampStr =
                        snap.child("timestamp").getValue(String::class.java)

                    val formatter =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                    val dateTime = try {
                        timestampStr?.let {
                            LocalDateTime.parse(it, formatter)
                        } ?: LocalDateTime.now()
                    } catch (e: Exception) {
                        LocalDateTime.now()
                    }

                    val formattedTime =
                        dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

                    // Pump Entry
                    firebaseList.add(
                        LogEntry(
                            id = "fb_pump_$id",
                            title = if (pump == 1)
                                "Pump Turned ON"
                            else
                                "Pump Turned OFF",
                            description = "Real-time update from IoT system.",
                            type = "irrigation",
                            time = formattedTime,
                            pumpStatus = pump,
                            timestamp = dateTime
                        )
                    )

                    // TDS Alert
                    if (tds > 500) {
                        firebaseList.add(
                            LogEntry(
                                id = "fb_tds_$id",
                                title = "High TDS Alert",
                                description = "TDS reached $tds ppm.",
                                type = "tds",
                                time = formattedTime,
                                isAlert = true,
                                timestamp = dateTime
                            )
                        )
                    }

                    // Tank Alert
                    if (tank < 25) {
                        firebaseList.add(
                            LogEntry(
                                id = "fb_tank_$id",
                                title = "Low Tank Level",
                                description = "Tank level at $tank%. Refill needed.",
                                type = "alert",
                                time = formattedTime,
                                isAlert = true,
                                timestamp = dateTime
                            )
                        )
                    }
                }

                // 🔥 MERGE DEMO + FIREBASE
                val combined = (demoList + firebaseList)
                    .distinctBy { it.id }
                    .sortedByDescending { it.timestamp }

                _logs.value = combined
                _isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _logs.value = demoLogs()
                _isLoading.value = false
            }
        })
    }

    // DEMO DATA
    private fun demoLogs(): List<LogEntry> {

        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

        return listOf(
            LogEntry(
                id = "demo1",
                title = "Pump Turned ON",
                description = "Manual irrigation started.",
                type = "irrigation",
                time = now.minusHours(2).format(formatter),
                pumpStatus = 1,
                timestamp = now.minusHours(2)
            ),
            LogEntry(
                id = "demo2",
                title = "High TDS Alert",
                description = "TDS reached 610 ppm.",
                type = "tds",
                time = now.minusHours(1).format(formatter),
                isAlert = true,
                timestamp = now.minusHours(1)
            ),
            LogEntry(
                id = "demo3",
                title = "Low Tank Level",
                description = "Tank dropped below 20%.",
                type = "alert",
                time = now.minusDays(1).format(formatter),
                isAlert = true,
                timestamp = now.minusDays(1)
            )
        )
    }
}