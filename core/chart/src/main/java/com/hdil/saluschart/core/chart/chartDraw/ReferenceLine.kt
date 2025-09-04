package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.BaseChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.StackedChartPoint
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 기준선 타입을 나타내는 enum class
 */
enum class ReferenceLineType {
    NONE,      // 기준선 없음
    AVERAGE,   // 평균선
    TREND      // 추세선
}

/**
 * 기준선 스타일을 나타내는 enum class
 */
enum class LineStyle(internal val dashPattern: FloatArray?) {
    SOLID(null),                              // 실선
    DASHED(floatArrayOf(10f, 5f)),           // 짧은 점선 
    DOTTED(floatArrayOf(2f, 5f)),            // 점선
    DASHDOT(floatArrayOf(10f, 5f, 2f, 5f)),  // 점-선 혼합
    LONGDASH(floatArrayOf(20f, 10f))         // 긴 점선
}



/**
 * 기준선을 그리고 관리하는 객체
 */
object ReferenceLine {

    /**
     * 차트 데이터에서 평균값을 계산합니다.
     * 
     * @param data 차트 데이터 포인트 목록
     * @param chartType 차트 타입 (스택 바 차트의 경우 총합의 평균을 계산)
     * @return 계산된 평균값 (정수로 반올림됨)
     */
    fun calculateAverage(data: List<BaseChartPoint>, chartType: ChartType): Float {
        if (data.isEmpty()) return 0f

        return when (chartType) {
            ChartType.STACKED_BAR -> {
                // 스택 바 차트: 각 스택의 총합의 평균
                val stackedData = data.filterIsInstance<StackedChartPoint>()
                if (stackedData.isEmpty()) {
                    data.map { it.y }.average().roundToInt().toFloat()
                } else {
                    stackedData.map { it.total }.average().roundToInt().toFloat()
                }
            }
            else -> {
                // 일반 차트: y 값들의 평균
                data.map { it.y }.average().roundToInt().toFloat()
            }
        }
    }

    /**
     * 추세선 계산을 위한 선형 회귀
     * 
     * @param data 차트 데이터 포인트 목록
     * @return 추세선의 기울기와 y절편을 포함한 Pair (slope, intercept)
     */
    private fun calculateTrendLine(data: List<BaseChartPoint>): Pair<Float, Float> {
        if (data.size < 2) return Pair(0f, 0f)

        // 선형 회귀를 이용한 추세선 계산
        val n = data.size
        val sumX = data.sumOf { it.x.toDouble() }
        val sumY = data.sumOf { it.y.toDouble() }
        val sumXY = data.sumOf { it.x.toDouble() * it.y.toDouble() }
        val sumXSquared = data.sumOf { it.x.toDouble().pow(2) }

        val slope = ((n * sumXY - sumX * sumY) / (n * sumXSquared - sumX.pow(2))).toFloat()
        val intercept = ((sumY - slope * sumX) / n).toFloat()

        return Pair(slope, intercept)
    }

    /**
     * 기준선을 그립니다.
     * 
     * @param modifier Compose 모디파이어
     * @param data 차트 데이터 포인트 목록
     * @param metrics 차트 메트릭 정보
     * @param chartType 차트 타입
     * @param referenceLineType 기준선 타입
     * @param color 기준선 색상
     * @param strokeWidth 기준선 두께
     * @param lineStyle 기준선 스타일 (실선, 점선, 점-선 등)
     * @param showLabel 기준선 값 레이블 표시 여부 (평균선만 해당)
     * @param labelFormat 레이블 값 포맷 (예: "평균: %.0f")
     * @param yAxisPosition Y축 위치 (레이블 위치 결정용)
     * @param interactive 터치 상호작용 가능 여부
     * @param onClick 기준선 클릭 시 콜백
     */
    @Composable
    fun ReferenceLine(
        modifier: Modifier = Modifier,
        data: List<BaseChartPoint>,
        metrics: ChartMath.ChartMetrics,
        chartType: ChartType,
        referenceLineType: ReferenceLineType,
        color: Color = Color.Red,
        strokeWidth: Dp = 2.dp,
        lineStyle: LineStyle = LineStyle.DASHED,
        showLabel: Boolean = true,
        labelFormat: String = "평균: %.0f",
        yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
        interactive: Boolean = false,
        onClick: (() -> Unit)? = null,
    ) {
        if (referenceLineType == ReferenceLineType.NONE || data.isEmpty()) return

        Box(modifier = modifier.fillMaxSize()) {
            when (referenceLineType) {
                ReferenceLineType.AVERAGE -> {
                    AverageLine(
                        data = data,
                        metrics = metrics,
                        chartType = chartType,
                        color = color,
                        strokeWidth = strokeWidth,
                        lineStyle = lineStyle,
                        showLabel = showLabel,
                        labelFormat = labelFormat,
                        yAxisPosition = yAxisPosition,
                        interactive = interactive,
                        onClick = onClick,
                    )
                }
                ReferenceLineType.TREND -> {
                    TrendLine(
                        data = data,
                        metrics = metrics,
                        color = color,
                        strokeWidth = strokeWidth,
                        lineStyle = lineStyle,
                        interactive = interactive,
                        onClick = onClick,
                        showLabel = showLabel,
                        labelFormat = labelFormat
                    )
                }
                ReferenceLineType.NONE -> {
                    // 아무것도 그리지 않음
                }
            }
        }
    }

    /**
     * 평균선을 Compose로 그립니다.
     */
    @Composable
    private fun AverageLine(
        data: List<BaseChartPoint>,
        metrics: ChartMath.ChartMetrics,
        chartType: ChartType,
        color: Color,
        strokeWidth: Dp,
        lineStyle: LineStyle,
        showLabel: Boolean,
        labelFormat: String,
        yAxisPosition: YAxisPosition,
        interactive: Boolean,
        onClick: (() -> Unit)?
    ) {
        val average = calculateAverage(data, chartType)
        
        // 평균값이 차트 범위를 벗어나면 그리지 않음
        if (average < metrics.minY || average > metrics.maxY) return
        
        val density = LocalDensity.current
        val interactionSource = remember { MutableInteractionSource() }
        
        // Y축 좌표 계산
        val y = metrics.chartHeight - ((average - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
        
        Box(modifier = Modifier.fillMaxSize()) {
            // 기준선 Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (interactive) {
                            Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onClick?.invoke()
                            }
                        } else Modifier
                    )
            ) {
                val startX = metrics.paddingX
                val endX = metrics.paddingX + metrics.chartWidth

                // 점선 효과 설정
                val pathEffect = lineStyle.dashPattern?.let {
                    PathEffect.dashPathEffect(it, 0f)
                }

                // 평균선 그리기
                drawLine(
                    color = color,
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = strokeWidth.toPx(),
                    pathEffect = pathEffect
                )
            }
        }
        if (showLabel) {
            Box(modifier = Modifier.fillMaxSize()) {
                ReferenceLineLabel(
                    value = average,
                    yPosition = y,
                    color = color,
                    labelFormat = labelFormat,
                    metrics = metrics,
                    yAxisPosition = yAxisPosition,
                    interactive = interactive,
                    onClick = onClick
                )
            }
        }
    }

    /**
     * 추세선을 Compose로 그립니다.
     */
    @Composable
    private fun TrendLine(
        data: List<BaseChartPoint>,
        metrics: ChartMath.ChartMetrics,
        color: Color,
        strokeWidth: Dp,
        lineStyle: LineStyle,
        showLabel: Boolean,
        labelFormat: String,
        interactive: Boolean,
        onClick: (() -> Unit)?,
    ) {
        val (slope, intercept) = calculateTrendLine(data)
        val density = LocalDensity.current
        val interactionSource = remember { MutableInteractionSource() }
        
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (interactive) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onClick?.invoke()
                        }
                    } else Modifier
                )
        ) {
            // 추세선의 시작점과 끝점 계산
            val startX = metrics.paddingX
            val endX = metrics.paddingX + metrics.chartWidth
            
            // X 좌표를 데이터 좌표계로 변환
            val dataStartX = if (data.isNotEmpty()) data.first().x else 0f
            val dataEndX = if (data.isNotEmpty()) data.last().x else 1f
            
            val startY = slope * dataStartX + intercept
            val endY = slope * dataEndX + intercept
            
            // Y 좌표를 화면 좌표계로 변환
            val screenStartY = metrics.chartHeight - ((startY - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
            val screenEndY = metrics.chartHeight - ((endY - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
            
            // 점선 효과 설정
            val pathEffect = lineStyle.dashPattern?.let { 
                PathEffect.dashPathEffect(it, 0f) 
            }
            
            // 추세선 그리기
            drawLine(
                color = color,
                start = Offset(startX, screenStartY),
                end = Offset(endX, screenEndY),
                strokeWidth = strokeWidth.toPx(),
                pathEffect = pathEffect
            )
        }
    }
    
    /**
     * 기준선의 값 레이블을 표시합니다.
     */
    @Composable
    private fun ReferenceLineLabel(
        value: Float,
        yPosition: Float,
        color: Color,
        labelFormat: String,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition,
        interactive: Boolean,
        onClick: (() -> Unit)?
    ) {
        val density = LocalDensity.current
        val labelText = labelFormat.format(value)
        
        // Y축 위치에 따라 레이블 X 위치 결정
        val labelX = when (yAxisPosition) {
            YAxisPosition.LEFT -> {
                // 왼쪽 Y축: 레이블을 Y축 왼쪽에 배치
                with(density) { 10.dp.toPx() }
            }
            YAxisPosition.RIGHT -> {
                // 오른쪽 Y축: 레이블을 Y축 오른쪽에 배치  
                metrics.paddingX + metrics.chartWidth + with(density) { 10.dp.toPx() }
            }
        }
        
        // 화면 경계 내에서 위치 조정
        val adjustedX = with(density) {
            labelX.toDp().coerceAtMost((metrics.paddingX + metrics.chartWidth + 100f).toDp())
        }
        val adjustedY = with(density) {
            yPosition.toDp().coerceIn(20.dp, (metrics.chartHeight - 20f).toDp())
        }
        
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = adjustedX.roundToPx(),
                        y = (adjustedY - 12.dp).roundToPx() // 텍스트 중앙 정렬을 위한 조정
                    )
                }
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                )
                .clip(RoundedCornerShape(4.dp))
                .then(
                    if (interactive) {
                        Modifier.clickable {
                            onClick?.invoke()
                        }
                    } else Modifier
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = labelText,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}