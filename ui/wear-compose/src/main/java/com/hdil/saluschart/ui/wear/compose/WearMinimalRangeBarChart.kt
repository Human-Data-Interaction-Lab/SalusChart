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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme
import kotlin.math.max

@Composable
fun WearMinimalRangeBarChart(
    data: List<RangeChartMark>,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    chartHeight: Dp = WearChartDefaults.MinimalChartHeight,
    barWidthRatio: Float = 0.42f,
    trackColor: Color = Color.White.copy(alpha = 0.05f),
    showTrack: Boolean = true
) {
    if (data.isEmpty()) return
    val palette = wearResolvedPalette(emptyList())
    val resolvedColor = if (color == Color.Unspecified) palette.first() else color
    val minValue = data.minOf { it.minPoint.y }
    val maxValue = data.maxOf { it.maxPoint.y }.takeIf { it > minValue } ?: (minValue + 1.0)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val slotWidth = size.width / max(1, data.size)
        val barWidth = slotWidth * barWidthRatio
        val corner = CornerRadius(barWidth / 2f, barWidth / 2f)

        data.forEachIndexed { index, mark ->
            val left = slotWidth * index + (slotWidth - barWidth) / 2f
            val top = (1f - normalizeY(mark.maxPoint.y, minValue, maxValue)) * size.height
            val bottom = (1f - normalizeY(mark.minPoint.y, minValue, maxValue)) * size.height
            val height = (bottom - top).coerceAtLeast(size.height * 0.05f)

            if (showTrack) {
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = corner
                )
            }

            drawRoundRect(
                color = resolvedColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, height),
                cornerRadius = corner
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMinimalRangeBarChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Sunset
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMinimalRangeBarChart(
                data = listOf(
                    RangeChartMark(0.0, ChartMark(0.0, 58.0), ChartMark(0.0, 114.0)),
                    RangeChartMark(1.0, ChartMark(1.0, 62.0), ChartMark(1.0, 122.0)),
                    RangeChartMark(2.0, ChartMark(2.0, 60.0), ChartMark(2.0, 118.0)),
                    RangeChartMark(3.0, ChartMark(3.0, 64.0), ChartMark(3.0, 126.0))
                )
            )
        }
    }
}
