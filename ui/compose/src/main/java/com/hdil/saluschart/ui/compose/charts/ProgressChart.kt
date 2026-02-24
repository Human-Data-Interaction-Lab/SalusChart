package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.ui.theme.ColorUtils
import kotlin.math.hypot

/**
 * Displays a progress chart in either:
 * - Donut (ring) mode
 * - Horizontal bar mode
 *
 * This composable supports multiple progress items. In donut mode, each item
 * is rendered as a concentric ring. In bar mode, each item is rendered as a
 * horizontal progress bar stacked vertically.
 *
 * @param modifier Modifier applied to the chart container.
 *
 * @param data List of [ProgressChartMark] representing progress items.
 * Each item should provide current value, maximum value, label, and optional unit.
 *
 * @param title Title text displayed above the chart.
 *
 * @param isDonut If true, renders a donut (ring) progress chart.
 * If false, renders a horizontal bar progress chart.
 *
 * @param isPercentage If true, values are displayed as percentages.
 * If false, raw values (current / max) are displayed.
 *
 * @param colors Optional list of colors used for progress items.
 * If null, default colors will be generated automatically.
 * If fewer colors than data items are provided, colors will repeat cyclically.
 *
 * @param donutHeight Height of the donut chart container.
 * Only applied when [isDonut] is true.
 *
 * @param strokeWidth Thickness of each progress ring in donut mode.
 * Ignored in bar mode.
 *
 * @param barHeight Height of each individual bar in bar mode.
 * Ignored in donut mode.
 *
 * @param barSpacing Vertical spacing between bars in bar mode.
 * Ignored in donut mode.
 *
 * @param topPadding Top padding inside the drawing area (bar mode only).
 *
 * @param bottomPadding Bottom padding inside the drawing area (bar mode only).
 *
 * @param showLabels If true, displays labels for each progress item.
 *
 * @param showValues If true, displays numeric progress values.
 *
 * @param showCenterInfo If true, displays center title and subtitle
 * inside the donut chart. Ignored in bar mode.
 *
 * @param centerTitle Title text displayed at the center of the donut chart.
 * Only used when [isDonut] is true and [showCenterInfo] is true.
 *
 * @param centerSubtitle Subtitle text displayed below [centerTitle]
 * inside the donut chart.
 *
 * @param showLegend If true, displays a legend below the chart.
 *
 * @param legendTitle Optional title displayed above the legend.
 *
 * @param legendColorBoxSize Size of the color indicator box in the legend.
 *
 * @param legendTextSize Text size of legend labels.
 *
 * @param legendSpacing Horizontal spacing between legend items.
 *
 * @param legendPadding Vertical padding applied around the legend.
 *
 * @param interactionsEnabled Controls whether the chart responds to user gestures.
 * When false, tap detection and selection behavior are disabled,
 * and tooltips will not appear even if [tooltipEnabled] is true.
 *
 * @param tooltipEnabled Whether to show a tooltip on donut ring taps.
 *
 * @param tooltipFormatter Optional formatter for tooltip text.
 * If provided, it overrides the default text.
 *
 * @param tooltipBackgroundColor Background color of the default tooltip.
 * Ignored if tooltipContent is provided.
 *
 * @param tooltipTextColor Text color of the default tooltip.
 * Ignored if tooltipContent is provided.
 *
 * @param tooltipContent Optional full tooltip composable override.
 * If provided, it takes priority over tooltipFormatter and the default tooltip UI.
 */

@Composable
fun ProgressChart(
    modifier: Modifier = Modifier,
    data: List<ProgressChartMark>,
    title: String = "Progress Chart",
    isDonut: Boolean = true,
    isPercentage: Boolean = true,
    colors: List<Color>? = null,
    donutHeight: Dp = 200.dp,
    strokeWidth: Dp = 20.dp,
    barHeight: Dp = 16.dp,
    barSpacing: Dp = 10.dp,
    topPadding: Dp = 8.dp,
    bottomPadding: Dp = 8.dp,
    showLabels: Boolean = true,
    showValues: Boolean = true,
    showCenterInfo: Boolean = true,
    centerTitle: String = "Activity",
    centerSubtitle: String = "Progress",
    showLegend: Boolean = true,
    legendTitle: String? = null,
    legendColorBoxSize: Dp = 10.dp,
    legendTextSize: TextUnit = 12.sp,
    legendSpacing: Dp = 8.dp,
    legendPadding: Dp = 8.dp,
    interactionsEnabled: Boolean = true,
    tooltipEnabled: Boolean = true,
    tooltipFormatter: ((ProgressChartMark) -> String)? = null,
    tooltipBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    tooltipTextColor: Color = MaterialTheme.colorScheme.onSurface,
    tooltipContent: (@Composable (mark: ProgressChartMark, color: Color, modifier: Modifier) -> Unit)? = null,
) {
    if (data.isEmpty()) return

    val resolvedColors = remember(data.size, colors) {
        val base = colors ?: ColorUtils.rainbowPalette(data.size.coerceAtLeast(1))
        // Repeat colors if the caller provided fewer than data.size
        List(data.size) { i -> base[i % base.size] }
    }

    val legendLabels = remember(data) {
        data.mapIndexed { i, p -> p.label ?: "Item ${i + 1}" }
    }

    // Tap state for donut tooltip interaction
    var tappedIndex by remember { mutableStateOf<Int?>(null) }
    var tapOffset by remember { mutableStateOf<Offset?>(null) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val barHeightPx = with(density) { barHeight.toPx() }
    val barSpacingPx = with(density) { barSpacing.toPx() }
    val topPaddingPx = with(density) { topPadding.toPx() }
    val bottomPaddingPx = with(density) { bottomPadding.toPx() }

    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(20.dp))

        val canvasModifier = if (isDonut) {
            Modifier.fillMaxWidth().height(donutHeight)
        } else {
            // Compute required height for bar layout in pixels, then convert to dp
            val contentHeightPx =
                topPaddingPx +
                        bottomPaddingPx +
                        data.size * barHeightPx +
                        (data.size - 1).coerceAtLeast(0) * barSpacingPx

            val contentHeightDp = with(density) { contentHeightPx.toDp() }
            Modifier.fillMaxWidth().height(contentHeightDp)
        }

        Box(
            modifier = canvasModifier
                .padding(start = 24.dp)
                .onGloballyPositioned { boxSize = it.size }
        ) {
            val chartInteractionModifier =
                if (interactionsEnabled && isDonut) {
                    Modifier.pointerInput(data, isDonut, strokeWidthPx) {
                        detectTapGestures { pos ->
                            val canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                            val (center, _, ringRadii) =
                                ChartMath.Progress.computeProgressDonutMetrics(
                                    size = canvasSize,
                                    data = data,
                                    strokeWidth = strokeWidthPx
                                )

                            val d = hypot(pos.x - center.x, pos.y - center.y)
                            val half = strokeWidthPx / 2f
                            val hitIndex = ringRadii.indexOfFirst { r -> d in (r - half)..(r + half) }

                            if (hitIndex == -1) {
                                tappedIndex = null
                                tapOffset = null
                            } else {
                                tappedIndex = hitIndex
                                tapOffset = pos
                            }
                        }
                    }
                } else {
                    Modifier
                }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(chartInteractionModifier)
            ) {
                // Draw progress marks (donut or bars)
                ChartDraw.Progress.drawProgressMarks(
                    drawScope = this,
                    data = data,
                    size = size,
                    colors = resolvedColors,
                    isDonut = isDonut,
                    strokeWidth = strokeWidthPx,
                    barHeight = barHeightPx,
                    barSpacing = barSpacingPx,
                    topPadding = topPaddingPx,
                    cornerRadius = barHeightPx / 2f
                )

                if (isDonut && showCenterInfo) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    ChartDraw.Progress.drawProgressCenterInfo(
                        drawScope = this,
                        center = center,
                        title = centerTitle,
                        subtitle = centerSubtitle
                    )
                }

                if (showLabels) {
                    ChartDraw.Progress.drawProgressLabels(
                        drawScope = this,
                        data = data,
                        size = size,
                        isDonut = isDonut,
                        strokeWidth = strokeWidthPx,
                        barHeight = barHeightPx,
                        barSpacing = barSpacingPx,
                        topPadding = topPaddingPx
                    )
                }

                if (showValues) {
                    ChartDraw.Progress.drawProgressValues(
                        drawScope = this,
                        data = data,
                        size = size,
                        isDonut = isDonut,
                        strokeWidth = strokeWidthPx,
                        barHeight = barHeightPx,
                        isPercentage = isPercentage,
                        barSpacing = barSpacingPx,
                        topPadding = topPaddingPx
                    )
                }
            }

            // Tooltip overlay (donut only)
            val i = tappedIndex
            val pos = tapOffset
            if (tooltipEnabled && interactionsEnabled && i != null && pos != null && isDonut) {
                // Expected tooltip bounds for clamping within the Box
                val tipWidthDp = 180.dp
                val tipHeightDp = 72.dp
                val marginDp = 12.dp

                val tipW = with(density) { tipWidthDp.toPx() }
                val tipH = with(density) { tipHeightDp.toPx() }
                val margin = with(density) { marginDp.toPx() }

                // Position above the tap, then clamp into the container
                var px = pos.x
                var py = pos.y - 80f

                val maxX = (boxSize.width - tipW - margin).coerceAtLeast(margin)
                val maxY = (boxSize.height - tipH - margin).coerceAtLeast(margin)
                px = px.coerceIn(margin, maxX)
                py = py.coerceIn(margin, maxY)

                val xDp = with(density) { px.toDp() }
                val yDp = with(density) { py.toDp() }

                val point = data[i]
                val tipModifier = Modifier.offset(x = xDp, y = yDp)
                val tipColor = resolvedColors[i]

                // Priority: tooltipContent > tooltipFormatter > default tooltip
                if (tooltipContent != null) {
                    tooltipContent(point, tipColor, tipModifier)
                } else {
                    val defaultText = if (isPercentage) {
                        val pct = if (point.max > 0f) (point.current / point.max * 100f) else 0f
                        "${pct.toInt()}%"
                    } else {
                        if (!point.unit.isNullOrBlank()) {
                            "${point.current.toInt()} ${point.unit} / ${point.max.toInt()} ${point.unit}"
                        } else {
                            "${point.current.toInt()} / ${point.max.toInt()}"
                        }
                    }

                    val text = tooltipFormatter?.invoke(point) ?: defaultText

                    val tooltipPoint = ChartMark(
                        x = i.toDouble(),
                        y = point.current,
                        label = point.label
                    )

                    ChartTooltip(
                        chartMark = tooltipPoint,
                        customText = text,
                        backgroundColor = tooltipBackgroundColor,
                        textColor = tooltipTextColor,
                        modifier = tipModifier,
                        color = tipColor
                    )
                }
            }
        }

        if (showLegend) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ChartLegend(
                    modifier = Modifier.padding(vertical = legendPadding),
                    labels = legendLabels,
                    colors = resolvedColors,
                    position = LegendPosition.BOTTOM,
                    title = legendTitle,
                    colorBoxSize = legendColorBoxSize,
                    textSize = legendTextSize,
                    spacing = legendSpacing
                )
            }
        }

        Spacer(Modifier.height(6.dp))
    }
}