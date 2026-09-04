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
    onNavigateToAlerts: () -> Unit
) {
    val selectedZone = locations.find { it.id == selectedZoneId } ?: locations.first()
    var showDetourDetail by remember { mutableStateOf(false) }

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
                            text = "Interactive Geospatial Risk Map",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NavyDark
                        )
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

                GisRiskCanvasMap(
                    locations = locations,
                    sensors = sensors,
                    roads = roads,
                    selectedZoneId = selectedZoneId,
                    mapLayers = mapLayers,
                    onSelectZone = onSelectZone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
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
            val blockedRoad = roads.firstOrNull { it.status != RoadStatus.OPEN } ?: roads.first()

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
                            Icon(Icons.Default.AltRoute, contentDescription = null, tint = RiskCritical)
                            Column {
                                Text(
                                    text = "Highway Safety & Safe Detour",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                Text(
                                    text = blockedRoad.name,
                                    fontSize = 11.sp,
                                    color = Gray600
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RiskCriticalLight
                        ) {
                            Text(
                                text = blockedRoad.status.name,
                                color = RiskCritical,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = blockedRoad.reason,
                        fontSize = 11.sp,
                        color = Gray600
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Safe Alternative Corridor Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = RiskSafeLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RiskSafe.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = RiskSafe, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "RECOMMENDED SAFE DETOUR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RiskSafe
                                    )
                                }
                                Text(
                                    text = "${blockedRoad.alternativeRouteRiskPercentage}% Risk",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RiskSafe
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = blockedRoad.alternativeRouteName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyDark
                            )
                            Text(
                                text = "Clear for all light vehicles. Monitored by BRO & Sikkim Police checkpoints.",
                                fontSize = 10.sp,
                                color = Gray600
                            )
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
