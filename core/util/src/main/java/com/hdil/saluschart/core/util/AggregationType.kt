package com.hdil.saluschart.core.util

/**
 * Defines how values should be aggregated when resampling/converting time-based data
 * into a different interval (e.g., hourly → daily, daily → weekly).
 *
 * This enum describes the *aggregation operation* applied to all samples that fall into
 * the same target bucket/window.
 */
enum class AggregationType {

    /**
     * Sum of all values in the bucket.
     *
     * Example: total steps per day from multiple step samples.
     */
    SUM,

    /**
     * Average value per day (daily mean).
     *
     * Use when you want a normalized "per-day" average rather than a total.
     * (Exact interpretation depends on the caller: e.g., average of daily totals.)
     */
    DAILY_AVERAGE,

    /**
     * Sum of durations in the bucket.
     *
     * Typically used for time-based metrics (e.g., minutes of exercise).
     * Convention: durations are treated as minutes unless the caller specifies otherwise.
     */
    DURATION_SUM,

    /**
     * Compute the minimum and maximum values in the bucket.
     *
     * Useful for range visualizations such as min/max (range bar) charts.
     */
    MIN_MAX
}