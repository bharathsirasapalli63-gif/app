package com.example.ui.citizen

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
fun RiskMonitoringScreen(
    zone: LocationZone,
    sensors: List<SensorNode>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Zone Title
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MULTI-FACTOR GEOTECHNICAL MONITORING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 0.5.sp)
                    Text(zone.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Real-time sensor telemetry & seismic correlation", fontSize = 11.sp, color = Gray300)
                }
            }
        }

        // Multi-Factor Risk Gauge Sliders
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Risk Contribution Parameters", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)

                    // 1. Rainfall
                    FactorProgressItem(
                        label = "24h Accumulated Rainfall",
                        valueStr = "${zone.rainfall.toInt()} mm",
                        progress = (zone.rainfall / 150.0).coerceIn(0.0, 1.0).toFloat(),
                        threshold = "Warning > 80 mm",
                        color = if (zone.rainfall > 80) RiskCritical else BlueAccent
                    )

                    // 2. Soil Moisture Saturation
                    FactorProgressItem(
                        label = "Sub-Surface Soil Moisture",
                        valueStr = "${zone.soilMoisture.toInt()}%",
                        progress = (zone.soilMoisture / 100.0).coerceIn(0.0, 1.0).toFloat(),
                        threshold = "Plastic limit ~ 85%",
                        color = if (zone.soilMoisture > 85) RiskCritical else RiskHigh
                    )

                    // 3. Ground Movement Velocity
                    FactorProgressItem(
                        label = "Micro-Seismic Ground Movement",
                        valueStr = "${zone.groundMovement} mm/s",
                        progress = (zone.groundMovement / 8.0).coerceIn(0.0, 1.0).toFloat(),
                        threshold = "Displacement alert > 3.0 mm/s",
                        color = if (zone.groundMovement > 3.0) RiskCritical else RiskSafe
                    )

                    // 4. Slope Inclination
                    FactorProgressItem(
                        label = "Slope Incline Angle",
                        valueStr = "${zone.slopeAngle.toInt()}°",
                        progress = (zone.slopeAngle / 60.0).coerceIn(0.0, 1.0).toFloat(),
                        threshold = "High gravitational shear > 35°",
                        color = if (zone.slopeAngle > 35) RiskHigh else RiskModerate
                    )
                }
            }
        }

        // IoT Sensors Fleet Telemetry Cards
        item {
            Text("IoT Slope Sensor Telemetry", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
        }

        items(sensors.take(5)) { sensor ->
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = "${sensor.id}: ${sensor.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = sensor.status.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Reading", fontSize = 10.sp, color = Gray600)
                            Text(
                                text = if (sensor.status == SensorStatus.OFFLINE) "DISCONNECTED" else "${sensor.value} ${sensor.unit}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sensor.status == SensorStatus.OFFLINE) RiskCritical else NavyDark
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Battery", fontSize = 10.sp, color = Gray600)
                                Text("${sensor.battery}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (sensor.battery < 20) RiskCritical else NavyDark)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Signal", fontSize = 10.sp, color = Gray600)
                                Text("${sensor.signalDbm} dBm", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
                            }
                        }
                    }

                    if (sensor.faultDescription != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RiskCriticalLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ ${sensor.faultDescription}",
                                fontSize = 10.sp,
                                color = RiskCritical,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Seismic Earthquakes Correlation
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Waves, contentDescription = null, tint = NavyMedium)
                        Column {
                            Text("Seismic History & Slope Fatigue", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                            Text("Tremors documented triggering shear displacements", fontSize = 11.sp, color = Gray600)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    zone.recentEarthquakes.forEach { eq ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Gray50,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${eq.date}, ${eq.year} • Mag ${eq.magnitude} (${eq.distance})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NavyDark)
                                    Text(eq.impact, fontSize = 10.sp, color = Gray600)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FactorProgressItem(
    label: String,
    valueStr: String,
    progress: Float,
    threshold: String,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = Gray800, fontWeight = FontWeight.Medium)
            Text(valueStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = Gray200,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
        Text(threshold, fontSize = 9.sp, color = Gray600)
    }
}
