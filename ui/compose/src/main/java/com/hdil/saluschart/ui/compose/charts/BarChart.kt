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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.ui.theme.ChartColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    data: List<ChartPoint>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Bar Chart Example",
    barColor: Color = ChartColor.Default,
    minY: Double? = null,
    maxY: Double? = null,
    barWidthRatio: Float = 0.8f,
    xLabelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Bar = InteractionType.Bar.BAR,
    onBarClick: ((Int, Double) -> Unit)? = null,
    showLabel: Boolean = false,
    windowSize: Int? = null,                 // free-scroll mode only
    maxXTicksLimit: Int? = null,
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Black,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null,

    // Free-scroll mode axis fix
    fixedYAxis: Boolean = false,
    // Grid tick step (both modes)
    yTickStep: Double? = null,

    // Free-scroll paddings
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showTitle: Boolean = true,
    autoFixYAxisOnScroll: Boolean = true,

    renderInnerYAxis: Boolean = true,
    pagingEnabled: Boolean = false,          // when true -> pager path
    pageSize: Int? = null,                       // items per page
    unifyYAxisAcrossPages: Boolean = true,
    initialPage: Int? = null,
    yAxisFixedWidth: Dp = 6.dp,             // external axis width in paged mode
    yTickStepDefaultForPaged: Double = 10.0, // default step if none given
    unit: String = "",
) {
    if (data.isEmpty()) return

    // compute effective page size (0 = off)
    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)

    // enable paging if either:
    //  - caller set a positive pageSize (old style), or
    //  - caller set pagingEnabled=true AND provided a positive pageSize (new style)
    val enablePaging = requestedPageSize > 0 && data.size > requestedPageSize

    if (enablePaging) {
        BarChartPagedInternal(
            modifier = modifier,
            data = data,
            pageSize = requestedPageSize,
            // visuals
            title = title,
            xLabel = xLabel,
            yLabel = yLabel,
            barColor = barColor,
            barWidthRatio = barWidthRatio,
            xLabelTextSize = xLabelTextSize,
            tooltipTextSize = tooltipTextSize,
            interactionType = interactionType,
            yAxisPosition = yAxisPosition,
            yAxisFixedWidth = yAxisFixedWidth,
            showLabel = showLabel,
            onBarClick = onBarClick,
            // scale/paging
            outerPadding = contentPadding,
            unifyYAxisAcrossPages = unifyYAxisAcrossPages,
            yTickStep = yTickStep ?: yTickStepDefaultForPaged,
            initialPage = initialPage,
            minY = minY,
            maxY = maxY,
            unit = unit
        )
        return
    }

    val chartType = ChartType.BAR

    // ─────────────────────────────
    // Free-scroll mode
    // ─────────────────────────────
    val useScrolling  = windowSize != null && windowSize < data.size
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

            val canvasWidth = if (useScrolling) {
                val chartWidth = availableWidth - (marginHorizontal * 2)
                val sectionsCount = (data.size.toFloat() / windowSize!!.toFloat()).toInt()
                chartWidth * sectionsCount
            } else null

            val xLabels = data.map { it.label ?: it.x.toString() }
            val yValues = data.map { it.y }

            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
            var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

            Row(Modifier.fillMaxSize()) {
                // LEFT fixed axis pane
                if (renderInnerYAxis && isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) {
                    Canvas(
                        modifier = Modifier
                            .width(0.dp)
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

                val startPad = if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) 0.dp else marginHorizontal
                val endPad   = if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) 0.dp else marginHorizontal

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
                        val metrics = ChartMath.computeMetrics(
                            size = size,
                            values = yValues,
                            chartType = chartType,
                            minY = minY,
                            maxY = maxY,
                            includeYAxisPadding = !isFixedYAxis,
                            fixedTickStep = yTickStep
                        )
                        chartMetrics = metrics

                        ChartDraw.drawGrid(
                            drawScope = this,
                            size = size,
                            metrics = metrics,
                            yAxisPosition = yAxisPosition,
                            drawLabels = renderInnerYAxis && !isFixedYAxis
                        )
                        if (renderInnerYAxis && !isFixedYAxis) {
                            ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                        }
                        ChartDraw.Bar.drawBarXAxisLabels(
                            ctx = drawContext,
                            labels = xLabels,
                            metrics = metrics,
                            textSize = xLabelTextSize,
                            maxXTicksLimit = maxXTicksLimit
                        )
                    }

                    // Bars & interaction
                    when (interactionType) {
                        InteractionType.Bar.TOUCH_AREA -> {
                            chartMetrics?.let { metrics ->
                                ChartDraw.Bar.BarMarker(
                                    data = data,
                                    minValues = List(yValues.size) { metrics.minY },
                                    maxValues = yValues,
                                    metrics = metrics,
                                    color = barColor,
                                    barWidthRatio = barWidthRatio,
                                    interactive = false,
                                    chartType = chartType,
                                    showTooltipForIndex = selectedBarIndex,
                                    showLabel = showLabel,
                                    unit = unit
                                )
                            }
                            chartMetrics?.let { metrics ->
                                ChartDraw.Bar.BarMarker(
                                    data = data,
                                    minValues = List(yValues.size) { metrics.minY },
                                    maxValues = yValues,
                                    metrics = metrics,
                                    onBarClick = { index, _ ->
                                        selectedBarIndex = if (selectedBarIndex == index) null else index
                                        onBarClick?.invoke(index, data.getOrNull(index)?.y ?: 0.0)
                                    },
                                    chartType = chartType,
                                    isTouchArea = true,
                                    showLabel = false
                                )
                            }
                        }
                        InteractionType.Bar.BAR -> {
                            chartMetrics?.let { metrics ->
                                ChartDraw.Bar.BarMarker(
                                    data = data,
                                    minValues = List(yValues.size) { metrics.minY },
                                    maxValues = yValues,
                                    metrics = metrics,
                                    color = barColor,
                                    barWidthRatio = barWidthRatio,
                                    interactive = true,
                                    onBarClick = { index, _ ->
                                        onBarClick?.invoke(index, data.getOrNull(index)?.y ?: 0.0)
                                    },
                                    chartType = chartType,
                                    showLabel = showLabel,
                                    unit = unit
                                )
                            }
                        }
                    }

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
                }

                // RIGHT fixed axis pane
                if (renderInnerYAxis && isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                    Canvas(
                        modifier = Modifier
                            .width(0.dp)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BarChartPagedInternal(
    modifier: Modifier,
    data: List<ChartPoint>,
    pageSize: Int,
    // visuals
    title: String,
    xLabel: String,
    yLabel: String,
    barColor: Color,
    barWidthRatio: Float,
    xLabelTextSize: Float,
    tooltipTextSize: Float,
    interactionType: InteractionType.Bar,
    yAxisPosition: YAxisPosition,
    yAxisFixedWidth: Dp,
    showLabel: Boolean,
    onBarClick: ((Int, Double) -> Unit)?,
    // scale/paging
    unifyYAxisAcrossPages: Boolean,
    yTickStep: Double,
    initialPage: Int?,
    minY: Double?,
    maxY: Double?,
    unit: String,
    outerPadding: PaddingValues = PaddingValues(0.dp),
) {
    val pageCount = remember(data.size, pageSize) {
        kotlin.math.ceil(data.size / pageSize.toFloat()).toInt()
    }
    val firstPage = initialPage ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    // unified Y-range (respect explicit maxY if provided)
    val rawMax = if (unifyYAxisAcrossPages) data.maxOf { it.y } else data.maxOf { it.y }
    val forcedMax = maxY ?: rawMax
    val maxRounded = remember(forcedMax, yTickStep) {
        kotlin.math.ceil(forcedMax / yTickStep) * yTickStep
    }

    Column(modifier = modifier.padding(outerPadding)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            // LEFT fixed external Y-axis
            if (yAxisPosition == YAxisPosition.LEFT && yAxisFixedWidth > 0.dp) {
                FixedPagerYAxis(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = yTickStep.toFloat(),
                    width = yAxisFixedWidth
                )
            }

            // PAGER
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val start = page * pageSize
                val end = kotlin.math.min(start + pageSize, data.size)
                val slice = data.subList(start, end)

                val padStart = if (yAxisPosition == YAxisPosition.LEFT) 0.dp else 0.dp
                val padEnd   = if (yAxisPosition == YAxisPosition.RIGHT) 0.dp else 0.dp

                // Render a normal BarChart page:
                // - no inner scroll (windowSize = null)
                // - fixedYAxis = true (hide inner axis)
                // - maxY unified with the external axis
                BarChart(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    data = slice,
                    xLabel = xLabel,
                    yLabel = yLabel,
                    title = title,
                    barColor = barColor,
                    minY = 0.0.takeIf { false } ?: minY,
                    maxY = maxRounded,
                    barWidthRatio = barWidthRatio,
                    xLabelTextSize = xLabelTextSize,
                    tooltipTextSize = tooltipTextSize,
                    yAxisPosition = yAxisPosition,
                    interactionType = interactionType,
                    onBarClick = onBarClick?.let { cb ->
                        { i, v -> cb(start + i, v) } // rebase index to global
                    },
                    showLabel = showLabel,
                    windowSize = null,                 // no inner scroll
                    maxXTicksLimit = slice.size,       // show all labels in page
                    referenceLineType = ReferenceLineType.NONE,
                    fixedYAxis = true,                 // suppress inner axis
                    yAxisFixedWidth = 0.dp,
                    renderInnerYAxis = false,
                    yTickStep = yTickStep,
                    showTitle = false,
                    contentPadding = PaddingValues(
                        start = padStart, end = padEnd, top = 0.dp, bottom = 0.dp
                    ),
                    autoFixYAxisOnScroll = false,      // page isn’t scrollable
                    pagingEnabled = false,             // don’t recurse
                    unit = unit
                )
            }

            // RIGHT fixed external Y-axis
            if (yAxisPosition == YAxisPosition.RIGHT && yAxisFixedWidth > 0.dp) {
                FixedPagerYAxis(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = yTickStep.toFloat(),
                    width = yAxisFixedWidth
                )
            }
        }
    }
}

@Composable
private fun FixedPagerYAxis(
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
            values = listOf(0.0, maxY),
            chartType = ChartType.BAR,
            minY = 0.0,
            maxY = maxY,
            includeYAxisPadding = false,
            fixedTickStep = step.toDouble()
        )
        ChartDraw.drawYAxisStandalone(
            drawScope = this,
            metrics = m,
            yAxisPosition = yAxisPosition,
            paneWidthPx = size.width
        )
    }
}
