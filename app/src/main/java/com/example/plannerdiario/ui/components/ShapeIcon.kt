package com.jsjstudios.dailyplanner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws a geometric shape filled with [color].
 * Supported shapes: "circle", "star", "triangle", "diamond", "rectangle", "hexagon"
 */
@Composable
fun ShapeIcon(
    shape: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        when (shape) {
            "star"      -> drawPath(starPath(size), color)
            "triangle"  -> drawPath(trianglePath(size), color)
            "diamond"   -> drawPath(diamondPath(size), color)
            "rectangle" -> {
                val inset = size.width * 0.1f
                drawRect(
                    color = color,
                    topLeft = Offset(inset, inset * 1.8f),
                    size = Size(size.width - inset * 2, size.height - inset * 3.6f)
                )
            }
            "hexagon"   -> drawPath(hexagonPath(size), color)
            else        -> drawCircle(color) // "circle" + fallback
        }
    }
}

// ── Path helpers ─────────────────────────────────────────────────────────────

private fun starPath(size: Size): Path {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerR = minOf(size.width, size.height) / 2f
    val innerR = outerR * 0.42f
    val path = Path()
    for (i in 0..9) {
        val angle = (i * 36.0 - 90.0) * PI / 180.0
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + (r * cos(angle)).toFloat()
        val y = cy + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun trianglePath(size: Size): Path {
    val p = size.width * 0.05f
    val path = Path()
    path.moveTo(size.width / 2f, p)
    path.lineTo(size.width - p, size.height - p)
    path.lineTo(p, size.height - p)
    path.close()
    return path
}

private fun diamondPath(size: Size): Path {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val path = Path()
    path.moveTo(cx, 0f)
    path.lineTo(size.width, cy)
    path.lineTo(cx, size.height)
    path.lineTo(0f, cy)
    path.close()
    return path
}

private fun hexagonPath(size: Size): Path {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = minOf(size.width, size.height) / 2f
    val path = Path()
    for (i in 0..5) {
        val angle = (i * 60.0 - 30.0) * PI / 180.0
        val x = cx + (r * cos(angle)).toFloat()
        val y = cy + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

