package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartMark

/**
 * Displays a legend for a chart.
 *
 * A legend item consists of:
 * - A colored box (representing a dataset/segment)
 * - A label
 *
 * Labels can be provided directly via [labels], or derived from [chartData].
 * If both are null, an empty legend is rendered.
 *
 * Layout behavior:
 * - [LegendPosition.TOP] and [LegendPosition.BOTTOM] → horizontal layout (LazyRow)
 * - [LegendPosition.LEFT] and [LegendPosition.RIGHT] → vertical layout (Column)
 *
 * @param modifier Modifier applied to the legend container.
 * @param labels Optional explicit labels for legend items.
 * @param chartData Optional chart data used to derive labels if [labels] is null.
 * @param position Legend placement relative to the chart.
 * @param colors Colors corresponding to each legend item.
 * @param title Optional legend title (shown only in LEFT/RIGHT positions).
 * @param colorBoxSize Size of the colored indicator box.
 * @param textSize Text size of legend labels.
 * @param spacing Spacing between legend items.
 */
@Composable
fun ChartLegend(
    modifier: Modifier = Modifier,
    labels: List<String>? = null,
    chartData: List<ChartMark>? = null,
    position: LegendPosition = LegendPosition.BOTTOM,
    colors: List<Color>,
    title: String? = null,
    colorBoxSize: Dp = 12.dp,
    textSize: TextUnit = 12.sp,
    spacing: Dp = 8.dp
) {
    // If explicit labels are not provided, derive them from chartData.
    val legendLabels = labels ?: chartData?.mapIndexed { index, point ->
        point.label ?: "항목 ${index + 1}"
    } ?: emptyList()

    when (position) {
        LegendPosition.TOP,
        LegendPosition.BOTTOM -> {
            // Horizontal layout
            LazyRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(spacing * 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(legendLabels.size) { index ->
                    if (index < colors.size) {
                        LegendItem(
                            color = colors[index],
                            label = legendLabels[index],
                            colorBoxSize = colorBoxSize,
                            textSize = textSize,
                            spacing = spacing
                        )
                    }
                }
            }
        }

        LegendPosition.LEFT,
        LegendPosition.RIGHT -> {
            // Vertical layout
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Optional title (shown only in vertical layout)
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = (textSize.value + 2).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                legendLabels.forEachIndexed { index, label ->
                    if (index < colors.size) {
                        LegendItem(
                            color = colors[index],
                            label = label,
                            colorBoxSize = colorBoxSize,
                            textSize = textSize,
                            spacing = spacing
                        )
                    }
                }
            }
        }
    }
}

/**
 * A single legend entry consisting of:
 * - A small colored square
 * - A label text
 *
 * @param color Color used to represent the corresponding dataset/segment.
 * @param label Display text of the legend item.
 * @param colorBoxSize Size of the colored square indicator.
 * @param textSize Text size of the label.
 * @param spacing Space between the color box and the text.
 */
@Composable
fun LegendItem(
    color: Color,
    label: String,
    colorBoxSize: Dp = 20.dp,
    textSize: TextUnit = 16.sp,
    spacing: Dp = 8.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Color indicator box
        Box(
            modifier = Modifier
                .size(colorBoxSize)
                .background(
                    color = color,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        // Label text
        Text(
            text = label,
            fontSize = textSize,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Position of the legend relative to the chart.
 */
enum class LegendPosition {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM
}