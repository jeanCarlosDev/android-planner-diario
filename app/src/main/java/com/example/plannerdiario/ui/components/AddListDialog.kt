package com.example.plannerdiario.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.plannerdiario.R
import com.example.plannerdiario.data.ColorShapeOption
import com.example.plannerdiario.data.listPresetOptions
import com.example.plannerdiario.ui.theme.*

@Composable
fun AddListDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String?, shape: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf<ColorShapeOption?>(null) }
    var nameError by remember { mutableStateOf(false) }
    var isAuto by remember { mutableStateOf(true) }
    val appColors = LocalAppColors.current

    Dialog(onDismissRequest = onDismiss) {
        // Outer box: allows the shadow to overflow (no clipping)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { clip = false }
                .drawBehind {
                drawRoundRect(
                        color        = appColors.shadow,
                        topLeft      = Offset(5.dp.toPx(), 5.dp.toPx()),
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
        ) {
            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = appColors.surface,
                border = BorderStroke(2.dp, appColors.ink),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ── Title row ──────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dlg_new_list_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = PinkVivid
                        )
                        // X button – bordered box (app signature style)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .border(1.5.dp, appColors.ink, RoundedCornerShape(6.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close, contentDescription = stringResource(R.string.btn_close),
                                modifier = Modifier.size(18.dp), tint = appColors.ink
                            )
                        }
                    }

                    // ── Input + Criar row ──────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val borderColor = when {
                            nameError -> Color(0xFFE53935)
                            else      -> PinkVivid
                        }
                        TextField(
                            value = name,
                            onValueChange = { name = it; nameError = false },
                            placeholder = {
                                Text(
                                    stringResource(R.string.hint_list_name),
                                    color = DarkInk.copy(alpha = 0.38f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .border(2.dp, borderColor, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor   = appColors.surface,
                                unfocusedContainerColor = appColors.surface,
                                errorContainerColor     = appColors.surface,
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                errorIndicatorColor     = Color.Transparent,
                                focusedTextColor        = appColors.ink,
                                unfocusedTextColor      = appColors.ink,
                                errorTextColor          = appColors.ink
                            )
                        )

                        // "Criar" button – estilo da imagem (borda + sombra offset)
                        val active = name.isNotBlank()
                        Box(
                            modifier = Modifier
                                .graphicsLayer { clip = false }
                                .drawBehind {
                                     drawRoundRect(
                                         color = appColors.shadow.copy(alpha = if (active) 1f else 0.4f),
                                         topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                                         size = size,
                                         cornerRadius = CornerRadius(8.dp.toPx())
                                     )
                                }
                        ) {
                            Button(
                                onClick = {
                                    if (name.isBlank()) { nameError = true; return@Button }
                                    onConfirm(
                                        name.trim(),
                                        if (isAuto) null else selectedOption?.colorHex,
                                        if (isAuto) null else selectedOption?.shape
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                 border = BorderStroke(2.dp, appColors.ink.copy(alpha = if (active) 1f else 0.4f)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PinkVivid,
                                    disabledContainerColor = PinkVivid.copy(alpha = 0.5f)
                                ),
                                enabled = active,
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp)
                            ) {
                                Text(
                                    stringResource(R.string.btn_create),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // ── Shape / Color selector + auto ─────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Row of shape icons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listPresetOptions.forEach { option ->
                                val color = Color(android.graphics.Color.parseColor(option.colorHex))
                                val isSelected = !isAuto && selectedOption == option
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .then(
                                            if (isSelected)
                                                Modifier.border(2.5.dp, DarkInk, RoundedCornerShape(8.dp))
                                            else
                                                Modifier.border(1.dp, DarkInk.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color.copy(alpha = 0.15f))
                                        .clickable { selectedOption = option; isAuto = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    ShapeIcon(shape = option.shape, color = color, modifier = Modifier.size(22.dp))
                                }
                            }
                        }

                        // "auto" below the icons
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .then(
                                    if (isAuto) Modifier.background(PinkVivid.copy(alpha = 0.12f))
                                    else Modifier
                                )
                                .clickable { isAuto = true; selectedOption = null }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.lbl_auto),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAuto) PinkVivid else appColors.ink.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }
        }
    }
}
