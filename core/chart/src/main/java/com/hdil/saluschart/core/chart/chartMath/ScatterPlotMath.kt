package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ChartMark

object ScatterPlotMath {
    
    /**
     * 스캐터 플롯용 데이터 포인트를 화면 좌표로 변환합니다.
     * 실제 X값 기반 포지셔닝 (다대다 매핑 지원)
     *
     * @param data 차트 데이터 포인트 목록
     * @param size Canvas의 전체 크기
     * @param metrics 차트 메트릭 정보
     * @return 화면 좌표로 변환된 Offset 목록
     * - 카테고리형(정수, 연속) x값인 경우: Bar 차트와 동일하게 카테고리 중심에 정렬
     * - 연속형(실수) x값인 경우: 기존 minX..maxX 연속 매핑 유지
     */
    fun mapScatterToCanvasPoints(data: List<ChartMark>, size: Size, metrics: ChartMath.ChartMetrics): List<Offset> {
        if (data.isEmpty()) return emptyList()
        
        // X축 범위 계산
        val minX = data.minOf { it.x }
        val maxX = data.maxOf { it.x }
        val xRange = if (maxX > minX) (maxX - minX).toFloat() else 1f

//        return data.map { point ->
//            // X값을 실제 좌표계에 매핑
//            val xPosition = if (xRange > 0f) {
//                metrics.paddingX + ((point.x - minX).toFloat() / xRange) * metrics.chartWidth
//            } else {
//                metrics.paddingX + metrics.chartWidth / 2f
//            }
//
//            // Y값을 좌표계에 매핑
//            val yPosition = metrics.paddingY + metrics.chartHeight - ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)).toFloat() * metrics.chartHeight
//
//            Offset(xPosition, yPosition)
//        }
//        // 유니크한 X 목록 및 정렬
//        val uniqueXs = data.map { it.x }.distinct().sorted()
//        val categoriesCount = uniqueXs.size
//
//        // 카테고리형 판단: 모든 x가 정수이고, 연속적이며(step=1), 최소 2개 이상
//        val allInt = uniqueXs.all { kotlin.math.abs(it - it.toLong()) < 1e-6 }
//        val contiguous = categoriesCount >= 2 && kotlin.math.abs(uniqueXs.last() - uniqueXs.first() - (categoriesCount - 1)) < 1e-6
//        val treatAsCategorical = allInt && contiguous
//
//        return if (treatAsCategorical) {
//            // Bar 스타일: 각 카테고리의 중심에 위치시키기 위해 0.5 step 보정
//            val spacing = metrics.chartWidth / categoriesCount
//            data.map { point ->
//                val idx = uniqueXs.binarySearch(point.x).let { if (it >= 0) it else 0 }
//                val xPosition = metrics.paddingX + (idx + 0.5f) * spacing
//                val yPosition = metrics.paddingY + metrics.chartHeight - ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)).toFloat() * metrics.chartHeight
//                Offset(xPosition, yPosition)
//            }
//        } else {
//            // 기존 연속형 매핑 유지
//            val minX = uniqueXs.first()
//            val maxX = uniqueXs.last()
//            val xRange = if (maxX > minX) (maxX - minX).toFloat() else 1f
//            data.map { point ->
//                val xPosition = if (xRange > 0f) {
//                    metrics.paddingX + ((point.x - minX).toFloat() / xRange) * metrics.chartWidth
//                } else {
//                    metrics.paddingX + metrics.chartWidth / 2f
//                }
//                val yPosition =
//                    metrics.paddingY + metrics.chartHeight - ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)).toFloat() * metrics.chartHeight
//                Offset(xPosition, yPosition)
//            }
//        }

        // 유니크한 X 목록 및 정렬
        val uniqueXs = data.map { it.x }.distinct().sorted()
        val categoriesCount = uniqueXs.size
        // Bar 스타일: 각 카테고리의 중심에 위치시키기 위해 0.5 step 보정
        val spacing = metrics.chartWidth / categoriesCount
        return data.map { point ->
            val idx = uniqueXs.binarySearch(point.x).let { if (it >= 0) it else 0 }
            val xPosition = metrics.paddingX + (idx + 0.5f) * spacing
            val yPosition = metrics.paddingY + metrics.chartHeight - ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)).toFloat() * metrics.chartHeight
            Offset(xPosition, yPosition)
        }
    }
}