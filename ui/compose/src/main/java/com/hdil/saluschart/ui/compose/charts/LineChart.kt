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
import com.hdil.saluschart.ui.theme.ChartColor

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Line Chart Example",
    lineColor: Color = ChartColor.Default,
    strokeWidth: Float = 4f,
    minY: Double? = null, // Minimum Y value for chart
    maxY: Double? = null, // Maximum Y value for chart
    xLabelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Line = InteractionType.Line.POINT,
    pointRadius: Pair<Dp, Dp> = Pair(4.dp, 2.dp),
    showLegend: Boolean = false,
    legendLabel: String = "",
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    xLabelAutoSkip: Boolean = true, // Automatically skip labels if they overlap
    maxXTicksLimit: Int? = null, // Maximum number of x-axis labels to display
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    // Display
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showPoint: Boolean = false,
    showValue: Boolean = false, // TODO: showValue (computeLabelAnchors 사용) 항상 false
    yTickStep: Double? = null, // y-axis grid tick step (automatically calculated if null)
    unit: String = "",
    // Scroll/Page
    windowSize: Int? = null, // visible items in scroll window
    contentPadding: PaddingValues = PaddingValues(16.dp), // Free-scroll paddings
    pageSize: Int? = null, // items per page
    unifyYAxisAcrossPages: Boolean = true,
    initialPageIndex: Int? = null, // initial page to show (last page if null)
    renderTooltipExternally: Boolean = true, // Whether to render tooltip outside the chart canvas
    yAxisFixedWidth: Dp = 30.dp, // Padding between the chart and the y-axis
    includeYAxisPaddingOverride: Boolean? = null,
    onMetricsCalculated: ((ChartMath.ChartMetrics) -> Unit)? = null,
    tooltipColor: Color = lineColor,
    ) {
    if (data.isEmpty()) return

    // Validate that scrolling and paging modes are not both enabled
    require(!(windowSize != null && pageSize != null)) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    // Compute effective page size (0 = off)
    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)

    // Enable paging if pageSize is provided and data exceeds page size
    val enablePaging = requestedPageSize > 0 && data.size > requestedPageSize

    if (enablePaging) {
        LineChartPagedInternal(
            modifier = modifier,
            data = data,
            pageSize = requestedPageSize,
            // visuals
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
            // scale/paging
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
                // Left fixed axis pane
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

                // Calculate padding when Y-axis is hidden (external axis handles it), fixed axis, or VerticalAxisLabel already provides margin
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
                        if (showYAxis && !isFixedYAxis) ChartDraw.drawYAxis(this, metrics, yAxisPosition)

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
                        // TODO: 이거 무슨 용도?
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

                    // Interactions & points
                    when (interactionType) {
                        InteractionType.Line.TOUCH_AREA -> {
                            // 1) 먼저 점/레이블만 그리기 (비인터랙티브)
                            if (canvasSize != Size.Zero && canvasPoints.isNotEmpty()) {
                                ChartDraw.Scatter.PointMarker(
                                    data = data,
                                    points = canvasPoints,
                                    values = yValues,
                                    color = lineColor,
                                    showPoint = showPoint,
                                    selectedPointIndex = selectedPointIndex,
                                    onPointClick = null,                 // ← 클릭 없음
                                    pointRadius = pointRadius.first,
                                    innerRadius = pointRadius.second,
                                    interactive = false,                 // ← 꼭 false 유지
                                    chartType = chartType,
                                    showValue = showValue,
                                    showTooltipForIndex = if (renderTooltipExternally) null else selectedPointIndex,
                                    canvasSize = canvasSize,
                                )
                            }

                            // 2) 그 위에 터치 영역(수직 스트립) 올리기
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

                    // Draw reference lines
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

                    // External tooltip overlay (top-most, no size-measurement cycle)
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

                // Right fixed axis pane
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

// TODO: unifyYAxisAcrossPages 의미?
// Function for paged line chart
@Composable
private fun LineChartPagedInternal(
    modifier: Modifier,
    data: List<ChartMark>,
    pageSize: Int,
    // visuals
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
    // scale/paging
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

    // metrics shared with the external Y-axis
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
            // Left fixed external Y-axis
            if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                FixedPagerYAxisLine(
                    metrics = sharedMetrics,
                    yAxisPosition = yAxisPosition,
                    width = yAxisFixedWidth,
                    referenceLines = if (showYAxisHighlight) referenceLines else emptyList()
                )
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val start = page * pageSize
                val end = kotlin.math.min(start + pageSize, data.size)
                val slice = data.subList(start, end)

                // Render a normal LineChart page:
                // - no inner scroll (windowSize = null)
                // - maxY unified with the external axis
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

            // Right fixed external Y-axis
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

// Function for fixed external Y-axis in paged line chart
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
