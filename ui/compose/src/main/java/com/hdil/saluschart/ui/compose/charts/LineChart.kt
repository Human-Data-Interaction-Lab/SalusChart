package com.hdil.saluschart.ui.compose.charts

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.ui.theme.ChartColor

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,      // ChartMark 기반
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Line Chart Example",
    lineColor: Color = ChartColor.Default,
    strokeWidth: Float = 4f,
    minY: Double? = null,                    // 사용자 지정 최소 Y값
    maxY: Double? = null,                    // 사용자 지정 최대 Y값
    xLabelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Line = InteractionType.Line.POINT,
    pointRadius: Pair<Dp, Dp> = Pair(4.dp, 2.dp),
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Red,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null,
    // Display
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showPoint: Boolean = false,
    showValue: Boolean = false,
    yTickStep: Double? = null,
    unit: String = "",
    // Scroll/Page
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    initialPageIndex: Int? = null,
    renderTooltipExternally: Boolean = true,
    yAxisFixedWidth: Dp = 0.dp,
) {
    if (data.isEmpty()) return

    // Validate that scrolling and paging modes are not both enabled
    require(!(windowSize != null && pageSize != null)) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    // compute effective page size (0 = off)
    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)

    // enable paging if pageSize is provided and data exceeds page size
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
            xLabelAutoSkip = xLabelAutoSkip
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
        BoxWithConstraints {
            val availableWidth = maxWidth
            val marginHorizontal = 16.dp

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

                // LEFT fixed Y-axis pane
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

                // Use 0.dp padding when Y-axis is hidden (external axis handles it) or when it's a fixed axis on that side
                val startPad = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT)) 0.dp else marginHorizontal
                val endPad = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT)) 0.dp else marginHorizontal

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .let { if (useScrolling) it.horizontalScroll(scrollState, overscrollEffect = null) else it }
                        .padding(start = startPad, end = endPad)
                        .onGloballyPositioned { chartPaneCoords = it }
                ) {
                    Canvas(
                        modifier = if (useScrolling) {
                            Modifier.width(canvasWidth!!).fillMaxHeight()
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ) {
                        canvasSize = size
                        val metrics = ChartMath.computeMetrics(
                            size = size,
                            values = yValues,
                            chartType = chartType,
                            minY = minY,
                            maxY = maxY,
                            includeYAxisPadding = !isFixedYAxis,  // no inner L/R padding when fixed axis is used
                            fixedTickStep = yTickStep
                        )
                        chartMetrics = metrics

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

                    // Interactions / points
                    when (interactionType) {
                        InteractionType.Line.TOUCH_AREA -> {
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

                            // draw points/labels only when we actually know canvas size & have points
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
                                    canvasSize = canvasSize               // ← same here
                                )
                            }
                        }
                    }

                    // Reference line
                    if (referenceLineType != ReferenceLineType.NONE) {
                        chartMetrics?.let { metrics ->
                            ReferenceLine.ReferenceLine(
                                modifier = Modifier.fillMaxSize(),
                                data = data,
                                metrics = metrics,
                                chartType = chartType,
                                referenceLineType = referenceLineType,
                                color = referenceLineColor,
                                strokeWidth = referenceLineStrokeWidth,
                                lineStyle = referenceLineStyle,
                                showLabel = showReferenceLineLabel,
                                labelFormat = referenceLineLabelFormat,
                                yAxisPosition = yAxisPosition,
                                interactive = referenceLineInteractive,
                                onClick = onReferenceLineClick
                            )
                        }
                    }

                    // ---- External tooltip overlay (top-most, with clamping) ----
                    if (renderTooltipExternally &&
                        selectedPointIndex != null &&
                        selectedPointIndex in canvasPoints.indices
                    ) {
                        val i = selectedPointIndex!!
                        val pt = canvasPoints[i] // canvas-space point
                        val density = LocalDensity.current

                        var hostSize by remember { mutableStateOf(IntSize.Zero) }
                        var tipSize  by remember { mutableStateOf(IntSize.Zero) }

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .zIndex(2f)                    // above everything in this Box
                                .onSizeChanged { hostSize = it }
                        ) {
                            val pad = with(density) { 8.dp.toPx() }

                            // desired position: to the right and above the point
                            val desiredX = pt.x + pad
                            val desiredYAbove = pt.y - pad - tipSize.height

                            // if there isn't room above, place it below the point
                            val yPlaced = if (desiredYAbove < 0f) (pt.y + pad)
                            else desiredYAbove

                            // clamp inside the host box on both axes
                            val maxX = (hostSize.width  - tipSize.width ).coerceAtLeast(0)
                            val maxY = (hostSize.height - tipSize.height).coerceAtLeast(0)
                            val xClamped = desiredX.coerceIn(0f, maxX.toFloat())
                            val yClamped = yPlaced.coerceIn(0f, maxY.toFloat())

                            ChartTooltip(
                                ChartMark = data[i],
                                unit = unit,
                                modifier = Modifier
                                    .offset { IntOffset(xClamped.toInt(), yClamped.toInt()) }
                                    .onSizeChanged { tipSize = it },   // measure so clamping is exact
                                color = lineColor
                            )
                        }
                    }
                }

                // RIGHT fixed Y-axis pane
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
        }

        Spacer(Modifier.height(4.dp))
    }
}


// SJ_COMMENT: unifyYAxisAcrossPages 의미?
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
    // scale/paging
    showTitle: Boolean,
    unifyYAxisAcrossPages: Boolean,
    yTickStep: Double?,
    initialPageIndex: Int?,
    minY: Double?,
    maxY: Double?,
    unit: String,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    yAxisFixedWidth: Dp = 0.dp,
    maxXTicksLimit: Int? = null,
    xLabelAutoSkip: Boolean
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
            chartType = ChartType.LINE,
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

        Row(Modifier.fillMaxWidth()) {
            // LEFT fixed external Y-axis
            if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                FixedPagerYAxisLine(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = effectiveTickStep.toFloat(),
                    width = yAxisFixedWidth
                )
            }

            // pages area
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
                    xLabel = xLabel,
                    yLabel = yLabel,
                    title = title,
                    lineColor = lineColor,
                    strokeWidth = strokeWidth,
                    minY = minY,
                    maxY = maxRounded,
                    xLabelTextSize = xLabelTextSize,
                    tooltipTextSize = tooltipTextSize,
                    yAxisPosition = yAxisPosition,
                    interactionType = interactionType,
                    showPoint = showPoint,
                    showValue = showValue,
                    maxXTicksLimit = maxXTicksLimit,
                    xLabelAutoSkip = xLabelAutoSkip,
                    referenceLineType = ReferenceLineType.NONE,
                    showTitle = false,                    // external axis handles it
                    showYAxis = false,                    // external axis handles it
                    yTickStep = effectiveTickStep,        // keep grid aligned with external axis
                    windowSize = null,                    // no inner scroll
                    contentPadding = PaddingValues(
                        start = if (yAxisPosition == YAxisPosition.LEFT) 0.dp else 0.dp,
                        end = if (yAxisPosition == YAxisPosition.RIGHT) 0.dp else 0.dp,
                        top = 0.dp,
                        bottom = 0.dp
                    ),
                    pageSize = null,                      // don't recurse
                    unit = unit
                )
            }

            // RIGHT fixed external Y-axis
            if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                FixedPagerYAxisLine(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = effectiveTickStep.toFloat(),
                    width = yAxisFixedWidth
                )
            }
        }
    }
}

@Composable
private fun FixedPagerYAxisLine(
    maxY: Double,
    yAxisPosition: YAxisPosition,
    step: Float,
    width: Dp
) {
    Canvas(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
    ) {
        val m = ChartMath.computeMetrics(
            size = size,
            values = listOf(0.0, maxY.toDouble()),
            chartType = ChartType.LINE,
            minY = 0.0,                    // start at 0 for typical activity charts (tweak if needed)
            maxY = maxY.toDouble(),
            includeYAxisPadding = false,  // no inner side padding; this pane is just the axis
            fixedTickStep = step.toDouble(),
        )
        ChartDraw.drawYAxisStandalone(
            drawScope = this,
            metrics = m,
            yAxisPosition = yAxisPosition,
            paneWidthPx = size.width
        )
    }
}

// helper so the call site reads clearly; adapt if your Line charts shouldn’t force 0
// SJ_COMMENT: Currently unused.
private fun chartTypeForLineWantsZero(): Boolean = false
