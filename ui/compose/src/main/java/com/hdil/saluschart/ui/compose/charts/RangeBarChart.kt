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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.transform.toRangeChartPoints
import com.hdil.saluschart.ui.theme.ChartColor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Float.roundUpToStep(step: Float): Float =
    (kotlin.math.ceil(this / step) * step).toFloat()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<ChartPoint>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Range Bar Chart",
    barColor: Color = ChartColor.Default,
    barWidthRatio: Float = 0.6f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.RangeBar = InteractionType.RangeBar.BAR,
    onBarClick: ((Int, RangeChartPoint) -> Unit)? = null,
    windowSize: Int? = null,
    maxXTicksLimit: Int? = null,
    unit: String = "",

    // Fixed Y-axis (free-scroll mode)
    fixedYAxis: Boolean = false,
    yTickStep: Double? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showTitle: Boolean = true,
    autoFixYAxisOnScroll: Boolean = true,

    // Paged mode
    pagingEnabled: Boolean = false,
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    yTickStepDefaultForPaged: Double = 10.0,
    initialPage: Int? = null,
    yAxisFixedWidth: Dp = 2.dp,               // external fixed axis width in paged mode
    outerPadding: PaddingValues = contentPadding, // wraps axis + pager

    // spacing (paged)
    axisToBarsGap: Dp = 0.dp,
    tooltipSafePaddingEnd: Dp = 0.dp
) {
    if (data.isEmpty()) return
    val chartType = ChartType.RANGE_BAR

    // transform once
    val rangeData = remember(data) {
        data.toRangeChartPoints(
            minValueSelector = { g -> g.minByOrNull { it.y } ?: g.first() },
            maxValueSelector = { g -> g.maxByOrNull { it.y } ?: g.first() }
        )
    }
    if (rangeData.isEmpty()) return

    val effectivePageSize = (pageSize ?: 0).coerceAtLeast(0)
    val enablePaging = effectivePageSize > 0 && rangeData.size > effectivePageSize

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
            maxXTicksLimit = effectivePageSize,
            unit = unit,
            // paging/scale
            pageSize = effectivePageSize,
            unifyYAxisAcrossPages = unifyYAxisAcrossPages,
            yTickStep = yTickStep ?: yTickStepDefaultForPaged,
            initialPage = initialPage,
            yAxisFixedWidth = yAxisFixedWidth,
            axisToBarsGap = axisToBarsGap,
            tooltipSafePaddingEnd = tooltipSafePaddingEnd,
            outerPadding = outerPadding
        )
        return
    }

    // ─────────────────────────────
    // Free-scroll mode with optional fixed Y-axis pane
    // ─────────────────────────────
    val useScrolling  = windowSize != null && windowSize < rangeData.size
    val isFixedYAxis  = if (autoFixYAxisOnScroll) (fixedYAxis || useScrolling) else fixedYAxis
    val scrollState   = rememberScrollState()

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
                val sectionsCount = (rangeData.size.toFloat() / windowSize!!.toFloat()).toInt()
                chartWidth * sectionsCount
            } else null

            val labels = rangeData.map { it.label ?: it.x.toString() }

            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
            var selectedIndex by remember { mutableStateOf<Int?>(null) }

            Row(Modifier.fillMaxSize()) {
                // LEFT fixed axis pane
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

                // Chart area
                val startPad = if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) 0.dp else marginHorizontal
                val endPad   = if (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT) 0.dp else marginHorizontal

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
                            drawLabels = !isFixedYAxis
                        )
                        if (!isFixedYAxis) {
                            ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                        }
                        ChartDraw.Bar.drawBarXAxisLabels(
                            ctx = drawContext,
                            labels = labels,
                            metrics = metrics,
                            maxXTicksLimit = maxXTicksLimit
                        )
                    }

                    // Range bars & interaction
                    val minValues = rangeData.map { it.minPoint.y }
                    val maxValues = rangeData.map { it.maxPoint.y }

                    when (interactionType) {
                        InteractionType.RangeBar.TOUCH_AREA -> {
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

                // RIGHT fixed axis pane
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
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun FixedPagerYAxisRange(
    minY: Double,
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
            values = listOf(minY.toDouble(), maxY.toDouble()),
            chartType = ChartType.RANGE_BAR,
            minY = minY.toDouble(),
            maxY = maxY.toDouble(),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RangeBarChartPagedInternal(
    modifier: Modifier,
    data: List<RangeChartPoint>,
    title: String,
    barColor: Color,
    barWidthRatio: Float,
    yAxisPosition: YAxisPosition,
    interactionType: InteractionType.RangeBar,
    onBarClick: ((Int, RangeChartPoint) -> Unit)?,
    maxXTicksLimit: Int?,
    unit: String,
    // paging/scale
    pageSize: Int,
    unifyYAxisAcrossPages: Boolean,
    yTickStep: Double,
    initialPage: Int?,
    yAxisFixedWidth: Dp,
    axisToBarsGap: Dp,
    tooltipSafePaddingEnd: Dp,
    outerPadding: PaddingValues
) {
    val pageCount = remember(data.size, pageSize) {
        kotlin.math.ceil(data.size / pageSize.toFloat()).toInt()
    }
    val firstPage = initialPage ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    val globalMin = remember(data) { data.minOf { it.minPoint.y } }
    val globalMax = remember(data) { data.maxOf { it.maxPoint.y } }
    val minRounded = kotlin.math.floor(globalMin / yTickStep) * yTickStep
    val maxRounded = kotlin.math.ceil(globalMax / yTickStep) * yTickStep

    Column(modifier = modifier.padding(outerPadding)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            if (yAxisPosition == YAxisPosition.LEFT && yAxisFixedWidth > 0.dp) {
                FixedPagerYAxisRange(
                    minY = if (unifyYAxisAcrossPages) minRounded else globalMin,
                    maxY = if (unifyYAxisAcrossPages) maxRounded else globalMax,
                    yAxisPosition = YAxisPosition.LEFT,
                    step = yTickStep.toFloat(),
                    width = yAxisFixedWidth
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) { page ->
                val start = page * pageSize
                val end = kotlin.math.min(start + pageSize, data.size)
                val slice = data.subList(start, end)

                val labels = slice.map { it.label ?: it.x.toString() }
                val minValues = slice.map { it.minPoint.y }
                val maxValues = slice.map { it.maxPoint.y }

                val padStart = if (yAxisPosition == YAxisPosition.LEFT) axisToBarsGap else 0.dp
                val baseEnd  = if (yAxisPosition == YAxisPosition.RIGHT) axisToBarsGap else 0.dp
                val padEnd   = baseEnd + tooltipSafePaddingEnd

                var metrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
                var selectedIndex by remember { mutableStateOf<Int?>(null) }

                Box(Modifier.fillMaxWidth().padding(PaddingValues(start = padStart, end = padEnd))) {
                    Canvas(Modifier.fillMaxSize()) {
                        val valuesForScale = if (unifyYAxisAcrossPages) {
                            listOf(minRounded, maxRounded)
                        } else {
                            buildList {
                                addAll(minValues)
                                addAll(maxValues)
                            }
                        }
                        val m = ChartMath.computeMetrics(
                            size = size,
                            values = valuesForScale,
                            chartType = ChartType.RANGE_BAR,
                            minY = if (unifyYAxisAcrossPages) minRounded else null,
                            maxY = if (unifyYAxisAcrossPages) maxRounded else null,
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
                        when (interactionType) {
                            InteractionType.RangeBar.TOUCH_AREA -> {
                                ChartDraw.Bar.BarMarker(
                                    data = slice,
                                    minValues = minValues,
                                    maxValues = maxValues,
                                    metrics = m,
                                    color = barColor,
                                    barWidthRatio = barWidthRatio,
                                    interactive = false,
                                    chartType = ChartType.RANGE_BAR,
                                    showTooltipForIndex = selectedIndex,
                                    unit = unit
                                )
                                ChartDraw.Bar.BarMarker(
                                    data = slice,
                                    minValues = List(slice.size) { m.minY },
                                    maxValues = maxValues,
                                    metrics = m,
                                    onBarClick = { localIdx, _ ->
                                        selectedIndex = if (selectedIndex == localIdx) null else localIdx
                                        onBarClick?.invoke(start + localIdx, slice[localIdx])
                                    },
                                    chartType = ChartType.RANGE_BAR,
                                    isTouchArea = true,
                                    unit = unit
                                )
                            }
                            InteractionType.RangeBar.BAR -> {
                                ChartDraw.Bar.BarMarker(
                                    data = slice,
                                    minValues = minValues,
                                    maxValues = maxValues,
                                    metrics = m,
                                    color = barColor,
                                    barWidthRatio = barWidthRatio,
                                    interactive = true,
                                    onBarClick = { localIdx, _ ->
                                        onBarClick?.invoke(start + localIdx, slice[localIdx])
                                    },
                                    chartType = ChartType.RANGE_BAR,
                                    unit = unit
                                )
                            }
                        }
                    }
                }
            }

            if (yAxisPosition == YAxisPosition.RIGHT && yAxisFixedWidth > 0.dp) {
                FixedPagerYAxisRange(
                    minY = if (unifyYAxisAcrossPages) minRounded else globalMin,
                    maxY = if (unifyYAxisAcrossPages) maxRounded else globalMax,
                    yAxisPosition = YAxisPosition.RIGHT,
                    step = yTickStep.toFloat(),
                    width = yAxisFixedWidth
                )
            }
        }
    }
}
