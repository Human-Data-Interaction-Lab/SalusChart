package com.hdil.saluschart.core.transform

import com.hdil.saluschart.core.chart.BaseChartMark
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.data.model.model.*

/**
 * TemporalDataSet 시간 단위 변환 및 HealthData → ChartMark 편의 함수 모음
 * 
 * 이 파일은 HealthData를 ChartMark로 직접 변환하는 편의 메서드들을 포함합니다.
 * 내부적으로 HealthData → TemporalDataSet → Transform → ChartMark 과정을 거칩니다.
 */

// ═══════════════════════════════════════════════════════════════════════════
// Internal Helper Functions (TemporalDataSet 변환)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * TemporalDataSet에 대한 확장 함수 - 시간 단위 변환
 * 
 * 내부적으로 사용되는 헬퍼 함수입니다.
 * 외부에서는 HealthData의 .transform() 편의 함수를 사용하세요.
 */
internal fun TemporalDataSet.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): TemporalDataSet {
    return DataTransformer().transform(
        data = this,
        transformTimeUnit = timeUnit,
        aggregationType = aggregationType
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// HealthData → ChartMark 편의 변환 함수들
// ═══════════════════════════════════════════════════════════════════════════
// 
// 참고: 모든 변환 함수는 기본적으로 시간 간격을 자동으로 채웁니다 (fillGaps = true).
// 이는 누락된 날짜/시간을 0 값으로 표시하여 연속적인 차트를 생성합니다.
// 이 동작을 비활성화하려면 fillGaps = false를 명시적으로 전달하세요.
//
// 예: exerciseData.transform(timeUnit = TimeUnitGroup.DAY, fillGaps = false)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * StepCount 리스트를 직접 ChartMark로 변환하는 편의 함수
 * 
 * 내부적으로 HealthData → TemporalDataSet → Transform → ChartMark 과정을 자동 처리합니다.
 * 기본적으로 누락된 시간 포인트를 0으로 채웁니다.
 * 
 * @param timeUnit 변환할 시간 단위 (기본값: DAY)
 * @param aggregationType 집계 방법 (기본값: SUM)
 * @param fillGaps 누락된 시간 포인트를 0으로 채울지 여부 (기본값: true)
 * @return ChartMark 리스트
 */
@JvmName("stepCountTransform")
fun List<StepCount>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM,
    fillGaps: Boolean = true
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps)
}

/**
 * Exercise 리스트를 직접 ChartMark로 변환하는 편의 함수
 * 기본적으로 누락된 시간 포인트를 0으로 채웁니다.
 */
@JvmName("exerciseTransform")
fun List<Exercise>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM,
    fillGaps: Boolean = true
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps)
}

/**
 * Diet 리스트를 직접 ChartMark로 변환하는 편의 함수
 * 모든 속성(calories, protein, carbohydrate, fat)을 라벨에 포함하여 반환합니다.
 */
@JvmName("dietTransform")
fun List<Diet>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): List<ChartMark> {
    val ChartMarksMap = this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarksMap()

    return ChartMarksMap.flatMap { (property, ChartMarks) ->
        ChartMarks.map { ChartMark ->
            ChartMark(
                x = ChartMark.x,
                y = ChartMark.y,
                label = "${ChartMark.label} ($property)"
            )
        }
    }
}

/**
 * Diet 리스트에서 특정 속성을 선택하여 ChartMark로 변환하는 편의 함수
 * 
 * @param property 추출할 속성 ("calories", "protein", "carbohydrate", "fat")
 */
@JvmName("dietTransformByProperty")
fun List<Diet>.transformByProperty(
    property: String,
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarksByProperty(property)
}

/**
 * BloodPressure 리스트를 속성별로 분리된 ChartMark 맵으로 변환하는 편의 함수
 * 다중 시리즈 차트에 유용함 (각 속성을 다른 색상으로 표시)
 * 
 * @return "systolic"과 "diastolic" 키를 가진 맵
 */
@JvmName("bloodPressureTransform")
fun List<BloodPressure>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = true
): Map<String, List<ChartMark>> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarksMap(fillGaps)
}

/**
 * BloodPressure 리스트에서 특정 속성을 선택하여 ChartMark로 변환하는 편의 함수
 * 
 * @param property "systolic" 또는 "diastolic"
 */
@JvmName("bloodPressureTransformByProperty")
fun List<BloodPressure>.transformByProperty(
    property: String, // "systolic" or "diastolic"
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = true
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarksByProperty(property, fillGaps)
}

/**
 * BloodGlucose 리스트를 직접 ChartMark로 변환하는 편의 함수
 */
@JvmName("bloodGlucoseTransform")
fun List<BloodGlucose>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.MIN_MAX,
    fillGaps: Boolean = true
): List<BaseChartMark> {
    if (aggregationType == AggregationType.MIN_MAX) {
        return this.toTemporalDataSet()
            .transform(timeUnit, aggregationType)
            .toRangeChartMarks(fillGaps)
    } else {
        return this.toTemporalDataSet()
            .transform(timeUnit, aggregationType)
            .toChartMarks(fillGaps)
    }
}

/**
 * HeartRate 리스트를 ChartMark로 변환하는 편의 함수 (단일 값 집계용)
 * DAILY_AVERAGE, SUM 등 단일 값을 반환하는 집계 타입에 사용하세요.
 * LineChart, BarChart, ScatterPlot 등에 직접 사용할 수 있습니다.
 * 
 * @param timeUnit 변환할 시간 단위 (기본값: DAY)
 * @param aggregationType 집계 방법 - MIN_MAX는 사용할 수 없습니다 (기본값: DAILY_AVERAGE)
 * @param fillGaps 누락된 시간 포인트를 0으로 채울지 여부 (기본값: true)
 * @return ChartMark 리스트
 */
@JvmName("heartRateTransformToChartMark")
fun List<HeartRate>.transformToChartMark(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = true
): List<ChartMark> {
    require(aggregationType != AggregationType.MIN_MAX) {
        "MIN_MAX aggregation returns RangeChartMark. Use transformToRangeChartMark() instead."
    }
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps)
}

//TODO: 지우기
/**
 * HeartRate 리스트를 RangeChartMark로 변환하는 편의 함수 (MIN_MAX 집계용)
 * RangeBarChart 등에 사용하세요.
 * 
 * @param timeUnit 변환할 시간 단위 (기본값: DAY)
 * @param fillGaps 누락된 시간 포인트를 0으로 채울지 여부 (기본값: true)
 * @return RangeChartMark 리스트
 */
@JvmName("heartRateTransformToRangeChartMark")
fun List<HeartRate>.transformToRangeChartMark(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    fillGaps: Boolean = true
): List<RangeChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, AggregationType.MIN_MAX)
        .toRangeChartMarks(fillGaps)
}

/**
 * HeartRate 리스트를 직접 BaseChartMark로 변환하는 편의 함수 (범용)
 * 기본적으로 누락된 시간 포인트를 0으로 채웁니다.
 * MIN_MAX 집계의 경우, 데이터가 없는 날은 min=0, max=0으로 표시됩니다.
 */
@JvmName("heartRateTransform")
fun List<HeartRate>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.MIN_MAX,
    fillGaps: Boolean = true
): List<BaseChartMark> {
    if (aggregationType == AggregationType.MIN_MAX){
        return this.toTemporalDataSet()
            .transform(timeUnit, aggregationType)
            .toRangeChartMarks(fillGaps)
    }
    else {
        return this.toTemporalDataSet()
            .transform(timeUnit, aggregationType)
            .toChartMarks(fillGaps)
    }

}

/**
 * SleepSession 리스트를 직접 ChartMark로 변환하는 편의 함수
 */
@JvmName("sleepSessionTransform")
fun List<SleepSession>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.SUM,
    fillGaps: Boolean = true
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps)
}

/**
 * Weight 리스트를 직접 ChartMark로 변환하는 편의 함수
 * 
 * @param massUnit 질량 단위 지정 (MassUnit.KILOGRAM, MassUnit.POUND, MassUnit.GRAM 등)
 * @param timeUnit 시간 단위
 * @param aggregationType 집계 방법
 */
@JvmName("weightTransform")
fun List<Weight>.transform(
    massUnit: MassUnit = MassUnit.KILOGRAM,
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = true
): List<ChartMark> {
    // Transform to base ChartMarks first (no unit conversion yet)
    val baseChartMarks = this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps)
    
    // Now convert the y values to the target unit (keep original labels)
    return baseChartMarks.map { ChartMark ->
        // ChartMark.y is in kilograms (from Mass.toKilograms() in toTemporalDataSet)
        // Convert from kg to target unit
        val convertedValue = when (massUnit) {
            MassUnit.KILOGRAM -> ChartMark.y // Already in kg
            MassUnit.POUND -> ChartMark.y * 2.20462 // kg to pounds
            MassUnit.GRAM -> ChartMark.y * 1000.0 // kg to grams  
            MassUnit.OUNCE -> ChartMark.y * 35.274 // kg to ounces
        }
        
        ChartMark(
            x = ChartMark.x,
            y = convertedValue,
            label = ChartMark.label // Keep original time-based label
        )
    }
}

/**
 * BodyFat 리스트를 직접 ChartMark로 변환하는 편의 함수
 */
@JvmName("bodyFatTransform")
fun List<BodyFat>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = true
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps)
}

/**
 * SkeletalMuscleMass 리스트를 직접 ChartMark로 변환하는 편의 함수
 */
@JvmName("skeletalMuscleMassTransform")
fun List<SkeletalMuscleMass>.transform(
    timeUnit: TimeUnitGroup = TimeUnitGroup.DAY,
    aggregationType: AggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps: Boolean = true
): List<ChartMark> {
    return this.toTemporalDataSet()
        .transform(timeUnit, aggregationType)
        .toChartMarks(fillGaps)
}

