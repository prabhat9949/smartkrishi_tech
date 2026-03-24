package com.smartkrishi.presentation.market

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartkrishi.presentation.theme.ThemeState
import kotlin.math.abs

// ------------------------------------------------------------------
// Data models
// ------------------------------------------------------------------
data class MarketPrice(
    val commodity: String,
    val location: String,
    val grade: String,
    val unit: String = "₹ / Quintal",
    val todayPrice: Int,
    val change: Int,
    val lastUpdated: String
)

data class PricePoint(
    val dayLabel: String,
    val value: Int
)

data class PredictionDay(
    val label: String,
    val price: Int
)

data class NearbyMandiPrice(
    val mandiName: String,
    val distanceKm: Int,
    val price: Int
)

// ------------------------------------------------------------------
// MOCK REPOSITORY – replace with Retrofit when you're ready
// ------------------------------------------------------------------
object MarketRepositoryMock {
    fun getTodayPrice(commodity: String, location: String): MarketPrice {
        return MarketPrice(
            commodity = commodity,
            location = location,
            grade = "A",
            todayPrice = 2500,
            change = 50,
            lastUpdated = "10:30 AM"
        )
    }

    fun getPriceTrend7D(): List<PricePoint> {
        return listOf(
            PricePoint("Day 1", 2100),
            PricePoint("Day 2", 2200),
            PricePoint("Day 3", 2300),
            PricePoint("Day 4", 2350),
            PricePoint("Day 5", 2400),
            PricePoint("Day 6", 2480),
            PricePoint("Today", 2500)
        )
    }

    fun getPredictions(): List<PredictionDay> {
        return listOf(
            PredictionDay("Tomorrow", 2520),
            PredictionDay("Day After", 2550),
            PredictionDay("In 3 Days", 2530),
        )
    }

    fun getNearbyMandis(): List<NearbyMandiPrice> {
        return listOf(
            NearbyMandiPrice("Pune APMC", 15, 2510),
            NearbyMandiPrice("Manchar Market", 45, 2540),
            NearbyMandiPrice("Pimpri Mandi", 22, 2480),
        )
    }
}

// ------------------------------------------------------------------
// MAIN SCREEN
// ------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPriceScreen(
    navController: NavController
) {
    val isDark = ThemeState.isDarkTheme.value
    val backgroundColor = if (isDark) Color(0xFF0F1A15) else Color(0xFFF4F6F5)
    val cardColor = if (isDark) Color(0xFF162920) else Color.White
    val textPrimary = if (isDark) Color(0xFFEAF7EC) else Color(0xFF0B140C)
    val textSecondary = if (isDark) Color(0xFFA9C3AF) else Color.Gray
    val primaryGreen = Color(0xFF2E7D32)
    val chipBg = if (isDark) Color(0xFF1F3328) else Color(0xFFE4F3E6)

    var commodityQuery by remember { mutableStateOf(TextFieldValue("Tomato")) }
    var selectedLocation by remember { mutableStateOf("Pune") }
    var selectedGrade by remember { mutableStateOf("A") }
    var selectedRange by remember { mutableStateOf("7D") }

    // In real world this would be state from ViewModel (API)
    val marketPrice = MarketRepositoryMock.getTodayPrice(
        commodityQuery.text.ifBlank { "Tomato" },
        selectedLocation
    )
    val trendPoints = MarketRepositoryMock.getPriceTrend7D()
    val predictions = MarketRepositoryMock.getPredictions()
    val nearbyMandis = MarketRepositoryMock.getNearbyMandis()

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Market Insights", color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: share */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(backgroundColor)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Search
            SearchBarCard(
                query = commodityQuery,
                onQueryChange = { commodityQuery = it },
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardColor = cardColor
            )

            // Filters row (Location / Grade)
            FilterRow(
                selectedLocation = selectedLocation,
                onLocationClick = { /* TODO open location picker */ },
                selectedGrade = selectedGrade,
                onGradeClick = { /* TODO open grade picker */ },
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                chipBg = chipBg
            )

            // Today's price
            TodayPriceCard(
                price = marketPrice,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                primaryGreen = primaryGreen
            )

            // Trend graph
            PriceTrendCard(
                selectedRange = selectedRange,
                onRangeSelected = { selectedRange = it },
                trendPoints = trendPoints,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                primaryGreen = primaryGreen
            )

            // 3-day prediction
            PredictionCard(
                predictions = predictions,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                primaryGreen = primaryGreen
            )

            // Nearby mandis
            NearbyMandisSection(
                items = nearbyMandis,
                cardColor = cardColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                primaryGreen = primaryGreen
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ------------------------------------------------------------------
// SEARCH BAR
// ------------------------------------------------------------------
@Composable
private fun SearchBarCard(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    cardColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = textSecondary
            )
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = textPrimary,
                    fontSize = 16.sp
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                decorationBox = { inner ->
                    if (query.text.isEmpty()) {
                        Text(
                            "Tomato",
                            color = textSecondary,
                            fontSize = 16.sp
                        )
                    }
                    inner()
                }
            )
            IconButton(onClick = { /* TODO: voice search maybe */ }) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = textSecondary)
            }
        }
    }
}

// ------------------------------------------------------------------
// FILTER ROW
// ------------------------------------------------------------------
@Composable
private fun FilterRow(
    selectedLocation: String,
    onLocationClick: () -> Unit,
    selectedGrade: String,
    onGradeClick: () -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    chipBg: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChipCard(
            icon = Icons.Default.Place,
            label = "Location: $selectedLocation",
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            bgColor = chipBg,
            modifier = Modifier.weight(1f),
            onClick = onLocationClick
        )
        FilterChipCard(
            icon = Icons.Default.Grade,
            label = "Grade: $selectedGrade",
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            bgColor = chipBg,
            modifier = Modifier.weight(1f),
            onClick = onGradeClick
        )
    }
}

@Composable
private fun FilterChipCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    textPrimary: Color,
    textSecondary: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = textPrimary)
            Text(
                label,
                color = textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = textSecondary
            )
        }
    }
}

// ------------------------------------------------------------------
// TODAY PRICE CARD
// ------------------------------------------------------------------
@Composable
private fun TodayPriceCard(
    price: MarketPrice,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    primaryGreen: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Today’s Average Price",
                color = textSecondary,
                fontSize = 13.sp
            )
            Text(
                "₹${price.todayPrice} / Quintal",
                color = textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val up = price.change >= 0
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (up) primaryGreen.copy(alpha = 0.15f)
                            else Color.Red.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (up) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (up) primaryGreen else Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
                val sign = if (up) "+" else "-"
                val percentChange = if (price.todayPrice != 0)
                    price.change.toFloat() / (price.todayPrice - price.change).coerceAtLeast(1) * 100f
                else 0f

                Text(
                    "$sign₹${abs(price.change)} (${String.format("%.2f", abs(percentChange))}%)",
                    color = if (up) primaryGreen else Color.Red,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Last updated: ${price.lastUpdated}",
                color = textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// ------------------------------------------------------------------
// PRICE TREND CARD + GRAPH
// ------------------------------------------------------------------
@Composable
private fun PriceTrendCard(
    selectedRange: String,
    onRangeSelected: (String) -> Unit,
    trendPoints: List<PricePoint>,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    primaryGreen: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Price Trend", color = textPrimary, fontWeight = FontWeight.SemiBold)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("7D", "1M", "3M").forEach { label ->
                        val isSelected = selectedRange == label
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) primaryGreen else Color.Transparent,
                            border = if (isSelected) null
                            else BorderStroke(1.dp, primaryGreen.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .height(30.dp)
                                .clickable { onRangeSelected(label) }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (isSelected) Color.White else primaryGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                PriceLineChart(
                    points = trendPoints,
                    lineColor = primaryGreen,
                    fillColor = primaryGreen.copy(alpha = 0.18f)
                )
            }
        }
    }
}

@Composable
private fun PriceLineChart(
    points: List<PricePoint>,
    lineColor: Color,
    fillColor: Color
) {
    if (points.isEmpty()) return

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        val max = points.maxOf { it.value }
        val min = points.minOf { it.value }
        val range = (max - min).coerceAtLeast(1)

        val stepX = if (points.size > 1) size.width / (points.size - 1) else 0f

        val chartPoints = points.mapIndexed { index, p ->
            val ratio = (p.value - min).toFloat() / range.toFloat()
            val y = size.height - (ratio * size.height)
            val x = stepX * index
            Offset(x, y)
        }

        // Area path
        val areaPath = Path().apply {
            moveTo(chartPoints.first().x, size.height)
            chartPoints.forEach { lineTo(it.x, it.y) }
            lineTo(chartPoints.last().x, size.height)
            close()
        }
        drawPath(
            path = areaPath,
            color = fillColor
        )

        // Line path
        val linePath = Path().apply {
            moveTo(chartPoints.first().x, chartPoints.first().y)
            chartPoints.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            // ✅ Correct Stroke usage (no strokeWidth named param)
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Data point circles
        chartPoints.forEach {
            drawCircle(
                color = lineColor,
                radius = 5.dp.toPx(),
                center = it
            )
        }
    }
}

// ------------------------------------------------------------------
// PREDICTION CARD
// ------------------------------------------------------------------
@Composable
private fun PredictionCard(
    predictions: List<PredictionDay>,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    primaryGreen: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("3-Day Prediction", color = textPrimary, fontWeight = FontWeight.SemiBold)
                Text("Best Selling Day", color = textSecondary, fontSize = 13.sp)
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                predictions.forEach { day ->
                    PredictionSmallCard(
                        label = day.label,
                        price = day.price,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        borderColor = primaryGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = primaryGreen.copy(alpha = 0.06f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = primaryGreen
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Best Day to Sell: Friday",
                            color = primaryGreen,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Text(
                            "Prices expected to peak due to increased weekend demand.",
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionSmallCard(
    label: String,
    price: Int,
    textPrimary: Color,
    textSecondary: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = textSecondary, fontSize = 11.sp)
            Text(
                "₹$price",
                color = textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("/ Quintal", color = textSecondary, fontSize = 10.sp)
        }
    }
}

// ------------------------------------------------------------------
// NEARBY MANDIS
// ------------------------------------------------------------------
@Composable
private fun NearbyMandisSection(
    items: List<NearbyMandiPrice>,
    cardColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    primaryGreen: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Prices in Nearby Mandis", color = textPrimary, fontWeight = FontWeight.SemiBold)

        items.forEach { mandi ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            mandi.mandiName,
                            color = textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${mandi.distanceKm} km away",
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "₹${mandi.price}",
                            color = primaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.End
                        )
                        Text(
                            "/ Quintal",
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
