package com.example.plannerdiario.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.plannerdiario.ui.theme.LocalAppColors
import com.example.plannerdiario.ui.theme.PinkVivid
import com.example.plannerdiario.ui.theme.YellowFresh
import androidx.compose.ui.res.stringResource
import com.example.plannerdiario.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// Dias da semana localizados — começa no domingo (padrão BR e US)
private fun weekDayLabels(locale: Locale): List<String> =
    listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
           DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
        .map { it.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { c -> c.uppercase() }.take(3) }

// Offset de coluna para o primeiro dia do mês (domingo = coluna 0)
private fun firstDayOffset(month: YearMonth): Int {
    val dow = month.atDay(1).dayOfWeek.value // Mon=1 … Sat=6, Sun=7
    return if (dow == 7) 0 else dow
}

@Composable
fun CustomDatePickerDialog(
    selectedDate: LocalDate,
    completedDates: Set<LocalDate>,
    scheduledDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    val locale    = Locale.getDefault()
    val today     = LocalDate.now()

    var displayMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var tempSelected by remember { mutableStateOf(selectedDate) }
    val weekDays = remember(locale) { weekDayLabels(locale) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
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
                Column(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // ── Título ────────────────────────────────────────────
                    Text(
                        stringResource(R.string.dlg_select_date_title),
                        style         = MaterialTheme.typography.labelLarge,
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color         = PinkVivid
                    )

                    // ── Navegação de mês ──────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick  = { displayMonth = displayMonth.minusMonths(1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, stringResource(R.string.btn_prev_month), tint = appColors.ink)
                        }

                        val monthLabel = displayMonth.month
                            .getDisplayName(TextStyle.FULL, locale)
                            .replaceFirstChar { it.uppercase() }
                        Text(
                            "$monthLabel ${displayMonth.year}",
                            fontWeight = FontWeight.Bold,
                            color      = appColors.ink
                        )

                        IconButton(
                            onClick  = { displayMonth = displayMonth.plusMonths(1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, stringResource(R.string.btn_next_month), tint = appColors.ink)
                        }
                    }

                    // ── Cabeçalho dias da semana ──────────────────────────
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekDays.forEach { label ->
                            Text(
                                label,
                                modifier  = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style     = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color     = appColors.ink.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // ── Grade do calendário ───────────────────────────────
                    val offset      = firstDayOffset(displayMonth)
                    val daysInMonth = displayMonth.lengthOfMonth()
                    val totalRows   = ((offset + daysInMonth) + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (row in 0 until totalRows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 7) {
                                    val dayNum = row * 7 + col - offset + 1
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dayNum in 1..daysInMonth) {
                                            val date         = displayMonth.atDay(dayNum)
                                            val isSelected   = date == tempSelected
                                            val isToday      = date == today
                                            val hasCompleted = date in completedDates
                                            val hasScheduled = date in scheduledDates

                                            DayCell(
                                                dayNum       = dayNum,
                                                isSelected   = isSelected,
                                                isToday      = isToday,
                                                hasCompleted = hasCompleted,
                                                hasScheduled = hasScheduled,
                                                appColors    = appColors,
                                                onClick      = { tempSelected = date }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Legenda ───────────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        LegendItem(color = Color(0xFF43A047), label = stringResource(R.string.legend_completed))
                        Spacer(Modifier.width(16.dp))
                        LegendItem(color = YellowFresh, label = stringResource(R.string.legend_scheduled))
                    }

                    // ── Botões ────────────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape   = RoundedCornerShape(8.dp),
                            border  = BorderStroke(1.5.dp, appColors.ink.copy(alpha = 0.4f)),
                            colors  = ButtonDefaults.outlinedButtonColors(contentColor = appColors.ink)
                        ) { Text(stringResource(R.string.btn_cancel)) }

                        Box(
                            modifier = Modifier
                                .graphicsLayer { clip = false }
                                .drawBehind {
                                    drawRoundRect(
                                        color        = appColors.shadow,
                                        topLeft      = Offset(3.dp.toPx(), 3.dp.toPx()),
                                        size         = size,
                                        cornerRadius = CornerRadius(8.dp.toPx())
                                    )
                                }
                        ) {
                            Button(
                                onClick = { onDateSelected(tempSelected); onDismiss() },
                                shape   = RoundedCornerShape(8.dp),
                                border  = BorderStroke(1.5.dp, appColors.ink),
                                colors  = ButtonDefaults.buttonColors(containerColor = PinkVivid)
                            ) {
                                Text(stringResource(R.string.btn_ok), fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Célula de dia ─────────────────────────────────────────────────────────────
@Composable
private fun DayCell(
    dayNum: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasCompleted: Boolean,
    hasScheduled: Boolean,
    appColors: com.example.plannerdiario.ui.theme.AppColors,
    onClick: () -> Unit
) {
    val circleBg = when {
        isSelected -> PinkVivid
        isToday    -> PinkVivid.copy(alpha = 0.15f)
        else       -> Color.Transparent
    }
    val textColor = if (isSelected) Color.White else appColors.ink

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 3.dp)
    ) {
        // Círculo com número do dia
        Box(
            modifier         = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                dayNum.toString(),
                textAlign  = TextAlign.Center,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color      = textColor
            )
        }

        // Indicadores — sempre ocupam 6dp para manter grid alinhado
        Row(
            modifier              = Modifier.height(6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (hasCompleted) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF43A047))
                )
            }
            if (hasScheduled) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(YellowFresh)
                )
            }
        }
    }
}

// ── Item de legenda ───────────────────────────────────────────────────────────
@Composable
private fun LegendItem(color: Color, label: String) {
    val appColors = LocalAppColors.current
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = appColors.ink.copy(alpha = 0.7f)
        )
    }
}
