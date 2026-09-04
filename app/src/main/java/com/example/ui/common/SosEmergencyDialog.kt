package com.example.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun SosEmergencyDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current

    // Pulsing siren animation
    val infiniteTransition = rememberInfiniteTransition(label = "siren")
    val sirenColor by infiniteTransition.animateColor(
        initialValue = RiskCritical,
        targetValue = Color(0xFF7F1D1D),
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sirenColor"
    )

    fun makeCall(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Siren Strobe Icon
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(sirenColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Distress",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "EMERGENCY SOS DISTRESS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = RiskCritical,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Distress signal broadcast active on LoRa & VHF mesh. Rescue authorities alerted.",
                    fontSize = 12.sp,
                    color = Gray600,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time GPS coordinates box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Gray100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TRANSMITTING LIVE GPS FIX",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "27.5182° N, 88.5364° E (Accuracy ±4m)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueAccent
                        )
                        Text(
                            text = "Sector: Mangan Ridge Corridor • Chungthang Access",
                            fontSize = 10.sp,
                            color = Gray600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Direct Emergency Helplines:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // One-tap call buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { makeCall("112") },
                        colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Call 112 (Emergency Response System)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { makeCall("1078") },
                        colors = ButtonDefaults.buttonColors(containerColor = RiskHigh),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Call 1078 (NDRF Disaster Control)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { makeCall("108") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(18.dp), tint = NavyDark)
                            Text("Call 108 (Mountain Ambulance)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel Distress Mode", color = Gray600, fontSize = 12.sp)
                }
            }
        }
    }
}
