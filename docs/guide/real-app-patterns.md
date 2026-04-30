# Real App Patterns

SalusChart is designed for screens, not only isolated chart demos. These patterns show how to combine charts, transforms, and interactions in health apps.

## Mobile dashboard

Use a dashboard when the user needs a compact overview of the current day or week.

Recommended chart mix:

- `ProgressChart` for activity rings
- `BarChart` or `MinimalBarChart` for steps
- `RangeBarChart` for heart rate min/max
- `SleepStageChart` for the latest sleep session
- `MiniActivityRings` for small summary cards

See [Mobile Dashboard](../examples/mobile-dashboard).

## Health detail screen

Use a detail screen when one metric needs history, goals, and exact values.

Recommended features:

- `LineChart` or `BarChart` with `windowSize` for long history
- `ReferenceLineSpec` for goal, average, or normal range
- `InteractionType.*.TOUCH_AREA` for comfortable selection
- `unit` and `tooltipColor` for readable inspection

```kotlin
LineChart(
    data = weeklyWeight,
    title = "Weight",
    unit = "kg",
    windowSize = 8,
    interactionType = InteractionType.Line.TOUCH_AREA,
    referenceLines = listOf(goalLine),
)
```

## Weekly report

Use weekly reports for comparison and trend summaries.

Recommended chart mix:

- `BarChart(pageSize = 7)` for daily totals
- `LineChart(pageSize = 7)` for daily averages
- `RangeBarChart(pageSize = 7)` for min/max vitals
- `ReferenceLineType.AVERAGE` or `ReferenceLineType.TREND` for context

Keep `unifyYAxisAcrossPages = true` so the scale stays stable between weeks.

## Coordinated charts

Keep selected state in the parent screen when multiple charts should respond to the same day or record.

```kotlin
var selectedIndex by remember { mutableStateOf<Int?>(null) }

BarChart(
    data = dailySteps,
    interactionType = InteractionType.Bar.TOUCH_AREA,
    onBarClick = { index, _ -> selectedIndex = index },
)

LineChart(
    data = selectedIndex?.let { listOf(heartRate[it]) } ?: heartRate,
    title = selectedIndex?.let { "Heart rate on ${dailySteps[it].label}" } ?: "Heart rate",
)
```

See [Interactions](./interactions) for more selection patterns.

## Wear OS complication style

Wear screens should stay dense and glanceable.

Recommended chart mix:

- `WearProgressRing` for activity progress
- `WearMinimalActivityRing` for compact ring summaries
- `WearMiniRangeBarChart` for heart rate range
- `WearMinimalSleepChart` for sleep overview

Guidelines:

- Fill the circular viewport with `Modifier.fillMaxSize()`
- Prefer minimal variants over axis-heavy charts
- Avoid long labels
- Use high-contrast colors for small screens

See [Wear OS Examples](../examples/wear-os) and [Wear OS Charts](../charts/wear-os-charts).

## Platform-backed apps

When the app reads from a platform health source, keep the integration layer separate from the chart UI.

```text
repository reads platform data
    -> mapper returns data:model records
    -> transform returns chart marks
    -> screen renders charts
```

This keeps Apple Health, Samsung Health, Wear OS, and backend records behind the same chart input shape.

See [Platform Integrations](./platform-integrations).
