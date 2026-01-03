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
import kotlin.math.min

@Composable
fun MinimalProgressBar(
    modifier: Modifier = Modifier,

    progress: Float, // 0f..1f
    label: String = "${(progress * 100).toInt()}%",

    // colors
    trackColor: Color = Color(0xFFEDEDED),
    fillColor: Color = Color(0xFF00C853),

    bubbleColor: Color = Color(0xFFD6F5DF),
    bubbleTextColor: Color = Color(0xFF0A7A32),

    // sizing
    barHeight: Dp = 12.dp,
    cornerRadius: Dp = 999.dp,

    bubblePaddingH: Dp = 10.dp,
    bubblePaddingV: Dp = 6.dp,
    bubbleCornerRadius: Dp = 18.dp,
    pointerWidth: Dp = 14.dp,
    pointerHeight: Dp = 8.dp,
    bubbleGapFromBar: Dp = 8.dp,
    bubbleTextSizeSp: Float = 12f,
) {
    val textMeasurer = rememberTextMeasurer()
    val p = progress.coerceIn(0f, 1f)

    Canvas(modifier = modifier) {
        val h = barHeight.toPx()
        val r = min(cornerRadius.toPx(), h / 2f)

        val barLeft = 0f
        val barTop = size.height - h
        val barW = size.width

        // Track
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(barLeft, barTop),
            size = Size(barW, h),
            cornerRadius = CornerRadius(r, r)
        )

        // Fill (clipped to pill)
        val clipPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = barLeft,
                    top = barTop,
                    right = barLeft + barW,
                    bottom = barTop + h,
                    cornerRadius = CornerRadius(r, r)
                )
            )
        }

        val fillW = barW * p
        clipPath(clipPath) {
            drawRect(
                color = fillColor,
                topLeft = Offset(barLeft, barTop),
                size = Size(fillW, h)
            )
        }

        // Bubble measure
        val style = TextStyle(color = bubbleTextColor, fontSize = bubbleTextSizeSp.sp)
        val layout = textMeasurer.measure(label, style)

        val padH = bubblePaddingH.toPx()
        val padV = bubblePaddingV.toPx()
        val bubbleW = layout.size.width + padH * 2f
        val bubbleH = layout.size.height + padV * 2f

        val bubbleR = bubbleCornerRadius.toPx()
        val pointerW = pointerWidth.toPx()
        val pointerH = pointerHeight.toPx()
        val gap = bubbleGapFromBar.toPx()

        // Anchor: near end of the filled part (like your screenshot)
        val anchorX = (barLeft + fillW).coerceIn(0f, barLeft + barW)
        val bubbleLeft = (anchorX - bubbleW / 2f).coerceIn(0f, size.width - bubbleW)
        val bubbleTop = (barTop - gap - pointerH - bubbleH).coerceAtLeast(0f)

        // Bubble
        drawRoundRect(
            color = bubbleColor,
            topLeft = Offset(bubbleLeft, bubbleTop),
            size = Size(bubbleW, bubbleH),
            cornerRadius = CornerRadius(bubbleR, bubbleR)
        )

        // Pointer tail
        val pointerCenterX = anchorX.coerceIn(bubbleLeft + bubbleR, bubbleLeft + bubbleW - bubbleR)
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

        // Text
        drawText(
            textMeasurer = textMeasurer,
            text = label,
            topLeft = Offset(
                x = bubbleLeft + (bubbleW - layout.size.width) / 2f,
                y = bubbleTop + (bubbleH - layout.size.height) / 2f
            ),
            style = style
        )
    }
}
