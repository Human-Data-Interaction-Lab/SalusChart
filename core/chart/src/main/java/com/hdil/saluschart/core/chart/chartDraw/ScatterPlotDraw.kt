package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.PointType
import com.hdil.saluschart.core.chart.chartMath.LineChartMath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object ScatterPlotDraw {

    // TODO: 현재 line chart 및 scatter plot 첫 번째 데이터포인트가 왼쪽 가장 끝에서 시작 (첫 번째 포인트가 y축과 맞닿음)
    // - 이에 첫 번째 PointMarker가 y축과 겹치는 현상 발생 (ExampleUI의 ScatterPlot 예시 참고)
    // - 따라서 BarMarker 함수와 유사한 포지셔닝 로직 필요할 수 있음
    // - BarMarker의 포지셔닝 로직 (useLIneChartPositioning = false) 참고 

    // - 현재 BarMarker 함수는 BarMarker 내에서 각 바의 좌표를 계산하지만, 
    // - PointMarker 함수는 외부에서 좌표를 계산한 후 (mapLineToCanvasPoints 또는 mapScatterToCanvasPoints 함수) points 파라미터로 전달받고 있음
    // - 일관성이 부족한 디자인
    
    /**
     * 각 데이터 포인트를 원으로 표시합니다.
     * BarMarker 방식으로 최적화: 중첩 Box 최소화, 변수 사전 계산
     *
     * @param points 포인트 중심 좌표 목록 (외부에서 이미 계산된 좌표 리스트를 불러 옴)
     * @param values 표시할 값 목록
     * @param pointRadius 포인트 외부 반지름
     * @param innerRadius 포인트 내부 반지름
     * @param selectedPointIndex 현재 선택된 포인트 인덱스 (null이면 모든 포인트 선택됨)
     * @param onPointClick 포인트 클릭 시 호출되는 콜백 (포인트 인덱스)
     * @param interactive true이면 클릭 가능하고 툴팁 표시, false이면 순수 시각적 렌더링 (기본값: true)
     * @param chartType 차트 타입 (툴팁 위치 결정용)
     * @param showTooltipForIndex 외부에서 제어되는 툴팁 표시 인덱스 (null이면 표시 안함)
     */
    @Composable
    fun PointMarker(
        data: List<ChartMark>,
        points: List<Offset>,
        values: List<Double>,
        color: Color = Color.Black,
        pointRadius: Dp = 4.dp,
        innerRadius: Dp = 2.dp,
        selectedPointIndex: Int? = null,
        selectedIndices: Set<Int>? = null,
        onPointClick: ((Int) -> Unit)? = null,
        interactive: Boolean = false,
        showPoint: Boolean = true,
        pointType: PointType = PointType.Circle,
        showValue: Boolean = false,
        chartType: ChartType,
        showTooltipForIndex: Int? = null,
        showTooltipForIndices: Set<Int>? = null,
        canvasSize: Size,
        unit : String = "",
    ) {
        val density = LocalDensity.current

        // 최적화: 툴팁 엔트리 용량을 미리 예상하여 할당 (선택된 인덱스 수만큼)
        val expectedTooltipCount = showTooltipForIndices?.size ?: if (showTooltipForIndex != null) 1 else 0
        val tooltipEntries = remember(showTooltipForIndex, showTooltipForIndices) {
            ArrayList<Pair<ChartMark, Offset>>(expectedTooltipCount)
        }.apply { clear() }

        // value labels 사전 계산 (루프 밖으로 이동)
        val textPx = with(density) { 12.sp.toPx() }

        // BarMarker 방식: 단순화된 단일 Box 구조
        points.forEachIndexed { index, center ->
            // Float 좌표를 Dp로 변환
            val xDp = with(density) { center.x.toDp() }
            val yDp = with(density) { center.y.toDp() }

            // 선택 상태 계산: selectedIndices가 우선, 없으면 단일 인덱스 규칙 사용(null이면 모두 선택)
            val isSelected = when {
                selectedIndices != null -> selectedIndices.contains(index)
                else -> selectedPointIndex == null || selectedPointIndex == index
            }
            val pointColor = if (showPoint) {
                if (isSelected) color else Color.Gray
            } else {
                if ((selectedIndices?.contains(index) == true) || selectedPointIndex == index) color else Color.Transparent
            }

            // 툴팁 표시 여부: 집합 기준 우선, 아니면 단일 인덱스 기준
            val showTip = when {
                showTooltipForIndices != null -> showTooltipForIndices.contains(index)
                else -> showTooltipForIndex == index
            }
            if (showTip) {
                tooltipEntries += (data[index] to center)
            }

            // 포인트 타입에 따라 렌더링 (BarMarker처럼 단순화)
            when (pointType) {
                PointType.Circle -> {
                    // 외부 원 - 단일 Box로 처리
                    Box(
                        modifier = Modifier
                            .offset(x = xDp - pointRadius, y = yDp - pointRadius)
                            .size(pointRadius * 2)
                            .background(color = pointColor, shape = CircleShape)
                            .clickable(enabled = interactive) { onPointClick?.invoke(index) }
                    )
                    // 내부 원 (innerRadius > 0인 경우에만)
                    if (innerRadius > 0.dp && (showPoint || isSelected)) {
                        Box(
                            modifier = Modifier
                                .offset(x = xDp - innerRadius, y = yDp - innerRadius)
                                .size(innerRadius * 2)
                                .background(color = Color.White, shape = CircleShape)
                        )
                    }
                }

                PointType.Triangle -> {
                    // Triangle은 Canvas 필요
                    Box(
                        modifier = Modifier
                            .offset(x = xDp - pointRadius, y = yDp - pointRadius)
                            .size(pointRadius * 2)
                            .clickable(enabled = interactive) { onPointClick?.invoke(index) }
                    ) {
                        Canvas(modifier = Modifier.size(pointRadius * 2)) {
                            val trianglePath = Path().apply {
                                val half = size.minDimension / 2
                                val center = Offset(half, half)
                                val angleOffset = -PI / 2
                                for (i in 0..2) {
                                    val angle = angleOffset + i * (2 * PI / 3)
                                    val x = center.x + half * cos(angle).toFloat()
                                    val y = center.y + half * sin(angle).toFloat()
                                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                                }
                                close()
                            }
                            drawPath(path = trianglePath, color = pointColor)
                        }
                    }
                }

                PointType.Square -> {
                    // Square는 BarMarker처럼 단순 Box로 처리
                    Box(
                        modifier = Modifier
                            .offset(x = xDp - pointRadius, y = yDp - pointRadius)
                            .size(pointRadius * 2)
                            .background(color = pointColor)
                            .clickable(enabled = interactive) { onPointClick?.invoke(index) }
                    )
                }
            }
        }

        if (showValue) {
            val anchors = remember(points, values, canvasSize) {
                if (canvasSize.width <= 0f || canvasSize.height <= 0f || points.isEmpty()) emptyList() else {
                    LineChartMath.computeLabelAnchors(
                        points = points,
                        values = values.map { it.toFloat() },
                        canvas = canvasSize,
                        textPx = textPx,
                        padPx = with(density) { 4.dp.toPx() },
                        minGapToLinePx = with(density) { 4.dp.toPx() }
                    )
                }
            }

            // value labels 렌더링 (루프 밖에서 별도로 처리)
            if (anchors.size == points.size) {
                anchors.forEachIndexed { i, topLeft ->
                    val label = values.getOrElse(i) { 0.0 }.let { v ->
                        if (kotlin.math.abs(v - v.toInt()) < 0.001) v.toInt()
                            .toString() else v.toString()
                    }
                    val xDp = with(density) { topLeft.x.toDp() }
                    val yDp = with(density) { topLeft.y.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = xDp, y = yDp)
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = label,
                            color = Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 멀티 툴팁 렌더링
        if (tooltipEntries.isNotEmpty()) {
            tooltipEntries.forEach { (mark, offset) ->
                val xDp = with(density) { offset.x.toDp() }
                val yDp = with(density) { offset.y.toDp() }
                ChartTooltip(
                    chartMark = mark,
                    modifier = Modifier.offset(x = xDp - pointRadius, y = yDp + pointRadius),
                    unit = unit,
                    color = color
                )
            }
        }
    }
//
//    @Composable
//    fun PointMarker(
//        data: List<ChartMark>,
//        points: List<Offset>,
//        values: List<Double>,
//        color: Color = Color.Black,
//        pointRadius: Dp = 4.dp,
//        innerRadius: Dp = 2.dp,
//        selectedPointIndex: Int? = null,
//        onPointClick: ((Int) -> Unit)? = null,
//        interactive: Boolean = false,
//        showPoint: Boolean = true,
//        pointType: PointType = PointType.Circle,
//        showValue: Boolean = false,
//        chartType: ChartType,
//        showTooltipForIndex: Int? = null,
//        canvasSize: Size,
//        unit : String = "",
//    ) {
//        val density = LocalDensity.current
//        var shouldShowTooltip = false
//
//        // 툴팁 정보 저장 변수
//        var tooltipOffset: Offset? = null
//        var tooltipData: ChartMark? = null
//
//
//        points.forEachIndexed { index, center ->
//            // Float 좌표를 Dp로 변환
//            val xDp = with(density) { center.x.toDp() }
//            val yDp = with(density) { center.y.toDp() }
//
//            // 선택 상태 결정: selectedPointIndex가 null이면 모든 포인트 선택됨
//            val isSelected = selectedPointIndex == null || selectedPointIndex == index
//            val outerColor = if (showPoint) if (isSelected) color else Color.Gray else if (selectedPointIndex == index) color else Color.Transparent
//            val innerColor = if (showPoint || selectedPointIndex == index) Color.White else Color.Transparent
//
//            // 툴팁 표시 여부 결정: showTooltipForIndex 값만 사용
//            shouldShowTooltip = (showTooltipForIndex == index)
//
//            if (shouldShowTooltip) {
//                tooltipData = data[index]
//                tooltipOffset = center
//            }
//            val textPx = with(density) { 12.sp.toPx() }
//            val anchors = remember(points, values, canvasSize) {
//                // safe guard: if we can’t compute, return empty (no labels)
//                if (canvasSize.width <= 0f || canvasSize.height <= 0f || points.isEmpty()) {
//                    emptyList()
//                } else {
//                    LineChartMath.computeLabelAnchors(
//                        points = points,
//                        values = values.map { it.toFloat() },
//                        canvas = canvasSize,
//                        textPx = textPx,
//                        padPx = with(density) { 4.dp.toPx() },
//                        minGapToLinePx = with(density) { 4.dp.toPx() }
//                    )
//                }
//            }
//
//            Box(
//                modifier = Modifier
//                    .offset(x = xDp - pointRadius, y = yDp - pointRadius)
//                    .size(pointRadius * 2)
//                    .clickable {
//                        // 외부 클릭 이벤트 처리만 수행
//                        if (interactive) onPointClick?.invoke(index)
//                    },
//                contentAlignment = Alignment.Center
//            ) {
//                when (pointType) {
//                    PointType.Circle -> // 바깥쪽 원
//                        Box(
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .size(pointRadius * 2)
//                                    .background(color = outerColor, shape = CircleShape)
//                            )
//                            // 안쪽 흰색 원
//                            Box(
//                                modifier = Modifier
//                                    .size(innerRadius * 2)
//                                    .background(color = innerColor, shape = CircleShape)
//                            )
//                        }
//
//                    PointType.Triangle ->
//                        Box {
//                            Canvas(modifier = Modifier.size(pointRadius * 2)) {
//                                val trianglePath = Path().apply {
//                                    val half = size.minDimension / 2
//                                    val center = Offset(half, half)
//                                    val angleOffset = -PI / 2 // 위쪽 꼭짓점이 위로 가도록
//                                    for (i in 0..2) {
//                                        val angle = angleOffset + i * (2 * PI / 3)
//                                        val x = center.x + half * cos(angle).toFloat()
//                                        val y = center.y + half * sin(angle).toFloat()
//                                        if (i == 0) moveTo(x, y) else lineTo(x, y)
//                                    }
//                                    close()
//                                }
//                                drawPath(
//                                    path = trianglePath,
//                                    color = outerColor
//                                )
//                            }
//                        }
//                    PointType.Square ->
//                        Box {
//                            Canvas(modifier = Modifier.size(pointRadius * 2)) {
//                                drawRect(
//                                    color = outerColor,
//                                    topLeft = Offset(0f, 0f),
//                                    size = Size(size.width, size.height)
//                                )
//                            }
//                        }
//                }
//            }
//            if (showValue && anchors.size == points.size) {
//                anchors.forEachIndexed { i, topLeft ->
//                    val label = values.getOrElse(i) { 0.0 }.let { v ->
//                        if (kotlin.math.abs(v - v.toInt()) < 0.001) v.toInt().toString() else v.toString()
//                    }
//                    val xDp = with(density) { topLeft.x.toDp() }
//                    val yDp = with(density) { topLeft.y.toDp() }
//
//                    Box(
//                        modifier = Modifier
//                            .offset(x = xDp, y = yDp)
//                            .padding(horizontal = 2.dp, vertical = 1.dp)
//                    ) {
//                        Text(
//                            text = label,
//                            color = Color.Black,
//                            fontSize = 12.sp
//                        )
//                    }
//                }
//            }
//        }
//        // 툴팁은 forEachIndexed 밖에서 한 번만 표시
//        if (tooltipData != null && tooltipOffset != null) {
//            val xDp = with(density) { tooltipOffset!!.x.toDp() }
//            val yDp = with(density) { tooltipOffset!!.y.toDp() }
//            ChartTooltip(
//                ChartMark = tooltipData!!,
//                modifier = Modifier.offset(x = xDp - pointRadius, y = yDp + pointRadius),
//                unit = unit,
//            )
//        }
//    }
}
