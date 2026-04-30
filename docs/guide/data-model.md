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
import com.hdil.saluschart.ui.compose.charts.CellMarkerType
import com.hdil.saluschart.ui.compose.charts.BubbleType
import java.time.LocalDate

data class CalendarEntry(
    val date: LocalDate,
    val value: Float,           // drives bubble size or color intensity
    val color: Color? = null,   // override bubble color
    val rings: List<ProgressChartMark>? = null  // for MINI_RINGS marker type
)
```

### CellMarkerType

Controls how each calendar cell is decorated.

| Value | Description |
|---|---|
| `BUBBLE` | Draws a circle or rectangle proportional to `value` |
| `MINI_RINGS` | Draws compact activity rings using `rings` (requires `ProgressChartMark` list) |

### BubbleType

Shape of the bubble when `CellMarkerType.BUBBLE` is used.

| Value | Description |
|---|---|
| `CIRCLE` | Circular bubble |
| `RECTANGLE` | Rounded rectangle bubble |

## BarCornerRadiusFractions

Per-corner radius fractions for bar charts. Each value is in the range `0f..1f`, where `1f` equals the bar's maximum possible corner radius (half of bar width).

```kotlin
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions

data class BarCornerRadiusFractions(
    val topStart: Float = 0f,
    val topEnd: Float = 0f,
    val bottomStart: Float = 0f,
    val bottomEnd: Float = 0f
)
```

Used by: `BarChart` (`barCornerRadiusFractions` parameter).

```kotlin
// Rounded top only (pill-top bar)
BarChart(
    data = data,
    barCornerRadiusFractions = BarCornerRadiusFractions(
        topStart = 0.4f, topEnd = 0.4f,
        bottomStart = 0f, bottomEnd = 0f,
    )
)
```

::: tip
For a uniform radius on all corners, use `barCornerRadiusFraction: Float` instead.
Use `roundTopOnly = true` as a quick shorthand for rounding only the top two corners.
:::

## HorizontalStackedBarRow

One row in a `HorizontalStackedBarChartList`.

```kotlin
import com.hdil.saluschart.ui.compose.charts.HorizontalStackedBarRow

data class HorizontalStackedBarRow(
    val title: String,                        // row label (e.g. day name)
    val unit: String,                         // unit shown in tooltip and total
    val total: Float,                         // total value displayed in the header
    val segments: List<Float>,                // segment values in order
    val segmentLabels: List<String> = emptyList(), // per-segment labels (optional)
    val trackMax: Float? = null               // override max for track width; defaults to total
)
```

Used by: `HorizontalStackedBarChartList`.

```kotlin
HorizontalStackedBarRow(
    title = "Monday",
    unit = "g",
    total = 390f,
    segments = listOf(250f, 80f, 60f),
    segmentLabels = listOf("Carbs", "Protein", "Fat"),
)
```

::: tip trackMax
Set `trackMax` when comparing rows against a shared maximum (e.g., daily calorie goal)
rather than each row's own total.
:::

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

These live in `data:model` and are inputs to `core:transform`. They provide a common shape for health data before it becomes chart marks.

| Model | Use |
|---|---|
| `StepCount` | interval step totals |
| `Exercise` | interval calories burned |
| `HeartRate` | session with timestamped BPM samples |
| `Weight` | point-in-time body weight with `Mass` |
| `BloodPressure` | point-in-time systolic and diastolic values |
| `BloodGlucose` | point-in-time glucose level |
| `BodyFat` | point-in-time body fat percentage |
| `SkeletalMuscleMass` | point-in-time skeletal muscle mass |
| `Diet` | interval meal calories and macro nutrients |
| `SleepSession` | sleep interval with optional sleep stages |

Platform SDKs can expose different raw record types. Map those records into these models first, then transform them with `core:transform`.

```text
Apple Health / Samsung Health / Wear OS records
    -> data:model records
    -> ChartMark / RangeChartMark
    -> chart composables
```

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

Use `core:transform` to aggregate health records into chart marks:

```kotlin
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup

val chartMarks = stepRecords.transform(
    timeUnit = TimeUnitGroup.DAY,
    aggregationType = AggregationType.SUM,
)
```

See [Data Transform](./data-transform) for daily, weekly, monthly, and min/max examples.
