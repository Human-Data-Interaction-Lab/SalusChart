package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartMark
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
    val Scatter = ScatterPlotMath
    val Progress = ProgressChartMath
    val SleepStage = SleepStageChartMath

    /**
     * Y축 범위 정보를 담는 데이터 클래스 (픽셀 계산 없이 순수한 데이터 범위만)
     *
     * @param minY Y축의 최소값
     * @param maxY Y축의 최대값
     * @param yTicks Y축에 표시할 눈금 값들
     */
    data class YAxisRange(
        val minY: Double,
        val maxY: Double,
        val yTicks: List<Double>
    ) {
        /**
         * Y축 눈금 간격을 계산합니다 (yTicks에서 추출)
         * @return 눈금 간격, yTicks가 2개 미만이면 10.0 반환
         */
        val tickStep: Double
            get() = if (yTicks.size >= 2) yTicks[1] - yTicks[0] else 10.0
    }

    /**
     * 차트 그리기에 필요한 메트릭 정보를 담는 데이터 클래스
     *
     * @param paddingX X축 패딩 값
     * @param paddingY Y축 패딩 값
     * @param chartWidth 차트의 실제 너비
     * @param chartHeight 차트의 실제 높이
     * @param yAxisRange Y축 범위 정보 (minY, maxY, yTicks 포함)
     */
    data class ChartMetrics(
        val paddingX: Float,
        val paddingY: Float,
        val chartWidth: Float,
        val chartHeight: Float,
        val yAxisRange: YAxisRange
    ) {
        val minY: Double get() = yAxisRange.minY
        val maxY: Double get() = yAxisRange.maxY
        val yTicks: List<Double> get() = yAxisRange.yTicks
    }

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
        min: Double,
        max: Double,
        tickCount: Int = 5,
        chartType: ChartType? = null,
        actualMin: Double? = null,
        actualMax: Double? = null
    ): List<Double> {
        if (min >= max) {
            return listOf(0.0, 1.0)
        }
        
        // 바 차트의 경우 최소값을 0으로 강제 설정
        var min = if (chartType == ChartType.BAR ||
                             chartType == ChartType.STACKED_BAR || 
                             chartType == ChartType.MINIMAL_BAR) {
            0.0
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
        val ticks = mutableListOf<Double>()
        
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
            val tickValue = roundedTick
            
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
     * Y축 범위와 눈금을 계산합니다 (픽셀 계산 없이 순수한 데이터 범위만).
     * 이 함수는 페이징 모드에서 통일된 Y축 범위를 미리 계산할 때 유용합니다.
     *
     * @param values 차트에 표시할 Y축 데이터 값 목록
     * @param chartType 차트 타입 (BAR/STACKED_BAR 타입일 경우 기본적으로 minY를 0으로 설정)
     * @param minY 사용자 지정 최소 Y값
     * @param maxY 사용자 지정 최대 Y값
     * @param fixedTickStep 고정 눈금 간격 (지정시 nice ticks 대신 사용)
     * @param tickCount 원하는 Y축 눈금 개수 (fixedTickStep이 null일 때만 사용, 기본값: 5)
     * @return Y축 범위 객체 (minY, maxY, yTicks)
     */
    fun computeYAxisRange(
        values: List<Double>,
        chartType: ChartType? = null,
        minY: Double? = null,
        maxY: Double? = null,
        fixedTickStep: Double? = null,
        tickCount: Int = 5
    ): YAxisRange {
        // data range
        val dataMax = values.maxOrNull() ?: 1.0
        val dataMin = values.minOrNull() ?: 0.0

        // decide if we want zero-based min
        val wantsZeroMin = (chartType == ChartType.BAR ||
                chartType == ChartType.STACKED_BAR ||
                chartType == ChartType.MINIMAL_BAR)

        val baseMin = minY ?: if (wantsZeroMin) 0.0 else dataMin
        val baseMax = maxY ?: dataMax

        // compute ticks
        val yTicks: List<Double>
        val actualMinY: Double
        val actualMaxY: Double

        if (fixedTickStep != null && fixedTickStep > 0.0) {
            val start = if (wantsZeroMin) 0.0 else kotlin.math.floor(baseMin / fixedTickStep) * fixedTickStep
            val end = kotlin.math.ceil(baseMax / fixedTickStep) * fixedTickStep
            val ticks = mutableListOf<Double>()
            var t = start
            while (t <= end + 1e-6) {
                ticks.add(t)
                t += fixedTickStep
            }
            yTicks = ticks
            actualMinY = if (minY != null) minY else start
            actualMaxY = if (maxY != null) maxY else end
        } else {
            // Use baseMin/baseMax instead of dataMin/dataMax to respect chart type requirements
            val ticksNice = computeNiceTicks(baseMin, baseMax, tickCount, chartType, actualMin = minY, actualMax = maxY)
            yTicks = ticksNice
            actualMinY = minY ?: (ticksNice.minOrNull() ?: baseMin)
            actualMaxY = maxY ?: (ticksNice.maxOrNull() ?: baseMax)
        }

        return YAxisRange(
            minY = actualMinY,
            maxY = actualMaxY,
            yTicks = yTicks
        )
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
        values: List<Double>,
        tickCount: Int = 5,
        chartType: ChartType? = null,
        isMinimal: Boolean = false,
        paddingX: Float = if (isMinimal) 4f else 30f,
        paddingY: Float = if (isMinimal) 8f else 40f,
        minY: Double? = null,
        maxY: Double? = null,

        includeYAxisPadding: Boolean = true,
        // defaults to current paddingX
        yAxisPaddingPx: Float = paddingX,
        // force a constant tick step (e.g., 10.0)
        fixedTickStep: Double? = null
    ): ChartMetrics {
        val effectivePaddingX = if (includeYAxisPadding) yAxisPaddingPx else 0f

        // Call computeYAxisRange to calculate Y-axis 
        val yAxisRange = computeYAxisRange(
            values = values,
            chartType = chartType,
            minY = minY,
            maxY = maxY,
            fixedTickStep = fixedTickStep,
            tickCount = tickCount
        )

        val chartWidth  = size.width  - effectivePaddingX * 2f
        val chartHeight = size.height - paddingY

        return ChartMetrics(
            paddingX = effectivePaddingX,
            paddingY = paddingY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            yAxisRange = yAxisRange
        )
    }

    /**
     * X축 라벨을 자동으로 측정하여 겹치지 않도록 감소시킵니다.
     * 실제 텍스트 너비를 측정하여 차트 너비에 맞는 적절한 간격(skipRatio)을 계산합니다.
     *
     * @param labels 원본 X축 라벨 목록
     * @param textSize 라벨 텍스트 크기 (픽셀)
     * @param chartWidth 차트의 실제 너비 (픽셀)
     * @param maxXTicksLimit 최대 라벨 개수 제한 (null이면 제한 없음)
     * @return 감소된 라벨 목록과 해당 인덱스 목록의 Pair
     */
    fun computeAutoSkipLabels(
        labels: List<String>,
        textSize: Float,
        chartWidth: Float,
        maxXTicksLimit: Int? = null
    ): Pair<List<String>, List<Int>> {
        if (labels.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }
        
        if (labels.size == 1) {
            return Pair(labels, listOf(0))
        }

        // 1. Measure label widths using Paint
        val paint = android.graphics.Paint().apply {
            this.textSize = textSize
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        val labelWidths = labels.map { label ->
            paint.measureText(label)
        }
        
        // 2. Calculate average label width
        val avgLabelWidth = labelWidths.average().toFloat()
        
        // 3. Calculate capacity: how many labels can fit
        // Each label needs its width + padding on both sides
        val autoSkipPadding = textSize * 0.3f // 30% of text size as padding
        val spacePerLabel = avgLabelWidth + autoSkipPadding
        val estimatedCapacity = (chartWidth / spacePerLabel).toInt().coerceAtLeast(1)
        
        // 4. Apply maxXTicksLimit constraint if provided
        val finalCapacity = if (maxXTicksLimit != null) {
            minOf(estimatedCapacity, maxXTicksLimit)
        } else {
            estimatedCapacity
        }
        
        // 5. If all labels fit, return all of them
        if (labels.size <= finalCapacity) {
            return Pair(labels, labels.indices.toList())
        }
        
        // 6. Calculate skip ratio (interval)
        val skipRatio = ceil(labels.size.toDouble() / finalCapacity).toInt()
        
        // 7. Select labels with the calculated interval
        val reducedLabels = mutableListOf<String>()
        val reducedIndices = mutableListOf<Int>()
        
        // Always include the first label
        reducedLabels.add(labels[0])
        reducedIndices.add(0)
        
        // Add labels at skipRatio intervals
        for (i in skipRatio until labels.size step skipRatio) {
            reducedLabels.add(labels[i])
            reducedIndices.add(i)
        }
        
        return Pair(reducedLabels, reducedIndices)
    }
}