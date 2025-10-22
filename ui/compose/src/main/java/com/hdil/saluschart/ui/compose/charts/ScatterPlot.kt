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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.PointType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath

@Composable
fun ScatterPlot(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "X Axis",
    yLabel: String = "Y Axis",
    title: String = "Scatter Plot Example",
    pointColor: Color = com.hdil.saluschart.ui.theme.ChartColor.Default,
    pointType: PointType = PointType.Circle,
    pointSize: Dp = 8.dp,
    minY: Double? = null,
    maxY: Double? = null,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Scatter = InteractionType.Scatter.POINT,
    // reference line
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Red,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null,
    // Display
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    yTickStep: Double? = null,
    unit: String = "",
    // Scroll/Page
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 0.dp,
) {
    if (data.isEmpty()) return

    // Validate that scrolling and paging modes are not both enabled
    require(!(windowSize != null && pageSize != null)) {
        "Cannot enable both scrolling mode (windowSize) and paging mode (pageSize) simultaneously"
    }

    val chartType = ChartType.SCATTERPLOT

    // compute effective page size (0 = off)
    val requestedPageSize = (pageSize ?: 0).coerceAtLeast(0)

    // enable paging if pageSize is provided and data exceeds page size
    val enablePaging = requestedPageSize > 0 && data.size > requestedPageSize

    if (enablePaging) {
        ScatterPlotPagedInternal(
            modifier = modifier,
            data = data,
            pageSize = requestedPageSize,
            // visuals
            title = title,
            xLabel = xLabel,
            yLabel = yLabel,
            pointColor = pointColor,
            pointType = pointType,
            pointSize = pointSize,
            tooltipTextSize = tooltipTextSize,
            interactionType = interactionType,
            yAxisPosition = yAxisPosition,
            showYAxis = showYAxis,
            // scale/paging
            showTitle = showTitle,
            outerPadding = contentPadding,
            yTickStep = yTickStep,
            initialPageIndex = initialPageIndex,
            minY = minY,
            maxY = maxY,
            unit = unit,
            yAxisFixedWidth = yAxisFixedWidth,
            maxXTicksLimit = maxXTicksLimit,
            xLabelAutoSkip = xLabelAutoSkip
        )
        return
    }

    val useScrolling = windowSize != null && windowSize < data.size
    val isFixedYAxis = showYAxis && useScrolling
    val scrollState = rememberScrollState()

    // Use the same approach as BarChart and LineChart for x-axis labels
    // Remove duplicate labels while preserving order for scatter plots with multiple points per x-value
    val xLabels = data.map { it.label ?: it.x.toString() }.distinct()
    val yValues = data.map { it.y }

    var canvasPoints by remember { mutableStateOf(listOf<Offset>()) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
    
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var selectedIndices by remember { mutableStateOf<Set<Int>?>(null) }

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        BoxWithConstraints {
            val availableWidth = maxWidth
            val marginHorizontal = 16.dp

            // 스크롤 모드에서 실제 표시할 데이터와 캔버스 너비 계산
            val canvasWidth = if (useScrolling) {
                val chartWidth = availableWidth - (marginHorizontal * 2)
                val ws = requireNotNull(windowSize)
                val sectionsCount = (data.size.toFloat() / ws.toFloat()).toInt()
                chartWidth * sectionsCount
            } else null

            Row(Modifier.fillMaxSize()) {
                // LEFT fixed axis pane
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

                // Chart area
                // Use 0.dp padding when Y-axis is hidden (external axis handles it) or when it's a fixed axis on that side
                val startPad = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.LEFT)) 0.dp else marginHorizontal
                val endPad   = if (!showYAxis || (isFixedYAxis && yAxisPosition == YAxisPosition.RIGHT)) 0.dp else marginHorizontal

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
                        val points = ChartMath.Scatter.mapScatterToCanvasPoints(data, size, metrics)

                        canvasPoints = points
                        canvasSize = size
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
//                        ChartDraw.Line.drawLineXAxisLabels(
                        // X축 라벨은 Bar 스타일로 중앙 정렬(막대 중심) 위치에 맞춰 그림
                        ChartDraw.Bar.drawBarXAxisLabels(
                            ctx = drawContext,
                            labels = xLabels,
                            metrics = metrics,
                            maxXTicksLimit = maxXTicksLimit,
                            xLabelAutoSkip = xLabelAutoSkip
                        )
                    }


//                    ChartDraw.Scatter.PointMarker(
//                        data = data,
//                        points = canvasPoints,
//                        values = yValues,
//                        selectedPointIndex = selectedPointIndex,
//                        onPointClick = null, // No point interaction in this mode
//                        pointType = pointType,
//                        interactive = false, // Visual only, no interactions
//                        chartType = chartType,
//                        showTooltipForIndex = selectedPointIndex,
//                        canvasSize = canvasSize,
//                        unit = unit,
//                    )

                    // Conditional interaction based on interactionType parameter
                    when (interactionType) {
                        InteractionType.Scatter.POINT -> {
                            // 포인트 직접 터치: 단일 선택 사용, 집합 선택 초기화
                            selectedIndices = null
                            ChartDraw.Scatter.PointMarker(
                                data = data,
                                points = canvasPoints,
                                values = yValues,
                                color = pointColor,
                                selectedPointIndex = selectedPointIndex,
                                onPointClick = { index ->
                                    selectedPointIndex = if (selectedPointIndex == index) null else index
                                },
                                pointType = pointType,
                                chartType = chartType,
                                showTooltipForIndex = selectedPointIndex,
                                pointRadius = pointSize,
                                innerRadius = 0.dp,
                                interactive = true,
                                canvasSize = canvasSize,
                                unit = unit,
                            )
                        }

                        InteractionType.Scatter.TOUCH_AREA -> {
                            // 넓은 터치 영역: 카테고리(x 고유값)별로 전체 높이 스트립을 깔아 터치 감도를 높임
                            val metrics = chartMetrics
                            if (metrics != null && canvasPoints.isNotEmpty()) {
                                // 최적화: 카테고리 관련 계산을 remember로 캐싱
                                // 고유 x값(카테고리)와 인덱스 매핑
                                val categoryData = remember(data) {
                                    val uniqueXs = data.map { it.x }.distinct().sorted()
                                    val catCount = uniqueXs.size
                                    val catIndexToDataIndex = uniqueXs.map { ux ->
                                        data.indexOfFirst { it.x == ux }
                                    }
                                    val catMarks = uniqueXs.mapIndexed { idx, ux ->
                                        val firstIdx = catIndexToDataIndex[idx].coerceAtLeast(0)
                                        val src = data.getOrNull(firstIdx)
                                        ChartMark(
                                            x = ux,
                                            y = src?.y ?: 0.0,
                                            label = src?.label ?: ux.toString()
                                        )
                                    }
                                    Triple(uniqueXs, catCount, catMarks)
                                }
                                val (uniqueXs, catCount, catMarks) = categoryData

                                // 최적화: X값별 인덱스 맵을 사전 계산하여 클릭 시 빠른 조회
                                val xToIndicesMap = remember(data) {
                                    data.indices.groupBy { data[it].x }
                                        .mapValues { it.value.toSet() }
                                }

                                ChartDraw.Bar.BarMarker(
                                    data = catMarks,
                                    minValues = List(catCount) { metrics.minY },
                                    maxValues = List(catCount) { metrics.maxY },
                                    metrics = metrics,
                                    color = Color.Transparent,
                                    barWidthRatio = 1.0f,
                                    interactive = true,
                                    onBarClick = { idx, _ ->
                                        // 최적화: 사전 계산된 맵에서 O(1) 조회
                                        // 동일 카테고리의 모든 포인트(1:N) 집합
                                        val ux = uniqueXs.getOrNull(idx)
                                        val indicesInCat: Set<Int> = ux?.let { xToIndicesMap[it] } ?: emptySet()
                                        selectedPointIndex = null
                                        selectedIndices = if (indicesInCat.isNotEmpty() && selectedIndices == indicesInCat) null else indicesInCat
                                    },
                                    chartType = chartType,
                                    isTouchArea = true,
                                    showLabel = false,
                                    unit = unit
                                )

                                // 포인트/툴팁 렌더링 (비인터랙티브 포인트 + 외부 선택 인덱스로 툴팁 표시)
                                ChartDraw.Scatter.PointMarker(
                                    data = data,
                                    points = canvasPoints,
                                    values = yValues,
                                    color = pointColor,
                                    selectedPointIndex = null,
                                    selectedIndices = selectedIndices,
                                    onPointClick = null,
                                    pointType = pointType,
                                    chartType = chartType,
                                    showTooltipForIndex = null,
                                    showTooltipForIndices = selectedIndices,
                                    pointRadius = pointSize,
                                    innerRadius = 0.dp,
                                    interactive = false,
                                    canvasSize = canvasSize,
                                    unit = unit,
                                )
                            } else {
                                // 안전장치: 메트릭스가 아직 없으면 기본 비인터랙티브 렌더링
                                ChartDraw.Scatter.PointMarker(
                                    data = data,
                                    points = canvasPoints,
                                    values = yValues,
                                    color = pointColor,
                                    selectedPointIndex = null,
                                    selectedIndices = null,
                                    onPointClick = null,
                                    pointType = pointType,
                                    chartType = chartType,
                                    showTooltipForIndex = null,
                                    showTooltipForIndices = null,
                                    pointRadius = pointSize,
                                    innerRadius = 0.dp,
                                    interactive = false,
                                    canvasSize = canvasSize,
                                    unit = unit,
                                )
                            }
                        }
                    }

                    // 기준선 표시
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

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScatterPlotPagedInternal(
    modifier: Modifier,
    data: List<ChartMark>,
    pageSize: Int,
    // visuals
    title: String,
    xLabel: String,
    yLabel: String,
    pointColor: Color,
    pointType: PointType,
    pointSize: Dp,
    tooltipTextSize: Float,
    interactionType: InteractionType.Scatter,
    yAxisPosition: YAxisPosition,
    showYAxis: Boolean,
    // scale/paging
    showTitle: Boolean,
    yTickStep: Double?,
    initialPageIndex: Int?,
    minY: Double?,
    maxY: Double?,
    unit: String,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    yAxisFixedWidth: Dp = 0.dp,
    maxXTicksLimit: Int? = null,
    xLabelAutoSkip: Boolean
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
            chartType = ChartType.SCATTERPLOT,
            minY = minY,
            maxY = maxY,
            fixedTickStep = yTickStep
        )
    }

    val maxRounded = yAxisRange.maxY
    val effectiveTickStep = yAxisRange.tickStep

    Column(modifier = modifier.padding(outerPadding)) {
        if (showTitle) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
        }

        Row(Modifier.fillMaxWidth()) {
            // LEFT fixed external Y-axis
            if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                FixedPagerYAxisScatter(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = effectiveTickStep,
                    width = yAxisFixedWidth
                )
            }

            // pages area
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val start = page * pageSize
                val end = kotlin.math.min(start + pageSize, data.size)
                val slice = data.subList(start, end)

                // Render a normal ScatterPlot page:
                // - no inner scroll (windowSize = null)
                // - maxY unified with the external axis
                ScatterPlot(
                    modifier = Modifier.fillMaxSize(),
                    data = slice,
                    xLabel = xLabel,
                    yLabel = yLabel,
                    title = title,
                    pointColor = pointColor,
                    pointType = pointType,
                    pointSize = pointSize,
                    minY = minY,
                    maxY = maxRounded,
                    tooltipTextSize = tooltipTextSize,
                    yAxisPosition = yAxisPosition,
                    interactionType = interactionType,
                    showTitle = false,                    // external axis handles it
                    showYAxis = false,                    // external axis handles it
                    maxXTicksLimit = maxXTicksLimit,
                    xLabelAutoSkip = xLabelAutoSkip,
                    yTickStep = effectiveTickStep,        // keep grid aligned with external axis
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

            // RIGHT fixed external Y-axis
            if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                FixedPagerYAxisScatter(
                    maxY = maxRounded,
                    yAxisPosition = yAxisPosition,
                    step = effectiveTickStep,
                    width = yAxisFixedWidth
                )
            }
        }
    }
}

@Composable
private fun FixedPagerYAxisScatter(
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
            values = listOf(0.0, maxY),
            chartType = ChartType.SCATTERPLOT,
            minY = 0.0,
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
