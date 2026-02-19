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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.*
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
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
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

private fun ceilToStep(v: Double, step: Double): Double {
    if (step <= 0.0) return v
    return ceil(v / step) * step
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StackedBarChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    segmentLabels: List<String> = emptyList(),
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Stacked Bar Chart",
    colors: List<Color> = listOf(
        Color(0xFF2196F3),
        Color(0xFFFF9800),
        Color(0xFF4CAF50),
        Color(0xFF9C27B0),
        Color(0xFFE91E63),
        Color(0xFFFFEB3B),
    ),
    barWidthRatio: Float = 0.6f, // Ratio of bar width to space width (바 너비 / 한 칸 너비)
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.StackedBar = InteractionType.StackedBar.TOUCH_AREA,
    onBarClick: ((barIndex: Int, segmentIndex: Int?, value: Float) -> Unit)? = null,
    // Reference line
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE, // NONE, AVERAGE
    referenceLineColor: Color = Color.Red,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f", // Format for reference line label
    referenceLineInteractive: Boolean = false, // Whether to make the reference line clickable
    onReferenceLineClick: (() -> Unit)? = null,
    // Display
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showLabel: Boolean = false,
    xLabelAutoSkip: Boolean = true, // Automatically skip labels if they overlap
    maxXTicksLimit: Int? = null, // Maximum number of x-axis labels to display
    minY: Double? = null, // Minimum Y value for chart
    maxY: Double? = null, // Maximum Y value for chart
    yTickStep: Double? = null, // y-axis grid tick step (automatically calculated if null)
    unit: String = "",
    // Scroll/Page
    windowSize: Int? = null, // number of visible items in scroll window (enables scrolling if not null)
    contentPadding: PaddingValues = PaddingValues(16.dp), // Free-scroll paddings
    pageSize: Int? = null, // number of items per page (enables paging if not null)
    unifyYAxisAcrossPages: Boolean = true,
    initialPageIndex: Int? = null, // initial page to show (last page if null)
    yAxisFixedWidth: Dp = 0.dp, // Padding between the chart and the y-axis
) {
    if (data.isEmpty()) return

    // Validate that scrolling and paging modes are not both enabled
    require(!(windowSize != null && pageSize != null)) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    val chartType = ChartType.STACKED_BAR

    // Transform ChartMark to StackedChartMark (memoized)
    // TODO: 현재 ChartMark에서 StackedChartMark로 변환하는 과정 필수, 이에 input을 항상 List<ChartMark>로 고정
    // 이는 StackedChartMark가 한 개의 x값에 대응하는 y value가 여러 개 (list) 포함되어 있기 때문
    // 모든 chart type은 ChartMark를 input으로 통일하기 때문에, stacked bar chart에서 StackedChartMark를 사용하려면 .toStackedChartMarks() 를 통한 변환 필요
    // (HealthData -> TemporalDataSet -> Transform -> ChartMark 형태 유지)
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
        )
        return
    }

    val useScrolling = windowSize != null && windowSize < stackedData.size
    val isFixedYAxis = showYAxis && useScrolling
    val scrollState = rememberScrollState()
    val onBarClickState by rememberUpdatedState(onBarClick)

    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
        }

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

                val density = LocalDensity.current

                Row(Modifier.fillMaxSize()) {
                    // Left fixed axis pane
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
                        Canvas(
                            modifier = if (useScrolling) {
                                Modifier.width(canvasWidth!!).fillMaxHeight()
                            } else {
                                Modifier.fillMaxSize()
                            }
                        ) {
                            val m = ChartMath.computeMetrics(
                                size = size,
                                values = totals,
                                chartType = chartType,
                                minY = minY ?: 0.0,
                                maxY = maxY,
                                includeYAxisPadding = !isFixedYAxis,
                                fixedTickStep = yTickStep
                            )
                            chartMetrics = m

                            ChartDraw.drawGrid(this, size, m, yAxisPosition, drawLabels = showYAxis && !isFixedYAxis)
                            if (showYAxis && !isFixedYAxis) ChartDraw.drawYAxis(this, m, yAxisPosition)
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
                                    unit = unit
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

                        if (referenceLineType != ReferenceLineType.NONE) {
                            ReferenceLine.ReferenceLine(
                                modifier = Modifier.fillMaxSize(),
                                data = stackedData,
                                metrics = m,
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
                                    color = Color.Black
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
            }
        }

        // Draw legend
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
            }
            LegendPosition.TOP, LegendPosition.BOTTOM -> {
                if (showLegend && legendPosition == LegendPosition.TOP && segmentLabels.isNotEmpty()) {
                    CenteredLegend(segmentLabels, colors, LegendPosition.TOP)
                    Spacer(Modifier.height(40.dp))
                }

                Box(Modifier.weight(1f, fill = true)) { chartBlock() }  // ← weighted chart area

                if (showLegend && legendPosition == LegendPosition.BOTTOM && segmentLabels.isNotEmpty()) {
                    Spacer(Modifier.height(40.dp))
                    CenteredLegend(segmentLabels, colors, LegendPosition.BOTTOM)
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
    width: Dp
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
        ChartDraw.drawYAxisStandalone(
            drawScope = this,
            metrics = m,
            yAxisPosition = yAxisPosition,
            paneWidthPx = size.width
        )
    }
}

@Composable
private fun CenteredLegend(
    labels: List<String>,
    colors: List<Color>,
    position: LegendPosition
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        ChartLegend(
            labels = labels,
            colors = colors,
            position = position
        )
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
    yAxisFixedWidth: Dp = 0.dp,
    xLabelAutoSkip: Boolean,
    maxXTicksLimit: Int? = null,
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
                // Left fixed external Y-axis
                if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                    FixedPagerYAxisStacked(
                        totals = totals,
                        minY = minRounded,
                        maxY = maxRounded,
                        yAxisPosition = YAxisPosition.LEFT,
                        step = effectiveTickStep,
                        width = yAxisFixedWidth
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
                        width = yAxisFixedWidth
                    )
                }
            }
        }

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
                    Box(Modifier.weight(1f, fill = true)) { chartArea() }
                    if (showLegend && legendPosition == LegendPosition.RIGHT && segmentLabels.isNotEmpty()) {
                        ChartLegend(labels = segmentLabels, colors = colors, position = LegendPosition.RIGHT)
                    }
                }
            }
            LegendPosition.TOP -> {
                Column(Modifier.fillMaxSize()) {
                    if (showLegend && segmentLabels.isNotEmpty()) {
                        CenteredLegend(segmentLabels, colors, LegendPosition.TOP)
                        Spacer(Modifier.height(40.dp))
                    }
                    Box(Modifier.weight(1f, fill = true)) { chartArea() }
                }
            }
            LegendPosition.BOTTOM -> {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f, fill = true)) { chartArea() }
                    if (showLegend && segmentLabels.isNotEmpty()) {
                        Spacer(Modifier.height(40.dp))
                        CenteredLegend(segmentLabels, colors, LegendPosition.BOTTOM)
                    }
                }
            }
        }
    }
}