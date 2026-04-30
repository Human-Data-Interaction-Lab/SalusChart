# CalendarChart

Single-month calendar heatmap. Each cell is one day. Cell appearance is driven by `CalendarEntry.value` and the configured marker type.

**Import:** `com.hdil.saluschart.ui.compose.charts.CalendarChart`

## Data

```kotlin
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import java.time.LocalDate
import java.time.YearMonth

val yearMonth = YearMonth.now()

val entries = listOf(
    CalendarEntry(date = LocalDate.of(2024, 1, 1),  value = 8200f),
    CalendarEntry(date = LocalDate.of(2024, 1, 2),  value = 5400f),
    CalendarEntry(date = LocalDate.of(2024, 1, 15), value = 12000f),
)
```

## Basic example

![CalendarChart bubble](/charts/calendar-chart-bubble.png)

```kotlin
import com.hdil.saluschart.ui.compose.charts.CalendarChart
import com.hdil.saluschart.ui.compose.charts.CellMarkerType

CalendarChart(
    modifier = Modifier.fillMaxWidth(),
    entries = entries,
    yearMonth = yearMonth,
    markerType = CellMarkerType.BUBBLE,
)
```

## Marker types

```kotlin
import com.hdil.saluschart.ui.compose.charts.CellMarkerType
import com.hdil.saluschart.ui.compose.charts.BubbleType

// Bubble scaled by value
CalendarChart(
    entries = entries,
    yearMonth = yearMonth,
    markerType = CellMarkerType.BUBBLE,
    bubbleType = BubbleType.CIRCLE,  // or RECTANGLE
)

// Mini activity rings per cell
CalendarChart(
    entries = entriesWithRings,  // CalendarEntry.rings must be non-null
    yearMonth = yearMonth,
    markerType = CellMarkerType.MINI_RINGS,
)
```

## CalendarChart with activity rings per cell

```kotlin
val entriesWithRings = listOf(
    CalendarEntry(
        date = LocalDate.of(2024, 1, 1),
        value = 0.7f,
        rings = listOf(
            ProgressChartMark(x = 0.0, current = 420.0, max = 600.0, label = "Move"),
            ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0,  label = "Exercise"),
            ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0,  label = "Stand"),
        )
    ),
)
```

## PagedCalendarChart

Multi-month swipeable calendar.

**Import:** `com.hdil.saluschart.ui.compose.charts.PagedCalendarChart`

```kotlin
import com.hdil.saluschart.ui.compose.charts.PagedCalendarChart

PagedCalendarChart(
    modifier = Modifier.fillMaxWidth(),
    entries = allEntries,           // entries across multiple months
    initialYearMonth = YearMonth.now(),
    markerType = CellMarkerType.BUBBLE,
)
```

## Parameters (CalendarChart)

| Parameter | Type | Default | Description |
|---|---|---|---|
| `entries` | `List<CalendarEntry>` | — | Day entries |
| `yearMonth` | `YearMonth` | — | Month to display |
| `markerType` | `CellMarkerType` | `BUBBLE` | `BUBBLE` or `MINI_RINGS` |
| `bubbleType` | `BubbleType` | `CIRCLE` | `CIRCLE` or `RECTANGLE` |
| `onDayClick` | `((CalendarEntry) -> Unit)?` | `null` | Tap callback |
