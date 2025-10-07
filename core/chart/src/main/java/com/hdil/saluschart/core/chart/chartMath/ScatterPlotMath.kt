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
     */
    fun mapScatterToCanvasPoints(data: List<ChartMark>, size: Size, metrics: ChartMath.ChartMetrics): List<Offset> {
        if (data.isEmpty()) return emptyList()
        
        // X축 범위 계산
        val minX = data.minOf { it.x }
        val maxX = data.maxOf { it.x }
        val xRange = if (maxX > minX) (maxX - minX).toFloat() else 1f
        
        return data.map { point ->
            // X값을 실제 좌표계에 매핑
            val xPosition = if (xRange > 0f) {
                metrics.paddingX + ((point.x - minX).toFloat() / xRange) * metrics.chartWidth
            } else {
                metrics.paddingX + metrics.chartWidth / 2f
            }
            
            // Y값을 좌표계에 매핑
            val yPosition = metrics.paddingY + metrics.chartHeight - ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)).toFloat() * metrics.chartHeight

            Offset(xPosition, yPosition)
        }
    }
}