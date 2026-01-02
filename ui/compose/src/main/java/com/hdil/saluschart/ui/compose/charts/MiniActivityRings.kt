package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.hdil.saluschart.core.chart.ProgressChartMark
import kotlin.math.floor
import kotlin.math.min

@Composable
fun MiniActivityRings(
    modifier: Modifier = Modifier,
    rings: List<ProgressChartMark>,
    colors: List<Color>,
    strokeWidth: Float,          // px
    maxLaps: Int = 2,            // show up to 200%
    trackAlpha: Float = 0.22f,
    gapRatio: Float = 0.22f,
    startAngle: Float = -90f,
) {
    if (rings.isEmpty()) return

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)

        val maxRadius = (min(size.width, size.height) / 2f) - strokeWidth / 2f
        if (maxRadius <= 0f) return@Canvas

        val gap = strokeWidth * gapRatio
        val ringStep = strokeWidth + gap

        rings.forEachIndexed { i, mark ->
            val radius = maxRadius - i * ringStep
            if (radius <= 0f) return@forEachIndexed

            val d = radius * 2f
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(d, d)

            val ringColor = colors.getOrElse(i) { Color.White }
            val trackColor = ringColor.copy(alpha = trackAlpha)

            // Track (full 360)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // raw ratio is UNCLAMPED current/max, not mark.progress (which is clamped to 1)
            val raw: Double =
                if (mark.max > 0.0) (mark.current / mark.max) else 0.0

            if (raw <= 0.0) return@forEachIndexed

            // Cap to maxLaps for display (Apple-like)
            val capped = raw.coerceAtMost(maxLaps.toDouble())

            val laps = floor(capped).toInt().coerceAtLeast(0)
            val residual = (capped - laps).coerceIn(0.0, 1.0) // fraction of next lap
            val residualDeg = (residual * 360.0).toFloat()

            // 1) Draw completed laps (each is ~360 with seam hidden)
            // Big chart uses 359.6f to hide seam, we do the same.
            repeat(laps.coerceAtMost(maxLaps)) {
                drawArc(
                    color = ringColor,
                    startAngle = startAngle,
                    sweepAngle = 359.6f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 2) Draw residual lap on top
            if (residualDeg > 0f && laps < maxLaps) {
                drawArc(
                    color = ringColor,
                    startAngle = startAngle,
                    sweepAngle = residualDeg,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
    }
}
