package com.hdil.saluschart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.PointType
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import com.hdil.saluschart.data.model.model.SleepStageType
import com.hdil.saluschart.ui.compose.charts.BarChart
import com.hdil.saluschart.ui.compose.charts.BubbleType
import com.hdil.saluschart.ui.compose.charts.CalendarChart
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import com.hdil.saluschart.ui.compose.charts.CellMarkerType
import com.hdil.saluschart.ui.compose.charts.GaugeSegment
import com.hdil.saluschart.ui.compose.charts.HorizontalStackedBarChartList
import com.hdil.saluschart.ui.compose.charts.HorizontalStackedBarRow
import com.hdil.saluschart.ui.compose.charts.LineChart
import com.hdil.saluschart.ui.compose.charts.MiniActivityRings
import com.hdil.saluschart.ui.compose.charts.MinimalBarChart
import com.hdil.saluschart.ui.compose.charts.MinimalGaugeChart
import com.hdil.saluschart.ui.compose.charts.MinimalGaugeSegment
import com.hdil.saluschart.ui.compose.charts.MinimalHorizontalStackedBar
import com.hdil.saluschart.ui.compose.charts.MinimalLadderChart
import com.hdil.saluschart.ui.compose.charts.MinimalLineChart
import com.hdil.saluschart.ui.compose.charts.MinimalMultiSegmentGauge
import com.hdil.saluschart.ui.compose.charts.MinimalProgressBar
import com.hdil.saluschart.ui.compose.charts.MinimalRangeBarChart
import com.hdil.saluschart.ui.compose.charts.MinimalSleepChart
import com.hdil.saluschart.ui.compose.charts.MinimalSleepStageChart
import com.hdil.saluschart.ui.compose.charts.MultiSegmentGaugeChart
import com.hdil.saluschart.ui.compose.charts.PieChart
import com.hdil.saluschart.ui.compose.charts.ProgressChart
import com.hdil.saluschart.ui.compose.charts.RangeBarChart
import com.hdil.saluschart.ui.compose.charts.RangeGaugeChart
import com.hdil.saluschart.ui.compose.charts.ScatterPlot
import com.hdil.saluschart.ui.compose.charts.SleepColumn
import com.hdil.saluschart.ui.compose.charts.SleepSegment
import com.hdil.saluschart.ui.compose.charts.SleepStageChart
import com.hdil.saluschart.ui.compose.charts.StackedBarChart
import com.hdil.saluschart.ui.compose.charts.StackedBarSegment
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

// ── Shared sample data ────────────────────────────────────────────────────────

private val sleepSession = SleepSession(
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

private val gaugeSegments = listOf(
    GaugeSegment(start = 0f,  end = 20f,  color = Color(0xFF4CAF50)),
    GaugeSegment(start = 20f, end = 40f,  color = Color(0xFF8BC34A)),
    GaugeSegment(start = 40f, end = 60f,  color = Color(0xFFFFEB3B)),
    GaugeSegment(start = 60f, end = 80f,  color = Color(0xFFFF9800)),
    GaugeSegment(start = 80f, end = 100f, color = Color(0xFFF44336)),
)

private val rangeBarData = listOf(
    RangeChartMark(
        x = 0.0,
        minPoint = ChartMark(x = 0.0, y = 55.0),
        maxPoint = ChartMark(x = 0.0, y = 150.0),
        label = "Mon"
    ),
    RangeChartMark(
        x = 1.0,
        minPoint = ChartMark(x = 1.0, y = 60.0),
        maxPoint = ChartMark(x = 1.0, y = 140.0),
        label = "Tue"
    ),
    RangeChartMark(
        x = 2.0,
        minPoint = ChartMark(x = 2.0, y = 58.0),
        maxPoint = ChartMark(x = 2.0, y = 155.0),
        label = "Wed"
    ),
    RangeChartMark(
        x = 3.0,
        minPoint = ChartMark(x = 3.0, y = 62.0),
        maxPoint = ChartMark(x = 3.0, y = 145.0),
        label = "Thu"
    ),
    RangeChartMark(
        x = 4.0,
        minPoint = ChartMark(x = 4.0, y = 57.0),
        maxPoint = ChartMark(x = 4.0, y = 160.0),
        label = "Fri"
    ),
)

private val horizontalRangeData = listOf(
    RangeChartMark(
        x = 0.0,
        minPoint = ChartMark(x = 0.0, y = 23.0),
        maxPoint = ChartMark(x = 0.0, y = 31.0),
        label = "Mon"
    ),
    RangeChartMark(
        x = 1.0,
        minPoint = ChartMark(x = 1.0, y = 22.5),
        maxPoint = ChartMark(x = 1.0, y = 30.5),
        label = "Tue"
    ),
    RangeChartMark(
        x = 2.0,
        minPoint = ChartMark(x = 2.0, y = 23.5),
        maxPoint = ChartMark(x = 2.0, y = 31.5),
        label = "Wed"
    ),
)

private val stackedBarData = listOf(
    ChartMark(x = 0.0, y = 250.0, label = "Mon"),
    ChartMark(x = 0.0, y = 80.0,  label = "Mon"),
    ChartMark(x = 0.0, y = 60.0,  label = "Mon"),
    ChartMark(x = 1.0, y = 220.0, label = "Tue"),
    ChartMark(x = 1.0, y = 90.0,  label = "Tue"),
    ChartMark(x = 1.0, y = 70.0,  label = "Tue"),
)

private val calendarEntries = listOf(
    CalendarEntry(date = LocalDate.of(2024, 1, 1),  value = 8200f),
    CalendarEntry(date = LocalDate.of(2024, 1, 2),  value = 5400f),
    CalendarEntry(date = LocalDate.of(2024, 1, 15), value = 12000f),
)

// ── Screen list ───────────────────────────────────────────────────────────────

private val docScreens = listOf(
    "BarChart – Basic",
    "BarChart – With Reference Line",
    "BarChart – Rounded Corners",
    "LineChart – Basic",
    "LineChart – With Reference Line",
    "RangeBarChart – Basic",
    "RangeBarChart – With Overlay Dots",
    "ScatterPlot – Basic",
    "PieChart – Donut",
    "PieChart – Full Pie",
    "ProgressChart – Activity Rings",
    "ProgressChart – Progress Bars",
    "StackedBarChart – Basic",
    "CalendarChart – Bubble",
    "SleepStageChart – Basic",
    "MultiSegmentGaugeChart",
    "RangeGaugeChart",
    "HorizontalStackedBarChartList",
    "MinimalBarChart",
    "MinimalLineChart",
    "MinimalRangeBarChart",
    "MinimalProgressBar",
    "MinimalGaugeChart",
    "MinimalMultiSegmentGauge",
    "MinimalSleepChart",
    "MinimalSleepStageChart",
    "MinimalHorizontalStackedBar",
    "MinimalLadderChart",
    "MiniActivityRings",
)

@Composable
fun DocsExamplesUI(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<String?>(null) }

    if (selected != null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "← Back",
                color = Color(0xFF7C4DFF),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { selected = null }
                    .padding(bottom = 16.dp)
            )
            Text(
                text = selected!!,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            DocsExampleContent(name = selected!!)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Docs Examples",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Code examples from the SalusChart documentation",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            docScreens.forEach { name ->
                Text(
                    text = name,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = name }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE0E0E0))
                )
            }
        }
    }
}

@Composable
private fun DocsExampleContent(name: String) {
    when (name) {

        // ── BarChart ──────────────────────────────────────────────────────────

        "BarChart – Basic" -> BarChart(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 4200.0, label = "Mon"),
                ChartMark(x = 1.0, y = 7800.0, label = "Tue"),
                ChartMark(x = 2.0, y = 6100.0, label = "Wed"),
                ChartMark(x = 3.0, y = 9500.0, label = "Thu"),
                ChartMark(x = 4.0, y = 3200.0, label = "Fri"),
            ),
            title = "Daily steps",
            xLabel = "Day",
            yLabel = "Steps",
            barColor = Color(0xFF7C4DFF),
        )

        "BarChart – With Reference Line" -> BarChart(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 4200.0, label = "Mon"),
                ChartMark(x = 1.0, y = 7800.0, label = "Tue"),
                ChartMark(x = 2.0, y = 6100.0, label = "Wed"),
                ChartMark(x = 3.0, y = 9500.0, label = "Thu"),
                ChartMark(x = 4.0, y = 3200.0, label = "Fri"),
            ),
            title = "Daily steps",
            xLabel = "Day",
            yLabel = "Steps",
            barColor = Color(0xFF7C4DFF),
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

        "BarChart – Rounded Corners" -> BarChart(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 4200.0, label = "Mon"),
                ChartMark(x = 1.0, y = 7800.0, label = "Tue"),
                ChartMark(x = 2.0, y = 6100.0, label = "Wed"),
                ChartMark(x = 3.0, y = 9500.0, label = "Thu"),
                ChartMark(x = 4.0, y = 3200.0, label = "Fri"),
            ),
            title = "Daily steps",
            xLabel = "Day",
            yLabel = "Steps",
            barColor = Color(0xFF7C4DFF),
            barCornerRadiusFractions = BarCornerRadiusFractions(
                topStart = 0.4f, topEnd = 0.4f,
                bottomStart = 0f, bottomEnd = 0f,
            )
        )

        // ── LineChart ─────────────────────────────────────────────────────────

        "LineChart – Basic" -> LineChart(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 68.5, label = "Jan 1"),
                ChartMark(x = 1.0, y = 68.2, label = "Jan 2"),
                ChartMark(x = 2.0, y = 67.9, label = "Jan 3"),
                ChartMark(x = 3.0, y = 67.5, label = "Jan 4"),
            ),
            title = "Weight",
            xLabel = "Date",
            yLabel = "kg",
            lineColor = Color(0xFF26A69A),
            strokeWidth = 4f,
        )

        "LineChart – With Reference Line" -> LineChart(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 68.5, label = "Jan 1"),
                ChartMark(x = 1.0, y = 68.2, label = "Jan 2"),
                ChartMark(x = 2.0, y = 67.9, label = "Jan 3"),
                ChartMark(x = 3.0, y = 67.5, label = "Jan 4"),
            ),
            title = "Weight",
            xLabel = "Date",
            yLabel = "kg",
            lineColor = Color(0xFF26A69A),
            referenceLines = listOf(
                ReferenceLineSpec(
                    type = ReferenceLineType.THRESHOLD,
                    y = 68.4,
                    label = "Target weight",
                    showLabel = true,
                ),
            )
        )

        // ── RangeBarChart ─────────────────────────────────────────────────────

        "RangeBarChart – Basic" -> RangeBarChart(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            data = rangeBarData,
            title = "Daily heart rate range",
            xLabel = "Day",
            yLabel = "bpm",
            barColor = Color(0xFFFF9800),
            barWidthRatio = 0.6f,
        )

        "RangeBarChart – With Overlay Dots" -> RangeBarChart(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            data = rangeBarData,
            title = "Daily heart rate range",
            xLabel = "Day",
            yLabel = "bpm",
            barColor = Color(0xFFFF9800),
            pointValues = listOf(
                listOf(75.0),
                listOf(80.0),
                listOf(72.0),
                listOf(85.0),
                listOf(78.0),
            )
        )

        // ── ScatterPlot ───────────────────────────────────────────────────────

        "ScatterPlot – Basic" -> ScatterPlot(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 120.0, label = "Jan 1"),
                ChartMark(x = 1.0, y = 118.0, label = "Jan 2"),
                ChartMark(x = 2.0, y = 125.0, label = "Jan 3"),
                ChartMark(x = 3.0, y = 115.0, label = "Jan 4"),
            ),
            title = "Systolic blood pressure",
            xLabel = "Date",
            yLabel = "mmHg",
            pointColor = Color(0xFFE91E63),
            pointType = PointType.Circle,
            pointSize = 8.dp,
        )

        // ── PieChart ──────────────────────────────────────────────────────────

        "PieChart – Donut" -> PieChart(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 250.0, label = "Carbs"),
                ChartMark(x = 1.0, y = 80.0,  label = "Protein"),
                ChartMark(x = 2.0, y = 60.0,  label = "Fat"),
            ),
            title = "Macronutrients",
            isDonut = true,
            colors = listOf(
                Color(0xFF7C4DFF),
                Color(0xFF26A69A),
                Color(0xFFFF9800),
            ),
            showLegend = true,
            showLabel = true,
        )

        "PieChart – Full Pie" -> PieChart(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 250.0, label = "Carbs"),
                ChartMark(x = 1.0, y = 80.0,  label = "Protein"),
                ChartMark(x = 2.0, y = 60.0,  label = "Fat"),
            ),
            title = "Macronutrients",
            isDonut = false,
            colors = listOf(
                Color(0xFF7C4DFF),
                Color(0xFF26A69A),
                Color(0xFFFF9800),
            ),
            showLegend = true,
            legendPosition = LegendPosition.RIGHT,
        )

        // ── ProgressChart ─────────────────────────────────────────────────────

        "ProgressChart – Activity Rings" -> ProgressChart(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            data = listOf(
                ProgressChartMark(x = 0.0, current = 420.0, max = 600.0, label = "Move",     unit = "kcal"),
                ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0,  label = "Exercise", unit = "min"),
                ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0,  label = "Stand",    unit = "hr"),
            ),
            title = "Daily activity",
            isDonut = true,
            strokeWidth = 20.dp,
            colors = listOf(
                Color(0xFFE91E63),
                Color(0xFF4CAF50),
                Color(0xFF9C27B0),
            ),
            showLegend = true,
        )

        "ProgressChart – Progress Bars" -> ProgressChart(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            data = listOf(
                ProgressChartMark(x = 0.0, current = 420.0, max = 600.0, label = "Move",     unit = "kcal"),
                ProgressChartMark(x = 1.0, current = 28.0,  max = 45.0,  label = "Exercise", unit = "min"),
                ProgressChartMark(x = 2.0, current = 8.0,   max = 12.0,  label = "Stand",    unit = "hr"),
            ),
            title = "Today's goals",
            isDonut = false,
            isPercentage = true,
            colors = listOf(
                Color(0xFF00C7BE),
                Color(0xFFFF6B35),
                Color(0xFF3A86FF),
            ),
        )

        // ── StackedBarChart ───────────────────────────────────────────────────

        "StackedBarChart – Basic" -> StackedBarChart(
            modifier = Modifier.fillMaxWidth().height(350.dp),
            data = stackedBarData,
            segmentLabels = listOf("Carbs", "Protein", "Fat"),
            colors = listOf(Color(0xFF7C4DFF), Color(0xFF26A69A), Color(0xFFFF9800)),
            title = "Daily nutrition",
            xLabel = "Day",
            yLabel = "kcal",
        )

        // ── CalendarChart ─────────────────────────────────────────────────────

        "CalendarChart – Bubble" -> CalendarChart(
            modifier = Modifier.fillMaxWidth(),
            entries = calendarEntries,
            yearMonth = YearMonth.of(2024, 1),
            markerType = CellMarkerType.BUBBLE,
            bubbleType = BubbleType.CIRCLE,
        )

        // ── SleepStageChart ───────────────────────────────────────────────────

        "SleepStageChart – Basic" -> SleepStageChart(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            sleepSession = sleepSession,
            title = "Last night",
            showXAxis = true,
            showYAxis = true,
        )

        // ── Gauge Charts ──────────────────────────────────────────────────────

        "MultiSegmentGaugeChart" -> MultiSegmentGaugeChart(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            title = "Fitness Level",
            value = 62f,
            minValue = 0f,
            maxValue = 100f,
            segments = gaugeSegments,
            tickValues = listOf(0f, 20f, 40f, 60f, 80f, 100f),
            leftHint = "Poor",
            rightHint = "Excellent",
        )

        "RangeGaugeChart" -> RangeGaugeChart(
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

        // ── Horizontal Charts ─────────────────────────────────────────────────

        "HorizontalStackedBarChartList" -> HorizontalStackedBarChartList(
            modifier = Modifier.fillMaxWidth(),
            title = "Weekly nutrition",
            rows = listOf(
                HorizontalStackedBarRow(
                    title = "Monday",
                    unit = "kcal",
                    total = 390f,
                    segments = listOf(250f, 80f, 60f),
                    segmentLabels = listOf("Carbs", "Protein", "Fat"),
                ),
                HorizontalStackedBarRow(
                    title = "Tuesday",
                    unit = "kcal",
                    total = 380f,
                    segments = listOf(220f, 90f, 70f),
                    segmentLabels = listOf("Carbs", "Protein", "Fat"),
                ),
            ),
            colors = listOf(Color(0xFF7C4DFF), Color(0xFF26A69A), Color(0xFFFF9800)),
            showLegend = true,
            legendLabels = listOf("Carbs", "Protein", "Fat"),
        )

        // ── Minimal Charts ────────────────────────────────────────────────────

        "MinimalBarChart" -> MinimalBarChart(
            modifier = Modifier.size(120.dp, 60.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 4200.0),
                ChartMark(x = 1.0, y = 7800.0),
                ChartMark(x = 2.0, y = 6100.0),
            ),
            color = Color(0xFF7C4DFF),
        )

        "MinimalLineChart" -> MinimalLineChart(
            modifier = Modifier.size(120.dp, 60.dp),
            data = listOf(
                ChartMark(x = 0.0, y = 68.5),
                ChartMark(x = 1.0, y = 68.2),
                ChartMark(x = 2.0, y = 67.9),
            ),
            color = Color(0xFF26A69A),
        )

        // MinimalRangeBarChart ignores its modifier param (Box(Modifier) bug),
        // so we size the outer Box instead.
        "MinimalRangeBarChart" -> androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth().height(120.dp)
        ) {
            MinimalRangeBarChart(
                modifier = Modifier.fillMaxSize(),
                data = listOf(
                    ChartMark(x = 0.0, y = 55.0),
                    ChartMark(x = 0.0, y = 150.0),
                    ChartMark(x = 1.0, y = 60.0),
                    ChartMark(x = 1.0, y = 140.0),
                    ChartMark(x = 2.0, y = 58.0),
                    ChartMark(x = 2.0, y = 155.0),
                ),
                color = Color(0xFFFF9800),
            )
        }

        "MinimalProgressBar" -> MinimalProgressBar(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            progress = 0.7f,
            fillColor = Color(0xFFE91E63),
        )

        "MinimalGaugeChart" -> MinimalGaugeChart(
            modifier = Modifier.size(200.dp, 100.dp),
            data = RangeChartMark(
                x = 0.0,
                minPoint = ChartMark(x = 0.0, y = 55.0),
                maxPoint = ChartMark(x = 0.0, y = 95.0),
            ),
            containerMin = 40.0,
            containerMax = 120.0,
            rangeColor = Color(0xFF26A69A),
            label = "Normal",
        )

        "MinimalMultiSegmentGauge" -> MinimalMultiSegmentGauge(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            segments = listOf(
                MinimalGaugeSegment(fraction = 0.25f, color = Color(0xFF4CAF50)),
                MinimalGaugeSegment(fraction = 0.25f, color = Color(0xFFFFEB3B)),
                MinimalGaugeSegment(fraction = 0.25f, color = Color(0xFFFF9800)),
                MinimalGaugeSegment(fraction = 0.25f, color = Color(0xFFF44336)),
            ),
            markerRatio = 0.62f,
            label = "Good",
        )

        // segmentGapRatio must be very small or segments get clipped to 0 height.
        "MinimalSleepChart" -> MinimalSleepChart(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            segmentGapRatio = 0.02f,
            columns = listOf(
                SleepColumn(listOf(
                    SleepSegment(value = 30f,  color = Color(0xFFE53935)),
                    SleepSegment(value = 90f,  color = Color(0xFF42A5F5)),
                    SleepSegment(value = 90f,  color = Color(0xFF1A237E)),
                    SleepSegment(value = 60f,  color = Color(0xFF7E57C2)),
                )),
                SleepColumn(listOf(
                    SleepSegment(value = 20f,  color = Color(0xFFE53935)),
                    SleepSegment(value = 100f, color = Color(0xFF42A5F5)),
                    SleepSegment(value = 80f,  color = Color(0xFF1A237E)),
                    SleepSegment(value = 50f,  color = Color(0xFF7E57C2)),
                )),
                SleepColumn(listOf(
                    SleepSegment(value = 25f,  color = Color(0xFFE53935)),
                    SleepSegment(value = 110f, color = Color(0xFF42A5F5)),
                    SleepSegment(value = 70f,  color = Color(0xFF1A237E)),
                    SleepSegment(value = 65f,  color = Color(0xFF7E57C2)),
                )),
                SleepColumn(listOf(
                    SleepSegment(value = 15f,  color = Color(0xFFE53935)),
                    SleepSegment(value = 95f,  color = Color(0xFF42A5F5)),
                    SleepSegment(value = 100f, color = Color(0xFF1A237E)),
                    SleepSegment(value = 60f,  color = Color(0xFF7E57C2)),
                )),
                SleepColumn(listOf(
                    SleepSegment(value = 35f,  color = Color(0xFFE53935)),
                    SleepSegment(value = 80f,  color = Color(0xFF42A5F5)),
                    SleepSegment(value = 85f,  color = Color(0xFF1A237E)),
                    SleepSegment(value = 70f,  color = Color(0xFF7E57C2)),
                )),
            )
        )

        "MinimalSleepStageChart" -> MinimalSleepStageChart(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            sleepSession = sleepSession,
        )

        "MinimalHorizontalStackedBar" -> MinimalHorizontalStackedBar(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            segments = listOf(
                StackedBarSegment(value = 250f, color = Color(0xFF7C4DFF)),
                StackedBarSegment(value = 80f,  color = Color(0xFF26A69A)),
                StackedBarSegment(value = 60f,  color = Color(0xFFFF9800)),
            )
        )

        "MinimalLadderChart" -> MinimalLadderChart(
            modifier = Modifier.size(200.dp, 60.dp),
            bandCount = 5,
            selectedBandIndex = 2,
            markerRatio = 0.5f,
            trackColor = Color(0xFFE0E0E0),
            selectedColor = Color(0xFF7C4DFF),
            markerColor = Color(0xFF4A00E0),
        )

        "MiniActivityRings" -> MiniActivityRings(
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
            strokeWidth = 12f,
        )
    }
}
