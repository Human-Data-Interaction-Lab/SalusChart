package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
    pointSize: Dp = 8.dp, // 포인트 크기 (반지름)
    tooltipTextSize: Float = 32f,        // 툴팁 텍스트 크기
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT, // Y축 위치
    interactionType: InteractionType.Scatter = InteractionType.Scatter.POINT,
    chartType: ChartType = ChartType.SCATTERPLOT, // 차트 타입 (툴팁 위치 결정용
    windowSize: Int? = null,             // 윈도우 크기 (null이면 전체 화면)
    maxXTicksLimit: Int? = null,             // X축에 표시할 최대 라벨 개수 (null이면 모든 라벨 표시)
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Red,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null,

    // Fixed y-axis parameters (same as BarChart)
    fixedYAxis: Boolean = false,
    yAxisFixedWidth: Dp = 16.dp,
    autoFixYAxisOnScroll: Boolean = true,
    minY: Float? = null,
    maxY: Float? = null
) {
    if (data.isEmpty()) return

    // windowSize 기반 스크롤 여부 결정
    val useScrolling = windowSize != null && windowSize < data.size
    val isFixedYAxis = if (autoFixYAxisOnScroll) (fixedYAxis || useScrolling) else fixedYAxis
    val scrollState = rememberScrollState()

    // Use the same approach as BarChart and LineChart for x-axis labels
    // Remove duplicate labels while preserving order for scatter plots with multiple points per x-value
    val xLabels = data.map { it.label ?: it.x.toString() }.distinct()
    val yValues = data.map { it.y }

    var canvasPoints by remember { mutableStateOf(listOf<Offset>()) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
    
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints {
            val availableWidth = maxWidth
            val marginHorizontal = 16.dp

            // 스크롤 모드에서 실제 표시할 데이터와 캔버스 너비 계산
            val canvasWidth = if (useScrolling) {
                val chartWidth = availableWidth - (marginHorizontal * 2)
                val sectionsCount = (data.size.toFloat() / windowSize!!.toFloat()).toInt()
                chartWidth * sectionsCount
            } else null

            Row(Modifier.fillMaxSize()) {
                // LEFT fixed axis pane
                if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT && yAxisFixedWidth > 0.dp) {
                    Canvas(
                        modifier = Modifier
                            .width(yAxisFixedWidth)
                            .fillMaxHeight()
                    ) {
                        chartMetrics?.let { m ->
                            ChartDraw.drawYAxisStandalone(
                                drawScope = this,
                                metrics = m,
                                yAxisPosition = yAxisPosition,
                                paneWidthPx = size.width
                            )
                        }
                    }
                }

                // Chart area
                val startPad = if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) 0.dp else marginHorizontal
                val endPad   = if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) 0.dp else marginHorizontal

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .let { if (useScrolling) it.horizontalScroll(scrollState, overscrollEffect = null) else it }
                        .padding(start = startPad, end = endPad)
                ) {
                    Canvas(
                        modifier = if (useScrolling) {
                            Modifier.width(canvasWidth!!).fillMaxHeight()
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ) {
                        val metrics = ChartMath.computeMetrics(
                            size = size,
                            values = yValues,
                            chartType = ChartType.SCATTERPLOT,
                            minY = minY,
                            maxY = maxY,
                            includeYAxisPadding = !isFixedYAxis
                        )
                        val points = ChartMath.Scatter.mapScatterToCanvasPoints(data, size, metrics)

                        canvasPoints = points
                        canvasSize = size
                        chartMetrics = metrics

                        ChartDraw.drawGrid(
                            drawScope = this,
                            size = size,
                            metrics = metrics,
                            yAxisPosition = yAxisPosition,
                            drawLabels = !isFixedYAxis
                        )
                        if (!isFixedYAxis) {
                            ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                        }
                        ChartDraw.Line.drawXAxisLabels(drawContext, xLabels, metrics, maxXTicksLimit = maxXTicksLimit)
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
                        showTooltipForIndex = selectedPointIndex,
                        canvasSize = canvasSize,
                    )
                }

                    // Conditional interaction based on interactionType parameter
                    when (interactionType) {
                        InteractionType.Scatter.POINT -> {
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
                                pointRadius = pointSize,
                                innerRadius = 0.dp,
                                interactive = true,
                                canvasSize = canvasSize,
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
                                showTooltipForIndex = null,
                                pointRadius = pointSize,
                                innerRadius = 0.dp,
                                interactive = false,
                                canvasSize = canvasSize,
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

                // RIGHT fixed axis pane
                if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT && yAxisFixedWidth > 0.dp) {
                    Canvas(
                        modifier = Modifier
                            .width(yAxisFixedWidth)
                            .fillMaxHeight()
                    ) {
                        chartMetrics?.let { m ->
                            ChartDraw.drawYAxisStandalone(
                                drawScope = this,
                                metrics = m,
                                yAxisPosition = yAxisPosition,
                                paneWidthPx = size.width
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}