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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.ui.theme.ChartColor

@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<RangeChartPoint>,
    xLabel: String = "Time",
    yLabel: String = "Value", 
    title: String = "Range Bar Chart",
    barColor: androidx.compose.ui.graphics.Color = ChartColor.Default,
    barWidthRatio: Float = 0.6f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT, // Y축 위치
    interactionType: InteractionType.RangeBar = InteractionType.RangeBar.BAR,
    onBarClick: ((Int, RangeChartPoint) -> Unit)? = null,
    chartType: ChartType = ChartType.RANGE_BAR,
    windowSize: Int? = null, // 윈도우 크기 (null이면 전체 화면)
    maxXTicksLimit: Int? = null             // X축에 표시할 최대 라벨 개수 (null이면 모든 라벨 표시)
) {
    if (data.isEmpty()) return

    val useScrolling = windowSize != null && windowSize < data.size
    val scrollState = rememberScrollState()

    Column(modifier = modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        BoxWithConstraints {
            val availableWidth = maxWidth
            val marginHorizontal = 16.dp

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

            val labels = data.map { it.label ?: it.x.toString() }
            var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }


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
                    val metrics = ChartMath.RangeBar.computeRangeMetrics(size, data)
                    chartMetrics = metrics

                    ChartDraw.drawGrid(this, size, metrics, yAxisPosition)
                    ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                    ChartDraw.Bar.drawBarXAxisLabels(drawContext, labels, metrics, maxXTicksLimit = maxXTicksLimit)
                }

                // Conditional interaction based on interactionType parameter
                when (interactionType) {
                    InteractionType.RangeBar.BAR -> {
                        // Interactive range bars
                        chartMetrics?.let { metrics ->
                            ChartDraw.Bar.BarMarker(
                                data = data,
                                minValues = data.map { it.yMin },
                                maxValues = data.map { it.yMax },
                                metrics = metrics,
                                color = barColor,
                                barWidthRatio = barWidthRatio,
                                interactive = true,
                                onBarClick = { index, tooltipText ->
                                    selectedBarIndex = if (selectedBarIndex == index) null else index
                                    onBarClick?.invoke(index, data[index])
                                },
                                chartType = chartType,
                                showTooltipForIndex = selectedBarIndex
                            )
                        }
                    }
                    InteractionType.RangeBar.TOUCH_AREA -> {
                        // Non-interactive range bars
                        chartMetrics?.let { metrics ->
                            ChartDraw.Bar.BarMarker(
                                data = data,
                                minValues = data.map { it.yMin },
                                maxValues = data.map { it.yMax },
                                metrics = metrics,
                                color = barColor,
                                barWidthRatio = barWidthRatio,
                                interactive = false,
                                chartType = chartType,
                                showTooltipForIndex = selectedBarIndex
                            )
                        }

                        chartMetrics?.let { metrics ->
                            ChartDraw.Bar.BarMarker(
                                data = data,
                                minValues = List(data.size) { metrics.minY },
                                maxValues = data.map { it.yMax },
                                metrics = metrics,
                                onBarClick = { index, _ ->
                                    selectedBarIndex = if (selectedBarIndex == index) null else index
                                    onBarClick?.invoke(index, data[index])
                                },
                                chartType = chartType,
                                showTooltipForIndex = selectedBarIndex,
                                isTouchArea = true
                            )
                        }
                    }
                    else -> {
                        // Default case - no interaction
                        chartMetrics?.let { metrics ->
                            ChartDraw.Bar.BarMarker(
                                data = data,
                                minValues = data.map { it.yMin },
                                maxValues = data.map { it.yMax },
                                metrics = metrics,
                                color = barColor,
                                barWidthRatio = barWidthRatio,
                                interactive = false,
                                chartType = chartType,
                                showTooltipForIndex = null
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}
