package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import kotlin.math.cos
import kotlin.math.sin

/**
 * Low-level drawing helpers for progress charts.
 *
 * Supports two visual modes:
 * - Donut rings (multi-ring progress)
 * - Horizontal progress bars (one row per item)
 *
 * These functions are intentionally low-level and expect the caller to provide layout context
 * (canvas [Size], stroke widths, paddings, etc.).
 */
object ProgressChartDraw {

    /**
     * Draws progress marks either as donut rings or horizontal bars.
     *
     * Donut mode:
     * - Computes ring radii using [ChartMath.Progress.computeProgressDonutMetrics].
     * - Draws a faint background track (full 360°).
     * - Draws progress:
     *   - `0..1` => a single arc with a subtle linear gradient.
     *   - `>1`  => one full lap + an extra lap arc on top (capped at 2.0).
     *
     * Bar mode:
     * - Draws a faint background track for each row and an overlay filled segment
     *   proportional to `point.progress` clamped to `[0, 1]`.
     *
     * @param drawScope Compose draw scope.
     * @param data Progress data items.
     * @param size Canvas size.
     * @param colors Colors used per item (falls back to the first color if missing).
     * @param isDonut If true, render donut rings; otherwise render horizontal bars.
     * @param strokeWidth Ring thickness in pixels (donut mode).
     * @param barHeight Bar height in pixels (bar mode).
     * @param backgroundAlpha Alpha applied to the background track color.
     * @param barSpacing Vertical spacing between bar rows (bar mode).
     * @param topPadding Top padding before the first bar row (bar mode).
     * @param cornerRadius Corner radius used for rounded bar tracks/fills (bar mode).
     */
    fun drawProgressMarks(
        drawScope: DrawScope,
        data: List<ProgressChartMark>,
        size: Size,
        colors: List<Color>,
        isDonut: Boolean,
        strokeWidth: Float = 40f,
        barHeight: Float = 30f,
        backgroundAlpha: Float = 0.1f,
        barSpacing: Float = 16f,
        topPadding: Float = 8f,
        cornerRadius: Float = barHeight / 2f
    ) {
        if (isDonut) {
            val (center, _, ringRadii) = ChartMath.Progress.computeProgressDonutMetrics(
                size = size,
                data = data,
                strokeWidth = strokeWidth
            )

            // Simple color helpers (kept local to preserve existing rendering logic).
            fun lighten(c: Color, f: Float) = Color(
                (c.red + (1f - c.red) * f).coerceIn(0f, 1f),
                (c.green + (1f - c.green) * f).coerceIn(0f, 1f),
                (c.blue + (1f - c.blue) * f).coerceIn(0f, 1f),
                c.alpha
            )

            fun darken(c: Color, f: Float) = Color(
                (c.red * (1f - f)).coerceIn(0f, 1f),
                (c.green * (1f - f)).coerceIn(0f, 1f),
                (c.blue * (1f - f)).coerceIn(0f, 1f),
                c.alpha
            )

            data.forEachIndexed { idx, pt ->
                if (idx >= ringRadii.size) return@forEachIndexed

                val radius = ringRadii[idx]
                val base = colors.getOrElse(idx) { colors.first() }

                val ringRect = Rect(
                    left = center.x - radius,
                    top = center.y - radius,
                    right = center.x + radius,
                    bottom = center.y + radius
                )

                // 1) Background track (full ring).
                drawScope.drawArc(
                    color = base.copy(alpha = backgroundAlpha),
                    startAngle = 0f,
                    sweepAngle = 359.9f,
                    useCenter = false,
                    topLeft = Offset(ringRect.left, ringRect.top),
                    size = Size(ringRect.width, ringRect.height),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // 2) Progress arc(s).
                val raw = if (pt.max > 0.0) pt.current / pt.max else pt.progress
                val progress = raw.coerceIn(0.0, 2.0)
                if (progress <= 0.0) return@forEachIndexed

                val startAt = -90f

                /**
                 * Creates a linear gradient aligned between the arc head/tail positions for
                 * the given [sweep] and [radius].
                 *
                 * Note: This intentionally uses a linear gradient (not a sweep gradient)
                 * to match the existing look.
                 */
                fun arcGradient(radius: Float, sweep: Float, tailDark: Boolean): Brush {
                    val startRad = Math.toRadians(startAt.toDouble())
                    val endRad = Math.toRadians((startAt + sweep).toDouble())

                    val startPoint = Offset(
                        x = center.x + radius * cos(startRad).toFloat(),
                        y = center.y + radius * sin(startRad).toFloat()
                    )
                    val endPoint = Offset(
                        x = center.x + radius * cos(endRad).toFloat(),
                        y = center.y + radius * sin(endRad).toFloat()
                    )

                    val tailColor = if (tailDark) darken(base, 0.2f) else base
                    val midColor = base
                    val headColor = lighten(base, 0.35f)

                    return Brush.linearGradient(
                        colors = listOf(tailColor, midColor, headColor),
                        start = startPoint,
                        end = endPoint
                    )
                }

                if (progress <= 1.0) {
                    // 0–100%: single arc with gradient.
                    val sweep = (progress * 360.0).toFloat()
                    val brush = arcGradient(radius, sweep, tailDark = true)

                    drawScope.drawArc(
                        brush = brush,
                        startAngle = startAt,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(ringRect.left, ringRect.top),
                        size = Size(ringRect.width, ringRect.height),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                } else {
                    // >100%: one full lap + extra lap on top (clamped to an additional 0–100%).
                    drawScope.drawArc(
                        color = base,
                        startAngle = startAt,
                        sweepAngle = 359.9f,
                        useCenter = false,
                        topLeft = Offset(ringRect.left, ringRect.top),
                        size = Size(ringRect.width, ringRect.height),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )

                    val extraRatio = (progress - 1.0).coerceIn(0.0, 1.0)
                    if (extraRatio > 0.0) {
                        val extraSweep = (extraRatio * 360.0).toFloat()
                        val extraBrush = arcGradient(radius, extraSweep, tailDark = false)

                        drawScope.drawArc(
                            brush = extraBrush,
                            startAngle = startAt,
                            sweepAngle = extraSweep,
                            useCenter = false,
                            topLeft = Offset(ringRect.left, ringRect.top),
                            size = Size(ringRect.width, ringRect.height),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        } else {
            // Bar mode.
            val leftPadding = 90f
            val rightPadding = 100f
            val trackWidth = (size.width - leftPadding - rightPadding).coerceAtLeast(0f)

            data.forEachIndexed { index, point ->
                val y = topPadding + index * (barHeight + barSpacing)
                val color = colors.getOrElse(index) { colors.first() }
                val progressWidth = trackWidth * point.progress.coerceIn(0.0, 1.0).toFloat()

                drawScope.drawRoundRect(
                    color = color.copy(alpha = backgroundAlpha),
                    topLeft = Offset(leftPadding, y),
                    size = Size(trackWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )

                if (progressWidth > 0f) {
                    drawScope.drawRoundRect(
                        color = color,
                        topLeft = Offset(leftPadding, y),
                        size = Size(progressWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                    )
                }
            }
        }
    }

    /**
     * Draws progress labels for each progress item.
     *
     * Donut mode:
     * - Label positions are computed per ring using [ChartMath.Progress.computeLabelPosition].
     *
     * Bar mode:
     * - Labels are drawn on the left side, aligned right, centered vertically per row.
     *
     * @param drawScope Compose draw scope.
     * @param data Progress data items.
     * @param size Canvas size.
     * @param isDonut If true, render donut labels; otherwise render bar labels.
     * @param strokeWidth Ring thickness in pixels (donut mode).
     * @param barHeight Bar height in pixels (bar mode).
     * @param textSize Text size in pixels.
     * @param barSpacing Vertical spacing between bar rows (bar mode).
     * @param topPadding Top padding before the first bar row (bar mode).
     */
    fun drawProgressLabels(
        drawScope: DrawScope,
        data: List<ProgressChartMark>,
        size: Size,
        isDonut: Boolean,
        strokeWidth: Float = 40f,
        barHeight: Float = 30f,
        textSize: Float = 32f,
        barSpacing: Float = 16f,
        topPadding: Float = 8f
    ) {
        if (isDonut) {
            val (center, _, ringRadii) = ChartMath.Progress.computeProgressDonutMetrics(
                size = size,
                data = data,
                strokeWidth = strokeWidth
            )

            data.forEachIndexed { index, point ->
                val radius = ringRadii.getOrElse(index) { 0f }
                val labelPosition = ChartMath.Progress.computeLabelPosition(
                    center = center,
                    radius = radius,
                    isDonut = true,
                    point = point,
                )

                point.label?.let { label ->
                    drawScope.drawContext.canvas.nativeCanvas.drawText(
                        label,
                        labelPosition.x,
                        labelPosition.y,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.DKGRAY
                            this.textSize = textSize
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )
                }
            }
        } else {
            val leftX = 64f

            data.forEachIndexed { index, point ->
                val centerY = topPadding + index * (barHeight + barSpacing) + barHeight / 2f

                point.label?.let { label ->
                    drawScope.drawContext.canvas.nativeCanvas.drawText(
                        label,
                        leftX,
                        centerY + textSize * 0.35f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.DKGRAY
                            this.textSize = textSize
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }
    }

    /**
     * Draws progress values for each progress item.
     *
     * Value formatting:
     * - If [isPercentage] is true: uses `"NN%"`
     * - Otherwise: uses `"current [unit] / max [unit]"` (unit is appended when present)
     *
     * Donut mode:
     * - Value positions are computed per ring using [ChartMath.Progress.computeValuePosition].
     *
     * Bar mode:
     * - Values are drawn on the right side, aligned right, centered vertically per row.
     *
     * @param drawScope Compose draw scope.
     * @param data Progress data items.
     * @param size Canvas size.
     * @param isDonut If true, render donut values; otherwise render bar values.
     * @param strokeWidth Ring thickness in pixels (donut mode).
     * @param barHeight Bar height in pixels (bar mode).
     * @param textSize Text size in pixels.
     * @param isPercentage If true, display percentage; otherwise display current/max.
     * @param barSpacing Vertical spacing between bar rows (bar mode).
     * @param topPadding Top padding before the first bar row (bar mode).
     */
    fun drawProgressValues(
        drawScope: DrawScope,
        data: List<ProgressChartMark>,
        size: Size,
        isDonut: Boolean,
        strokeWidth: Float = 40f,
        barHeight: Float = 30f,
        textSize: Float = 28f,
        isPercentage: Boolean,
        barSpacing: Float = 16f,
        topPadding: Float = 8f
    ) {
        if (isDonut) {
            val (center, _, ringRadii) = ChartMath.Progress.computeProgressDonutMetrics(
                size = size,
                data = data,
                strokeWidth = strokeWidth
            )

            data.forEachIndexed { index, point ->
                val radius = ringRadii.getOrElse(index) { 0f }
                val valuePosition = ChartMath.Progress.computeValuePosition(
                    center = center,
                    radius = radius,
                    isDonut = true,
                    point = point
                )

                val valueText = if (isPercentage) {
                    "${(point.percentage).toInt()}%"
                } else {
                    buildString {
                        append("${point.current.toInt()}")
                        point.unit?.let { append(" $it") }
                        append(" / ${point.max.toInt()}")
                        point.unit?.let { append(" $it") }
                    }
                }

                drawScope.drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    valuePosition.x,
                    valuePosition.y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        this.textSize = textSize
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        } else {
            val rightX = size.width - 24f

            data.forEachIndexed { index, point ->
                val centerY = topPadding + index * (barHeight + barSpacing) + barHeight / 2f

                val valueText = if (isPercentage) {
                    "${point.percentage.toInt()}%"
                } else {
                    buildString {
                        append(point.current.toInt())
                        point.unit?.let { append(" $it") }
                        append(" / ${point.max.toInt()}")
                        point.unit?.let { append(" $it") }
                    }
                }

                drawScope.drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    rightX,
                    centerY + textSize * 0.35f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        this.textSize = textSize
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }
        }
    }

    /**
     * Draws the centered “summary” text inside a donut progress chart.
     *
     * This is only intended for donut mode. The caller controls when to draw it.
     *
     * @param drawScope Compose draw scope.
     * @param center Center of the donut in canvas coordinates.
     * @param title Top line text.
     * @param subtitle Bottom line text.
     * @param titleSize Title text size in pixels.
     * @param subtitleSize Subtitle text size in pixels.
     */
    fun drawProgressCenterInfo(
        drawScope: DrawScope,
        center: Offset,
        title: String = "Activity",
        subtitle: String = "Progress",
        titleSize: Float = 36f,
        subtitleSize: Float = 24f
    ) {
        drawScope.drawContext.canvas.nativeCanvas.drawText(
            title,
            center.x,
            center.y - 10f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = titleSize
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
        )

        drawScope.drawContext.canvas.nativeCanvas.drawText(
            subtitle,
            center.x,
            center.y + 20f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = subtitleSize
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
    }
}