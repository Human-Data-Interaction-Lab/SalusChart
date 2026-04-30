# RangeBarChart

Vertical bars showing a min–max range at each x position. Commonly used for heart rate ranges, blood glucose, blood pressure, and temperature data.

**Import:** `com.hdil.saluschart.ui.compose.charts.RangeBarChart`

## Data

```kotlin
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark

val data = listOf(
    RangeChartMark(
        x = 0.0,
        minPoint = ChartMark(x = 0.0, y = 55.0),
        maxPoint = ChartMark(x = 0.0, y = 150.0),
        label = "Mon"
    ),
    RangeChartMark(
        x = 1.0,
        minPoint = ChartMark(x = 1.0, y = 60.0),
        maxPoint = ChartMark(x = 1.0, y = 140.0),
        label = "Tue"
    ),
)
```

## Basic example

![RangeBarChart basic example](/charts/range-bar-chart-basic.png)

```kotlin
import com.hdil.saluschart.ui.compose.charts.RangeBarChart

RangeBarChart(
    modifier = Modifier.fillMaxWidth().height(400.dp),
    data = data,
    title = "Daily heart rate range",
    xLabel = "Day",
    yLabel = "bpm",
    barColor = Color(0xFFFF9800),
    barWidthRatio = 0.6f,
)
```

## Signature

```kotlin
@Composable
fun RangeBarChart(
    modifier: Modifier = Modifier,
    data: List<RangeChartMark>,
    xLabel: String = "",
    yLabel: String = "",
    title: String = "Range Bar Chart",
    barColor: Color = Color.Unspecified,
    barWidthRatio: Float = 0.6f,
    windowSize: Int? = null,
    pageSize: Int? = null,
    referenceLines: List<ReferenceLineSpec> = emptyList(),
    interactionType: InteractionType.RangeBar = InteractionType.RangeBar.BAR,
    pointValues: List<List<Double>>? = null,
    showLegend: Boolean = false,
    unit: String = "",
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 0.dp),
)
```

## Display modes

```kotlin
// Paging — swipe through 24-hour pages
RangeBarChart(data = data, pageSize = 24)

// Scrolling — 7 visible bars, scroll for more
RangeBarChart(data = data, windowSize = 7)
```

## With overlay dots

`pointValues` overlays individual measurements on top of range bars (e.g., actual readings within the heart rate range):

```kotlin
RangeBarChart(
    data = rangeData,
    pointValues = listOf(
        listOf(75.0),   // overlay values for bar 0
        listOf(80.0),   // overlay values for bar 1
    )
)
```

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `data` | `List<RangeChartMark>` | — | Range data |
| `barColor` | `Color` | `Color.Unspecified` | Bar fill color |
| `barWidthRatio` | `Float` | `0.6f` | Bar width as fraction of slot |
| `windowSize` | `Int?` | `null` | Scroll window size |
| `pageSize` | `Int?` | `null` | Items per page |
| `interactionType` | `InteractionType.RangeBar` | `BAR` | Tap target mode |
| `pointValues` | `List<List<Double>>?` | `null` | Per-bar overlay dots |
| `unit` | `String` | `""` | Unit shown in tooltip |
| `referenceLines` | `List<ReferenceLineSpec>` | `[]` | Guide lines |
| `yAxisPosition` | `YAxisPosition` | `LEFT` | Y-axis side |
| `contentPadding` | `PaddingValues` | `PaddingValues(0)` | Chart padding |
