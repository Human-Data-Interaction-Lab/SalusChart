package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.chartMath.ChartMath

object RangeBarChartDraw {

//    /**
//     * 범위 바 차트의 막대들을 그립니다.
//     * 각 막대는 yMin에서 yMax까지의 범위를 표시합니다.
//     *
//     * @param drawScope 그리기 영역
//     * @param data 범위 차트 데이터 포인트 목록
//     * @param metrics 차트 메트릭 정보
//     * @param color 바 색상
//     * @param barWidthRatio 바 너비 비율 (0.0 ~ 1.0, 기본값 0.6)
//     */
//    fun drawRangeBars(
//        drawScope: DrawScope,
//        data: List<RangeChartMark>,
//        metrics: ChartMath.ChartMetrics,
//        color: Color,
//        barWidthRatio: Float = 0.6f
//    ) {
//        val barWidth = (metrics.chartWidth / data.size) * barWidthRatio
//        val spacing = metrics.chartWidth / data.size
//
//        data.forEachIndexed { i, rangePoint ->
//            val yMinScreen = metrics.chartHeight - ((rangePoint.yMin - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
//            val yMaxScreen = metrics.chartHeight - ((rangePoint.yMax - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
//
//            val barHeight = yMinScreen - yMaxScreen // 범위의 높이
//            val barX = metrics.paddingX + (spacing - barWidth) / 2 + i * spacing
//
//            drawScope.drawRect(
//                color = color,
//                topLeft = Offset(barX, yMaxScreen),
//                size = Size(barWidth, barHeight)
//            )
//        }
//    }
}