# Getting Started

SalusChart is a modular Jetpack Compose charting library for Android, focused on health data visualization.

## Requirements

- Android minSdk 30
- Kotlin 1.9+
- Jetpack Compose (BOM 2024.x or later)

## Quick install

Add the Maven Central repository if not already present:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

Add the core dependency:

```kotlin
// build.gradle.kts (app or module)
dependencies {
    val salusVersion = "<latest>"
    implementation("io.github.hdilys:saluschart-ui-compose:$salusVersion")
    implementation("io.github.hdilys:saluschart-ui-theme:$salusVersion")
    implementation("io.github.hdilys:saluschart-data-model:$salusVersion")
    implementation("io.github.hdilys:saluschart-core-transform:$salusVersion")
    implementation("io.github.hdilys:saluschart-core-util:$salusVersion")
}
```

See [Releases](./releases) for the latest version.

## Your first chart

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.compose.charts.BarChart

@Composable
fun StepCountChart() {
    val data = listOf(
        ChartMark(x = 0.0, y = 4200.0, label = "Mon"),
        ChartMark(x = 1.0, y = 7800.0, label = "Tue"),
        ChartMark(x = 2.0, y = 6100.0, label = "Wed"),
        ChartMark(x = 3.0, y = 9500.0, label = "Thu"),
        ChartMark(x = 4.0, y = 3200.0, label = "Fri"),
    )

    BarChart(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        data = data,
        title = "Step count",
        xLabel = "Day",
        yLabel = "Steps",
        barColor = Color(0xFF7C4DFF),
    )
}
```

## From health records

Most health apps start from platform records, map them into SalusChart's health models, then aggregate them into chart marks.

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.StepCount
import com.hdil.saluschart.ui.compose.charts.BarChart
import java.time.Instant

@Composable
fun DailyStepsFromHealthRecords() {
    val records = listOf(
        StepCount(
            startTime = Instant.parse("2026-04-27T00:00:00Z"),
            endTime = Instant.parse("2026-04-27T23:59:00Z"),
            stepCount = 7_842,
        ),
        StepCount(
            startTime = Instant.parse("2026-04-28T00:00:00Z"),
            endTime = Instant.parse("2026-04-28T23:59:00Z"),
            stepCount = 9_120,
        ),
    )

    val dailySteps = records.transform(
        timeUnit = TimeUnitGroup.DAY,
        aggregationType = AggregationType.SUM,
    )

    BarChart(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        data = dailySteps,
        title = "Daily steps",
        yLabel = "Steps",
        unit = "steps",
        barColor = Color(0xFF7C4DFF),
    )
}
```

The same flow works for platform integrations:

```text
platform records -> data:model -> core:transform -> chart composable
```

## Next steps

- [Installation details →](./installation)
- [Module overview →](./modules)
- [Data model →](./data-model)
- [Data transform →](./data-transform)
- [Interactions →](./interactions)
- [Platform integrations →](./platform-integrations)
- [Releases →](./releases)
- [All chart types →](../charts/)
