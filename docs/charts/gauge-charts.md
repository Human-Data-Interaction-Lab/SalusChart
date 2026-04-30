# Gauge Charts

## MultiSegmentGaugeChart

A card-style gauge bar divided into colored zones, with a hexagonal marker at the current value position. Suitable for fitness level, VO2 max, stress, and similar single-value indicators.

**Import:** `com.hdil.saluschart.ui.compose.charts.MultiSegmentGaugeChart`

### Data

```kotlin
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.ui.compose.charts.GaugeSegment

val segments = listOf(
    GaugeSegment(start = 0f,  end = 20f,  color = Color(0xFF4CAF50)),  // Poor
    GaugeSegment(start = 20f, end = 40f,  color = Color(0xFF8BC34A)),  // Fair
    GaugeSegment(start = 40f, end = 60f,  color = Color(0xFFFFEB3B)),  // Good
    GaugeSegment(start = 60f, end = 80f,  color = Color(0xFFFF9800)),  // Very good
    GaugeSegment(start = 80f, end = 100f, color = Color(0xFFF44336)),  // Excellent
)

val tickValues = listOf(0f, 20f, 40f, 60f, 80f, 100f)
```

### Example

![MultiSegmentGaugeChart](/charts/multi-segment-gauge.png)

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.ui.compose.charts.MultiSegmentGaugeChart

MultiSegmentGaugeChart(
    modifier = Modifier.fillMaxWidth().padding(16.dp),
    title = "Fitness Level",
    value = 62f,
    minValue = 0f,
    maxValue = 100f,
    segments = segments,
    tickValues = tickValues,
    leftHint = "Poor",
    rightHint = "Excellent",
)
```

### No data state

```kotlin
MultiSegmentGaugeChart(
    title = "Fitness Level",
    value = null,              // triggers no-data state
    noDataMessage = "No data available",
    segments = segments,
    tickValues = tickValues,
    minValue = 0f,
    maxValue = 100f,
)
```

### Key parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `title` | `String` | — | Card title |
| `value` | `Float?` | — | Current value; null = no-data state |
| `minValue` | `Float` | — | Scale minimum |
| `maxValue` | `Float` | — | Scale maximum |
| `segments` | `List<GaugeSegment>` | — | Colored zones |
| `tickValues` | `List<Float>` | — | Tick label positions |
| `leftHint` | `String?` | `null` | Label below left end |
| `rightHint` | `String?` | `null` | Label below right end |
| `noDataMessage` | `String?` | `null` | Message when value is null |
| `barHeight` | `Dp` | `28.dp` | Gauge bar height |
| `cornerRadius` | `Dp` | `26.dp` | Card corner radius |

---

## RangeGaugeChart

Renders a card-style linear gauge with one highlighted range and a dotted marker for the most recent value. Useful for values such as heart rate ranges.

**Import:** `com.hdil.saluschart.ui.compose.charts.RangeGaugeChart`

### Example

![RangeGaugeChart](/charts/range-gauge-chart.png)

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.ui.compose.charts.RangeGaugeChart

RangeGaugeChart(
    modifier = Modifier.fillMaxWidth().padding(16.dp),
    minValue = 40f,
    maxValue = 200f,
    rangeStart = 60f,
    rangeEnd = 100f,
    recentValue = 78f,
    unit = "bpm",
    recentLabel = "Latest recording 3:40 PM",
    rangeColor = Color(0xFFFF8A3D),
)
```

### Key parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `minValue` | `Float` | — | Scale minimum |
| `maxValue` | `Float` | — | Scale maximum |
| `rangeStart` | `Float` | — | Start of the highlighted range |
| `rangeEnd` | `Float` | — | End of the highlighted range |
| `recentValue` | `Float` | — | Most recent reading marked on the gauge |
| `unit` | `String` | `"bpm"` | Unit appended to the range title |
| `recentLabel` | `String` | `"최근기록 오후 3:40"` | Subtitle below the gauge |
| `trackColor` | `Color` | `Color(0xFFF2F2F2)` | Background track color |
| `rangeColor` | `Color` | `Color(0xFFFF8A3D)` | Highlighted range color |
| `barHeight` | `Dp` | `28.dp` | Gauge bar height |
| `markerDotCount` | `Int` | `7` | Number of dots in the recent-value marker |
