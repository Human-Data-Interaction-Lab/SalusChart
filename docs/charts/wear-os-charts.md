# Wear OS Charts

The `ui:wear-compose` module provides chart variants optimized for round Wear OS watch faces.

## Installation

```kotlin
dependencies {
    val salusVersion = "0.1.6"
    implementation("io.github.hdilys:saluschart-ui-wear-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
}
```

## Available charts

| Composable | Description |
|---|---|
| `WearPieChart` | Pie/donut for circular displays |
| `WearProgressRing` | Single progress ring for watch faces |
| `WearMinimalActivityRing` | Compact activity ring |
| `WearMinimalLineChart` | Compact line chart |
| `WearMinimalBarChart` | Compact bar chart |
| `WearMiniRangeBarChart` | Compact range bar |
| `WearMinimalRangeBarChart` | Compact range bar (alternate) |
| `WearMinimalGaugeChart` | Compact gauge |
| `WearMinimalMultiSegmentGaugeChart` | Compact segmented gauge |
| `WearMinimalSleepChart` | Compact sleep timeline |
| `WearMinimalStackedBarChart` | Compact stacked bar |
| `WearMinimalLadderChart` | Compact ladder chart |
| `WearSleepStageChart` | Full sleep stage chart |

All Wear variants are in package `com.hdil.saluschart.ui.wear.compose`.

## Example: WearProgressRing

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.ui.wear.compose.WearProgressRing

WearProgressRing(
    modifier = Modifier.fillMaxSize(),
    data = listOf(
        ProgressChartMark(x = 0.0, current = 420.0, max = 600.0),
        ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0),
        ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0),
    ),
    colors = listOf(
        Color(0xFFE91E63),
        Color(0xFF4CAF50),
        Color(0xFF9C27B0),
    )
)
```

## Example: WearSleepStageChart

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.hdil.saluschart.ui.wear.compose.WearSleepStageChart

WearSleepStageChart(
    modifier = Modifier.fillMaxSize(),
    sleepSession = session,
)
```

## WearChartDefaults

`WearChartDefaults` provides reusable sizing and color defaults used by the Wear chart composables:

```kotlin
import com.hdil.saluschart.ui.wear.compose.WearChartDefaults

val padding = WearChartDefaults.ChartPadding
val minimalHeight = WearChartDefaults.MinimalChartHeight
val compactHeight = WearChartDefaults.CompactChartHeight
val summaryHeight = WearChartDefaults.SummaryChartHeight
val ringStroke = WearChartDefaults.RingStroke
val ringGap = WearChartDefaults.RingGap
val cardCornerRadius = WearChartDefaults.CardCornerRadius
val cardBackground = WearChartDefaults.CardBackground
```

`WearChartDefaults.palette()` is a composable function that returns the active SalusChart theme palette. `WearChartDefaults.trackColor(color)` returns the same color with the alpha used for Wear chart tracks.
