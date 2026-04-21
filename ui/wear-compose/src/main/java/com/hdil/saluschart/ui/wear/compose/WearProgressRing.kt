package com.hdil.saluschart.ui.wear.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme
import kotlin.math.min

@Composable
fun WearProgressRing(
    data: List<ProgressChartMark>,
    modifier: Modifier = Modifier,
    title: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    colors: List<Color> = emptyList(),
    maxLaps: Int = 1,
    chartHeight: Dp = WearChartDefaults.SummaryChartHeight,
    strokeWidth: Dp = WearChartDefaults.RingStroke,
    ringGap: Dp = WearChartDefaults.RingGap,
    showLabels: Boolean = true,
    showValues: Boolean = true,
    showCenterInfo: Boolean = true,
    centerLabel: String? = null,
    centerCaption: String = "daily goal",
    centerLabelColor: Color = MaterialTheme.colorScheme.onBackground,
    centerCaptionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    labelMaxLength: Int = 12,
    labelColor: Color = MaterialTheme.colorScheme.onBackground,
    valueColor: Color? = null,
    labelChipColor: Color = Color.White.copy(alpha = 0.04f),
    indicatorSize: Dp = 8.dp
) {
    if (data.isEmpty()) return
    val resolvedColors = colors.ifEmpty { WearChartDefaults.palette() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val resolvedCenterLabel = centerLabel ?: remember(data) {
        "${((data.map { it.progress }.averageOrNull() ?: 0.0) * 100).toInt()}%"
    }

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
                val strokeWidthPx = strokeWidth.toPx()
                val gapPx = ringGap.toPx()
                val maxRadius = (min(size.width, size.height) / 2f) - strokeWidthPx / 2f
                val ringStep = strokeWidthPx + gapPx
                val center = Offset(size.width / 2f, size.height / 2f)

                data.forEachIndexed { index, mark ->
                    val radius = maxRadius - (index * ringStep)
                    if (radius <= 0f) return@forEachIndexed

                    val arcSize = Size(radius * 2f, radius * 2f)
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val color = resolvedColors.getOrElse(index) { primaryColor }
                    val rawProgress = if (mark.max > 0.0) mark.current / mark.max else 0.0
                    val sweep = rawProgress.coerceIn(0.0, maxLaps.toDouble()).toFloat() * 360f

                    drawArc(
                        color = WearChartDefaults.trackColor(color),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    if (sweep > 0f) {
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            if (showCenterInfo) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = resolvedCenterLabel,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = centerLabelColor
                    )
                    Text(
                        text = centerCaption,
                        style = MaterialTheme.typography.bodySmall,
                        color = centerCaptionColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (showLabels) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(WearChartDefaults.MicroSpacing)
            ) {
                data.forEachIndexed { index, mark ->
                    val color = resolvedColors.getOrElse(index) { primaryColor }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(labelChipColor)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(indicatorSize)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = mark.label?.take(labelMaxLength) ?: "Series ${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = labelColor,
                                    maxLines = 1
                                )
                                if (showValues) {
                                    Text(
                                        text = "${mark.percentage.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        if (showValues) {
                            Text(
                                text = "${mark.current.toInt()}${mark.unit?.let { " $it" } ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = valueColor ?: color,
                                textAlign = TextAlign.End,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Spacer(modifier = Modifier.width(0.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun Iterable<Double>.averageOrNull(): Double? {
    var count = 0
    var sum = 0.0
    for (value in this) {
        sum += value
        count++
    }
    return if (count == 0) null else sum / count
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearProgressRingPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Summer
    ) {
        androidx.compose.material3.MaterialTheme {
            WearProgressRing(
                data = listOf(
                    ProgressChartMark(0.0, 540.0, 700.0, "Move", "kcal"),
                    ProgressChartMark(1.0, 26.0, 30.0, "Exercise", "min"),
                    ProgressChartMark(2.0, 9.0, 12.0, "Stand", "hr")
                ),
                title = "Activity rings"
            )
        }
    }
}
