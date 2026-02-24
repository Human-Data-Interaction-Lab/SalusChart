package com.hdil.saluschart.core.chart

/** Visual style preset for a chart. */
enum class ChartStyle { NORMAL, MINIMAL }

/** Layout orientation when a chart supports it. */
enum class ChartOrientation { VERTICAL, HORIZONTAL }

enum class GaugeVariant { SINGLE, MULTI_SEGMENT, RANGE }

enum class ProgressVariant { RINGS, BAR }

/**
 * High-level chart families.
 *
 * This describes *what* is rendered (chart family).
 * Layout/style variants belong in separate flags (e.g., [ChartStyle], [ChartOrientation]).
 */
enum class ChartType {
    LINE,
    BAR,
    RANGE_BAR,
    STACKED_BAR,
    PIE,
    PROGRESS,     // RINGS/BAR via [ProgressVariant]
    SCATTERPLOT,
    CALENDAR,
    SLEEP_STAGE,
    GAUGE,        // SINGLE/MULTI_SEGMENT/RANGE via [GaugeVariant]
    LADDER;

    companion object {
        fun fromString(type: String?): ChartType? {
            if (type.isNullOrBlank()) return null
            return entries.find { it.name.equals(type, ignoreCase = true) }
        }
    }
}

/** Unified chart “identity + variants” container. */
data class ChartSpec(
    val type: ChartType,
    val style: ChartStyle = ChartStyle.NORMAL,
    val orientation: ChartOrientation = ChartOrientation.VERTICAL,
    val gaugeVariant: GaugeVariant? = null,
    val progressVariant: ProgressVariant? = null
)

/**
 * Represents the interaction behavior supported by a chart.
 *
 * Interaction types are separated per chart family to enforce
 * type-safe interaction configuration.
 *
 * Example:
 * - Bar charts can use [InteractionType.Bar]
 * - Line charts use [InteractionType.Line]
 */
sealed interface InteractionType {

    /**
     * Interaction modes for Bar charts.
     */
    sealed interface Bar : InteractionType {
        data object BAR : Bar              // Direct bar touch
        data object TOUCH_AREA : Bar       // Expanded touch region
    }

    /**
     * Interaction modes for Line charts.
     */
    sealed interface Line : InteractionType {
        data object POINT : Line           // Tap on specific data point
        data object TOUCH_AREA : Line      // Tap anywhere near the line
    }

    /**
     * Interaction modes for Scatter plots.
     */
    sealed interface Scatter : InteractionType {
        data object POINT : Scatter
        data object TOUCH_AREA : Scatter
    }

    /**
     * Interaction modes for Stacked Bar charts.
     */
    sealed interface StackedBar : InteractionType {
        data object BAR : StackedBar           // Individual segment tap
        data object TOUCH_AREA : StackedBar    // Whole stacked bar tap
    }

    /**
     * Interaction modes for Range Bar charts.
     */
    sealed interface RangeBar : InteractionType {
        data object BAR : RangeBar
        data object TOUCH_AREA : RangeBar
    }

    /**
     * Interaction modes for Pie charts.
     */
    sealed interface Pie : InteractionType {
        data object PIE : Pie
    }

    /**
     * No interaction.
     */
    data object None : InteractionType
}

/**
 * Defines the shape used for rendering points in Line and Scatter charts.
 */
enum class PointType {
    Circle,
    Square,
    Triangle
}