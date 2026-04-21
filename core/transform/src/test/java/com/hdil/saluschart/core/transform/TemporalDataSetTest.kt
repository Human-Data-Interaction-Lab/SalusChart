package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TemporalDataSetTest {

    @Test
    fun `single value dataset exposes single value helpers`() {
        val x = listOf(
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-01-01T01:00:00Z")
        )
        val dataSet = TemporalDataSet(
            x = x,
            y = listOf(10.0, 20.0),
            timeUnit = TimeUnitGroup.HOUR
        )

        assertTrue(dataSet.isSingleValue)
        assertFalse(dataSet.isMultiValue)
        assertEquals(listOf(10.0, 20.0), dataSet.values)
        assertTrue(dataSet.propertyNames.isEmpty())
        assertNull(dataSet.getValues("anything"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `dataset requires exactly one value source`() {
        TemporalDataSet(
            x = listOf(Instant.parse("2026-01-01T00:00:00Z")),
            y = listOf(1.0),
            yMultiple = mapOf("min" to listOf(1.0))
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `dataset rejects size mismatch`() {
        TemporalDataSet(
            x = listOf(Instant.parse("2026-01-01T00:00:00Z")),
            y = listOf(1.0, 2.0)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `dataset rejects empty multi value map`() {
        TemporalDataSet(
            x = listOf(Instant.parse("2026-01-01T00:00:00Z")),
            yMultiple = emptyMap()
        )
    }
}
