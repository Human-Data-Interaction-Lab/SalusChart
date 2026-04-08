package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.ui.theme.LocalSalusChartColors

/**
 * Minimal gauge chart for small screens such as widgets or smartwatches.
 * Displays a range bar within the container bounds with optional range text above.
 */
@Composable
fun MinimalGaugeChart(
    modifier: Modifier = Modifier,
    data: RangeChartMark,
    containerMin: Double,
    containerMax: Double,
    containerColor: Color = Color.LightGray,
    rangeColor: Color = Color.Unspecified,
    textColor: Color = Color.Black,
    showRangeText: Boolean = true,
) {
    val rangeColor = rangeColor.takeIf { it != Color.Unspecified } ?: LocalSalusChartColors.current.primary
    val clampedDataMin = data.minPoint.y.coerceIn(containerMin, containerMax)
    val clampedDataMax = data.maxPoint.y.coerceIn(containerMin, containerMax)

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showRangeText) {
            ChartDraw.Gauge.RangeText(
                dataMin = clampedDataMin.toFloat(),
                dataMax = clampedDataMax.toFloat(),
                textColor = textColor
            )
        }

        ChartDraw.Gauge.GaugeBar(
            dataMin = clampedDataMin.toFloat(),
            dataMax = clampedDataMax.toFloat(),
            containerMin = containerMin.toFloat(),
            containerMax = containerMax.toFloat(),
            containerColor = containerColor,
            rangeColor = rangeColor
        )
    }
}
