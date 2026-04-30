# Data Transform

`core:transform` converts health-domain records into chart-ready marks.

The usual pipeline is:

```text
raw platform data -> data:model health records -> TemporalDataSet -> ChartMark / RangeChartMark
```

Use this layer when your app receives many raw samples and needs daily, weekly, monthly, or min/max chart data.

## Supported time groups

```kotlin
import com.hdil.saluschart.core.util.TimeUnitGroup
```

| Time group | Typical use |
|---|---|
| `MINUTE` | baseline samples, intraday heart rate |
| `HOUR` | hourly activity or vitals |
| `DAY` | daily dashboard charts |
| `WEEK` | weekly reports |
| `MONTH` | long-term trends |
| `YEAR` | annual summaries |

## Supported aggregation

```kotlin
import com.hdil.saluschart.core.util.AggregationType
```

| Aggregation | Typical use |
|---|---|
| `SUM` | steps, calories, exercise totals |
| `DAILY_AVERAGE` | weight, blood pressure, glucose, heart rate averages |
| `DURATION_SUM` | time-based totals |
| `MIN_MAX` | range charts for heart rate or glucose |

## Step count totals

```kotlin
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup

val dailySteps = stepRecords.transform(
    timeUnit = TimeUnitGroup.DAY,
    aggregationType = AggregationType.SUM,
)

BarChart(
    data = dailySteps,
    title = "Daily steps",
    unit = "steps",
)
```

## Weight trend

Sparse measurements such as weight should usually keep gaps empty instead of filling them with zero.

```kotlin
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.MassUnit

val weeklyWeight = weightRecords.transform(
    massUnit = MassUnit.KILOGRAM,
    timeUnit = TimeUnitGroup.WEEK,
    aggregationType = AggregationType.DAILY_AVERAGE,
    fillGaps = false,
)

LineChart(
    data = weeklyWeight,
    title = "Weight",
    unit = "kg",
)
```

## Heart rate min/max range

Use `transformToRangeChartMark` when a chart should show the lowest and highest value in each bucket.

```kotlin
import com.hdil.saluschart.core.transform.transformToRangeChartMark
import com.hdil.saluschart.core.util.TimeUnitGroup

val dailyHeartRateRange = heartRateRecords.transformToRangeChartMark(
    timeUnit = TimeUnitGroup.DAY,
    fillGaps = false,
)

RangeBarChart(
    data = dailyHeartRateRange,
    title = "Heart rate range",
    unit = "bpm",
)
```

## Blood pressure properties

Blood pressure produces separate systolic and diastolic series.

```kotlin
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup

val bloodPressure = bloodPressureRecords.transform(
    timeUnit = TimeUnitGroup.DAY,
    aggregationType = AggregationType.DAILY_AVERAGE,
)

val systolic = bloodPressure.getValue("systolic")
val diastolic = bloodPressure.getValue("diastolic")
```

## Multi-property diet data

Use `transformByProperty` when a health record contains several values and the chart needs one series.

```kotlin
import com.hdil.saluschart.core.transform.transformByProperty
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup

val dailyCalories = dietRecords.transformByProperty(
    property = "calories",
    timeUnit = TimeUnitGroup.DAY,
    aggregationType = AggregationType.SUM,
)
```

Available diet properties are `calories`, `protein`, `carbohydrate`, and `fat`.

## Gap filling

Use `fillGaps = true` for dense activity data where missing buckets should render as zero.

Use `fillGaps = false` for sparse measurements where zero would be misleading:

- weight
- body fat
- skeletal muscle mass
- blood pressure
- blood glucose
- heart rate min/max ranges

## Custom platform mapping

Platform SDKs such as Apple Health, Samsung Health, and Health Services on Wear OS usually return their own data types. Map those records into `data:model` first, then call the transform APIs.

```kotlin
val records: List<StepCount> = platformStepRecords.map { record ->
    StepCount(
        startTime = record.startTime,
        endTime = record.endTime,
        stepCount = record.count,
    )
}

val chartData = records.transform(
    timeUnit = TimeUnitGroup.DAY,
    aggregationType = AggregationType.SUM,
)
```

See [Platform Integrations](./platform-integrations) for sample project links.
