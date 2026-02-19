package com.hdil.saluschart.ui.compose.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.TooltipSpec
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions
import com.hdil.saluschart.core.chart.toRangeChartMarksByXGroup
import com.hdil.saluschart.ui.theme.ChartColor
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.round

/**
 * Renders a **range bar chart** where each x-position is drawn as a vertical bar spanning
 * from a minimum value to a maximum value.
 *
 * ## Behavior
 * - Each item in [data] contributes one bar representing `[minPoint.y, maxPoint.y]`.
 * - Optional **tap selection** can be handled either on the bar itself or via a larger touch area
 *   (see [interactionType]). Tapping the selected bar again clears selection.
 * - Optional **overlay points** can be drawn on top of each bar (see [pointValues]).
 * - Optional **horizontal reference lines** can be drawn (see [referenceLines]).
 * - Optional **legend** can be shown below the x-axis tick labels (see [legendItems], [showLegend]).
 *
 * ## Layout modes
 * This chart supports three display modes:
 * 1) Static (default): all items are shown without paging or scrolling.
 * 2) Scrolling: enabled when [windowSize] is non-null and smaller than `data.size`.
 *    The chart becomes horizontally scrollable and attempts to keep the Y-axis readable.
 * 3) Paging: enabled when [pageSize] is non-null, `pageSize > 0`, and smaller than `data.size`.
 *
 * Paging mode and scrolling mode are **mutually exclusive**; enabling both will throw.
 *
 * ## Axes and labels
 * - X-axis tick labels are derived from each mark’s `label` when available, otherwise `x.toString()`.
 * - [xLabel] and [yLabel] are optional titles; they are only shown when not blank.
 *
 * @param modifier Modifier applied to the chart container.
 * @param data Chart data. Each entry is a [RangeChartMark] containing min/max points for one x-position.
 * @param xLabel Optional x-axis title shown below the chart when not blank.
 * @param yLabel Optional y-axis title (layout-dependent) when not blank.
 * @param title Chart title shown above the chart when [showTitle] is true.
 * @param barColor Color used to draw range bars.
 * @param barWidthRatio Ratio of bar width to each slot width (0–1 recommended).
 * @param yAxisPosition Side on which the Y-axis is drawn.
 * @param interactionType Controls whether taps target the bar itself or a larger touch area.
 * @param onBarClick Callback invoked when a bar is selected. Provides (globalIndex, mark).
 * @param showTitle Whether to display [title].
 * @param showYAxis Whether to draw the Y-axis and y-grid labels (exact rendering depends on mode).
 * @param xLabelAutoSkip If true, x-axis tick labels may be skipped to avoid overlap.
 * @param maxXTicksLimit Optional maximum number of x-axis tick labels to render.
 * @param yTickStep Optional fixed tick step for the y-axis grid. If null, ticks are computed automatically.
 * @param unit Unit suffix used for tooltip/value formatting (e.g., `"mg/dL"`, `"bpm"`).
 * @param pointValues Optional overlay points for each x-position (one list per bar index).
 *   `pointValues[index]` corresponds to `data[index]`.
 * @param pointColor Color used to draw overlay points.
 * @param pointRadius Radius of overlay points.
 * @param barCornerRadiusFraction Uniform corner radius fraction applied to bars (recommended 0.0–0.5).
 * @param barCornerRadiusFractions Optional per-corner radius configuration. Takes priority over [barCornerRadiusFraction].
 * @param roundTopOnly If true, only the top corners are rounded for bars.
 * @param windowSize Enables free scrolling when provided and smaller than `data.size`.
 *   Interpreted as the target number of visible items in the viewport.
 * @param contentPadding Padding applied around the chart content.
 * @param pageSize Enables paging when provided, `pageSize > 0`, and smaller than `data.size`.
 *   Interpreted as the number of items per page.
 * @param initialPageIndex Optional initial page index for paging mode. If null, defaults to the last page.
 * @param yAxisFixedWidth Width reserved for an external/fixed Y-axis pane in modes that support it.
 * @param referenceLines Optional horizontal reference lines drawn across the plot area.
 * @param legendItems Legend entries. When non-empty and [showLegend] is true, the legend is shown below the chart.
 * @param showLegend Whether to display the legend (requires [legendItems] to be non-empty).
 *
 * @throws IllegalArgumentException if both [windowSize] and [pageSize] are provided.
 */

data class LegendItem(
    val label: String,
    val color: Color,
    val shape: LegendShape = LegendShape.Dot
)

enum class LegendShape { Dot, Box }

@JvmName("RangeBarChartRangeMarks")
@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<RangeChartMark>,
    xLabel: String = "",
    yLabel: String = "",
    title: String = "Range Bar Chart",
    barColor: Color = ChartColor.Default,
    barWidthRatio: Float = 0.6f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.RangeBar = InteractionType.RangeBar.BAR,
    onBarClick: ((Int, RangeChartMark) -> Unit)? = null,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    yTickStep: Double? = null,
    unit: String = "",
    pointValues: List<List<Double>>? = null,
    pointColor: Color = barColor,
    pointRadius: Dp = 3.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 0.dp,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    legendItems: List<LegendItem> = emptyList(),
    showLegend: Boolean = true,
    ) {
    if (data.isEmpty()) return

    require(windowSize == null || pageSize == null) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)
    val enablePaging = requestedPageSize > 0 && data.size > requestedPageSize

    if (enablePaging) {
        RangeBarChartPagedInternal(
            modifier = modifier,
            data = data,
            title = title,
            barColor = barColor,
            barWidthRatio = barWidthRatio,
            yAxisPosition = yAxisPosition,
            interactionType = interactionType,
            onBarClick = onBarClick,
            showTitle = showTitle,
            showYAxis = showYAxis,
            maxXTicksLimit = maxXTicksLimit,
            xLabelAutoSkip = xLabelAutoSkip,
            unit = unit,
            pointValues = pointValues,
            pointColor = pointColor,
            pointRadius = pointRadius,
            pageSize = requestedPageSize,
            yTickStep = yTickStep,
            initialPageIndex = initialPageIndex,
            outerPadding = contentPadding,
            yAxisFixedWidth = yAxisFixedWidth,
            barCornerRadiusFraction = barCornerRadiusFraction,
            barCornerRadiusFractions = barCornerRadiusFractions,
            roundTopOnly = roundTopOnly,
            referenceLines = referenceLines,
            xLabel = xLabel,
            )
        return
    }

    RangeBarChartContent(
        modifier = modifier,
        rangeData = data,
        xLabel = xLabel,
        yLabel = yLabel,
        title = title,
        barColor = barColor,
        barWidthRatio = barWidthRatio,
        yAxisPosition = yAxisPosition,
        interactionType = interactionType,
        onBarClick = onBarClick,
        showTitle = showTitle,
        showYAxis = showYAxis,
        xLabelAutoSkip = xLabelAutoSkip,
        maxXTicksLimit = maxXTicksLimit,
        yTickStep = yTickStep,
        unit = unit,
        pointValues = pointValues,
        pointColor = pointColor,
        pointRadius = pointRadius,
        barCornerRadiusFraction = barCornerRadiusFraction,
        barCornerRadiusFractions = barCornerRadiusFractions,
        roundTopOnly = roundTopOnly,
        windowSize = windowSize,
        contentPadding = contentPadding,
        yAxisFixedWidth = yAxisFixedWidth,
        referenceLines = referenceLines,
        legendItems = legendItems,
        showLegend = showLegend,
        )
}

@JvmName("RangeBarChartChartMarks")
@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "",
    yLabel: String = "",
    title: String = "Range Bar Chart",
    barColor: Color = ChartColor.Default,
    barWidthRatio: Float = 0.6f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.RangeBar = InteractionType.RangeBar.BAR,
    onBarClick: ((Int, RangeChartMark) -> Unit)? = null,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    yTickStep: Double? = null,
    unit: String = "",
    pointValues: List<List<Double>>? = null,
    pointColor: Color = barColor,
    pointRadius: Dp = 3.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 0.dp,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    legendItems: List<LegendItem> = emptyList(),
    showLegend: Boolean = true,
    ) {
    val rangeData = remember(data) {
        data.toRangeChartMarksByXGroup(
            minValueSelector = { group -> group.minByOrNull { it.y } ?: group.first() },
            maxValueSelector = { group -> group.maxByOrNull { it.y } ?: group.first() }
        )
    }

    RangeBarChart(
        modifier = modifier,
        data = rangeData,
        xLabel = xLabel,
        yLabel = yLabel,
        title = title,
        barColor = barColor,
        barWidthRatio = barWidthRatio,
        yAxisPosition = yAxisPosition,
        interactionType = interactionType,
        onBarClick = onBarClick,
        showTitle = showTitle,
        showYAxis = showYAxis,
        xLabelAutoSkip = xLabelAutoSkip,
        maxXTicksLimit = maxXTicksLimit,
        yTickStep = yTickStep,
        unit = unit,
        pointValues = pointValues,
        pointColor = pointColor,
        pointRadius = pointRadius,
        barCornerRadiusFraction = barCornerRadiusFraction,
        barCornerRadiusFractions = barCornerRadiusFractions,
        roundTopOnly = roundTopOnly,
        windowSize = windowSize,
        contentPadding = contentPadding,
        pageSize = pageSize,
        initialPageIndex = initialPageIndex,
        yAxisFixedWidth = yAxisFixedWidth,
        referenceLines = referenceLines,
        legendItems = legendItems,
        showLegend = showLegend,
        )
}

@Composable
private fun RangeBarChartContent(
    modifier: Modifier,
    rangeData: List<RangeChartMark>,
    xLabel: String,
    yLabel: String,
    title: String,
    barColor: Color,
    barWidthRatio: Float,
    yAxisPosition: YAxisPosition,
    interactionType: InteractionType.RangeBar,
    onBarClick: ((Int, RangeChartMark) -> Unit)?,
    showTitle: Boolean,
    showYAxis: Boolean,
    xLabelAutoSkip: Boolean,
    maxXTicksLimit: Int?,
    yTickStep: Double?,
    unit: String,
    pointValues: List<List<Double>>?,
    pointColor: Color,
    pointRadius: Dp,
    barCornerRadiusFraction: Float,
    barCornerRadiusFractions: BarCornerRadiusFractions?,
    roundTopOnly: Boolean,
    windowSize: Int?,
    contentPadding: PaddingValues,
    yAxisFixedWidth: Dp,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    forcedMinY: Double? = null,
    forcedMaxY: Double? = null,
    legendItems: List<LegendItem> = emptyList(),
    showLegend: Boolean = true,
) {
    if (rangeData.isEmpty()) return

    val chartType = ChartType.RANGE_BAR

    val useScrolling = windowSize != null && windowSize < rangeData.size
    val isFixedYAxis = showYAxis && useScrolling
    val scrollState = rememberScrollState()

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        // Axis titles
        if (showYAxis) {
            Spacer(Modifier.height(4.dp))
        }

        BoxWithConstraints {
            val availableWidth = maxWidth
            val parentWidthDp = maxWidth
            val marginHorizontal = 16.dp

            // Calculate canvas width for scrolling mode
            val canvasWidth = if (useScrolling) {
                val chartWidth = availableWidth - (marginHorizontal * 2)
                val slotWidth = chartWidth / windowSize!!.toFloat()
                slotWidth * rangeData.size
            } else null

            val chartWidth = availableWidth - (marginHorizontal * 2)
            val slotWidthDp = if (useScrolling) chartWidth / windowSize!!.toFloat() else 0.dp
            val clipRightDp = if (useScrolling) slotWidthDp * 0.55f else 0.dp

            val labels = rangeData.map { it.label ?: it.x.toString() }

            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
            var selectedIndex by remember { mutableStateOf<Int?>(null) }
            var tooltipSpec by remember { mutableStateOf<TooltipSpec?>(null) }

            val legendAndLabelSpace =
                (if (showLegend && legendItems.isNotEmpty()) 32.dp else 0.dp) +
                        (if (xLabel.isNotBlank()) 24.dp else 0.dp) +
                        28.dp // padding buffer

            val chartAreaHeight = (maxHeight - legendAndLabelSpace).coerceAtLeast(220.dp)

            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartAreaHeight)
                ) {
                    // Left fixed axis pane (only when scrolling + fixed y-axis)
                    if (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT) {
                        Canvas(
                            modifier = Modifier
                                .width(yAxisFixedWidth)
                                .fillMaxHeight()
                        ) {
                            chartMetrics?.let { m ->
                                val tol = 1e-6
                                val baseTicks = m.yTicks.map(::norm)

                                fun findExistingTick(y: Double): Double? {
                                    val ny = norm(y)
                                    return baseTicks.firstOrNull { close(it, ny, tol) }
                                }

                                val highlightTargets: List<Double> =
                                    referenceLines.map { spec ->
                                        findExistingTick(spec.y) ?: norm(
                                            spec.y
                                        )
                                    }

                                val extraTickValues: List<Double> =
                                    highlightTargets.filter { target ->
                                        baseTicks.none {
                                            close(
                                                it,
                                                target,
                                                tol
                                            )
                                        }
                                    }

                                ChartDraw.drawYAxisStandalone(
                                    drawScope = this,
                                    metrics = m,
                                    yAxisPosition = yAxisPosition,
                                    paneWidthPx = size.width,
                                    highlightValues = highlightTargets,
                                    extraTickValues = extraTickValues,
                                    highlightColorForValue = { y ->
                                        val match =
                                            referenceLines.minByOrNull { abs(norm(it.y) - norm(y)) }
                                        match?.color ?: Color.Transparent
                                    },
                                    highlightTolerance = tol
                                )
                            }
                        }
                    }

                    // Padding rules around the chart canvas
                    val startPad =
                        if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT)) 0.dp else marginHorizontal
                    val endPad =
                        if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT)) 0.dp else marginHorizontal

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = startPad, end = endPad)
                    ) {
                    // Chart plot area (scrollable if needed)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .let {
                                if (useScrolling) it.horizontalScroll(
                                    scrollState,
                                    overscrollEffect = null
                                ) else it
                            }
                            .padding(end = clipRightDp)
                    ) {
                        Canvas(
                            modifier = if (useScrolling) {
                                Modifier
                                    .width(canvasWidth!!)
                                    .fillMaxHeight()
                            } else {
                                Modifier.fillMaxSize()
                            }
                        ) {
                            val valuesForScale = buildList {
                                addAll(rangeData.map { it.minPoint.y })
                                addAll(rangeData.map { it.maxPoint.y })
                            }

                            val metrics = ChartMath.computeMetrics(
                                size = size,
                                values = valuesForScale,
                                chartType = chartType,
                                minY = forcedMinY,
                                maxY = forcedMaxY,
                                includeYAxisPadding = false,
                                fixedTickStep = yTickStep
                            )

                            if (chartMetrics != metrics) chartMetrics = metrics

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

                        // Range bars & interaction (Composable layer)
                        val minValues = rangeData.map { it.minPoint.y }
                        val maxValues = rangeData.map { it.maxPoint.y }

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .zIndex(1f)
                        ) {
                            when (interactionType) {
                                InteractionType.RangeBar.TOUCH_AREA -> {
                                    chartMetrics?.let { m ->
                                        // Visual bars
                                        ChartDraw.Bar.BarMarker(
                                            data = rangeData,
                                            minValues = minValues,
                                            maxValues = maxValues,
                                            metrics = m,
                                            color = barColor,
                                            barWidthRatio = barWidthRatio,
                                            interactive = false,
                                            chartType = chartType,
                                            unit = unit,
                                            showTooltipForIndex = selectedIndex,
                                            onTooltipSpec = { tooltipSpec = it },
                                            barCornerRadiusFraction = barCornerRadiusFraction,
                                            barCornerRadiusFractions = barCornerRadiusFractions,
                                            roundTopOnly = roundTopOnly,
                                        )

                                        // Transparent touch strips
                                        ChartDraw.Bar.BarMarker(
                                            data = rangeData,
                                            minValues = List(rangeData.size) { m.minY },
                                            maxValues = maxValues,
                                            metrics = m,
                                            chartType = chartType,
                                            isTouchArea = true,
                                            unit = unit,
                                            onBarClick = { idx, _ ->
                                                selectedIndex =
                                                    if (selectedIndex == idx) null else idx
                                                onBarClick?.invoke(idx, rangeData[idx])
                                            },
                                            barCornerRadiusFraction = barCornerRadiusFraction,
                                            barCornerRadiusFractions = barCornerRadiusFractions,
                                            roundTopOnly = roundTopOnly,
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
                                            interactive = false,
                                            onBarClick = { idx, _ ->
                                                selectedIndex =
                                                    if (selectedIndex == idx) null else idx
                                                onBarClick?.invoke(idx, rangeData[idx])
                                            },
                                            chartType = chartType,
                                            unit = unit,
                                            showTooltipForIndex = selectedIndex,
                                            onTooltipSpec = { tooltipSpec = it },
                                            barCornerRadiusFraction = barCornerRadiusFraction,
                                            barCornerRadiusFractions = barCornerRadiusFractions,
                                            roundTopOnly = roundTopOnly,
                                        )
                                    }
                                }
                            }
                        }

                        // Overlay points (Canvas layer)
                        if (!pointValues.isNullOrEmpty()) {
                            chartMetrics?.let { m ->
                                val radiusPx = with(LocalDensity.current) { pointRadius.toPx() }
                                val dataSize = rangeData.size
                                val slotWidth = m.chartWidth / dataSize.toFloat()
                                val activeIndex = selectedIndex

                                Canvas(modifier = Modifier.matchParentSize()) {
                                    pointValues.forEachIndexed { index, values ->
                                        if (index >= dataSize) return@forEachIndexed

                                        val centerX = m.paddingX + slotWidth * (index + 0.5f)

                                        val dotColor =
                                            if (activeIndex == null || index == activeIndex) {
                                                pointColor
                                            } else {
                                                pointColor.copy(alpha = 0.2f)
                                            }

                                        values.forEach { v ->
                                            val minY = m.minY
                                            val maxY = m.maxY

                                            fun valueToYPx(value: Double): Float {
                                                val ratio =
                                                    if (maxY == minY) 0f
                                                    else ((value - minY) / (maxY - minY)).toFloat()
                                                return m.chartHeight * (1f - ratio)
                                            }

                                            val yDot = valueToYPx(v)

                                            drawCircle(
                                                color = dotColor,
                                                radius = radiusPx,
                                                center = Offset(centerX, yDot)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Reference lines overlay
                        if (referenceLines.isNotEmpty()) {
                            chartMetrics?.let { m ->
                                Canvas(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .zIndex(10f)
                                ) {
                                    ReferenceLineDraw.drawHorizontalLinesOnly(
                                        drawScope = this,
                                        metrics = m,
                                        lines = referenceLines
                                    )
                                }
                            }
                        }

                        // Tooltip overlay
                        tooltipSpec?.let { spec ->
                            val density = LocalDensity.current
                            val parentWidthPx = with(density) { parentWidthDp.toPx() }

                            val estimatedWidthPx = with(density) { 160.dp.toPx() }
                            var measuredWidthPx by remember(spec) { mutableStateOf<Float?>(null) }
                            val tooltipWidthPx = measuredWidthPx ?: estimatedWidthPx

                            val anchorXPx = spec.offset.x
                            val anchorYPx = spec.offset.y
                            val gapPx = with(density) { 8.dp.toPx() }

                            val wouldOverflowRight =
                                anchorXPx + tooltipWidthPx + gapPx > parentWidthPx
                            val targetXPx =
                                if (wouldOverflowRight) anchorXPx - tooltipWidthPx - gapPx
                                else anchorXPx + gapPx

                            val animatedX by animateFloatAsState(
                                targetValue = targetXPx,
                                label = "tooltipX"
                            )

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .zIndex(999f)
                            ) {
                                ChartTooltip(
                                    chartMark = spec.chartMark,
                                    unit = unit,
                                    color = barColor,
                                    modifier = Modifier
                                        .offset(
                                            x = with(density) { animatedX.toDp() },
                                            y = with(density) { anchorYPx.toDp() } - 80.dp
                                        )
                                        .onSizeChanged { measuredWidthPx = it.width.toFloat() }
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
                                val tol = 1e-6
                                val baseTicks = m.yTicks.map(::norm)

                                fun findExistingTick(y: Double): Double? {
                                    val ny = norm(y)
                                    return baseTicks.firstOrNull { close(it, ny, tol) }
                                }

                                val highlightTargets: List<Double> =
                                    referenceLines.map { spec -> findExistingTick(spec.y) ?: norm(spec.y) }

                                val extraTickValues: List<Double> =
                                    highlightTargets.filter { target -> baseTicks.none { close(it, target, tol) } }

                                ChartDraw.drawYAxisStandalone(
                                    drawScope = this,
                                    metrics = m,
                                    yAxisPosition = yAxisPosition,
                                    paneWidthPx = size.width,
                                    highlightValues = highlightTargets,
                                    extraTickValues = extraTickValues,
                                    highlightColorForValue = { y ->
                                        val match = referenceLines.minByOrNull { abs(norm(it.y) - norm(y)) }
                                        match?.color ?: Color.Transparent
                                    },
                                    highlightTolerance = tol
                                )
                            }
                        }
                    }
                }
                if (showLegend && legendItems.isNotEmpty()) {
                    Spacer(Modifier.height(28.dp))
                    ChartLegend(items = legendItems)
                }
                if (xLabel.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = xLabel, style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (showYAxis && yLabel.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

object ReferenceLineDraw {
    fun drawHorizontalLinesOnly(
        drawScope: DrawScope,
        metrics: ChartMath.ChartMetrics,
        lines: List<ReferenceLineSpec>,
    ) = with(drawScope) {
        if (lines.isEmpty()) return

        val dashCache = HashMap<LineStyle, PathEffect?>()

        fun valueToCanvasY(v: Double): Float {
            val minY = metrics.minY
            val maxY = metrics.maxY
            val ratio = if (maxY == minY) 0f else ((v - minY) / (maxY - minY)).toFloat()
            return metrics.paddingY + metrics.chartHeight * (1f - ratio)
        }

        val startX = metrics.paddingX
        val endX = metrics.paddingX + metrics.chartWidth

        lines.forEach { spec ->
            if (spec.y < metrics.minY || spec.y > metrics.maxY) return@forEach

            val yPx = valueToCanvasY(spec.y)

            val pathEffect = dashCache.getOrPut(spec.style) {
                spec.style.dashPattern?.let { PathEffect.dashPathEffect(it, 0f) }
            }

            drawLine(
                color = spec.color,
                start = Offset(startX, yPx),
                end = Offset(endX, yPx),
                strokeWidth = spec.strokeWidth.toPx(),
                pathEffect = pathEffect
            )
        }
    }
}

private fun norm(v: Double): Double = round(v * 1_000_000.0) / 1_000_000.0
private fun close(a: Double, b: Double, tol: Double) = abs(a - b) <= tol

// Function for fixed external Y-axis in paged range bar chart
@Composable
private fun FixedPagerYAxisRange(
    minY: Double,
    maxY: Double,
    yAxisPosition: YAxisPosition,
    step: Double,
    width: Dp,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
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
            paneWidthPx = size.width,
            highlightValues = referenceLines.map { it.y },
            highlightColorForValue = { v ->
                referenceLines.firstOrNull { it.y == v }?.color ?: Color.Transparent
            },
            extraTickValues = referenceLines.map { it.y },
            highlightTolerance = 1e-6
        )
    }
}

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
    xLabel: String,
    maxXTicksLimit: Int? = null,
    xLabelAutoSkip: Boolean,
    unit: String,
    pointValues: List<List<Double>>? = null,
    pointColor: Color = barColor,
    pointRadius: Dp = 3.dp,
    pageSize: Int,
    yTickStep: Double?,
    initialPageIndex: Int?,
    outerPadding: PaddingValues,
    yAxisFixedWidth: Dp = 0.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
) {
    if (data.isEmpty()) return
    require(pageSize > 0) { "pageSize must be > 0" }

    val pageCount = remember(data.size, pageSize) {
        kotlin.math.ceil(data.size / pageSize.toFloat()).toInt().coerceAtLeast(1)
    }
    val firstPage = initialPageIndex ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    // Unified Y axis range across ALL pages
    val yAxisRange = remember(data, yTickStep) {
        val allValues = buildList {
            addAll(data.map { it.minPoint.y })
            addAll(data.map { it.maxPoint.y })
        }
        ChartMath.computeYAxisRange(
            values = allValues,
            chartType = ChartType.RANGE_BAR,
            minY = null,
            maxY = null,
            fixedTickStep = yTickStep
        )
    }

    val unifiedMinY = yAxisRange.minY
    val unifiedMaxY = yAxisRange.maxY
    val unifiedTickStep = yAxisRange.tickStep

    Column(modifier = modifier.padding(outerPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
        }

        Row(Modifier.fillMaxWidth()) {

            // LEFT fixed external Y-axis (paged mode)
            if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                FixedPagerYAxisRange(
                    minY = unifiedMinY,
                    maxY = unifiedMaxY,
                    yAxisPosition = YAxisPosition.LEFT,
                    step = unifiedTickStep,
                    width = yAxisFixedWidth,
                    referenceLines = referenceLines
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val start = page * pageSize
                    val endExclusive = min(start + pageSize, data.size)

                    val pageSlice = data.subList(start, endExclusive)

                    val pagePoints = pointValues?.let { pts ->
                        val safeEnd = min(endExclusive, pts.size)
                        if (start >= safeEnd) emptyList()
                        else pts.subList(start, safeEnd)
                    }

                    RangeBarChartContent(
                        modifier = Modifier.fillMaxSize(),
                        rangeData = pageSlice,
                        xLabel = "",
                        yLabel = "",
                        title = title,
                        barColor = barColor,
                        barWidthRatio = barWidthRatio,
                        yAxisPosition = yAxisPosition,
                        interactionType = interactionType,
                        onBarClick = onBarClick?.let { cb ->
                            { localIdx, mark -> cb(start + localIdx, mark) }
                        },
                        showTitle = false,
                        showYAxis = false,
                        xLabelAutoSkip = xLabelAutoSkip,
                        maxXTicksLimit = maxXTicksLimit,
                        yTickStep = unifiedTickStep,
                        unit = unit,
                        pointValues = pagePoints,
                        pointColor = pointColor,
                        pointRadius = pointRadius,
                        barCornerRadiusFraction = barCornerRadiusFraction,
                        barCornerRadiusFractions = barCornerRadiusFractions,
                        roundTopOnly = roundTopOnly,
                        windowSize = null,
                        contentPadding = PaddingValues(0.dp),
                        yAxisFixedWidth = 0.dp,
                        forcedMinY = unifiedMinY,
                        forcedMaxY = unifiedMaxY,
                        referenceLines = emptyList(),
                    )
                }

                if (referenceLines.isNotEmpty()) {
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .zIndex(10f)
                    ) {
                        val metrics = ChartMath.computeMetrics(
                            size = size,
                            values = listOf(unifiedMinY, unifiedMaxY),
                            chartType = ChartType.RANGE_BAR,
                            minY = unifiedMinY,
                            maxY = unifiedMaxY,
                            includeYAxisPadding = false,
                            fixedTickStep = unifiedTickStep
                        )

                        ReferenceLineDraw.drawHorizontalLinesOnly(
                            drawScope = this,
                            metrics = metrics,
                            lines = referenceLines
                        )
                    }
                }
            }

            // RIGHT fixed external Y-axis (paged mode)
            if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                FixedPagerYAxisRange(
                    minY = unifiedMinY,
                    maxY = unifiedMaxY,
                    yAxisPosition = YAxisPosition.RIGHT,
                    step = unifiedTickStep,
                    width = yAxisFixedWidth,
                    referenceLines = referenceLines
                )
            }
        }
        if (xLabel.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = xLabel,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChartLegend(
    items: List<LegendItem>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val markerModifier = Modifier.size(10.dp)

                when (item.shape) {
                    LegendShape.Dot -> Box(
                        modifier = markerModifier
                            .clip(CircleShape)
                            .background(item.color)
                    )
                    LegendShape.Box -> Box(
                        modifier = markerModifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(item.color)
                    )
                }

                Spacer(Modifier.width(6.dp))

                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
