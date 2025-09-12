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

// ============================================================================
// TYPE-SPECIFIC toTimeDataPoint() EXTENSION FUNCTIONS
// ============================================================================
// Note: Due to JVM type erasure, we cannot use the same method name for all 
// List<T> types. Each type needs a unique method name to avoid platform declaration clashes.

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
 * 샘플 데이터의 평균값을 시작 시간 기준으로 변환
 */
fun List<HeartRate>.toHeartRateTimeDataPoint(): TimeDataPoint {
    return TimeDataPoint(
        x = this.map { it.startTime },
        y = this.map { heartRate ->
            if (heartRate.samples.isNotEmpty()) {
                heartRate.samples.map { it.beatsPerMinute.toFloat() }.average().toFloat()
            } else {
                0f
            }
        },
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
 * 수면 시간(시간 단위)을 시작 시간 기준으로 변환
 */
fun List<SleepSession>.toSleepSessionTimeDataPoint(): TimeDataPoint {
    return TimeDataPoint(
        x = this.map { it.startTime },
        y = this.map {
            Duration.between(it.startTime, it.endTime).toMinutes() / 60.0f
        },
        timeUnit = TimeUnitGroup.MINUTE
    )
}

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

// ============================================================================
// UNIFIED API USING @JvmName - SOLUTION 2 (RECOMMENDED)
// ============================================================================

/*
/**
 * SOLUTION 1: Factory object with different method names
 * No type erasure issues, clear and explicit
 */
object TimeDataPointConverter {
    fun fromStepCount(data: List<StepCount>): TimeDataPoint = data.toStepCountTimeDataPoint()
    fun fromExercise(data: List<Exercise>): TimeDataPoint = data.toExerciseTimeDataPoint()
    fun fromDiet(data: List<Diet>): TimeDataPoint = data.toDietTimeDataPoint()
    fun fromHeartRate(data: List<HeartRate>): TimeDataPoint = data.toHeartRateTimeDataPoint()
    fun fromSleepSession(data: List<SleepSession>): TimeDataPoint = data.toSleepSessionTimeDataPoint()
    fun fromBloodPressure(data: List<BloodPressure>): TimeDataPoint = data.toBloodPressureTimeDataPoint()
    fun fromBloodGlucose(data: List<BloodGlucose>): TimeDataPoint = data.toBloodGlucoseTimeDataPoint()
    fun fromWeight(data: List<Weight>): TimeDataPoint = data.toWeightTimeDataPoint()
    fun fromBodyFat(data: List<BodyFat>): TimeDataPoint = data.toBodyFatTimeDataPoint()
    fun fromSkeletalMuscleMass(data: List<SkeletalMuscleMass>): TimeDataPoint = data.toSkeletalMuscleMassTimeDataPoint()
}
*/

/**
 * SOLUTION 2: Using @JvmName for unified method names
 * Allows same method name in Kotlin, different JVM signatures
 * This is the RECOMMENDED approach for a clean, unified API
 */

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

/*
// ============================================================================
// SOLUTION 3: Type-Safe Wrapper Classes (Alternative Approach)
// ============================================================================

/**
 * Type-safe wrapper for HealthData lists that eliminates type erasure issues
 * This approach uses sealed classes to maintain type information at runtime
 */
sealed class HealthDataList<T : HealthData>(val data: List<T>) {
    class StepCountList(data: List<StepCount>) : HealthDataList<StepCount>(data)
    class ExerciseList(data: List<Exercise>) : HealthDataList<Exercise>(data)
    class DietList(data: List<Diet>) : HealthDataList<Diet>(data)
    class HeartRateList(data: List<HeartRate>) : HealthDataList<HeartRate>(data)
    class SleepSessionList(data: List<SleepSession>) : HealthDataList<SleepSession>(data)
    class BloodPressureList(data: List<BloodPressure>) : HealthDataList<BloodPressure>(data)
    class BloodGlucoseList(data: List<BloodGlucose>) : HealthDataList<BloodGlucose>(data)
    class WeightList(data: List<Weight>) : HealthDataList<Weight>(data)
    class BodyFatList(data: List<BodyFat>) : HealthDataList<BodyFat>(data)
    class SkeletalMuscleMassList(data: List<SkeletalMuscleMass>) : HealthDataList<SkeletalMuscleMass>(data)
    
    /**
     * Unified toTimeDataPoint() method that works for all types without type erasure
     */
    fun toTimeDataPoint(): TimeDataPoint {
        return when (this) {
            is StepCountList -> data.toStepCountTimeDataPoint()
            is ExerciseList -> data.toExerciseTimeDataPoint()
            is DietList -> data.toDietTimeDataPoint()
            is HeartRateList -> data.toHeartRateTimeDataPoint()
            is SleepSessionList -> data.toSleepSessionTimeDataPoint()
            is BloodPressureList -> data.toBloodPressureTimeDataPoint()
            is BloodGlucoseList -> data.toBloodGlucoseTimeDataPoint()
            is WeightList -> data.toWeightTimeDataPoint()
            is BodyFatList -> data.toBodyFatTimeDataPoint()
            is SkeletalMuscleMassList -> data.toSkeletalMuscleMassTimeDataPoint()
        }
    }
    
    /**
     * Unified transform() method for direct HealthData -> ChartPoint conversion
     */
    fun transform(
        timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
        aggregationType: AggregationType = AggregationType.SUM
    ): List<ChartPoint> {
        return toTimeDataPoint()
            .transform(timeUnit, aggregationType)
            .let { transformedTimeData ->
                when (this) {
                    is DietList -> transformedTimeData.toChartPointsByProperty("calories")
                    is BloodPressureList -> transformedTimeData.toChartPointsByProperty("systolic")
                    else -> transformedTimeData.toChartPoints()
                }
            }
    }
}

/**
 * Convenience factory functions for creating wrapper instances
 */
fun List<StepCount>.asHealthDataList(): HealthDataList.StepCountList = HealthDataList.StepCountList(this)
fun List<Exercise>.asHealthDataList(): HealthDataList.ExerciseList = HealthDataList.ExerciseList(this)
fun List<Diet>.asHealthDataList(): HealthDataList.DietList = HealthDataList.DietList(this)
fun List<HeartRate>.asHealthDataList(): HealthDataList.HeartRateList = HealthDataList.HeartRateList(this)
fun List<SleepSession>.asHealthDataList(): HealthDataList.SleepSessionList = HealthDataList.SleepSessionList(this)
fun List<BloodPressure>.asHealthDataList(): HealthDataList.BloodPressureList = HealthDataList.BloodPressureList(this)
fun List<BloodGlucose>.asHealthDataList(): HealthDataList.BloodGlucoseList = HealthDataList.BloodGlucoseList(this)
fun List<Weight>.asHealthDataList(): HealthDataList.WeightList = HealthDataList.WeightList(this)
fun List<BodyFat>.asHealthDataList(): HealthDataList.BodyFatList = HealthDataList.BodyFatList(this)
fun List<SkeletalMuscleMass>.asHealthDataList(): HealthDataList.SkeletalMuscleMassList = HealthDataList.SkeletalMuscleMassList(this)

*/


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


/**
 * 여러 datatype이 혼합된 HealthData 리스트를 타입별로 분리된 TimeDataPoint 맵으로 변환
 * 
 * @return 데이터 타입명을 키로 하고 해당 TimeDataPoint를 값으로 하는 맵
 */
fun List<HealthData>.toTimeDataPointsByType(): Map<String, TimeDataPoint> {
    val result = mutableMapOf<String, TimeDataPoint>()
    
    // 타입별로 그룹핑
    val stepCounts = filterIsInstance<StepCount>()
    val exercises = filterIsInstance<Exercise>()
    val diets = filterIsInstance<Diet>()
    val heartRates = filterIsInstance<HeartRate>()
    val sleepSessions = filterIsInstance<SleepSession>()
    val bloodPressures = filterIsInstance<BloodPressure>()
    val bloodGlucoses = filterIsInstance<BloodGlucose>()
    val weights = filterIsInstance<Weight>()
    val bodyFats = filterIsInstance<BodyFat>()
    val skeletalMuscleMasses = filterIsInstance<SkeletalMuscleMass>()
    
    // 각 타입별로 TimeDataPoint 생성
    if (stepCounts.isNotEmpty()) {
        result["StepCount"] = stepCounts.toStepCountTimeDataPoint()
    }
    if (exercises.isNotEmpty()) {
        result["Exercise"] = exercises.toExerciseTimeDataPoint()
    }
    if (diets.isNotEmpty()) {
        result["Diet"] = diets.toDietTimeDataPoint()
    }
    if (heartRates.isNotEmpty()) {
        result["HeartRate"] = heartRates.toHeartRateTimeDataPoint()
    }
    if (sleepSessions.isNotEmpty()) { 
        result["SleepSession"] = sleepSessions.toSleepSessionTimeDataPoint()
    }
    if (bloodPressures.isNotEmpty()) {
        result["BloodPressure"] = bloodPressures.toBloodPressureTimeDataPoint()
    }
    if (bloodGlucoses.isNotEmpty()) {
        result["BloodGlucose"] = bloodGlucoses.toBloodGlucoseTimeDataPoint()
    }
    if (weights.isNotEmpty()) {
        result["Weight"] = weights.toWeightTimeDataPoint()
    }
    if (bodyFats.isNotEmpty()) {
        result["BodyFat"] = bodyFats.toBodyFatTimeDataPoint()
    }
    if (skeletalMuscleMasses.isNotEmpty()) {
        result["SkeletalMuscleMass"] = skeletalMuscleMasses.toSkeletalMuscleMassTimeDataPoint()
    }
    
    return result
}

// ============================================================================
// UNIFIED transform() API using @JvmName
// ============================================================================
// Direct HealthData -> ChartPoint transformation with same method name for all types

/**
 * 단일 타입 HealthData 리스트를 직접 ChartPoint로 변환하는 편의 함수
 * 내부적으로 HealthData -> TimeDataPoint -> 시간 단위 변환 -> ChartPoint 과정을 자동 처리
 *
 * @param timeUnit 변환할 시간 단위 (기본값: DAY)
 * @param aggregationType 집계 방법 (기본값: SUM)
 * @return ChartPoint 리스트
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
 * Diet 리스트를 직접 ChartPoint로 변환하는 편의 함수 (다중 값 - 칼로리만 사용)
 */
@JvmName("dietTransform")
fun List<Diet>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPointsByProperty("calories")
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
 * BloodPressure 리스트를 직접 ChartPoint로 변환하는 편의 함수 (수축기혈압 기본값)
 */
@JvmName("bloodPressureTransform")
fun List<BloodPressure>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.AVERAGE
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPointsByProperty("systolic")
}

/**
 * BloodPressure 리스트에서 특정 속성을 선택하여 ChartPoint로 변환하는 편의 함수
 */
@JvmName("bloodPressureTransformByProperty")
fun List<BloodPressure>.transformByProperty(
    property: String, // "systolic" or "diastolic"
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.AVERAGE
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
    aggregationType: AggregationType = AggregationType.AVERAGE
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

/**
 * Weight 리스트를 직접 ChartPoint로 변환하는 편의 함수
 */
@JvmName("weightTransform")
fun List<Weight>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.AVERAGE
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
    aggregationType: AggregationType = AggregationType.AVERAGE
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
    aggregationType: AggregationType = AggregationType.AVERAGE
): List<ChartPoint> {
    return this.toTimeDataPoint()
        .transform(timeUnit, aggregationType)
        .toChartPoints()
}

// ============================================================================
// CONVENIENCE FUNCTIONS: Mixed HealthData types
// ============================================================================

/**
 * 혼합 HealthData 리스트를 타입별로 변환하여 ChartPoint 맵으로 반환하는 편의 함수
 *
 * @param timeUnit 변환할 시간 단위
 * @param aggregationType 집계 방법
 * @return 타입명을 키로 하고 ChartPoint 리스트를 값으로 하는 맵
 */
fun List<HealthData>.transformByType(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): Map<String, List<ChartPoint>> {
    val result = mutableMapOf<String, List<ChartPoint>>()
    
    // 타입별로 그룹핑
    val stepCounts = filterIsInstance<StepCount>()
    val exercises = filterIsInstance<Exercise>()
    val diets = filterIsInstance<Diet>()
    val heartRates = filterIsInstance<HeartRate>()
    val sleepSessions = filterIsInstance<SleepSession>()
    val bloodPressures = filterIsInstance<BloodPressure>()
    val bloodGlucoses = filterIsInstance<BloodGlucose>()
    val weights = filterIsInstance<Weight>()
    val bodyFats = filterIsInstance<BodyFat>()
    val skeletalMuscleMasses = filterIsInstance<SkeletalMuscleMass>()
    
    // 각 타입별로 ChartPoint로 직접 변환
    if (stepCounts.isNotEmpty()) {
        result["StepCount"] = stepCounts.transform(timeUnit, aggregationType)
    }
    if (exercises.isNotEmpty()) {
        result["Exercise"] = exercises.transform(timeUnit, aggregationType)
    }
    if (diets.isNotEmpty()) {
        result["Diet"] = diets.transform(timeUnit, aggregationType) // 기본적으로 calories
    }
    if (heartRates.isNotEmpty()) {
        result["HeartRate"] = heartRates.toTimeDataPoint().transform(timeUnit, aggregationType).toChartPoints()
    }
    if (sleepSessions.isNotEmpty()) {
        result["SleepSession"] = sleepSessions.toTimeDataPoint().transform(timeUnit, aggregationType).toChartPoints()
    }
    if (bloodPressures.isNotEmpty()) {
        result["BloodPressure"] = bloodPressures.transform(timeUnit, aggregationType) // 기본적으로 systolic
    }
    if (bloodGlucoses.isNotEmpty()) {
        result["BloodGlucose"] = bloodGlucoses.transform(timeUnit, aggregationType)
    }
    if (weights.isNotEmpty()) {
        result["Weight"] = weights.transform(timeUnit, aggregationType)
    }
    if (bodyFats.isNotEmpty()) {
        result["BodyFat"] = bodyFats.transform(timeUnit, aggregationType)
    }
    if (skeletalMuscleMasses.isNotEmpty()) {
        result["SkeletalMuscleMass"] = skeletalMuscleMasses.transform(timeUnit, aggregationType)
    }

    return result
}

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
