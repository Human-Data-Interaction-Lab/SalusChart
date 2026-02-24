package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Time-based data transformation engine.
 *
 * Converts a [TemporalDataSet] from its original [TimeUnitGroup] granularity into a target time unit
 * by grouping and aggregating values.
 *
 * Typical healthcare use-cases:
 * - Sum of steps/calories per day/week/month
 * - Daily average summaries over weekly/monthly windows
 * - Duration counting (minute-resolution “event present” data → total minutes)
 * - Min/max bands per interval (e.g., heart rate ranges)
 *
 * This class is intentionally **pure** (no UI concerns) and relies on the input timestamps only.
 */
class DataTransformer {

    /**
     * Transforms a time-series dataset by grouping it into [transformTimeUnit] buckets and applying
     * [aggregationType] to each bucket.
     *
     * Aggregation semantics:
     * - [AggregationType.SUM]:
     *   Sums all values in each time bucket.
     * - [AggregationType.DAILY_AVERAGE]:
     *   Computes daily averages first, then averages those daily values inside each larger bucket
     *   (DAY/WEEK/MONTH/YEAR). Days with no data are excluded from the denominator.
     * - [AggregationType.DURATION_SUM]:
     *   Counts how many data points exist in the bucket (each point represents 1 minute),
     *   returning “minutes” as a double. Only valid when input [TemporalDataSet.timeUnit] is MINUTE.
     * - [AggregationType.MIN_MAX]:
     *   For single-value datasets only. Produces a multi-value dataset containing "min" and "max"
     *   series per bucket.
     *
     * Validations:
     * - DAILY_AVERAGE only supports DAY or coarser units (DAY/WEEK/MONTH/YEAR).
     * - DURATION_SUM only supports minute-resolution datasets.
     * - MIN_MAX only supports single-value datasets.
     *
     * @param data Input dataset.
     * @param transformTimeUnit Target grouping unit.
     * @param aggregationType Aggregation method to apply per bucket.
     * @return Transformed dataset in the target time unit.
     * @throws IllegalArgumentException If validation constraints are not met.
     */
    fun transform(
        data: TemporalDataSet,
        transformTimeUnit: TimeUnitGroup,
        aggregationType: AggregationType = AggregationType.SUM
    ): TemporalDataSet {

        // DAILY_AVERAGE is only meaningful when the target unit is DAY or coarser.
        if (aggregationType == AggregationType.DAILY_AVERAGE) {
            require(TimeUnitGroup.DAY.isSmallerThanOrEqual(transformTimeUnit)) {
                "DAILY_AVERAGE requires transformTimeUnit to be DAY or larger. " +
                        "transformTimeUnit=$transformTimeUnit"
            }
        }

        // DURATION_SUM requires minute-resolution inputs (each point ≈ 1 minute).
        if (aggregationType == AggregationType.DURATION_SUM) {
            require(data.timeUnit == TimeUnitGroup.MINUTE) {
                "DURATION_SUM is only supported for MINUTE TemporalDataSet. current=${data.timeUnit}"
            }
        }

        // MIN_MAX applies only to single-value datasets.
        if (aggregationType == AggregationType.MIN_MAX) {
            require(data.isSingleValue) {
                "MIN_MAX is only supported for single-value TemporalDataSet."
            }
        }

        // Fast path: same unit and SUM means no-op.
        if (data.timeUnit == transformTimeUnit && aggregationType == AggregationType.SUM) {
            return data
        }

        return groupByTimeUnit(data, transformTimeUnit, aggregationType)
    }

    /**
     * Groups the dataset timestamps into [targetTimeUnit] buckets and aggregates values according to
     * [aggregationType].
     */
    private fun groupByTimeUnit(
        data: TemporalDataSet,
        targetTimeUnit: TimeUnitGroup,
        aggregationType: AggregationType
    ): TemporalDataSet {

        // Convert Instant -> LocalDateTime using system default zone
        val parsedTimes = data.x.map { instant ->
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        }

        return if (data.isSingleValue) {
            if (aggregationType == AggregationType.MIN_MAX) {
                val timeValuePairs = parsedTimes.zip(data.y!!)
                val minMaxData = processMinMaxAggregation(timeValuePairs, targetTimeUnit)

                val newXValues = minMaxData.map { (time, _) ->
                    time.atZone(ZoneId.systemDefault()).toInstant()
                }
                val minValues = minMaxData.map { it.second.first }
                val maxValues = minMaxData.map { it.second.second }

                TemporalDataSet(
                    x = newXValues,
                    yMultiple = mapOf("min" to minValues, "max" to maxValues),
                    timeUnit = targetTimeUnit
                )
            } else {
                val timeValuePairs = parsedTimes.zip(data.y!!)
                val aggregatedData = processAggregation(timeValuePairs, targetTimeUnit, aggregationType)

                val newXValues = aggregatedData.map { (time, _) ->
                    time.atZone(ZoneId.systemDefault()).toInstant()
                }
                val newYValues = aggregatedData.map { it.second }

                TemporalDataSet(
                    x = newXValues,
                    y = newYValues,
                    timeUnit = targetTimeUnit
                )
            }
        } else {
            // Multi-value datasets (e.g., nutrients, systolic/diastolic)
            val aggregatedMultipleData = mutableMapOf<String, List<Pair<LocalDateTime, Double>>>()

            data.yMultiple!!.forEach { (property, values) ->
                val timeValuePairs = parsedTimes.zip(values)
                aggregatedMultipleData[property] = processAggregation(timeValuePairs, targetTimeUnit, aggregationType)
            }

            // Assume all properties share the same time keys (use first property as reference).
            val firstProperty = aggregatedMultipleData.keys.first()
            val newXValues = aggregatedMultipleData[firstProperty]!!.map { (time, _) ->
                time.atZone(ZoneId.systemDefault()).toInstant()
            }

            val newYMultiple = aggregatedMultipleData.mapValues { (_, timeValueList) ->
                timeValueList.map { it.second }
            }

            TemporalDataSet(
                x = newXValues,
                yMultiple = newYMultiple,
                timeUnit = targetTimeUnit
            )
        }
    }

    /**
     * Aggregates time/value pairs into [targetTimeUnit] buckets according to [aggregationType].
     */
    private fun processAggregation(
        timeValuePairs: List<Pair<LocalDateTime, Double>>,
        targetTimeUnit: TimeUnitGroup,
        aggregationType: AggregationType
    ): List<Pair<LocalDateTime, Double>> {
        return when (aggregationType) {
            AggregationType.SUM -> {
                val groupedData = groupValuesByUnit(timeValuePairs, targetTimeUnit)
                groupedData.map { (time, values) -> time to values.sum() }
                    .sortedBy { it.first }
            }

            AggregationType.DAILY_AVERAGE -> {
                calculateIntervalAverages(timeValuePairs, targetTimeUnit)
            }

            AggregationType.DURATION_SUM -> {
                val groupedData = groupValuesByUnit(timeValuePairs, targetTimeUnit)
                groupedData.map { (time, values) -> time to values.size.toDouble() }
                    .sortedBy { it.first }
            }

            AggregationType.MIN_MAX -> {
                // MIN_MAX is handled via processMinMaxAggregation() in groupByTimeUnit().
                error("Unreachable: MIN_MAX is handled separately for single-value datasets.")
            }
        }
    }

    /**
     * MIN_MAX aggregation:
     * for each [targetTimeUnit] bucket, compute (min, max).
     */
    private fun processMinMaxAggregation(
        timeValuePairs: List<Pair<LocalDateTime, Double>>,
        targetTimeUnit: TimeUnitGroup
    ): List<Pair<LocalDateTime, Pair<Double, Double>>> {
        val groupedData = groupValuesByUnit(timeValuePairs, targetTimeUnit)

        return groupedData.map { (time, values) ->
            val minValue = values.minOrNull() ?: 0.0
            val maxValue = values.maxOrNull() ?: 0.0
            time to (minValue to maxValue)
        }.sortedBy { it.first }
    }

    /**
     * Groups the raw values into buckets defined by [targetTimeUnit].
     *
     * NOTE: MINUTE grouping preserves each timestamp (no truncation), matching existing behavior.
     */
    private fun groupValuesByUnit(
        timeValuePairs: List<Pair<LocalDateTime, Double>>,
        targetTimeUnit: TimeUnitGroup
    ): Map<LocalDateTime, List<Double>> {
        return when (targetTimeUnit) {
            TimeUnitGroup.MINUTE -> timeValuePairs
                .associate { (time, value) -> time to listOf(value) }

            TimeUnitGroup.HOUR -> groupByHour(timeValuePairs)
            TimeUnitGroup.DAY -> groupByDay(timeValuePairs)
            TimeUnitGroup.WEEK -> groupByWeek(timeValuePairs)
            TimeUnitGroup.MONTH -> groupByMonth(timeValuePairs)
            TimeUnitGroup.YEAR -> groupByYear(timeValuePairs)
        }
    }

    /** Groups by hour (truncated to the start of the hour). */
    private fun groupByHour(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            time.truncatedTo(ChronoUnit.HOURS)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /** Groups by day (truncated to the start of the day). */
    private fun groupByDay(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            time.truncatedTo(ChronoUnit.DAYS)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /** Groups by week (week starts on Sunday). */
    private fun groupByWeek(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            val sunday = time.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            sunday.atStartOfDay()
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /** Groups by month (bucket key is the first day of the month at 00:00). */
    private fun groupByMonth(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            LocalDateTime.of(time.year, time.month, 1, 0, 0)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /** Groups by year (bucket key is Jan 1st at 00:00). */
    private fun groupByYear(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            LocalDateTime.of(time.year, 1, 1, 0, 0)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /**
     * DAILY_AVERAGE interval averaging.
     *
     * Algorithm:
     * 1) Normalize to daily bins by averaging all samples within each day.
     * 2) Create complete interval windows (WEEK/MONTH/YEAR) across [minTime.maxTime].
     * 3) For each window, average only over days that actually have data.
     *    (Windows with no data return 0.0.)
     */
    private fun calculateIntervalAverages(
        timeValuePairs: List<Pair<LocalDateTime, Double>>,
        targetTimeUnit: TimeUnitGroup
    ): List<Pair<LocalDateTime, Double>> {
        if (timeValuePairs.isEmpty()) return emptyList()

        val dailyAggregatedData = groupByDay(timeValuePairs).map { (date, values) ->
            date to values.average()
        }.sortedBy { it.first }

        if (dailyAggregatedData.isEmpty()) return emptyList()

        if (targetTimeUnit == TimeUnitGroup.DAY) {
            return dailyAggregatedData
        }

        val minTime = dailyAggregatedData.first().first
        val maxTime = dailyAggregatedData.last().first
        val targetIntervals = generateCompleteIntervals(minTime, maxTime, targetTimeUnit)

        return targetIntervals.map { intervalStart ->
            val intervalEnd = getIntervalEnd(intervalStart, targetTimeUnit)

            val dailyDataInWindow = dailyAggregatedData.filter { (date, _) ->
                date >= intervalStart && date < intervalEnd
            }

            val dailyAverage = if (dailyDataInWindow.isNotEmpty()) {
                dailyDataInWindow.sumOf { it.second } / dailyDataInWindow.size
            } else {
                0.0
            }

            intervalStart to dailyAverage
        }.sortedBy { it.first }
    }

    /**
     * Generates complete interval starts across the full range.
     *
     * Supported for DAILY_AVERAGE: WEEK/MONTH/YEAR only.
     */
    private fun generateCompleteIntervals(
        minTime: LocalDateTime,
        maxTime: LocalDateTime,
        targetTimeUnit: TimeUnitGroup
    ): List<LocalDateTime> {
        val intervals = mutableListOf<LocalDateTime>()

        when (targetTimeUnit) {
            TimeUnitGroup.WEEK -> {
                var current = minTime.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                    .atStartOfDay()

                val endDate = maxTime.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                while (current.toLocalDate().isBefore(endDate)) {
                    intervals.add(current)
                    current = current.plusWeeks(1)
                }
            }

            TimeUnitGroup.MONTH -> {
                var current = LocalDateTime.of(minTime.year, minTime.month, 1, 0, 0)
                val endYear = maxTime.year
                val endMonth = maxTime.monthValue

                while (current.year < endYear || (current.year == endYear && current.monthValue <= endMonth)) {
                    intervals.add(current)
                    current = current.plusMonths(1)
                }
            }

            TimeUnitGroup.YEAR -> {
                var current = LocalDateTime.of(minTime.year, 1, 1, 0, 0)
                val endYear = maxTime.year

                while (current.year <= endYear) {
                    intervals.add(current)
                    current = current.plusYears(1)
                }
            }

            else -> {
                throw IllegalArgumentException("DAILY_AVERAGE supports only DAY, WEEK, MONTH, YEAR units. target=$targetTimeUnit")
            }
        }

        return intervals
    }

    /**
     * Computes the exclusive end timestamp of an interval that starts at [intervalStart].
     *
     * NOTE: This logic may overlap with TemporalDataSetFillGaps interval computation.
     */
    private fun getIntervalEnd(intervalStart: LocalDateTime, targetTimeUnit: TimeUnitGroup): LocalDateTime {
        return when (targetTimeUnit) {
            TimeUnitGroup.MINUTE -> intervalStart.plusMinutes(1)
            TimeUnitGroup.HOUR -> intervalStart.plusHours(1)
            TimeUnitGroup.DAY -> intervalStart.plusDays(1)
            TimeUnitGroup.WEEK -> intervalStart.plusWeeks(1)
            TimeUnitGroup.MONTH -> intervalStart.plusMonths(1)
            TimeUnitGroup.YEAR -> intervalStart.plusYears(1)
        }
    }
}