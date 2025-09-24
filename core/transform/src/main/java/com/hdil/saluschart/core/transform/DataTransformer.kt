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
        if (aggregationType == AggregationType.DAILY_AVERAGE) {
            // 원본 시간 단위가 변환 시간 단위보다 작거나 같아야 함
            require(TimeUnitGroup.DAY.isSmallerThanOrEqual(transformTimeUnit)) {
                "평균 계산을 위해서는 변환 시간 단위($transformTimeUnit)가 일 단위보다 크거나 같아야 합니다."
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
            val aggregatedMultipleData = mutableMapOf<String, List<Pair<LocalDateTime, Double>>>()
            
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
        timeValuePairs: List<Pair<LocalDateTime, Double>>,
        targetTimeUnit: TimeUnitGroup,
        aggregationType: AggregationType
    ): List<Pair<LocalDateTime, Double>> {
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
            AggregationType.DAILY_AVERAGE -> {
                calculateIntervalAverages(timeValuePairs, targetTimeUnit)
            }
        }
    }

    /**
     * 시간별 그룹핑
     */
    private fun groupByHour(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            time.truncatedTo(ChronoUnit.HOURS)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /**
     * 일별 그룹핑
     */
    private fun groupByDay(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            time.truncatedTo(ChronoUnit.DAYS)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /**
     * 주별 그룹핑 (일요일 기준)
     */
    private fun groupByWeek(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            // 일요일을 기준으로 해당 주의 시작 날짜 계산
            val sunday = time.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            sunday.atStartOfDay()
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /**
     * 월별 그룹핑
     */
    private fun groupByMonth(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            LocalDateTime.of(time.year, time.month, 1, 0, 0)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }

    /**
     * 연별 그룹핑
     */
    private fun groupByYear(timeValuePairs: List<Pair<LocalDateTime, Double>>): Map<LocalDateTime, List<Double>> {
        return timeValuePairs.groupBy { (time, _) ->
            LocalDateTime.of(time.year, 1, 1, 0, 0)
        }.mapValues { (_, pairs) -> pairs.map { it.second } }
    }
    /**
     * 간격 기반 평균 계산
     * DAILY_AVERAGE의 경우 targetTimeUnit 윈도우 내에서 일별 평균을 계산
     * 
     * 알고리즘:
     * 1. 일별로 데이터를 집계 (normalization to daily bins)
     * 2. targetTimeUnit 윈도우별로 그룹핑
     * 3. 각 윈도우 내에서 실제 데이터가 있는 일수로만 나누어 평균 계산
     */
    private fun calculateIntervalAverages(
        timeValuePairs: List<Pair<LocalDateTime, Double>>,
        targetTimeUnit: TimeUnitGroup
    ): List<Pair<LocalDateTime, Double>> {
        if (timeValuePairs.isEmpty()) return emptyList()

        // Step 1: 일별로 데이터 집계 (normalize to daily bins)
        val dailyAggregatedData = groupByDay(timeValuePairs).map { (date, values) ->
            date to values.sum() // 하루 내 모든 값들의 합계
        }.sortedBy { it.first }

        if (dailyAggregatedData.isEmpty()) return emptyList()

        // targetTimeUnit이 DAY인 경우, 이미 일별 데이터이므로 그대로 반환
        if (targetTimeUnit == TimeUnitGroup.DAY) {
            return dailyAggregatedData
        }

        // Step 2: targetTimeUnit 윈도우 생성
        val minTime = dailyAggregatedData.first().first
        val maxTime = dailyAggregatedData.last().first
        val targetIntervals = generateCompleteIntervals(minTime, maxTime, targetTimeUnit)

        // Step 3: 각 targetTimeUnit 윈도우에서 일별 평균 계산
        return targetIntervals.map { intervalStart ->
            val intervalEnd = getIntervalEnd(intervalStart, targetTimeUnit)

            // 해당 윈도우에 속하는 일별 데이터들 필터링
            val dailyDataInWindow = dailyAggregatedData.filter { (date, _) ->
                date >= intervalStart && date < intervalEnd
            }
            
            // 실제 데이터가 있는 일수로만 나누어 평균 계산
            val dailyAverage = if (dailyDataInWindow.isNotEmpty()) {
                val totalSum = dailyDataInWindow.sumOf { it.second }
                val actualDaysWithData = dailyDataInWindow.size
                totalSum / actualDaysWithData
            } else {
                0.0 // 데이터가 없는 윈도우는 0
            }
            
            intervalStart to dailyAverage
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
            else -> {
                throw IllegalArgumentException("DAILY_AVERAGE 집계는 DAY, WEEK, MONTH, YEAR 단위에서만 지원됩니다.")
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