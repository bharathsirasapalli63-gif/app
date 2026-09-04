package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = NavyDark,
    primaryContainer = NavySurface,
    onPrimaryContainer = Color.White,
    secondary = BlueAccent,
    onSecondary = Color.White,
    background = NavyDark,
    surface = NavySurface,
    onBackground = Color.White,
    onSurface = Color.White,
    error = RiskCritical,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NavyMedium,
    onPrimary = Color.White,
    primaryContainer = BlueLight,
    onPrimaryContainer = NavyDark,
    secondary = BlueAccent,
    onSecondary = Color.White,
    background = Gray50,
    surface = Color.White,
    onBackground = Gray800,
    onSurface = Gray800,
    error = RiskCritical,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent authoritative emergency styling
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
