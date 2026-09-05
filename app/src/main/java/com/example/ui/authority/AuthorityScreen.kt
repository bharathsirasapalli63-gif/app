package com.example.ui.authority

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.CitizenReportEntity
import com.example.data.model.*
import com.example.ui.gis.GisRiskCanvasMap
import com.example.ui.theme.*
import com.example.ui.viewmodel.MapLayerConfig

@Composable
fun AuthorityScreen(
    locations: List<LocationZone>,
    sensors: List<SensorNode>,
    roads: List<RoadCorridor>,
    reports: List<CitizenReportEntity>,
    selectedZoneId: String,
    mapLayers: MapLayerConfig,
    onSelectZone: (String) -> Unit,
    onToggleMapLayer: (String) -> Unit,
    onVerifyReport: (String) -> Unit,
    onRejectReport: (String) -> Unit,
    onAssignReport: (String, String) -> Unit,
    onBroadcastAlert: (title: String, msg: String, district: String, level: RiskLevel, action: String) -> Unit,
    onToggleRoadStatus: (String, RoadStatus) -> Unit,
    onBasemapChange: ((com.example.ui.viewmodel.GisBasemapType) -> Unit)? = null
) {
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var authorityTab by remember { mutableStateOf("triage") } // triage, map, roads, population
    var isMapExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Command Authority Header & Broadcast Trigger
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
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
                            Icon(Icons.Default.Security, contentDescription = null, tint = CyanAccent)
                            Column {
                                Text("SDMA / EOC EMERGENCY COMMAND", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 0.5.sp)
                                Text("Disaster Operations Center", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Button(
                            onClick = { showBroadcastDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push Siren Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Command KPI Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val critZones = locations.count { it.riskLevel == RiskLevel.CRITICAL || it.riskLevel == RiskLevel.HIGH }
                        val blockedCount = roads.count { it.status == RoadStatus.BLOCKED }
                        val pendingCount = reports.count { it.status == "PENDING" }

                        KpiBadge(title = "High Risk", count = "$critZones", color = RiskCritical, modifier = Modifier.weight(1f))
                        KpiBadge(title = "Blocked", count = "$blockedCount", color = RiskHigh, modifier = Modifier.weight(1f))
                        KpiBadge(title = "Sensors", count = "${sensors.size}", color = CyanAccent, modifier = Modifier.weight(1f))
                        KpiBadge(title = "Pending", count = "$pendingCount", color = RiskModerate, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Sub-Navigation Tabs for Authority Portal
        item {
            TabRow(
                selectedTabIndex = when (authorityTab) {
                    "triage" -> 0
                    "map" -> 1
                    "roads" -> 2
                    "population" -> 3
                    else -> 0
                },
                containerColor = Color.White,
                contentColor = NavyDark
            ) {
                Tab(
                    selected = authorityTab == "triage",
                    onClick = { authorityTab = "triage" },
                    text = { Text("Report Triage (${reports.count { it.status == "PENDING" }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = authorityTab == "map",
                    onClick = { authorityTab = "map" },
                    text = { Text("GIS Radar", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = authorityTab == "roads",
                    onClick = { authorityTab = "roads" },
                    text = { Text("Road Corridors", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = authorityTab == "population",
                    onClick = { authorityTab = "population" },
                    text = { Text("Shelters & Pop", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (authorityTab) {
            "triage" -> {
                // Triage Queue List
                item {
                    Text("Resident Hazard Reports Triage Queue", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                }

                items(reports) { report ->
                    val statusColor = when (report.status) {
                        "VERIFIED" -> RiskSafe
                        "ASSIGNED" -> BlueAccent
                        "PENDING" -> RiskModerate
                        "REJECTED" -> Color.Gray
                        else -> RiskSafe
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = RiskHigh, modifier = Modifier.size(16.dp))
                                    Text("${report.id} • ${report.hazardType}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.15f)) {
                                    Text(report.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(report.description, fontSize = 11.sp, color = Gray800)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📍 ${report.locationName} (${report.timestamp})", fontSize = 10.sp, color = Gray600)
                                Text("Reporter: ${report.reporterName}", fontSize = 10.sp, color = Gray600)
                            }

                            if (report.assignedOfficer != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(shape = RoundedCornerShape(6.dp), color = BlueLight, modifier = Modifier.fillMaxWidth()) {
                                    Text("Dispatched to: ${report.assignedOfficer}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = NavyDark, modifier = Modifier.padding(6.dp))
                                }
                            }

                            // Triage Action Buttons
                            if (report.status == "PENDING") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { onVerifyReport(report.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RiskSafe),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text("Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onAssignReport(report.id, "Field Officer T. Dorjee (FO-402)") },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyMedium),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1.4f).height(32.dp)
                                    ) {
                                        Text("Dispatch FO-402", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { onRejectReport(report.id) },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text("Reject", fontSize = 11.sp, color = Gray600)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "map" -> {
                // GIS Map & Layer Toggles
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("GIS Command Layer Controls", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyDark)

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
                                            if (isMapExpanded) "Compact (460dp)" else "Expand (560dp)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMapExpanded) CyanAccent else Gray700
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FilterChip(
                                    selected = mapLayers.riskZones,
                                    onClick = { onToggleMapLayer("riskZones") },
                                    label = { Text("Risk Polygons", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = mapLayers.sensors,
                                    onClick = { onToggleMapLayer("sensors") },
                                    label = { Text("IoT Sensors", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = mapLayers.roads,
                                    onClick = { onToggleMapLayer("roads") },
                                    label = { Text("Highways", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                item {
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
                            .height(if (isMapExpanded) 560.dp else 460.dp)
                    )
                }
            }

            "roads" -> {
                // Highway Blockage Manager
                item {
                    Text("Highway Corridors & Diversion Management", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                }

                items(roads) { road ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(road.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (road.status) {
                                        RoadStatus.BLOCKED -> RiskCriticalLight
                                        RoadStatus.UNSAFE -> RiskHighLight
                                        RoadStatus.OPEN -> RiskSafeLight
                                    }
                                ) {
                                    Text(
                                        text = road.status.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (road.status) {
                                            RoadStatus.BLOCKED -> RiskCritical
                                            RoadStatus.UNSAFE -> RiskHigh
                                            RoadStatus.OPEN -> RiskSafe
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(road.reason, fontSize = 11.sp, color = Gray600)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Blocked segment: ${road.blockedSection}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RiskCritical)

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Detour Corridor: ${road.alternativeRouteName}", fontSize = 11.sp, color = NavyDark)

                            Spacer(modifier = Modifier.height(10.dp))
                            // Status Toggles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onToggleRoadStatus(road.id, RoadStatus.OPEN) },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Set Open", fontSize = 10.sp, color = RiskSafe)
                                }
                                OutlinedButton(
                                    onClick = { onToggleRoadStatus(road.id, RoadStatus.UNSAFE) },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Set Unsafe", fontSize = 10.sp, color = RiskHigh)
                                }
                                Button(
                                    onClick = { onToggleRoadStatus(road.id, RoadStatus.BLOCKED) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                                    modifier = Modifier.weight(1f).height(30.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Set Blocked", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            "population" -> {
                // Population at risk & shelters
                item {
                    Text("Population Exposure & Camp Capacities", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                }

                items(locations) { zone ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(zone.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                Text("${zone.populationAtRisk} Residents", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RiskCritical)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Designated Camps in District:", fontSize = 10.sp, color = Gray600)

                            zone.shelters.forEach { sh ->
                                val occRatio = sh.occupied.toFloat() / sh.capacity.toFloat()
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${sh.name}", fontSize = 11.sp, color = Gray800)
                                    Text("${sh.occupied}/${sh.capacity} (${(occRatio * 100).toInt()}%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (occRatio > 0.8f) RiskCritical else NavyDark)
                                }
                                LinearProgressIndicator(
                                    progress = { occRatio },
                                    color = if (occRatio > 0.8f) RiskCritical else BlueAccent,
                                    trackColor = Gray200,
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Siren Broadcast Dialog
    if (showBroadcastDialog) {
        var alertTitle by remember { mutableStateOf("CRITICAL LANDSLIDE WARNING: Chungthang - Mangan Corridor") }
        var alertDistrict by remember { mutableStateOf("North Sikkim") }
        var alertMsg by remember { mutableStateOf("Torrential monsoon rainfall combined with high micro-seismic vibrations have compromised slope equilibrium. Immediate evacuation ordered.") }
        var alertAction by remember { mutableStateOf("Evacuate slope-toe residences to Chungthang Community Hall Relief Camp immediately. NH-10 closed.") }
        var alertLevel by remember { mutableStateOf(RiskLevel.CRITICAL) }

        Dialog(onDismissRequest = { showBroadcastDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Broadcast Emergency Siren Alert", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RiskCritical)
                    Text("Pushes immediate alert to all resident devices in district", fontSize = 11.sp, color = Gray600)

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = alertTitle,
                        onValueChange = { alertTitle = it },
                        label = { Text("Alert Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = alertDistrict,
                        onValueChange = { alertDistrict = it },
                        label = { Text("Target District / Sector") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = alertMsg,
                        onValueChange = { alertMsg = it },
                        label = { Text("Broadcast Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = alertAction,
                        onValueChange = { alertAction = it },
                        label = { Text("Mandatory Resident Action") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showBroadcastDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onBroadcastAlert(alertTitle, alertMsg, alertDistrict, alertLevel, alertAction)
                                showBroadcastDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Transmit Siren", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiBadge(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = NavySurface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Gray300)
        }
    }
}
