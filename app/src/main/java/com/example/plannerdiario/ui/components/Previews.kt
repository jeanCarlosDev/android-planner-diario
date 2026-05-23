package com.example.plannerdiario.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.plannerdiario.data.Task
import com.example.plannerdiario.ui.theme.PlannerDiarioTheme

// ── TaskItem ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "TaskItem - Normal", widthDp = 360)
@Composable
private fun TaskItemPreview() {
    PlannerDiarioTheme {
        TaskItem(
            task = Task(
                id = 1, listId = 1,
                title = "Estudar Compose",
                description = "Ver documentação oficial"
            ),
            onToggle = {},
            onDelete = {},
            onEdit   = { _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "TaskItem - Concluída", widthDp = 360)
@Composable
private fun TaskItemCompletedPreview() {
    PlannerDiarioTheme {
        TaskItem(
            task = Task(
                id = 2, listId = 1,
                title = "Tarefa concluída",
                isCompleted = true
            ),
            onToggle = {},
            onDelete = {},
            onEdit   = { _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "TaskItem - Com link", widthDp = 360)
@Composable
private fun TaskItemLinkPreview() {
    PlannerDiarioTheme {
        TaskItem(
            task = Task(
                id = 3, listId = 1,
                title = "Ver documentação",
                attachmentUri  = "https://developer.android.com",
                attachmentType = "LINK",
                attachmentName = "developer.android.com"
            ),
            onToggle = {},
            onDelete = {},
            onEdit   = { _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "TaskItem - Dark", widthDp = 360)
@Composable
private fun TaskItemDarkPreview() {
    PlannerDiarioTheme(darkTheme = true) {
        TaskItem(
            task = Task(
                id = 4, listId = 1,
                title = "Tarefa modo noturno",
                description = "Descrição da tarefa"
            ),
            onToggle = {},
            onDelete = {},
            onEdit   = { _, _, _, _, _, _, _ -> }
        )
    }
}

// ── AddTaskDialog ─────────────────────────────────────────────────────────────
// Dialogs não renderizam com @Preview diretamente.
// Usamos AddTaskDialogContent — função interna exposta apenas para preview.

@Preview(showBackground = true, name = "AddTaskDialog - Nova tarefa", widthDp = 360)
@Composable
private fun AddTaskDialogPreview() {
    PlannerDiarioTheme {
        AddTaskDialogContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "AddTaskDialog - Editar tarefa", widthDp = 360)
@Composable
private fun EditTaskDialogPreview() {
    PlannerDiarioTheme {
        AddTaskDialogContent(
            initialTask = Task(
                id = 1, listId = 1,
                title = "Tarefa existente",
                description = "Descrição da tarefa",
                attachmentUri  = "https://google.com",
                attachmentType = "LINK",
                attachmentName = "google.com"
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "AddTaskDialog - Dark", widthDp = 360)
@Composable
private fun AddTaskDialogDarkPreview() {
    PlannerDiarioTheme(darkTheme = true) {
        AddTaskDialogContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
