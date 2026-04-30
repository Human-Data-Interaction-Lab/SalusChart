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
    windowSize: Int? = null,
    pageSize: Int? = null,
    renderTooltipExternally: Boolean = true,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showLegend: Boolean = false,
    legendLabel: String = "",
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
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

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<ChartMark>` | — | Chart data |
| `xLabel` | `String` | `"Time"` | X-axis label |
| `yLabel` | `String` | `"Value"` | Y-axis label |
| `title` | `String` | `"Line Chart Example"` | Chart title |
| `lineColor` | `Color` | `Color.Unspecified` | Line and point color |
| `strokeWidth` | `Float` | `4f` | Line thickness in pixels |
| `minY` | `Double?` | `null` | Y-axis minimum |
| `maxY` | `Double?` | `null` | Y-axis maximum |
| `pointRadius` | `Pair<Dp, Dp>` | `(4.dp, 2.dp)` | Outer and inner radius of selected point |
| `interactionType` | `InteractionType.Line` | `POINT` | Tap target mode |
| `windowSize` | `Int?` | `null` | Visible points in scroll mode |
| `pageSize` | `Int?` | `null` | Points per page |
| `referenceLines` | `List<ReferenceLineSpec>` | `[]` | Guide lines |
| `showLegend` | `Boolean` | `false` | Show legend |
| `yAxisPosition` | `YAxisPosition` | `LEFT` | Y-axis side |
