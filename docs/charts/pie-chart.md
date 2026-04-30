# PieChart

Pie or donut chart. Each `ChartMark.y` determines the slice magnitude.

**Import:** `com.hdil.saluschart.ui.compose.charts.PieChart`

## Signature

```kotlin
@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    title: String = "Pie Chart Example",
    isDonut: Boolean = true,
    colors: List<Color> = emptyList(),
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    showLabel: Boolean = false,
    interactionsEnabled: Boolean = true,
)
```

## Donut chart

![PieChart donut](/charts/pie-chart-donut.png)

```kotlin
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.compose.charts.PieChart

PieChart(
    modifier = Modifier.fillMaxWidth().height(280.dp),
    data = listOf(
        ChartMark(x = 0.0, y = 30.0, label = "Mon"),
        ChartMark(x = 1.0, y = 20.0, label = "Tue"),
        ChartMark(x = 2.0, y = 25.0, label = "Wed"),
        ChartMark(x = 3.0, y = 15.0, label = "Thu"),
    ),
    title = "Weekly activity",
    isDonut = true,
    colors = listOf(
        Color(0xFF7C4DFF),
        Color(0xFF26A69A),
        Color(0xFFFF9800),
        Color(0xFFFFEB3B),
    ),
    showLegend = true,
    showLabel = true,
)
```

## Full pie chart

![PieChart full pie](/charts/pie-chart-full.png)

```kotlin
PieChart(
    data = data,
    isDonut = false,   // solid pie instead of donut
    showLegend = true,
    legendPosition = LegendPosition.RIGHT,
)
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<ChartMark>` | — | Slice data (y = magnitude) |
| `title` | `String` | `"Pie Chart Example"` | Chart title |
| `isDonut` | `Boolean` | `true` | Donut if true, solid pie if false |
| `colors` | `List<Color>` | `[]` | Slice colors (cycles if fewer than slices) |
| `showLegend` | `Boolean` | `false` | Show legend |
| `legendPosition` | `LegendPosition` | `BOTTOM` | Legend placement |
| `showLabel` | `Boolean` | `false` | Show percentage labels on slices |
| `interactionsEnabled` | `Boolean` | `true` | Enable tap to highlight slice |
