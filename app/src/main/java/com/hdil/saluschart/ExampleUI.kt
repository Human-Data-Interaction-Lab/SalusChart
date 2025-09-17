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
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.transform.TimeDataPoint
import com.hdil.saluschart.core.transform.toChartPoints
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.ui.compose.charts.BarChart
import com.hdil.saluschart.ui.compose.charts.BubbleType
import com.hdil.saluschart.ui.compose.charts.CalendarChart
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import com.hdil.saluschart.ui.compose.charts.LineChart
import com.hdil.saluschart.ui.compose.charts.MinimalBarChart
import com.hdil.saluschart.ui.compose.charts.MinimalGaugeChart
import com.hdil.saluschart.ui.compose.charts.MinimalLineChart
import com.hdil.saluschart.ui.compose.charts.MinimalRangeBarChart
import com.hdil.saluschart.ui.compose.charts.PagedCalendarChart
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

// 스택 바 차트용 세그먼트 레이블 (한 번만 정의)
private val segmentLabels = listOf("단백질", "지방", "탄수화물")
private val sampleData = listOf(10f, 25f, 40f, 20f, 35f, 55f, 45f)
private val sampleData2 = listOf(5f, 15f, 60f, 45f, 35f, 25f, 10f)
private val sampleData3 = listOf(8f, 22f, 10f, 40f, 18f, 32f, 12f)
private val sampleData4 = listOf(10f, 25f, 37f, 20f, 45f, 55f, 45f, 17f, 30f, 45f, 45f, 35f, 25f, 10f, 8f, 22f, 10f, 40f, 18f, 32f, 12f)
private val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")

private val isoTime = listOf(
    "2025-05-05T00:00:00Z", "2025-05-05T06:00:00Z", "2025-05-05T12:00:00Z", "2025-05-05T18:00:00Z",
    "2025-05-06T00:00:00Z", "2025-05-06T06:00:00Z", "2025-05-06T12:00:00Z", "2025-05-06T18:00:00Z",
    "2025-05-07T00:00:00Z", "2025-05-07T06:00:00Z", "2025-05-07T12:00:00Z", "2025-05-07T18:00:00Z",
    "2025-05-08T00:00:00Z", "2025-05-08T06:00:00Z", "2025-05-08T12:00:00Z", "2025-05-08T18:00:00Z",
    "2025-05-09T00:00:00Z", "2025-05-09T06:00:00Z", "2025-05-09T12:00:00Z", "2025-05-09T18:00:00Z",
    "2025-05-10T00:00:00Z", "2025-05-10T06:00:00Z", "2025-05-10T12:00:00Z", "2025-05-10T18:00:00Z",
    "2025-05-11T00:00:00Z", "2025-05-11T06:00:00Z", "2025-05-11T12:00:00Z", "2025-05-11T18:00:00Z",
    "2025-05-12T00:00:00Z", "2025-05-12T06:00:00Z", "2025-05-12T12:00:00Z", "2025-05-12T18:00:00Z",
    "2025-05-13T00:00:00Z", "2025-05-13T06:00:00Z", "2025-05-13T12:00:00Z", "2025-05-13T18:00:00Z",
    "2025-05-14T00:00:00Z", "2025-05-14T06:00:00Z", "2025-05-14T12:00:00Z", "2025-05-14T18:00:00Z",
    "2025-05-15T00:00:00Z", "2025-05-15T06:00:00Z", "2025-05-15T12:00:00Z", "2025-05-15T18:00:00Z",
    "2025-05-16T00:00:00Z", "2025-05-16T06:00:00Z", "2025-05-16T12:00:00Z", "2025-05-16T18:00:00Z",
    "2025-05-17T00:00:00Z", "2025-05-17T06:00:00Z", "2025-05-17T12:00:00Z", "2025-05-17T18:00:00Z",
    "2025-05-18T00:00:00Z", "2025-05-18T06:00:00Z", "2025-05-18T12:00:00Z", "2025-05-18T18:00:00Z"
).map { Instant.parse(it) }

private val stepCounts = listOf(
    8123f, 523f, 9672f, 7540f,
    6453f, 984f, 8732f, 6891f,
    7215f, 642f, 9321f, 8990f,
    8320f, 885f, 7124f, 9983f,
    6152f, 751f, 8023f, 7654f,
    9472f, 934f, 8820f, 5932f,
    6723f, 653f, 9021f, 7114f,
    5987f, 752f, 8653f, 9411f,
    7840f, 801f, 9192f, 6833f,
    8794f, 912f, 7364f, 9950f,
    9332f, 891f, 9045f, 6021f,
    7981f, 912f, 6740f, 8942f,
    8024f, 992f, 9684f, 7782f,
    6875f, 864f, 8550f, 9333f,
    7121f, 941f, 9821f, 8732f
)

private val timeDataPoint = TimeDataPoint(
    x = isoTime,
    y = stepCounts,
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

private val chartPoints4 = sampleData4.mapIndexed { index, value ->
    ChartPoint(
        x = index.toFloat(),
        y = value,
        label = weekDays[index % weekDays.size]
    )
}

@Composable
fun ExampleUI(modifier: Modifier = Modifier) {
    val chartType = listOf(
        "BarChart 1",
        "BarChart 2",
        "BarChart 3",
        "DonutChart 1",
        "LineChart 1",
        "LineChart 2",
        "LineChart 3",
        "PieChart 1",
        "CalendarChart 1",
        "CalendarChart 2",
        "CalendarChart 3",
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
                "BarChart 3" -> BarChart_3()
                "DonutChart 1" -> DonutChart_1()
                "LineChart 1" -> LineChart_1()
                "LineChart 2" -> LineChart_2()
                "LineChart 3" -> LineChart_3()
                "PieChart 1" -> PieChart_1()
                "CalendarChart 1" -> CalendarChart_1()
                "CalendarChart 2" -> CalendarChart_2()
                "CalendarChart 3" -> CalendarChart_3()
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
        yAxisFixedWidth = 16.dp
    )
}

@Composable
fun BarChart_3() {
    BarChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = chartPoints4,
        title = "Weekly Data",
        barColor = Primary_Purple,
        yAxisPosition = YAxisPosition.RIGHT,
        showLabel = true,
        // paged mode:
        pageSize = 7,
        unifyYAxisAcrossPages = true,
        yTickStepDefaultForPaged = 10f
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
            yAxisPosition = YAxisPosition.RIGHT,
            yAxisFixedWidth = 28.dp,
            yTickStep = 10f
        )
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
fun LineChart_3() {
    LineChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = chartPoints4,
        title = "요일별 활동량",
        lineColor = Primary_Purple,
        strokeWidth = 12f,
        showPoint = false,
        showValue = true,
        yAxisPosition = YAxisPosition.RIGHT,
        // paging:
        pagingEnabled = true,
        pageSize = 7,
        unifyYAxisAcrossPages = true,
        yAxisFixedWidth = 28.dp,
        yTickStep = 10f
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
fun CalendarChart_3() {
    val entriesByMonth = remember(entries) {
        entries.groupBy { YearMonth.from(it.date) }
    }
    val initialYm = YearMonth.from(startDate)

    PagedCalendarChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp),
        initialYearMonth = initialYm,
        entriesForMonth = { ym -> entriesByMonth[ym].orEmpty() },
        color = Primary_Purple,
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
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("시간대별") }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "시간대별 걸음 수",
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
            DropdownMenuItem(
                text = { Text("주별") },
                onClick = {
                    selectedOption = "주별"
                    expanded = false
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        BarChart(
            modifier = Modifier.fillMaxWidth().height(500.dp),
            data = when (selectedOption) {
                "시간대별" -> timeDataPoint.toChartPoints()
                "일별" -> timeDataPoint.transform(
                    timeUnit = TimeUnitGroup.DAY,
                    aggregationType = AggregationType.AVERAGE
                ).toChartPoints()
                "주별" -> timeDataPoint.transform(
                    timeUnit = TimeUnitGroup.WEEK,
                    aggregationType = AggregationType.AVERAGE
                ).toChartPoints()
                else -> timeDataPoint.toChartPoints()
            },
            title = "걸음 수 (${selectedOption}) - ${if (selectedOption == "시간대별") "가로 스크롤" else "Smart Label Reduction"}",
            barColor = Primary_Purple,
            barWidthRatio = 0.5f,
            labelTextSize = 20f,
            // 시간대별일 때는 windowSize로 스크롤링 활성화
            windowSize = when (selectedOption) {
                "시간대별" -> 8
                "일별" -> 4
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