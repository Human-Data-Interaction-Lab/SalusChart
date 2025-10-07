package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.toRangeChartMarks

/**
 * 미니멀 범위 바 차트 - 위젯이나 스마트워치 등 작은 화면용
 * 범위 데이터를 컨테이너 범위 내에서 표시하며, 상단에 범위 텍스트 표시
 * 
 * @param modifier 모디파이어
 * @param data 차트 포인트 데이터 (같은 x값을 가진 포인트들이 min/max로 변환됨)
 * @param containerMin 컨테이너의 최소값 (전체 범위 시작)
 * @param containerMax 컨테이너의 최대값 (전체 범위 끝)
 * @param containerColor 컨테이너(배경) 바 색상
 * @param rangeColor 범위 바 색상
 * @param textColor 범위 텍스트 색상
 * @param width 차트 너비
 * @param height 차트 높이
 * @param padding 차트 주변 패딩
 * @param showRangeText 범위 텍스트를 표시할지 여부
 * @param cornerRadius 바의 모서리 둥글기
 */
@Composable
fun MinimalRangeBarChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    color: Color = Color.Blue,
) {
    if (data.isEmpty()) return
    val chartType = ChartType.MINIMAL_RANGE_BAR

    // Transform ChartMarks to RangeChartMarks automatically
    val rangeData = data.toRangeChartMarks(
        minValueSelector = { group -> group.minByOrNull { it.y } ?: group.first() },
        maxValueSelector = { group -> group.maxByOrNull { it.y } ?: group.first() }
    )

    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

    Box(
        Modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val metrics = ChartMath.RangeBar.computeRangeMetrics(size, rangeData)
            chartMetrics = metrics
        }
        chartMetrics?.let { metrics ->
            ChartDraw.Bar.BarMarker(
                data = rangeData,
                minValues = rangeData.map { it.minPoint.y },
                maxValues = rangeData.map { it.maxPoint.y },
                metrics = metrics,
                color = color,
                barWidthRatio = 0.8f,
                interactive = false,
                chartType = chartType,
            )
        }
    }
}
