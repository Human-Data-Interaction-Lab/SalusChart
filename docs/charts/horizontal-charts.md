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

### Parameters

#### Data

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<RangeChartMark>` | — | Range data (required) |
| `minX` | `Double` | — | Axis minimum value (required) |
| `maxX` | `Double` | — | Axis maximum value (required) |

#### Header

| Parameter | Type | Default | Description |
|---|---|---|---|
| `title` | `String?` | `null` | Card title |
| `datePeriodText` | `String?` | `null` | Date range subtitle below title |

#### Row labels

| Parameter | Type | Default | Description |
|---|---|---|---|
| `rowLabels` | `List<String>` | auto (1, 2, …) | Label for each row; must have `>= data.size` entries |
| `leftLabelWidth` | `Dp` | `36.dp` | Width reserved for row labels |
| `leftLabelColor` | `Color` | `onBackground` | Row label text color |
| `labelTextSizeSp` | `Float` | `14f` | Row label font size (sp) |

#### Layout

| Parameter | Type | Default | Description |
|---|---|---|---|
| `rowHeight` | `Dp` | `42.dp` | Height of each row slot |
| `rowSpacing` | `Dp` | `12.dp` | Gap between rows |
| `barThickness` | `Dp` | `10.dp` | Bar height |
| `barCornerRadius` | `Dp` | `999.dp` | Bar corner radius (default: pill shape) |
| `chartStartPadding` | `Dp` | `18.dp` | Padding before chart area starts |
| `chartEndPadding` | `Dp` | `22.dp` | Padding after chart area ends |

#### Color

| Parameter | Type | Default | Description |
|---|---|---|---|
| `goodColor` | `Color` | theme primary | Bar fill color for rows where `isGood` returns `true` |
| `badColor` | `Color` | `Color(0xFFD6D6D6)` | Bar fill color for rows where `isGood` returns `false` |
| `isGood` | `(RangeChartMark) -> Boolean` | `{ true }` | Per-row color selector |

#### Guide lines

Two vertical "pillar" markers with dashed center lines. Useful for marking target bedtime / wake time.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `showGuides` | `Boolean` | `true` | Show pillar guides |
| `guideStartX` | `Double` | `minX` | X value for the left pillar |
| `guideEndX` | `Double` | `maxX` | X value for the right pillar |
| `pillarWidth` | `Dp` | `30.dp` | Width of each pillar background |
| `pillarFill` | `Color` | `Color(0xFF6E86FF, alpha=0.06)` | Pillar background fill |
| `dashColor` | `Color` | `Color(0xFF6E86FF, alpha=0.55)` | Dashed center line color |
| `dashWidth` | `Dp` | `1.dp` | Dashed line stroke width |
| `dashOn` | `Float` | `6f` | Dash length (px) |
| `dashOff` | `Float` | `6f` | Gap length (px) |

#### Bottom axis labels

| Parameter | Type | Default | Description |
|---|---|---|---|
| `bottomStartLabel` | `String` | `""` | Label below left end of axis |
| `bottomEndLabel` | `String` | `""` | Label below right end of axis |
| `bottomLabelTopPadding` | `Dp` | `10.dp` | Gap above bottom labels |
| `bottomLabelTextColor` | `Color` | `Color(0xFF6E86FF)` | Bottom label text color |

#### Tooltip

| Parameter | Type | Default | Description |
|---|---|---|---|
| `enableTooltip` | `Boolean` | `true` | Show tooltip on tap |
| `unit` | `String` | `"시간"` | Unit string shown in tooltip |

::: tip Tooltip format
The tooltip automatically formats sleep duration as `"N시간 M분"` or `"M분"` based on the tap position. The `unit` parameter is overridden by this automatic formatting.
:::

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

### Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `title` | `String` | — | Section title (required) |
| `datePeriodText` | `String?` | `null` | Date range subtitle below title |
| `rows` | `List<HorizontalStackedBarRow>` | — | Row data (required) |
| `colors` | `List<Color>` | theme palette | Segment colors in order; cycles if fewer than max segment count |
| `barTrackColor` | `Color` | `Color(0xFFF1F1F1)` | Background track color |
| `showLegend` | `Boolean` | `false` | Show legend |
| `legendPosition` | `LegendPosition` | `BOTTOM` | Legend placement (`TOP`, `BOTTOM`, `LEFT`, `RIGHT`) |
| `legendLabels` | `List<String>` | `[]` | Labels for legend entries; aligned with `colors` by index |
| `onRowClick` | `((Int, Int?, Float) -> Unit)?` | `null` | Tap callback: (rowIndex, segmentIndex, segmentValue) |

### HorizontalStackedBarRow fields

| Field | Type | Description |
|---|---|---|
| `title` | `String` | Row label (e.g. day name) |
| `unit` | `String` | Unit shown in tooltip |
| `total` | `Float` | Total value (100% bar width) |
| `segments` | `List<Float>` | Segment values in order |
| `segmentLabels` | `List<String>` | Per-segment labels (optional) |
| `trackMax` | `Float?` | Override max for track width (optional) |
