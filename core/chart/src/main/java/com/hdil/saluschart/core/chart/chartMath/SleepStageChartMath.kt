package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.chartMath.ChartMath.ChartMetrics
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import com.hdil.saluschart.data.model.model.SleepStageType
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

object SleepStageChartMath {

    /**
     * Get color for each sleep stage type
     */
    fun getSleepStageColor(stageType: SleepStageType): Color {
        return when (stageType) {
            SleepStageType.AWAKE -> Color(0xFFFFD700) // Yellow for awake
            SleepStageType.REM -> Color(0xFF00CED1)   // Dark turquoise for REM
            SleepStageType.LIGHT -> Color(0xFF87CEEB) // Sky blue for light sleep
            SleepStageType.DEEP -> Color(0xFF191970)  // Midnight blue for deep sleep
            SleepStageType.UNKNOWN -> Color.Gray      // Gray for unknown
        }
    }
    
    /**
     * 밀리초를 시간 형식으로 변환합니다.
     * 
     * @param milliseconds 밀리초 값 (Double로 전달되어 정밀도 보장)
     * @param withDate true이면 "MM/DD HH:MM" 형식, false이면 "HH:MM" 형식
     * @return 시간 문자열
     */
    fun formatTimeFromMilliseconds(
        milliseconds: Double, 
        withDate: Boolean = true
    ): String {
        // Double을 반올림하여 Long으로 변환 (정밀도 손실 최소화)
        val millisecondsLong = kotlin.math.round(milliseconds).toLong()
        val instant = Instant.ofEpochMilli(millisecondsLong)
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        
        return if (withDate) {
            String.format(Locale.getDefault(), "%02d/%02d %02d:%02d",
                instant.atZone(ZoneId.systemDefault()).monthValue,
                instant.atZone(ZoneId.systemDefault()).dayOfMonth,
                localTime.hour, 
                localTime.minute
            )
        } else {
            String.format(Locale.getDefault(), "%02d:%02d",
                localTime.hour, 
                localTime.minute
            )
        }
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
     * 중간 정각 레이블을 계산합니다 (0~3개).
     * 수면 세션 시간에 따라 적절한 간격을 자동으로 결정합니다.
     *
     * @param startInstant 시작 시간
     * @param endInstant 끝 시간
     * @return 중간에 표시할 정각 시간 목록 (0~3개)
     */
    fun calculateIntermediateHourLabels(startInstant: Instant, endInstant: Instant): List<Instant> {
        // 전체 시간 길이 (시간 단위)
        val totalHours = ChronoUnit.HOURS.between(startInstant, endInstant)

        // 시간 길이에 따라 간격 결정
        val intervalHours = when {
            totalHours <= 2 -> return emptyList()  // 2시간 이하: 중간 레이블 없음
            totalHours <= 6 -> 2  // 2~6시간: 2시간 간격
            totalHours <= 9 -> 3  // 6~9시간: 3시간 간격
            totalHours <= 12 -> 4  // 9~12시간: 4시간 간격
            else -> 6  // 12시간 이상: 6시간 간격
        }

        // 시작 시간을 다음 정각으로 올림
        val startZdt = startInstant.atZone(ZoneId.systemDefault())
        val firstHour = startZdt.plusHours(1).truncatedTo(ChronoUnit.HOURS)

        // 끝 시간을 이전 정각으로 내림
        val endZdt = endInstant.atZone(ZoneId.systemDefault())
        val lastHour = endZdt.truncatedTo(ChronoUnit.HOURS)

        // 간격에 맞는 정각 시간 생성
        val labels = mutableListOf<Instant>()
        var currentHour = firstHour

        while (currentHour.isBefore(lastHour) || currentHour.isEqual(lastHour)) {
            // 시작 시간과 끝 시간이 아닌 경우만 추가
            if (currentHour.toInstant() != startInstant && currentHour.toInstant() != endInstant) {
                labels.add(currentHour.toInstant())
            }

            currentHour = currentHour.plusHours(intervalHours.toLong())
        }

        return labels
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