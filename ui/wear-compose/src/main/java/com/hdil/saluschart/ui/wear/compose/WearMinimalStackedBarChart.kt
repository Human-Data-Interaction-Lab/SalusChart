package com.hdil.saluschart.ui.wear.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.StackedChartMark
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme

@Composable
fun WearMinimalStackedBarChart(
    data: List<StackedChartMark>,
    modifier: Modifier = Modifier,
    colors: List<Color> = emptyList(),
    chartHeight: Dp = WearChartDefaults.MinimalChartHeight,
    barWidthRatio: Float = 0.54f,
    showTrack: Boolean = false,
    trackColor: Color = Color.White.copy(alpha = 0.05f)
) {
    if (data.isEmpty()) return
    val palette = wearResolvedPalette(colors)
    val maxTotal = data.maxOfOrNull { it.y }?.takeIf { it > 0.0 } ?: 1.0

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val slotWidth = size.width / data.size
        val barWidth = slotWidth * barWidthRatio
        val corner = CornerRadius(barWidth / 2f, barWidth / 2f)

        data.forEachIndexed { index, mark ->
            val left = slotWidth * index + (slotWidth - barWidth) / 2f
            val clip = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = left,
                        top = 0f,
                        right = left + barWidth,
                        bottom = size.height,
                        cornerRadius = corner
                    )
                )
            }

            if (showTrack) {
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = corner
                )
            }

            clipPath(clip) {
                var currentBottom = size.height
                mark.segments.forEachIndexed { segmentIndex, segment ->
                    val segmentHeight = ((segment.y / maxTotal) * size.height).toFloat().coerceAtLeast(0f)
                    val top = currentBottom - segmentHeight
                    drawRect(
                        color = segment.colorOr(palette.getOrElse(segmentIndex) { palette.first() }),
                        topLeft = Offset(left, top),
                        size = Size(barWidth, segmentHeight)
                    )
                    currentBottom = top
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMinimalStackedBarChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Spring
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMinimalStackedBarChart(
                data = listOf(
                    StackedChartMark(0.0, listOf(ChartMark(0.0, 4.0), ChartMark(0.0, 3.0), ChartMark(0.0, 2.0))),
                    StackedChartMark(1.0, listOf(ChartMark(1.0, 2.0), ChartMark(1.0, 5.0), ChartMark(1.0, 2.5))),
                    StackedChartMark(2.0, listOf(ChartMark(2.0, 3.0), ChartMark(2.0, 3.5), ChartMark(2.0, 3.0)))
                ),
                showTrack = true
            )
        }
    }
}
