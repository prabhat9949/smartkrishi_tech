package com.smartkrishi.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartkrishi.presentation.model.Farm

// ────────────────────────────────────────────────
//   Color Definitions (consistent with your theme)
// ────────────────────────────────────────────────
private val PrimaryGreen      = Color(0xFF1B5E20)
private val PrimaryGreenLight = Color(0xFF2E7D32)
private val AccentGreen       = Color(0xFF4CAF50)
private val DangerRed         = Color(0xFFD32F2F)
private val SurfaceLight      = Color(0xFFF8FAFC)
private val CardBg            = Color.White
private val DividerColor      = Color(0xFFE2E8F0)

// ────────────────────────────────────────────────
//   Enhanced Farm Action Bottom Sheet
// ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmActionSheet(
    farm: Farm,
    onOpenFarm: () -> Unit,
    onEditFarm: () -> Unit,
    onDeleteFarmClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { /* Handled by parent */ },
        containerColor = CardBg,
        tonalElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(width = 42.dp, height = 5.dp),
                shape = CircleShape,
                color = Color.LightGray.copy(alpha = 0.5f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Farm Header Card ───────────────────────────────
            FarmPreviewHeader(farm = farm)

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Primary Actions ────────────────────────────────
            EnhancedActionItem(
                icon = Icons.Default.Analytics,
                title = "View Dashboard",
                subtitle = "View analytics, insights & recommendations",
                onClick = onOpenFarm
            )

            EnhancedActionItem(
                icon = Icons.Default.EditNote,
                title = "Edit Farm Details",
                subtitle = "Update name, crop, area, location...",
                onClick = onEditFarm
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ─── Dangerous Action ───────────────────────────────
            EnhancedActionItem(
                icon = Icons.Default.DeleteForever,
                title = "Delete This Farm",
                subtitle = "This action cannot be undone",
                isDanger = true,
                onClick = onDeleteFarmClick
            )
        }
    }
}

// ────────────────────────────────────────────────
//   Farm Preview Header
// ────────────────────────────────────────────────
@Composable
private fun FarmPreviewHeader(farm: Farm) {
    val cropEmoji = when {
        farm.cropType.contains("wheat",  ignoreCase = true) -> "🌾"
        farm.cropType.contains("rice",   ignoreCase = true) -> "🍚"
        farm.cropType.contains("corn",   ignoreCase = true) ||
                farm.cropType.contains("maize",  ignoreCase = true) -> "🌽"
        farm.cropType.contains("potato", ignoreCase = true) -> "🥔"
        farm.cropType.contains("tomato", ignoreCase = true) -> "🍅"
        farm.cropType.contains("sugarcane", ignoreCase = true) -> "🎋"
        farm.cropType.contains("cotton", ignoreCase = true) -> "🌿"
        else -> "🌱"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE8F5E9),
                                Color(0xFFC8E6C9).copy(alpha = 0.7f)
                            )
                        )
                    )
                    .border(2.dp, AccentGreen.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cropEmoji,
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = farm.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${farm.acres} acres",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryGreenLight
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "•",
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = farm.cropType,
                        fontSize = 14.sp,
                        color = Color(0xFF616161)
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────
//   Modern Action Item Row
// ────────────────────────────────────────────────
@Composable
private fun EnhancedActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    val accentColor = if (isDanger) DangerRed else AccentGreen
    val bgTint     = if (isDanger) DangerRed.copy(alpha = 0.08f) else AccentGreen.copy(alpha = 0.09f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        color = CardBg,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with subtle gradient
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDanger) DangerRed else PrimaryGreen
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}