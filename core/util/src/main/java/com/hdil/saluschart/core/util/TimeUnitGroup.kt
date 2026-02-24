package com.hdil.saluschart.core.util

/**
 * Represents time unit granularity levels used for grouping
 * time-series data.
 *
 * Ordering reflects increasing time span.
 */
enum class TimeUnitGroup(
    val level: Int
) {
    MINUTE(0),
    HOUR(1),
    DAY(2),
    WEEK(3),
    MONTH(4),
    YEAR(5);

    /** Returns true if this unit is smaller (finer granularity) than [other]. */
    fun isSmallerThan(other: TimeUnitGroup): Boolean {
        return this.level < other.level
    }

    /** Returns true if this unit is smaller than or equal to [other]. */
    fun isSmallerThanOrEqual(other: TimeUnitGroup): Boolean {
        return this.level <= other.level
    }

    /** Returns true if this unit is larger (coarser granularity) than [other]. */
    fun isBiggerThan(other: TimeUnitGroup): Boolean {
        return this.level > other.level
    }
}