# Mobile Dashboard

A scrollable health dashboard combining multiple chart types. This example shows how to arrange cards with different chart types using Jetpack Compose.

## Full example

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.ui.compose.charts.*

@Composable
fun HealthDashboard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ActivityRingsCard()
        StepCountCard()
        HeartRateCard()
        SleepCard()
    }
}

@Composable
private fun ActivityRingsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        ProgressChart(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            data = listOf(
                ProgressChartMark(x = 0.0, current = 420.0, max = 600.0, label = "Move", unit = "kcal"),
                ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0,  label = "Exercise", unit = "min"),
                ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0,  label = "Stand", unit = "hr"),
            ),
            title = "Activity",
            isDonut = true,
            colors = listOf(
                Color(0xFFE91E63),
                Color(0xFF4CAF50),
                Color(0xFF9C27B0),
            ),
            donutHeight = 180.dp,
            strokeWidth = 18.dp,
        )
    }
}

@Composable
private fun StepCountCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        BarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(16.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 4200.0, label = "Mon"),
                ChartMark(x = 1.0, y = 7800.0, label = "Tue"),
                ChartMark(x = 2.0, y = 6100.0, label = "Wed"),
                ChartMark(x = 3.0, y = 9500.0, label = "Thu"),
                ChartMark(x = 4.0, y = 3200.0, label = "Fri"),
                ChartMark(x = 5.0, y = 8800.0, label = "Sat"),
                ChartMark(x = 6.0, y = 7200.0, label = "Sun"),
            ),
            title = "Steps",
            xLabel = "Day",
            yLabel = "Steps",
            barColor = Color(0xFF7C4DFF),
        )
    }
}

@Composable
private fun HeartRateCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        RangeBarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(16.dp),
            data = listOf(
                RangeChartMark(
                    x = 0.0,
                    minPoint = ChartMark(x = 0.0, y = 55.0),
                    maxPoint = ChartMark(x = 0.0, y = 148.0),
                    label = "Mon"
                ),
                RangeChartMark(
                    x = 1.0,
                    minPoint = ChartMark(x = 1.0, y = 58.0),
                    maxPoint = ChartMark(x = 1.0, y = 155.0),
                    label = "Tue"
                ),
                RangeChartMark(
                    x = 2.0,
                    minPoint = ChartMark(x = 2.0, y = 60.0),
                    maxPoint = ChartMark(x = 2.0, y = 142.0),
                    label = "Wed"
                ),
            ),
            title = "Heart rate",
            yLabel = "bpm",
            barColor = Color(0xFFE91E63),
            unit = "bpm",
        )
    }
}

@Composable
private fun SleepCard() {
    val session = /* your SleepSession */ return

    Card(modifier = Modifier.fillMaxWidth()) {
        SleepStageChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(16.dp),
            sleepSession = session,
            title = "Last night",
        )
    }
}
```

## Dashboard with minimal charts in a grid

Use minimal chart variants for a compact overview grid:

```kotlin
@Composable
fun MiniDashboardGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Steps sparkline
        Card(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Steps", style = MaterialTheme.typography.labelSmall)
                MinimalBarChart(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    data = weeklySteps,
                    barColor = Color(0xFF7C4DFF),
                )
                Text("7,842", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Weight trend
        Card(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Weight", style = MaterialTheme.typography.labelSmall)
                MinimalLineChart(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    data = weightTrend,
                    lineColor = Color(0xFF26A69A),
                )
                Text("68.2 kg", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
```

## Using core:transform

Transform raw health data before passing it to charts:

```kotlin
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup

// Raw step records → daily totals → ChartMark list
val dailySteps = rawStepData.transform(
    timeUnit = TimeUnitGroup.DAY,
    aggregationType = AggregationType.SUM,
)

BarChart(data = dailySteps, title = "Daily steps")
```
