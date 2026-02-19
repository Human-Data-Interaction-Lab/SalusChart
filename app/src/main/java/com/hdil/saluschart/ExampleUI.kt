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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.BaseChartMark
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.InteractionType
import com.hdil.saluschart.core.chart.PointType
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.ReferenceLineSpec
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.core.chart.model.BarCornerRadiusFractions
import com.hdil.saluschart.core.transform.transform
import com.hdil.saluschart.core.transform.transformToChartMark
import com.hdil.saluschart.core.util.AggregationType
import com.hdil.saluschart.core.util.TimeUnitGroup
import com.hdil.saluschart.data.model.model.MassUnit
import com.hdil.saluschart.data.provider.SampleDataProvider
import com.hdil.saluschart.data.provider.SampleDataProvider.getHeartRateData
import com.hdil.saluschart.ui.compose.charts.BarChart
import com.hdil.saluschart.ui.compose.charts.BubbleType
import com.hdil.saluschart.ui.compose.charts.CalendarChart
import com.hdil.saluschart.ui.compose.charts.CellMarkerType
import com.hdil.saluschart.ui.compose.charts.GaugeSegment
import com.hdil.saluschart.ui.compose.charts.HorizontalRangeBarChart
import com.hdil.saluschart.ui.compose.charts.HorizontalStackedBarChartList
import com.hdil.saluschart.ui.compose.charts.HorizontalStackedBarRow
import com.hdil.saluschart.ui.compose.charts.LegendItem
import com.hdil.saluschart.ui.compose.charts.LegendShape
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
import com.hdil.saluschart.ui.compose.charts.MultiSegmentGaugeChart
import com.hdil.saluschart.ui.compose.charts.PagedCalendarChart
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
import com.hdil.saluschart.ui.compose.charts.markerColorForRatio
import com.hdil.saluschart.ui.theme.Orange
import com.hdil.saluschart.ui.theme.Primary_Purple
import com.hdil.saluschart.ui.theme.Teel
import com.hdil.saluschart.ui.theme.Yellow
import java.time.YearMonth
import kotlin.math.abs

// Note: Sample data moved to SampleDataProvider for better organization

// Sample data references - now organized in SampleDataProvider
private val stepCountHealthData = SampleDataProvider.getStepCountData()
private val exerciseHealthData = SampleDataProvider.getExerciseHealthData()
private val heartRateHealthData = SampleDataProvider.getHeartRateData()
private val singleSleepSessionData = SampleDataProvider.getSingleSleepSessionData()
private val weightHealthData = SampleDataProvider.getWeightData()
private val bloodPressureHealthData = SampleDataProvider.getBloodPressureData()
private val bloodGlucoseHealthData = SampleDataProvider.getBloodGlucoseData()
private val bodyFatHealthData = SampleDataProvider.getBodyFatData()
private val rangeData = SampleDataProvider.getHeartRateRangeData()
private val stackedData = SampleDataProvider.getNutritionStackedData()
private val segmentLabels = SampleDataProvider.segmentLabels
private val ChartMarks = SampleDataProvider.getBasicChartMarks()
private val ChartMarks4 = SampleDataProvider.getExtendedChartMarks()


private val yearMonth = YearMonth.now()
private val entries = SampleDataProvider.getCalendarEntries(yearMonth)

@Composable
fun ExampleUI(modifier: Modifier = Modifier) {
    val chartType = listOf(
        "Step Count - Bar Chart",
        "Exercise - Bar Chart",
        "Step Count - Rounded Bar Chart",
        "Exercise - Rounded Bar Chart",
        "Heart Rate - Range Bar Chart",
        "Heart Rate - Range Bar Chart (with dots)",
        "Heart Rate - Line Chart",
        "Sleep Session - Sleep Stage Chart",
        "Weight - Line Chart",
        "Body Fat - Line Chart",
        "Body Fat 2 - Line Chart",
        "Blood Pressure - Scatter Plot",
        "Blood Glucose - Range Bar Chart",
        "Diet - Stacked Bar Chart FreeScroll",
        "Minimal Charts",
        "CalendarChart 1",
        "CalendarChart 2",
        "CalendarChart with Paging",
        "CalendarChart with Activity Rings",
        "PieChart 1",
        "DonutChart 1",
        "Progress Bar Chart",
        "Progress Ring Chart",
        "Sleep Session - Sleep Stage Chart",
        "Sleep Consistency - Horizontal Range Bar Chart",
        "Nutrition - Horizontal Stacked Bar Chart",
        "Heart Rate - Range Gauge Chart",
        "Fitness Level - Multi Segment Gauge Chart"
    )

    var selectedChartType by remember { mutableStateOf<String?>("Diet - Stacked Bar Chart FreeScroll") }

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
                "Step Count - Bar Chart" -> BarChart_StepCount()
                "Exercise - Bar Chart" -> BarChart_Exercise()
                "Step Count - Rounded Bar Chart" -> RoundedBarChart_StepCount()
                "Exercise - Rounded Bar Chart" -> RoundedBarChart_Exercise()
                "Weight - Line Chart" -> LineChart_Weight()
                "Body Fat - Line Chart" -> LineChart_BodyFat()
                "Body Fat 2 - Line Chart" -> LineChart_BodyFat_2()
                "Heart Rate - Line Chart" -> LineChart_HeartRate()
                "Blood Pressure - Scatter Plot" -> ScatterPlot_BloodPressure()
                "Diet - Stacked Bar Chart FreeScroll" -> StackedBarChart_1()
                "Diet - Stacked Bar Chart Paged" -> StackedBarChart_Paged_LeftAxis()
                "Heart Rate - Range Bar Basic Chart" -> RangeBarChart_1()
                "Heart Rate - Range Bar Chart" -> RangeBarChart_HeartRate()
                "Heart Rate - Range Bar Chart (with dots)" -> VerticalRangePlot_HeartRate()
                "Blood Glucose - Range Bar Chart" -> RangeBarChart_BloodGlucose()
                "Minimal Charts" -> Minimal_Chart()
                "CalendarChart 1" -> CalendarChart_1()
                "CalendarChart 2" -> CalendarChart_2()
                "CalendarChart with Paging" -> CalendarChart_3()
                "CalendarChart with Activity Rings" -> CalendarChart_4()
                "PieChart 1" -> PieChart_1()
                "DonutChart 1" -> DonutChart_1()
                "Progress Bar Chart" -> ProgressBarChart_1()
                "Progress Ring Chart" -> ProgressBarChart_2()
                "Sleep Session - Sleep Stage Chart" -> SleepStageChart_1()
                "Sleep Consistency - Horizontal Range Bar Chart" -> HorizontalRangeBarChart()
                "Nutrition - Horizontal Stacked Bar Chart" -> HorizontalStackedBarChart()
                "Heart Rate - Range Gauge Chart" -> RangeGaugeChart()
                "Fitness Level - Multi Segment Gauge Chart" -> MultiSegmentGauge_Fitness()
                else -> Text("Unknown Chart Type")
            }
        }
    }
}

@Composable
fun BarChart_StepCount() {
    var selectedTimeUnit by remember { mutableStateOf("Hour") }
    var expanded by remember { mutableStateOf(false) }

    val timeUnitOptions = listOf("Hour", "Day")

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
                    else -> TimeUnitGroup.HOUR
                },
                aggregationType = when (selectedTimeUnit) {
                    "Hour" -> AggregationType.SUM
                    "Day" -> AggregationType.SUM
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
            xLabelTextSize = 28f,
            tooltipTextSize = 32f,
            interactionType = InteractionType.Bar.TOUCH_AREA,
            pageSize  = when (selectedTimeUnit) {
                "Hour" -> 24
                "Day" -> null
                else -> 12
            },
            unifyYAxisAcrossPages = true,
            yTickStep = when (selectedTimeUnit) {
                "Hour" -> 400.0
                "Day" -> 4000.0
                else -> 1000.0
            },
            maxY = when (selectedTimeUnit) {
                "Hour" -> null
                "Day" -> 24000.0
                else -> null
            }, // 임시 (tooltip 잘 보이기 위해)
            showLabel = false,
            xLabelAutoSkip = true,
            yAxisPosition = YAxisPosition.RIGHT,
        )
    }
}

@Composable
fun BarChart_Exercise() {
    var selectedFillGaps by remember { mutableStateOf("Fill Gaps") }
    var expanded by remember { mutableStateOf(false) }

    val fillGapsOptions = listOf("Fill Gaps", "No Fill Gaps")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Fill Gaps Selection Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "간격 채우기:",
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
                        text = selectedFillGaps,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Fill Gaps Dropdown",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    fillGapsOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedFillGaps = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Exercise Bar Chart
        BarChart(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            data = exerciseHealthData.transform(
                timeUnit = TimeUnitGroup.DAY,
                aggregationType = AggregationType.DURATION_SUM,
                fillGaps = selectedFillGaps == "Fill Gaps"
            ),
            title = "일별 운동 시간 (${if (selectedFillGaps == "Fill Gaps") "간격 채움" else "간격 비움"})",
            barColor = Primary_Purple,
            yAxisPosition = YAxisPosition.LEFT,
            showLabel = true,
            windowSize = 7,
//            pageSize = 7,
            interactionType = InteractionType.Bar.TOUCH_AREA,
            referenceLineType = ReferenceLineType.AVERAGE,
            showReferenceLineLabel = true,
            unit = "분",
        )
    }
}

@Composable
fun RoundedBarChart_StepCount() {
    var selectedTimeUnit by remember { mutableStateOf("Hour") }
    var expanded by remember { mutableStateOf(false) }

    val timeUnitOptions = listOf("Hour", "Day")

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
                    else -> TimeUnitGroup.HOUR
                },
                aggregationType = when (selectedTimeUnit) {
                    "Hour" -> AggregationType.SUM
                    "Day" -> AggregationType.SUM
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
            xLabelTextSize = 28f,
            tooltipTextSize = 32f,
            interactionType = InteractionType.Bar.TOUCH_AREA,
            pageSize  = when (selectedTimeUnit) {
                "Hour" -> 24
                "Day" -> null
                else -> 12
            },
            unifyYAxisAcrossPages = true,
            yTickStep = when (selectedTimeUnit) {
                "Hour" -> 400.0
                "Day" -> 4000.0
                else -> 1000.0
            },
            maxY = when (selectedTimeUnit) {
                "Hour" -> null
                "Day" -> 24000.0
                else -> null
            }, // 임시 (tooltip 잘 보이기 위해)
            showLabel = false,
            xLabelAutoSkip = true,
            yAxisPosition = YAxisPosition.RIGHT,
            barCornerRadiusFractions = BarCornerRadiusFractions(
                topStart = 0.1f,
                topEnd = 0.5f,
                bottomStart = 0.2f,
                bottomEnd = 0.0f
            ),
            barCornerRadiusFraction = 0.5f,
            roundTopOnly = true,
        )
    }
}

@Composable
fun RoundedBarChart_Exercise() {
    var selectedFillGaps by remember { mutableStateOf("Fill Gaps") }
    var expanded by remember { mutableStateOf(false) }

    val fillGapsOptions = listOf("Fill Gaps", "No Fill Gaps")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Fill Gaps Selection Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "간격 채우기:",
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
                        text = selectedFillGaps,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Fill Gaps Dropdown",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    fillGapsOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedFillGaps = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Exercise Bar Chart
        BarChart(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            data = exerciseHealthData.transform(
                timeUnit = TimeUnitGroup.DAY,
                aggregationType = AggregationType.DURATION_SUM,
                fillGaps = selectedFillGaps == "Fill Gaps"
            ),
            title = "일별 운동 시간 (${if (selectedFillGaps == "Fill Gaps") "간격 채움" else "간격 비움"})",
            barColor = Primary_Purple,
            yAxisPosition = YAxisPosition.LEFT,
            showLabel = true,
            windowSize = 7,
//            pageSize = 7,
            interactionType = InteractionType.Bar.TOUCH_AREA,
            referenceLineType = ReferenceLineType.AVERAGE,
            showReferenceLineLabel = true,
            unit = "분",
            barCornerRadiusFractions = BarCornerRadiusFractions(
                topStart = 0.5f,
                topEnd = 0.1f,
                bottomStart = 0.0f,
                bottomEnd = 0.3f
            ),
            barCornerRadiusFraction = 0.5f,
            roundTopOnly = false,
        )
    }
}

@Composable
fun DonutChart_1() {
    PieChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = ChartMarks.subList(0, 4),
        title = "요일별 활동량",
        isDonut = true,
        colors = listOf(Primary_Purple, Teel, Orange, Yellow),
        showLegend = true,
        showLabel = true
    )
}

@Composable
fun LineChart_Weight() {
    var selectedUnit by remember { mutableStateOf("kg") }
    var unitExpanded by remember { mutableStateOf(false) }
    var selectedAutoSkip by remember { mutableStateOf("Auto Skip On") }
    var autoSkipExpanded by remember { mutableStateOf(false) }

    val unitOptions = listOf("kg", "lb")
    val autoSkipOptions = listOf("Auto Skip On", "Auto Skip Off")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Dropdown controls row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unit Selection Dropdown
            Text(
                text = "체중 단위:",
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 16.sp,
                color = Color.Black
            )

            Box {
                Row(
                    modifier = Modifier
                        .clickable { unitExpanded = !unitExpanded }
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
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
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
                                unitExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Auto Skip Selection Dropdown
            Text(
                text = "라벨 자동 생략:",
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 16.sp,
                color = Color.Black
            )

            Box {
                Row(
                    modifier = Modifier
                        .clickable { autoSkipExpanded = !autoSkipExpanded }
                        .background(
                            Color.LightGray.copy(alpha = 0.3f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedAutoSkip,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Auto Skip Dropdown",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = autoSkipExpanded,
                    onDismissRequest = { autoSkipExpanded = false }
                ) {
                    autoSkipOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedAutoSkip = option
                                autoSkipExpanded = false
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
                interactionType = InteractionType.Line.TOUCH_AREA,
                yAxisPosition = YAxisPosition.RIGHT,
                xLabelAutoSkip = selectedAutoSkip == "Auto Skip On"
            )
        }
    }
}

@Composable
fun LineChart_BodyFat() {
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
        showReferenceLineLabel = true,
        referenceLineStyle = LineStyle.DASHDOT,
        windowSize = 8
    )
}

@Composable
fun LineChart_BodyFat_2() {
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
        yAxisPosition = YAxisPosition.LEFT,
        referenceLineType = ReferenceLineType.TREND,
        showReferenceLineLabel = true,
        referenceLineStyle = LineStyle.DASHDOT,
        pageSize = 8
    )
}

@Composable
fun LineChart_HeartRate() {
    LineChart(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        data = heartRateHealthData.transformToChartMark(
            timeUnit = TimeUnitGroup.DAY,
            aggregationType = AggregationType.DAILY_AVERAGE
        ),
        title = "일별 심박수 평균 변화",
        unit = "bpm",
        lineColor = Color(0xFFE91E63),
        showPoint = true,
        minY = 70.0,
        maxY = 90.0,
        interactionType = InteractionType.Line.TOUCH_AREA
    )
}

@Composable
fun PieChart_1() {
    PieChart(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = ChartMarks.subList(0, 4),
        title = "요일별 활동량",
        isDonut = false,
        colors = listOf(Primary_Purple, Teel, Orange, Yellow),
        showLegend = true,
        legendPosition = LegendPosition.RIGHT

    )
}

@Composable
fun CalendarChart_1() {
    val ym = remember(entries) {
        entries.firstOrNull()?.date?.let { YearMonth.from(it) } ?: YearMonth.now()
    }

    CalendarChart(
        modifier = Modifier.fillMaxWidth().height(600.dp),
        entries = entries,
        yearMonth = ym,
        color = Primary_Purple,
        maxBubbleSize = 10f,
        minBubbleSize = 6f
    )
}

@Composable
fun CalendarChart_2() {
    val ym = remember(entries) {
        entries.firstOrNull()?.date?.let { YearMonth.from(it) } ?: YearMonth.now()
    }

    CalendarChart(
        modifier = Modifier.width(300.dp).height(500.dp),
        entries = entries,
        yearMonth = ym,
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
fun CalendarChart_4() {
    val yearMonth = remember { YearMonth.now() }

    val ringEntries = remember(yearMonth) {
        SampleDataProvider.getCalendarActivityRingEntries(yearMonth)
    }

    CalendarChart(
        modifier = Modifier.fillMaxWidth().height(600.dp),
        entries = ringEntries,
        yearMonth = yearMonth,
        color = Primary_Purple,
        maxBubbleSize = 10f,
        minBubbleSize = 6f,
        markerType = CellMarkerType.MINI_RINGS,
        bubbleType = BubbleType.CIRCLE
    )
}



@Composable
fun ScatterPlot_BloodPressure() {
    // Get blood pressure data as a map, then flatten to a single list
    val bloodPressureMap = bloodPressureHealthData.transform(
        timeUnit = TimeUnitGroup.DAY,
        aggregationType = AggregationType.DAILY_AVERAGE
    )

    // Flatten the map into a single list of ChartMarks
    // This allows multiple points (systolic + diastolic) at the same x-axis
    val allBloodPressurePoints = bloodPressureMap.flatMap { (property, ChartMarks) ->
        ChartMarks.map { ChartMark ->
            ChartMark(
                x = ChartMark.x,
                y = ChartMark.y,
                label = "${ChartMark.label}"
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
        interactionType = InteractionType.Scatter.TOUCH_AREA,
        pointType = PointType.Circle,
        pointSize = 4.dp,
        yAxisPosition = YAxisPosition.LEFT,
        windowSize = 14
    )
}

@Composable
fun Minimal_Chart() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp) // prevents last card from being cut off
    ) {
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
                    data = ChartMarks,
                    color = Primary_Purple,
                    referenceLineType = ReferenceLineType.AVERAGE,
//                    barWidthRatio = 0.85f,
//                    barCornerRadiusFraction = 0.6f,
//                    roundTopOnly = false
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
                    data = ChartMarks,
                    color = Primary_Purple,
                    referenceLineType = ReferenceLineType.TREND
                )
            }
        }
    }
    ActivityRingsCard()
        MinimalProgressBarCard()
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
                    barCornerRadiusFraction = 0.6f,
                    roundTopOnly = false,
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
                val singleRangeData = RangeChartMark(
                    x = 0.0,
                    minPoint = ChartMark(x = 0.0, y = 78.0),
                    maxPoint = ChartMark(x = 0.0, y = 104.0),
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
    CardioFitnessMinimalCard()

    SleepMinimalCard()

        MinimalStackedBarCard()

        MinimalMultiSegmentGaugeCard()

    }
}

@Composable
private fun ActivityRingsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: title + 3 columns
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "오늘 활동",
                        color = Color.Black,
                        letterSpacing = 0.2.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                val move = 115
                val exercise = 7
                val stand = 6

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActivityMetricColumn(
                        title = "Move",
                        titleColor = Color(0xFFE53935),
                        value = move.toString(),
                        unit = "kcal",
                        modifier = Modifier.weight(1f)
                    )

                    VerticalDivider()

                    ActivityMetricColumn(
                        title = "Exercise",
                        titleColor = Color(0xFF00C853),
                        value = exercise.toString(),
                        unit = "분",
                        modifier = Modifier.weight(1f)
                    )

                    VerticalDivider()

                    ActivityMetricColumn(
                        title = "Stand",
                        titleColor = Color(0xFF00B0FF),
                        value = stand.toString(),
                        unit = "시간",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // RIGHT: rings
            Box(
                modifier = Modifier
                    .size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                val rings = remember {
                    listOf(
                        ProgressChartMark(x = 0.0, current = 115.0, max = 200.0, label = "Move", unit = "kcal"),
                        ProgressChartMark(x = 1.0, current = 7.0,   max = 30.0,  label = "Exercise", unit = "min"),
                        ProgressChartMark(x = 2.0, current = 6.0,   max = 12.0,  label = "Stand", unit = "h"),
                    )
                }
                val ringColors = remember {
                    listOf(
                        Color(0xFFFF2D55), // move (pink/red)
                        Color(0xFF32D74B), // exercise (green)
                        Color(0xFF00C7FF)  // stand (cyan)
                    )
                }

                val density = LocalDensity.current
                val strokeWidthPx = with(density) { 6.dp.toPx() }

                MiniActivityRings(
                    modifier = Modifier.size(54.dp),
                    rings = rings,
                    colors = ringColors,
                    strokeWidth = strokeWidthPx,
                    maxLaps = 2,
                    trackAlpha = 0.20f,
                    gapRatio = 0.14f,
                    startAngle = -90f
                )
            }
        }
    }
}

@Composable
private fun ActivityMetricColumn(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color,
    value: String,
    unit: String
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = titleColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = unit,
                color = Color(0xFF9B9B9B),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun VerticalDivider(
    height: Dp = 50.dp,
    thickness: Dp = 1.dp,
    sidePadding: Dp = 12.dp
) {
    Box(
        modifier = Modifier
            .padding(horizontal = sidePadding)
            .height(height)
            .width(thickness)
            .background(Color(0xFFE6E6E6))
    )
}

@Composable
fun SleepMinimalCard() {
    // Placeholder values
    val hours = 6
    val mins = 38
    val sleepTeal = Color(0xFF00B7B2)

    val sleepColumns = remember {
        fun main(v: Float) = SleepSegment(v, sleepTeal.copy(alpha = 1.00f))
        fun light(v: Float) = SleepSegment(v, sleepTeal.copy(alpha = 0.85f))
        fun gap(v: Float) = SleepSegment(v, sleepTeal.copy(alpha = 0.65f))

        listOf(
            // Day 1 – long sleep, tiny interruption
            SleepColumn(listOf(
                main(78f),
                gap(12f),
                light(18f)
            )),

            // Day 2 – clean, continuous night
            SleepColumn(listOf(
                main(92f)
            )),

            // Day 3 – slightly fragmented
            SleepColumn(listOf(
                light(22f),
                main(58f),
                gap(12f),
                light(14f)
            )),

            // Day 4 – very solid sleep
            SleepColumn(listOf(
                main(88f)
            )),

            // Day 5 – wake-up in the middle
            SleepColumn(listOf(
                main(46f),
                gap(24f),
                main(34f)
            )),

            // Day 6 – short early wake
            SleepColumn(listOf(
                light(18f),
                main(72f)
            )),

            // Day 7 – moderate sleep
            SleepColumn(listOf(
                main(64f),
                gap(15f),
                light(20f)
            )),
        )
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: title + value (match your other cards)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "오늘 수면",
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = hours.toString(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = " 시간",
                        color = Color(0xFF9B9B9B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = mins.toString(),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = " 분",
                        color = Color(0xFF9B9B9B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )
                }
            }

            // RIGHT: wider chart area
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(60.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                MinimalSleepChart(
                    modifier = Modifier.fillMaxSize(),
                    columns = sleepColumns,
                    barWidthRatio = 0.55f,
                    columnGapRatio = 0.15f,
                    segmentGapRatio = 0.28f,
                    cornerRadiusRatio = 0.95f,
                    trackAlpha = 0f
                )
            }
        }
    }
}

@Composable
fun CardioFitnessMinimalCard() {
    // Example values
    val title = "유산소 피트니스"
    val status = "평균 이하"
    val vo2 = 35.7

    // Ladder position config
    val selectedBand = 1      // 0=top, 1=middle, 2=bottom
    val markerRatio = 0.5f   // 0..1 across the bar

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: text
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = status,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "$vo2 VO₂ max",
                    color = Color(0xFF9B9B9B),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            // RIGHT: ladder chart
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(56.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                MinimalLadderChart(
                    modifier = Modifier.fillMaxSize(),
                    bandCount = 4,
                    selectedBandIndex = selectedBand,
                    markerRatio = markerRatio,
                    trackColor = Color(0xFFEDEDED),
                    selectedColor = Color(0xFFF6A5B6),
                    markerColor = Color(0xFFE91E63)
                )
            }
        }
    }
}

@Composable
fun MinimalStackedBarCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: title / value
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = "스트레스",
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "높음",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "오늘 평균",
                    color = Color(0xFF9B9B9B),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            // RIGHT: the stacked bar + bubble
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(52.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                val segments = remember {
                    listOf(
                        StackedBarSegment(0.35f, Color(0xFF7CE7A0)), // light green
                        StackedBarSegment(0.45f, Color(0xFF00C853)), // green
                        StackedBarSegment(0.10f, Color(0xFFFF8A3D)), // orange
                        StackedBarSegment(0.10f, Color(0xFFEFEFEF))  // remainder-ish
                    )
                }

                MinimalHorizontalStackedBar(
                    modifier = Modifier.fillMaxSize(),
                    segments = segments,
                    trackColor = Color(0xFFEDEDED),
                    selectedSegmentIndex = 2, // points to orange
                    label = "높음",
                    bubbleColor = Color(0xFFD6F5DF),
                    bubbleTextColor = Color(0xFF0A7A32),
                    barHeight = 12.dp
                )
            }
        }
    }
}

@Composable
fun MinimalProgressBarCard() {
    val progress = 0.78f
    val title = "오늘 걸음 수"
    val valueText = "4,680"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: title / value
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = valueText,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "/6,000 걸음",
                    color = Color(0xFF9B9B9B),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            // RIGHT: progress bar + bubble
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                MinimalProgressBar(
                    modifier = Modifier.fillMaxSize(),
                    progress = progress,
                    label = "78%",
                    trackColor = Color(0xFFEDEDED),
                    fillColor = Color(0xFF00C853),
                    bubbleColor = Color(0xFFD6F5DF),
                    bubbleTextColor = Color(0xFF0A7A32),
                    barHeight = 12.dp
                )
            }
        }
    }
}

@Composable
fun MinimalMultiSegmentGaugeCard() {
    // Dummy example values
    val title = "최종당화산물 지수"
    val status = "보통"
    val desc = "오늘 기준"

    // Marker position (0..1)
    val markerRatio = 0.35f

    val segments = remember {
        listOf(
            MinimalGaugeSegment(0.25f, Color(0xFF10A5F5)), // blue
            MinimalGaugeSegment(0.25f, Color(0xFF8BEA3B)), // green
            MinimalGaugeSegment(0.25f, Color(0xFFFFC629)), // yellow
            MinimalGaugeSegment(0.25f, Color(0xFFFF8A1D))  // orange
        )
    }

    val markerColor = remember(markerRatio, segments) {
        markerColorForRatio(markerRatio, segments)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT text area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    letterSpacing = 0.2.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = status,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = desc,
                    color = Color(0xFF9B9B9B),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            // RIGHT gauge
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(52.dp)
                    .padding(top = 32.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                MinimalMultiSegmentGauge(
                    modifier = Modifier.fillMaxSize(),
                    segments = segments,
                    markerRatio = markerRatio,
                    barHeight = 14.dp,
                    markerWidth = 36.dp,
                    markerHeight = 20.dp,
                    markerColor = markerColor
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
        yAxisFixedWidth = 10.dp,
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
        yTickStep = 20.0,
        yAxisPosition = YAxisPosition.LEFT,
        interactionType = InteractionType.StackedBar.BAR,
        colors = listOf(
            Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFF4CAF50)
        ),
        unit = "g"
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
fun RangeBarChart_HeartRate() {
    val marks: List<BaseChartMark> = heartRateHealthData.transform(
        timeUnit = TimeUnitGroup.HOUR,
        aggregationType = AggregationType.MIN_MAX
    )

    val rangeMarks = remember(marks) { marks.filterIsInstance<RangeChartMark>() }

    RangeBarChart(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = rangeMarks,
        title = "시간별 심박수 범위",
        yLabel = "심박수 (bpm)",
        xLabel = "시간",
        barWidthRatio = 0.8f,
        barColor = Color(0xFFE91E63),
        interactionType = InteractionType.RangeBar.TOUCH_AREA,
        unit = "bpm",
        pageSize = 24,
        referenceLines = listOf(
            ReferenceLineSpec(
                y = 120.0,
                label = "120",
                color = Color(0xFFFF7A00),
                strokeWidth = 2.dp,
                style = LineStyle.DASHED
            ),
            ReferenceLineSpec(
                y = 70.0,
                label = "70",
                color = Color(0xFFFF7A00),
                strokeWidth = 2.dp,
                style = LineStyle.DASHED
            )
        )
    )
}

@Composable
fun VerticalRangePlot_HeartRate() {
    val marks: List<BaseChartMark> = heartRateHealthData.transform(
        timeUnit = TimeUnitGroup.HOUR,
        aggregationType = AggregationType.MIN_MAX
    )

    val rangeMarks: List<RangeChartMark> = remember(marks) {
        marks.filterIsInstance<RangeChartMark>()
    }

    val allHeartRateBlocks = remember { getHeartRateData() }

    val allSamples = remember(allHeartRateBlocks) {
        allHeartRateBlocks.flatMap { it.samples }
    }

    val pointValues = remember(allSamples, rangeMarks) {

        val sortedSamples = allSamples.sortedBy { it.time }
        val grouped = sortedSamples
            .mapIndexed { index, sample ->
                (index / 4) to sample.beatsPerMinute.toDouble() // 4 samples per hour → group into hourly buckets
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )

        List(rangeMarks.size) { hourIndex ->
            grouped[hourIndex] ?: emptyList()
        }
    }

    RangeBarChart(
        modifier = Modifier.fillMaxWidth().height(400.dp),
        data = rangeMarks,
        title = "심박수 범위 (일간)",
        yLabel = "심박수 (bpm)",
        xLabel = "시간",
        barWidthRatio = 0.45f,
        barColor = Color(0xFFE91E63),
        interactionType = InteractionType.RangeBar.TOUCH_AREA,
        unit = "bpm",
        pageSize = 24,
        pointValues = pointValues,
        pointColor = Color(0xFFE91E63),
        pointRadius = 3.dp,
        barCornerRadiusFraction = 0.5f,
        roundTopOnly = false,
    )
}

@Composable
fun RangeBarChart_BloodGlucose() {
    val marks: List<BaseChartMark> = bloodGlucoseHealthData.transform(
        timeUnit = TimeUnitGroup.DAY,
        aggregationType = AggregationType.MIN_MAX
    )

    val rangeMarks = remember(marks) { marks.filterIsInstance<RangeChartMark>() }

    RangeBarChart(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        data = rangeMarks,
        title = "일별 혈당 범위",
        legendItems = listOf(
            LegendItem(label = "목표 수면 시간대", color = Color(0xFFFF7A00), shape = LegendShape.Box),
            LegendItem(label = "범위(최소~최대)", color = Color(0xFF4CAF50), shape = LegendShape.Dot),
        ),
        barWidthRatio = 0.8f,
        barColor = Color(0xFF4CAF50),
        interactionType = InteractionType.RangeBar.TOUCH_AREA,
        unit = "mg/dL",
        windowSize = 7,
        barCornerRadiusFraction = 0.3f,
        roundTopOnly = false,
        referenceLines = listOf(
            ReferenceLineSpec(
                y = 62.0,
                label = "62",
                color = Color(0xFFFF7A00),
                strokeWidth = 2.dp,
                style = LineStyle.DASHED
            ),
            ReferenceLineSpec(
                y = 42.0,
                label = "42",
                color = Color(0xFFFF7A00),
                strokeWidth = 2.dp,
                style = LineStyle.DASHED
            )
        )
    )
}

@Composable
fun RangeBarChart_FreeScroll_FixedAxis() {
    RangeBarChart(
        modifier = Modifier.fillMaxWidth().height(400.dp),
        data = rangeData,
        title = "Free-scroll + Fixed Y-Axis",
        windowSize = 7,
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
        yTickStep = 10.0,
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
        yTickStep = 10.0,
        yAxisPosition = YAxisPosition.RIGHT,
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
        strokeWidth = 80.dp,
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
        strokeWidth = 20.dp,
        showLegend = true,
        showValues = false,
        // tooltipEnabled = false,
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
        val denseChartMarks = SampleDataProvider.getDenseChartMarks(50)

        Text(
            text = "Without Tick Reduction (50 labels)",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BarChart(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            data = denseChartMarks,
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
            data = denseChartMarks,
            title = "Dense Data - Reduced Labels (Clean)",
            barColor = Primary_Purple,
            barWidthRatio = 0.8f,
            xLabelTextSize = 20f,
            maxXTicksLimit = 10 // Limit to 10 labels
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Test 3: Auto-skip feature
        Text(
            text = "With Auto-Skip (Automatic Width-Based)",
            fontSize = 16.sp,
            color = Color.Blue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BarChart(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            data = denseChartMarks,
            title = "Dense Data - Auto-Skip (Width-Based)",
            barColor = Color.Blue,
            barWidthRatio = 0.8f,
            xLabelTextSize = 20f,
            xLabelAutoSkip = true // Enable auto-skip based on text width
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
        showXAxis = true,
        showYAxis = true,
        onStageClick = { index, tooltipText ->
            // Handle stage click if needed
        },
        barHeightRatio = 0.6f,
        yAxisPosition = YAxisPosition.LEFT,
        showStartEndLabels = true
    )
}

@Composable
fun HorizontalRangeBarChart() {
    val rangeMarks = remember {
        (3..9).map { day ->
            RangeChartMark(
                x = day.toDouble(),
                minPoint = com.hdil.saluschart.core.chart.ChartMark(x = day.toDouble(), y = 1.0 + (day % 3) * 0.5, label = "$day"),
                maxPoint = com.hdil.saluschart.core.chart.ChartMark(x = day.toDouble(), y = 7.5 - (day % 2) * 0.3, label = "$day"),
                label = day.toString()
            )
        }
    }

    HorizontalRangeBarChart(
        title = "수면 규칙성",
        datePeriodText = "11월 3일 - 9일",
        data = rangeMarks,
        minX = 0.0,
        maxX = 8.0,
        rowLabels = listOf("3","4","5","6","7","8","9"),
        bottomStartLabel = "오전\n12:00",
        bottomEndLabel = "오전\n8:00",
        isGood = { mark ->
            // example: treat duration >= 6h as good
            val dur = abs(mark.maxPoint.y - mark.minPoint.y)
            dur >= 6.0
        }
    )

}

@Composable
fun HorizontalStackedBarChart() {
    val rows = remember {
        listOf(
            HorizontalStackedBarRow(
                title = "탄수화물",
                unit = "g",
                total = 287.4f,
                segments = listOf(131.0f, 87.3f, 40.1f),
                segmentLabels = listOf("부대찌개", "토마토파스타", "붕어빵"),
                trackMax = 300f
            ),

                    HorizontalStackedBarRow(
                title = "포화 지방",
                unit = "g",
                total = 18.4f,
                segments = listOf(15.2f, 2.6f, 0.6f),
                segmentLabels = listOf("부대찌개", "붕어빵", "토마토파스타"),
                trackMax = 45f
            ),
            HorizontalStackedBarRow(
                title = "나트륨",
                unit = "mg",
                total = 3775.3f,
                segments = listOf(2913f, 672.3f, 190f),
                segmentLabels = listOf("부대찌개", "토마토파스타", "붕어빵"),
                trackMax = 5000f
            )
        )
    }

    HorizontalStackedBarChartList(
        title = "영양정보",
        datePeriodText = "11월 3일 - 9일",
        rows = rows,
        onRowClick = { _, _, _ -> }
    )
}

@Composable
fun RangeGaugeChart() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        RangeGaugeChart(
            minValue = 40f,
            maxValue = 120f,
            rangeStart = 54f,
            rangeEnd = 98f,
            recentValue = 75f,
            unit = "bpm",
            recentLabel = "최근기록 오후 3:40",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MultiSegmentGauge_Fitness() {
    val segments = listOf(
        GaugeSegment(0f, 0f, Color(0xFF0AA7FF)),
        GaugeSegment(0f, 0f, Color(0xFF7ADB2A)),
        GaugeSegment(0f, 0f, Color(0xFFFFC400)),
        GaugeSegment(0f, 0f, Color(0xFFFF8A3D))
    )

    MultiSegmentGaugeChart(
        title = "적절함",
        value = 342f,
        minValue = 120f,
        maxValue = 646f,
        segments = segments,

        tickValues = listOf(120f, 302f, 334f, 516f, 646f),

        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun BarChartPreview() {
    StackedBarChart_1()
}