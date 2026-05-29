package com.jsjstudios.dailyplanner.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.jsjstudios.dailyplanner.R
import com.jsjstudios.dailyplanner.data.Task
import com.jsjstudios.dailyplanner.ui.theme.DarkInk
import com.jsjstudios.dailyplanner.ui.theme.LocalAppColors
import com.jsjstudios.dailyplanner.ui.theme.MintTeal
import com.jsjstudios.dailyplanner.ui.theme.PinkVivid

@Composable
fun TaskItem(
    task: Task,
    listColor: Color = DarkInk,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (title: String, description: String, attachUri: String?, attachType: String?, attachName: String?, isScheduled: Boolean, repeatDays: Int, isRecurring: Boolean, recurrenceInterval: String?) -> Unit
) {
    val appColors = LocalAppColors.current
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog    by remember { mutableStateOf(false) }

    val isScheduledActive = task.isScheduled && !task.isCompleted

    // Modo claro: fundo mint definido + borda teal escuro (bom contraste)
    // Modo escuro: fundo teal levemente translúcido + borda MintTeal vibrante
    val cardBorderColor = when {
        isScheduledActive -> if (appColors.isDark) MintTeal else Color(0xFF00897B)
        else              -> appColors.ink
    }
    val cardBgColor = when {
        task.isCompleted  -> appColors.completedCard
        isScheduledActive -> if (appColors.isDark) MintTeal.copy(alpha = 0.14f) else Color(0xFFDFF6F4)
        else              -> appColors.card
    }

    // ── Diálogo de edição ─────────────────────────────────────────────────
    if (showEditDialog) {
        AddTaskDialog(
            initialTask = task,
            onDismiss   = { showEditDialog = false },
            onConfirm   = { title, desc, uri, type, name, isScheduled, repeatDays, isRecurring, recurrenceInterval ->
                onEdit(title, desc, uri, type, name, isScheduled, repeatDays, isRecurring, recurrenceInterval)
                showEditDialog = false
            }
        )
    }

    // ── Diálogo de confirmação de exclusão ────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    stringResource(R.string.dlg_delete_task_title),
                    fontWeight = FontWeight.ExtraBold,
                    color = appColors.ink
                )
            },
            text = {
                Text(
                    stringResource(R.string.dlg_delete_task_message, task.title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.ink.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkVivid),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, appColors.ink)
                ) {
                    Text(stringResource(R.string.btn_delete), fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, appColors.ink)
                ) {
                    Text(stringResource(R.string.btn_cancel), color = appColors.ink)
                }
            },
            containerColor = appColors.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .graphicsLayer { clip = false }
            .drawBehind {
                drawRoundRect(
                    color       = appColors.shadow,
                    topLeft     = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size        = size,
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            },
        shape  = RoundedCornerShape(8.dp),
        color  = cardBgColor,
        border = BorderStroke(1.5.dp, cardBorderColor)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor   = listColor,
                        uncheckedColor = appColors.ink.copy(alpha = 0.4f),
                        checkmarkColor = Color.White
                    )
                )
                // Botão editar — quadrado com borda ao lado do checkbox
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.5.dp, appColors.ink.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(appColors.surface)
                        .clickable { showEditDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit, stringResource(R.string.btn_edit),
                        modifier = Modifier.size(18.dp),
                        tint     = appColors.ink.copy(alpha = 0.55f)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isScheduledActive) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = stringResource(R.string.label_scheduled_task),
                                modifier = Modifier.size(13.dp),
                                tint     = MintTeal
                            )
                        }
                        Text(
                            text           = task.title,
                            style          = MaterialTheme.typography.bodyLarge,
                            fontWeight     = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            color          = if (task.isCompleted) appColors.ink.copy(alpha = 0.4f) else appColors.ink,
                            maxLines       = 1,
                            overflow       = TextOverflow.Ellipsis
                        )
                    }
                    if (task.description.isNotBlank()) {
                        Text(
                            text     = task.description,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = appColors.ink.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Botão excluir
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close, stringResource(R.string.btn_remove),
                        modifier = Modifier.size(16.dp),
                        tint     = appColors.ink.copy(alpha = 0.4f)
                    )
                }
            }
            // Attachment chip
            if (!task.attachmentUri.isNullOrBlank() && !task.attachmentType.isNullOrBlank()) {
                val (icon, label) = when (task.attachmentType) {
                    "PDF"   -> Icons.Default.PictureAsPdf to (task.attachmentName ?: "PDF")
                    "IMAGE" -> Icons.Default.Image        to (task.attachmentName ?: "Imagem")
                    "LINK"  -> Icons.Default.Link         to (task.attachmentName ?: task.attachmentUri)
                    else    -> Icons.Default.AttachFile   to (task.attachmentName ?: "Anexo")
                }
                SuggestionChip(
                    onClick = { openAttachment(context, task.attachmentUri, task.attachmentType) },
                    label   = {
                        Text(label!!, style = MaterialTheme.typography.labelSmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 220.dp))
                    },
                    icon     = { Icon(icon, null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.padding(start = 52.dp, bottom = 6.dp)
                )
            }
        }
    }
}

private fun openAttachment(context: android.content.Context, uri: String, type: String) {
    try {
        val intent = when (type) {
            "PDF"   -> Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(uri), "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            "IMAGE" -> Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(uri), "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            "LINK"  -> {
                val url = if (uri.startsWith("http://") || uri.startsWith("https://")) uri
                          else "https://$uri"
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            }
            else -> Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
