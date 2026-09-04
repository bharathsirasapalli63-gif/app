package com.example.ui.fieldofficer

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
fun FieldOfficerScreen(
    tasks: List<FieldTask>,
    activeTaskId: String,
    onSelectTask: (String) -> Unit,
    onAdvanceWorkflow: (String) -> Unit,
    onSubmitInspection: (String, InspectionResult) -> Unit,
    onOpenSos: () -> Unit
) {
    val activeTask = tasks.find { it.id == activeTaskId } ?: tasks.firstOrNull()
    var fieldTab by remember { mutableStateOf("inspection") } // inspection, navigation, tasks

    // Geotechnical Form States
    var crackWidth by remember(activeTaskId) { mutableStateOf("18.0") }
    var slopeTilt by remember(activeTaskId) { mutableStateOf("43.0") }
    var rockfallSeverity by remember(activeTaskId) { mutableStateOf("High") }
    var seepageRate by remember(activeTaskId) { mutableStateOf("Rapid Turbid Flow") }
    var roadDamage by remember(activeTaskId) { mutableStateOf("Severe Structural Fissuring") }
    var fieldNotes by remember(activeTaskId) { mutableStateOf("Overburden colluvium is supersaturated. Toe shear displacement observed.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Field Officer Unit Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BlueAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text("FIELD GEOTECHNICAL UNIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 0.5.sp)
                                Text("FO-402 (T. Dorjee)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Sikkim State Disaster Response Force", fontSize = 10.sp, color = Gray300)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NavySurface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(RiskSafe))
                                Text("LoRa Link: -68 dBm", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active Task Card & Workflow Stepper
        if (activeTask != null) {
            item {
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
                                shape = RoundedCornerShape(4.dp),
                                color = when (activeTask.priority) {
                                    TaskPriority.CRITICAL -> RiskCriticalLight
                                    TaskPriority.HIGH -> RiskHighLight
                                    TaskPriority.MEDIUM -> BlueLight
                                }
                            ) {
                                Text(
                                    text = "${activeTask.priority.name} PRIORITY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (activeTask.priority) {
                                        TaskPriority.CRITICAL -> RiskCritical
                                        TaskPriority.HIGH -> RiskHigh
                                        TaskPriority.MEDIUM -> NavyMedium
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text("Deadline: ${activeTask.deadline}", fontSize = 11.sp, color = Gray600, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(activeTask.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📍 ${activeTask.locationName} (${activeTask.latitude}, ${activeTask.longitude})", fontSize = 11.sp, color = BlueAccent, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(activeTask.instructions, fontSize = 11.sp, color = Gray600)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Workflow Progress Stepper (Assigned -> Accepted -> Travelling -> On Site -> Inspection -> Reported -> Resolved)
                        Text("Operational Mission Stage:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gray600)
                        Spacer(modifier = Modifier.height(6.dp))

                        val steps = WorkflowStep.values()
                        val currentStepIndex = steps.indexOf(activeTask.status)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            steps.forEachIndexed { index, step ->
                                val isDone = index <= currentStepIndex
                                val isCurrent = index == currentStepIndex
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isDone) (if (isCurrent) RiskCritical else RiskSafe) else Gray200),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = step.label,
                                        fontSize = 8.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) NavyDark else Gray600,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Advance Workflow Button
                        if (activeTask.status != WorkflowStep.RESOLVED) {
                            Button(
                                onClick = { onAdvanceWorkflow(activeTask.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyMedium),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Advance to Next Stage", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sub Tabs: Inspection Form / Turn-by-Turn GPS / All Tasks
        item {
            TabRow(
                selectedTabIndex = when (fieldTab) {
                    "inspection" -> 0
                    "navigation" -> 1
                    "tasks" -> 2
                    else -> 0
                },
                containerColor = Color.White,
                contentColor = NavyDark
            ) {
                Tab(
                    selected = fieldTab == "inspection",
                    onClick = { fieldTab = "inspection" },
                    text = { Text("Geotech Form", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = fieldTab == "navigation",
                    onClick = { fieldTab = "navigation" },
                    text = { Text("GPS Nav Route", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = fieldTab == "tasks",
                    onClick = { fieldTab = "tasks" },
                    text = { Text("Task Queue (${tasks.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (fieldTab) {
            "inspection" -> {
                // Geotechnical Inspection Form
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("On-Site Geotechnical Telemetry Recording", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                            Text("Feeds directly into GSI slope stability algorithm", fontSize = 11.sp, color = Gray600)

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = crackWidth,
                                    onValueChange = { crackWidth = it },
                                    label = { Text("Crack Width (mm)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = slopeTilt,
                                    onValueChange = { slopeTilt = it },
                                    label = { Text("Slope Tilt (°)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = rockfallSeverity,
                                onValueChange = { rockfallSeverity = it },
                                label = { Text("Rockfall Severity (Low/Moderate/High)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = seepageRate,
                                onValueChange = { seepageRate = it },
                                label = { Text("Water Seepage Rate") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = roadDamage,
                                onValueChange = { roadDamage = it },
                                label = { Text("Highway / Foundation Damage") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = fieldNotes,
                                onValueChange = { fieldNotes = it },
                                label = { Text("Geologist Field Notes") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Simulated AI Computer-Vision Fracture Analysis Card
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BlueLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("AI Computer Vision Crack Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                                        Text("91% match with retrogressive rotational slip. High risk of secondary slide within 3 hours.", fontSize = 10.sp, color = Gray800)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (activeTask != null) {
                                        val result = InspectionResult(
                                            crackWidthMm = crackWidth.toDoubleOrNull() ?: 18.0,
                                            slopeTiltDeg = slopeTilt.toDoubleOrNull() ?: 43.0,
                                            rockfallSeverity = rockfallSeverity,
                                            waterSeepageRate = seepageRate,
                                            roadDamage = roadDamage,
                                            aiAnalysisResult = "AI Model: 91% rotational slip probability confirmed.",
                                            notes = fieldNotes
                                        )
                                        onSubmitInspection(activeTask.id, result)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Telemetry & Assessment", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            "navigation" -> {
                // Tactical Navigation Guidance
                item {
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = BlueAccent)
                                    Column {
                                        Text("Tactical Navigation Route", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                                        Text("ETA: 18 mins (14.2 km) via Reshi Pass Bypass", fontSize = 11.sp, color = Gray600)
                                    }
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = RiskSafeLight) {
                                    Text("CLEAR ROUTE", color = RiskSafe, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Waypoints
                            val navSteps = listOf(
                                "Depart Gangtok Field HQ via NH-10A towards Singtam (Clear)",
                                "Turn right at Mile 18 towards Reshi - Rhenock alternative corridor",
                                "Hazard Alert: Active rockfall screen at km 22. Proceed with caution.",
                                "Arrive at Mangan Bridge landslide sector (Target Site)"
                            )

                            navSteps.forEachIndexed { i, step ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(NavyMedium),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${i + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(step, fontSize = 11.sp, color = Gray800)
                                }
                            }
                        }
                    }
                }
            }

            "tasks" -> {
                // Task Queue
                items(tasks) { task ->
                    val isSelected = task.id == activeTaskId
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) BlueLight else Color.White),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, BlueAccent) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTask(task.id) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${task.id} • ${task.taskType}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                                Surface(shape = RoundedCornerShape(4.dp), color = NavyDark) {
                                    Text(task.status.label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Gray800)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Assigned: ${task.assignedAt} • Priority: ${task.priority.name}", fontSize = 10.sp, color = Gray600)
                        }
                    }
                }
            }
        }
    }
}
