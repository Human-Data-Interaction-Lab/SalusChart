package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.BaseChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.StackedChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The type of reference line to display on a chart.
 */
enum class ReferenceLineType {
    /** A horizontal line drawn at the average Y value of the dataset (auto-computed). */
    AVERAGE,

    /** A diagonal trend line computed using linear regression (auto-computed). */
    TREND,

    /** A horizontal line drawn at a user-specified Y value ([ReferenceLineSpec.y]). */
    THRESHOLD,

    /** A shaded horizontal band between [ReferenceLineSpec.y] and [ReferenceLineSpec.yEnd]. */
    ZONE,
}

/**
 * Visual style for a reference line, represented as an optional dash pattern.
 *
 * If [dashPattern] is null, the line is solid.
 */
enum class LineStyle(val dashPattern: FloatArray?) {
    /** Solid line. */
    SOLID(null),

    /** Short dashed line. */
    DASHED(floatArrayOf(10f, 5f)),

    /** Dotted line. */
    DOTTED(floatArrayOf(2f, 5f)),

    /** Dash-dot pattern. */
    DASHDOT(floatArrayOf(10f, 5f, 2f, 5f)),

    /** Long dashed line. */
    LONGDASH(floatArrayOf(20f, 10f))
}

/**
 * Utilities for computing and rendering chart reference lines.
 *
 * Supports four types (see [ReferenceLineType]):
 * - AVERAGE  — horizontal line at the dataset mean
 * - TREND    — diagonal line via linear regression
 * - THRESHOLD — horizontal line at a fixed user-supplied Y value
 * - ZONE      — shaded band between two Y values
 *
 * Entry points:
 * - [ReferenceLines] — composable overlay that renders a full [List<ReferenceLineSpec>].
 *   Use this for LineChart, BarChart, ScatterPlot.
 * - [drawOnCanvas] — canvas draw function for charts that draw inside a DrawScope
 *   (handles THRESHOLD and ZONE only; use [ReferenceLines] composable for AVERAGE/TREND).
 */
object ReferenceLine {

    // ─────────────────────────────────────────────────────────────────────────
    // Math helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes the average Y value for the given dataset.
     * For [ChartType.STACKED_BAR] the stack-total Y is used.
     */
    fun calculateAverage(data: List<BaseChartMark>, chartType: ChartType): Float {
        if (data.isEmpty()) return 0f
        return when (chartType) {
            ChartType.STACKED_BAR -> {
                val stackedData = data.filterIsInstance<StackedChartMark>()
                if (stackedData.isEmpty()) data.map { it.y }.average().roundToInt().toFloat()
                else stackedData.map { it.y }.average().roundToInt().toFloat()
            }
            else -> data.map { it.y }.average().roundToInt().toFloat()
        }
    }

    /**
     * Returns (slope, intercept) for the linear regression of [data].
     * Returns (0, 0) if fewer than 2 points exist.
     */
    private fun calculateTrendLine(data: List<BaseChartMark>): Pair<Float, Float> {
        if (data.size < 2) return Pair(0f, 0f)
        val n = data.size
        val sumX = data.sumOf { it.x.toDouble() }
        val sumY = data.sumOf { it.y.toDouble() }
        val sumXY = data.sumOf { it.x.toDouble() * it.y.toDouble() }
        val sumXSquared = data.sumOf { it.x.toDouble().pow(2) }
        val slope = ((n * sumXY - sumX * sumY) / (n * sumXSquared - sumX.pow(2))).toFloat()
        val intercept = ((sumY - slope * sumX) / n).toFloat()
        return Pair(slope, intercept)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public composable entry point (LineChart / BarChart / ScatterPlot)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders all reference lines/zones in [specs] as a composable overlay.
     *
     * Place this inside the same [Box] that contains the chart canvas, using
     * `Modifier.fillMaxSize()` so it aligns with the canvas coordinate system.
     *
     * @param modifier Modifier for the overlay container.
     * @param specs    Reference line specs to render.
     * @param data     Chart data (used to compute AVERAGE and TREND lines).
     * @param metrics  Chart metrics for coordinate mapping.
     * @param chartType Chart type (affects AVERAGE computation for stacked bars).
     * @param yAxisPosition Which side the Y-axis is on (affects label placement).
     */
    @Composable
    fun ReferenceLines(
        modifier: Modifier = Modifier,
        specs: List<ReferenceLineSpec>,
        data: List<BaseChartMark>,
        metrics: ChartMath.ChartMetrics,
        chartType: ChartType,
        yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    ) {
        if (specs.isEmpty() || data.isEmpty()) return
        Box(modifier = modifier.fillMaxSize()) {
            specs.forEach { spec ->
                when (spec.type) {
                    ReferenceLineType.AVERAGE -> AverageLine(
                        spec = spec,
                        data = data,
                        metrics = metrics,
                        chartType = chartType,
                        yAxisPosition = yAxisPosition,
                    )
                    ReferenceLineType.TREND -> TrendLine(
                        spec = spec,
                        data = data,
                        metrics = metrics,
                    )
                    ReferenceLineType.THRESHOLD -> ThresholdLine(
                        spec = spec,
                        metrics = metrics,
                        yAxisPosition = yAxisPosition,
                    )
                    ReferenceLineType.ZONE -> ZoneBand(
                        spec = spec,
                        metrics = metrics,
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Canvas draw entry point (RangeBarChart canvas-based rendering)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws THRESHOLD and ZONE specs directly onto a [androidx.compose.ui.graphics.drawscope.DrawScope].
     *
     * AVERAGE and TREND specs are skipped here (they require chart data that is not available
     * in a DrawScope context). Use [ReferenceLines] composable for those types.
     */
    fun drawOnCanvas(
        drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
        metrics: ChartMath.ChartMetrics,
        lines: List<ReferenceLineSpec>,
    ) = with(drawScope) {
        if (lines.isEmpty()) return
        val denom = (metrics.maxY - metrics.minY).takeIf { it != 0.0 } ?: return

        val dashCache = HashMap<LineStyle, PathEffect?>()

        lines.forEach { spec ->
            when (spec.type) {
                ReferenceLineType.THRESHOLD -> {
                    val y = spec.y
                    if (y < metrics.minY || y > metrics.maxY) return@forEach

                    val screenY = (metrics.paddingY + metrics.chartHeight -
                            ((y - metrics.minY) / denom) * metrics.chartHeight).toFloat()

                    val pathEffect = dashCache.getOrPut(spec.style) {
                        spec.style.dashPattern?.let { PathEffect.dashPathEffect(it, 0f) }
                    }

                    drawLine(
                        color = spec.color,
                        start = Offset(metrics.paddingX, screenY),
                        end = Offset(metrics.paddingX + metrics.chartWidth, screenY),
                        strokeWidth = spec.strokeWidth.toPx(),
                        pathEffect = pathEffect
                    )
                }

                ReferenceLineType.ZONE -> {
                    val yLow = spec.y
                    val yHigh = spec.yEnd ?: return@forEach
                    if (yLow >= yHigh) return@forEach

                    val clampedLow = yLow.coerceIn(metrics.minY, metrics.maxY)
                    val clampedHigh = yHigh.coerceIn(metrics.minY, metrics.maxY)

                    val screenTop = (metrics.paddingY + metrics.chartHeight -
                            ((clampedHigh - metrics.minY) / denom) * metrics.chartHeight).toFloat()
                    val screenBottom = (metrics.paddingY + metrics.chartHeight -
                            ((clampedLow - metrics.minY) / denom) * metrics.chartHeight).toFloat()

                    drawRect(
                        color = spec.color.copy(alpha = spec.color.alpha * 0.15f),
                        topLeft = Offset(metrics.paddingX, screenTop),
                        size = Size(metrics.chartWidth, screenBottom - screenTop)
                    )
                    // Optional top and bottom border lines
                    val pathEffect = dashCache.getOrPut(spec.style) {
                        spec.style.dashPattern?.let { PathEffect.dashPathEffect(it, 0f) }
                    }
                    listOf(screenTop, screenBottom).forEach { borderY ->
                        drawLine(
                            color = spec.color,
                            start = Offset(metrics.paddingX, borderY),
                            end = Offset(metrics.paddingX + metrics.chartWidth, borderY),
                            strokeWidth = spec.strokeWidth.toPx(),
                            pathEffect = pathEffect
                        )
                    }
                }

                else -> { /* AVERAGE and TREND not supported in canvas path */ }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private composable implementations
    // ─────────────────────────────────────────────────────────────────────────

    @Composable
    private fun AverageLine(
        spec: ReferenceLineSpec,
        data: List<BaseChartMark>,
        metrics: ChartMath.ChartMetrics,
        chartType: ChartType,
        yAxisPosition: YAxisPosition,
    ) {
        val average = calculateAverage(data, chartType)
        if (average < metrics.minY || average > metrics.maxY) return

        val density = LocalDensity.current
        val interactionSource = remember { MutableInteractionSource() }
        var isPressed by remember { mutableStateOf(false) }

        val denom = (metrics.maxY - metrics.minY)
        val y = metrics.chartHeight - ((average - metrics.minY) / denom) * metrics.chartHeight

        val lineWidth = with(density) { metrics.chartWidth.toDp() }
        val touchThreshold = 20.dp
        val touchAreaHeight = touchThreshold * 2

        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { metrics.paddingX.toDp() },
                    y = with(density) { (metrics.paddingY + y.toFloat() - touchThreshold.toPx()).toDp() }
                )
                .size(width = lineWidth, height = touchAreaHeight)
        ) {
            Canvas(modifier = Modifier.size(width = lineWidth, height = touchAreaHeight)) {
                val pathEffect = spec.style.dashPattern?.let { PathEffect.dashPathEffect(it, 0f) }
                drawLine(
                    color = spec.color,
                    start = Offset(0f, touchThreshold.toPx()),
                    end = Offset(size.width, touchThreshold.toPx()),
                    strokeWidth = spec.strokeWidth.toPx(),
                    pathEffect = pathEffect
                )
            }

            if (spec.interactive) {
                Box(
                    modifier = Modifier
                        .size(width = lineWidth, height = touchAreaHeight)
                        .pointerInput(Unit) { detectTapGestures { isPressed = !isPressed } }
                )
            } else if (spec.onClick != null) {
                Box(
                    modifier = Modifier
                        .size(width = lineWidth, height = touchAreaHeight)
                        .clickable(interactionSource = interactionSource, indication = null) {
                            spec.onClick.invoke()
                        }
                )
            }

            if (spec.showLabel || (spec.interactive && isPressed)) {
                val displayLabel = spec.label ?: spec.labelFormat.format(average)
                ReferenceLineLabel(
                    text = displayLabel,
                    lineY = with(density) { touchThreshold.toPx() },
                    color = spec.color,
                    metrics = metrics,
                )
            }
        }
    }

    @Composable
    private fun TrendLine(
        spec: ReferenceLineSpec,
        data: List<BaseChartMark>,
        metrics: ChartMath.ChartMetrics,
    ) {
        val (slope, intercept) = calculateTrendLine(data)

        val dataStartX = data.first().x.toFloat()
        val dataEndX = data.last().x.toFloat()
        val startY = slope * dataStartX + intercept
        val endY = slope * dataEndX + intercept

        val denom = (metrics.maxY - metrics.minY)
        val screenStartY = metrics.paddingY + metrics.chartHeight - ((startY - metrics.minY) / denom) * metrics.chartHeight
        val screenEndY = metrics.paddingY + metrics.chartHeight - ((endY - metrics.minY) / denom) * metrics.chartHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val pathEffect = spec.style.dashPattern?.let { PathEffect.dashPathEffect(it, 0f) }
            drawLine(
                color = spec.color,
                start = Offset(metrics.paddingX, screenStartY.toFloat()),
                end = Offset(metrics.paddingX + metrics.chartWidth, screenEndY.toFloat()),
                strokeWidth = spec.strokeWidth.toPx(),
                pathEffect = pathEffect
            )
        }
    }

    @Composable
    private fun ThresholdLine(
        spec: ReferenceLineSpec,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition,
    ) {
        val y = spec.y
        if (y < metrics.minY || y > metrics.maxY) return

        val density = LocalDensity.current
        val interactionSource = remember { MutableInteractionSource() }
        var isPressed by remember { mutableStateOf(false) }

        val denom = (metrics.maxY - metrics.minY)
        val screenY = metrics.chartHeight - ((y - metrics.minY) / denom) * metrics.chartHeight

        val lineWidth = with(density) { metrics.chartWidth.toDp() }
        val touchThreshold = 20.dp
        val touchAreaHeight = touchThreshold * 2

        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { metrics.paddingX.toDp() },
                    y = with(density) { (metrics.paddingY + screenY.toFloat() - touchThreshold.toPx()).toDp() }
                )
                .size(width = lineWidth, height = touchAreaHeight)
        ) {
            Canvas(modifier = Modifier.size(width = lineWidth, height = touchAreaHeight)) {
                val pathEffect = spec.style.dashPattern?.let { PathEffect.dashPathEffect(it, 0f) }
                drawLine(
                    color = spec.color,
                    start = Offset(0f, touchThreshold.toPx()),
                    end = Offset(size.width, touchThreshold.toPx()),
                    strokeWidth = spec.strokeWidth.toPx(),
                    pathEffect = pathEffect
                )
            }

            if (spec.interactive) {
                Box(
                    modifier = Modifier
                        .size(width = lineWidth, height = touchAreaHeight)
                        .pointerInput(Unit) { detectTapGestures { isPressed = !isPressed } }
                )
            } else if (spec.onClick != null) {
                Box(
                    modifier = Modifier
                        .size(width = lineWidth, height = touchAreaHeight)
                        .clickable(interactionSource = interactionSource, indication = null) {
                            spec.onClick.invoke()
                        }
                )
            }

            if (spec.showLabel || (spec.interactive && isPressed)) {
                val displayLabel = spec.label ?: spec.labelFormat.format(y)
                ReferenceLineLabel(
                    text = displayLabel,
                    lineY = with(density) { touchThreshold.toPx() },
                    color = spec.color,
                    metrics = metrics,
                )
            }
        }
    }

    @Composable
    private fun ZoneBand(
        spec: ReferenceLineSpec,
        metrics: ChartMath.ChartMetrics,
    ) {
        val yLow = spec.y
        val yHigh = spec.yEnd ?: return
        if (yLow >= yHigh) return

        val denom = (metrics.maxY - metrics.minY).takeIf { it != 0.0 } ?: return

        val clampedLow = yLow.coerceIn(metrics.minY, metrics.maxY)
        val clampedHigh = yHigh.coerceIn(metrics.minY, metrics.maxY)

        val screenTop = metrics.chartHeight - ((clampedHigh - metrics.minY) / denom) * metrics.chartHeight
        val screenBottom = metrics.chartHeight - ((clampedLow - metrics.minY) / denom) * metrics.chartHeight

        val density = LocalDensity.current
        val left = with(density) { metrics.paddingX.toDp() }
        val top = with(density) { (metrics.paddingY + screenTop.toFloat()).toDp() }
        val width = with(density) { metrics.chartWidth.toDp() }
        val height = with(density) { (screenBottom - screenTop).toFloat().toDp() }

        Canvas(
            modifier = Modifier
                .offset(x = left, y = top)
                .size(width = width, height = height)
        ) {
            // Filled band
            drawRect(color = spec.color.copy(alpha = spec.color.alpha * 0.15f))

            // Border lines at top and bottom
            val pathEffect = spec.style.dashPattern?.let { PathEffect.dashPathEffect(it, 0f) }
            val sw = spec.strokeWidth.toPx()
            drawLine(
                color = spec.color,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = sw,
                pathEffect = pathEffect
            )
            drawLine(
                color = spec.color,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = sw,
                pathEffect = pathEffect
            )
        }

        // Optional label — centered in zone, just outside the chart right edge
        if (spec.showLabel) {
            val displayLabel = spec.label ?: "${yHigh.toInt()}–${yLow.toInt()}"
            val labelXPx = metrics.paddingX + metrics.chartWidth + with(density) { 5.dp.toPx() }
            val zoneCenterY = metrics.paddingY + (screenTop + screenBottom).toFloat() / 2f
            val halfLabelHeightPx = with(density) { 12.dp.toPx() }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = labelXPx.roundToInt(),
                            y = (zoneCenterY - halfLabelHeightPx).roundToInt()
                        )
                    }
                    .background(spec.color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = displayLabel,
                    color = spec.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared label composable
    // ─────────────────────────────────────────────────────────────────────────

    @Composable
    private fun ReferenceLineLabel(
        text: String,
        lineY: Float,
        color: Color,
        metrics: ChartMath.ChartMetrics,
    ) {
        val density = LocalDensity.current
        // ~12dp = half of a typical single-line label height (12sp text + 4dp vertical padding)
        val halfLabelHeightPx = with(density) { 12.dp.toPx() }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (metrics.chartWidth + 5.dp.toPx()).roundToInt(),
                        y = (lineY - halfLabelHeightPx).roundToInt()
                    )
                }
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
