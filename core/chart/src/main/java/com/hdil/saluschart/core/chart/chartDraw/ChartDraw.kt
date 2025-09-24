package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.hdil.saluschart.core.chart.chartMath.ChartMath

/**
 * Y축 위치를 나타내는 열거형 클래스
 */
enum class YAxisPosition {
    LEFT,   // 왼쪽
    RIGHT   // 오른쪽
}

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
    fun drawGrid(drawScope: DrawScope, size: Size, metrics: ChartMath.ChartMetrics, yAxisPosition: YAxisPosition = YAxisPosition.LEFT, drawLabels: Boolean = true) {
        // Y축 라인의 실제 X 좌표 계산
        val yAxisX = when (yAxisPosition) {
            YAxisPosition.RIGHT -> metrics.paddingX + metrics.chartWidth
            YAxisPosition.LEFT -> metrics.paddingX
        }

        metrics.yTicks.forEach { yVal ->
            // Convert chart-relative Y to canvas coordinates
            val y = metrics.paddingY + metrics.chartHeight - ((yVal - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight

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
            end = Offset(metrics.paddingX + metrics.chartWidth, metrics.paddingY + metrics.chartHeight),
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
    fun drawYAxis(drawScope: DrawScope, metrics: ChartMath.ChartMetrics, yAxisPosition: YAxisPosition = YAxisPosition.LEFT) {
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

    fun drawYAxisStandalone(
        drawScope: DrawScope,
        metrics: ChartMath.ChartMetrics,
        yAxisPosition: YAxisPosition,
        paneWidthPx: Float,
        labelTextSizePx: Float = 28f
    ) {
        // Axis X anchored to the pane edge (1px in from the edge)
        val axisX = if (yAxisPosition == YAxisPosition.RIGHT) paneWidthPx - 0.5f else 0.5f

        // Axis line
        drawScope.drawLine(
            color = Color.Black,
            start = Offset(axisX, metrics.paddingY),
            end = Offset(axisX, metrics.paddingY + metrics.chartHeight),
            strokeWidth = 2f
        )

        // Ticks & labels
        metrics.yTicks.forEach { yVal ->
            val y = metrics.paddingY + metrics.chartHeight - ((yVal - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight

            // small tick mark
            val tickLen = 8f
            val tickStartX = if (yAxisPosition == YAxisPosition.RIGHT) axisX else axisX
            val tickEndX = if (yAxisPosition == YAxisPosition.RIGHT) axisX - tickLen else axisX + tickLen
            drawScope.drawLine(
                color = Color.DarkGray,
                start = Offset(tickStartX, y.toFloat()),
                end = Offset(tickEndX, y.toFloat()),
                strokeWidth = 1f
            )

            val label = formatTickLabel(yVal.toFloat())
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.DKGRAY
                textSize = labelTextSizePx
                textAlign = if (yAxisPosition == YAxisPosition.RIGHT)
                    android.graphics.Paint.Align.LEFT else android.graphics.Paint.Align.RIGHT
            }
            val labelX = if (yAxisPosition == YAxisPosition.RIGHT)
                axisX + 10f else axisX - 10f

            drawScope.drawContext.canvas.nativeCanvas.drawText(label, labelX, y.toFloat() + 10f, paint)
        }
    }


// ** 쓰이지 않는 Canvas API를 사용한 코드 **
//
//    /**
//     * 범례를 그립니다 (스케일링 지원).
//     *
//     * @param drawScope 그리기 영역
//     * @param labels 범례 항목 레이블 목록
//     * @param colors 색상 목록
//     * @param position 범례가 표시될 위치 좌표
//     * @param chartSize 차트 전체 크기 (스케일링 계산용)
//     * @param title 범례 제목 (null인 경우 제목 없음)
//     * @param baseItemHeight 기본 항목 간 세로 간격 (스케일링 적용됨)
//     */
//    fun drawLegend(
//        drawScope: DrawScope,
//        labels: List<String>,
//        colors: List<Color>,
//        position: Offset,
//        chartSize: androidx.compose.ui.geometry.Size,
//        title: String? = null,
//        baseItemHeight: Float = 20f
//    ) {
//        // 차트 크기에 따른 스케일 팩터 계산 (기준: 250x250)
//        val scaleFactor = minOf(chartSize.width, chartSize.height) / 250f
//        val clampedScale = scaleFactor.coerceIn(0.5f, 2.0f)
//
//        val colorBoxSize = (8f * clampedScale).coerceAtLeast(4f)
//        val padding = (4f * clampedScale).coerceAtLeast(2f)
//        val itemHeight = baseItemHeight * clampedScale
//        val titleTextSize = (14f * clampedScale).coerceAtLeast(10f)
//        val labelTextSize = (12f * clampedScale).coerceAtLeast(8f)
//
//        Log.e("ChartDraw", "Legend scale factor: $clampedScale, itemHeight: $itemHeight, colorBoxSize: $colorBoxSize, labelTextSize: $labelTextSize")
//
//        var yOffset = position.y
//
//        // 범례 제목 그리기 (제공된 경우)
//        title?.let {
//            drawScope.drawContext.canvas.nativeCanvas.drawText(
//                it,
//                position.x,
//                yOffset,
//                android.graphics.Paint().apply {
//                    color = android.graphics.Color.DKGRAY
//                    textSize = titleTextSize
//                    isFakeBoldText = true
//                }
//            )
//            yOffset += itemHeight * 0.8f
//        }
//
//        // 각 범례 항목 그리기
//        labels.forEachIndexed { index, label ->
//            if (index < colors.size) {
//                drawLegendItem(
//                    drawScope,
//                    colors[index],
//                    label,
//                    Offset(position.x, yOffset),
//                    colorBoxSize,
//                    padding,
//                    labelTextSize
//                )
//                yOffset += itemHeight * 0.7f
//            }
//        }
//    }
//
//    /**
//     * 차트의 범례를 그립니다 (통합된 범례 시스템, 스케일링 지원).
//     *
//     * 파이 차트와 스택 바 차트 모두에서 사용할 수 있는 통합된 범례 시스템입니다.
//     * 레이블을 직접 제공하거나 차트 데이터에서 추출할 수 있습니다.
//     *
//     * @param drawScope 그리기 영역
//     * @param labels 범례 항목 레이블 목록 (직접 제공된 경우)
//     * @param chartData 차트 데이터 포인트 목록 (레이블을 추출할 경우)
//     * @param colors 각 항목에 사용한 색상 목록
//     * @param position 범례가 표시될 위치 좌표
//     * @param chartSize 차트 전체 크기 (스케일링 계산용)
//     * @param title 범례 제목 (기본값: null)
//     * @param itemHeight 항목 간 세로 간격
//     */
//    fun drawChartLegend(
//        drawScope: DrawScope,
//        labels: List<String>? = null,
//        chartData: List<ChartPoint>? = null,
//        colors: List<Color>,
//        position: Offset,
//        chartSize: androidx.compose.ui.geometry.Size,
//        title: String? = null,
//        itemHeight: Float = 40f
//    ) {
//        val legendLabels = labels ?: chartData?.mapIndexed { i, point ->
//            point.label ?: "항목 ${i+1}"
//        } ?: emptyList()
//
//        drawLegend(drawScope, legendLabels, colors, position, chartSize, title, itemHeight)
//    }
//
//    /**
//     * 범례의 개별 항목을 그립니다 (스케일링 지원).
//     *
//     * @param drawScope 그리기 영역
//     * @param color 색상
//     * @param label 레이블 텍스트
//     * @param position 항목이 표시될 위치
//     * @param boxSize 색상 상자 크기 (이미 스케일링 적용됨)
//     * @param padding 상자와 텍스트 사이 간격 (이미 스케일링 적용됨)
//     * @param textSize 텍스트 크기 (이미 스케일링 적용됨)
//     */
//    fun drawLegendItem(
//        drawScope: DrawScope,
//        color: Color,
//        label: String,
//        position: Offset,
//        boxSize: Float,
//        padding: Float,
//        textSize: Float = 30f
//    ) {
//        // 색상 상자 그리기
//        drawScope.drawRect(
//            color = color,
//            topLeft = position,
//            size = Size(boxSize, boxSize)
//        )
//
//        // 레이블 그리기
//        drawScope.drawContext.canvas.nativeCanvas.drawText(
//            label,
//            position.x + boxSize + padding,
//            position.y + boxSize,
//            android.graphics.Paint().apply {
//                this.color = android.graphics.Color.DKGRAY
//                this.textSize = textSize
//            }
//        )
//    }
//
//    /**
//     * 차트 툴팁을 그립니다 (모든 차트 타입에서 공통 사용).
//     *
//     * @param drawScope 그리기 영역
//     * @param value 표시할 값
//     * @param position 툴팁이 표시될 위치 (미리 계산된 최적 위치)
//     * @param backgroundColor 툴팁 배경 색상
//     * @param textColor 텍스트 색상
//     * @param textSize 툴팁 텍스트 크기 (기본값: 32f)
//     */
//    fun drawTooltip(
//        drawScope: DrawScope,
//        value: Float,
//        position: Offset,
//        backgroundColor: Color = Color(0xE6333333), // 반투명 다크 그레이
//        textColor: Int = android.graphics.Color.WHITE,
//        textSize: Float = 32f
//    ) {
//        val tooltipText = formatTickLabel(value)
//        val textPaint = android.graphics.Paint().apply {
//            color = textColor
//            this.textSize = textSize
//            textAlign = android.graphics.Paint.Align.CENTER
//        }
//
//        // 텍스트 크기 측정
//        val textBounds = android.graphics.Rect()
//        textPaint.getTextBounds(tooltipText, 0, tooltipText.length, textBounds)
//
//        // 툴팁 크기 계산 (패딩 포함)
//        val padding = 16f
//        val tooltipWidth = textBounds.width() + padding * 2
//        val tooltipHeight = textBounds.height() + padding * 2
//
//        // 툴팁이 화면 밖으로 나가지 않도록 위치 조정
//        val tooltipX = position.x.coerceIn(
//            tooltipWidth / 2,
//            drawScope.size.width - tooltipWidth / 2
//        )
//        val tooltipY = position.y.coerceIn(
//            tooltipHeight / 2,
//            drawScope.size.height - tooltipHeight / 2
//        )
//
//        // 배경 그리기
//        drawScope.drawRoundRect(
//            color = backgroundColor,
//            topLeft = Offset(tooltipX - tooltipWidth / 2, tooltipY - tooltipHeight / 2),
//            size = Size(tooltipWidth, tooltipHeight),
//            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
//        )
//
//        // 텍스트 그리기
//        drawScope.drawContext.canvas.nativeCanvas.drawText(
//            tooltipText,
//            tooltipX,
//            tooltipY + textBounds.height() / 2,
//            textPaint
//        )
//    }
}
