# ProgressChart

Shows progress toward goals. Supports two modes:

- **Donut/ring** (`isDonut = true`) — concentric rings, one per item
- **Bar** (`isDonut = false`) — stacked horizontal progress bars

**Import:** `com.hdil.saluschart.ui.compose.charts.ProgressChart`

## Signature

```kotlin
@Composable
fun ProgressChart(
    modifier: Modifier = Modifier,
    data: List<ProgressChartMark>,
    title: String = "Progress Chart",
    isDonut: Boolean = true,
    isPercentage: Boolean = true,
    colors: List<Color>? = null,
    donutHeight: Dp = 200.dp,
    strokeWidth: Dp = 20.dp,
    barHeight: Dp = 16.dp,
    barSpacing: Dp = 10.dp,
    showLabels: Boolean = true,
    showValues: Boolean = true,
    showCenterInfo: Boolean = true,
    centerTitle: String = "Activity",
    centerSubtitle: String = "Progress",
    showLegend: Boolean = true,
    interactionsEnabled: Boolean = true,
    tooltipEnabled: Boolean = true,
    tooltipFormatter: ((ProgressChartMark) -> String)? = null,
    tooltipContent: (@Composable (mark: ProgressChartMark, color: Color, modifier: Modifier) -> Unit)? = null,
)
```

## Activity rings

![ProgressChart activity rings](/charts/progress-chart-rings.png)

```kotlin
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.ui.compose.charts.ProgressChart

ProgressChart(
    modifier = Modifier.fillMaxWidth().height(280.dp),
    data = listOf(
        ProgressChartMark(x = 0.0, current = 420.0, max = 600.0, label = "Move", unit = "kcal"),
        ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0,  label = "Exercise", unit = "min"),
        ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0,  label = "Stand", unit = "hr"),
    ),
    title = "Daily activity",
    isDonut = true,
    strokeWidth = 20.dp,
    colors = listOf(
        Color(0xFFE91E63),
        Color(0xFF4CAF50),
        Color(0xFF9C27B0),
    ),
    showLegend = true,
)
```

## Progress bars

![ProgressChart progress bars](/charts/progress-chart-bars.png)

```kotlin
ProgressChart(
    data = activityData,
    title = "Today's goals",
    isDonut = false,
    isPercentage = true,
    colors = listOf(
        Color(0xFF00C7BE),
        Color(0xFFFF6B35),
        Color(0xFF3A86FF),
    ),
)
```

## Custom tooltip

```kotlin
ProgressChart(
    data = data,
    tooltipFormatter = { mark ->
        "${mark.label}: ${mark.current.toInt()} / ${mark.max.toInt()} ${mark.unit}"
    }
)
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<ProgressChartMark>` | — | Progress items |
| `title` | `String` | `"Progress Chart"` | Chart title |
| `isDonut` | `Boolean` | `true` | Rings (true) or bars (false) |
| `isPercentage` | `Boolean` | `true` | Show percentage vs. raw values |
| `colors` | `List<Color>?` | `null` | Per-item colors |
| `donutHeight` | `Dp` | `200.dp` | Height of the ring area |
| `strokeWidth` | `Dp` | `20.dp` | Ring stroke width |
| `barHeight` | `Dp` | `16.dp` | Bar height in bar mode |
| `barSpacing` | `Dp` | `10.dp` | Gap between bars |
| `showLabels` | `Boolean` | `true` | Show item labels |
| `showValues` | `Boolean` | `true` | Show current/max values |
| `showCenterInfo` | `Boolean` | `true` | Show center text in donut mode |
| `centerTitle` | `String` | `"Activity"` | Center title text |
| `centerSubtitle` | `String` | `"Progress"` | Center subtitle text |
| `showLegend` | `Boolean` | `true` | Show legend |
| `tooltipEnabled` | `Boolean` | `true` | Enable tooltips |
| `tooltipFormatter` | `((ProgressChartMark) -> String)?` | `null` | Custom tooltip text |
| `tooltipContent` | `(@Composable (...) -> Unit)?` | `null` | Fully custom tooltip composable |
