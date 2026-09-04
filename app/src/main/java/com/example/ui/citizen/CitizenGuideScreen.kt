package com.example.ui.citizen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyHelpline
import com.example.ui.theme.*

@Composable
fun CitizenGuideScreen(
    helplines: List<EmergencyHelpline>
) {
    val context = LocalContext.current

    fun callPhone(num: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$num")
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Survival Guide Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("HIMALAYAN MOUNTAIN SURVIVAL PROTOCOL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 0.5.sp)
                    Text("Standard Operating Procedures for Landslides", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Formulated with NDMA, SDMA, and Geological Survey of India", fontSize = 11.sp, color = Gray300)
                }
            }
        }

        // Before Landslide
        item {
            SurvivalPhaseCard(
                phase = "1. BEFORE A LANDSLIDE (Preparedness)",
                color = RiskModerate,
                tips = listOf(
                    "Identify alternative escape paths away from stream channels and steep gullies.",
                    "Inspect surrounding hillsides for opening ground tension cracks, tilting utility poles, or muddy spring seepages.",
                    "Keep an Emergency Go-Bag ready with torch, battery radio, water purification tablets, and documents.",
                    "Listen to TerraAlert early warning sirens and rainfall alerts continuously."
                )
            )
        }

        // During Landslide
        item {
            SurvivalPhaseCard(
                phase = "2. DURING A LANDSLIDE (Immediate Action)",
                color = RiskCritical,
                tips = listOf(
                    "Evacuate immediately away from the slide path. Run perpendicular to the direction of debris flow.",
                    "If caught indoors and cannot escape: Curl into a tight ball and protect your head under heavy furniture.",
                    "Avoid river bottoms and low gorges; debris flows accelerate rapidly down mountain stream beds.",
                    "Be alert for sudden roaring rumble sounds or breaking tree branches upstream."
                )
            )
        }

        // After Landslide
        item {
            SurvivalPhaseCard(
                phase = "3. AFTER A LANDSLIDE (Recovery & Safety)",
                color = RiskSafe,
                tips = listOf(
                    "Stay away from the slide zone; secondary and retrogressive slope failures frequently occur.",
                    "Check for injured or trapped persons without entering the direct hazard area.",
                    "Report broken power lines, damaged culverts, and gas leaks to district emergency authorities.",
                    "Follow official road opening announcements before attempting highway travel."
                )
            )
        }

        // Emergency Helplines Section
        item {
            Text("Emergency Helplines Directory", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyDark)
        }

        items(helplines) { line ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { callPhone(line.number) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(line.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyDark)
                        Text(line.description, fontSize = 11.sp, color = Gray600)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Category: ${line.category}", fontSize = 10.sp, color = BlueAccent, fontWeight = FontWeight.SemiBold)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RiskCriticalLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = RiskCritical, modifier = Modifier.size(14.dp))
                            Text(line.number, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RiskCritical)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurvivalPhaseCard(
    phase: String,
    color: Color,
    tips: List<String>
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(phase, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Text(tip, fontSize = 12.sp, color = Gray800, lineHeight = 16.sp)
                }
            }
        }
    }
}
