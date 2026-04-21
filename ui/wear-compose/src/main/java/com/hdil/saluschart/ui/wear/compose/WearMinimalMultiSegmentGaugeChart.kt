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
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme
import kotlin.math.max
import kotlin.math.min

data class WearMinimalGaugeSegment(
    val fraction: Float,
    val color: Color
)

@Composable
fun WearMinimalMultiSegmentGaugeChart(
    segments: List<WearMinimalGaugeSegment>,
    markerRatio: Float,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 18.dp,
    cornerRadius: Dp = 999.dp,
    markerWidth: Dp = 12.dp,
    markerColor: Color = Color.White
) {
    if (segments.isEmpty()) return
    val safeRatio = markerRatio.coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val height = size.height
        val radius = min(cornerRadius.toPx(), height / 2f)
        val total = segments.sumOf { it.fraction.toDouble() }.toFloat().let { if (it <= 0f) 1f else it }
        val clipPath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = height,
                    cornerRadius = CornerRadius(radius, radius)
                )
            )
        }

        var startX = 0f
        val ranges = mutableListOf<Pair<Float, Float>>()
        clipPath(clipPath) {
            segments.forEach { segment ->
                val width = ((segment.fraction / total).coerceAtLeast(0f)) * size.width
                ranges += startX to (startX + width)
                drawRect(
                    color = segment.color,
                    topLeft = Offset(startX, 0f),
                    size = Size(width, height)
                )
                startX += width
            }
        }

        val centerX = safeRatio * size.width
        val markerHalf = markerWidth.toPx() / 2f
        val targetIndex = ranges.indexOfFirst { centerX <= it.second }.coerceAtLeast(0)
        val currentRange = ranges.getOrElse(targetIndex) { 0f to size.width }
        val markerLeft = (centerX - markerHalf).coerceIn(currentRange.first, currentRange.second - markerWidth.toPx())

        drawRoundRect(
            color = markerColor,
            topLeft = Offset(markerLeft, 0f),
            size = Size(min(markerWidth.toPx(), max(0f, currentRange.second - currentRange.first)), height),
            cornerRadius = CornerRadius(radius, radius)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMinimalMultiSegmentGaugeChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Spring
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMinimalMultiSegmentGaugeChart(
                segments = listOf(
                    WearMinimalGaugeSegment(0.3f, Color(0xFF60A5FA)),
                    WearMinimalGaugeSegment(0.4f, Color(0xFF22C55E)),
                    WearMinimalGaugeSegment(0.3f, Color(0xFFF97316))
                ),
                markerRatio = 0.62f
            )
        }
    }
}
