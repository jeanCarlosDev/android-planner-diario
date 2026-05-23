package com.example.plannerdiario.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.plannerdiario.R
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.plannerdiario.data.Task
import com.example.plannerdiario.ui.theme.*

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    initialTask: Task? = null,
    onConfirm: (
        title: String,
        description: String,
        attachUri: String?,
        attachType: String?,
        attachName: String?,
        isScheduled: Boolean,
        repeatDays: Int
    ) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        // wrap onConfirm to make the lambda type explicit and avoid overload ambiguity
        AddTaskDialogContent(
            initialTask = initialTask,
            onDismiss   = onDismiss,
            onConfirm   = { title, description, attachUri, attachType, attachName, isScheduled, repeatDays ->
                onConfirm(title, description, attachUri, attachType, attachName, isScheduled, repeatDays)
            }
        )
    }
}


/** Conteúdo interno do dialog — separado para uso em @Preview */
@Composable
fun AddTaskDialogContent(
    modifier: Modifier = Modifier,
    initialTask: Task? = null,
    onDismiss: () -> Unit = {},
    onConfirm: (
        title: String,
        description: String,
        attachUri: String?,
        attachType: String?,
        attachName: String?,
        isScheduled: Boolean,
        repeatDays: Int
    ) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    val isEditing = initialTask != null
    val context = LocalContext.current
    var title           by remember { mutableStateOf(initialTask?.title ?: "") }
    var description     by remember { mutableStateOf(initialTask?.description ?: "") }
    var titleError      by remember { mutableStateOf(false) }
    var attachType      by remember { mutableStateOf<String?>(initialTask?.attachmentType) }
    var attachUri       by remember { mutableStateOf<String?>(
        if (initialTask?.attachmentType == "LINK") null else initialTask?.attachmentUri
    ) }
    var attachName      by remember { mutableStateOf<String?>(
        if (initialTask?.attachmentType == "LINK") null else initialTask?.attachmentName
    ) }
    var linkUrl         by remember { mutableStateOf(
        if (initialTask?.attachmentType == "LINK") initialTask.attachmentUri ?: "" else ""
    ) }
    var isScheduled     by remember { mutableStateOf(initialTask?.isScheduled ?: false) }
    var repeatDaysText  by remember { mutableStateOf("1") }
    var requestLinkFocus by remember { mutableStateOf(false) }

    val linkFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(requestLinkFocus) {
        if (requestLinkFocus) {
            delay(150)
            runCatching { linkFocusRequester.requestFocus() }
            keyboardController?.show()
            requestLinkFocus = false
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            grantPersistablePermission(context, it)
            attachUri  = it.toString()
            attachType = "PDF"
            attachName = getFileName(context, it)
            linkUrl    = ""
        }
    }
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            grantPersistablePermission(context, it)
            attachUri  = it.toString()
            attachType = "IMAGE"
            attachName = getFileName(context, it)
            linkUrl    = ""
        }
    }

    val appColors = LocalAppColors.current

    @Composable
    fun styledTextFieldColors() = TextFieldDefaults.colors(
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

    // Outer box – lets the shadow overflow without being clipped
    Box(
        modifier = modifier
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
                shape  = RoundedCornerShape(16.dp),
                color  = appColors.surface,
                border = BorderStroke(2.dp, appColors.ink),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // ── Title row ──────────────────────���──────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text         = if (isEditing) stringResource(R.string.dlg_edit_task_title) else stringResource(R.string.dlg_new_task_title),
                            style        = MaterialTheme.typography.labelLarge,
                            fontWeight   = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color        = PinkVivid
                        )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(1.5.dp, appColors.ink, RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close, stringResource(R.string.btn_close),
                            modifier = Modifier.size(18.dp),
                            tint     = appColors.ink
                        )
                    }
                    }

                    // ── Título da tarefa ───────────────────────────────────
                    val titleBorder = if (titleError) Color(0xFFE53935) else PinkVivid
                    TextField(
                        value         = title,
                        onValueChange = { title = it; titleError = false },
                        placeholder   = {
                            Text(
                                stringResource(R.string.hint_task_title),
                                color = DarkInk.copy(alpha = 0.38f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = true,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .border(2.dp, titleBorder, RoundedCornerShape(8.dp)),
                        shape  = RoundedCornerShape(8.dp),
                        colors = styledTextFieldColors()
                    )
                    if (titleError) {
                        Text(
                            stringResource(R.string.error_title_required),
                            color = Color(0xFFE53935),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.offset(y = (-8).dp)
                        )
                    }

                    // ── Descrição ──────────────────────────────────────────
                    TextField(
                        value         = description,
                        onValueChange = { description = it },
                        placeholder   = {
                            Text(
                                stringResource(R.string.hint_task_description),
                                color = DarkInk.copy(alpha = 0.38f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, PinkVivid.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        shape  = RoundedCornerShape(8.dp),
                        colors = styledTextFieldColors()
                    )

                    // ── Anexo ──────────────────────────────────────────────
                        Text(
                            stringResource(R.string.label_attachment),
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color      = appColors.ink.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TaskAttachButton(
                            icon     = Icons.Default.PictureAsPdf,
                            label    = "PDF",
                            selected = attachType == "PDF",
                            modifier = Modifier.weight(1f),
                            onClick  = { pdfLauncher.launch(arrayOf("application/pdf")) }
                        )
                        TaskAttachButton(
                            icon     = Icons.Default.Image,
                            label    = stringResource(R.string.btn_image),
                            selected = attachType == "IMAGE",
                            modifier = Modifier.weight(1f),
                            onClick  = { imageLauncher.launch(arrayOf("image/*")) }
                        )
                        TaskAttachButton(
                            icon     = Icons.Default.Link,
                            label    = stringResource(R.string.btn_link),
                            selected = attachType == "LINK",
                            modifier = Modifier.weight(1f),
                            onClick  = {
                                attachType = "LINK"
                                attachUri = null
                                attachName = null
                                requestLinkFocus = true
                            }
                        )
                    }

                    // Chip do arquivo selecionado
                    if (attachType in listOf("PDF", "IMAGE") && attachUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, DarkInk.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(PinkVivid.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment   = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    if (attachType == "PDF") Icons.Default.PictureAsPdf
                                    else Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint     = PinkVivid
                                )
                                Text(
                                    text     = attachName ?: stringResource(R.string.attachment_fallback),
                                    style    = MaterialTheme.typography.bodySmall,
                                    color    = appColors.ink,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            attachType = null
                                            attachUri  = null
                                            attachName = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close, stringResource(R.string.btn_remove),
                                        modifier = Modifier.size(14.dp),
                                        tint     = appColors.ink.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Campo de link
                    if (attachType == "LINK") {
                        TextField(
                            value         = linkUrl,
                            onValueChange = { linkUrl = it },
                            placeholder   = {
                                Text(
                                    stringResource(R.string.hint_url),
                                    color = DarkInk.copy(alpha = 0.38f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Link, null,
                                    modifier = Modifier.size(18.dp), tint = PinkVivid)
                            },
                            trailingIcon = if (linkUrl.isNotEmpty()) ({
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { attachType = null; linkUrl = "" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, stringResource(R.string.btn_close),
                                        modifier = Modifier.size(16.dp),
                                        tint = appColors.ink.copy(alpha = 0.6f))
                                }
                            }) else null,
                            singleLine = true,
                            modifier   = Modifier
                                .fillMaxWidth()
                                .focusRequester(linkFocusRequester)
                                .border(2.dp, PinkVivid.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            shape  = RoundedCornerShape(8.dp),
                            colors = styledTextFieldColors()
                        )

                    }

                    // ── Tarefa Programada ──────────────────────────────────
                    if (!isEditing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, PinkVivid.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(appColors.surface)
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.label_scheduled_task),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = appColors.ink
                                )
                                Text(
                                    stringResource(R.string.label_scheduled_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = appColors.ink.copy(alpha = 0.5f)
                                )
                            }
                            Switch(
                                checked = isScheduled,
                                onCheckedChange = {
                                    isScheduled = it
                                    if (!it) repeatDaysText = "1"
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PinkVivid,
                                    uncheckedThumbColor = appColors.ink.copy(alpha = 0.5f),
                                    uncheckedTrackColor = appColors.ink.copy(alpha = 0.15f)
                                )
                            )
                        }

                        // Campo de repetição — aparece apenas quando programada
                        if (isScheduled) {
                            TextField(
                                value = repeatDaysText,
                                onValueChange = { newVal ->
                                    if (newVal.isEmpty() || (newVal.all { it.isDigit() } && newVal.length <= 4)) {
                                        repeatDaysText = newVal
                                    }
                                },
                            placeholder = {
                                Text(
                                    stringResource(R.string.hint_repeat_days),
                                    color = DarkInk.copy(alpha = 0.38f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            label = {
                                Text(
                                    stringResource(R.string.label_repeat_days),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = appColors.ink.copy(alpha = 0.6f)
                                )
                            },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Repeat, null,
                                        modifier = Modifier.size(18.dp),
                                        tint = PinkVivid
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, PinkVivid, RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp),
                                colors = styledTextFieldColors()
                            )
                        }
                    }

                    // ── Botão Adicionar ────────────────────────────────────
                    val active = title.isNotBlank()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { clip = false }
                            .drawBehind {
                                drawRoundRect(
                                    color        = appColors.shadow.copy(alpha = if (active) 1f else 0.4f),
                                    topLeft      = Offset(4.dp.toPx(), 4.dp.toPx()),
                                    size         = size,
                                    cornerRadius = CornerRadius(8.dp.toPx())
                                )
                            }
                    ) {
                        Button(
                            onClick = {
                                if (title.isBlank()) { titleError = true; return@Button }
                                val finalUri = when (attachType) {
                                    "LINK" -> linkUrl.trim().ifBlank { null }
                                    else   -> attachUri
                                }
                                val finalName = when (attachType) {
                                    "LINK" -> linkUrl.trim().ifBlank { null }
                                    else   -> attachName
                                }
                                onConfirm(
                                     title.trim(), description.trim(),
                                     finalUri, if (finalUri != null) attachType else null,
                                     finalName,
                                     isScheduled,
                                     repeatDaysText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                 )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(8.dp),
                            enabled  = active,
                            border   = BorderStroke(2.dp, appColors.ink.copy(alpha = if (active) 1f else 0.4f)),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor         = PinkVivid,
                                disabledContainerColor = PinkVivid.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text(
                                if (isEditing) stringResource(R.string.btn_save) else stringResource(R.string.btn_add),
                                fontWeight    = FontWeight.ExtraBold,
                                color         = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
}

// ── Botão de anexo estilizado ─────────────────────────────────────────────────
@Composable
private fun TaskAttachButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    Box(
        modifier = modifier
            .graphicsLayer { clip = false }
            .drawBehind {
                if (selected) {
                    drawRoundRect(
                        color        = appColors.shadow,
                        topLeft      = Offset(3.dp.toPx(), 3.dp.toPx()),
                        size         = size,
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (selected) 2.dp else 1.5.dp,
                    color = if (selected) appColors.ink else appColors.ink.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) PinkVivid else appColors.surface)
                .clickable { onClick() }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    icon, null,
                    modifier = Modifier.size(18.dp),
                    tint     = if (selected) Color.White else appColors.ink.copy(alpha = 0.6f)
                )
                Text(
                    label,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (selected) Color.White else appColors.ink.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun grantPersistablePermission(context: Context, uri: Uri) {
    try {
        context.contentResolver.takePersistableUriPermission(
            uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (_: Exception) {}
}

private fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) result = cursor.getString(idx)
            }
        }
    }
    return result ?: uri.lastPathSegment ?: context.getString(R.string.attachment_fallback)
}
