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
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme

@Composable
fun WearMinimalBarChart(
    data: List<ChartMark>,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    chartHeight: Dp = WearChartDefaults.MinimalChartHeight,
    barWidthRatio: Float = 0.6f,
    cornerRadiusRatio: Float = 0.45f,
    minBarHeightRatio: Float = 0.08f,
    trackColor: Color? = null
) {
    if (data.isEmpty()) return
    val palette = wearResolvedPalette(emptyList())
    val resolvedColor = if (color == Color.Unspecified) palette.first() else color

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val maxY = positiveOrOne(data.safeMaxY())
        val slotWidth = size.width / data.size
        val barWidth = slotWidth * barWidthRatio
        val corner = CornerRadius(barWidth * cornerRadiusRatio, barWidth * cornerRadiusRatio)

        data.forEachIndexed { index, mark ->
            val left = slotWidth * index + (slotWidth - barWidth) / 2f
            val normalized = (mark.y / maxY).toFloat().coerceIn(0f, 1f)
            val height = (normalized.coerceAtLeast(minBarHeightRatio) * size.height).coerceAtMost(size.height)
            val top = size.height - height

            if (trackColor != null) {
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = corner
                )
            }

            drawRoundRect(
                color = mark.colorOr(resolvedColor),
                topLeft = Offset(left, top),
                size = Size(barWidth, height),
                cornerRadius = corner
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMinimalBarChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Spring
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMinimalBarChart(
                data = listOf(
                    ChartMark(0.0, 5.0),
                    ChartMark(1.0, 8.0),
                    ChartMark(2.0, 4.0),
                    ChartMark(3.0, 10.0),
                    ChartMark(4.0, 7.0)
                ),
                trackColor = Color.White.copy(alpha = 0.05f)
            )
        }
    }
}
