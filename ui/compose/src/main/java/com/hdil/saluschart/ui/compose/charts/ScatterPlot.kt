package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.PointType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.TooltipContainer
import com.hdil.saluschart.core.chart.chartDraw.VerticalAxisLabel
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.ui.theme.LocalSalusChartColors

/**
 * Displays a scatter plot for the provided data points, with support for interactive tooltips,
 * optional horizontal scrolling, paged navigation, a configurable Y-axis, reference lines,
 * and a legend.
 *
 * Two interaction modes are available: [InteractionType.Scatter.POINT] selects individual points,
 * while [InteractionType.Scatter.TOUCH_AREA] groups all points sharing the same X value under a
 * single wide tap strip and shows a combined tooltip.
 *
 * When [pageSize] is set and the data exceeds that value, the chart automatically switches to a
 * horizontally pageable layout with a shared Y-axis across all pages.
 *
 * @param modifier Modifier applied to the outermost layout container.
 * @param data List of [ChartMark] entries to render as scatter points. Returns early if empty.
 * @param xLabel Text label displayed below the X-axis.
 * @param yLabel Text label displayed alongside the Y-axis.
 * @param title Chart title shown above the chart area when [showTitle] is true.
 * @param pointColor Fill color applied to all scatter points.
 * @param pointType Shape of each scatter point (e.g. [PointType.Circle]).
 * @param pointSize Diameter of each scatter point in [Dp].
 * @param minY Optional lower bound for the Y-axis; computed from data when null.
 * @param maxY Optional upper bound for the Y-axis; computed from data when null.
 * @param tooltipTextSize Text size in pixels for the tooltip label.
 * @param yAxisPosition Side on which the Y-axis is drawn ([YAxisPosition.LEFT] or [YAxisPosition.RIGHT]).
 * @param interactionType Controls point tap behaviour: [InteractionType.Scatter.POINT] for individual
 *   point selection; [InteractionType.Scatter.TOUCH_AREA] for column-wide selection with a combined tooltip.
 * @param referenceLines Horizontal reference lines drawn across the plot area.
 * @param showYAxisHighlight When true, reference-line values are highlighted on the Y-axis.
 * @param showTitle Whether to render the [title] above the chart.
 * @param showYAxis Whether to draw the Y-axis line and tick labels.
 * @param xLabelAutoSkip When true, overlapping X-axis tick labels are automatically skipped.
 * @param maxXTicksLimit Optional cap on the number of X-axis tick labels rendered.
 * @param yTickStep Fixed interval between Y-axis grid lines; auto-calculated when null.
 * @param unit Unit string appended to tooltip values (e.g. "kg", "bpm").
 * @param windowSize Number of data points visible at once in free-scroll mode; enables horizontal
 *   scrolling when smaller than the total data size. Mutually exclusive with [pageSize].
 * @param contentPadding Padding applied around the chart content area.
 * @param pageSize Number of data points shown per page; enables the pager when data exceeds this
 *   value. Mutually exclusive with [windowSize].
 * @param initialPageIndex Page to display first in paged mode; defaults to the last page when null.
 * @param yAxisFixedWidth Width reserved for the external Y-axis pane in scroll mode.
 * @param showLegend Whether to display a legend entry near the chart.
 * @param legendPosition Where to place the legend relative to the chart.
 * @param legendLabel Text shown in the legend for this data series.
 * @param tooltipColor Background color of tooltip bubbles; defaults to [pointColor].
 */
@Composable
fun ScatterPlot(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "X Axis",
    yLabel: String = "Y Axis",
    title: String = "Scatter Plot Example",
    pointColor: Color = Color.Unspecified,
    pointType: PointType = PointType.Circle,
    pointSize: Dp = 8.dp,
    minY: Double? = null,
    maxY: Double? = null,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Scatter = InteractionType.Scatter.POINT,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    yTickStep: Double? = null,
    unit: String = "",
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 30.dp,
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    legendLabel: String = "",
    tooltipColor: Color = Color.Unspecified,
) {
    if (data.isEmpty()) return

    val pointColor = pointColor.takeIf { it != Color.Unspecified } ?: LocalSalusChartColors.current.primary
    val tooltipColor = tooltipColor.takeIf { it != Color.Unspecified } ?: pointColor

    // Validate that scrolling and paging modes are not both enabled
    require(!(windowSize != null && pageSize != null)) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    val chartType = ChartType.SCATTERPLOT
    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)
    val enablePaging = requestedPageSize > 0 && data.size > requestedPageSize

    if (enablePaging) {
        ScatterPlotPagedInternal(
            modifier = modifier,
            data = data,
            pageSize = requestedPageSize,
            title = title,
            xLabel = xLabel,
            yLabel = yLabel,
            pointColor = pointColor,
            pointType = pointType,
            pointSize = pointSize,
            tooltipTextSize = tooltipTextSize,
            interactionType = interactionType,
            yAxisPosition = yAxisPosition,
            showYAxis = showYAxis,
            showTitle = showTitle,
            outerPadding = contentPadding,
            yTickStep = yTickStep,
            initialPageIndex = initialPageIndex,
            minY = minY,
            maxY = maxY,
            unit = unit,
            yAxisFixedWidth = yAxisFixedWidth,
            maxXTicksLimit = maxXTicksLimit,
            xLabelAutoSkip = xLabelAutoSkip,
            showLegend = showLegend,
            legendPosition = legendPosition,
            legendLabel = legendLabel,
            referenceLines = referenceLines,
            showYAxisHighlight = showYAxisHighlight,
            tooltipColor = tooltipColor,
        )
        return
    }

    val useScrolling = windowSize != null && windowSize < data.size
    val isFixedYAxis = showYAxis && useScrolling
    val scrollState = rememberScrollState()

    // Deduplicate labels: scatter plots may have multiple points sharing the same x-value
    val xLabels = data.map { it.label ?: it.x.toString() }.distinct()
    val yValues = data.map { it.y }

    var canvasPoints by remember { mutableStateOf(listOf<Offset>()) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
    
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var selectedIndices by remember { mutableStateOf<Set<Int>?>(null) }

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        BoxWithConstraints(Modifier.weight(1f)) {
            val availableWidth = maxWidth
            val marginHorizontal = 16.dp

            // Calculate canvas width for scrolling mode
            val canvasWidth = if (useScrolling) {
                val chartWidth = availableWidth - (marginHorizontal * 2)
                val ws = requireNotNull(windowSize)
                val sectionsCount = (data.size.toFloat() / ws.toFloat()).toInt()
                chartWidth * sectionsCount
            } else null

            Row(Modifier.fillMaxSize()) {
                if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                    VerticalAxisLabel(yLabel)
                }
                if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) {
                    Canvas(
                        modifier = Modifier
                            .width(yAxisFixedWidth)
                            .fillMaxHeight()
                            .clipToBounds()
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

                val hasLeftLabel  = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
                val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
                val startPad = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) || hasLeftLabel) 0.dp else marginHorizontal
                val endPad   = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) || hasRightLabel) 0.dp else marginHorizontal

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
                            (if (isFixedYAxis) 80.dp else 30.dp).toPx()
                        } else 0f
                        val metrics = ChartMath.computeMetrics(
                            size = Size(size.width - labelReservePx, size.height),
                            values = yValues,
                            chartType = chartType,
                            minY = minY,
                            maxY = maxY,
                            includeYAxisPadding = !isFixedYAxis,
                            yAxisPaddingPx = 80f,
                            fixedTickStep = yTickStep,
                            paddingBottom = if (isFixedYAxis) 20f else 0f
                        )
                        val points = ChartMath.Scatter.mapScatterToCanvasPoints(data, size, metrics)

                        canvasPoints = points
                        canvasSize = size
                        chartMetrics = metrics

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
                        // X-axis labels use bar-style center alignment to align with point columns
                        ChartDraw.Bar.drawBarXAxisLabels(
                            ctx = drawContext,
                            labels = xLabels,
                            metrics = metrics,
                            maxXTicksLimit = maxXTicksLimit,
                            xLabelAutoSkip = xLabelAutoSkip
                        )
                    }

                    when (interactionType) {
                        InteractionType.Scatter.POINT -> {
                            selectedIndices = null
                            ChartDraw.Scatter.PointMarker(
                                data = data,
                                points = canvasPoints,
                                values = yValues,
                                color = pointColor,
                                selectedPointIndex = selectedPointIndex,
                                onPointClick = { index ->
                                    selectedPointIndex = if (selectedPointIndex == index) null else index
                                },
                                pointType = pointType,
                                chartType = chartType,
                                showTooltipForIndex = selectedPointIndex,
                                pointRadius = pointSize,
                                innerRadius = 0.dp,
                                interactive = true,
                                canvasSize = canvasSize,
                                unit = unit,
                            )
                        }

                        InteractionType.Scatter.TOUCH_AREA -> {
                            val metrics = chartMetrics
                            if (metrics != null && canvasPoints.isNotEmpty()) {
                                val categoryData = remember(data) {
                                    val uniqueXs = data.map { it.x }.distinct().sorted()
                                    val catCount = uniqueXs.size
                                    val catIndexToDataIndex = uniqueXs.map { ux ->
                                        data.indexOfFirst { it.x == ux }
                                    }
                                    val catMarks = uniqueXs.mapIndexed { idx, ux ->
                                        val firstIdx = catIndexToDataIndex[idx].coerceAtLeast(0)
                                        val src = data.getOrNull(firstIdx)
                                        ChartMark(
                                            x = ux,
                                            y = src?.y ?: 0.0,
                                            label = src?.label ?: ux.toString()
                                        )
                                    }
                                    Triple(uniqueXs, catCount, catMarks)
                                }
                                val (uniqueXs, catCount, catMarks) = categoryData

                                // Precompute per-X index map for O(1) category lookup on tap
                                val xToIndicesMap = remember(data) {
                                    data.indices.groupBy { data[it].x }
                                        .mapValues { it.value.toSet() }
                                }

                                ChartDraw.Bar.BarMarker(
                                    data = catMarks,
                                    minValues = List(catCount) { metrics.minY },
                                    maxValues = List(catCount) { metrics.maxY },
                                    metrics = metrics,
                                    color = Color.Transparent,
                                    barWidthRatio = 1.0f,
                                    interactive = true,
                                    onBarClick = { idx, _ ->
                                        val ux = uniqueXs.getOrNull(idx)
                                        val indicesInCat: Set<Int> = ux?.let { xToIndicesMap[it] } ?: emptySet()
                                        selectedPointIndex = null
                                        selectedIndices = if (indicesInCat.isNotEmpty() && selectedIndices == indicesInCat) {
                                            null       // tap again to deselect
                                        } else {
                                            indicesInCat
                                        }
                                    },
                                    chartType = chartType,
                                    isTouchArea = true,
                                    showLabel = false,
                                    unit = unit
                                )

                                ChartDraw.Scatter.PointMarker(
                                    data = data,
                                    points = canvasPoints,
                                    values = yValues,
                                    color = pointColor,
                                    selectedPointIndex = null,
                                    selectedIndices = selectedIndices,
                                    onPointClick = null,
                                    pointType = pointType,
                                    chartType = chartType,
                                    showTooltipForIndex = null,
                                    showTooltipForIndices = null, // built-in multi-tooltips disabled; combined tooltip rendered below
                                    pointRadius = pointSize,
                                    innerRadius = 0.dp,
                                    interactive = false,
                                    canvasSize = canvasSize,
                                    unit = unit,
                                )

                                // Combined tooltip rendered at high zIndex to appear above reference lines
                                if (!selectedIndices.isNullOrEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize().zIndex(999f)) {
                                        ScatterCombinedTooltip(
                                            indices = selectedIndices!!,
                                            data = data,
                                            points = canvasPoints,
                                            canvasSize = canvasSize,
                                            unit = unit,
                                            dotColor = tooltipColor,
                                        )
                                    }
                                }
                            } else {
                                // Metrics not yet available; render non-interactive points as a fallback
                                ChartDraw.Scatter.PointMarker(
                                    data = data,
                                    points = canvasPoints,
                                    values = yValues,
                                    color = pointColor,
                                    selectedPointIndex = null,
                                    selectedIndices = null,
                                    onPointClick = null,
                                    pointType = pointType,
                                    chartType = chartType,
                                    showTooltipForIndex = null,
                                    showTooltipForIndices = null,
                                    pointRadius = pointSize,
                                    innerRadius = 0.dp,
                                    interactive = false,
                                    canvasSize = canvasSize,
                                    unit = unit,
                                )
                            }
                        }
                    }

                    if (referenceLines.isNotEmpty()) {
                        chartMetrics?.let { metrics ->
                            ReferenceLine.ReferenceLines(
                                modifier = Modifier.fillMaxSize(),
                                specs = referenceLines,
                                data = data,
                                metrics = metrics,
                                chartType = chartType,
                                yAxisPosition = yAxisPosition,
                            )
                        }
                    }
                }

                if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                    Canvas(
                        modifier = Modifier
                            .width(yAxisFixedWidth)
                            .fillMaxHeight()
                            .clipToBounds()
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
                if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                    VerticalAxisLabel(yLabel)
                }
            }
        }

        if (xLabel.isNotBlank()) {
            val xLabelSpacerHeight = with(LocalDensity.current) { (50f + 28f).toDp() }
            Spacer(Modifier.height(xLabelSpacerHeight))
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val leftOffset = (if (hasLeftLabel) 30.dp else 0.dp) + (if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val rightOffset = (if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 30.dp else 0.dp)
            Row(Modifier.fillMaxWidth()) {
                if (leftOffset > 0.dp) Spacer(Modifier.width(leftOffset))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = xLabel, style = MaterialTheme.typography.bodySmall)
                }
                if (rightOffset > 0.dp) Spacer(Modifier.width(rightOffset))
            }
        }
        if (showLegend && legendLabel.isNotBlank()) {
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val legendLeftOffset = (if (hasLeftLabel) 30.dp else 0.dp) + (if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val legendRightOffset = (if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 30.dp else 0.dp)
            when (legendPosition) {
                LegendPosition.TOP, LegendPosition.BOTTOM -> {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        if (legendLeftOffset > 0.dp) Spacer(Modifier.width(legendLeftOffset))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            ChartLegend(
                                labels = listOf(legendLabel),
                                colors = listOf(pointColor),
                                position = legendPosition
                            )
                        }
                        if (legendRightOffset > 0.dp) Spacer(Modifier.width(legendRightOffset))
                    }
                }
                LegendPosition.LEFT -> {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        ChartLegend(labels = listOf(legendLabel), colors = listOf(pointColor), position = LegendPosition.LEFT)
                    }
                }
                LegendPosition.RIGHT -> {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        ChartLegend(labels = listOf(legendLabel), colors = listOf(pointColor), position = LegendPosition.RIGHT)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun ScatterCombinedTooltip(
    indices: Set<Int>,
    data: List<ChartMark>,
    points: List<Offset>,
    canvasSize: Size,
    unit: String,
    dotColor: Color = MaterialTheme.colorScheme.primary,
) {
    if (indices.isEmpty() || points.isEmpty()) return

    val density = LocalDensity.current

    val sorted = indices.sorted()
    val anchorIndex = sorted.firstOrNull() ?: return
    if (anchorIndex !in points.indices || anchorIndex !in data.indices) return

    val anchorPoint = points[anchorIndex]
    val marks = sorted.mapNotNull { data.getOrNull(it) }

    val padPx = with(density) { 8.dp.toPx() }
    val estimatedW = with(density) { 160.dp.toPx() }
    val estimatedH = with(density) { 84.dp.toPx() }

    // Center tooltip on the selected column; prefer above anchor, fall back below; clamp to canvas
    val xPx = (anchorPoint.x - estimatedW / 2f)
        .coerceIn(0f, (canvasSize.width - estimatedW).coerceAtLeast(0f))
    val preferredY = if (anchorPoint.y - estimatedH - padPx >= 0f)
        anchorPoint.y - estimatedH - padPx
    else
        anchorPoint.y + padPx
    val yPx = preferredY.coerceIn(0f, (canvasSize.height - estimatedH).coerceAtLeast(0f))

    TooltipContainer(
        modifier = Modifier.offset { IntOffset(xPx.toInt(), yPx.toInt()) }
    ) {
        Text(
            text = marks.firstOrNull()?.label.orEmpty(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 16.sp
        )
        marks.forEach { mark ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dotColor, shape = CircleShape)
                )
                Text(
                    text = if (unit.isNotEmpty()) "${mark.y.toInt()}$unit" else mark.y.toInt().toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScatterPlotPagedInternal(
    modifier: Modifier,
    data: List<ChartMark>,
    pageSize: Int,
    title: String,
    xLabel: String,
    yLabel: String,
    pointColor: Color,
    pointType: PointType,
    pointSize: Dp,
    tooltipTextSize: Float,
    interactionType: InteractionType.Scatter,
    yAxisPosition: YAxisPosition,
    showYAxis: Boolean,
    showTitle: Boolean,
    yTickStep: Double?,
    initialPageIndex: Int?,
    minY: Double?,
    maxY: Double?,
    unit: String,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    yAxisFixedWidth: Dp = 30.dp,
    maxXTicksLimit: Int? = null,
    xLabelAutoSkip: Boolean,
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    legendLabel: String = "",
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    tooltipColor: Color = pointColor,
) {
    val pageCount = remember(data.size, pageSize) {
        kotlin.math.ceil(data.size / pageSize.toFloat()).toInt()
    }
    val firstPage = initialPageIndex ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    // Compute unified Y-axis range using the lighter function (no pixel calculations)
    val yAxisRange = remember(data, minY, maxY, yTickStep) {
        val yValues = data.map { it.y }
        ChartMath.computeYAxisRange(
            values = yValues,
            chartType = ChartType.SCATTERPLOT,
            minY = minY,
            maxY = maxY,
            fixedTickStep = yTickStep
        )
    }

    val maxRounded = yAxisRange.maxY
    val effectiveTickStep = yAxisRange.tickStep

    Column(modifier = modifier.padding(outerPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
        }

        Row(Modifier.fillMaxWidth().weight(1f)) {
            if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                VerticalAxisLabel(yLabel)
            }
            if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                FixedPagerYAxisScatter(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = effectiveTickStep,
                    width = yAxisFixedWidth,
                    referenceLines = if (showYAxisHighlight) referenceLines else emptyList()
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val start = page * pageSize
                val end = kotlin.math.min(start + pageSize, data.size)
                val slice = data.subList(start, end)

                ScatterPlot(
                    modifier = Modifier.fillMaxSize(),
                    data = slice,
                    xLabel = "",
                    yLabel = "",
                    title = title,
                    pointColor = pointColor,
                    pointType = pointType,
                    pointSize = pointSize,
                    minY = minY,
                    maxY = maxRounded,
                    tooltipTextSize = tooltipTextSize,
                    yAxisPosition = yAxisPosition,
                    interactionType = interactionType,
                    showTitle = false,
                    showYAxis = false,
                    maxXTicksLimit = maxXTicksLimit,
                    xLabelAutoSkip = xLabelAutoSkip,
                    yTickStep = effectiveTickStep,
                    windowSize = null,
                    contentPadding = PaddingValues(
                        start = if (yAxisPosition == YAxisPosition.LEFT) 0.dp else 0.dp,
                        end = if (yAxisPosition == YAxisPosition.RIGHT) 0.dp else 0.dp,
                        top = 0.dp,
                        bottom = 0.dp
                    ),
                    pageSize = null,
                    unit = unit,
                    tooltipColor = tooltipColor,
                )
            }

            if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                FixedPagerYAxisScatter(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = effectiveTickStep,
                    width = yAxisFixedWidth,
                    referenceLines = if (showYAxisHighlight) referenceLines else emptyList()
                )
            }
            if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                VerticalAxisLabel(yLabel)
            }
        }
        if (xLabel.isNotBlank()) {
            val xLabelSpacerHeight = with(LocalDensity.current) { (50f + 28f).toDp() }
            Spacer(Modifier.height(xLabelSpacerHeight))
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val leftOffset = (if (hasLeftLabel) 30.dp else 0.dp) + (if (showYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val rightOffset = (if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 30.dp else 0.dp)
            Row(Modifier.fillMaxWidth()) {
                if (leftOffset > 0.dp) Spacer(Modifier.width(leftOffset))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = xLabel, style = MaterialTheme.typography.bodySmall)
                }
                if (rightOffset > 0.dp) Spacer(Modifier.width(rightOffset))
            }
        }
        if (showLegend && legendLabel.isNotBlank()) {
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val legendLeftOffset = (if (hasLeftLabel) 30.dp else 0.dp) + (if (showYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val legendRightOffset = (if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 30.dp else 0.dp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                if (legendLeftOffset > 0.dp) Spacer(Modifier.width(legendLeftOffset))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ChartLegend(
                        labels = listOf(legendLabel),
                        colors = listOf(pointColor),
                        position = LegendPosition.BOTTOM
                    )
                }
                if (legendRightOffset > 0.dp) Spacer(Modifier.width(legendRightOffset))
            }
        }
    }
}

@Composable
private fun FixedPagerYAxisScatter(
    maxY: Double,
    yAxisPosition: YAxisPosition,
    step: Double,
    width: Dp,
    referenceLines: List<ReferenceLineSpec> = emptyList()
) {
    Canvas(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
    ) {
        val m = ChartMath.computeMetrics(
            size = size,
            values = listOf(0.0, maxY),
            chartType = ChartType.SCATTERPLOT,
            minY = 0.0,
            maxY = maxY,
            includeYAxisPadding = false,
            fixedTickStep = step,
            paddingBottom = 20f
        )
        if (referenceLines.isNotEmpty()) {
            ChartDraw.drawYAxisStandaloneWithReferenceHighlights(
                drawScope = this, metrics = m, yAxisPosition = yAxisPosition,
                paneWidthPx = size.width, referenceLines = referenceLines
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
