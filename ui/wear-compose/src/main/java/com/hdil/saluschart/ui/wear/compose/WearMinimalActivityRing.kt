package com.hdil.saluschart.ui.wear.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme
import kotlin.math.min

@Composable
fun WearMinimalActivityRing(
    data: List<ProgressChartMark>,
    modifier: Modifier = Modifier,
    colors: List<Color> = emptyList(),
    strokeWidth: Dp = 9.dp,
    ringGap: Dp = 5.dp,
    chartHeight: Dp = WearChartDefaults.MinimalChartHeight,
    maxLaps: Int = 1,
    trackAlpha: Float = 0.2f
) {
    if (data.isEmpty()) return
    val resolvedColors = wearResolvedPalette(colors)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val strokePx = strokeWidth.toPx()
        val gapPx = ringGap.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = (min(size.width, size.height) / 2f) - strokePx / 2f
        val step = strokePx + gapPx

        data.forEachIndexed { index, mark ->
            val radius = maxRadius - index * step
            if (radius <= 0f) return@forEachIndexed
            val arcSize = Size(radius * 2f, radius * 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val color = resolvedColors.getOrElse(index) { resolvedColors.first() }
            val progress = if (mark.max > 0.0) (mark.current / mark.max).coerceIn(0.0, maxLaps.toDouble()) else 0.0

            drawArc(
                color = color.copy(alpha = trackAlpha),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            if (progress > 0.0) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = (progress * 360.0).toFloat(),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMinimalActivityRingPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Summer
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMinimalActivityRing(
                data = listOf(
                    ProgressChartMark(0.0, 540.0, 700.0),
                    ProgressChartMark(1.0, 26.0, 30.0),
                    ProgressChartMark(2.0, 9.0, 12.0)
                )
            )
        }
    }
}
