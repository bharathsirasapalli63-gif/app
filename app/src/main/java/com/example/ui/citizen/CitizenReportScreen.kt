package com.example.ui.citizen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.CitizenReportEntity
import com.example.ui.theme.*

@Composable
fun CitizenReportScreen(
    citizenReports: List<CitizenReportEntity>,
    onSubmitReport: (hazardType: String, description: String, locationName: String, photoUrl: String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Landslide") }
    var locationInput by remember { mutableStateOf("Near Mangan Bridge, Chungthang Road, Sikkim") }
    var descriptionInput by remember { mutableStateOf("") }
    var photoAttached by remember { mutableStateOf(true) }
    var submittedId by remember { mutableStateOf<String?>(null) }

    val hazardCategories = listOf(
        "Landslide",
        "Ground Crack",
        "Road Blockage",
        "Rockfall",
        "Water Seepage"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Card
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
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = RiskHigh)
                        Column {
                            Text("Citizen Landslide & Hazard Report", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Text("Real-time dispatch to District EOC & Field Geologists", fontSize = 11.sp, color = Gray600)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Hazard Type Picker
                    Text("Hazard Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(hazardCategories) { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NavyMedium else Gray100,
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else Gray800,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. GPS Location
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text("Location Description / Landmark") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = RiskCritical)
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BlueLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(14.dp))
                            Text(
                                text = "GPS Locked: 27.518° N, 88.536° E (North Sikkim Sector)",
                                fontSize = 10.sp,
                                color = NavyDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Observations
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Hazard Description / Observations") },
                        placeholder = { Text("e.g. Boulders rolling down, tensile cracks expanding on slope...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Photo Evidence
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Gray100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gray300),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { photoAttached = !photoAttached }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (photoAttached) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = if (photoAttached) RiskSafe else Gray600
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (photoAttached) "Field Photo Attached (IMG_202604_SLOPE.JPG)" else "Attach Field Photo / Geo-Tag",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                Text(
                                    text = "Provides visual evidence for AI crack & debris estimation",
                                    fontSize = 10.sp,
                                    color = Gray600
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val desc = if (descriptionInput.isBlank()) "Observed active debris & rock displacement on hillside corridor." else descriptionInput
                            onSubmitReport(
                                selectedCategory,
                                desc,
                                locationInput,
                                if (photoAttached) "https://images.unsplash.com/photo-1506744038136-46273834b3fb" else null
                            )
                            descriptionInput = ""
                            submittedId = "CR-${(10250..99999).random()}"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Transmit Emergency Report", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Recent Community Hazard Reports Stream
        item {
            Text("Recent Community Reports", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
        }

        items(citizenReports) { report ->
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
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = RiskHigh, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${report.id} • ${report.hazardType}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NavyDark
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = report.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = report.description,
                        fontSize = 11.sp,
                        color = Gray800
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍 ${report.locationName} (${report.timestamp})",
                            fontSize = 10.sp,
                            color = Gray600
                        )
                        if (report.assignedOfficer != null) {
                            Text(
                                text = "👤 ${report.assignedOfficer}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BlueAccent
                            )
                        }
                    }
                }
            }
        }
    }
}
