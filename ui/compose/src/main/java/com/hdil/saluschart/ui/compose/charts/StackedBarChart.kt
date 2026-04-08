package com.hdil.saluschart.ui.compose.charts

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.*
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.VerticalAxisLabel
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.toStackedChartMarks
import kotlin.math.ceil
import kotlin.math.min
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import com.hdil.saluschart.ui.theme.LocalSalusChartColors

private fun ceilToStep(v: Double, step: Double): Double {
    if (step <= 0.0) return v
    return ceil(v / step) * step
}

/**
 * Renders a stacked bar chart where each x-position displays multiple vertical segments
 * stacked on top of each other.
 *
 * Input [data] is transformed into stacked marks via [toStackedChartMarks]; marks sharing
 * the same `x` value form one stacked bar with one segment per mark. The chart supports
 * three display modes:
 * 1. Static (default): all bars are visible without scrolling or paging.
 * 2. Scrolling: enabled when [windowSize] is non-null and smaller than the bar count.
 * 3. Paging: enabled when [pageSize] is non-null and smaller than the bar count.
 *
 * Scrolling and paging are mutually exclusive.
 *
 * @param modifier Modifier applied to the chart container.
 * @param data Raw chart marks. Marks sharing the same `x` form one stacked bar.
 * @param segmentLabels Optional labels for each segment layer, used in the legend.
 * @param xLabel Optional x-axis title.
 * @param yLabel Optional y-axis title.
 * @param title Chart title displayed when [showTitle] is true.
 * @param colors Colors assigned to segment layers by index; cycles if fewer layers than colors.
 * @param barWidthRatio Ratio of bar width to slot width (0–1 recommended).
 * @param showLegend Whether to display the legend.
 * @param legendPosition Position of the legend relative to the chart.
 * @param yAxisPosition Side on which the Y-axis labels are drawn.
 * @param interactionType Controls the tap hit area for bar/segment selection.
 * @param onBarClick Optional callback invoked on tap; receives (barIndex, segmentIndex, value).
 * @param referenceLines Optional horizontal reference lines drawn across the plot area.
 * @param showYAxisHighlight Whether to highlight the Y-axis tick nearest the selected bar total.
 * @param showTitle Whether to display [title].
 * @param showYAxis Whether to draw the Y-axis and grid lines.
 * @param showLabel Whether to draw value labels on each segment.
 * @param xLabelAutoSkip If true, x-axis labels may be skipped to avoid overlap.
 * @param maxXTicksLimit Maximum number of x-axis tick labels to display.
 * @param minY Optional minimum Y value for the axis; auto-computed when null.
 * @param maxY Optional maximum Y value for the axis; auto-computed when null.
 * @param yTickStep Fixed y-axis tick step; auto-computed when null.
 * @param unit Unit suffix appended to tooltip and value labels.
 * @param windowSize Enables free horizontal scrolling when provided and smaller than bar count.
 * @param contentPadding Padding applied around the chart content in scroll/page mode.
 * @param pageSize Enables paging when provided; mutually exclusive with [windowSize].
 * @param unifyYAxisAcrossPages Whether all pages share the same Y-axis scale in paging mode.
 * @param initialPageIndex Initial page index for paging mode; defaults to last page when null.
 * @param yAxisFixedWidth Width reserved for the Y-axis pane in scroll/page mode.
 * @param tooltipColor Color of the tooltip indicator dot.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StackedBarChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    segmentLabels: List<String> = emptyList(),
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Stacked Bar Chart",
    colors: List<Color> = emptyList(),
    barWidthRatio: Float = 0.6f,
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.StackedBar = InteractionType.StackedBar.TOUCH_AREA,
    onBarClick: ((barIndex: Int, segmentIndex: Int?, value: Float) -> Unit)? = null,
    referenceLines: List<com.hdil.saluschart.core.chart.ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showLabel: Boolean = false,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    minY: Double? = null,
    maxY: Double? = null,
    yTickStep: Double? = null,
    unit: String = "",
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 20.dp,
    tooltipColor: Color = Color.Unspecified,
) {
    if (data.isEmpty()) return

    val colors = colors.ifEmpty { LocalSalusChartColors.current.palette }
    val tooltipColor = tooltipColor.takeIf { it != Color.Unspecified } ?: LocalSalusChartColors.current.primary

    // Validate that scrolling and paging modes are not both enabled
    require(!(windowSize != null && pageSize != null)) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    val chartType = ChartType.STACKED_BAR

    // Transform ChartMark to StackedChartMark (memoized)
    val stackedData = remember(data) {
        data.toStackedChartMarks(
            segmentOrdering = { group: List<ChartMark> -> group.sortedByDescending { it.y } }
        )
    }
    val totals: List<Double> = remember(stackedData) { stackedData.map { it.y } }
    val xLabels = remember(stackedData) { stackedData.map { it.label ?: it.x.toString() } }

    // Pre-compute segment bands (mins/maxs per segment across all bars) to avoid repeated allocations
    val segCount = stackedData.firstOrNull()?.segments?.size ?: 0
    val segmentBands: List<Pair<List<Double>, List<Double>>> = remember(stackedData, segCount) {
        List(segCount) { segIndex ->
            val mins = MutableList(stackedData.size) { 0.0 }
            val maxs = MutableList(stackedData.size) { 0.0 }
            var i = 0
            for (sp in stackedData) {
                var cum = 0.0
                var k = 0
                while (k < segIndex) {
                    cum += sp.segments.getOrNull(k)?.y ?: 0.0
                    k++
                }
                val seg = sp.segments.getOrNull(segIndex)?.y ?: 0.0
                mins[i] = cum
                maxs[i] = cum + seg
                i++
            }
            mins to maxs
        }
    }

    // Compute effective page size (0 = off)
    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)

    // Enable paging if pageSize is provided and data exceeds page size
    val enablePaging = requestedPageSize > 0 && data.size > requestedPageSize

    if (enablePaging) {
        StackedBarChartPagedInternal(
            modifier = modifier,
            data = data,
            segmentLabels = segmentLabels,
            xLabel = xLabel,
            yLabel = yLabel,
            title = title,
            colors = colors,
            barWidthRatio = barWidthRatio,
            showLegend = showLegend,
            legendPosition = legendPosition,
            interactionType = interactionType,
            onBarClick = onBarClick,
            showTitle = showTitle,
            showLabel = showLabel,
            showYAxis = showYAxis,
            unit = unit,
            // scale/paging
            pageSize = requestedPageSize,
            unifyYAxisAcrossPages = unifyYAxisAcrossPages,
            yTickStep = yTickStep,
            initialPageIndex = initialPageIndex,
            minY = minY,
            maxY = maxY,
            yAxisPosition = yAxisPosition,
            outerPadding = contentPadding,
            yAxisFixedWidth = yAxisFixedWidth,
            xLabelAutoSkip = xLabelAutoSkip,
            maxXTicksLimit = maxXTicksLimit,
            referenceLines = referenceLines,
            showYAxisHighlight = showYAxisHighlight,
        )
        return
    }

    val useScrolling = windowSize != null && windowSize < stackedData.size
    val isFixedYAxis = showYAxis && useScrolling
    val useExternalYAxis = isFixedYAxis || (showYAxisHighlight && showYAxis && referenceLines.isNotEmpty())
    val scrollState = rememberScrollState()
    val onBarClickState by rememberUpdatedState(onBarClick)

    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
        }

        val density = LocalDensity.current
        val effectiveYAxisWidth = if (useExternalYAxis) {
            val yAxisRange = remember(totals, minY, maxY, yTickStep) {
                ChartMath.computeYAxisRange(
                    values = totals,
                    chartType = chartType,
                    minY = minY ?: 0.0,
                    maxY = maxY,
                    fixedTickStep = yTickStep
                )
            }
            val tickPaint = remember { android.graphics.Paint().apply { textSize = 28f } }
            val longestTickPx = remember(yAxisRange) {
                yAxisRange.yTicks.maxOfOrNull { tick ->
                    tickPaint.measureText(ChartDraw.formatTickLabel(tick.toFloat()))
                } ?: 0f
            }
            val extraPx = if (showYAxisHighlight) 30f else 20f
            maxOf(yAxisFixedWidth, with(density) { (longestTickPx + extraPx).toDp() })
        } else yAxisFixedWidth

        val chartBlock: @Composable () -> Unit = {
            BoxWithConstraints {
                val availableWidth = maxWidth
                val marginHorizontal = 16.dp

                // Calculate canvas width for scrolling mode
                val canvasWidth = if (useScrolling) {
                    val chartWidth = availableWidth - (marginHorizontal * 2)
                    val sectionsCount =
                        (stackedData.size.toFloat() / windowSize.toFloat()).toInt().coerceAtLeast(1)
                    chartWidth * sectionsCount
                } else null

                var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

                var tooltipSpec by remember {
                    mutableStateOf<com.hdil.saluschart.core.chart.chartDraw.TooltipSpec?>(null)
                }
                var tooltipSize by remember { mutableStateOf<androidx.compose.ui.unit.IntSize>(androidx.compose.ui.unit.IntSize.Zero) }

                val yAxisPaddingPxValue = with(density) { effectiveYAxisWidth.toPx() }

                Row(Modifier.fillMaxSize()) {
                    if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                        VerticalAxisLabel(yLabel)
                    }
                    // Left fixed axis pane
                    if (useExternalYAxis && yAxisPosition == YAxisPosition.LEFT) {
                        Box(
                            modifier = Modifier
                                .width(effectiveYAxisWidth)
                                .fillMaxHeight()
                                .clipToBounds()
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .width(effectiveYAxisWidth)
                                    .fillMaxHeight()
                            ) {
                                chartMetrics?.let { m ->
                                    if (showYAxisHighlight && referenceLines.isNotEmpty()) {
                                        ChartDraw.drawYAxisStandaloneWithReferenceHighlights(
                                            drawScope = this, metrics = m, yAxisPosition = yAxisPosition,
                                            paneWidthPx = size.width, referenceLines = referenceLines
                                        )
                                    } else {
                                        ChartDraw.drawYAxisStandalone(
                                            drawScope = this, metrics = m, yAxisPosition = yAxisPosition, paneWidthPx = size.width
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
                    val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
                    val startPad =
                        if (!showYAxis || (useExternalYAxis && yAxisPosition == YAxisPosition.LEFT) || hasLeftLabel) 0.dp
                        else if (useExternalYAxis && !isFixedYAxis) 8.dp
                        else marginHorizontal
                    val endPad =
                        if (!showYAxis || (useExternalYAxis && yAxisPosition == YAxisPosition.RIGHT) || hasRightLabel) 0.dp
                        else if (useExternalYAxis && !isFixedYAxis) 8.dp
                        else marginHorizontal

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .let { if (useScrolling) it.horizontalScroll(scrollState, overscrollEffect = null) else it }
                            .padding(start = startPad, end = endPad)
                    ) {
                        Canvas(
                            modifier = if (useScrolling) {
                                Modifier.width(canvasWidth!!).fillMaxHeight()
                            } else {
                                Modifier.fillMaxSize()
                            }
                        ) {
                            val labelReservePx = if (referenceLines.any { it.showLabel || it.label != null }) {
                                (if (isFixedYAxis) 80.dp else 20.dp).toPx()
                            } else 0f
                            val paddingBottom = if (useExternalYAxis) 10f else 0f
                            val m = ChartMath.computeMetrics(
                                size = Size(size.width - labelReservePx, size.height),
                                values = totals,
                                chartType = chartType,
                                minY = minY ?: 0.0,
                                maxY = maxY,
                                includeYAxisPadding = !useExternalYAxis,
                                yAxisPaddingPx = yAxisPaddingPxValue,
                                fixedTickStep = yTickStep,
                                paddingBottom = paddingBottom
                            )
                            chartMetrics = m

                            ChartDraw.drawGrid(this, size, m, yAxisPosition, drawLabels = showYAxis && !useExternalYAxis)
                            if (showYAxis && !useExternalYAxis) ChartDraw.drawYAxis(this, m, yAxisPosition)
                            ChartDraw.Bar.drawBarXAxisLabels(
                                ctx = drawContext,
                                labels = xLabels,
                                metrics = m,
                                maxXTicksLimit = maxXTicksLimit,
                                xLabelAutoSkip = xLabelAutoSkip
                            )
                        }

                        val m = chartMetrics ?: return@Box

                        segmentBands.forEachIndexed { segIndex, (mins, maxs) ->
                            if (maxs.zip(mins).any { (mx, mn) -> mx > mn }) {
                                val c = colors.getOrNull(segIndex) ?: Color.Gray
                                ChartDraw.Bar.BarMarker(
                                    data = stackedData,
                                    minValues = mins,
                                    maxValues = maxs,
                                    metrics = m,
                                    color = c,
                                    barWidthRatio = barWidthRatio,
                                    interactive = false,
                                    chartType = chartType,
                                    unit = unit,
                                    showTooltipForIndex = selectedBarIndex
                                )
                            }
                        }

                        val minBase = List(stackedData.size) { m.minY }

                        when (interactionType) {
                            InteractionType.StackedBar.TOUCH_AREA -> {
                                ChartDraw.Bar.BarMarker(
                                    data = stackedData,
                                    minValues = minBase,
                                    maxValues = totals,
                                    metrics = m,
                                    chartType = chartType,
                                    interactive = false,
                                    color = Color.Transparent,
                                    showTooltipForIndex = selectedBarIndex,
                                    showLabel = showLabel,
                                    unit = unit,
                                    onTooltipSpec = { tooltipSpec = it }
                                )

                                ChartDraw.Bar.BarMarker(
                                    data = stackedData,
                                    minValues = minBase,
                                    maxValues = totals,
                                    metrics = m,
                                    chartType = chartType,
                                    isTouchArea = true,
                                    unit = unit,
                                    onBarClick = { index, _ ->
                                        stackedData.getOrNull(index)?.let { sp ->
                                            selectedBarIndex = if (selectedBarIndex == index) null else index
                                            onBarClickState?.invoke(index, null, sp.y.toFloat())
                                        }
                                    }
                                )
                            }
                            InteractionType.StackedBar.BAR -> {
                                ChartDraw.Bar.BarMarker(
                                    data = stackedData,
                                    minValues = minBase,
                                    maxValues = totals,
                                    metrics = m,
                                    chartType = chartType,
                                    interactive = true,
                                    color = Color.Transparent,
                                    unit = unit,
                                    onBarClick = { index, _ ->
                                        stackedData.getOrNull(index)?.let { sp ->
                                            onBarClickState?.invoke(index, null, sp.y.toFloat())
                                        }
                                    }
                                )
                            }
                        }

                        if (referenceLines.isNotEmpty()) {
                            ReferenceLine.ReferenceLines(
                                modifier = Modifier.fillMaxSize(),
                                specs = referenceLines,
                                data = stackedData,
                                metrics = m,
                                chartType = chartType,
                                yAxisPosition = yAxisPosition,
                            )
                        }

                        val spec = tooltipSpec
                        if (spec != null) {
                            val marginPx = with(density) { 8.dp.toPx() }

                            // Tooltip anchor in chart px space
                            val tipX = spec.offset.x
                            val gapPx = with(density) { 10.dp.toPx() }
                            val tipY = spec.offset.y - tooltipSize.height.toFloat() - gapPx

                            val w = tooltipSize.width.toFloat()
                            val h = tooltipSize.height.toFloat()
                            val hasSize = w > 0f && h > 0f

                            val baseX = if (hasSize) tipX - w / 2f else tipX
                            val baseY = tipY

                            // Clamp inside the chart drawing region
                            val maxX = (m.paddingX + m.chartWidth - w).coerceAtLeast(0f)
                            val maxY = (m.paddingY + m.chartHeight - h).coerceAtLeast(0f)

                            val clampedX = baseX.coerceIn(0f, maxX)
                            val clampedY = baseY.coerceIn(0f, maxY)

                            val offset = androidx.compose.ui.unit.IntOffset(
                                clampedX.toInt(),
                                clampedY.toInt()
                            )

                            Box(
                                Modifier
                                    .zIndex(10f)
                                    .offset { offset }
                                    .onGloballyPositioned { coords -> tooltipSize = coords.size }
                                    .graphicsLayer { alpha = if (hasSize) 1f else 0f }
                            ) {
                                com.hdil.saluschart.core.chart.chartDraw.ChartTooltip(
                                    chartMark = spec.chartMark,
                                    color = tooltipColor,
                                    segmentColors = colors,
                                )
                            }
                        }
                    }

                    // Right fixed axis pane
                    if (useExternalYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                        Box(
                            modifier = Modifier
                                .width(effectiveYAxisWidth)
                                .fillMaxHeight()
                                .clipToBounds()
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .width(effectiveYAxisWidth)
                                    .fillMaxHeight()
                            ) {
                                chartMetrics?.let { m ->
                                    if (showYAxisHighlight && referenceLines.isNotEmpty()) {
                                        ChartDraw.drawYAxisStandaloneWithReferenceHighlights(
                                            drawScope = this, metrics = m, yAxisPosition = yAxisPosition,
                                            paneWidthPx = size.width, referenceLines = referenceLines
                                        )
                                    } else {
                                        ChartDraw.drawYAxisStandalone(
                                            drawScope = this, metrics = m, yAxisPosition = yAxisPosition, paneWidthPx = size.width
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                        VerticalAxisLabel(yLabel)
                    }
                }
            }
        }

        val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
        val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
        val chartLeftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (useExternalYAxis && yAxisPosition == YAxisPosition.LEFT) effectiveYAxisWidth else 0.dp)
        val chartRightOffset = (if (useExternalYAxis && yAxisPosition == YAxisPosition.RIGHT) effectiveYAxisWidth else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)

        val xLabelBlock: @Composable () -> Unit = {
            if (xLabel.isNotBlank()) {
                val xLabelSpacerHeight = with(LocalDensity.current) { (50f + 28f).toDp() }
                Spacer(Modifier.height(xLabelSpacerHeight))
                Row(Modifier.fillMaxWidth()) {
                    if (chartLeftOffset > 0.dp) Spacer(Modifier.width(chartLeftOffset))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(text = xLabel, style = MaterialTheme.typography.bodySmall)
                    }
                    if (chartRightOffset > 0.dp) Spacer(Modifier.width(chartRightOffset))
                }
            }
        }

        // Draw legend — xLabel always appears directly below the chart, above any bottom legend
        when (legendPosition) {
            LegendPosition.LEFT, LegendPosition.RIGHT -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showLegend && legendPosition == LegendPosition.LEFT && segmentLabels.isNotEmpty()) {
                        ChartLegend(labels = segmentLabels, colors = colors, position = LegendPosition.LEFT)
                    }
                    Box(Modifier.weight(1f)) { chartBlock() }
                    if (showLegend && legendPosition == LegendPosition.RIGHT && segmentLabels.isNotEmpty()) {
                        ChartLegend(labels = segmentLabels, colors = colors, position = LegendPosition.RIGHT)
                    }
                }
                xLabelBlock()
            }
            LegendPosition.TOP -> {
                if (showLegend && segmentLabels.isNotEmpty()) {
                    CenteredLegend(segmentLabels, colors, LegendPosition.TOP, chartLeftOffset, chartRightOffset)
                    Spacer(Modifier.height(16.dp))
                }
                Box(Modifier.weight(1f, fill = true)) { chartBlock() }
                xLabelBlock()
            }
            LegendPosition.BOTTOM -> {
                Box(Modifier.weight(1f, fill = true)) { chartBlock() }
                xLabelBlock()
                if (showLegend && segmentLabels.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    CenteredLegend(segmentLabels, colors, LegendPosition.BOTTOM, chartLeftOffset, chartRightOffset)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

// Function for fixed external Y-axis in paged stacked bar chart
@Composable
private fun FixedPagerYAxisStacked(
    totals: List<Double>,
    minY: Double,
    maxY: Double,
    yAxisPosition: YAxisPosition,
    step: Double,
    width: Dp,
    referenceLines: List<com.hdil.saluschart.core.chart.ReferenceLineSpec> = emptyList()
) {
    Canvas(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
    ) {
        val m = ChartMath.computeMetrics(
            size = size,
            values = totals,  // Use actual totals like the inner chart
            chartType = ChartType.STACKED_BAR,
            minY = minY,
            maxY = maxY,
            includeYAxisPadding = false,
            fixedTickStep = step
        )
        // Call standalone y-axis drawing function
        if (referenceLines.isNotEmpty()) {
            ChartDraw.drawYAxisStandaloneWithReferenceHighlights(
                drawScope = this,
                metrics = m,
                yAxisPosition = yAxisPosition,
                paneWidthPx = size.width,
                referenceLines = referenceLines
            )
        } else {
            ChartDraw.drawYAxisStandalone(
                drawScope = this,
                metrics = m,
                yAxisPosition = yAxisPosition,
                paneWidthPx = size.width
            )
        }
    }
}

@Composable
private fun CenteredLegend(
    labels: List<String>,
    colors: List<Color>,
    position: LegendPosition,
    leftOffset: Dp = 0.dp,
    rightOffset: Dp = 0.dp,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (leftOffset > 0.dp) Spacer(Modifier.width(leftOffset))
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            ChartLegend(labels = labels, colors = colors, position = position)
        }
        if (rightOffset > 0.dp) Spacer(Modifier.width(rightOffset))
    }
}

// Function for paged stacked bar chart
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StackedBarChartPagedInternal(
    modifier: Modifier,
    data: List<ChartMark>,
    segmentLabels: List<String>,
    xLabel: String,
    yLabel: String,
    title: String,
    colors: List<Color>,
    barWidthRatio: Float,
    showLegend: Boolean,
    legendPosition: LegendPosition,
    interactionType: InteractionType.StackedBar,
    onBarClick: ((barIndex: Int, segmentIndex: Int?, value: Float) -> Unit)?,
    showTitle: Boolean,
    showYAxis: Boolean,
    showLabel: Boolean,
    unit: String,
    // scale/paging
    pageSize: Int,
    unifyYAxisAcrossPages: Boolean,
    yTickStep: Double?,
    initialPageIndex: Int?,
    minY: Double?,
    maxY: Double?,
    yAxisPosition: YAxisPosition,
    outerPadding: PaddingValues,
    yAxisFixedWidth: Dp = 20.dp,
    xLabelAutoSkip: Boolean,
    maxXTicksLimit: Int? = null,
    referenceLines: List<com.hdil.saluschart.core.chart.ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
) {
    // Transform ChartMark to StackedChartMark
    val stackedData = remember(data) {
        data.toStackedChartMarks(segmentOrdering = { group -> group.sortedByDescending { it.y } })
    }
    if (stackedData.isEmpty()) return

    val pageCount = remember(stackedData.size, pageSize) {
        ceil(stackedData.size / pageSize.toDouble()).toInt()
    }
    val firstPage = initialPageIndex ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    // Extract totals for use in both Y-axis range calculation and FixedPagerYAxis
    val totals = remember(stackedData) { stackedData.map { it.y } }
    val onBarClickState by rememberUpdatedState(onBarClick)

    // Compute global Y-axis range only if unify is requested
    val globalRange = remember(totals, minY, maxY, yTickStep, unifyYAxisAcrossPages) {
        if (unifyYAxisAcrossPages) {
            ChartMath.computeYAxisRange(
                values = totals,
                chartType = ChartType.STACKED_BAR,
                minY = minY ?: 0.0,
                maxY = maxY,
                fixedTickStep = yTickStep
            )
        } else null
    }

    // Current page range when not unified (cheap and derived from current page)
    val currentPageRange by remember(stackedData, totals, pageSize, minY, maxY, yTickStep, unifyYAxisAcrossPages) {
        derivedStateOf {
            globalRange ?: run {
                val p = pagerState.currentPage.coerceIn(0, pageCount - 1)
                val start = p * pageSize
                val end = min(start + pageSize, stackedData.size)
                val sliceTotals = totals.subList(start, end)
                ChartMath.computeYAxisRange(
                    values = sliceTotals,
                    chartType = ChartType.STACKED_BAR,
                    minY = minY ?: 0.0,
                    maxY = maxY,
                    fixedTickStep = yTickStep
                )
            }
        }
    }

    // Use global range if unified, otherwise use current page range
    val effectiveRange = globalRange ?: currentPageRange
    val minRounded = effectiveRange.minY
    val maxRounded = effectiveRange.maxY
    val effectiveTickStep = effectiveRange.tickStep

    Column(modifier = modifier.padding(outerPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
        }

        val chartArea: @Composable () -> Unit = {
            Row(Modifier.fillMaxSize()) {
                if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                    VerticalAxisLabel(yLabel)
                }
                // Left fixed external Y-axis
                if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                    FixedPagerYAxisStacked(
                        totals = totals,
                        minY = minRounded,
                        maxY = maxRounded,
                        yAxisPosition = YAxisPosition.LEFT,
                        step = effectiveTickStep,
                        width = yAxisFixedWidth,
                        referenceLines = if (showYAxisHighlight) referenceLines else emptyList()
                    )
                }

                // Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) { page ->
                    val start = page * pageSize
                    val end = min(start + pageSize, stackedData.size)
                    val slice = stackedData.subList(start, end)

                    // Convert StackedChartMark back to ChartMark for the function call
                    val sliceAsChartMarks = slice.flatMap { it.segments }

                    // Per-page range when unify is off; otherwise reuse global
                    val pageRange = globalRange ?: run {
                        val sliceTotals = totals.subList(start, end)
                        ChartMath.computeYAxisRange(
                            values = sliceTotals,
                            chartType = ChartType.STACKED_BAR,
                            minY = minY ?: 0.0,
                            maxY = maxY,
                            fixedTickStep = yTickStep
                        )
                    }

                    StackedBarChart(
                        modifier = Modifier.fillMaxSize(),
                        data = sliceAsChartMarks,
                        segmentLabels = segmentLabels,
                        title = title,
                        colors = colors,
                        barWidthRatio = barWidthRatio,
                        showLegend = false,                   // legend is shown outside
                        yAxisPosition = yAxisPosition,
                        interactionType = interactionType,
                        onBarClick = onBarClickState?.let { cb ->
                            { barIdx, segIdx, value -> cb(start + barIdx, segIdx, value) }
                        },
                        showTitle = false,
                        showYAxis = false,                    // external axis handles it
                        maxXTicksLimit = maxXTicksLimit,
                        xLabelAutoSkip = xLabelAutoSkip,
                        yTickStep = pageRange.tickStep,
                        minY = pageRange.minY,                // unify or page-specific
                        maxY = pageRange.maxY,                // unify or page-specific
                        windowSize = null,                    // no inner scroll
                        contentPadding = PaddingValues(0.dp),
                        pageSize = null,                      // don't recurse
                        unit = unit
                    )
                }

                // Right fixed external Y-axis
                if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                    FixedPagerYAxisStacked(
                        totals = totals,
                        minY = minRounded,
                        maxY = maxRounded,
                        yAxisPosition = YAxisPosition.RIGHT,
                        step = effectiveTickStep,
                        width = yAxisFixedWidth,
                        referenceLines = if (showYAxisHighlight) referenceLines else emptyList()
                    )
                }
                if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                    VerticalAxisLabel(yLabel)
                }
            }
        }

        val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
        val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
        val chartLeftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (showYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
        val chartRightOffset = (if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)

        val xLabelBlock: @Composable () -> Unit = {
            if (xLabel.isNotBlank()) {
                val xLabelSpacerHeight = with(LocalDensity.current) { (50f + 28f).toDp() }
                Spacer(Modifier.height(xLabelSpacerHeight))
                Row(Modifier.fillMaxWidth()) {
                    if (chartLeftOffset > 0.dp) Spacer(Modifier.width(chartLeftOffset))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(text = xLabel, style = MaterialTheme.typography.bodySmall)
                    }
                    if (chartRightOffset > 0.dp) Spacer(Modifier.width(chartRightOffset))
                }
            }
        }

        when (legendPosition) {
            LegendPosition.LEFT, LegendPosition.RIGHT -> {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showLegend && legendPosition == LegendPosition.LEFT && segmentLabels.isNotEmpty()) {
                        ChartLegend(labels = segmentLabels, colors = colors, position = LegendPosition.LEFT)
                    }
                    Box(Modifier.weight(1f, fill = true)) { chartArea() }
                    if (showLegend && legendPosition == LegendPosition.RIGHT && segmentLabels.isNotEmpty()) {
                        ChartLegend(labels = segmentLabels, colors = colors, position = LegendPosition.RIGHT)
                    }
                }
                xLabelBlock()
            }
            LegendPosition.TOP -> {
                Column(Modifier.weight(1f)) {
                    if (showLegend && segmentLabels.isNotEmpty()) {
                        CenteredLegend(segmentLabels, colors, LegendPosition.TOP, chartLeftOffset, chartRightOffset)
                        Spacer(Modifier.height(8.dp))
                    }
                    Box(Modifier.weight(1f, fill = true)) { chartArea() }
                }
                xLabelBlock()
            }
            LegendPosition.BOTTOM -> {
                Column(Modifier.weight(1f)) {
                    Box(Modifier.weight(1f, fill = true)) { chartArea() }
                }
                xLabelBlock()
                if (showLegend && segmentLabels.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    CenteredLegend(segmentLabels, colors, LegendPosition.BOTTOM, chartLeftOffset, chartRightOffset)
                }
            }
        }
    }
}