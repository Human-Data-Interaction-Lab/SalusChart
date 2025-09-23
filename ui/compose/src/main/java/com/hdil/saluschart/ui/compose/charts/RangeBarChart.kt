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
    chartType: ChartType = ChartType.RANGE_BAR,
    windowSize: Int? = null, // 윈도우 크기 (null이면 전체 화면)
    maxXTicksLimit: Int? = null, // X축에 표시할 최대 라벨 개수 (null이면 모든 라벨 표시)
    unit: String = ""

    // Free-scroll mode
    windowSize: Int? = null,
    maxXTicksLimit: Int? = null,
    unit: String = "",

    // Fixed Y-axis (free-scroll mode)
    fixedYAxis: Boolean = false,
    yAxisFixedWidth: Dp = 16.dp,
    yTickStep: Float? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showTitle: Boolean = true,
    autoFixYAxisOnScroll: Boolean = true,

    // Paged mode
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    yTickStepDefaultForPaged: Float = 10f,

    // spacing / layout (paged mode)
    axisOuterPadding: Dp = 12.dp,      // gap between screen edge and fixed Y axis
    axisToBarsGap: Dp = 0.dp,          // tiny gap between fixed Y axis and first bar
    tooltipSafePaddingEnd: Dp = 0.dp  // extra space on the far end to avoid clipping tooltips
) {
    if (data.isEmpty()) return

    val rangeData = data.toRangeChartPoints(
        minValueSelector = { group: List<ChartPoint> -> group.minByOrNull { it.y } ?: group.first() },
        maxValueSelector = { group: List<ChartPoint> -> group.maxByOrNull { it.y } ?: group.first() }
    )
    if (rangeData.isEmpty()) return

    // ─────────────────────────────
    // Paged mode (HorizontalPager)
    // ─────────────────────────────
    if (pageSize != null && pageSize > 0) {
        val pageCount = remember(rangeData.size, pageSize) {
            kotlin.math.ceil(rangeData.size / pageSize.toFloat()).toInt()
        }
        val pagerState = rememberPagerState(
            initialPage = pageCount - 1,
            pageCount = { pageCount }
        )

        // Unified axis across pages for a stable Y scale
        val globalMin = rangeData.minOf { it.minPoint.y }
        val globalMax = rangeData.maxOf { it.maxPoint.y }
        val tickStep = yTickStep ?: yTickStepDefaultForPaged
        val minRounded = (kotlin.math.floor(globalMin / tickStep) * tickStep).toFloat()
        val maxRounded = globalMax.roundUpToStep(tickStep)

        Column(modifier) {
            if (showTitle) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            Row(Modifier.fillMaxWidth()) {
                // LEFT fixed axis
                if (yAxisPosition == YAxisPosition.LEFT) {
                    Box(Modifier
                        .padding(start = axisOuterPadding)   // gap from the screen edge
                        .fillMaxHeight()
                        .width(yAxisFixedWidth)
                    ) {
                        FixedPagerYAxisRange(
                            minY = if (unifyYAxisAcrossPages) minRounded else globalMin,
                            maxY = if (unifyYAxisAcrossPages) maxRounded else globalMax,
                            yAxisPosition = YAxisPosition.LEFT,
                            step = tickStep,
                            width = yAxisFixedWidth
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) { page ->
                    val start = page * pageSize
                    val end   = kotlin.math.min(start + pageSize, rangeData.size)
                    val slice = rangeData.subList(start, end)

                    val padStart = if (yAxisPosition == YAxisPosition.LEFT) axisToBarsGap else 16.dp
                    // add extra space at the far end for tooltip safety
                    val baseEnd  = if (yAxisPosition == YAxisPosition.RIGHT) axisToBarsGap else 16.dp
                    val padEnd   = baseEnd + tooltipSafePaddingEnd

                    RangeBarChartPageContent(
                        rangeData = slice,
                        title = title,
                        barColor = barColor,
                        barWidthRatio = barWidthRatio,
                        yAxisPosition = yAxisPosition,
                        interactionType = interactionType,
                        onBarClick = if (onBarClick != null) { i, p -> onBarClick(start + i, p) } else null,
                        chartType = chartType,
                        maxXTicksLimit = pageSize,
                        fixedYAxis = true,
                        yTickStep = tickStep,
                        showTitle = false,
                        contentPadding = PaddingValues(start = padStart, end = padEnd),
                        unit = unit,
                        fixedMinY = if (unifyYAxisAcrossPages) minRounded else null,
                        fixedMaxY = if (unifyYAxisAcrossPages) maxRounded else null,
                    )
                }

                // RIGHT fixed axis
                if (yAxisPosition == YAxisPosition.RIGHT) {
                    Box(Modifier
                        .padding(end = axisOuterPadding)     // gap from the screen edge
                        .fillMaxHeight()
                        .width(yAxisFixedWidth)
                    ) {
                        FixedPagerYAxisRange(
                            minY = if (unifyYAxisAcrossPages) minRounded else globalMin,
                            maxY = if (unifyYAxisAcrossPages) maxRounded else globalMax,
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
                            chartType = ChartType.RANGE_BAR,
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
private fun FixedPagerYAxisRange(
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
            chartType = ChartType.RANGE_BAR,
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
private fun RangeBarChartPageContent(
    rangeData: List<RangeChartPoint>,
    title: String,
    barColor: Color,
    barWidthRatio: Float,
    yAxisPosition: YAxisPosition,
    interactionType: InteractionType.RangeBar,
    onBarClick: ((Int, RangeChartPoint) -> Unit)?,
    chartType: ChartType,
    maxXTicksLimit: Int?,
    fixedYAxis: Boolean,
    yTickStep: Float,
    showTitle: Boolean,
    contentPadding: PaddingValues,
    unit: String,
    fixedMinY: Float?,
    fixedMaxY: Float?
) {
    if (rangeData.isEmpty()) return

    Column(Modifier.fillMaxWidth().padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        val labels = rangeData.map { it.label ?: it.x.toString() }
        var metricsState by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
        var selectedIndex by remember { mutableStateOf<Int?>(null) }

        Box(Modifier.fillMaxWidth().fillMaxHeight()) {
            Canvas(Modifier.fillMaxSize()) {
                val valuesForScale = buildList {
                    addAll(rangeData.map { it.minPoint.y })
                    addAll(rangeData.map { it.maxPoint.y })
                }
                val metrics = ChartMath.computeMetrics(
                    size = size,
                    values = valuesForScale,
                    chartType = ChartType.RANGE_BAR,
                    minY = fixedMinY,
                    maxY = fixedMaxY,
                    includeYAxisPadding = !fixedYAxis,
                    fixedTickStep = yTickStep
                )
                metricsState = metrics

                ChartDraw.drawGrid(
                    drawScope = this,
                    size = size,
                    metrics = metrics,
                    yAxisPosition = yAxisPosition,
                    drawLabels = !fixedYAxis
                )
                if (!fixedYAxis) {
                    ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                }
                ChartDraw.Bar.drawBarXAxisLabels(
                    ctx = drawContext,
                    labels = labels,
                    metrics = metrics,
                    maxXTicksLimit = maxXTicksLimit
                )
            }

            val minValues = rangeData.map { it.minPoint.y }
            val maxValues = rangeData.map { it.maxPoint.y }

            when (interactionType) {
                InteractionType.RangeBar.TOUCH_AREA -> {
                    metricsState?.let { m ->
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
                    metricsState?.let { m ->
                        ChartDraw.Bar.BarMarker(
                            data = rangeData,
                            minValues = minValues,
                            maxValues = maxValues,
                            metrics = m,
                            color = barColor,
                            barWidthRatio = barWidthRatio,
                            interactive = true,
                            onBarClick = { idx, _ -> onBarClick?.invoke(idx, rangeData[idx]) },
                            chartType = chartType,
                            unit = unit
                        )
                    }
                }
            }
        }
    }
}
