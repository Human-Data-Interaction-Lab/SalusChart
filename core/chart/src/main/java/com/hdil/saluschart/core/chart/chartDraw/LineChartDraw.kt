package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawContext
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.hdil.saluschart.core.chart.chartMath.ChartMath

/**
 * Drawing helpers for line charts.
 *
 * This object contains low-level rendering utilities used by line-chart composables:
 * - Drawing a polyline that connects data points
 * - Drawing X-axis labels using the line-chart spacing rule
 */
object LineChartDraw {

    /**
     * Draws a polyline connecting the given points in order.
     *
     * The caller is responsible for providing points already converted into screen/canvas
     * coordinates (e.g., using [ChartMath.ChartMetrics]).
     *
     * No drawing occurs if fewer than 2 points are provided.
     *
     * @param drawScope Compose draw scope.
     * @param points Points in canvas coordinates, in draw order.
     * @param color Line color.
     * @param strokeWidth Line width in pixels.
     */
    fun drawLine(
        drawScope: DrawScope,
        points: List<Offset>,
        color: Color,
        strokeWidth: Float
    ) {
        if (points.size < 2) return

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        drawScope.drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }

    /**
     * Draws X-axis labels for a line chart.
     *
     * Positioning rule:
     * - Labels are centered within each "slot" using `(index + 0.5) * spacing`, where
     *   `spacing = chartWidth / totalLabelCount`.
     *
     * Label density:
     * - If [xLabelAutoSkip] is enabled, labels may be skipped using
     *   [ChartMath.computeAutoSkipLabels] to reduce overlap.
     *
     * Clamping behavior (when [centered] is true):
     * - The label's center X is clamped so the text does not overflow the canvas bounds.
     *
     * Note:
     * - The current implementation uses the line-chart spacing rule and places the first label
     *   half a slot away from the left edge. This matches existing point positioning behavior.
     *
     * @param ctx Draw context whose native canvas is used to render text.
     * @param labels Full list of labels (one per x-position).
     * @param metrics Chart metrics used for padding and chart dimensions.
     * @param centered If true, center-align the text (default: true).
     * @param textSize Text size in pixels (default: 28f).
     * @param maxXTicksLimit Optional cap on how many labels can be displayed (null = no cap).
     * @param xLabelAutoSkip If true, compute an auto-skip factor based on text width and chart width.
     */
    fun drawLineXAxisLabels(
        ctx: DrawContext,
        labels: List<String>,
        metrics: ChartMath.ChartMetrics,
        centered: Boolean = true,
        textSize: Float = 28f,
        maxXTicksLimit: Int? = null,
        xLabelAutoSkip: Boolean = false
    ) {
        val (displayLabels, displayIndices) =
            if (xLabelAutoSkip) {
                ChartMath.computeAutoSkipLabels(
                    labels = labels,
                    textSize = textSize,
                    chartWidth = metrics.chartWidth,
                    maxXTicksLimit = maxXTicksLimit
                )
            } else {
                Pair(labels, labels.indices.toList())
            }

        val total = labels.size
        val spacing = if (total > 1) metrics.chartWidth / total else 0f

        val canvas = ctx.canvas.nativeCanvas
        val canvasWidth = ctx.size.width
        val y = metrics.paddingY + metrics.chartHeight + 50f

        displayLabels.forEachIndexed { displayIndex, label ->
            val originalIndex = displayIndices[displayIndex]

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                this.textSize = textSize
            }

            val baseX = metrics.paddingX + (originalIndex + 0.5f) * spacing

            if (centered) {
                paint.textAlign = android.graphics.Paint.Align.CENTER

                val textWidth = paint.measureText(label)
                val half = textWidth / 2f
                val clamped = baseX.coerceIn(half, canvasWidth - half)

                canvas.drawText(label, clamped, y, paint)
            } else {
                paint.textAlign = android.graphics.Paint.Align.LEFT
                canvas.drawText(label, baseX, y, paint)
            }
        }
    }
}