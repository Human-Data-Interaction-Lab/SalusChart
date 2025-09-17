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
import com.hdil.saluschart.core.chart.ChartPoint
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
    data: List<ChartPoint>,      // ChartPoint 기반
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Line Chart Example",
    lineColor: Color = ChartColor.Default,
    strokeWidth: Float = 4f,
    minY: Float? = null,                    // 사용자 지정 최소 Y값
    maxY: Float? = null,                    // 사용자 지정 최대 Y값
    labelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,  // Y축 위치
    interactionType: InteractionType.Line = InteractionType.Line.POINT,
    showPoint: Boolean = false, // 포인트 표시 여부
    pointRadius: Pair<Dp, Dp> = Pair(4.dp, 2.dp), // 포인트 외부 반지름, 내부 반지름
    showValue: Boolean = false, // 값 표시 여부
    showLegend: Boolean = false,
    windowSize: Int? = null, // 윈도우 크기 (null이면 전체 화면)
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    chartType : ChartType = ChartType.LINE, // 차트 타입 (툴팁 위치 결정용
    maxXTicksLimit: Int? = null,             // X축에 표시할 최대 라벨 개수 (null이면 모든 라벨 표시)
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Red,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null,

    // fixed external Y-axis support
    fixedYAxis: Boolean = false,
    yAxisFixedWidth: Dp = 56.dp,
    yTickStep: Float? = null,                    // e.g., 10f for even ticks
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showTitle: Boolean = true,
    autoFixYAxisOnScroll: Boolean = true,         // auto-fix when horizontally scrollable

    pagingEnabled: Boolean = false,  // when true: use page-based swipe instead of free scroll
    pageSize: Int = 0,               // items per page (e.g., 7 for a week)
    unifyYAxisAcrossPages: Boolean = true,
    initialPage: Int? = null,

    renderTooltipExternally: Boolean = true,
    ) {
    if (data.isEmpty()) return

    if (pagingEnabled && pageSize > 0 && data.size > pageSize) {
        LineChartPagedInternal(
            modifier = modifier,
            data = data,
            pageSize = pageSize,
            // carry over visual options
            title = title,
            xLabel = xLabel,
            yLabel = yLabel,
            lineColor = lineColor,
            strokeWidth = strokeWidth,
            labelTextSize = labelTextSize,
            tooltipTextSize = tooltipTextSize,
            interactionType = interactionType,
            yAxisPosition = yAxisPosition,
            yAxisFixedWidth = yAxisFixedWidth,      // external axis width
            showPoint = showPoint,
            showValue = showValue,
            onReferenceLineClick = onReferenceLineClick,
            // scale/paging options
            unifyYAxisAcrossPages = unifyYAxisAcrossPages,
            yTickStep = yTickStep,
            initialPage = initialPage,
            // we want fixed axis + no inner scroll on each page
            minY = minY,
            maxY = maxY
        )
        return
    }

    // windowSize 기반 스크롤 여부 결정
    val useScrolling = windowSize != null && windowSize < data.size
    val isFixedYAxis = if (autoFixYAxisOnScroll) (fixedYAxis || useScrolling) else fixedYAxis
    val scrollState = rememberScrollState()

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }
        BoxWithConstraints {
            val availableWidth = maxWidth
            val marginHorizontal = 16.dp

            val axisGutter = if (isFixedYAxis) 2.dp else 0.dp

            // paddings for the chart area (axis side gets the small gutter)
            val startPad =
                if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) axisGutter else marginHorizontal
            val endPad =
                if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) axisGutter else marginHorizontal

            // width taken by the fixed Y-axis pane (left or right)
            val fixedPaneWidth = if (isFixedYAxis) yAxisFixedWidth else 0.dp

            // width available to the scrollable chart area (exclude axis pane + inner paddings)
            val contentWidth = availableWidth - fixedPaneWidth - (startPad + endPad)

            // in scroll mode, canvas spans per-window width * data size
            val canvasWidth = if (useScrolling) {
                val pointWidth = contentWidth / windowSize!!
                pointWidth * data.size
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
                if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT && yAxisFixedWidth > 0.dp) {
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
                            chartType = ChartType.LINE,
                            minY = minY,
                            maxY = maxY,
                            includeYAxisPadding = !isFixedYAxis,  // no inner L/R padding when fixed axis is used
                            fixedTickStep = yTickStep
                        )
                        chartMetrics = metrics

                        ChartDraw.drawGrid(this, size, metrics, yAxisPosition, drawLabels = !isFixedYAxis)
                        if (!isFixedYAxis) ChartDraw.drawYAxis(this, metrics, yAxisPosition)

                        val points = ChartMath.Line.mapLineToCanvasPoints(data, size, metrics)
                        canvasPoints = points
                        ChartDraw.Line.drawLine(this, points, lineColor, strokeWidth)
                        ChartDraw.Line.drawXAxisLabels(
                            ctx = drawContext,
                            labels = data.map { it.x.toString() },
                            metrics = metrics,
                            textSize = labelTextSize,
                            maxXTicksLimit = maxXTicksLimit
                        )

                        val canvasForLabels = Size(
                            width  = chartMetrics!!.paddingX + chartMetrics!!.chartWidth,
                            height = chartMetrics!!.chartHeight
                        )

                        ChartMath.Line.computeLabelAnchors(
                            points = points,
                            values = yValues,
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
                                chartPoint = data[i],
                                modifier = Modifier
                                    .offset { IntOffset(xClamped.toInt(), yClamped.toInt()) }
                                    .onSizeChanged { tipSize = it }   // measure so clamping is exact
                            )
                        }
                    }
                }

                // RIGHT fixed Y-axis pane
                if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT && yAxisFixedWidth > 0.dp) {
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

@Composable
private fun LineChartPagedInternal(
    modifier: Modifier,
    data: List<ChartPoint>,
    pageSize: Int,
    // visuals
    title: String,
    xLabel: String,
    yLabel: String,
    lineColor: Color,
    strokeWidth: Float,
    labelTextSize: Float,
    tooltipTextSize: Float,
    interactionType: InteractionType.Line,
    yAxisPosition: YAxisPosition,
    yAxisFixedWidth: Dp,
    showPoint: Boolean,
    showValue: Boolean,
    onReferenceLineClick: (() -> Unit)?,
    // scale/paging
    unifyYAxisAcrossPages: Boolean,
    yTickStep: Float?,
    initialPage: Int?,
    minY: Float?,
    maxY: Float?
) {
    // how many pages
    val pageCount = remember(data.size, pageSize) {
        kotlin.math.ceil(data.size / pageSize.toFloat()).toInt()
    }
    val firstPage = initialPage ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    // single fixed Y range for all pages (so the external axis matches)
    val rawMax = if (unifyYAxisAcrossPages) data.maxOf { it.y } else data.maxOf { it.y }
    val forcedMax = maxY ?: rawMax
    val step = yTickStep ?: 0f
    val maxRounded = remember(forcedMax, step) {
        if (step > 0f) (kotlin.math.ceil(forcedMax / step) * step).toFloat() else forcedMax
    }

    Column(modifier) {
        // fixed title/header
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxSize()) {
            // left fixed axis (optional)
            if (yAxisPosition == YAxisPosition.LEFT && yAxisFixedWidth > 0.dp) {
                FixedPagerYAxisLine(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = yTickStep,
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

                // small visual padding so the line does not touch the external axis
                val padStart = if (yAxisPosition == YAxisPosition.LEFT) 12.dp else 16.dp
                val padEnd   = if (yAxisPosition == YAxisPosition.RIGHT) 12.dp else 16.dp

                // render a normal LineChart for this page, but:
                // - no inner scroll (windowSize = null)
                // - fixed external axis (fixedYAxis = true, width=0 inside)
                // - unified maxY so scales match axis
                LineChart(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    data = slice,
                    xLabel = xLabel,
                    yLabel = yLabel,
                    title = title,
                    lineColor = lineColor,
                    strokeWidth = strokeWidth,
                    minY = 0f.takeIf { chartTypeForLineWantsZero() } ?: minY,
                    maxY = maxRounded,
                    labelTextSize = labelTextSize,
                    tooltipTextSize = tooltipTextSize,
                    yAxisPosition = yAxisPosition,
                    interactionType = interactionType,
                    showPoint = showPoint,
                    showValue = showValue,
                    windowSize = null,                    // no inner scroll
                    chartType = ChartType.LINE,
                    maxXTicksLimit = slice.size,          // show all X labels of the slice
                    referenceLineType = ReferenceLineType.NONE,
                    fixedYAxis = true,                    // suppress in-canvas axis
                    yAxisFixedWidth = 0.dp,               // hide inner axis pane
                    yTickStep = yTickStep,                // keep grid aligned with external axis
                    showTitle = false,                    // title already fixed above
                    contentPadding = PaddingValues(
                        start = padStart,
                        end = padEnd,
                        top = 0.dp,
                        bottom = 0.dp
                    ),
                    autoFixYAxisOnScroll = false,         // this page is not scrollable
                    pagingEnabled = false                  // do not recurse into pager
                )
            }

            // right fixed axis (optional)
            if (yAxisPosition == YAxisPosition.RIGHT && yAxisFixedWidth > 0.dp) {
                FixedPagerYAxisLine(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = yTickStep,
                    width = yAxisFixedWidth
                )
            }
        }
    }
}

@Composable
private fun FixedPagerYAxisLine(
    maxY: Float,
    yAxisPosition: YAxisPosition,
    step: Float?,
    width: Dp
) {
    Canvas(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
    ) {
        val m = ChartMath.computeMetrics(
            size = size,
            values = listOf(0f, maxY),
            chartType = ChartType.LINE,
            minY = 0f,                    // start at 0 for typical activity charts (tweak if needed)
            maxY = maxY,
            includeYAxisPadding = false,  // no inner side padding; this pane is just the axis
            fixedTickStep = step,
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
private fun chartTypeForLineWantsZero(): Boolean = false
