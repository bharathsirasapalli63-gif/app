package com.example.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LocationZone
import com.example.ui.theme.*

@Composable
fun Terrain3dSheet(
    isOpen: Boolean,
    zone: LocationZone?,
    onDismiss: () -> Unit
) {
    if (!isOpen || zone == null) return

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
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = BlueAccent)
                        Column {
                            Text(
                                text = "Geological Sub-Surface Profile",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = zone.name,
                                fontSize = 11.sp,
                                color = Gray600
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Gray600)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Canvas showing mountain slope cross-section, rock strata & shear slip plane
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyDark)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // 1. Bedrock base layer (dark gray-blue)
                        val bedrockPath = Path().apply {
                            moveTo(0f, h)
                            lineTo(0f, h * 0.45f)
                            lineTo(w * 0.4f, h * 0.65f)
                            lineTo(w, h * 0.85f)
                            lineTo(w, h)
                            close()
                        }
                        drawPath(bedrockPath, color = Color(0xFF1E293B))

                        // 2. Weathered Colluvium / Sandstone-Shale overburden layer
                        val overburdenPath = Path().apply {
                            moveTo(0f, h * 0.45f)
                            lineTo(w * 0.35f, h * 0.2f)
                            lineTo(w * 0.85f, h * 0.55f)
                            lineTo(w, h * 0.85f)
                            lineTo(w * 0.4f, h * 0.65f)
                            close()
                        }
                        drawPath(overburdenPath, color = Color(0xFF475569))

                        // 3. Saturated groundwater table line
                        val waterPath = Path().apply {
                            moveTo(0f, h * 0.55f)
                            quadraticTo(w * 0.4f, h * 0.48f, w * 0.8f, h * 0.7f)
                        }
                        drawPath(
                            waterPath,
                            color = CyanAccent,
                            style = Stroke(
                                width = 3f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                            )
                        )

                        // 4. Critical Rotational Shear Failure Arc (Red dashed)
                        val slipPlane = Path().apply {
                            moveTo(w * 0.25f, h * 0.24f)
                            cubicTo(w * 0.45f, h * 0.35f, w * 0.65f, h * 0.62f, w * 0.8f, h * 0.58f)
                        }
                        drawPath(
                            slipPlane,
                            color = RiskCritical,
                            style = Stroke(width = 4f)
                        )

                        // Tension scarp crack marker at crest
                        drawCircle(color = RiskCritical, radius = 6f, center = Offset(w * 0.25f, h * 0.24f))
                        drawLine(
                            color = RiskCritical,
                            start = Offset(w * 0.25f, h * 0.24f),
                            end = Offset(w * 0.25f, h * 0.38f),
                            strokeWidth = 3f
                        )
                    }

                    // Water table and slip plane annotations
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "— Critical Shear Arc",
                                color = RiskCritical,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "- - Water Saturation",
                                color = CyanAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Geotechnical Factor of Safety (FoS) & Strata Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Gray100),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Factor of Safety (FoS)", fontSize = 10.sp, color = Gray600)
                            val fos = (1.0f - (zone.riskPercentage * 0.007f)).coerceAtLeast(0.68f)
                            Text(
                                text = String.format("%.2f", fos),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (fos < 1.0f) RiskCritical else RiskSafe
                            )
                            Text(
                                text = if (fos < 1.0f) "Unstable (FoS < 1.0)" else "Stable (FoS > 1.2)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (fos < 1.0f) RiskCritical else RiskSafe
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Gray100),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Slope Incline", fontSize = 10.sp, color = Gray600)
                            Text(
                                text = "${zone.slopeAngle.toInt()}° Incline",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text("Critical angle > 35°", fontSize = 9.sp, color = Gray600)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Geological description
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BlueLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Lithology & Shear Assessment",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${zone.terrainType}. Pore-water pressure has reached plastic limit under recent 24h precipitation, reducing basal shear resistance.",
                            fontSize = 11.sp,
                            color = Gray800,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
