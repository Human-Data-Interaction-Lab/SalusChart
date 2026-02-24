package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Drawing helpers for a minimal horizontal gauge visualization.
 *
 * This file provides small, reusable building blocks:
 * - A text label that displays a numeric range.
 * - A gauge bar consisting of a background container and an overlaid range segment.
 *
 * Note: This is currently used by the minimal gauge variant. The APIs are kept small and stable
 * so they can be reused by future (larger) gauge chart implementations without changing behavior.
 */
object GaugeChartDraw {

    /**
     * Displays a simple range label formatted as `"min-max"`.
     *
     * @param dataMin Lower bound of the displayed range.
     * @param dataMax Upper bound of the displayed range.
     * @param textColor Color of the text label.
     */
    @Composable
    fun RangeText(
        dataMin: Float,
        dataMax: Float,
        textColor: Color
    ) {
        Text(
            text = "${dataMin.toInt()}-${dataMax.toInt()}",
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    /**
     * Draws a horizontal gauge bar with:
     * - A full-width container (background bar) representing [containerMin]..[containerMax]
     * - An overlay segment representing the data range [dataMin]..[dataMax], clamped to the container
     *
     * Positioning is computed as a ratio of the measured container width:
     * - `startRatio = (dataMin - containerMin) / (containerMax - containerMin)`
     * - `endRatio   = (dataMax - containerMin) / (containerMax - containerMin)`
     *
     * Both ratios are clamped into `[0, 1]` and the segment width is `endRatio - startRatio`.
     *
     * @param dataMin Lower bound of the data range to highlight.
     * @param dataMax Upper bound of the data range to highlight.
     * @param containerMin Minimum value represented by the full container bar.
     * @param containerMax Maximum value represented by the full container bar.
     * @param containerColor Background/container bar color.
     * @param rangeColor Highlight segment color.
     */
    @Composable
    fun GaugeBar(
        dataMin: Float,
        dataMax: Float,
        containerMin: Float,
        containerMax: Float,
        containerColor: Color,
        rangeColor: Color
    ) {
        val density = LocalDensity.current
        var containerWidth by remember { mutableStateOf(0.dp) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .onGloballyPositioned { coordinates ->
                    // Measure container width in dp for placing the range segment.
                    containerWidth = with(density) { coordinates.size.width.toDp() }
                }
        ) {
            val containerRange = containerMax - containerMin

            // Safe ratio calculations (guarding divide-by-zero and clamping).
            val startRatio = if (containerRange > 0) {
                ((dataMin - containerMin) / containerRange).coerceIn(0f, 1f)
            } else 0f

            val endRatio = if (containerRange > 0) {
                ((dataMax - containerMin) / containerRange).coerceIn(0f, 1f)
            } else 0f

            val widthRatio = (endRatio - startRatio).coerceAtLeast(0f)

            // Container bar (background).
            ContainerBar(
                containerColor = containerColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )

            // Range segment (data range), positioned precisely within the container.
            if (widthRatio > 0f && containerWidth > 0.dp) {
                RangeBar(
                    rangeColor = rangeColor,
                    startOffset = containerWidth * startRatio,
                    barWidth = containerWidth * widthRatio,
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }

    /**
     * Background/container bar for the gauge.
     *
     * @param containerColor Fill color of the container.
     * @param modifier Modifier applied to the container bar.
     */
    @Composable
    private fun ContainerBar(
        containerColor: Color,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(containerColor)
        )
    }

    /**
     * Highlight segment representing the data range within the gauge.
     *
     * @param rangeColor Fill color of the range segment.
     * @param startOffset Horizontal offset from the container start.
     * @param barWidth Width of the range segment.
     * @param modifier Modifier applied to the range segment.
     */
    @Composable
    private fun RangeBar(
        rangeColor: Color,
        startOffset: Dp,
        barWidth: Dp,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .width(barWidth)
                .offset(x = startOffset)
                .clip(RoundedCornerShape(8.dp))
                .background(rangeColor)
        )
    }
}