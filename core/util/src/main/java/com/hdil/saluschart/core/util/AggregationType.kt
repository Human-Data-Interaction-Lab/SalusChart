package com.hdil.saluschart.core.util

/**
 * 시간 단위 변환 시 데이터 집계 방법을 정의하는 열거형
 */
enum class AggregationType {
    /** 합계 계산 */
    SUM,
    /** 일일 평균 계산 */
    DAILY_AVERAGE,
    /** 시간 지속 시간 합계 계산 (분 단위로 카운트) */
    DURATION_SUM,
    /** 최소값과 최대값 계산 (범위 차트용) */
    MIN_MAX
}
