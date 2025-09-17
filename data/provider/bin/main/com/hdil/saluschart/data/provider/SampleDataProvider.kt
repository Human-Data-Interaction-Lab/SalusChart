package com.hdil.saluschart.data.provider

import com.hdil.saluschart.core.chart.BaseChartPoint
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ProgressChartPoint
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.StackedChartPoint
import com.hdil.saluschart.data.model.model.Mass
import com.hdil.saluschart.data.model.model.StepCount
import com.hdil.saluschart.data.model.model.Weight
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

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
    fun getStepCountData(): List<StepCount> = listOf(
        StepCount(Instant.parse("2025-05-04T08:00:00Z"), Instant.parse("2025-05-04T08:30:00Z"), 2431),
        StepCount(Instant.parse("2025-05-04T09:00:00Z"), Instant.parse("2025-05-04T09:30:00Z"), 1359),
        StepCount(Instant.parse("2025-05-04T10:00:00Z"), Instant.parse("2025-05-04T10:30:00Z"), 6149),
        StepCount(Instant.parse("2025-05-04T11:00:00Z"), Instant.parse("2025-05-04T11:30:00Z"), 4246),
        StepCount(Instant.parse("2025-05-04T12:00:00Z"), Instant.parse("2025-05-04T12:30:00Z"), 2855),
        StepCount(Instant.parse("2025-05-04T13:00:00Z"), Instant.parse("2025-05-04T13:30:00Z"), 9831),
        StepCount(Instant.parse("2025-05-04T14:00:00Z"), Instant.parse("2025-05-04T14:30:00Z"), 1498),
        StepCount(Instant.parse("2025-05-04T15:00:00Z"), Instant.parse("2025-05-04T15:30:00Z"), 8455),
        StepCount(Instant.parse("2025-05-04T16:00:00Z"), Instant.parse("2025-05-04T16:30:00Z"), 4662),
        StepCount(Instant.parse("2025-05-04T17:00:00Z"), Instant.parse("2025-05-04T17:30:00Z"), 1329),
        StepCount(Instant.parse("2025-05-04T18:00:00Z"), Instant.parse("2025-05-04T18:30:00Z"), 2327),
        StepCount(Instant.parse("2025-05-04T19:00:00Z"), Instant.parse("2025-05-04T19:30:00Z"), 7369),
        StepCount(Instant.parse("2025-05-04T20:00:00Z"), Instant.parse("2025-05-04T20:30:00Z"), 1649),
        StepCount(Instant.parse("2025-05-04T21:00:00Z"), Instant.parse("2025-05-04T21:30:00Z"), 8768),
        StepCount(Instant.parse("2025-05-04T22:00:00Z"), Instant.parse("2025-05-04T22:30:00Z"), 5235),
        StepCount(Instant.parse("2025-05-04T23:00:00Z"), Instant.parse("2025-05-04T23:30:00Z"), 8286),
        StepCount(Instant.parse("2025-05-05T00:00:00Z"), Instant.parse("2025-05-05T00:30:00Z"), 9606),
        StepCount(Instant.parse("2025-05-05T01:00:00Z"), Instant.parse("2025-05-05T01:30:00Z"), 8043),
        StepCount(Instant.parse("2025-05-05T02:00:00Z"), Instant.parse("2025-05-05T02:30:00Z"), 6046),
        StepCount(Instant.parse("2025-05-05T03:00:00Z"), Instant.parse("2025-05-05T03:30:00Z"), 4874),
        StepCount(Instant.parse("2025-05-05T04:00:00Z"), Instant.parse("2025-05-05T04:30:00Z"), 5893),
        StepCount(Instant.parse("2025-05-05T05:00:00Z"), Instant.parse("2025-05-05T05:30:00Z"), 5060),
        StepCount(Instant.parse("2025-05-05T06:00:00Z"), Instant.parse("2025-05-05T06:30:00Z"), 4520),
        StepCount(Instant.parse("2025-05-05T07:00:00Z"), Instant.parse("2025-05-05T07:30:00Z"), 6420),
        StepCount(Instant.parse("2025-05-05T08:00:00Z"), Instant.parse("2025-05-05T08:30:00Z"), 1837),
        StepCount(Instant.parse("2025-05-05T09:00:00Z"), Instant.parse("2025-05-05T09:30:00Z"), 5893),
        StepCount(Instant.parse("2025-05-05T10:00:00Z"), Instant.parse("2025-05-05T10:30:00Z"), 4000),
        StepCount(Instant.parse("2025-05-05T11:00:00Z"), Instant.parse("2025-05-05T11:30:00Z"), 5760),
        StepCount(Instant.parse("2025-05-05T12:00:00Z"), Instant.parse("2025-05-05T12:30:00Z"), 3621),
        StepCount(Instant.parse("2025-05-05T13:00:00Z"), Instant.parse("2025-05-05T13:30:00Z"), 7387)
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
        ProgressChartPoint(x = 0f, current = 1200f, max = 2000f, label = "Move", unit = "KJ"),
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
}