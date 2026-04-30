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
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showLabel: Boolean = false,
    windowSize: Int? = null,
    pageSize: Int? = null,
    unifyYAxisAcrossPages: Boolean = true,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    showLegend: Boolean = false,
    legendLabel: String = "",
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    barCornerRadiusFractions: BarCornerRadiusFractions? = null,
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

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<ChartMark>` | — | Chart data |
| `xLabel` | `String` | `"Time"` | X-axis label |
| `yLabel` | `String` | `"Value"` | Y-axis label |
| `title` | `String` | `"Bar Chart Example"` | Chart title |
| `barColor` | `Color` | `Color.Unspecified` | Bar fill color |
| `minY` | `Double?` | `null` | Y-axis minimum (auto if null) |
| `maxY` | `Double?` | `null` | Y-axis maximum (auto if null) |
| `barWidthRatio` | `Float` | `0.8f` | Bar width as fraction of slot (0–1) |
| `windowSize` | `Int?` | `null` | Visible bars when scrolling |
| `pageSize` | `Int?` | `null` | Bars per page when paging |
| `unifyYAxisAcrossPages` | `Boolean` | `true` | Same Y scale on all pages |
| `interactionType` | `InteractionType.Bar` | `BAR` | Tap target: bar or full-height zone |
| `referenceLines` | `List<ReferenceLineSpec>` | `[]` | Horizontal/vertical guide lines |
| `showLegend` | `Boolean` | `false` | Show legend |
| `showLabel` | `Boolean` | `false` | Show value label on top of each bar |
| `yAxisPosition` | `YAxisPosition` | `LEFT` | Y-axis side |
| `cornerRadiusFractions` | `BarCornerRadiusFractions` | default | Corner rounding per corner |
