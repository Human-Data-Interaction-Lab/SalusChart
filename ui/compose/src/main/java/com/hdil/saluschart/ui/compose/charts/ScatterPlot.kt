package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.PointType
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import androidx.compose.ui.unit.Dp

@Composable
fun ScatterPlot(
    modifier: Modifier = Modifier,
    data: List<ChartPoint>,
    xLabel: String = "X Axis",
    yLabel: String = "Y Axis",
    title: String = "Scatter Plot Example",
    pointColor: Color = com.hdil.saluschart.ui.theme.ChartColor.Default,
    pointType: PointType = PointType.Circle, // 포인트 타입 (Circle, Square, Triangle 등)
    tooltipTextSize: Float = 32f,        // 툴팁 텍스트 크기
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT, // Y축 위치
    interactionType: InteractionType = InteractionType.POINT,
    chartType: ChartType = ChartType.SCATTERPLOT, // 차트 타입 (툴팁 위치 결정용
    maxXTicksLimit: Int? = null,             // X축에 표시할 최대 라벨 개수 (null이면 모든 라벨 표시)
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Red,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null
) {
    if (data.isEmpty()) return

    val xLabels = data.map { it.x }
    val yValues = data.map { it.y }

    var canvasPoints by remember { mutableStateOf(listOf<Offset>()) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
    
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            Modifier
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val metrics = ChartMath.computeMetrics(size, yValues)
                val points = ChartMath.mapToCanvasPoints(data, size, metrics)

                canvasPoints = points
                canvasSize = size
                chartMetrics = metrics

                ChartDraw.drawGrid(this, size, metrics, yAxisPosition)
                ChartDraw.Line.drawXAxisLabels(drawContext, xLabels.map { it.toString() }, metrics, maxXTicksLimit = maxXTicksLimit)
            }

            // Conditional interaction based on interactionType parameter
            when (interactionType) {
                InteractionType.TOUCH_AREA -> {
                    // BarMarker interactions (invisible bars for easier touching)
                    chartMetrics?.let { metrics ->
                        ChartDraw.Bar.BarMarker(
                            data = data,
                            minValues = List(yValues.size) { metrics.minY },
                            maxValues = yValues,
                            metrics = metrics,
                            useLineChartPositioning = true,
                            onBarClick = { index, tooltipText ->
                                selectedPointIndex =
                                    if (selectedPointIndex == index) null else index
                            },
                            chartType = chartType,
                            isTouchArea = true
                        )
                    }
                    ChartDraw.Scatter.PointMarker(
                        data = data,
                        points = canvasPoints,
                        values = yValues,
                        selectedPointIndex = selectedPointIndex,
                        onPointClick = null, // No point interaction in this mode
                        pointType = pointType,
                        interactive = false, // Visual only, no interactions
                        chartType = chartType,
                        showTooltipForIndex = selectedPointIndex
                    )
                }

                InteractionType.POINT -> {
                    // PointMarker interactions (direct point touching)
                    ChartDraw.Scatter.PointMarker(
                        data = data,
                        points = canvasPoints,
                        values = yValues,
                        color = pointColor,
                        selectedPointIndex = selectedPointIndex,
                        onPointClick = { index ->
                            // Handle point click - toggle selection
                            selectedPointIndex = if (selectedPointIndex == index) null else index
                        },
                        pointType = pointType,
                        chartType = chartType,
                        showTooltipForIndex = selectedPointIndex,
                        pointRadius = 8.dp,
                        innerRadius = 0.dp,
                        interactive = true
                    )
                }

                else -> {
                    // Default to non-interactive rendering
                    ChartDraw.Scatter.PointMarker(
                        data = data,
                        points = canvasPoints,
                        values = yValues,
                        selectedPointIndex = null, // No selection in non-interactive mode
                        onPointClick = null,
                        pointType = pointType,
                        chartType = chartType,
                        showTooltipForIndex = null
                    )
                }
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
                        yAxisPosition = yAxisPosition,
                        interactive = referenceLineInteractive,
                        onClick = onReferenceLineClick
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}