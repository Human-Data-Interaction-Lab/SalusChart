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
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.transform.toStackedChartPoints
import kotlin.math.ceil
import kotlin.math.min

private fun ceilToStep(v: Double, step: Double): Double {
    if (step <= 0.0) return v
    return ceil(v / step) * step
}

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

    // interaction – choose one
    interactionType: InteractionType.StackedBar = InteractionType.StackedBar.TOUCH_AREA,
    onBarClick: ((barIndex: Int, segmentIndex: Int?, value: Float) -> Unit)? = null,
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
    yTickStep: Double? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showTitle: Boolean = true,
    autoFixYAxisOnScroll: Boolean = true,

    // paged mode
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    yTickStepDefaultForPaged: Double = 10.0,
    initialPage: Int? = null,
    yAxisFixedWidth: Dp = 2.dp,
    outerPadding: PaddingValues = contentPadding,
    axisToBarsGap: Dp = 0.dp,
    tooltipSafePaddingEnd: Dp = 0.dp
) {
    if (data.isEmpty()) return
    val chartType = ChartType.STACKED_BAR

    // transform to stacked points
    val stackedData = data.toStackedChartPoints(
        segmentOrdering = { group: List<ChartPoint> -> group.sortedByDescending { it.y } }
    )
    val totals: List<Double> = stackedData.map { it.y }
    val xLabels = stackedData.map { it.label ?: it.x.toString() }
    val effectivePageSize = (pageSize ?: 0).coerceAtLeast(0)
    val enablePaging = effectivePageSize > 0 && data.size > effectivePageSize

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
            maxXTicksLimit = effectivePageSize, // show all labels in page
            unit = unit,
            pageSize = effectivePageSize,
            unifyYAxisAcrossPages = unifyYAxisAcrossPages,
            yTickStep = yTickStep ?: yTickStepDefaultForPaged,
            initialPage = initialPage,
            yAxisPosition = yAxisPosition,
            yAxisFixedWidth = yAxisFixedWidth,
            axisToBarsGap = axisToBarsGap,
            tooltipSafePaddingEnd = tooltipSafePaddingEnd,
            outerPadding = outerPadding
        )
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
                    if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) {
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
                    val endPad = if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) 0.dp else marginHorizontal

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
                                minY = 0.0,
                                maxY = null,
                                includeYAxisPadding = !isFixedYAxis,
                                fixedTickStep = yTickStep
                            )
                            chartMetrics = m

                            ChartDraw.drawGrid(this, size, m, yAxisPosition, drawLabels = !isFixedYAxis)
                            if (!isFixedYAxis) ChartDraw.drawYAxis(this, m, yAxisPosition)
                            ChartDraw.drawXAxis(this, m)
                            ChartDraw.Bar.drawBarXAxisLabels(
                                ctx = drawContext,
                                labels = xLabels,
                                metrics = m,
                                maxXTicksLimit = maxXTicksLimit
                            )
                        }

                        val m = chartMetrics ?: return@Box

                        // colored stacked segments (non-interactive)
                        val segCount = stackedData.firstOrNull()?.segments?.size ?: 0
                        for (segIndex in 0 until segCount) {
                            val mins = mutableListOf<Double>()
                            val maxs = mutableListOf<Double>()
                            stackedData.forEach { sp ->
                                var cum = 0.0
                                for (i in 0 until segIndex) cum += sp.segments.getOrNull(i)?.y ?: 0.0
                                val seg = sp.segments.getOrNull(segIndex)?.y ?: 0.0
                                mins.add(cum)
                                maxs.add(cum + seg)
                            }
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

                        when (interactionType) {
                            InteractionType.StackedBar.TOUCH_AREA -> {
                                ChartDraw.Bar.BarMarker(
                                    data = stackedData,
                                    minValues = List(stackedData.size) { m.minY },
                                    maxValues = totals,
                                    metrics = m,
                                    chartType = chartType,
                                    isTouchArea = true,
                                    unit = unit,
                                    onBarClick = { index, _ ->
                                        stackedData.getOrNull(index)?.let { sp ->
                                            onBarClick?.invoke(index, null, sp.y.toFloat())
                                        }
                                    }
                                )
                            }
                            InteractionType.StackedBar.BAR -> {
                                ChartDraw.Bar.BarMarker(
                                    data = stackedData,
                                    minValues = List(stackedData.size) { m.minY },
                                    maxValues = totals,
                                    metrics = m,
                                    chartType = chartType,
                                    interactive = true,
                                    color = Color.Transparent,
                                    unit = unit,
                                    onBarClick = { index, _ ->
                                        stackedData.getOrNull(index)?.let { sp ->
                                            onBarClick?.invoke(index, null, sp.y.toFloat())
                                        }
                                    }
                                )
                            }
                        }

                        if (referenceLineType != ReferenceLineType.NONE) {
                            chartMetrics?.let { m ->
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
                        }
                    }

                    if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) {
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

@Composable
private fun FixedPagerYAxisStacked(
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StackedBarChartPagedInternal(
    modifier: Modifier,
    data: List<ChartPoint>,
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
    maxXTicksLimit: Int?,
    unit: String,

    // paging/scale
    pageSize: Int,
    unifyYAxisAcrossPages: Boolean,
    yTickStep: Double,
    initialPage: Int?,
    yAxisPosition: YAxisPosition,
    yAxisFixedWidth: Dp,
    axisToBarsGap: Dp,
    tooltipSafePaddingEnd: Dp,
    outerPadding: PaddingValues,
) {
    // transform once
    val stackedData = remember(data) {
        data.toStackedChartPoints(segmentOrdering = { group -> group.sortedByDescending { it.y } })
    }
    if (stackedData.isEmpty()) return

    val pageCount = remember(stackedData.size, pageSize) {
        ceil(stackedData.size / pageSize.toDouble()).toInt()
    }
    val firstPage = initialPage ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    val globalMax = remember(stackedData) { stackedData.maxOf { it.y } }
    val minRounded = 0.0
    val maxRounded = remember(globalMax, yTickStep) {
        ceilToStep(globalMax, yTickStep).coerceAtLeast(yTickStep)
    }

    Column(modifier = modifier.padding(outerPadding)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        val chartArea: @Composable () -> Unit = {
            Row(Modifier.fillMaxWidth()) {
                // LEFT fixed external Y-axis
                if (yAxisPosition == YAxisPosition.LEFT && yAxisFixedWidth > 0.dp) {
                    FixedPagerYAxisStacked(
                        minY = minRounded,
                        maxY = if (unifyYAxisAcrossPages) maxRounded else globalMax,
                        yAxisPosition = YAxisPosition.LEFT,
                        step = yTickStep,
                        width = yAxisFixedWidth
                    )
                }

                // Pages (no inner Y-axis)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val start = page * pageSize
                    val end = min(start + pageSize, stackedData.size)
                    val slice = stackedData.subList(start, end)

                    val labels = slice.map { it.label ?: it.x.toString() }
                    val totals = slice.map { it.y }
                    val fixedMaxForPage = if (unifyYAxisAcrossPages) maxRounded
                    else (slice.maxOfOrNull { it.y } ?: maxRounded)

                    val padStart = if (yAxisPosition == YAxisPosition.LEFT) axisToBarsGap else 0.dp
                    val baseEnd  = if (yAxisPosition == YAxisPosition.RIGHT) axisToBarsGap else 0.dp
                    val padEnd   = baseEnd + tooltipSafePaddingEnd

                    var metrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }

                    Box(Modifier.fillMaxWidth().padding(PaddingValues(start = padStart, end = padEnd))) {
                        // canvas (grid + x labels; NO y-axis)
                        Canvas(Modifier.fillMaxSize()) {
                            val m = ChartMath.computeMetrics(
                                size = size,
                                values = totals,
                                chartType = ChartType.STACKED_BAR,
                                minY = 0.0,
                                maxY = fixedMaxForPage,
                                includeYAxisPadding = false,
                                fixedTickStep = yTickStep
                            )
                            metrics = m

                            ChartDraw.drawGrid(this, size, m, yAxisPosition, drawLabels = false)
                            ChartDraw.drawXAxis(this, m)
                            ChartDraw.Bar.drawBarXAxisLabels(
                                ctx = drawContext,
                                labels = labels,
                                metrics = m,
                                maxXTicksLimit = maxXTicksLimit
                            )
                        }

                        metrics?.let { m ->
                            val segCount = slice.firstOrNull()?.segments?.size ?: 0
                            for (segIndex in 0 until segCount) {
                                val mins = MutableList(slice.size) { 0.0 }
                                val maxs = MutableList(slice.size) { 0.0 }
                                for (i in slice.indices) {
                                    var cum = 0.0
                                    for (j in 0 until segIndex) {
                                        cum += slice[i].segments.getOrNull(j)?.y ?: 0.0
                                    }
                                    val seg = slice[i].segments.getOrNull(segIndex)?.y ?: 0.0
                                    mins[i] = cum
                                    maxs[i] = cum + seg
                                }
                                if (maxs.zip(mins).any { (mx, mn) -> mx > mn }) {
                                    val c = colors.getOrNull(segIndex) ?: Color.Gray
                                    ChartDraw.Bar.BarMarker(
                                        data = slice,
                                        minValues = mins,
                                        maxValues = maxs,
                                        metrics = m,
                                        color = c,
                                        barWidthRatio = barWidthRatio,
                                        interactive = false,
                                        chartType = ChartType.STACKED_BAR,
                                        unit = unit
                                    )
                                }
                            }

                            when (interactionType) {
                                InteractionType.StackedBar.TOUCH_AREA -> {
                                    ChartDraw.Bar.BarMarker(
                                        data = slice,
                                        minValues = List(slice.size) { m.minY },
                                        maxValues = totals,
                                        metrics = m,
                                        chartType = ChartType.STACKED_BAR,
                                        isTouchArea = true,
                                        unit = unit,
                                        onBarClick = { localIndex, _ ->
                                            val globalIndex = start + localIndex
                                            onBarClick?.invoke(globalIndex, null, totals[localIndex].toFloat())
                                        }
                                    )
                                }
                                InteractionType.StackedBar.BAR -> {
                                    ChartDraw.Bar.BarMarker(
                                        data = slice,
                                        minValues = List(slice.size) { m.minY },
                                        maxValues = totals,
                                        metrics = m,
                                        chartType = ChartType.STACKED_BAR,
                                        interactive = true,
                                        color = Color.Transparent,
                                        unit = unit,
                                        onBarClick = { localIndex, _ ->
                                            val globalIndex = start + localIndex
                                            onBarClick?.invoke(globalIndex, null, totals[localIndex].toFloat())
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // RIGHT fixed external Y-axis
                if (yAxisPosition == YAxisPosition.RIGHT && yAxisFixedWidth > 0.dp) {
                    FixedPagerYAxisStacked(
                        minY = minRounded,
                        maxY = if (unifyYAxisAcrossPages) maxRounded else globalMax,
                        yAxisPosition = YAxisPosition.RIGHT,
                        step = yTickStep,
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
                Column(Modifier.fillMaxWidth()) {
                    if (showLegend && segmentLabels.isNotEmpty()) {
                        CenteredLegend(segmentLabels, colors, LegendPosition.TOP)
                        Spacer(Modifier.height(40.dp))
                    }
                    Box(Modifier.weight(1f, fill = true)) { chartArea() }
                }
            }
            LegendPosition.BOTTOM -> {
                Column(Modifier.fillMaxWidth()) {
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