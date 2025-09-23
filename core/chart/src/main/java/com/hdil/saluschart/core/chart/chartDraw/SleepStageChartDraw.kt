package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.BaseChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.chartMath.SleepStageChartMath

object SleepStageChartDraw {
    
    /**
     * 수평 바 차트의 바들을 Composable로 생성합니다.
     * 수면 단계 차트와 같은 수평 바 차트에 사용됩니다.
     *
     * @param data 차트 데이터 포인트 목록
     * @param minValues 바의 최소값 목록 (X축 방향의 시작값)
     * @param maxValues 바의 최대값 목록 (X축 방향의 끝값)
     * @param metrics 차트 메트릭 정보
     * @param color 바 색상 (단일 바용)
     * @param barHeightRatio 바 높이 배수 (기본값: 0.8f)
     * @param interactive true이면 클릭 가능하고 툴팁 표시, false이면 순수 시각적 렌더링 (기본값: true)
     * @param onBarClick 바 클릭 시 호출되는 콜백 (바 인덱스, 툴팁 텍스트)
     * @param chartType 차트 타입 (툴팁 위치 결정용)
     * @param showTooltipForIndex 외부에서 제어되는 툴팁 표시 인덱스 (null이면 표시 안함)
     * @param isTouchArea true이면 터치 영역용 (투명, 전체 너비, 상호작용 가능), false이면 일반 바 (기본값: false)
     * @param customTooltipText 커스텀 툴팁 텍스트 목록 (null이면 기본 툴팁 사용)
     * @param showLabel 라벨 표시 여부 (기본값: false)
     * @param unit 단위 (기본값: "")
     */
    @Composable
    fun HorizontalBarMarker(
        data: List<BaseChartPoint>,
        minValues: List<Float>,
        maxValues: List<Float>,
        metrics: ChartMath.ChartMetrics,
        color: Color = Color.Black,
        barHeightRatio: Float = 0.5f,
        interactive: Boolean = true,
        onBarClick: ((Int, String) -> Unit)? = null,
        chartType: ChartType,
        showTooltipForIndex: Int? = null,
        isTouchArea: Boolean = false,
        customTooltipText: List<String>? = null,
//        showLabel: Boolean = false,  <- SleepStage에서는 필요없음, ProgressBar에서 사용
        unit: String = "",
    ) {
        val density = LocalDensity.current

        // 터치 영역용인 경우 자동으로 파라미터 설정
        val actualBarHeightRatio = if (isTouchArea) 1.0f else barHeightRatio
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

            // tooltipText is only used for onBarClick callback
            val tooltipText = customTooltipText?.getOrNull(index) ?: "Sleep Stage"

            // 바 너비와 위치 계산 (수평이므로 X축 방향으로 계산)
            val (barWidth, barX) = if (isTouchArea) {
                // 전체 차트 너비 사용 (터치 영역용)
                Pair(metrics.chartWidth, 0f)
            } else {
                // minValue에서 maxValue까지의 바 계산 (X축 방향)
                val xMinScreen = ((minValue - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartWidth
                val xMaxScreen = ((maxValue - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartWidth
                val width = xMaxScreen - xMinScreen
                Pair(width, xMinScreen)
            }

            // 바 Y 위치 계산 (수평이므로 Y축은 sleep stage ordinal 기반)
            val sleepStageOrdinal = data.getOrNull(index)?.x?.toInt() ?: 0
            val totalSleepStages = 4 // AWAKE, REM, LIGHT, DEEP
            val spacing = metrics.chartHeight / totalSleepStages
            val barHeight = spacing * actualBarHeightRatio
            val barY = metrics.paddingY + sleepStageOrdinal * spacing + (spacing - barHeight) / 2f

            // Float 좌표를 Dp로 변환
            val barXDp = with(density) { barX.toDp() }
            val barYDp = with(density) { barY.toDp() }
            val barWidthDp = with(density) { barWidth.toDp() }
            val barHeightDp = with(density) { barHeight.toDp() }

            // 툴팁 표시 여부 결정
            val shouldShowTooltip = when {
                isTouchArea -> false // 터치 영역용이므로 툴팁 표시 안함
                chartType in listOf(ChartType.RANGE_BAR, ChartType.STACKED_BAR) -> {
                    if (actualInteractive) {
                        clickedBarIndex == index
                    } else {
                        showTooltipForIndex == index
                    }
                }
                else -> false // 다른 차트 타입에서는 툴팁 표시 안함
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
                    }
            )
        }
        
        // 툴팁 표시 (바 박스 외부에 독립적으로 배치)
        if (tooltipData != null && tooltipOffset != null) {
            val xDp = with(density) { tooltipOffset.x.toDp() }
            val yDp = with(density) { tooltipOffset.y.toDp() }
            
            // Generate custom tooltip text for sleep stage charts
            val customTooltipText = if (tooltipData is RangeChartPoint) {
                val sleepStageOrdinal = tooltipData.x.toInt()
                val sleepStageName = when (sleepStageOrdinal) {
                    0 -> "Awake"
                    1 -> "REM"
                    2 -> "Light"
                    3 -> "Deep"
                    else -> "Unknown"
                }
                val startTime = SleepStageChartMath.formatTimeFromMilliseconds(tooltipData.minPoint.y)
                val endTime = SleepStageChartMath.formatTimeFromMilliseconds(tooltipData.maxPoint.y)
                "$startTime - $endTime"
            } else null
            
            ChartTooltip(
                chartPoint = tooltipData,
                unit = unit,
                customText = customTooltipText,
                modifier = Modifier.offset(x = xDp, y = yDp - 80.dp)
            )
        }
    }
}