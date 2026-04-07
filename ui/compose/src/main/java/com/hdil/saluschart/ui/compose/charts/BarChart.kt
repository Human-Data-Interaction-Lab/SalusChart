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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.VerticalAxisLabel
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.TooltipSpec
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions
import com.hdil.saluschart.ui.theme.ChartColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Bar Chart Example",
    barColor: Color = ChartColor.Default,
    minY: Double? = null, // Minimum Y value for chart
    maxY: Double? = null, // Maximum Y value for chart
    barWidthRatio: Float = 0.8f, // Ratio of bar width to space width (바 너비 / 한 칸 너비)
    xLabelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Bar = InteractionType.Bar.BAR,
    onBarClick: ((Int, Double) -> Unit)? = null,
    showTitle: Boolean = false,
    showYAxis: Boolean = true,
    showLabel: Boolean = false,
    xLabelAutoSkip: Boolean = true, // Automatically skip labels if they overlap
    maxXTicksLimit: Int? = null, // Maximum number of x-axis labels to display
    yTickStep: Double? = null, // y-axis grid tick step (automatically calculated if null)
    unit: String = "",
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    // Free-scroll mode
    windowSize: Int? = null,                    // visible items in scroll window
    contentPadding: PaddingValues = PaddingValues(16.dp),    // Free-scroll paddings
    pageSize: Int? = null,                      // items per page
    unifyYAxisAcrossPages: Boolean = true,
    initialPageIndex: Int? = null,          // initial page to show (last page if null)
    yAxisFixedWidth: Dp = 20.dp,            // Padding between the chart and the y-axis
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = false,
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    legendLabel: String = "",
    tooltipColor: Color = barColor,
    ) {
    if (data.isEmpty()) return

    // Validate that scrolling and paging modes are not both enabled
    require(!(windowSize != null && pageSize != null)) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    // Compute effective page size (0 = off)
    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)

    // Enable paging if pageSize is provided and data exceeds page size
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
            tooltipColor = tooltipColor,
            interactionType = interactionType,
            yAxisPosition = yAxisPosition,
            showLabel = showLabel,
            showYAxis = showYAxis,
            onBarClick = onBarClick,
            // scale/paging
            showTitle = showTitle,
            outerPadding = contentPadding,
            unifyYAxisAcrossPages = unifyYAxisAcrossPages,
            yTickStep = yTickStep,
            initialPageIndex = initialPageIndex,
            minY = minY,
            maxY = maxY,
            unit = unit,
            yAxisFixedWidth = yAxisFixedWidth,
            maxXTicksLimit = maxXTicksLimit,
            xLabelAutoSkip = xLabelAutoSkip,
            barCornerRadiusFraction = barCornerRadiusFraction,
            barCornerRadiusFractions = barCornerRadiusFractions,
            roundTopOnly = roundTopOnly,
            showLegend = showLegend,
            legendPosition = legendPosition,
            legendLabel = legendLabel,
            referenceLines = referenceLines,
            showYAxisHighlight = showYAxisHighlight,
        )
        return
    }

    val chartType = ChartType.BAR

    val useScrolling  = windowSize != null && windowSize < data.size
    val isFixedYAxis = showYAxis && useScrolling
    val useExternalYAxis = isFixedYAxis || (showYAxisHighlight && showYAxis && referenceLines.isNotEmpty())
    val scrollState = rememberScrollState()

    // Adaptive y-axis pane width: measure the longest tick label and add padding for pills.
    // padX=12f, labelGap=14f → minimum pane width for a full pill = advance + 30px.
    val density = LocalDensity.current
    val effectiveYAxisWidth = if (useExternalYAxis) {
        val yAxisRange = remember(data, minY, maxY, yTickStep) {
            ChartMath.computeYAxisRange(
                values = data.map { it.y },
                chartType = ChartType.BAR,
                minY = minY,
                maxY = maxY,
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

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        BoxWithConstraints(Modifier.weight(1f)) {
            val availableWidth = maxWidth
            val marginHorizontal = 16.dp
            val yAxisPaddingPxValue = with(LocalDensity.current) { effectiveYAxisWidth.toPx() }

            // Calculate canvas width for scrolling mode
            val canvasWidth = if (useScrolling) {
                val chartWidth = availableWidth - (marginHorizontal * 2)
                val sectionsCount = (data.size.toFloat() / windowSize!!.toFloat()).toInt()
                chartWidth * sectionsCount
            } else null

            val xLabels = data.map { it.label ?: it.x.toString() }
            val yValues = data.map { it.y }

            var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
            var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
            var tooltipSpec by remember { mutableStateOf<TooltipSpec?>(null) }
            val parentWidthDp = maxWidth
            val parentHeightDp = maxHeight

            Row(Modifier.fillMaxSize()) {
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
                                    drawScope = this, metrics = m, yAxisPosition = yAxisPosition, paneWidthPx = size.width
                                )
                            }
                        }
                    }
                }

                // Calculate padding when Y-axis is hidden (external axis handles it), fixed axis, or VerticalAxisLabel already provides margin
                val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
                val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
                val startPad = when {
                    !showYAxis || hasLeftLabel -> 0.dp
                    useExternalYAxis && yAxisPosition == YAxisPosition.LEFT -> if (!isFixedYAxis) 8.dp else 0.dp
                    else -> marginHorizontal
                }
                val endPad = when {
                    !showYAxis || hasRightLabel -> 0.dp
                    useExternalYAxis && yAxisPosition == YAxisPosition.RIGHT -> if (!isFixedYAxis) 8.dp else 0.dp
                    else -> marginHorizontal
                }

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
                        val labelReservePx = if (referenceLines.any { it.showLabel || it.label != null }) {
                            (if (isFixedYAxis) 80.dp else 30.dp).toPx()
                        } else 0f
                        val metrics = ChartMath.computeMetrics(
                            size = Size(size.width - labelReservePx, size.height),
                            values = yValues,
                            chartType = chartType,
                            minY = minY,
                            maxY = maxY,
                            includeYAxisPadding = !useExternalYAxis,
                            yAxisPaddingPx = yAxisPaddingPxValue,
                            fixedTickStep = yTickStep,
                            paddingBottom = if (useExternalYAxis) 10f else 0f
                        )
                        chartMetrics = metrics

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
                            labels = xLabels,
                            metrics = metrics,
                            textSize = xLabelTextSize,
                            maxXTicksLimit = maxXTicksLimit,
                            xLabelAutoSkip = xLabelAutoSkip
                        )
                    }

                    // Bars & interaction
                    when (interactionType) {
                        InteractionType.Bar.TOUCH_AREA -> {
                            // Draw visual bars
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
                                    onTooltipSpec = { tooltipSpec = it },
                                    showLabel = showLabel,
                                    unit = unit,
                                    barCornerRadiusFraction = barCornerRadiusFraction,
                                    barCornerRadiusFractions = barCornerRadiusFractions,
                                    roundTopOnly = roundTopOnly,
                                )
                            }
                            // Draw interactive touch area
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
                            // Draw interactive bars
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
                                        selectedBarIndex = if (selectedBarIndex == index) null else index
                                        onBarClick?.invoke(index, data.getOrNull(index)?.y ?: 0.0)
                                    },
                                    showTooltipForIndex = selectedBarIndex,
                                    onTooltipSpec = { tooltipSpec = it },
                                    chartType = chartType,
                                    showLabel = showLabel,
                                    unit = unit,
                                    barCornerRadiusFraction = barCornerRadiusFraction,
                                    barCornerRadiusFractions = barCornerRadiusFractions,
                                    roundTopOnly = roundTopOnly,
                                )
                            }
                        }
                    }

                    // Draw reference lines
                    if (referenceLines.isNotEmpty()) {
                        chartMetrics?.let { metrics ->
                            ReferenceLine.ReferenceLines(
                                modifier = Modifier.fillMaxSize(),
                                specs = referenceLines,
                                data = data,
                                metrics = metrics,
                                chartType = chartType,
                                yAxisPosition = yAxisPosition,
                            )
                        }
                    }
                    tooltipSpec?.let { spec ->
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
                                    drawScope = this, metrics = m, yAxisPosition = yAxisPosition, paneWidthPx = size.width
                                )
                            }
                        }
                    }
                }
                if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                    VerticalAxisLabel(yLabel)
                }
            }
        }

        if (xLabel.isNotBlank()) {
            val xLabelSpacerHeight = with(LocalDensity.current) { (50f + xLabelTextSize).toDp() }
            Spacer(Modifier.height(xLabelSpacerHeight))
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val leftOffset = (if (hasLeftLabel) 40.dp else 0.dp) + (if (useExternalYAxis && yAxisPosition == YAxisPosition.LEFT) effectiveYAxisWidth else 0.dp)
            val rightOffset = (if (useExternalYAxis && yAxisPosition == YAxisPosition.RIGHT) effectiveYAxisWidth else 0.dp) + (if (hasRightLabel) 40.dp else 0.dp)
            Row(Modifier.fillMaxWidth()) {
                if (leftOffset > 0.dp) Spacer(Modifier.width(leftOffset))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = xLabel, style = MaterialTheme.typography.bodySmall)
                }
                if (rightOffset > 0.dp) Spacer(Modifier.width(rightOffset))
            }
        }
        if (showLegend && legendLabel.isNotBlank()) {
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val legendLeftOffset = (if (hasLeftLabel) 40.dp else 0.dp) + (if (useExternalYAxis && yAxisPosition == YAxisPosition.LEFT) effectiveYAxisWidth else 0.dp)
            val legendRightOffset = (if (useExternalYAxis && yAxisPosition == YAxisPosition.RIGHT) effectiveYAxisWidth else 0.dp) + (if (hasRightLabel) 40.dp else 0.dp)
            when (legendPosition) {
                LegendPosition.TOP, LegendPosition.BOTTOM -> {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        if (legendLeftOffset > 0.dp) Spacer(Modifier.width(legendLeftOffset))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            ChartLegend(
                                labels = listOf(legendLabel),
                                colors = listOf(barColor),
                                position = legendPosition
                            )
                        }
                        if (legendRightOffset > 0.dp) Spacer(Modifier.width(legendRightOffset))
                    }
                }
                LegendPosition.LEFT -> {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        ChartLegend(labels = listOf(legendLabel), colors = listOf(barColor), position = LegendPosition.LEFT)
                    }
                }
                LegendPosition.RIGHT -> {
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        ChartLegend(labels = listOf(legendLabel), colors = listOf(barColor), position = LegendPosition.RIGHT)
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
    data: List<ChartMark>,
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
    showLabel: Boolean,
    showYAxis: Boolean,
    onBarClick: ((Int, Double) -> Unit)?,
    // scale/paging
    showTitle: Boolean,
    unifyYAxisAcrossPages: Boolean,
    yTickStep: Double?,
    initialPageIndex: Int?,
    minY: Double?,
    maxY: Double?,
    unit: String,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    yAxisFixedWidth: Dp = 20.dp,
    maxXTicksLimit: Int? = null,
    xLabelAutoSkip: Boolean,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = true,
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    legendLabel: String = "",
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    tooltipColor: Color = Color.Black,
) {
    val pageCount = remember(data.size, pageSize) {
        kotlin.math.ceil(data.size / pageSize.toFloat()).toInt()
    }
    val firstPage = initialPageIndex ?: (pageCount - 1).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = firstPage, pageCount = { pageCount })

    // Compute unified Y-axis range using the lighter function (no pixel calculations)
    val yAxisRange = remember(data, minY, maxY, yTickStep) {
        val yValues = data.map { it.y }
        ChartMath.computeYAxisRange(
            values = yValues,
            chartType = ChartType.BAR,
            minY = minY,
            maxY = maxY,
            fixedTickStep = yTickStep
        )
    }

    // Compute max rounded value and effective tick step
    val maxRounded = yAxisRange.maxY
    val effectiveTickStep = yAxisRange.tickStep

    // Compute adaptive y-axis pane width so tick labels are never clipped
    val density = LocalDensity.current
    val tickLabelPaint = remember { android.graphics.Paint().apply { textSize = 28f } }
    val longestTickLabelPx = remember(yAxisRange) {
        yAxisRange.yTicks.maxOfOrNull { tick ->
            val label = when {
                tick == 0.0 -> "0"
                tick >= 1_000_000.0 -> "%.1fM".format(tick / 1_000_000.0)
                tick >= 1_000.0 -> "%.1fK".format(tick / 1_000.0)
                tick % 1.0 == 0.0 -> "%.0f".format(tick)
                else -> "%.1f".format(tick)
            }
            tickLabelPaint.measureText(label)
        } ?: 0f
    }
    // drawYAxisStandalone: label right-aligned at paneWidth - 10.5f; need paneWidth >= labelWidth + 10.5f + margin
    val adaptiveYAxisWidthDp = maxOf(yAxisFixedWidth, with(density) { (longestTickLabelPx + 20f).toDp() })

    // Space needed so x-axis tick labels (drawn at canvas.height + 50f) stay within the pager page.
    // Both FixedPagerYAxis and the inner BarChart canvas must shrink by the same amount so
    // their chartHeight values match (keeping y-tick positions aligned).
    val xAxisOverflowDp = with(density) { (50f + xLabelTextSize).toDp() }
    val xLabelOverhangDp = with(density) { (xLabelTextSize / 2f).toDp() }

    Column(modifier = modifier.padding(outerPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
        }

        Row(Modifier.fillMaxWidth().weight(1f)) {
            if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                VerticalAxisLabel(yLabel, modifier = Modifier.padding(bottom = xAxisOverflowDp))
            }
            // Left fixed external Y-axis
            if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                FixedPagerYAxis(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = effectiveTickStep.toFloat(),
                    width = adaptiveYAxisWidthDp,
                    bottomPadding = xAxisOverflowDp,
                    referenceLines = if (showYAxisHighlight) referenceLines else emptyList()
                )
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val start = page * pageSize
                val end = kotlin.math.min(start + pageSize, data.size)
                val slice = data.subList(start, end)

                // Render a normal BarChart page:
                // - no inner scroll (windowSize = null)
                // - maxY unified with the external axis
                BarChart(
                    modifier = Modifier.fillMaxSize(),
                    data = slice,
                    xLabel = "",
                    yLabel = "",
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
                    maxXTicksLimit = maxXTicksLimit,
                    xLabelAutoSkip = xLabelAutoSkip,
                    showYAxis = false,                 // external axis handles it
                    yTickStep = effectiveTickStep,
                    showTitle = false,
                    yAxisFixedWidth = 0.dp,            // no internal padding; external axis aligns with bars
                    contentPadding = PaddingValues(bottom = xAxisOverflowDp - 4.dp, end = xLabelOverhangDp),
                    unit = unit,
                    barCornerRadiusFraction = barCornerRadiusFraction,
                    barCornerRadiusFractions = barCornerRadiusFractions,
                    roundTopOnly = roundTopOnly,
                    tooltipColor = tooltipColor,
                )
            }

            // Right fixed external Y-axis
            if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                FixedPagerYAxis(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = effectiveTickStep.toFloat(),
                    width = adaptiveYAxisWidthDp,
                    bottomPadding = xAxisOverflowDp,
                    referenceLines = if (showYAxisHighlight) referenceLines else emptyList()
                )
            }
            if (yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                VerticalAxisLabel(yLabel, modifier = Modifier.padding(bottom = xAxisOverflowDp))
            }
        }
        if (xLabel.isNotBlank()) {
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val leftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (showYAxis && yAxisPosition == YAxisPosition.LEFT) adaptiveYAxisWidthDp else 0.dp)
            val rightOffset = (if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) adaptiveYAxisWidthDp else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)
            Row(Modifier.fillMaxWidth()) {
                if (leftOffset > 0.dp) Spacer(Modifier.width(leftOffset))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = xLabel, style = MaterialTheme.typography.bodySmall)
                }
                if (rightOffset > 0.dp) Spacer(Modifier.width(rightOffset))
            }
        }
        if (showLegend && legendLabel.isNotBlank()) {
            val hasLeftLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.LEFT
            val hasRightLabel = yLabel.isNotBlank() && showYAxis && yAxisPosition == YAxisPosition.RIGHT
            val legendLeftOffset = (if (hasLeftLabel) 20.dp else 0.dp) + (if (showYAxis && yAxisPosition == YAxisPosition.LEFT) adaptiveYAxisWidthDp else 0.dp)
            val legendRightOffset = (if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) adaptiveYAxisWidthDp else 0.dp) + (if (hasRightLabel) 20.dp else 0.dp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                if (legendLeftOffset > 0.dp) Spacer(Modifier.width(legendLeftOffset))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ChartLegend(
                        labels = listOf(legendLabel),
                        colors = listOf(barColor),
                        position = LegendPosition.BOTTOM
                    )
                }
                if (legendRightOffset > 0.dp) Spacer(Modifier.width(legendRightOffset))
            }
        }
    }
}

// Function for fixed external Y-axis in paged bar chart
@Composable
private fun FixedPagerYAxis(
    maxY: Double,
    yAxisPosition: YAxisPosition,
    step: Float,
    width: Dp,
    bottomPadding: Dp = 0.dp,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
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
            values = listOf(0.0, maxY),
            chartType = ChartType.BAR,
            minY = 0.0,
            maxY = maxY,
            includeYAxisPadding = false,
            fixedTickStep = step.toDouble(),
            paddingBottom = 10f
        )
        // Call standalone y-axis drawing function
        if (referenceLines.isNotEmpty()) {
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
