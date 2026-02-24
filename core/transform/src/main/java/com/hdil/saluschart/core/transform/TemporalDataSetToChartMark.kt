package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.util.TimeUnitGroup
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Converts a single-value [TemporalDataSet] into a list of [ChartMark].
 *
 * Pipeline:
 * HealthData -> TemporalDataSet -> (transform/aggregate) -> ChartMark
 *
 * ⚠️ About gap filling:
 * - If [fillGaps] is true, missing buckets inside the time range are filled with [fillValue].
 * - For sparse measurement data (BP, glucose, weight), filling with 0.0 can distort charts
 *   and min/max statistics. Prefer fillGaps=false or fillValue=Double.NaN.
 */
fun TemporalDataSet.toChartMarks(
    fillGaps: Boolean = true,
    fillValue: Double = 0.0
): List<ChartMark> {
    require(isSingleValue) { "Use toChartMarksByProperty() for multi-value TemporalDataSet" }

    val dataToConvert = if (fillGaps) this.fillTemporalGaps(fillValue) else this
    val labels = dataToConvert.generateTimeLabels()

    return dataToConvert.x.indices.map { index ->
        ChartMark(
            x = index.toDouble(),
            y = dataToConvert.y!![index],
            label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
        )
    }
}

/**
 * Converts a multi-value [TemporalDataSet] into [ChartMark] list for a single [property].
 *
 * @param fillGaps If true, fills missing buckets with [fillValue].
 * @param fillValue Value used for missing buckets (default: 0.0).
 */
fun TemporalDataSet.toChartMarksByProperty(
    property: String,
    fillGaps: Boolean = true,
    fillValue: Double = 0.0
): List<ChartMark> {
    require(isMultiValue) { "Use toChartMarks() for single-value TemporalDataSet" }
    require(propertyNames.contains(property)) {
        "Property '$property' not found. Available properties: ${propertyNames.joinToString()}"
    }

    val dataToConvert = if (fillGaps) this.fillTemporalGaps(fillValue) else this
    val values = dataToConvert.getValues(property)!!
    val labels = dataToConvert.generateTimeLabels()

    return dataToConvert.x.indices.map { index ->
        ChartMark(
            x = index.toDouble(),
            y = values[index],
            label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
        )
    }
}

/**
 * Converts a multi-value [TemporalDataSet] into a map of property -> [ChartMark] list.
 *
 * @param fillGaps If true, fills missing buckets with [fillValue].
 * @param fillValue Value used for missing buckets (default: 0.0).
 */
fun TemporalDataSet.toChartMarksMap(
    fillGaps: Boolean = true,
    fillValue: Double = 0.0
): Map<String, List<ChartMark>> {
    require(isMultiValue) { "Use toChartMarks() for single-value TemporalDataSet" }

    val dataToConvert = if (fillGaps) this.fillTemporalGaps(fillValue) else this
    val labels = dataToConvert.generateTimeLabels()

    return dataToConvert.propertyNames.associateWith { property ->
        val values = dataToConvert.getValues(property)!!
        dataToConvert.x.indices.map { index ->
            ChartMark(
                x = index.toDouble(),
                y = values[index],
                label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
            )
        }
    }
}

/**
 * Converts a MIN_MAX aggregated [TemporalDataSet] (with "min" and "max" properties)
 * into a list of [RangeChartMark].
 *
 * NOTE:
 * - Default [fillGaps] is false because filling missing buckets with 0 can be misleading for vitals.
 * - If you enable fillGaps, missing buckets are filled with [fillValue] for both min/max.
 */
fun TemporalDataSet.toRangeChartMarksFromMinMax(
    fillGaps: Boolean = false,
    fillValue: Double = 0.0
): List<RangeChartMark> {
    require(isMultiValue) {
        "toRangeChartMarksFromMinMax() requires multi-value TemporalDataSet from MIN_MAX aggregation"
    }
    require(propertyNames.contains("min") && propertyNames.contains("max")) {
        "TemporalDataSet must contain 'min' and 'max' properties. " +
                "Available properties: ${propertyNames.joinToString()}. " +
                "Use transform(aggregationType = AggregationType.MIN_MAX) to create min/max data."
    }

    val dataToConvert = if (fillGaps) this.fillTemporalGaps(fillValue) else this

    val minValues = dataToConvert.getValues("min")!!
    val maxValues = dataToConvert.getValues("max")!!
    val labels = dataToConvert.generateTimeLabels()

    return dataToConvert.x.indices.map { index ->
        val label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
        RangeChartMark(
            x = index.toDouble(),
            minPoint = ChartMark(x = index.toDouble(), y = minValues[index], label = label),
            maxPoint = ChartMark(x = index.toDouble(), y = maxValues[index], label = label),
            label = label
        )
    }
}

/**
 * Generates time labels based on the dataset's [timeUnit].
 *
 * Examples:
 * - MINUTE: "14:05"
 * - HOUR: "14시"
 * - DAY: "5/8 월"
 * - WEEK: "5월 1주차" (Sunday-based week-of-month)
 * - MONTH: "2025년 5월"
 * - YEAR: "2025년"
 */
fun TemporalDataSet.generateTimeLabels(): List<String> {
    val zone = ZoneId.systemDefault()

    return when (timeUnit) {
        TimeUnitGroup.MINUTE -> x.map { instant ->
            val dt = LocalDateTime.ofInstant(instant, zone)
            "${dt.hour}:${dt.minute.toString().padStart(2, '0')}"
        }

        TimeUnitGroup.HOUR -> x.map { instant ->
            val dt = LocalDateTime.ofInstant(instant, zone)
            "${dt.hour}시"
        }

        TimeUnitGroup.DAY -> x.map { instant ->
            val dt = LocalDateTime.ofInstant(instant, zone)
            val dow = when (dt.dayOfWeek.value) {
                1 -> "월"; 2 -> "화"; 3 -> "수"; 4 -> "목"; 5 -> "금"; 6 -> "토"; 7 -> "일"
                else -> "?"
            }
            "${dt.monthValue}/${dt.dayOfMonth} $dow"
        }

        TimeUnitGroup.WEEK -> x.map { instant ->
            val dt = LocalDateTime.ofInstant(instant, zone)
            val sunday = dt.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

            val firstSundayOfMonth = LocalDate.of(sunday.year, sunday.month, 1).let { firstDay ->
                val dayOfWeek = firstDay.dayOfWeek.value
                if (dayOfWeek == 7) firstDay else firstDay.plusDays((7 - dayOfWeek).toLong())
            }

            val rawWeek = ((sunday.toEpochDay() - firstSundayOfMonth.toEpochDay()) / 7 + 1).toInt()
            val weekNumber = rawWeek.coerceAtLeast(1) // clamp safety
            "${sunday.monthValue}월 ${weekNumber}주차"
        }

        TimeUnitGroup.MONTH -> x.map { instant ->
            val dt = LocalDateTime.ofInstant(instant, zone)
            "${dt.year}년 ${dt.monthValue}월"
        }

        TimeUnitGroup.YEAR -> x.map { instant ->
            val dt = LocalDateTime.ofInstant(instant, zone)
            "${dt.year}년"
        }
    }
}