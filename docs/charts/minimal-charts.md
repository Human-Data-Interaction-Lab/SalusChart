# Minimal Charts

Compact, card-sized chart composables with no title, axes, or legends. Designed for dashboards and list items where space is limited.

All minimal charts are in `com.hdil.saluschart.ui.compose.charts`.

---

## MinimalBarChart {#minimalbar}

![MinimalBarChart](/charts/minimal-bar.png)

```kotlin
import com.hdil.saluschart.ui.compose.charts.MinimalBarChart

MinimalBarChart(
    modifier = Modifier.size(120.dp, 60.dp),
    data = listOf(
        ChartMark(x = 0.0, y = 4200.0),
        ChartMark(x = 1.0, y = 7800.0),
        ChartMark(x = 2.0, y = 6100.0),
    ),
    color = Color(0xFF7C4DFF),
)
```

---

## MinimalLineChart {#minimalline}

![MinimalLineChart](/charts/minimal-line.png)

```kotlin
import com.hdil.saluschart.ui.compose.charts.MinimalLineChart

MinimalLineChart(
    modifier = Modifier.size(120.dp, 60.dp),
    data = listOf(
        ChartMark(x = 0.0, y = 68.5),
        ChartMark(x = 1.0, y = 68.2),
        ChartMark(x = 2.0, y = 67.9),
    ),
    color = Color(0xFF26A69A),
)
```

---

## MinimalRangeBarChart {#minimalrangebar}

```kotlin
import com.hdil.saluschart.ui.compose.charts.MinimalRangeBarChart

MinimalRangeBarChart(
    modifier = Modifier.size(120.dp, 80.dp),
    data = listOf(
        ChartMark(x = 0.0, y = 75.0),
        ChartMark(x = 1.0, y = 82.0),
        ChartMark(x = 2.0, y = 68.0),
    ),
    color = Color(0xFFFF9800),
)
```

---

## MinimalProgressBar {#minimalprogress}

Single horizontal progress bar. `progress` is a value between `0f` and `1f`.

![MinimalProgressBar](/charts/minimal-progress-bar.png)

```kotlin
import com.hdil.saluschart.ui.compose.charts.MinimalProgressBar

MinimalProgressBar(
    modifier = Modifier.fillMaxWidth().height(12.dp),
    progress = 0.7f,        // 70 %
    fillColor = Color(0xFFE91E63),
)
```

---

## MinimalGaugeChart {#minimalgauge}

![MinimalGaugeChart](/charts/minimal-gauge.png)

Displays a range bar with a bubble marker showing the current position.
`data` is a `RangeChartMark` where `minPoint.y` is the range start and `maxPoint.y` is the range end.

```kotlin
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.ui.compose.charts.MinimalGaugeChart

MinimalGaugeChart(
    modifier = Modifier.fillMaxWidth().height(60.dp),
    data = RangeChartMark(
        x = 0.0,
        minPoint = ChartMark(x = 0.0, y = 60.0),
        maxPoint = ChartMark(x = 0.0, y = 100.0),
    ),
    containerMin = 40.0,
    containerMax = 200.0,
    rangeColor = Color(0xFF4CAF50),
    label = "Normal",
)
```

---

## MinimalMultiSegmentGauge {#minimalmultisegment}

```kotlin
import com.hdil.saluschart.ui.compose.charts.MinimalMultiSegmentGauge
import com.hdil.saluschart.ui.compose.charts.MinimalGaugeSegment

MinimalMultiSegmentGauge(
    modifier = Modifier.fillMaxWidth().height(60.dp),
    segments = listOf(
        MinimalGaugeSegment(fraction = 0.33f, color = Color(0xFF4CAF50)),
        MinimalGaugeSegment(fraction = 0.34f, color = Color(0xFFFFEB3B)),
        MinimalGaugeSegment(fraction = 0.33f, color = Color(0xFFF44336)),
    ),
    markerRatio = 0.62f,    // 0 = left edge, 1 = right edge
    label = "Good",
)
```

---

## MinimalSleepChart {#minimalsleep}

Each column is a vertical stack of colored sleep stage capsules.

```kotlin
import com.hdil.saluschart.ui.compose.charts.MinimalSleepChart
import com.hdil.saluschart.ui.compose.charts.SleepColumn
import com.hdil.saluschart.ui.compose.charts.SleepSegment

MinimalSleepChart(
    modifier = Modifier.fillMaxWidth().height(60.dp),
    columns = listOf(
        SleepColumn(segments = listOf(
            SleepSegment(value = 60f,  color = Color(0xFF5C6BC0)),  // Deep
            SleepSegment(value = 120f, color = Color(0xFF7986CB)),  // Light
            SleepSegment(value = 30f,  color = Color(0xFF9575CD)),  // REM
        )),
        SleepColumn(segments = listOf(
            SleepSegment(value = 90f,  color = Color(0xFF5C6BC0)),
            SleepSegment(value = 150f, color = Color(0xFF7986CB)),
            SleepSegment(value = 45f,  color = Color(0xFF9575CD)),
        )),
    ),
    segmentGapRatio = 0.02f,
)
```

---

## MinimalSleepStageChart {#minimalsleepstage}

```kotlin
import com.hdil.saluschart.ui.compose.charts.MinimalSleepStageChart

MinimalSleepStageChart(
    modifier = Modifier.fillMaxWidth().height(80.dp),
    sleepSession = session,
)
```

---

## MinimalHorizontalStackedBar {#minimalhorizontalstacked}

```kotlin
import com.hdil.saluschart.ui.compose.charts.MinimalHorizontalStackedBar
import com.hdil.saluschart.ui.compose.charts.StackedBarSegment

MinimalHorizontalStackedBar(
    modifier = Modifier.fillMaxWidth().height(16.dp),
    segments = listOf(
        StackedBarSegment(value = 250f, color = Color(0xFF7C4DFF)),
        StackedBarSegment(value = 80f,  color = Color(0xFF26A69A)),
        StackedBarSegment(value = 60f,  color = Color(0xFFFF9800)),
    )
)
```

---

## MinimalLadderChart {#minimalladder}

A stepped band chart useful for visualizing fitness levels or graded progress.

![MinimalLadderChart](/charts/minimal-ladder-chart.png)

```kotlin
import com.hdil.saluschart.ui.compose.charts.MinimalLadderChart

MinimalLadderChart(
    modifier = Modifier.size(120.dp, 60.dp),
    bandCount = 5,
    selectedBandIndex = 2,      // 0-based, from the left
    markerRatio = 0.5f,         // 0..1 position within the selected band
    trackColor = Color(0xFFEEEEEE),
    selectedColor = Color(0xFF7C4DFF),
    markerColor = Color(0xFF7C4DFF),
)
```

---

## MiniActivityRings {#miniactivityrings}

Apple Watch-style concentric activity rings. Not strictly a "minimal" variant but similarly compact.

![MiniActivityRings](/charts/mini-activity-rings.png)

```kotlin
import com.hdil.saluschart.ui.compose.charts.MiniActivityRings

MiniActivityRings(
    modifier = Modifier.size(80.dp),
    rings = listOf(
        ProgressChartMark(x = 0.0, current = 420.0, max = 600.0),
        ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0),
        ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0),
    ),
    colors = listOf(
        Color(0xFFE91E63),
        Color(0xFF4CAF50),
        Color(0xFF9C27B0),
    ),
    strokeWidth = 8f,
)
```
