package com.hdil.saluschart.ui.wear.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme
import kotlin.math.max

@Composable
fun WearMiniRangeBarChart(
    data: List<RangeChartMark>,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    color: Color? = null,
    chartHeight: Dp = WearChartDefaults.CompactChartHeight,
    barWidthRatio: Float = 0.42f,
    minimumVisibleRangeRatio: Float = 0.05f,
    showSummary: Boolean = true,
    summaryTitle: String = "Range",
    summaryBackgroundColor: Color = Color.White.copy(alpha = 0.04f),
    summaryTextColor: Color = MaterialTheme.colorScheme.onBackground
) {
    if (data.isEmpty()) return
    val resolvedColor = color ?: WearChartDefaults.palette().firstOrNull() ?: MaterialTheme.colorScheme.primary

    val minValue = data.minOf { it.minPoint.y }
    val maxValue = data.maxOf { it.maxPoint.y }.takeIf { it > minValue } ?: (minValue + 1.0)

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

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val barSpace = size.width / max(data.size, 1)
            val barWidth = barSpace * barWidthRatio
            val usableHeight = size.height * 0.86f
            val topPadding = size.height * 0.08f

            data.forEachIndexed { index, mark ->
                val normalizedMin = ((mark.minPoint.y - minValue) / (maxValue - minValue)).toFloat()
                val normalizedMax = ((mark.maxPoint.y - minValue) / (maxValue - minValue)).toFloat()
                val left = (barSpace * index) + (barSpace - barWidth) / 2f
                val top = topPadding + (1f - normalizedMax) * usableHeight
                val height = (normalizedMax - normalizedMin).coerceAtLeast(minimumVisibleRangeRatio) * usableHeight

                drawRoundRect(
                    color = WearChartDefaults.trackColor(resolvedColor),
                    topLeft = Offset(left, topPadding),
                    size = Size(barWidth, usableHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )

                drawRoundRect(
                    color = resolvedColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }

        if (showSummary) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(summaryBackgroundColor)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(WearChartDefaults.MicroSpacing)
            ) {
                Text(
                    text = summaryTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${minValue.toInt()} - ${maxValue.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = summaryTextColor
                )
                Text(
                    text = data.joinToString(" ") { it.label?.firstOrNull()?.toString() ?: "•" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearMiniRangeBarChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Sunset
    ) {
        androidx.compose.material3.MaterialTheme {
            WearMiniRangeBarChart(
                title = "Weekly heart rate",
                data = listOf(
                    RangeChartMark(0.0, ChartMark(0.0, 62.0), ChartMark(0.0, 118.0), "Mon"),
                    RangeChartMark(1.0, ChartMark(1.0, 60.0), ChartMark(1.0, 121.0), "Tue"),
                    RangeChartMark(2.0, ChartMark(2.0, 64.0), ChartMark(2.0, 116.0), "Wed"),
                    RangeChartMark(3.0, ChartMark(3.0, 58.0), ChartMark(3.0, 124.0), "Thu"),
                    RangeChartMark(4.0, ChartMark(4.0, 61.0), ChartMark(4.0, 119.0), "Fri")
                )
            )
        }
    }
}
