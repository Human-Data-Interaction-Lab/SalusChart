package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * A vertically-rotated text label for use alongside a Y-axis.
 *
 * Renders [text] rotated 90° counterclockwise (reads bottom-to-top), fills the full
 * row height, and centers itself vertically so it never overlaps axis tick values.
 * Intended to be placed as the first or last child of the chart's content Row.
 */
@Composable
fun VerticalAxisLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(min = 20.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            modifier = Modifier
                .graphicsLayer { rotationZ = -90f }
                .layout { measurable, _ ->
                    val placeable = measurable.measure(
                        Constraints(
                            minWidth = 0,
                            maxWidth = Constraints.Infinity,
                            minHeight = 0,
                            maxHeight = Constraints.Infinity
                        )
                    )
                    // Swap width↔height so the layout system sees the post-rotation footprint
                    layout(placeable.height, placeable.width) {
                        placeable.placeRelative(
                            x = -(placeable.width - placeable.height) / 2,
                            y = -(placeable.height - placeable.width) / 2
                        )
                    }
                }
        )
    }
}
