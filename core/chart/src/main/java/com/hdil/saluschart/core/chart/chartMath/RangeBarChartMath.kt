package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath.ChartMetrics

/**
 * Math utilities specific to range bar charts.
 *
 * This object currently provides a helper to compute [ChartMetrics] for range bars by:
 * - Applying fixed padding values
 * - Collecting both min/max Y values from each [RangeChartMark]
 * - Delegating Y-axis range/tick generation to [ChartMath.computeYAxisRange]
 *
 * Note: Padding values are currently fixed to preserve existing layout behavior.
 */
object RangeBarChartMath {

    /**
     * Computes chart metrics needed to draw a range bar chart.
     *
     * The returned [ChartMetrics] includes:
     * - Fixed paddings ([paddingX], [paddingY])
     * - Drawable chart width/height derived from [size] and paddings
     * - A computed Y-axis range based on both min/max values in [data]
     *
     * @param size Total canvas size.
     * @param data Range bar marks containing min/max points.
     * @param tickCount Desired Y tick count when using "nice" ticks (default: 5).
     * @param minY Optional explicit minimum Y value (overrides computed minimum).
     * @param maxY Optional explicit maximum Y value (overrides computed maximum).
     * @param fixedTickStep Optional fixed tick step (uses fixed ticks instead of "nice" ticks).
     * @return Computed [ChartMetrics] for rendering a range bar chart.
     */
    fun computeRangeMetrics(
        size: Size,
        data: List<RangeChartMark>,
        tickCount: Int = 5,
        minY: Double? = null,
        maxY: Double? = null,
        fixedTickStep: Double? = null
    ): ChartMetrics {
        val paddingX = 30f
        val paddingY = 40f
        val chartWidth = size.width - paddingX * 2
        val chartHeight = size.height - paddingY

        // Extract all Y values (both min and max) to compute the full range.
        val allYValues = data.flatMap { listOf(it.minPoint.y, it.maxPoint.y) }

        val yAxisRange = ChartMath.computeYAxisRange(
            values = allYValues,
            chartType = ChartType.RANGE_BAR,
            minY = minY,
            maxY = maxY,
            fixedTickStep = fixedTickStep,
            tickCount = tickCount
        )

        return ChartMetrics(
            paddingX = paddingX,
            paddingY = paddingY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            yAxisRange = yAxisRange
        )
    }
}