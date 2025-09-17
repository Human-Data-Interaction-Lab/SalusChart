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
    minY: Float? = null,
    maxY: Float? = null,
    barWidthRatio: Float = 0.8f,
    labelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Bar = InteractionType.Bar.BAR,
    onBarClick: ((Int, Float) -> Unit)? = null,
    showLabel: Boolean = false,
    windowSize: Int? = null,                 // used by free-scroll mode
    chartType: ChartType = ChartType.BAR,
    maxXTicksLimit: Int? = null,
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Black,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null,

    // Fixed internal axis (free-scroll mode)
    fixedYAxis: Boolean = false,
    yAxisFixedWidth: Dp = 16.dp,

    // grid tick step (both modes)
    yTickStep: Float? = null,

    // Outer padding when using the free-scroll mode
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showTitle: Boolean = true,
    autoFixYAxisOnScroll: Boolean = true,

    // NEW — paged mode
    pageSize: Int? = null,                      // if not null => paged mode
    unifyYAxisAcrossPages: Boolean = true,      // for paged mode
    yTickStepDefaultForPaged: Float = 10f       // for paged mode (use 10s)
) {
    if (data.isEmpty()) return

    // ─────────────────────────────
    // Paged mode (HorizontalPager)
    // ─────────────────────────────
    if (pageSize != null && pageSize > 0) {
        val pageCount = remember(data.size, pageSize) {
            kotlin.math.ceil(data.size / pageSize.toFloat()).toInt()
        }
        val pagerState = rememberPagerState(
            initialPage = pageCount - 1,
            pageCount = { pageCount }
        )

        // Global max rounded to next tick so the fixed axis & pages align perfectly
        val rawMax = if (unifyYAxisAcrossPages) data.maxOf { it.y } else data.maxOf { it.y }
        val tickStep = yTickStep ?: yTickStepDefaultForPaged
        val maxRounded = remember(rawMax, tickStep) {
            (kotlin.math.ceil(rawMax / tickStep) * tickStep).toFloat()
        }

        Column(modifier) {
            if (showTitle) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            Row(Modifier.fillMaxWidth()) {
                // LEFT fixed axis
                if (yAxisPosition == YAxisPosition.LEFT) {
                    FixedPagerYAxis(
                        maxY = maxRounded,
                        yAxisPosition = YAxisPosition.LEFT,
                        step = tickStep,
                        width = yAxisFixedWidth
                    )
                }

                // Pager pages (bars only; internal axis suppressed)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val start = page * pageSize
                    val end = kotlin.math.min(start + pageSize, data.size)
                    val slice = data.subList(start, end)

                    // Axis side = 0.dp (so bars hug the fixed axis), far side = 16.dp
                    val padStart = if (yAxisPosition == YAxisPosition.LEFT) 0.dp else 16.dp
                    val padEnd   = if (yAxisPosition == YAxisPosition.RIGHT) 0.dp else 16.dp

                    // Render one "page" using the same BarChart body (free-scroll path)
                    BarChartPageContent(
                        data = slice,
                        xLabel = xLabel,
                        yLabel = yLabel,
                        title = title,
                        barColor = barColor,
                        minY = null,                 // BAR => baseline 0
                        maxY = maxRounded,           // unified with axis
                        barWidthRatio = barWidthRatio,
                        labelTextSize = labelTextSize,
                        tooltipTextSize = tooltipTextSize,
                        yAxisPosition = yAxisPosition,
                        interactionType = interactionType,
                        onBarClick = if (onBarClick != null) { i, v -> onBarClick(start + i, v) } else null,
                        showLabel = showLabel,
                        chartType = chartType,
                        maxXTicksLimit = pageSize,   // show all labels for the slice
                        referenceLineType = ReferenceLineType.NONE,
                        // suppress internal axis & labels
                        fixedYAxis = true,
                        yAxisFixedWidth = 0.dp,
                        yTickStep = tickStep,
                        showTitle = false,
                        contentPadding = PaddingValues(start = padStart, end = padEnd)
                    )
                }

                // RIGHT fixed axis
                if (yAxisPosition == YAxisPosition.RIGHT) {
                    FixedPagerYAxis(
                        maxY = maxRounded,
                        yAxisPosition = YAxisPosition.RIGHT,
                        step = tickStep,
                        width = yAxisFixedWidth
                    )
                }
            }
        }
        return
    }

    // ─────────────────────────────
    // Free-scroll mode (existing)
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

                // Chart area
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
                            chartType = ChartType.BAR,
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
                            drawLabels = !isFixedYAxis
                        )
                        if (!isFixedYAxis) {
                            ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                        }
                        ChartDraw.Bar.drawBarXAxisLabels(
                            ctx = drawContext,
                            labels = xLabels,
                            metrics = metrics,
                            textSize = labelTextSize,
                            maxXTicksLimit = maxXTicksLimit
                        )
                    }

                    // Bars & interaction (unchanged)
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
                                    chartType = ChartType.BAR,
                                    showTooltipForIndex = selectedBarIndex
                                )
                            }
                            chartMetrics?.let { metrics ->
                                ChartDraw.Bar.BarMarker(
                                    data = data,
                                    minValues = List(yValues.size) { metrics.minY },
                                    maxValues = yValues,
                                    metrics = metrics,
                                    onBarClick = { index, tooltipText ->
                                        selectedBarIndex = if (selectedBarIndex == index) null else index
                                        onBarClick?.invoke(index, tooltipText.toFloat())
                                    },
                                    chartType = chartType,
                                    isTouchArea = true
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
                                    onBarClick = { index, tooltipText ->
                                        onBarClick?.invoke(index, tooltipText.toFloat())
                                    },
                                    chartType = chartType,
                                    showLabel = showLabel
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
private fun FixedPagerYAxis(
    maxY: Float,
    yAxisPosition: YAxisPosition,
    step: Float,
    width: Dp
) {
    Canvas(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
    ) {
        // Metrics for just the vertical axis & ticks
        val m = ChartMath.computeMetrics(
            size = size,
            values = listOf(0f, maxY),
            chartType = ChartType.BAR,
            minY = 0f,
            maxY = maxY,
            includeYAxisPadding = false,
            fixedTickStep = step
        )

        ChartDraw.drawYAxisStandalone(
            drawScope = this,
            metrics = m,
            yAxisPosition = yAxisPosition,
            paneWidthPx = size.width
        )
    }
}

@Composable
private fun BarChartPageContent(
    data: List<ChartPoint>,
    xLabel: String,
    yLabel: String,
    title: String,
    barColor: Color,
    minY: Float?,
    maxY: Float?,
    barWidthRatio: Float,
    labelTextSize: Float,
    tooltipTextSize: Float,
    yAxisPosition: YAxisPosition,
    interactionType: InteractionType.Bar,
    onBarClick: ((Int, Float) -> Unit)?,
    showLabel: Boolean,
    chartType: ChartType,
    maxXTicksLimit: Int?,
    referenceLineType: ReferenceLineType,
    fixedYAxis: Boolean,
    yAxisFixedWidth: Dp,
    yTickStep: Float,
    showTitle: Boolean,
    contentPadding: PaddingValues
) {
    BarChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = data,
        xLabel = xLabel,
        yLabel = yLabel,
        title = title,
        barColor = barColor,
        minY = minY,
        maxY = maxY,
        barWidthRatio = barWidthRatio,
        labelTextSize = labelTextSize,
        tooltipTextSize = tooltipTextSize,
        yAxisPosition = yAxisPosition,
        interactionType = interactionType,
        onBarClick = onBarClick,
        showLabel = showLabel,
        windowSize = null,                 // no inner scrolling for a page
        chartType = chartType,
        maxXTicksLimit = maxXTicksLimit,
        referenceLineType = referenceLineType,
        fixedYAxis = fixedYAxis,
        yAxisFixedWidth = yAxisFixedWidth,
        yTickStep = yTickStep,
        showTitle = showTitle,
        contentPadding = contentPadding
    )
}

