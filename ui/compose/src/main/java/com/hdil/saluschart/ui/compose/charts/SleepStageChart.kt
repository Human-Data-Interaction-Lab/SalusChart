package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.chartMath.SleepStageChartMath.toSleepStageRangeChartPoints
import com.hdil.saluschart.data.model.model.SleepSession

@Composable
fun SleepStageChart(
    modifier: Modifier = Modifier,
    sleepSession: SleepSession,
    title: String = "Sleep Stage Chart",
    showLabels: Boolean = true,
    showXAxis: Boolean = false,
    onStageClick: ((Int, String) -> Unit)? = null,
    barHeightRatio: Float = 0.5f,
    fixedYAxis: Boolean = false,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    showTitle: Boolean = true,
    autoFixYAxisOnScroll: Boolean = true
) {
    if (sleepSession.stages.isEmpty()) return
    val chartType = ChartType.SLEEP_STAGE

    // windowSize 기반 스크롤 여부 결정
    val useScrolling = windowSize != null && windowSize < sleepSession.stages.size
    val isFixedYAxis = if (autoFixYAxisOnScroll) (fixedYAxis || useScrolling) else fixedYAxis
    val scrollState = rememberScrollState()

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        BoxWithConstraints {
            val availableWidth = maxWidth
            val marginHorizontal = 16.dp

            // Use the same simple padding logic as BarChart
            val startPad = if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) 0.dp else marginHorizontal
            val endPad = if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) 0.dp else marginHorizontal

            // width taken by the fixed Y-axis pane (left or right)
            val fixedPaneWidth = 4.dp

            // width available to the scrollable chart area (exclude axis pane + inner paddings)
            val contentWidth = availableWidth - fixedPaneWidth - (startPad + endPad)

            // in scroll mode, canvas spans per-window width * data size
            val canvasWidth = if (useScrolling) {
                val pointWidth = contentWidth / windowSize!!
                pointWidth * sleepSession.stages.size
            } else null

            var selectedStageIndex by remember { mutableStateOf<Int?>(null) }
            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

            Row(Modifier.fillMaxSize()) {
                // LEFT fixed Y-axis pane
                if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) {
                    Canvas(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                    ) {
                        chartMetrics?.let { m ->
                            ChartDraw.SleepStage.drawSleepStageYAxisStandalone(
                                drawScope = this,
                                metrics = m,
                                yAxisPosition = yAxisPosition,
                                paneWidthPx = size.width
                            )
                        }
                    }
                }

                // Chart area
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
                        // Calculate metrics for sleep stage chart
                        val metrics = ChartMath.SleepStage.computeSleepStageMetrics(size, sleepSession)
                        chartMetrics = metrics

                        // Draw grid lines between sleep stages
                        ChartDraw.SleepStage.drawSleepStageGridLines(drawContext, metrics)

                        // Draw X-axis line if enabled
                        if (showXAxis) {
                            ChartDraw.drawXAxis(this, metrics)
                        }

                        // Draw X-axis labels (start and end time of SleepSession)
                        ChartDraw.SleepStage.drawSleepStageXAxisLabels(
                            ctx = drawContext,
                            metrics = metrics,
                            startTimeMillis = sleepSession.startTime.toEpochMilli().toDouble(),
                            endTimeMillis = sleepSession.endTime.toEpochMilli().toDouble()
                        )

                        // Draw connecting lines between sleep stages for timeline continuity (Canvas-based)
                        ChartDraw.SleepStage.drawSleepStageConnector(
                            drawScope = this,
                            data = sleepSession.stages.toSleepStageRangeChartPoints(),
                            metrics = metrics,
                            lineColor = Color(0xFFCCCCCC),
                            totalSleepStages = 4,
                            barHeightRatio = barHeightRatio
                        )
                    }

                    // Draw horizontal bars for all sleep stages
                    chartMetrics?.let { metrics ->
                        // Convert SleepStages to RangeChartPoints using sleep stage specific transformation
                        val rangeData = sleepSession.stages.toSleepStageRangeChartPoints()
                        
                        // Draw all horizontal bars with automatic color assignment
                        ChartDraw.SleepStage.HorizontalBarMarker(
                            data = rangeData,
                            minValues = rangeData.map { it.minPoint.y },
                            maxValues = rangeData.map { it.maxPoint.y }, 
                            metrics = metrics,
                            color = Color.Black, // This will be overridden by automatic color assignment
                            barHeightRatio = barHeightRatio,
                            interactive = true,
                            onBarClick = { index, tooltipText ->
                                selectedStageIndex = if (selectedStageIndex == index) null else index
                                onStageClick?.invoke(index, tooltipText)
                            },
                            chartType = ChartType.RANGE_BAR,
                            showTooltipForIndex = selectedStageIndex,
                            unit = ""
                        )
                    }
                }

                // RIGHT fixed Y-axis pane
                if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                    Canvas(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                    ) {
                        chartMetrics?.let { m ->
                            ChartDraw.SleepStage.drawSleepStageYAxisStandalone(
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

        Spacer(Modifier.height(4.dp))
    }
}
