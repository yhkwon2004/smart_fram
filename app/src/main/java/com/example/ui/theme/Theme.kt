package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = FarmGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = FarmGreenContainer,
    onPrimaryContainer = OnFarmGreenContainer,
    secondary = SoftSkyBlue,
    onSecondary = Color.White,
    secondaryContainer = SoftSkyBlueContainer,
    onSecondaryContainer = OnSoftSkyBlue,
    tertiary = WarmAmber,
    onTertiary = Color.White,
    tertiaryContainer = WarmAmberContainer,
    onTertiaryContainer = OnWarmAmber,
    background = SoftCream,
    onBackground = DarkForestText,
    surface = CardBackground,
    onSurface = DarkForestText,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = SecondaryMutedText,
    outline = OutlineGreen
)

private val DarkColorScheme = darkColorScheme(
    primary = FarmGreenLight,
    onPrimary = Color.Black,
    primaryContainer = FarmGreenDark,
    onPrimaryContainer = FarmGreenContainer,
    secondary = SoftSkyBlue,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = SoftSkyBlueContainer,
    tertiary = WarmAmber,
    onTertiary = Color.Black,
    background = Color(0xFF131F17),
    onBackground = Color(0xFFE2EBE2),
    surface = Color(0xFF1B2B20),
    onSurface = Color(0xFFE2EBE2),
    surfaceVariant = Color(0xFF263A2D),
    onSurfaceVariant = Color(0xFFB5C9B8),
    outline = Color(0xFF3B5643)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional garden branding
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
        shapes = AppShapes,
        content = content
    )
}
