package com.hdil.saluschart.ui.wear.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme

@Composable
fun WearMinimalLineChart(
    data: List<ChartMark>,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    strokeWidth: Dp = 2.5.dp,
    chartHeight: Dp = WearChartDefaults.MinimalChartHeight,
    paddingRatio: Float = 0.1f,
    showPoints: Boolean = false,
    pointColor: Color = Color.Unspecified,
    pointRadius: Dp = 3.dp,
    smooth: Boolean = true,
    trackColor: Color? = null
) {
    if (data.isEmpty()) return
    val palette = wearResolvedPalette(emptyList())
    val lineColor = if (color == Color.Unspecified) data.first().colorOr(palette.first()) else color
    val resolvedPointColor = if (pointColor == Color.Unspecified) lineColor else pointColor

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val minY = data.safeMinY()
        val maxY = data.safeMaxY()
        val horizontalPadding = size.width * paddingRatio
        val verticalPadding = size.height * paddingRatio
        val usableWidth = (size.width - horizontalPadding * 2).coerceAtLeast(1f)
        val usableHeight = (size.height - verticalPadding * 2).coerceAtLeast(1f)
        val stepX = if (data.size == 1) 0f else usableWidth / (data.lastIndex.toFloat())

        val points = data.mapIndexed { index, mark ->
            val x = horizontalPadding + stepX * index
            val y = verticalPadding + (1f - normalizeY(mark.y, minY, maxY)) * usableHeight
            Offset(x, y)
        }

        if (trackColor != null) {
            drawLine(
                color = trackColor,
                start = Offset(horizontalPadding, size.height - verticalPadding),
                end = Offset(size.width - horizontalPadding, size.height - verticalPadding),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.cornerPathEffect(8f)
            )
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            if (smooth && points.size > 2) {
                for (i in 1 until points.size) {
                    val previous = points[i - 1]
                    val current = points[i]
                    val midX = (previous.x + current.x) / 2f
                    cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
                }
            } else {
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )

        if (showPoints) {
            val radius = pointRadius.toPx()
            points.forEach { point ->
                drawCircle(color = resolvedPointColor, radius = radius, center = point)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMinimalLineChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Summer
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMinimalLineChart(
                data = listOf(
                    ChartMark(0.0, 72.0),
                    ChartMark(1.0, 80.0),
                    ChartMark(2.0, 76.0),
                    ChartMark(3.0, 92.0),
                    ChartMark(4.0, 84.0)
                ),
                showPoints = true
            )
        }
    }
}
