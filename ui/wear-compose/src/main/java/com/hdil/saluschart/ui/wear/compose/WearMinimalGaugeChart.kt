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
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme

@Composable
fun WearMinimalGaugeChart(
    data: RangeChartMark,
    containerMin: Double,
    containerMax: Double,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White.copy(alpha = 0.12f),
    rangeColor: Color = Color.Unspecified,
    chartHeight: Dp = 16.dp,
    cornerRadiusRatio: Float = 0.9f
) {
    val palette = wearResolvedPalette(emptyList())
    val resolvedRangeColor = if (rangeColor == Color.Unspecified) palette.first() else rangeColor

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val min = data.minPoint.y.coerceIn(containerMin, containerMax)
        val max = data.maxPoint.y.coerceIn(containerMin, containerMax)
        val totalRange = (containerMax - containerMin).takeIf { it > 0.0 } ?: 1.0
        val left = (((min - containerMin) / totalRange) * size.width).toFloat()
        val right = (((max - containerMin) / totalRange) * size.width).toFloat()
        val corner = CornerRadius(size.height * cornerRadiusRatio, size.height * cornerRadiusRatio)

        drawRoundRect(
            color = containerColor,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
            cornerRadius = corner
        )

        drawRoundRect(
            color = resolvedRangeColor,
            topLeft = Offset(left, 0f),
            size = Size((right - left).coerceAtLeast(0f), size.height),
            cornerRadius = corner
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMinimalGaugeChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Sunset
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMinimalGaugeChart(
                data = RangeChartMark(
                    x = 0.0,
                    minPoint = ChartMark(0.0, 58.0),
                    maxPoint = ChartMark(0.0, 104.0)
                ),
                containerMin = 40.0,
                containerMax = 140.0
            )
        }
    }
}
