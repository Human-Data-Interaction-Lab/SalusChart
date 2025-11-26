package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
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

        // Y축 범위 생성 (수면 단계 차트는 틱이 필요 없음)
        val yAxisRange = ChartMath.YAxisRange(
            minY = startTimeMs,    // X축 시간 범위의 시작
            maxY = endTimeMs,      // X축 시간 범위의 끝
            yTicks = emptyList()   // 수면 단계 차트는 틱이 필요 없음
        )

        return ChartMetrics(
            paddingX = paddingX,
            paddingY = paddingY,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            yAxisRange = yAxisRange
        )
    }

    /**
     * 수면 단계 데이터를 RangeChartMark로 변환합니다.
     * 각 SleepStage는 다음과 같이 RangeChartMark로 변환됩니다:
     * - x = 수면 단계 타입의 순서 값 (Y축 포지셔닝용)
     * - minPoint = 시작 시간 ChartMark (x = 단계 순서값, y = 시작 시간)
     * - maxPoint = 종료 시간 ChartMark (x = 단계 순서값, y = 종료 시간)
     * minPoint/maxPoint의 x값과 y값은 HorizontalBarMarker에 입력될 때 서로 바뀝니다.
     * 
     * @param List<SleepStage> 수면 단계 객체 목록
     * @return 수면 단계 차트용 RangeChartMark 목록
     */
    fun List<SleepStage>.toSleepStageRangeChartMarks(): List<RangeChartMark> {
        return mapIndexed { index, stage ->

            // Map stages to Y positions (index)
            val stageIndex = when(stage.stage) {
                SleepStageType.DEEP  -> 0.0
                SleepStageType.LIGHT -> 1.0   // Apple Core = Light
                SleepStageType.REM   -> 2.0
                SleepStageType.AWAKE -> 3.0
                SleepStageType.UNKNOWN -> TODO()
            }

            val startTime = stage.startTime.toEpochMilli().toDouble()
            val endTime = stage.endTime.toEpochMilli().toDouble()

            RangeChartMark(
                x = startTime,   // Not used in our custom chart but keep API
                minPoint = ChartMark(
                    x = startTime,
                    y = stageIndex,
                    label = stage.stage.name
                ),
                maxPoint = ChartMark(
                    x = endTime,
                    y = stageIndex,
                    label = stage.stage.name
                ),
                label = stage.stage.name
            )
        }
    }

    /**
     * 중간 정각 레이블을 자동으로 생성하고 측정하여 겹치지 않도록 필터링합니다.
     * 시작과 끝 사이의 모든 정각 시간을 생성한 후, 텍스트 너비를 측정하여 겹치지 않는 레이블만 선택합니다.
     * 
     * @param startInstant 시작 시간
     * @param endInstant 끝 시간
     * @param textSize 레이블 텍스트 크기 (픽셀)
     * @param chartWidth 차트의 실제 너비 (픽셀)
     * @param showStartEndLabels 시작/끝 레이블 표시 여부
     * @return 겹치지 않는 중간 레이블 목록
     */

     // 추가 정보는 노션 페이지 'Enhanced Readability Algorithm' 참고 (computeAutoSkipLabels와 유사)
    fun computeNonOverlappingIntermediateLabels(
        startInstant: Instant,
        endInstant: Instant,
        textSize: Float,
        chartWidth: Float,
        showStartEndLabels: Boolean
    ): List<Instant> {
        // 1. Generate all possible intermediate hour labels (every hour on the hour)
        val startZdt = startInstant.atZone(ZoneId.systemDefault())
        val endZdt = endInstant.atZone(ZoneId.systemDefault())
        
        // Round up to next hour for first intermediate label
        val firstHour = startZdt.plusHours(1).truncatedTo(ChronoUnit.HOURS)
        // Round down to previous hour for last intermediate label
        val lastHour = endZdt.truncatedTo(ChronoUnit.HOURS)
        
        val allIntermediateLabels = mutableListOf<Instant>()
        var currentHour = firstHour
        
        while (currentHour.isBefore(lastHour) || currentHour.isEqual(lastHour)) {
            // Don't include start or end time
            if (currentHour.toInstant() != startInstant && currentHour.toInstant() != endInstant) {
                allIntermediateLabels.add(currentHour.toInstant())
            }
            currentHour = currentHour.plusHours(1)
        }
        
        if (allIntermediateLabels.isEmpty()) {
            return emptyList()
        }

        // 2. Measure text and filter based on width to prevent overlapping
        val paint = android.graphics.Paint().apply {
            this.textSize = textSize
            isAntiAlias = true
        }

        val startTimeMs = startInstant.toEpochMilli().toDouble()
        val endTimeMs = endInstant.toEpochMilli().toDouble()

        // Padding between labels (30% of text size, same as computeAutoSkipLabels)
        val labelPadding = textSize * 0.8f

        // Data class to hold label information
        data class LabelInfo(
            val instant: Instant,
            val text: String,
            val xPosition: Float,
            val textWidth: Float,
            val leftBound: Float,
            val rightBound: Float
        )

        // Calculate bounds for start label (if shown)
        val startLabelInfo = if (showStartEndLabels) {
            val startText = formatTimeFromMilliseconds(startTimeMs, withDate = false)
            val startWidth = paint.apply { textAlign = android.graphics.Paint.Align.LEFT }.measureText(startText)
            LabelInfo(
                instant = startInstant,
                text = startText,
                xPosition = 0f,
                textWidth = startWidth,
                leftBound = 0f,
                rightBound = startWidth + labelPadding
            )
        } else null

        // Calculate bounds for end label (if shown)
        val endLabelInfo = if (showStartEndLabels) {
            val endText = formatTimeFromMilliseconds(endTimeMs, withDate = false)
            val endWidth = paint.apply { textAlign = android.graphics.Paint.Align.RIGHT }.measureText(endText)
            LabelInfo(
                instant = endInstant,
                text = endText,
                xPosition = chartWidth,
                textWidth = endWidth,
                leftBound = chartWidth - endWidth - labelPadding,
                rightBound = chartWidth
            )
        } else null

        // Calculate info for each intermediate label
        val intermediateLabelInfos = allIntermediateLabels.map { instant ->
            val timeMs = instant.toEpochMilli().toDouble()
            val text = formatTimeFromMilliseconds(timeMs, withDate = false)
            val width = paint.apply { textAlign = android.graphics.Paint.Align.CENTER }.measureText(text)
            
            // Calculate x position based on time ratio
            val ratio = (timeMs - startTimeMs) / (endTimeMs - startTimeMs)
            val xPos = ratio.toFloat() * chartWidth
            
            LabelInfo(
                instant = instant,
                text = text,
                xPosition = xPos,
                textWidth = width,
                leftBound = xPos - width / 2f - labelPadding,
                rightBound = xPos + width / 2f + labelPadding
            )
        }

        // Filter intermediate labels to avoid overlaps
        val acceptedLabels = mutableListOf<Instant>()
        var lastAcceptedRightBound = startLabelInfo?.rightBound ?: Float.NEGATIVE_INFINITY

        for (labelInfo in intermediateLabelInfos) {
            // Check if this label would overlap with the last accepted label
            if (labelInfo.leftBound < lastAcceptedRightBound) {
                continue // Skip this label due to overlap with previous
            }

            // Check if this label would overlap with end label
            if (endLabelInfo != null && labelInfo.rightBound > endLabelInfo.leftBound) {
                continue // Skip this label due to overlap with end label
            }

            // No overlap, accept this label
            acceptedLabels.add(labelInfo.instant)
            lastAcceptedRightBound = labelInfo.rightBound
        }

        return acceptedLabels
    }
}