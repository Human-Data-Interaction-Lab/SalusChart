package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * HealthData 리스트를 TemporalDataSet로 변환하는 확장 함수들
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
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
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
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 * 각 HeartRate의 샘플들의 평균 BPM을 해당 측정 기간에 걸쳐 분별로 분산
 */
fun List<HeartRate>.toHeartRateTemporalDataSet(): TemporalDataSet {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { heartRate ->
            // 해당 HeartRate의 평균 BPM 계산
            val avgBpm = if (heartRate.samples.isNotEmpty()) {
                heartRate.samples.map { it.beatsPerMinute.toDouble() }.average()
            } else {
                0.0
            }
            mapOf("bpm" to avgBpm)
        }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    return TemporalDataSet(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("bpm") ?: 0.0 },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * HeartRate 리스트를 범위 기반 TemporalDataSet로 변환 (최소/최대값)
 * 범위 차트에 유용함
 */
fun List<HeartRate>.toRangeTemporalDataSet(): TemporalDataSet {
    val times = this.map { it.startTime }
    val yMultiple = mapOf(
        "min" to this.map { heartRate ->
            if (heartRate.samples.isNotEmpty()) {
                heartRate.samples.minOf { it.beatsPerMinute }.toDouble()
            } else {
                0.0
            }
        },
        "max" to this.map { heartRate ->
            if (heartRate.samples.isNotEmpty()) {
                heartRate.samples.maxOf { it.beatsPerMinute }.toDouble()
            } else {
                0.0
            }
        }
    )

    return TemporalDataSet(
        x = times,
        yMultiple = yMultiple,
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * SleepSession 리스트를 TemporalDataSet로 변환
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 * 수면 세션의 총 시간(시간 단위)을 해당 수면 기간에 걸쳐 분별로 분산
 * 
 * 참고: 수면 단계(sleep stages) 정보는 현재 TemporalDataSet에서 보존되지 않습니다.
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

///**
// * SleepSession 리스트를 수면 단계별 다중 값 TemporalDataSet로 변환
// * 각 수면 단계별 시간(시간 단위)을 해당 수면 기간에 걸쳐 분별로 분산
// *
// * 이 함수는 향후 수면 단계 차트 구현을 위해 준비된 함수입니다.
// * 현재는 기본 차트 컴포넌트에서 사용 가능하지만, 전용 수면 단계 차트에서 더 효과적으로 활용될 예정입니다.
// */
//fun List<SleepSession>.toSleepStageTemporalDataSet(): TemporalDataSet {
//    val aggregatedData = aggregateActivityDataTime(
//        activities = this,
//        getStartTime = { it.startTime },
//        getEndTime = { it.endTime },
//        extractValues = { sleepSession ->
//            // 각 수면 단계별 총 시간 계산 (시간 단위)
//            val stageHours = mutableMapOf<String, Float>()
//
//            sleepSession.stages.forEach { stage ->
//                val stageDuration = Duration.between(stage.startTime, stage.endTime).toMinutes() / 60.0f
//                val stageKey = stage.stage.name.lowercase()
//                stageHours[stageKey] = stageHours.getOrDefault(stageKey, 0f) + stageDuration
//            }
//
//            // 모든 가능한 수면 단계에 대해 0으로 초기화 후 실제 값 설정
//            val allStages = listOf("deep", "light", "rem", "awake", "awake_in_bed", "out_of_bed", "sleeping", "unknown")
//            allStages.associateWith { stage ->
//                stageHours.getOrDefault(stage, 0f)
//            }
//        }
//    )
//
//    val sortedTimes = aggregatedData.keys.sorted()
//    val allStageNames = listOf("deep", "light", "rem", "awake", "awake_in_bed", "out_of_bed", "sleeping", "unknown")
//
//    val yMultiple = allStageNames.associateWith { stageName ->
//        sortedTimes.map { time ->
//            aggregatedData[time]?.get(stageName) ?: 0f
//        }
//    }
//
//    return TemporalDataSet(
//        x = sortedTimes,
//        yMultiple = yMultiple,
//        timeUnit = TimeUnitGroup.MINUTE
//    )
//}

/**
 * BloodPressure 리스트를 다중 값 TemporalDataSet로 변환
 * 단일 시점 데이터
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


/**
 * 혼합 HealthData 리스트를 TemporalDataSet로 변환 (첫 번째 일치하는 타입 사용)
 * 단일 데이터 타입만 포함된 리스트에 권장됨
 *
 * @return TemporalDataSet 또는 지원되지 않는 타입인 경우 null
 */
fun List<HealthData>.toTemporalDataSet(): TemporalDataSet? {
    return when {
        this.any { it is StepCount } -> this.filterIsInstance<StepCount>().toStepCountTemporalDataSet()
        this.any { it is Exercise } -> this.filterIsInstance<Exercise>().toExerciseTemporalDataSet()
        this.any { it is Diet } -> this.filterIsInstance<Diet>().toDietTemporalDataSet()
        this.any { it is HeartRate } -> this.filterIsInstance<HeartRate>().toHeartRateTemporalDataSet()
        this.any { it is SleepSession } -> this.filterIsInstance<SleepSession>().toSleepSessionTemporalDataSet()
        this.any { it is BloodPressure } -> this.filterIsInstance<BloodPressure>().toBloodPressureTemporalDataSet()
        this.any { it is BloodGlucose } -> this.filterIsInstance<BloodGlucose>().toBloodGlucoseTemporalDataSet()
        this.any { it is Weight } -> this.filterIsInstance<Weight>().toWeightTemporalDataSet()
        this.any { it is BodyFat } -> this.filterIsInstance<BodyFat>().toBodyFatTemporalDataSet()
        this.any { it is SkeletalMuscleMass } -> this.filterIsInstance<SkeletalMuscleMass>().toSkeletalMuscleMassTemporalDataSet()
        else -> null
    }
}


// TODO: HealthData 여러 타입이 하나의 리스트에 섞여있는 경우
///**
// * 여러 datatype이 혼합된 HealthData 리스트를 타입별로 분리된 TemporalDataSet 맵으로 변환
// *
// * @return 데이터 타입명을 키로 하고 해당 TemporalDataSet를 값으로 하는 맵
// */
//fun List<HealthData>.toTemporalDataSetsByType(): Map<String, TemporalDataSet> {
//    val result = mutableMapOf<String, TemporalDataSet>()
//
//    // 타입별로 그룹핑
//    val stepCounts = filterIsInstance<StepCount>()
//    val exercises = filterIsInstance<Exercise>()
//    val diets = filterIsInstance<Diet>()
//    val heartRates = filterIsInstance<HeartRate>()
//    val sleepSessions = filterIsInstance<SleepSession>()
//    val bloodPressures = filterIsInstance<BloodPressure>()
//    val bloodGlucoses = filterIsInstance<BloodGlucose>()
//    val weights = filterIsInstance<Weight>()
//    val bodyFats = filterIsInstance<BodyFat>()
//    val skeletalMuscleMasses = filterIsInstance<SkeletalMuscleMass>()
//
//    // 각 타입별로 TemporalDataSet 생성
//    if (stepCounts.isNotEmpty()) {
//        result["StepCount"] = stepCounts.toStepCountTemporalDataSet()
//    }
//    if (exercises.isNotEmpty()) {
//        result["Exercise"] = exercises.toExerciseTemporalDataSet()
//    }
//    if (diets.isNotEmpty()) {
//        result["Diet"] = diets.toDietTemporalDataSet()
//    }
//    if (heartRates.isNotEmpty()) {
//        result["HeartRate"] = heartRates.toHeartRateTemporalDataSet()
//    }
//    if (sleepSessions.isNotEmpty()) {
//        result["SleepSession"] = sleepSessions.toSleepSessionTemporalDataSet()
//    }
//    if (bloodPressures.isNotEmpty()) {
//        result["BloodPressure"] = bloodPressures.toBloodPressureTemporalDataSet()
//    }
//    if (bloodGlucoses.isNotEmpty()) {
//        result["BloodGlucose"] = bloodGlucoses.toBloodGlucoseTemporalDataSet()
//    }
//    if (weights.isNotEmpty()) {
//        result["Weight"] = weights.toWeightTemporalDataSet()
//    }
//    if (bodyFats.isNotEmpty()) {
//        result["BodyFat"] = bodyFats.toBodyFatTemporalDataSet()
//    }
//    if (skeletalMuscleMasses.isNotEmpty()) {
//        result["SkeletalMuscleMass"] = skeletalMuscleMasses.toSkeletalMuscleMassTemporalDataSet()
//    }
//
//    return result
//}

// TODO: HealthData 여러 타입이 하나의 리스트에 섞여있는 경우
///**
// * 혼합 HealthData 리스트를 타입별로 변환하여 ChartMark 맵으로 반환하는 편의 함수
// *
// * @param timeUnit 변환할 시간 단위
// * @param aggregationType 집계 방법
// * @return 타입명을 키로 하고 ChartMark 리스트를 값으로 하는 맵
// */
//fun List<HealthData>.transformByType(
//    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
//    aggregationType: AggregationType = AggregationType.SUM
//): Map<String, List<ChartMark>> {
//    val result = mutableMapOf<String, List<ChartMark>>()
//
//    // 타입별로 그룹핑
//    val stepCounts = filterIsInstance<StepCount>()
//    val exercises = filterIsInstance<Exercise>()
//    val diets = filterIsInstance<Diet>()
//    val heartRates = filterIsInstance<HeartRate>()
//    val sleepSessions = filterIsInstance<SleepSession>()
//    val bloodPressures = filterIsInstance<BloodPressure>()
//    val bloodGlucoses = filterIsInstance<BloodGlucose>()
//    val weights = filterIsInstance<Weight>()
//    val bodyFats = filterIsInstance<BodyFat>()
//    val skeletalMuscleMasses = filterIsInstance<SkeletalMuscleMass>()
//
//    // 각 타입별로 ChartMark로 직접 변환
//    if (stepCounts.isNotEmpty()) {
//        result["StepCount"] = stepCounts.transform(timeUnit, aggregationType)
//    }
//    if (exercises.isNotEmpty()) {
//        result["Exercise"] = exercises.transform(timeUnit, aggregationType)
//    }
//    if (diets.isNotEmpty()) {
//        result["Diet"] = diets.transform(timeUnit, aggregationType) // 기본적으로 calories
//    }
//    if (heartRates.isNotEmpty()) {
//        result["HeartRate"] = heartRates.toTemporalDataSet().transform(timeUnit, aggregationType).toChartMarks()
//    }
//    if (sleepSessions.isNotEmpty()) {
//        result["SleepSession"] = sleepSessions.toTemporalDataSet().transform(timeUnit, aggregationType).toChartMarks()
//    }
//    if (bloodPressures.isNotEmpty()) {
//        result["BloodPressure"] = bloodPressures.transform(timeUnit, aggregationType) // 기본적으로 systolic
//    }
//    if (bloodGlucoses.isNotEmpty()) {
//        result["BloodGlucose"] = bloodGlucoses.transform(timeUnit, aggregationType)
//    }
//    if (weights.isNotEmpty()) {
//        result["Weight"] = weights.transform(timeUnit, aggregationType)
//    }
//    if (bodyFats.isNotEmpty()) {
//        result["BodyFat"] = bodyFats.transform(timeUnit, aggregationType)
//    }
//    if (skeletalMuscleMasses.isNotEmpty()) {
//        result["SkeletalMuscleMass"] = skeletalMuscleMasses.transform(timeUnit, aggregationType)
//    }
//
//    return result
//}

/**
 * 시간 간격 기반 활동 데이터를 분별로 집계
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
            while (!currentMinute.isAfter(endMinute)) {
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
                
                val minuteMap = minuteValues.getOrPut(currentMinute) { mutableMapOf() }
                activityValues.forEach { (property, value) ->
                    val proportionalValue = value * proportion
                    minuteMap[property] = minuteMap.getOrDefault(property, 0.0) + proportionalValue
                }
                
                currentMinute = currentMinute.atZone(ZoneId.systemDefault()).plusMinutes(1).toInstant()
            }
        }
    }
    
    return minuteValues
}

// TODO: HealthData 여러 타입이 하나의 리스트에 섞여있는 경우, 굳이 필요할까
///**
// * 혼합 HealthData 리스트에서 다중 값 타입의 특정 속성들을 선택하여 변환하는 편의 함수
// *
// * @param propertySelections 타입별 속성 선택 맵 (예: "Diet" to "calories", "BloodPressure" to "systolic")
// * @param timeUnit 변환할 시간 단위
// * @param aggregationType 집계 방법
// * @return 타입명을 키로 하고 ChartMark 리스트를 값으로 하는 맵
// */
//fun List<HealthData>.transformByTypeWithProperties(
//    propertySelections: Map<String, String>,
//    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
//    aggregationType: AggregationType = AggregationType.SUM
//): Map<String, List<ChartMark>> {
//    val result = mutableMapOf<String, List<ChartMark>>()
//
//    // 타입별로 그룹핑
//    val diets = filterIsInstance<Diet>()
//    val bloodPressures = filterIsInstance<BloodPressure>()
//
//    // Diet 타입 처리
//    if (diets.isNotEmpty()) {
//        val dietProperty = propertySelections["Diet"] ?: "calories"
//        result["Diet"] = diets.transformByProperty(dietProperty, timeUnit, aggregationType)
//    }
//
//    // BloodPressure 타입 처리
//    if (bloodPressures.isNotEmpty()) {
//        val bpProperty = propertySelections["BloodPressure"] ?: "systolic"
//        result["BloodPressure"] = bloodPressures.transformByProperty(bpProperty, timeUnit, aggregationType)
//    }
//
//    // 단일 값 타입들은 기본 transform 함수 사용
//    val basicResult = this.transformByType(timeUnit, aggregationType)
//    result.putAll(basicResult.filterKeys { it !in listOf("Diet", "BloodPressure") })
//
//    return result
//}
