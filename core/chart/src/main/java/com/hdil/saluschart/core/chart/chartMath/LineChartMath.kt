package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartPoint
import kotlin.math.sqrt

object LineChartMath {

    /**
     * 탄젠트 벡터를 기반으로 최적의 라벨 위치를 계산합니다.
     * 라인과의 겹침을 최소화하기 위해 접선에 수직인 방향으로 라벨을 배치합니다.
     *
     * @param pointIndex 현재 포인트의 인덱스
     * @param points 모든 데이터 포인트들
     * @return 최적의 라벨 위치
     */
    fun calculateLabelPosition(
        pointIndex: Int,
        points: List<Offset>,
    ): Offset {
        val currentPoint = points[pointIndex]
        val baseDistance = 50f
        var incoming: Offset = Offset(0f, 0f)
        var outgoing: Offset = Offset(0f, 0f)

        // Step 1: Calculate tangent vector
        when {
            points.size < 2 -> {
                incoming = Offset(1f, 0f)
                outgoing = Offset(1f, 0f)
            }
            pointIndex == 0 -> {
                // Start point: use direction to next point
                val direction = points[1] - currentPoint
                outgoing = normalizeVector(direction)
                incoming = outgoing  // Same as outgoing for start point
            }
            pointIndex == points.size - 1 -> {
                // End point: use direction from previous point
                val direction = currentPoint - points[pointIndex - 1]
                incoming = normalizeVector(direction)
                outgoing = incoming  // Same as incoming for end point
            }
            else -> {
                // Interior point: calculate incoming and outgoing directions
                incoming = normalizeVector(currentPoint - points[pointIndex - 1])  // Direction FROM previous TO current
                outgoing = normalizeVector(points[pointIndex + 1] - currentPoint)  // Direction FROM current TO next
            }
        }
        // Calculate tangent as average of incoming and outgoing (after the when block)
        val tangent = normalizeVector(
            Offset(
                (incoming.x + outgoing.x) / 2f,
                (incoming.y + outgoing.y) / 2f
            )
        )

        // Step 2: Calculate normal vectors (perpendicular to tangent)
        val normal1 = Offset(-tangent.y, tangent.x)   // 90° counterclockwise
        val normal2 = Offset(tangent.y, -tangent.x)   // 90° clockwise

        // Step 3: Generate candidate positions
        val candidate1 = Offset(
            currentPoint.x + normal1.x * baseDistance,
            currentPoint.y + normal1.y * baseDistance
        )
        val candidate2 = Offset(
            currentPoint.x + normal2.x * baseDistance,
            currentPoint.y + normal2.y * baseDistance
        )

        // Step 4: Choose the better candidate by checking the cross product
        val crossProduct = incoming.x * outgoing.y - incoming.y * outgoing.x

        return if (crossProduct >= 0) candidate2 else candidate1
    }

    /**
     * 벡터를 정규화합니다.
     *
     * @param vector 정규화할 벡터
     * @return 정규화된 벡터 (길이가 1��� 단위 벡터)
     */
    fun normalizeVector(vector: Offset): Offset {
        val magnitude = sqrt(vector.x * vector.x + vector.y * vector.y)
        return if (magnitude > 0f) {
            Offset(vector.x / magnitude, vector.y / magnitude)
        } else {
            Offset(1f, 0f)
        }
    }

    /**
     * 라인 차트용 데이터 포인트를 화면 좌표로 변환합니다.
     * 인덱스 기반 X축 포지셔닝 (일대일 매핑)
     *
     * @param data 차트 데이터 포인트 목록
     * @param size Canvas의 전체 크기
     * @param metrics 차트 메트릭 정보
     * @return 화면 좌표로 변환된 Offset 목록
     */
    fun mapLineToCanvasPoints(data: List<ChartPoint>, size: Size, metrics: ChartMath.ChartMetrics): List<Offset> {
        val spacing = metrics.chartWidth / (data.size - 1)
        return data.mapIndexed { i, point ->
            val x = metrics.paddingX + i * spacing
            val y = metrics.chartHeight - ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
            Offset(x, y)
        }
    }
}