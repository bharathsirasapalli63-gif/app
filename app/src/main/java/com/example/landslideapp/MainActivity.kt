package com.example.landslideapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LandslideAlertApp()
        }
    }
}

@Composable
fun LandslideAlertApp() {
    var currentTime by remember { mutableStateOf(getCurrentTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = getCurrentTime()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.foundation.background(
                        Color(0xFF667eea)
                    ).brush ?: androidx.compose.foundation.background(Color(0xFF667eea)).brush!!
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🏔️ Landslide Alert",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                color = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Time",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Text(
                        text = currentTime,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667eea),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Text(
                        text = "Real-time Monitoring Active",
                        fontSize = 14.sp,
                        color = Color(0xFF764ba2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "⚠️ Stay Alert | Stay Safe",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}