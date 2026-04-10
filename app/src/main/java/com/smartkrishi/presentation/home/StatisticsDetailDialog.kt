package com.smartkrishi.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartkrishi.presentation.model.Farm

// ────────────────────────────────────────────────
//   Professional Color Palette
// ────────────────────────────────────────────────
private val PrimaryGreen     = Color(0xFF1B5E20)
private val PrimaryGreenMid  = Color(0xFF2E7D32)
private val AccentGreen      = Color(0xFF4CAF50)
private val SoftGreen        = Color(0xFFE8F5E9)
private val VeryLightGreen   = Color(0xFFF5FBF5)
private val TextPrimary      = Color(0xFF0F1C0F)
private val TextSecondary    = Color(0xFF4A5F4A)
private val DividerSoft      = Color(0xFFDDE6DD)

// ────────────────────────────────────────────────
//   Statistics Detail Dialog – Refined & Professional
// ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsDetailDialog(
    statType: StatType,
    farms: List<Farm>,
    totalAcres: Int,
    cropDistribution: Map<String, Int>,
    onDismiss: () -> Unit,
    onFarmClick: (Farm) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp)),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = VeryLightGreen,
                    modifier = Modifier
                        .size(72.dp)
                        .border(3.dp, AccentGreen.copy(alpha = 0.3f), CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (statType) {
                                StatType.FARMS  -> Icons.Default.Agriculture
                                StatType.ACRES  -> Icons.Default.Terrain
                                StatType.CROPS  -> Icons.Default.Grass
                            },
                            contentDescription = null,
                            tint = PrimaryGreenMid,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (statType) {
                        StatType.FARMS  -> "Farm Overview"
                        StatType.ACRES  -> "Land Summary"
                        StatType.CROPS  -> "Crop Distribution"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (statType) {
                        StatType.FARMS  -> "${farms.size} ${if (farms.size == 1) "farm" else "farms"} registered"
                        StatType.ACRES  -> "Total cultivated area: $totalAcres acres"
                        StatType.CROPS  -> "${cropDistribution.size} crop ${if (cropDistribution.size == 1) "type" else "types"}"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = DividerSoft,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 420.dp)
                ) {
                    when (statType) {
                        StatType.FARMS -> {
                            items(farms) { farm ->
                                CompactFarmListItem(farm, onFarmClick)
                            }
                        }

                        StatType.ACRES -> {
                            item {
                                TotalAreaHighlightCard(totalAcres, farms.size)
                            }
                            items(farms) { farm ->
                                CompactFarmListItem(farm, onFarmClick)
                            }
                        }

                        StatType.CROPS -> {
                            val sorted = cropDistribution.entries
                                .sortedByDescending { it.value }

                            items(sorted) { (crop, count) ->
                                CropDistributionRow(crop, count)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryGreenMid)
            ) {
                Text(
                    "CLOSE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    )
}

// ────────────────────────────────────────────────
//   Compact single-line friendly farm row
// ────────────────────────────────────────────────
@Composable
private fun CompactFarmListItem(
    farm: Farm,
    onClick: (Farm) -> Unit
) {
    val emoji = when {
        farm.cropType.contains("wheat",    ignoreCase = true) -> "🌾"
        farm.cropType.contains("rice",     ignoreCase = true) -> "🍚"
        farm.cropType.contains("corn",     ignoreCase = true) ||
                farm.cropType.contains("maize",    ignoreCase = true) -> "🌽"
        farm.cropType.contains("potato",   ignoreCase = true) -> "🥔"
        farm.cropType.contains("tomato",   ignoreCase = true) -> "🍅"
        farm.cropType.contains("sugarcane",ignoreCase = true) -> "🎋"
        farm.cropType.contains("cotton",   ignoreCase = true) -> "🌿"
        else -> "🌱"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick(farm) }
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = farm.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = farm.cropType,
                    fontSize = 13.5.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${farm.acres} ac",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentGreen
                )
                if (!farm.location.isNullOrBlank()) {
                    Text(
                        text = farm.location,
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────
//   Total Area Highlight Card – compact & strong
// ────────────────────────────────────────────────
@Composable
private fun TotalAreaHighlightCard(
    totalAcres: Int,
    farmCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        color = SoftGreen
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AccentGreen.copy(alpha = 0.14f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Terrain,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))



            Text(
                text = "$totalAcres acres",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreenMid,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

// ────────────────────────────────────────────────
//   Crop Distribution Row – clean & compact
// ────────────────────────────────────────────────
@Composable
private fun CropDistributionRow(
    crop: String,
    count: Int
) {
    val emoji = when {
        crop.contains("wheat",     ignoreCase = true) -> "🌾"
        crop.contains("rice",      ignoreCase = true) -> "🍚"
        crop.contains("corn",      ignoreCase = true) ||
                crop.contains("maize",     ignoreCase = true) -> "🌽"
        crop.contains("sugarcane", ignoreCase = true) -> "🎋"
        crop.contains("cotton",    ignoreCase = true) -> "🌿"
        crop.contains("potato",    ignoreCase = true) -> "🥔"
        crop.contains("tomato",    ignoreCase = true) -> "🍅"
        else -> "🌱"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = crop,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Farms growing this crop",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AccentGreen.copy(alpha = 0.12f),
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreenMid,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}