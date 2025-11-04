package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawContext
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.hdil.saluschart.core.chart.chartMath.ChartMath

object LineChartDraw {

    /**
     * 데이터 포인트를 연결하는 라인을 그립니다.
     *
     * @param drawScope 그리기 영역
     * @param points 화면 좌표로 변환된 데이터 포인트 목록
     * @param color 라인 색상
     * @param strokeWidth 라인 두께
     */
    fun drawLine(
        drawScope: DrawScope,
        points: List<Offset>,
        color: Color,
        strokeWidth: Float
    ) {
        if (points.size < 2) return

        // 라인 그리기
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawScope.drawPath(path, color = color, style = Stroke(width = strokeWidth))
    }
    
    // TODO: 현재 line chart 및 scatter plot 레이블이 왼쪽 가장 끝에서 시작 (첫 번째 레이블이 y축과 맞닿음)
    // - 이에 첫 번째 PointMarker가 y축과 겹치는 현상 발생 (ScatterPlot 예시 참고)
    // - 따라서 drawBarXAxisLabels 함수 참고하여 첫 번째 레이블과 데이터포인트를 왼쪽 끝에서 반 칸 띄어서 그리는 작업 필요할 수도 있음
    /**
     * X축 레이블을 그립니다 (라인차트용 - 첫 번째 레이블이 왼쪽 끝에서 시작).
     *
     * @param ctx 그리기 컨텍스트
     * @param labels X축에 표시할 레이블 목록
     * @param metrics 차트 메트릭 정보
     * @param centered 텍스트를 중앙 정렬할지 여부 (기본값: true)
     * @param textSize 레이블 텍스트 크기 (기본값: 28f)
     * @param maxXTicksLimit X축에 표시할 최대 라벨 개수 (null이면 모든 라벨 표시)
     * @param xLabelAutoSkip 라벨 자동 스킵 활성화 여부 (true이면 텍스트 너비 기반 자동 계산)
     */
    fun drawLineXAxisLabels(
        ctx: DrawContext,
        labels: List<String>,
        metrics: ChartMath.ChartMetrics,
        centered: Boolean = true,
        textSize: Float = 28f,
        maxXTicksLimit: Int? = null,
        xLabelAutoSkip: Boolean = false
    ) {
        // 라벨 감소 로직
        val (displayLabels, displayIndices) = if (xLabelAutoSkip) {
            // 자동 스킵: 텍스트 너비 기반으로 계산
            ChartMath.computeAutoSkipLabels(
                labels = labels,
                textSize = textSize,
                chartWidth = metrics.chartWidth,
                maxXTicksLimit = maxXTicksLimit
            )
        } else {
            // 모든 라벨 표시
            Pair(labels, labels.indices.toList())
        }
        
        val totalLabels = labels.size
        val spacing = if (totalLabels > 1) metrics.chartWidth / (totalLabels - 1) else 0f
        
        displayLabels.forEachIndexed { displayIndex, label ->
            // 원본 라벨 목록에서의 실제 인덱스를 사용
            val originalIndex = displayIndices[displayIndex]
            val x = metrics.paddingX + originalIndex * spacing
            ctx.canvas.nativeCanvas.drawText(
                label,
                x,
                metrics.paddingY + metrics.chartHeight + 50f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    this.textSize = textSize
                    if (centered) {
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                }
            )
        }
    }

}