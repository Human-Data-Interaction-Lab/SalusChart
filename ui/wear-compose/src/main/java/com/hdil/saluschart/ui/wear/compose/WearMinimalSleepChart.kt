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
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme
import kotlin.math.max

data class WearSleepSegment(
    val value: Float,
    val color: Color
)

data class WearSleepColumn(
    val segments: List<WearSleepSegment>
)

@Composable
fun WearMinimalSleepChart(
    columns: List<WearSleepColumn>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = WearChartDefaults.MinimalChartHeight,
    barWidthRatio: Float = 0.52f,
    segmentGapRatio: Float = 0.22f,
    trackColor: Color = Color.White.copy(alpha = 0.05f),
    showTrack: Boolean = false,
    cornerRadiusRatio: Float = 0.45f
) {
    if (columns.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        val totals = columns.map { column -> column.segments.sumOf { it.value.toDouble() }.toFloat() }
        val maxTotal = max(1f, totals.maxOrNull() ?: 1f)
        val slotWidth = size.width / columns.size
        val barWidth = slotWidth * barWidthRatio
        val gap = barWidth * segmentGapRatio
        val corner = CornerRadius(barWidth * cornerRadiusRatio, barWidth * cornerRadiusRatio)

        columns.forEachIndexed { index, column ->
            val left = slotWidth * index + (slotWidth - barWidth) / 2f
            var currentBottom = size.height

            if (showTrack) {
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = corner
                )
            }

            column.segments.forEach { segment ->
                val rawHeight = (segment.value / maxTotal) * size.height
                val height = max(0f, rawHeight - gap)
                val top = currentBottom - rawHeight + gap / 2f

                if (height > 0f) {
                    drawRoundRect(
                        color = segment.color,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, height),
                        cornerRadius = corner
                    )
                }

                currentBottom -= rawHeight
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMinimalSleepChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Spring
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMinimalSleepChart(
                columns = listOf(
                    WearSleepColumn(listOf(WearSleepSegment(15f, Color(0xFF4338CA)), WearSleepSegment(20f, Color(0xFF60A5FA)))),
                    WearSleepColumn(listOf(WearSleepSegment(10f, Color(0xFF6366F1)), WearSleepSegment(24f, Color(0xFF38BDF8)))),
                    WearSleepColumn(listOf(WearSleepSegment(16f, Color(0xFF4F46E5)), WearSleepSegment(14f, Color(0xFF7DD3FC)))),
                    WearSleepColumn(listOf(WearSleepSegment(12f, Color(0xFF312E81)), WearSleepSegment(18f, Color(0xFF93C5FD))))
                ),
                showTrack = true
            )
        }
    }
}
