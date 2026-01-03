package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

data class StackedBarSegment(
    val value: Float,          // portion size (relative)
    val color: Color
)

@Composable
fun MinimalHorizontalStackedBar(
    modifier: Modifier = Modifier,

    segments: List<StackedBarSegment>,
    trackColor: Color = Color(0xFFEFEFEF),

    // which category to point at (index into segments)
    selectedSegmentIndex: Int = 0,
    label: String = "높음",

    // bubble styling
    bubbleColor: Color = Color(0xFFD6F5DF),
    bubbleTextColor: Color = Color(0xFF0A7A32),

    // sizing
    barHeight: Dp = 12.dp,
    cornerRadius: Dp = 999.dp,      // pill
    bubblePaddingH: Dp = 10.dp,
    bubblePaddingV: Dp = 6.dp,
    bubbleCornerRadius: Dp = 18.dp,
    pointerWidth: Dp = 14.dp,
    pointerHeight: Dp = 8.dp,
    bubbleGapFromBar: Dp = 8.dp,
    bubbleTextSizeSp: Float = 12f
) {
    if (segments.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val h = barHeight.toPx()
        val r = minOf(cornerRadius.toPx(), h / 2f)

        val total = segments.sumOf { it.value.toDouble() }.toFloat().let { if (it <= 0f) 1f else it }
        val safeIndex = selectedSegmentIndex.coerceIn(0, segments.lastIndex)

        val barTop = size.height - h
        val barLeft = 0f
        val barWidth = size.width

        // Draw track (pill)
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(barLeft, barTop),
            size = Size(barWidth, h),
            cornerRadius = CornerRadius(r, r)
        )

        // Draw stacked segments clipped to pill
        val clipPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = barLeft,
                    top = barTop,
                    right = barLeft + barWidth,
                    bottom = barTop + h,
                    cornerRadius = CornerRadius(r, r)
                )
            )
        }

        var x = barLeft
        var selectedCenterX = barLeft

        clipPath(clipPath) {
            segments.forEachIndexed { i, seg ->
                val w = (seg.value / total) * barWidth
                val wSafe = max(0f, w)

                if (wSafe > 0f) {
                    drawRect(
                        color = seg.color,
                        topLeft = Offset(x, barTop),
                        size = Size(wSafe, h)
                    )
                }

                if (i == safeIndex) {
                    selectedCenterX = x + wSafe / 2f
                }
                x += wSafe
            }
        }

        // Bubble measurement
        val style = TextStyle(
            color = bubbleTextColor,
            fontSize = bubbleTextSizeSp.sp
        )
        val textLayout = textMeasurer.measure(label, style)

        val padH = bubblePaddingH.toPx()
        val padV = bubblePaddingV.toPx()
        val bubbleW = textLayout.size.width + padH * 2f
        val bubbleH = textLayout.size.height + padV * 2f

        val bubbleR = bubbleCornerRadius.toPx()
        val pointerW = pointerWidth.toPx()
        val pointerH = pointerHeight.toPx()
        val gap = bubbleGapFromBar.toPx()

        // Place bubble centered over selected segment, clamp within canvas
        val bubbleLeft = (selectedCenterX - bubbleW / 2f).coerceIn(0f, size.width - bubbleW)
        val bubbleTop = (barTop - gap - pointerH - bubbleH).coerceAtLeast(0f)

        // Bubble rect
        drawRoundRect(
            color = bubbleColor,
            topLeft = Offset(bubbleLeft, bubbleTop),
            size = Size(bubbleW, bubbleH),
            cornerRadius = CornerRadius(bubbleR, bubbleR)
        )

        // Pointer triangle (small speech tail)
        val pointerCenterX = selectedCenterX.coerceIn(bubbleLeft + bubbleR, bubbleLeft + bubbleW - bubbleR)
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

        // Bubble text
        drawText(
            textMeasurer = textMeasurer,
            text = label,
            topLeft = Offset(
                x = bubbleLeft + (bubbleW - textLayout.size.width) / 2f,
                y = bubbleTop + (bubbleH - textLayout.size.height) / 2f
            ),
            style = style
        )
    }
}
