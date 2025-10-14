# Multiple Sessions Per X-Value in Range Bar Charts

## Overview

The Range Bar Chart now supports **multiple sessions (multiple range bars) per single x-value**. This is perfect for visualizing scenarios like:
- Multiple sleep sessions in a single day
- Multiple workout sessions on the same date  
- Multiple time ranges for any activity within the same time period

## What Changed?

### Previous Limitation
Previously, when you passed `List<ChartMark>` to `RangeBarChart`, it would use `.toRangeChartMarks()` which:
1. Groups all ChartMarks by x-value
2. Extracts the min and max from each group
3. Creates **ONE** `RangeChartMark` per x-value

This made it impossible to have multiple range bars at the same x-value.

### New Implementation
The new implementation:
1. **Directly accepts `List<RangeChartMark>`** - You can now pass multiple `RangeChartMark` objects with the same `x` value
2. **Groups bars by x-value for positioning** - All bars with the same `x` value are drawn at the same x-position
3. **Shows unique x-axis labels** - Only one label per unique x-value (not one per bar)

## How to Use

### Creating Data with Multiple Sessions Per Day

```kotlin
// Example: Two sleep sessions on the same day (day 1)
val sleepData = listOf(
    // Day 1, Session 1: 11 PM - 2 AM (3 hours)
    RangeChartMark(
        x = 1.0,  // Day 1
        minPoint = ChartMark(x = 1.0, y = 23.0),  // 11 PM
        maxPoint = ChartMark(x = 1.0, y = 26.0),  // 2 AM (26 = 24 + 2)
        label = "Aug 25"
    ),
    // Day 1, Session 2: 10 AM - 3 PM (5 hours) - afternoon nap
    RangeChartMark(
        x = 1.0,  // Same day
        minPoint = ChartMark(x = 1.0, y = 10.0),  // 10 AM
        maxPoint = ChartMark(x = 1.0, y = 15.0),  // 3 PM
        label = "Aug 25"
    ),
    
    // Day 2, Single session: 10 PM - 6 AM (8 hours)
    RangeChartMark(
        x = 2.0,  // Day 2
        minPoint = ChartMark(x = 2.0, y = 22.0),  // 10 PM
        maxPoint = ChartMark(x = 2.0, y = 30.0),  // 6 AM (30 = 24 + 6)
        label = "Aug 26"
    ),
    
    // Day 3, Three short sessions
    RangeChartMark(
        x = 3.0,
        minPoint = ChartMark(x = 3.0, y = 23.0),
        maxPoint = ChartMark(x = 3.0, y = 25.0),
        label = "Aug 27"
    ),
    RangeChartMark(
        x = 3.0,  // Same day
        minPoint = ChartMark(x = 3.0, y = 10.0),
        maxPoint = ChartMark(x = 3.0, y = 12.0),
        label = "Aug 27"
    ),
    RangeChartMark(
        x = 3.0,  // Same day
        minPoint = ChartMark(x = 3.0, y = 14.0),
        maxPoint = ChartMark(x = 3.0, y = 16.0),
        label = "Aug 27"
    ),
)

// Use it in the chart
RangeBarChart(
    data = sleepData,  // Pass RangeChartMarks directly
    title = "Sleep Sessions",
    barColor = Color.Blue,
    barWidthRatio = 0.6f
)
```

### Key Points

1. **Same `x` value** - All `RangeChartMark` objects that should appear in the same x-position must have the same `x` value
2. **Direct RangeChartMark input** - Don't convert from `ChartMark` using `.toRangeChartMarks()` if you want multiple sessions
3. **Automatic grouping** - The chart automatically detects which bars belong to the same x-group and positions them together
4. **Unique labels** - X-axis will show only one label per unique x-value (e.g., "Aug 25" appears once even if there are 2 sessions)

## Technical Details

### Internal Changes

1. **X-Group Index Mapping** (`RangeBarChart.kt`)
   - Computes `xGroupIndices: List<Int>` that maps each bar to its x-group
   - Example: `[0, 0, 1, 2, 2, 2]` means bars 0&1 are at x-position 0, bar 2 is at position 1, bars 3-5 are at position 2

2. **Unique Label Extraction** (`RangeBarChart.kt`)
   - Extracts `uniqueLabels` by grouping by x-value and taking the first label from each group
   - Passed to `drawBarXAxisLabels` for rendering

3. **Grouped Bar Positioning** (`BarChartDraw.kt`)
   - New parameter: `xGroupIndices: List<Int>?` in `BarMarker` function
   - When provided, calculates spacing based on number of unique x-groups, not total bars
   - All bars with the same x-group index are drawn at the same x-position

### Backward Compatibility

The changes are **100% backward compatible**:
- If you don't pass `xGroupIndices` (it defaults to `null`), the original behavior is used
- Existing charts continue to work exactly as before
- Only `RangeBarChart` automatically uses the new grouped positioning when multiple bars share an x-value

## Example: Sleep Chart Like the Screenshot

To create a sleep chart like the one in your screenshot:

```kotlin
@Composable
fun SleepChart(sleepSessions: List<SleepSession>) {
    // Convert sleep sessions to RangeChartMarks
    val rangeData = sleepSessions.map { session ->
        RangeChartMark(
            x = session.date.toEpochDays().toDouble(),  // Day number
            minPoint = ChartMark(
                x = session.date.toEpochDays().toDouble(),
                y = session.startTime.toDecimalHours()  // e.g., 23.5 for 11:30 PM
            ),
            maxPoint = ChartMark(
                x = session.date.toEpochDays().toDouble(),
                y = session.endTime.toDecimalHours()   // e.g., 7.25 for 7:15 AM
            ),
            label = session.date.format("MMM dd")
        )
    }
    
    RangeBarChart(
        data = rangeData,
        title = "Sleep Schedule",
        barColor = Color(0xFF4A90E2),  // Blue color
        barWidthRatio = 0.6f,
        yAxisPosition = YAxisPosition.RIGHT,
        pageSize = 7,  // Show 7 days per page
        unit = "h"
    )
}
```

## Notes

- **Overlapping bars**: If multiple sessions overlap in time, they will be drawn on top of each other at the same x-position. You may want to adjust bar width or add transparency if this is an issue.
- **Bar width**: When multiple bars share the same x-position, they all have the same width calculated based on the number of unique x-groups (not the total number of bars).
- **Click handling**: Each bar can still be clicked individually, even if multiple bars are at the same x-position.

## Migration from Old Approach

If you were previously using `List<ChartMark>.toRangeChartMarks()`:

### Before (Single session per x-value)
```kotlin
val chartMarks = listOf(
    ChartMark(x = 1.0, y = 23.0),
    ChartMark(x = 1.0, y = 26.0),
    ChartMark(x = 2.0, y = 22.0),
    ChartMark(x = 2.0, y = 30.0),
)

RangeBarChart(
    data = chartMarks,  // Will be converted to 2 RangeChartMarks
    // ...
)
```

### After (Multiple sessions possible)
```kotlin
val rangeMarks = listOf(
    // Day 1, Session 1
    RangeChartMark(
        x = 1.0,
        minPoint = ChartMark(x = 1.0, y = 23.0),
        maxPoint = ChartMark(x = 1.0, y = 26.0),
        label = "Day 1"
    ),
    // Day 1, Session 2 (NEW!)
    RangeChartMark(
        x = 1.0,  // Same x-value as Session 1
        minPoint = ChartMark(x = 1.0, y = 10.0),
        maxPoint = ChartMark(x = 1.0, y = 15.0),
        label = "Day 1"
    ),
    // Day 2, Session 1
    RangeChartMark(
        x = 2.0,
        minPoint = ChartMark(x = 2.0, y = 22.0),
        maxPoint = ChartMark(x = 2.0, y = 30.0),
        label = "Day 2"
    ),
)

RangeBarChart(
    data = rangeMarks,  // 3 RangeChartMarks, 2 at x=1.0, 1 at x=2.0
    // ...
)
```

