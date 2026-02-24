package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.time.YearMonth

/**
 * Math/utilities for calendar-style charts (e.g., month grid heatmaps / bubble calendars).
 *
 * This object provides:
 * - Month grid metrics (first weekday offset, number of days, number of rows)
 * - Bubble sizing based on a normalized value
 * - Bubble color lightness adjustment based on a normalized value
 */
object CalendarChartMath {

    /**
     * Computes layout metrics needed to render a month calendar grid.
     *
     * The returned triple is:
     * - `firstDayOffset`: column index of the first day of the month (Sunday = 0)
     * - `totalDays`: number of days in the month
     * - `weeks`: number of rows (weeks) required to display the month
     *
     * Weekday offset notes:
     * - `java.time.DayOfWeek.value` uses Monday = 1 ... Sunday = 7.
     * - This function converts it so Sunday becomes 0, Monday 1, ..., Saturday 6.
     *
     * @param yearMonth The year-month to display.
     * @return Triple(firstDayOffset, totalDays, weeks).
     */
    fun computeCalendarMetrics(yearMonth: YearMonth): Triple<Int, Int, Int> {
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()

        // Sunday -> 0, Monday -> 1, ..., Saturday -> 6
        val firstDayOffset = firstDayOfMonth.dayOfWeek.value % 7
        val totalDays = lastDayOfMonth.dayOfMonth

        // Rows needed = ceil((offset + totalDays) / 7)
        val weeks = (firstDayOffset + totalDays + 6) / 7

        return Triple(firstDayOffset, totalDays, weeks)
    }

    /**
     * Computes the bubble radius/size for a given value in `[0, maxValue]`.
     *
     * The result is a linear interpolation between [minSize] and [maxSize]:
     * `minSize + (maxSize - minSize) * (value / maxValue)`.
     *
     * If [maxValue] is non-positive, [minSize] is returned.
     *
     * @param value Current value.
     * @param maxValue Maximum possible value (used for normalization).
     * @param minSize Minimum bubble size.
     * @param maxSize Maximum bubble size.
     * @return Bubble size for the given value.
     */
    fun calculateBubbleSize(
        value: Float,
        maxValue: Float,
        minSize: Float,
        maxSize: Float
    ): Float {
        if (maxValue <= 0f) return minSize
        val normalizedValue = value / maxValue
        return minSize + (maxSize - minSize) * normalizedValue
    }

    /**
     * Computes a bubble color by adjusting the lightness of [color] based on [value].
     *
     * Algorithm:
     * - Convert [color] to HSL
     * - Keep hue and saturation
     * - Increase lightness as [value] decreases (up to a max lightness of 0.9)
     *
     * This produces a visual effect where small values appear lighter, while the maximum
     * value is closest to the original color.
     *
     * Note: [minSize] and [maxSize] are accepted but not used by the current implementation.
     * They are kept to preserve the existing public signature.
     *
     * If [maxValue] is non-positive, [color] is returned unchanged.
     *
     * @param color Base color.
     * @param value Current value.
     * @param maxValue Maximum possible value (used for normalization).
     * @param minSize Minimum bubble size (unused; kept for API stability).
     * @param maxSize Maximum bubble size (unused; kept for API stability).
     * @return Color with adjusted lightness.
     */
    fun calculateBubbleColor(
        color: Color,
        value: Float,
        maxValue: Float,
        minSize: Float,
        maxSize: Float
    ): Color {
        if (maxValue <= 0f) return color

        val normalizedValue = (value / maxValue).coerceIn(0f, 1f)

        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(color.toArgb(), hsl)

        val originalL = hsl[2]

        // At max value: keep original lightness.
        // As value decreases: increase lightness up to 0.9.
        hsl[2] = originalL + (0.9f - originalL) * (1f - normalizedValue)
        hsl[2] = hsl[2].coerceIn(0f, 0.9f)

        val newArgb = androidx.core.graphics.ColorUtils.HSLToColor(hsl)
        return Color(newArgb)
    }
}