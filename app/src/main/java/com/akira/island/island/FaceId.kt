package com.akira.island.island

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.sin

private val FaceIdGreen = Color(0xFF30D158)

/**
 * Значок Face ID: четыре скруглённых угла, внутри лицо (глаза, нос, улыбка), которое слегка «кивает»,
 * пока идёт сканирование, а затем превращается в зелёную галочку.
 *
 * [scan] — ход сканирования, 0..1; [done] — ход превращения в галочку, 0..1.
 * Оба читаются только при рисовании, поэтому анимация не вызывает перекомпоновку.
 */
@Composable
fun FaceIdGlyph(
    size: Dp,
    scan: () -> Float,
    done: () -> Float,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.minDimension
        val d = done().coerceIn(0f, 1f)
        val sc = scan().coerceIn(0f, 1f)
        val color = lerp(tint, FaceIdGreen, d)
        val sw = w * 0.075f
        val stroke = Stroke(width = sw, cap = StrokeCap.Round)

        drawBrackets(w, color, stroke)

        // Лицо: кивок вбок, пока идёт сканирование, и плавное исчезновение, когда рисуется галочка
        val faceAlpha = if (sc > 0f) 1f - d else 0f
        if (faceAlpha > 0f) {
            val nod = (sin(sc * 2f * PI.toFloat() * 1.5f) * w * 0.05f) * (1f - sc * 0.4f)
            translate(left = nod, top = 0f) {
                drawFace(w, tint.copy(alpha = faceAlpha), stroke)
            }
        }

        if (d > 0f) drawCheck(w, color, stroke, d)
    }
}

private fun DrawScope.drawBrackets(w: Float, color: Color, stroke: Stroke) {
    val m = w * 0.07f
    val len = w * 0.27f
    val r = w * 0.16f
    // Верх-лево
    drawLine(color, Offset(m, m + len), Offset(m, m + r), stroke.width, StrokeCap.Round)
    drawArc(color, 180f, 90f, false, Offset(m, m), Size(2 * r, 2 * r), style = stroke)
    drawLine(color, Offset(m + r, m), Offset(m + len, m), stroke.width, StrokeCap.Round)
    // Верх-право
    drawLine(color, Offset(w - m - len, m), Offset(w - m - r, m), stroke.width, StrokeCap.Round)
    drawArc(color, 270f, 90f, false, Offset(w - m - 2 * r, m), Size(2 * r, 2 * r), style = stroke)
    drawLine(color, Offset(w - m, m + r), Offset(w - m, m + len), stroke.width, StrokeCap.Round)
    // Низ-право
    drawLine(color, Offset(w - m, w - m - len), Offset(w - m, w - m - r), stroke.width, StrokeCap.Round)
    drawArc(color, 0f, 90f, false, Offset(w - m - 2 * r, w - m - 2 * r), Size(2 * r, 2 * r), style = stroke)
    drawLine(color, Offset(w - m - r, w - m), Offset(w - m - len, w - m), stroke.width, StrokeCap.Round)
    // Низ-лево
    drawLine(color, Offset(m + len, w - m), Offset(m + r, w - m), stroke.width, StrokeCap.Round)
    drawArc(color, 90f, 90f, false, Offset(m, w - m - 2 * r), Size(2 * r, 2 * r), style = stroke)
    drawLine(color, Offset(m, w - m - r), Offset(m, w - m - len), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawFace(w: Float, color: Color, stroke: Stroke) {
    val sw = stroke.width
    // Глаза
    drawLine(color, Offset(w * 0.36f, w * 0.35f), Offset(w * 0.36f, w * 0.44f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * 0.64f, w * 0.35f), Offset(w * 0.64f, w * 0.44f), sw, StrokeCap.Round)
    // Нос
    drawLine(color, Offset(w * 0.50f, w * 0.40f), Offset(w * 0.50f, w * 0.57f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * 0.50f, w * 0.57f), Offset(w * 0.45f, w * 0.57f), sw, StrokeCap.Round)
    // Улыбка
    drawArc(color, 25f, 130f, false, Offset(w * 0.34f, w * 0.44f), Size(w * 0.32f, w * 0.24f), style = stroke)
}

private fun DrawScope.drawCheck(w: Float, color: Color, stroke: Stroke, progress: Float) {
    val a = Offset(w * 0.31f, w * 0.52f)
    val b = Offset(w * 0.45f, w * 0.65f)
    val c = Offset(w * 0.70f, w * 0.37f)
    val first = (progress / 0.4f).coerceIn(0f, 1f)
    val second = ((progress - 0.4f) / 0.6f).coerceIn(0f, 1f)
    drawLine(color, a, a + (b - a) * first, stroke.width, StrokeCap.Round)
    if (second > 0f) drawLine(color, b, b + (c - b) * second, stroke.width, StrokeCap.Round)
}
