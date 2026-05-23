package com.example.plannerdiario.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.example.plannerdiario.R
import com.example.plannerdiario.data.LanguagePreferences
import com.example.plannerdiario.ui.components.AddListDialog
import com.example.plannerdiario.ui.components.AddTaskDialog
import com.example.plannerdiario.ui.components.AdBannerView
import com.example.plannerdiario.ui.components.CustomDatePickerDialog
import com.example.plannerdiario.ui.components.DataManagementDialog
import com.example.plannerdiario.ui.components.LanguagePickerDialog
import com.example.plannerdiario.ui.components.availableLanguages
import com.example.plannerdiario.ui.components.ShapeIcon
import com.example.plannerdiario.ui.components.UsageSuggestionDialog
import com.example.plannerdiario.ui.components.TaskItem
import com.example.plannerdiario.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PlannerViewModel,
    currentLanguageTag: String = LanguagePreferences.DEFAULT_TAG,
    onToggleDark: () -> Unit = {},
    onChangeLanguage: (String) -> Unit = {},
    adsRemoved: Boolean = false,
    purchasePrice: String? = null,
    onRemoveAds: () -> Unit = {},
    onInteraction: () -> Unit = {}
) {
    val appColors = LocalAppColors.current
    val context   = LocalContext.current
    val scope     = rememberCoroutineScope()

    val selectedDate    by viewModel.selectedDate.collectAsStateWithLifecycle()
    val taskLists       by viewModel.taskLists.collectAsStateWithLifecycle()
    val selectedList    by viewModel.selectedList.collectAsStateWithLifecycle()
    val tasks           by viewModel.tasks.collectAsStateWithLifecycle()
    val completedDates  by viewModel.completedDates.collectAsStateWithLifecycle()
    val scheduledDates  by viewModel.scheduledDates.collectAsStateWithLifecycle()

    var showDatePicker       by remember { mutableStateOf(false) }
    var showListDropdown     by remember { mutableStateOf(false) }
    var showAddListDialog    by remember { mutableStateOf(false) }
    var showAddTaskDialog    by remember { mutableStateOf(false) }
    var showOptionsMenu      by remember { mutableStateOf(false) }
    var showClearConfirm     by remember { mutableStateOf(false) }
    var showImportError      by remember { mutableStateOf(false) }
    var showImportSuccess    by remember { mutableStateOf(false) }
    var showLanguagePicker   by remember { mutableStateOf(false) }
    var showUsageGuide       by remember { mutableStateOf(false) }
    var showDataManagement   by remember { mutableStateOf(false) }
    var showRemoveAdsDialog  by remember { mutableStateOf(false) }
    var pendingExportJson    by remember { mutableStateOf<String?>(null) }

    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    val locale        = Locale.getDefault()
    val dayOfWeekName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.uppercase() }.take(6).uppercase()
    val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, locale)
    val isToday   = selectedDate == LocalDate.now()
    val listColor = remember(selectedList) {
        try { selectedList?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } }
        catch (_: Exception) { null }
    } ?: PinkVivid
    val listIndex = taskLists.indexOfFirst { it.id == selectedList?.id }

    // ── Launcher: EXPORTAR (criar arquivo JSON) ───────────────────────────
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val json = pendingExportJson ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            } catch (_: Exception) {}
            pendingExportJson = null
        }
    }

    // ── Launcher: IMPORTAR (abrir arquivo JSON) ───────────────────────────
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)
                    ?.use { it.bufferedReader().readText() } ?: return@launch
                viewModel.importBackup(json) { success ->
                    if (success) showImportSuccess = true else showImportError = true
                }
            } catch (_: Exception) { showImportError = true }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────
    if (showDatePicker) {
        CustomDatePickerDialog(
            selectedDate   = selectedDate,
            completedDates = completedDates,
            scheduledDates = scheduledDates,
            onDateSelected = { viewModel.selectDate(it); onInteraction() },
            onDismiss      = { showDatePicker = false }
        )
    }
    if (showAddListDialog) {
        AddListDialog(
            onDismiss = { showAddListDialog = false },
            onConfirm = { name, color, shape ->
                viewModel.addList(name, color, shape)
                showAddListDialog = false
                onInteraction()
            }
        )
    }
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, uri, type, name, isScheduled, repeatDays ->
                viewModel.addTask(title, desc, uri, type, name, isScheduled, repeatDays)
                showAddTaskDialog = false
                onInteraction()
            }
        )
    }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            currentTag = currentLanguageTag,
            onSelect   = { tag -> onChangeLanguage(tag) },
            onDismiss  = { showLanguagePicker = false }
        )
    }

    if (showUsageGuide) {
        UsageSuggestionDialog(onDismiss = { showUsageGuide = false })
    }

    if (showDataManagement) {
        DataManagementDialog(
            onDismiss = { showDataManagement = false },
            onExport  = {
                viewModel.exportBackup { json ->
                    pendingExportJson = json
                    val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                    exportLauncher.launch("planner_backup_$dateStr.json")
                }
            },
            onImport  = {
                importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            onClearAll = {
                viewModel.clearAll()
            },
            onGetBackupJson = { callback ->
                viewModel.exportBackup { json -> callback(json) }
            },
            onImportJson = { json, callback ->
                viewModel.importBackup(json, callback)
            }
        )
    }

    // ── Diálogo: Remover Anúncios ─────────────────────────────────────────
    if (showRemoveAdsDialog) {
        val priceLabel = purchasePrice ?: "..."
        AppConfirmDialog(
            title        = stringResource(R.string.dlg_remove_ads_title),
            message      = stringResource(R.string.dlg_remove_ads_message, priceLabel),
            confirmLabel = stringResource(R.string.btn_purchase, priceLabel),
            onConfirm    = {
                showRemoveAdsDialog = false
                onRemoveAds()
            },
            onDismiss    = { showRemoveAdsDialog = false },
            appColors    = appColors,
            destructive  = false
        )
    }

    // ── Confirmação: Limpar tudo ──────────────────────────────────────────
    if (showClearConfirm) {
                    AppConfirmDialog(
                        title    = stringResource(R.string.dlg_clear_all_title),
                        message  = stringResource(R.string.dlg_clear_all_message),
                        confirmLabel = stringResource(R.string.btn_clear_all_confirm),
                        onConfirm = { viewModel.clearAll(); showClearConfirm = false },
                        onDismiss = { showClearConfirm = false },
                        appColors = appColors,
                        destructive = true
                    )
    }

    // ── Feedback: Importação bem-sucedida ─────────────────────────────────
    if (showImportSuccess) {
        AppConfirmDialog(
            title    = stringResource(R.string.dlg_imported_title),
            message  = stringResource(R.string.dlg_imported_message),
            confirmLabel = stringResource(R.string.btn_ok),
            onConfirm = { showImportSuccess = false },
            onDismiss = { showImportSuccess = false },
            appColors = appColors,
            destructive = false
        )
    }

    // ── Feedback: Erro na importação ──────────────────────────────────────
    if (showImportError) {
        AppConfirmDialog(
            title    = stringResource(R.string.dlg_error_title),
            message  = stringResource(R.string.dlg_import_error_message),
            confirmLabel = stringResource(R.string.btn_ok),
            onConfirm = { showImportError = false },
            onDismiss = { showImportError = false },
            appColors = appColors,
            destructive = false
        )
    }

    // ── Root ──────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Decorative background shapes ──────────────────────────────────
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 60.dp, y = (-55).dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { clip = false }
                .background(appColors.pinkDecor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(110.dp)
                .offset(x = (-45).dp, y = 45.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer { rotationZ = 30f; clip = false }
                .background(appColors.mintDecor, RoundedCornerShape(24.dp))
        )

        // ── Main content ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isTablet) Modifier.widthIn(max = 640.dp) else Modifier)
                .align(Alignment.TopCenter)
        ) {
            // ── TOP BAR ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Grid icon button (left) – abre menu de opções
                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .drawBehind {
                                drawRoundRect(appColors.shadow, Offset(3.dp.toPx(), 3.dp.toPx()), size, CornerRadius(8.dp.toPx()))
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .background(PinkVivid)
                            .clickable { showOptionsMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }

                    // ── Menu de opções ────────────────────────────────────
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        // Gerenciar Dados
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_manage_data), fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.Storage, null, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                Icon(Icons.Default.ChevronRight, null,
                                    modifier = Modifier.size(18.dp),
                                    tint = appColors.ink.copy(alpha = 0.4f))
                            },
                            onClick = { showOptionsMenu = false; showDataManagement = true }
                        )
                        HorizontalDivider()
                        // ── Idioma ─────────────────────────────────────────
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.menu_language),
                                        fontWeight = FontWeight.Medium
                                    )
                                    // badge com a bandeira do idioma atual
                                    val currentFlag = availableLanguages
                                        .firstOrNull { it.tag == currentLanguageTag }?.flag ?: "🌐"
                                    Text(
                                        currentFlag,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Language, null, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                Icon(Icons.Default.ChevronRight, null,
                                    modifier = Modifier.size(18.dp),
                                    tint = appColors.ink.copy(alpha = 0.4f))
                            },
                            onClick = {
                                showOptionsMenu = false
                                showLanguagePicker = true
                            }
                        )
                        // ── Guia Rápido ────────────────────────────────────
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.menu_usage_suggestion),
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                Icon(Icons.Default.ChevronRight, null,
                                    modifier = Modifier.size(18.dp),
                                    tint = appColors.ink.copy(alpha = 0.4f))
                            },
                            onClick = {
                                showOptionsMenu = false
                                showUsageGuide = true
                            }
                        )
                        HorizontalDivider()
                        // ── Remover Anúncios ───────────────────────────────
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (adsRemoved) stringResource(R.string.menu_ads_removed)
                                    else            stringResource(R.string.menu_remove_ads),
                                    fontWeight = FontWeight.Medium,
                                    color = if (adsRemoved) appColors.ink.copy(alpha = 0.5f) else appColors.ink
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (adsRemoved) Icons.Default.CheckCircle else Icons.Default.MoneyOff,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (adsRemoved) appColors.ink.copy(alpha = 0.5f) else appColors.ink
                                )
                            },
                            trailingIcon = {
                                if (!adsRemoved) {
                                    Icon(Icons.Default.ChevronRight, null,
                                        modifier = Modifier.size(18.dp),
                                        tint = appColors.ink.copy(alpha = 0.4f))
                                }
                            },
                            onClick = {
                                showOptionsMenu = false
                                if (!adsRemoved) showRemoveAdsDialog = true
                            },
                            enabled = !adsRemoved
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.app_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = appColors.ink,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    maxLines = 2,
                    overflow = TextOverflow.Clip
                )

                // Dark mode toggle button (right) – mesmo estilo do botão esquerdo
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .drawBehind {
                            drawRoundRect(appColors.shadow, Offset(3.dp.toPx(), 3.dp.toPx()), size, CornerRadius(8.dp.toPx()))
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .background(PinkVivid)
                        .clickable { onToggleDark() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (appColors.isDark) Icons.Default.WbSunny else Icons.Default.DarkMode,
                        contentDescription = if (appColors.isDark) stringResource(R.string.light_mode_label) else stringResource(R.string.dark_mode_label),
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // ── DATE SECTION ─────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dayOfWeekName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = appColors.ink.copy(alpha = 0.55f)
                    )
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .background(YellowFresh, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    stringResource(R.string.today_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = DarkInk
                                )
                            }
                        }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = selectedDate.dayOfMonth.toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = appColors.ink,
                        lineHeight = 72.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(text = monthName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PinkVivid)
                        Text(text = selectedDate.year.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = appColors.ink)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, appColors.ink.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.ink)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_change_date), style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Divider ───────────────────────────────────────────────────
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.5.dp,
                color = appColors.ink.copy(alpha = 0.12f)
            )
            Spacer(Modifier.height(16.dp))

            // ── LIST SELECTOR ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Captura tamanho do card para posicionar o popup
                var selectorHeightPx by remember { mutableStateOf(0) }
                var selectorWidthPx  by remember { mutableStateOf(0) }
                val density = LocalDensity.current

                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { clip = false }
                            .drawBehind {
                                drawRoundRect(appColors.shadow, Offset(4.dp.toPx(), 4.dp.toPx()), size, CornerRadius(8.dp.toPx()))
                            }
                            .onGloballyPositioned { coords ->
                                selectorHeightPx = coords.size.height
                                selectorWidthPx  = coords.size.width
                            },
                        shape  = RoundedCornerShape(8.dp),
                        color  = appColors.card,
                        border = BorderStroke(1.5.dp, appColors.ink),
                        onClick = { showListDropdown = !showListDropdown }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ShapeIcon(
                                shape = selectedList?.shape ?: "circle",
                                color = listColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = selectedList?.name ?: stringResource(R.string.hint_select_list),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedList != null) listColor else appColors.ink.copy(alpha = 0.4f),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (taskLists.isNotEmpty() && listIndex >= 0) {
                                Text(
                                    "${listIndex + 1}/${taskLists.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.ink.copy(alpha = 0.4f)
                                )
                            }
                            Icon(
                                if (showListDropdown) Icons.Default.KeyboardArrowUp
                                else                  Icons.Default.KeyboardArrowDown,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = appColors.ink
                            )
                        }
                    }

                    // ── Popup custom: mesma largura e visual do seletor ────
                    if (showListDropdown) {
                        // Lista local mutável — sincronizada com taskLists uma vez ao abrir
                        val reorderableList = remember(taskLists) { taskLists.toMutableStateList() }
                        // Drag state: usa o ID estável do item, não o índice
                        var draggingId  by remember { mutableStateOf<Long?>(null) }
                        var dragTotalY  by remember { mutableStateOf(0f) }
                        val rowHeights  = remember { mutableStateMapOf<Long, Float>() }

                        Popup(
                            alignment  = Alignment.TopStart,
                            offset     = IntOffset(
                                x = 0,
                                y = selectorHeightPx + with(density) { 6.dp.roundToPx() }
                            ),
                            onDismissRequest = { showListDropdown = false },
                            properties = PopupProperties(focusable = true)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(with(density) { selectorWidthPx.toDp() })
                                    .graphicsLayer { clip = false }
                                    .drawBehind {
                                        drawRoundRect(
                                            appColors.shadow,
                                            Offset(4.dp.toPx(), 4.dp.toPx()),
                                            size,
                                            CornerRadius(8.dp.toPx())
                                        )
                                    }
                            ) {
                                Surface(
                                    shape  = RoundedCornerShape(8.dp),
                                    color  = appColors.card,
                                    border = BorderStroke(1.5.dp, appColors.ink),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (reorderableList.isEmpty()) {
                                    Box(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.no_lists_created),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = appColors.ink.copy(alpha = 0.4f)
                                        )
                                    }
                                    } else {
                                        val avgHeight = rowHeights.values.average()
                                            .takeIf { it.isFinite() && it > 0 } ?: 48.0

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 300.dp)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            reorderableList.forEachIndexed { index, list ->
                                                val isDragging  = draggingId == list.id
                                                val draggingIdx = reorderableList.indexOfFirst { it.id == draggingId }
                                                // Índice alvo visual baseado no offset acumulado
                                                val targetIdx   = if (draggingIdx >= 0)
                                                    (draggingIdx + (dragTotalY / avgHeight).roundToInt())
                                                        .coerceIn(0, reorderableList.lastIndex)
                                                else -1

                                                // Desloca itens não arrastados para abrir/fechar espaço
                                                val itemTranslation = when {
                                                    isDragging -> dragTotalY
                                                    draggingIdx < 0 -> 0f
                                                    index > draggingIdx && index <= targetIdx ->
                                                        -avgHeight.toFloat()
                                                    index < draggingIdx && index >= targetIdx ->
                                                        avgHeight.toFloat()
                                                    else -> 0f
                                                }

                                                val c = try {
                                                    Color(android.graphics.Color.parseColor(list.colorHex))
                                                } catch (_: Exception) { PinkVivid }
                                                val isSelected = list.id == selectedList?.id

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .zIndex(if (isDragging) 10f else 0f)
                                                        .graphicsLayer {
                                                            translationY   = itemTranslation
                                                            shadowElevation = if (isDragging) 12f else 0f
                                                        }
                                                        .onGloballyPositioned {
                                                            rowHeights[list.id] = it.size.height.toFloat()
                                                        }
                                                        .background(
                                                            when {
                                                                isDragging -> appColors.ink.copy(alpha = 0.10f)
                                                                isSelected -> appColors.ink.copy(alpha = 0.06f)
                                                                else       -> Color.Transparent
                                                            }
                                                        )
                                                        // Gesto de arrastar — chave estável = list.id
                                                        .pointerInput(list.id) {
                                                            detectDragGesturesAfterLongPress(
                                                                onDragStart = {
                                                                    draggingId  = list.id
                                                                    dragTotalY  = 0f
                                                                },
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    dragTotalY += dragAmount.y
                                                                },
                                                                onDragEnd = {
                                                                    val fromIdx = reorderableList
                                                                        .indexOfFirst { it.id == draggingId }
                                                                    if (fromIdx >= 0) {
                                                                        val toIdx = (fromIdx + (dragTotalY / avgHeight).roundToInt())
                                                                            .coerceIn(0, reorderableList.lastIndex)
                                                                        if (toIdx != fromIdx) {
                                                                            val item = reorderableList.removeAt(fromIdx)
                                                                            reorderableList.add(toIdx, item)
                                                                        }
                                                                        viewModel.reorderLists(reorderableList.map { it.id })
                                                                    }
                                                                    draggingId = null
                                                                    dragTotalY = 0f
                                                                },
                                                                onDragCancel = {
                                                                    draggingId = null
                                                                    dragTotalY = 0f
                                                                }
                                                            )
                                                        }
                                                        .clickable {
                                                            if (draggingId == null) {
                                                                viewModel.selectList(list.id)
                                                                showListDropdown = false
                                                            }
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 13.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DragHandle, null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = appColors.ink.copy(alpha = 0.3f)
                                                    )
                                                    ShapeIcon(
                                                        shape = list.shape,
                                                        color = c,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = list.name,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) c else appColors.ink,
                                                        modifier = Modifier.weight(1f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.Check, null,
                                                            tint = PinkVivid,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                if (index < reorderableList.lastIndex) {
                                                    HorizontalDivider(
                                                        color = appColors.ink.copy(alpha = 0.08f),
                                                        thickness = 1.dp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // + Add List button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .graphicsLayer { clip = false }
                        .drawBehind {
                            drawRoundRect(appColors.shadow, Offset(4.dp.toPx(), 4.dp.toPx()), size, CornerRadius(8.dp.toPx()))
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .background(PinkVivid)
                        .clickable { showAddListDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, stringResource(R.string.btn_new_list), tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── TASK LIST ─────────────────────────────────────────────────
            when {
                selectedList == null && taskLists.isEmpty() -> EmptyState(
                    Icons.Default.PlaylistAdd, stringResource(R.string.empty_no_lists_title),
                    stringResource(R.string.empty_no_lists_subtitle), appColors
                )
                selectedList == null -> EmptyState(
                    Icons.Default.FormatListBulleted, stringResource(R.string.empty_select_list_title),
                    stringResource(R.string.empty_select_list_subtitle), appColors
                )
                tasks.isEmpty() -> EmptyState(
                    Icons.Default.CheckCircleOutline, stringResource(R.string.empty_no_tasks_title),
                    stringResource(R.string.empty_no_tasks_subtitle), appColors
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItem(
                            task      = task,
                            listColor = listColor,
                            onToggle  = { viewModel.toggleTask(task); onInteraction() },
                            onDelete  = { viewModel.deleteTask(task); onInteraction() },
                            onEdit    = { title, desc, uri, type, name, _, _ ->
                                viewModel.updateTask(task, title, desc, uri, type, name)
                                onInteraction()
                            }
                        )
                    }
                }
            }
        }

        // ── FAB centralizado ──────────────────────────────────────────────
        if (selectedList != null) {
            Box(modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (adsRemoved) 24.dp else 76.dp)
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { clip = false }
                        .drawBehind {
                            drawRoundRect(appColors.shadow, Offset(4.dp.toPx(), 4.dp.toPx()), size, CornerRadius(8.dp.toPx()))
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .background(PinkVivid)
                        .clickable { showAddTaskDialog = true }
                        .padding(horizontal = 28.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Text(
                            stringResource(R.string.btn_new_task),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // ── Banner de anúncio (rodapé fixo) ──────────────────────────────
        if (!adsRemoved) {
            AdBannerView(
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    appColors: AppColors
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(64.dp), tint = appColors.ink.copy(alpha = 0.2f))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            color = appColors.ink, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium,
            color = appColors.ink.copy(alpha = 0.5f), textAlign = TextAlign.Center)
    }
}

// ── Diálogo de confirmação / feedback estilizado ──────────────────────────────
@Composable
private fun AppConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    appColors: AppColors,
    destructive: Boolean = false,
    showCancel: Boolean = true
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = if (destructive) Color(0xFFE53935) else PinkVivid
                    )
                    Text(
                        message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = appColors.ink.copy(alpha = 0.8f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        if (showCancel) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, appColors.ink.copy(alpha = 0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.ink)
                            ) { Text(stringResource(R.string.btn_cancel)) }
                        }
                        // Botão estilizado com sombra
                        val btnColor = if (destructive) Color(0xFFE53935) else PinkVivid
                        Box(
                            modifier = Modifier
                                .graphicsLayer { clip = false }
                                .drawBehind {
                                    drawRoundRect(appColors.shadow, Offset(3.dp.toPx(), 3.dp.toPx()), size, CornerRadius(8.dp.toPx()))
                                }
                        ) {
                            Button(
                                onClick = onConfirm,
                                shape  = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, appColors.ink),
                                colors = ButtonDefaults.buttonColors(containerColor = btnColor)
                            ) {
                                Text(confirmLabel, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

