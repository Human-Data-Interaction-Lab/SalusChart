package com.hdil.saluschart.ui.compose.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.TooltipSpec
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions
import com.hdil.saluschart.core.chart.toRangeChartMarksByXGroup
import com.hdil.saluschart.ui.theme.ChartColor

/**
 * Displays a range bar chart where each x-position is represented by a vertical bar spanning
 * from a minimum value to a maximum value.
 *
 * Supports:
 * - Optional paging mode (via [pageSize])
 * - Optional free scrolling mode (via [windowSize])
 * - Tap interaction on bars (see [interactionType] and [onBarClick])
 * - Optional overlay points per x-position (see [pointValues])
 *
 * Notes:
 * - Paging mode ([pageSize]) and scrolling mode ([windowSize]) are mutually exclusive.
 * - Tapping the selected bar again clears the selection.
 *
 * @param modifier Modifier applied to the chart container.
 * @param data Chart data. Each entry is a [RangeChartMark] representing min/max values for one x-position.
 * @param xLabel X-axis label displayed at the bottom when not blank.
 * @param yLabel Y-axis label displayed at the side when not blank.
 * @param title Title displayed above the chart when [showTitle] is true.
 * @param barColor Color used to draw range bars.
 * @param barWidthRatio Ratio of bar width to each slot width.
 * @param yAxisPosition Side on which the Y-axis is drawn.
 * @param interactionType Determines whether interaction is handled on the bar shape itself or via a larger touch area.
 * @param onBarClick Optional callback invoked when a bar is selected. Provides (index, mark).
 * @param showTitle Whether to display [title].
 * @param showYAxis Whether to draw the Y-axis and horizontal grid labels (depends on internal mode/layout).
 * @param xLabelAutoSkip If true, automatically skips x-axis labels when they would overlap.
 * @param maxXTicksLimit Optional max number of x-axis labels to render.
 * @param yTickStep Optional fixed tick step for the y-axis grid. If null, ticks are computed automatically.
 * @param unit Unit suffix used in tooltip/value formatting (e.g., "mg/dL", "bpm").
 * @param pointValues Optional overlay points drawn for each x-position (one list per bar index).
 * @param pointColor Color used to draw overlay points.
 * @param pointRadius Radius of overlay points.
 * @param barCornerRadiusFraction Corner radius fraction applied uniformly to bar corners (recommended 0.0–0.5).
 * @param barCornerRadiusFractions Optional per-corner radius configuration. Takes priority over [barCornerRadiusFraction].
 * @param roundTopOnly If true, only the top corners are rounded for bars.
 * @param windowSize Enables free scrolling when provided and smaller than the data size. Represents number of visible items.
 * @param contentPadding Padding applied around the chart content.
 * @param pageSize Enables paging when provided and smaller than the data size. Represents items per page.
 * @param initialPageIndex Optional initial page index (if null, defaults to the last page).
 * @param yAxisFixedWidth Width reserved for an external/fixed Y-axis pane in modes that support it.
 */

@JvmName("RangeBarChartRangeMarks")
@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<RangeChartMark>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Range Bar Chart",
    barColor: Color = ChartColor.Default,
    barWidthRatio: Float = 0.6f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.RangeBar = InteractionType.RangeBar.BAR,
    onBarClick: ((Int, RangeChartMark) -> Unit)? = null,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    yTickStep: Double? = null,
    unit: String = "",
    pointValues: List<List<Double>>? = null,
    pointColor: Color = barColor,
    pointRadius: Dp = 3.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 0.dp,
) {
    if (data.isEmpty()) return

    require(windowSize == null || pageSize == null) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)
    val enablePaging = requestedPageSize > 0 && data.size > requestedPageSize

    if (enablePaging) {
        RangeBarChartPagedInternal(
            modifier = modifier,
            data = data,
            title = title,
            barColor = barColor,
            barWidthRatio = barWidthRatio,
            yAxisPosition = yAxisPosition,
            interactionType = interactionType,
            onBarClick = onBarClick,
            showTitle = showTitle,
            showYAxis = showYAxis,
            maxXTicksLimit = maxXTicksLimit,
            xLabelAutoSkip = xLabelAutoSkip,
            unit = unit,
            pointValues = pointValues,
            pointColor = pointColor,
            pointRadius = pointRadius,
            pageSize = requestedPageSize,
            yTickStep = yTickStep,
            initialPageIndex = initialPageIndex,
            outerPadding = contentPadding,
            yAxisFixedWidth = yAxisFixedWidth,
            barCornerRadiusFraction = barCornerRadiusFraction,
            barCornerRadiusFractions = barCornerRadiusFractions,
            roundTopOnly = roundTopOnly,
        )
        return
    }

    RangeBarChartContent(
        modifier = modifier,
        rangeData = data,
        xLabel = xLabel,
        yLabel = yLabel,
        title = title,
        barColor = barColor,
        barWidthRatio = barWidthRatio,
        yAxisPosition = yAxisPosition,
        interactionType = interactionType,
        onBarClick = onBarClick,
        showTitle = showTitle,
        showYAxis = showYAxis,
        xLabelAutoSkip = xLabelAutoSkip,
        maxXTicksLimit = maxXTicksLimit,
        yTickStep = yTickStep,
        unit = unit,
        pointValues = pointValues,
        pointColor = pointColor,
        pointRadius = pointRadius,
        barCornerRadiusFraction = barCornerRadiusFraction,
        barCornerRadiusFractions = barCornerRadiusFractions,
        roundTopOnly = roundTopOnly,
        windowSize = windowSize,
        contentPadding = contentPadding,
        yAxisFixedWidth = yAxisFixedWidth,
    )
}

@JvmName("RangeBarChartChartMarks")
@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Range Bar Chart",
    barColor: Color = ChartColor.Default,
    barWidthRatio: Float = 0.6f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.RangeBar = InteractionType.RangeBar.BAR,
    onBarClick: ((Int, RangeChartMark) -> Unit)? = null,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    yTickStep: Double? = null,
    unit: String = "",
    pointValues: List<List<Double>>? = null,
    pointColor: Color = barColor,
    pointRadius: Dp = 3.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 0.dp,
) {
    val rangeData = remember(data) {
        data.toRangeChartMarksByXGroup(
            minValueSelector = { group -> group.minByOrNull { it.y } ?: group.first() },
            maxValueSelector = { group -> group.maxByOrNull { it.y } ?: group.first() }
        )
    }

    RangeBarChart(
        modifier = modifier,
        data = rangeData,
        xLabel = xLabel,
        yLabel = yLabel,
        title = title,
        barColor = barColor,
        barWidthRatio = barWidthRatio,
        yAxisPosition = yAxisPosition,
        interactionType = interactionType,
        onBarClick = onBarClick,
        showTitle = showTitle,
        showYAxis = showYAxis,
        xLabelAutoSkip = xLabelAutoSkip,
        maxXTicksLimit = maxXTicksLimit,
        yTickStep = yTickStep,
        unit = unit,
        pointValues = pointValues,
        pointColor = pointColor,
        pointRadius = pointRadius,
        barCornerRadiusFraction = barCornerRadiusFraction,
        barCornerRadiusFractions = barCornerRadiusFractions,
        roundTopOnly = roundTopOnly,
        windowSize = windowSize,
        contentPadding = contentPadding,
        pageSize = pageSize,
        initialPageIndex = initialPageIndex,
        yAxisFixedWidth = yAxisFixedWidth,
    )
}

@Composable
private fun RangeBarChartContent(
    modifier: Modifier,
    rangeData: List<RangeChartMark>,
    xLabel: String,
    yLabel: String,
    title: String,
    barColor: Color,
    barWidthRatio: Float,
    yAxisPosition: YAxisPosition,
    interactionType: InteractionType.RangeBar,
    onBarClick: ((Int, RangeChartMark) -> Unit)?,
    showTitle: Boolean,
    showYAxis: Boolean,
    xLabelAutoSkip: Boolean,
    maxXTicksLimit: Int?,
    yTickStep: Double?,
    unit: String,
    pointValues: List<List<Double>>?,
    pointColor: Color,
    pointRadius: Dp,
    barCornerRadiusFraction: Float,
    barCornerRadiusFractions: BarCornerRadiusFractions?,
    roundTopOnly: Boolean,
    windowSize: Int?,
    contentPadding: PaddingValues,
    yAxisFixedWidth: Dp,
) {
    if (rangeData.isEmpty()) return

    val chartType = ChartType.RANGE_BAR

    val useScrolling = windowSize != null && windowSize < rangeData.size
    val isFixedYAxis = showYAxis && useScrolling
    val scrollState = rememberScrollState()

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        // Axis titles
        if (showYAxis) {
            Spacer(Modifier.height(4.dp))
        }

        BoxWithConstraints {
            val availableWidth = maxWidth
            val parentWidthDp = maxWidth
            val marginHorizontal = 16.dp

            // Calculate canvas width for scrolling mode
            val canvasWidth = if (useScrolling) {
                val chartWidth = availableWidth - (marginHorizontal * 2)
                val sectionsCount = (rangeData.size.toFloat() / windowSize!!.toFloat()).toInt()
                chartWidth * sectionsCount
            } else null

            val labels = rangeData.map { it.label ?: it.x.toString() }

            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
            var selectedIndex by remember { mutableStateOf<Int?>(null) }
            var tooltipSpec by remember { mutableStateOf<TooltipSpec?>(null) }

            Row(Modifier.fillMaxSize()) {
                // Left fixed axis pane (used only when scrolling + showYAxis)
                if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) {
                    Canvas(
                        modifier = Modifier
                            .width(yAxisFixedWidth)
                            .fillMaxHeight()
                    ) {
                        chartMetrics?.let { m ->
                            ChartDraw.drawYAxisStandalone(
                                drawScope = this,
                                metrics = m,
                                yAxisPosition = yAxisPosition,
                                paneWidthPx = size.width
                            )
                        }
                    }
                }

                // Padding rules around the chart canvas:
                // - If Y-axis is hidden, no extra padding.
                // - If Y-axis is fixed on one side, no extra padding on that side.
                val startPad =
                    if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT)) 0.dp else marginHorizontal
                val endPad =
                    if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT)) 0.dp else marginHorizontal

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .let { if (useScrolling) it.horizontalScroll(scrollState, overscrollEffect = null) else it }
                        .padding(start = startPad, end = endPad)
                ) {
                    // Grid + axis + x labels (Canvas layer)
                    Canvas(
                        modifier = if (useScrolling) {
                            Modifier.width(canvasWidth!!).fillMaxHeight()
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ) {
                        val valuesForScale = buildList {
                            addAll(rangeData.map { it.minPoint.y })
                            addAll(rangeData.map { it.maxPoint.y })
                        }

                        val metrics = ChartMath.computeMetrics(
                            size = size,
                            values = valuesForScale,
                            chartType = chartType,
                            minY = null,
                            maxY = null,
                            includeYAxisPadding = !isFixedYAxis,
                            fixedTickStep = yTickStep
                        )

                        if (chartMetrics != metrics) chartMetrics = metrics

                        ChartDraw.drawGrid(
                            drawScope = this,
                            size = size,
                            metrics = metrics,
                            yAxisPosition = yAxisPosition,
                            drawLabels = showYAxis && !isFixedYAxis
                        )

                        if (showYAxis && !isFixedYAxis) {
                            ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                        }

                        ChartDraw.Bar.drawBarXAxisLabels(
                            ctx = drawContext,
                            labels = labels,
                            metrics = metrics,
                            maxXTicksLimit = maxXTicksLimit,
                            xLabelAutoSkip = xLabelAutoSkip
                        )
                    }

                    // Range bars & interaction (Composable layer)
                    val minValues = rangeData.map { it.minPoint.y }
                    val maxValues = rangeData.map { it.maxPoint.y }

                    when (interactionType) {
                        InteractionType.RangeBar.TOUCH_AREA -> {
                            chartMetrics?.let { m ->
                                // Visual bars (non-interactive)
                                ChartDraw.Bar.BarMarker(
                                    data = rangeData,
                                    minValues = minValues,
                                    maxValues = maxValues,
                                    metrics = m,
                                    color = barColor,
                                    barWidthRatio = barWidthRatio,
                                    interactive = false,
                                    chartType = chartType,
                                    unit = unit,
                                    showTooltipForIndex = selectedIndex,
                                    onTooltipSpec = { tooltipSpec = it },
                                    barCornerRadiusFraction = barCornerRadiusFraction,
                                    barCornerRadiusFractions = barCornerRadiusFractions,
                                    roundTopOnly = roundTopOnly,
                                )

                                // Transparent full-height touch strips
                                ChartDraw.Bar.BarMarker(
                                    data = rangeData,
                                    minValues = List(rangeData.size) { m.minY },
                                    maxValues = maxValues,
                                    metrics = m,
                                    chartType = chartType,
                                    isTouchArea = true,
                                    unit = unit,
                                    onBarClick = { idx, _ ->
                                        selectedIndex = if (selectedIndex == idx) null else idx
                                        onBarClick?.invoke(idx, rangeData[idx])
                                    },
                                    barCornerRadiusFraction = barCornerRadiusFraction,
                                    barCornerRadiusFractions = barCornerRadiusFractions,
                                    roundTopOnly = roundTopOnly,
                                )
                            }
                        }

                        InteractionType.RangeBar.BAR -> {
                            chartMetrics?.let { m ->
                                // Bars handle interaction directly
                                ChartDraw.Bar.BarMarker(
                                    data = rangeData,
                                    minValues = minValues,
                                    maxValues = maxValues,
                                    metrics = m,
                                    color = barColor,
                                    barWidthRatio = barWidthRatio,
                                    interactive = false,
                                    onBarClick = { idx, _ ->
                                        selectedIndex = if (selectedIndex == idx) null else idx
                                        onBarClick?.invoke(idx, rangeData[idx])
                                    },
                                    chartType = chartType,
                                    unit = unit,
                                    showTooltipForIndex = selectedIndex,
                                    onTooltipSpec = { tooltipSpec = it },
                                    barCornerRadiusFraction = barCornerRadiusFraction,
                                    barCornerRadiusFractions = barCornerRadiusFractions,
                                    roundTopOnly = roundTopOnly,
                                )
                            }
                        }
                    }

                    // Overlay points (Canvas layer)
                    if (!pointValues.isNullOrEmpty()) {
                        chartMetrics?.let { m ->
                            val radiusPx = with(LocalDensity.current) { pointRadius.toPx() }
                            val dataSize = rangeData.size
                            if (dataSize == 0) return@let

                            val slotWidth = m.chartWidth / dataSize.toFloat()
                            val activeIndex = selectedIndex

                            Canvas(modifier = Modifier.matchParentSize()) {
                                pointValues.forEachIndexed { index, values ->
                                    if (index >= dataSize) return@forEachIndexed

                                    val barMin = minValues[index]
                                    val barMax = maxValues[index]

                                    val centerX = m.paddingX + slotWidth * (index + 0.5f)

                                    val dotColor =
                                        if (activeIndex == null || index == activeIndex) {
                                            pointColor
                                        } else {
                                            pointColor.copy(alpha = 0.2f)
                                        }

                                    values.forEach { v ->
                                        val minY = m.minY
                                        val maxY = m.maxY

                                        fun valueToYPx(value: Double): Float {
                                            val ratio =
                                                if (maxY == minY) 0f
                                                else ((value - minY) / (maxY - minY)).toFloat()
                                            return m.chartHeight * (1f - ratio)
                                        }

                                        val yDot = valueToYPx(v)

                                        drawCircle(
                                            color = dotColor,
                                            radius = radiusPx,
                                            center = Offset(centerX, yDot)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tooltip overlay
                    tooltipSpec?.let { spec ->
                        val density = LocalDensity.current
                        val parentWidthPx = with(density) { parentWidthDp.toPx() }

                        val estimatedWidthPx = with(density) { 160.dp.toPx() }
                        var measuredWidthPx by remember(spec) { mutableStateOf<Float?>(null) }
                        val tooltipWidthPx = measuredWidthPx ?: estimatedWidthPx

                        val anchorXPx = spec.offset.x
                        val anchorYPx = spec.offset.y
                        val gapPx = with(density) { 8.dp.toPx() }

                        val wouldOverflowRight = anchorXPx + tooltipWidthPx + gapPx > parentWidthPx
                        val targetXPx =
                            if (wouldOverflowRight) anchorXPx - tooltipWidthPx - gapPx
                            else anchorXPx + gapPx

                        val animatedX by animateFloatAsState(
                            targetValue = targetXPx,
                            label = "tooltipX"
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .zIndex(999f)
                        ) {
                            ChartTooltip(
                                chartMark = spec.chartMark,
                                unit = unit,
                                color = barColor,
                                modifier = Modifier
                                    .offset(
                                        x = with(density) { animatedX.toDp() },
                                        y = with(density) { anchorYPx.toDp() } - 80.dp
                                    )
                                    .onSizeChanged { measuredWidthPx = it.width.toFloat() }
                            )
                        }
                    }

                    // X-axis label
                    if (xLabel.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(bottom = 4.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = xLabel,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Right fixed axis pane
                if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                    Canvas(
                        modifier = Modifier
                            .width(yAxisFixedWidth)
                            .fillMaxHeight()
                    ) {
                        chartMetrics?.let { m ->
                            ChartDraw.drawYAxisStandalone(
                                drawScope = this,
                                metrics = m,
                                yAxisPosition = yAxisPosition,
                                paneWidthPx = size.width
                            )
                        }
                    }
                }
            }

            // Y-axis label
            if (showYAxis && yLabel.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun AxisLabelX(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun AxisLabelY(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.graphicsLayer(rotationZ = -90f)
    )
}

// Function for fixed external Y-axis in paged range bar chart
@Composable
private fun FixedPagerYAxisRange(
    minY: Double,
    maxY: Double,
    yAxisPosition: YAxisPosition,
    step: Double,
    width: Dp
) {
    Canvas(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
    ) {
        val m = ChartMath.computeMetrics(
            size = size,
            values = listOf(minY, maxY),
            chartType = ChartType.RANGE_BAR,
            minY = minY,
            maxY = maxY,
            includeYAxisPadding = false,
            fixedTickStep = step
        )
        // Call standalone y-axis drawing function
        ChartDraw.drawYAxisStandalone(
            drawScope = this,
            metrics = m,
            yAxisPosition = yAxisPosition,
            paneWidthPx = size.width
        )
    }
}

@Composable
private fun RangeBarChartPagedInternal(
    modifier: Modifier,
    data: List<RangeChartMark>,
    title: String,
    barColor: Color,
    barWidthRatio: Float,
    yAxisPosition: YAxisPosition,
    interactionType: InteractionType.RangeBar,
    onBarClick: ((Int, RangeChartMark) -> Unit)?,
    showTitle: Boolean,
    showYAxis: Boolean,
    maxXTicksLimit: Int? = null,
    xLabelAutoSkip: Boolean,
    unit: String,
    pointValues: List<List<Double>>? = null, // one list per x index; null = no points
    pointColor: Color = barColor,
    pointRadius: Dp = 3.dp,
    // scale/paging
    pageSize: Int,
    yTickStep: Double?,
    initialPageIndex: Int?,
    outerPadding: PaddingValues,
    yAxisFixedWidth: Dp = 0.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
) {
    val pageCount = remember(data.size, pageSize) {
        kotlin.math.ceil(data.size / pageSize.toFloat()).toInt()
    }
    val firstPage = initialPageIndex ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    // Compute unified Y-axis range (shared across pages)
    val yAxisRange = remember(data, yTickStep) {
        val minValues = data.map { it.minPoint.y }
        val maxValues = data.map { it.maxPoint.y }
        val allValues = minValues + maxValues
        ChartMath.computeYAxisRange(
            values = allValues,
            chartType = ChartType.RANGE_BAR,
            minY = null,
            maxY = null,
            fixedTickStep = yTickStep
        )
    }

    val minRounded = yAxisRange.minY
    val maxRounded = yAxisRange.maxY
    val effectiveTickStep = yAxisRange.tickStep

    Column(modifier = modifier.padding(outerPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
        }

        Row(Modifier.fillMaxWidth()) {
            // Left fixed external Y-axis
            if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                FixedPagerYAxisRange(
                    minY = minRounded,
                    maxY = maxRounded,
                    yAxisPosition = YAxisPosition.LEFT,
                    step = effectiveTickStep,
                    width = yAxisFixedWidth
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) { page ->
                val start = page * pageSize
                val end = kotlin.math.min(start + pageSize, data.size)

                val pageSlice = data.subList(start, end)

                // Slice points to match visible bars
                val pagePoints = pointValues?.let { pts ->
                    if (pts.size >= end) pts.subList(start, end) else pts.drop(start).take(end - start)
                }

                // Render the page directly
                RangeBarChartContent(
                    modifier = Modifier.fillMaxSize(),
                    rangeData = pageSlice,
                    xLabel = "",                 // keep the page clean; outer container can show a label later
                    yLabel = "",
                    title = title,
                    barColor = barColor,
                    barWidthRatio = barWidthRatio,
                    yAxisPosition = yAxisPosition,
                    interactionType = interactionType,
                    onBarClick = onBarClick?.let { cb ->
                        { localIdx, rangePoint -> cb(start + localIdx, rangePoint) }
                    },
                    showTitle = false,
                    showYAxis = false,            // external axis handles it
                    xLabelAutoSkip = xLabelAutoSkip,
                    maxXTicksLimit = maxXTicksLimit,
                    yTickStep = effectiveTickStep,
                    unit = unit,
                    pointValues = pagePoints,
                    pointColor = pointColor,
                    pointRadius = pointRadius,
                    barCornerRadiusFraction = barCornerRadiusFraction,
                    barCornerRadiusFractions = barCornerRadiusFractions,
                    roundTopOnly = roundTopOnly,
                    windowSize = null,            // no inner scroll per page
                    contentPadding = PaddingValues(0.dp),
                    yAxisFixedWidth = 0.dp,
                )
            }

            // Right fixed external Y-axis
            if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                FixedPagerYAxisRange(
                    minY = minRounded,
                    maxY = maxRounded,
                    yAxisPosition = YAxisPosition.RIGHT,
                    step = effectiveTickStep,
                    width = yAxisFixedWidth
                )
            }
        }
    }
}
