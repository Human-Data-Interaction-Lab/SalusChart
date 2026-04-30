# LineChart

Connected line chart with interactive point selection, scrolling, paging, reference lines, and legends.

**Import:** `com.hdil.saluschart.ui.compose.charts.LineChart`

## Signature

```kotlin
@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    xLabel: String = "Time",
    yLabel: String = "Value",
    title: String = "Line Chart Example",
    lineColor: Color = Color.Unspecified,
    strokeWidth: Float = 4f,
    minY: Double? = null,
    maxY: Double? = null,
    xLabelTextSize: Float = 28f,
    tooltipTextSize: Float = 32f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    interactionType: InteractionType.Line = InteractionType.Line.POINT,
    pointRadius: Pair<Dp, Dp> = Pair(4.dp, 2.dp),
    showLegend: Boolean = false,
    legendLabel: String = "",
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    xLabelAutoSkip: Boolean = true,
    maxXTicksLimit: Int? = null,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showYAxisHighlight: Boolean = false,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showPoint: Boolean = false,
    showValue: Boolean = false,
    yTickStep: Double? = null,
    unit: String = "",
    windowSize: Int? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    initialPageIndex: Int? = null,
    renderTooltipExternally: Boolean = true,
    yAxisFixedWidth: Dp = 30.dp,
    includeYAxisPaddingOverride: Boolean? = null,
    onMetricsCalculated: ((ChartMath.ChartMetrics) -> Unit)? = null,
    tooltipColor: Color = Color.Unspecified,
)
```

## Basic example

![LineChart basic example](/charts/line-chart-basic.png)

```kotlin
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.compose.charts.LineChart

LineChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    data = listOf(
        ChartMark(x = 0.0, y = 68.5, label = "Jan 1"),
        ChartMark(x = 1.0, y = 68.2, label = "Jan 2"),
        ChartMark(x = 2.0, y = 67.9, label = "Jan 3"),
        ChartMark(x = 3.0, y = 67.5, label = "Jan 4"),
    ),
    title = "Weight",
    xLabel = "Date",
    yLabel = "kg",
    lineColor = Color(0xFF26A69A),
    strokeWidth = 4f,
)
```

## Touch area interaction

Use `TOUCH_AREA` when points are close together or the dataset is dense:

```kotlin
import com.hdil.saluschart.core.chart.InteractionType

LineChart(
    data = data,
    interactionType = InteractionType.Line.TOUCH_AREA,
)
```

## With reference line

```kotlin
LineChart(
    data = data,
    referenceLines = listOf(
        ReferenceLineSpec(y = 70.0, label = "Target weight", showLabel = true),
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
| `title` | `String` | `"Line Chart Example"` | Chart title |
| `unit` | `String` | `""` | Unit string appended to tooltip values (e.g. `"kg"`, `"bpm"`) |

#### Appearance

| Parameter | Type | Default | Description |
|---|---|---|---|
| `lineColor` | `Color` | theme primary | Line and default tooltip color |
| `strokeWidth` | `Float` | `4f` | Line thickness (px) |
| `pointRadius` | `Pair<Dp, Dp>` | `(4.dp, 2.dp)` | Outer and inner radius of the selected-point ring |
| `showPoint` | `Boolean` | `false` | Draw a dot at every data point |
| `showValue` | `Boolean` | `false` | Draw the numeric value label next to each data point |
| `tooltipColor` | `Color` | `lineColor` | Tooltip bubble background color |
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
| `yAxisFixedWidth` | `Dp` | `30.dp` | Width reserved for external Y-axis pane in scroll mode |
| `showYAxis` | `Boolean` | `true` | Show Y-axis line and tick labels |
| `showYAxisHighlight` | `Boolean` | `false` | Highlight reference-line values on the Y-axis |
| `xLabelAutoSkip` | `Boolean` | `true` | Skip overlapping X-axis tick labels automatically |
| `maxXTicksLimit` | `Int?` | `null` | Cap on the number of X-axis tick labels rendered |
| `includeYAxisPaddingOverride` | `Boolean?` | `null` | Override automatic Y-axis padding inclusion; `null` uses default behaviour |

#### Display toggles

| Parameter | Type | Default | Description |
|---|---|---|---|
| `showTitle` | `Boolean` | `true` | Show the `title` above the chart |
| `showLegend` | `Boolean` | `false` | Show legend |
| `legendLabel` | `String` | `""` | Legend entry text |
| `legendPosition` | `LegendPosition` | `BOTTOM` | Legend placement (`TOP`, `BOTTOM`, `LEFT`, `RIGHT`) |

#### Scrolling & paging

| Parameter | Type | Default | Description |
|---|---|---|---|
| `windowSize` | `Int?` | `null` | Visible point count for free-scroll mode (mutually exclusive with `pageSize`) |
| `pageSize` | `Int?` | `null` | Points per page for pager mode (mutually exclusive with `windowSize`) |
| `unifyYAxisAcrossPages` | `Boolean` | `true` | Share the same Y scale across all pages |
| `initialPageIndex` | `Int?` | `null` | Starting page index; defaults to last page when null |

#### Interaction & advanced

| Parameter | Type | Default | Description |
|---|---|---|---|
| `interactionType` | `InteractionType.Line` | `POINT` | Tap mode: `POINT` (dot tap) or `TOUCH_AREA` (vertical strip) |
| `referenceLines` | `List<ReferenceLineSpec>` | `[]` | Horizontal reference lines |
| `renderTooltipExternally` | `Boolean` | `true` | Render tooltip as an overlay composable outside Canvas to avoid edge clipping |
| `onMetricsCalculated` | `((ChartMath.ChartMetrics) -> Unit)?` | `null` | Callback with computed chart metrics after each layout pass; useful for syncing an external Y-axis pane |
