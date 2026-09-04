package com.example.ui.citizen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CitizenReportEntity
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MapLayerConfig

@Composable
fun CitizenPortalView(
    activeTab: String,
    onTabSelect: (String) -> Unit,
    locations: List<LocationZone>,
    sensors: List<SensorNode>,
    roads: List<RoadCorridor>,
    alerts: List<EmergencyAlert>,
    citizenReports: List<CitizenReportEntity>,
    helplines: List<EmergencyHelpline>,
    selectedZoneId: String,
    mapLayers: MapLayerConfig,
    onSelectZone: (String) -> Unit,
    onToggle3D: () -> Unit,
    onOpenSos: () -> Unit,
    onSubmitReport: (hazardType: String, description: String, locationName: String, photoUrl: String?) -> Unit
) {
    val selectedZone = locations.find { it.id == selectedZoneId } ?: locations.first()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = NavyDark,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple("home", "Home", Icons.Default.Home),
                    Triple("alerts", "Alerts", Icons.Default.Notifications),
                    Triple("monitoring", "Sensors", Icons.Default.Sensors),
                    Triple("report", "Report", Icons.Default.Campaign),
                    Triple("guide", "Guide", Icons.Default.MenuBook)
                )

                navItems.forEach { (tabId, label, icon) ->
                    val isSelected = activeTab == tabId
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelect(tabId) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) CyanAccent else Gray300
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanAccent else Gray300
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = NavySurface
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "home" -> CitizenHomeScreen(
                    locations = locations,
                    sensors = sensors,
                    roads = roads,
                    selectedZoneId = selectedZoneId,
                    mapLayers = mapLayers,
                    onSelectZone = onSelectZone,
                    onToggle3D = onToggle3D,
                    onReportHazardClick = { onTabSelect("report") },
                    onNavigateToAlerts = { onTabSelect("alerts") }
                )
                "alerts" -> CitizenAlertsScreen(
                    alerts = alerts,
                    locations = locations,
                    selectedZoneId = selectedZoneId,
                    onOpenSos = onOpenSos
                )
                "monitoring" -> RiskMonitoringScreen(
                    zone = selectedZone,
                    sensors = sensors
                )
                "report" -> CitizenReportScreen(
                    citizenReports = citizenReports,
                    onSubmitReport = onSubmitReport
                )
                "guide" -> CitizenGuideScreen(
                    helplines = helplines
                )
                else -> CitizenHomeScreen(
                    locations = locations,
                    sensors = sensors,
                    roads = roads,
                    selectedZoneId = selectedZoneId,
                    mapLayers = mapLayers,
                    onSelectZone = onSelectZone,
                    onToggle3D = onToggle3D,
                    onReportHazardClick = { onTabSelect("report") },
                    onNavigateToAlerts = { onTabSelect("alerts") }
                )
            }
        }
    }
}
