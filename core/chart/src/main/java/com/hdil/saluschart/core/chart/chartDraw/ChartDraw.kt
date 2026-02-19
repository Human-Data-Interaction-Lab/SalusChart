package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.hdil.saluschart.core.chart.chartMath.ChartMath

/**
 * Y축 위치를 나타내는 열거형 클래스
 */
enum class YAxisPosition {
    LEFT,   // 왼쪽
    RIGHT   // 오른쪽
}

// TODO : metric 관련 함수에서 고정값 수정 필요
object ChartDraw {

    var Pie = PieChartDraw
    val RangeBar = RangeBarChartDraw
    val Line = LineChartDraw
    val Bar = BarChartDraw
    val Scatter = ScatterPlotDraw
    val Progress = ProgressChartDraw
    val Gauge = GaugeChartDraw
    val SleepStage = SleepStageChartDraw

    /**
     * 눈금 값을 적절한 형식으로 포맷합니다.
     *
     * @param value 눈금 값
     * @return 포맷된 문자열
     */
    fun formatTickLabel(value: Float): String {
        return when {
            value == 0f -> "0"
            value >= 1000000 -> "%.1fM".format(value / 1000000)
            value >= 1000 -> "%.1fK".format(value / 1000)
            value % 1 == 0f -> "%.0f".format(value)
            else -> "%.1f".format(value)
        }
    }

    /**
     * Y축 그리드와 레이블을 그립니다.
     *
     * @param drawScope 그리기 영역
     * @param size Canvas의 전체 크기
     * @param metrics 차트 메트릭 정보
     * @param yAxisPosition Y축 위치
     */
    fun drawGrid(
        drawScope: DrawScope,
        size: Size,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
        drawLabels: Boolean = true
    ) {
        // Y축 라인의 실제 X 좌표 계산
        val yAxisX = when (yAxisPosition) {
            YAxisPosition.RIGHT -> metrics.paddingX + metrics.chartWidth
            YAxisPosition.LEFT -> metrics.paddingX
        }

        metrics.yTicks.forEach { yVal ->
            // Convert chart-relative Y to canvas coordinates
            val y =
                metrics.paddingY + metrics.chartHeight - ((yVal - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight

            // 그리드 라인은 차트 영역 전체에 걸쳐 그리기
            val gridStart = metrics.paddingX // 왼쪽 Y축까지
            val gridEnd = when (yAxisPosition) {
                YAxisPosition.RIGHT -> metrics.paddingX + metrics.chartWidth // 오른쪽 Y축까지
                YAxisPosition.LEFT -> metrics.paddingX + metrics.chartWidth // 오른쪽 끝까지
            }

            drawScope.drawLine(
                color = Color.LightGray,
                start = Offset(gridStart, y.toFloat()),
                end = Offset(gridEnd, y.toFloat()),
                strokeWidth = 1f
            )

            // only draw labels when drawLabels = true
            if (drawLabels) {
                val labelText = formatTickLabel(yVal.toFloat())

                // Y축 레이블 위치를 yAxisPosition에 따라 결정
                val labelX = when (yAxisPosition) {
                    YAxisPosition.RIGHT -> yAxisX + 20f // 오른쪽 Y축 라인의 오른쪽에 위치
                    YAxisPosition.LEFT -> 20f // 기본값: 왼쪽 위치
                }

                // Y축 레이블 정렬을 yAxisPosition에 따라 결정
                val textAlign = when (yAxisPosition) {
                    YAxisPosition.RIGHT -> android.graphics.Paint.Align.LEFT // 오른쪽 Y축일 때는 왼쪽 정렬
                    YAxisPosition.LEFT -> android.graphics.Paint.Align.RIGHT // 왼쪽 Y축일 때는 오른쪽 정렬
                }

                drawScope.drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    labelX,
                    y.toFloat() + 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 28f
                        this.textAlign = textAlign
                    }
                )
            }
        }
    }

    /**
     * X축 라인을 그립니다.
     *
     * @param drawScope 그리기 영역
     * @param metrics 차트 메트릭 정보
     */
    fun drawXAxis(drawScope: DrawScope, metrics: ChartMath.ChartMetrics) {
        drawScope.drawLine(
            color = Color.Black,
            start = Offset(metrics.paddingX, metrics.paddingY + metrics.chartHeight),
            end = Offset(
                metrics.paddingX + metrics.chartWidth,
                metrics.paddingY + metrics.chartHeight
            ),
            strokeWidth = 2f
        )
    }

    /**
     * Y축 라인을 그립니다.
     *
     * @param drawScope 그리기 영역
     * @param metrics 차트 메트릭 정보
     * @param yAxisPosition Y축 위치
     */
    fun drawYAxis(
        drawScope: DrawScope,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition = YAxisPosition.LEFT
    ) {
        // Y축 라인 위치를 yAxisPosition에 따라 결정
        val axisStartX = when (yAxisPosition) {
            YAxisPosition.RIGHT -> metrics.paddingX + metrics.chartWidth // 오른쪽 위치
            YAxisPosition.LEFT -> metrics.paddingX // 기본값: 왼쪽 위치
        }

        drawScope.drawLine(
            color = Color.Black,
            start = Offset(axisStartX, metrics.paddingY),
            end = Offset(axisStartX, metrics.paddingY + metrics.chartHeight),
            strokeWidth = 2f
        )
    }

    /*
     * 고정된 Y축을 그립니다. (paging용)
     *
     * @param drawScope 그리기 영역
     * @param metrics 차트 메트릭 정보
     * @param yAxisPosition Y축 위치
     * @param paneWidthPx 팬 너비
     * @param labelTextSizePx 레이블 텍스트 크기
     */
    fun drawYAxisStandalone(
        drawScope: DrawScope,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition,
        paneWidthPx: Float,
        labelTextSizePx: Float = 28f,
        highlightValues: List<Double> = emptyList(),
        highlightColorForValue: (Double) -> Color = { Color.Transparent },
        extraTickValues: List<Double> = emptyList(),
        highlightTolerance: Double = 1e-6
    ) {
        val denom = (metrics.maxY - metrics.minY)
        if (denom == 0.0) return

        // Axis X anchored to the pane edge (1px in from the edge)
        val axisX = if (yAxisPosition == YAxisPosition.RIGHT) paneWidthPx - 0.5f else 0.5f

        // Axis line
        drawScope.drawLine(
            color = Color.Black,
            start = Offset(axisX, metrics.paddingY),
            end = Offset(axisX, metrics.paddingY + metrics.chartHeight),
            strokeWidth = 2f
        )

        val mergedTicks = (metrics.yTicks + extraTickValues)
            .distinct()
            .sorted()

        val tickLen = 8f
        val labelGap = 10f

        val normalPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.DKGRAY
            textSize = labelTextSizePx
            textAlign = if (yAxisPosition == YAxisPosition.RIGHT)
                android.graphics.Paint.Align.LEFT else android.graphics.Paint.Align.RIGHT
        }

        val highlightPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = labelTextSizePx
            textAlign = normalPaint.textAlign
        }

        val pillPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }

        val fm = normalPaint.fontMetrics
        val textHeight = (fm.descent - fm.ascent) // px

        drawScope.drawContext.canvas.nativeCanvas.apply {
            mergedTicks.forEach { yVal ->
                // skip ticks outside visible range
                if (yVal < metrics.minY - 1e-9 || yVal > metrics.maxY + 1e-9) return@forEach

                val y =
                    metrics.paddingY +
                            metrics.chartHeight -
                            (((yVal - metrics.minY) / denom) * metrics.chartHeight).toFloat()

                // Tick mark
                val tickEndX =
                    if (yAxisPosition == YAxisPosition.RIGHT) axisX - tickLen else axisX + tickLen

                drawScope.drawLine(
                    color = Color.DarkGray,
                    start = Offset(axisX, y),
                    end = Offset(tickEndX, y),
                    strokeWidth = 1f
                )

                // Label
                val label = formatTickLabel(yVal.toFloat())

                val isHighlighted = highlightValues.any { hv ->
                    kotlin.math.abs(hv - yVal) <= highlightTolerance
                }

                val labelX =
                    if (yAxisPosition == YAxisPosition.RIGHT) axisX + labelGap else axisX - labelGap

                if (isHighlighted) {
                    val c = highlightColorForValue(yVal)

                    val textColorInt = c.toArgb()
                    highlightPaint.color = textColorInt

                    pillPaint.color = c.copy(alpha = 0.18f).toArgb()

                    val textWidth = highlightPaint.measureText(label)

                    val padX = 14f
                    val padY = 8f
                    val pillH = textHeight + padY * 2f
                    val pillRadius = pillH / 2f

                    val left: Float
                    val right: Float

                    if (yAxisPosition == YAxisPosition.RIGHT) {
                        // textAlign LEFT => labelX is left edge
                        left = labelX - padX
                        right = labelX + textWidth + padX
                    } else {
                        // textAlign RIGHT => labelX is right edge
                        left = labelX - textWidth - padX
                        right = labelX + padX
                    }

                    val top = y + fm.ascent - padY
                    val bottom = y + fm.descent + padY

                    val clampedLeft = left.coerceIn(0f, paneWidthPx)
                    val clampedRight = right.coerceIn(0f, paneWidthPx)

                    drawRoundRect(
                        clampedLeft,
                        top,
                        clampedRight,
                        bottom,
                        pillRadius,
                        pillRadius,
                        pillPaint
                    )

                    // Draw highlighted text
                    drawText(label, labelX, y - 0f, highlightPaint)
                } else {
                    // Normal tick label
                    drawText(label, labelX, y - 0f, normalPaint)
                }
            }
        }
    }
}