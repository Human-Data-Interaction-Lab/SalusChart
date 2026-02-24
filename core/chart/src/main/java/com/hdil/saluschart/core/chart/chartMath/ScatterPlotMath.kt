package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartMark

/**
 * Math utilities for scatter plots.
 *
 * This object currently provides a mapper that converts scatter plot data points into
 * canvas coordinates using the chart's [ChartMath.ChartMetrics].
 *
 * Current positioning strategy:
 * - X values are treated as *categorical* values (even if they are numeric).
 * - Unique X values are collected, sorted, and each category is assigned an equal-width slot.
 * - Each point is placed at the center of its category slot: `(idx + 0.5) * spacing`.
 *
 * Notes:
 * - The function computes minX/maxX but currently uses category-based positioning instead of
 *   continuous mapping. This preserves the existing rendering behavior and supports multiple
 *   points per X category.
 */
object ScatterPlotMath {

    /**
     * Maps scatter plot data points to canvas coordinates (px).
     *
     * Y mapping:
     * - Values are mapped from [metrics.minY]..[metrics.maxY] into the chart's drawable height
     *   and then offset by [metrics.paddingY].
     *
     * X mapping:
     * - Uses category slots derived from distinct, sorted X values.
     * - Points are centered within their slot.
     *
     * @param data Scatter plot data points.
     * @param size Canvas size (currently unused; kept for API stability).
     * @param metrics Chart metrics (padding, width/height, Y range).
     * @return A list of [Offset] positions in canvas coordinates.
     */
    fun mapScatterToCanvasPoints(
        data: List<ChartMark>,
        size: Size,
        metrics: ChartMath.ChartMetrics
    ): List<Offset> {
        if (data.isEmpty()) return emptyList()

        // X range is computed but not used by the current category-based strategy.
        val minX = data.minOf { it.x }
        val maxX = data.maxOf { it.x }
        val xRange = if (maxX > minX) (maxX - minX).toFloat() else 1f

        val uniqueXs = data.map { it.x }.distinct().sorted()
        val categoriesCount = uniqueXs.size

        // Each category gets an equal slot; points are placed at slot centers.
        val spacing = metrics.chartWidth / categoriesCount

        return data.map { point ->
            val idx = uniqueXs.binarySearch(point.x).let { if (it >= 0) it else 0 }

            val xPosition = metrics.paddingX + (idx + 0.5f) * spacing

            val yPosition =
                metrics.paddingY +
                        metrics.chartHeight -
                        ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)).toFloat() * metrics.chartHeight

            Offset(xPosition, yPosition)
        }
    }
}