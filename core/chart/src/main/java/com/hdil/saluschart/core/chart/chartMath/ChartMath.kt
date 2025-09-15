package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartPoint
import java.time.YearMonth
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

object ChartMath {

    var Pie = PieChartMath
    var Calendar = CalendarChartMath
    val RangeBar = RangeBarChartMath
    val Line = LineChartMath
    val Progress = ProgressChartMath

    /**
     * 차트 그리기에 필요한 메트릭 정보를 담는 데이터 클래스
     *
     * @param paddingLeftX 왼쪽 X축 패딩 값
     * @param paddingRightX 오른쪽 X축 패딩 값
     * @param paddingY Y축 패딩 값
     * @param chartWidth 차트의 실제 너비
     * @param chartHeight 차트의 실제 높이
     * @param minY Y축의 최소값
     * @param maxY Y축의 최대값
     * @param yTicks Y축에 표시할 눈금 값들
     */
    data class ChartMetrics(
        val paddingX: Float,
        val paddingY: Float,
        val chartWidth: Float,
        val chartHeight: Float,
        val minY: Float,
        val maxY: Float,
        val yTicks: List<Float>
    )

    /**
     * y-axis 눈금 값들을 계산합니다.
     * 1, 2, 5의 배수를 사용하여 시각적으로 깔끔한 눈금을 생성합니다.
     *
     * @param min 데이터의 최소값
     * @param max 데이터의 최대값
     * @param tickCount 원하는 눈금 개수 (기본값: 5)
     * @param chartType 차트 타입 (BAR/STACKED_BAR/MINIMAL_BAR일 경우 최소값을 0으로 강제)
     * @param actualMin 사용자 지정 최소 Y값 (지정시 데이터 범위를 확장)
     * @param actualMax 사용자 지정 최대 Y값 (지정시 데이터 범위를 확장)
     * @return 계산된 눈금 값들의 리스트
     */
    fun computeNiceTicks(
        min: Float, 
        max: Float, 
        tickCount: Int = 5, 
        chartType: ChartType? = null,
        actualMin: Float? = null,
        actualMax: Float? = null
    ): List<Float> {
        if (min >= max) {
            return listOf(0f, 1f)
        }
        
        // 바 차트의 경우 최소값을 0으로 강제 설정
        var min = if (chartType == ChartType.BAR ||
                             chartType == ChartType.STACKED_BAR || 
                             chartType == ChartType.MINIMAL_BAR) {
            0f
        } else {
            min
        }
        
        val rawStep = (max - min) / tickCount.toDouble()
        val power = 10.0.pow(floor(log10(rawStep)))
        val candidates = listOf(1.0, 2.0, 5.0).map { it * power }
        val step = candidates.minByOrNull { abs(it - rawStep) } ?: power

        val niceMin = floor(min / step) * step
        val niceMax = ceil(max / step) * step

        // 사용자 지정 범위가 있으면 항상 우선 사용 (확장 또는 축소 모두 허용)
        val finalMin = actualMin?.toDouble() ?: niceMin
        val finalMax = actualMax?.toDouble() ?: niceMax

        // 최종 범위에 대해 ticks 생성
        val ticks = mutableListOf<Float>()
        
        // 사용자 지정 최소값이 있으면 먼저 추가
        actualMin?.let { userMin ->
            ticks.add(userMin)
        }
        
        // step에 따른 nice ticks 추가
        var t = if (actualMin != null) {
            // 사용자 최소값 다음부터 step 단위로 시작
            ceil(finalMin / step) * step
        } else {
            finalMin
        }
        
        while (t <= finalMax + 1e-6) {
            val roundedTick = round(t * 1000000) / 1000000
            val tickValue = roundedTick.toFloat()
            
            // 사용자 지정 값과 중복되지 않는 경우만 추가
            if (actualMin == null || abs(tickValue - actualMin) > 1e-6) {
                if (actualMax == null || abs(tickValue - actualMax) > 1e-6) {
                    ticks.add(tickValue)
                }
            }
            t += step
        }
        
        // 사용자 지정 최대값이 있으면 마지막에 추가
        actualMax?.let { userMax ->
            ticks.add(userMax)
        }

        return ticks.distinct().sorted()
    }

    /**
     * 차트 그리기에 필요한 메트릭 값을 계산합니다.
     *
     * @param size Canvas의 전체 크기
     * @param values 차트에 표시할 Y축 데이터 값 목록
     * @param tickCount 원하는 Y축 눈금 개수 (기본값: 5)
     * @param chartType 차트 타입 (BAR/STACKED_BAR 타입일 경우 기본적으로 minY를 0으로 설정)
     * @param isMinimal 미니멀 차트 모드인지 여부 (기본값: false)
     * @param paddingLeftX 왼쪽 X축 패딩 값 (기본값: normal=30f, minimal=4f)
     * @param paddingRightX 오른쪽 X축 패딩 값 (기본값: normal=30f, minimal=4f)
     * @param paddingY Y축 패딩 값 (기본값: normal=40f, minimal=8f)
     * @param minY 사용자 지정 최소 Y값 (지정시 바 차트의 기본 동작을 오버라이드)
     * @param maxY 사용자 지정 최대 Y값 (지정시 nice ticks보다 우선적용)
     * @return 차트 메트릭 객체
     */
    fun computeMetrics(
        size: Size,
        values: List<Float>,
        tickCount: Int = 5,
        chartType: ChartType? = null,
        isMinimal: Boolean = false,
        paddingX: Float = if (isMinimal) 4f else 30f,
        paddingY: Float = if (isMinimal) 8f else 40f,
        minY: Float? = null,
        maxY: Float? = null,

        includeYAxisPadding: Boolean = true,
        // defaults to current paddingX
        yAxisPaddingPx: Float = paddingX,
        // force a constant tick step (e.g., 10f)
        fixedTickStep: Float? = null
    ): ChartMetrics {
        val effectivePaddingX = if (includeYAxisPadding) yAxisPaddingPx else 0f

        // data range
        val dataMax = values.maxOrNull() ?: 1f
        val dataMin = values.minOrNull() ?: 0f

        // decide min/max used for ticks
        val wantsZeroMin = (chartType == ChartType.BAR ||
                chartType == ChartType.STACKED_BAR ||
                chartType == ChartType.MINIMAL_BAR)

        val baseMin = minY ?: if (wantsZeroMin) 0f else dataMin
        val baseMax = maxY ?: dataMax

        // ticks
        val yTicks: List<Float>
        val actualMinY: Float
        val actualMaxY: Float

        if (fixedTickStep != null && fixedTickStep > 0f) {
            val start = if (wantsZeroMin) 0f else kotlin.math.floor(baseMin / fixedTickStep) * fixedTickStep
            val end = kotlin.math.ceil(baseMax / fixedTickStep) * fixedTickStep
            val ticks = mutableListOf<Float>()
            var t = start
            while (t <= end + 1e-6f) {
                ticks.add(t)
                t += fixedTickStep
            }
            yTicks = ticks
            actualMinY = if (minY != null) minY else start
            actualMaxY = if (maxY != null) maxY else end
        } else {
            val ticksNice = computeNiceTicks(dataMin, dataMax, tickCount, chartType, actualMin = minY, actualMax = maxY)
            yTicks = ticksNice
            actualMinY = minY ?: (ticksNice.minOrNull() ?: dataMin)
            actualMaxY = maxY ?: (ticksNice.maxOrNull() ?: dataMax)
        }

        val chartWidth  = size.width  - effectivePaddingX * 2f
        val chartHeight = size.height - paddingY

        return ChartMetrics(
            paddingX = effectivePaddingX,
            paddingY = paddingY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            minY = actualMinY,
            maxY = actualMaxY,
            yTicks = yTicks
        )
    }

    /**
     * X축 라벨을 제한된 수로 줄입니다.
     * 너무 많은 라벨이 있으면 겹치거나 텍스트가 너무 작아질 수 있으므로
     * 최대 개수를 제한하여 적절한 간격으로 표시합니다.
     *
     * @param labels 원본 X축 라벨 목록
     * @param maxXTicksLimit X축에 표시할 최대 라벨 개수 (기본값: 10)
     * @return 감소된 라벨 목록과 해당 인덱스 목록의 Pair
     */
    fun reduceXAxisTicks(labels: List<String>, maxXTicksLimit: Int = 10): Pair<List<String>, List<Int>> {
        // 1. 초기 틱 - 모든 후보 틱으로 시작
        if (labels.size <= maxXTicksLimit) {
            // 라벨이 충분히 적으면 모든 라벨을 표시
            return Pair(labels, labels.indices.toList())
        }
        
        // 2. maxTicksLimit 체크 - 라벨이 너무 많으면 솎아내기 시작
        // 3. 솎아내기 팩터 계산
        val skipRatio = ceil(labels.size.toDouble() / maxXTicksLimit).toInt()
        
        val reducedLabels = mutableListOf<String>()
        val reducedIndices = mutableListOf<Int>()
        
        // 4. 첫 번째와 마지막 라벨은 항상 유지하여 축의 경계를 명확히 함
        // 첫 번째 라벨 추가
        reducedLabels.add(labels[0])
        reducedIndices.add(0)
        
        // skipRatio 간격으로 중간 라벨들 추가
        for (i in skipRatio until labels.size - 1 step skipRatio) {
            reducedLabels.add(labels[i])
            reducedIndices.add(i)
        }
        
        // 마지막 라벨 추가 (첫 번째 라벨과 다른 경우에만)
        if (labels.size > 1 && reducedIndices.last() != labels.size - 1) {
            reducedLabels.add(labels.last())
            reducedIndices.add(labels.size - 1)
        }
        
        return Pair(reducedLabels, reducedIndices)
    }

    /**
     * 데이터 포인트를 화면 좌표로 변환합니다.
     *
     * @param data 차트 데이터 포인트 목록
     * @param size Canvas의 전체 크기
     * @param metrics 차트 메트릭 정보
     * @return 화면 좌표로 변환된 Offset 목록
     */
    fun mapToCanvasPoints(data: List<ChartPoint>, size: Size, metrics: ChartMetrics): List<Offset> {
        val spacing = metrics.chartWidth / (data.size - 1)
        return data.mapIndexed { i, point ->
            val x = metrics.paddingX + i * spacing
            val y = metrics.chartHeight - ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
            Offset(x, y)
        }
    }
}