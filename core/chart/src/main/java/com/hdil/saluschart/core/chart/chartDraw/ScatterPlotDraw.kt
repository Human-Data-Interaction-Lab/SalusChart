package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.PointType
import com.hdil.saluschart.core.chart.chartMath.LineChartMath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Low-level drawing helpers for scatter plot markers (and re-used point markers).
 *
 * This file renders individual points at precomputed canvas coordinates and optionally:
 * - Applies selection styling
 * - Emits tooltips for externally targeted indices
 * - Draws value labels near points with collision-aware anchor computation
 *
 * Note:
 * - Point positions are provided via [points] (already mapped to canvas coordinates by the caller).
 * - This differs from BarMarker (which computes its own positioning internally).
 */
object ScatterPlotDraw {

    /**
     * Draws point markers at the provided canvas positions.
     *
     * Selection:
     * - If [selectedIndices] is provided, it is used as the selection source.
     * - Otherwise [selectedPointIndex] is used (null means "all selected").
     *
     * Tooltips:
     * - If [showTooltipForIndices] is provided, tooltips are shown for those indices.
     * - Otherwise [showTooltipForIndex] is used.
     *
     * Point rendering:
     * - [PointType.Circle] uses a filled circle and optional inner white dot.
     * - [PointType.Triangle] uses a Canvas path.
     * - [PointType.Square] uses a simple Box.
     *
     * Value labels:
     * - When [showValue] is true, anchors are computed using
     *   [LineChartMath.computeLabelAnchors] and then rendered as text near each point.
     *
     * @param data Chart marks (used for tooltip payload).
     * @param points Point centers in canvas coordinates (px).
     * @param values Values associated with each point (used for value labels).
     * @param color Base point/tooltip color.
     * @param pointRadius Outer point radius.
     * @param innerRadius Inner dot radius (circle type only).
     * @param selectedPointIndex Single selected index; null means all points are treated as selected.
     * @param selectedIndices Optional multi-selection set (takes precedence over [selectedPointIndex]).
     * @param onPointClick Callback for point tap (index).
     * @param interactive If true, points are clickable.
     * @param showPoint If false, points can be hidden (selection may still influence visibility).
     * @param pointType Marker shape.
     * @param showValue If true, draw value labels near points.
     * @param chartType Chart type (currently unused here; kept for API stability).
     * @param showTooltipForIndex Externally-controlled tooltip index.
     * @param showTooltipForIndices Externally-controlled tooltip indices.
     * @param canvasSize Canvas size used to compute value-label anchors.
     * @param unit Unit suffix passed to [ChartTooltip].
     */
    @Composable
    fun PointMarker(
        data: List<ChartMark>,
        points: List<Offset>,
        values: List<Double>,
        color: Color = Color.Black,
        pointRadius: Dp = 4.dp,
        innerRadius: Dp = 2.dp,
        selectedPointIndex: Int? = null,
        selectedIndices: Set<Int>? = null,
        onPointClick: ((Int) -> Unit)? = null,
        interactive: Boolean = false,
        showPoint: Boolean = true,
        pointType: PointType = PointType.Circle,
        showValue: Boolean = false,
        chartType: ChartType,
        showTooltipForIndex: Int? = null,
        showTooltipForIndices: Set<Int>? = null,
        canvasSize: Size,
        unit: String = "",
    ) {
        val density = LocalDensity.current

        // Pre-allocate tooltip entries based on expected count.
        val expectedTooltipCount =
            showTooltipForIndices?.size ?: if (showTooltipForIndex != null) 1 else 0

        val tooltipEntries = remember(showTooltipForIndex, showTooltipForIndices) {
            ArrayList<Pair<ChartMark, Offset>>(expectedTooltipCount)
        }.apply { clear() }

        // Value label sizing in px (used by anchor computation).
        val textPx = with(density) { 12.sp.toPx() }

        points.forEachIndexed { index, center ->
            val xDp = with(density) { center.x.toDp() }
            val yDp = with(density) { center.y.toDp() }

            // Selection: set-based selection takes precedence.
            val isSelected = when {
                selectedIndices != null -> selectedIndices.contains(index)
                else -> selectedPointIndex == null || selectedPointIndex == index
            }

            val pointColor = if (showPoint) {
                if (isSelected) color else Color.Gray
            } else {
                if ((selectedIndices?.contains(index) == true) || selectedPointIndex == index) color else Color.Transparent
            }

            // Tooltip targeting: set-based targeting takes precedence.
            val showTip = when {
                showTooltipForIndices != null -> showTooltipForIndices.contains(index)
                else -> showTooltipForIndex == index
            }
            if (showTip) {
                tooltipEntries += (data[index] to center)
            }

            when (pointType) {
                PointType.Circle -> {
                    Box(
                        modifier = Modifier
                            .offset(x = xDp - pointRadius, y = yDp - pointRadius)
                            .size(pointRadius * 2)
                            .background(color = pointColor, shape = CircleShape)
                            .clickable(enabled = interactive) { onPointClick?.invoke(index) }
                    )

                    if (innerRadius > 0.dp && (showPoint || isSelected)) {
                        Box(
                            modifier = Modifier
                                .offset(x = xDp - innerRadius, y = yDp - innerRadius)
                                .size(innerRadius * 2)
                                .background(color = Color.White, shape = CircleShape)
                        )
                    }
                }

                PointType.Triangle -> {
                    Box(
                        modifier = Modifier
                            .offset(x = xDp - pointRadius, y = yDp - pointRadius)
                            .size(pointRadius * 2)
                            .clickable(enabled = interactive) { onPointClick?.invoke(index) }
                    ) {
                        Canvas(modifier = Modifier.size(pointRadius * 2)) {
                            val trianglePath = Path().apply {
                                val half = size.minDimension / 2
                                val center = Offset(half, half)
                                val angleOffset = -PI / 2

                                for (i in 0..2) {
                                    val angle = angleOffset + i * (2 * PI / 3)
                                    val x = center.x + half * cos(angle).toFloat()
                                    val y = center.y + half * sin(angle).toFloat()
                                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                                }
                                close()
                            }
                            drawPath(path = trianglePath, color = pointColor)
                        }
                    }
                }

                PointType.Square -> {
                    Box(
                        modifier = Modifier
                            .offset(x = xDp - pointRadius, y = yDp - pointRadius)
                            .size(pointRadius * 2)
                            .background(color = pointColor)
                            .clickable(enabled = interactive) { onPointClick?.invoke(index) }
                    )
                }
            }
        }

        if (showValue) {
            val anchors = remember(points, values, canvasSize) {
                if (canvasSize.width <= 0f || canvasSize.height <= 0f || points.isEmpty()) {
                    emptyList()
                } else {
                    LineChartMath.computeLabelAnchors(
                        points = points,
                        values = values.map { it.toFloat() },
                        canvas = canvasSize,
                        textPx = textPx,
                        padPx = with(density) { 4.dp.toPx() },
                        minGapToLinePx = with(density) { 4.dp.toPx() }
                    )
                }
            }

            if (anchors.size == points.size) {
                anchors.forEachIndexed { i, topLeft ->
                    val label = values.getOrElse(i) { 0.0 }.let { v ->
                        if (kotlin.math.abs(v - v.toInt()) < 0.001) v.toInt().toString() else v.toString()
                    }

                    val xDp = with(density) { topLeft.x.toDp() }
                    val yDp = with(density) { topLeft.y.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = xDp, y = yDp)
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = label,
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Render tooltips for targeted points.
        if (tooltipEntries.isNotEmpty()) {
            tooltipEntries.forEach { (mark, offset) ->
                val xDp = with(density) { offset.x.toDp() }
                val yDp = with(density) { offset.y.toDp() }

                ChartTooltip(
                    chartMark = mark,
                    modifier = Modifier.offset(x = xDp - pointRadius, y = yDp + pointRadius),
                    unit = unit,
                    color = color
                )
            }
        }
    }
}