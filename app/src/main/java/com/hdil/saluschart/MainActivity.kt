package com.hdil.saluschart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.ui.compose.charts.LineChart
import com.hdil.saluschart.ui.compose.charts.ScatterPlot
import com.hdil.saluschart.ui.compose.charts.BarChart
import com.hdil.saluschart.ui.compose.charts.CalendarChart
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import com.hdil.saluschart.ui.compose.charts.PieChart
import com.hdil.saluschart.ui.compose.charts.RangeBarChart
import com.hdil.saluschart.ui.compose.charts.StackedBarChart
import com.hdil.saluschart.core.chart.StackedChartMark
import java.time.LocalDate
import java.time.YearMonth
import com.hdil.saluschart.core.chart.ProgressChartMark
import com.hdil.saluschart.ui.compose.charts.ProgressChart
import com.hdil.saluschart.ui.compose.charts.MinimalBarChart
import com.hdil.saluschart.ui.compose.charts.MinimalLineChart
import com.hdil.saluschart.ui.compose.charts.MinimalRangeBarChart
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.ui.compose.charts.MinimalGaugeChart
import com.hdil.saluschart.ui.theme.SalusChartTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.core.chart.InteractionType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalusChartTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    SampleCharts(
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
                Surface {
                    ExampleUI(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

//
//@Composable
//fun SampleCharts(modifier: Modifier = Modifier) {
//    // 차트 타입 선택 상태 관리
//    var selectedChartType by remember { mutableStateOf("Line") }
//
//    // 프로그레스 차트 모드 선택 상태 관리 (도넛 vs 바)
//    var isProgressDonut by remember { mutableStateOf(true) }
//
////    // 현재 연월과 랜덤 데이터 생성
//    val yearMonth = YearMonth.now()
////    val random = java.util.Random(0)
////    val entries = (1..28).map { day ->
////        val date = yearMonth.atDay(day)
////        val value = random.nextFloat() * 100
////        CalendarEntry(
////            date = date,
////            value = value,
////            color = if (random.nextBoolean()) null else Color.Green
////        )
////    }
//    val startDate = LocalDate.of(yearMonth.year, 6, 1)
//    val endDate = LocalDate.of(yearMonth.year, 7, 15)
//    val random = java.util.Random(0)
//    val entries = generateSequence(startDate) { date ->
//        if (date.isBefore(endDate)) date.plusDays(1) else null
//    }.map { date ->
//        val value = random.nextFloat() * 100
//        CalendarEntry(
//            date = date,
//            value = value,
//            color = if (random.nextBoolean()) null else Color.Green
//        )
//    }.toList()
//
//    // 기본적인 raw 데이터로 차트 그리기
//    val sampleData = listOf(
//        12453f, 8932f, 15742f, 6420f, 10238f,
//        19837f, 13429f, 17845f, 5620f, 14672f,
//        9821f, 19382f, 11840f, 13752f, 8954f,
//        16420f, 15237f, 18754f, 10023f, 7654f,
//        13548f, 19845f, 14562f, 17420f, 9237f,
//        19234f, 11029f, 15345f, 18329f, 6021f
//    )
//    val days = listOf(
//        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
//        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
//        "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"
//    )
//
//    Column(
//        modifier = modifier.padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        // 차트 타입 선택 토글 버튼
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            SingleChoiceSegmentedButtonRow {
//                SegmentedButton(
//                    selected = selectedChartType == "Line",
//                    onClick = { selectedChartType = "Line" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 8),
//                    label = { Text("Line", fontSize = 7.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "Scatter",
//                    onClick = { selectedChartType = "Scatter" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 8),
//                    label = { Text("Scatter", fontSize = 6.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "Bar",
//                    onClick = { selectedChartType = "Bar" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 8),
//                    label = { Text("Bar", fontSize = 8.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "Stacked",
//                    onClick = { selectedChartType = "Stacked" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 3, count = 8),
//                    label = { Text("Stacked", fontSize = 6.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "Range",
//                    onClick = { selectedChartType = "Range" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 4, count = 8),
//                    label = { Text("Range", fontSize = 7.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "Pie",
//                    onClick = { selectedChartType = "Pie" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 5, count = 8),
//                    label = { Text("Pie", fontSize = 8.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "Progress",
//                    onClick = { selectedChartType = "Progress" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 6, count = 8),
//                    label = { Text("Progress", fontSize = 5.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "Calendar",
//                    onClick = { selectedChartType = "Calendar" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 7, count = 8),
//                    label = { Text("Calendar", fontSize = 6.sp) }
//                )
//            }
//        }
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            SingleChoiceSegmentedButtonRow {
//                SegmentedButton(
//                    selected = selectedChartType == "MinBar",
//                    onClick = { selectedChartType = "MinBar" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4),
//                    label = { Text("Minimal Bar", fontSize = 7.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "MinLine",
//                    onClick = { selectedChartType = "MinLine" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4),
//                    label = { Text("Minimal Line", fontSize = 7.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "MinRange",
//                    onClick = { selectedChartType = "MinRange" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4),
//                    label = { Text("Minimal Range", fontSize = 7.sp) }
//                )
//                SegmentedButton(
//                    selected = selectedChartType == "MinGauge",
//                    onClick = { selectedChartType = "MinGauge" },
//                    shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4),
//                    label = { Text("Minimal Gauge", fontSize = 6.sp) }
//                )
//            }
//        }
//
//        // 프로그레스 차트 모드 토글 (프로그레스 차트 선택 시에만 표시)
//        if (selectedChartType == "Progress") {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
//            ) {
//                Text("Bar Mode")
//                Spacer(modifier = Modifier.width(8.dp))
//                Switch(
//                    checked = isProgressDonut,
//                    onCheckedChange = { isProgressDonut = it }
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Donut Mode")
//            }
//        }
//
//        // ChartMark 리스트로 변환
//        val ChartMarks = sampleData.mapIndexed { index, value ->
//            ChartMark(
//                x = index.toFloat(),
//                y = value,
//                label = days.getOrElse(index) { "" }
//            )
//        }
//
//        // 범위 차트용 샘플 데이터 (심박수 범위 예시)
//        val rangeData = listOf(
//            RangeChartMark(x = 0f, minPoint = ChartMark(x = 0f, y = 54f), maxPoint = ChartMark(x = 0f, y = 160f), label = "2일"),
//            RangeChartMark(x = 1f, minPoint = ChartMark(x = 1f, y = 65f), maxPoint = ChartMark(x = 1f, y = 145f), label = "3일"),
//            RangeChartMark(x = 2f, minPoint = ChartMark(x = 2f, y = 58f), maxPoint = ChartMark(x = 2f, y = 125f), label = "4일"),
//            RangeChartMark(x = 3f, minPoint = ChartMark(x = 3f, y = 75f), maxPoint = ChartMark(x = 3f, y = 110f), label = "6일"),
//            RangeChartMark(x = 4f, minPoint = ChartMark(x = 4f, y = 68f), maxPoint = ChartMark(x = 4f, y = 162f), label = "7일"),
//            RangeChartMark(x = 5f, minPoint = ChartMark(x = 5f, y = 72f), maxPoint = ChartMark(x = 5f, y = 168f), label = "8일"),
//            RangeChartMark(x = 6f, minPoint = ChartMark(x = 6f, y = 65f), maxPoint = ChartMark(x = 6f, y = 138f), label = "9일"),
//            RangeChartMark(x = 7f, minPoint = ChartMark(x = 7f, y = 85f), maxPoint = ChartMark(x = 7f, y = 105f), label = "10일")
//        )
//
//        // 스택 바 차트용 샘플 데이터 (일별 영양소 섭취량 예시)
//        val stackedData = listOf(
//            // Monday (x = 0)
//            ChartMark(x = 0f, y = 80f, label = "월"),
//            ChartMark(x = 0f, y = 45f, label = "월"),
//            ChartMark(x = 0f, y = 120f, label = "월"),
//
//            // Tuesday (x = 1)
//            ChartMark(x = 1f, y = 75f, label = "화"),
//            ChartMark(x = 1f, y = 38f, label = "화"),
//            ChartMark(x = 1f, y = 110f, label = "화"),
//
//            // Wednesday (x = 2)
//            ChartMark(x = 2f, y = 90f, label = "수"),
//            ChartMark(x = 2f, y = 52f, label = "수"),
//            ChartMark(x = 2f, y = 140f, label = "수"),
//
//            // Thursday (x = 3)
//            ChartMark(x = 3f, y = 85f, label = "목"),
//            ChartMark(x = 3f, y = 41f, label = "목"),
//            ChartMark(x = 3f, y = 135f, label = "목"),
//
//            // Friday (x = 4)
//            ChartMark(x = 4f, y = 95f, label = "금"),
//            ChartMark(x = 4f, y = 58f, label = "금"),
//            ChartMark(x = 4f, y = 150f, label = "금"),
//
//            // Saturday (x = 5)
//            ChartMark(x = 5f, y = 70f, label = "토"),
//            ChartMark(x = 5f, y = 35f, label = "토"),
//            ChartMark(x = 5f, y = 100f, label = "토"),
//
//            // Sunday (x = 6)
//            ChartMark(x = 6f, y = 88f, label = "일"),
//            ChartMark(x = 6f, y = 48f, label = "일"),
//            ChartMark(x = 6f, y = 125f, label = "일")
//        )
//
//        // 선택된 차트 타입에 따라 다른 차트 표시
//        when (selectedChartType) {
//            "Line" -> {
//                LineChart(
//                    data = ChartMarks,
//                    title = "요일별 활동량",
//                    yLabel = "활동량",
//                    xLabel = "요일",
//                    strokeWidth = 10f,
//                    interactionType = InteractionType.Line.TOUCH_AREA
//                )
//            }
//            "Scatter" -> {
//                ScatterPlot(
//                    data = ChartMarks,
//                    title = "요일별 활동량",
//                    yLabel = "활동량",
//                    xLabel = "요일",
//                    interactionType = InteractionType.Scatter.POINT
//                )
//            }
//            "Bar" -> {
//                BarChart(
//                    data = ChartMarks,
//                    title = "요일별 활동량",
//                    yLabel = "활동량",
//                    xLabel = "요일",
//                    maxY = 88f,
//                    interactionType = InteractionType.Bar.TOUCH_AREA
//                )
//            }
////            "Stacked" -> {
////                StackedBarChart(
////                    data = stackedData,
////                    segmentLabels = segmentLabels,
////                    title = "요일별 영양소 섭취량",
////                    yLabel = "영양소 (g)",
////                    xLabel = "요일",
////                    showLegend = true,
////                    colors = listOf(
////                        Color(0xFF2196F3), // 파랑 (단백질)
////                        Color(0xFFFF9800), // 주황 (지방)
////                        Color(0xFF4CAF50)  // 초록 (탄수화물)
////                    ),
////                    interactionType = InteractionType.StackedBar.BAR
////                )
////            }
////            "Range" -> {
////                RangeBarChart(
////                    data = rangeData,
////                    title = "일별 심박수 범위",
////                    yLabel = "심박수 (bpm)",
////                    xLabel = "날짜",
////                    barColor = Color(0xFFFF9800),
////                    interactionType = InteractionType.RangeBar.TOUCH_AREA
////                )
////            }
//            "Pie" -> {
//                PieChart(
//                    data = ChartMarks,
//                    title = "요일별 활동량",
//                    isDonut = true,
//                    showLegend = true,
//                )
//            }
//            "Progress" -> {
//                 val progressData = listOf(
//                     ProgressChartMark(
//                         x = 0f,
//                         current = 1200f,
//                         max = 2000f,
//                         label = "Move",
//                         unit = "KJ"
//                     ),
//                     ProgressChartMark(
//                         x = 1f,
//                         current = 20f,
//                         max = 60f,
//                         label = "Exercise",
//                         unit = "min"
//                     ),
//                     ProgressChartMark(
//                         x = 2f,
//                         current = 7f,
//                         max = 10f,
//                         label = "Stand",
//                         unit = "h"
//                     )
//                 )
//                 ProgressChart(
//                     data = progressData,
//                     title = "일일 활동 진행률",
//                     isDonut = isProgressDonut,
//                     isPercentage = false,
//                     colors = listOf(
//                         Color(0xFF00C7BE), // 청록색 (Move)
//                         Color(0xFFFF6B35), // 주황색 (Exercise)
//                         Color(0xFF3A86FF)  // 파란색 (Stand)
//                     )
//                 )
//             }
//            "Calendar" -> {
//                CalendarChart(
//                    entries = entries,
//                    yearMonth = yearMonth,
//
//                )
//            }
//            "MinBar" -> {
//                 MinimalBarChart(
//
//                     data = ChartMarks,
//                     color = Color.Blue,
//                 )
//             }
//             "MinLine" -> {
//                 MinimalLineChart(
//                     data = ChartMarks,
//                     color = Color.Blue,
//                     showPoints = true
//                 )
//             }
////            "MinRange" -> {
////                 MinimalRangeBarChart(
////                     data = rangeData,
////                     color = Color.Blue,
////                 )
////             }
////             "MinGauge" -> {
////                 // 단일 범위 데이터 생성 (심박수 범위 예시: 76-104 bpm)
////                 val singleRangeData = RangeChartMark(
////                     x = 0f,
////                     minPoint = ChartMark(x = 0f, y = 76f),
////                     maxPoint = ChartMark(x = 0f, y = 104f),
////                     label = "Heart Rate"
////                 )
////                 MinimalGaugeChart(
////                     data = singleRangeData,
////                     containerMin = 60f,  // 정상 심박수 범위 시작
////                     containerMax = 120f, // 정상 심박수 범위 끝
////                     containerColor = Color.LightGray,
////                     rangeColor = Color(0xFFFF9500),
////                 )
////             }
//
//        }
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun ChartPreview() {
//    SalusChartTheme {
//        SampleCharts()
//    }
//}