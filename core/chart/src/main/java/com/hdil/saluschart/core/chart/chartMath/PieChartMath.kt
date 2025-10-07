package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartMark

object PieChartMath {
    /**
     * 파이 차트의 중심점과 반지름을 계산합니다.
     *
     * @param size 캔버스 크기
     * @param padding 원 테두리 패딩 값
     * @return Pair(중심 좌표, 반지름)
     */
    fun computePieMetrics(size: Size, padding: Float = 32f): Pair<Offset, Float> {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2 - padding
        return Pair(center, radius)
    }

    /**
     * 파이 차트의 각 섹션의 각도를 계산합니다.
     *
     * @param data 차트 데이터 포인트 목록
     * @return List<Triple<시작 각도, 스윕 각도, 값 비율>>
     */
    fun computePieAngles(data: List<ChartMark>): List<Triple<Float, Float, Float>> {
        val totalValue = data.sumOf { it.y.toDouble() }.toFloat()
        if (totalValue <= 0f) return emptyList()

        var startAngle = -90f // 12시 방향에서 시작

        return data.map { point ->
            val ratio = (point.y / totalValue).toFloat()
            val sweepAngle = ratio * 360f
            val result = Triple(startAngle, sweepAngle, ratio)
            startAngle += sweepAngle
            result
        }
    }

    /**
     * 파이 섹션의 가운데 위치를 계산합니다(라벨, 툴팁을 위해서).
     *
     * @param center 원의 중심점
     * @param radius 원의 기본 반지름
     * @param angle 각도(라디안)
     * @return 레이블이 표시될 위치 좌표
     */
    fun calculateCenterPosition(center: Offset, radius: Float, angleInDegrees: Float, forToolTip: Boolean = false): Offset {
        val angleInRadians = Math.toRadians(angleInDegrees.toDouble())
        val labelRadius = radius
        val x = if (forToolTip) labelRadius * Math.cos(angleInRadians).toFloat() else center.x + labelRadius * Math.cos(angleInRadians).toFloat()
        val y = if (forToolTip) labelRadius * Math.sin(angleInRadians).toFloat() else center.y + labelRadius * Math.sin(angleInRadians).toFloat()
        return Offset(x, y)
    }


    /**
     * 클릭된 위치가 어떤 파이 섹션에 해당하는지 감지합니다.
     *
     * @param clickPosition 클릭된 위치
     * @param center 파이 차트의 중심점
     * @param radius 파이 차트의 반지름
     * @param sections 파이 섹션 정보 목록
     * @return 클릭된 섹션의 인덱스 (클릭된 섹션이 없으면 -1)
     */
    fun getClickedSectionIndex(
        clickPosition: Offset,
        center: Offset,
        radius: Float,
        sections: List<Triple<Float, Float, Float>>
    ): Int {
        // 클릭 위치가 원 안에 있는지 확인
        val distance = kotlin.math.sqrt(
            (clickPosition.x - center.x) * (clickPosition.x - center.x) +
            (clickPosition.y - center.y) * (clickPosition.y - center.y)
        )

        // TODO: 지금은 임의로 50f를 더했지만, 이 값은 필요에 따라 조정 가능 (예: 도넛 차트의 경우)
        // StrokeWidth를 고려 필요
        if (distance > radius+50f) return -1

        // 클릭 위치의 각도 계산 (12시 방향 기준)
        val angleRad = kotlin.math.atan2(
            clickPosition.y - center.y,
            clickPosition.x - center.x
        )
        var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

        // -90도를 더해서 12시 방향을 0도로 맞춤
        angleDeg += 90f
        if (angleDeg < 0) angleDeg += 360f

        // 어떤 섹션에 해당하는지 찾기
        sections.forEachIndexed { index, (startAngle, sweepAngle, _) ->
            var normalizedStart = startAngle + 90f // 12시 방향 기준으로 변환
            if (normalizedStart < 0) normalizedStart += 360f

            val endAngle = normalizedStart + sweepAngle

            if (endAngle <= 360f) {
                // 일반적인 경우
                if (angleDeg >= normalizedStart && angleDeg <= endAngle) {
                    return index
                }
            } else {
                // 360도를 넘어가는 경우
                if (angleDeg >= normalizedStart || angleDeg <= (endAngle - 360f)) {
                    return index
                }
            }
        }

        return -1
    }
}