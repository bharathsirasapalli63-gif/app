package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppPortal
import com.example.ui.theme.*

@Composable
fun PortalSwitcherModal(
    isOpen: Boolean,
    pendingPortal: AppPortal?,
    authError: String?,
    onDismiss: () -> Unit,
    onSelectPortal: (AppPortal) -> Unit,
    onSubmitAuth: (String, String) -> Unit,
    onAutoFillAndLogin: (AppPortal) -> Unit
) {
    if (!isOpen) return

    var username by remember(pendingPortal) {
        mutableStateOf(
            when (pendingPortal) {
                AppPortal.AUTHORITY -> "authority"
                AppPortal.FIELD_OFFICER -> "officer"
                AppPortal.ADMIN -> "admin"
                else -> ""
            }
        )
    }
    var password by remember(pendingPortal) {
        mutableStateOf(
            when (pendingPortal) {
                AppPortal.AUTHORITY -> "disaster2026"
                AppPortal.FIELD_OFFICER -> "field2026"
                AppPortal.ADMIN -> "admin2026"
                else -> ""
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
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
                    Column {
                        Text(
                            text = if (pendingPortal != null) "Role Authentication" else "Switch Operational Portal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "TerraAlert Unified Disaster Management Platform",
                            fontSize = 11.sp,
                            color = Gray600
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Gray600)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (pendingPortal == null) {
                    // List of portals to choose from
                    val portals = listOf(
                        AppPortal.CITIZEN to Triple("Resident Portal", "Mobile-first public risk map & alerts", Icons.Default.Person),
                        AppPortal.AUTHORITY to Triple("Command Authority", "EOC / SDMA / NDRF triage & broadcast", Icons.Default.Shield),
                        AppPortal.FIELD_OFFICER to Triple("Field Unit", "Geological on-site inspection & GPS nav", Icons.Default.Engineering),
                        AppPortal.ADMIN to Triple("Super Admin", "IoT sensor fleet & AI ensemble weights", Icons.Default.AdminPanelSettings)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        portals.forEach { (portal, info) ->
                            val (title, desc, icon) = info
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Gray50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPortal(portal) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(NavyMedium, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(22.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyDark)
                                        Text(desc, fontSize = 11.sp, color = Gray600)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gray600)
                                }
                            }
                        }
                    }
                } else {
                    // Password prompt for protected portal
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BlueLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NavyDark, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Accessing: ${pendingPortal.displayName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = authError, color = RiskCritical, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1-Click Demo Fill & Submit Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAutoFillAndLogin(pendingPortal) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = BlueAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("1-Click Demo", fontSize = 12.sp, color = BlueAccent)
                        }

                        Button(
                            onClick = { onSubmitAuth(username, password) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyMedium)
                        ) {
                            Text("Sign In", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
