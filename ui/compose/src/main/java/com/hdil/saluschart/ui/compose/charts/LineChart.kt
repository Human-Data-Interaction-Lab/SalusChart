package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.ui.theme.ChartColor

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: List<ChartPoint>,      // ChartPoint 기반
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Line Chart Example",
    lineColor: Color = ChartColor.Default,
    strokeWidth: Float = 4f,
    minY: Float? = null,                    // 사용자 지정 최소 Y값
    maxY: Float? = null,                    // 사용자 지정 최대 Y값
    labelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,  // Y축 위치
    interactionType: InteractionType.Line = InteractionType.Line.POINT,
    showPoint: Boolean = false, // 포인트 표시 여부
    pointRadius: Pair<Dp, Dp> = Pair(4.dp, 2.dp), // 포인트 외부 반지름, 내부 반지름
    showValue: Boolean = false, // 값 표시 여부
    showLegend: Boolean = false,
    windowSize: Int? = null, // 윈도우 크기 (null이면 전체 화면)
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    chartType : ChartType = ChartType.LINE, // 차트 타입 (툴팁 위치 결정용
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

    // windowSize 기반 스크롤 여부 결정
    val useScrolling = windowSize != null && windowSize < data.size
    val scrollState = rememberScrollState()

    Column(modifier = modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        BoxWithConstraints {
            val availableWidth = maxWidth // 전체 사용 가능한 너비
            val marginHorizontal = 16.dp // 좌우 마진

            // 스크롤 모드에서 실제 표시할 데이터와 캔버스 너비 계산
            val canvasWidth = if (useScrolling) {
                // 포인트당 width = (availableWidth - margin*2) / windowSize
                val pointWidth = (availableWidth - (marginHorizontal * 2)) / windowSize!!
                // 전체 캔버스 width = 포인트당 width * 데이터 개수 + margin*2
                val totalWidth = pointWidth * data.size + (marginHorizontal * 2)
                totalWidth
            } else {
                // 일반 모드: 전체 데이터를 화면에 맞춤
                null
            }

            val xLabels = data.map { it.x }
            val yValues = data.map { it.y }

            var canvasPoints by remember { mutableStateOf(listOf<androidx.compose.ui.geometry.Offset>()) }
            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
            var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

            Box(
                modifier = if (useScrolling) {
                    Modifier
                        .horizontalScroll(
                            scrollState,
                            overscrollEffect = null
                        ) // fling 제거
                        .padding(horizontal = marginHorizontal) // 좌우 마진 추가
                } else {
                    Modifier
                }
            ) {
                Canvas(
                    modifier = if (useScrolling) {
                        Modifier
                            .width(canvasWidth!!) // 계산된 캔버스 너비 사용
                            .fillMaxHeight()
                    } else {
                        Modifier.fillMaxSize()
                    }
                ) {
                    val metrics = ChartMath.computeMetrics(
                        size = size,
                        values = yValues, // 전체 데이터 기준으로 Y축 스케일 계산
                        chartType = ChartType.BAR,
                        minY = minY,
                        maxY = maxY
                    )

                    val points = ChartMath.mapToCanvasPoints(data, size, metrics)

                    // 포인트 위치와 차트 메트릭스를 상태 변수에 저장
                    canvasPoints = points
                    chartMetrics = metrics

                    ChartDraw.drawGrid(this, size, metrics, yAxisPosition)
                    ChartDraw.Line.drawLine(this, points, lineColor, strokeWidth)
                    ChartDraw.Line.drawXAxisLabels(
                        ctx = drawContext,
                        labels = xLabels.map { it.toString() },
                        metrics = metrics,
                        textSize = labelTextSize,
                        maxXTicksLimit = maxXTicksLimit
                    )
                }

                // Conditional interaction based on interactionType parameter
                when (interactionType) {
                    InteractionType.Line.TOUCH_AREA -> {
                        // BarMarker interactions (invisible bars for easier touching)
                        chartMetrics?.let { metrics ->
                            ChartDraw.Bar.BarMarker(
                                data = data,
                                minValues = List(yValues.size) { metrics.minY },
                                maxValues = yValues,
                                metrics = metrics,
                                useLineChartPositioning = true,
                                onBarClick = { index, _ ->
                                    selectedPointIndex =
                                        if (selectedPointIndex == index) null else index
                                },
                                isTouchArea = true,
                                chartType = chartType,
                                showTooltipForIndex = selectedPointIndex
                            )
                        }
                        ChartDraw.Scatter.PointMarker(
                            data = data,
                            points = canvasPoints,
                            values = yValues,
                            color = lineColor,
                            showPoint = showPoint,
                            selectedPointIndex = selectedPointIndex,
                            onPointClick = null,
                            pointRadius = pointRadius.first,
                            innerRadius = pointRadius.second,
                            interactive = false,
                            chartType = chartType,
                            showValue = showValue,
                            showTooltipForIndex = selectedPointIndex
                        )
                    }

                    InteractionType.Line.POINT -> {
                        // PointMarker interactions (interactive data points)
                        ChartDraw.Scatter.PointMarker(
                            data = data,
                            points = canvasPoints,
                            values = yValues,
                            color = lineColor,
                            showPoint = showPoint,
                            selectedPointIndex = selectedPointIndex,
                            onPointClick = { index ->
                                // 이미 선택된 포인트를 다시 클릭하면 선택 해제(null로 설정)
                                selectedPointIndex =
                                    if (selectedPointIndex == index) null else index
                            },
                            pointRadius = pointRadius.first,
                            innerRadius = pointRadius.second,
                            interactive = true,
                            chartType = chartType,
                            showValue = showValue,
                            showTooltipForIndex = selectedPointIndex
                        )
                    }

                    else -> {
                        // Non-interactive rendering
                        ChartDraw.Scatter.PointMarker(
                            data = data,
                            points = canvasPoints,
                            values = yValues,
                            color = lineColor,
                            selectedPointIndex = selectedPointIndex,
                            onPointClick = null,
                            pointRadius = pointRadius.first,
                            innerRadius = pointRadius.second,
                            interactive = false,
                            showValue = showValue,
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
        }

        Spacer(Modifier.height(4.dp))
    }
}