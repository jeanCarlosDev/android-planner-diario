package com.jsjstudios.dailyplanner.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Legacy
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

// ── Static design tokens (always the same) ────────────────────────────────
val PinkVivid      = Color(0xFFE8185D)
val DarkInk        = Color(0xFF1A1A2E)
val YellowFresh    = Color(0xFFFFC300)
val Cream          = Color(0xFFF5EFE6)
val MintTeal       = Color(0xFF4ECDC4)
val PinkDecorLight = Color(0xFFFFB3C6)
val CardWhite      = Color(0xFFFFFFFF)
val CompletedBg    = Color(0xFFF0F0F0)

// ── Dynamic AppColors ─────────────────────────────────────────────────────
data class AppColors(
    val background: Color,    // tela principal
    val surface: Color,       // diálogos, cards de seletor, text-fields
    val card: Color,          // cartões de tarefa
    val completedCard: Color, // tarefa concluída
    val ink: Color,           // texto + bordas
    val shadow: Color,        // retângulo de sombra offset
    val pinkDecor: Color,     // forma decorativa superior
    val mintDecor: Color,     // forma decorativa inferior
    val isDark: Boolean
)

val LightAppColors = AppColors(
    background    = Color(0xFFF5EFE6),
    surface       = Color(0xFFF5EFE6),
    card          = Color(0xFFFFFFFF),
    completedCard = Color(0xFFF0F0F0),
    ink           = Color(0xFF1A1A2E),
    shadow        = Color(0xFF1A1A2E),
    pinkDecor     = Color(0xFFFFB3C6).copy(alpha = 0.55f),
    mintDecor     = Color(0xFF4ECDC4).copy(alpha = 0.45f),
    isDark        = false
)

val DarkAppColors = AppColors(
    background    = Color(0xFF0F0F1A), // navy muito escuro
    surface       = Color(0xFF1A1A2E), // navy médio (cards, diálogos)
    card          = Color(0xFF1E1E30), // navy levemente mais claro
    completedCard = Color(0xFF252538),
    ink           = Color(0xFFE2E2F0), // lavanda claro (texto + bordas)
    shadow        = Color(0xFF000000), // preto puro para profundidade
    pinkDecor     = Color(0xFFE8185D).copy(alpha = 0.35f),
    mintDecor     = Color(0xFF4ECDC4).copy(alpha = 0.30f),
    isDark        = true
)

val LocalAppColors = staticCompositionLocalOf<AppColors> { LightAppColors }
