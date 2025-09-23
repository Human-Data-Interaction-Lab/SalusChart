package com.hdil.saluschart.data.provider

import com.hdil.saluschart.core.chart.BaseChartPoint
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ProgressChartPoint
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.StackedChartPoint
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.data.model.model.BloodPressure
import com.hdil.saluschart.data.model.model.BodyFat
import com.hdil.saluschart.data.model.model.Mass
import com.hdil.saluschart.data.model.model.StepCount
import com.hdil.saluschart.data.model.model.Weight
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.*
import kotlin.random.Random
import kotlin.math.roundToInt


/**
 * Provides sample data for demonstration and testing purposes.
 * All sample data is organized here for better maintainability.
 */
object SampleDataProvider {
    
    // Basic sample data arrays
    val sampleData = listOf(10f, 25f, 40f, 20f, 35f, 55f, 45f)
    val sampleData2 = listOf(5f, 15f, 60f, 45f, 35f, 25f, 10f)
    val sampleData3 = listOf(8f, 22f, 10f, 40f, 18f, 32f, 12f)
    val sampleData4 = listOf(10f, 25f, 40f, 20f, 35f, 55f, 45f, 5f, 15f, 60f, 45f, 35f, 25f, 10f, 8f, 22f, 10f, 40f, 18f, 32f, 12f)
    
    // Week days labels
    val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")
    
    // Nutrition segment labels for stacked charts
    val segmentLabels = listOf("단백질", "지방", "탄수화물")

    /**
     * Sample step count health data spanning two days with 30-minute intervals
     */
//    fun getStepCountData(): List<StepCount> = listOf(
//        StepCount(Instant.parse("2025-05-04T08:00:00Z"), Instant.parse("2025-05-04T08:30:00Z"), 43),
//        StepCount(Instant.parse("2025-05-04T09:00:00Z"), Instant.parse("2025-05-04T09:30:00Z"), 139),
//        StepCount(Instant.parse("2025-05-04T10:00:00Z"), Instant.parse("2025-05-04T10:30:00Z"), 649),
//        StepCount(Instant.parse("2025-05-04T11:00:00Z"), Instant.parse("2025-05-04T11:30:00Z"), 426),
//        StepCount(Instant.parse("2025-05-04T12:00:00Z"), Instant.parse("2025-05-04T12:30:00Z"), 285),
//        StepCount(Instant.parse("2025-05-04T13:00:00Z"), Instant.parse("2025-05-04T13:30:00Z"), 981),
//        StepCount(Instant.parse("2025-05-04T14:00:00Z"), Instant.parse("2025-05-04T14:30:00Z"), 148),
//        StepCount(Instant.parse("2025-05-04T15:00:00Z"), Instant.parse("2025-05-04T15:30:00Z"), 845),
//        StepCount(Instant.parse("2025-05-04T16:00:00Z"), Instant.parse("2025-05-04T16:30:00Z"), 462),
//        StepCount(Instant.parse("2025-05-04T17:00:00Z"), Instant.parse("2025-05-04T17:30:00Z"), 139),
//        StepCount(Instant.parse("2025-05-04T18:00:00Z"), Instant.parse("2025-05-04T18:30:00Z"), 237),
//        StepCount(Instant.parse("2025-05-04T19:00:00Z"), Instant.parse("2025-05-04T19:30:00Z"), 739),
//        StepCount(Instant.parse("2025-05-04T20:00:00Z"), Instant.parse("2025-05-04T20:30:00Z"), 169),
//        StepCount(Instant.parse("2025-05-04T21:00:00Z"), Instant.parse("2025-05-04T21:30:00Z"), 878),
//        StepCount(Instant.parse("2025-05-04T22:00:00Z"), Instant.parse("2025-05-04T22:30:00Z"), 525),
//        StepCount(Instant.parse("2025-05-04T23:00:00Z"), Instant.parse("2025-05-04T23:30:00Z"), 826),
//        StepCount(Instant.parse("2025-05-05T00:00:00Z"), Instant.parse("2025-05-05T00:30:00Z"), 96),
//        StepCount(Instant.parse("2025-05-05T01:00:00Z"), Instant.parse("2025-05-05T01:30:00Z"), 80),
//        StepCount(Instant.parse("2025-05-05T02:00:00Z"), Instant.parse("2025-05-05T02:30:00Z"), 0),
//        StepCount(Instant.parse("2025-05-05T03:00:00Z"), Instant.parse("2025-05-05T03:30:00Z"), 0),
//        StepCount(Instant.parse("2025-05-05T04:00:00Z"), Instant.parse("2025-05-05T04:30:00Z"), 0),
//        StepCount(Instant.parse("2025-05-05T05:00:00Z"), Instant.parse("2025-05-05T05:30:00Z"), 0),
//        StepCount(Instant.parse("2025-05-05T06:00:00Z"), Instant.parse("2025-05-05T06:30:00Z"), 0),
//        StepCount(Instant.parse("2025-05-05T07:00:00Z"), Instant.parse("2025-05-05T07:30:00Z"), 0),
//        StepCount(Instant.parse("2025-05-05T08:00:00Z"), Instant.parse("2025-05-05T08:30:00Z"), 18),
//        StepCount(Instant.parse("2025-05-05T09:00:00Z"), Instant.parse("2025-05-05T09:30:00Z"), 289),
//        StepCount(Instant.parse("2025-05-05T10:00:00Z"), Instant.parse("2025-05-05T10:30:00Z"), 400),
//        StepCount(Instant.parse("2025-05-05T11:00:00Z"), Instant.parse("2025-05-05T11:30:00Z"), 576),
//        StepCount(Instant.parse("2025-05-05T12:00:00Z"), Instant.parse("2025-05-05T12:30:00Z"), 362),
//        StepCount(Instant.parse("2025-05-05T13:00:00Z"), Instant.parse("2025-05-05T13:30:00Z"), 738)
//    )

    /**
     * Generates StepCount data with 0 steps from 02:00–08:00 local time.
     * - intervalMinutes: typically 30
     * - zoneId: used to decide sleep window in local time (default Asia/Seoul)
     * - seed: pass a value for deterministic output; null = random
     */
    fun generateStepCounts(
        startInclusive: Instant,
        endExclusive: Instant,
        intervalMinutes: Long = 30,
        zoneId: ZoneId = ZoneId.of("Asia/Seoul"),
        seed: Long? = null
    ): List<StepCount> {
        require(!endExclusive.isBefore(startInclusive)) { "endExclusive must be >= startInclusive" }
        val rng = seed?.let { Random(it) } ?: Random(System.currentTimeMillis())

        fun randomCountForLocalTime(zdt: ZonedDateTime): Int {
            // Sleep window: 02:00–07:59 local time
            if (zdt.hour in 2..7) return 0

            val hour = zdt.hour
            // Base ranges by time-of-day (tuned to look like your samples)
            val (min, max) = when (hour) {
                in 8..10 -> 40 to 450    // ramp up morning
                in 11..13 -> 200 to 700  // mid-day move
                in 14..17 -> 300 to 850  // afternoon
                in 18..21 -> 350 to 950  // evening peak
                else -> 50 to 400        // late night & very early morning outside sleep
            }

            // Smooth random with a slight burst chance
            var v = rng.nextInt(min, max + 1)
            if (rng.nextDouble() < 0.12) v = (v * 1.3).roundToInt()  // occasional burst
            return v.coerceIn(0, 1200)
        }

        val out = ArrayList<StepCount>()
        var cursor = startInclusive
        val step = Duration.ofMinutes(intervalMinutes)

        while (cursor.isBefore(endExclusive)) {
            val zdt = cursor.atZone(zoneId)
            val count = randomCountForLocalTime(zdt).toLong()
            val next = cursor.plus(step)
            out += StepCount(cursor, next, count)
            cursor = next
        }
        return out
    }

    fun getStepCountData(): List<StepCount> = generateStepCounts(
        startInclusive = Instant.parse("2025-05-04T15:00:00Z"),
        endExclusive = Instant.parse("2025-05-11T14:00:00Z"),
        intervalMinutes = 30,
        zoneId = ZoneId.of("Asia/Seoul"),
    )

    /**
     * Sample weight data spanning 30 days
     */
    fun getWeightData(): List<Weight> = listOf(
        Weight(Instant.parse("2025-05-04T08:00:00Z"), Mass.kilograms(74.7)),
        Weight(Instant.parse("2025-05-03T08:00:00Z"), Mass.kilograms(68.6)),
        Weight(Instant.parse("2025-05-02T08:00:00Z"), Mass.kilograms(71.6)),
        Weight(Instant.parse("2025-05-01T08:00:00Z"), Mass.kilograms(74.2)),
        Weight(Instant.parse("2025-04-30T08:00:00Z"), Mass.kilograms(60.6)),
        Weight(Instant.parse("2025-04-29T08:00:00Z"), Mass.kilograms(73.8)),
        Weight(Instant.parse("2025-04-28T08:00:00Z"), Mass.kilograms(65.8)),
        Weight(Instant.parse("2025-04-27T08:00:00Z"), Mass.kilograms(73.2)),
        Weight(Instant.parse("2025-04-26T08:00:00Z"), Mass.kilograms(73.8)),
        Weight(Instant.parse("2025-04-25T08:00:00Z"), Mass.kilograms(67.5)),
        Weight(Instant.parse("2025-04-24T08:00:00Z"), Mass.kilograms(70.4)),
        Weight(Instant.parse("2025-04-23T08:00:00Z"), Mass.kilograms(61.4)),
        Weight(Instant.parse("2025-04-22T08:00:00Z"), Mass.kilograms(74.8)),
        Weight(Instant.parse("2025-04-21T08:00:00Z"), Mass.kilograms(66.1)),
        Weight(Instant.parse("2025-04-20T08:00:00Z"), Mass.kilograms(63.9)),
        Weight(Instant.parse("2025-04-19T08:00:00Z"), Mass.kilograms(67.9)),
        Weight(Instant.parse("2025-04-18T08:00:00Z"), Mass.kilograms(67.3)),
        Weight(Instant.parse("2025-04-17T08:00:00Z"), Mass.kilograms(66.8)),
        Weight(Instant.parse("2025-04-16T08:00:00Z"), Mass.kilograms(67.9)),
        Weight(Instant.parse("2025-04-15T08:00:00Z"), Mass.kilograms(66.4)),
        Weight(Instant.parse("2025-04-14T08:00:00Z"), Mass.kilograms(74.6)),
        Weight(Instant.parse("2025-04-13T08:00:00Z"), Mass.kilograms(66.1)),
        Weight(Instant.parse("2025-04-12T08:00:00Z"), Mass.kilograms(71.5)),
        Weight(Instant.parse("2025-04-11T08:00:00Z"), Mass.kilograms(72.9)),
        Weight(Instant.parse("2025-04-10T08:00:00Z"), Mass.kilograms(70.8)),
        Weight(Instant.parse("2025-04-09T08:00:00Z"), Mass.kilograms(69.9)),
        Weight(Instant.parse("2025-04-08T08:00:00Z"), Mass.kilograms(60.9)),
        Weight(Instant.parse("2025-04-07T08:00:00Z"), Mass.kilograms(66.9)),
        Weight(Instant.parse("2025-04-06T08:00:00Z"), Mass.kilograms(69.3)),
        Weight(Instant.parse("2025-04-05T08:00:00Z"), Mass.kilograms(61.1))
    )

    fun getBodyFatData(): List<BodyFat> = listOf(
        BodyFat(Instant.parse("2025-05-04T08:00:00Z"), 22.1),
        BodyFat(Instant.parse("2025-05-03T08:00:00Z"), 24.2),
        BodyFat(Instant.parse("2025-05-02T08:00:00Z"), 20.7),
        BodyFat(Instant.parse("2025-05-01T08:00:00Z"), 18.3),
        BodyFat(Instant.parse("2025-04-30T08:00:00Z"), 20.9),
        BodyFat(Instant.parse("2025-04-29T08:00:00Z"), 15.3),
        BodyFat(Instant.parse("2025-04-28T08:00:00Z"), 18.0),
        BodyFat(Instant.parse("2025-04-27T08:00:00Z"), 19.4),
        BodyFat(Instant.parse("2025-04-26T08:00:00Z"), 18.3),
        BodyFat(Instant.parse("2025-04-25T08:00:00Z"), 19.8),
        BodyFat(Instant.parse("2025-04-24T08:00:00Z"), 21.7),
        BodyFat(Instant.parse("2025-04-23T08:00:00Z"), 22.1),
        BodyFat(Instant.parse("2025-04-22T08:00:00Z"), 22.4),
        BodyFat(Instant.parse("2025-04-21T08:00:00Z"), 21.6),
        BodyFat(Instant.parse("2025-04-20T08:00:00Z"), 19.2),
        BodyFat(Instant.parse("2025-04-19T08:00:00Z"), 20.0),
        BodyFat(Instant.parse("2025-04-18T08:00:00Z"), 17.9),
        BodyFat(Instant.parse("2025-04-17T08:00:00Z"), 16.0),
        BodyFat(Instant.parse("2025-04-16T08:00:00Z"), 15.7),
        BodyFat(Instant.parse("2025-04-15T08:00:00Z"), 14.4),
        BodyFat(Instant.parse("2025-04-14T08:00:00Z"), 18.0),
        BodyFat(Instant.parse("2025-04-13T08:00:00Z"), 16.7),
        BodyFat(Instant.parse("2025-04-12T08:00:00Z"), 17.5),
        BodyFat(Instant.parse("2025-04-11T08:00:00Z"), 12.0),
        BodyFat(Instant.parse("2025-04-10T08:00:00Z"), 14.7),
        BodyFat(Instant.parse("2025-04-09T08:00:00Z"), 11.8),
        BodyFat(Instant.parse("2025-04-08T08:00:00Z"), 13.7),
        BodyFat(Instant.parse("2025-04-07T08:00:00Z"), 13.1),
        BodyFat(Instant.parse("2025-04-06T08:00:00Z"), 13.2),
        BodyFat(Instant.parse("2025-04-05T08:00:00Z"), 12.9)
    )

    fun getBloodPressureData(): List<BloodPressure> = listOf(
        BloodPressure(Instant.parse("2025-05-04T08:00:00Z"), 119.5, 73.3),
        BloodPressure(Instant.parse("2025-05-03T08:00:00Z"), 123.4, 82.6),
        BloodPressure(Instant.parse("2025-05-02T08:00:00Z"), 115.2, 84.9),
        BloodPressure(Instant.parse("2025-05-01T08:00:00Z"), 128.2, 81.2),
        BloodPressure(Instant.parse("2025-04-30T08:00:00Z"), 129.3, 71.8),
        BloodPressure(Instant.parse("2025-04-29T08:00:00Z"), 118.5, 79.6),
        BloodPressure(Instant.parse("2025-04-28T08:00:00Z"), 120.4, 71.5),
        BloodPressure(Instant.parse("2025-04-27T08:00:00Z"), 113.1, 80.9),
        BloodPressure(Instant.parse("2025-04-26T08:00:00Z"), 112.8, 82.1),
        BloodPressure(Instant.parse("2025-04-25T08:00:00Z"), 114.9, 79.3),
        BloodPressure(Instant.parse("2025-04-24T08:00:00Z"), 124.3, 70.1),
        BloodPressure(Instant.parse("2025-04-23T08:00:00Z"), 123.1, 82.6),
        BloodPressure(Instant.parse("2025-04-22T08:00:00Z"), 123.4, 82.2),
        BloodPressure(Instant.parse("2025-04-21T08:00:00Z"), 122.0, 80.9),
        BloodPressure(Instant.parse("2025-04-20T08:00:00Z"), 118.8, 80.4),
        BloodPressure(Instant.parse("2025-04-19T08:00:00Z"), 123.2, 76.2),
        BloodPressure(Instant.parse("2025-04-18T08:00:00Z"), 113.0, 84.4),
        BloodPressure(Instant.parse("2025-04-17T08:00:00Z"), 115.5, 82.6),
        BloodPressure(Instant.parse("2025-04-16T08:00:00Z"), 123.9, 83.8),
        BloodPressure(Instant.parse("2025-04-15T08:00:00Z"), 111.4, 79.8),
        BloodPressure(Instant.parse("2025-04-14T08:00:00Z"), 119.5, 84.2),
        BloodPressure(Instant.parse("2025-04-13T08:00:00Z"), 114.1, 71.0),
        BloodPressure(Instant.parse("2025-04-12T08:00:00Z"), 116.1, 81.9),
        BloodPressure(Instant.parse("2025-04-11T08:00:00Z"), 121.9, 76.5),
        BloodPressure(Instant.parse("2025-04-10T08:00:00Z"), 123.9, 73.0),
        BloodPressure(Instant.parse("2025-04-09T08:00:00Z"), 119.1, 83.1),
        BloodPressure(Instant.parse("2025-04-08T08:00:00Z"), 129.2, 72.8),
        BloodPressure(Instant.parse("2025-04-07T08:00:00Z"), 123.4, 78.2),
        BloodPressure(Instant.parse("2025-04-06T08:00:00Z"), 126.2, 76.2),
        BloodPressure(Instant.parse("2025-04-05T08:00:00Z"), 111.5, 84.7)
    )

    /**
     * Sample heart rate range data for range bar charts
     */
    fun getHeartRateRangeData(): List<RangeChartPoint> = listOf(
        RangeChartPoint(x = 0f, yMin = 54f, yMax = 160f, label = "2일"),
        RangeChartPoint(x = 1f, yMin = 65f, yMax = 145f, label = "3일"),
        RangeChartPoint(x = 2f, yMin = 58f, yMax = 125f, label = "4일"),
        RangeChartPoint(x = 3f, yMin = 75f, yMax = 110f, label = "6일"),
        RangeChartPoint(x = 4f, yMin = 68f, yMax = 162f, label = "7일"),
        RangeChartPoint(x = 5f, yMin = 72f, yMax = 168f, label = "8일"),
        RangeChartPoint(x = 6f, yMin = 65f, yMax = 138f, label = "9일"),
        RangeChartPoint(x = 7f, yMin = 85f, yMax = 105f, label = "10일")
    )

    /**
     * Sample nutrition data for stacked bar charts (protein, fat, carbs)
     */
    fun getNutritionStackedData(): List<StackedChartPoint> = listOf(
        StackedChartPoint(x = 0f, values = listOf(80f, 45f, 120f), label = "월"), // 단백질, 지방, 탄수화물 (g)
        StackedChartPoint(x = 1f, values = listOf(75f, 38f, 110f), label = "화"),
        StackedChartPoint(x = 2f, values = listOf(90f, 52f, 140f), label = "수"),
        StackedChartPoint(x = 3f, values = listOf(85f, 41f, 135f), label = "목"),
        StackedChartPoint(x = 4f, values = listOf(95f, 58f, 150f), label = "금"),
        StackedChartPoint(x = 5f, values = listOf(70f, 35f, 100f), label = "토"),
        StackedChartPoint(x = 6f, values = listOf(88f, 48f, 125f), label = "일")
    )

    /**
     * Sample progress data for Apple Watch-style activity rings
     */
    fun getActivityProgressData(): List<ProgressChartPoint> = listOf(
        ProgressChartPoint(x = 0f, current = 2500f, max = 2000f, label = "Move", unit = "KJ"),
        ProgressChartPoint(x = 1f, current = 20f, max = 60f, label = "Exercise", unit = "min"),
        ProgressChartPoint(x = 2f, current = 7f, max = 10f, label = "Stand", unit = "h")
    )

    /**
     * Sample calendar entries for calendar charts
     */
    fun getCalendarEntries(yearMonth: YearMonth = YearMonth.now()): List<CalendarEntry> {
        val startDate = LocalDate.of(yearMonth.year, yearMonth.monthValue, 1)
        val endDate = LocalDate.of(yearMonth.year, yearMonth.monthValue, yearMonth.lengthOfMonth())
        val random = java.util.Random(0)
        
        return generateSequence(startDate) { date ->
            if (date.isBefore(endDate)) date.plusDays(1) else null
        }.map { date ->
            val value = random.nextFloat() * 100
            CalendarEntry(date = date, value = value)
        }.toList()
    }

    /**
     * Convert basic sample data to ChartPoint format
     */
    fun getBasicChartPoints(): List<ChartPoint> = sampleData.mapIndexed { index, value ->
        ChartPoint(x = index.toFloat(), y = value, label = weekDays.getOrElse(index) { "" })
    }

    /**
     * Convert extended sample data to ChartPoint format (for pagination demos)
     */
    fun getExtendedChartPoints(): List<ChartPoint> = sampleData4.mapIndexed { index, value ->
        ChartPoint(x = index.toFloat(), y = value, label = weekDays[index % weekDays.size])
    }

    /**
     * Generate dense chart points for tick reduction demos
     */
    fun getDenseChartPoints(count: Int = 50): List<ChartPoint> {
        val labels = (1..count).map { "Day $it" }
        val values = (1..count).map { (20..80).random().toFloat() }
        return labels.mapIndexed { index, label ->
            ChartPoint(x = index.toFloat(), y = values[index], label = label)
        }
    }

    /**
     * Get blood pressure data as separate ChartPoint lists for systolic and diastolic
     * Uses the transform convenience function for proper time-based processing
     */
    fun getBloodPressureChartPointsMap(): Map<String, List<ChartPoint>> {
        return getBloodPressureData().transform(
            timeUnit = TimeUnitGroup.DAY,
            aggregationType = AggregationType.DAILY_AVERAGE
        )
    }
}