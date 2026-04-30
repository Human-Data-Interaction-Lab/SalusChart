# SleepStageChart

Apple Health-style sleep stage visualization. Renders a `SleepSession` as colored horizontal capsules on a grid of four rows (Deep, Core/Light, REM, Awake). Consecutive stages of the same type are merged before rendering.

**Import:** `com.hdil.saluschart.ui.compose.charts.SleepStageChart`

## Signature

```kotlin
@Composable
fun SleepStageChart(
    modifier: Modifier = Modifier,
    sleepSession: SleepSession,
    title: String = "Sleep Stage Analysis",
    showLabels: Boolean = true,
    showXAxis: Boolean = true,
    onStageClick: ((Int, String) -> Unit)? = null,
    barHeightRatio: Float = 0.5f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    yAxisFixedWidth: Dp = 60.dp,
)
```

## Data

```kotlin
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import com.hdil.saluschart.data.model.model.SleepStageType
import java.time.Instant

val session = SleepSession(
    startTime = Instant.parse("2024-01-15T23:00:00Z"),
    endTime   = Instant.parse("2024-01-16T07:00:00Z"),
    stages = listOf(
        SleepStage(
            startTime = Instant.parse("2024-01-15T23:00:00Z"),
            endTime   = Instant.parse("2024-01-15T23:30:00Z"),
            stage = SleepStageType.AWAKE
        ),
        SleepStage(
            startTime = Instant.parse("2024-01-15T23:30:00Z"),
            endTime   = Instant.parse("2024-01-16T01:00:00Z"),
            stage = SleepStageType.LIGHT
        ),
        SleepStage(
            startTime = Instant.parse("2024-01-16T01:00:00Z"),
            endTime   = Instant.parse("2024-01-16T02:30:00Z"),
            stage = SleepStageType.DEEP
        ),
        SleepStage(
            startTime = Instant.parse("2024-01-16T02:30:00Z"),
            endTime   = Instant.parse("2024-01-16T03:30:00Z"),
            stage = SleepStageType.REM
        ),
    )
)
```

## Basic example

![SleepStageChart](/charts/sleep-stage-chart.png)

```kotlin
import com.hdil.saluschart.ui.compose.charts.SleepStageChart

SleepStageChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    sleepSession = session,
    title = "Last night",
    showXAxis = true,
    showYAxis = true,
)
```

## Tap callback

```kotlin
SleepStageChart(
    sleepSession = session,
    onStageClick = { index, label ->
        println("Tapped stage $index: $label")
    }
)
```

## SleepStageType values

| Value | Color (default theme) |
|---|---|
| `AWAKE` | Red / warm |
| `REM` | Purple |
| `LIGHT` | Blue |
| `DEEP` | Deep blue |
| `UNKNOWN` | Gray |

## Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `sleepSession` | `SleepSession` | — | Sleep data |
| `title` | `String` | `"Sleep Stage Analysis"` | Chart title |
| `showXAxis` | `Boolean` | `true` | Show time axis |
| `showYAxis` | `Boolean` | `true` | Show stage name labels |
| `yAxisPosition` | `YAxisPosition` | `LEFT` | Label side |
| `barHeightRatio` | `Float` | `0.5f` | Capsule height fraction of row |
| `contentPadding` | `PaddingValues` | `(24, 16)dp` | Chart padding |
| `onStageClick` | `((Int, String) -> Unit)?` | `null` | Tap callback |
| `yAxisFixedWidth` | `Dp` | `60.dp` | Y-axis label column width |
