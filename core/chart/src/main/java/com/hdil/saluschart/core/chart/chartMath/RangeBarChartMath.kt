package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath.ChartMetrics

object RangeBarChartMath {
    /**
     * 범위 차트 그리기에 필요한 메트릭 값을 계산합니다.
     *
     * @param size Canvas의 전체 크기
     * @param data 범위 차트 데이터 포인트 목록
     * @param tickCount 원하는 Y축 눈금 개수 (기본값: 5)
     * @param minY 사용자 지정 최소 Y값 (null이면 데이터 기반)
     * @param maxY 사용자 지정 최대 Y값 (null이면 데이터 기반)
     * @param fixedTickStep 고정 눈금 간격 (null이면 자동 계산)
     * @return 차트 메트릭 객체
     */
    fun computeRangeMetrics(
        size: Size,
        data: List<RangeChartMark>,
        tickCount: Int = 5,
        minY: Double? = null,
        maxY: Double? = null,
        fixedTickStep: Double? = null
    ): ChartMetrics {
        val paddingX = 30f
        val paddingY = 40f
        val chartWidth = size.width - paddingX * 2
        val chartHeight = size.height - paddingY

        // Extract all Y values from range data
        val allYValues = data.flatMap { listOf(it.minPoint.y, it.maxPoint.y) }

        // Compute Y-axis range using the standardized function
        val yAxisRange = ChartMath.computeYAxisRange(
            values = allYValues,
            chartType = ChartType.RANGE_BAR,
            minY = minY,
            maxY = maxY,
            fixedTickStep = fixedTickStep,
            tickCount = tickCount
        )

        return ChartMetrics(
            paddingX = paddingX,
            paddingY = paddingY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            yAxisRange = yAxisRange
        )
    }
}