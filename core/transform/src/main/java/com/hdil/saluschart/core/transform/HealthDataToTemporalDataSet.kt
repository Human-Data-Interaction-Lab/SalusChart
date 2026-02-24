package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * Extension functions to convert health-domain models into [TemporalDataSet].
 *
 * Pipeline:
 * HealthData → TemporalDataSet → (DataTransformer) → ChartMark → UI chart.
 *
 * Design notes:
 * - “Interval” activities (StepCount/Exercise/Diet/SleepSession) have start/end time and are distributed
 *   into minute buckets so downstream aggregation can be applied consistently.
 * - “Point-in-time” measurements (BloodPressure/Weight/etc.) are emitted as timepoint samples.
 *
 * Convention:
 * - Most datasets here use [TimeUnitGroup.MINUTE] as a baseline “time resolution” for charting,
 *   even if the data is conceptually point-based.
 */
//
// ────────────────────────────────────────────────────────────────────────────────
// Activity / interval-based data
// ────────────────────────────────────────────────────────────────────────────────
//

/**
 * Converts interval-based [StepCount] to a minute-bucketed single-value [TemporalDataSet].
 *
 * The stepCount value of each interval is proportionally distributed across minute buckets
 * based on overlap duration. Summing all minute values returns the original total steps.
 */
fun List<StepCount>.toStepCountTemporalDataSet(): TemporalDataSet {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { mapOf("stepCount" to it.stepCount.toDouble()) }
    )

    val sortedTimes = aggregatedData.keys.sorted()
    return TemporalDataSet(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("stepCount") ?: 0.0 },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Converts interval-based [Exercise] to a minute-bucketed single-value [TemporalDataSet].
 *
 * The caloriesBurned value of each interval is proportionally distributed across minute buckets
 * based on overlap duration. Summing all minute values returns the original total calories.
 */
fun List<Exercise>.toExerciseTemporalDataSet(): TemporalDataSet {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { mapOf("caloriesBurned" to it.caloriesBurned.toDouble()) }
    )

    val sortedTimes = aggregatedData.keys.sorted()
    return TemporalDataSet(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("caloriesBurned") ?: 0.0 },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Converts interval-based [Diet] to a minute-bucketed multi-value [TemporalDataSet].
 *
 * Each minute bucket may contain:
 * - calories
 * - protein (kg)
 * - carbohydrate (kg)
 * - fat (kg)
 *
 * Values are proportionally distributed across minute buckets based on overlap duration.
 * Summing all minute values returns the original totals.
 */
fun List<Diet>.toDietTemporalDataSet(): TemporalDataSet {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { diet ->
            mapOf(
                "calories" to diet.calories.toDouble(),
                "protein" to diet.protein.toKilograms().toDouble(),
                "carbohydrate" to diet.carbohydrate.toKilograms().toDouble(),
                "fat" to diet.fat.toKilograms().toDouble()
            )
        }
    )

    val sortedTimes = aggregatedData.keys.sorted()
    val propertyNames = aggregatedData.values.flatMap { it.keys }.distinct()

    val yMultiple = propertyNames.associateWith { property ->
        sortedTimes.map { time ->
            aggregatedData[time]?.get(property) ?: 0.0
        }
    }

    return TemporalDataSet(
        x = sortedTimes,
        yMultiple = yMultiple,
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Converts [HeartRate] to a single-value [TemporalDataSet] using individual samples.
 *
 * - Each [HeartRateSample] becomes a separate point.
 * - [HeartRate.startTime]/[HeartRate.endTime] are ignored.
 *
 * Note: multiple samples can share the same timestamp; this function preserves them.
 */
fun List<HeartRate>.toHeartRateTemporalDataSet(): TemporalDataSet {
    val allSamples = this.flatMap { it.samples }

    if (allSamples.isEmpty()) {
        return TemporalDataSet(
            x = emptyList(),
            y = emptyList(),
            timeUnit = TimeUnitGroup.MINUTE
        )
    }

    val sortedSamples = allSamples.sortedBy { it.time }

    return TemporalDataSet(
        x = sortedSamples.map { it.time },
        y = sortedSamples.map { it.beatsPerMinute.toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Converts interval-based [SleepSession] to a minute-bucketed single-value [TemporalDataSet].
 *
 * The extracted value is the session’s **total sleep hours**.
 * That value is then proportionally distributed across minute buckets based on overlap duration,
 * so summing all minute bucket values reproduces the original total hours.
 *
 * Note:
 * - Sleep stages are not preserved here. If you render sleep-stage charts, use a dedicated mapper.
 */
fun List<SleepSession>.toSleepSessionTemporalDataSet(): TemporalDataSet {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { sleepSession ->
            val totalSleepHours =
                Duration.between(sleepSession.startTime, sleepSession.endTime).toMinutes() / 60.0
            mapOf("sleepHours" to totalSleepHours)
        }
    )

    val sortedTimes = aggregatedData.keys.sorted()
    return TemporalDataSet(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("sleepHours") ?: 0.0 },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

//
// ────────────────────────────────────────────────────────────────────────────────
// Point-in-time measurements
// ────────────────────────────────────────────────────────────────────────────────
//

/**
 * Converts point-in-time [BloodPressure] to a multi-value [TemporalDataSet].
 *
 * Properties:
 * - systolic
 * - diastolic
 *
 * Note: input ordering is preserved; callers should provide chronologically sorted data if needed.
 */
fun List<BloodPressure>.toBloodPressureTemporalDataSet(): TemporalDataSet {
    val times = this.map { it.time }
    val yMultiple = mapOf(
        "systolic" to this.map { it.systolic.toDouble() },
        "diastolic" to this.map { it.diastolic.toDouble() }
    )

    return TemporalDataSet(
        x = times,
        yMultiple = yMultiple,
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Converts point-in-time [BloodGlucose] to a single-value [TemporalDataSet].
 *
 * Note: input ordering is preserved.
 */
fun List<BloodGlucose>.toBloodGlucoseTemporalDataSet(): TemporalDataSet {
    return TemporalDataSet(
        x = this.map { it.time },
        y = this.map { it.level.toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Converts point-in-time [Weight] to a single-value [TemporalDataSet] in kilograms.
 *
 * Note: input ordering is preserved.
 */
fun List<Weight>.toWeightTemporalDataSet(): TemporalDataSet {
    return TemporalDataSet(
        x = this.map { it.time },
        y = this.map { it.weight.toKilograms().toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Converts point-in-time [BodyFat] to a single-value [TemporalDataSet] (percentage).
 *
 * Note: input ordering is preserved.
 */
fun List<BodyFat>.toBodyFatTemporalDataSet(): TemporalDataSet {
    return TemporalDataSet(
        x = this.map { it.time },
        y = this.map { it.bodyFatPercentage.toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Converts point-in-time [SkeletalMuscleMass] to a single-value [TemporalDataSet].
 *
 * Note: input ordering is preserved.
 */
fun List<SkeletalMuscleMass>.toSkeletalMuscleMassTemporalDataSet(): TemporalDataSet {
    return TemporalDataSet(
        x = this.map { it.time },
        y = this.map { it.skeletalMuscleMass.toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

//
// ────────────────────────────────────────────────────────────────────────────────
// Overload helpers (avoid JVM signature clashes)
// ────────────────────────────────────────────────────────────────────────────────
//

@JvmName("stepCountToTemporalDataSet")
fun List<StepCount>.toTemporalDataSet(): TemporalDataSet = this.toStepCountTemporalDataSet()

@JvmName("exerciseToTemporalDataSet")
fun List<Exercise>.toTemporalDataSet(): TemporalDataSet = this.toExerciseTemporalDataSet()

@JvmName("dietToTemporalDataSet")
fun List<Diet>.toTemporalDataSet(): TemporalDataSet = this.toDietTemporalDataSet()

@JvmName("heartRateToTemporalDataSet")
fun List<HeartRate>.toTemporalDataSet(): TemporalDataSet = this.toHeartRateTemporalDataSet()

@JvmName("sleepSessionToTemporalDataSet")
fun List<SleepSession>.toTemporalDataSet(): TemporalDataSet = this.toSleepSessionTemporalDataSet()

@JvmName("bloodPressureToTemporalDataSet")
fun List<BloodPressure>.toTemporalDataSet(): TemporalDataSet = this.toBloodPressureTemporalDataSet()

@JvmName("bloodGlucoseToTemporalDataSet")
fun List<BloodGlucose>.toTemporalDataSet(): TemporalDataSet = this.toBloodGlucoseTemporalDataSet()

@JvmName("weightToTemporalDataSet")
fun List<Weight>.toTemporalDataSet(): TemporalDataSet = this.toWeightTemporalDataSet()

@JvmName("bodyFatToTemporalDataSet")
fun List<BodyFat>.toTemporalDataSet(): TemporalDataSet = this.toBodyFatTemporalDataSet()

@JvmName("skeletalMuscleMassToTemporalDataSet")
fun List<SkeletalMuscleMass>.toTemporalDataSet(): TemporalDataSet = this.toSkeletalMuscleMassTemporalDataSet()

/**
 * Aggregates interval-based “activity” data into per-minute buckets.
 *
 * Each activity has a [startTime, endTime] interval. For each minute bucket that overlaps the interval,
 * a proportional fraction of each extracted value is added to that bucket.
 *
 * Bucket convention:
 * - Buckets are keyed by instants truncated to the start of the minute (seconds/nanos = 0).
 * - The loop uses [startMinute, endMinute) semantics (end is exclusive).
 *
 * @param T Activity type.
 * @param activities Input activities.
 * @param getStartTime Function to read the interval start time.
 * @param getEndTime Function to read the interval end time.
 * @param extractValues Function mapping an activity → (propertyName → value).
 * @return Map of minuteStartInstant → (propertyName → aggregatedValue).
 */
private fun <T> aggregateActivityDataTime(
    activities: List<T>,
    getStartTime: (T) -> Instant,
    getEndTime: (T) -> Instant,
    extractValues: (T) -> Map<String, Double>
): Map<Instant, Map<String, Double>> {

    val zone = ZoneId.systemDefault()
    fun Instant.truncateToMinute(): Instant =
        this.atZone(zone).withSecond(0).withNano(0).toInstant()

    val minuteValues = mutableMapOf<Instant, MutableMap<String, Double>>()

    activities.forEach { activity ->
        val startTime = getStartTime(activity)
        val endTime = getEndTime(activity)

        val totalDurationMs = Duration.between(startTime, endTime).toMillis()
        val activityValues = extractValues(activity)

        val startMinute = startTime.truncateToMinute()
        val endMinute = endTime.truncateToMinute()

        if (startMinute == endMinute) {
            // Activity occurs within the same minute bucket
            val minuteMap = minuteValues.getOrPut(startMinute) { mutableMapOf() }
            activityValues.forEach { (property, value) ->
                minuteMap[property] = minuteMap.getOrDefault(property, 0.0) + value
            }
        } else {
            // Activity spans multiple minute buckets; distribute proportionally by overlap duration
            var currentMinute = startMinute
            while (currentMinute.isBefore(endMinute)) {
                val minuteStart = currentMinute
                val minuteEnd = currentMinute.atZone(zone).plusMinutes(1).toInstant()

                val actualStart = maxOf(startTime, minuteStart)
                val actualEnd = minOf(endTime, minuteEnd)
                val minuteDurationMs = Duration.between(actualStart, actualEnd).toMillis()

                val proportion = if (totalDurationMs > 0) {
                    minuteDurationMs.toDouble() / totalDurationMs
                } else {
                    0.0
                }

                if (proportion > 0) {
                    val minuteMap = minuteValues.getOrPut(currentMinute) { mutableMapOf() }
                    activityValues.forEach { (property, value) ->
                        val proportionalValue = value * proportion
                        minuteMap[property] = minuteMap.getOrDefault(property, 0.0) + proportionalValue
                    }
                }

                currentMinute = currentMinute.atZone(zone).plusMinutes(1).toInstant()
            }
        }
    }

    return minuteValues
}