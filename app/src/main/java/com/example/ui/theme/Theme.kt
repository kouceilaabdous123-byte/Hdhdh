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
    primary = DiagCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = DiagBlue,
    onSecondary = Color(0xFF002B73),
    secondaryContainer = Color(0xFF003FA4),
    onSecondaryContainer = Color(0xFFD9E2FF),
    tertiary = DiagGreen,
    onTertiary = Color(0xFF003915),
    error = DiagRed,
    background = TechDarkBg,
    onBackground = Color(0xFFE2E8F0),
    surface = TechDarkSurface,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = TechDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = TechDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF0056D2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9E2FF),
    onSecondaryContainer = Color(0xFF001944),
    tertiary = Color(0xFF006D38),
    onTertiary = Color.White,
    error = Color(0xFFBA1A1A),
    background = TechLightBg,
    onBackground = Color(0xFF0F172A),
    surface = TechLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = TechLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = TechLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our sleek diagnostic theme by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
