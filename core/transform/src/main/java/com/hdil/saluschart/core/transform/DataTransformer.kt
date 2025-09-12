package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.core.util.AggregationType
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * 시간 기반 데이터 변환 엔진
 */
class DataTransformer {

    /**
     * 시간 기반 데이터를 차트 포인트로 변환
     * @param data 원시 시간 데이터 리스트
     * @param transformTimeUnit 그룹핑할 시간 단위
     * @param aggregationType 집계 방법 (합계 또는 평균)
     */
    fun transform(
        data: TimeDataPoint,
        transformTimeUnit: TimeUnitGroup,
        aggregationType: AggregationType = AggregationType.SUM
    ): TimeDataPoint {

        // 평균 계산 시 유효성 검증
        if (aggregationType == AggregationType.AVERAGE) {
            // 원본 시간 단위가 변환 시간 단위보다 작거나 같아야 함
            require(data.timeUnit.isSmallerThanOrEqual(transformTimeUnit)) {
                "평균 계산을 위해서는 원본 시간 단위(${data.timeUnit})가 변환 시간 단위($transformTimeUnit)보다 작거나 같아야 합니다."
            }
        }

        // 같은 시간 단위이고 합계 계산인 경우 그대로 반환
        if (data.timeUnit == transformTimeUnit && aggregationType == AggregationType.SUM) {
            return data
        }

        val transformedData = groupByTimeUnit(data, transformTimeUnit, aggregationType)
        return transformedData
    }

    /**
     * 시간 단위별 그룹핑
     */
    private fun groupByTimeUnit(
        data: TimeDataPoint,
        targetTimeUnit: TimeUnitGroup,
        aggregationType: AggregationType
    ): TimeDataPoint {

        // Instant를 LocalDateTime으로 변환
        val parsedTimes = data.x.map { instant ->
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        }

        return if (data.isSingleValue) {
            // 단일 값 처리
            val timeValuePairs = parsedTimes.zip(data.y!!)
            val aggregatedData = processAggregation(timeValuePairs, targetTimeUnit, aggregationType)
            
            val newXValues = aggregatedData.map { (time, _) ->
                time.atZone(ZoneId.systemDefault()).toInstant()
            }
            val newYValues = aggregatedData.map { it.second }

            TimeDataPoint(
                x = newXValues,
                y = newYValues,
                timeUnit = targetTimeUnit
            )
        } else {
            // 다중 값 처리
            val aggregatedMultipleData = mutableMapOf<String, List<Pair<LocalDateTime, Float>>>()
            
            // 각 속성별로 집계 수행
            data.yMultiple!!.forEach { (property, values) ->
                val timeValuePairs = parsedTimes.zip(values)
                aggregatedMultipleData[property] = processAggregation(timeValuePairs, targetTimeUnit, aggregationType)
            }
            
            // 모든 속성이 동일한 시간 키를 가져야 하므로 첫 번째 속성의 시간을 기준으로 사용
            val firstProperty = aggregatedMultipleData.keys.first()
            val newXValues = aggregatedMultipleData[firstProperty]!!.map { (time, _) ->
                time.atZone(ZoneId.systemDefault()).toInstant()
            }
            
            // 각 속성의 값들을 새로운 yMultiple 맵으로 구성
            val newYMultiple = aggregatedMultipleData.mapValues { (_, timeValueList) ->
                timeValueList.map { it.second }
            }

            TimeDataPoint(
                x = newXValues,
                yMultiple = newYMultiple,
                timeUnit = targetTimeUnit,
            )
        }
    }

    /**
     * 시간-값 쌍에 대한 집계 처리 (공통 로직)
     */
    private fun processAggregation(
        timeValuePairs: List<Pair<LocalDateTime, Float>>,
        targetTimeUnit: TimeUnitGroup,
        aggregationType: AggregationType
    ): List<Pair<LocalDateTime, Float>> {
        return when (aggregationType) {
            AggregationType.SUM -> {
                val groupedData = when (targetTimeUnit) {
                    TimeUnitGroup.MINUTE -> timeValuePairs.map { it.first to listOf(it.second) }.toMap()
                    TimeUnitGroup.HOUR -> groupByHour(timeValuePairs)
                    TimeUnitGroup.DAY -> groupByDay(timeValuePairs)
                    TimeUnitGroup.WEEK -> groupByWeek(timeValuePairs)
                    TimeUnitGroup.MONTH -> groupByMonth(timeValuePairs)
                    TimeUnitGroup.YEAR -> groupByYear(timeValuePairs)
                }
                
                groupedData.map { (time, values) ->
                    time to values.sum()
                }.sortedBy { it.first }
            }
            AggregationType.AVERAGE -> {
                calculateIntervalAverages(timeValuePairs, targetTimeUnit)
            }
        }
    }

    /**
     * 시간별 그룹핑
     */
    private fun groupByHour(timeValuePairs: List<Pair<LocalDateTime, Float>>): Map<LocalDateTime, List<Float>> {
        return timeValuePairs.groupBy { (time, _) ->
            time.truncatedTo(ChronoUnit.HOURS)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /**
     * 일별 그룹핑
     */
    private fun groupByDay(timeValuePairs: List<Pair<LocalDateTime, Float>>): Map<LocalDateTime, List<Float>> {
        return timeValuePairs.groupBy { (time, _) ->
            time.truncatedTo(ChronoUnit.DAYS)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /**
     * 주별 그룹핑 (일요일 기준)
     */
    private fun groupByWeek(timeValuePairs: List<Pair<LocalDateTime, Float>>): Map<LocalDateTime, List<Float>> {
        return timeValuePairs.groupBy { (time, _) ->
            // 일요일을 기준으로 해당 주의 시작 날짜 계산
            val sunday = time.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            sunday.atStartOfDay()
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /**
     * 월별 그룹핑
     */
    private fun groupByMonth(timeValuePairs: List<Pair<LocalDateTime, Float>>): Map<LocalDateTime, List<Float>> {
        return timeValuePairs.groupBy { (time, _) ->
            LocalDateTime.of(time.year, time.month, 1, 0, 0)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /**
     * 연별 그룹핑
     */
    private fun groupByYear(timeValuePairs: List<Pair<LocalDateTime, Float>>): Map<LocalDateTime, List<Float>> {
        return timeValuePairs.groupBy { (time, _) ->
            LocalDateTime.of(time.year, 1, 1, 0, 0)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }
    /**
     * 간격 기반 평균 계산
     * 데이터를 완전한 간격으로 나누고 각 간격의 평균을 계산
     */
    private fun calculateIntervalAverages(
        timeValuePairs: List<Pair<LocalDateTime, Float>>,
        targetTimeUnit: TimeUnitGroup
    ): List<Pair<LocalDateTime, Float>> {
        if (timeValuePairs.isEmpty()) return emptyList()

        // 데이터의 시간 범위 결정
        val minTime = timeValuePairs.minByOrNull { it.first }!!.first
        val maxTime = timeValuePairs.maxByOrNull { it.first }!!.first
        
        // 완전한 간격 생성
        val intervals = generateCompleteIntervals(minTime, maxTime, targetTimeUnit)
        
        // 각 간격에 대해 평균 계산
        return intervals.map { intervalStart ->
            val intervalEnd = getIntervalEnd(intervalStart, targetTimeUnit)
            
            // 해당 간격에 속하는 데이터 포인트들 필터링
            val dataInInterval = timeValuePairs.filter { (time, _) ->
                time >= intervalStart && time < intervalEnd
            }
            
            // 평균 계산 (실제 데이터 포인트 수로 나눔)
            val average = if (dataInInterval.isNotEmpty()) {
                dataInInterval.map { it.second }.average().toFloat()
            } else {
                0f // 데이터가 없는 간격은 0
            }
            
            intervalStart to average
        }.sortedBy { it.first }
    }
    
    /**
     * 시간 범위에 대해 완전한 간격 생성
     */
    private fun generateCompleteIntervals(
        minTime: LocalDateTime,
        maxTime: LocalDateTime,
        targetTimeUnit: TimeUnitGroup
    ): List<LocalDateTime> {
        val intervals = mutableListOf<LocalDateTime>()
        
        when (targetTimeUnit) {
            TimeUnitGroup.MINUTE -> {
                var current = minTime.truncatedTo(ChronoUnit.MINUTES)
                val end = maxTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)
                while (current.isBefore(end)) {
                    intervals.add(current)
                    current = current.plusMinutes(1)
                }
            }
            TimeUnitGroup.HOUR -> {
                var current = minTime.truncatedTo(ChronoUnit.HOURS)
                val end = maxTime.truncatedTo(ChronoUnit.HOURS).plusHours(1)
                while (current.isBefore(end)) {
                    intervals.add(current)
                    current = current.plusHours(1)
                }
            }
            TimeUnitGroup.DAY -> {
                var current = minTime.truncatedTo(ChronoUnit.DAYS)
                val end = maxTime.truncatedTo(ChronoUnit.DAYS).plusDays(1)
                while (current.isBefore(end)) {
                    intervals.add(current)
                    current = current.plusDays(1)
                }
            }
            TimeUnitGroup.WEEK -> {
                var current = minTime.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atStartOfDay()
                val endDate = maxTime.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                while (current.toLocalDate().isBefore(endDate)) {
                    intervals.add(current)
                    current = current.plusWeeks(1)
                }
            }
            TimeUnitGroup.MONTH -> {
                var current = LocalDateTime.of(minTime.year, minTime.month, 1, 0, 0)
                val endYear = maxTime.year
                val endMonth = maxTime.monthValue
                while (current.year < endYear || (current.year == endYear && current.monthValue <= endMonth)) {
                    intervals.add(current)
                    current = current.plusMonths(1)
                }
            }
            TimeUnitGroup.YEAR -> {
                var current = LocalDateTime.of(minTime.year, 1, 1, 0, 0)
                val endYear = maxTime.year
                while (current.year <= endYear) {
                    intervals.add(current)
                    current = current.plusYears(1)
                }
            }
        }
        
        return intervals
    }
    
    /**
     * 간격의 끝 시간 계산
     */
    private fun getIntervalEnd(intervalStart: LocalDateTime, targetTimeUnit: TimeUnitGroup): LocalDateTime {
        return when (targetTimeUnit) {
            TimeUnitGroup.MINUTE -> intervalStart.plusMinutes(1)
            TimeUnitGroup.HOUR -> intervalStart.plusHours(1)
            TimeUnitGroup.DAY -> intervalStart.plusDays(1)
            TimeUnitGroup.WEEK -> intervalStart.plusWeeks(1)
            TimeUnitGroup.MONTH -> intervalStart.plusMonths(1)
            TimeUnitGroup.YEAR -> intervalStart.plusYears(1)
        }
    }
}