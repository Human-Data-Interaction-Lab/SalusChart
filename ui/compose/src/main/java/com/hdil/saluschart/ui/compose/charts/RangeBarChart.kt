package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.BaseChartMark
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.toRangeChartMarks
import com.hdil.saluschart.ui.theme.ChartColor

// TODO: unused?
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Float.roundUpToStep(step: Float): Float =
    (kotlin.math.ceil(this / step) * step).toFloat()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<BaseChartMark>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Range Bar Chart",
    barColor: Color = ChartColor.Default,
    barWidthRatio: Float = 0.6f, // Ratio of bar width to space width (바 너비 / 한 칸 너비)
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.RangeBar = InteractionType.RangeBar.BAR,
    onBarClick: ((Int, RangeChartMark) -> Unit)? = null,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    xLabelAutoSkip: Boolean = true, // Automatically skip labels if they overlap
    maxXTicksLimit: Int? = null, // Maximum number of x-axis labels to display
    yTickStep: Double? = null, // y-axis grid tick step (automatically calculated if null)
    unit: String = "",
    // Scroll/Page
    windowSize: Int? = null, // visible items in scroll window
    contentPadding: PaddingValues = PaddingValues(16.dp), // Free-scroll paddings
    pageSize: Int? = null, // items per page
    unifyYAxisAcrossPages: Boolean = true,
    initialPageIndex: Int? = null, // initial page to show (last page if null)
    yAxisFixedWidth: Dp = 0.dp, // Padding between the chart and the y-axis
) {
    if (data.isEmpty()) return

    // Validate that scrolling and paging modes are not both enabled
    require(!(windowSize != null && pageSize != null)) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    val chartType = ChartType.RANGE_BAR

    // Convert data to RangeChartMark if needed
    val rangeData = remember(data) {
        when {
            data.all { it is RangeChartMark } -> {
                // Data is already RangeChartMark, cast directly
                data.map { it as RangeChartMark }
            }
            data.all { it is ChartMark } -> {
                // Data is ChartMark, convert using toRangeChartMarks
                (data as List<ChartMark>).toRangeChartMarks(
                    minValueSelector = { g -> g.minByOrNull { it.y } ?: g.first() },
                    maxValueSelector = { g -> g.maxByOrNull { it.y } ?: g.first() }
                )
            }
            else -> {
                require (false) { "Data must be either all ChartMark or all RangeChartMark" }
                emptyList<RangeChartMark>() // Unreachable, but required for compilation
            }
        }
    }

    // Compute effective page size (0 = off)
    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)

    // Enable paging if pageSize is provided and data exceeds page size
    val enablePaging = requestedPageSize > 0 && rangeData.size > requestedPageSize

    if (enablePaging) {
        RangeBarChartPagedInternal(
            modifier = modifier,
            data = rangeData,
            title = title,
            barColor = barColor,
            barWidthRatio = barWidthRatio,
            yAxisPosition = yAxisPosition,
            interactionType = interactionType,
            onBarClick = onBarClick,
            showTitle = showTitle,
            showYAxis = showYAxis,
            unit = unit,
            // scale/paging
            pageSize = requestedPageSize,
            unifyYAxisAcrossPages = unifyYAxisAcrossPages,
            yTickStep = yTickStep,
            initialPageIndex = initialPageIndex,
            outerPadding = contentPadding,
            yAxisFixedWidth = yAxisFixedWidth,
            maxXTicksLimit = maxXTicksLimit,
            xLabelAutoSkip = xLabelAutoSkip
        )
        return
    }

    val useScrolling = windowSize != null && windowSize < rangeData.size
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

            // Calculate canvas width for scrolling mode
            val canvasWidth = if (useScrolling) {
                val chartWidth = availableWidth - (marginHorizontal * 2)
                val sectionsCount = (rangeData.size.toFloat() / windowSize!!.toFloat()).toInt()
                chartWidth * sectionsCount
            } else null

            val labels = rangeData.map { it.label ?: it.x.toString() }

            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
            var selectedIndex by remember { mutableStateOf<Int?>(null) }

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

                // Calculate padding when Y-axis is hidden (external axis handles it) or when it's a fixed axis on that side
                val startPad = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT)) 0.dp else marginHorizontal
                val endPad   = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT)) 0.dp else marginHorizontal

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .let { if (useScrolling) it.horizontalScroll(scrollState, overscrollEffect = null) else it }
                        .padding(start = startPad, end = endPad)
                        .onGloballyPositioned { coords ->
                            val p = coords.positionInWindow()
                        }
                ) {
                Canvas(
                        modifier = if (useScrolling) {
                            Modifier.width(canvasWidth!!).fillMaxHeight()
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ) {
                        // Use generic computeMetrics so we can control tick step & padding
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
                        ChartDraw.Bar.drawBarXAxisLabels(
                            ctx = drawContext,
                            labels = labels,
                            metrics = metrics,
                            maxXTicksLimit = maxXTicksLimit,
                            xLabelAutoSkip = xLabelAutoSkip
                        )
                    }

                    // Range bars & interaction
                    val minValues = rangeData.map { it.minPoint.y }
                    val maxValues = rangeData.map { it.maxPoint.y }

                    when (interactionType) {
                        InteractionType.RangeBar.TOUCH_AREA -> {
                            // Draw visual bars
                            chartMetrics?.let { m ->
                                ChartDraw.Bar.BarMarker(
                                    data = rangeData,
                                    minValues = minValues,
                                    maxValues = maxValues,
                                    metrics = m,
                                    color = barColor,
                                    barWidthRatio = barWidthRatio,
                                    interactive = false,
                                    chartType = chartType,
                                    showTooltipForIndex = selectedIndex,
                                    unit = unit
                                )
                                // Draw interactive touch area
                                ChartDraw.Bar.BarMarker(
                                    data = rangeData,
                                    minValues = List(rangeData.size) { m.minY },
                                    maxValues = maxValues,
                                    metrics = m,
                                    onBarClick = { idx, _ ->
                                        selectedIndex = if (selectedIndex == idx) null else idx
                                        onBarClick?.invoke(idx, rangeData[idx])
                                    },
                                    chartType = chartType,
                                    isTouchArea = true,
                                    unit = unit
                                )
                            }
                        }
                        InteractionType.RangeBar.BAR -> {
                            // Draw interactive bars
                            chartMetrics?.let { m ->
                                ChartDraw.Bar.BarMarker(
                                    data = rangeData,
                                    minValues = minValues,
                                    maxValues = maxValues,
                                    metrics = m,
                                    color = barColor,
                                    barWidthRatio = barWidthRatio,
                                    interactive = true,
                                    onBarClick = { idx, _ ->
                                        onBarClick?.invoke(idx, rangeData[idx])
                                    },
                                    chartType = chartType,
                                    unit = unit
                                )
                            }
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
        Spacer(Modifier.height(4.dp))
    }
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

// TODO: unifyYAxisAcrossPages 의미?
// Function for paged range bar chart
@OptIn(ExperimentalFoundationApi::class)
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
    // scale/paging
    pageSize: Int,
    unifyYAxisAcrossPages: Boolean,
    yTickStep: Double?,
    initialPageIndex: Int?,
    outerPadding: PaddingValues,
    yAxisFixedWidth: Dp = 0.dp,
) {
    val pageCount = remember(data.size, pageSize) {
        kotlin.math.ceil(data.size / pageSize.toFloat()).toInt()
    }
    val firstPage = initialPageIndex ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    // Compute unified Y-axis range using the lighter function (no pixel calculations)
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

    // Compute min/max rounded values and effective tick step
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

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) { page ->
                val start = page * pageSize
                val end = kotlin.math.min(start + pageSize, data.size)
                val slice = data.subList(start, end)

                // Convert RangeChartMark back to ChartMark for the function call
                val sliceAsChartMarks = slice.flatMap { rangePoint ->
                    listOf(rangePoint.minPoint, rangePoint.maxPoint)
                }

                // Render a normal RangeBarChart page:
                // - no inner scroll (windowSize = null)
                // - unified scale with the external axis
                RangeBarChart(
                    modifier = Modifier.fillMaxSize(),
                    data = sliceAsChartMarks,
                    title = title,
                    barColor = barColor,
                    barWidthRatio = barWidthRatio,
                    yAxisPosition = yAxisPosition,
                    interactionType = interactionType,
                    onBarClick = onBarClick?.let { cb ->
                        { localIdx, rangePoint -> cb(start + localIdx, rangePoint) }
                    },
                    showTitle = false,
                    showYAxis = false,                    // external axis handles it
                    maxXTicksLimit = maxXTicksLimit,
                    xLabelAutoSkip = xLabelAutoSkip,
                    yTickStep = effectiveTickStep,
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
