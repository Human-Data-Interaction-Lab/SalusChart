package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.VerticalAxisLabel
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.ui.theme.LocalSalusChartColors

/**
 * Displays a line chart connecting the provided data points, with support for interactive
 * point tooltips, optional horizontal scrolling, paged navigation, a configurable Y-axis,
 * reference lines, and a legend.
 *
 * When [pageSize] is set and the data exceeds that value, the chart automatically switches to a
 * horizontally pageable layout with a unified Y-axis across all pages.
 *
 * @param modifier Modifier applied to the outermost layout container.
 * @param data List of [ChartMark] entries to plot as a connected line. Returns early if empty.
 * @param xLabel Text label displayed below the X-axis.
 * @param yLabel Text label displayed alongside the Y-axis.
 * @param title Chart title shown above the chart area when [showTitle] is true.
 * @param lineColor Color of the drawn line and default tooltip background.
 * @param strokeWidth Thickness of the line in pixels.
 * @param minY Optional lower bound for the Y-axis; computed from data when null.
 * @param maxY Optional upper bound for the Y-axis; computed from data when null.
 * @param xLabelTextSize Text size in pixels for X-axis tick labels.
 * @param tooltipTextSize Text size in pixels for the tooltip label.
 * @param yAxisPosition Side on which the Y-axis is drawn ([YAxisPosition.LEFT] or [YAxisPosition.RIGHT]).
 * @param interactionType Controls point tap behaviour: [InteractionType.Line.POINT] makes each dot
 *   tappable; [InteractionType.Line.TOUCH_AREA] uses invisible vertical strip regions instead.
 * @param pointRadius Outer and inner radii (as [Dp]) for the selected-point indicator ring.
 * @param showLegend Whether to display a legend entry near the chart.
 * @param legendLabel Text shown in the legend for this data series.
 * @param legendPosition Where to place the legend relative to the chart.
 * @param xLabelAutoSkip When true, overlapping X-axis tick labels are automatically skipped.
 * @param maxXTicksLimit Optional cap on the number of X-axis tick labels rendered.
 * @param referenceLines Horizontal reference lines drawn across the plot area.
 * @param showYAxisHighlight When true, reference-line values are highlighted on the Y-axis.
 * @param showTitle Whether to render the [title] above the chart.
 * @param showYAxis Whether to draw the Y-axis line and tick labels.
 * @param showPoint Whether to draw a dot marker at each data point.
 * @param showValue Whether to draw the numeric value label next to each data point.
 * @param yTickStep Fixed interval between Y-axis grid lines; auto-calculated when null.
 * @param unit Unit string appended to tooltip values (e.g. "kg", "bpm").
 * @param windowSize Number of data points visible at once in free-scroll mode; enables horizontal
 *   scrolling when smaller than the total data size. Mutually exclusive with [pageSize].
 * @param contentPadding Padding applied around the chart content area.
 * @param pageSize Number of data points shown per page; enables the pager when data exceeds this
 *   value. Mutually exclusive with [windowSize].
 * @param unifyYAxisAcrossPages When true, all pages share the same Y-axis range in paged mode.
 * @param initialPageIndex Page to display first in paged mode; defaults to the last page when null.
 * @param renderTooltipExternally When true, the tooltip is rendered as an overlay composable outside
 *   the Canvas, avoiding clipping issues near chart edges.
 * @param yAxisFixedWidth Width reserved for the external Y-axis pane in scrolling mode.
 * @param includeYAxisPaddingOverride Overrides the automatic decision of whether to include internal
 *   horizontal padding for the Y-axis; pass null to use the default behaviour.
 * @param onMetricsCalculated Optional callback invoked with the computed [ChartMath.ChartMetrics]
 *   after each layout pass, useful for synchronising an external Y-axis pane.
 * @param tooltipColor Background color of the tooltip bubble; defaults to [lineColor].
 */
@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Line Chart Example",
    lineColor: Color = Color.Unspecified,
    strokeWidth: Float = 4f,
    minY: Double? = null,
    maxY: Double? = null,
    xLabelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Line = InteractionType.Line.POINT,
    pointRadius: Pair<Dp, Dp> = Pair(4.dp, 2.dp),
    showLegend: Boolean = false,
    legendLabel: String = "",
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showPoint: Boolean = false,
    showValue: Boolean = false,
    yTickStep: Double? = null,
    unit: String = "",
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    initialPageIndex: Int? = null,
    renderTooltipExternally: Boolean = true,
    yAxisFixedWidth: Dp = 30.dp,
    includeYAxisPaddingOverride: Boolean? = null,
    onMetricsCalculated: ((ChartMath.ChartMetrics) -> Unit)? = null,
    tooltipColor: Color = Color.Unspecified,
) {
    if (data.isEmpty()) return

    val lineColor = lineColor.takeIf { it != Color.Unspecified } ?: LocalSalusChartColors.current.primary
    val tooltipColor = tooltipColor.takeIf { it != Color.Unspecified } ?: lineColor

    // Validate that scrolling and paging modes are not both enabled
    require(!(windowSize != null && pageSize != null)) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)
    val enablePaging = requestedPageSize > 0 && data.size > requestedPageSize

    if (enablePaging) {
        LineChartPagedInternal(
            modifier = modifier,
            data = data,
            pageSize = requestedPageSize,
            title = title,
            xLabel = xLabel,
            yLabel = yLabel,
            lineColor = lineColor,
            strokeWidth = strokeWidth,
            xLabelTextSize = xLabelTextSize,
            tooltipTextSize = tooltipTextSize,
            interactionType = interactionType,
            yAxisPosition = yAxisPosition,
            showPoint = showPoint,
            showValue = showValue,
            showYAxis = showYAxis,
            referenceLines = referenceLines,
            showYAxisHighlight = showYAxisHighlight,
            showTitle = showTitle,
            outerPadding = contentPadding,
            unifyYAxisAcrossPages = unifyYAxisAcrossPages,
            yTickStep = yTickStep,
            initialPageIndex = initialPageIndex,
            minY = minY,
            maxY = maxY,
            unit = unit,
            yAxisFixedWidth = yAxisFixedWidth,
            maxXTicksLimit = maxXTicksLimit,
            xLabelAutoSkip = xLabelAutoSkip,
            showLegend = showLegend,
            legendLabel = legendLabel,
            legendPosition = legendPosition,
        )
        return
    }

    val chartType = ChartType.LINE
    val useScrolling = windowSize != null && windowSize < data.size
    val isFixedYAxis = showYAxis && useScrolling
    val scrollState = rememberScrollState()

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }
        BoxWithConstraints(Modifier.weight(1f)) {
            val availableWidth = maxWidth
            val marginHorizontal = 16.dp
            val yAxisPaddingPxValue = with(LocalDensity.current) { yAxisFixedWidth.toPx() }

            // Calculate canvas width for scrolling mode
            val canvasWidth = if (useScrolling) {
                val chartWidth = availableWidth - (marginHorizontal * 2)
                val sectionsCount = (data.size.toFloat() / windowSize!!.toFloat()).toInt()
                chartWidth * sectionsCount
            } else null

            val xLabels = data.map { it.label ?: it.x.toString() }
            val yValues = data.map { it.y }

            var canvasPoints by remember { mutableStateOf(listOf<androidx.compose.ui.geometry.Offset>()) }
            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
            var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
            var chartPaneCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
            var canvasSize by remember { mutableStateOf(Size.Zero) }

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

                val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
                val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
                val startPad = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) || hasLeftLabel) 0.dp else marginHorizontal
                val endPad   = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) || hasRightLabel) 0.dp else marginHorizontal

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .let { if (useScrolling) it.horizontalScroll(scrollState, overscrollEffect = null) else it }
                        .padding(start = startPad, end = endPad)
                        .onGloballyPositioned { chartPaneCoords = it }
                ) {
                    val includeYAxisPadding =
                        includeYAxisPaddingOverride ?: !isFixedYAxis
                    Canvas(
                        modifier = if (useScrolling) {
                            Modifier.width(canvasWidth!!).fillMaxHeight()
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ) {
                        canvasSize = size
                        val labelReservePx = if (referenceLines.any { it.showLabel || it.label != null }) {
                            (if (!includeYAxisPadding) 80.dp else 20.dp).toPx()
                        } else 0f
                        val metrics = ChartMath.computeMetrics(
                            size = Size(size.width - labelReservePx, size.height),
                            values = yValues,
                            chartType = chartType,
                            minY = minY,
                            maxY = maxY,
                            includeYAxisPadding = includeYAxisPadding,  // no inner L/R padding when fixed axis is used
                            yAxisPaddingPx = yAxisPaddingPxValue,
                            fixedTickStep = yTickStep,
                            paddingBottom = 10f
                        )
                        chartMetrics = metrics
                        onMetricsCalculated?.invoke(metrics)

                        ChartDraw.drawGrid(this, size, metrics, yAxisPosition, drawLabels = showYAxis && !isFixedYAxis)
                        if (showYAxis && !isFixedYAxis)
                            ChartDraw.drawYAxis(this, metrics, yAxisPosition)

                        val points = ChartMath.Line.mapLineToCanvasPoints(data, size, metrics)
                        canvasPoints = points
                        ChartDraw.Line.drawLine(this, points, lineColor, strokeWidth)
                        ChartDraw.Line.drawLineXAxisLabels(
                            ctx = drawContext,
                            labels = xLabels,
                            metrics = metrics,
                            textSize = xLabelTextSize,
                            maxXTicksLimit = maxXTicksLimit,
                            xLabelAutoSkip = xLabelAutoSkip
                        )

                        val canvasForLabels = Size(
                            width  = chartMetrics!!.paddingX + chartMetrics!!.chartWidth,
                            height = chartMetrics!!.chartHeight
                        )
                        ChartMath.Line.computeLabelAnchors(
                            points = points,
                            values = yValues.map { it.toFloat() },
                            canvas = canvasForLabels,
                            textPx = with(drawContext.density) { 12.sp.toPx() },
                            padPx  = with(drawContext.density) { 2.dp.toPx() },
                            minGapToLinePx = with(drawContext.density) { 6.dp.toPx() },
                            passes = 6,
                            strokeWidthPx = strokeWidth,
                            edgeMarginPx  = with(drawContext.density) { 8.dp.toPx() }
                        )
                    }

                    when (interactionType) {
                        InteractionType.Line.TOUCH_AREA -> {
                            if (canvasSize != Size.Zero && canvasPoints.isNotEmpty()) {
                                ChartDraw.Scatter.PointMarker(
                                    data = data,
                                    points = canvasPoints,
                                    values = yValues,
                                    color = lineColor,
                                    showPoint = showPoint,
                                    selectedPointIndex = selectedPointIndex,
                                    onPointClick = null,
                                    pointRadius = pointRadius.first,
                                    innerRadius = pointRadius.second,
                                    interactive = false,
                                    chartType = chartType,
                                    showValue = showValue,
                                    showTooltipForIndex = if (renderTooltipExternally) null else selectedPointIndex,
                                    canvasSize = canvasSize,
                                )
                            }

                            chartMetrics?.let { metrics ->
                                ChartDraw.Bar.BarMarker(
                                    data = data,
                                    minValues = List(yValues.size) { metrics.minY },
                                    maxValues = yValues,
                                    metrics = metrics,
                                    useLineChartPositioning = true,
                                    onBarClick = { index, _ ->
                                        selectedPointIndex = if (selectedPointIndex == index) null else index
                                    },
                                    isTouchArea = true,
                                    chartType = chartType,
                                    showTooltipForIndex = if (renderTooltipExternally) null else selectedPointIndex
                                )
                            }
                        }

                        InteractionType.Line.POINT -> {
                            if (canvasSize != Size.Zero && canvasPoints.isNotEmpty()) {
                                ChartDraw.Scatter.PointMarker(
                                    data = data,
                                    points = canvasPoints,
                                    values = yValues,
                                    color = lineColor,
                                    showPoint = showPoint,
                                    selectedPointIndex = selectedPointIndex,
                                    onPointClick = { index ->
                                        selectedPointIndex = if (selectedPointIndex == index) null else index
                                    },
                                    pointRadius = pointRadius.first,
                                    innerRadius = pointRadius.second,
                                    interactive = true,
                                    chartType = chartType,
                                    showValue = showValue,
                                    showTooltipForIndex = if (renderTooltipExternally) null else selectedPointIndex,
                                    canvasSize = canvasSize
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

                    // External tooltip overlay: rendered outside Canvas to avoid clipping near edges
                    if (renderTooltipExternally &&
                        selectedPointIndex != null &&
                        selectedPointIndex in canvasPoints.indices
                    ) {
                        val i = selectedPointIndex!!
                        val pt = canvasPoints[i]
                        val density = LocalDensity.current
                        val pad = with(density) { 8.dp.toPx() }
                        val estimatedW = with(density) { 160.dp.toPx() }
                        val estimatedH = with(density) { 64.dp.toPx() }

                        // X: right of point, clamped so tooltip stays within canvas
                        val xClamped = (pt.x + pad)
                            .coerceAtMost((canvasSize.width - estimatedW).coerceAtLeast(0f))
                        // Y: above point if room, else below
                        val yPlaced = if (pt.y - pad - estimatedH >= 0f)
                            pt.y - pad - estimatedH
                        else
                            pt.y + pad

                        Box(modifier = Modifier.matchParentSize().zIndex(2f)) {
                            ChartTooltip(
                                chartMark = data[i],
                                unit = unit,
                                modifier = Modifier.offset { IntOffset(xClamped.toInt(), yPlaced.toInt()) },
                                color = tooltipColor
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
            val xLabelSpacerHeight = with(LocalDensity.current) { (50f + xLabelTextSize).toDp() }
            Spacer(Modifier.height(xLabelSpacerHeight))
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val leftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val rightOffset = (if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)
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
            val legendLeftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val legendRightOffset = (if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)
            when (legendPosition) {
                LegendPosition.TOP, LegendPosition.BOTTOM -> {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        if (legendLeftOffset > 0.dp) Spacer(Modifier.width(legendLeftOffset))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            ChartLegend(
                                labels = listOf(legendLabel),
                                colors = listOf(lineColor),
                                position = legendPosition
                            )
                        }
                        if (legendRightOffset > 0.dp) Spacer(Modifier.width(legendRightOffset))
                    }
                }
                LegendPosition.LEFT -> {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        ChartLegend(
                            labels = listOf(legendLabel),
                            colors = listOf(lineColor),
                            position = LegendPosition.LEFT
                        )
                    }
                }
                LegendPosition.RIGHT -> {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        ChartLegend(
                            labels = listOf(legendLabel),
                            colors = listOf(lineColor),
                            position = LegendPosition.RIGHT
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun LineChartPagedInternal(
    modifier: Modifier,
    data: List<ChartMark>,
    pageSize: Int,
    title: String,
    xLabel: String,
    yLabel: String,
    lineColor: Color,
    strokeWidth: Float,
    xLabelTextSize: Float,
    tooltipTextSize: Float,
    interactionType: InteractionType.Line,
    yAxisPosition: YAxisPosition,
    showPoint: Boolean,
    showValue: Boolean,
    showYAxis: Boolean,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    showTitle: Boolean,
    unifyYAxisAcrossPages: Boolean,
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
    legendLabel: String = "",
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
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
            values = data.map { it.y },
            chartType = ChartType.LINE,
            minY = minY,
            maxY = maxY,
            fixedTickStep = yTickStep
        )
    }

    val minRounded = yAxisRange.minY
    val maxRounded = yAxisRange.maxY
    val effectiveTickStep = yAxisRange.tickStep

    var sharedMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

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
                FixedPagerYAxisLine(
                    metrics = sharedMetrics,
                    yAxisPosition = yAxisPosition,
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

                LineChart(
                    modifier = Modifier.fillMaxSize(),
                    data = slice,
                    xLabel = "",
                    yLabel = "",
                    title = title,
                    lineColor = lineColor,
                    strokeWidth = strokeWidth,
                    xLabelTextSize = xLabelTextSize,
                    tooltipTextSize = tooltipTextSize,
                    yAxisPosition = yAxisPosition,
                    interactionType = interactionType,
                    showPoint = showPoint,
                    showValue = showValue,
                    maxXTicksLimit = maxXTicksLimit,
                    xLabelAutoSkip = xLabelAutoSkip,
                    referenceLines = referenceLines,
                    minY = if (unifyYAxisAcrossPages) minRounded else minY,
                    maxY = if (unifyYAxisAcrossPages) maxRounded else maxY,
                    yTickStep = if (unifyYAxisAcrossPages) effectiveTickStep else yTickStep,
                    showTitle = false,
                    showYAxis = false,
                    windowSize = null,
                    pageSize = null,
                    contentPadding = PaddingValues(0.dp),
                    unit = unit,
                    includeYAxisPaddingOverride = false,
                    onMetricsCalculated = { sharedMetrics = it },
                )
            }

            if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                FixedPagerYAxisLine(
                    metrics = sharedMetrics,
                    yAxisPosition = yAxisPosition,
                    width = yAxisFixedWidth,
                    referenceLines = if (showYAxisHighlight) referenceLines else emptyList()
                )
            }
            if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                VerticalAxisLabel(yLabel)
            }
        }
        if (xLabel.isNotBlank()) {
            val xLabelSpacerHeight = with(LocalDensity.current) { (50f + xLabelTextSize).toDp() }
            Spacer(Modifier.height(xLabelSpacerHeight))
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val leftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (showYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val rightOffset = (if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)
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
            val legendLeftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (showYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val legendRightOffset = (if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                if (legendLeftOffset > 0.dp) Spacer(Modifier.width(legendLeftOffset))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ChartLegend(
                        labels = listOf(legendLabel),
                        colors = listOf(lineColor),
                        position = LegendPosition.BOTTOM
                    )
                }
                if (legendRightOffset > 0.dp) Spacer(Modifier.width(legendRightOffset))
            }
        }
    }
}

@Composable
private fun FixedPagerYAxisLine(
    metrics: ChartMath.ChartMetrics?,
    yAxisPosition: YAxisPosition,
    width: Dp,
    referenceLines: List<ReferenceLineSpec> = emptyList()
) {
    Canvas(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
    ) {
        metrics?.let { m ->
            if (referenceLines.isNotEmpty()) {
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
