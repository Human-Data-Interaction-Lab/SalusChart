package com.hdil.saluschart.core.chart.chartDraw

import android.graphics.Paint
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
import androidx.compose.ui.graphics.drawscope.DrawContext
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.BaseChartMark
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.data.model.model.SleepStageType
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object SleepStageChartDraw {
    // TODO: 현재 SleepStageChart 제작 시 사용 중, 추후 Gauge Chart 제작 시 재사용 가능할 수 있음
    // - 그러나 현재 HorizontalBarMarker 함수는 Sleep Stage Chart에 특화된 함수이므로, 추후 일반화 필요할 수 있음

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
     * @param unit 단위 (기본값: "")
     */
    @Composable
    fun HorizontalBarMarker(
        data: List<BaseChartMark>,
        minValues: List<Double>,
        maxValues: List<Double>,
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
        var tooltipData: BaseChartMark? = null

        (0 until dataSize).forEach { index ->
            // 값 추출
            val minValue = minValues.getOrNull(index) ?: 0.0
            val maxValue = maxValues.getOrNull(index) ?: 0.0

            // tooltipText is only used for onBarClick callback
            val tooltipText = customTooltipText?.getOrNull(index) ?: "Sleep Stage"

            // 바 너비와 위치 계산 (수평이므로 X축 방향으로 계산)
            val (barWidth, barX) = if (isTouchArea) {
                // 전체 차트 너비 사용 (터치 영역용)
                Pair(metrics.chartWidth, metrics.paddingX)
            } else {
                // minValue에서 maxValue까지의 바 계산 (X축 방향)
                val xMinScreen =
                    ((minValue - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartWidth
                val xMaxScreen =
                    ((maxValue - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartWidth
                val width = xMaxScreen - xMinScreen
                // Add paddingX to position bars correctly within the chart area
                Pair(width, metrics.paddingX + xMinScreen)
            }

            // 바 Y 위치 계산 (수평이므로 Y축은 sleep stage ordinal 기반)
            val sleepStageOrdinal = data.getOrNull(index)?.x?.toInt() ?: 0
            val totalSleepStages = 4 // AWAKE, REM, LIGHT, DEEP
            val spacing = metrics.chartHeight / totalSleepStages
            val barHeight = spacing * actualBarHeightRatio
            val barY = metrics.paddingY + sleepStageOrdinal * spacing + (spacing - barHeight) / 2f

            // Double 좌표를 Dp로 변환
            val barXDp = with(density) { barX.toFloat().toDp() }
            val barYDp = with(density) { barY.toFloat().toDp() }
            val barWidthDp = with(density) { barWidth.toFloat().toDp() }
            val barHeightDp = with(density) { barHeight.toFloat().toDp() }

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
                tooltipOffset = Offset(barX.toFloat(), barY.toFloat())
            }

            // Get sleep stage color based on ordinal (reuse existing sleepStageOrdinal)
            val sleepStageType = when (sleepStageOrdinal) {
                0 -> SleepStageType.AWAKE
                1 -> SleepStageType.REM
                2 -> SleepStageType.LIGHT
                3 -> SleepStageType.DEEP
                else -> SleepStageType.UNKNOWN
            }
            val stageColor = ChartMath.SleepStage.getSleepStageColor(sleepStageType)

            val actualColor = if (isTouchArea) {
                Color.Transparent // 터치 영역용은 투명
            } else {
                if (actualInteractive) {
                    if (clickedBarIndex == index || clickedBarIndex == null) {
                        stageColor
                    } else {
                        stageColor.copy(alpha = 0.3f) // 클릭되지 않은 바는 반투명 처리
                    }
                } else {
                    if (showTooltipForIndex == index || showTooltipForIndex == null) {
                        stageColor
                    } else {
                        stageColor.copy(alpha = 0.3f) // 클릭되지 않은 바는 반투명 처리
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
            val customTooltipText = if (tooltipData is RangeChartMark) {
                val sleepStageOrdinal = tooltipData.x.toInt()
                val sleepStageName = when (sleepStageOrdinal) {
                    0 -> "Awake"
                    1 -> "REM"
                    2 -> "Light"
                    3 -> "Deep"
                    else -> "Unknown"
                }
                val startTime =
                    ChartMath.SleepStage.formatTimeFromMilliseconds(tooltipData.minPoint.y)
                val endTime =
                    ChartMath.SleepStage.formatTimeFromMilliseconds(tooltipData.maxPoint.y)
                "$startTime - $endTime"
            } else null

            val sleepcolor = if (tooltipData is RangeChartMark) {
                val sleepStageOrdinal = tooltipData.x.toInt()
                val sleepStageType = when (sleepStageOrdinal) {
                    0 -> SleepStageType.AWAKE
                    1 -> SleepStageType.REM
                    2 -> SleepStageType.LIGHT
                    3 -> SleepStageType.DEEP
                    else -> SleepStageType.UNKNOWN
                }
                ChartMath.SleepStage.getSleepStageColor(sleepStageType)
            } else Color.Black

            ChartTooltip(
                ChartMark = tooltipData,
                unit = unit,
                customText = customTooltipText,
                modifier = Modifier.offset(x = xDp, y = yDp - 80.dp),
                color = sleepcolor
            )
        }
    }

    /**
     * 수면 단계 차트의 X축 레이블을 그립니다.
     * SleepSession의 시작/끝 시간과 중간에 0~4개의 정각 레이블을 표시합니다.
     * 필요 시 자동 스킵 기능을 통해 레이블이 겹치지 않도록 합니다.
     *
     * @param ctx 그리기 컨텍스트
     * @param metrics 차트 메트릭 정보
     * @param startTimeMillis SleepSession의 시작 시간 (밀리초)
     * @param endTimeMillis SleepSession의 끝 시간 (밀리초)
     * @param textSize 레이블 텍스트 크기 (기본값: 28f)
     * @param showStartEndLabels 시작/끝 레이블 표시 여부 (기본값: false)
     * @param xLabelAutoSkip 라벨 자동 스킵 활성화 여부 (true이면 텍스트 너비 기반 자동 계산)
     */
    fun drawSleepStageXAxisLabels(
        ctx: DrawContext,
        metrics: ChartMath.ChartMetrics,
        startTimeMillis: Double,
        endTimeMillis: Double,
        textSize: Float = 28f,
        showStartEndLabels: Boolean,
        xLabelAutoSkip: Boolean = false
    ) {
        // 시작/끝 시간을 Instant로 변환
        val startInstant = Instant.ofEpochMilli(kotlin.math.round(startTimeMillis).toLong())
        val endInstant = Instant.ofEpochMilli(kotlin.math.round(endTimeMillis).toLong())

        // 시작과 끝 시간 텍스트 (날짜 포함)
        val startTimeText =
            ChartMath.SleepStage.formatTimeFromMilliseconds(startTimeMillis, withDate = false)
        val endTimeText =
            ChartMath.SleepStage.formatTimeFromMilliseconds(endTimeMillis, withDate = false)

        // X축 레이블 위치 계산
        val startX = metrics.paddingX
        val endX = metrics.paddingX + metrics.chartWidth
        val xAxisY = metrics.paddingY + metrics.chartHeight
        val labelY = xAxisY + 50f

        // Paint 설정
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            this.textSize = textSize
            isAntiAlias = true
        }

        // 틱 마커용 Paint 설정
        val tickPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 2f
            isAntiAlias = true
        }

        if (showStartEndLabels) {
            // 시작 시간 틱 마커 그리기
            ctx.canvas.nativeCanvas.drawLine(
                startX,
                xAxisY,
                startX,
                xAxisY + 15f,
                tickPaint
            )

            // 시작 시간 레이블 그리기 (왼쪽 정렬)
            paint.textAlign = android.graphics.Paint.Align.LEFT
            ctx.canvas.nativeCanvas.drawText(startTimeText, startX, labelY, paint)

            // 끝 시간 틱 마커 그리기
            ctx.canvas.nativeCanvas.drawLine(
                endX,
                xAxisY,
                endX,
                xAxisY + 15f,
                tickPaint
            )

            // 끝 시간 레이블 그리기 (오른쪽 정렬)
            paint.textAlign = android.graphics.Paint.Align.RIGHT
            ctx.canvas.nativeCanvas.drawText(endTimeText, endX, labelY, paint)
        }

        // 중간 정각 레이블 생성 및 필터링
        val intermediateLabels = if (xLabelAutoSkip) {
            // 자동 스킵: 모든 정각 레이블 생성 후 텍스트 너비 기반으로 필터링
            ChartMath.SleepStage.computeNonOverlappingIntermediateLabels(
                startInstant = startInstant,
                endInstant = endInstant,
                textSize = textSize,
                chartWidth = metrics.chartWidth,
                showStartEndLabels = showStartEndLabels
            )
        } else {
            // 자동 스킵 비활성화: 모든 정각 레이블 표시 (겹칠 수 있음)
            val startZdt = startInstant.atZone(java.time.ZoneId.systemDefault())
            val endZdt = endInstant.atZone(java.time.ZoneId.systemDefault())
            val firstHour = startZdt.plusHours(1).truncatedTo(java.time.temporal.ChronoUnit.HOURS)
            val lastHour = endZdt.truncatedTo(java.time.temporal.ChronoUnit.HOURS)

            val allLabels = mutableListOf<Instant>()
            var currentHour = firstHour
            while (currentHour.isBefore(lastHour) || currentHour.isEqual(lastHour)) {
                if (currentHour.toInstant() != startInstant && currentHour.toInstant() != endInstant) {
                    allLabels.add(currentHour.toInstant())
                }
                currentHour = currentHour.plusHours(1)
            }
            allLabels
        }

        // 중간 레이블 그리기 (중앙 정렬, 날짜 제외)
        paint.textAlign = android.graphics.Paint.Align.CENTER
        intermediateLabels.forEach { hourInstant ->
            val hourTimeMs = hourInstant.toEpochMilli().toDouble()
            val hourText =
                ChartMath.SleepStage.formatTimeFromMilliseconds(hourTimeMs, withDate = false)

            // X 위치 계산 (시간 비율에 따라)
            val ratio = (hourTimeMs - startTimeMillis) / (endTimeMillis - startTimeMillis)
            val x = metrics.paddingX + ratio.toFloat() * metrics.chartWidth

            // 틱 마커 그리기
            ctx.canvas.nativeCanvas.drawLine(
                x,
                xAxisY,
                x,
                xAxisY + 15f,
                tickPaint
            )

            ctx.canvas.nativeCanvas.drawText(hourText, x, labelY, paint)
        }
    }

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
        ctx: DrawContext,
        metrics: ChartMath.ChartMetrics,
        lineColor: Int = android.graphics.Color.LTGRAY,
        lineWidth: Float = 1f
    ) {
        val totalStages = 4 // AWAKE, REM, LIGHT, DEEP
        val stageHeight = metrics.chartHeight / totalStages

        // 각 수면 단계 사이에 수평선 그리기
        for (i in 0 until totalStages + 1) {
            val y = metrics.paddingY + i * stageHeight

            ctx.canvas.nativeCanvas.drawLine(
                metrics.paddingX, // 시작 X (차트 영역 시작)
                y,                // Y 위치
                metrics.paddingX + metrics.chartWidth, // 끝 X (차트 영역 끝)
                y,                // Y 위치
                Paint().apply {
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
        drawScope: DrawScope,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition,
        paneWidthPx: Float,
        labelTextSizePx: Float = 28f
    ) {
        val totalStages = 4 // AWAKE, REM, LIGHT, DEEP
        val stageHeight = metrics.chartHeight / totalStages

        // Y축 라인 위치 결정
        val axisX = if (yAxisPosition == YAxisPosition.RIGHT) paneWidthPx - 0.5f else 0.5f

        // 수면 단계 레이블 그리기
        val sleepStages = listOf("Awake", "REM", "Light", "Deep")

        sleepStages.forEachIndexed { index, stage ->
            val y = metrics.paddingY + (index + 0.5f) * stageHeight

            // 레이블 텍스트 그리기
            val paint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.DKGRAY
                textSize = labelTextSizePx
                textAlign = if (yAxisPosition == YAxisPosition.RIGHT)
                    Paint.Align.LEFT else Paint.Align.RIGHT
            }
            val labelX = if (yAxisPosition == YAxisPosition.RIGHT)
                axisX + 10f else axisX - 10f

            drawScope.drawContext.canvas.nativeCanvas.drawText(stage, labelX, y + 10f, paint)
        }
    }

    /**
     * 수면 단계 차트의 바들 사이에 연결선을 그립니다.
     * 연속된 수면 단계들 사이에 수평선을 그려서 시간 연속성을 시각적으로 표현합니다.
     *
     * @param data 차트 데이터 포인트 목록 (시간순으로 정렬된 RangeChartMark)
     * @param metrics 차트 메트릭 정보
     * @param lineColor 연결선 색상 (기본값: 연한 회색)
     * @param lineWidth 연결선 두께 (기본값: 2f)
     * @param totalSleepStages 전체 수면 단계 수 (기본값: 4)
     * @param barHeightRatio 바 높이 비율 (기본값: 0.5f)
     */
    fun drawSleepStageConnector(
        drawScope: DrawScope,
        data: List<BaseChartMark>,
        metrics: ChartMath.ChartMetrics,
        lineColor: Color = Color(0xFFCCCCCC),
        lineWidth: Float = 10f,
        totalSleepStages: Int = 4,
        barHeightRatio: Float = 0.5f
    ) {
        // 데이터가 2개 미만이면 연결선을 그릴 수 없음
        if (data.size < 2) return

        // 바 높이 계산
        val spacing = metrics.chartHeight / totalSleepStages
        val barHeight = spacing * barHeightRatio

        // 연속된 단계들 사이에 선 그리기
        for (i in 0 until data.size - 1) {
            val currentStage = data[i]
            val nextStage = data[i + 1]

            val currentEndX = if (currentStage is RangeChartMark) {
                ((currentStage.maxPoint.y - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartWidth + metrics.paddingX
            } else {
                ((currentStage.x - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartWidth + metrics.paddingX
            }

            val nextStartX = if (nextStage is RangeChartMark) {
                ((nextStage.minPoint.y - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartWidth + metrics.paddingX
            } else {
                ((nextStage.x - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartWidth + metrics.paddingX
            }

            val currentStageOrdinal = currentStage.x.toInt()
            val nextStageOrdinal = nextStage.x.toInt()

            val currentY = metrics.paddingY + currentStageOrdinal * spacing + spacing / 2f
            val nextY = metrics.paddingY + nextStageOrdinal * spacing + spacing / 2f

            // (startY < endY)일 때 -barHeight/2 적용
            val adjustedStartX = if (currentY < nextY) {
                currentEndX.toFloat()
            } else {
                currentEndX.toFloat()
            }
            val adjustedEndX = if (currentY < nextY) {
                nextStartX.toFloat()
            } else {
                nextStartX.toFloat()
            }

            // (startY < endY)일 때 -barHeight/2 적용
            val adjustedStartY = if (currentY < nextY) {
                currentY - barHeight / 2f
            } else {
                currentY + barHeight / 2f
            }
            val adjustedEndY = if (currentY < nextY) {
                nextY + barHeight / 2f
            } else {
                nextY - barHeight / 2f
            }

            // 선 그리기
            drawScope.drawLine(
                color = lineColor.copy(alpha = 0.9f),
                start = Offset(adjustedStartX, adjustedStartY),
                end = Offset(adjustedEndX, adjustedEndY),
                strokeWidth = lineWidth
            )
        }
    }
}