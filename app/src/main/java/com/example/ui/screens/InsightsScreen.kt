package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CarbonCalculator
import com.example.ui.EcoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: EcoViewModel) {
    val scrollState = rememberScrollState()
    val profileState by viewModel.userProfile.collectAsState()
    val historyState by viewModel.trackHistory.collectAsState()

    val profile = profileState ?: return

    val baselineBreakdown = CarbonCalculator.calculateBaseline(profile)

    // Calculate details
    val totalFootprint = baselineBreakdown.totalDaily
    val isCleanerThanAverage = totalFootprint < 18.0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Eco Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Statement Card
            OverviewMetricsCard(totalFootprint = totalFootprint, isCleaner = isCleanerThanAverage)

            // Canvas drawing for Pie Chart Breakdown
            PieChartBreakdownCard(breakdown = baselineBreakdown)

            // Category Bar Progress list
            CategoryDetailsBarList(breakdown = baselineBreakdown)

            // Tracked Carbon Logs History
            HistoricalLogsSection(historyList = historyState, baseline = baselineBreakdown.totalDaily)
        }
    }
}

@Composable
fun OverviewMetricsCard(totalFootprint: Double, isCleaner: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("insights_overview_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCleaner) Icons.Default.TrendingDown else Icons.Default.TrendingFlat,
                    contentDescription = null,
                    tint = if (isCleaner) MaterialTheme.colorScheme.primary else Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Weekly Efficiency Summary",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isCleaner) {
                    "Outstanding! Your daily calculated carbon foot footprint of ${String.format("%.1f", totalFootprint)} kg CO₂e is nearly 35% safer than regional household thresholds. Keep using renewable energy patterns and green transit links!"
                } else {
                    "Your daily footprints are averaging slightly above optimal thresholds. Reducing single car travel and shifting diet selections can cut your carbon volume by up to 2.5 tons yearly!"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun PieChartBreakdownCard(breakdown: CarbonCalculator.CarbonBreakdown) {
    // Elegant colors for segments
    val categories = listOf(
        "Transport" to (breakdown.transport to Color(0xFF26A69A)),
        "Energy" to (breakdown.energy to Color(0xFF4DB6AC)),
        "Diet" to (breakdown.diet to Color(0xFF81C784)),
        "Flights" to (breakdown.flight to Color(0xFF4CAF50)),
        "Shopping" to (breakdown.shopping to Color(0xFFAB47BC)),
        "Waste" to (breakdown.waste to Color(0xFFFFB300))
    ).filter { it.second.first > 0.1 }

    val total = categories.sumOf { it.second.first }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("insights_pie_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Emission Sources Breakdown",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Canvas Drawing
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    var startAngle = 0f
                    categories.forEach { (_, data) ->
                        val (value, color) = data
                        val sweepAngle = ((value / total) * 360f).toFloat()
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            size = Size(size.width, size.height),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 24.dp.toPx())
                        )
                        startAngle += sweepAngle
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.1f", breakdown.totalDaily)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "kg/day",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legended segments list in grid pairs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        pair.forEach { (name, data) ->
                            val (valSelected, color) = data
                            val pct = (valSelected / total * 100).toInt()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.width(130.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$name ($pct%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDetailsBarList(breakdown: CarbonCalculator.CarbonBreakdown) {
    val largestCat = breakdown.getLargestCategoryName()
    val list = listOf(
        Triple("Transportation", breakdown.transport, Color(0xFF26A69A)),
        Triple("Home Energy", breakdown.energy, Color(0xFF4DB6AC)),
        Triple("Diet", breakdown.diet, Color(0xFF81C784)),
        Triple("Flights (allocated)", breakdown.flight, Color(0xFF4CAF50)),
        Triple("Shopping Habits", breakdown.shopping, Color(0xFFAB47BC)),
        Triple("Waste & Recycling", breakdown.waste, Color(0xFFFFB300))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("insights_bars_list"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Emission Intensity Level",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            list.forEach { (name, amt, color) ->
                val ratio = (amt / breakdown.totalDaily).toFloat().coerceIn(0f, 1f)
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (name == largestCat) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "LARGEST",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${String.format("%.1f", amt)} kg",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        color = color,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

@Composable
fun HistoricalLogsSection(historyList: List<com.example.data.DailyTrackEntity>, baseline: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("insights_historical_logs_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Tracking Activity Logs",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No track logs present yet. Complete onboarding or daily submissions!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    historyList.take(6).forEach { track ->
                        val savedKg = baseline - track.carbonValue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = track.dateString,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (track.hadChanges) "Modified changes applied" else "Standard profile baseline applied",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format("%.1f", track.carbonValue)} kg",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                if (savedKg != 0.0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (savedKg > 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = if (savedKg > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (savedKg > 0) "Saved ${String.format("%.1f", savedKg)}kg" else "Added ${String.format("%.1f", -savedKg)}kg",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (savedKg > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
