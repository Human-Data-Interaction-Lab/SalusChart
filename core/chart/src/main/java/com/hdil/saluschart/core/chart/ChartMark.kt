package com.hdil.saluschart.core.chart

/**
 * Base interface for all chart marks used in SalusChart.
 *
 * A "mark" represents a single logical datum plotted on a chart.
 * Some charts operate on a single mark type (e.g., [ChartMark]), while others may accept a
 * heterogeneous list and treat them uniformly via this interface.
 *
 * Coordinate conventions:
 * - [x] is the logical X position (index/category/value depending on chart).
 * - [y] is the logical Y value used for rendering and/or computations.
 * - [label] is optional display text (axis labels, legends, tooltips, etc.).
 */
interface BaseChartMark {
    /** Logical X position (index/category/value depending on chart). */
    val x: Double

    /** Logical Y value used for rendering or derived metrics. */
    val y: Double

    /** Optional display label associated with this mark. */
    val label: String?
}

/**
 * The default mark type used by most charts (line, bar, scatter, pie, etc.).
 *
 * @param x Logical X position or index.
 * @param y Y value.
 * @param label Optional label for tooltips/legends/axes.
 * @param color Optional color override (ARGB int). When null, the chart's palette is used.
 * @param isSelected Optional selection state. Some charts may use this to alter styling.
 */
data class ChartMark(
    override val x: Double,
    override val y: Double,
    override val label: String? = null,
    val color: Int? = null,
    val isSelected: Boolean = false
) : BaseChartMark {
    override fun toString(): String {
        return "ChartMark(x=$x, y=$y, label=$label, color=$color, isSelected=$isSelected)"
    }
}

/**
 * A mark for range bar charts (min/max bar).
 *
 * This represents a vertical span from [minPoint] to [maxPoint] at a given [x].
 *
 * Important:
 * - [y] is derived as `maxPoint.y - minPoint.y` to represent the span height.
 * - Consumers that need the absolute min/max should use [minPoint] and [maxPoint] directly.
 *
 * @param x Logical X position or index.
 * @param minPoint Minimum value point.
 * @param maxPoint Maximum value point.
 * @param label Optional label for tooltips/legends/axes.
 */
data class RangeChartMark(
    override val x: Double,
    val minPoint: ChartMark,
    val maxPoint: ChartMark,
    override val label: String? = null
) : BaseChartMark {
    /** Span height (max - min). */
    override val y: Double get() = maxPoint.y - minPoint.y

    override fun toString(): String {
        return "RangeChartMark(x=$x, minPoint=$minPoint, maxPoint=$maxPoint, label=$label)"
    }
}

/**
 * A mark for stacked bar charts.
 *
 * A stacked bar is composed of multiple [segments]. The total bar height is the sum of the
 * segment values.
 *
 * Important:
 * - [y] is derived as the sum of all segment y-values.
 * - Each [ChartMark] in [segments] typically represents one stacked segment.
 *
 * @param x Logical X position or index.
 * @param segments List of stacked segments.
 * @param label Optional label for tooltips/legends/axes.
 */
data class StackedChartMark(
    override val x: Double,
    val segments: List<ChartMark>,
    override val label: String? = null
) : BaseChartMark {
    /** Total stack height (sum of segment values). */
    override val y: Double get() = segments.sumOf { it.y }

    override fun toString(): String {
        return "StackedChartMark(x=$x, segments=$segments, label=$label)"
    }
}

/**
 * A mark for progress charts (donut rings or progress bars).
 *
 * Progress is derived from [current] and [max]. When [max] is 0 or negative, progress is 0.
 *
 * Derived properties:
 * - [progress] is clamped to 0..1 (for typical progress displays).
 * - [percentage] is `progress * 100`.
 * - [y] returns [progress] to fit into the common mark interface.
 *
 * @param x Logical X position or index (often 0..N-1 for multiple rings/bars).
 * @param current Current value.
 * @param max Max/target value.
 * @param label Optional label (e.g., "Move", "Exercise", "Stand").
 * @param unit Optional unit (e.g., "kJ", "min", "h").
 * @param color Optional color override (ARGB int). When null, the chart's palette is used.
 * @param isSelected Optional selection state.
 */
data class ProgressChartMark(
    override val x: Double,
    val current: Double,
    val max: Double,
    override val label: String? = null,
    val unit: String? = null,
    val color: Int? = null,
    val isSelected: Boolean = false
) : BaseChartMark {

    /** Normalized progress in 0..1, derived from current/max. */
    val progress: Double = if (max > 0.0) (current / max).coerceIn(0.0, 1.0) else 0.0

    /** Progress in percent (0..100). */
    val percentage: Double = progress * 100.0

    /** For progress charts, y is the normalized progress. */
    override val y: Double get() = progress

    override fun toString(): String {
        return "ProgressChartMark(x=$x, current=$current, max=$max, progress=$progress, label=$label, unit=$unit)"
    }
}