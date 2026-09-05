package com.example.ui.gis

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GisBasemapType
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
    onBasemapChange: ((GisBasemapType) -> Unit)? = null,
    isHeroMode: Boolean = true
) {
    var activeBasemap by remember(mapLayers.basemapType) { mutableStateOf(mapLayers.basemapType) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isFullscreenOpen by remember { mutableStateOf(false) }

    val handleBasemapSwitch: (GisBasemapType) -> Unit = { newType ->
        activeBasemap = newType
        onBasemapChange?.invoke(newType)
    }

    Box(modifier = modifier) {
        GisMapContent(
            locations = locations,
            sensors = sensors,
            roads = roads,
            selectedZoneId = selectedZoneId,
            mapLayers = mapLayers,
            activeBasemap = activeBasemap,
            zoomScale = zoomScale,
            panOffset = panOffset,
            onZoomChange = { zoomScale = it },
            onPanChange = { panOffset = it },
            onSelectZone = onSelectZone,
            onBasemapSwitch = handleBasemapSwitch,
            onToggleFullscreen = { isFullscreenOpen = true },
            modifier = Modifier.fillMaxSize(),
            isFullscreen = false
        )
    }

    // Fullscreen Immersive Map Dialog
    if (isFullscreenOpen) {
        Dialog(
            onDismissRequest = { isFullscreenOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = NavyDark
            ) {
                GisMapContent(
                    locations = locations,
                    sensors = sensors,
                    roads = roads,
                    selectedZoneId = selectedZoneId,
                    mapLayers = mapLayers,
                    activeBasemap = activeBasemap,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                    onZoomChange = { zoomScale = it },
                    onPanChange = { panOffset = it },
                    onSelectZone = onSelectZone,
                    onBasemapSwitch = handleBasemapSwitch,
                    onToggleFullscreen = { isFullscreenOpen = false },
                    modifier = Modifier.fillMaxSize(),
                    isFullscreen = true
                )
            }
        }
    }
}

@Composable
private fun GisMapContent(
    locations: List<LocationZone>,
    sensors: List<SensorNode>,
    roads: List<RoadCorridor>,
    selectedZoneId: String,
    mapLayers: MapLayerConfig,
    activeBasemap: GisBasemapType,
    zoomScale: Float,
    panOffset: Offset,
    onZoomChange: (Float) -> Unit,
    onPanChange: (Offset) -> Unit,
    onSelectZone: (String) -> Unit,
    onBasemapSwitch: (GisBasemapType) -> Unit,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false
) {
    val textMeasurer = rememberTextMeasurer()
    var isWeatherActive by remember { mutableStateOf(mapLayers.weatherOverlay) }

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

    // Radar beam sweep rotation
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )

    // Coords mapping helper for North-East India bounding box:
    // Longitude 88.0° to 95.0° (West to East)
    // Latitude 23.0° to 28.5° (South to North)
    val minLon = 88.0
    val maxLon = 95.0
    val minLat = 23.0
    val maxLat = 28.5

    Box(
        modifier = modifier
            .clip(if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp))
            .background(
                when (activeBasemap) {
                    GisBasemapType.TOPOGRAPHY -> Color(0xFF1E281C)
                    GisBasemapType.SATELLITE -> Color(0xFF0D1D13)
                    GisBasemapType.RADAR -> NavyDark
                }
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(locations, zoomScale, panOffset) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val cx = w / 2f
                        val cy = h / 2f
                        locations.forEach { loc ->
                            val nx = ((loc.longitude - minLon) / (maxLon - minLon)).coerceIn(0.02, 0.98).toFloat()
                            val ny = (1f - ((loc.latitude - minLat) / (maxLat - minLat))).coerceIn(0.02, 0.98).toFloat()
                            val rawX = nx * w
                            val rawY = ny * h
                            val sx = cx + (rawX - cx) * zoomScale + panOffset.x
                            val sy = cy + (rawY - cy) * zoomScale + panOffset.y
                            val dx = tapOffset.x - sx
                            val dy = tapOffset.y - sy
                            if (dx * dx + dy * dy < 50f * 50f) {
                                onSelectZone(loc.id)
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (zoomScale * zoom).coerceIn(0.75f, 4.0f)
                        onZoomChange(newScale)
                        val maxPanX = size.width * (newScale - 0.5f).coerceAtLeast(0.5f)
                        val maxPanY = size.height * (newScale - 0.5f).coerceAtLeast(0.5f)
                        onPanChange(
                            Offset(
                                (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                                (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                            )
                        )
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f

            // Helper to map lat/lon to Canvas pixels with pan and zoom applied
            fun toScreenOffset(lat: Double, lon: Double): Offset {
                val nx = ((lon - minLon) / (maxLon - minLon)).coerceIn(0.02, 0.98).toFloat()
                val ny = (1f - ((lat - minLat) / (maxLat - minLat))).coerceIn(0.02, 0.98).toFloat()
                val rawX = nx * width
                val rawY = ny * height
                val sx = cx + (rawX - cx) * zoomScale + panOffset.x
                val sy = cy + (rawY - cy) * zoomScale + panOffset.y
                return Offset(sx, sy)
            }

            // ════════════════════════════════════════════════════════════════
            // 1. BASEMAP RENDERING (Topography vs Satellite vs Radar)
            // ════════════════════════════════════════════════════════════════
            when (activeBasemap) {
                GisBasemapType.TOPOGRAPHY -> {
                    drawTopographyBasemap(
                        width = width,
                        height = height,
                        toScreenOffset = ::toScreenOffset,
                        textMeasurer = textMeasurer
                    )
                }
                GisBasemapType.SATELLITE -> {
                    drawSatelliteBasemap(
                        width = width,
                        height = height,
                        toScreenOffset = ::toScreenOffset,
                        textMeasurer = textMeasurer
                    )
                }
                GisBasemapType.RADAR -> {
                    drawRadarBasemap(
                        width = width,
                        height = height,
                        radarAngle = radarAngle,
                        toScreenOffset = ::toScreenOffset,
                        textMeasurer = textMeasurer
                    )
                }
            }

            // ════════════════════════════════════════════════════════════════
            // 2. HIGHWAY CORRIDORS (Safe = Green, Medium = Orange, Danger = Red)
            // ════════════════════════════════════════════════════════════════
            if (mapLayers.roads) {
                roads.forEach { road ->
                    val roadColor = when (road.status) {
                        RoadStatus.OPEN -> RoadSafeGreen
                        RoadStatus.UNSAFE -> RoadMediumOrange
                        RoadStatus.BLOCKED -> RoadDangerRed
                    }

                    // Multi-point coordinates for major Himalayan highway networks
                    val waypoints: List<Pair<Double, Double>> = when {
                        road.id.contains("nh10") -> listOf(
                            26.85 to 88.42, // Siliguri
                            27.05 to 88.43, // Teesta
                            27.20 to 88.48, // 29th Mile Block Point
                            27.33 to 88.61, // Gangtok
                            27.51 to 88.53  // Mangan
                        )
                        road.id.contains("nh29") -> listOf(
                            25.90 to 93.73, // Dimapur
                            25.75 to 93.85, // Medziphema
                            25.68 to 94.02, // Zubza Crack Point
                            25.67 to 94.11  // Kohima
                        )
                        road.id.contains("nh310") -> listOf(
                            27.33 to 88.61, // Gangtok
                            27.38 to 88.72, // Karponang
                            27.43 to 88.83  // Nathu La Pass
                        )
                        road.id.contains("nh54") -> listOf(
                            25.75 to 93.18, // Lumding
                            25.30 to 93.15, // Maibang
                            25.17 to 93.02, // Haflong
                            24.83 to 92.80  // Silchar
                        )
                        road.id.contains("sh8") -> listOf(
                            27.51 to 88.53, // Mangan
                            27.38 to 88.54, // Dikchu
                            27.23 to 88.50  // Singtam
                        )
                        road.id.contains("lava") -> listOf(
                            27.08 to 88.66, // Lava
                            27.15 to 88.62, // Reshi
                            27.25 to 88.60, // Rhenock
                            27.33 to 88.61  // Gangtok
                        )
                        else -> listOf(
                            27.25 to 88.40,
                            27.40 to 88.55,
                            27.55 to 88.70
                        )
                    }

                    val screenPts = waypoints.map { toScreenOffset(it.first, it.second) }

                    // Outer casing for contrast on terrain & satellite
                    for (i in 0 until screenPts.size - 1) {
                        drawLine(
                            color = Color.Black.copy(alpha = 0.75f),
                            start = screenPts[i],
                            end = screenPts[i + 1],
                            strokeWidth = 10f * zoomScale.coerceIn(0.8f, 1.4f)
                        )
                    }

                    // Main highway stroke with exact Safe=Green, Medium=Orange, Danger=Red
                    for (i in 0 until screenPts.size - 1) {
                        drawLine(
                            color = roadColor,
                            start = screenPts[i],
                            end = screenPts[i + 1],
                            strokeWidth = 6.5f * zoomScale.coerceIn(0.8f, 1.4f)
                        )
                    }

                    // Dashed centerline for open or medium roads
                    if (road.status != RoadStatus.BLOCKED) {
                        for (i in 0 until screenPts.size - 1) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.85f),
                                start = screenPts[i],
                                end = screenPts[i + 1],
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                            )
                        }
                    }

                    // Road status label tag at midpoint
                    if (screenPts.size >= 2) {
                        val midIndex = screenPts.size / 2
                        val labelPt = screenPts[midIndex]

                        // Hazard icon / Blocker indicator on Danger (Red) roads
                        if (road.status == RoadStatus.BLOCKED) {
                            drawCircle(
                                color = RoadDangerRed.copy(alpha = pulseAlpha),
                                radius = pulseRadius * 1.3f,
                                center = labelPt
                            )
                            drawCircle(
                                color = RoadDangerRed,
                                radius = 9f,
                                center = labelPt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4f,
                                center = labelPt
                            )
                        } else if (road.status == RoadStatus.UNSAFE) {
                            // Medium (Orange) caution ring
                            drawCircle(
                                color = RoadMediumOrange,
                                radius = 7f,
                                center = labelPt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = labelPt
                            )
                        }

                        // Highway status pill
                        val statusText = when (road.status) {
                            RoadStatus.BLOCKED -> "🔴 ${road.name.split(" ").first()}: DANGER (BLOCKED)"
                            RoadStatus.UNSAFE -> "🟠 ${road.name.split(" ").first()}: MEDIUM (CAUTION)"
                            RoadStatus.OPEN -> "🟢 ${road.name.split(" ").first()}: SAFE"
                        }
                        val pillWidth = 110f * zoomScale.coerceIn(0.85f, 1.2f)
                        val pillHeight = 18f * zoomScale.coerceIn(0.85f, 1.2f)

                        drawRoundRect(
                            color = NavyDark.copy(alpha = 0.90f),
                            topLeft = Offset(labelPt.x - pillWidth / 2, labelPt.y - 24f * zoomScale),
                            size = Size(pillWidth, pillHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = roadColor,
                            topLeft = Offset(labelPt.x - pillWidth / 2, labelPt.y - 24f * zoomScale),
                            size = Size(pillWidth, pillHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                            style = Stroke(width = 1.2f)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = statusText,
                            topLeft = Offset(labelPt.x - pillWidth / 2 + 4f, labelPt.y - 23f * zoomScale),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (7.5f * zoomScale.coerceIn(0.85f, 1.15f)).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                    }
                }
            }

            // ════════════════════════════════════════════════════════════════
            // 3. RISK ZONES (Polygons, Pulsing shockwaves, Pin markers)
            // ════════════════════════════════════════════════════════════════
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
                            radius = pulseRadius * (if (isSelected) 1.6f else 1.2f),
                            center = center
                        )
                    }

                    // Zone radius based on risk %
                    val baseRadius = (22f + (loc.riskPercentage * 0.16f)) * (if (isSelected) 1.25f else 1.0f)
                    drawCircle(
                        color = zoneColor.copy(alpha = if (isSelected) 0.40f else 0.22f),
                        radius = baseRadius,
                        center = center
                    )
                    drawCircle(
                        color = zoneColor,
                        radius = baseRadius,
                        center = center,
                        style = Stroke(width = if (isSelected) 3.5f else 2f)
                    )

                    // Inner pin
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

                    // Zone Name label above pin
                    val labelText = loc.name.split("-").first().trim()
                    drawText(
                        textMeasurer = textMeasurer,
                        text = labelText,
                        topLeft = Offset(center.x - 30f, center.y + baseRadius + 4f),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            background = Color.Black.copy(alpha = 0.55f)
                        )
                    )
                }
            }

            // ════════════════════════════════════════════════════════════════
            // 4. SENSORS if enabled
            // ════════════════════════════════════════════════════════════════
            if (mapLayers.sensors) {
                sensors.forEach { sensor ->
                    val pos = toScreenOffset(sensor.latitude, sensor.longitude)
                    val sensorColor = when (sensor.status) {
                        SensorStatus.ONLINE -> CyanAccent
                        SensorStatus.WARNING -> RiskModerate
                        SensorStatus.CRITICAL -> RiskCritical
                        SensorStatus.OFFLINE -> Color.Gray
                    }
                    val sPath = Path().apply {
                        moveTo(pos.x, pos.y - 9f)
                        lineTo(pos.x + 8f, pos.y + 7f)
                        lineTo(pos.x - 8f, pos.y + 7f)
                        close()
                    }
                    drawPath(sPath, color = sensorColor)
                }
            }

            // ════════════════════════════════════════════════════════════════
            // 5. SHELTERS if enabled
            // ════════════════════════════════════════════════════════════════
            if (mapLayers.shelters) {
                locations.forEach { loc ->
                    loc.shelters.forEach { shelter ->
                        val pos = toScreenOffset(shelter.latitude, shelter.longitude)
                        drawCircle(
                            color = Color(0xFF3B82F6),
                            radius = 6f,
                            center = pos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = pos
                        )
                    }
                }
            }

            // ════════════════════════════════════════════════════════════════
            // 6. WEATHER IN GIS MAP (Precipitation Radar, Cloud Cells, Stations, Wind)
            // ════════════════════════════════════════════════════════════════
            if (isWeatherActive) {
                // Precipitation radar reflectivity cores
                locations.forEach { loc ->
                    val center = toScreenOffset(loc.latitude, loc.longitude)
                    if (loc.rainfall > 70.0) {
                        val severity = (loc.rainfall / 220.0).toFloat().coerceIn(0.4f, 1.2f)
                        val baseRadius = 42f * zoomScale * severity

                        // Outer rain drizzle halo (Cyan)
                        drawCircle(
                            color = Color(0x3300E5FF),
                            radius = baseRadius * 1.5f,
                            center = center
                        )
                        // Mid heavy rain band (Lime/Yellow)
                        drawCircle(
                            color = Color(0x4484CC16),
                            radius = baseRadius * 1.1f,
                            center = center
                        )
                        // Intense core downpour (Orange/Red)
                        if (loc.rainfall > 140.0) {
                            drawCircle(
                                color = Color(0x55EF4444),
                                radius = baseRadius * 0.7f,
                                center = center
                            )
                        }
                    }
                }

                // Dynamic falling rain streaks over high-precipitation storm zones
                val rainStep = (pulseRadius * 2.5f) % 22f
                locations.filter { it.rainfall > 90.0 }.forEach { stormLoc ->
                    val center = toScreenOffset(stormLoc.latitude, stormLoc.longitude)
                    for (i in -3..3) {
                        val rx = center.x + (i * 14f * zoomScale)
                        val ry = center.y - 28f + ((i * 5f + rainStep) % 38f)
                        drawLine(
                            color = Color(0x8838BDF8),
                            start = Offset(rx, ry),
                            end = Offset(rx - 4f, ry + 11f),
                            strokeWidth = 1.6f
                        )
                    }
                }

                // Weather Station floating badges pinned to each zone
                locations.forEach { loc ->
                    val pos = toScreenOffset(loc.latitude, loc.longitude)
                    val wOffset = Offset(pos.x + 18f * zoomScale, pos.y - 36f * zoomScale)

                    val weatherEmoji = when {
                        loc.weatherCondition.contains("Lightning", ignoreCase = true) || loc.weatherCondition.contains("Severe", ignoreCase = true) -> "⛈️"
                        loc.weatherCondition.contains("Torrential", ignoreCase = true) || loc.rainfall > 150.0 -> "🌧️"
                        loc.weatherCondition.contains("Heavy", ignoreCase = true) -> "🌧️"
                        loc.weatherCondition.contains("Showers", ignoreCase = true) -> "🌦️"
                        else -> "⛅"
                    }

                    val rainBadgeBorder = when {
                        loc.rainfall > 150.0 -> RoadDangerRed
                        loc.rainfall > 70.0 -> RoadMediumOrange
                        else -> RoadSafeGreen
                    }

                    val bWidth = 118f * zoomScale.coerceIn(0.85f, 1.25f)
                    val bHeight = 44f * zoomScale.coerceIn(0.85f, 1.25f)

                    // Weather station badge card
                    drawRoundRect(
                        color = NavyDark.copy(alpha = 0.92f),
                        topLeft = wOffset,
                        size = Size(bWidth, bHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = rainBadgeBorder,
                        topLeft = wOffset,
                        size = Size(bWidth, bHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                        style = Stroke(width = 1.4f)
                    )

                    // Line 1: Weather emoji + Temp + Rain mm
                    val line1 = "$weatherEmoji ${loc.weatherTemp}°C | ${loc.rainfall.toInt()}mm"
                    drawText(
                        textMeasurer = textMeasurer,
                        text = line1,
                        topLeft = Offset(wOffset.x + 5f, wOffset.y + 3f),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (9.5f * zoomScale.coerceIn(0.85f, 1.2f)).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )

                    // Line 2: Wind snippet & condition
                    val line2 = "💨 ${loc.weatherWind}"
                    drawText(
                        textMeasurer = textMeasurer,
                        text = line2,
                        topLeft = Offset(wOffset.x + 5f, wOffset.y + 22f),
                        style = TextStyle(
                            color = CyanAccent,
                            fontSize = (8.5f * zoomScale.coerceIn(0.85f, 1.2f)).sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }
        }

        // ════════════════════════════════════════════════════════════════
        // FLOATING CONTROLS: BASEMAP SWITCHER & WEATHER TOGGLE (TOP-RIGHT)
        // ════════════════════════════════════════════════════════════════
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            shape = RoundedCornerShape(20.dp),
            color = NavyDark.copy(alpha = 0.94f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray700),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GisBasemapType.values().forEach { mode ->
                    val isSelected = activeBasemap == mode
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) BlueAccent else Color.Transparent,
                        modifier = Modifier.clickable { onBasemapSwitch(mode) }
                    ) {
                        Text(
                            text = mode.iconLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Gray300,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }

                // Weather overlay toggle chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isWeatherActive) CyanAccent.copy(alpha = 0.25f) else Color.Transparent,
                    border = if (isWeatherActive) androidx.compose.foundation.BorderStroke(1.dp, CyanAccent) else null,
                    modifier = Modifier.clickable { isWeatherActive = !isWeatherActive }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = if (isWeatherActive) "🌦️ Weather ON" else "🌦️ Weather OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWeatherActive) CyanAccent else Gray400
                        )
                    }
                }
            }
        }

        // ════════════════════════════════════════════════════════════════
        // FLOATING CONTROLS: TELEMETRY BADGE (TOP-LEFT)
        // ════════════════════════════════════════════════════════════════
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NavySurface.copy(alpha = 0.92f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (activeBasemap == GisBasemapType.RADAR) RiskCritical else CyanAccent)
            )
            Text(
                text = when (activeBasemap) {
                    GisBasemapType.TOPOGRAPHY -> "⛰️ TOPOGRAPHIC CONTOURS • 200m"
                    GisBasemapType.SATELLITE -> "🛰️ SENTINEL-2 MSI • 10m HIGH-RES"
                    GisBasemapType.RADAR -> "📡 LIVE RADAR • HIMALAYAN SECTOR"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.4.sp
            )
        }

        // ════════════════════════════════════════════════════════════════
        // FLOATING CONTROLS: ZOOM / PAN / FULLSCREEN BAR (RIGHT MIDDLE)
        // ════════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Zoom in button
            Surface(
                shape = CircleShape,
                color = NavySurface.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray700),
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onZoomChange((zoomScale + 0.3f).coerceAtMost(4.0f)) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Zoom out button
            Surface(
                shape = CircleShape,
                color = NavySurface.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray700),
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onZoomChange((zoomScale - 0.3f).coerceAtLeast(0.75f)) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Reset Center / Zoom button
            Surface(
                shape = CircleShape,
                color = NavySurface.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray700),
                modifier = Modifier
                    .size(36.dp)
                    .clickable {
                        onZoomChange(1.0f)
                        onPanChange(Offset.Zero)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Reset View", tint = CyanAccent, modifier = Modifier.size(18.dp))
                }
            }

            // Fullscreen toggle button
            Surface(
                shape = CircleShape,
                color = if (isFullscreen) RiskCritical else NavySurface.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray700),
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onToggleFullscreen() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isFullscreen) Icons.Default.Close else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Expand Fullscreen Map",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ════════════════════════════════════════════════════════════════
        // SELECTED ZONE QUICK METRIC CARD (BOTTOM-LEFT)
        // ════════════════════════════════════════════════════════════════
        val currentSelected = locations.find { it.id == selectedZoneId } ?: locations.firstOrNull()
        if (currentSelected != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .widthIn(max = 290.dp),
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
                            text = currentSelected.name.split("-").first().trim(),
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
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${currentSelected.district}, ${currentSelected.state} • 🌡️ ${currentSelected.weatherTemp}°C (${currentSelected.weatherCondition})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🌧️ Rain: ${currentSelected.rainfall.toInt()}mm • 💨 Wind: ${currentSelected.weatherWind} • Soil: ${currentSelected.soilMoisture.toInt()}%",
                        fontSize = 9.5.sp,
                        color = Gray300
                    )
                }
            }
        }

        // ════════════════════════════════════════════════════════════════
        // FLOATING ROAD STATUS LEGEND (SAFE: GREEN, MEDIUM: ORANGE, DANGER: RED)
        // ════════════════════════════════════════════════════════════════
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(20.dp),
            color = NavyDark.copy(alpha = 0.94f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray700),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ROADS:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray400
                )
                // Safe (Green)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RoadSafeGreen))
                    Text("Safe (Green)", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = RoadSafeGreenLight)
                }
                // Medium (Orange)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RoadMediumOrange))
                    Text("Medium (Orange)", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = RoadMediumOrangeLight)
                }
                // Danger (Red)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RoadDangerRed))
                    Text("Danger (Red)", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = RoadDangerRedLight)
                }
            }
        }

        // Scale bar & Datum in bottom-right
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.60f))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "0 ━━ 10km • WGS84",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Gray300
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// TOPOGRAPHY BASEMAP DRAWING HELPER
// ═════════════════════════════════════════════════════════════════════════════
private fun DrawScope.drawTopographyBasemap(
    width: Float,
    height: Float,
    toScreenOffset: (Double, Double) -> Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    // 1. Hypsometric relief elevation gradient background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF282730), // High northern shale ridge (3,500m+)
                Color(0xFF382F22), // High alpine escarpment (2,500m - 3,500m)
                Color(0xFF343324), // Mid mountain slope (1,500m - 2,500m)
                Color(0xFF233221), // Foothill evergreen zone (1,000m - 1,500m)
                Color(0xFF1B2B1B)  // Low river valley floor (< 1,000m)
            )
        )
    )

    // 2. Intermediate fine contour lines (every 250m)
    val contourFineColor = Color(0xFF6B5C4B).copy(alpha = 0.45f)
    for (i in 1..8) {
        val yBase = height * (0.10f + i * 0.10f)
        val p = Path().apply {
            moveTo(0f, yBase)
            cubicTo(
                width * 0.22f, yBase - 28f,
                width * 0.48f, yBase + 34f,
                width * 0.76f, yBase - 18f
            )
            quadraticTo(width * 0.90f, yBase + 12f, width, yBase)
        }
        drawPath(p, color = contourFineColor, style = Stroke(width = 1f))
    }

    // 3. Bold index contour lines (1,000m, 2,000m, 3,000m, 4,000m)
    val indexColor = Color(0xFFC7A57E)
    val indexContours = listOf(
        Pair(0.20f, "3,500m"),
        Pair(0.38f, "2,800m"),
        Pair(0.55f, "2,000m"),
        Pair(0.72f, "1,400m")
    )
    indexContours.forEach { (fraction, label) ->
        val yBase = height * fraction
        val path = Path().apply {
            moveTo(0f, yBase)
            cubicTo(
                width * 0.28f, yBase - 36f,
                width * 0.58f, yBase + 42f,
                width * 0.82f, yBase - 22f
            )
            quadraticTo(width * 0.92f, yBase + 16f, width, yBase - 8f)
        }
        drawPath(path, color = indexColor, style = Stroke(width = 2.2f))

        // Elevation contour label text
        drawText(
            textMeasurer = textMeasurer,
            text = label,
            topLeft = Offset(width * 0.18f, yBase - 22f),
            style = TextStyle(
                color = indexColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                background = Color.Black.copy(alpha = 0.5f)
            )
        )
    }

    // 4. Closed summit knoll contour loops (mountain crests)
    val summits = listOf(
        Pair(toScreenOffset(27.80, 88.55), "▲ Kanchenjunga Spur 4,280m"),
        Pair(toScreenOffset(27.58, 88.58), "▲ Mangan Crest 2,450m"),
        Pair(toScreenOffset(27.48, 88.42), "▲ Dzongu Peak 2,890m"),
        Pair(toScreenOffset(27.28, 88.52), "▲ Dikchu Ridge 1,840m"),
        Pair(toScreenOffset(25.25, 93.10), "▲ Haflong Ridge 1,680m"),
        Pair(toScreenOffset(25.70, 94.15), "▲ Kohima Saddle 1,440m")
    )

    summits.forEach { (pos, name) ->
        // Concentric summit ring contours
        drawCircle(color = indexColor.copy(alpha = 0.5f), radius = 22f, center = pos, style = Stroke(1.5f))
        drawCircle(color = indexColor.copy(alpha = 0.7f), radius = 13f, center = pos, style = Stroke(1.5f))
        // Peak glyph
        drawCircle(color = Color(0xFFFFD54F), radius = 3.5f, center = pos)

        drawText(
            textMeasurer = textMeasurer,
            text = name,
            topLeft = Offset(pos.x - 35f, pos.y - 18f),
            style = TextStyle(
                color = Color(0xFFFFE082),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                background = Color.Black.copy(alpha = 0.6f)
            )
        )
    }

    // 5. River valley drainage line (Teesta River Gorge)
    val riverPath = Path().apply {
        val p1 = toScreenOffset(27.85, 88.62)
        val p2 = toScreenOffset(27.55, 88.54)
        val p3 = toScreenOffset(27.35, 88.50)
        val p4 = toScreenOffset(27.10, 88.48)
        moveTo(p1.x, p1.y)
        cubicTo(p2.x - 20f, p2.y, p2.x + 10f, p3.y, p3.x, p3.y)
        quadraticTo(p3.x - 15f, (p3.y + p4.y) / 2f, p4.x, p4.y)
    }
    drawPath(riverPath, color = Color(0xFF388E3C).copy(alpha = 0.7f), style = Stroke(width = 3.5f))
}

// ═════════════════════════════════════════════════════════════════════════════
// SATELLITE BASEMAP DRAWING HELPER
// ═════════════════════════════════════════════════════════════════════════════
private fun DrawScope.drawSatelliteBasemap(
    width: Float,
    height: Float,
    toScreenOffset: (Double, Double) -> Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    // 1. Base dark forest canopy satellite gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF13191F), // High alpine granite moraine
                Color(0xFF0F2618), // Deep sub-alpine conifer forest
                Color(0xFF132F1E), // Mid-altitude broadleaf canopy
                Color(0xFF183B25), // Subtropical foothills
                Color(0xFF1F442E)  // River valley vegetation
            )
        )
    )

    // 2. Snow / Glacier fields on northern Himalayan peaks (Sikkim high range)
    val snowRidge = Path().apply {
        moveTo(0f, 0f)
        lineTo(width, 0f)
        lineTo(width, height * 0.18f)
        cubicTo(
            width * 0.75f, height * 0.22f,
            width * 0.50f, height * 0.12f,
            width * 0.25f, height * 0.20f
        )
        lineTo(0f, height * 0.15f)
        close()
    }
    drawPath(snowRidge, color = Color(0xFFD6E4ED).copy(alpha = 0.85f))
    drawPath(snowRidge, color = Color(0xFFFFFFFF).copy(alpha = 0.5f), style = Stroke(2f))

    // 3. Bare rock alpine granite scree bands on steep ridges
    val rockBand = Path().apply {
        moveTo(0f, height * 0.15f)
        cubicTo(
            width * 0.35f, height * 0.24f,
            width * 0.65f, height * 0.18f,
            width, height * 0.22f
        )
        lineTo(width, height * 0.28f)
        cubicTo(
            width * 0.60f, height * 0.24f,
            width * 0.30f, height * 0.30f,
            0f, height * 0.21f
        )
        close()
    }
    drawPath(rockBand, color = Color(0xFF383C42).copy(alpha = 0.75f))

    // 4. Glacial river network (Teesta River system & tributaries)
    // Main Teesta River branch
    val teestaPath = Path().apply {
        val s1 = toScreenOffset(27.85, 88.62)
        val s2 = toScreenOffset(27.60, 88.56)
        val s3 = toScreenOffset(27.45, 88.51)
        val s4 = toScreenOffset(27.20, 88.48)
        val s5 = toScreenOffset(26.85, 88.45)
        moveTo(s1.x, s1.y)
        cubicTo(s2.x + 15f, s2.y - 10f, s2.x - 10f, s3.y - 20f, s3.x, s3.y)
        cubicTo(s3.x - 20f, s4.y - 15f, s4.x + 10f, s4.y + 10f, s5.x, s5.y)
    }
    // Water halo & casing
    drawPath(teestaPath, color = Color(0xFF0F324D), style = Stroke(width = 7f))
    // Silt-laden glacial blue river channel
    drawPath(teestaPath, color = Color(0xFF3880A8), style = Stroke(width = 4.5f))

    // Tributary river branch (Dikchu Chu)
    val dikchuPath = Path().apply {
        val d1 = toScreenOffset(27.42, 88.68)
        val d2 = toScreenOffset(27.45, 88.51)
        moveTo(d1.x, d1.y)
        quadraticTo((d1.x + d2.x) / 2f + 10f, (d1.y + d2.y) / 2f, d2.x, d2.y)
    }
    drawPath(dikchuPath, color = Color(0xFF3880A8), style = Stroke(width = 3f))

    // 5. Agricultural terrace hillside patches & settlement clusters
    val settlements = listOf(
        toScreenOffset(27.51, 88.53), // Mangan
        toScreenOffset(27.33, 88.61), // Gangtok
        toScreenOffset(27.59, 88.64), // Chungthang
        toScreenOffset(25.17, 93.02)  // Haflong
    )
    settlements.forEach { pt ->
        drawCircle(color = Color(0xFF425540).copy(alpha = 0.8f), radius = 18f, center = pt)
        drawCircle(color = Color(0xFF5E6D5C).copy(alpha = 0.6f), radius = 10f, center = pt)
    }

    // 6. Orbital satellite HUD crosshairs (+) and lat/lon coordinate markers
    val reticleColor = Color(0xFF7CB342).copy(alpha = 0.40f)
    val crosshairPoints = listOf(
        Offset(width * 0.25f, height * 0.30f),
        Offset(width * 0.75f, height * 0.30f),
        Offset(width * 0.25f, height * 0.70f),
        Offset(width * 0.75f, height * 0.70f)
    )
    crosshairPoints.forEach { pt ->
        drawLine(color = reticleColor, start = Offset(pt.x - 8f, pt.y), end = Offset(pt.x + 8f, pt.y), strokeWidth = 1.5f)
        drawLine(color = reticleColor, start = Offset(pt.x, pt.y - 8f), end = Offset(pt.x, pt.y + 8f), strokeWidth = 1.5f)
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// RADAR GIS BASEMAP DRAWING HELPER
// ═════════════════════════════════════════════════════════════════════════════
private fun DrawScope.drawRadarBasemap(
    width: Float,
    height: Float,
    radarAngle: Float,
    toScreenOffset: (Double, Double) -> Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    // 1. Tactical deep cyber navy base
    drawRect(color = Color(0xFF070F22))

    // 2. Radar range rings centered on Mangan Sector
    val center = toScreenOffset(27.51, 88.53)
    val ringRads = listOf(60f, 130f, 210f, 300f)
    ringRads.forEachIndexed { idx, r ->
        drawCircle(
            color = CyanAccent.copy(alpha = 0.16f),
            radius = r,
            center = center,
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "${(idx + 1) * 15}km",
            topLeft = Offset(center.x + r + 4f, center.y - 8f),
            style = TextStyle(color = CyanAccent.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        )
    }

    // 3. Rotating radar sweep line
    val rad = Math.toRadians(radarAngle.toDouble())
    val sweepLength = 340f
    val sweepEnd = Offset(
        (center.x + sweepLength * cos(rad)).toFloat(),
        (center.y + sweepLength * sin(rad)).toFloat()
    )
    drawLine(
        color = CyanAccent.copy(alpha = 0.85f),
        start = center,
        end = sweepEnd,
        strokeWidth = 2f
    )

    // 4. Subtle tactical coordinate grid
    val gridColor = Color(0xFF132845)
    for (x in 0..6) {
        val gx = width * (x / 6f)
        drawLine(color = gridColor, start = Offset(gx, 0f), end = Offset(gx, height), strokeWidth = 1f)
    }
    for (y in 0..6) {
        val gy = height * (y / 6f)
        drawLine(color = gridColor, start = Offset(0f, gy), end = Offset(width, gy), strokeWidth = 1f)
    }
}
