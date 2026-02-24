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
import com.hdil.saluschart.core.chart.StackedChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The type of reference line to display on a chart.
 */
enum class ReferenceLineType {
    /** No reference line is drawn. */
    NONE,

    /** A horizontal line drawn at the average Y value of the dataset. */
    AVERAGE,

    /** A trend line computed using linear regression. */
    TREND
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
 * Utilities for computing and rendering chart reference lines (average line / trend line).
 *
 * This file provides:
 * - Average computation (including stacked bar handling)
 * - Linear regression for trend line
 * - A composable overlay that draws the selected reference line type
 *
 * Note: Interaction behavior differs by type:
 * - Average line supports a touch-area overlay (toggle label) and optional click callback.
 * - Trend line is currently drawn as a visual-only overlay (no interaction).
 */
object ReferenceLine {

    /**
     * Computes the average Y value for the given dataset.
     *
     * Special handling:
     * - For [ChartType.STACKED_BAR], the average is computed using the `y` value of
     *   [StackedChartMark] items (which represents the stack total).
     *
     * The result is rounded to the nearest integer and returned as a [Float].
     *
     * @param data Chart data marks.
     * @param chartType Chart type (used for stacked bar handling).
     * @return Average Y value (rounded to an integer).
     */
    fun calculateAverage(data: List<BaseChartMark>, chartType: ChartType): Float {
        if (data.isEmpty()) return 0f

        return when (chartType) {
            ChartType.STACKED_BAR -> {
                val stackedData = data.filterIsInstance<StackedChartMark>()
                if (stackedData.isEmpty()) {
                    data.map { it.y }.average().roundToInt().toFloat()
                } else {
                    stackedData.map { it.y }.average().roundToInt().toFloat()
                }
            }

            else -> data.map { it.y }.average().roundToInt().toFloat()
        }
    }

    /**
     * Computes a trend line using simple linear regression.
     *
     * The returned pair represents `(slope, intercept)` for the line:
     * `y = slope * x + intercept`.
     *
     * If fewer than 2 points are provided, `(0f, 0f)` is returned.
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

    /**
     * Draws the selected reference line as an overlay.
     *
     * This composable renders nothing when:
     * - [referenceLineType] is [ReferenceLineType.NONE]
     * - [data] is empty
     *
     * @param modifier Modifier applied to the overlay container.
     * @param data Chart data marks used to compute the reference line.
     * @param metrics Chart metrics used for coordinate conversion.
     * @param chartType Chart type (affects average computation for stacked bars).
     * @param referenceLineType Which reference line to draw.
     * @param color Line/label color.
     * @param strokeWidth Line width.
     * @param lineStyle Line dash style.
     * @param showLabel Whether to show the value label (average line only).
     * @param labelFormat Label formatting string (e.g., `"평균: %.0f"`).
     * @param yAxisPosition Y-axis placement (affects label alignment/position).
     * @param interactive If true, average line supports touch toggle behavior.
     * @param onClick Optional click callback (applies to average line when [interactive] is false).
     */
    @Composable
    fun ReferenceLine(
        modifier: Modifier = Modifier,
        data: List<BaseChartMark>,
        metrics: ChartMath.ChartMetrics,
        chartType: ChartType,
        referenceLineType: ReferenceLineType,
        color: Color = Color.Red,
        strokeWidth: Dp = 2.dp,
        lineStyle: LineStyle = LineStyle.DASHED,
        showLabel: Boolean = true,
        labelFormat: String = "평균: %.0f",
        yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
        interactive: Boolean = false,
        onClick: (() -> Unit)? = null,
    ) {
        if (referenceLineType == ReferenceLineType.NONE || data.isEmpty()) return

        Box(modifier = modifier.fillMaxSize()) {
            when (referenceLineType) {
                ReferenceLineType.AVERAGE -> {
                    AverageLine(
                        data = data,
                        metrics = metrics,
                        chartType = chartType,
                        color = color,
                        strokeWidth = strokeWidth,
                        lineStyle = lineStyle,
                        showLabel = showLabel,
                        labelFormat = labelFormat,
                        yAxisPosition = yAxisPosition,
                        interactive = interactive,
                        onClick = onClick,
                    )
                }

                ReferenceLineType.TREND -> {
                    TrendLine(
                        data = data,
                        metrics = metrics,
                        color = color,
                        strokeWidth = strokeWidth,
                        lineStyle = lineStyle,
                        interactive = interactive,
                        onClick = onClick,
                        showLabel = showLabel,
                        labelFormat = labelFormat
                    )
                }

                ReferenceLineType.NONE -> {
                    // no-op
                }
            }
        }
    }

    /**
     * Renders an average line as a horizontal dashed/solid line positioned at the average Y value.
     *
     * Interaction behavior:
     * - If [interactive] is true, a large touch area toggles a "pressed" state that can reveal the label.
     * - If [interactive] is false and [onClick] is provided, the touch area triggers [onClick].
     *
     * Label behavior:
     * - The label is shown if [showLabel] is true, OR if [interactive] is true and the line is pressed.
     */
    @Composable
    private fun AverageLine(
        data: List<BaseChartMark>,
        metrics: ChartMath.ChartMetrics,
        chartType: ChartType,
        color: Color,
        strokeWidth: Dp,
        lineStyle: LineStyle,
        showLabel: Boolean,
        labelFormat: String,
        yAxisPosition: YAxisPosition,
        interactive: Boolean,
        onClick: (() -> Unit)?
    ) {
        val average = calculateAverage(data, chartType)

        // Do not draw if the average is outside the visible chart range.
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
                    y = with(density) { (y.toFloat() - touchThreshold.toPx()).toDp() }
                )
                .size(width = lineWidth, height = touchAreaHeight)
        ) {
            Canvas(
                modifier = Modifier.size(width = lineWidth, height = touchAreaHeight)
            ) {
                val startX = 0f
                val endX = size.width
                val lineY = touchThreshold.toPx()

                val pathEffect = lineStyle.dashPattern?.let {
                    PathEffect.dashPathEffect(it, 0f)
                }

                drawLine(
                    color = color,
                    start = Offset(startX, lineY),
                    end = Offset(endX, lineY),
                    strokeWidth = strokeWidth.toPx(),
                    pathEffect = pathEffect
                )
            }

            if (interactive) {
                Box(
                    modifier = Modifier
                        .size(width = lineWidth, height = touchAreaHeight)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                isPressed = !isPressed
                            }
                        }
                )
            } else if (onClick != null) {
                Box(
                    modifier = Modifier
                        .size(width = lineWidth, height = touchAreaHeight)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onClick.invoke()
                        }
                )
            }

            if (showLabel || (interactive && isPressed)) {
                ReferenceLineLabel(
                    value = average,
                    yPosition = with(density) { touchThreshold.toPx() },
                    color = color,
                    labelFormat = labelFormat,
                    metrics = metrics,
                    yAxisPosition = yAxisPosition
                )
            }
        }
    }

    /**
     * Renders a trend line computed via linear regression.
     *
     * Current behavior:
     * - Draw-only overlay (no interaction).
     * - The line is drawn between the first and last data x-values mapped to the chart bounds.
     *
     * Note: [interactive], [onClick], [showLabel], and [labelFormat] are currently accepted to keep
     * the call signature stable, but are not used by the existing implementation.
     */
    @Composable
    private fun TrendLine(
        data: List<BaseChartMark>,
        metrics: ChartMath.ChartMetrics,
        color: Color,
        strokeWidth: Dp,
        lineStyle: LineStyle,
        showLabel: Boolean,
        labelFormat: String,
        interactive: Boolean,
        onClick: (() -> Unit)?,
    ) {
        val (slope, intercept) = calculateTrendLine(data)

        val startX = metrics.paddingX
        val endX = metrics.paddingX + metrics.chartWidth

        val dataStartX = if (data.isNotEmpty()) data.first().x else 0f
        val dataEndX = if (data.isNotEmpty()) data.last().x else 1f

        val startY = slope * dataStartX.toFloat() + intercept
        val endY = slope * dataEndX.toFloat() + intercept

        val denom = (metrics.maxY - metrics.minY)
        val screenStartY = metrics.chartHeight - ((startY - metrics.minY) / denom) * metrics.chartHeight
        val screenEndY = metrics.chartHeight - ((endY - metrics.minY) / denom) * metrics.chartHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val pathEffect = lineStyle.dashPattern?.let {
                PathEffect.dashPathEffect(it, 0f)
            }

            drawLine(
                color = color,
                start = Offset(startX, screenStartY.toFloat()),
                end = Offset(endX, screenEndY.toFloat()),
                strokeWidth = strokeWidth.toPx(),
                pathEffect = pathEffect
            )
        }
    }

    /**
     * Displays a small pill label showing the reference line value.
     *
     * The label is positioned near the corresponding Y-axis side (left/right) based on
     * [yAxisPosition]. Coordinates are relative to the parent line container.
     *
     * Note: Several sizes here are intentionally kept as fixed values to preserve the
     * existing look-and-feel.
     */
    @Composable
    private fun ReferenceLineLabel(
        value: Float,
        yPosition: Float,
        color: Color,
        labelFormat: String,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition
    ) {
        val density = LocalDensity.current
        val labelText = labelFormat.format(value)

        val textSize = 12.sp
        val verticalPadding = 5.dp
        val estimatedLabelHeight = with(density) {
            textSize.toPx() + (verticalPadding * 2).toPx()
        }

        val labelX = when (yAxisPosition) {
            YAxisPosition.LEFT -> -with(density) { 5.dp.toPx() }
            YAxisPosition.RIGHT -> metrics.chartWidth + with(density) { 5.dp.toPx() }
        }

        val adjustedX = with(density) { labelX.toDp() }
        val adjustedY = with(density) { yPosition.toDp() }

        val labelHeightOffset = with(density) { estimatedLabelHeight.toDp() }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = adjustedX.roundToPx(),
                        y = (adjustedY - labelHeightOffset).roundToPx()
                    )
                }
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                )
                .clip(RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = labelText,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}