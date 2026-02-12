package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.BaseChartMark
import kotlin.math.roundToInt

/**
 * Tooltip composable used by charts to display information about a selected data point.
 *
 * The tooltip shows:
 * - An optional label (if the mark provides one)
 * - A colored indicator dot
 * - A value string derived from the mark type, unless [customText] is provided
 *
 * If [customText] is not null, it takes priority over all default formatting logic.
 *
 * @param chartMark The selected chart mark to display.
 * @param unit Optional unit suffix appended to values (e.g., "kg", "bpm", "min").
 * @param backgroundColor Tooltip background color.
 * @param textColor Tooltip text color.
 * @param customText Optional custom tooltip text. When provided, default formatting is skipped.
 * @param color Color of the indicator dot (used for non-stacked marks).
 * @param segmentColors Optional list of colors for stacked marks. If provided, it is used to color
 * each segment row indicator in [com.hdil.saluschart.core.chart.StackedChartMark].
 * @param modifier Modifier applied to the tooltip container (useful for positioning).
 */

@Composable
fun ChartTooltip(
    chartMark: BaseChartMark,
    unit: String = "",
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    customText: String? = null,
    color: Color = Color.Black,
    segmentColors: List<Color>? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(
                color = backgroundColor.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chartMark.label?.let { label ->
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha =1f),
                    lineHeight = 16.sp
                )
            }
            // Custom text has the highest priority
            if (customText != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = color as Color,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = customText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor.copy(alpha = 0.9f),
                        lineHeight = 14.sp
                    )
                }
            } else {
                // Custom text for RangeChartMark and StackedChartMark
                when (chartMark) {
                    is com.hdil.saluschart.core.chart.RangeChartMark -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = color as Color,
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = "${chartMark.minPoint.y.roundToInt()}$unit ~ ${chartMark.maxPoint.y.roundToInt()}$unit", // Custom text format: '100 kg ~ 120 kg'
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.9f),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    is com.hdil.saluschart.core.chart.StackedChartMark -> {
                        chartMark.segments.asReversed().forEachIndexed { index, segment ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = segmentColors?.getOrNull(index) ?: Color.Black,
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = if (unit.isNotEmpty()) {
                                        "${segment.y.roundToInt()}$unit"
                                    } else {
                                        segment.y.roundToInt().toString()
                                           },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor.copy(alpha = 0.9f),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                    // Default text
                    else -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = color as Color,
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = if (unit.isNotEmpty()) {
                                    "${chartMark.y.roundToInt()}$unit"
                                } else {
                                    chartMark.y.roundToInt().toString()
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor.copy(alpha = 0.9f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
