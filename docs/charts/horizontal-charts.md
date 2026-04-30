# Horizontal Charts

## HorizontalRangeBarChart

Horizontal version of `RangeBarChart`. Each row shows a min–max range. Useful for sleep consistency timelines and activity time windows.

**Import:** `com.hdil.saluschart.ui.compose.charts.HorizontalRangeBarChart`

### Example

```kotlin
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.ui.compose.charts.HorizontalRangeBarChart

val data = listOf(
    RangeChartMark(
        x = 0.0,
        minPoint = ChartMark(x = 0.0, y = 23.0),  // 23:00 (sleep start)
        maxPoint = ChartMark(x = 0.0, y = 31.0),  // 07:00 next day (31h on 24h wrap)
        label = "Mon"
    ),
    RangeChartMark(
        x = 1.0,
        minPoint = ChartMark(x = 1.0, y = 22.5),
        maxPoint = ChartMark(x = 1.0, y = 30.5),
        label = "Tue"
    ),
)

HorizontalRangeBarChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    data = data,
    title = "Sleep consistency",
    minX = 20.0,
    maxX = 32.0,
    goodColor = Color(0xFF7C4DFF),
    bottomStartLabel = "20:00",
    bottomEndLabel = "08:00",
    unit = "시간",
)
```

### Key parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<RangeChartMark>` | — | Range data |
| `minX` | `Double` | — | Scale minimum (required) |
| `maxX` | `Double` | — | Scale maximum (required) |
| `title` | `String?` | `null` | Card title |
| `rowLabels` | `List<String>` | auto | Label for each row |
| `goodColor` | `Color` | `Color.Unspecified` | Bar fill color for normal ranges |
| `badColor` | `Color` | `Color(0xFFD6D6D6)` | Bar fill color when `isGood` returns false |
| `isGood` | `(RangeChartMark) -> Boolean` | `{ true }` | Per-bar color selector |
| `barThickness` | `Dp` | `10.dp` | Bar height |
| `rowHeight` | `Dp` | `42.dp` | Height of each row slot |
| `bottomStartLabel` | `String` | `""` | Label below left end of axis |
| `bottomEndLabel` | `String` | `""` | Label below right end of axis |
| `unit` | `String` | `"시간"` | Unit shown in tooltip |
| `enableTooltip` | `Boolean` | `true` | Show tooltip on tap |

---

## HorizontalStackedBarChartList

A list of horizontal stacked bar rows, one per item. Each row has segments drawn left to right. Suitable for macronutrient breakdowns, time allocation, and activity tracking.

**Import:** `com.hdil.saluschart.ui.compose.charts.HorizontalStackedBarChartList`

### Data types

```kotlin
import com.hdil.saluschart.ui.compose.charts.HorizontalStackedBarRow
import com.hdil.saluschart.ui.compose.charts.HorizontalStackedBarChartList

val rows = listOf(
    HorizontalStackedBarRow(
        title = "Monday",
        unit = "g",
        total = 390f,
        segments = listOf(250f, 80f, 60f),
        segmentLabels = listOf("Carbs", "Protein", "Fat"),
    ),
    HorizontalStackedBarRow(
        title = "Tuesday",
        unit = "g",
        total = 380f,
        segments = listOf(220f, 90f, 70f),
        segmentLabels = listOf("Carbs", "Protein", "Fat"),
    ),
)

val colors = listOf(
    Color(0xFF7C4DFF),
    Color(0xFF26A69A),
    Color(0xFFFF9800),
)

val legendLabels = listOf("Carbs", "Protein", "Fat")
```

### Example

![HorizontalStackedBarChartList](/charts/horizontal-stacked-bar.png)

```kotlin
HorizontalStackedBarChartList(
    modifier = Modifier.fillMaxWidth(),
    title = "Weekly nutrition",
    rows = rows,
    colors = colors,
    legendLabels = legendLabels,
    showLegend = true,
)
```

### Key parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `title` | `String` | — | Section title |
| `rows` | `List<HorizontalStackedBarRow>` | — | Row data |
| `colors` | `List<Color>` | `[]` | Segment colors (in order) |
| `legendLabels` | `List<String>` | `[]` | Legend labels (in order) |
| `showLegend` | `Boolean` | `false` | Show legend |
| `legendPosition` | `LegendPosition` | `BOTTOM` | Legend placement |
| `barTrackColor` | `Color` | `Color(0xFFF1F1F1)` | Background track color |
| `onRowClick` | `((Int, Int?, Float) -> Unit)?` | `null` | Tap callback: (rowIndex, segmentIndex?, value) |

### HorizontalStackedBarRow fields

| Field | Type | Description |
|---|---|---|
| `title` | `String` | Row label (e.g. day name) |
| `unit` | `String` | Unit shown in tooltip |
| `total` | `Float` | Total value (100% bar width) |
| `segments` | `List<Float>` | Segment values in order |
| `segmentLabels` | `List<String>` | Per-segment labels (optional) |
| `trackMax` | `Float?` | Override max for track width (optional) |
