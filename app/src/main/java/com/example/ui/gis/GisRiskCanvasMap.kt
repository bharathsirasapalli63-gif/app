package com.example.ui.gis

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MapLayerConfig
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GisRiskCanvasMap(
    locations: List<LocationZone>,
    sensors: List<SensorNode>,
    roads: List<RoadCorridor>,
    selectedZoneId: String,
    mapLayers: MapLayerConfig,
    onSelectZone: (String) -> Unit,
    modifier: Modifier = Modifier,
    isHeroMode: Boolean = true
) {
    // Pulse animation for critical hazard zones & sensors
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRad"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Coords mapping helper for North-East India bounding box:
    // Longitude roughly 88.0° to 95.0° (West to East)
    // Latitude roughly 23.0° to 28.0° (South to North)
    val minLon = 88.0
    val maxLon = 95.0
    val minLat = 23.0
    val maxLat = 28.0

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NavyDark)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(locations) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height
                        // Check if tap is near any zone center
                        locations.forEach { loc ->
                            val nx = ((loc.longitude - minLon) / (maxLon - minLon)).toFloat()
                            val ny = (1f - ((loc.latitude - minLat) / (maxLat - minLat))).toFloat()
                            val cx = nx * w
                            val cy = ny * h
                            val dx = tapOffset.x - cx
                            val dy = tapOffset.y - cy
                            if (dx * dx + dy * dy < 60f * 60f) {
                                onSelectZone(loc.id)
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // Helper to map lat/lon to Canvas pixels
            fun toScreenOffset(lat: Double, lon: Double): Offset {
                val nx = ((lon - minLon) / (maxLon - minLon)).coerceIn(0.05, 0.95).toFloat()
                val ny = (1f - ((lat - minLat) / (maxLat - minLat))).coerceIn(0.05, 0.95).toFloat()
                return Offset(nx * width, ny * height)
            }

            // 1. Draw subtle topography contour lines & mountain ridge shading
            val contourPath1 = Path().apply {
                moveTo(0f, height * 0.35f)
                cubicTo(width * 0.25f, height * 0.25f, width * 0.5f, height * 0.45f, width, height * 0.3f)
            }
            drawPath(contourPath1, color = Color(0xFF162942), style = Stroke(width = 2f))

            val contourPath2 = Path().apply {
                moveTo(0f, height * 0.6f)
                cubicTo(width * 0.3f, height * 0.7f, width * 0.7f, height * 0.5f, width, height * 0.65f)
            }
            drawPath(contourPath2, color = Color(0xFF162942), style = Stroke(width = 2f))

            // 2. Draw Roads (NH-10, NH-29) if enabled
            if (mapLayers.roads) {
                roads.forEach { road ->
                    val isNh10 = road.id.contains("nh10")
                    val startPt = if (isNh10) toScreenOffset(27.35, 88.45) else toScreenOffset(25.55, 93.95)
                    val midPt = if (isNh10) toScreenOffset(27.50, 88.52) else toScreenOffset(25.67, 94.10)
                    val endPt = if (isNh10) toScreenOffset(27.65, 88.60) else toScreenOffset(25.75, 94.20)

                    // Main road
                    val roadColor = when (road.status) {
                        RoadStatus.BLOCKED -> RiskCritical
                        RoadStatus.UNSAFE -> RiskHigh
                        RoadStatus.OPEN -> RiskSafe
                    }
                    drawLine(
                        color = roadColor,
                        start = startPt,
                        end = midPt,
                        strokeWidth = 6f
                    )
                    drawLine(
                        color = roadColor,
                        start = midPt,
                        end = endPt,
                        strokeWidth = 6f
                    )

                    // Alternative safe detour path (green dashed)
                    val detourPt = if (isNh10) toScreenOffset(27.42, 88.65) else toScreenOffset(25.60, 94.18)
                    val detourPath = Path().apply {
                        moveTo(startPt.x, startPt.y)
                        quadraticTo(detourPt.x, detourPt.y, endPt.x, endPt.y)
                    }
                    drawPath(
                        path = detourPath,
                        color = RiskSafe,
                        style = Stroke(
                            width = 4f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                        )
                    )
                }
            }

            // 3. Draw Risk Zones
            if (mapLayers.riskZones) {
                locations.forEach { loc ->
                    val center = toScreenOffset(loc.latitude, loc.longitude)
                    val isSelected = loc.id == selectedZoneId
                    val zoneColor = when (loc.riskLevel) {
                        RiskLevel.CRITICAL -> RiskCritical
                        RiskLevel.HIGH -> RiskHigh
                        RiskLevel.MODERATE -> RiskModerate
                        RiskLevel.LOW -> RiskSafe
                    }

                    // Pulsing shockwave ring for Critical / High
                    if (loc.riskLevel == RiskLevel.CRITICAL || loc.riskLevel == RiskLevel.HIGH) {
                        drawCircle(
                            color = zoneColor.copy(alpha = pulseAlpha),
                            radius = pulseRadius * (if (isSelected) 1.5f else 1.1f),
                            center = center
                        )
                    }

                    // Zone radius based on risk %
                    val baseRadius = 22f + (loc.riskPercentage * 0.15f)
                    drawCircle(
                        color = zoneColor.copy(alpha = if (isSelected) 0.35f else 0.18f),
                        radius = baseRadius,
                        center = center
                    )
                    drawCircle(
                        color = zoneColor,
                        radius = baseRadius,
                        center = center,
                        style = Stroke(width = if (isSelected) 3.5f else 2f)
                    )

                    // Inner solid pin
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = center
                    )
                    drawCircle(
                        color = zoneColor,
                        radius = 6f,
                        center = center
                    )
                }
            }

            // 4. Draw Sensors if enabled
            if (mapLayers.sensors) {
                sensors.forEach { sensor ->
                    val pos = toScreenOffset(sensor.latitude, sensor.longitude)
                    val sensorColor = when (sensor.status) {
                        SensorStatus.ONLINE -> CyanAccent
                        SensorStatus.WARNING -> RiskModerate
                        SensorStatus.CRITICAL -> RiskCritical
                        SensorStatus.OFFLINE -> Color.Gray
                    }
                    // Small triangle/diamond for sensor
                    val sPath = Path().apply {
                        moveTo(pos.x, pos.y - 8f)
                        lineTo(pos.x + 7f, pos.y + 6f)
                        lineTo(pos.x - 7f, pos.y + 6f)
                        close()
                    }
                    drawPath(sPath, color = sensorColor)
                }
            }

            // 5. Draw Evacuation Shelters if enabled
            if (mapLayers.shelters) {
                locations.forEach { loc ->
                    loc.shelters.forEach { shelter ->
                        val pos = toScreenOffset(shelter.latitude, shelter.longitude)
                        drawCircle(
                            color = Color(0xFF3B82F6),
                            radius = 5f,
                            center = pos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f,
                            center = pos
                        )
                    }
                }
            }
        }

        // Top-left live telemetry status badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NavySurface.copy(alpha = 0.90f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(RiskCritical)
            )
            Text(
                text = "GIS LIVE RADAR • HIMALAYAN CORRIDOR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        }

        // Selected Zone Quick Chip
        val currentSelected = locations.find { it.id == selectedZoneId } ?: locations.firstOrNull()
        if (currentSelected != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .widthIn(max = 280.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface.copy(alpha = 0.94f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentSelected.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                        val chipColor = when (currentSelected.riskLevel) {
                            RiskLevel.CRITICAL -> RiskCritical
                            RiskLevel.HIGH -> RiskHigh
                            RiskLevel.MODERATE -> RiskModerate
                            RiskLevel.LOW -> RiskSafe
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = chipColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${currentSelected.riskPercentage}% ${currentSelected.riskLevel.name}",
                                color = chipColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currentSelected.district}, ${currentSelected.state} • Rain: ${currentSelected.rainfall.toInt()}mm • Soil: ${currentSelected.soilMoisture.toInt()}%",
                        fontSize = 10.sp,
                        color = Gray300
                    )
                }
            }
        }
    }
}
