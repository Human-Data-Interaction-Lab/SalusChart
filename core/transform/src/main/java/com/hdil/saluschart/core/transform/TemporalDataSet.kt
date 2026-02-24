package com.hdil.saluschart.core.transform

import androidx.compose.runtime.Immutable
import com.hdil.saluschart.core.util.TimeUnitGroup
import java.time.Instant

/**
 * Normalized time-series container used internally by SalusChart.
 *
 * This class represents time-indexed numeric data before it is mapped
 * to chart-specific models (e.g., ChartMark).
 *
 * Invariants:
 * - Either [y] (single-value series) OR [yMultiple] (multi-value series) must be provided.
 * - Not both.
 * - All value lists must match the size of [x].
 *
 * @param x Time axis values (Instant).
 * @param y Single-value series (nullable if multi-value).
 * @param yMultiple Multi-value series map (nullable if single-value).
 * @param timeUnit Granularity of the dataset.
 */
@Immutable
data class TemporalDataSet(
    val x: List<Instant>,
    val y: List<Double>? = null,
    val yMultiple: Map<String, List<Double>>? = null,
    val timeUnit: TimeUnitGroup = TimeUnitGroup.HOUR
) {

    init {
        require((y != null) xor (yMultiple != null)) {
            "Exactly one of 'y' or 'yMultiple' must be provided."
        }

        if (y != null) {
            require(x.size == y.size) {
                "Size mismatch: x(${x.size}) and y(${y.size}) must have the same length."
            }
        }

        if (yMultiple != null) {
            require(yMultiple.isNotEmpty()) {
                "yMultiple cannot be empty."
            }

            yMultiple.forEach { (property, valueList) ->
                require(x.size == valueList.size) {
                    "Size mismatch: x(${x.size}) and yMultiple['$property'](${valueList.size}) must have the same length."
                }
            }
        }
    }

    /** True if this dataset represents a single-value time series. */
    val isSingleValue: Boolean
        get() = y != null

    /** True if this dataset represents a multi-value time series. */
    val isMultiValue: Boolean
        get() = yMultiple != null

    /**
     * Returns values for a specific property in multi-value datasets.
     * Returns null if this is a single-value dataset or property does not exist.
     */
    fun getValues(property: String): List<Double>? =
        yMultiple?.get(property)

    /**
     * Returns property names for multi-value datasets.
     * Returns empty set for single-value datasets.
     */
    val propertyNames: Set<String>
        get() = yMultiple?.keys ?: emptySet()

    /**
     * Convenience accessor for single-value datasets.
     * Returns empty list if this is a multi-value dataset.
     */
    val values: List<Double>
        get() = y ?: emptyList()
}