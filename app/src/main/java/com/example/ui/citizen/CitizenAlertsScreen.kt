package com.example.ui.citizen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyAlert
import com.example.data.model.LocationZone
import com.example.data.model.RiskLevel
import com.example.ui.theme.*

@Composable
fun CitizenAlertsScreen(
    alerts: List<EmergencyAlert>,
    locations: List<LocationZone>,
    selectedZoneId: String,
    onOpenSos: () -> Unit
) {
    var playingAlertId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = RiskCritical, modifier = Modifier.size(32.dp))
                        Column {
                            Text(
                                text = "Emergency Broadcast Network",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Official NDMA • SSDMA • ASDMA Dispatches",
                                fontSize = 11.sp,
                                color = Gray300
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RiskCritical
                    ) {
                        Text(
                            text = "${alerts.size} ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Active Alerts List
        items(alerts) { alert ->
            val levelColor = when (alert.level) {
                RiskLevel.CRITICAL -> RiskCritical
                RiskLevel.HIGH -> RiskHigh
                RiskLevel.MODERATE -> RiskModerate
                RiskLevel.LOW -> RiskSafe
            }
            val levelBg = when (alert.level) {
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = levelBg
                        ) {
                            Text(
                                text = "${alert.level.name} • ${alert.probability}% PROBABILITY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = levelColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = alert.issuedAt,
                            fontSize = 11.sp,
                            color = Gray600,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = alert.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = alert.message,
                        fontSize = 12.sp,
                        color = Gray600,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Required Safety Action box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = levelBg.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = levelColor, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = "MANDATORY SAFETY ACTION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = levelColor
                                )
                                Text(
                                    text = alert.action,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray800
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Issued by: ${alert.issuedBy}",
                            fontSize = 10.sp,
                            color = Gray600
                        )

                        // Siren audio test button
                        OutlinedButton(
                            onClick = {
                                playingAlertId = if (playingAlertId == alert.id) null else alert.id
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = if (playingAlertId == alert.id) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = levelColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (playingAlertId == alert.id) "Siren Active" else "Test Siren",
                                fontSize = 10.sp,
                                color = levelColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Evacuation Shelters for the Selected Zone
        val zone = locations.find { it.id == selectedZoneId } ?: locations.first()
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
                        Icon(Icons.Default.HolidayVillage, contentDescription = null, tint = BlueAccent)
                        Column {
                            Text(
                                text = "Designated Relief Shelters & Camps",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = "Nearest safe gathering centers for ${zone.name}",
                                fontSize = 11.sp,
                                color = Gray600
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    zone.shelters.forEach { shelter ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Gray50,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(shelter.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                                    Text(
                                        text = "GPS: ${shelter.latitude}, ${shelter.longitude} • Managed by District Civil Defense",
                                        fontSize = 10.sp,
                                        color = Gray600
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BlueLight
                                ) {
                                    Text(
                                        text = "${shelter.occupied}/${shelter.capacity} Beds",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyMedium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
