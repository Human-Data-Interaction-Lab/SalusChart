package com.hdil.saluschart.core.chart.chartMath

import android.util.Log
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartType
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

// TODO : metric 관련 함수에서 고정값 수정 필요
/**
 * Shared math utilities for chart layout and axis calculations.
 *
 * This object centralizes:
 * - Axis tick generation (nice ticks using 1/2/5 steps)
 * - Y-axis range computation (data-only, no pixel metrics)
 * - Chart metrics computation (padding + drawable width/height + Y-axis range)
 * - X-axis label auto-skip based on measured text widths
 *
 * It also exposes sub-modules used by specific chart types (Pie, Calendar, Progress, etc.).
 */
object ChartMath {

    /** Pie chart math utilities. */
    var Pie = PieChartMath

    /** Calendar chart math utilities. */
    var Calendar = CalendarChartMath

    /** Range bar chart math utilities. */
    val RangeBar = RangeBarChartMath

    /** Line chart math utilities. */
    val Line = LineChartMath

    /** Scatter plot math utilities. */
    val Scatter = ScatterPlotMath

    /** Progress chart math utilities. */
    val Progress = ProgressChartMath

    /**
     * Pure Y-axis range information (no pixel sizing).
     *
     * Useful for cases where you want to precompute a unified Y-axis across multiple pages
     * (e.g., paging charts).
     *
     * @param minY Minimum Y value of the axis.
     * @param maxY Maximum Y value of the axis.
     * @param yTicks Tick values to display along the Y axis.
     */
    data class YAxisRange(
        val minY: Double,
        val maxY: Double,
        val yTicks: List<Double>
    ) {
        /**
         * Tick spacing inferred from [yTicks].
         *
         * @return Step between adjacent ticks, or 10.0 if fewer than 2 ticks exist.
         */
        val tickStep: Double
            get() = if (yTicks.size >= 2) yTicks[1] - yTicks[0] else 10.0
    }

    /**
     * Pixel-level metrics required to draw a chart into a canvas.
     *
     * @param paddingX Horizontal padding applied to both left and right sides (in px).
     * @param paddingY Top padding applied above the drawable chart area (in px).
     * @param chartWidth Width of the drawable chart area (in px).
     * @param chartHeight Height of the drawable chart area (in px).
     * @param yAxisRange Data-only Y-axis range and ticks.
     */
    data class ChartMetrics(
        val paddingX: Float,
        val paddingY: Float,
        val chartWidth: Float,
        val chartHeight: Float,
        val yAxisRange: YAxisRange
    ) {
        /** Convenience accessor for [YAxisRange.minY]. */
        val minY: Double get() = yAxisRange.minY

        /** Convenience accessor for [YAxisRange.maxY]. */
        val maxY: Double get() = yAxisRange.maxY

        /** Convenience accessor for [YAxisRange.yTicks]. */
        val yTicks: List<Double> get() = yAxisRange.yTicks
    }

    /**
     * Generates "nice" Y-axis tick values using 1/2/5 * 10^n step sizes.
     *
     * This produces readable tick marks by selecting a step close to the ideal step
     * `(max - min) / tickCount`, then expanding min/max outward to multiples of that step.
     *
     * Chart type behavior:
     * - For BAR/STACKED_BAR/MINIMAL_BAR, the minimum is forced to 0 unless overridden by [actualMin].
     *
     * User overrides:
     * - If [actualMin] and/or [actualMax] are provided, they are always used as the final bounds,
     *   and ticks are generated accordingly (including de-duplication).
     *
     * Notes:
     * - If `min >= max`, a fallback list `[0.0, 1.0]` is returned.
     * - Floating values are rounded to 6 decimal places to reduce noise.
     *
     * @param min Minimum data value.
     * @param max Maximum data value.
     * @param tickCount Desired number of tick intervals (default: 5).
     * @param chartType Optional chart type to apply special rules (e.g., zero-based bars).
     * @param actualMin Optional explicit minimum Y value.
     * @param actualMax Optional explicit maximum Y value.
     * @return A sorted list of tick values.
     */
    fun computeNiceTicks(
        min: Double,
        max: Double,
        tickCount: Int = 5,
        chartType: ChartType? = null,
        actualMin: Double? = null,
        actualMax: Double? = null
    ): List<Double> {
        if (min >= max) {
            return listOf(0.0, 1.0)
        }

        // For bar charts, force the minimum to 0 unless the caller overrides via actualMin/minY.
        val adjustedMin = if (
            chartType == ChartType.BAR ||
            chartType == ChartType.STACKED_BAR
        ) {
            0.0
        } else {
            min
        }

        val rawStep = (max - adjustedMin) / tickCount.toDouble()
        val power = 10.0.pow(floor(log10(rawStep)))
        val candidates = listOf(1.0, 2.0, 5.0).map { it * power }
        val step = candidates.minByOrNull { abs(it - rawStep) } ?: power

        val niceMin = floor(adjustedMin / step) * step
        val niceMax = ceil(max / step) * step

        // User overrides always win (expand or shrink).
        val finalMin = actualMin ?: niceMin
        val finalMax = actualMax ?: niceMax

        val ticks = mutableListOf<Double>()

        // Include explicit user min first (if provided).
        actualMin?.let { ticks.add(it) }

        // Start tick generation:
        // - If actualMin exists, start from the next step boundary at/above finalMin.
        // - Otherwise start from finalMin directly.
        var t = if (actualMin != null) {
            ceil(finalMin / step) * step
        } else {
            finalMin
        }

        while (t <= finalMax + 1e-6) {
            val roundedTick = round(t * 1_000_000) / 1_000_000
            val tickValue = roundedTick

            // Avoid duplicating explicit user bounds.
            if (actualMin == null || abs(tickValue - actualMin) > 1e-6) {
                if (actualMax == null || abs(tickValue - actualMax) > 1e-6) {
                    ticks.add(tickValue)
                }
            }

            t += step
        }

        // Include explicit user max last (if provided).
        actualMax?.let { ticks.add(it) }

        return ticks.distinct().sorted()
    }

    /**
     * Computes the Y-axis range and ticks without any pixel-based sizing.
     *
     * This is useful when:
     * - You want a unified Y-axis range across multiple pages (paging charts).
     * - You want to precompute ticks before drawing into a specific canvas size.
     *
     * Behavior:
     * - [minY] / [maxY] override the computed bounds.
     * - If [fixedTickStep] is provided (> 0), ticks are generated at that fixed interval.
     * - Otherwise, ticks are generated via [computeNiceTicks].
     *
     * Chart type behavior:
     * - BAR/STACKED_BAR/MINIMAL_BAR/SCATTERPLOT currently request a zero-based minimum
     *   unless overridden by [minY].
     *
     * @param values Y values present in the chart.
     * @param chartType Optional chart type to apply special rules.
     * @param minY Optional explicit minimum Y value.
     * @param maxY Optional explicit maximum Y value.
     * @param fixedTickStep Optional fixed tick step (uses fixed ticks instead of "nice" ticks).
     * @param tickCount Desired tick count when using "nice" ticks (default: 5).
     * @return A [YAxisRange] containing min/max bounds and tick list.
     */
    fun computeYAxisRange(
        values: List<Double>,
        chartType: ChartType? = null,
        minY: Double? = null,
        maxY: Double? = null,
        fixedTickStep: Double? = null,
        tickCount: Int = 5
    ): YAxisRange {
        val dataMax = values.maxOrNull() ?: 1.0
        val dataMin = values.minOrNull() ?: 0.0

        val wantsZeroMin =
            chartType == ChartType.BAR ||
                    chartType == ChartType.STACKED_BAR ||
                    chartType == ChartType.SCATTERPLOT

        val baseMin = minY ?: if (wantsZeroMin) 0.0 else dataMin
        val baseMax = maxY ?: dataMax

        val yTicks: List<Double>
        val actualMinY: Double
        val actualMaxY: Double

        if (fixedTickStep != null && fixedTickStep > 0.0) {
            val start = if (wantsZeroMin) {
                0.0
            } else {
                floor(baseMin / fixedTickStep) * fixedTickStep
            }
            val end = ceil(baseMax / fixedTickStep) * fixedTickStep

            val ticks = mutableListOf<Double>()
            var t = start
            while (t <= end + 1e-6) {
                ticks.add(t)
                t += fixedTickStep
            }

            yTicks = ticks
            actualMinY = minY ?: start
            actualMaxY = maxY ?: end
        } else {
            val ticksNice = computeNiceTicks(
                min = baseMin,
                max = baseMax,
                tickCount = tickCount,
                chartType = chartType,
                actualMin = minY,
                actualMax = maxY
            )

            yTicks = ticksNice
            actualMinY = minY ?: (ticksNice.minOrNull() ?: baseMin)
            actualMaxY = maxY ?: (ticksNice.maxOrNull() ?: baseMax)
        }

        return YAxisRange(
            minY = actualMinY,
            maxY = actualMaxY,
            yTicks = yTicks
        )
    }

    /**
     * Computes pixel-level chart metrics required for drawing into a canvas [size].
     *
     * The returned [ChartMetrics] contains:
     * - Final padding values
     * - Drawable chart width/height
     * - The computed [YAxisRange]
     *
     * Padding behavior:
     * - [paddingX] is used as the default horizontal padding for "normal" charts.
     * - If [includeYAxisPadding] is false, horizontal padding is forced to 0.
     * - [yAxisPaddingPx] allows providing a specific X padding for Y-axis label space.
     *
     * Tick behavior:
     * - If [fixedTickStep] is provided (> 0), fixed ticks are used.
     * - Otherwise "nice" ticks are used.
     *
     * @param size Total canvas size.
     * @param values Y values present in the chart.
     * @param tickCount Desired tick count when using "nice" ticks (default: 5).
     * @param chartType Optional chart type to apply special rules.
     * @param isMinimal Whether this is a minimal chart mode (affects default padding values).
     * @param paddingX Base horizontal padding (default depends on [isMinimal]).
     * @param paddingY Top padding (default depends on [isMinimal]).
     * @param minY Optional explicit minimum Y value.
     * @param maxY Optional explicit maximum Y value.
     * @param includeYAxisPadding Whether to include horizontal padding for Y-axis labels.
     * @param yAxisPaddingPx Horizontal padding to use when [includeYAxisPadding] is true.
     * @param fixedTickStep Optional fixed tick step.
     */
    fun computeMetrics(
        size: Size,
        values: List<Double>,
        tickCount: Int = 5,
        chartType: ChartType? = null,
        isMinimal: Boolean = false,
        paddingX: Float = if (isMinimal) 4f else 30f,
        paddingY: Float = if (isMinimal) 8f else 40f,
        minY: Double? = null,
        maxY: Double? = null,
        includeYAxisPadding: Boolean = true,
        yAxisPaddingPx: Float = paddingX,
        fixedTickStep: Double? = null
    ): ChartMetrics {
        val effectivePaddingX = if (includeYAxisPadding) yAxisPaddingPx else 0f

        Log.e("ChartMath", "includYAxisPadding: $includeYAxisPadding, yAxisPaddingPx: $yAxisPaddingPx")

        val yAxisRange = computeYAxisRange(
            values = values,
            chartType = chartType,
            minY = minY,
            maxY = maxY,
            fixedTickStep = fixedTickStep,
            tickCount = tickCount
        )

        val chartWidth = size.width - effectivePaddingX * 2f
        val chartHeight = size.height - paddingY

        Log.e(
            "ChartMath",
            "Computed Metrics - chartWidth: $chartWidth, chartHeight: $chartHeight, " +
                    "paddingX: $effectivePaddingX, paddingY: $paddingY, yAxisRange: $yAxisRange"
        )

        return ChartMetrics(
            paddingX = effectivePaddingX,
            paddingY = paddingY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            yAxisRange = yAxisRange
        )
    }

    /**
     * Auto-reduces X-axis labels to avoid overlap by measuring actual text width.
     *
     * This function:
     * 1) Measures each label width using [android.graphics.Paint]
     * 2) Estimates how many labels can fit into [chartWidth]
     * 3) Chooses a skip interval (`skipRatio`) and returns a reduced label list + indices
     *
     * Rules:
     * - Always includes the first label.
     * - Applies [maxXTicksLimit] if provided.
     * - Returns `(emptyList(), emptyList())` when [labels] is empty.
     *
     * @param labels Original X-axis labels.
     * @param textSize Text size in pixels.
     * @param chartWidth Available chart width in pixels.
     * @param maxXTicksLimit Optional max number of labels to show.
     * @return Pair(reducedLabels, reducedIndices).
     */
    fun computeAutoSkipLabels(
        labels: List<String>,
        textSize: Float,
        chartWidth: Float,
        maxXTicksLimit: Int? = null
    ): Pair<List<String>, List<Int>> {
        if (labels.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }

        if (labels.size == 1) {
            return Pair(labels, listOf(0))
        }

        val paint = android.graphics.Paint().apply {
            this.textSize = textSize
            textAlign = android.graphics.Paint.Align.CENTER
        }

        val labelWidths = labels.map { label -> paint.measureText(label) }

        val avgLabelWidth = labelWidths.average().toFloat()

        val autoSkipPadding = textSize * 0.3f
        val spacePerLabel = avgLabelWidth + autoSkipPadding
        val estimatedCapacity = (chartWidth / spacePerLabel).toInt().coerceAtLeast(1)

        val finalCapacity = if (maxXTicksLimit != null) {
            minOf(estimatedCapacity, maxXTicksLimit)
        } else {
            estimatedCapacity
        }

        if (labels.size <= finalCapacity) {
            return Pair(labels, labels.indices.toList())
        }

        val skipRatio = ceil(labels.size.toDouble() / finalCapacity).toInt()

        val reducedLabels = mutableListOf<String>()
        val reducedIndices = mutableListOf<Int>()

        reducedLabels.add(labels[0])
        reducedIndices.add(0)

        for (i in skipRatio until labels.size step skipRatio) {
            reducedLabels.add(labels[i])
            reducedIndices.add(i)
        }

        return Pair(reducedLabels, reducedIndices)
    }
}