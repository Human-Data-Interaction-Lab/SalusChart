package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.SleepStageChartDraw
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.chartMath.SleepStageChartMath
import com.hdil.saluschart.core.chart.chartMath.SleepStageChartMath.toSleepStageRangeChartPoints
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStageType
import com.hdil.saluschart.ui.theme.ChartColor
import java.time.Instant

@Composable
fun SleepStageChart(
    modifier: Modifier = Modifier,
    sleepSession: SleepSession,
    title: String = "Sleep Stage Chart",
    showLabels: Boolean = true,
    onStageClick: ((Int, String) -> Unit)? = null
) {
    if (sleepSession.stages.isEmpty()) return

    Column(modifier = modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        BoxWithConstraints {
            var selectedStageIndex by remember { mutableStateOf<Int?>(null) }
            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Calculate metrics for sleep stage chart
                    val metrics = SleepStageChartMath.computeSleepStageMetrics(size, sleepSession)
                    chartMetrics = metrics

                    // Draw sleep stage labels on Y-axis
                    if (showLabels) {
                        SleepStageChartMath.drawSleepStageLabels(drawContext, metrics)
                    }

                    // Draw grid lines between sleep stages
                    SleepStageChartMath.drawSleepStageGridLines(drawContext, metrics)

                    // Draw X-axis line (time axis)
                    ChartDraw.drawXAxis(this, metrics)
                }

                // Draw horizontal bars for each sleep stage
                chartMetrics?.let { metrics ->
                    // Step 1: Convert SleepStages to RangeChartPoints using sleep stage specific transformation
                    val rangeData = sleepSession.stages.toSleepStageRangeChartPoints()
                    
                    // Step 2: Group by sleep stage type for different colors
                    val stagesByType = sleepSession.stages.groupBy { it.stage }
                    
                    stagesByType.forEach { (stageType, stages) ->
                        val stageColor = getSleepStageColor(stageType)
                        
                        // Filter range data for this sleep stage type
                        val stageRangeData = rangeData.filter { rangePoint ->
                            stages.any { stage -> 
                                stage.stage.ordinal.toFloat() == rangePoint.x
                            }
                        }

                        // Step 3: Draw horizontal bars for this sleep stage type
                        SleepStageChartDraw.HorizontalBarMarker(
                            data = stageRangeData,
                            minValues = stageRangeData.map { it.minPoint.y },
                            maxValues = stageRangeData.map { it.maxPoint.y }, 
                            metrics = metrics,
                            color = stageColor,
                            barHeightRatio = 0.5f,
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
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

/**
 * Get color for each sleep stage type
 */
private fun getSleepStageColor(stageType: SleepStageType): Color {
    return when (stageType) {
        SleepStageType.AWAKE -> Color(0xFFFFD700) // Yellow for awake
        SleepStageType.REM -> Color(0xFF00CED1)   // Dark turquoise for REM
        SleepStageType.LIGHT -> Color(0xFF87CEEB) // Sky blue for light sleep
        SleepStageType.DEEP -> Color(0xFF191970)  // Midnight blue for deep sleep
        SleepStageType.UNKNOWN -> Color.Gray      // Gray for unknown
    }
}