package com.hdil.saluschart.core.chart

// BaseChartMark is the base interface for all chart marks
// Sometimes used when multiple types of chart marks are used in the same chart
interface BaseChartMark {
    val x: Double
    val y: Double
    val label: String?
}

/**
 * 모든 차트에 사용되는 기본 데이터 포인트 클래스
 * 
 * @param x X축 위치 또는 인덱스
 * @param y Y축 값
 * @param label 라벨 
 * @param color 색상 (null인 경우 기본 색상 팔레트 사용)
 * @param isSelected 선택 상태 여부
 */
data class ChartMark(
    override val x: Double,
    override val y: Double,
    override val label: String? = null,
    val color: Int? = null,
    val isSelected: Boolean = false
) : BaseChartMark {
    override fun toString(): String {
        return "ChartMark(x=$x, y=$y, label=$label, color=$color, isSelected=$isSelected)"
    }
}

/**
 * 범위 바 차트를 위한 데이터 포인트 클래스
 * 
 * @param x X축 위치 또는 인덱스
 * @param minPoint 최소 값 데이터 포인트
 * @param maxPoint 최대 값 데이터 포인트
 * @param label 라벨
 */
data class RangeChartMark(
    override val x: Double,
    val minPoint: ChartMark,
    val maxPoint: ChartMark,
    override val label: String? = null
) : BaseChartMark {
    override val y: Double get() = maxPoint.y - minPoint.y
    override fun toString(): String {
        return "RangeChartMark(x=$x, minPoint=$minPoint, maxPoint=$maxPoint, label=$label)"
    }
}

/**
 * 스택 바 차트를 위한 데이터 포인트 클래스
 * 
 * @param x X축 위치 또는 인덱스
 * @param segments 세그먼트 목록 (ChartMark 리스트로 구성)
 * @param label 라벨
 */
data class StackedChartMark(
    override val x: Double,
    val segments: List<ChartMark>,
    override val label: String? = null
) : BaseChartMark {
    override val y: Double get() = segments.sumOf { it.y }
    override fun toString(): String {
        return "StackedChartMark(x=$x, segments=$segments, label=$label)"
    }
}

/**
 * 프로그레스 차트를 위한 데이터 포인트 클래스
 * 
 * @param x X축 위치 또는 인덱스 (0, 1, 2 for Move, Exercise, Stand)
 * @param current 현재 값
 * @param max 최대 값
 * @param label 라벨 (예: "Move", "Exercise", "Stand")
 * @param unit 단위 (예: "KJ", "min", "h")
 * @param color 색상 (null인 경우 기본 색상 팔레트 사용)
 * @param isSelected 선택 상태 여부
 */
data class ProgressChartMark(
    override val x: Double,
    val current: Double,
    val max: Double,
    override val label: String? = null,
    val unit: String? = null,
    val color: Int? = null,
    val isSelected: Boolean = false
) : BaseChartMark {
    val progress: Double = if (max > 0.0) (current / max).coerceIn(0.0, 1.0) else 0.0
    val percentage: Double = progress * 100.0
    override val y: Double get() = progress

    override fun toString(): String {
        return "ProgressChartMark(x=$x, current=$current, max=$max, progress=$progress, label=$label, unit=$unit)"
    }
}
