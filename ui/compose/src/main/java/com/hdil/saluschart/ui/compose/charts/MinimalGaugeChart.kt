package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    label: String = "높음",
    bubbleColor: Color = Color(0xFFD6F5DF),
    bubbleTextColor: Color = Color(0xFF0A7A32),
    gaugeBarHeight: Dp = 24.dp,
    cornerRadius: Dp = 8.dp,
    bubblePaddingH: Dp = 10.dp,
    bubblePaddingV: Dp = 6.dp,
    bubbleCornerRadius: Dp = 18.dp,
    pointerWidth: Dp = 14.dp,
    pointerHeight: Dp = 8.dp,
    bubbleGapFromBar: Dp = 8.dp,
    bubbleTextSizeSp: Float = 12f,
) {
    val rangeColor = rangeColor.takeIf { it != Color.Unspecified } ?: LocalSalusChartColors.current.primary
    val clampedDataMin = data.minPoint.y.coerceIn(containerMin, containerMax)
    val clampedDataMax = data.maxPoint.y.coerceIn(containerMin, containerMax)
    val textMeasurer = rememberTextMeasurer()

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

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    gaugeBarHeight +
                        bubblePaddingV * 2 +
                        pointerHeight +
                        bubbleGapFromBar +
                        20.dp
                )
        ) {
            val containerRange = containerMax - containerMin
            val startRatio = if (containerRange > 0.0) {
                ((clampedDataMin - containerMin) / containerRange).coerceIn(0.0, 1.0).toFloat()
            } else 0f
            val endRatio = if (containerRange > 0.0) {
                ((clampedDataMax - containerMin) / containerRange).coerceIn(0.0, 1.0).toFloat()
            } else 0f
            val widthRatio = (endRatio - startRatio).coerceAtLeast(0f)

            val barHeightPx = gaugeBarHeight.toPx()
            val barTop = size.height - barHeightPx
            val barWidth = size.width
            val barCornerRadius = CornerRadius(
                x = cornerRadius.toPx(),
                y = cornerRadius.toPx()
            )

            drawRoundRect(
                color = containerColor,
                topLeft = Offset(0f, barTop),
                size = Size(barWidth, barHeightPx),
                cornerRadius = barCornerRadius
            )

            if (widthRatio > 0f) {
                val rangeLeft = barWidth * startRatio
                val rangeWidth = barWidth * widthRatio

                drawRoundRect(
                    color = rangeColor,
                    topLeft = Offset(rangeLeft, barTop),
                    size = Size(rangeWidth, barHeightPx),
                    cornerRadius = barCornerRadius
                )
            }

            val selectedCenterX = barWidth * ((startRatio + endRatio) / 2f)
            val style = TextStyle(
                color = bubbleTextColor,
                fontSize = bubbleTextSizeSp.sp
            )
            val textLayout = textMeasurer.measure(label, style)

            val padH = bubblePaddingH.toPx()
            val padV = bubblePaddingV.toPx()
            val bubbleW = textLayout.size.width + padH * 2f
            val bubbleH = textLayout.size.height + padV * 2f
            val bubbleR = minOf(bubbleCornerRadius.toPx(), bubbleW / 2f, bubbleH / 2f)
            val pointerW = pointerWidth.toPx()
            val pointerH = pointerHeight.toPx()
            val gap = bubbleGapFromBar.toPx()
            val bubbleMaxLeft = (size.width - bubbleW).coerceAtLeast(0f)
            val bubbleLeft = (selectedCenterX - bubbleW / 2f).coerceIn(0f, bubbleMaxLeft)
            val bubbleTop = (barTop - gap - pointerH - bubbleH).coerceAtLeast(0f)

            drawRoundRect(
                color = bubbleColor,
                topLeft = Offset(bubbleLeft, bubbleTop),
                size = Size(bubbleW, bubbleH),
                cornerRadius = CornerRadius(bubbleR, bubbleR)
            )

            val pointerCenterX = selectedCenterX.coerceIn(
                bubbleLeft + bubbleR,
                bubbleLeft + bubbleW - bubbleR
            )
            val pLeft = pointerCenterX - pointerW / 2f
            val pRight = pointerCenterX + pointerW / 2f
            val pTop = bubbleTop + bubbleH
            val pBottom = pTop + pointerH

            val pointerPath = Path().apply {
                moveTo(pLeft, pTop)
                lineTo(pRight, pTop)
                lineTo(pointerCenterX, pBottom)
                close()
            }
            drawPath(pointerPath, bubbleColor)

            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    x = bubbleLeft + (bubbleW - textLayout.size.width) / 2f,
                    y = bubbleTop + (bubbleH - textLayout.size.height) / 2f
                )
            )
        }
    }
}
