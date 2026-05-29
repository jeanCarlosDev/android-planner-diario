package com.jsjstudios.dailyplanner.ui.components

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
// import androidx.compose.ui.platform.LocalContext   // reativar com Google Drive
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jsjstudios.dailyplanner.R
// ─── GOOGLE DRIVE ─── descomentar para reativar no futuro ───────────────────
// import com.jsjstudios.dailyplanner.data.DriveBackupManager
// import com.google.android.gms.auth.api.signin.GoogleSignIn
// import com.google.android.gms.common.api.ApiException
// ────────────────────────────────────────────────────────────────────────────
import com.jsjstudios.dailyplanner.ui.theme.LocalAppColors
import com.jsjstudios.dailyplanner.ui.theme.PinkVivid

@Composable
fun DataManagementDialog(
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearAll: () -> Unit,
    // ─── GOOGLE DRIVE — parâmetros reservados para implementação futura ───────
    // Para reativar: descomentar imports acima, restaurar estado/launchers abaixo
    // e descomentar a SEÇÃO 2 — GOOGLE DRIVE no Column
    @Suppress("UNUSED_PARAMETER") onGetBackupJson: (callback: (String) -> Unit) -> Unit,
    @Suppress("UNUSED_PARAMETER") onImportJson: (json: String, callback: (Boolean) -> Unit) -> Unit
    // ─────────────────────────────────────────────────────────────────────────
) {
    val appColors = LocalAppColors.current
    // val context = LocalContext.current           // reativar com Google Drive
    // val scope   = rememberCoroutineScope()       // reativar com Google Drive

    // ── UI state ──────────────────────────────────────────────────────────────
    var showClearConfirm by remember { mutableStateOf(false) }

    // ─── GOOGLE DRIVE — estado e launchers (reativar quando implementar) ──────
    // val driveManager = remember { DriveBackupManager(context) }
    // var driveAccount by remember {
    //     mutableStateOf(driveManager.getSignedInAccount()?.takeIf { driveManager.isSignedInWithDrive() })
    // }
    // var isLoading       by remember { mutableStateOf(false) }
    // var feedbackMsg     by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    // var needsPermIntent by remember { mutableStateOf<android.content.Intent?>(null) }
    // val signInLauncher = rememberLauncherForActivityResult(
    //     ActivityResultContracts.StartActivityForResult()
    // ) { result ->
    //     val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    //     try { driveAccount = task.getResult(ApiException::class.java) }
    //     catch (_: ApiException) { feedbackMsg = Pair(false, context.getString(R.string.drive_signin_error)) }
    // }
    // val permissionLauncher = rememberLauncherForActivityResult(
    //     ActivityResultContracts.StartActivityForResult()
    // ) { _ -> driveAccount = driveManager.getSignedInAccount() }
    // feedbackMsg?.let { (_, msg) -> LaunchedEffect(msg) { kotlinx.coroutines.delay(3000); feedbackMsg = null } }
    // ─────────────────────────────────────────────────────────────────────────

    // ── Clear confirm ─────────────────────────────────────────────────────────
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.dlg_clear_all_title), fontWeight = FontWeight.ExtraBold, color = Color(0xFFE53935)) },
            text  = { Text(stringResource(R.string.dlg_clear_all_message)) },
            confirmButton = {
                TextButton(onClick = { showClearConfirm = false; onClearAll(); onDismiss() }) {
                    Text(stringResource(R.string.btn_clear_all_confirm), color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // ── Dialog root ───────────────────────────────────────────────────────────
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .graphicsLayer { clip = false }
                .drawBehind {
                    drawRoundRect(appColors.shadow, Offset(5.dp.toPx(), 5.dp.toPx()), size, CornerRadius(16.dp.toPx()))
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
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Header ────────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Storage, null, tint = PinkVivid, modifier = Modifier.size(20.dp))
                            Text(
                                stringResource(R.string.dlg_manage_data_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = PinkVivid
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
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp), tint = appColors.ink)
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(color = appColors.ink.copy(alpha = 0.15f))
                    Spacer(Modifier.height(16.dp))

                    // ══════════════════════════════════════════════════════════
                    // SEÇÃO 1 — DADOS LOCAIS
                    // ══════════════════════════════════════════════════════════
                    SectionTitle(stringResource(R.string.section_local_data), appColors)
                    Spacer(Modifier.height(10.dp))

                    DataActionButton(
                        icon      = Icons.Default.Upload,
                        label     = stringResource(R.string.menu_export_backup),
                        color     = appColors.ink,
                        appColors = appColors,
                        onClick   = { onExport(); onDismiss() }
                    )
                    Spacer(Modifier.height(8.dp))
                    DataActionButton(
                        icon      = Icons.Default.Download,
                        label     = stringResource(R.string.menu_import_backup),
                        color     = appColors.ink,
                        appColors = appColors,
                        onClick   = { onImport(); onDismiss() }
                    )
                    Spacer(Modifier.height(8.dp))
                    DataActionButton(
                        icon      = Icons.Default.DeleteForever,
                        label     = stringResource(R.string.menu_clear_all),
                        color     = Color(0xFFD32F2F),
                        appColors = appColors,
                        onClick   = { showClearConfirm = true }
                    )

                    // ══════════════════════════════════════════════════════════
                    // SEÇÃO 2 — GOOGLE DRIVE  (desativada — reativar no futuro)
                    // Para reativar: descomentar o bloco abaixo e todos os
                    // estados/launchers/#imports marcados com "Google Drive" acima
                    // Pré-requisito: configurar OAuth no Google Cloud Console
                    //   → ver instruções em DriveBackupManager.kt
                    // ══════════════════════════════════════════════════════════
                    //
                    // Spacer(Modifier.height(20.dp))
                    // SectionTitle(stringResource(R.string.section_google_drive), appColors)
                    // Spacer(Modifier.height(10.dp))
                    // [ Card conta Google ]
                    // Box { Surface { if (driveAccount != null) { /* conta conectada */ }
                    //                 else { /* botão conectar */ } } }
                    // Spacer(Modifier.height(10.dp))
                    // [ Botão Salvar no Drive ]
                    // DataActionButton(icon=CloudUpload, color=Color(0xFF4285F4), ...)
                    // Spacer(Modifier.height(8.dp))
                    // [ Botão Restaurar do Drive ]
                    // DataActionButton(icon=CloudDownload, color=Color(0xFF4285F4), ...)
                    // [ Barra de progresso ]
                    // if (isLoading) LinearProgressIndicator(...)
                    // ──────────────────────────────────────────────────────────

                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

// ── Componentes internos ──────────────────────────────────────────────────────

@Composable
private fun SectionTitle(
    title: String,
    appColors: com.jsjstudios.dailyplanner.ui.theme.AppColors
) {
    val lineColor = appColors.ink.copy(alpha = 0.25f)
    val textColor = appColors.ink.copy(alpha = 0.55f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(modifier = Modifier.width(12.dp), color = lineColor)
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = textColor
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = lineColor)
    }
}

@Composable
private fun DataActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    appColors: com.jsjstudios.dailyplanner.ui.theme.AppColors,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val bgColor     = if (enabled) appColors.card else Color.Transparent
    val borderColor = when {
        !enabled         -> appColors.ink.copy(alpha = 0.15f)
        appColors.isDark -> color.copy(alpha = 0.70f)
        else             -> color
    }
    val contentColor = if (enabled) color else appColors.ink.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false }
            .drawBehind {
                if (enabled) drawRoundRect(
                    appColors.shadow,
                    Offset(3.dp.toPx(), 3.dp.toPx()),
                    size,
                    CornerRadius(8.dp.toPx())
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
