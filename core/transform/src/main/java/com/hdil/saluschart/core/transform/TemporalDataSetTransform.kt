package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.chart.BaseChartMark
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.*

/**
 * TemporalDataSet time transforms + HealthData -> ChartMark convenience APIs.
 *
 * These helpers implement:
 * HealthData -> TemporalDataSet -> transform(aggregate) -> ChartMarks
 *
 * ⚠️ Gap filling:
 * Many healthcare measurements are sparse (weight/BP/glucose). Filling missing buckets with 0.0
 * can distort charts and min/max calculations. Prefer fillGaps=false or fillValue=Double.NaN.
 */
internal fun TemporalDataSet.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): TemporalDataSet {
    return DataTransformer().transform(
        data = this,
        transformTimeUnit = timeUnit,
        aggregationType = aggregationType
    )
}

/* ─────────────────────────────────────────────────────────────────────────────
 * Activity-like data (dense / interval-based): default fillGaps=true
 * ───────────────────────────────────────────────────────────────────────────── */

/** StepCount -> ChartMarks */
@JvmName("stepCountTransform")
fun List<StepCount>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM,
    fillGaps: Boolean = true,
    fillValue: Double = 0.0
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps = fillGaps, fillValue = fillValue)
}

/** Exercise -> ChartMarks */
@JvmName("exerciseTransform")
fun List<Exercise>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM,
    fillGaps: Boolean = true,
    fillValue: Double = 0.0
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps = fillGaps, fillValue = fillValue)
}

/**
 * Diet -> ChartMarks (all properties).
 * Label includes property name, e.g. "5/8 월 (protein)".
 */
@JvmName("dietTransform")
fun List<Diet>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM,
    fillGaps: Boolean = true,
    fillValue: Double = 0.0
): List<ChartMark> {
    val chartMarksMap = this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarksMap(fillGaps = fillGaps, fillValue = fillValue)

    return chartMarksMap.flatMap { (property, marks) ->
        marks.map { mark ->
            mark.copy(label = "${mark.label} ($property)")
        }
    }
}

/** Diet -> ChartMarks for one property */
@JvmName("dietTransformByProperty")
fun List<Diet>.transformByProperty(
    property: String,
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM,
    fillGaps: Boolean = true,
    fillValue: Double = 0.0
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarksByProperty(property, fillGaps = fillGaps, fillValue = fillValue)
}

/** SleepSession -> ChartMarks */
@JvmName("sleepSessionTransform")
fun List<SleepSession>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM,
    fillGaps: Boolean = true,
    fillValue: Double = 0.0
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps = fillGaps, fillValue = fillValue)
}

/* ─────────────────────────────────────────────────────────────────────────────
 * Measurement-like data (often sparse): recommend default fillGaps=false
 * ───────────────────────────────────────────────────────────────────────────── */

/**
 * BloodPressure -> per-property ChartMark map.
 * Default aggregation: DAILY_AVERAGE.
 *
 * NOTE: BP is usually sparse; consider fillGaps=false (default below).
 */
@JvmName("bloodPressureTransform")
fun List<BloodPressure>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): Map<String, List<ChartMark>> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarksMap(fillGaps = fillGaps, fillValue = fillValue)
}

/** BloodPressure -> one property */
@JvmName("bloodPressureTransformByProperty")
fun List<BloodPressure>.transformByProperty(
    property: String,
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarksByProperty(property, fillGaps = fillGaps, fillValue = fillValue)
}

/**
 * BloodGlucose -> ChartMarks or RangeChartMarks (MIN_MAX).
 *
 * IMPORTANT:
 * - MIN_MAX should usually not fill gaps with 0-range points. Default fillGaps=false below.
 */
@JvmName("bloodGlucoseTransform")
fun List<BloodGlucose>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.MIN_MAX,
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): List<BaseChartMark> {
    val transformed = this.toTemporalDataSet().transform(timeUnit, aggregationType)

    return if (aggregationType == AggregationType.MIN_MAX) {
        transformed.toRangeChartMarksFromMinMax(fillGaps = fillGaps, fillValue = fillValue)
    } else {
        transformed.toChartMarks(fillGaps = fillGaps, fillValue = fillValue)
    }
}

/**
 * HeartRate -> ChartMarks (single-value aggregations).
 * MIN_MAX not allowed here.
 */
@JvmName("heartRateTransformToChartMark")
fun List<HeartRate>.transformToChartMark(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): List<ChartMark> {
    require(aggregationType != AggregationType.MIN_MAX) {
        "MIN_MAX aggregation returns RangeChartMark. Use transformToRangeChartMark() instead."
    }
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps = fillGaps, fillValue = fillValue)
}

/** HeartRate -> RangeChartMarks (MIN_MAX) */
@JvmName("heartRateTransformToRangeChartMark")
fun List<HeartRate>.transformToRangeChartMark(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): List<RangeChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, AggregationType.MIN_MAX)
        .toRangeChartMarksFromMinMax(fillGaps = fillGaps, fillValue = fillValue)
}

/**
 * HeartRate -> BaseChartMark (either ChartMarks or RangeChartMarks).
 * Default aggregation: MIN_MAX.
 */
@JvmName("heartRateTransform")
fun List<HeartRate>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.MIN_MAX,
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): List<BaseChartMark> {
    val transformed = this.toTemporalDataSet().transform(timeUnit, aggregationType)
    return if (aggregationType == AggregationType.MIN_MAX) {
        transformed.toRangeChartMarksFromMinMax(fillGaps = fillGaps, fillValue = fillValue)
    } else {
        transformed.toChartMarks(fillGaps = fillGaps, fillValue = fillValue)
    }
}

/**
 * Weight -> ChartMarks.
 * Default: DAILY_AVERAGE.
 *
 * NOTE: sparse dataset; default fillGaps=false.
 */
@JvmName("weightTransform")
fun List<Weight>.transform(
    massUnit: MassUnit = MassUnit.KILOGRAM,
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): List<ChartMark> {
    val baseMarks = this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps = fillGaps, fillValue = fillValue)

    return baseMarks.map { mark ->
        val convertedValue = when (massUnit) {
            MassUnit.KILOGRAM -> mark.y
            MassUnit.POUND -> mark.y * 2.20462
            MassUnit.GRAM -> mark.y * 1000.0
            MassUnit.OUNCE -> mark.y * 35.274
        }
        mark.copy(y = convertedValue)
    }
}

/** BodyFat -> ChartMarks (sparse; default fillGaps=false) */
@JvmName("bodyFatTransform")
fun List<BodyFat>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps = fillGaps, fillValue = fillValue)
}

/** SkeletalMuscleMass -> ChartMarks (sparse; default fillGaps=false) */
@JvmName("skeletalMuscleMassTransform")
fun List<SkeletalMuscleMass>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps = fillGaps, fillValue = fillValue)
}