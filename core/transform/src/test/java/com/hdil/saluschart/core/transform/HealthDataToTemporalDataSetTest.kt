package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.BloodPressure
import com.hdil.saluschart.data.model.model.HeartRate
import com.hdil.saluschart.data.model.model.HeartRateSample
import com.hdil.saluschart.data.model.model.StepCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class HealthDataToTemporalDataSetTest {

    @Test
    fun `step count is distributed proportionally across minute buckets`() {
        val dataSet = listOf(
            StepCount(
                startTime = Instant.parse("2026-01-01T00:00:30Z"),
                endTime = Instant.parse("2026-01-01T00:02:00Z"),
                stepCount = 90
            )
        ).toStepCountTemporalDataSet()

        assertEquals(TimeUnitGroup.MINUTE, dataSet.timeUnit)
        assertEquals(
            listOf(
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:01:00Z")
            ),
            dataSet.x
        )
        assertEquals(30.0, dataSet.y!![0], 0.0001)
        assertEquals(60.0, dataSet.y!![1], 0.0001)
        assertEquals(90.0, dataSet.y!!.sum(), 0.0001)
    }

    @Test
    fun `heart rate samples are flattened and sorted by sample time`() {
        val dataSet = listOf(
            HeartRate(
                startTime = Instant.parse("2026-01-01T00:00:00Z"),
                endTime = Instant.parse("2026-01-01T00:10:00Z"),
                samples = listOf(
                    HeartRateSample(Instant.parse("2026-01-01T00:03:00Z"), 82),
                    HeartRateSample(Instant.parse("2026-01-01T00:01:00Z"), 76)
                )
            ),
            HeartRate(
                startTime = Instant.parse("2026-01-01T00:10:00Z"),
                endTime = Instant.parse("2026-01-01T00:20:00Z"),
                samples = listOf(
                    HeartRateSample(Instant.parse("2026-01-01T00:02:00Z"), 79)
                )
            )
        ).toHeartRateTemporalDataSet()

        assertEquals(
            listOf(
                Instant.parse("2026-01-01T00:01:00Z"),
                Instant.parse("2026-01-01T00:02:00Z"),
                Instant.parse("2026-01-01T00:03:00Z")
            ),
            dataSet.x
        )
        assertEquals(listOf(76.0, 79.0, 82.0), dataSet.y)
    }

    @Test
    fun `blood pressure preserves per property values`() {
        val dataSet = listOf(
            BloodPressure(
                time = Instant.parse("2026-01-01T07:00:00Z"),
                systolic = 120.0,
                diastolic = 80.0
            ),
            BloodPressure(
                time = Instant.parse("2026-01-01T19:00:00Z"),
                systolic = 125.0,
                diastolic = 78.0
            )
        ).toBloodPressureTemporalDataSet()

        assertTrue(dataSet.isMultiValue)
        assertEquals(TimeUnitGroup.MINUTE, dataSet.timeUnit)
        assertEquals(listOf(120.0, 125.0), dataSet.getValues("systolic"))
        assertEquals(listOf(80.0, 78.0), dataSet.getValues("diastolic"))
    }
}
