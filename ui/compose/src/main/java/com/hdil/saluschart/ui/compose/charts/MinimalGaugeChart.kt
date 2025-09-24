package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw

/**
 * 미니멀 범위 바 차트 - 위젯이나 스마트워치 등 작은 화면용
 * 범위 데이터를 컨테이너 범위 내에서 표시하며, 상단에 범위 텍스트 표시
 * 
 * @param modifier 모디파이어
 * @param data 범위 차트 데이터 (yMin, yMax 포함)
 * @param containerMin 컨테이너의 최소값 (전체 범위 시작)
 * @param containerMax 컨테이너의 최대값 (전체 범위 끝)
 * @param containerColor 컨테이너(배경) 바 색상
 * @param rangeColor 범위 바 색상
 * @param textColor 범위 텍스트 색상
 * @param showRangeText 범위 텍스트를 표시할지 여부
 * @param chartType 차트 타입 (사용되지 않음, 호환성을 위해 유지)
 */
@Composable
fun MinimalGaugeChart(
    modifier: Modifier = Modifier,
    data: RangeChartPoint,
    containerMin: Double,
    containerMax: Double,
    containerColor: Color = Color.LightGray,
    rangeColor: Color = Color(0xFFFF9500),
    textColor: Color = Color.Black,
    showRangeText: Boolean = true,
    chartType: ChartType = ChartType.MINIMAL_GAUGE
) {
    // 데이터 범위가 컨테이너 범위를 벗어나지 않도록 클램핑
    val clampedDataMin = data.minPoint.y.coerceIn(containerMin, containerMax)
    val clampedDataMax = data.maxPoint.y.coerceIn(containerMin, containerMax)

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 범위 텍스트 표시
        if (showRangeText) {
            ChartDraw.Gauge.RangeText(
                dataMin = clampedDataMin.toFloat(),
                dataMax = clampedDataMax.toFloat(),
                textColor = textColor
            )
        }

        // 게이지 바 컴포저블
        ChartDraw.Gauge.GaugeBar(
            dataMin = clampedDataMin.toFloat(),
            dataMax = clampedDataMax.toFloat(),
            containerMin = containerMin.toFloat(),
            containerMax = containerMax.toFloat(),
            containerColor = containerColor,
            rangeColor = rangeColor
        )
    }
}
