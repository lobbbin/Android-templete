package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = WhiteHouse,
    primaryContainer = LightSlate,
    onPrimaryContainer = NavyBlue,
    secondary = PresidentialGold,
    onSecondary = NavyBlue,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = NavyBlue,
    tertiary = FlagRed,
    onTertiary = WhiteHouse,
    background = Parchment,
    onBackground = SlateGray,
    surface = WhiteHouse,
    onSurface = SlateGray,
    surfaceVariant = Color(0xFFECEFF1),
    onSurfaceVariant = LightSlate,
    outline = LightSlate,
    error = FlagRed,
    onError = WhiteHouse,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF607D8B),
    onPrimary = DarkNavy,
    primaryContainer = DarkNavy,
    onPrimaryContainer = PaleGold,
    secondary = DarkGold,
    onSecondary = DarkNavy,
    secondaryContainer = Color(0xFF4A3A00),
    onSecondaryContainer = PaleGold,
    tertiary = Color(0xFFEF5350),
    onTertiary = DarkNavy,
    background = Charcoal,
    onBackground = Color(0xFFECEFF1),
    surface = MidnightGray,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = Color(0xFF263238),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF78909C),
    error = Color(0xFFEF5350),
    onError = DarkNavy,
)

@Composable
fun PresidentialTheme(
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