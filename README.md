# SalusChart

A modular Jetpack Compose charting library (health-focused) for Android.

**Latest version:** `0.1.2`

## Modules (published)

These artifacts are published to **Maven Central**:

- `io.github.hdilys:saluschart-ui-compose:0.1.2`
- `io.github.hdilys:saluschart-ui-theme:0.1.2`
- `io.github.hdilys:saluschart-core-chart:0.1.2`
- `io.github.hdilys:saluschart-core-transform:0.1.2`
- `io.github.hdilys:saluschart-core-util:0.1.2`
- `io.github.hdilys:saluschart-data-model:0.1.2`

## Installation

Make sure you have Maven Central:

```kotlin
repositories {
    mavenCentral()
}
```

Add dependencies:

```kotlin
dependencies {
    implementation("io.github.hdilys:saluschart-ui-compose:0.1.2") ✅ (recommended for most users)
    implementation("io.github.hdilys:saluschart-ui-theme:0.1.2") ✅ (recommended for most users)
    implementation("io.github.hdilys:saluschart-core-chart:0.1.2") ✅ (recommended for most users)
    implementation("io.github.hdilys:saluschart-core-transform:0.1.2")
    implementation("io.github.hdilys:saluschart-core-util:0.1.2")
    implementation("io.github.hdilys:saluschart-data-model:0.1.2")
    
}
```

## Basic Data Model

SalusChart charts take simple “mark” objects:
- ChartMark(x, y, label?): a single point used by many charts.
- RangeChartMark: min/max range at each x-position (used by RangeBarChart).
- ProgressChartMark: progress item with current/max and label (used by ProgressChart).

In most charts:
- x is your category/time index (Double)
- y is the value (Double)
- label is what shows on the x-axis and/or legend

## PieChart

PieChart consumes List<ChartMark> and uses each mark’s y as slice magnitude.

Example:
```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.compose.charts.PieChart

@Composable
fun PieChartExample() {
    val data = listOf(
        ChartMark(x = 0.0, y = 30.0, label = "Mon"),
        ChartMark(x = 1.0, y = 20.0, label = "Tue"),
        ChartMark(x = 2.0, y = 25.0, label = "Wed"),
        ChartMark(x = 3.0, y = 15.0, label = "Thu"),
    )

    PieChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = data,
        title = "Weekly activity",
        isDonut = true,               // donut if true, full pie if false
        colors = listOf(
            Color(0xFF7C4DFF),
            Color(0xFF26A69A),
            Color(0xFFFF9800),
            Color(0xFFFFEB3B),
        ),
        showLegend = true,
        showLabel = true,
        interactionsEnabled = true
    )
}
```

## ProgressChart

ProgressChart consumes List<ProgressChartMark>.

- Donut mode (isDonut=true): concentric rings (one ring per item)
- Bar mode (isDonut=false): stacked horizontal bars

### Progress bars - stacked vertically

Example:
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.ui.compose.charts.ProgressChart
import com.hdil.saluschart.ui.compose.charts.ProgressChartMark

@Composable
fun ProgressBarChartExample() {
    val data = listOf(
        ProgressChartMark(x = 0.0, current = 420.0, max = 600.0, label = "Move", unit = "kcal"),
        ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0,  label = "Exercise", unit = "min"),
        ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0,  label = "Stand", unit = "hr"),
    )

    ProgressChart(
        data = data,
        title = "Daily activity progress",
        isDonut = false,
        isPercentage = true,
        colors = listOf(
            Color(0xFF00C7BE),
            Color(0xFFFF6B35),
            Color(0xFF3A86FF),
        ),
        showLegend = true,
        interactionsEnabled = true
    )
}
```

### Donut/ring progress

Example:
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.ui.compose.charts.ProgressChart
import com.hdil.saluschart.ui.compose.charts.ProgressChartMark

@Composable
fun ProgressRingChartExample() {
    val data = listOf(
        ProgressChartMark(x = 0.0, current = 420.0, max = 600.0, label = "Move", unit = "kcal"),
        ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0,  label = "Exercise", unit = "min"),
        ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0,  label = "Stand", unit = "hr"),
    )

    ProgressChart(
        data = data,
        title = "Daily activity progress",
        isDonut = true,
        isPercentage = false,
        colors = listOf(
            Color(0xFFE91E63),
            Color(0xFF4CAF50),
            Color(0xFF9C27B0),
        ),
        strokeWidth = 20.dp,
        showLegend = true,
        showLabels = false,
        showValues = false,
        tooltipEnabled = true,
        interactionsEnabled = true
    )
}
```

Notes:
- If you want only one ring, pass a list with one ProgressChartMark.
- You can customize tooltip text using tooltipFormatter.

## RangeBarChart (min/max range per x)

RangeBarChart consumes List<RangeChartMark> (each mark contains minPoint and maxPoint). 

### Simple (static)

Example:
```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.compose.charts.InteractionType
import com.hdil.saluschart.ui.compose.charts.RangeBarChart
import com.hdil.saluschart.ui.compose.charts.RangeChartMark

@Composable
fun RangeBarChartExample() {
    val rangeMarks = listOf(
        RangeChartMark(
            x = 0.0,
            minPoint = ChartMark(x = 0.0, y = 55.0, label = "Day 1"),
            maxPoint = ChartMark(x = 0.0, y = 150.0, label = "Day 1"),
            label = "Day 1"
        ),
        RangeChartMark(
            x = 1.0,
            minPoint = ChartMark(x = 1.0, y = 54.0, label = "Day 2"),
            maxPoint = ChartMark(x = 1.0, y = 160.0, label = "Day 2"),
            label = "Day 2"
        ),
        RangeChartMark(
            x = 2.0,
            minPoint = ChartMark(x = 2.0, y = 65.0, label = "Day 3"),
            maxPoint = ChartMark(x = 2.0, y = 145.0, label = "Day 3"),
            label = "Day 3"
        )
    )

    RangeBarChart(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = rangeMarks,
        title = "Daily heart rate range",
        yLabel = "Heart rate (bpm)",
        xLabel = "Day",
        barWidthRatio = 0.8f,
        barColor = Color(0xFFFF9800),
        interactionType = InteractionType.RangeBar.TOUCH_AREA,
        unit = "bpm"
    )
}
```

### Paging vs scrolling

RangeBarChart supports 3 display modes:
- Static (default): show all items
- Scrolling: set windowSize = N (N visible items in viewport)
- Paging: set pageSize = N (N items per page)

windowSize and pageSize are mutually exclusive.

Example (paging):
```kotlin
RangeBarChart(
    modifier = Modifier.fillMaxWidth().height(500.dp),
    data = rangeMarks,
    title = "Hourly heart rate range",
    xLabel = "Hour",
    yLabel = "bpm",
    barColor = Color(0xFFE91E63),
    interactionType = InteractionType.RangeBar.TOUCH_AREA,
    unit = "bpm",
    pageSize = 24
)
```

Example (scrolling):
```kotlin
RangeBarChart(
    modifier = Modifier.fillMaxWidth().height(500.dp),
    data = rangeMarks,
    title = "Daily blood glucose range",
    xLabel = "Day",
    yLabel = "mg/dL",
    barColor = Color(0xFF4CAF50),
    interactionType = InteractionType.RangeBar.TOUCH_AREA,
    unit = "mg/dL",
    windowSize = 7
)
```

## Customization Notes

If you include SalusChart via Maven dependency:

✅ You can customize what the API exposes:
- chart size via modifier
- colors via parameters (colors, barColor, etc.)
- labels/legend/tooltips via parameters (showLegend, showLabel, tooltipFormatter, etc.)
- some charts expose padding parameters (e.g. RangeBarChart(contentPadding = ...))

🔧 If you need to change internal layout decisions that are not parameterized (e.g., a hardcoded padding inside drawing logic), you currently need to:
- request a new customization parameter / open an issue, or
- fork the repository and modify the source.

## Demo / Sample

This repository also contains a demo app (:app) and a sample module (:sample) for showcasing usage.
They are not published to Maven Central.

## Development

- Build: ./gradlew build
- Publish locally (for testing): ./gradlew publishToMavenLocal

## License

Apache License 2.0.
