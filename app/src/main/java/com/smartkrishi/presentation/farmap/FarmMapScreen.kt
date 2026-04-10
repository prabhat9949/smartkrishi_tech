package com.smartkrishi.presentation.farmmap

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.smartkrishi.presentation.model.Farm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmMapScreen(
    farm: Farm,
    onBack: () -> Unit
) {

    val farmLocation = LatLng(farm.lat, farm.lon)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(farmLocation, 15f)
    }

    val primaryGreen = Color(0xFF2E7D32)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            farm.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Farm Location",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryGreen
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            /* ---------- GOOGLE MAP ---------- */

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {

                Marker(
                    state = MarkerState(position = farmLocation),
                    title = farm.name,
                    snippet = "${farm.acres} acres • ${farm.cropType}"
                )
            }

            /* ---------- FARM INFO CARD ---------- */

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(
                            Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = primaryGreen,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                farm.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = primaryGreen
                            )

                            Text(
                                "${farm.acres} acres • ${farm.cropType}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row {
                        InfoChip(
                            icon = Icons.Default.LocationOn,
                            label = "Lat: ${String.format("%.6f", farm.lat)}"
                        )
                        Spacer(Modifier.width(8.dp))
                        InfoChip(
                            icon = Icons.Default.LocationOn,
                            label = "Lon: ${String.format("%.6f", farm.lon)}"
                        )
                    }
                }
            }
        }
    }
}

/* ---------- INFO CHIP ---------- */

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(14.dp)
            )

            Spacer(Modifier.width(4.dp))

            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
        }
    }
}
