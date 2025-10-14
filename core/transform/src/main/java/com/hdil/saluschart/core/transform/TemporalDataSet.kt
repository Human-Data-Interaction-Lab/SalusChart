package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import java.time.Instant

/**
 * 시간 기반 원시 데이터 포인트
 * ChartMark로 변환해서 사용
 * @param x 시간 데이터 (Instant 타입)
 * @param y 측정값 리스트 (단일 값용)
 * @param yMultiple 다중 측정값 맵 (다중 값용) - 키는 속성명, 값은 해당 속성의 값 리스트
 * @param timeUnit 시간 단위
 */
data class TemporalDataSet(
    val x : List<Instant>,
    val y : List<Double>? = null,
    val yMultiple : Map<String, List<Double>>? = null,
    val timeUnit : TimeUnitGroup = TimeUnitGroup.HOUR
) {
    init {
        require((y != null) xor (yMultiple != null)) {
            "Either y or yMultiple must be provided, but not both"
        }
        
        if (y != null) {
            require(x.size == y.size) {
                "x and y lists must have the same size"
            }
        }
        
        if (yMultiple != null) {
            require(yMultiple.isNotEmpty()) {
                "yMultiple cannot be empty"
            }
            yMultiple.values.forEach { valueList ->
                require(x.size == valueList.size) {
                    "All value lists in yMultiple must have the same size as x"
                }
            }
        }
    }
    
    val isSingleValue: Boolean get() = y != null     // 단일 값 여부 확인
    val isMultiValue: Boolean get() = yMultiple != null    // 다중 값 여부 확인
    fun getValues(property: String): List<Double>? = yMultiple?.get(property)    // 다중 값에서 특정 속성의 값 가져오기
    val propertyNames: Set<String> get() = yMultiple?.keys ?: emptySet()    // 다중 값의 속성명 목록 가져오기
}
