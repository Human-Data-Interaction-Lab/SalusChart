package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
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
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.LegendShape
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.VerticalAxisLabel
import com.hdil.saluschart.core.chart.chartDraw.TooltipSpec
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions
import com.hdil.saluschart.core.chart.toRangeChartMarksByXGroup
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import kotlin.math.min

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

/**
 * A single entry in a [RangeBarChart] legend.
 *
 * @param label Text label for this legend entry.
 * @param color Color of the legend indicator.
 * @param shape Shape of the legend indicator; defaults to [LegendShape.Dot].
 */
data class LegendItem(
    val label: String,
    val color: Color,
    val shape: LegendShape = LegendShape.Dot
)

@JvmName("RangeBarChartRangeMarks")
@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<RangeChartMark>,
    xLabel: String = "",
    yLabel: String = "",
    title: String = "Range Bar Chart",
    barColor: Color = Color.Unspecified,
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
    pointColor: Color = Color.Unspecified,
    pointRadius: Dp = 3.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 30.dp,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    tooltipColor: Color = Color.Unspecified,
    legendItems: List<LegendItem> = emptyList(),
    showLegend: Boolean = true,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    ) {
    if (data.isEmpty()) return

    val barColor = barColor.takeIf { it != Color.Unspecified } ?: LocalSalusChartColors.current.primary
    val pointColor = pointColor.takeIf { it != Color.Unspecified } ?: barColor
    val tooltipColor = tooltipColor.takeIf { it != Color.Unspecified } ?: barColor

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
            showYAxisHighlight = showYAxisHighlight,
            tooltipColor = tooltipColor,
            xLabel = xLabel,
            yLabel = yLabel,
            legendItems = legendItems,
            showLegend = showLegend,
            legendPosition = legendPosition,
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
        showYAxisHighlight = showYAxisHighlight,
        tooltipColor = tooltipColor,
        legendItems = legendItems,
        showLegend = showLegend,
        legendPosition = legendPosition,
        )
}

/**
 * Convenience overload of [RangeBarChart] that accepts raw [ChartMark] data.
 *
 * Marks sharing the same `x` value are automatically grouped into a [RangeChartMark] by selecting
 * the minimum and maximum `y` values within each group, then delegates to the primary overload.
 *
 * @param modifier Modifier applied to the chart container.
 * @param data Raw chart marks. Marks with the same `x` are merged into a single range bar.
 * @param xLabel Optional x-axis title.
 * @param yLabel Optional y-axis title.
 * @param title Chart title shown when [showTitle] is true.
 * @param barColor Color used to draw range bars.
 * @param barWidthRatio Ratio of bar width to slot width (0–1 recommended).
 * @param yAxisPosition Side on which the Y-axis is drawn.
 * @param interactionType Controls the tap hit area for bar selection.
 * @param onBarClick Callback invoked when a bar is selected.
 * @param showTitle Whether to display [title].
 * @param showYAxis Whether to draw the Y-axis.
 * @param xLabelAutoSkip Whether to skip overlapping x-axis tick labels.
 * @param maxXTicksLimit Maximum number of x-axis tick labels to display.
 * @param yTickStep Fixed tick step for the y-axis; auto-computed when null.
 * @param unit Unit suffix for tooltips/values.
 * @param pointValues Optional overlay points per bar index.
 * @param pointColor Color of overlay points.
 * @param pointRadius Radius of overlay points.
 * @param barCornerRadiusFraction Uniform corner radius fraction for bars.
 * @param barCornerRadiusFractions Per-corner radius configuration; overrides [barCornerRadiusFraction].
 * @param roundTopOnly If true, only the top corners of bars are rounded.
 * @param windowSize Enables horizontal scrolling when provided and smaller than `data.size`.
 * @param contentPadding Padding around the chart content.
 * @param pageSize Enables paging when provided; mutually exclusive with [windowSize].
 * @param initialPageIndex Initial page for paging mode; defaults to the last page when null.
 * @param yAxisFixedWidth Width reserved for the Y-axis pane.
 * @param referenceLines Optional horizontal reference lines.
 * @param showYAxisHighlight Whether to highlight the Y-axis value corresponding to the selected bar.
 * @param tooltipColor Color of the tooltip indicator.
 * @param legendItems Legend entries shown when [showLegend] is true.
 * @param showLegend Whether to display the legend.
 * @param legendPosition Position of the legend relative to the chart.
 */
@JvmName("RangeBarChartChartMarks")
@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "",
    yLabel: String = "",
    title: String = "Range Bar Chart",
    barColor: Color = Color.Unspecified,
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
    pointColor: Color = Color.Unspecified,
    pointRadius: Dp = 3.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 30.dp,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    tooltipColor: Color = Color.Unspecified,
    legendItems: List<LegendItem> = emptyList(),
    showLegend: Boolean = true,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
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
        showYAxisHighlight = showYAxisHighlight,
        tooltipColor = tooltipColor,
        legendItems = legendItems,
        showLegend = showLegend,
        legendPosition = legendPosition,
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
    showYAxisHighlight: Boolean = false,
    forcedMinY: Double? = null,
    forcedMaxY: Double? = null,
    forcedLabelReservePx: Float = 0f,
    tooltipColor: Color = Color.Black,
    legendItems: List<LegendItem> = emptyList(),
    showLegend: Boolean = true,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    onExternalTooltipSpec: ((TooltipSpec?) -> Unit)? = null,
) {
    if (rangeData.isEmpty()) return

    val chartType = ChartType.RANGE_BAR

    val useScrolling = windowSize != null && windowSize < rangeData.size
    val isFixedYAxis = showYAxis && useScrolling
    val useExternalYAxis = isFixedYAxis || (showYAxisHighlight && showYAxis && referenceLines.isNotEmpty())
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
            val parentHeightDp = maxHeight
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

            val xLabelSpaceDp = if (xLabel.isNotBlank()) with(LocalDensity.current) { (10f + 28f).toDp() } + 8.dp else 0.dp
            val legendAndLabelSpace =
                (if (showLegend && legendItems.isNotEmpty()) 32.dp else 0.dp) +
                        xLabelSpaceDp +
                        8.dp // padding buffer

            val chartAreaHeight = if (constraints.hasBoundedHeight) {
                (maxHeight - legendAndLabelSpace).coerceAtLeast(220.dp)
            } else {
                220.dp
            }

            val density = LocalDensity.current
            val effectiveYAxisWidth = if (useExternalYAxis) {
                val valuesForAdaptive = buildList {
                    addAll(rangeData.map { it.minPoint.y })
                    addAll(rangeData.map { it.maxPoint.y })
                }
                val yAxisRange = remember(valuesForAdaptive, yTickStep, forcedMinY, forcedMaxY) {
                    ChartMath.computeYAxisRange(
                        values = valuesForAdaptive,
                        chartType = chartType,
                        minY = forcedMinY,
                        maxY = forcedMaxY,
                        fixedTickStep = yTickStep
                    )
                }
                val tickPaint = remember { android.graphics.Paint().apply { textSize = 28f } }
                val longestTickPx = remember(yAxisRange) {
                    yAxisRange.yTicks.maxOfOrNull { tick ->
                        tickPaint.measureText(ChartDraw.formatTickLabel(tick.toFloat()))
                    } ?: 0f
                }
                val extraPx = if (showYAxisHighlight) 30f else 20f
                maxOf(yAxisFixedWidth, with(density) { (longestTickPx + extraPx).toDp() })
            } else yAxisFixedWidth

            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val chartLeftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (useExternalYAxis && yAxisPosition == YAxisPosition.LEFT) effectiveYAxisWidth else 0.dp)
            val chartRightOffset = (if (useExternalYAxis && yAxisPosition == YAxisPosition.RIGHT) effectiveYAxisWidth else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)

            Column(Modifier.fillMaxWidth()) {
                // TOP legend (before chart)
                if (showLegend && legendItems.isNotEmpty() && legendPosition == LegendPosition.TOP) {
                    Row(Modifier.fillMaxWidth()) {
                        if (chartLeftOffset > 0.dp) Spacer(Modifier.width(chartLeftOffset))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            ChartLegend(
                                labels = legendItems.map { it.label },
                                colors = legendItems.map { it.color },
                                shapes = legendItems.map { it.shape },
                                position = LegendPosition.TOP
                            )
                        }
                        if (chartRightOffset > 0.dp) Spacer(Modifier.width(chartRightOffset))
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartAreaHeight)
                ) {
                    if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                        VerticalAxisLabel(yLabel)
                    }
                    // Left fixed axis pane
                    if (useExternalYAxis && yAxisPosition == YAxisPosition.LEFT) {
                        Canvas(
                            modifier = Modifier
                                .width(effectiveYAxisWidth)
                                .fillMaxHeight()
                                .clipToBounds()
                        ) {
                            chartMetrics?.let { m ->
                                if (showYAxisHighlight && referenceLines.isNotEmpty()) {
                                    ChartDraw.drawYAxisStandaloneWithReferenceHighlights(
                                        drawScope = this, metrics = m, yAxisPosition = yAxisPosition,
                                        paneWidthPx = size.width, referenceLines = referenceLines
                                    )
                                } else {
                                    ChartDraw.drawYAxisStandalone(
                                        drawScope = this, metrics = m, yAxisPosition = yAxisPosition,
                                        paneWidthPx = size.width
                                    )
                                }
                            }
                        }
                    }

                    // Padding rules around the chart canvas
                    val startPad =
                        if (!showYAxis || (useExternalYAxis && yAxisPosition == YAxisPosition.LEFT)) 0.dp
                        else if (useExternalYAxis && !isFixedYAxis) 8.dp
                        else marginHorizontal
                    val endPad =
                        if (!showYAxis || (useExternalYAxis && yAxisPosition == YAxisPosition.RIGHT)) 0.dp
                        else if (useExternalYAxis && !isFixedYAxis) 8.dp
                        else marginHorizontal

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

                            val labelReservePx = maxOf(
                                forcedLabelReservePx,
                                if (referenceLines.any { it.showLabel || it.label != null }) {
                                    (if (useExternalYAxis) 50.dp else 20.dp).toPx()
                                } else 0f
                            )
                            val metrics = ChartMath.computeMetrics(
                                size = Size(size.width - labelReservePx, size.height),
                                values = valuesForScale,
                                chartType = chartType,
                                minY = forcedMinY,
                                maxY = forcedMaxY,
                                includeYAxisPadding = showYAxis && !useExternalYAxis,
                                yAxisPaddingPx = 80f,
                                paddingBottom = 20f,
                                fixedTickStep = yTickStep
                            )

                            if (chartMetrics != metrics) chartMetrics = metrics

                            ChartDraw.drawGrid(
                                drawScope = this,
                                size = size,
                                metrics = metrics,
                                yAxisPosition = yAxisPosition,
                                drawLabels = showYAxis && !useExternalYAxis
                            )

                            if (showYAxis && !useExternalYAxis) {
                                ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                            }

                            ChartDraw.Bar.drawBarXAxisLabels(
                                ctx = drawContext,
                                labels = labels,
                                metrics = metrics,
                                maxXTicksLimit = maxXTicksLimit,
                                xLabelAutoSkip = xLabelAutoSkip,
                                labelYOffset = 30f
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
                                            onTooltipSpec = { spec ->
                                                if (onExternalTooltipSpec != null) onExternalTooltipSpec(spec)
                                                else tooltipSpec = spec
                                            },
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
                                            onTooltipSpec = { spec ->
                                                if (onExternalTooltipSpec != null) onExternalTooltipSpec(spec)
                                                else tooltipSpec = spec
                                            },
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

                                        val slotCenterX = m.paddingX + slotWidth * (index + 0.5f)

                                        val dotColor =
                                            if (activeIndex == null || index == activeIndex) {
                                                pointColor
                                            } else {
                                                pointColor.copy(alpha = 0.2f)
                                            }

                                        values.forEach { v ->
                                            val denom = (m.maxY - m.minY).takeIf { it != 0.0 } ?: 1.0
                                            val ratio = ((v - m.minY) / denom).toFloat().coerceIn(0f, 1f)
                                            val dotY = m.paddingY + m.chartHeight * (1f - ratio)

                                            drawCircle(
                                                color = dotColor,
                                                radius = radiusPx,
                                                center = Offset(slotCenterX, dotY)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Reference lines overlay
                        if (referenceLines.isNotEmpty()) {
                            chartMetrics?.let { m ->
                                ReferenceLine.ReferenceLines(
                                    modifier = Modifier.matchParentSize().zIndex(10f),
                                    specs = referenceLines,
                                    data = rangeData,
                                    metrics = m,
                                    chartType = chartType,
                                    yAxisPosition = yAxisPosition,
                                )
                            }
                        }

                        // Tooltip overlay (only when not handled externally by paged mode)
                        if (onExternalTooltipSpec == null) tooltipSpec?.let { spec ->
                            val density = LocalDensity.current
                            val parentWidthPx = with(density) { parentWidthDp.toPx() }
                            val parentHeightPx = with(density) { parentHeightDp.toPx() }
                            val estimatedW = with(density) { 160.dp.toPx() }
                            val estimatedH = with(density) { 64.dp.toPx() }
                            val gapPx = with(density) { 8.dp.toPx() }

                            val anchorXPx = spec.offset.x
                            val anchorYPx = spec.offset.y

                            // Center tooltip on bar; clamp so it stays within chart bounds
                            val xPx = (anchorXPx - estimatedW / 2f)
                                .coerceIn(0f, (parentWidthPx - estimatedW).coerceAtLeast(0f))

                            // Above bar top; fall back to below if too close to top, then clamp
                            val preferredY = if (anchorYPx - estimatedH - gapPx >= 0f)
                                anchorYPx - estimatedH - gapPx
                            else
                                anchorYPx + gapPx
                            val yPx = preferredY.coerceIn(0f, (parentHeightPx - estimatedH).coerceAtLeast(0f))

                            Box(modifier = Modifier.matchParentSize().zIndex(999f)) {
                                ChartTooltip(
                                    chartMark = spec.chartMark,
                                    unit = unit,
                                    color = tooltipColor,
                                    modifier = Modifier.offset(
                                        x = with(density) { xPx.toDp() },
                                        y = with(density) { yPx.toDp() }
                                    )
                                )
                            }
                        }
                    }
                }
                    // Right fixed axis pane
                    if (useExternalYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                        Canvas(
                            modifier = Modifier
                                .width(effectiveYAxisWidth)
                                .fillMaxHeight()
                                .clipToBounds()
                        ) {
                            chartMetrics?.let { m ->
                                if (showYAxisHighlight && referenceLines.isNotEmpty()) {
                                    ChartDraw.drawYAxisStandaloneWithReferenceHighlights(
                                        drawScope = this, metrics = m, yAxisPosition = yAxisPosition,
                                        paneWidthPx = size.width, referenceLines = referenceLines
                                    )
                                } else {
                                    ChartDraw.drawYAxisStandalone(
                                        drawScope = this, metrics = m, yAxisPosition = yAxisPosition,
                                        paneWidthPx = size.width
                                    )
                                }
                            }
                        }
                    }
                    if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                        VerticalAxisLabel(yLabel)
                    }
                }
                if (xLabel.isNotBlank()) {
                    val xLabelSpacerHeight = with(LocalDensity.current) { (10 + 28f).toDp() }
                    Spacer(Modifier.height(xLabelSpacerHeight))
                    Row(Modifier.fillMaxWidth()) {
                        if (chartLeftOffset > 0.dp) Spacer(Modifier.width(chartLeftOffset))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(text = xLabel, style = MaterialTheme.typography.bodySmall)
                        }
                        if (chartRightOffset > 0.dp) Spacer(Modifier.width(chartRightOffset))
                    }
                }
                if (showLegend && legendItems.isNotEmpty() && legendPosition != LegendPosition.TOP) {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        if (chartLeftOffset > 0.dp) Spacer(Modifier.width(chartLeftOffset))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            ChartLegend(
                                labels = legendItems.map { it.label },
                                colors = legendItems.map { it.color },
                                shapes = legendItems.map { it.shape },
                                position = LegendPosition.BOTTOM
                            )
                        }
                        if (chartRightOffset > 0.dp) Spacer(Modifier.width(chartRightOffset))
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
    width: Dp,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    bottomPadding: Dp = 0.dp,
) {
    Canvas(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(bottom = bottomPadding)
            .clipToBounds()
    ) {
        val m = ChartMath.computeMetrics(
            size = size,
            values = listOf(minY, maxY),
            chartType = ChartType.RANGE_BAR,
            minY = minY,
            maxY = maxY,
            includeYAxisPadding = false,
            paddingBottom = 20f,
            fixedTickStep = step
        )

        if (showYAxisHighlight && referenceLines.isNotEmpty()) {
            ChartDraw.drawYAxisStandaloneWithReferenceHighlights(
                drawScope = this,
                metrics = m,
                yAxisPosition = yAxisPosition,
                paneWidthPx = size.width,
                referenceLines = referenceLines
            )
        } else {
            ChartDraw.drawYAxisStandalone(
                drawScope = this,
                metrics = m,
                yAxisPosition = yAxisPosition,
                paneWidthPx = size.width
            )
        }
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
    yLabel: String = "",
    maxXTicksLimit: Int? = null,
    xLabelAutoSkip: Boolean,
    unit: String,
    pointValues: List<List<Double>>? = null,
    pointColor: Color = Color.Unspecified,
    pointRadius: Dp = 3.dp,
    pageSize: Int,
    yTickStep: Double?,
    initialPageIndex: Int?,
    outerPadding: PaddingValues,
    yAxisFixedWidth: Dp = 30.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    tooltipColor: Color = Color.Black,
    legendItems: List<LegendItem> = emptyList(),
    showLegend: Boolean = true,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
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

    val density = LocalDensity.current

    Column(modifier = modifier.padding(outerPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
        }

        Row(Modifier.fillMaxWidth().weight(1f)) {

            if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                VerticalAxisLabel(yLabel)
            }

            // LEFT fixed external Y-axis (paged mode)
            if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                FixedPagerYAxisRange(
                    minY = unifiedMinY,
                    maxY = unifiedMaxY,
                    yAxisPosition = YAxisPosition.LEFT,
                    step = unifiedTickStep,
                    width = yAxisFixedWidth,
                    referenceLines = referenceLines,
                    showYAxisHighlight = showYAxisHighlight,
                    bottomPadding = 8.dp
                )
            }

            var pagerBoxSizePx by remember { mutableStateOf(IntSize.Zero) }
            val pageLabelReservePx = if (referenceLines.any { it.showLabel || it.label != null }) with(density) { 50.dp.toPx() } else 0f

            // Tooltip state hoisted outside the pager so it can render above the reference line overlay
            var pagedTooltipSpec by remember { mutableStateOf<TooltipSpec?>(null) }
            LaunchedEffect(pagerState.currentPage) { pagedTooltipSpec = null }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .onSizeChanged { pagerBoxSizePx = it }
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
                        forcedLabelReservePx = pageLabelReservePx,
                        tooltipColor = tooltipColor,
                        onExternalTooltipSpec = { pagedTooltipSpec = it },
                    )
                }

                // Fixed reference line overlay — rendered outside the pager so it doesn't move with pages
                if (referenceLines.isNotEmpty() && pagerBoxSizePx.width > 0) {
                    val legendSpacePx = with(density) { 8.dp.toPx() }
                    val canvasHeightPx = (pagerBoxSizePx.height.toFloat() - legendSpacePx).coerceAtLeast(0f)
                    val overlayMetrics = remember(pagerBoxSizePx, unifiedMinY, unifiedMaxY, unifiedTickStep, pageLabelReservePx) {
                        ChartMath.computeMetrics(
                            size = Size(pagerBoxSizePx.width.toFloat() - pageLabelReservePx, canvasHeightPx),
                            values = listOf(unifiedMinY, unifiedMaxY),
                            chartType = ChartType.RANGE_BAR,
                            minY = unifiedMinY,
                            maxY = unifiedMaxY,
                            includeYAxisPadding = false,
                            paddingBottom = 20f,
                            fixedTickStep = unifiedTickStep
                        )
                    }
                    ReferenceLine.ReferenceLines(
                        modifier = Modifier.matchParentSize().zIndex(10f),
                        specs = referenceLines,
                        data = data,
                        metrics = overlayMetrics,
                        chartType = ChartType.RANGE_BAR,
                        yAxisPosition = yAxisPosition,
                    )
                }

                // Paged tooltip overlay — rendered above the reference line overlay (zIndex > 10)
                pagedTooltipSpec?.let { spec ->
                    val parentWidthPx = pagerBoxSizePx.width.toFloat()
                    val parentHeightPx = pagerBoxSizePx.height.toFloat()
                    val estimatedW = with(density) { 160.dp.toPx() }
                    val estimatedH = with(density) { 64.dp.toPx() }
                    val gapPx = with(density) { 8.dp.toPx() }

                    val anchorXPx = spec.offset.x
                    val anchorYPx = spec.offset.y

                    val xPx = (anchorXPx - estimatedW / 2f)
                        .coerceIn(0f, (parentWidthPx - estimatedW).coerceAtLeast(0f))
                    val preferredY = if (anchorYPx - estimatedH - gapPx >= 0f)
                        anchorYPx - estimatedH - gapPx
                    else
                        anchorYPx + gapPx
                    val yPx = preferredY.coerceIn(0f, (parentHeightPx - estimatedH).coerceAtLeast(0f))

                    Box(modifier = Modifier.matchParentSize().zIndex(999f)) {
                        ChartTooltip(
                            chartMark = spec.chartMark,
                            unit = unit,
                            color = tooltipColor,
                            modifier = Modifier.offset(
                                x = with(density) { xPx.toDp() },
                                y = with(density) { yPx.toDp() }
                            )
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
                    referenceLines = referenceLines,
                    showYAxisHighlight = showYAxisHighlight,
                    bottomPadding = 8.dp
                )
            }

            if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                VerticalAxisLabel(yLabel)
            }
        }
        if (xLabel.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val leftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (showYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val rightOffset = (if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)
            Row(Modifier.fillMaxWidth()) {
                if (leftOffset > 0.dp) Spacer(Modifier.width(leftOffset))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = xLabel, style = MaterialTheme.typography.bodySmall)
                }
                if (rightOffset > 0.dp) Spacer(Modifier.width(rightOffset))
            }
        }
        if (showLegend && legendItems.isNotEmpty()) {
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val leftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (showYAxis && yAxisPosition == YAxisPosition.LEFT) yAxisFixedWidth else 0.dp)
            val rightOffset = (if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) yAxisFixedWidth else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                if (leftOffset > 0.dp) Spacer(Modifier.width(leftOffset))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ChartLegend(
                        labels = legendItems.map { it.label },
                        colors = legendItems.map { it.color },
                        shapes = legendItems.map { it.shape },
                        position = LegendPosition.BOTTOM
                    )
                }
                if (rightOffset > 0.dp) Spacer(Modifier.width(rightOffset))
            }
        }
    }
}

