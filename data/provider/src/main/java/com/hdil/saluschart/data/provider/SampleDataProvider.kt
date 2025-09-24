package com.hdil.saluschart.data.provider

import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ProgressChartPoint
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.BloodPressure
import com.hdil.saluschart.data.model.model.BodyFat
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

    /**
     * Sample heart rate range data for range bar charts
     * Returns List<ChartPoint> where multiple points with same x-value represent min and max values
     */
    fun getHeartRateRangeData(): List<ChartPoint> = listOf(
        // Day 1
        ChartPoint(x = 0.0, y = 55.0, label = "1일"),
        ChartPoint(x = 0.0, y = 150.0, label = "1일"),

        // Day 2
        ChartPoint(x = 1.0, y = 54.0, label = "2일"),
        ChartPoint(x = 1.0, y = 160.0, label = "2일"),

        // Day 3
        ChartPoint(x = 2.0, y = 65.0, label = "3일"),
        ChartPoint(x = 2.0, y = 145.0, label = "3일"),

        // Day 4
        ChartPoint(x = 3.0, y = 58.0, label = "4일"),
        ChartPoint(x = 3.0, y = 125.0, label = "4일"),

        // Day 5
        ChartPoint(x = 4.0, y = 70.0, label = "5일"),
        ChartPoint(x = 4.0, y = 140.0, label = "5일"),

        // Day 6
        ChartPoint(x = 5.0, y = 75.0, label = "6일"),
        ChartPoint(x = 5.0, y = 110.0, label = "6일"),

        // Day 7
        ChartPoint(x = 6.0, y = 68.0, label = "7일"),
        ChartPoint(x = 6.0, y = 162.0, label = "7일"),

        // Day 8
        ChartPoint(x = 7.0, y = 72.0, label = "8일"),
        ChartPoint(x = 7.0, y = 168.0, label = "8일"),

        // Day 9
        ChartPoint(x = 8.0, y = 65.0, label = "9일"),
        ChartPoint(x = 8.0, y = 138.0, label = "9일"),

        // Day 10
        ChartPoint(x = 9.0, y = 85.0, label = "10일"),
        ChartPoint(x = 9.0, y = 105.0, label = "10일"),

        // Day 11
        ChartPoint(x = 10.0, y = 62.0, label = "11일"),
        ChartPoint(x = 10.0, y = 140.0, label = "11일"),

        // Day 12
        ChartPoint(x = 11.0, y = 70.0, label = "12일"),
        ChartPoint(x = 11.0, y = 155.0, label = "12일"),

        // Day 13
        ChartPoint(x = 12.0, y = 60.0, label = "13일"),
        ChartPoint(x = 12.0, y = 130.0, label = "13일"),

        // Day 14
        ChartPoint(x = 13.0, y = 75.0, label = "14일"),
        ChartPoint(x = 13.0, y = 150.0, label = "14일"),

        // Day 15
        ChartPoint(x = 14.0, y = 66.0, label = "15일"),
        ChartPoint(x = 14.0, y = 142.0, label = "15일"),

        // Day 16
        ChartPoint(x = 15.0, y = 78.0, label = "16일"),
        ChartPoint(x = 15.0, y = 160.0, label = "16일"),

        // Day 17
        ChartPoint(x = 16.0, y = 64.0, label = "17일"),
        ChartPoint(x = 16.0, y = 135.0, label = "17일"),

        // Day 18
        ChartPoint(x = 17.0, y = 72.0, label = "18일"),
        ChartPoint(x = 17.0, y = 150.0, label = "18일"),

        // Day 19
        ChartPoint(x = 18.0, y = 70.0, label = "19일"),
        ChartPoint(x = 18.0, y = 145.0, label = "19일"),

        // Day 20
        ChartPoint(x = 19.0, y = 68.0, label = "20일"),
        ChartPoint(x = 19.0, y = 155.0, label = "20일"),
    )

    /**
     * Sample nutrition data for stacked bar charts (protein, fat, carbs)
     * Returns List<ChartPoint> where multiple points with same x-value represent different segments
     */
    fun getNutritionStackedData(): List<ChartPoint> = listOf(
        // Monday (x = 0)
        ChartPoint(x = 0.0, y = 80.0, label = "월"),
        ChartPoint(x = 0.0, y = 45.0, label = "월"),
        ChartPoint(x = 0.0, y = 120.0, label = "월"),

        // Tuesday (x = 1)
        ChartPoint(x = 1.0, y = 75.0, label = "화"),
        ChartPoint(x = 1.0, y = 38.0, label = "화"),
        ChartPoint(x = 1.0, y = 110.0, label = "화"),

        // Wednesday (x = 2)
        ChartPoint(x = 2.0, y = 90.0, label = "수"),
        ChartPoint(x = 2.0, y = 52.0, label = "수"),
        ChartPoint(x = 2.0, y = 140.0, label = "수"),

        // Thursday (x = 3)
        ChartPoint(x = 3.0, y = 85.0, label = "목"),
        ChartPoint(x = 3.0, y = 41.0, label = "목"),
        ChartPoint(x = 3.0, y = 135.0, label = "목"),

        // Friday (x = 4)
        ChartPoint(x = 4.0, y = 95.0, label = "금"),
        ChartPoint(x = 4.0, y = 58.0, label = "금"),
        ChartPoint(x = 4.0, y = 150.0, label = "금"),

        // Saturday (x = 5)
        ChartPoint(x = 5.0, y = 70.0, label = "토"),
        ChartPoint(x = 5.0, y = 35.0, label = "토"),
        ChartPoint(x = 5.0, y = 100.0, label = "토"),

        // Sunday (x = 6)
        ChartPoint(x = 6.0, y = 88.0, label = "일"),
        ChartPoint(x = 6.0, y = 48.0, label = "일"),
        ChartPoint(x = 6.0, y = 125.0, label = "일")
    )

    /**
     * Sample progress data for Apple Watch-style activity rings
     */
    fun getActivityProgressData(): List<ProgressChartPoint> = listOf(
        ProgressChartPoint(x = 0.0, current = 2500.0, max = 2000.0, label = "Move", unit = "KJ"),
        ProgressChartPoint(x = 1.0, current = 20.0, max = 60.0, label = "Exercise", unit = "min"),
        ProgressChartPoint(x = 2.0, current = 7.0, max = 10.0, label = "Stand", unit = "h")
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
     * Convert basic sample data to ChartPoint format
     */
    fun getBasicChartPoints(): List<ChartPoint> = sampleData.mapIndexed { index, value ->
        ChartPoint(x = index.toDouble(), y = value, label = weekDays.getOrElse(index) { "" })
    }

    /**
     * Convert extended sample data to ChartPoint format (for pagination demos)
     */
    fun getExtendedChartPoints(): List<ChartPoint> = sampleData4.mapIndexed { index, value ->
        ChartPoint(x = index.toDouble(), y = value, label = weekDays[index % weekDays.size])
    }

    /**
     * Generate dense chart points for tick reduction demos
     */
    fun getDenseChartPoints(count: Int = 50): List<ChartPoint> {
        val labels = (1..count).map { "Day $it" }
        val values = (1..count).map { (20..80).random().toDouble() }
        return labels.mapIndexed { index, label ->
            ChartPoint(x = index.toDouble(), y = values[index], label = label)
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