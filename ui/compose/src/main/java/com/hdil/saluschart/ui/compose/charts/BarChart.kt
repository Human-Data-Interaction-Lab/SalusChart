package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.ScrollableDefaults
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
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.ui.theme.ChartColor

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    data: List<ChartPoint>,      // ChartPoint 기반
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Bar Chart Example",
    barColor: androidx.compose.ui.graphics.Color = ChartColor.Default,
    minY: Float? = null,                    // 사용자 지정 최소 Y값
    maxY: Float? = null,                    // 사용자 지정 최대 Y값
    barWidthRatio: Float = 0.8f,       // 바 너비 배수
    labelTextSize: Float = 28f,             // X축 레이블 텍스트 크기
    tooltipTextSize: Float = 32f,           // 툴팁 텍스트 크기
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,  // Y축 위치
    interactionType: InteractionType.Bar = InteractionType.BAR, // 상호작용 타입
    onBarClick: ((Int, Float) -> Unit)? = null,  // 바 클릭 콜백
    showLabel: Boolean = false,
    windowSize: Int? = null, // 윈도우 크기 (null이면 전체 화면)
    chartType: ChartType = ChartType.BAR, // 차트 타입 (툴팁 위치 결정용)
    maxXTicksLimit: Int? = null,             // X축에 표시할 최대 라벨 개수 (null이면 모든 라벨 표시)
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Black,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null
) {
    if (data.isEmpty()) return

    val useScrolling  = windowSize != null && windowSize < data.size
    val scrollState = rememberScrollState()

    Column(modifier = modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        BoxWithConstraints {
            val availableWidth = maxWidth // 전체 사용 가능한 너비
            val marginHorizontal = 16.dp

            // 스크롤 모드에서 실제 표시할 데이터와 캔버스 너비 계산
            val canvasWidth = if(useScrolling) {
                // 스크롤 모드: 좌우 마진을 고려한 실제 차트 너비 계산
                val chartWidth = availableWidth - (marginHorizontal * 2) // 좌우 마진 제외
                val sectionsCount = (data.size.toFloat() / windowSize!!.toFloat()).toInt()
                val totalWidth = chartWidth * sectionsCount
                totalWidth
            } else {
                // 일반 모드: 전체 데이터를 화면에 맞춤
                null
            }

            val xLabels = data.map { it.label ?: it.x.toString() }
            val yValues = data.map { it.y }

            var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
            var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

            Box(
                modifier = if (useScrolling) {
                    Modifier
                        .horizontalScroll(
                            scrollState,
                            overscrollEffect = null
                        )
                        .padding(horizontal = marginHorizontal)
                } else {
                    modifier
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
                        values = yValues,
                        chartType = ChartType.BAR,
                        minY = minY,
                        maxY = maxY
                    )

                    // Store metrics and canvas size for InteractiveBars
                    canvasSize = size
                    chartMetrics = metrics

                    ChartDraw.drawGrid(this, size, metrics, yAxisPosition)
                    ChartDraw.drawXAxis(this, metrics)
                    ChartDraw.drawYAxis(this, metrics, yAxisPosition)

                    ChartDraw.Bar.drawBarXAxisLabels(
                        ctx = drawContext,
                        labels = xLabels,
                        metrics = metrics,
                        textSize = labelTextSize,
                        maxXTicksLimit = maxXTicksLimit
                    )
                }

                // Conditional interaction based on interactionType parameter
                when (interactionType) {
                    InteractionType.TOUCH_AREA -> {
                        // Visual bars (non-interactive)
                        chartMetrics?.let { metrics ->
                            ChartDraw.Bar.BarMarker(
                                data = data,
                                minValues = List(yValues.size) { metrics.minY },
                                maxValues = yValues,
                                metrics = metrics,
                                color = barColor,
                                barWidthRatio = barWidthRatio,
                                interactive = false,
                                chartType = ChartType.BAR,
                                showTooltipForIndex = selectedBarIndex
                            )
                        }

                        // Interactive bars overlay (transparent bars for easier touching)
                        chartMetrics?.let { metrics ->
                            ChartDraw.Bar.BarMarker(
                                data = data,
                                minValues = List(yValues.size) { metrics.minY },
                                maxValues = yValues,
                                metrics = metrics,
                                onBarClick = { index, tooltipText ->
                                    selectedBarIndex =
                                        if (selectedBarIndex == index) null else index
                                    onBarClick?.invoke(index, tooltipText.toFloat())
                                },
                                chartType = chartType,
                                showTooltipForIndex = selectedBarIndex,
                                isTouchArea = true
                            )
                        }
                    }

                    InteractionType.BAR -> {
                        // Interactive visual bars (direct bar touching)
                        chartMetrics?.let { metrics ->
                            ChartDraw.Bar.BarMarker(
                                data = data,
                                minValues = List(yValues.size) { metrics.minY },
                                maxValues = yValues,
                                metrics = metrics,
                                color = barColor,
                                barWidthRatio = barWidthRatio,
                                interactive = true,
                                onBarClick = { index, tooltipText ->
                                    onBarClick?.invoke(index, tooltipText.toFloat())
                                },
                                chartType = chartType,
                                showLabel = showLabel
                            )
                        }
                    }

                    else -> {
                        // Visual bars (non-interactive)
                        chartMetrics?.let { metrics ->
                            ChartDraw.Bar.BarMarker(
                                data = data,
                                minValues = List(yValues.size) { metrics.minY },
                                maxValues = yValues,
                                metrics = metrics,
                                color = barColor,
                                barWidthRatio = barWidthRatio,
                                interactive = false,
                                chartType = chartType,
                                showTooltipForIndex = selectedBarIndex
                            )
                        }
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
