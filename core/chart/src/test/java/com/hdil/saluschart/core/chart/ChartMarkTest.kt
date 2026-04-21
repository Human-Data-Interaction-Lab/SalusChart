package com.hdil.saluschart.core.chart

import org.junit.Assert.assertEquals
import org.junit.Test

class ChartMarkTest {

    @Test
    fun `range chart mark exposes span as y`() {
        val mark = RangeChartMark(
            x = 0.0,
            minPoint = ChartMark(x = 0.0, y = 55.0, label = "low"),
            maxPoint = ChartMark(x = 0.0, y = 145.0, label = "high"),
            label = "range"
        )

        assertEquals(90.0, mark.y, 0.0)
    }

    @Test
    fun `stacked chart mark sums segment values`() {
        val mark = StackedChartMark(
            x = 0.0,
            segments = listOf(
                ChartMark(x = 0.0, y = 10.0),
                ChartMark(x = 0.0, y = 20.5),
                ChartMark(x = 0.0, y = 4.5)
            )
        )

        assertEquals(35.0, mark.y, 0.0)
    }

    @Test
    fun `progress chart mark clamps normalized progress`() {
        val over = ProgressChartMark(x = 0.0, current = 15.0, max = 10.0, label = "goal")
        val under = ProgressChartMark(x = 1.0, current = -2.0, max = 10.0, label = "goal")
        val zeroMax = ProgressChartMark(x = 2.0, current = 5.0, max = 0.0, label = "goal")

        assertEquals(1.0, over.progress, 0.0)
        assertEquals(100.0, over.percentage, 0.0)
        assertEquals(0.0, under.progress, 0.0)
        assertEquals(0.0, zeroMax.progress, 0.0)
        assertEquals(over.progress, over.y, 0.0)
    }
}
