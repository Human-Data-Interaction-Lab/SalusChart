package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import kotlin.math.abs
import kotlin.math.round

/**
 * Placement for the Y-axis within a chart.
 */
enum class YAxisPosition {
    /** Y-axis is drawn on the left edge of the chart. */
    LEFT,

    /** Y-axis is drawn on the right edge of the chart. */
    RIGHT
}

/**
 * Shared drawing utilities for chart primitives (axes, grid, tick labels) and accessors
 * to chart-specific draw helpers.
 *
 * This object is intended as a central entry point for chart rendering helpers used across
 * multiple chart types (line, bar, pie, etc.).
 */
object ChartDraw {

    // Chart-specific drawers (kept as properties for existing call sites / API stability).
    var Pie = PieChartDraw
    val Line = LineChartDraw
    val Bar = BarChartDraw
    val Scatter = ScatterPlotDraw
    val Progress = ProgressChartDraw
    val Gauge = GaugeChartDraw

    /**
     * Formats a tick value into a compact human-readable string.
     *
     * Formatting rules:
     * - `0` is shown as `"0"`
     * - `>= 1,000,000` uses `"M"` (millions) with 1 decimal (e.g., `1.2M`)
     * - `>= 1,000` uses `"K"` (thousands) with 1 decimal (e.g., `3.4K`)
     * - Integer values are shown without decimals
     * - Otherwise, 1 decimal place is used
     *
     * @param value Tick value.
     * @return Formatted tick label.
     */
    fun formatTickLabel(value: Float): String {
        return when {
            value == 0f -> "0"
            value >= 1_000_000f -> "%.1fM".format(value / 1_000_000f)
            value >= 1_000f -> "%.1fK".format(value / 1_000f)
            value % 1f == 0f -> "%.0f".format(value)
            else -> "%.1f".format(value)
        }
    }

    /**
     * Draws horizontal grid lines for each Y tick and optionally draws Y-axis tick labels.
     *
     * Grid lines span the entire chart plotting area ([metrics.paddingX] to
     * `[metrics.paddingX] + [metrics.chartWidth]`).
     *
     * @param drawScope Compose draw scope.
     * @param size Full canvas size (currently unused; kept for API stability / future use).
     * @param metrics Chart layout and tick information.
     * @param yAxisPosition Which side the Y-axis labels should appear on.
     * @param drawLabels If true, tick labels are drawn next to the Y-axis.
     */
    fun drawGrid(
        drawScope: DrawScope,
        size: Size,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
        drawLabels: Boolean = true
    ) {
        val denom = (metrics.maxY - metrics.minY)
        if (denom == 0.0) return

        // Y-axis line X position (used to place labels).
        val yAxisX = when (yAxisPosition) {
            YAxisPosition.RIGHT -> metrics.paddingX + metrics.chartWidth
            YAxisPosition.LEFT -> metrics.paddingX
        }

        metrics.yTicks.forEach { yVal ->
            // Convert chart-relative Y to canvas coordinates.
            val y =
                metrics.paddingY +
                        metrics.chartHeight -
                        ((yVal - metrics.minY) / denom) * metrics.chartHeight

            val gridStart = metrics.paddingX
            val gridEnd = metrics.paddingX + metrics.chartWidth

            drawScope.drawLine(
                color = Color.LightGray,
                start = Offset(gridStart, y.toFloat()),
                end = Offset(gridEnd, y.toFloat()),
                strokeWidth = 1f
            )

            if (!drawLabels) return@forEach

            val labelText = formatTickLabel(yVal.toFloat())

            val labelX = when (yAxisPosition) {
                YAxisPosition.RIGHT -> yAxisX + 5f
                YAxisPosition.LEFT -> (metrics.paddingX - 5f).coerceAtLeast(15f)
            }

            val textAlign = when (yAxisPosition) {
                YAxisPosition.RIGHT -> android.graphics.Paint.Align.LEFT
                YAxisPosition.LEFT -> android.graphics.Paint.Align.RIGHT
            }

            drawScope.drawContext.canvas.nativeCanvas.drawText(
                labelText,
                labelX,
                y.toFloat() + 10f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 28f
                    this.textAlign = textAlign
                }
            )
        }
    }

    /**
     * Draws the X-axis baseline at the bottom of the chart plotting area.
     *
     * @param drawScope Compose draw scope.
     * @param metrics Chart metrics used to locate the baseline.
     */
    fun drawXAxis(drawScope: DrawScope, metrics: ChartMath.ChartMetrics) {
        drawScope.drawLine(
            color = Color.Black,
            start = Offset(metrics.paddingX, metrics.paddingY + metrics.chartHeight),
            end = Offset(metrics.paddingX + metrics.chartWidth, metrics.paddingY + metrics.chartHeight),
            strokeWidth = 2f
        )
    }

    /**
     * Draws the Y-axis baseline on the left or right edge of the chart plotting area.
     *
     * @param drawScope Compose draw scope.
     * @param metrics Chart metrics used to locate the axis.
     * @param yAxisPosition Which side to draw the Y-axis on.
     */
    fun drawYAxis(
        drawScope: DrawScope,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition = YAxisPosition.LEFT
    ) {
        val axisStartX = when (yAxisPosition) {
            YAxisPosition.RIGHT -> metrics.paddingX + metrics.chartWidth
            YAxisPosition.LEFT -> metrics.paddingX
        }

        drawScope.drawLine(
            color = Color.Black,
            start = Offset(axisStartX, metrics.paddingY),
            end = Offset(axisStartX, metrics.paddingY + metrics.chartHeight),
            strokeWidth = 2f
        )
    }

    /**
     * Draws a standalone (fixed) Y-axis pane, used for charts with horizontal paging/scrolling
     * where the plot area moves but the Y-axis remains fixed.
     *
     * This function:
     * - Draws the axis line at the pane edge
     * - Draws tick marks
     * - Draws tick labels
     * - Optionally highlights certain tick labels with a “pill” background
     *
     * Highlighting:
     * - A tick is considered highlighted if any entry in [highlightValues] is within
     *   [highlightTolerance] of the tick value.
     * - The highlight text/pill color is derived from [highlightColorForValue].
     *
     * Extra ticks:
     * - [extraTickValues] may be provided to render additional labels (e.g., for reference lines).
     * - Ticks are merged with [metrics.yTicks], distinct, and sorted.
     *
     * @param drawScope Compose draw scope to draw into.
     * @param metrics Chart metrics describing Y-range, padding, height, and base ticks.
     * @param yAxisPosition Whether the axis is at the left or right edge of the pane.
     * @param paneWidthPx Width of this Y-axis pane in pixels.
     * @param labelTextSizePx Tick label text size in pixels.
     * @param highlightValues Y-values to highlight (approximate match within [highlightTolerance]).
     * @param highlightColorForValue Function mapping a highlighted value to a text color.
     * @param extraTickValues Additional tick values to include besides [metrics.yTicks].
     * @param highlightTolerance Absolute tolerance used to match ticks for highlighting.
     */
    fun drawYAxisStandalone(
        drawScope: DrawScope,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition,
        paneWidthPx: Float,
        labelTextSizePx: Float = 28f,
        highlightValues: List<Double> = emptyList(),
        highlightColorForValue: (Double) -> Color = { Color.Transparent },
        extraTickValues: List<Double> = emptyList(),
        highlightTolerance: Double = 1e-6
    ) {
        val denom = (metrics.maxY - metrics.minY)
        if (denom == 0.0) return

        // Axis X anchored to the inner edge of the pane (the side touching the chart area).
        // LEFT pane: axis at right edge (touching chart). RIGHT pane: axis at left edge.
        val axisX = if (yAxisPosition == YAxisPosition.LEFT) paneWidthPx - 0.5f else 0.5f

        // Axis line.
        drawScope.drawLine(
            color = Color.Black,
            start = Offset(axisX, metrics.paddingY),
            end = Offset(axisX, metrics.paddingY + metrics.chartHeight),
            strokeWidth = 2f
        )

        val mergedTicks = (metrics.yTicks + extraTickValues)
            .distinct()
            .sorted()

        val tickLen = 8f
        val padX = 12f
        val padY = 8f
        val labelGap = padX + 2f

        val normalPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.DKGRAY
            textSize = labelTextSizePx
            textAlign = if (yAxisPosition == YAxisPosition.RIGHT) {
                android.graphics.Paint.Align.LEFT
            } else {
                android.graphics.Paint.Align.RIGHT
            }
        }

        val highlightPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = labelTextSizePx
            textAlign = normalPaint.textAlign
        }

        val pillPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }

        val fm = normalPaint.fontMetrics
        val textHeight = (fm.descent - fm.ascent)

        drawScope.drawContext.canvas.nativeCanvas.apply {
            mergedTicks.forEach { yVal ->
                // Skip ticks outside visible range.
                if (yVal < metrics.minY - 1e-9 || yVal > metrics.maxY + 1e-9) return@forEach

                val y =
                    metrics.paddingY +
                            metrics.chartHeight -
                            (((yVal - metrics.minY) / denom) * metrics.chartHeight).toFloat()

                // Tick mark extends inward (away from the chart area).
                // LEFT pane: axis at right edge → tick extends left. RIGHT pane: axis at left edge → tick extends right.
                val tickEndX =
                    if (yAxisPosition == YAxisPosition.LEFT) axisX - tickLen else axisX + tickLen

                drawScope.drawLine(
                    color = Color.DarkGray,
                    start = Offset(axisX, y),
                    end = Offset(tickEndX, y),
                    strokeWidth = 1f
                )

                val label = formatTickLabel(yVal.toFloat())

                // Center text visually on the tick mark.
                val centeredY = y - (fm.ascent + fm.descent) / 2f

                val isHighlighted = highlightValues.any { hv ->
                    kotlin.math.abs(hv - yVal) <= highlightTolerance
                }

                val labelX =
                    if (yAxisPosition == YAxisPosition.RIGHT) axisX + labelGap else axisX - labelGap

                if (isHighlighted) {
                    val c = highlightColorForValue(yVal)

                    highlightPaint.color = c.toArgb()
                    pillPaint.color = c.copy(alpha = 0.18f).toArgb()

                    val advance = highlightPaint.measureText(label)

                    val pillH = textHeight + padY * 2f
                    val pillRadius = pillH / 2f

                    // Center the pill on the text's visual midpoint.
                    val textCenterX = if (yAxisPosition == YAxisPosition.RIGHT) {
                        labelX + advance / 2f
                    } else {
                        labelX - advance / 2f
                    }
                    val halfPill = advance / 2f + padX
                    val left = textCenterX - halfPill
                    val right = textCenterX + halfPill

                    val top = centeredY + fm.ascent - padY
                    val bottom = centeredY + fm.descent + padY

                    drawRoundRect(
                        left,
                        top,
                        right,
                        bottom,
                        pillRadius,
                        pillRadius,
                        pillPaint
                    )

                    drawText(label, labelX, centeredY, highlightPaint)
                } else {
                    drawText(label, labelX, centeredY, normalPaint)
                }
            }
        }
    }

    /**
     * Convenience wrapper around [drawYAxisStandalone] that automatically derives highlight pills
     * from [referenceLines]: each line's y-value (and yEnd for ZONE) is highlighted in the axis
     * using the corresponding reference line color.
     */
    fun drawYAxisStandaloneWithReferenceHighlights(
        drawScope: DrawScope,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition,
        paneWidthPx: Float,
        referenceLines: List<ReferenceLineSpec>
    ) {
        val tol = 1e-6
        fun norm(v: Double) = round(v * 1_000_000.0) / 1_000_000.0

        val allRefYValues = referenceLines.flatMap { spec ->
            if (spec.type == ReferenceLineType.ZONE) listOfNotNull(spec.y, spec.yEnd)
            else listOf(spec.y)
        }
        val baseTicks = metrics.yTicks.map(::norm)
        val highlightTargets = allRefYValues.map { y ->
            val ny = norm(y)
            baseTicks.firstOrNull { abs(it - ny) <= tol } ?: ny
        }
        val extraTicks = highlightTargets.filter { target ->
            baseTicks.none { abs(it - target) <= tol }
        }

        drawYAxisStandalone(
            drawScope = drawScope,
            metrics = metrics,
            yAxisPosition = yAxisPosition,
            paneWidthPx = paneWidthPx,
            highlightValues = highlightTargets,
            extraTickValues = extraTicks,
            highlightColorForValue = { v ->
                val nv = norm(v)
                referenceLines.firstOrNull { spec ->
                    abs(norm(spec.y) - nv) <= tol ||
                    (spec.type == ReferenceLineType.ZONE && spec.yEnd != null &&
                     abs(norm(spec.yEnd!!) - nv) <= tol)
                }?.color ?: Color.Transparent
            },
            highlightTolerance = tol
        )
    }
}