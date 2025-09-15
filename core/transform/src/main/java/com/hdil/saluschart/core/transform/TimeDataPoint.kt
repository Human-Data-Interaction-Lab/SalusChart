package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.core.util.AggregationType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek


/**
 * 시간 기반 원시 데이터 포인트
 * ChartPoint로 변환해서 사용
 * @param x 시간 데이터 (Instant 타입)
 * @param y 측정값 리스트 (단일 값용)
 * @param yMultiple 다중 측정값 맵 (다중 값용) - 키는 속성명, 값은 해당 속성의 값 리스트
 * @param timeUnit 시간 단위
 */
data class TimeDataPoint(
    val x : List<Instant>,
    val y : List<Float>? = null,
    val yMultiple : Map<String, List<Float>>? = null,
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
    fun getValues(property: String): List<Float>? = yMultiple?.get(property)    // 다중 값에서 특정 속성의 값 가져오기
    val propertyNames: Set<String> get() = yMultiple?.keys ?: emptySet()    // 다중 값의 속성명 목록 가져오기
}

/**
 * TimeDataPoint를 ChartPoint 리스트로 변환하는 확장 함수
 * 단일 값 데이터용
 *
 * @return ChartPoint 리스트
 *
 * 각 시간 단위에 따라 레이블이 생성됩니다:
 * HOUR: "14시" (for 2 PM)
 * DAY: "5/8 월" (for May 8th Monday, includes day of week)
 * WEEK: "5월 1주차" (for first week of May)
 * MONTH: "2025년 5월" (for May 2025)
 * YEAR: "2025년" (for year 2025)
 */
fun TimeDataPoint.toChartPoints(): List<ChartPoint> {
    require(isSingleValue) { "Use toChartPointsByProperty() for multi-value TimeDataPoint" }
    
    val labels = generateTimeLabels()

    return x.indices.map { index ->
        ChartPoint(
            x = index.toFloat(),
            y = y!![index],
            label = labels.getOrNull(index) ?: x.getOrNull(index)?.toString()
        )
    }
}

/**
 * 다중 값 TimeDataPoint에서 특정 속성을 추출하여 ChartPoint 리스트로 변환하는 확장 함수
 *
 * @param property 추출할 속성명 (예: "systolic", "diastolic", "calories", "protein" 등)
 * @return 해당 속성의 ChartPoint 리스트
 */
fun TimeDataPoint.toChartPointsByProperty(property: String): List<ChartPoint> {
    require(isMultiValue) { "Use toChartPoints() for single-value TimeDataPoint" }
    require(propertyNames.contains(property)) { 
        "Property '$property' not found. Available properties: ${propertyNames.joinToString()}" 
    }
    
    val values = getValues(property)!!
    val labels = generateTimeLabels()

    return x.indices.map { index ->
        ChartPoint(
            x = index.toFloat(),
            y = values[index],
            label = labels.getOrNull(index) ?: x.getOrNull(index)?.toString()
        )
    }
}

/**
 * 다중 값 TimeDataPoint를 모든 속성별로 분리된 ChartPoint 맵으로 변환하는 확장 함수
 *
 * @return 속성명을 키로 하고 해당 속성의 ChartPoint 리스트를 값으로 하는 맵
 */
fun TimeDataPoint.toChartPointsMap(): Map<String, List<ChartPoint>> {
    require(isMultiValue) { "Use toChartPoints() for single-value TimeDataPoint" }
    
    val labels = generateTimeLabels()
    
    return propertyNames.associateWith { property ->
        val values = getValues(property)!!
        x.indices.map { index ->
            ChartPoint(
                x = index.toFloat(),
                y = values[index],
                label = labels.getOrNull(index) ?: x.getOrNull(index)?.toString()
            )
        }
    }
}

/**
 * 시간 단위에 따른 레이블 생성 (공통 로직)
 */
private fun TimeDataPoint.generateTimeLabels(): List<String> {
    return when (timeUnit) {
        TimeUnitGroup.MINUTE -> {
            x.map { instant ->
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                "${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
            }
        }
        TimeUnitGroup.HOUR -> {
            x.map { instant ->
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                "${dateTime.hour}시"
            }
        }
        TimeUnitGroup.DAY -> {
            x.map { instant ->
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                val dayOfWeekKorean = when (dateTime.dayOfWeek.value) {
                    1 -> "월"
                    2 -> "화"
                    3 -> "수"
                    4 -> "목"
                    5 -> "금"
                    6 -> "토"
                    7 -> "일"
                    else -> "?"
                }
                "${dateTime.monthValue}/${dateTime.dayOfMonth} $dayOfWeekKorean"
            }
        }
        TimeUnitGroup.WEEK -> {
            x.map { instant ->
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                val sunday = dateTime.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                val firstSundayOfMonth = LocalDate.of(sunday.year, sunday.month, 1)
                    .let { firstDay ->
                        val dayOfWeek = firstDay.dayOfWeek.value
                        if (dayOfWeek == 7) firstDay else firstDay.plusDays((7 - dayOfWeek).toLong())
                    }
                val weekNumber = ((sunday.toEpochDay() - firstSundayOfMonth.toEpochDay()) / 7 + 1).toInt()
                "${sunday.monthValue}월 ${weekNumber}주차"
            }
        }
        TimeUnitGroup.MONTH -> {
            x.map { instant ->
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                "${dateTime.year}년 ${dateTime.monthValue}월"
            }
        }
        TimeUnitGroup.YEAR -> {
            x.map { instant ->
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                "${dateTime.year}년"
            }
        }
    }
}
