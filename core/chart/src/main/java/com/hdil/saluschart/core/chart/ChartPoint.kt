package com.hdil.saluschart.core.chart

interface BaseChartPoint {
    val x: Float
    val y: Float
    val label: String?
}

data class ChartPoint(
    override val x: Float,
    override val y: Float,
    override val label: String? = null,
    val color: Int? = null,
    val isSelected: Boolean = false
) : BaseChartPoint {
    override fun toString(): String {
        return "ChartPoint(x=$x, y=$y, label=$label, color=$color, isSelected=$isSelected)"
    }
}

/**
 * 범위 바 차트를 위한 데이터 포인트 클래스
 */
data class RangeChartPoint(
    override val x: Float,
    val minPoint: ChartPoint,
    val maxPoint: ChartPoint,
    override val label: String? = null
) : BaseChartPoint {
    override val y: Float get() = maxPoint.y - minPoint.y
    override fun toString(): String {
        return "RangeChartPoint(x=$x, minPoint=$minPoint, maxPoint=$maxPoint, label=$label)"
    }
}

/**
 * 스택 바 차트를 위한 데이터 포인트 클래스
 */
data class StackedChartPoint(
    override val x: Float,
    val segments: List<ChartPoint>,
    override val label: String? = null
) : BaseChartPoint {
    override val y: Float get() = segments.sumOf { it.y.toDouble() }.toFloat()
    override fun toString(): String {
        return "StackedChartPoint(x=$x, segments=$segments, label=$label)"
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
data class ProgressChartPoint(
    override val x: Float,
    val current: Float,
    val max: Float,
    override val label: String? = null,
    val unit: String? = null,
    val color: Int? = null,
    val isSelected: Boolean = false
) : BaseChartPoint {
    val progress: Float = if (max > 0f) (current / max).coerceIn(0f, 1f) else 0f
    val percentage: Float = progress * 100f
    override val y: Float get() = progress

    override fun toString(): String {
        return "ProgressChartPoint(x=$x, current=$current, max=$max, progress=$progress, label=$label, unit=$unit)"
    }
}
