package com.hdil.saluschart.core.chart.chartDraw

import android.graphics.fonts.FontStyle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawContext
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.hdil.saluschart.core.chart.BaseChartPoint
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.StackedChartPoint
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw.formatTickLabel
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import kotlin.Boolean

object BarChartDraw {
    /**
     * 바 차트의 X축 레이블을 그립니다 (첫 번째 레이블이 바 너비의 절반만큼 오른쪽에서 시작).
     *
     * @param ctx 그리기 컨텍스트
     * @param labels X축에 표시할 레이블 목록
     * @param metrics 차트 메트릭 정보
     * @param centered 텍스트를 중앙 정렬할지 여부 (기본값: true)
     * @param textSize 레이블 텍스트 크기 (기본값: 28f)
     * @param maxXTicksLimit X축에 표시할 최대 라벨 개수 (null이면 모든 라벨 표시)
     */
    fun drawBarXAxisLabels(
        ctx: DrawContext,
        labels: List<String>,
        metrics: ChartMath.ChartMetrics,
        centered: Boolean = true,
        textSize: Float = 28f,
        maxXTicksLimit: Int? = null
    ) {
        // 틱 개수 제한이 설정된 경우 라벨을 줄임
        val (displayLabels, displayIndices) = if (maxXTicksLimit != null) {
            ChartMath.reduceXAxisTicks(labels, maxXTicksLimit)
        } else {
            Pair(labels, labels.indices.toList())
        }
        
        val totalLabels = labels.size
        val barWidth = metrics.chartWidth / totalLabels / 2
        val spacing = metrics.chartWidth / totalLabels
        
        displayLabels.forEachIndexed { displayIndex, label ->
            // 원본 라벨 목록에서의 실제 인덱스를 사용
            val originalIndex = displayIndices[displayIndex]
            // 차트 영역의 시작점(paddingLeftX)에서 바의 중심까지 계산
            val x = metrics.paddingX + barWidth + originalIndex * spacing
            ctx.canvas.nativeCanvas.drawText(
                label,
                x,
                metrics.paddingY + metrics.chartHeight + 50f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    this.textSize = textSize
                    if (centered) {
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                }
            )
        }
    }

    /**
     * 바 차트 막대들을 Composable로 생성합니다.
     * 상호작용 여부를 제어할 수 있습니다.
     *
     * @param minValues 바의 최소값 목록 (일반 바 차트는 0, 범위 바 차트는 실제 최소값)
     * @param maxValues 바의 최대값 목록 (바의 상단 값)
     * @param metrics 차트 메트릭 정보
     * @param color 바 색상 (단일 바용)
     * @param barWidthRatio 바 너비 배수 (기본값: 0.8f)
     * @param interactive true이면 클릭 가능하고 툴팁 표시, false이면 순수 시각적 렌더링 (기본값: true)
     * @param useLineChartPositioning true이면 라인차트 ���지셔닝 사용, false이면 바차트 포지셔닝 사용 (기본값: false)
     * @param onBarClick 바 클릭 시 호출되는 콜백 (바 인덱스, 툴팁 텍스트)
     * @param chartType 차트 타입 (툴팁 위치 결정용)
     * @param showTooltipForIndex 외부에서 제어되는 툴팁 표시 인덱스 (null이면 표시 안함)
     * @param isTouchArea true이면 터치 영역용 (투명, 전체 높이, 상호작용 가능), false이면 일반 바 (기본값: false)
     * @param customTooltipText 커스텀 툴팁 텍스트 목록 (null이면 기본 툴팁 사용)
     * @param segmentIndex 스택 바 차트에서 세그먼트 인덱스 (툴팁 위치 조정용, null이면 기본 위치)
     */
    @Composable
    fun BarMarker(
        data: List<BaseChartPoint>,
        minValues: List<Float>,
        maxValues: List<Float>,
        metrics: ChartMath.ChartMetrics,
        color: Color = Color.Black,
        barWidthRatio: Float = 0.8f,
        interactive: Boolean = true,
        useLineChartPositioning: Boolean = false,
        onBarClick: ((Int, String) -> Unit)? = null,
        chartType: ChartType,
        showTooltipForIndex: Int? = null,
        isTouchArea: Boolean = false,
        customTooltipText: List<String>? = null,
        segmentIndex: Int? = null,
        showLabel: Boolean = false,
        unit: String = "",
    ) {
        val density = LocalDensity.current

        // 터치 영역용인 경우 자동으로 파라미터 설정
        val actualBarWidthRatio = if (isTouchArea) 1.0f else barWidthRatio
        val actualInteractive = if (isTouchArea) true else interactive

        val dataSize = maxOf(minValues.size, maxValues.size)

        // 클릭된 바의 인덱스를 관리하는 상태 변수
        var clickedBarIndex by remember { mutableStateOf<Int?>(null) }

        // 툴팁 정보 저장 변수
        var tooltipOffset: Offset? = null
        var tooltipData: BaseChartPoint? = null

        (0 until dataSize).forEach { index ->
            // 값 추출
            val minValue = minValues.getOrNull(index) ?: 0f
            val maxValue = maxValues.getOrNull(index) ?: 0f

            // 툴팁 텍스트 결정: 커스텀 텍스트가 있으면 사용, 없으면 기본 로직 사용
            val tooltipText = customTooltipText?.getOrNull(index) ?: run {
                if (minValue == metrics.minY) {
                    // For regular bars starting from chart minimum, show only max value
                    maxValue.toInt().toString()
                } else {
                    // For range bars, show min-max range
                    "${minValue.toInt()}-${maxValue.toInt()}"
                }
            }

            // 바 높이와 위치 계산
            val (barHeight, barY) = if (isTouchArea) {
                // 전체 차트 높이 사용 (터치 영역용)
                Pair(metrics.chartHeight, metrics.paddingY)
            } else {
                // minValue에서 maxValue까지의 바 계산
                val yMinScreen = metrics.chartHeight - ((minValue - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
                val yMaxScreen = metrics.chartHeight - ((maxValue - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
                val height = yMinScreen - yMaxScreen
                // Convert chart-relative Y to canvas coordinates by adding paddingY
                Pair(height, metrics.paddingY + yMaxScreen)
            }

            // 바 X 위치 계산 - 차트 타입에 따라 다른 포지셔닝 로직 사용
            val (barWidth, barX) = if (useLineChartPositioning) {
                // 라인차트 포지셔닝: 포인트 중심에 바 배치
                val pointSpacing = if (dataSize > 1) metrics.chartWidth / (dataSize - 1) else 0f
                val pointX = metrics.paddingX + index * pointSpacing

                // 첫 번째와 마지막 바는 차트 영역을 벗어나지 않도록 절반 너비로 설정
                val isFirstOrLast = (index == 0 || index == dataSize - 1) && dataSize > 1
                val widthMultiplier = if (isFirstOrLast) 0.5f else 1.0f
                val barW = if (dataSize > 1) {
                    pointSpacing * actualBarWidthRatio * widthMultiplier
                } else {
                    metrics.chartWidth * actualBarWidthRatio
                }

                // 첫 번째 바는 오른쪽으로만 확장, 마지막 바는 왼쪽으로만 확장
                val barXPos = when {
                    index == 0 && dataSize > 1 -> pointX // 첫 번째 바: 포인트에서 시작
                    index == dataSize - 1 && dataSize > 1 -> pointX - barW // 마지막 바: 포인트에서 끝
                    else -> pointX - barW / 2f // 중간 바들: 포인트 중심
                }

                Pair(barW, barXPos)
            } else {
                // 바차트 포지셔닝: 할당된 공간의 중앙에 배치
                val barW = metrics.chartWidth / dataSize * actualBarWidthRatio
                val spacing = metrics.chartWidth / dataSize
                val barXPos = metrics.paddingX + index * spacing + (spacing - barW) / 2f
                Pair(barW, barXPos)
            }

            // Float 좌표를 Dp로 변환
            val barXDp = with(density) { barX.toDp() }
            val barYDp = with(density) { barY.toDp() }
            val barWidthDp = with(density) { barWidth.toDp() }
            val barHeightDp = with(density) { barHeight.toDp() }

            // 툴팁 표시 여부 결정:
            // - isTouchArea = true인 경우 툴팁 표시 안함 (터치 영역용이므로)
            // - 바 차트 타입이 아닌 경우 툴팁 표시 안함 (LINE, SCATTERPLOT 등은 PointMarker 사용)
            val shouldShowTooltip = when {
                isTouchArea -> false // 터치 영역용이므로 툴팁 표시 안함
                chartType in listOf(ChartType.BAR, ChartType.RANGE_BAR, ChartType.STACKED_BAR) -> {
                    if (actualInteractive) {
                        clickedBarIndex == index
                    } else {
                        showTooltipForIndex == index
                    }
                }
                else -> false // LINE, SCATTERPLOT 등에서는 툴팁 표시 안함
            }


            if (shouldShowTooltip) {
                tooltipData = data[index]
                tooltipOffset = Offset(barX, barY)
            }

            val actualColor = if (isTouchArea) {
                Color.Transparent // 터치 영역용은 투명
            } else {
                if (actualInteractive) {
                    if (clickedBarIndex == index || clickedBarIndex == null) {
                        color
                    } else {
                        color.copy(alpha = 0.3f) // 클릭되지 않은 바는 반투명 처리
                    }
                } else {
                    if (showTooltipForIndex == index || showTooltipForIndex == null) {
                        color
                    } else {
                        color.copy(alpha = 0.3f) // 클릭되지 않은 바는 반투명 처리
                    }
                }
            }

            Box(
                modifier = Modifier
                    .offset(x = barXDp, y = barYDp)
                    .size(width = barWidthDp, height = barHeightDp)
                    .background(color = actualColor)
                    .clickable {
                        if (actualInteractive) {
                            // 클릭된 바 인덱스 토글
                            clickedBarIndex = if (clickedBarIndex == index) null else index
                            // 외부 클릭 이벤트 처리
                            onBarClick?.invoke(index, tooltipText)
                        }
                    },
                contentAlignment = Alignment.TopCenter
            ) {

                // label 표시 여부 결정
                if (showLabel) {
                    Box(
                        modifier = Modifier
                            .offset(0.dp, (0).dp) // 바 위에 표시
                    ) {
                        Text(
                            text = maxValue.toInt().toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Center),
                            maxLines = 1, // 한 줄로 제한하여 수평 확장 유도
                            softWrap = false // 텍스트 래핑 비활성화
                        )
                    }
                }
            }
        }
        // 툴팁 표시 (바 박스 외부에 독립적으로 배치)
        if (tooltipData != null && tooltipOffset != null) {
            val xDp = with(density) { tooltipOffset.x.toDp() }
            val yDp = with(density) { tooltipOffset.y.toDp() }
            ChartTooltip(
                chartPoint = tooltipData,
                unit = unit,
                modifier = Modifier.offset(x = xDp, y = yDp - 80.dp)
            )
        }
    }
}