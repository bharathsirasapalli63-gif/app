package com.example.ui.citizen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CitizenOnboardingView(
    selectedLanguage: String,
    onSelectLanguage: (String) -> Unit,
    onFinishOnboarding: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var locationGranted by remember { mutableStateOf(true) }
    var alertsGranted by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Step Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (step == index + 1) 32.dp else 12.dp)
                                .clip(CircleShape)
                                .background(if (step == index + 1) NavyMedium else Gray200)
                        )
                    }
                }

                when (step) {
                    1 -> {
                        // Step 1: Language selection
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(BlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Translate,
                                contentDescription = null,
                                tint = NavyMedium,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Choose Your Language",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "Select your preferred language for landslide disaster broadcasts in North-Eastern India.",
                            fontSize = 12.sp,
                            color = Gray600,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val langs = listOf(
                            "en" to "English (Default)",
                            "hi" to "हिंदी (Hindi)",
                            "as" to "অসমীয়া (Assamese)",
                            "bn" to "বাংলা (Bengali)",
                            "ne" to "नेपाली (Nepali)"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            langs.forEach { (code, label) ->
                                val isSelected = selectedLanguage == code
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) BlueLight else Gray50,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BlueAccent) else null,
                                    onClick = { onSelectLanguage(code) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) NavyDark else Gray800,
                                            fontSize = 13.sp
                                        )
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { step = 2 },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyMedium),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Continue", fontWeight = FontWeight.Bold)
                        }
                    }

                    2 -> {
                        // Step 2: Location & Emergency Alert Permissions
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(RiskSafeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = RiskSafe,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Geofenced Safety Alerts",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "TerraAlert monitors geological sensor nodes within your corridor to alert you before slope failure.",
                            fontSize = 12.sp,
                            color = Gray600,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Gray50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("GPS Mountain Proximity", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                        Text("Alerts when entering high-risk highway segments", fontSize = 11.sp, color = Gray600)
                                    }
                                    Switch(checked = locationGranted, onCheckedChange = { locationGranted = it })
                                }

                                Divider(color = Gray200)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("SSDMA Siren Overrides", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                                        Text("Audible warning during critical midnight evacuations", fontSize = 11.sp, color = Gray600)
                                    }
                                    Switch(checked = alertsGranted, onCheckedChange = { alertsGranted = it })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = { step = 3 },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyMedium),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Next", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    3 -> {
                        // Step 3: Emergency Protocol & Confirmation
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(RiskHighLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = RiskHigh,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Disaster Advisory Protocol",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "TerraAlert operates in coordination with the National Disaster Management Authority (NDMA) & GSI.",
                            fontSize = 12.sp,
                            color = Gray600,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BlueLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Early Warnings: Predictions are generated 4-6 hours in advance using rainfall thresholds and inclinometer slope telemetry.",
                                        fontSize = 11.sp,
                                        color = Gray800
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Emergency SOS: Always dial 112 or 1078 if trapped or witnessing an immediate catastrophic slope breach.",
                                        fontSize = 11.sp,
                                        color = Gray800
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onFinishOnboarding,
                            colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Text("Enter TerraAlert Portal", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
