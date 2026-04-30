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
    implementation("io.github.hdilys:saluschart-ui-compose:0.1.6")
    implementation("io.github.hdilys:saluschart-ui-theme:0.1.6")
    implementation("io.github.hdilys:saluschart-data-model:0.1.6")
}
```

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

## Next steps

- [Installation details →](./installation)
- [Module overview →](./modules)
- [Data model →](./data-model)
- [All chart types →](../charts/)
