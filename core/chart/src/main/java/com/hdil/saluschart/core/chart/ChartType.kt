package com.hdil.saluschart.core.chart

enum class ChartType {
    LINE,
    BAR,
    RANGE_BAR,
    STACKED_BAR,
    PIE,
    PROGRESS,
    SCATTERPLOT,
    SLEEPSTAGE_CHART,
    CALENDAR,
    MINIMAL_BAR,
    MINIMAL_LINE,
    MINIMAL_RANGE_BAR,
    MINIMAL_GAUGE;

    companion object {
        fun fromString(type: String): ChartType? {
            return values().find { it.name.equals(type, ignoreCase = true) }
        }
    }
}

//enum class InteractionType {
//    POINT,         // Direct point touching using PointMarker()
//    TOUCH_AREA,    // Area-based touching using BarMarker()
//    BAR,           // Bar touching using BarMarker()
//    STACKED_BAR;   // Individual segment touching in stacked bar charts
//}

sealed interface InteractionType {
    // 차트별 허용 집합을 타입으로 분리
    sealed interface Bar : InteractionType
    sealed interface Line : InteractionType
    sealed interface StackedBar : InteractionType

    data object BAR        : Bar, Line, InteractionType
    data object TOUCH_AREA : Bar, Line, InteractionType

    data object STACKED_BAR: StackedBar

    data object POINT      : InteractionType
}


enum class PointType {
    Circle,      // 원형 포인트
    Square,      // 사각형 포인트
    Triangle,    // 삼각형 포인트
}
