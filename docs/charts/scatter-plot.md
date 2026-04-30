# ScatterPlot

Individual data points on an x/y plane. Useful for distributions and two-variable data (e.g., systolic vs. diastolic blood pressure).

**Import:** `com.hdil.saluschart.ui.compose.charts.ScatterPlot`

## Basic example

![ScatterPlot basic example](/charts/scatter-plot-basic.png)

```kotlin
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.PointType
import com.hdil.saluschart.ui.compose.charts.ScatterPlot

ScatterPlot(
    modifier = Modifier.fillMaxWidth().height(400.dp),
    data = listOf(
        ChartMark(x = 0.0, y = 120.0, label = "Jan 1"),
        ChartMark(x = 1.0, y = 118.0, label = "Jan 2"),
        ChartMark(x = 2.0, y = 125.0, label = "Jan 3"),
        ChartMark(x = 3.0, y = 115.0, label = "Jan 4"),
    ),
    title = "Systolic blood pressure",
    xLabel = "Date",
    yLabel = "mmHg",
    pointColor = Color(0xFFE91E63),
    pointType = PointType.Circle,
    pointSize = 8.dp,
)
```

## Interaction modes

```kotlin
import com.hdil.saluschart.core.chart.InteractionType

// Tap the point dot exactly
ScatterPlot(data = data, interactionType = InteractionType.Scatter.POINT)

// Tap anywhere in the vertical strip
ScatterPlot(data = data, interactionType = InteractionType.Scatter.TOUCH_AREA)
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<ChartMark>` | — | Data points |
| `xLabel` | `String` | `"Time"` | X-axis label |
| `yLabel` | `String` | `"Value"` | Y-axis label |
| `title` | `String` | — | Chart title |
| `pointColor` | `Color` | `Color.Unspecified` | Point fill color |
| `pointType` | `PointType` | `Circle` | `Circle` or `Square` |
| `pointSize` | `Dp` | `8.dp` | Point size |
| `interactionType` | `InteractionType.Scatter` | `POINT` | Tap mode |
| `windowSize` | `Int?` | `null` | Scroll window |
| `pageSize` | `Int?` | `null` | Items per page |
| `referenceLines` | `List<ReferenceLineSpec>` | `[]` | Guide lines |
| `showLegend` | `Boolean` | `false` | Show legend |
| `yAxisPosition` | `YAxisPosition` | `LEFT` | Y-axis side |
