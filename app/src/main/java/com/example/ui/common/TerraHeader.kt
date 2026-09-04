package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppPortal
import com.example.ui.theme.*

@Composable
fun TerraHeader(
    activePortal: AppPortal,
    onOpenPortalSwitcher: () -> Unit,
    onOpenSos: () -> Unit,
    selectedLanguage: String,
    onSelectLanguage: (String) -> Unit
) {
    var showLangMenu by remember { mutableStateOf(false) }

    Surface(
        color = NavyDark,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Brand and Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "TerraAlert Logo",
                            tint = CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "TerraAlert",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E3A8A)
                            ) {
                                Text(
                                    text = "NDMA • SIH",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF93C5FD),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "AI + IoT Landslide Platform • NE India",
                            fontSize = 10.sp,
                            color = Gray300
                        )
                    }
                }

                // Header Actions: Portal Switcher, SOS, Language
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Portal Switcher button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NavySurface,
                        modifier = Modifier.clickable { onOpenPortalSwitcher() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (activePortal) {
                                    AppPortal.CITIZEN -> Icons.Default.Person
                                    AppPortal.AUTHORITY -> Icons.Default.Shield
                                    AppPortal.FIELD_OFFICER -> Icons.Default.Engineering
                                    AppPortal.ADMIN -> Icons.Default.AdminPanelSettings
                                },
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when (activePortal) {
                                    AppPortal.CITIZEN -> "Resident"
                                    AppPortal.AUTHORITY -> "Authority"
                                    AppPortal.FIELD_OFFICER -> "Officer"
                                    AppPortal.ADMIN -> "Admin"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Gray300,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Critical Distress SOS Button
                    Button(
                        onClick = onOpenSos,
                        colors = ButtonDefaults.buttonColors(containerColor = RiskCritical),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "SOS",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "SOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Language dropdown toggle
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = NavySurface,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { showLangMenu = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = selectedLanguage.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            val languages = listOf(
                                "en" to "English",
                                "hi" to "हिंदी (Hindi)",
                                "as" to "অসমীয়া (Assamese)",
                                "bn" to "বাংলা (Bengali)",
                                "ne" to "नेपाली (Nepali)"
                            )
                            languages.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 13.sp) },
                                    onClick = {
                                        onSelectLanguage(code)
                                        showLangMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Sub-bar with Live Telemetry Ticker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF07101E))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(RiskSafe)
                    )
                    Text(
                        text = "LIVE SATELLITE & IOT DISPATCH ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray300,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "CORRIDORS: SIKKIM • ASSAM • NAGALAND • MEGHALAYA",
                    fontSize = 9.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
