package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// Activity Data (StepCount, Exercise, Diet, SleepSession, HeartRate)
// - Each activity data has start time and end time
// - Therefore, it needs to be aggregated by time unit (minute)
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * HealthData 리스트를 TemporalDataSet로 변환하는 확장 함수들
 * "HealthData → TemporalDataSet" → Transform → ChartMark
 * 
 * HealthData type 각각의 구조적인 특성을 반영하여 적절한 집계 및 변환을 수행
 */

/**
 * StepCount 리스트를 TemporalDataSet로 변환
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 */
fun List<StepCount>.toStepCountTemporalDataSet(): TemporalDataSet {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { mapOf("stepCount" to it.stepCount.toDouble()) }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    return TemporalDataSet(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("stepCount") ?: 0.0 },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Exercise 리스트를 TemporalDataSet로 변환
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 */
fun List<Exercise>.toExerciseTemporalDataSet(): TemporalDataSet {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { mapOf("caloriesBurned" to it.caloriesBurned.toDouble()) }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    return TemporalDataSet(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("caloriesBurned") ?: 0.0 },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Diet 리스트를 다중 값 TemporalDataSet로 변환
 * 시간 간격 데이터를 분별로 집계하여 시점별 다중 속성 데이터로 변환
 * (각 시점마다 calories, protein, carbohydrate, fat 값을 포함)
 */
fun List<Diet>.toDietTemporalDataSet(): TemporalDataSet {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { diet ->
            mapOf(
                "calories" to diet.calories.toDouble(),
                "protein" to diet.protein.toKilograms().toDouble(),
                "carbohydrate" to diet.carbohydrate.toKilograms().toDouble(),
                "fat" to diet.fat.toKilograms().toDouble()
            )
        }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    val propertyNames = aggregatedData.values.flatMap { it.keys }.distinct()
    
    val yMultiple = propertyNames.associateWith { property ->
        sortedTimes.map { time ->
            aggregatedData[time]?.get(property) ?: 0.0
        }
    }
    
    return TemporalDataSet(
        x = sortedTimes,
        yMultiple = yMultiple,
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * HeartRate 리스트를 TemporalDataSet로 변환
 * 
 * 모든 HeartRateSample을 추출하여 각 샘플을 독립적인 데이터 포인트로 변환
 * HeartRate의 startTime/endTime는 무시되며, 각 샘플의 time만 사용
 * 
 */
fun List<HeartRate>.toHeartRateTemporalDataSet(): TemporalDataSet {
    // 모든 HeartRate의 샘플을 추출 (각 샘플 = 독립적인 데이터 포인트)
    val allSamples = this.flatMap { heartRate ->
        heartRate.samples
    }
    
    if (allSamples.isEmpty()) {
        return TemporalDataSet(
            x = emptyList(),
            y = emptyList(),
            timeUnit = TimeUnitGroup.MINUTE
        )
    }
    
    // 시간순 정렬 (같은 시간에 여러 샘플이 있을 수 있으므로 그대로 유지)
    val sortedSamples = allSamples.sortedBy { it.time }
    
    return TemporalDataSet(
        x = sortedSamples.map { it.time },
        y = sortedSamples.map { it.beatsPerMinute.toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * SleepSession 리스트를 TemporalDataSet로 변환
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 * 수면 세션의 총 시간(시간 단위)을 해당 수면 기간에 걸쳐 분별로 분산
 * 
 * 참고: 수면 단계(sleep stages) 정보는 현재 TemporalDataSet에서 보존되지 않습니다.
 * 이는 SleepStageChart 제작 시 TemporalDataSet 정보가 불필요하기 때문입니다.
 * 향후 수면 단계 차트 구현 시 별도의 변환 함수가 필요할 수 있습니다.
 */
fun List<SleepSession>.toSleepSessionTemporalDataSet(): TemporalDataSet {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { sleepSession ->
            // 수면 세션의 총 시간을 시간 단위로 계산
            val totalSleepHours = Duration.between(sleepSession.startTime, sleepSession.endTime).toMinutes() / 60.0
            mapOf("sleepHours" to totalSleepHours)
        }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    return TemporalDataSet(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("sleepHours") ?: 0.0 },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// Body Measurement Data (BloodPressure, BloodGlucose, Weight, BodyFat, SkeletalMuscleMass)
// - Each body measurement data has only one value at a specific timepoint
// - Therefore, it can be directly converted to a single value TemporalDataSet
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * BloodPressure 리스트를 다중 값 TemporalDataSet로 변환
 * 다중 속성 데이터
 * (각 시점마다 systolic, diastolic 값을 포함)
 */
fun List<BloodPressure>.toBloodPressureTemporalDataSet(): TemporalDataSet {
    val times = this.map { it.time }
    val yMultiple = mapOf(
        "systolic" to this.map { it.systolic.toDouble() },
        "diastolic" to this.map { it.diastolic.toDouble() }
    )

    return TemporalDataSet(
        x = times,
        yMultiple = yMultiple,
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * BloodGlucose 리스트를 TemporalDataSet로 변환
 * 단일 시점 데이터
 */
fun List<BloodGlucose>.toBloodGlucoseTemporalDataSet(): TemporalDataSet {
    return TemporalDataSet(
        x = this.map { it.time },
        y = this.map { it.level.toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Weight 리스트를 TemporalDataSet로 변환
 * 단일 시점 데이터
 */
fun List<Weight>.toWeightTemporalDataSet(): TemporalDataSet {
    return TemporalDataSet(
        x = this.map { it.time },
        y = this.map { it.weight.toKilograms().toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * BodyFat 리스트를 TemporalDataSet로 변환
 * 단일 시점 데이터
 */
fun List<BodyFat>.toBodyFatTemporalDataSet(): TemporalDataSet {
    return TemporalDataSet(
        x = this.map { it.time },
        y = this.map { it.bodyFatPercentage.toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * SkeletalMuscleMass 리스트를 TemporalDataSet로 변환
 * 단일 시점 데이터
 */
fun List<SkeletalMuscleMass>.toSkeletalMuscleMassTemporalDataSet(): TemporalDataSet {
    return TemporalDataSet(
        x = this.map { it.time },
        y = this.map { it.skeletalMuscleMass.toDouble() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

@JvmName("stepCountToTemporalDataSet")
fun List<StepCount>.toTemporalDataSet(): TemporalDataSet = this.toStepCountTemporalDataSet()

@JvmName("exerciseToTemporalDataSet") 
fun List<Exercise>.toTemporalDataSet(): TemporalDataSet = this.toExerciseTemporalDataSet()

@JvmName("dietToTemporalDataSet")
fun List<Diet>.toTemporalDataSet(): TemporalDataSet = this.toDietTemporalDataSet()

@JvmName("heartRateToTemporalDataSet")
fun List<HeartRate>.toTemporalDataSet(): TemporalDataSet = this.toHeartRateTemporalDataSet()

@JvmName("sleepSessionToTemporalDataSet")
fun List<SleepSession>.toTemporalDataSet(): TemporalDataSet = this.toSleepSessionTemporalDataSet()

@JvmName("bloodPressureToTemporalDataSet")
fun List<BloodPressure>.toTemporalDataSet(): TemporalDataSet = this.toBloodPressureTemporalDataSet()

@JvmName("bloodGlucoseToTemporalDataSet")
fun List<BloodGlucose>.toTemporalDataSet(): TemporalDataSet = this.toBloodGlucoseTemporalDataSet()

@JvmName("weightToTemporalDataSet")
fun List<Weight>.toTemporalDataSet(): TemporalDataSet = this.toWeightTemporalDataSet()

@JvmName("bodyFatToTemporalDataSet")
fun List<BodyFat>.toTemporalDataSet(): TemporalDataSet = this.toBodyFatTemporalDataSet()

@JvmName("skeletalMuscleMassToTemporalDataSet")
fun List<SkeletalMuscleMass>.toTemporalDataSet(): TemporalDataSet = this.toSkeletalMuscleMassTemporalDataSet()

// TODO: Util 파일 따로 제작한다면, 이 함수도 이동해야 할 듯
/**
 * 시간 간격 기반 활동(Activity) 데이터를 분별로 집계
 * 활동 데이터는 시작 시간과 종료 시간을 가지고 있으므로, 이를 분 단위로 집계해서 시간-값 쌍 맵으로 변환
 * 
 * @param T 활동 데이터 타입
 * @param activities 집계할 활동 데이터 리스트
 * @param getStartTime 활동의 시작 시간을 가져오는 함수
 * @param getEndTime 활동의 종료 시간을 가져오는 함수
 * @param extractValues 활동에서 측정값들을 추출하는 함수 (속성명 -> 값)
 * @return 분별 집계된 측정값 맵 (Instant -> Map<PropertyName, Value>)
 */
private fun <T> aggregateActivityDataTime(
    activities: List<T>,
    getStartTime: (T) -> Instant,
    getEndTime: (T) -> Instant,
    extractValues: (T) -> Map<String, Double>
): Map<Instant, Map<String, Double>> {
    val minuteValues = mutableMapOf<Instant, MutableMap<String, Double>>()
    
    activities.forEach { activity ->
        val startTime = getStartTime(activity)
        val endTime = getEndTime(activity)
        val totalDuration = Duration.between(startTime, endTime).toMillis()
        val activityValues = extractValues(activity)
        
        // 시작 시간과 종료 시간을 분 단위로 정규화
        val startMinute = startTime.atZone(ZoneId.systemDefault())
            .withSecond(0).withNano(0).toInstant()
        val endMinute = endTime.atZone(ZoneId.systemDefault())
            .withSecond(0).withNano(0).toInstant()
        
        if (startMinute == endMinute) {
            // 같은 분 내의 활동
            val minuteMap = minuteValues.getOrPut(startMinute) { mutableMapOf() }
            activityValues.forEach { (property, value) ->
                minuteMap[property] = minuteMap.getOrDefault(property, 0.0) + value
            }
        } else {
            // 여러 분에 걸친 활동 - 분별로 비례 분할
            var currentMinute = startMinute
            while (currentMinute.isBefore(endMinute)) {
                val minuteStart = currentMinute
                val minuteEnd = currentMinute.atZone(ZoneId.systemDefault()).plusMinutes(1).toInstant()
                
                // 해당 분의 실제 활동 시간 계산
                val actualStart = maxOf(startTime, minuteStart)
                val actualEnd = minOf(endTime, minuteEnd)
                val minuteDuration = Duration.between(actualStart, actualEnd).toMillis()
                
                // 비율로 값 계산
                val proportion = if (totalDuration > 0) {
                    minuteDuration.toDouble() / totalDuration
                } else {
                    0.0
                }
                
                // 실제로 활동이 있는 분만 추가 (proportion > 0)
                if (proportion > 0) {
                    val minuteMap = minuteValues.getOrPut(currentMinute) { mutableMapOf() }
                    activityValues.forEach { (property, value) ->
                        val proportionalValue = value * proportion
                        minuteMap[property] = minuteMap.getOrDefault(property, 0.0) + proportionalValue
                    }
                }
                
                currentMinute = currentMinute.atZone(ZoneId.systemDefault()).plusMinutes(1).toInstant()
            }
        }
    }
    
    return minuteValues
}