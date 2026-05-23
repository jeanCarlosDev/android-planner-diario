package com.example.plannerdiario.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary    = PinkVivid,
    background = Color(0xFF0F0F1A),
    surface    = Color(0xFF1A1A2E),
    onSurface  = Color(0xFFE2E2F0),
    onBackground = Color(0xFFE2E2F0)
)

private val LightColorScheme = lightColorScheme(
    primary          = PinkVivid,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFFFD6E0),
    secondary        = DarkInk,
    background       = Cream,
    surface          = CardWhite,
    onBackground     = DarkInk,
    onSurface        = DarkInk,
    onSurfaceVariant = DarkInk.copy(alpha = 0.55f),
    outline          = DarkInk.copy(alpha = 0.3f)
)

@Composable
fun PlannerDiarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}
