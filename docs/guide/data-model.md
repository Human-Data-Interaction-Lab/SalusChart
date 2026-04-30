# Data Model

SalusChart uses a small set of mark types as its chart input format.

## ChartMark

The standard input for most charts.

```kotlin
import com.hdil.saluschart.core.chart.ChartMark

data class ChartMark(
    val x: Double,          // category index or time position
    val y: Double,          // value to render
    val label: String? = null,  // x-axis label / legend text
    val color: Int? = null,     // per-point color override (ARGB Int)
    val isSelected: Boolean = false
)
```

Used by: `BarChart`, `LineChart`, `ScatterPlot`, `PieChart`, `StackedBarChart`.

```kotlin
val marks = listOf(
    ChartMark(x = 0.0, y = 4200.0, label = "Mon"),
    ChartMark(x = 1.0, y = 7800.0, label = "Tue"),
    ChartMark(x = 2.0, y = 6100.0, label = "Wed"),
)
```

## RangeChartMark

For charts that show a min–max range at each x position.

```kotlin
import com.hdil.saluschart.core.chart.RangeChartMark

data class RangeChartMark(
    val x: Double,
    val minPoint: ChartMark,
    val maxPoint: ChartMark,
    val label: String? = null
)
```

Used by: `RangeBarChart`, `HorizontalRangeBarChart`.

```kotlin
val marks = listOf(
    RangeChartMark(
        x = 0.0,
        minPoint = ChartMark(x = 0.0, y = 55.0),
        maxPoint = ChartMark(x = 0.0, y = 150.0),
        label = "Mon"
    ),
)
```

## ProgressChartMark

For progress rings and bars.

```kotlin
import com.hdil.saluschart.core.chart.ProgressChartMark

data class ProgressChartMark(
    val x: Double,
    val current: Double,
    val max: Double,
    val label: String? = null,
    val unit: String? = null,
    val color: Int? = null,
    val isSelected: Boolean = false
)
// Computed: progress = current / max (clamped 0..1)
//           percentage = progress * 100
```

Used by: `ProgressChart`, `CalendarEntry.rings`.

```kotlin
val rings = listOf(
    ProgressChartMark(x = 0.0, current = 420.0, max = 600.0, label = "Move", unit = "kcal"),
    ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0,  label = "Exercise", unit = "min"),
    ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0,  label = "Stand", unit = "hr"),
)
```

## CalendarEntry

One cell in a `CalendarChart`.

```kotlin
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import java.time.LocalDate

data class CalendarEntry(
    val date: LocalDate,
    val value: Float,           // drives bubble size or color intensity
    val color: Color? = null,   // override bubble color
    val rings: List<ProgressChartMark>? = null  // for MINI_RINGS marker type
)
```

## GaugeSegment

Defines a colored zone in a `MultiSegmentGaugeChart` or `RangeGaugeChart`.

```kotlin
import com.hdil.saluschart.ui.compose.charts.GaugeSegment

data class GaugeSegment(
    val start: Float,
    val end: Float,
    val color: Color
)
```

## Health data models

These live in `data:model` and are inputs to `core:transform`.

### SleepSession

```kotlin
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import com.hdil.saluschart.data.model.model.SleepStageType
import java.time.Instant

val session = SleepSession(
    startTime = Instant.parse("2024-01-15T23:00:00Z"),
    endTime   = Instant.parse("2024-01-16T07:00:00Z"),
    stages = listOf(
        SleepStage(
            startTime = Instant.parse("2024-01-15T23:00:00Z"),
            endTime   = Instant.parse("2024-01-15T23:30:00Z"),
            stage = SleepStageType.LIGHT
        ),
        SleepStage(
            startTime = Instant.parse("2024-01-15T23:30:00Z"),
            endTime   = Instant.parse("2024-01-16T01:00:00Z"),
            stage = SleepStageType.DEEP
        ),
    )
)
```

`SleepStageType` values: `AWAKE`, `REM`, `LIGHT`, `DEEP`, `UNKNOWN`.

### Mass

```kotlin
import com.hdil.saluschart.data.model.model.Mass
import com.hdil.saluschart.data.model.model.MassUnit

val weight = Mass(inGrams = 70_000.0)
weight.toKilograms() // 70.0
weight.toPounds()    // 154.3...
```

`MassUnit` values: `KILOGRAM`, `POUND`, `GRAM`, `OUNCE`.

## Transforming health data

Use `core:transform` to aggregate raw health records into chart marks:

```kotlin
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.transform.transformToChartMark
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup

val chartMarks = rawHealthData
    .transform(TimeUnitGroup.DAY, AggregationType.SUM)
    .transformToChartMark()
```
