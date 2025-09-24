package com.hdil.saluschart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.MassUnit
import com.hdil.saluschart.data.provider.SampleDataProvider
import com.hdil.saluschart.ui.compose.charts.BarChart
import com.hdil.saluschart.ui.compose.charts.BubbleType
import com.hdil.saluschart.ui.compose.charts.CalendarChart
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
import com.hdil.saluschart.ui.compose.charts.SleepStageChart
import com.hdil.saluschart.ui.compose.charts.StackedBarChart
import com.hdil.saluschart.ui.theme.Orange
import com.hdil.saluschart.ui.theme.Primary_Purple
import com.hdil.saluschart.ui.theme.Teel
import com.hdil.saluschart.ui.theme.Yellow
import java.time.YearMonth

// Note: Sample data moved to SampleDataProvider for better organization

// Sample data references - now organized in SampleDataProvider
private val stepCountHealthData = SampleDataProvider.getStepCountData()
private val singleSleepSessionData = SampleDataProvider.getSingleSleepSessionData()
private val weightHealthData = SampleDataProvider.getWeightData()
private val bloodPressureHealthData = SampleDataProvider.getBloodPressureData()
private val bodyFatHealthData = SampleDataProvider.getBodyFatData()
private val rangeData = SampleDataProvider.getHeartRateRangeData()
private val stackedData = SampleDataProvider.getNutritionStackedData()
private val segmentLabels = SampleDataProvider.segmentLabels
private val chartPoints = SampleDataProvider.getBasicChartPoints()
private val chartPoints4 = SampleDataProvider.getExtendedChartPoints()


private val yearMonth = YearMonth.now()
private val entries = SampleDataProvider.getCalendarEntries(yearMonth)

@Composable
fun ExampleUI(modifier: Modifier = Modifier) {
    val chartType = listOf(
        "Progress Bar Chart",
        "Progress Ring Chart",
        "Diet - Stacked Bar Chart FreeScroll",
        "Diet - Stacked Bar Chart Paged",
        "Heart Rate - Range Bar Basic Chart",
        "Heart Rate - Range Bar FreeScroll Fixed Axis",
        "Heart Rate - Range Bar Paged (Left Fixed)",
        //"Standard Bar Chart",
        //"BarChart with Paging",
        //"BarChart 3",
        //"Standard Line Chart",
        //"LineChart with Paging",
        //"CalendarChart 1",
        //"CalendarChart 2",
        //"Heart Rate - Range Bar Paged (Right Fixed)",
        //"Step Count - Bar Chart",
        //"Weight - Line Chart",
        //"Body Fat - Line Chart",
        //"Blood Pressure - Scatter Plot",
        //"CalendarChart with Paging",
        //"Minimal Charts",
        //"PieChart 1",
        //"DonutChart 1",
        "Sleep Stage Chart",
        "Sleep Stage Chart - FreeScroll Fixed Axis",
    )

    var selectedChartType by remember { mutableStateOf<String?>("Sleep Stage Chart") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (selectedChartType == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                chartType.forEach { type ->
                    Text(
                        text = type,
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable { selectedChartType = type }
                    )
                }
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
                "Standard Bar Chart" -> BarChart_1()
                "Step Count - Bar Chart" -> BarChart_2()
                "BarChart with Paging" -> BarChart_3()
                "LineChart with Paging" -> LineChart_3()
                "Standard Line Chart" -> LineChart_3()
                "Weight - Line Chart" -> LineChart_1()
                "Body Fat - Line Chart" -> LineChart_2()
                "Blood Pressure - Scatter Plot" -> ScatterPlot_1()
                "Diet - Stacked Bar Chart FreeScroll" -> StackedBarChart_1()
                "Diet - Stacked Bar Chart Paged" -> StackedBarChart_Paged_LeftAxis()
                "Heart Rate - Range Bar Basic Chart" -> RangeBarChart_1()
                "Heart Rate - Range Bar FreeScroll Fixed Axis" -> RangeBarChart_FreeScroll_FixedAxis()
                "Heart Rate - Range Bar Paged (Left Fixed)", -> RangeBarChart_Paged_LeftAxis()
                "Heart Rate - Range Bar Paged (Right Fixed)", -> RangeBarChart_Paged_RightAxis()
                "Minimal Charts" -> Minimal_Chart()
                "CalendarChart 1" -> CalendarChart_1()
                "CalendarChart 2" -> CalendarChart_2()
                "CalendarChart with Paging" -> CalendarChart_3()
                "PieChart 1" -> PieChart_1()
                "DonutChart 1" -> DonutChart_1()
                "Progress Bar Chart" -> ProgressBarChart_1()
                "Progress Ring Chart" -> ProgressBarChart_2()
                "Sleep Stage Chart" -> SleepStageChart_1()
                "Sleep Stage Chart - FreeScroll Fixed Axis" -> SleepStageChart_2()
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
        title = "요일별 데이터",
        barColor = Primary_Purple,
        maxY = 70.0,
        barWidthRatio = 0.8f,
        xLabelTextSize = 40f,
        tooltipTextSize = 5f,
        interactionType = InteractionType.Bar.BAR,
        yAxisPosition = YAxisPosition.LEFT,
        showLabel = true,
        referenceLineType = ReferenceLineType.AVERAGE,
        referenceLineStyle = LineStyle.DASHED,
        showReferenceLineLabel = false,
        referenceLineInteractive = true,
    )
}

@Composable
fun BarChart_2() {
    var selectedTimeUnit by remember { mutableStateOf("Hour") }
    var expanded by remember { mutableStateOf(false) }

    val timeUnitOptions = listOf("Hour", "Day", "Week")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Time Unit Selection Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "시간 단위:",
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 16.sp,
                color = Color.Black
            )

            Box {
                Row(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .background(
                            Color.LightGray.copy(alpha = 0.3f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTimeUnit,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Time Unit Dropdown",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    timeUnitOptions.forEach { timeUnit ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = timeUnit,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedTimeUnit = timeUnit
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Step Count Bar Chart
        BarChart(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            data = stepCountHealthData.transform(
                timeUnit = when (selectedTimeUnit) {
                    "Hour" -> TimeUnitGroup.HOUR
                    "Day" -> TimeUnitGroup.DAY
                    "Week" -> TimeUnitGroup.WEEK
                    else -> TimeUnitGroup.HOUR
                },
                aggregationType = when (selectedTimeUnit) {
                    "Hour" -> AggregationType.SUM
                    "Day" -> AggregationType.DAILY_AVERAGE
                    "Week" -> AggregationType.SUM
                    else -> AggregationType.SUM
                }
            ),
            unit = "걸음",
            xLabel = when (selectedTimeUnit) {
                "Hour" -> "시간"
                "Day" -> "날짜"
                else -> "시간"
            },
            yLabel = "걸음수",
            title = "걸음수 데이터 ($selectedTimeUnit 단위)",
            barColor = Primary_Purple,
            barWidthRatio = 0.8f,
            xLabelTextSize = 15f,
            tooltipTextSize = 32f,
            interactionType = InteractionType.Bar.TOUCH_AREA,
            pageSize  = when (selectedTimeUnit) {
                "Hour" -> 24
                "Day" -> null
                "Week" -> null
                else -> 12
            },
            unifyYAxisAcrossPages = true,
            yTickStepDefaultForPaged = when (selectedTimeUnit) {
                "Hour" -> 400.0
                "Day" -> 4000.0
                "Week" -> 10000.0
                else -> 1000.0
            },
            maxY = when (selectedTimeUnit) {
                "Hour" -> null
                "Day" -> 24000.0
                "Week" -> null
                else -> null
            }, // 임시 (tooltip 잘 보이기 위해)
            showLabel = false,
            yAxisPosition = YAxisPosition.RIGHT,
            referenceLineType = ReferenceLineType.AVERAGE,
            referenceLineStyle = LineStyle.DASHED,
            showReferenceLineLabel = false,
            yAxisFixedWidth = 16.dp
        )
    }
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
        yTickStepDefaultForPaged = 10.0
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
    var selectedUnit by remember { mutableStateOf("kg") }
    var expanded by remember { mutableStateOf(false) }

    val unitOptions = listOf("kg", "lb")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Unit Selection Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "체중 단위:",
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 16.sp,
                color = Color.Black
            )

            Box {
                Row(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .background(
                            Color.LightGray.copy(alpha = 0.3f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedUnit,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Unit Dropdown",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    unitOptions.forEach { unit ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = unit,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedUnit = unit
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Weight Chart
        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            LineChart(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                data = weightHealthData.transform(
                    massUnit = when (selectedUnit) {
                        "kg" -> MassUnit.KILOGRAM
                        "lb" -> MassUnit.POUND
                        else -> MassUnit.KILOGRAM
                    },
                    timeUnit = TimeUnitGroup.DAY,
                    aggregationType = AggregationType.DAILY_AVERAGE
                ),
                title = "일별 체중 변화",
                unit = selectedUnit,
                yLabel = "체중 ($selectedUnit)",
                xLabel = "날짜",
                minY = when (selectedUnit) {
                    "kg" -> 50.0
                    "lb" -> 110.0
                    else -> 50.0
                },
                lineColor = Primary_Purple,
                strokeWidth = 12f,
                showPoint = true,
                pointRadius = Pair(6.dp, 3.dp),
                showValue = false,
                windowSize = 7,
                interactionType = InteractionType.Line.POINT,
                yAxisPosition = YAxisPosition.RIGHT
            )
        }
    }
}

@Composable
fun LineChart_2() {
    LineChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = bodyFatHealthData.transform(
            timeUnit = TimeUnitGroup.DAY,
            aggregationType = AggregationType.SUM
        ),
        title = "일별 체지방률 변화",
        unit = "%",
        yLabel = "활동량",
        xLabel = "요일",
        minY = 10.0,
        maxY = 30.0,
        lineColor = Primary_Purple,
        showPoint = true,
        pointRadius = Pair(8.dp, 4.dp),
        strokeWidth = 4f,
        interactionType = InteractionType.Line.TOUCH_AREA,
        yAxisPosition = YAxisPosition.RIGHT,
        referenceLineType = ReferenceLineType.TREND,
        referenceLineStyle = LineStyle.DASHDOT,
        windowSize = 8,
        autoFixYAxisOnScroll = true
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
        yTickStep = 10.0
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
    val initialYm = YearMonth.now()

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
    // Get blood pressure data as a map, then flatten to a single list
    val bloodPressureMap = bloodPressureHealthData.transform(
        timeUnit = TimeUnitGroup.DAY,
        aggregationType = AggregationType.DAILY_AVERAGE
    )
    
    // Flatten the map into a single list of ChartPoints
    // This allows multiple points (systolic + diastolic) at the same x-axis
    val allBloodPressurePoints = bloodPressureMap.flatMap { (property, chartPoints) ->
        chartPoints.map { chartPoint ->
            ChartPoint(
                x = chartPoint.x,
                y = chartPoint.y,
                label = "${chartPoint.label}"
            )
        }
    }
    
    ScatterPlot(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = allBloodPressurePoints,
        pointColor = Primary_Purple,
        title = "혈압 데이터 (수축기 + 이완기)",
        unit = "mmHg",
        yLabel = "혈압 (mmHg)",
        xLabel = "일자",
        interactionType = InteractionType.Scatter.POINT,
        pointType = PointType.Circle,
        pointSize = 4.dp,
        yAxisPosition = YAxisPosition.LEFT,
        windowSize = 14
    )
}

@Composable
fun Minimal_Chart() {
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
                    referenceLineType = ReferenceLineType.AVERAGE
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
                    referenceLineType = ReferenceLineType.TREND
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
                    x = 0.0,
                    minPoint = ChartPoint(x = 0.0, y = 78.0),
                    maxPoint = ChartPoint(x = 0.0, y = 104.0),
                    label = "Heart Rate"
                )
                MinimalGaugeChart(
                    data = singleRangeData,
                    containerMin = 60.0,  // 정상 심박수 범위 시작
                    containerMax = 120.0, // 정상 심박수 범위 끝
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
        barWidthRatio = 0.8f,
        windowSize = 3,
        legendPosition = LegendPosition.BOTTOM,
        yAxisPosition = YAxisPosition.RIGHT,
        interactionType = InteractionType.StackedBar.TOUCH_AREA,
        colors = listOf(
            Color(0xFF2196F3), // 파랑 (단백질)
            Color(0xFFFF9800), // 주황 (지방)
            Color(0xFF4CAF50)  // 초록 (탄수화물)
        ),
        unit = "g"
    )
}

@Composable
fun StackedBarChart_Paged_LeftAxis() {
    StackedBarChart(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = stackedData,
        segmentLabels = segmentLabels,
        title = "요일별 영양소 (Paged + Fixed Left Axis)",
        yLabel = "영양소 (g)",
        xLabel = "요일",
        showLegend = true,
        legendPosition = LegendPosition.BOTTOM,
        barWidthRatio = 0.8f,
        pageSize = 4,
        unifyYAxisAcrossPages = true,
        yTickStepDefaultForPaged = 20.0,
        yAxisPosition = YAxisPosition.LEFT,
        yAxisFixedWidth = 24.dp,
        interactionType = InteractionType.StackedBar.BAR,
        colors = listOf(
            Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFF4CAF50)
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
        barWidthRatio = 0.8f,
        barColor = Color(0xFFFF9800),
        interactionType = InteractionType.RangeBar.TOUCH_AREA,
        unit = "bpm"
    )
}

@Composable
fun RangeBarChart_FreeScroll_FixedAxis() {
    RangeBarChart(
        modifier = Modifier.fillMaxWidth().height(400.dp),
        data = rangeData,
        title = "Free-scroll + Fixed Y-Axis",
        windowSize = 7,
        fixedYAxis = true,
        yAxisFixedWidth = 24.dp,
        yTickStep = 10.0,
        barWidthRatio = 0.7f,
        interactionType = InteractionType.RangeBar.BAR, // bars themselves are tappable
        unit = "bpm"
    )
}

@Composable
fun RangeBarChart_Paged_LeftAxis() {
    RangeBarChart(
        modifier = Modifier.fillMaxWidth().height(400.dp),
        data = rangeData,
        title = "Paged + Fixed Left Y-Axis",
        pageSize = 7,
        unifyYAxisAcrossPages = true,
        yTickStepDefaultForPaged = 10.0,
        yAxisFixedWidth = 24.dp,
        barWidthRatio = 0.75f,
        interactionType = InteractionType.RangeBar.TOUCH_AREA,
        unit = "bpm"
    )
}

@Composable
fun RangeBarChart_Paged_RightAxis() {
    RangeBarChart(
        modifier = Modifier.fillMaxWidth().height(400.dp),
        data = rangeData,
        title = "Paged + Fixed Right Y-Axis",
        pageSize = 7,
        unifyYAxisAcrossPages = true,
        yTickStepDefaultForPaged = 10.0,
        yAxisPosition = YAxisPosition.RIGHT,
        yAxisFixedWidth = 24.dp,
        barWidthRatio = 0.75f,
        interactionType = InteractionType.RangeBar.BAR,
        unit = "bpm"
    )
}

@Composable
fun ProgressBarChart_1() {
    val progressData = SampleDataProvider.getActivityProgressData()
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
        strokeWidth = 80f,
    )
}

@Composable
fun ProgressBarChart_2() {
    val progressData = SampleDataProvider.getActivityProgressData()
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
        strokeWidth = 60f,
        showLegend = true,
        showValues = false
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
        val denseChartPoints = SampleDataProvider.getDenseChartPoints(50)
        
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
            xLabelTextSize = 20f, // Normal size to show overlap
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
            xLabelTextSize = 20f,
            maxXTicksLimit = 10 // Limit to 10 labels
        )
    }
}

@Composable
fun TimeStepBarChart() {
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
                "시간대별" -> stepCountHealthData.transform(
                    timeUnit = TimeUnitGroup.HOUR,
                    aggregationType = AggregationType.SUM
                )
                "일별" -> stepCountHealthData.transform(
                    timeUnit = TimeUnitGroup.DAY,
                    aggregationType = AggregationType.SUM
                )
                else -> stepCountHealthData.transform(
                    timeUnit = TimeUnitGroup.HOUR,
                    aggregationType = AggregationType.SUM
                )
            },
            title = "걸음 수 (${selectedOption}) - ${if (selectedOption == "30분대별") "Raw Data" else "Aggregated"}",
            barColor = Primary_Purple,
            barWidthRatio = 0.5f,
            xLabelTextSize = 20f,
            // 30분대별과 시간대별일 때는 windowSize로 스크롤링 활성화
            windowSize = when (selectedOption) {
                "30분대별" -> 10
                "시간대별" -> 8
                else -> null
            }
        )
    }
}

@Composable
fun SleepStageChart_1() {
    SleepStageChart(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        sleepSession = singleSleepSessionData,
        title = "Sleep Stage Analysis",
        showLabels = true,
        onStageClick = { index, tooltipText ->
            // Handle stage click if needed
        },
        barHeightRatio = 0.6f,
        fixedYAxis = true,
        yAxisPosition = YAxisPosition.LEFT
    )
}

@Composable
fun SleepStageChart_2() {
    SleepStageChart(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        sleepSession = singleSleepSessionData,
        title = "Sleep Stage Analysis",
        showLabels = true,
        onStageClick = { index, tooltipText ->
            // Handle stage click if needed
        },
        barHeightRatio = 0.8f,
        fixedYAxis = true,
        yAxisPosition = YAxisPosition.LEFT,
        windowSize = 8,
        autoFixYAxisOnScroll = true
    )
}