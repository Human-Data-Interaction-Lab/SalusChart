package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

// TODO: 누락된 시간 포인트에 0 값을 대입하면, min/max 계산 시 문제가 생길 여지 존재 
// - (min value가 따로 존재하는데도 최대/최소 계산 범위 내 누락된 시간 포인트로 인해 항상 0으로 처리되는 문제)
// - 이 문제가 실제로 생기는지 확인 필요 

// TODO: transform 모듈 내에서 로직 겹치는 함수 존재, Utils 파일로 빼야할 듯
// - ex. normalizeToTimeUnitDateTime, incrementByTimeUnit

/**
 * TemporalDataSet의 시간 간격을 채우는 확장 함수들
 * 
 * 시간 범위 내에서 누락된 시간 포인트를 0 값으로 채워서
 * 차트에서 연속적인 시간 축을 표시할 수 있게 합니다.
 */

/**
 * 시간 범위의 모든 간격을 0 값으로 채운 TemporalDataSet을 반환
 * 
 * 이 함수는 TemporalDataSet의 최소/최대 시간 사이의 모든 시간 포인트를 생성하고,
 * 누락된 시간 포인트는 0 값으로 채웁니다.
 * 
 * 사용 예시:
 * ```
 * val exerciseData = exercises.toTemporalDataSet()
 *     .transform(timeUnit = TimeUnitGroup.DAY)
 *     .fillTemporalGaps()  // 누락된 날짜를 0으로 채움
 *     .toChartMarks()
 * ```
 * 
 * @return 간격이 채워진 새로운 TemporalDataSet
 */
fun TemporalDataSet.fillTemporalGaps(): TemporalDataSet {
    if (x.isEmpty()) return this
    
    val minTime = x.minOrNull() ?: return this
    val maxTime = x.maxOrNull() ?: return this
    
    // 완전한 시간 간격 생성
    val completeTimePoints = generateCompleteTimePoints(minTime, maxTime, timeUnit)

    // 기존 데이터를 맵으로 변환 (빠른 조회를 위해)
    val normalizedExistingData = x.map { normalizeTimePoint(it, timeUnit) }
    
    return if (isSingleValue) {
        // 단일 값 처리
        val existingDataMap = normalizedExistingData.zip(y!!).toMap()
        
        val filledYValues = completeTimePoints.map { timePoint ->
            val normalized = normalizeTimePoint(timePoint, timeUnit)
            existingDataMap[normalized] ?: 0.0
        }
        
        TemporalDataSet(
            x = completeTimePoints,
            y = filledYValues,
            timeUnit = timeUnit
        )
    } else {
        // 다중 값 처리
        val existingDataMaps = yMultiple!!.mapValues { (_, values) ->
            normalizedExistingData.zip(values).toMap()
        }
        
        val filledYMultiple = existingDataMaps.mapValues { (_, dataMap) ->
            completeTimePoints.map { timePoint ->
                val normalized = normalizeTimePoint(timePoint, timeUnit)
                dataMap[normalized] ?: 0.0
            }
        }
        
        TemporalDataSet(
            x = completeTimePoints,
            yMultiple = filledYMultiple,
            timeUnit = timeUnit
        )
    }
}

/**
 * 시간 범위의 모든 간격을 생성
 */
private fun generateCompleteTimePoints(
    minTime: Instant,
    maxTime: Instant,
    timeUnit: TimeUnitGroup
): List<Instant> {
    val zoneId = ZoneId.systemDefault()
    val startDateTime = LocalDateTime.ofInstant(minTime, zoneId)
    val endDateTime = LocalDateTime.ofInstant(maxTime, zoneId)
    
    val normalizedStart = normalizeToTimeUnitDateTime(startDateTime, timeUnit)
    val normalizedEnd = normalizeToTimeUnitDateTime(endDateTime, timeUnit)
    
    val timePoints = mutableListOf<LocalDateTime>()
    var current = normalizedStart
    
    while (!current.isAfter(normalizedEnd)) {
        timePoints.add(current)
        current = incrementByTimeUnit(current, timeUnit)
    }
    
    return timePoints.map { it.atZone(zoneId).toInstant() }
}

/**
 * 시간 단위에 따라 LocalDateTime을 정규화
 */
 // TODO: DataTransformer의 groupBy'TimeUnit' 함수와 유사한 로직 존재 
private fun normalizeToTimeUnitDateTime(dateTime: LocalDateTime, timeUnit: TimeUnitGroup): LocalDateTime {
    return when (timeUnit) {
        TimeUnitGroup.MINUTE -> dateTime.truncatedTo(ChronoUnit.MINUTES)
        TimeUnitGroup.HOUR -> dateTime.truncatedTo(ChronoUnit.HOURS)
        TimeUnitGroup.DAY -> dateTime.truncatedTo(ChronoUnit.DAYS)
        TimeUnitGroup.WEEK -> {
            val sunday = dateTime.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            sunday.atStartOfDay()
        }
        TimeUnitGroup.MONTH -> LocalDateTime.of(dateTime.year, dateTime.month, 1, 0, 0)
        TimeUnitGroup.YEAR -> LocalDateTime.of(dateTime.year, 1, 1, 0, 0)
    }
}

/**
 * Instant를 시간 단위에 따라 정규화
 */
private fun normalizeTimePoint(instant: Instant, timeUnit: TimeUnitGroup): Instant {
    val zoneId = ZoneId.systemDefault()
    val dateTime = LocalDateTime.ofInstant(instant, zoneId)
    val normalized = normalizeToTimeUnitDateTime(dateTime, timeUnit)
    return normalized.atZone(zoneId).toInstant()
}

/**
 * 시간 단위에 따라 LocalDateTime 증가
 */
 // TODO: DataTransformer의 간격 끝 시간 계산 로직이 TemporalDataSetFillGaps와 중복됨
private fun incrementByTimeUnit(dateTime: LocalDateTime, timeUnit: TimeUnitGroup): LocalDateTime {
    return when (timeUnit) {
        TimeUnitGroup.MINUTE -> dateTime.plusMinutes(1)
        TimeUnitGroup.HOUR -> dateTime.plusHours(1)
        TimeUnitGroup.DAY -> dateTime.plusDays(1)
        TimeUnitGroup.WEEK -> dateTime.plusWeeks(1)
        TimeUnitGroup.MONTH -> dateTime.plusMonths(1)
        TimeUnitGroup.YEAR -> dateTime.plusYears(1)
    }
}

