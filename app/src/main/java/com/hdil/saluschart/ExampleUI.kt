package com.hdil.saluschart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.PointType
import com.hdil.saluschart.core.chart.ProgressChartPoint
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.StackedChartPoint
import com.hdil.saluschart.core.transform.TimeDataPoint
import com.hdil.saluschart.core.transform.toChartPoints
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.data.model.model.HealthData
import com.hdil.saluschart.data.model.model.StepCount
import com.hdil.saluschart.ui.compose.charts.BarChart
import com.hdil.saluschart.ui.compose.charts.BubbleType
import com.hdil.saluschart.ui.compose.charts.CalendarChart
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import com.hdil.saluschart.ui.compose.charts.LineChart
import com.hdil.saluschart.ui.compose.charts.MinimalBarChart
import com.hdil.saluschart.ui.compose.charts.MinimalGaugeChart
import com.hdil.saluschart.ui.compose.charts.MinimalLineChart
import com.hdil.saluschart.ui.compose.charts.MinimalRangeBarChart
import com.hdil.saluschart.ui.compose.charts.PieChart
import com.hdil.saluschart.ui.compose.charts.ProgressChart
import com.hdil.saluschart.ui.compose.charts.RangeBarChart
import com.hdil.saluschart.ui.compose.charts.ScatterPlot
import com.hdil.saluschart.ui.compose.charts.StackedBarChart
import com.hdil.saluschart.ui.theme.Orange
import com.hdil.saluschart.ui.theme.Primary_Purple
import com.hdil.saluschart.ui.theme.Teel
import com.hdil.saluschart.ui.theme.Yellow
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

// 스택 바 차트용 세그먼트 레이블 (한 번만 정의)
private val segmentLabels = listOf("단백질", "지방", "탄수화물")
private val sampleData = listOf(10f, 25f, 40f, 20f, 35f, 55f, 45f)
private val sampleData2 = listOf(5f, 15f, 60f, 45f, 35f, 25f, 10f)
private val sampleData3 = listOf(8f, 22f, 10f, 40f, 18f, 32f, 12f)
private val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")

// Real step count data from JSON - 30-minute activity periods
private val stepCountHealthData = listOf(
    StepCount(Instant.parse("2025-05-04T08:00:00Z"), Instant.parse("2025-05-04T08:30:00Z"), 2431),
    StepCount(Instant.parse("2025-05-04T09:00:00Z"), Instant.parse("2025-05-04T09:30:00Z"), 1359),
    StepCount(Instant.parse("2025-05-04T10:00:00Z"), Instant.parse("2025-05-04T10:30:00Z"), 6149),
    StepCount(Instant.parse("2025-05-04T11:00:00Z"), Instant.parse("2025-05-04T11:30:00Z"), 4246),
    StepCount(Instant.parse("2025-05-04T12:00:00Z"), Instant.parse("2025-05-04T12:30:00Z"), 2855),
    StepCount(Instant.parse("2025-05-04T13:00:00Z"), Instant.parse("2025-05-04T13:30:00Z"), 9831),
    StepCount(Instant.parse("2025-05-04T14:00:00Z"), Instant.parse("2025-05-04T14:30:00Z"), 1498),
    StepCount(Instant.parse("2025-05-04T15:00:00Z"), Instant.parse("2025-05-04T15:30:00Z"), 8455),
    StepCount(Instant.parse("2025-05-04T16:00:00Z"), Instant.parse("2025-05-04T16:30:00Z"), 4662),
    StepCount(Instant.parse("2025-05-04T17:00:00Z"), Instant.parse("2025-05-04T17:30:00Z"), 1329),
    StepCount(Instant.parse("2025-05-04T18:00:00Z"), Instant.parse("2025-05-04T18:30:00Z"), 2327),
    StepCount(Instant.parse("2025-05-04T19:00:00Z"), Instant.parse("2025-05-04T19:30:00Z"), 7369),
    StepCount(Instant.parse("2025-05-04T20:00:00Z"), Instant.parse("2025-05-04T20:30:00Z"), 1649),
    StepCount(Instant.parse("2025-05-04T21:00:00Z"), Instant.parse("2025-05-04T21:30:00Z"), 8768),
    StepCount(Instant.parse("2025-05-04T22:00:00Z"), Instant.parse("2025-05-04T22:30:00Z"), 5235),
    StepCount(Instant.parse("2025-05-04T23:00:00Z"), Instant.parse("2025-05-04T23:30:00Z"), 8286),
    StepCount(Instant.parse("2025-05-05T00:00:00Z"), Instant.parse("2025-05-05T00:30:00Z"), 9606),
    StepCount(Instant.parse("2025-05-05T01:00:00Z"), Instant.parse("2025-05-05T01:30:00Z"), 8043),
    StepCount(Instant.parse("2025-05-05T02:00:00Z"), Instant.parse("2025-05-05T02:30:00Z"), 6046),
    StepCount(Instant.parse("2025-05-05T03:00:00Z"), Instant.parse("2025-05-05T03:30:00Z"), 4874),
    StepCount(Instant.parse("2025-05-05T04:00:00Z"), Instant.parse("2025-05-05T04:30:00Z"), 5893),
    StepCount(Instant.parse("2025-05-05T05:00:00Z"), Instant.parse("2025-05-05T05:30:00Z"), 5060),
    StepCount(Instant.parse("2025-05-05T06:00:00Z"), Instant.parse("2025-05-05T06:30:00Z"), 4520),
    StepCount(Instant.parse("2025-05-05T07:00:00Z"), Instant.parse("2025-05-05T07:30:00Z"), 6420),
    StepCount(Instant.parse("2025-05-05T08:00:00Z"), Instant.parse("2025-05-05T08:30:00Z"), 1837),
    StepCount(Instant.parse("2025-05-05T09:00:00Z"), Instant.parse("2025-05-05T09:30:00Z"), 5893),
    StepCount(Instant.parse("2025-05-05T10:00:00Z"), Instant.parse("2025-05-05T10:30:00Z"), 4000),
    StepCount(Instant.parse("2025-05-05T11:00:00Z"), Instant.parse("2025-05-05T11:30:00Z"), 5760),
    StepCount(Instant.parse("2025-05-05T12:00:00Z"), Instant.parse("2025-05-05T12:30:00Z"), 3621),
    StepCount(Instant.parse("2025-05-05T13:00:00Z"), Instant.parse("2025-05-05T13:30:00Z"), 7387)
)

// Keep the old TimeDataPoint for other examples that still need it
private val timeDataPoint = TimeDataPoint(
    x = stepCountHealthData.map { it.startTime },
    y = stepCountHealthData.map { it.stepCount.toFloat() },
    timeUnit = TimeUnitGroup.HOUR,
)

private val yearMonth = YearMonth.now()
private val startDate = LocalDate.of(yearMonth.year, 8, 1)
private val endDate = LocalDate.of(yearMonth.year, 8, 25)
private val random = java.util.Random(0)
private val entries = generateSequence(startDate) { date ->
    if (date.isBefore(endDate)) date.plusDays(1) else null
}.map { date ->
    val value = random.nextFloat() * 100
    CalendarEntry(
        date = date,
        value = value,
    )
}.toList()

// ChartPoint 리스트로 변환
private val chartPoints = sampleData.mapIndexed { index, value ->
    ChartPoint(
        x = index.toFloat(),
        y = value,
        label = weekDays.getOrElse(index) { "" }
    )
}

@Composable
fun ExampleUI(modifier: Modifier = Modifier) {
    val chartType = listOf(
        "BarChart 1",
        "BarChart 2",
        "DonutChart 1",
        "LineChart 1",
        "LineChart 2",
        "PieChart 1",
        "CalendarChart 1",
        "CalendarChart 2",
        "ScatterPlot 1",
        "Minimal Chart",
        "Stacked Bar Chart",
        "Range Bar Chart",
        "Progress Bar Chart",
        "Progress Ring Chart",
        "BarChart Timestep Transformation",
        "X-Axis Tick Reduction Demo"
    )

    var selectedChartType by remember { mutableStateOf<String?>("Stacked Bar Chart") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (selectedChartType == null) {
            chartType.forEach { type ->
                Text(
                    text = type,
                    modifier = Modifier
                        .padding(12.dp)
                        .clickable { selectedChartType = type }
                )
            }
        } else {

            IconButton(
                onClick = { selectedChartType = null },
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp) // 아이콘 크기 조정

                )
            }

            when (selectedChartType) {
                "BarChart 1" -> BarChart_1()
                "BarChart 2" -> BarChart_2()
                "DonutChart 1" -> DonutChart_1()
                "LineChart 1" -> LineChart_1()
                "LineChart 2" -> LineChart_2()
                "PieChart 1" -> PieChart_1()
                "CalendarChart 1" -> CalendarChart_1()
                "CalendarChart 2" -> CalendarChart_2()
                "ScatterPlot 1" -> ScatterPlot_1()
                "Minimal Chart" -> Minimal_BarChart() // Placeholder for minimal bar chart
                "Stacked Bar Chart" -> StackedBarChart_1()
                "Range Bar Chart" -> RangeBarChart_1()
                "Progress Bar Chart" -> ProgressBarChart_1()
                "Progress Ring Chart" -> ProgressBarChart_2()
                "BarChart Timestep Transformation" -> TimeStepBarChart()
                "X-Axis Tick Reduction Demo" -> XAxisTickReductionDemo()
                else -> Text("Unknown Chart Type")
            }
        }
    }
}

@Composable
fun BarChart_1() {
    val avgY = if (chartPoints.isNotEmpty()) chartPoints.map { it.y }.average().toFloat() else 0f
    BarChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = chartPoints,
        xLabel = "Week",
        yLabel = "Value",
        title = "Weekly Data",
        barColor = Primary_Purple,
        maxY = 70f,
        barWidthRatio = 0.8f,
        labelTextSize = 40f,
        tooltipTextSize = 5f,
        interactionType = InteractionType.Bar.TOUCH_AREA,
        yAxisPosition = YAxisPosition.LEFT,
        referenceLineType = ReferenceLineType.AVERAGE,
        referenceLineStyle = LineStyle.DASHED ,
        showReferenceLineLabel = true,
    )
}

@Composable
fun BarChart_2() {
    BarChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = chartPoints,
        xLabel = "Week",
        yLabel = "Value",
        title = "Weekly Data",
        barColor = Primary_Purple,
        maxY = 60f,
        barWidthRatio = 0.5f,
        labelTextSize = 28f,
        tooltipTextSize = 32f,
        interactionType = InteractionType.Bar.BAR,
        windowSize = 3,
        showLabel = true,
        yAxisPosition = YAxisPosition.RIGHT,
    )
}

@Composable
fun DonutChart_1() {
    PieChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = chartPoints.subList(0, 4),
        title = "요일별 활동량",
        isDonut = true,
        colors = listOf(Primary_Purple, Teel, Orange, Yellow),
        showLegend = true,
        showLabel = true
    )
}

@Composable
fun LineChart_1() {
    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {

        LineChart(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            data = chartPoints,
            title = "요일별 활동량",
            yLabel = "활동량",
            xLabel = "요일",
            lineColor = Primary_Purple,
            strokeWidth = 12f,
            showPoint = false,
            showValue = true,
            windowSize = 3,
            interactionType = InteractionType.Line.TOUCH_AREA,
            yAxisPosition = YAxisPosition.RIGHT
        )
    }
}

@Composable
fun LineChart_2() {
    LineChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = chartPoints,
        title = "요일별 활동량",
        yLabel = "활동량",
        xLabel = "요일",
        lineColor = Primary_Purple,
        showPoint = true,
        pointRadius = Pair(8.dp, 4.dp),
        strokeWidth = 4f,
        minY = 5f,
        maxY = 70f,
        interactionType = InteractionType.Line.POINT,
        yAxisPosition = YAxisPosition.LEFT,
        referenceLineType = ReferenceLineType.TREND,
        referenceLineStyle = LineStyle.DASHDOT
    )
}

@Composable
fun PieChart_1() {
    PieChart(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = chartPoints.subList(0, 4),
        title = "요일별 활동량",
        isDonut = false,
        colors = listOf(Primary_Purple, Teel, Orange, Yellow),
        showLegend = true,
        legendPosition = LegendPosition.RIGHT

    )
}

@Composable
fun CalendarChart_1() {
    CalendarChart(
        modifier = Modifier.fillMaxWidth().height(600.dp),
        entries = entries,
        yearMonth = yearMonth,
        color = Primary_Purple,
        maxBubbleSize = 10f,
        minBubbleSize = 6f
    )
}

@Composable
fun CalendarChart_2() {
    CalendarChart(
        modifier = Modifier.width(300.dp).height(500.dp),
        entries = entries,
        yearMonth = yearMonth,
        color = Primary_Purple,
        bubbleType = BubbleType.RECTANGLE,
        maxBubbleSize = 10f,
        minBubbleSize = 6f
    )
}

@Composable
fun ScatterPlot_1() {
    ScatterPlot(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = chartPoints,
        pointColor = Primary_Purple,
        title = "요일별 활동량",
        yLabel = "활동량",
        xLabel = "요일",
        interactionType = InteractionType.Scatter.POINT,
        pointType = PointType.Triangle,
        yAxisPosition = YAxisPosition.LEFT
    )
}

@Composable
fun Minimal_BarChart() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                ,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "오늘 운동",
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "1시간 20분",
                    color = Primary_Purple,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .height(36.dp)
                    .align(androidx.compose.ui.Alignment.CenterVertically)
            ) {
                MinimalBarChart(
                    data = chartPoints,
                    color = Primary_Purple,

                )
            }
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
            ,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "오늘 운동",
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "1시간 20분",
                    color = Primary_Purple,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .height(36.dp)
                    .align(androidx.compose.ui.Alignment.CenterVertically)
            ) {
                MinimalLineChart(
                    data = chartPoints,
                    color = Primary_Purple,
                )
            }
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
            ,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "일일 심박수",
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "78 ~ 104 bpm",
                    color = Orange,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                MinimalRangeBarChart(
                    data = rangeData,
                    color = Orange,
                )
            }
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
            ,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "심박수",
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "78 ~ 104 bpm",
                    color = Orange,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .height(52.dp)
                    .align(androidx.compose.ui.Alignment.CenterVertically)
            ) {
                val singleRangeData = RangeChartPoint(
                    x = 0f,
                    yMin = 78f,
                    yMax = 104f,
                    label = "Heart Rate"
                )
                MinimalGaugeChart(
                    data = singleRangeData,
                    containerMin = 60f,  // 정상 심박수 범위 시작
                    containerMax = 120f, // 정상 심박수 범위 끝
                    containerColor = Color.LightGray,
                    rangeColor = Orange,
                )
            }
        }
    }
}

@Composable
fun StackedBarChart_1() {
    StackedBarChart(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = stackedData,
        segmentLabels = segmentLabels,
        title = "요일별 영양소 섭취량",
        yLabel = "영양소 (g)",
        xLabel = "요일",
        showLegend = true,
        legendPosition = LegendPosition.BOTTOM,
        windowSize = 3,
        yAxisPosition = YAxisPosition.RIGHT,
        interactionType = InteractionType.StackedBar.TOUCH_AREA,
        colors = listOf(
            Color(0xFF2196F3), // 파랑 (단백질)
            Color(0xFFFF9800), // 주황 (지방)
            Color(0xFF4CAF50)  // 초록 (탄수화물)
        )
    )
}

@Composable
fun RangeBarChart_1() {
    RangeBarChart(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = rangeData,
        title = "일별 심박수 범위",
        yLabel = "심박수 (bpm)",
        xLabel = "날짜",
        windowSize = 4,
        barColor = Color(0xFFFF9800),
        interactionType = InteractionType.RangeBar.TOUCH_AREA
    )
}

@Composable
fun ProgressBarChart_1() {
    val progressData = listOf(
        ProgressChartPoint(
            x = 0f,
            current = 1200f,
            max = 2000f,
            label = "Move",
            unit = "KJ"
        ),
        ProgressChartPoint(
            x = 1f,
            current = 20f,
            max = 60f,
            label = "Exercise",
            unit = "min"
        ),
        ProgressChartPoint(
            x = 2f,
            current = 7f,
            max = 10f,
            label = "Stand",
            unit = "h"
        )
    )
    ProgressChart(
        data = progressData,
        title = "일일 활동 진행률",
        isDonut = false,
        isPercentage = true,
        colors = listOf(
            Color(0xFF00C7BE), // 청록색 (Move)
            Color(0xFFFF6B35), // 주황색 (Exercise)
            Color(0xFF3A86FF)  // 파란색 (Stand)
        ),
        strokeWidth = 80f
    )
}

@Composable
fun ProgressBarChart_2() {
    val progressData = listOf(
        ProgressChartPoint(
            x = 0f,
            current = 1200f,
            max = 2000f,
            label = "Move",
            unit = "KJ"
        ),
        ProgressChartPoint(
            x = 1f,
            current = 20f,
            max = 60f,
            label = "Exercise",
            unit = "min"
        ),
        ProgressChartPoint(
            x = 2f,
            current = 7f,
            max = 10f,
            label = "Stand",
            unit = "h"
        )
    )
    ProgressChart(
        data = progressData,
        title = "일일 활동 진행률",
        isDonut = true,
        isPercentage = false,
        showLabels = false,
        colors = listOf(
            Color(0xFFE91E63), // 핑크
            Color(0xFF4CAF50), // 초록
            Color(0xFF9C27B0), // 보라
        ),
        strokeWidth = 60f
    )
}

@Composable
fun XAxisTickReductionDemo() {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "X-Axis Tick Reduction Algorithm Demo",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Create dense data with many labels (50 points)
        val denseDataLabels = (1..50).map { "Day $it" }
        val denseDataValues = (1..50).map { (20..80).random().toFloat() }
        val denseChartPoints = denseDataLabels.mapIndexed { index, label ->
            ChartPoint(
                x = index.toFloat(),
                y = denseDataValues[index],
                label = label
            )
        }
        
        Text(
            text = "Without Tick Reduction (50 labels)",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BarChart(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            data = denseChartPoints,
            title = "Dense Data - All Labels (Overlapping)",
            barColor = Color.Red,
            barWidthRatio = 0.8f,
            labelTextSize = 20f, // Normal size to show overlap
            maxXTicksLimit = null // Show all labels
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "With Tick Reduction (Max 10 labels)",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BarChart(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            data = denseChartPoints,
            title = "Dense Data - Reduced Labels (Clean)",
            barColor = Primary_Purple,
            barWidthRatio = 0.8f,
            labelTextSize = 20f,
            maxXTicksLimit = 10 // Limit to 10 labels
        )
    }
}

@Composable
fun TimeStepBarChart() {
    // Updated to use real step count data with 30-minute intervals
    // Data spans from May 4, 2025 08:00 to May 5, 2025 13:30 (30 data points)
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("30분대별") }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "실제 걸음 수 데이터 (5/4-5/5)",
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                color = Color.Black
            )
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = Color.Black
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            DropdownMenuItem(
                text = { Text("30분대별") },
                onClick = {
                    selectedOption = "30분대별"
                    expanded = false
                },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenuItem(
                text = { Text("시간대별") },
                onClick = {
                    selectedOption = "시간대별"
                    expanded = false
                },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenuItem(
                text = { Text("일별") },
                onClick = {
                    selectedOption = "일별"
                    expanded = false
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        BarChart(
            modifier = Modifier.fillMaxWidth().height(500.dp),
            data = when (selectedOption) {
                "30분대별" -> stepCountHealthData.transform(
                    timeUnit = TimeUnitGroup.MINUTE,
                    aggregationType = AggregationType.SUM
                )
                "시간대별" -> stepCountHealthData.transform(
                    timeUnit = TimeUnitGroup.HOUR,
                    aggregationType = AggregationType.SUM
                )
                "일별" -> stepCountHealthData.transform(
                    timeUnit = TimeUnitGroup.DAY,
                    aggregationType = AggregationType.SUM
                )
                else -> stepCountHealthData.transform(
                    timeUnit = TimeUnitGroup.MINUTE,
                    aggregationType = AggregationType.SUM
                )
            },
            title = "걸음 수 (${selectedOption}) - ${if (selectedOption == "30분대별") "Raw Data" else "Aggregated"}",
            barColor = Primary_Purple,
            barWidthRatio = 0.5f,
            labelTextSize = 20f,
            // 30분대별과 시간대별일 때는 windowSize로 스크롤링 활성화
            windowSize = when (selectedOption) {
                "30분대별" -> 10
                "시간대별" -> 8
                else -> null
            }
        )
    }
}


// 범위 차트용 샘플 데이터 (심박수 범위 예시)
private val rangeData = listOf(
    RangeChartPoint(x = 0f, yMin = 54f, yMax = 160f, label = "2일"),
    RangeChartPoint(x = 1f, yMin = 65f, yMax = 145f, label = "3일"),
    RangeChartPoint(x = 2f, yMin = 58f, yMax = 125f, label = "4일"),
    RangeChartPoint(x = 3f, yMin = 75f, yMax = 110f, label = "6일"),
    RangeChartPoint(x = 4f, yMin = 68f, yMax = 162f, label = "7일"),
    RangeChartPoint(x = 5f, yMin = 72f, yMax = 168f, label = "8일"),
    RangeChartPoint(x = 6f, yMin = 65f, yMax = 138f, label = "9일"),
    RangeChartPoint(x = 7f, yMin = 85f, yMax = 105f, label = "10일")
)

// 스택 바 차트용 샘플 데이터 (일별 영양소 섭취량 예시)
private val stackedData = listOf(
    StackedChartPoint(
        x = 0f,
        values = listOf(80f, 45f, 120f), // 단백질, 지방, 탄수화물 (g)
        label = "월"
    ),
    StackedChartPoint(
        x = 1f,
        values = listOf(75f, 38f, 110f),
        label = "화"
    ),
    StackedChartPoint(
        x = 2f,
        values = listOf(90f, 52f, 140f),
        label = "수"
    ),
    StackedChartPoint(
        x = 3f,
        values = listOf(85f, 41f, 135f),
        label = "목"
    ),
    StackedChartPoint(
        x = 4f,
        values = listOf(95f, 58f, 150f),
        label = "금"
    ),
    StackedChartPoint(
        x = 5f,
        values = listOf(70f, 35f, 100f),
        label = "토"
    ),
    StackedChartPoint(
        x = 6f,
        values = listOf(88f, 48f, 125f),
        label = "일"
    )
)