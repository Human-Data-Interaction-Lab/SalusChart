package com.hdil.saluschart.ui.compose.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.chartMath.PieChartMath
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.ui.theme.ColorUtils

/**
 * 파이 차트를 표시하는 컴포저블 함수입니다.
 *
 * @param modifier 모디파이어
 * @param data 파이 차트에 표시할 데이터 포인트 목록
 * @param title 차트 제목
 * @param isDonut 도넛 차트로 표시할지 여부 (기본값: true)
 * @param colors 각 조각에 사용할 색상 목록
 * @param showLegend 범례를 표시할지 여부
 * @param legendPosition 범례 위치 (LEFT, RIGHT, TOP, BOTTOM)
 * @param showLabel 레이블 표시 여부
 * @param chartType 차트 타입 (툴팁 위치 결정용)
 */
@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    data: List<ChartPoint>,
    title: String = "Pie Chart Example",
    isDonut: Boolean = true,
    colors: List<androidx.compose.ui.graphics.Color> = ColorUtils.ColorUtils(data.size.coerceAtLeast(1)),
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    showLabel: Boolean = false,
    chartType: ChartType = ChartType.PIE // 차트 타입 (툴팁 위치 결정용)
) {
    if (data.isEmpty()) return

    // 클릭된 섹션의 인덱스 상태
    var selectedSectionIndex by remember { mutableStateOf(-1) }

    // 툴팁 위치와 표시 상태
    var tooltipPosition by remember { mutableStateOf(IntOffset.Zero) }
    var showTooltip by remember { mutableStateOf(false) }

    // 애니메이션 스케일
    val animationScale by animateFloatAsState(
        targetValue = 1.05f,
        animationSpec = tween(300),
        label = "pieScale"
    )

    val density = LocalDensity.current

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
                if (showLegend && legendPosition == LegendPosition.LEFT) {
                    ChartLegend(
                        modifier = Modifier,
                        chartData = data,
                        colors = colors,
                        position = LegendPosition.LEFT
                    )
                }

                // 파이 차트
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val (center, radius) = ChartMath.Pie.computePieMetrics(androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()))
                                    val sections = ChartMath.Pie.computePieAngles(data)

                                    val clickedIndex = ChartMath.Pie.getClickedSectionIndex(
                                        offset, center, radius, sections
                                    )

                                    if (clickedIndex >= 0) {
                                        selectedSectionIndex = if (selectedSectionIndex == clickedIndex) -1 else clickedIndex
                                        if (selectedSectionIndex >= 0) {
                                            val (startAngle, sweepAngle, _) = sections[selectedSectionIndex]
                                            val midAngle = startAngle + sweepAngle / 2

                                            val labelPos = PieChartMath.calculateCenterPosition(center, radius/2, midAngle, true)
                                            // 툴팁 위치 설정 (라벨 위치와 동일)
                                            tooltipPosition = IntOffset(
                                                with(density) { labelPos.x.toInt() },
                                                with(density) { labelPos.y.toInt() }
                                            )

                                            showTooltip = true
                                        } else {
                                            showTooltip = false
                                        }
                                    } else {
                                        selectedSectionIndex = -1
                                        showTooltip = false
                                    }
                                }
                            }
                    ) {
                        val (center, radius) = ChartMath.Pie.computePieMetrics(size)
                        val sections = ChartMath.Pie.computePieAngles(data)

                        if (sections.isNotEmpty()) {
                            // 각 섹션 그리기
                            sections.forEachIndexed { i, (startAngle, sweepAngle, _) ->
                                val colorIndex = i % colors.size
                                val isSelected = i == selectedSectionIndex
                                val alpha = if (selectedSectionIndex >= 0 && !isSelected) 0.3f else 1.0f
                                val scale = if (isSelected) animationScale else 1.0f

                                ChartDraw.Pie.drawPieSection(
                                    drawScope = this,
                                    center = center,
                                    radius = radius,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    color = colors[colorIndex],
                                    isDonut = isDonut,
                                    strokeWidth = 100f,
                                    isSelected = isSelected,
                                    animationScale = scale,
                                    alpha = alpha
                                )
                            }

                            // 레이블 그리기
                            if (showLabel) {
                                ChartDraw.Pie.drawPieLabels(
                                    drawScope = this,
                                    center = center,
                                    radius = radius,
                                    data = data,
                                    sections = sections
                                )
                            }
                        }
                    }

                    // 툴팁 표시
                    if (showTooltip && selectedSectionIndex >= 0 && selectedSectionIndex < data.size) {
                        ChartTooltip(
                            chartPoint = data[selectedSectionIndex],
                            modifier = Modifier.offset {
                                tooltipPosition
                            }
                        )
                    }
                }

                // 범례를 오른쪽에 배치
                if (showLegend && legendPosition == LegendPosition.RIGHT) {
                    ChartLegend(
                        modifier = Modifier,
                        chartData = data,
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
                    .fillMaxHeight(), // 전체 높이 차지
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showLegend && legendPosition == LegendPosition.TOP) {
                    ChartLegend(
                        modifier = Modifier,
                        chartData = data,
                        colors = colors,
                        position = LegendPosition.TOP
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val (center, radius) = ChartMath.Pie.computePieMetrics(androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()))
                                    val sections = ChartMath.Pie.computePieAngles(data)

                                    val clickedIndex = ChartMath.Pie.getClickedSectionIndex(
                                        offset, center, radius, sections
                                    )

                                    if (clickedIndex >= 0) {
                                        selectedSectionIndex = if (selectedSectionIndex == clickedIndex) -1 else clickedIndex
                                        if (selectedSectionIndex >= 0) {
                                            // 클릭된 섹션의 중앙 각도 계산 (라벨 위치와 동일)
                                            val (startAngle, sweepAngle, _) = sections[selectedSectionIndex]
                                            val midAngle = startAngle + sweepAngle / 2

                                            val labelPos = PieChartMath.calculateCenterPosition(center, radius/2, midAngle, true)
                                            // 툴팁 위치 설정 (라벨 위치와 동일)
                                            tooltipPosition = IntOffset(
                                                with(density) { labelPos.x.toInt() },
                                                with(density) { labelPos.y.toInt() }
                                            )
                                            showTooltip = true
                                        } else {
                                            showTooltip = false
                                        }
                                    } else {
                                        selectedSectionIndex = -1
                                        showTooltip = false
                                    }
                                }
                            }
                    ) {
                        val (center, radius) = ChartMath.Pie.computePieMetrics(size)
                        val sections = ChartMath.Pie.computePieAngles(data)

                        if (sections.isNotEmpty()) {
                            // 각 섹션 그리기
                            sections.forEachIndexed { i, (startAngle, sweepAngle, _) ->
                                val colorIndex = i % colors.size
                                val isSelected = i == selectedSectionIndex
                                val alpha = if (selectedSectionIndex >= 0 && !isSelected) 0.3f else 1.0f
                                val scale = if (isSelected) animationScale else 1.0f

                                ChartDraw.Pie.drawPieSection(
                                    drawScope = this,
                                    center = center,
                                    radius = radius,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    color = colors[colorIndex],
                                    isDonut = isDonut,
                                    strokeWidth = 100f,
                                    isSelected = isSelected,
                                    animationScale = scale,
                                    alpha = alpha
                                )
                            }

                            // 레이블 그리기
                            if (showLabel) {
                                ChartDraw.Pie.drawPieLabels(
                                    drawScope = this,
                                    center = center,
                                    radius = radius,
                                    data = data,
                                    sections = sections
                                )
                            }
                        }
                    }

                    // 툴팁 표시
                    if (showTooltip && selectedSectionIndex >= 0 && selectedSectionIndex < data.size) {
                        ChartTooltip(
                            chartPoint = data[selectedSectionIndex],
                            modifier = Modifier.offset {
                                tooltipPosition
                            }
                        )
                    }
                }

                if (showLegend && legendPosition == LegendPosition.BOTTOM) {
                    Spacer(Modifier.height(16.dp))
                    ChartLegend(
                        chartData = data,
                        colors = colors,
                        position = LegendPosition.BOTTOM
                    )
                }
            }
        }
    }
}