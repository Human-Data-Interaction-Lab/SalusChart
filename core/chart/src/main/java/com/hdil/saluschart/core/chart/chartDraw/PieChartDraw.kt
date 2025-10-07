package com.hdil.saluschart.core.chart.chartDraw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import kotlin.math.cos
import kotlin.math.sin

object PieChartDraw {

    /**
     * 파이 차트의 개별 섹션을 그립니다.
     *
     * @param drawScope 그리기 영역
     * @param center 원의 중심점
     * @param radius 원의 반지름
     * @param startAngle 시작 각도
     * @param sweepAngle 호의 각도
     * @param color 섹션 색상
     * @param isDonut 도넛 형태로 그릴지 여부
     * @param strokeWidth 도넛일 경우 테두리 두께
     * @param isSelected 선택된 섹션인지 여부
     * @param animationScale 애니메이션 스케일 (1.0f가 기본)
     * @param alpha 투명도 (1.0f가 기본)
     */
    fun drawPieSection(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        startAngle: Float,
        sweepAngle: Float,
        color: Color,
        isDonut: Boolean,
        strokeWidth: Float,
        isSelected: Boolean = false,
        animationScale: Float = 1.0f,
        alpha: Float = 1.0f
    ) {
        // 선택된 섹션인 경우 약간 확대하고 중심에서 바깥쪽으로 이동
        val scaledRadius = radius * animationScale
        val offsetDistance = if (isSelected) 10f * animationScale else 0f

        // 섹션의 중심 각도 계산
        val midAngle = startAngle + sweepAngle / 2
        val offsetX = cos(Math.toRadians(midAngle.toDouble())).toFloat() * offsetDistance
        val offsetY = sin(Math.toRadians(midAngle.toDouble())).toFloat() * offsetDistance
        val adjustedCenter = Offset(center.x + offsetX, center.y + offsetY)

        // 색상에 투명도 적용
        val adjustedColor = color.copy(alpha = alpha)

        if (isDonut) {
            // 도넛 형태로 그리기
            drawScope.drawArc(
                color = adjustedColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(adjustedCenter.x - scaledRadius, adjustedCenter.y - scaledRadius),
                size = Size(scaledRadius * 2, scaledRadius * 2),
                style = Stroke(width = strokeWidth)
            )

        } else {
            // arc 그리기
            drawScope.drawArc(
                color = adjustedColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(adjustedCenter.x - scaledRadius, adjustedCenter.y - scaledRadius),
                size = Size(scaledRadius * 2, scaledRadius * 2)
            )
            // 각 arc의 왼쪽(시작)과 오른쪽(끝)에 분리선 그리기
            val gap = 4 * drawScope.drawContext.density.density // 2.dp를 px로 변환
            // 왼쪽(시작) 분리선
            val leftRad = Math.toRadians(startAngle.toDouble())
            val leftEndPoint = Offset(
                (adjustedCenter.x + cos(leftRad) * scaledRadius).toFloat(),
                (adjustedCenter.y + sin(leftRad) * scaledRadius).toFloat()
            )
            drawScope.drawLine(
                color = Color.White,
                start = adjustedCenter,
                end = leftEndPoint,
                strokeWidth = gap
            )
            // 오른쪽(끝) 분리선
            val rightAngle = startAngle + sweepAngle
            val rightRad = Math.toRadians(rightAngle.toDouble())
            val rightEndPoint = Offset(
                (adjustedCenter.x + cos(rightRad) * scaledRadius).toFloat(),
                (adjustedCenter.y + sin(rightRad) * scaledRadius).toFloat()
            )
            drawScope.drawLine(
                color = Color.White,
                start = adjustedCenter,
                end = rightEndPoint,
                strokeWidth = gap
            )
        }
    }

    /**
     * 파이 차트의 라벨을 그립니다.
     *
     * @param drawScope 그리기 영역
     * @param center 원의 중심점
     * @param radius 원의 반지름
     * @param data 차트 데이터 포인트 목록
     * @param sections 계산된 섹션 정보 목록
     */
    fun drawPieLabels(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        data: List<ChartMark>,
        sections: List<Triple<Float, Float, Float>>
    ) {
        sections.forEachIndexed { i, (startAngle, sweepAngle, _) ->
            val point = data[i]
            // 레이블이 있는 경우, 파이 차트 조각 가운데에 레이블 표시
            if (point.label != null) {
                // 현재 조각의 중앙 각도
                val midAngle = startAngle + sweepAngle / 2

                // 레이블 위치 계산
                val labelPos = ChartMath.Pie.calculateCenterPosition(center, radius, midAngle)

                // 레이블 그리기
                drawScope.drawContext.canvas.nativeCanvas.drawText(
                    point.y.toString(),
                    labelPos.x,
                    labelPos.y,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 12f * drawScope.drawContext.density.density // 12sp를 px로 변환
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }

}