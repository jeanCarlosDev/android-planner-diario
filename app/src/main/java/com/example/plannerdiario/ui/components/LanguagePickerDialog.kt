package com.example.plannerdiario.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.plannerdiario.R
import com.example.plannerdiario.ui.theme.LocalAppColors
import com.example.plannerdiario.ui.theme.PinkVivid

// ── Modelo de opção de idioma ─────────────────────────────────────────────────
data class LanguageOption(
    val tag: String,        // BCP-47 (ex: "pt-BR")
    val flag: String,       // emoji de bandeira
    val nativeName: String, // nome no próprio idioma
    val localName: String   // nome traduzido para o idioma da interface
)

val availableLanguages = listOf(
    LanguageOption("pt-BR", "🇧🇷", "Português (Brasil)", "Português (Brasil)"),
    LanguageOption("en-US", "🇺🇸", "English (US)",       "English (US)"),
    LanguageOption("es-ES", "🇪🇸", "Español",            "Spanish"),
    LanguageOption("zh-CN", "🇨🇳", "中文 (简体)",         "Chinese (Simplified)"),
    LanguageOption("ja-JP", "🇯🇵", "日本語",              "Japanese"),
    LanguageOption("ko-KR", "🇰🇷", "한국어",              "Korean"),
    LanguageOption("de-DE", "🇩🇪", "Deutsch",            "German"),
    LanguageOption("fr-FR", "🇫🇷", "Français",           "French"),
    LanguageOption("ru-RU", "🇷🇺", "Русский",            "Russian"),
    LanguageOption("ar-SA", "🇸🇦", "العربية",             "Arabic"),
    LanguageOption("id-ID", "🇮🇩", "Bahasa Indonesia",    "Indonesian")
    // adicione mais idiomas aqui no futuro
)

// ── Dialog de seleção de idioma ───────────────────────────────────────────────
@Composable
fun LanguagePickerDialog(
    currentTag: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { clip = false }
                .drawBehind {
                    drawRoundRect(
                        color        = appColors.shadow,
                        topLeft      = Offset(5.dp.toPx(), 5.dp.toPx()),
                        size         = size,
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
        ) {
            Surface(
                shape    = RoundedCornerShape(16.dp),
                color    = appColors.surface,
                border   = BorderStroke(2.dp, appColors.ink),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {

                    // ── Cabeçalho ─────────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = null,
                                tint               = PinkVivid,
                                modifier           = Modifier.size(20.dp)
                            )
                            Text(
                                text          = stringResource(R.string.menu_language).uppercase(),
                                style         = MaterialTheme.typography.labelLarge,
                                fontWeight    = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color         = PinkVivid
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .border(1.5.dp, appColors.ink, RoundedCornerShape(6.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close, null,
                                modifier = Modifier.size(18.dp),
                                tint     = appColors.ink
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // ── Lista de idiomas ──────────────────────────────────
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableLanguages) { lang ->
                            LanguageItem(
                                option    = lang,
                                isSelected = lang.tag == currentTag,
                                appColors  = appColors,
                                onClick    = {
                                    onSelect(lang.tag)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Item de idioma ────────────────────────────────────────────────────────────
@Composable
private fun LanguageItem(
    option: LanguageOption,
    isSelected: Boolean,
    appColors: com.example.plannerdiario.ui.theme.AppColors,
    onClick: () -> Unit
) {
    val selectedBorderColor = if (appColors.isDark) PinkVivid else Color(0xFF1565C0)
    val selectedBgColor = if (appColors.isDark) {
        PinkVivid.copy(alpha = 0.18f)
    } else {
        Color(0xFFEAF3FF)
    }
    val borderColor = if (isSelected) selectedBorderColor else appColors.ink.copy(alpha = 0.2f)
    val bgColor = if (isSelected) selectedBgColor else Color.Transparent
    val selectedTextColor = if (appColors.isDark) PinkVivid else Color(0xFF1565C0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false }
            .drawBehind {
                if (isSelected) drawRoundRect(
                    color        = appColors.shadow,
                    topLeft      = Offset(3.dp.toPx(), 3.dp.toPx()),
                    size         = size,
                    cornerRadius = CornerRadius(10.dp.toPx())
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(if (isSelected) 2.dp else 1.5.dp, borderColor, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Bandeira
            Text(
                text  = option.flag,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.size(36.dp),
            )

            // Nomes
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = option.nativeName,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) selectedTextColor else appColors.ink
                )
                if (option.nativeName != option.localName) {
                    Text(
                        text  = option.localName,
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.ink.copy(alpha = 0.5f)
                    )
                }
            }

            // Checkmark
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint     = selectedTextColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
