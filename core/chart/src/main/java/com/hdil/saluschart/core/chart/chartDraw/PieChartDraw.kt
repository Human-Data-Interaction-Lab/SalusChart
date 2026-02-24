package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import kotlin.math.cos
import kotlin.math.sin

/**
 * Low-level drawing helpers for pie/donut charts.
 *
 * This object provides:
 * - Drawing individual pie/donut sections (with optional selection offset + scale)
 * - Drawing labels positioned near the center of each section
 *
 * All angles are in degrees, matching Compose's `drawArc` APIs.
 */
object PieChartDraw {

    /**
     * Draws a single pie (or donut) section.
     *
     * Behavior:
     * - When [isSelected] is true, the section is slightly offset outward from the center
     *   and can be scaled using [animationScale].
     * - When [isDonut] is true, the section is drawn as a stroked arc (ring segment).
     * - When [isDonut] is false, the section is drawn as a filled wedge and includes
     *   two white divider lines at its boundaries.
     *
     * @param drawScope Compose draw scope.
     * @param center Center of the pie in canvas coordinates.
     * @param radius Base radius of the pie in pixels.
     * @param startAngle Start angle in degrees.
     * @param sweepAngle Sweep angle in degrees.
     * @param color Section color.
     * @param isDonut If true, draw as a donut (ring segment) using [strokeWidth].
     * @param strokeWidth Stroke width (pixels) used only when [isDonut] is true.
     * @param isSelected If true, offset the section outward from the center.
     * @param animationScale Scale factor applied to [radius] (1.0f = no scaling).
     * @param alpha Alpha applied to [color] (1.0f = fully opaque).
     */
    fun drawPieSection(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        startAngle: Float,
        sweepAngle: Float,
        color: Color,
        isDonut: Boolean,
        strokeWidth: Float,
        isSelected: Boolean = false,
        animationScale: Float = 1.0f,
        alpha: Float = 1.0f
    ) {
        // Selected sections are slightly scaled and pushed outward.
        val scaledRadius = radius * animationScale
        val offsetDistance = if (isSelected) 10f * animationScale else 0f

        // Mid-angle used to compute outward offset direction.
        val midAngle = startAngle + sweepAngle / 2
        val offsetX = cos(Math.toRadians(midAngle.toDouble())).toFloat() * offsetDistance
        val offsetY = sin(Math.toRadians(midAngle.toDouble())).toFloat() * offsetDistance
        val adjustedCenter = Offset(center.x + offsetX, center.y + offsetY)

        val adjustedColor = color.copy(alpha = alpha)

        if (isDonut) {
            // Donut segment (stroke arc).
            drawScope.drawArc(
                color = adjustedColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(adjustedCenter.x - scaledRadius, adjustedCenter.y - scaledRadius),
                size = Size(scaledRadius * 2, scaledRadius * 2),
                style = Stroke(width = strokeWidth)
            )
        } else {
            // Filled wedge.
            drawScope.drawArc(
                color = adjustedColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(adjustedCenter.x - scaledRadius, adjustedCenter.y - scaledRadius),
                size = Size(scaledRadius * 2, scaledRadius * 2)
            )

            // Divider lines at the start/end boundaries of the wedge.
            val gap = 4 * drawScope.drawContext.density.density // 2.dp-ish converted to px

            val leftRad = Math.toRadians(startAngle.toDouble())
            val leftEndPoint = Offset(
                (adjustedCenter.x + cos(leftRad) * scaledRadius).toFloat(),
                (adjustedCenter.y + sin(leftRad) * scaledRadius).toFloat()
            )
            drawScope.drawLine(
                color = Color.White,
                start = adjustedCenter,
                end = leftEndPoint,
                strokeWidth = gap
            )

            val rightAngle = startAngle + sweepAngle
            val rightRad = Math.toRadians(rightAngle.toDouble())
            val rightEndPoint = Offset(
                (adjustedCenter.x + cos(rightRad) * scaledRadius).toFloat(),
                (adjustedCenter.y + sin(rightRad) * scaledRadius).toFloat()
            )
            drawScope.drawLine(
                color = Color.White,
                start = adjustedCenter,
                end = rightEndPoint,
                strokeWidth = gap
            )
        }
    }

    /**
     * Draws labels for pie sections.
     *
     * Labels are only drawn when `data[i].label != null` (this matches existing behavior),
     * and the drawn text uses `point.y.toString()` positioned near the center of the slice
     * using [ChartMath.Pie.calculateCenterPosition].
     *
     * @param drawScope Compose draw scope.
     * @param center Center of the pie in canvas coordinates.
     * @param radius Radius of the pie in pixels.
     * @param data Chart marks; `data[i]` corresponds to `sections[i]`.
     * @param sections Section tuples `(startAngle, sweepAngle, value)` produced by the pie layout logic.
     */
    fun drawPieLabels(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        data: List<ChartMark>,
        sections: List<Triple<Float, Float, Float>>
    ) {
        sections.forEachIndexed { i, (startAngle, sweepAngle, _) ->
            val point = data[i]

            // Only draw a label if a label exists for that mark.
            if (point.label != null) {
                val midAngle = startAngle + sweepAngle / 2
                val labelPos = ChartMath.Pie.calculateCenterPosition(center, radius, midAngle)

                drawScope.drawContext.canvas.nativeCanvas.drawText(
                    point.y.toString(),
                    labelPos.x,
                    labelPos.y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 12f * drawScope.drawContext.density.density // 12sp-ish to px
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}