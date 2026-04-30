# Modules

SalusChart is split into focused modules so you only pull in what you need.

## Module map

```
io.github.hdilys:saluschart-ui-compose        ← chart composables
io.github.hdilys:saluschart-ui-theme          ← color theming
io.github.hdilys:saluschart-core-chart        ← math, draw, data marks
io.github.hdilys:saluschart-core-transform    ← aggregation
io.github.hdilys:saluschart-core-util         ← enums
io.github.hdilys:saluschart-data-model        ← health models
io.github.hdilys:saluschart-ui-wear-compose   ← Wear OS charts
```

## ui:compose

The main module. Contains every `@Composable` chart function:

- `BarChart`, `LineChart`, `ScatterPlot`
- `RangeBarChart`, `HorizontalRangeBarChart`
- `StackedBarChart`, `HorizontalStackedBarChartList`
- `PieChart`, `ProgressChart`
- `SleepStageChart`, `CalendarChart`, `PagedCalendarChart`
- `MultiSegmentGaugeChart`, `RangeGaugeChart`
- `MiniActivityRings`
- All `Minimal*` compact variants

Depends on: `core:chart`, `ui:theme`.

## ui:theme

Provides `LocalSalusChartColors` — a `CompositionLocal` carrying a `SalusChartColorScheme`. Wrap your composable hierarchy to propagate a custom palette:

```kotlin
CompositionLocalProvider(
    LocalSalusChartColors provides SalusChartColorScheme(
        primary = Color(0xFF7C4DFF),
        secondary = Color(0xFF26A69A),
    )
) {
    MyScreen()
}
```

## core:chart

Low-level drawing and math:

- Data marks: `ChartMark`, `RangeChartMark`, `ProgressChartMark`, `StackedChartMark`
- Interaction: `InteractionType`, `PointType`
- Draw helpers: `ChartDraw`, `ChartLegend`, `ReferenceLine`, `ChartTooltip`
- Layout: `LegendPosition`, `YAxisPosition`
- Config: `ReferenceLineSpec`, `BarCornerRadiusFractions`

## core:transform

Aggregates raw health data into chart marks:

```kotlin
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.transform.transformToChartMark
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup

val chartData = healthData
    .transform(TimeUnitGroup.DAY, AggregationType.SUM)
    .transformToChartMark()
```

Available aggregation types: `SUM`, `AVERAGE`, `DURATION_SUM`, `MIN`, `MAX`.
Available time units: `HOUR`, `DAY`, `WEEK`, `MONTH`.

## core:util

Enums used by `core:transform`:

- `AggregationType` — how values are collapsed within a time bucket
- `TimeUnitGroup` — the bucket size

## data:model

Health-domain data classes:

- `SleepSession`, `SleepStage`, `SleepStageType`
- `Mass`, `MassUnit`
- `StepCount`, `Exercise`, `HeartRate`, `Weight`, `BodyFat`
- `BloodPressure`, `BloodGlucose`, `Diet`

## ui:wear-compose

Wear OS-optimized chart variants. See [Wear OS Charts](../charts/wear-os-charts).
