package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.chartMath.SleepStageChartMath.toSleepStageRangeChartMarks
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
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showStartEndLabels: Boolean = true,
    xLabelAutoSkip: Boolean = true,
    yAxisFixedWidth: Dp = 0.dp
) {
    if (sleepSession.stages.isEmpty()) return

    var selectedStageIndex by remember { mutableStateOf<Int?>(null) }
    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        Row(Modifier.fillMaxSize()) {
            // LEFT Y-axis pane (always shown if showYAxis=true)
            if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                Canvas(
                    modifier = Modifier
                        .width(yAxisFixedWidth)
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
            Box(modifier = Modifier.weight(1f)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
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
                        endTimeMillis = sleepSession.endTime.toEpochMilli().toDouble(),
                        showStartEndLabels = showStartEndLabels,
                        xLabelAutoSkip = xLabelAutoSkip
                    )

                    // Draw connecting lines between sleep stages for timeline continuity (Canvas-based)
                    ChartDraw.SleepStage.drawSleepStageConnector(
                        drawScope = this,
                        data = sleepSession.stages.toSleepStageRangeChartMarks(),
                        metrics = metrics,
                        lineColor = Color(0xFFCCCCCC),
                        totalSleepStages = 4,
                        barHeightRatio = barHeightRatio
                    )
                }

                // Draw horizontal bars for all sleep stages
                chartMetrics?.let { metrics ->
                    // Convert SleepStages to RangeChartMarks using sleep stage specific transformation
                    val rangeData = sleepSession.stages.toSleepStageRangeChartMarks()
                    
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

            // RIGHT Y-axis pane (always shown if showYAxis=true)
            if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                Canvas(
                    modifier = Modifier
                        .width(yAxisFixedWidth)
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

        Spacer(Modifier.height(4.dp))
    }
}
