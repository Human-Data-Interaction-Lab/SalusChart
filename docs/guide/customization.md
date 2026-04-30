# Customization

## Size and layout

All charts accept a `modifier` parameter. Use it to control size:

```kotlin
BarChart(
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(horizontal = 16.dp),
    data = data,
)
```

## Colors

Most charts accept color parameters:

```kotlin
BarChart(data = data, barColor = Color(0xFF7C4DFF))
LineChart(data = data, lineColor = Color(0xFF26A69A))
PieChart(data = data, colors = listOf(Color(0xFF7C4DFF), Color(0xFFFF9800)))
```

## Theme

Wrap your composable tree with `LocalSalusChartColors` to apply a palette globally:

```kotlin
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme
import androidx.compose.runtime.CompositionLocalProvider

CompositionLocalProvider(
    LocalSalusChartColors provides SalusChartColorScheme(
        primary = Color(0xFF7C4DFF),
        secondary = Color(0xFF26A69A),
    )
) {
    MyHealthDashboard()
}
```

## Paging and scrolling

Charts that handle time-series data support three display modes:

| Mode | Parameter | Behavior |
|---|---|---|
| Static | (default) | All data visible at once |
| Scrolling | `windowSize = N` | N items visible, scroll horizontally |
| Paging | `pageSize = N` | N items per page, swipe to navigate |

`windowSize` and `pageSize` are mutually exclusive.

```kotlin
// Show 7 days in a scrollable window
BarChart(data = data, windowSize = 7)

// Page through 24-hour chunks
BarChart(data = data, pageSize = 24)
```

## Legends

```kotlin
BarChart(
    data = data,
    showLegend = true,
    legendPosition = LegendPosition.BOTTOM, // TOP, BOTTOM, LEFT, RIGHT
)
```

## Reference lines

```kotlin
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.LineStyle

LineChart(
    data = data,
    referenceLines = listOf(
        ReferenceLineSpec(
            type = ReferenceLineType.THRESHOLD,
            y = 8000.0,
            label = "Goal",
            style = LineStyle.DASHED,
            color = Color(0xFFFF5722),
            showLabel = true,
        )
    )
)
```

`ReferenceLineType` supports `AVERAGE`, `TREND`, `THRESHOLD`, and `ZONE`.

## Y-axis position

```kotlin
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition

BarChart(
    data = data,
    yAxisPosition = YAxisPosition.RIGHT, // default: LEFT
)
```

## Interaction types

For a complete guide to tap targets, tooltips, callbacks, paging, and coordinated dashboards, see [Interactions](./interactions).

```kotlin
import com.hdil.saluschart.core.chart.InteractionType

// Bar: tapping the bar itself
BarChart(data = data, interactionType = InteractionType.Bar.BAR)

// Bar: invisible full-height tap zone
BarChart(data = data, interactionType = InteractionType.Bar.TOUCH_AREA)

// Line: tap the point dot
LineChart(data = data, interactionType = InteractionType.Line.POINT)

// Line: vertical strip tap zones
LineChart(data = data, interactionType = InteractionType.Line.TOUCH_AREA)
```

## Bar corner radius

```kotlin
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions

BarChart(
    data = data,
    barCornerRadiusFractions = BarCornerRadiusFractions(
        topStart = 0.4f,
        topEnd = 0.4f,
        bottomStart = 0f,
        bottomEnd = 0f,
    )
)
```

## Tooltips

For chart-wide tooltip patterns, see [Interactions](./interactions).

```kotlin
ProgressChart(
    data = data,
    tooltipEnabled = true,
    tooltipFormatter = { mark -> "${mark.label}: ${mark.current.toInt()} / ${mark.max.toInt()} ${mark.unit}" },
)
```

For fully custom tooltip UI, use `tooltipContent`:

```kotlin
ProgressChart(
    data = data,
    tooltipContent = { mark, color, modifier ->
        Box(modifier = modifier.background(color)) {
            Text("${mark.label}: ${mark.current.toInt()}")
        }
    }
)
```

## Limits

Parameters not exposed by the public API (internal padding, fixed layout dimensions, etc.) require either opening an issue or forking the repo. The library does not support runtime injection of internal drawing parameters.

See [Known Limitations](./known-limitations) for current API boundaries.
