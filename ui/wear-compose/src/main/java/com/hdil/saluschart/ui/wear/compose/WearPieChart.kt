package com.hdil.saluschart.ui.wear.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme

@Composable
fun WearPieChart(
    data: List<ChartMark>,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    colors: List<Color> = emptyList(),
    selectedIndex: Int? = null,
    chartHeight: Dp = WearChartDefaults.SummaryChartHeight,
    ringThicknessRatio: Float = 0.22f,
    chartScaleRatio: Float = 0.82f,
    showCenterLabel: Boolean = true,
    centerLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    centerValueColor: Color = MaterialTheme.colorScheme.onBackground,
    showBreakdown: Boolean = true,
    breakdownItemCount: Int = 3,
    breakdownChipColor: Color = Color.White.copy(alpha = 0.04f)
) {
    if (data.isEmpty()) return
    val resolvedColors = colors.ifEmpty { WearChartDefaults.palette() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val total = remember(data) { data.sumOf { it.y }.takeIf { it > 0.0 } ?: 1.0 }
    val resolvedIndex = selectedIndex ?: data.indices.maxByOrNull { data[it].y } ?: 0
    val selected = data[resolvedIndex.coerceIn(data.indices)]

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(WearChartDefaults.HeaderSpacing)
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = titleColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val strokeWidth = size.minDimension * ringThicknessRatio
                val diameter = size.minDimension * chartScaleRatio
                val topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                data.forEachIndexed { index, mark ->
                    val sweep = ((mark.y / total) * 360f).toFloat()
                    drawArc(
                        color = resolvedColors.getOrElse(index) { primaryColor },
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweep
                }
            }

            if (showCenterLabel) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = selected.label ?: "Selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = centerLabelColor,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${((selected.y / total) * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = centerValueColor
                    )
                }
            }
        }

        if (showBreakdown) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(WearChartDefaults.MicroSpacing)
            ) {
                data.sortedByDescending { it.y }.take(breakdownItemCount).forEach { mark ->
                    val chipColor = resolvedColors.getOrElse(data.indexOf(mark)) { primaryColor }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(breakdownChipColor)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(chipColor)
                            )
                            Text(
                                text = mark.label ?: "Item",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "${((mark.y / total) * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = chipColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearPieChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Spring
    ) {
        androidx.compose.material3.MaterialTheme {
            WearPieChart(
                title = "Sleep mix",
                data = listOf(
                    ChartMark(0.0, 52.0, "Deep"),
                    ChartMark(1.0, 104.0, "Light"),
                    ChartMark(2.0, 28.0, "REM")
                )
            )
        }
    }
}
