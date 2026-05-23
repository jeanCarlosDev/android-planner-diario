package com.example.plannerdiario.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.plannerdiario.R
import com.example.plannerdiario.ui.theme.LocalAppColors
import com.example.plannerdiario.ui.theme.PinkVivid

@Composable
fun UsageSuggestionDialog(
    onDismiss: () -> Unit
) {
    val appColors   = LocalAppColors.current
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
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
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // ── Header ────────────────────────────────────────────
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
                                imageVector        = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint               = PinkVivid,
                                modifier           = Modifier.size(20.dp)
                            )
                            Text(
                                text          = stringResource(R.string.dlg_usage_title),
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

                    Spacer(Modifier.height(4.dp))

                    HorizontalDivider(
                        color     = appColors.ink.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )

                    Spacer(Modifier.height(12.dp))

                    // ── Scrollable content ────────────────────────────────
                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            val rawContent = stringResource(R.string.usage_content)
                            UsageContentSection(rawContent, appColors)
                        }

                        // Scroll fade overlay at bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Color.Transparent, appColors.surface)
                                    )
                                )
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Close button ──────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { clip = false }
                            .drawBehind {
                                drawRoundRect(
                                    color        = appColors.shadow,
                                    topLeft      = Offset(3.dp.toPx(), 3.dp.toPx()),
                                    size         = size,
                                    cornerRadius = CornerRadius(10.dp.toPx())
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, appColors.ink, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(PinkVivid)
                                .clickable { onDismiss() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = stringResource(R.string.btn_close),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageContentSection(
    content: String,
    appColors: com.example.plannerdiario.ui.theme.AppColors
) {
    val paragraphs = content.split("\n\n")
    paragraphs.forEach { paragraph ->
        val trimmed = paragraph.trimStart()
        val isHeader = trimmed.startsWith("📋") || trimmed.startsWith("🗂") ||
                trimmed.startsWith("✅") || trimmed.startsWith("🔁") ||
                trimmed.startsWith("📅") || trimmed.startsWith("💾") ||
                trimmed.startsWith("💡")

        if (paragraph.contains("\n")) {
            // Section with title and body
            val lines = paragraph.split("\n", limit = 2)
            val title = lines[0]
            val body  = if (lines.size > 1) lines[1] else ""

            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = appColors.ink,
                    lineHeight = 22.sp
                )
                if (body.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = body,
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = appColors.ink.copy(alpha = 0.85f),
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            Text(
                text       = paragraph,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                color      = if (isHeader) appColors.ink else appColors.ink.copy(alpha = 0.85f),
                modifier   = Modifier.padding(bottom = 16.dp),
                lineHeight = 22.sp,
                textAlign  = TextAlign.Start
            )
        }
    }
    // Extra bottom padding so content doesn't hide behind fade
    Spacer(Modifier.height(16.dp))
}

