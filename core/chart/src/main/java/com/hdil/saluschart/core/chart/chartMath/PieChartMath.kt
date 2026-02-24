package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartMark

/**
 * Math utilities for pie/donut charts.
 *
 * This object provides:
 * - Center/radius computation for a pie chart within a given canvas size
 * - Angle/sweep computation per section based on data values
 * - Helper to compute the "center" position of a slice (for labels/tooltips)
 * - Hit-testing to determine which slice was clicked
 */
object PieChartMath {

    /**
     * Computes the center point and radius for a pie chart.
     *
     * The radius is calculated as half of the smaller canvas dimension minus [padding].
     *
     * @param size Canvas size.
     * @param padding Padding between the pie edge and canvas bounds (in px).
     * @return Pair(center, radius).
     */
    fun computePieMetrics(size: Size, padding: Float = 32f): Pair<Offset, Float> {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2 - padding
        return Pair(center, radius)
    }

    /**
     * Computes section angles for a pie chart.
     *
     * Each returned triple is:
     * - startAngle (degrees)
     * - sweepAngle (degrees)
     * - ratio (0..1) of the total represented by the slice
     *
     * The first slice starts at -90° (12 o'clock).
     *
     * If the total sum of values is non-positive, an empty list is returned.
     *
     * @param data Chart data marks.
     * @return List of (startAngle, sweepAngle, ratio) per mark.
     */
    fun computePieAngles(data: List<ChartMark>): List<Triple<Float, Float, Float>> {
        val totalValue = data.sumOf { it.y.toDouble() }.toFloat()
        if (totalValue <= 0f) return emptyList()

        var startAngle = -90f

        return data.map { point ->
            val ratio = (point.y / totalValue).toFloat()
            val sweepAngle = ratio * 360f
            val result = Triple(startAngle, sweepAngle, ratio)
            startAngle += sweepAngle
            result
        }
    }

    /**
     * Computes a point on the slice "midline" at the given angle.
     *
     * This is used for label and tooltip placement.
     *
     * Behavior:
     * - When [forToolTip] is false, the returned point is offset from [center].
     * - When [forToolTip] is true, the returned point is relative (not offset by [center]).
     *   This preserves existing behavior and is used by some tooltip positioning logic.
     *
     * @param center Pie center (canvas coordinates).
     * @param radius Base radius used for placement.
     * @param angleInDegrees Angle in degrees.
     * @param forToolTip If true, returns coordinates without adding [center].
     * @return Position for label/tooltip placement.
     */
    fun calculateCenterPosition(
        center: Offset,
        radius: Float,
        angleInDegrees: Float,
        forToolTip: Boolean = false
    ): Offset {
        val angleInRadians = Math.toRadians(angleInDegrees.toDouble())
        val labelRadius = radius

        val x = if (forToolTip) {
            labelRadius * Math.cos(angleInRadians).toFloat()
        } else {
            center.x + labelRadius * Math.cos(angleInRadians).toFloat()
        }

        val y = if (forToolTip) {
            labelRadius * Math.sin(angleInRadians).toFloat()
        } else {
            center.y + labelRadius * Math.sin(angleInRadians).toFloat()
        }

        return Offset(x, y)
    }

    /**
     * Determines which pie section was clicked using distance + angle hit-testing.
     *
     * Steps:
     * 1) Ensure the click is within an allowed radius (currently `radius + 50f`).
     * 2) Convert the click position to an angle measured from 12 o'clock (0..360).
     * 3) Find the section whose (normalized) start/end angles contain the click angle,
     *    handling wrap-around past 360°.
     *
     * @param clickPosition Click position in canvas coordinates.
     * @param center Pie center in canvas coordinates.
     * @param radius Pie radius.
     * @param sections Section list computed by [computePieAngles].
     * @return Index of the clicked section, or -1 if none.
     */
    fun getClickedSectionIndex(
        clickPosition: Offset,
        center: Offset,
        radius: Float,
        sections: List<Triple<Float, Float, Float>>
    ): Int {
        val distance = kotlin.math.sqrt(
            (clickPosition.x - center.x) * (clickPosition.x - center.x) +
                    (clickPosition.y - center.y) * (clickPosition.y - center.y)
        )

        // TODO: Currently uses a fixed margin (50f). For donut charts, this may need to
        // incorporate stroke width / inner radius.
        if (distance > radius + 50f) return -1

        val angleRad = kotlin.math.atan2(
            clickPosition.y - center.y,
            clickPosition.x - center.x
        )
        var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

        // Convert so 12 o'clock is 0 degrees.
        angleDeg += 90f
        if (angleDeg < 0) angleDeg += 360f

        sections.forEachIndexed { index, (startAngle, sweepAngle, _) ->
            var normalizedStart = startAngle + 90f
            if (normalizedStart < 0) normalizedStart += 360f

            val endAngle = normalizedStart + sweepAngle

            if (endAngle <= 360f) {
                if (angleDeg >= normalizedStart && angleDeg <= endAngle) {
                    return index
                }
            } else {
                // Wrap-around case.
                if (angleDeg >= normalizedStart || angleDeg <= (endAngle - 360f)) {
                    return index
                }
            }
        }

        return -1
    }
}