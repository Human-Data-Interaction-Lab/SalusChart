# BarChart

Vertical bar chart with optional paging, scrolling, legends, reference lines, and interactive tooltips.

**Import:** `com.hdil.saluschart.ui.compose.charts.BarChart`

## Signature

```kotlin
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Bar Chart Example",
    barColor: Color = Color.Unspecified,
    minY: Double? = null,
    maxY: Double? = null,
    barWidthRatio: Float = 0.8f,
    xLabelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Bar = InteractionType.Bar.BAR,
    onBarClick: ((index: Int, value: Double) -> Unit)? = null,
    showTitle: Boolean = false,
    showYAxis: Boolean = true,
    showLabel: Boolean = false,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    yTickStep: Double? = null,
    unit: String = "",
    showYAxisHighlight: Boolean = false,
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    initialPageIndex: Int? = null,
    yAxisFixedWidth: Dp = 20.dp,
    barCornerRadiusFraction: Float = 0f,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
    roundTopOnly: Boolean = false,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showLegend: Boolean = false,
    legendLabel: String = "",
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    tooltipColor: Color = Color.Unspecified,
)
```

## Basic example

![BarChart basic example](/charts/bar-chart-basic.png)

```kotlin
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.compose.charts.BarChart

BarChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    data = listOf(
        ChartMark(x = 0.0, y = 4200.0, label = "Mon"),
        ChartMark(x = 1.0, y = 7800.0, label = "Tue"),
        ChartMark(x = 2.0, y = 6100.0, label = "Wed"),
        ChartMark(x = 3.0, y = 9500.0, label = "Thu"),
        ChartMark(x = 4.0, y = 3200.0, label = "Fri"),
    ),
    title = "Daily steps",
    xLabel = "Day",
    yLabel = "Steps",
    barColor = Color(0xFF7C4DFF),
)
```

## Paging

```kotlin
BarChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    data = hourlyData,   // 168 hours in a week
    title = "Hourly steps",
    pageSize = 24,       // 24 bars per page, swipe to navigate
)
```

## Scrolling

```kotlin
BarChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    data = dailyData,
    title = "Rolling 30 days",
    windowSize = 7,      // show 7 bars, scroll to see more
)
```

## With reference line

![BarChart with reference line](/charts/bar-chart-reference-line.png)

```kotlin
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartDraw.LineStyle

BarChart(
    data = data,
    referenceLines = listOf(
        ReferenceLineSpec(
            y = 8000.0,
            label = "Goal",
            style = LineStyle.DASHED,
            color = Color(0xFFFF5722),
            showLabel = true,
        )
    )
)
```

## Rounded corners

![BarChart rounded corners](/charts/bar-chart-rounded.png)

```kotlin
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions

BarChart(
    data = data,
    barCornerRadiusFractions = BarCornerRadiusFractions(
        topStart = 0.4f, topEnd = 0.4f,
        bottomStart = 0f, bottomEnd = 0f,
    )
)
```

## Parameters

#### Data & labels

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<ChartMark>` | — | Chart data (required) |
| `xLabel` | `String` | `"Time"` | X-axis label |
| `yLabel` | `String` | `"Value"` | Y-axis label |
| `title` | `String` | `"Bar Chart Example"` | Chart title |
| `unit` | `String` | `""` | Unit string appended to tooltip values (e.g. `"kg"`, `"bpm"`) |

#### Appearance

| Parameter | Type | Default | Description |
|---|---|---|---|
| `barColor` | `Color` | theme primary | Bar fill color |
| `barWidthRatio` | `Float` | `0.8f` | Bar width as fraction of slot width (0–1) |
| `barCornerRadiusFraction` | `Float` | `0f` | Uniform corner radius as a fraction of bar half-width |
| `barCornerRadiusFractions` | `BarCornerRadiusFractions?` | `null` | Per-corner radius fractions; overrides `barCornerRadiusFraction` |
| `roundTopOnly` | `Boolean` | `false` | When `true`, only the top two corners are rounded |
| `tooltipColor` | `Color` | `barColor` | Tooltip bubble background color |
| `xLabelTextSize` | `Float` | `28f` | X-axis tick label text size (px) |
| `tooltipTextSize` | `Float` | `32f` | Tooltip label text size (px) |
| `contentPadding` | `PaddingValues` | `PaddingValues(16.dp)` | Padding applied around the chart content area |

#### Axes

| Parameter | Type | Default | Description |
|---|---|---|---|
| `minY` | `Double?` | `null` | Y-axis minimum (auto if null) |
| `maxY` | `Double?` | `null` | Y-axis maximum (auto if null) |
| `yTickStep` | `Double?` | `null` | Fixed interval between Y-axis grid lines; auto-calculated when null |
| `yAxisPosition` | `YAxisPosition` | `LEFT` | Y-axis side (`LEFT` or `RIGHT`) |
| `yAxisFixedWidth` | `Dp` | `20.dp` | Width reserved for Y-axis pane in scroll/highlight mode |
| `showYAxis` | `Boolean` | `true` | Show Y-axis line and tick labels |
| `showYAxisHighlight` | `Boolean` | `false` | Highlight reference-line values on the Y-axis |
| `xLabelAutoSkip` | `Boolean` | `true` | Skip overlapping X-axis tick labels automatically |
| `maxXTicksLimit` | `Int?` | `null` | Cap on the number of X-axis tick labels rendered |

#### Display toggles

| Parameter | Type | Default | Description |
|---|---|---|---|
| `showTitle` | `Boolean` | `false` | Show the `title` above the chart |
| `showLabel` | `Boolean` | `false` | Show value labels on top of each bar |
| `showLegend` | `Boolean` | `false` | Show legend |
| `legendLabel` | `String` | `""` | Legend entry text |
| `legendPosition` | `LegendPosition` | `BOTTOM` | Legend placement (`TOP`, `BOTTOM`, `LEFT`, `RIGHT`) |

#### Scrolling & paging

| Parameter | Type | Default | Description |
|---|---|---|---|
| `windowSize` | `Int?` | `null` | Visible bar count for free-scroll mode (mutually exclusive with `pageSize`) |
| `pageSize` | `Int?` | `null` | Bars per page for pager mode (mutually exclusive with `windowSize`) |
| `unifyYAxisAcrossPages` | `Boolean` | `true` | Share the same Y scale across all pages |
| `initialPageIndex` | `Int?` | `null` | Starting page index; defaults to last page when null |

#### Interaction

| Parameter | Type | Default | Description |
|---|---|---|---|
| `interactionType` | `InteractionType.Bar` | `BAR` | Tap target: `BAR` (bar itself) or `TOUCH_AREA` (full-height zone) |
| `onBarClick` | `((Int, Double) -> Unit)?` | `null` | Callback with tapped bar index and Y value |
| `referenceLines` | `List<ReferenceLineSpec>` | `[]` | Horizontal reference lines drawn across the plot |
