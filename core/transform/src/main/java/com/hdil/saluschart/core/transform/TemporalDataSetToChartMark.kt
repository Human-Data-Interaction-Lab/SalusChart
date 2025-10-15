package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.util.TimeUnitGroup
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

/**
 * TemporalDataSet를 ChartMark 리스트로 변환하는 확장 함수
 * 단일 값 데이터용
 *
 * @param fillGaps true인 경우 시간 범위 내 누락된 시간 포인트를 0으로 채움 (기본값: true)
 * @return ChartMark 리스트
 *
 * 각 시간 단위에 따라 레이블이 생성됩니다:
 * HOUR: "14시" (for 2 PM)
 * DAY: "5/8 월" (for May 8th Monday, includes day of week)
 * WEEK: "5월 1주차" (for first week of May)
 * MONTH: "2025년 5월" (for May 2025)
 * YEAR: "2025년" (for year 2025)
 */
fun TemporalDataSet.toChartMarks(fillGaps: Boolean = true): List<ChartMark> {
    require(isSingleValue) { "Use toChartMarksByProperty() for multi-value TemporalDataSet" }
    
    // Fill temporal gaps before converting to ChartMarks
    val dataToConvert = if (fillGaps) this.fillTemporalGaps() else this
    
    val labels = dataToConvert.generateTimeLabels()

    return dataToConvert.x.indices.map { index ->
        ChartMark(
            x = index.toDouble(),
            y = dataToConvert.y!![index],
            label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
        )
    }
}

/**
 * 다중 값 TemporalDataSet에서 특정 속성을 추출하여 ChartMark 리스트로 변환하는 확장 함수
 *
 * @param property 추출할 속성명 (예: "systolic", "diastolic", "calories", "protein" 등)
 * @param fillGaps true인 경우 시간 범위 내 누락된 시간 포인트를 0으로 채움 (기본값: true)
 * @return 해당 속성의 ChartMark 리스트
 */
fun TemporalDataSet.toChartMarksByProperty(property: String, fillGaps: Boolean = true): List<ChartMark> {
    require(isMultiValue) { "Use toChartMarks() for single-value TemporalDataSet" }
    require(propertyNames.contains(property)) { 
        "Property '$property' not found. Available properties: ${propertyNames.joinToString()}" 
    }
    
    // Fill temporal gaps before converting to ChartMarks
    val dataToConvert = if (fillGaps) this.fillTemporalGaps() else this
    
    val values = dataToConvert.getValues(property)!!
    val labels = dataToConvert.generateTimeLabels()

    return dataToConvert.x.indices.map { index ->
        ChartMark(
            x = index.toDouble(),
            y = values[index],
            label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
        )
    }
}

/**
 * 다중 값 TemporalDataSet를 모든 속성별로 분리된 ChartMark 맵으로 변환하는 확장 함수
 *
 * @param fillGaps true인 경우 시간 범위 내 누락된 시간 포인트를 0으로 채움 (기본값: true)
 * @return 속성명을 키로 하고 해당 속성의 ChartMark 리스트를 값으로 하는 맵
 */
fun TemporalDataSet.toChartMarksMap(fillGaps: Boolean = true): Map<String, List<ChartMark>> {
    require(isMultiValue) { "Use toChartMarks() for single-value TemporalDataSet" }
    
    // Fill temporal gaps before converting to ChartMarks
    val dataToConvert = if (fillGaps) this.fillTemporalGaps() else this
    
    val labels = dataToConvert.generateTimeLabels()
    
    return dataToConvert.propertyNames.associateWith { property ->
        val values = dataToConvert.getValues(property)!!
        dataToConvert.x.indices.map { index ->
            ChartMark(
                x = index.toDouble(),
                y = values[index],
                label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
            )
        }
    }
}

/**
 * MIN_MAX 집계된 TemporalDataSet을 RangeChartMark 리스트로 변환하는 확장 함수
 * 
 * MIN_MAX aggregation으로 생성된 다중 값 TemporalDataSet (min, max 속성 포함)을
 * RangeChartMark 리스트로 변환합니다.
 * 
 * @param fillGaps true인 경우 시간 범위 내 누락된 시간 포인트를 min=0, max=0으로 채움 (기본값: true)
 * @return RangeChartMark 리스트
 * @throws IllegalArgumentException 단일 값 데이터이거나 min/max 속성이 없는 경우
 * 
 * 사용 예:
 * ```
 * val heartRateRange = heartRates.toHeartRateTemporalDataSet()
 *     .transform(timeUnit = TimeUnitGroup.DAY, aggregationType = AggregationType.MIN_MAX)
 *     .toRangeChartMarks()
 * RangeBarChart(data = heartRateRange, ...)
 * ```
 * 
 * 참고: fillGaps가 true인 경우, 데이터가 없는 날짜는 min=0, max=0으로 표시됩니다.
 * 이는 "데이터가 기록되지 않은 날"을 의미하며, 실제 측정값의 최소/최대와는 구분됩니다.
 */
fun TemporalDataSet.toRangeChartMarks(fillGaps: Boolean = true): List<RangeChartMark> {
    require(isMultiValue) { "toRangeChartMarks() requires multi-value TemporalDataSet from MIN_MAX aggregation" }
    require(propertyNames.contains("min") && propertyNames.contains("max")) {
        "TemporalDataSet must contain 'min' and 'max' properties. " +
        "Available properties: ${propertyNames.joinToString()}. " +
        "Use transform(aggregationType = AggregationType.MIN_MAX) to create min/max data."
    }
    
    // Fill temporal gaps before converting to ChartMarks
    val dataToConvert = if (fillGaps) this.fillTemporalGaps() else this
    
    val minValues = dataToConvert.getValues("min")!!
    val maxValues = dataToConvert.getValues("max")!!
    val labels = dataToConvert.generateTimeLabels()
    
    return dataToConvert.x.indices.map { index ->
        RangeChartMark(
            x = index.toDouble(),
            minPoint = ChartMark(
                x = index.toDouble(),
                y = minValues[index],
                label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
            ),
            maxPoint = ChartMark(
                x = index.toDouble(),
                y = maxValues[index],
                label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
            ),
            label = labels.getOrNull(index) ?: dataToConvert.x.getOrNull(index)?.toString()
        )
    }
}

/**
 * 시간 단위에 따른 레이블 생성 (공통 로직)
 */
fun TemporalDataSet.generateTimeLabels(): List<String> {
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
