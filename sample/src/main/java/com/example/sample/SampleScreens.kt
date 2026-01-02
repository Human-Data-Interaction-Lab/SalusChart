package com.example.sample
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hdil.saluschart.data.provider.SampleDataProvider
import com.hdil.saluschart.ui.compose.charts.HorizontalStackedBarChartList

@Composable
fun HorizontalStackedBarChartDemo() {
    val rows = remember { SampleDataProvider.getNutritionHorizontalStackedRows() }

    HorizontalStackedBarChartList(
        title = "영양정보",
        datePeriodText = "11월 3일 - 9일",
        rows = rows,
        onRowClick = { _, _, _ -> }
    )
}
