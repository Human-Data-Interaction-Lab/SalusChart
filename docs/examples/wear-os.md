# Wear OS Examples

Charts from the `ui:wear-compose` module are optimized for round watch faces and small screen sizes.

## Setup

```kotlin
// build.gradle.kts (wear module)
dependencies {
    val salusVersion = "0.1.6"
    implementation("io.github.hdilys:saluschart-ui-wear-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
}
```

## Activity rings watch face

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.ui.wear.compose.WearProgressRing

@Composable
fun ActivityWatchFace() {
    WearProgressRing(
        modifier = Modifier.fillMaxSize(),
        data = listOf(
            ProgressChartMark(x = 0.0, current = 420.0, max = 600.0, label = "Move"),
            ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0,  label = "Exercise"),
            ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0,  label = "Stand"),
        ),
        colors = listOf(
            Color(0xFFE91E63),
            Color(0xFF4CAF50),
            Color(0xFF9C27B0),
        )
    )
}
```

## Heart rate on watch

```kotlin
import com.hdil.saluschart.ui.wear.compose.WearMiniRangeBarChart

@Composable
fun HeartRateComplication() {
    WearMiniRangeBarChart(
        modifier = Modifier.fillMaxSize(),
        data = rangeMarks,
        color = Color(0xFFE91E63),
    )
}
```

## Sleep overview on watch

```kotlin
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.ui.wear.compose.WearSleepColumn
import com.hdil.saluschart.ui.wear.compose.WearSleepSegment
import com.hdil.saluschart.ui.wear.compose.WearMinimalSleepChart

@Composable
fun SleepComplication() {
    WearMinimalSleepChart(
        modifier = Modifier.fillMaxSize(),
        columns = sleepColumns,
        showTrack = true,
    )
}

val sleepColumns = listOf(
    WearSleepColumn(
        listOf(
            WearSleepSegment(15f, Color(0xFF4338CA)),
            WearSleepSegment(20f, Color(0xFF60A5FA)),
        )
    ),
    WearSleepColumn(
        listOf(
            WearSleepSegment(10f, Color(0xFF6366F1)),
            WearSleepSegment(24f, Color(0xFF38BDF8)),
        )
    ),
)
```

## Tips for Wear OS layouts

- Use `Modifier.fillMaxSize()` to fill the circular viewport
- Prefer minimal chart variants (`WearMinimal*`) for complications
- Use `WearProgressRing` and `WearMinimalActivityRing` for circular progress
- Avoid charts with long axis labels — they will be clipped on small screens

The sample-wear module (`sample-wear/`) contains additional layout examples. Run it on a Wear OS emulator or device.
