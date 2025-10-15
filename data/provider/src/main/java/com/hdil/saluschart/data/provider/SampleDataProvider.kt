package com.hdil.saluschart.data.provider

import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.HeartRate
import com.hdil.saluschart.data.model.model.HeartRateSample
import com.hdil.saluschart.data.model.model.BloodPressure
import com.hdil.saluschart.data.model.model.BloodGlucose
import com.hdil.saluschart.data.model.model.BodyFat
import com.hdil.saluschart.data.model.model.Exercise
import com.hdil.saluschart.data.model.model.Mass
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import com.hdil.saluschart.data.model.model.SleepStageType
import com.hdil.saluschart.data.model.model.StepCount
import com.hdil.saluschart.data.model.model.Weight
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.roundToInt
import kotlin.random.Random


/**
 * Provides sample data for demonstration and testing purposes.
 * All sample data is organized here for better maintainability.
 */
object SampleDataProvider {
    
    // Basic sample data arrays
    val sampleData = listOf(10.0, 25.0, 40.0, 20.0, 35.0, 55.0, 45.0)
    val sampleData2 = listOf(5.0, 15.0, 60.0, 45.0, 35.0, 25.0, 10.0)
    val sampleData3 = listOf(8.0, 22.0, 10.0, 40.0, 18.0, 32.0, 12.0)
    val sampleData4 = listOf(10.0, 25.0, 40.0, 20.0, 35.0, 55.0, 45.0, 5.0, 15.0, 60.0, 45.0, 35.0, 25.0, 10.0, 8.0, 22.0, 10.0, 40.0, 18.0, 32.0, 12.0)
    
    // Week days labels
    val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")
    
    // Nutrition segment labels for stacked charts
    val segmentLabels = listOf("단백질", "지방", "탄수화물")

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

    fun getExerciseHealthData(): List<Exercise> = listOf(
        Exercise(Instant.parse("2025-04-19T20:04:00Z"), Instant.parse("2025-04-19T20:46:00Z"), 370.0),
        Exercise(Instant.parse("2025-04-20T08:02:00Z"), Instant.parse("2025-04-20T08:46:00Z"), 450.0),
        Exercise(Instant.parse("2025-04-20T20:00:00Z"), Instant.parse("2025-04-20T20:47:00Z"), 390.0),
        Exercise(Instant.parse("2025-04-21T07:59:00Z"), Instant.parse("2025-04-21T08:45:00Z"), 271.0),
        Exercise(Instant.parse("2025-04-21T19:58:00Z"), Instant.parse("2025-04-21T20:44:00Z"), 432.0),
        Exercise(Instant.parse("2025-04-22T08:03:00Z"), Instant.parse("2025-04-22T08:31:00Z"), 210.0),
        Exercise(Instant.parse("2025-04-22T19:56:00Z"), Instant.parse("2025-04-22T20:41:00Z"), 342.0),
        Exercise(Instant.parse("2025-04-23T08:01:00Z"), Instant.parse("2025-04-23T08:36:00Z"), 221.0),
        Exercise(Instant.parse("2025-04-23T20:02:00Z"), Instant.parse("2025-04-23T20:47:00Z"), 329.0),
        Exercise(Instant.parse("2025-04-24T07:56:00Z"), Instant.parse("2025-04-24T08:43:00Z"), 390.0),
        Exercise(Instant.parse("2025-04-24T19:59:00Z"), Instant.parse("2025-04-24T20:47:00Z"), 398.0),
        Exercise(Instant.parse("2025-04-25T07:58:00Z"), Instant.parse("2025-04-25T08:46:00Z"), 408.0),
        Exercise(Instant.parse("2025-04-25T20:01:00Z"), Instant.parse("2025-04-25T20:44:00Z"), 349.0),
        Exercise(Instant.parse("2025-04-26T07:59:00Z"), Instant.parse("2025-04-26T08:29:00Z"), 231.0),
        Exercise(Instant.parse("2025-04-26T19:57:00Z"), Instant.parse("2025-04-26T20:38:00Z"), 246.0),
        Exercise(Instant.parse("2025-04-27T07:45:00Z"), Instant.parse("2025-04-27T08:30:00Z"), 270.0),
        Exercise(Instant.parse("2025-04-27T20:05:00Z"), Instant.parse("2025-04-27T20:51:00Z"), 281.0),
        Exercise(Instant.parse("2025-04-28T08:04:00Z"), Instant.parse("2025-04-28T08:37:00Z"), 215.0),
        Exercise(Instant.parse("2025-04-29T08:02:00Z"), Instant.parse("2025-04-29T08:47:00Z"), 306.0),
        Exercise(Instant.parse("2025-04-29T20:06:00Z"), Instant.parse("2025-04-29T20:52:00Z"), 419.0),
        Exercise(Instant.parse("2025-04-30T06:58:00Z"), Instant.parse("2025-04-30T07:40:00Z"), 252.0),
        Exercise(Instant.parse("2025-04-30T19:58:00Z"), Instant.parse("2025-04-30T20:40:00Z"), 231.0),
        Exercise(Instant.parse("2025-05-01T07:49:00Z"), Instant.parse("2025-05-01T08:33:00Z"), 268.0),
        Exercise(Instant.parse("2025-05-01T20:11:00Z"), Instant.parse("2025-05-01T21:00:00Z"), 353.0),
        Exercise(Instant.parse("2025-05-02T19:55:00Z"), Instant.parse("2025-05-02T20:46:00Z"), 383.0),
        Exercise(Instant.parse("2025-05-03T20:03:00Z"), Instant.parse("2025-05-03T20:44:00Z"), 299.0),
        Exercise(Instant.parse("2025-05-04T08:10:00Z"), Instant.parse("2025-05-04T08:53:00Z"), 310.0),
        Exercise(Instant.parse("2025-05-05T07:12:00Z"), Instant.parse("2025-05-05T07:56:00Z"), 304.0),
        Exercise(Instant.parse("2025-05-05T17:59:00Z"), Instant.parse("2025-05-05T19:03:00Z"), 582.0),
        Exercise(Instant.parse("2025-05-06T08:21:00Z"), Instant.parse("2025-05-06T09:12:00Z"), 362.0),
        // 2025-05-07 (0) — rest day
        Exercise(Instant.parse("2025-05-08T06:45:00Z"), Instant.parse("2025-05-08T07:22:00Z"), 222.0), Exercise(Instant.parse("2025-05-09T19:07:00Z"), Instant.parse("2025-05-09T19:50:00Z"), 353.0),
        Exercise(Instant.parse("2025-05-10T09:02:00Z"), Instant.parse("2025-05-10T09:34:00Z"), 208.0),
        Exercise(Instant.parse("2025-05-10T18:30:00Z"), Instant.parse("2025-05-10T19:41:00Z"), 625.0),
        Exercise(Instant.parse("2025-05-11T07:53:00Z"), Instant.parse("2025-05-11T08:48:00Z"), 440.0),
        Exercise(Instant.parse("2025-05-12T08:05:00Z"), Instant.parse("2025-05-12T08:54:00Z"), 343.0),
        Exercise(Instant.parse("2025-05-12T17:52:00Z"), Instant.parse("2025-05-12T18:33:00Z"), 328.0),
        // 2025-05-13 (0) — rest day
        Exercise(Instant.parse("2025-05-14T19:20:00Z"), Instant.parse("2025-05-14T20:26:00Z"), 561.0),
        Exercise(Instant.parse("2025-05-15T20:12:00Z"), Instant.parse("2025-05-15T21:03:00Z"), 408.0),
        Exercise(Instant.parse("2025-05-16T07:18:00Z"), Instant.parse("2025-05-16T08:02:00Z"), 308.0),
        Exercise(Instant.parse("2025-05-16T18:47:00Z"), Instant.parse("2025-05-16T19:35:00Z"), 432.0)
    )

    fun getHeartRateData(): List<HeartRate> = listOf(
        // ===== 2025-05-04 =====
        HeartRate(
            startTime = Instant.parse("2025-05-04T00:00:00Z"),
            endTime = Instant.parse("2025-05-04T04:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-04T00:00:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-04T00:20:00Z"), 57),
                HeartRateSample(Instant.parse("2025-05-04T00:40:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-04T01:00:00Z"), 53),
                HeartRateSample(Instant.parse("2025-05-04T01:20:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-04T01:40:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-04T02:00:00Z"), 52),
                HeartRateSample(Instant.parse("2025-05-04T02:20:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-04T02:40:00Z"), 53),
                HeartRateSample(Instant.parse("2025-05-04T03:00:00Z"), 52),
                HeartRateSample(Instant.parse("2025-05-04T03:20:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-04T03:40:00Z"), 54)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-04T04:00:00Z"),
            endTime = Instant.parse("2025-05-04T08:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-04T04:00:00Z"), 53),
                HeartRateSample(Instant.parse("2025-05-04T04:20:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-04T04:40:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-04T05:00:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-04T05:20:00Z"), 57),
                HeartRateSample(Instant.parse("2025-05-04T05:40:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-04T06:00:00Z"), 66),
                HeartRateSample(Instant.parse("2025-05-04T06:20:00Z"), 80),
                HeartRateSample(Instant.parse("2025-05-04T06:40:00Z"), 69),
                HeartRateSample(Instant.parse("2025-05-04T07:00:00Z"), 70),
                HeartRateSample(Instant.parse("2025-05-04T07:20:00Z"), 85),
                HeartRateSample(Instant.parse("2025-05-04T07:40:00Z"), 73)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-04T08:00:00Z"),
            endTime = Instant.parse("2025-05-04T12:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-04T08:00:00Z"), 76),
                HeartRateSample(Instant.parse("2025-05-04T08:20:00Z"), 91),
                HeartRateSample(Instant.parse("2025-05-04T08:40:00Z"), 79),
                HeartRateSample(Instant.parse("2025-05-04T09:00:00Z"), 78),
                HeartRateSample(Instant.parse("2025-05-04T09:20:00Z"), 94),
                HeartRateSample(Instant.parse("2025-05-04T09:40:00Z"), 82),
                HeartRateSample(Instant.parse("2025-05-04T10:00:00Z"), 82),
                HeartRateSample(Instant.parse("2025-05-04T10:20:00Z"), 98),
                HeartRateSample(Instant.parse("2025-05-04T10:40:00Z"), 86),
                HeartRateSample(Instant.parse("2025-05-04T11:00:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-04T11:20:00Z"), 100),
                HeartRateSample(Instant.parse("2025-05-04T11:40:00Z"), 88)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-04T12:00:00Z"),
            endTime = Instant.parse("2025-05-04T16:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-04T12:00:00Z"), 86),
                HeartRateSample(Instant.parse("2025-05-04T12:20:00Z"), 101),
                HeartRateSample(Instant.parse("2025-05-04T12:40:00Z"), 89),
                HeartRateSample(Instant.parse("2025-05-04T13:00:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-04T13:20:00Z"), 99),
                HeartRateSample(Instant.parse("2025-05-04T13:40:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-04T14:00:00Z"), 83),
                HeartRateSample(Instant.parse("2025-05-04T14:20:00Z"), 97),
                HeartRateSample(Instant.parse("2025-05-04T14:40:00Z"), 86),
                HeartRateSample(Instant.parse("2025-05-04T15:00:00Z"), 82),
                HeartRateSample(Instant.parse("2025-05-04T15:20:00Z"), 96),
                HeartRateSample(Instant.parse("2025-05-04T15:40:00Z"), 85)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-04T16:00:00Z"),
            endTime = Instant.parse("2025-05-04T20:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-04T16:00:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-04T16:20:00Z"), 100),
                HeartRateSample(Instant.parse("2025-05-04T16:40:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-04T17:00:00Z"), 86),
                HeartRateSample(Instant.parse("2025-05-04T17:20:00Z"), 103),
                HeartRateSample(Instant.parse("2025-05-04T17:40:00Z"), 90),
                HeartRateSample(Instant.parse("2025-05-04T18:00:00Z"), 132),
                HeartRateSample(Instant.parse("2025-05-04T18:20:00Z"), 147),
                HeartRateSample(Instant.parse("2025-05-04T18:40:00Z"), 138),
                HeartRateSample(Instant.parse("2025-05-04T19:00:00Z"), 92),
                HeartRateSample(Instant.parse("2025-05-04T19:20:00Z"), 107),
                HeartRateSample(Instant.parse("2025-05-04T19:40:00Z"), 95)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-04T20:00:00Z"),
            endTime = Instant.parse("2025-05-05T00:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-04T20:00:00Z"), 78),
                HeartRateSample(Instant.parse("2025-05-04T20:20:00Z"), 92),
                HeartRateSample(Instant.parse("2025-05-04T20:40:00Z"), 81),
                HeartRateSample(Instant.parse("2025-05-04T21:00:00Z"), 72),
                HeartRateSample(Instant.parse("2025-05-04T21:20:00Z"), 86),
                HeartRateSample(Instant.parse("2025-05-04T21:40:00Z"), 75),
                HeartRateSample(Instant.parse("2025-05-04T22:00:00Z"), 58),
                HeartRateSample(Instant.parse("2025-05-04T22:20:00Z"), 61),
                HeartRateSample(Instant.parse("2025-05-04T22:40:00Z"), 59),
                HeartRateSample(Instant.parse("2025-05-04T23:00:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-04T23:20:00Z"), 59),
                HeartRateSample(Instant.parse("2025-05-04T23:40:00Z"), 57)
            )
        ),

        // ===== 2025-05-05 =====
        HeartRate(
            startTime = Instant.parse("2025-05-05T00:00:00Z"),
            endTime = Instant.parse("2025-05-05T04:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-05T00:00:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-05T00:20:00Z"), 58),
                HeartRateSample(Instant.parse("2025-05-05T00:40:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-05T01:00:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-05T01:20:00Z"), 57),
                HeartRateSample(Instant.parse("2025-05-05T01:40:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-05T02:00:00Z"), 53),
                HeartRateSample(Instant.parse("2025-05-05T02:20:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-05T02:40:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-05T03:00:00Z"), 53),
                HeartRateSample(Instant.parse("2025-05-05T03:20:00Z"), 57),
                HeartRateSample(Instant.parse("2025-05-05T03:40:00Z"), 55)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-05T04:00:00Z"),
            endTime = Instant.parse("2025-05-05T08:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-05T04:00:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-05T04:20:00Z"), 57),
                HeartRateSample(Instant.parse("2025-05-05T04:40:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-05T05:00:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-05T05:20:00Z"), 58),
                HeartRateSample(Instant.parse("2025-05-05T05:40:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-05T06:00:00Z"), 68),
                HeartRateSample(Instant.parse("2025-05-05T06:20:00Z"), 83),
                HeartRateSample(Instant.parse("2025-05-05T06:40:00Z"), 71),
                HeartRateSample(Instant.parse("2025-05-05T07:00:00Z"), 72),
                HeartRateSample(Instant.parse("2025-05-05T07:20:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-05T07:40:00Z"), 76)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-05T08:00:00Z"),
            endTime = Instant.parse("2025-05-05T12:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-05T08:00:00Z"), 76),
                HeartRateSample(Instant.parse("2025-05-05T08:20:00Z"), 92),
                HeartRateSample(Instant.parse("2025-05-05T08:40:00Z"), 80),
                HeartRateSample(Instant.parse("2025-05-05T09:00:00Z"), 78),
                HeartRateSample(Instant.parse("2025-05-05T09:20:00Z"), 94),
                HeartRateSample(Instant.parse("2025-05-05T09:40:00Z"), 82),
                HeartRateSample(Instant.parse("2025-05-05T10:00:00Z"), 80),
                HeartRateSample(Instant.parse("2025-05-05T10:20:00Z"), 96),
                HeartRateSample(Instant.parse("2025-05-05T10:40:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-05T11:00:00Z"), 82),
                HeartRateSample(Instant.parse("2025-05-05T11:20:00Z"), 98),
                HeartRateSample(Instant.parse("2025-05-05T11:40:00Z"), 86)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-05T12:00:00Z"),
            endTime = Instant.parse("2025-05-05T16:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-05T12:00:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-05T12:20:00Z"), 100),
                HeartRateSample(Instant.parse("2025-05-05T12:40:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-05T13:00:00Z"), 86),
                HeartRateSample(Instant.parse("2025-05-05T13:20:00Z"), 102),
                HeartRateSample(Instant.parse("2025-05-05T13:40:00Z"), 90),
                HeartRateSample(Instant.parse("2025-05-05T14:00:00Z"), 85),
                HeartRateSample(Instant.parse("2025-05-05T14:20:00Z"), 100),
                HeartRateSample(Instant.parse("2025-05-05T14:40:00Z"), 89),
                HeartRateSample(Instant.parse("2025-05-05T15:00:00Z"), 83),
                HeartRateSample(Instant.parse("2025-05-05T15:20:00Z"), 98),
                HeartRateSample(Instant.parse("2025-05-05T15:40:00Z"), 87)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-05T16:00:00Z"),
            endTime = Instant.parse("2025-05-05T20:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-05T16:00:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-05T16:20:00Z"), 100),
                HeartRateSample(Instant.parse("2025-05-05T16:40:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-05T17:00:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-05T17:20:00Z"), 103),
                HeartRateSample(Instant.parse("2025-05-05T17:40:00Z"), 91),
                HeartRateSample(Instant.parse("2025-05-05T18:00:00Z"), 135),
                HeartRateSample(Instant.parse("2025-05-05T18:20:00Z"), 150),
                HeartRateSample(Instant.parse("2025-05-05T18:40:00Z"), 140),
                HeartRateSample(Instant.parse("2025-05-05T19:00:00Z"), 94),
                HeartRateSample(Instant.parse("2025-05-05T19:20:00Z"), 110),
                HeartRateSample(Instant.parse("2025-05-05T19:40:00Z"), 98)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-05T20:00:00Z"),
            endTime = Instant.parse("2025-05-06T00:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-05T20:00:00Z"), 78),
                HeartRateSample(Instant.parse("2025-05-05T20:20:00Z"), 92),
                HeartRateSample(Instant.parse("2025-05-05T20:40:00Z"), 81),
                HeartRateSample(Instant.parse("2025-05-05T21:00:00Z"), 70),
                HeartRateSample(Instant.parse("2025-05-05T21:20:00Z"), 85),
                HeartRateSample(Instant.parse("2025-05-05T21:40:00Z"), 73),
                HeartRateSample(Instant.parse("2025-05-05T22:00:00Z"), 58),
                HeartRateSample(Instant.parse("2025-05-05T22:20:00Z"), 61),
                HeartRateSample(Instant.parse("2025-05-05T22:40:00Z"), 59),
                HeartRateSample(Instant.parse("2025-05-05T23:00:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-05T23:20:00Z"), 59),
                HeartRateSample(Instant.parse("2025-05-05T23:40:00Z"), 57)
            )
        ),

        // ===== 2025-05-06 =====
        HeartRate(
            startTime = Instant.parse("2025-05-06T00:00:00Z"),
            endTime = Instant.parse("2025-05-06T04:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-06T00:00:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-06T00:20:00Z"), 58),
                HeartRateSample(Instant.parse("2025-05-06T00:40:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-06T01:00:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-06T01:20:00Z"), 57),
                HeartRateSample(Instant.parse("2025-05-06T01:40:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-06T02:00:00Z"), 53),
                HeartRateSample(Instant.parse("2025-05-06T02:20:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-06T02:40:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-06T03:00:00Z"), 52),
                HeartRateSample(Instant.parse("2025-05-06T03:20:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-06T03:40:00Z"), 54)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-06T04:00:00Z"),
            endTime = Instant.parse("2025-05-06T08:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-06T04:00:00Z"), 54),
                HeartRateSample(Instant.parse("2025-05-06T04:20:00Z"), 57),
                HeartRateSample(Instant.parse("2025-05-06T04:40:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-06T05:00:00Z"), 55),
                HeartRateSample(Instant.parse("2025-05-06T05:20:00Z"), 58),
                HeartRateSample(Instant.parse("2025-05-06T05:40:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-06T06:00:00Z"), 70),
                HeartRateSample(Instant.parse("2025-05-06T06:20:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-06T06:40:00Z"), 73),
                HeartRateSample(Instant.parse("2025-05-06T07:00:00Z"), 128),
                HeartRateSample(Instant.parse("2025-05-06T07:20:00Z"), 143),
                HeartRateSample(Instant.parse("2025-05-06T07:40:00Z"), 135)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-06T08:00:00Z"),
            endTime = Instant.parse("2025-05-06T12:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-06T08:00:00Z"), 82),
                HeartRateSample(Instant.parse("2025-05-06T08:20:00Z"), 98),
                HeartRateSample(Instant.parse("2025-05-06T08:40:00Z"), 86),
                HeartRateSample(Instant.parse("2025-05-06T09:00:00Z"), 80),
                HeartRateSample(Instant.parse("2025-05-06T09:20:00Z"), 96),
                HeartRateSample(Instant.parse("2025-05-06T09:40:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-06T10:00:00Z"), 78),
                HeartRateSample(Instant.parse("2025-05-06T10:20:00Z"), 94),
                HeartRateSample(Instant.parse("2025-05-06T10:40:00Z"), 82),
                HeartRateSample(Instant.parse("2025-05-06T11:00:00Z"), 76),
                HeartRateSample(Instant.parse("2025-05-06T11:20:00Z"), 92),
                HeartRateSample(Instant.parse("2025-05-06T11:40:00Z"), 80)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-06T12:00:00Z"),
            endTime = Instant.parse("2025-05-06T16:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-06T12:00:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-06T12:20:00Z"), 100),
                HeartRateSample(Instant.parse("2025-05-06T12:40:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-06T13:00:00Z"), 86),
                HeartRateSample(Instant.parse("2025-05-06T13:20:00Z"), 101),
                HeartRateSample(Instant.parse("2025-05-06T13:40:00Z"), 89),
                HeartRateSample(Instant.parse("2025-05-06T14:00:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-06T14:20:00Z"), 99),
                HeartRateSample(Instant.parse("2025-05-06T14:40:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-06T15:00:00Z"), 82),
                HeartRateSample(Instant.parse("2025-05-06T15:20:00Z"), 96),
                HeartRateSample(Instant.parse("2025-05-06T15:40:00Z"), 85)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-06T16:00:00Z"),
            endTime = Instant.parse("2025-05-06T20:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-06T16:00:00Z"), 84),
                HeartRateSample(Instant.parse("2025-05-06T16:20:00Z"), 100),
                HeartRateSample(Instant.parse("2025-05-06T16:40:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-06T17:00:00Z"), 86),
                HeartRateSample(Instant.parse("2025-05-06T17:20:00Z"), 101),
                HeartRateSample(Instant.parse("2025-05-06T17:40:00Z"), 89),
                HeartRateSample(Instant.parse("2025-05-06T18:00:00Z"), 90),
                HeartRateSample(Instant.parse("2025-05-06T18:20:00Z"), 105),
                HeartRateSample(Instant.parse("2025-05-06T18:40:00Z"), 94),
                HeartRateSample(Instant.parse("2025-05-06T19:00:00Z"), 88),
                HeartRateSample(Instant.parse("2025-05-06T19:20:00Z"), 103),
                HeartRateSample(Instant.parse("2025-05-06T19:40:00Z"), 91)
            )
        ),
        HeartRate(
            startTime = Instant.parse("2025-05-06T20:00:00Z"),
            endTime = Instant.parse("2025-05-07T00:00:00Z"),
            samples = listOf(
                HeartRateSample(Instant.parse("2025-05-06T20:00:00Z"), 76),
                HeartRateSample(Instant.parse("2025-05-06T20:20:00Z"), 91),
                HeartRateSample(Instant.parse("2025-05-06T20:40:00Z"), 79),
                HeartRateSample(Instant.parse("2025-05-06T21:00:00Z"), 70),
                HeartRateSample(Instant.parse("2025-05-06T21:20:00Z"), 85),
                HeartRateSample(Instant.parse("2025-05-06T21:40:00Z"), 73),
                HeartRateSample(Instant.parse("2025-05-06T22:00:00Z"), 58),
                HeartRateSample(Instant.parse("2025-05-06T22:20:00Z"), 61),
                HeartRateSample(Instant.parse("2025-05-06T22:40:00Z"), 59),
                HeartRateSample(Instant.parse("2025-05-06T23:00:00Z"), 56),
                HeartRateSample(Instant.parse("2025-05-06T23:20:00Z"), 59),
                HeartRateSample(Instant.parse("2025-05-06T23:40:00Z"), 57)
            )
        )
    )

    fun getSingleSleepSessionData(): SleepSession = SleepSession(
        startTime = Instant.parse("2025-05-04T13:29:00Z"),
        endTime   = Instant.parse("2025-05-04T21:09:00Z"),
        stages = listOf(
            SleepStage(Instant.parse("2025-05-04T13:29:00Z"), Instant.parse("2025-05-04T13:40:00Z"), SleepStageType.LIGHT),
            SleepStage(Instant.parse("2025-05-04T13:40:00Z"), Instant.parse("2025-05-04T14:05:00Z"), SleepStageType.DEEP),
            SleepStage(Instant.parse("2025-05-04T14:05:00Z"), Instant.parse("2025-05-04T14:15:00Z"), SleepStageType.LIGHT),
            SleepStage(Instant.parse("2025-05-04T14:15:00Z"), Instant.parse("2025-05-04T14:19:00Z"), SleepStageType.AWAKE),
            SleepStage(Instant.parse("2025-05-04T14:19:00Z"), Instant.parse("2025-05-04T14:30:00Z"), SleepStageType.LIGHT),
            SleepStage(Instant.parse("2025-05-04T14:30:00Z"), Instant.parse("2025-05-04T14:45:00Z"), SleepStageType.REM),
            SleepStage(Instant.parse("2025-05-04T14:45:00Z"), Instant.parse("2025-05-04T15:00:00Z"), SleepStageType.LIGHT),

            SleepStage(Instant.parse("2025-05-04T15:00:00Z"), Instant.parse("2025-05-04T15:15:00Z"), SleepStageType.DEEP),
            SleepStage(Instant.parse("2025-05-04T15:15:00Z"), Instant.parse("2025-05-04T15:19:00Z"), SleepStageType.AWAKE),
            SleepStage(Instant.parse("2025-05-04T15:19:00Z"), Instant.parse("2025-05-04T15:31:00Z"), SleepStageType.LIGHT),
            SleepStage(Instant.parse("2025-05-04T15:31:00Z"), Instant.parse("2025-05-04T15:47:00Z"), SleepStageType.REM),
            SleepStage(Instant.parse("2025-05-04T15:47:00Z"), Instant.parse("2025-05-04T16:00:00Z"), SleepStageType.LIGHT),

            SleepStage(Instant.parse("2025-05-04T16:00:00Z"), Instant.parse("2025-05-04T16:20:00Z"), SleepStageType.DEEP),
            SleepStage(Instant.parse("2025-05-04T16:20:00Z"), Instant.parse("2025-05-04T16:28:00Z"), SleepStageType.LIGHT),
            SleepStage(Instant.parse("2025-05-04T16:28:00Z"), Instant.parse("2025-05-04T16:32:00Z"), SleepStageType.AWAKE),
            SleepStage(Instant.parse("2025-05-04T16:32:00Z"), Instant.parse("2025-05-04T16:43:00Z"), SleepStageType.LIGHT),

            SleepStage(Instant.parse("2025-05-04T16:43:00Z"), Instant.parse("2025-05-04T17:00:00Z"), SleepStageType.REM),
            SleepStage(Instant.parse("2025-05-04T17:00:00Z"), Instant.parse("2025-05-04T17:13:00Z"), SleepStageType.LIGHT),
            SleepStage(Instant.parse("2025-05-04T17:13:00Z"), Instant.parse("2025-05-04T17:31:00Z"), SleepStageType.DEEP),
            SleepStage(Instant.parse("2025-05-04T17:31:00Z"), Instant.parse("2025-05-04T17:39:00Z"), SleepStageType.LIGHT),

            SleepStage(Instant.parse("2025-05-04T17:39:00Z"), Instant.parse("2025-05-04T17:43:00Z"), SleepStageType.AWAKE),
            SleepStage(Instant.parse("2025-05-04T17:43:00Z"), Instant.parse("2025-05-04T17:55:00Z"), SleepStageType.LIGHT),
            SleepStage(Instant.parse("2025-05-04T17:55:00Z"), Instant.parse("2025-05-04T18:13:00Z"), SleepStageType.REM),
            SleepStage(Instant.parse("2025-05-04T18:13:00Z"), Instant.parse("2025-05-04T18:25:00Z"), SleepStageType.LIGHT),

            SleepStage(Instant.parse("2025-05-04T18:25:00Z"), Instant.parse("2025-05-04T18:45:00Z"), SleepStageType.DEEP),
            SleepStage(Instant.parse("2025-05-04T18:45:00Z"), Instant.parse("2025-05-04T18:57:00Z"), SleepStageType.LIGHT),
            SleepStage(Instant.parse("2025-05-04T18:57:00Z"), Instant.parse("2025-05-04T19:01:00Z"), SleepStageType.AWAKE),
            SleepStage(Instant.parse("2025-05-04T19:01:00Z"), Instant.parse("2025-05-04T19:13:00Z"), SleepStageType.LIGHT),

            SleepStage(Instant.parse("2025-05-04T19:13:00Z"), Instant.parse("2025-05-04T19:33:00Z"), SleepStageType.REM),
            SleepStage(Instant.parse("2025-05-04T19:33:00Z"), Instant.parse("2025-05-04T19:47:00Z"), SleepStageType.LIGHT),
            SleepStage(Instant.parse("2025-05-04T19:47:00Z"), Instant.parse("2025-05-04T20:07:00Z"), SleepStageType.REM),
            SleepStage(Instant.parse("2025-05-04T20:07:00Z"), Instant.parse("2025-05-04T21:09:00Z"), SleepStageType.LIGHT)
        )
    )

    fun getMultipleSleepSessionData(): List<SleepSession> = listOf(
        SleepSession(
            startTime = Instant.parse("2025-05-04T00:00:00Z"),
            endTime = Instant.parse("2025-05-04T08:00:00Z"),
            stages = listOf(
                SleepStage(
                    Instant.parse("2025-05-04T00:00:00Z"),
                    Instant.parse("2025-05-04T02:00:00Z"),
                    SleepStageType.REM
                ),
                SleepStage(
                    Instant.parse("2025-05-04T02:00:00Z"),
                    Instant.parse("2025-05-04T04:00:00Z"),
                    SleepStageType.REM
                ),
                SleepStage(
                    Instant.parse("2025-05-04T04:00:00Z"),
                    Instant.parse("2025-05-04T06:00:00Z"),
                    SleepStageType.LIGHT
                ),
                SleepStage(
                    Instant.parse("2025-05-04T06:00:00Z"),
                    Instant.parse("2025-05-04T08:00:00Z"),
                    SleepStageType.AWAKE
                )
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-05-03T16:00:00Z"),
            endTime = Instant.parse("2025-05-04T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-05-03T16:00:00Z"), Instant.parse("2025-05-03T18:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-05-03T18:00:00Z"), Instant.parse("2025-05-03T20:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-05-03T20:00:00Z"), Instant.parse("2025-05-03T22:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-05-03T22:00:00Z"), Instant.parse("2025-05-04T00:00:00Z"), SleepStageType.LIGHT)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-05-03T08:00:00Z"),
            endTime = Instant.parse("2025-05-03T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-05-03T08:00:00Z"), Instant.parse("2025-05-03T10:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-05-03T10:00:00Z"), Instant.parse("2025-05-03T12:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-05-03T12:00:00Z"), Instant.parse("2025-05-03T14:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-05-03T14:00:00Z"), Instant.parse("2025-05-03T16:00:00Z"), SleepStageType.LIGHT)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-05-03T00:00:00Z"),
            endTime = Instant.parse("2025-05-03T08:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-05-03T00:00:00Z"), Instant.parse("2025-05-03T02:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-05-03T02:00:00Z"), Instant.parse("2025-05-03T04:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-05-03T04:00:00Z"), Instant.parse("2025-05-03T06:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-05-03T06:00:00Z"), Instant.parse("2025-05-03T08:00:00Z"), SleepStageType.AWAKE)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-05-02T16:00:00Z"),
            endTime = Instant.parse("2025-05-03T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-05-02T16:00:00Z"), Instant.parse("2025-05-02T18:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-05-02T18:00:00Z"), Instant.parse("2025-05-02T20:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-05-02T20:00:00Z"), Instant.parse("2025-05-02T22:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-05-02T22:00:00Z"), Instant.parse("2025-05-03T00:00:00Z"), SleepStageType.REM)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-05-02T08:00:00Z"),
            endTime = Instant.parse("2025-05-02T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-05-02T08:00:00Z"), Instant.parse("2025-05-02T10:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-05-02T10:00:00Z"), Instant.parse("2025-05-02T12:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-05-02T12:00:00Z"), Instant.parse("2025-05-02T14:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-05-02T14:00:00Z"), Instant.parse("2025-05-02T16:00:00Z"), SleepStageType.LIGHT)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-05-02T00:00:00Z"),
            endTime = Instant.parse("2025-05-02T08:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-05-02T00:00:00Z"), Instant.parse("2025-05-02T02:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-05-02T02:00:00Z"), Instant.parse("2025-05-02T04:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-05-02T04:00:00Z"), Instant.parse("2025-05-02T06:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-05-02T06:00:00Z"), Instant.parse("2025-05-02T08:00:00Z"), SleepStageType.LIGHT)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-05-01T16:00:00Z"),
            endTime = Instant.parse("2025-05-02T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-05-01T16:00:00Z"), Instant.parse("2025-05-01T18:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-05-01T18:00:00Z"), Instant.parse("2025-05-01T20:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-05-01T20:00:00Z"), Instant.parse("2025-05-01T22:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-05-01T22:00:00Z"), Instant.parse("2025-05-02T00:00:00Z"), SleepStageType.DEEP)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-05-01T08:00:00Z"),
            endTime = Instant.parse("2025-05-01T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-05-01T08:00:00Z"), Instant.parse("2025-05-01T10:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-05-01T10:00:00Z"), Instant.parse("2025-05-01T12:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-05-01T12:00:00Z"), Instant.parse("2025-05-01T14:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-05-01T14:00:00Z"), Instant.parse("2025-05-01T16:00:00Z"), SleepStageType.AWAKE)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-05-01T00:00:00Z"),
            endTime = Instant.parse("2025-05-01T08:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-05-01T00:00:00Z"), Instant.parse("2025-05-01T02:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-05-01T02:00:00Z"), Instant.parse("2025-05-01T04:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-05-01T04:00:00Z"), Instant.parse("2025-05-01T06:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-05-01T06:00:00Z"), Instant.parse("2025-05-01T08:00:00Z"), SleepStageType.DEEP)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-30T16:00:00Z"),
            endTime = Instant.parse("2025-05-01T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-30T16:00:00Z"), Instant.parse("2025-04-30T18:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-30T18:00:00Z"), Instant.parse("2025-04-30T20:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-30T20:00:00Z"), Instant.parse("2025-04-30T22:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-30T22:00:00Z"), Instant.parse("2025-05-01T00:00:00Z"), SleepStageType.DEEP)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-30T08:00:00Z"),
            endTime = Instant.parse("2025-04-30T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-30T08:00:00Z"), Instant.parse("2025-04-30T10:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-30T10:00:00Z"), Instant.parse("2025-04-30T12:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-30T12:00:00Z"), Instant.parse("2025-04-30T14:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-30T14:00:00Z"), Instant.parse("2025-04-30T16:00:00Z"), SleepStageType.AWAKE)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-30T00:00:00Z"),
            endTime = Instant.parse("2025-04-30T08:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-30T00:00:00Z"), Instant.parse("2025-04-30T02:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-30T02:00:00Z"), Instant.parse("2025-04-30T04:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-30T04:00:00Z"), Instant.parse("2025-04-30T06:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-30T06:00:00Z"), Instant.parse("2025-04-30T08:00:00Z"), SleepStageType.LIGHT)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-29T16:00:00Z"),
            endTime = Instant.parse("2025-04-30T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-29T16:00:00Z"), Instant.parse("2025-04-29T18:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-29T18:00:00Z"), Instant.parse("2025-04-29T20:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-29T20:00:00Z"), Instant.parse("2025-04-29T22:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-29T22:00:00Z"), Instant.parse("2025-04-30T00:00:00Z"), SleepStageType.AWAKE)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-29T08:00:00Z"),
            endTime = Instant.parse("2025-04-29T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-29T08:00:00Z"), Instant.parse("2025-04-29T10:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-29T10:00:00Z"), Instant.parse("2025-04-29T12:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-29T12:00:00Z"), Instant.parse("2025-04-29T14:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-29T14:00:00Z"), Instant.parse("2025-04-29T16:00:00Z"), SleepStageType.AWAKE)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-29T00:00:00Z"),
            endTime = Instant.parse("2025-04-29T08:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-29T00:00:00Z"), Instant.parse("2025-04-29T02:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-29T02:00:00Z"), Instant.parse("2025-04-29T04:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-29T04:00:00Z"), Instant.parse("2025-04-29T06:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-29T06:00:00Z"), Instant.parse("2025-04-29T08:00:00Z"), SleepStageType.AWAKE)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-28T16:00:00Z"),
            endTime = Instant.parse("2025-04-29T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-28T16:00:00Z"), Instant.parse("2025-04-28T18:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-28T18:00:00Z"), Instant.parse("2025-04-28T20:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-28T20:00:00Z"), Instant.parse("2025-04-28T22:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-28T22:00:00Z"), Instant.parse("2025-04-29T00:00:00Z"), SleepStageType.LIGHT)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-28T08:00:00Z"),
            endTime = Instant.parse("2025-04-28T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-28T08:00:00Z"), Instant.parse("2025-04-28T10:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-28T10:00:00Z"), Instant.parse("2025-04-28T12:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-28T12:00:00Z"), Instant.parse("2025-04-28T14:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-28T14:00:00Z"), Instant.parse("2025-04-28T16:00:00Z"), SleepStageType.REM)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-28T00:00:00Z"),
            endTime = Instant.parse("2025-04-28T08:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-28T00:00:00Z"), Instant.parse("2025-04-28T02:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-28T02:00:00Z"), Instant.parse("2025-04-28T04:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-28T04:00:00Z"), Instant.parse("2025-04-28T06:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-28T06:00:00Z"), Instant.parse("2025-04-28T08:00:00Z"), SleepStageType.REM)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-27T16:00:00Z"),
            endTime = Instant.parse("2025-04-28T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-27T16:00:00Z"), Instant.parse("2025-04-27T18:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-27T18:00:00Z"), Instant.parse("2025-04-27T20:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-27T20:00:00Z"), Instant.parse("2025-04-27T22:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-27T22:00:00Z"), Instant.parse("2025-04-28T00:00:00Z"), SleepStageType.DEEP)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-27T08:00:00Z"),
            endTime = Instant.parse("2025-04-27T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-27T08:00:00Z"), Instant.parse("2025-04-27T10:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-27T10:00:00Z"), Instant.parse("2025-04-27T12:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-27T12:00:00Z"), Instant.parse("2025-04-27T14:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-27T14:00:00Z"), Instant.parse("2025-04-27T16:00:00Z"), SleepStageType.LIGHT)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-27T00:00:00Z"),
            endTime = Instant.parse("2025-04-27T08:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-27T00:00:00Z"), Instant.parse("2025-04-27T02:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-27T02:00:00Z"), Instant.parse("2025-04-27T04:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-27T04:00:00Z"), Instant.parse("2025-04-27T06:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-27T06:00:00Z"), Instant.parse("2025-04-27T08:00:00Z"), SleepStageType.REM)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-26T16:00:00Z"),
            endTime = Instant.parse("2025-04-27T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-26T16:00:00Z"), Instant.parse("2025-04-26T18:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-26T18:00:00Z"), Instant.parse("2025-04-26T20:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-26T20:00:00Z"), Instant.parse("2025-04-26T22:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-26T22:00:00Z"), Instant.parse("2025-04-27T00:00:00Z"), SleepStageType.REM)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-26T08:00:00Z"),
            endTime = Instant.parse("2025-04-26T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-26T08:00:00Z"), Instant.parse("2025-04-26T10:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-26T10:00:00Z"), Instant.parse("2025-04-26T12:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-26T12:00:00Z"), Instant.parse("2025-04-26T14:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-26T14:00:00Z"), Instant.parse("2025-04-26T16:00:00Z"), SleepStageType.DEEP)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-26T00:00:00Z"),
            endTime = Instant.parse("2025-04-26T08:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-26T00:00:00Z"), Instant.parse("2025-04-26T02:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-26T02:00:00Z"), Instant.parse("2025-04-26T04:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-26T04:00:00Z"), Instant.parse("2025-04-26T06:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-26T06:00:00Z"), Instant.parse("2025-04-26T08:00:00Z"), SleepStageType.REM)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-25T16:00:00Z"),
            endTime = Instant.parse("2025-04-26T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-25T16:00:00Z"), Instant.parse("2025-04-25T18:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-25T18:00:00Z"), Instant.parse("2025-04-25T20:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-25T20:00:00Z"), Instant.parse("2025-04-25T22:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-25T22:00:00Z"), Instant.parse("2025-04-26T00:00:00Z"), SleepStageType.DEEP)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-25T08:00:00Z"),
            endTime = Instant.parse("2025-04-25T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-25T08:00:00Z"), Instant.parse("2025-04-25T10:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-25T10:00:00Z"), Instant.parse("2025-04-25T12:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-25T12:00:00Z"), Instant.parse("2025-04-25T14:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-25T14:00:00Z"), Instant.parse("2025-04-25T16:00:00Z"), SleepStageType.LIGHT)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-25T00:00:00Z"),
            endTime = Instant.parse("2025-04-25T08:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-25T00:00:00Z"), Instant.parse("2025-04-25T02:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-25T02:00:00Z"), Instant.parse("2025-04-25T04:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-25T04:00:00Z"), Instant.parse("2025-04-25T06:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-25T06:00:00Z"), Instant.parse("2025-04-25T08:00:00Z"), SleepStageType.REM)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-24T16:00:00Z"),
            endTime = Instant.parse("2025-04-25T00:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-24T16:00:00Z"), Instant.parse("2025-04-24T18:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-24T18:00:00Z"), Instant.parse("2025-04-24T20:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-24T20:00:00Z"), Instant.parse("2025-04-24T22:00:00Z"), SleepStageType.DEEP),
                SleepStage(Instant.parse("2025-04-24T22:00:00Z"), Instant.parse("2025-04-25T00:00:00Z"), SleepStageType.REM)
            )
        ),
        SleepSession(
            startTime = Instant.parse("2025-04-24T08:00:00Z"),
            endTime = Instant.parse("2025-04-24T16:00:00Z"),
            stages = listOf(
                SleepStage(Instant.parse("2025-04-24T08:00:00Z"), Instant.parse("2025-04-24T10:00:00Z"), SleepStageType.AWAKE),
                SleepStage(Instant.parse("2025-04-24T10:00:00Z"), Instant.parse("2025-04-24T12:00:00Z"), SleepStageType.REM),
                SleepStage(Instant.parse("2025-04-24T12:00:00Z"), Instant.parse("2025-04-24T14:00:00Z"), SleepStageType.LIGHT),
                SleepStage(Instant.parse("2025-04-24T14:00:00Z"), Instant.parse("2025-04-24T16:00:00Z"), SleepStageType.DEEP)
            )
        )
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

    fun getBloodGlucoseData(): List<BloodGlucose> = listOf(
        BloodGlucose(Instant.parse("2025-05-04T08:00:00Z"), 64.0),
        BloodGlucose(Instant.parse("2025-05-03T20:00:00Z"), 70.0),
        BloodGlucose(Instant.parse("2025-05-03T08:00:00Z"), 58.0),
        BloodGlucose(Instant.parse("2025-05-02T20:00:00Z"), 65.0),
        BloodGlucose(Instant.parse("2025-05-02T08:00:00Z"), 49.0),
        BloodGlucose(Instant.parse("2025-05-01T20:00:00Z"), 59.0),
        BloodGlucose(Instant.parse("2025-05-01T08:00:00Z"), 57.0),
        BloodGlucose(Instant.parse("2025-04-30T20:00:00Z"), 55.0),
        BloodGlucose(Instant.parse("2025-04-30T08:00:00Z"), 53.0),
        BloodGlucose(Instant.parse("2025-04-29T20:00:00Z"), 64.0),
        BloodGlucose(Instant.parse("2025-04-29T08:00:00Z"), 68.0),
        BloodGlucose(Instant.parse("2025-04-28T20:00:00Z"), 52.0),
        BloodGlucose(Instant.parse("2025-04-28T08:00:00Z"), 53.0),
        BloodGlucose(Instant.parse("2025-04-27T20:00:00Z"), 65.0),
        BloodGlucose(Instant.parse("2025-04-27T08:00:00Z"), 65.0),
        BloodGlucose(Instant.parse("2025-04-26T20:00:00Z"), 40.0),
        BloodGlucose(Instant.parse("2025-04-26T08:00:00Z"), 62.0),
        BloodGlucose(Instant.parse("2025-04-25T20:00:00Z"), 44.0),
        BloodGlucose(Instant.parse("2025-04-25T08:00:00Z"), 55.0),
        BloodGlucose(Instant.parse("2025-04-24T20:00:00Z"), 46.0),
        BloodGlucose(Instant.parse("2025-04-24T08:00:00Z"), 51.0),
        BloodGlucose(Instant.parse("2025-04-23T20:00:00Z"), 46.0),
        BloodGlucose(Instant.parse("2025-04-23T08:00:00Z"), 47.0),
        BloodGlucose(Instant.parse("2025-04-22T20:00:00Z"), 54.0),
        BloodGlucose(Instant.parse("2025-04-22T08:00:00Z"), 51.0),
        BloodGlucose(Instant.parse("2025-04-21T20:00:00Z"), 48.0),
        BloodGlucose(Instant.parse("2025-04-21T08:00:00Z"), 55.0),
        BloodGlucose(Instant.parse("2025-04-20T20:00:00Z"), 46.0),
        BloodGlucose(Instant.parse("2025-04-20T08:00:00Z"), 64.0),
        BloodGlucose(Instant.parse("2025-04-19T20:00:00Z"), 43.0)
    )

    /**
     * Sample heart rate range data for range bar charts
     * Returns List<ChartMark> where multiple points with same x-value represent min and max values
     */
    fun getHeartRateRangeData(): List<ChartMark> = listOf(
        // Day 1
        ChartMark(x = 0.0, y = 55.0, label = "1일"),
        ChartMark(x = 0.0, y = 150.0, label = "1일"),

        // Day 2
        ChartMark(x = 1.0, y = 54.0, label = "2일"),
        ChartMark(x = 1.0, y = 160.0, label = "2일"),

        // Day 3
        ChartMark(x = 2.0, y = 65.0, label = "3일"),
        ChartMark(x = 2.0, y = 145.0, label = "3일"),

        // Day 4
        ChartMark(x = 3.0, y = 58.0, label = "4일"),
        ChartMark(x = 3.0, y = 125.0, label = "4일"),

        // Day 5
        ChartMark(x = 4.0, y = 70.0, label = "5일"),
        ChartMark(x = 4.0, y = 140.0, label = "5일"),

        // Day 6
        ChartMark(x = 5.0, y = 75.0, label = "6일"),
        ChartMark(x = 5.0, y = 110.0, label = "6일"),

        // Day 7
        ChartMark(x = 6.0, y = 68.0, label = "7일"),
        ChartMark(x = 6.0, y = 162.0, label = "7일"),

        // Day 8
        ChartMark(x = 7.0, y = 72.0, label = "8일"),
        ChartMark(x = 7.0, y = 168.0, label = "8일"),

        // Day 9
        ChartMark(x = 8.0, y = 65.0, label = "9일"),
        ChartMark(x = 8.0, y = 138.0, label = "9일"),

        // Day 10
        ChartMark(x = 9.0, y = 85.0, label = "10일"),
        ChartMark(x = 9.0, y = 105.0, label = "10일"),

        // Day 11
        ChartMark(x = 10.0, y = 62.0, label = "11일"),
        ChartMark(x = 10.0, y = 140.0, label = "11일"),

        // Day 12
        ChartMark(x = 11.0, y = 70.0, label = "12일"),
        ChartMark(x = 11.0, y = 155.0, label = "12일"),

        // Day 13
        ChartMark(x = 12.0, y = 60.0, label = "13일"),
        ChartMark(x = 12.0, y = 130.0, label = "13일"),

        // Day 14
        ChartMark(x = 13.0, y = 75.0, label = "14일"),
        ChartMark(x = 13.0, y = 150.0, label = "14일"),

        // Day 15
        ChartMark(x = 14.0, y = 66.0, label = "15일"),
        ChartMark(x = 14.0, y = 142.0, label = "15일"),

        // Day 16
        ChartMark(x = 15.0, y = 78.0, label = "16일"),
        ChartMark(x = 15.0, y = 160.0, label = "16일"),

        // Day 17
        ChartMark(x = 16.0, y = 64.0, label = "17일"),
        ChartMark(x = 16.0, y = 135.0, label = "17일"),

        // Day 18
        ChartMark(x = 17.0, y = 72.0, label = "18일"),
        ChartMark(x = 17.0, y = 150.0, label = "18일"),

        // Day 19
        ChartMark(x = 18.0, y = 70.0, label = "19일"),
        ChartMark(x = 18.0, y = 145.0, label = "19일"),

        // Day 20
        ChartMark(x = 19.0, y = 68.0, label = "20일"),
        ChartMark(x = 19.0, y = 155.0, label = "20일"),
    )

    /**
     * Sample nutrition data for stacked bar charts (protein, fat, carbs)
     * Returns List<ChartMark> where multiple points with same x-value represent different segments
     */
    fun getNutritionStackedData(): List<ChartMark> = listOf(
        // Monday (x = 0)
        ChartMark(x = 0.0, y = 80.0, label = "월"),
        ChartMark(x = 0.0, y = 45.0, label = "월"),
        ChartMark(x = 0.0, y = 120.0, label = "월"),

        // Tuesday (x = 1)
        ChartMark(x = 1.0, y = 75.0, label = "화"),
        ChartMark(x = 1.0, y = 38.0, label = "화"),
        ChartMark(x = 1.0, y = 110.0, label = "화"),

        // Wednesday (x = 2)
        ChartMark(x = 2.0, y = 90.0, label = "수"),
        ChartMark(x = 2.0, y = 52.0, label = "수"),
        ChartMark(x = 2.0, y = 140.0, label = "수"),

        // Thursday (x = 3)
        ChartMark(x = 3.0, y = 85.0, label = "목"),
        ChartMark(x = 3.0, y = 41.0, label = "목"),
        ChartMark(x = 3.0, y = 135.0, label = "목"),

        // Friday (x = 4)
        ChartMark(x = 4.0, y = 95.0, label = "금"),
        ChartMark(x = 4.0, y = 58.0, label = "금"),
        ChartMark(x = 4.0, y = 150.0, label = "금"),

        // Saturday (x = 5)
        ChartMark(x = 5.0, y = 70.0, label = "토"),
        ChartMark(x = 5.0, y = 35.0, label = "토"),
        ChartMark(x = 5.0, y = 100.0, label = "토"),

        // Sunday (x = 6)
        ChartMark(x = 6.0, y = 88.0, label = "일"),
        ChartMark(x = 6.0, y = 48.0, label = "일"),
        ChartMark(x = 6.0, y = 125.0, label = "일")
    )

    /**
     * Sample progress data for Apple Watch-style activity rings
     */
    fun getActivityProgressData(): List<ProgressChartMark> = listOf(
        ProgressChartMark(x = 0.0, current = 2500.0, max = 2000.0, label = "Move", unit = "KJ"),
        ProgressChartMark(x = 1.0, current = 20.0, max = 60.0, label = "Exercise", unit = "min"),
        ProgressChartMark(x = 2.0, current = 7.0, max = 10.0, label = "Stand", unit = "h")
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
            val value = random.nextDouble() * 100
            CalendarEntry(date = date, value = value.toFloat())
        }.toList()
    }

    /**
     * Convert basic sample data to ChartMark format
     */
    fun getBasicChartMarks(): List<ChartMark> = sampleData.mapIndexed { index, value ->
        ChartMark(x = index.toDouble(), y = value, label = weekDays.getOrElse(index) { "" })
    }

    /**
     * Convert extended sample data to ChartMark format (for pagination demos)
     */
    fun getExtendedChartMarks(): List<ChartMark> = sampleData4.mapIndexed { index, value ->
        ChartMark(x = index.toDouble(), y = value, label = weekDays[index % weekDays.size])
    }

    /**
     * Generate dense chart points for tick reduction demos
     */
    fun getDenseChartMarks(count: Int = 50): List<ChartMark> {
        val labels = (1..count).map { "Day $it" }
        val values = (1..count).map { (20..80).random().toDouble() }
        return labels.mapIndexed { index, label ->
            ChartMark(x = index.toDouble(), y = values[index], label = label)
        }
    }

}