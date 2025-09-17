package com.hdil.saluschart.core.chart

import androidx.compose.foundation.interaction.Interaction

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


sealed interface InteractionType {
    // 차트별 허용 집합을 타입으로 분리 - 각 차트 타입별 전용 sealed interface
    sealed interface Bar : InteractionType {
        data object BAR : Bar        // 바를 직접 터치
        data object TOUCH_AREA : Bar     // 바 영역 터치 (더 넓은 터치 영역)
    }
    
    sealed interface Line : InteractionType {
        data object POINT : Line        // 라인의 포인트 터치
        data object TOUCH_AREA : Line    // 라인 영역 터치
    }
    
    sealed interface Scatter : InteractionType {
        data object POINT : Scatter     // 스캐터 포인트 직접 터치
    }
    
    sealed interface StackedBar : InteractionType {
        data object BAR : StackedBar    // 개별 세그먼트 터치
        data object TOUCH_AREA : StackedBar  // 전체 바 영역 터치
    }
    
    sealed interface RangeBar : InteractionType {
        data object BAR : RangeBar       // 레인지 바 직접 터치
        data object TOUCH_AREA : RangeBar    // 레인지 바 영역 터치
    }

    sealed interface Pie : InteractionType {
        data object PIE : Pie       // 파이 섹션 직접 터치
    }

    // 일반적인 상호작용 (특정 차트에 제한되지 않음)
    data object None : InteractionType  // 상호작용 없음
}

enum class PointType {
    Circle,      // 원형 포인트
    Square,      // 사각형 포인트
    Triangle,    // 삼각형 포인트
}
