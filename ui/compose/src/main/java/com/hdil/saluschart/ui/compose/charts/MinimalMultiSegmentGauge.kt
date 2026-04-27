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
import kotlin.math.min

/**
 * A single colored zone in a [MinimalMultiSegmentGauge].
 *
 * @param fraction Proportional width of this segment; all fractions are normalized against their sum.
 * @param color Fill color of this segment.
 */
data class MinimalGaugeSegment(
    val fraction: Float, // portion of the bar, sum should be ~1.0
    val color: Color
)

/**
 * Renders a compact horizontal multi-segment gauge bar with a floating capsule marker.
 *
 * The bar is divided into proportional colored zones defined by [segments]. A rounded-rectangle
 * marker is drawn at the horizontal position specified by [markerRatio], constrained to remain
 * within the segment it falls inside.
 *
 * @param modifier Modifier applied to the Canvas.
 * @param segments Ordered list of gauge segments that partition the bar from left to right.
 * @param markerRatio Position of the marker along the gauge (0 = left edge, 1 = right edge).
 * @param label Text displayed inside the callout bubble.
 * @param bubbleColor Background color of the callout bubble.
 * @param bubbleTextColor Text color inside the callout bubble.
 * @param barHeight Height of the gauge bar.
 * @param cornerRadius Corner radius of the gauge bar; defaults to fully rounded (pill).
 * @param markerWidth Requested width of the marker capsule; clamped to the containing segment width.
 * @param markerHeight Height of the marker capsule.
 * @param markerColor Fill color of the marker capsule.
 * @param markerShadowAlpha Alpha of the drop shadow rendered below the marker.
 * @param bubblePaddingH Horizontal padding inside the bubble.
 * @param bubblePaddingV Vertical padding inside the bubble.
 * @param bubbleCornerRadius Corner radius of the bubble rectangle.
 * @param pointerWidth Width of the triangular pointer.
 * @param pointerHeight Height of the triangular pointer.
 * @param bubbleGapFromBar Gap between the bottom of the bubble pointer and the top of the marker.
 * @param bubbleTextSizeSp Font size of the bubble label in sp.
 */
@Composable
fun MinimalMultiSegmentGauge(
    modifier: Modifier = Modifier,
    segments: List<MinimalGaugeSegment>,

    // 0..1 position along the gauge
    markerRatio: Float,
    label: String = "높음",

    // bubble styling
    bubbleColor: Color = Color(0xFFD6F5DF),
    bubbleTextColor: Color = Color(0xFF0A7A32),

    // sizing
    barHeight: Dp = 14.dp,
    cornerRadius: Dp = 999.dp,

    // marker capsule
    markerWidth: Dp = 44.dp,
    markerHeight: Dp = 22.dp,
    markerColor: Color = Color(0xFF7BE64C),
    markerShadowAlpha: Float = 0.10f,

    // bubble sizing
    bubblePaddingH: Dp = 10.dp,
    bubblePaddingV: Dp = 6.dp,
    bubbleCornerRadius: Dp = 18.dp,
    pointerWidth: Dp = 14.dp,
    pointerHeight: Dp = 8.dp,
    bubbleGapFromBar: Dp = 8.dp,
    bubbleTextSizeSp: Float = 12f,
) {
    if (segments.isEmpty()) return

    val p = markerRatio.coerceIn(0f, 1f)
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val h = barHeight.toPx()
        val r = min(cornerRadius.toPx(), h / 2f)
        val requestedMW = markerWidth.toPx()
        val mH = markerHeight.toPx()
        val markerVerticalOverflow = ((mH - h) / 2f).coerceAtLeast(0f)

        val barLeft = 0f
        val barTop = size.height - h - markerVerticalOverflow
        val barW = size.width

        val total = segments.sumOf { it.fraction.toDouble() }.toFloat().let { if (it <= 0f) 1f else it }

        var acc = 0f
        var selectedIndex = segments.lastIndex
        run {
            for (i in segments.indices) {
                val w = (segments[i].fraction / total).coerceAtLeast(0f)
                val next = acc + w
                if (p <= next || i == segments.lastIndex) {
                    selectedIndex = i
                    break
                }
                acc = next
            }
        }

        var x = barLeft
        val segStarts = FloatArray(segments.size)
        val segEnds = FloatArray(segments.size)

        for (i in segments.indices) {
            val wPx = ((segments[i].fraction / total).coerceAtLeast(0f)) * barW
            segStarts[i] = x
            segEnds[i] = x + wPx
            x += wPx
        }

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

        clipPath(clipPath) {
            for (i in segments.indices) {
                val wSafe = max(0f, segEnds[i] - segStarts[i])
                if (wSafe > 0f) {
                    drawRect(
                        color = segments[i].color,
                        topLeft = Offset(segStarts[i], barTop),
                        size = Size(wSafe, h)
                    )
                }
            }
        }

        val segLeft = segStarts[selectedIndex]
        val segRight = segEnds[selectedIndex]
        val segW = (segRight - segLeft).coerceAtLeast(0f)
        val mW = min(requestedMW, segW)

        val markerR = mH * 0.32f

        val cx = barLeft + p * barW

        // Compute left and clamp to segment bounds
        val idealLeft = cx - mW / 2f
        val mLeft = idealLeft.coerceIn(segLeft, segRight - mW)

        val mTop = barTop - (mH - h) / 2f

        drawRoundRect(
            color = Color.Black.copy(alpha = markerShadowAlpha),
            topLeft = Offset(mLeft, mTop + 1.5f),
            size = Size(mW, mH),
            cornerRadius = CornerRadius(markerR, markerR)
        )

        // marker body
        drawRoundRect(
            color = markerColor,
            topLeft = Offset(mLeft, mTop),
            size = Size(mW, mH),
            cornerRadius = CornerRadius(markerR, markerR)
        )

        val style = TextStyle(
            color = bubbleTextColor,
            fontSize = bubbleTextSizeSp.sp
        )
        val textLayout = textMeasurer.measure(label, style)

        val padH = bubblePaddingH.toPx()
        val padV = bubblePaddingV.toPx()
        val bubbleW = textLayout.size.width + padH * 2f
        val bubbleH = textLayout.size.height + padV * 2f
        val bubbleR = min(bubbleCornerRadius.toPx(), min(bubbleW / 2f, bubbleH / 2f))
        val pointerW = pointerWidth.toPx()
        val pointerH = pointerHeight.toPx()
        val gap = bubbleGapFromBar.toPx()
        val textBaselineCorrection = (textLayout.firstBaseline - textLayout.size.height / 2f)
            .coerceAtLeast(0f)

        val bubbleMaxLeft = (size.width - bubbleW).coerceAtLeast(0f)
        val bubbleLeft = (cx - bubbleW / 2f).coerceIn(0f, bubbleMaxLeft)
        val bubbleTop = (mTop - gap - pointerH - bubbleH).coerceAtLeast(0f)

        drawRoundRect(
            color = bubbleColor,
            topLeft = Offset(bubbleLeft, bubbleTop),
            size = Size(bubbleW, bubbleH),
            cornerRadius = CornerRadius(bubbleR, bubbleR)
        )

        val pointerCenterX = cx.coerceIn(
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
                y = bubbleTop + (bubbleH - textLayout.size.height) / 2f - textBaselineCorrection
            )
        )
    }
}

/**
 * Returns the segment color that contains the given [ratio] position.
 *
 * @param ratio Normalized position along the gauge (0–1).
 * @param segments Ordered list of gauge segments.
 * @param fallback Color returned when [segments] is empty.
 * @return The [Color] of the segment at [ratio], or [fallback] if [segments] is empty.
 */
fun markerColorForRatio(
    ratio: Float,
    segments: List<MinimalGaugeSegment>,
    fallback: Color = Color(0xFF8BEA3B)
): Color {
    if (segments.isEmpty()) return fallback

    val total = segments.sumOf { it.fraction.toDouble() }.toFloat().let { if (it <= 0f) 1f else it }
    val t = ratio.coerceIn(0f, 1f)

    var acc = 0f
    for (seg in segments) {
        val w = (seg.fraction / total).coerceAtLeast(0f)
        val next = acc + w
        // include the boundary in the current segment so it feels stable
        if (t <= next || seg == segments.last()) return seg.color
        acc = next
    }
    return segments.last().color
}
