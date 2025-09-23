package com.hdil.saluschart.ui.compose.charts

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
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.transform.toStackedChartPoints
import kotlin.math.ceil
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Float.roundUpToStep(step: Float): Float =
    (kotlin.math.ceil(this / step) * step).toFloat()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StackedBarChart(
    modifier: Modifier = Modifier,
    data: List<ChartPoint>,
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
    barWidthRatio: Float = 0.6f,
    showLegend: Boolean = true,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,

    // free-scroll
    windowSize: Int? = null,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,

    // interaction — always use a single touch area per bar
    interactionType: InteractionType.StackedBar = InteractionType.StackedBar.TOUCH_AREA,
    onBarClick: ((barIndex: Int, segmentIndex: Int?, value: Float) -> Unit)? = null,
    chartType: ChartType = ChartType.STACKED_BAR,
    maxXTicksLimit: Int? = null,

    // reference line
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Red,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null,
    unit: String = "",

    // fixed Y (free-scroll)
    fixedYAxis: Boolean = false,
    yAxisFixedWidth: Dp = 16.dp,
    yTickStep: Float? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showTitle: Boolean = true,
    autoFixYAxisOnScroll: Boolean = true,

    // paged mode
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    yTickStepDefaultForPaged: Float = 10f,
    axisOuterPadding: Dp = 12.dp,
    axisToBarsGap: Dp = 0.dp,
    tooltipSafePaddingEnd: Dp = 0.dp
) {
    if (data.isEmpty()) return

    // transform to stacked points
    val stackedData = data.toStackedChartPoints(
        segmentOrdering = { group: List<ChartPoint> -> group.sortedByDescending { it.y } }
    )
    if (stackedData.isEmpty()) return

    val xLabels = stackedData.map { it.label ?: it.x.toString() }
    val totals  = stackedData.map { it.y }

    // ─────────────────────────
    // PAGED MODE
    // ─────────────────────────
    if (pageSize != null && pageSize > 0) {
        val pageCount = remember(stackedData.size, pageSize) {
            ceil(stackedData.size / pageSize.toFloat()).toInt()
        }
        val pagerState = rememberPagerState(
            initialPage = pageCount - 1,
            pageCount = { pageCount }
        )

        val tickStep = yTickStep ?: yTickStepDefaultForPaged
        val minRounded = 0f
        val maxRounded = (totals.maxOrNull() ?: 0f).let { v ->
            if (v <= 0f) tickStep else (ceil(v / tickStep) * tickStep).toFloat()
        }

        Column(modifier) {
            if (showTitle) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            Row(Modifier.fillMaxWidth()) {
                // fixed left axis
                if (yAxisPosition == YAxisPosition.LEFT) {
                    Box(
                        Modifier
                            .padding(start = axisOuterPadding)
                            .fillMaxHeight()
                            .width(yAxisFixedWidth)
                    ) {
                        FixedPagerYAxisStacked(
                            minY = minRounded,
                            maxY = maxRounded,
                            yAxisPosition = YAxisPosition.LEFT,
                            step = tickStep,
                            width = yAxisFixedWidth
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val start = page * pageSize
                    val end   = min(start + pageSize, stackedData.size)
                    val slice = stackedData.subList(start, end)

                    val padStart = if (yAxisPosition == YAxisPosition.LEFT) axisToBarsGap else 16.dp
                    val baseEnd  = if (yAxisPosition == YAxisPosition.RIGHT) axisToBarsGap else 16.dp
                    val padEnd   = baseEnd + tooltipSafePaddingEnd

                    StackedBarChartPage(
                        stackedData = slice,
                        colors = colors,
                        barWidthRatio = barWidthRatio,
                        yAxisPosition = yAxisPosition,
                        onBarClick = { i, total ->
                            onBarClick?.invoke(start + i, null, total)
                        },
                        maxXTicksLimit = pageSize,
                        yTickStep = tickStep,
                        contentPadding = PaddingValues(start = padStart, end = padEnd),
                        unit = unit,
                        fixedMinY = minRounded,
                        fixedMaxY = if (unifyYAxisAcrossPages) maxRounded else (slice.maxOfOrNull { it.y } ?: maxRounded)
                    )
                }

                // fixed right axis
                if (yAxisPosition == YAxisPosition.RIGHT) {
                    Box(
                        Modifier
                            .padding(end = axisOuterPadding)
                            .fillMaxHeight()
                            .width(yAxisFixedWidth)
                    ) {
                        FixedPagerYAxisStacked(
                            minY = minRounded,
                            maxY = maxRounded,
                            yAxisPosition = YAxisPosition.RIGHT,
                            step = tickStep,
                            width = yAxisFixedWidth
                        )
                    }
                }
            }
        }
        return
    }

    // ─────────────────────────
    // FREE-SCROLL MODE
    // ─────────────────────────
    val useScrolling  = windowSize != null && windowSize < stackedData.size
    val isFixedYAxis  = if (autoFixYAxisOnScroll) (fixedYAxis || useScrolling) else fixedYAxis
    val scrollState   = rememberScrollState()

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
                val canvasWidth = if (useScrolling) {
                    val chartWidth = availableWidth - (marginHorizontal * 2)
                    val sectionsCount = (stackedData.size.toFloat() / windowSize!!.toFloat()).toInt()
                    chartWidth * sectionsCount
                } else null

                Row(Modifier.fillMaxSize()) {
                    // fixed left pane
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
                                values = totals,
                                chartType = ChartType.STACKED_BAR,
                                minY = 0f,
                                maxY = null,
                                includeYAxisPadding = !isFixedYAxis,
                                fixedTickStep = yTickStep
                            )
                            chartMetrics = metrics

                            ChartDraw.drawGrid(this, size, metrics, yAxisPosition, drawLabels = !isFixedYAxis)
                            if (!isFixedYAxis) ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                            ChartDraw.drawXAxis(this, metrics)
                            ChartDraw.Bar.drawBarXAxisLabels(
                                ctx = drawContext,
                                labels = xLabels,
                                metrics = metrics,
                                maxXTicksLimit = maxXTicksLimit
                            )
                        }

                        // Draw segments (NOT interactive)
                        chartMetrics?.let { metrics ->
                            val segmentCounts = stackedData.map { it.segments.size }
                            val maxSegments = segmentCounts.maxOrNull() ?: 0
                            for (segmentIndex in 0 until maxSegments) {
                                val segmentMin = mutableListOf<Float>()
                                val segmentMax = mutableListOf<Float>()
                                stackedData.forEach { sp ->
                                    var cum = 0f
                                    for (i in 0 until segmentIndex) cum += sp.segments.getOrNull(i)?.y ?: 0f
                                    val seg = sp.segments.getOrNull(segmentIndex)?.y ?: 0f
                                    segmentMin.add(cum)
                                    segmentMax.add(cum + seg)
                                }
                                val hasAny = segmentMax.zip(segmentMin).any { (mx, mn) -> mx > mn }
                                if (hasAny) {
                                    val c = colors.getOrNull(segmentIndex) ?: Color.Gray
                                    ChartDraw.Bar.BarMarker(
                                        data = stackedData,
                                        minValues = segmentMin,
                                        maxValues = segmentMax,
                                        metrics = metrics,
                                        color = c,
                                        barWidthRatio = barWidthRatio,
                                        interactive = false,     // segments are not clickable
                                        chartType = chartType,
                                        unit = unit
                                    )
                                }
                            }

                            // One transparent touch layer per bar (whole stack)
                            ChartDraw.Bar.BarMarker(
                                data = stackedData,
                                minValues = List(stackedData.size) { metrics.minY },
                                maxValues = totals,
                                metrics = metrics,
                                onBarClick = { index, _ ->
                                    val sp = stackedData.getOrNull(index)
                                    if (sp != null) onBarClick?.invoke(index, null, sp.y)
                                },
                                barWidthRatio = barWidthRatio,
                                chartType = chartType,
                                interactive = true,
                                color = Color.Transparent,
                                unit = unit
                            )
                        }

                        if (referenceLineType != ReferenceLineType.NONE) {
                            chartMetrics?.let { metrics ->
                                ReferenceLine.ReferenceLine(
                                    modifier = Modifier.fillMaxSize(),
                                    data = stackedData,
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

                    // fixed right pane
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
                    Box(Modifier.weight(1f)) { chartBlock() }
                    if (showLegend && legendPosition == LegendPosition.RIGHT && segmentLabels.isNotEmpty()) {
                        ChartLegend(labels = segmentLabels, colors = colors, position = LegendPosition.RIGHT)
                    }
                }
            }
            LegendPosition.TOP, LegendPosition.BOTTOM -> {
                if (showLegend && legendPosition == LegendPosition.TOP && segmentLabels.isNotEmpty()) {
                    ChartLegend(labels = segmentLabels, colors = colors, position = LegendPosition.TOP)
                    Spacer(Modifier.height(16.dp))
                }
                chartBlock()
                if (showLegend && legendPosition == LegendPosition.BOTTOM && segmentLabels.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    ChartLegend(labels = segmentLabels, colors = colors, position = LegendPosition.BOTTOM)
                }
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun FixedPagerYAxisStacked(
    minY: Float,
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
        val m = ChartMath.computeMetrics(
            size = size,
            values = listOf(minY, maxY),
            chartType = ChartType.STACKED_BAR,
            minY = minY,
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

/** One page: draws segments (not interactive) + a single touch layer per bar */
@Composable
private fun StackedBarChartPage(
    stackedData: List<StackedChartPoint>,
    colors: List<Color>,
    barWidthRatio: Float,
    yAxisPosition: YAxisPosition,
    onBarClick: (index: Int, total: Float) -> Unit,
    maxXTicksLimit: Int?,
    yTickStep: Float,
    contentPadding: PaddingValues,
    unit: String,
    fixedMinY: Float,
    fixedMaxY: Float
) {
    val xLabels = stackedData.map { it.label ?: it.x.toString() }
    val totals  = stackedData.map { it.y }
    var metrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

    Box(Modifier.fillMaxWidth().padding(contentPadding)) {
        Canvas(Modifier.fillMaxSize()) {
            val m = ChartMath.computeMetrics(
                size = size,
                values = totals,
                chartType = ChartType.STACKED_BAR,
                minY = fixedMinY,
                maxY = fixedMaxY,
                includeYAxisPadding = false,
                fixedTickStep = yTickStep
            )
            metrics = m

            ChartDraw.drawGrid(this, size, m, yAxisPosition, drawLabels = false)
            ChartDraw.drawXAxis(this, m)
            ChartDraw.Bar.drawBarXAxisLabels(
                ctx = drawContext,
                labels = xLabels,
                metrics = m,
                maxXTicksLimit = maxXTicksLimit
            )
        }

        metrics?.let { m ->
            // draw segments (non-interactive)
            val segmentCounts = stackedData.map { it.segments.size }
            val maxSegments = segmentCounts.maxOrNull() ?: 0
            for (segmentIndex in 0 until maxSegments) {
                val segmentMin = mutableListOf<Float>()
                val segmentMax = mutableListOf<Float>()
                stackedData.forEach { sp ->
                    var cum = 0f
                    for (i in 0 until segmentIndex) cum += sp.segments.getOrNull(i)?.y ?: 0f
                    val seg = sp.segments.getOrNull(segmentIndex)?.y ?: 0f
                    segmentMin.add(cum)
                    segmentMax.add(cum + seg)
                }
                val hasAny = segmentMax.zip(segmentMin).any { (mx, mn) -> mx > mn }
                if (hasAny) {
                    val c = colors.getOrNull(segmentIndex) ?: Color.Gray
                    ChartDraw.Bar.BarMarker(
                        data = stackedData,
                        minValues = segmentMin,
                        maxValues = segmentMax,
                        metrics = m,
                        color = c,
                        barWidthRatio = barWidthRatio,
                        interactive = false,
                        chartType = ChartType.STACKED_BAR,
                        unit = unit
                    )
                }
            }

            // single transparent touch layer per bar
            ChartDraw.Bar.BarMarker(
                data = stackedData,
                minValues = List(stackedData.size) { m.minY },
                maxValues = totals,
                metrics = m,
                onBarClick = { index, _ -> onBarClick(index, totals[index]) },
                barWidthRatio = barWidthRatio,
                chartType = ChartType.STACKED_BAR,
                interactive = true,
                color = Color.Transparent,
                unit = unit
            )
        }
    }
}