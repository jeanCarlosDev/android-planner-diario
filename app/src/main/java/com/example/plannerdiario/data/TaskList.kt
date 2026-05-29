package com.jsjstudios.dailyplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_lists")
data class TaskList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#EC407A",
    val shape: String = "circle",
    val sortOrder: Int = 0
)

data class ColorShapeOption(
    val colorHex: String,
    val shape: String
)

val listPresetOptions = listOf(
    ColorShapeOption("#EC407A", "circle"),     // Rosa    → Círculo
    ColorShapeOption("#F9A825", "star"),       // Amarelo escuro → Estrela
    ColorShapeOption("#26C6DA", "triangle"),   // Ciano   → Triângulo
    ColorShapeOption("#AB47BC", "diamond"),    // Roxo    → Losango
    ColorShapeOption("#FF7043", "rectangle"),  // Laranja → Retângulo
    ColorShapeOption("#66BB6A", "hexagon"),    // Verde   → Hexágono
)

// backward-compat alias
val listPresetColors = listPresetOptions.map { it.colorHex }

