# Interactions

SalusChart includes common health-app interactions for inspecting values, browsing long time ranges, and reacting to chart selections.

## Interaction types

Charts with dense data can use either direct target selection or a larger invisible touch area.

```kotlin
import com.hdil.saluschart.core.chart.InteractionType

// Directly tap the visible bar.
BarChart(
    data = dailySteps,
    interactionType = InteractionType.Bar.BAR,
)

// Tap anywhere in the bar's vertical slot.
BarChart(
    data = dailySteps,
    interactionType = InteractionType.Bar.TOUCH_AREA,
)

// Tap individual points.
LineChart(
    data = heartRate,
    interactionType = InteractionType.Line.POINT,
)

// Tap a vertical strip around each point.
LineChart(
    data = heartRate,
    interactionType = InteractionType.Line.TOUCH_AREA,
)
```

Supported interaction families:

| Chart | Direct target | Expanded target |
|---|---|---|
| `BarChart` | `InteractionType.Bar.BAR` | `InteractionType.Bar.TOUCH_AREA` |
| `LineChart` | `InteractionType.Line.POINT` | `InteractionType.Line.TOUCH_AREA` |
| `ScatterPlot` | `InteractionType.Scatter.POINT` | `InteractionType.Scatter.TOUCH_AREA` |
| `StackedBarChart` | `InteractionType.StackedBar.BAR` | `InteractionType.StackedBar.TOUCH_AREA` |
| `RangeBarChart` | `InteractionType.RangeBar.BAR` | `InteractionType.RangeBar.TOUCH_AREA` |

Use direct targets when marks are large enough to tap comfortably. Use expanded targets for small bars, dense line charts, and mobile dashboard cards.

## Tooltips

Most standard charts show a tooltip when the selected mark changes. Use `unit` and `tooltipColor` to keep the value readable and domain-specific.

```kotlin
LineChart(
    data = heartRate,
    title = "Heart rate",
    unit = "bpm",
    tooltipColor = Color(0xFFE91E63),
    interactionType = InteractionType.Line.TOUCH_AREA,
)
```

`ProgressChart` exposes additional tooltip controls for donut rings:

```kotlin
ProgressChart(
    data = activityRings,
    isDonut = true,
    tooltipEnabled = true,
    tooltipFormatter = { mark ->
        "${mark.label}: ${mark.current.toInt()} / ${mark.max.toInt()} ${mark.unit}"
    },
)
```

For custom tooltip UI, provide `tooltipContent`:

```kotlin
ProgressChart(
    data = activityRings,
    isDonut = true,
    tooltipContent = { mark, color, modifier ->
        Surface(
            modifier = modifier,
            color = color,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = "${mark.label}: ${mark.current.toInt()}",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    },
)
```

## Click callbacks

Some charts expose callbacks when a mark is selected. Use these to update surrounding UI such as summary values, detail panels, or other charts on the same screen.

```kotlin
var selectedDay by remember { mutableStateOf<String?>(null) }

BarChart(
    data = dailySteps,
    interactionType = InteractionType.Bar.TOUCH_AREA,
    onBarClick = { index, value ->
        selectedDay = "${dailySteps[index].label}: ${value.toInt()} steps"
    },
)
```

Range and stacked charts provide richer callback data:

```kotlin
RangeBarChart(
    data = heartRateRanges,
    interactionType = InteractionType.RangeBar.TOUCH_AREA,
    onBarClick = { index, mark ->
        // Use mark.minPoint and mark.maxPoint for the selected range.
    },
)

StackedBarChart(
    data = nutrition,
    interactionType = InteractionType.StackedBar.BAR,
    onBarClick = { barIndex, segmentIndex, value ->
        // segmentIndex is null when the whole bar is selected.
    },
)
```

## Scrolling and paging

For long time-series data, use either `windowSize` or `pageSize`.

| Mode | Parameter | Behavior |
|---|---|---|
| Static | default | Render all points in one chart |
| Scrolling | `windowSize = N` | Show N items and scroll horizontally |
| Paging | `pageSize = N` | Show N items per page and swipe between pages |

`windowSize` and `pageSize` are mutually exclusive.

```kotlin
// Rolling 30-day history with 7 visible days.
BarChart(
    data = dailySteps,
    windowSize = 7,
)

// One day per page for hourly samples.
LineChart(
    data = hourlyHeartRate,
    pageSize = 24,
    initialPageIndex = null, // defaults to the latest page
)
```

In paged mode, `unifyYAxisAcrossPages = true` keeps the Y-axis scale stable as the user swipes between pages.

## Reference lines

Reference lines can show goals, thresholds, averages, trends, and zones. They can also be interactive.

```kotlin
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType

BarChart(
    data = dailySteps,
    referenceLines = listOf(
        ReferenceLineSpec(
            type = ReferenceLineType.THRESHOLD,
            y = 10000.0,
            label = "Goal",
            showLabel = true,
            interactive = true,
            style = LineStyle.DASHED,
            color = Color(0xFFFF5722),
            onClick = {
                // React to the goal line tap.
            },
        ),
    ),
)
```

For health ranges, use `ReferenceLineType.ZONE`:

```kotlin
LineChart(
    data = restingHeartRate,
    referenceLines = listOf(
        ReferenceLineSpec(
            type = ReferenceLineType.ZONE,
            y = 60.0,
            yEnd = 100.0,
            label = "Typical range",
            showLabel = true,
            color = Color(0x3326A69A),
        ),
    ),
)
```

## Coordinated dashboards

Keep selected state in the parent composable when one chart should drive another part of the screen.

```kotlin
@Composable
fun WeeklyHealthDashboard(
    steps: List<ChartMark>,
    heartRate: List<ChartMark>,
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column {
        BarChart(
            data = steps,
            interactionType = InteractionType.Bar.TOUCH_AREA,
            onBarClick = { index, _ -> selectedIndex = index },
        )

        LineChart(
            data = selectedIndex?.let { listOf(heartRate[it]) } ?: heartRate,
            title = selectedIndex?.let { "Heart rate on ${steps[it].label}" } ?: "Heart rate",
            interactionType = InteractionType.Line.TOUCH_AREA,
        )
    }
}
```

This pattern works well for health dashboards where selecting a day updates summary cards, detail charts, and explanatory text together.
