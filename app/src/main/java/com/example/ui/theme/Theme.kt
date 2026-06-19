package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = WarmCream,
    primaryContainer = MeadowGreen,
    onPrimaryContainer = SoilBrown,
    secondary = GoldenWheat,
    onSecondary = SoilBrown,
    secondaryContainer = Color(0xFFFFE082),
    onSecondaryContainer = SoilBrown,
    tertiary = HarvestOrange,
    onTertiary = WarmCream,
    background = WarmCream,
    onBackground = SoilBrown,
    surface = Color(0xFFFFFBE6),
    onSurface = SoilBrown,
    surfaceVariant = Color(0xFFF5F0E8),
    onSurfaceVariant = LightSoil,
    outline = LightSoil,
    error = BarnRed,
    onError = WarmCream,
)

private val DarkColorScheme = darkColorScheme(
    primary = MeadowGreen,
    onPrimary = DarkForest,
    primaryContainer = DarkMeadow,
    onPrimaryContainer = PaleLight,
    secondary = NightGold,
    onSecondary = DarkSoil,
    secondaryContainer = Color(0xFF6D4C00),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = EmbersOrange,
    onTertiary = DarkSoil,
    background = NightCream,
    onBackground = PaleLight,
    surface = Color(0xFF1E1E1E),
    onSurface = PaleLight,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0A090),
    outline = Color(0xFF6D5D4D),
    error = DarkBarnRed,
    onError = PaleLight,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
