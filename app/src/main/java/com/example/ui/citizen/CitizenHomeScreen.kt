package com.example.ui.citizen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.common.LocalizationHelper
import com.example.ui.gis.GisRiskCanvasMap
import com.example.ui.theme.*
import com.example.ui.viewmodel.MapLayerConfig

@Composable
fun CitizenHomeScreen(
    locations: List<LocationZone>,
    sensors: List<SensorNode>,
    roads: List<RoadCorridor>,
    selectedZoneId: String,
    mapLayers: MapLayerConfig,
    onSelectZone: (String) -> Unit,
    onToggle3D: () -> Unit,
    onReportHazardClick: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onBasemapChange: ((com.example.ui.viewmodel.GisBasemapType) -> Unit)? = null,
    selectedLanguage: String = "en"
) {
    val selectedZone = locations.find { it.id == selectedZoneId } ?: locations.first()
    var showDetourDetail by remember { mutableStateOf(false) }
    var isMapExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Zone Selector Chips Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Monitoring Sector:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray600
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(locations) { zone ->
                        val isSelected = zone.id == selectedZoneId
                        val riskColor = when (zone.riskLevel) {
                            RiskLevel.CRITICAL -> RiskCritical
                            RiskLevel.HIGH -> RiskHigh
                            RiskLevel.MODERATE -> RiskModerate
                            RiskLevel.LOW -> RiskSafe
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NavyDark else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CyanAccent else Gray200
                            ),
                            modifier = Modifier.clickable { onSelectZone(zone.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(riskColor)
                                )
                                Text(
                                    text = zone.name.split("-").first().trim(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else NavyDark
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = riskColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${zone.riskPercentage}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = riskColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Interactive GIS Canvas Map
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = NavyMedium, modifier = Modifier.size(18.dp))
                        Text(
                            text = LocalizationHelper.getString("interactive_gis_map", selectedLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyDark
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Map Size Expand/Tall Toggle (540dp to 750dp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isMapExpanded) NavyDark else Gray100,
                            modifier = Modifier.clickable { isMapExpanded = !isMapExpanded }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    if (isMapExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = null,
                                    tint = if (isMapExpanded) CyanAccent else Gray700,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    if (isMapExpanded) "Standard (540dp)" else "Tall Map (750dp)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMapExpanded) CyanAccent else Gray700
                                )
                            }
                        }

                        // 3D Terrain Cross-Section Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BlueLight,
                            modifier = Modifier.clickable { onToggle3D() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Layers, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(14.dp))
                                Text("3D Strata", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                            }
                        }
                    }
                }

                GisRiskCanvasMap(
                    locations = locations,
                    sensors = sensors,
                    roads = roads,
                    selectedZoneId = selectedZoneId,
                    mapLayers = mapLayers,
                    onSelectZone = onSelectZone,
                    onBasemapChange = onBasemapChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isMapExpanded) 750.dp else 540.dp)
                )
            }
        }

        // 3. Primary "Am I Safe?" Status Card
        item {
            val statusColor = when (selectedZone.riskLevel) {
                RiskLevel.CRITICAL -> RiskCritical
                RiskLevel.HIGH -> RiskHigh
                RiskLevel.MODERATE -> RiskModerate
                RiskLevel.LOW -> RiskSafe
            }
            val statusBg = when (selectedZone.riskLevel) {
                RiskLevel.CRITICAL -> RiskCriticalLight
                RiskLevel.HIGH -> RiskHighLight
                RiskLevel.MODERATE -> RiskModerateLight
                RiskLevel.LOW -> RiskSafeLight
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "CURRENT SAFETY STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gray600,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = selectedZone.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "${selectedZone.district}, ${selectedZone.state}",
                                fontSize = 12.sp,
                                color = Gray600
                            )
                        }

                        // Big Risk Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = statusBg
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${selectedZone.riskPercentage}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor
                                )
                                Text(
                                    text = selectedZone.riskLevel.label.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Early Warning Prediction Window
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBg.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                            Text(
                                text = selectedZone.predictionWindow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI Telemetry Explainability breakdown
                    Text(
                        text = "Why is risk elevated?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedZone.whyElevated,
                        fontSize = 11.sp,
                        color = Gray600,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onReportHazardClick,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyMedium),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report Hazard", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateToAlerts,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp), tint = NavyDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Evac Orders", fontSize = 12.sp, color = NavyDark)
                        }
                    }
                }
            }
        }

        // 4. Highway Corridor Road Safety & Alternative Safe Detours Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AltRoute, contentDescription = null, tint = RoadDangerRed)
                            Column {
                                Text(
                                    text = LocalizationHelper.getString("road_safety_corridor", selectedLanguage),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                Text(
                                    text = "Real-Time BRO Highway Telemetry • NE India",
                                    fontSize = 11.sp,
                                    color = Gray600
                                )
                            }
                        }

                        // Summary pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Gray100
                        ) {
                            Text(
                                text = "${roads.count { it.status == RoadStatus.OPEN }} Safe • ${roads.count { it.status == RoadStatus.BLOCKED }} Blocked",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Road Status Legend Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavySurface)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RoadSafeGreen))
                            Text("Safe: Green", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoadSafeGreenLight)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RoadMediumOrange))
                            Text("Medium: Orange", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoadMediumOrangeLight)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RoadDangerRed))
                            Text("Danger: Red", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoadDangerRedLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // List of Road Corridors
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        roads.forEach { road ->
                            val (badgeBg, badgeText, badgeColor) = when (road.status) {
                                RoadStatus.OPEN -> Triple(RoadSafeGreenLight, "SAFE (GREEN)", RoadSafeGreen)
                                RoadStatus.UNSAFE -> Triple(RoadMediumOrangeLight, "MEDIUM (ORANGE)", RoadMediumOrange)
                                RoadStatus.BLOCKED -> Triple(RoadDangerRedLight, "DANGER (RED)", RoadDangerRed)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Gray50,
                                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = road.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = NavyDark
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = badgeBg
                                        ) {
                                            Text(
                                                text = badgeText,
                                                color = badgeColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = road.reason,
                                        fontSize = 11.sp,
                                        color = Gray700
                                    )

                                    if (road.status != RoadStatus.OPEN) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = RoadSafeGreen, modifier = Modifier.size(14.dp))
                                            Text(
                                                text = "Detour: ${road.alternativeRouteName} (${road.alternativeRouteRiskPercentage}% Risk)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoadSafeGreen
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

        // 5. Atmospheric & Saturation Micro-Climate Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AWS Himalayan Micro-Climate Telemetry",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Temp
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Gray100,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Ambient Temp", fontSize = 9.sp, color = Gray600)
                                Text("${selectedZone.weatherTemp}°C", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                                Text(selectedZone.weatherCondition, fontSize = 9.sp, color = Gray600, maxLines = 1)
                            }
                        }

                        // Rainfall 24h
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Gray100,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("24h Rain", fontSize = 9.sp, color = Gray600)
                                Text("${selectedZone.rainfall.toInt()} mm", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                                Text("Threshold 80mm", fontSize = 9.sp, color = Gray600)
                            }
                        }

                        // Soil Saturation
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Gray100,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Soil Saturation", fontSize = 9.sp, color = Gray600)
                                Text("${selectedZone.soilMoisture.toInt()}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RiskCritical)
                                Text("Plastic Limit 85%", fontSize = 9.sp, color = Gray600)
                            }
                        }
                    }
                }
            }
        }
    }
}
