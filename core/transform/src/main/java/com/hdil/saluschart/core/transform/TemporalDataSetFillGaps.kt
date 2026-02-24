package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Fills missing time buckets inside a [TemporalDataSet] so charts can render continuous time axes.
 *
 * IMPORTANT:
 * - This should typically be used *after* aggregation/transform to the target [timeUnit].
 * - If the dataset contains multiple points that normalize to the same bucket, gap-filling becomes
 *   ambiguous (because the mapping bucket -> value is no longer 1:1).
 *
 * By default, missing buckets are filled with 0.0. Be careful with sparse measurement data
 * (e.g., blood glucose, blood pressure, weight): filling with 0.0 can distort min/max and averages.
 *
 * @param fillValue Value used for missing buckets (default: 0.0).
 *                 Consider using [Double.NaN] for “missing” when you want downstream logic
 *                 to ignore gaps (but your chart/aggregation must handle NaN).
 */
fun TemporalDataSet.fillTemporalGaps(fillValue: Double = 0.0): TemporalDataSet {
    if (x.isEmpty()) return this

    val minTime = x.minOrNull() ?: return this
    val maxTime = x.maxOrNull() ?: return this

    val completeTimePoints = generateCompleteTimePoints(minTime, maxTime, timeUnit)

    // Normalize existing timestamps to bucket keys
    val normalizedExisting = x.map { normalizeTimePoint(it, timeUnit) }

    // Fail fast if duplicates exist in the same normalized bucket
    // (otherwise zip(...).toMap() silently overwrites values).
    val dupCount = normalizedExisting.size - normalizedExisting.distinct().size
    require(dupCount == 0) {
        "fillTemporalGaps() requires the dataset to be aggregated to a single value per '$timeUnit' bucket. " +
                "Found $dupCount duplicate bucket(s). Call transform(...) first or aggregate before filling gaps."
    }

    return if (isSingleValue) {
        val existingDataMap = normalizedExisting.zip(y!!).toMap()

        val filledY = completeTimePoints.map { timePoint ->
            val key = normalizeTimePoint(timePoint, timeUnit)
            existingDataMap[key] ?: fillValue
        }

        TemporalDataSet(
            x = completeTimePoints,
            y = filledY,
            timeUnit = timeUnit
        )
    } else {
        val existingMaps = yMultiple!!.mapValues { (_, values) ->
            normalizedExisting.zip(values).toMap()
        }

        val filledMultiple = existingMaps.mapValues { (_, dataMap) ->
            completeTimePoints.map { timePoint ->
                val key = normalizeTimePoint(timePoint, timeUnit)
                dataMap[key] ?: fillValue
            }
        }

        TemporalDataSet(
            x = completeTimePoints,
            yMultiple = filledMultiple,
            timeUnit = timeUnit
        )
    }
}

/**
 * Generates all bucket timestamps between [minTime] and [maxTime] inclusive for [timeUnit].
 */
private fun generateCompleteTimePoints(
    minTime: Instant,
    maxTime: Instant,
    timeUnit: TimeUnitGroup
): List<Instant> {
    val zoneId = ZoneId.systemDefault()
    val start = normalizeToTimeUnitDateTime(LocalDateTime.ofInstant(minTime, zoneId), timeUnit)
    val end = normalizeToTimeUnitDateTime(LocalDateTime.ofInstant(maxTime, zoneId), timeUnit)

    val points = mutableListOf<LocalDateTime>()
    var current = start
    while (!current.isAfter(end)) {
        points.add(current)
        current = incrementByTimeUnit(current, timeUnit)
    }

    return points.map { it.atZone(zoneId).toInstant() }
}

/**
 * Normalizes [dateTime] to the start of the corresponding bucket for [timeUnit].
 */
private fun normalizeToTimeUnitDateTime(dateTime: LocalDateTime, timeUnit: TimeUnitGroup): LocalDateTime {
    return when (timeUnit) {
        TimeUnitGroup.MINUTE -> dateTime.truncatedTo(ChronoUnit.MINUTES)
        TimeUnitGroup.HOUR -> dateTime.truncatedTo(ChronoUnit.HOURS)
        TimeUnitGroup.DAY -> dateTime.truncatedTo(ChronoUnit.DAYS)
        TimeUnitGroup.WEEK -> {
            val sunday = dateTime.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            sunday.atStartOfDay()
        }
        TimeUnitGroup.MONTH -> LocalDateTime.of(dateTime.year, dateTime.month, 1, 0, 0)
        TimeUnitGroup.YEAR -> LocalDateTime.of(dateTime.year, 1, 1, 0, 0)
    }
}

/**
 * Normalizes an [Instant] into the corresponding time bucket key for [timeUnit].
 */
private fun normalizeTimePoint(instant: Instant, timeUnit: TimeUnitGroup): Instant {
    val zoneId = ZoneId.systemDefault()
    val normalized = normalizeToTimeUnitDateTime(LocalDateTime.ofInstant(instant, zoneId), timeUnit)
    return normalized.atZone(zoneId).toInstant()
}

/**
 * Increments [dateTime] by exactly one bucket of [timeUnit].
 */
private fun incrementByTimeUnit(dateTime: LocalDateTime, timeUnit: TimeUnitGroup): LocalDateTime {
    return when (timeUnit) {
        TimeUnitGroup.MINUTE -> dateTime.plusMinutes(1)
        TimeUnitGroup.HOUR -> dateTime.plusHours(1)
        TimeUnitGroup.DAY -> dateTime.plusDays(1)
        TimeUnitGroup.WEEK -> dateTime.plusWeeks(1)
        TimeUnitGroup.MONTH -> dateTime.plusMonths(1)
        TimeUnitGroup.YEAR -> dateTime.plusYears(1)
    }
}