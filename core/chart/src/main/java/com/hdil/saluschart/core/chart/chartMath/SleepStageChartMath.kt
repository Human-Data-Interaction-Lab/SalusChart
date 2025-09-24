package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.chartMath.ChartMath.ChartMetrics
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

object SleepStageChartMath {
    
    /**
     * 밀리초를 시간 형식(HH:MM:SS)으로 변환합니다.
     * 
     * @param milliseconds 밀리초 값 (Double로 전달되어 정밀도 보장)
     * @return "HH:MM:SS" 형식의 시간 문자열 (소수점 제거)
     */
    fun formatTimeFromMilliseconds(milliseconds: Double): String {
        // Double을 반올림하여 Long으로 변환 (정밀도 손실 최소화)
        val millisecondsLong = kotlin.math.round(milliseconds).toLong()
        val instant = Instant.ofEpochMilli(millisecondsLong)
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", 
            localTime.hour, 
            localTime.minute, 
            localTime.second
        )
    }
    
    /**
     * 수면 단계 차트를 위한 메트릭을 계산합니다.
     * 수면 세션의 시작시간과 종료시간을 기반으로 간단한 메트릭을 생성합니다.
     *
     * @param size Canvas의 전체 크기
     * @param sleepSession 수면 세션 데이터
     * @param paddingX X축 패딩 값 (기본값: 30f)
     * @param paddingY Y축 패딩 값 (기본값: 40f)
     * @return 차트 메트릭 객체
     */
    fun computeSleepStageMetrics(
        size: Size,
        sleepSession: SleepSession,
        paddingX: Float = 30f,
        paddingY: Float = 40f
    ): ChartMetrics {
        val chartWidth = size.width - paddingX * 2
        val chartHeight = size.height - paddingY

        // 수면 세션의 시간 범위를 밀리초로 변환 (Double로 정밀도 보장)
        val startTimeMs = sleepSession.startTime.toEpochMilli().toDouble()
        val endTimeMs = sleepSession.endTime.toEpochMilli().toDouble()

        return ChartMetrics(
            paddingX = paddingX,
            paddingY = paddingY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            minY = startTimeMs, // X축 시간 범위의 시작
            maxY = endTimeMs,   // X축 시간 범위의 끝
            yTicks = emptyList() // 수면 단계 차트는 틱이 필요 없음
        )
    }

    /**
     * Sleep stage specific transformation: converts SleepStage objects to RangeChartPoints
     * Each SleepStage becomes a RangeChartPoint where:
     * - x = sleep stage type ordinal (for Y-axis positioning)
     * - minPoint = start time ChartPoint (x = stage ordinal, y = start time)
     * - maxPoint = end time ChartPoint (x = stage ordinal, y = end time)
     * 
     * The x value and y value for minPoint/maxPoint are switched when inputted into HorizontalBarMarker
     * 
     * @param List<SleepStage> List of SleepStage objects
     * @return List of RangeChartPoints for sleep stage chart
     */
    fun List<SleepStage>.toSleepStageRangeChartPoints(): List<RangeChartPoint> {
        return mapIndexed { index, stage ->
            val stageOrdinal = stage.stage.ordinal.toDouble()
            // Use Double to preserve precision throughout the conversion
            val startTimeMs = stage.startTime.toEpochMilli().toDouble()
            val endTimeMs = stage.endTime.toEpochMilli().toDouble()
            
            RangeChartPoint(
                x = stageOrdinal, // Sleep stage type for Y-axis positioning
                minPoint = ChartPoint(
                    x = stageOrdinal, 
                    y = startTimeMs, 
                    label = "Stage ${index + 1}"
                ),
                maxPoint = ChartPoint(
                    x = stageOrdinal, 
                    y = endTimeMs, 
                    label = "Stage ${index + 1}"
                ),
                label = stage.stage.name
            )
        }
    }
}