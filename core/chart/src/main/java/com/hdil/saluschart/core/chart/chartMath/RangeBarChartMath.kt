package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.chartMath.ChartMath.ChartMetrics
import com.hdil.saluschart.core.chart.chartMath.ChartMath.computeNiceTicks

object RangeBarChartMath {
    /**
     * 범위 차트 그리기에 필요한 메트릭 값을 계산합니다.
     *
     * @param size Canvas의 전체 크기
     * @param data 범위 차트 데이터 포인트 목록
     * @param tickCount 원하는 Y축 눈금 개수 (기본값: 5)
     * @return 차트 메트릭 객체
     */
    fun computeRangeMetrics(size: Size, data: List<RangeChartPoint>, tickCount: Int = 5): ChartMetrics {
        val paddingX = 30f
        val paddingY = 40f
        val chartWidth = size.width - paddingX * 2
        val chartHeight = size.height - paddingY

        val allYValues = data.flatMap { listOf(it.minPoint.y, it.maxPoint.y) }
        val dataMax = allYValues.maxOrNull() ?: 1f
        val dataMin = allYValues.minOrNull() ?: 0f

        val yTicks = computeNiceTicks(dataMin, dataMax, tickCount, ChartType.RANGE_BAR)

        val actualMinY = yTicks.minOrNull() ?: dataMin
        val actualMaxY = yTicks.maxOrNull() ?: dataMax

        return ChartMetrics(paddingX, paddingY, chartWidth, chartHeight, actualMinY, actualMaxY, yTicks)
    }
}