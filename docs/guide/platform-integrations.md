# Platform Integrations

SalusChart does not require a specific health data provider. Apps can read from Apple Health, Samsung Health, Health Connect, Wear OS, or their own backend, then map those records into SalusChart models.

## Integration flow

```text
platform SDK records
    -> app mapper
    -> data:model health records
    -> core:transform aggregation
    -> ui:compose or ui:wear-compose chart
```

The chart layer stays the same even when the source platform changes.

## Sample apps

Complete sample projects are available here:

| Platform | Sample |
|---|---|
| Apple Health | [SalusChart-AppleHealth-Sample](https://github.com/HDIL-YS/SalusChart-AppleHealth-Sample) |
| Samsung Health | [SalusChart-SamsungHealth-Sample](https://github.com/HDIL-YS/SalusChart-SamsungHealth-Sample) |
| Wear OS | [SalusChart-WearOS-Sample](https://github.com/HDIL-YS/SalusChart-WearOS-Sample) |

Use these samples to see the platform permission flow, data loading layer, and mapping code in context.

## Map records into common models

Each platform exposes different record classes, but the app can normalize them into SalusChart's health models.

```kotlin
import com.hdil.saluschart.data.model.model.StepCount

fun mapSteps(record: PlatformStepRecord): StepCount {
    return StepCount(
        startTime = record.startTime,
        endTime = record.endTime,
        stepCount = record.count,
    )
}
```

For body measurements:

```kotlin
import com.hdil.saluschart.data.model.model.Mass
import com.hdil.saluschart.data.model.model.Weight

fun mapWeight(record: PlatformWeightRecord): Weight {
    return Weight(
        time = record.time,
        weight = Mass.kilograms(record.kilograms),
    )
}
```

For heart rate samples:

```kotlin
import com.hdil.saluschart.data.model.model.HeartRate
import com.hdil.saluschart.data.model.model.HeartRateSample

fun mapHeartRate(session: PlatformHeartRateSession): HeartRate {
    return HeartRate(
        startTime = session.startTime,
        endTime = session.endTime,
        samples = session.samples.map {
            HeartRateSample(
                time = it.time,
                beatsPerMinute = it.bpm,
            )
        },
    )
}
```

## Transform after mapping

Once records are normalized, use the same transform code for every platform.

```kotlin
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup

val chartData = stepRecords.transform(
    timeUnit = TimeUnitGroup.DAY,
    aggregationType = AggregationType.SUM,
)

BarChart(data = chartData, title = "Steps")
```

## Choosing modules

For phone apps:

```kotlin
dependencies {
    implementation("io.github.hdilys:saluschart-ui-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
    implementation("io.github.hdilys:saluschart-core-transform:$salusVersion")
}
```

For Wear OS apps:

```kotlin
dependencies {
    implementation("io.github.hdilys:saluschart-ui-wear-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
}
```

See [Modules](./modules) for the full module map.
