package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*

@Composable
fun AdminScreen(
    sensors: List<SensorNode>,
    healthNodes: List<SystemHealthNode>,
    aiConfig: AiModelWeights,
    onDispatchRepair: (String) -> Unit,
    onUpdateAiConfig: (AiModelWeights) -> Unit
) {
    var adminTab by remember { mutableStateOf("sensors") } // sensors, health, ai, users

    // AI Weights local edit state
    var rainfallWeight by remember(aiConfig) { mutableStateOf(aiConfig.rainfall) }
    var soilWeight by remember(aiConfig) { mutableStateOf(aiConfig.soilMoisture) }
    var movementWeight by remember(aiConfig) { mutableStateOf(aiConfig.groundMovement) }
    var slopeWeight by remember(aiConfig) { mutableStateOf(aiConfig.slopeAngle) }
    var geologyWeight by remember(aiConfig) { mutableStateOf(aiConfig.terrainGeology) }
    var threshold by remember(aiConfig) { mutableStateOf(aiConfig.autoAlertTriggerThreshold.toFloat()) }

    val offlineSensor = sensors.find { it.status == SensorStatus.OFFLINE }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Header
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(28.dp))
                            Column {
                                Text("SYSTEM & SENSOR DIRECTORATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 0.5.sp)
                                Text("Super Administrator Console", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("IoT Mesh • AI Ensemble Tuning • Infrastructure", fontSize = 10.sp, color = Gray300)
                            }
                        }
                    }
                }
            }
        }

        // Automated Sensor Failure Incident Banner (S-102)
        if (offlineSensor != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = RiskCriticalLight),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RiskCritical),
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = RiskCritical)
                                Text(
                                    text = "SENSOR DISCONNECT: ${offlineSensor.id} OFFLINE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RiskCritical
                                )
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = RiskCritical) {
                                Text("CRITICAL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${offlineSensor.name} at ${offlineSensor.locationName} has stopped sending telemetry. Battery level: ${offlineSensor.battery}%. Fault: ${offlineSensor.faultDescription}",
                            fontSize = 11.sp,
                            color = Gray800
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onDispatchRepair(offlineSensor.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dispatch Repair Order & Re-initialize Telemetry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sub Tabs: Sensors / Health / AI Model / Users
        item {
            TabRow(
                selectedTabIndex = when (adminTab) {
                    "sensors" -> 0
                    "health" -> 1
                    "ai" -> 2
                    "users" -> 3
                    else -> 0
                },
                containerColor = Color.White,
                contentColor = NavyDark
            ) {
                Tab(
                    selected = adminTab == "sensors",
                    onClick = { adminTab = "sensors" },
                    text = { Text("Sensors (${sensors.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == "health",
                    onClick = { adminTab = "health" },
                    text = { Text("System Health", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == "ai",
                    onClick = { adminTab = "ai" },
                    text = { Text("AI Weights", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = adminTab == "users",
                    onClick = { adminTab = "users" },
                    text = { Text("RBAC", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (adminTab) {
            "sensors" -> {
                // Sensor Fleet Telemetry Management Table
                items(sensors) { sensor ->
                    val statusColor = when (sensor.status) {
                        SensorStatus.ONLINE -> RiskSafe
                        SensorStatus.WARNING -> RiskModerate
                        SensorStatus.CRITICAL -> RiskCritical
                        SensorStatus.OFFLINE -> Color.Gray
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
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                                    Text("${sensor.id}: ${sensor.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.15f)) {
                                    Text(sensor.status.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Type: ${sensor.typeLabel} • Installed: ${sensor.installationDate}", fontSize = 10.sp, color = Gray600)
                            Text("Location: ${sensor.locationName} (${sensor.latitude}, ${sensor.longitude})", fontSize = 10.sp, color = BlueAccent)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (sensor.status == SensorStatus.OFFLINE) "NO DATA" else "${sensor.value} ${sensor.unit}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sensor.status == SensorStatus.OFFLINE) RiskCritical else NavyDark
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("🔋 ${sensor.battery}%", fontSize = 10.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                                    Text("📶 ${sensor.signalDbm} dBm", fontSize = 10.sp, color = NavyDark, fontWeight = FontWeight.SemiBold)
                                    Text("🕒 ${sensor.lastPing}", fontSize = 10.sp, color = Gray600)
                                }
                            }
                        }
                    }
                }
            }

            "health" -> {
                // System Telemetry & Health Nodes
                items(healthNodes) { node ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(node.component, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                Text("Latency: ${node.latency} • Uptime: ${node.uptime}", fontSize = 11.sp, color = Gray600)
                                if (node.note != null) {
                                    Text(node.note!!, fontSize = 10.sp, color = RiskHigh, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (node.status == "OPERATIONAL") RiskSafeLight else RiskHighLight
                            ) {
                                Text(
                                    text = node.status,
                                    color = if (node.status == "OPERATIONAL") RiskSafe else RiskHigh,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            "ai" -> {
                // AI Multi-Factor Weight Configuration Sliders
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("AI Landslide Ensemble Feature Weights", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                            Text("Calibrate feature importances for early warning probability scoring", fontSize = 11.sp, color = Gray600)

                            // Rainfall slider
                            WeightSliderItem(
                                label = "Rainfall Saturation (24h Precipitation)",
                                weight = rainfallWeight,
                                onWeightChange = { rainfallWeight = it }
                            )

                            // Soil Moisture
                            WeightSliderItem(
                                label = "Sub-Surface Soil Moisture (TDR Probe)",
                                weight = soilWeight,
                                onWeightChange = { soilWeight = it }
                            )

                            // Ground Movement
                            WeightSliderItem(
                                label = "Micro-Seismic Ground Displacement",
                                weight = movementWeight,
                                onWeightChange = { movementWeight = it }
                            )

                            // Slope Incline
                            WeightSliderItem(
                                label = "Slope Angle & Gravitational Shear",
                                weight = slopeWeight,
                                onWeightChange = { slopeWeight = it }
                            )

                            // Geology
                            WeightSliderItem(
                                label = "Lithology & GSI Geological Formation",
                                weight = geologyWeight,
                                onWeightChange = { geologyWeight = it }
                            )

                            Divider(color = Gray200)

                            Text("Auto-Alert Trigger Threshold: ${threshold.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Slider(
                                value = threshold,
                                onValueChange = { threshold = it },
                                valueRange = 50f..95f,
                                steps = 9,
                                colors = SliderDefaults.colors(thumbColor = RiskCritical, activeTrackColor = RiskCritical)
                            )

                            Button(
                                onClick = {
                                    val updated = AiModelWeights(
                                        rainfall = rainfallWeight,
                                        soilMoisture = soilWeight,
                                        groundMovement = movementWeight,
                                        slopeAngle = slopeWeight,
                                        terrainGeology = geologyWeight,
                                        autoAlertTriggerThreshold = threshold.toInt()
                                    )
                                    onUpdateAiConfig(updated)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyMedium),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save & Synchronize AI Ensemble", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            "users" -> {
                // User Management & RBAC
                item {
                    Text("Role-Based Access Control (RBAC) Accounts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                }

                val roles = listOf(
                    Triple("citizen (Public)", "Resident Portal Access • Community Geo-Reporting", "Active"),
                    Triple("authority (Disaster Command)", "State EOC Operations • Broadcast Siren • Triage", "Active"),
                    Triple("officer (FO-402 T. Dorjee)", "Field Geotechnical Inspection • LoRa Telemetry", "Active"),
                    Triple("admin (Super Administrator)", "Directorate Control • AI Weight Calibration", "Active")
                )

                items(roles) { (user, desc, status) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                Text(desc, fontSize = 11.sp, color = Gray600)
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = RiskSafeLight) {
                                Text(status, color = RiskSafe, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightSliderItem(
    label: String,
    weight: Float,
    onWeightChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = Gray800)
            Text("${(weight * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
        }
        Slider(
            value = weight,
            onValueChange = onWeightChange,
            valueRange = 0.05f..0.50f,
            colors = SliderDefaults.colors(thumbColor = NavyMedium, activeTrackColor = NavyMedium)
        )
    }
}
