package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions

/**
 * 미니멀 바 차트 - 위젯이나 스마트워치 등 작은 화면용
 * 축, 그리드, 레이블 없이 순수 바 차트만 표시
 *
 * @param modifier 모디파이어
 * @param data 바 차트 데이터 값들
 * @param color 바 색상
 * @param width 차트 너비
 * @param height 차트 높이
 * @param padding 차트 주변 패딩
 */
@Composable
fun MinimalBarChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    color: Color = Color.Blue,
    padding: Float = 4f,
    barWidthRatio: Float = 0.8f,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Red,
    referenceLineStrokeWidth: Dp = 1.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false, // 미니멀 차트는 기본적으로 레이블 비활성화
    referenceLineLabelFormat: String = "%.0f",
    referenceLineInteractive: Boolean = false
) {
    if (data.isEmpty()) return

    val yValues = data.map { it.y }
    val chartType = ChartType.BAR

    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

    Box(
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val metrics = ChartMath.computeMetrics(
                size = size,
                values = yValues,
                isMinimal = true,
                paddingX = padding,
                paddingY = padding,
                chartType = chartType
            )

            // Store metrics for BarMarker
            chartMetrics = metrics
        }

        // Visual bars (non-interactive) using BarMarker
        chartMetrics?.let { metrics ->
            ChartDraw.Bar.BarMarker(
                data = data,
                minValues = List(yValues.size) { 0.0 },
                maxValues = yValues,
                metrics = metrics,
                color = color,
                barWidthRatio = barWidthRatio,
                interactive = false,
                chartType = chartType,
                barCornerRadiusFraction = barCornerRadiusFraction,
                barCornerRadiusFractions = barCornerRadiusFractions,
                roundTopOnly = roundTopOnly,
            )
        }
        
        // 기준선 표시
        if (referenceLineType != ReferenceLineType.NONE) {
            chartMetrics?.let { metrics ->
                ReferenceLine.ReferenceLine(
                    modifier = Modifier.fillMaxSize(),
                    data = data,
                    metrics = metrics,
                    chartType = chartType,
                    referenceLineType = referenceLineType,
                    color = referenceLineColor,
                    strokeWidth = referenceLineStrokeWidth,
                    lineStyle = referenceLineStyle,
                    showLabel = showReferenceLineLabel,
                    labelFormat = referenceLineLabelFormat,
                    yAxisPosition = YAxisPosition.LEFT, // 미니멀 차트는 기본 왼쪽
                    interactive = referenceLineInteractive
                )
            }
        }
    }
}
