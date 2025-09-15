package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.data.model.model.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * HealthData 리스트를 TimeDataPoint로 변환하는 확장 함수들
 */

/**
 * StepCount 리스트를 TimeDataPoint로 변환
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 */
fun List<StepCount>.toStepCountTimeDataPoint(): TimeDataPoint {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { mapOf("stepCount" to it.stepCount.toFloat()) }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    return TimeDataPoint(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("stepCount") ?: 0f },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Exercise 리스트를 TimeDataPoint로 변환
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 */
fun List<Exercise>.toExerciseTimeDataPoint(): TimeDataPoint {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { mapOf("caloriesBurned" to it.caloriesBurned.toFloat()) }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    return TimeDataPoint(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("caloriesBurned") ?: 0f },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Diet 리스트를 다중 값 TimeDataPoint로 변환
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 */
fun List<Diet>.toDietTimeDataPoint(): TimeDataPoint {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { diet ->
            mapOf(
                "calories" to diet.calories.toFloat(),
                "protein" to diet.protein.toKilograms().toFloat(),
                "carbohydrate" to diet.carbohydrate.toKilograms().toFloat(),
                "fat" to diet.fat.toKilograms().toFloat()
            )
        }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    val propertyNames = aggregatedData.values.flatMap { it.keys }.distinct()
    
    val yMultiple = propertyNames.associateWith { property ->
        sortedTimes.map { time ->
            aggregatedData[time]?.get(property) ?: 0f
        }
    }
    
    return TimeDataPoint(
        x = sortedTimes,
        yMultiple = yMultiple,
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * HeartRate 리스트를 TimeDataPoint로 변환
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 * 각 HeartRate의 샘플들의 평균 BPM을 해당 측정 기간에 걸쳐 분별로 분산
 */
fun List<HeartRate>.toHeartRateTimeDataPoint(): TimeDataPoint {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { heartRate ->
            // 해당 HeartRate의 평균 BPM 계산
            val avgBpm = if (heartRate.samples.isNotEmpty()) {
                heartRate.samples.map { it.beatsPerMinute.toFloat() }.average().toFloat()
            } else {
                0f
            }
            mapOf("bpm" to avgBpm)
        }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    return TimeDataPoint(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("bpm") ?: 0f },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * HeartRate 리스트를 범위 기반 TimeDataPoint로 변환 (최소/최대값)
 * 범위 차트에 유용함
 */
fun List<HeartRate>.toRangeTimeDataPoint(): TimeDataPoint {
    val times = this.map { it.startTime }
    val yMultiple = mapOf(
        "min" to this.map { heartRate ->
            if (heartRate.samples.isNotEmpty()) {
                heartRate.samples.minOf { it.beatsPerMinute }.toFloat()
            } else {
                0f
            }
        },
        "max" to this.map { heartRate ->
            if (heartRate.samples.isNotEmpty()) {
                heartRate.samples.maxOf { it.beatsPerMinute }.toFloat()
            } else {
                0f
            }
        }
    )

    return TimeDataPoint(
        x = times,
        yMultiple = yMultiple,
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * SleepSession 리스트를 TimeDataPoint로 변환
 * 시간 간격 데이터를 분별로 집계하여 단일 시점 데이터로 변환
 * 수면 세션의 총 시간(시간 단위)을 해당 수면 기간에 걸쳐 분별로 분산
 * 
 * 참고: 수면 단계(sleep stages) 정보는 현재 TimeDataPoint에서 보존되지 않습니다.
 * 향후 수면 단계 차트 구현 시 별도의 변환 함수가 필요할 수 있습니다.
 */
fun List<SleepSession>.toSleepSessionTimeDataPoint(): TimeDataPoint {
    val aggregatedData = aggregateActivityDataTime(
        activities = this,
        getStartTime = { it.startTime },
        getEndTime = { it.endTime },
        extractValues = { sleepSession ->
            // 수면 세션의 총 시간을 시간 단위로 계산
            val totalSleepHours = Duration.between(sleepSession.startTime, sleepSession.endTime).toMinutes() / 60.0f
            mapOf("sleepHours" to totalSleepHours)
        }
    )
    
    val sortedTimes = aggregatedData.keys.sorted()
    return TimeDataPoint(
        x = sortedTimes,
        y = sortedTimes.map { aggregatedData[it]?.get("sleepHours") ?: 0f },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

///**
// * SleepSession 리스트를 수면 단계별 다중 값 TimeDataPoint로 변환
// * 각 수면 단계별 시간(시간 단위)을 해당 수면 기간에 걸쳐 분별로 분산
// *
// * 이 함수는 향후 수면 단계 차트 구현을 위해 준비된 함수입니다.
// * 현재는 기본 차트 컴포넌트에서 사용 가능하지만, 전용 수면 단계 차트에서 더 효과적으로 활용될 예정입니다.
// */
//fun List<SleepSession>.toSleepStageTimeDataPoint(): TimeDataPoint {
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
//    return TimeDataPoint(
//        x = sortedTimes,
//        yMultiple = yMultiple,
//        timeUnit = TimeUnitGroup.MINUTE
//    )
//}

/**
 * BloodPressure 리스트를 다중 값 TimeDataPoint로 변환
 * 단일 시점 데이터
 */
fun List<BloodPressure>.toBloodPressureTimeDataPoint(): TimeDataPoint {
    val times = this.map { it.time }
    val yMultiple = mapOf(
        "systolic" to this.map { it.systolic.toFloat() },
        "diastolic" to this.map { it.diastolic.toFloat() }
    )

    return TimeDataPoint(
        x = times,
        yMultiple = yMultiple,
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * BloodGlucose 리스트를 TimeDataPoint로 변환
 * 단일 시점 데이터
 */
fun List<BloodGlucose>.toBloodGlucoseTimeDataPoint(): TimeDataPoint {
    return TimeDataPoint(
        x = this.map { it.time },
        y = this.map { it.level.toFloat() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * Weight 리스트를 TimeDataPoint로 변환
 * 단일 시점 데이터
 */
fun List<Weight>.toWeightTimeDataPoint(): TimeDataPoint {
    return TimeDataPoint(
        x = this.map { it.time },
        y = this.map { it.weight.toKilograms().toFloat() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * BodyFat 리스트를 TimeDataPoint로 변환
 * 단일 시점 데이터
 */
fun List<BodyFat>.toBodyFatTimeDataPoint(): TimeDataPoint {
    return TimeDataPoint(
        x = this.map { it.time },
        y = this.map { it.bodyFatPercentage.toFloat() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

/**
 * SkeletalMuscleMass 리스트를 TimeDataPoint로 변환
 * 단일 시점 데이터
 */
fun List<SkeletalMuscleMass>.toSkeletalMuscleMassTimeDataPoint(): TimeDataPoint {
    return TimeDataPoint(
        x = this.map { it.time },
        y = this.map { it.skeletalMuscleMass.toFloat() },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

@JvmName("stepCountToTimeDataPoint")
fun List<StepCount>.toTimeDataPoint(): TimeDataPoint = this.toStepCountTimeDataPoint()

@JvmName("exerciseToTimeDataPoint") 
fun List<Exercise>.toTimeDataPoint(): TimeDataPoint = this.toExerciseTimeDataPoint()

@JvmName("dietToTimeDataPoint")
fun List<Diet>.toTimeDataPoint(): TimeDataPoint = this.toDietTimeDataPoint()

@JvmName("heartRateToTimeDataPoint")
fun List<HeartRate>.toTimeDataPoint(): TimeDataPoint = this.toHeartRateTimeDataPoint()

@JvmName("sleepSessionToTimeDataPoint")
fun List<SleepSession>.toTimeDataPoint(): TimeDataPoint = this.toSleepSessionTimeDataPoint()

@JvmName("bloodPressureToTimeDataPoint")
fun List<BloodPressure>.toTimeDataPoint(): TimeDataPoint = this.toBloodPressureTimeDataPoint()

@JvmName("bloodGlucoseToTimeDataPoint")
fun List<BloodGlucose>.toTimeDataPoint(): TimeDataPoint = this.toBloodGlucoseTimeDataPoint()

@JvmName("weightToTimeDataPoint")
fun List<Weight>.toTimeDataPoint(): TimeDataPoint = this.toWeightTimeDataPoint()

@JvmName("bodyFatToTimeDataPoint")
fun List<BodyFat>.toTimeDataPoint(): TimeDataPoint = this.toBodyFatTimeDataPoint()

@JvmName("skeletalMuscleMassToTimeDataPoint")
fun List<SkeletalMuscleMass>.toTimeDataPoint(): TimeDataPoint = this.toSkeletalMuscleMassTimeDataPoint()


/**
 * 혼합 HealthData 리스트를 TimeDataPoint로 변환 (첫 번째 일치하는 타입 사용)
 * 단일 데이터 타입만 포함된 리스트에 권장됨
 *
 * @return TimeDataPoint 또는 지원되지 않는 타입인 경우 null
 */
fun List<HealthData>.toTimeDataPoint(): TimeDataPoint? {
    return when {
        this.any { it is StepCount } -> this.filterIsInstance<StepCount>().toStepCountTimeDataPoint()
        this.any { it is Exercise } -> this.filterIsInstance<Exercise>().toExerciseTimeDataPoint()
        this.any { it is Diet } -> this.filterIsInstance<Diet>().toDietTimeDataPoint()
        this.any { it is HeartRate } -> this.filterIsInstance<HeartRate>().toHeartRateTimeDataPoint()
        this.any { it is SleepSession } -> this.filterIsInstance<SleepSession>().toSleepSessionTimeDataPoint()
        this.any { it is BloodPressure } -> this.filterIsInstance<BloodPressure>().toBloodPressureTimeDataPoint()
        this.any { it is BloodGlucose } -> this.filterIsInstance<BloodGlucose>().toBloodGlucoseTimeDataPoint()
        this.any { it is Weight } -> this.filterIsInstance<Weight>().toWeightTimeDataPoint()
        this.any { it is BodyFat } -> this.filterIsInstance<BodyFat>().toBodyFatTimeDataPoint()
        this.any { it is SkeletalMuscleMass } -> this.filterIsInstance<SkeletalMuscleMass>().toSkeletalMuscleMassTimeDataPoint()
        else -> null
    }
}


// TODO: HealthData 여러 타입이 하나의 리스트에 섞여있는 경우
///**
// * 여러 datatype이 혼합된 HealthData 리스트를 타입별로 분리된 TimeDataPoint 맵으로 변환
// *
// * @return 데이터 타입명을 키로 하고 해당 TimeDataPoint를 값으로 하는 맵
// */
//fun List<HealthData>.toTimeDataPointsByType(): Map<String, TimeDataPoint> {
//    val result = mutableMapOf<String, TimeDataPoint>()
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
//    // 각 타입별로 TimeDataPoint 생성
//    if (stepCounts.isNotEmpty()) {
//        result["StepCount"] = stepCounts.toStepCountTimeDataPoint()
//    }
//    if (exercises.isNotEmpty()) {
//        result["Exercise"] = exercises.toExerciseTimeDataPoint()
//    }
//    if (diets.isNotEmpty()) {
//        result["Diet"] = diets.toDietTimeDataPoint()
//    }
//    if (heartRates.isNotEmpty()) {
//        result["HeartRate"] = heartRates.toHeartRateTimeDataPoint()
//    }
//    if (sleepSessions.isNotEmpty()) {
//        result["SleepSession"] = sleepSessions.toSleepSessionTimeDataPoint()
//    }
//    if (bloodPressures.isNotEmpty()) {
//        result["BloodPressure"] = bloodPressures.toBloodPressureTimeDataPoint()
//    }
//    if (bloodGlucoses.isNotEmpty()) {
//        result["BloodGlucose"] = bloodGlucoses.toBloodGlucoseTimeDataPoint()
//    }
//    if (weights.isNotEmpty()) {
//        result["Weight"] = weights.toWeightTimeDataPoint()
//    }
//    if (bodyFats.isNotEmpty()) {
//        result["BodyFat"] = bodyFats.toBodyFatTimeDataPoint()
//    }
//    if (skeletalMuscleMasses.isNotEmpty()) {
//        result["SkeletalMuscleMass"] = skeletalMuscleMasses.toSkeletalMuscleMassTimeDataPoint()
//    }
//
//    return result
//}

/**
 * 단일 타입 HealthData 리스트를 직접 ChartPoint로 변환하는 편의 함수
 * 내부적으로 HealthData -> TimeDataPoint -> 시간 단위 변환 -> ChartPoint 과정을 자동 처리
 *
 * @param timeUnit 변환할 시간 단위 (기본값: DAY)
 * @param aggregationType 집계 방법 (기본값: SUM)
 * @return ChartPoint 리스트
 */

/**
 * StepCount 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("stepCountTransform")
fun List<StepCount>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

/**
 * Exercise 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("exerciseTransform")
fun List<Exercise>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

/**
 * Diet 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("dietTransform")
fun List<Diet>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): List<ChartPoint> {
    val chartPointsMap = this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPointsMap()

    return chartPointsMap.flatMap { (property, chartPoints) ->
        chartPoints.map { chartPoint ->
            ChartPoint(
                x = chartPoint.x,
                y = chartPoint.y,
                label = "${chartPoint.label} ($property)"
            )
        }
    }
}

/**
 * Diet 리스트에서 특정 속성을 선택하여 ChartPoint로 변환하는 편의 함수
 */
@JvmName("dietTransformByProperty")
fun List<Diet>.transformByProperty(
    property: String,
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPointsByProperty(property)
}

/**
 * BloodPressure 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("bloodPressureTransform")
fun List<BloodPressure>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE
): List<ChartPoint> {
    val chartPointsMap = this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPointsMap()

    return chartPointsMap.flatMap { (property, chartPoints) ->
        chartPoints.map { chartPoint ->
            ChartPoint(
                x = chartPoint.x,
                y = chartPoint.y,
                label = "${chartPoint.label} ($property)"
            )
        }
    }
}

/**
 * BloodPressure 리스트에서 특정 속성을 선택하여 ChartPoint로 변환하는 편의 함수
 */
@JvmName("bloodPressureTransformByProperty")
fun List<BloodPressure>.transformByProperty(
    property: String, // "systolic" or "diastolic"
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPointsByProperty(property)
}

/**
 * BloodGlucose 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("bloodGlucoseTransform")
fun List<BloodGlucose>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

/**
 * HeartRate 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("heartRateTransform")
fun List<HeartRate>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

/**
 * SleepSession 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("sleepSessionTransform")
fun List<SleepSession>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

///**
// * SleepSession 리스트에서 특정 수면 단계를 선택하여 ChartPoint로 변환하는 편의 함수
// * 향후 수면 단계 차트 구현을 위한 준비 함수
// */
//@JvmName("sleepSessionTransformByStage")
//fun List<SleepSession>.transformByStage(
//    stage: String, // "deep", "light", "rem", "awake", etc.
//    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
//    aggregationType: AggregationType = AggregationType.SUM
//): List<ChartPoint> {
//    return this.toSleepStageTimeDataPoint()
//        .transform(timeUnit, aggregationType)
//        .toChartPointsByProperty(stage)
//}

/**
 * Weight 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("weightTransform")
fun List<Weight>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

/**
 * BodyFat 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("bodyFatTransform")
fun List<BodyFat>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

/**
 * SkeletalMuscleMass 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("skeletalMuscleMassTransform")
fun List<SkeletalMuscleMass>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

// TODO: HealthData 여러 타입이 하나의 리스트에 섞여있는 경우
///**
// * 혼합 HealthData 리스트를 타입별로 변환하여 ChartPoint 맵으로 반환하는 편의 함수
// *
// * @param timeUnit 변환할 시간 단위
// * @param aggregationType 집계 방법
// * @return 타입명을 키로 하고 ChartPoint 리스트를 값으로 하는 맵
// */
//fun List<HealthData>.transformByType(
//    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
//    aggregationType: AggregationType = AggregationType.SUM
//): Map<String, List<ChartPoint>> {
//    val result = mutableMapOf<String, List<ChartPoint>>()
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
//    // 각 타입별로 ChartPoint로 직접 변환
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
//        result["HeartRate"] = heartRates.toTimeDataPoint().transform(timeUnit, aggregationType).toChartPoints()
//    }
//    if (sleepSessions.isNotEmpty()) {
//        result["SleepSession"] = sleepSessions.toTimeDataPoint().transform(timeUnit, aggregationType).toChartPoints()
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
    extractValues: (T) -> Map<String, Float>
): Map<Instant, Map<String, Float>> {
    val minuteValues = mutableMapOf<Instant, MutableMap<String, Float>>()
    
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
                minuteMap[property] = minuteMap.getOrDefault(property, 0f) + value
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
                    val proportionalValue = (value * proportion).toFloat()
                    minuteMap[property] = minuteMap.getOrDefault(property, 0f) + proportionalValue
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
// * @return 타입명을 키로 하고 ChartPoint 리스트를 값으로 하는 맵
// */
//fun List<HealthData>.transformByTypeWithProperties(
//    propertySelections: Map<String, String>,
//    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
//    aggregationType: AggregationType = AggregationType.SUM
//): Map<String, List<ChartPoint>> {
//    val result = mutableMapOf<String, List<ChartPoint>>()
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
