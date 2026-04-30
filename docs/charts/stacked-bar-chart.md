# StackedBarChart

Vertical bars with multiple stacked segments per x position. Marks sharing the same `x` value form one stacked bar.

**Import:** `com.hdil.saluschart.ui.compose.charts.StackedBarChart`

## Data

Marks are grouped by their `x` value. Each group becomes one stacked bar, with each mark in the group becoming one segment (bottom to top in list order).

```kotlin
import com.hdil.saluschart.core.chart.ChartMark

// Three days of nutrition data, four macros per day
val data = listOf(
    ChartMark(x = 0.0, y = 250.0, label = "Mon"),  // carbs
    ChartMark(x = 0.0, y = 80.0,  label = "Mon"),  // protein
    ChartMark(x = 0.0, y = 60.0,  label = "Mon"),  // fat
    ChartMark(x = 1.0, y = 220.0, label = "Tue"),
    ChartMark(x = 1.0, y = 90.0,  label = "Tue"),
    ChartMark(x = 1.0, y = 70.0,  label = "Tue"),
)

val segmentLabels = listOf("Carbs", "Protein", "Fat")
val colors = listOf(Color(0xFF7C4DFF), Color(0xFF26A69A), Color(0xFFFF9800))
```

## Basic example

![StackedBarChart](/charts/stacked-bar-chart.png)

```kotlin
import com.hdil.saluschart.ui.compose.charts.StackedBarChart

StackedBarChart(
    modifier = Modifier.fillMaxWidth().height(350.dp),
    data = data,
    segmentLabels = segmentLabels,
    colors = colors,
    title = "Daily nutrition",
    xLabel = "Day",
    yLabel = "kcal",
)
```

## Scrolling

```kotlin
StackedBarChart(
    data = data,
    segmentLabels = segmentLabels,
    colors = colors,
    windowSize = 7,   // show 7 bars, free scroll
)
```

## Paging

```kotlin
StackedBarChart(
    data = data,
    segmentLabels = segmentLabels,
    colors = colors,
    pageSize = 7,     // 7 bars per page
)
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<ChartMark>` | — | Chart data (grouped by x) |
| `segmentLabels` | `List<String>` | `[]` | Label per segment layer |
| `colors` | `List<Color>` | `[]` | Color per segment layer |
| `xLabel` | `String` | `"Time"` | X-axis label |
| `yLabel` | `String` | `"Value"` | Y-axis label |
| `title` | `String` | — | Chart title |
| `windowSize` | `Int?` | `null` | Scroll window size |
| `pageSize` | `Int?` | `null` | Items per page |
| `showLegend` | `Boolean` | `false` | Show legend |
| `yAxisPosition` | `YAxisPosition` | `LEFT` | Y-axis side |
