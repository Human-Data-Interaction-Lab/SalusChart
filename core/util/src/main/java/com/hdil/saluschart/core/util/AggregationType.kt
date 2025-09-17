package com.hdil.saluschart.core.util

/**
 * 시간 단위 변환 시 데이터 집계 방법을 정의하는 열거형
 */
enum class AggregationType {
    /** 합계 계산 */
    SUM,
    /** 일일 평균 계산 */
    DAILY_AVERAGE
}
