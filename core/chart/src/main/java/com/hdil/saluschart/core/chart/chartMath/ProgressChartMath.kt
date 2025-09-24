package com.hdil.saluschart.core.chart.chartMath

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.hdil.saluschart.core.chart.ProgressChartPoint
import kotlin.math.cos
import kotlin.math.sin

object ProgressChartMath {
    
    /**
     * 프로그레스 도넛 차트의 메트릭을 계산합니다.
     * 
     * @param size 캔버스 크기
     * @param data 프로그레스 차트 데이터 리스트
     * @param padding 도넛 테두리 패딩 값
     * @param strokeWidth 각 링의 두께
     * @param ringSpacing 링 간의 간격
     * @return Triple<중심 좌표, 최대 반지름, 링별 반지름 리스트>
     */
    fun computeProgressDonutMetrics(
        size: Size, 
        data: List<ProgressChartPoint>,
        padding: Float = 32f,
        strokeWidth: Float = 40f,
        ringSpacing: Float = 8f
    ): Triple<Offset, Float, List<Float>> {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = minOf(size.width, size.height) / 2 - padding
        
        // 각 링의 반지름 계산 (바깥쪽부터 안쪽으로)
        val ringRadii = mutableListOf<Float>()
        val instanceCount = data.size
        
        for (i in 0 until instanceCount) {
            val ringRadius = maxRadius - (i * (strokeWidth + ringSpacing))
            if (ringRadius > strokeWidth / 2) {
                ringRadii.add(ringRadius)
            }
        }
        
        return Triple(center, maxRadius, ringRadii)
    }
    
    /**
     * 프로그레스 바 차트의 메트릭을 계산합니다.
     * 
     * @param size 캔버스 크기
     * @param data 프로그레스 차트 데이터 리스트
     * @param padding 패딩 값
     * @param barHeight 각 바의 높이
     * @param barSpacing 바 간의 간격
     * @return Pair<바 너비, 바별 Y 위치 리스트>
     */
    fun computeProgressBarMetrics(
        size: Size,
        data: List<ProgressChartPoint>,
        padding: Float = 40f,
        barHeight: Float = 30f,
        barSpacing: Float = 20f
    ): Pair<Float, List<Float>> {
        val barWidth = size.width - (padding * 2)
        val instanceCount = data.size
        
        // 전체 높이에서 바들과 간격들이 차지하는 높이 계산
        val totalBarsHeight = instanceCount * barHeight
        val totalSpacingHeight = (instanceCount - 1) * barSpacing
        val totalContentHeight = totalBarsHeight + totalSpacingHeight
        
        // 수직 중앙 정렬을 위한 시작 Y 위치
        val startY = (size.height - totalContentHeight) / 2f
        
        // 각 바의 Y 위치 계산
        val barYPositions = mutableListOf<Float>()
        for (i in 0 until instanceCount) {
            val y = startY + (i * (barHeight + barSpacing))
            barYPositions.add(y)
        }
        
        return Pair(barWidth, barYPositions)
    }
    
    /**
     * 프로그레스 도넛의 각 링에 대한 각도를 계산합니다.
     * 
     * @param data 프로그레스 차트 데이터 리스트
     * @return List<Pair<시작 각도, 스윕 각도>>
     */
    fun computeProgressAngles(data: List<ProgressChartPoint>): List<Pair<Float, Float>> {
        return data.map { point ->
            val startAngle = -90f // 12시 방향에서 시작
            val sweepAngle = (point.progress * 360.0).toFloat()
            Pair(startAngle, sweepAngle)
        }
    }
    
    /**
     * 프로그레스 라벨의 위치를 계산합니다.
     * 
     * @param center 중심점
     * @param radius 반지름
     * @param isDonut 도넛 차트 여부
     * @param point 프로그레스 차트 포인트
     * @param strokeWidth 도넛 차트의 링 두께
     * @param barY 바 차트일 경우 바의 Y 위치
     * @param barWidth 바 차트일 경우 바의 너비
     * @return 라벨 위치
     */
    fun computeLabelPosition(
        center: Offset,
        radius: Float = 0f,
        isDonut: Boolean,
        point: ProgressChartPoint,
        strokeWidth: Float = 40f,
        barY: Float = 0f,
        barWidth: Float = 0f
    ): Offset {
        return if (isDonut) {
            val startAngle = -90f // 12시 방향
            val startAngleRadians = Math.toRadians(startAngle.toDouble())

            val labelRadius = radius - (strokeWidth / 2f)
            val x = center.x + (labelRadius * cos(startAngleRadians)).toFloat()
            val y = center.y + (labelRadius * sin(startAngleRadians)).toFloat()

            Offset(x, y)
        } else {
            // 바 차트: 바의 왼쪽에 위치
            Offset(center.x - barWidth / 2f - 20f, barY + 15f)
        }
    }
    
    /**
     * 프로그레스 값 텍스트의 위치를 계산합니다.
     * 
     * @param center 중심점
     * @param radius 반지름
     * @param isDonut 도넛 차트 여부
     * @param point 프로그레스 차트 포인트
     * @param barY 바 차트일 경우 바의 Y 위치
     * @param barWidth 바 차트일 경우 바의 너비
     * @return 값 텍스트 위치
     */
    fun computeValuePosition(
        center: Offset,
        radius: Float = 0f,
        isDonut: Boolean,
        point: ProgressChartPoint,
        barY: Float = 0f,
        barWidth: Float = 0f
    ): Offset {
        return if (isDonut) {
            // 도넛 차트: 프로그레스 아크의 끝 지점에 위치
            val startAngle = -90f // 12시 방향에서 시작
            val sweepAngle = point.progress * 360f
            val endAngle = startAngle + sweepAngle
            
            // 각도를 라디안으로 변환
            val endAngleRadians = Math.toRadians(endAngle.toDouble())
            
            // 아크 끝 지점의 좌표 계산
            val textRadius = radius + 15f // 아크보다 조금 바깥쪽에 위치
            val x = center.x + (textRadius * cos(endAngleRadians)).toFloat()
            val y = center.y + (textRadius * sin(endAngleRadians)).toFloat()
            
            Offset(x, y)
        } else {
            // 바 차트: 바의 오른쪽에 위치
            Offset(center.x + barWidth / 2f + 20f, barY + 15f)
        }
    }
}
