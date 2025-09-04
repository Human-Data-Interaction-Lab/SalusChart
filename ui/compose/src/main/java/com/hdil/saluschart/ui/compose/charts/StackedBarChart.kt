package com.hdil.saluschart.ui.compose.charts

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.StackedChartPoint
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLine
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition

/**
 * 스택 바 차트 컴포저블 (건강 데이터 시각화에 최적화)
 * 
 * 영양소 섭취량, 운동 시간 등 여러 구성 요소를 가진 데이터를 시각화할 때 사용합니다.
 * 예: 일별 영양소 섭취량 (단백질, 지방, 탄수화물)
 * 
 * @param modifier 커스텀 modifier
 * @param data 스택 차트 데이터 포인트 목록
 * @param segmentLabels 각 세그먼트의 레이블들 (예: ["단백질", "지방", "탄수화물"])
 * @param xLabel X축 레이블 (예: "날짜")
 * @param yLabel Y축 레이블 (예: "영양소 (g)")
 * @param title 차트 제목
 * @param colors 각 세그먼트의 색상 팔레트 (건강 데이터에 적합한 기본 색상 제공)
 * @param barWidthRatio 바 너비 비율 (0.0 ~ 1.0)
 * @param showLegend 범례 표시 여부
 * @param legendPosition 범례 위치 (LEFT, RIGHT, TOP, BOTTOM)
 * @param yAxisPosition Y축 위치
 * @param interactionType 상호작용 타입 (STACKED_BAR: 개별 세그먼트 터치, TOUCH_AREA: 전체 스택 툴팁)
 * @param onBarClick 바 클릭 시 호출되는 콜백 (바 인덱스, 세그먼트 인덱스, 값)
 */
@Composable
fun StackedBarChart(
    modifier: Modifier = Modifier,
    data: List<StackedChartPoint>,
    segmentLabels: List<String> = emptyList(),
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Stacked Bar Chart",
    colors: List<Color> = listOf(
        Color(0xFF2196F3), // 파랑 (단백질)
        Color(0xFFFF9800), // 주황 (지방)
        Color(0xFF4CAF50), // 초록 (탄수화물)
        Color(0xFF9C27B0), // 보라 (기타)
        Color(0xFFE91E63), // 분홍
        Color(0xFFFFEB3B), // 노랑
    ),
    barWidthRatio: Float = 0.6f,
    showLegend: Boolean = true,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    windowSize: Int? = null, // 윈도우 크기 (null이면 전체 화면)
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT, // Y축 위치
    interactionType: InteractionType.StackedBar = InteractionType.StackedBar.BAR,
    onBarClick: ((barIndex: Int, segmentIndex: Int?, value: Float) -> Unit)? = null,
    chartType: ChartType = ChartType.STACKED_BAR, // 차트 타입 (툴팁 위치 결정용)
    maxXTicksLimit: Int? = null,             // X축에 표시할 최대 라벨 개수 (null이면 모든 라벨 표시)
    referenceLineType: ReferenceLineType = ReferenceLineType.NONE,
    referenceLineColor: Color = Color.Red,
    referenceLineStrokeWidth: Dp = 2.dp,
    referenceLineStyle: LineStyle = LineStyle.DASHED,
    showReferenceLineLabel: Boolean = false,
    referenceLineLabelFormat: String = "평균: %.0f",
    referenceLineInteractive: Boolean = false,
    onReferenceLineClick: (() -> Unit)? = null
) {
    if (data.isEmpty()) return

    val useScrolling = windowSize != null && windowSize < data.size
    val scrollState = rememberScrollState()

    val xLabels = data.map { it.label ?: it.x.toString() }
    var chartMetrics by remember { mutableStateOf<ChartMath.ChartMetrics?>(null) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        // 차트와 좌우 범례를 담는 Row
        if (legendPosition == LegendPosition.LEFT || legendPosition == LegendPosition.RIGHT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 범례를 왼쪽에 배치
                if (showLegend && legendPosition == LegendPosition.LEFT && segmentLabels.isNotEmpty()) {
                    ChartLegend(
                        modifier = Modifier,
                        labels = segmentLabels,
                        colors = colors,
                        position = LegendPosition.LEFT
                    )
                }

                // 스택 바 차트
                BoxWithConstraints {
                    val availableWidth = maxWidth // 전체 사용 가능한 너비
                    val marginHorizontal = 16.dp // 좌우 마진

                    // 스크롤 모드에서 실제 표시할 데이터와 캔버스 너비 계산
                    val canvasWidth = if (useScrolling) {
                        // 스크롤 모드: 좌우 마진을 고려한 실제 차트 너비 계산
                        val chartWidth = availableWidth - (marginHorizontal * 2) // 좌우 마진 제외
                        val sectionsCount = (data.size.toFloat() / windowSize!!.toFloat()).toInt()
                        val totalWidth = chartWidth * sectionsCount
                        totalWidth
                    } else {
                        // 일반 모드: 전체 데이터를 화면에 맞춤
                        null

                    }

                    Box(
                        modifier = if (useScrolling) {
                            Modifier
                                .horizontalScroll(
                                    scrollState,
                                    overscrollEffect = null
                                )
                                .padding(horizontal = marginHorizontal)
                        } else {
                            modifier
                        }
//                        modifier = Modifier.weight(1f)
                    ) {
                        Canvas(modifier = if (useScrolling) {
                            Modifier
                                .width(canvasWidth!!) // 계산된 캔버스 너비 사용
                                .fillMaxHeight()
                        } else {
                            Modifier.fillMaxSize()
                        }) {
                            val totalValues = data.map { it.total }
                            val metrics = ChartMath.computeMetrics(
                                size,
                                totalValues,
                                chartType = ChartType.STACKED_BAR
                            )
                            chartMetrics = metrics

                            ChartDraw.drawGrid(this, size, metrics, yAxisPosition)
                            ChartDraw.drawXAxis(this, metrics)
                            ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                            ChartDraw.Bar.drawBarXAxisLabels(
                                drawContext,
                                xLabels,
                                metrics,
                                maxXTicksLimit = maxXTicksLimit
                            )
                        }

                        // 상호작용 처리
                        when (interactionType) {
                            InteractionType.StackedBar.BAR -> {
                                // Individual segment interaction - each segment is touchable
                                chartMetrics?.let { metrics ->
                                    val segmentCounts = data.map { it.values.size }
                                    val maxSegments = segmentCounts.maxOrNull() ?: 0

                                    if (segmentCounts.any { it != maxSegments }) {
                                        throw IllegalArgumentException("All StackedChartPoints must have the same number of segments. Found: $segmentCounts")
                                    }

                                    for (segmentIndex in 0 until maxSegments) {
                                        val segmentMinValues = mutableListOf<Float>()
                                        val segmentMaxValues = mutableListOf<Float>()

                                        data.forEach { stackedPoint ->
                                            var cumulativeValue = 0f
                                            for (i in 0 until segmentIndex) {
                                                cumulativeValue += stackedPoint.values.getOrNull(i)
                                                    ?: 0f
                                            }
                                            val segmentValue =
                                                stackedPoint.values.getOrNull(segmentIndex) ?: 0f

                                            segmentMinValues.add(cumulativeValue)
                                            segmentMaxValues.add(cumulativeValue + segmentValue)
                                        }

                                        val hasNonZeroValues =
                                            segmentMaxValues.zip(segmentMinValues)
                                                .any { (max, min) -> max > min }
                                        if (hasNonZeroValues) {
                                            val segmentColor =
                                                colors.getOrNull(segmentIndex) ?: Color.Gray

                                            ChartDraw.Bar.BarMarker(
                                                data = data,
                                                minValues = segmentMinValues,
                                                maxValues = segmentMaxValues,
                                                metrics = metrics,
                                                color = segmentColor,
                                                barWidthRatio = barWidthRatio,
                                                interactive = false,
                                                chartType = chartType,
                                                segmentIndex = segmentIndex,
                                                onBarClick = { barIndex, _ ->
                                                    val segmentValue =
                                                        data.getOrNull(barIndex)?.values?.getOrNull(
                                                            segmentIndex
                                                        ) ?: 0f
                                                    onBarClick?.invoke(
                                                        barIndex,
                                                        segmentIndex,
                                                        segmentValue
                                                    )
                                                }
                                            )
                                        }
                                    }
                                    // 모든 bar 합친 barmarker
                                    ChartDraw.Bar.BarMarker(
                                        data = data,
                                        minValues = List(data.size) { metrics.minY },
                                        maxValues = data.map { it.total },
                                        metrics = metrics,
                                        onBarClick = { index, _ ->
                                            selectedBarIndex =
                                                if (selectedBarIndex == index) null else index
                                            val stackedPoint = data.getOrNull(index)
                                            if (stackedPoint != null) {
                                                onBarClick?.invoke(index, null, stackedPoint.total)
                                            }
                                        },
                                        barWidthRatio = barWidthRatio,
                                        chartType = chartType,
                                        showTooltipForIndex = selectedBarIndex,
                                        interactive = true,
                                        color = Color.Transparent
                                    )
                                }
                            }

                            InteractionType.StackedBar.TOUCH_AREA -> {
                                // Area-based interaction - show all segment values in tooltip
                                chartMetrics?.let { metrics ->
                                    val segmentCounts = data.map { it.values.size }
                                    val maxSegments = segmentCounts.maxOrNull() ?: 0

                                    if (segmentCounts.any { it != maxSegments }) {
                                        throw IllegalArgumentException("All StackedChartPoints must have the same number of segments. Found: $segmentCounts")
                                    }
                                    for (segmentIndex in 0 until maxSegments) {
                                        val segmentMinValues = mutableListOf<Float>()
                                        val segmentMaxValues = mutableListOf<Float>()

                                        data.forEach { stackedPoint ->
                                            var cumulativeValue = 0f
                                            for (i in 0 until segmentIndex) {
                                                cumulativeValue += stackedPoint.values.getOrNull(i)
                                                    ?: 0f
                                            }
                                            val segmentValue =
                                                stackedPoint.values.getOrNull(segmentIndex) ?: 0f

                                            segmentMinValues.add(cumulativeValue)
                                            segmentMaxValues.add(cumulativeValue + segmentValue)
                                        }

                                        val hasNonZeroValues =
                                            segmentMaxValues.zip(segmentMinValues)
                                                .any { (max, min) -> max > min }
                                        if (hasNonZeroValues) {
                                            val segmentColor =
                                                colors.getOrNull(segmentIndex) ?: Color.Gray

                                            ChartDraw.Bar.BarMarker(
                                                data = data,
                                                minValues = segmentMinValues,
                                                maxValues = segmentMaxValues,
                                                metrics = metrics,
                                                color = segmentColor,
                                                barWidthRatio = barWidthRatio,
                                                interactive = false,
                                                chartType = chartType,
                                                showTooltipForIndex = selectedBarIndex
                                            )
                                        }
                                    }

                                    // Then create transparent touch areas for interaction
                                    ChartDraw.Bar.BarMarker(
                                        data = data,
                                        minValues = List(data.size) { metrics.minY },
                                        maxValues = data.map { it.total },
                                        metrics = metrics,
                                        onBarClick = { index, _ ->
                                            selectedBarIndex =
                                                if (selectedBarIndex == index) null else index
                                            val stackedPoint = data.getOrNull(index)
                                            if (stackedPoint != null) {
                                                onBarClick?.invoke(index, null, stackedPoint.total)
                                            }
                                        },
                                        barWidthRatio = barWidthRatio,
                                        chartType = chartType,
                                        showTooltipForIndex = selectedBarIndex,
                                        isTouchArea = true
                                    )
                                }
                            }

                            else -> {
                                // Default: non-interactive rendering
                                chartMetrics?.let { metrics ->
                                    val segmentCounts = data.map { it.values.size }
                                    val maxSegments = segmentCounts.maxOrNull() ?: 0

                                    if (segmentCounts.any { it != maxSegments }) {
                                        throw IllegalArgumentException("All StackedChartPoints must have the same number of segments. Found: $segmentCounts")
                                    }

                                    for (segmentIndex in 0 until maxSegments) {
                                        val segmentMinValues = mutableListOf<Float>()
                                        val segmentMaxValues = mutableListOf<Float>()

                                        data.forEach { stackedPoint ->
                                            var cumulativeValue = 0f
                                            for (i in 0 until segmentIndex) {
                                                cumulativeValue += stackedPoint.values.getOrNull(i)
                                                    ?: 0f
                                            }
                                            val segmentValue =
                                                stackedPoint.values.getOrNull(segmentIndex) ?: 0f

                                            segmentMinValues.add(cumulativeValue)
                                            segmentMaxValues.add(cumulativeValue + segmentValue)
                                        }

                                        val hasNonZeroValues =
                                            segmentMaxValues.zip(segmentMinValues)
                                                .any { (max, min) -> max > min }
                                        if (hasNonZeroValues) {
                                            val segmentColor =
                                                colors.getOrNull(segmentIndex) ?: Color.Gray

                                            ChartDraw.Bar.BarMarker(
                                                data = data,
                                                minValues = segmentMinValues,
                                                maxValues = segmentMaxValues,
                                                metrics = metrics,
                                                color = segmentColor,
                                                barWidthRatio = barWidthRatio,
                                                interactive = false,
                                                chartType = chartType
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 범례를 오른쪽에 배치
                if (showLegend && legendPosition == LegendPosition.RIGHT && segmentLabels.isNotEmpty()) {
                    ChartLegend(
                        modifier = Modifier,
                        labels = segmentLabels,
                        colors = colors,
                        position = LegendPosition.RIGHT
                    )
                }
            }
        } else {
            // TOP, BOTTOM: 차트와 범례를 세로로 배치
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showLegend && legendPosition == LegendPosition.TOP && segmentLabels.isNotEmpty()) {
                    ChartLegend(
                        modifier = Modifier,
                        labels = segmentLabels,
                        colors = colors,
                        position = LegendPosition.TOP
                    )
                    Spacer(Modifier.height(16.dp))
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                ) {
                    val availableWidth = maxWidth // 전체 사용 가능한 너비
                    val marginHorizontal = 16.dp

                    // 스크롤 모드에서 실제 표시할 데이터와 캔버스 너비 계산
                    val canvasWidth = if(useScrolling) {
                        // 스크롤 모드: 좌우 마진을 고려한 실제 차트 너비 계산
                        val chartWidth = availableWidth - (marginHorizontal * 2) // 좌우 마진 제외
                        val sectionsCount = (data.size.toFloat() / windowSize!!.toFloat()).toInt()
                        val totalWidth = chartWidth * sectionsCount
                        totalWidth
                    } else {
                        // 일반 모드: 전체 데이터를 화면에 맞춤
                        null
                    }

                    Box(
//                        modifier = Modifier.weight(1f)
                        modifier = if (useScrolling) {
                            Modifier
                                .horizontalScroll(
                                    scrollState,
                                    overscrollEffect = null
                                )
                                .padding(horizontal = marginHorizontal)
                        } else {
                            modifier
                        }
                    ) {
                        Canvas(modifier = if (useScrolling) {
                            Modifier
                                .width(canvasWidth!!) // 계산된 캔버스 너비 사용
                                .fillMaxHeight()
                        } else {
                            Modifier.fillMaxSize()
                        }) {
                            val totalValues = data.map { it.total }
                            val metrics = ChartMath.computeMetrics(size, totalValues, chartType = ChartType.STACKED_BAR)
                            chartMetrics = metrics

                            ChartDraw.drawGrid(this, size, metrics, yAxisPosition)
                            ChartDraw.drawXAxis(this, metrics)
                            ChartDraw.drawYAxis(this, metrics, yAxisPosition)
                            ChartDraw.Bar.drawBarXAxisLabels(drawContext, xLabels, metrics, maxXTicksLimit = maxXTicksLimit)
                        }

                        // 상호작용 처리
                        when (interactionType) {
                            InteractionType.StackedBar.BAR -> {
                                // Individual segment interaction - each segment is touchable
                                chartMetrics?.let { metrics ->
                                    val segmentCounts = data.map { it.values.size }
                                    val maxSegments = segmentCounts.maxOrNull() ?: 0

                                    if (segmentCounts.any { it != maxSegments }) {
                                        throw IllegalArgumentException("All StackedChartPoints must have the same number of segments. Found: $segmentCounts")
                                    }

                                    for (segmentIndex in 0 until maxSegments) {
                                        val segmentMinValues = mutableListOf<Float>()
                                        val segmentMaxValues = mutableListOf<Float>()

                                        data.forEach { stackedPoint ->
                                            var cumulativeValue = 0f
                                            for (i in 0 until segmentIndex) {
                                                cumulativeValue += stackedPoint.values.getOrNull(i) ?: 0f
                                            }
                                            val segmentValue = stackedPoint.values.getOrNull(segmentIndex) ?: 0f

                                            segmentMinValues.add(cumulativeValue)
                                            segmentMaxValues.add(cumulativeValue + segmentValue)
                                        }

                                        val hasNonZeroValues = segmentMaxValues.zip(segmentMinValues).any { (max, min) -> max > min }
                                        if (hasNonZeroValues) {
                                            val segmentColor = colors.getOrNull(segmentIndex) ?: Color.Gray

                                            ChartDraw.Bar.BarMarker(
                                                data = data,
                                                minValues = segmentMinValues,
                                                maxValues = segmentMaxValues,
                                                metrics = metrics,
                                                color = segmentColor,
                                                barWidthRatio = barWidthRatio,
                                                interactive = false,
                                                chartType = chartType,
                                                segmentIndex = segmentIndex,
                                                onBarClick = { barIndex, _ ->
                                                    val segmentValue = data.getOrNull(barIndex)?.values?.getOrNull(segmentIndex) ?: 0f
                                                    onBarClick?.invoke(barIndex, segmentIndex, segmentValue)
                                                }
                                            )
                                        }
                                    }

                                    // 모든 bar 합친 barmarker
                                    ChartDraw.Bar.BarMarker(
                                        data = data,
                                        minValues = List(data.size) { metrics.minY },
                                        maxValues = data.map { it.total },
                                        metrics = metrics,
                                        onBarClick = { index, _ ->
                                            selectedBarIndex = if (selectedBarIndex == index) null else index
                                            val stackedPoint = data.getOrNull(index)
                                            if (stackedPoint != null) {
                                                onBarClick?.invoke(index, null, stackedPoint.total)
                                            }
                                        },
                                        barWidthRatio = barWidthRatio,
                                        chartType = chartType,
                                        showTooltipForIndex = selectedBarIndex,
                                        interactive = true,
                                        color = Color.Transparent
                                    )
                                }

                            }
                            InteractionType.StackedBar.TOUCH_AREA -> {
                                // Area-based interaction - show all segment values in tooltip
                                chartMetrics?.let { metrics ->
                                    val segmentCounts = data.map { it.values.size }
                                    val maxSegments = segmentCounts.maxOrNull() ?: 0

                                    if (segmentCounts.any { it != maxSegments }) {
                                        throw IllegalArgumentException("All StackedChartPoints must have the same number of segments. Found: $segmentCounts")
                                    }
                                    for (segmentIndex in 0 until maxSegments) {
                                        val segmentMinValues = mutableListOf<Float>()
                                        val segmentMaxValues = mutableListOf<Float>()

                                        data.forEach { stackedPoint ->
                                            var cumulativeValue = 0f
                                            for (i in 0 until segmentIndex) {
                                                cumulativeValue += stackedPoint.values.getOrNull(i) ?: 0f
                                            }
                                            val segmentValue = stackedPoint.values.getOrNull(segmentIndex) ?: 0f

                                            segmentMinValues.add(cumulativeValue)
                                            segmentMaxValues.add(cumulativeValue + segmentValue)
                                        }

                                        val hasNonZeroValues = segmentMaxValues.zip(segmentMinValues).any { (max, min) -> max > min }
                                        if (hasNonZeroValues) {
                                            val segmentColor = colors.getOrNull(segmentIndex) ?: Color.Gray

                                            ChartDraw.Bar.BarMarker(
                                                data = data,
                                                minValues = segmentMinValues,
                                                maxValues = segmentMaxValues,
                                                metrics = metrics,
                                                color = segmentColor,
                                                barWidthRatio = barWidthRatio,
                                                interactive = false,
                                                chartType = chartType,
                                                showTooltipForIndex = selectedBarIndex
                                            )
                                        }
                                    }

                                    // Then create transparent touch areas for interaction
                                    ChartDraw.Bar.BarMarker(
                                        data = data,
                                        minValues = List(data.size) { metrics.minY },
                                        maxValues = data.map { it.total },
                                        metrics = metrics,
                                        onBarClick = { index, _ ->
                                            selectedBarIndex = if (selectedBarIndex == index) null else index
                                            val stackedPoint = data.getOrNull(index)
                                            if (stackedPoint != null) {
                                                onBarClick?.invoke(index, null, stackedPoint.total)
                                            }
                                        },
                                        chartType = chartType,
                                        showTooltipForIndex = selectedBarIndex,
                                        isTouchArea = true
                                    )
                                }
                            }
                            else -> {
                                // Default: non-interactive rendering
                                chartMetrics?.let { metrics ->
                                    val segmentCounts = data.map { it.values.size }
                                    val maxSegments = segmentCounts.maxOrNull() ?: 0

                                    if (segmentCounts.any { it != maxSegments }) {
                                        throw IllegalArgumentException("All StackedChartPoints must have the same number of segments. Found: $segmentCounts")
                                    }

                                    for (segmentIndex in 0 until maxSegments) {
                                        val segmentMinValues = mutableListOf<Float>()
                                        val segmentMaxValues = mutableListOf<Float>()

                                        data.forEach { stackedPoint ->
                                            var cumulativeValue = 0f
                                            for (i in 0 until segmentIndex) {
                                                cumulativeValue += stackedPoint.values.getOrNull(i) ?: 0f
                                            }
                                            val segmentValue = stackedPoint.values.getOrNull(segmentIndex) ?: 0f

                                            segmentMinValues.add(cumulativeValue)
                                            segmentMaxValues.add(cumulativeValue + segmentValue)
                                        }

                                        val hasNonZeroValues = segmentMaxValues.zip(segmentMinValues).any { (max, min) -> max > min }
                                        if (hasNonZeroValues) {
                                            val segmentColor = colors.getOrNull(segmentIndex) ?: Color.Gray

                                            ChartDraw.Bar.BarMarker(
                                                data = data,
                                                minValues = segmentMinValues,
                                                maxValues = segmentMaxValues,
                                                metrics = metrics,
                                                color = segmentColor,
                                                barWidthRatio = barWidthRatio,
                                                interactive = false,
                                                chartType = chartType
                                            )
                                        }
                                    }
                                }
                            }
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

                if (showLegend && legendPosition == LegendPosition.BOTTOM && segmentLabels.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    ChartLegend(
                        labels = segmentLabels,
                        colors = colors,
                        position = LegendPosition.BOTTOM
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}
