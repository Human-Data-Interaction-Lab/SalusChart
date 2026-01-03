package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawContext
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.BaseChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions

data class TooltipSpec(
    val chartMark: BaseChartMark,
    val offset: Offset
)

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
     * @param xLabelAutoSkip 라벨 자동 스킵 활성화 여부 (true이면 텍스트 너비 기반 자동 계산)
     */
    fun drawBarXAxisLabels(
        ctx: DrawContext,
        labels: List<String>,
        metrics: ChartMath.ChartMetrics,
        centered: Boolean = true,
        textSize: Float = 28f,
        maxXTicksLimit: Int? = null,
        xLabelAutoSkip: Boolean = false
    ) {
        // 라벨 감소 로직
        val (displayLabels, displayIndices) = if (xLabelAutoSkip) {
            // 자동 스킵: 텍스트 너비 기반으로 계산
            ChartMath.computeAutoSkipLabels(
                labels = labels,
                textSize = textSize,
                chartWidth = metrics.chartWidth,
                maxXTicksLimit = maxXTicksLimit
            )
        } else {
            // 모든 라벨 표시
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
     * @param showLabel 레이블 표시 여부 (기본값: false)
     * @param unit 단위 (기본값: "")
     */
    @Composable
    fun BarMarker(
        data: List<BaseChartMark>,
        minValues: List<Double>,
        maxValues: List<Double>,
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
        barCornerRadiusFraction: Float = 0f,
        barCornerRadiusFractions: BarCornerRadiusFractions? = null,
        roundTopOnly: Boolean = true,
        onTooltipSpec: ((TooltipSpec?) -> Unit)? = null,
    ) {
        val density = LocalDensity.current
        var tooltipSpec by remember { mutableStateOf<TooltipSpec?>(null) }
        var computedTooltip: TooltipSpec? = null
        // 터치 영역용인 경우 자동으로 파라미터 설정
        val actualBarWidthRatio = if (isTouchArea) 1.0f else barWidthRatio
        val actualInteractive = if (isTouchArea) true else interactive

        val dataSize = maxOf(minValues.size, maxValues.size)

        // 클릭된 바의 인덱스를 관리하는 상태 변수
        var clickedBarIndex by remember { mutableStateOf<Int?>(null) }

        // 툴팁 정보 저장 변수
        var tooltipOffset: Offset? = null
        var tooltipData: BaseChartMark? = null

        (0 until dataSize).forEach { index ->
            // 값 추출
            val minValue = minValues.getOrNull(index) ?: 0.0
            val maxValue = maxValues.getOrNull(index) ?: 0.0

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

                val total = dataSize

                // LineChart spacing rule: chartWidth / (N + 1)
                val spacing = if (total > 0) metrics.chartWidth / total else 0f

                val pointX = metrics.paddingX + (index + 0.5) * spacing

                // Touch strip width = full spacing
                val barW = spacing * actualBarWidthRatio

                // Center bar on the point
                val barXPos = pointX - barW / 2f

                Pair(barW, barXPos)
            } else {
                // 바차트 포지셔닝: 할당된 공간의 중앙에 배치
                val barW = metrics.chartWidth / dataSize * actualBarWidthRatio
                val spacing = metrics.chartWidth / dataSize
                val barXPos = metrics.paddingX + index * spacing + (spacing - barW) / 2f
                Pair(barW, barXPos)
            }

            // Double 좌표를 Dp로 변환
            val barXDp = with(density) { barX.toFloat().toDp() }
            val barYDp = with(density) { barY.toFloat().toDp() }
            val barWidthDp = with(density) { barWidth.toFloat().toDp() }
            val barHeightDp = with(density) { barHeight.toFloat().toDp() }

            // 툴팁 표시 여부 결정:
            // - isTouchArea = true인 경우 툴팁 표시 안함 (터치 영역용이므로)
            // - 바 차트 타입이 아닌 경우 툴팁 표시 안함 (LINE, SCATTERPLOT 등은 PointMarker 사용)
            val shouldShowTooltip = when {
                isTouchArea -> false // 터치 영역용이므로 툴팁 표시 안함
                chartType in listOf(ChartType.BAR, ChartType.RANGE_BAR, ChartType.STACKED_BAR) -> {
                    if (showTooltipForIndex != null) {
                        showTooltipForIndex == index
                    } else if (actualInteractive) {
                        clickedBarIndex == index
                    } else {
                        false
                    }
                }
                else -> false // LINE, SCATTERPLOT 등에서는 툴팁 표시 안함
            }

            if (shouldShowTooltip) {
                val anchorY = if (chartType == ChartType.STACKED_BAR) {
                    val totalValue = maxValues.getOrNull(index) ?: 0.0
                    val denom = (metrics.maxY - metrics.minY).takeIf { it != 0.0 } ?: 1.0
                    val yTopScreen =
                        metrics.chartHeight - ((totalValue - metrics.minY) / denom) * metrics.chartHeight
                    (metrics.paddingY + yTopScreen).toFloat()
                } else {
                    barY.toFloat()
                }

                computedTooltip = TooltipSpec(
                    chartMark = data[index],
                    offset = Offset(
                        x = barX.toFloat() + barWidth.toFloat() / 2f,
                        y = anchorY
                    )
                )
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

            // pixel sizes as Float (barWidth is already Float, barHeight is Double)
            val barWidthPx = barWidth.toFloat()
            val barHeightPx = barHeight.toFloat()

            // Helper: convert a "fraction of bar width" into a safe Dp radius
            fun fractionToCornerDp(fraction: Float): Dp {
                if (isTouchArea) return 0.dp
                if (fraction <= 0f) return 0.dp
                if (barHeightPx <= 0f || barWidthPx <= 0f) return 0.dp

                val radiusPx = (barWidthPx * fraction)
                    // Keep it sane: cannot exceed half of width or half of height
                    .coerceAtMost(barWidthPx / 2f)
                    .coerceAtMost(barHeightPx / 2f)

                return with(density) { radiusPx.toDp() }
            }

            // Choose per-corner fractions if provided; otherwise fall back to existing behavior
            val fractions = barCornerRadiusFractions ?: run {
                if (roundTopOnly) {
                    BarCornerRadiusFractions(
                        topStart = barCornerRadiusFraction,
                        topEnd = barCornerRadiusFraction,
                        bottomStart = 0f,
                        bottomEnd = 0f
                    )
                } else {
                    BarCornerRadiusFractions(
                        topStart = barCornerRadiusFraction,
                        topEnd = barCornerRadiusFraction,
                        bottomStart = barCornerRadiusFraction,
                        bottomEnd = barCornerRadiusFraction
                    )
                }
            }

            // Convert to Dp per corner
            val topStartDp = fractionToCornerDp(fractions.topStart)
            val topEndDp = fractionToCornerDp(fractions.topEnd)
            val bottomStartDp = fractionToCornerDp(fractions.bottomStart)
            val bottomEndDp = fractionToCornerDp(fractions.bottomEnd)

            // Build shape only if any corner > 0
            val hasAnyCorner =
                topStartDp > 0.dp || topEndDp > 0.dp || bottomStartDp > 0.dp || bottomEndDp > 0.dp

            val shape = if (!isTouchArea && hasAnyCorner) {
                RoundedCornerShape(
                    topStart = topStartDp,
                    topEnd = topEndDp,
                    bottomStart = bottomStartDp,
                    bottomEnd = bottomEndDp
                )
            } else null

            // 기본 modifier
            var barModifier = Modifier
                .offset(x = barXDp, y = barYDp)
                .size(width = barWidthDp, height = barHeightDp)

            barModifier =
                if (shape != null) {
                    barModifier
                        .clip(shape)
                        .background(color = actualColor, shape = shape)
                } else {
                    barModifier.background(color = actualColor)
                }

            barModifier = barModifier.clickable(enabled = (onBarClick != null)) {
                if (actualInteractive) {
                    clickedBarIndex = if (clickedBarIndex == index) null else index
                }
                onBarClick?.invoke(index, tooltipText)
            }

            Box(
                modifier = barModifier,
                contentAlignment = Alignment.TopCenter
            ) {
                // label 표시 여부 결정
                if (showLabel) {
                    Box(
                        modifier = Modifier.offset(0.dp, 0.dp) // 바 위에 표시
                    ) {
                        Text(
                            text = maxValue.toInt().toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        tooltipSpec = computedTooltip

        SideEffect {
            onTooltipSpec?.invoke(tooltipSpec)
        }
    }
}