package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.chartMath.ChartMath.ChartMetrics
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

object SleepStageChartMath {
    
    /**
     * 밀리초를 시간 형식(HH:MM:SS)으로 변환합니다.
     * 
     * @param milliseconds 밀리초 값 (Float로 전달되지만 정밀도를 위해 반올림 처리)
     * @return "HH:MM:SS" 형식의 시간 문자열 (소수점 제거)
     */
    fun formatTimeFromMilliseconds(milliseconds: Float): String {
        // Float를 반올림하여 정밀도 손실 최소화
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
     * Instant를 직접 시간 형식(HH:MM:SS)으로 변환합니다.
     * 정밀도 손실 없이 원본 Instant를 사용합니다.
     * 
     * @param instant 변환할 Instant 객체
     * @return "HH:MM:SS" 형식의 시간 문자열
     */
    fun formatTimeFromInstant(instant: Instant): String {
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

        // 수면 세션의 시간 범위를 밀리초로 변환
        val startTimeMs = sleepSession.startTime.toEpochMilli().toFloat()
        val endTimeMs = sleepSession.endTime.toEpochMilli().toFloat()

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

//    /**
//     * 수면 단계 차트의 Y축 레이블을 그립니다.
//     * 수면 단계는 고정된 순서로 표시됩니다: AWAKE, REM, LIGHT, DEEP
//     *
//     * @param ctx 그리기 컨텍스트
//     * @param metrics 차트 메트릭 정보
//     * @param textSize 레이블 텍스트 크기 (기본값: 28f)
//     */
//    fun drawSleepStageLabels(
//        ctx: androidx.compose.ui.graphics.drawscope.DrawContext,
//        metrics: ChartMetrics,
//        textSize: Float = 28f
//    ) {
//        // 수면 단계는 고정된 순서 (위에서 아래로)
//        val sleepStages = listOf("Awake", "REM", "Light", "Deep")
//        val totalStages = sleepStages.size
//        val stageHeight = metrics.chartHeight / totalStages
//
//        sleepStages.forEachIndexed { index, stage ->
//            // 각 수면 단계의 중앙 Y 위치 계산
//            val y = metrics.paddingY + (index + 0.5f) * stageHeight
//
//            ctx.canvas.nativeCanvas.drawText(
//                stage,
//                20f, // Y축 레이블은 왼쪽에 고정
//                y,
//                android.graphics.Paint().apply {
//                    color = android.graphics.Color.DKGRAY
//                    this.textSize = textSize
//                    textAlign = android.graphics.Paint.Align.CENTER
//                }
//            )
//        }
//    }

    /**
     * 수면 단계 차트의 그리드 라인을 그립니다.
     * 각 수면 단계 사이에 수평선을 그려서 구분을 명확하게 합니다.
     *
     * @param ctx 그리기 컨텍스트
     * @param metrics 차트 메트릭 정보
     * @param lineColor 그리드 라인 색상 (기본값: 연한 회색)
     * @param lineWidth 그리드 라인 두께 (기본값: 1f)
     */
    fun drawSleepStageGridLines(
        ctx: androidx.compose.ui.graphics.drawscope.DrawContext,
        metrics: ChartMetrics,
        lineColor: Int = android.graphics.Color.LTGRAY,
        lineWidth: Float = 1f
    ) {
        val totalStages = 4 // AWAKE, REM, LIGHT, DEEP
        val stageHeight = metrics.chartHeight / totalStages
        
        // 각 수면 단계 사이에 수평선 그리기
        for (i in 1 until totalStages) {
            val y = metrics.paddingY + i * stageHeight
            
            ctx.canvas.nativeCanvas.drawLine(
                metrics.paddingX, // 시작 X (차트 영역 시작)
                y,                // Y 위치
                metrics.paddingX + metrics.chartWidth, // 끝 X (차트 영역 끝)
                y,                // Y 위치
                android.graphics.Paint().apply {
                    color = lineColor
                    strokeWidth = lineWidth
                    isAntiAlias = true
                }
            )
        }
    }

    /**
     * 수면 단계 차트의 독립적인 Y축을 그립니다.
     * 고정된 Y축 패널에서 사용됩니다.
     *
     * @param drawScope 그리기 영역
     * @param metrics 차트 메트릭 정보
     * @param yAxisPosition Y축 위치
     * @param paneWidthPx Y축 패널의 너비 (픽셀)
     * @param labelTextSizePx 레이블 텍스트 크기 (기본값: 28f)
     */
    fun drawSleepStageYAxisStandalone(
        drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
        metrics: ChartMetrics,
        yAxisPosition: com.hdil.saluschart.core.chart.chartDraw.YAxisPosition,
        paneWidthPx: Float,
        labelTextSizePx: Float = 28f
    ) {
        val totalStages = 4 // AWAKE, REM, LIGHT, DEEP
        val stageHeight = metrics.chartHeight / totalStages
        
        // Y축 라인 위치 결정
        val axisX = if (yAxisPosition == com.hdil.saluschart.core.chart.chartDraw.YAxisPosition.RIGHT) paneWidthPx - 0.5f else 0.5f
        
        // 수면 단계 레이블 그리기
        val sleepStages = listOf("Awake", "REM", "Light", "Deep")
        
        sleepStages.forEachIndexed { index, stage ->
            val y = metrics.paddingY + (index + 0.5f) * stageHeight
            
            // 레이블 텍스트 그리기
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.DKGRAY
                textSize = labelTextSizePx
                textAlign = if (yAxisPosition == com.hdil.saluschart.core.chart.chartDraw.YAxisPosition.RIGHT)
                    android.graphics.Paint.Align.LEFT else android.graphics.Paint.Align.RIGHT
            }
            val labelX = if (yAxisPosition == com.hdil.saluschart.core.chart.chartDraw.YAxisPosition.RIGHT)
                axisX + 10f else axisX - 10f
            
            drawScope.drawContext.canvas.nativeCanvas.drawText(stage, labelX, y + 10f, paint)
        }
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
     * @param sleepStages List of SleepStage objects
     * @return List of RangeChartPoints for sleep stage chart
     */
    fun List<SleepStage>.toSleepStageRangeChartPoints(): List<RangeChartPoint> {
        return mapIndexed { index, stage ->
            val stageOrdinal = stage.stage.ordinal.toFloat()
            // Use Double to preserve precision, then convert to Float for ChartPoint
            val startTimeMs = stage.startTime.toEpochMilli().toDouble().toFloat()
            val endTimeMs = stage.endTime.toEpochMilli().toDouble().toFloat()
            
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