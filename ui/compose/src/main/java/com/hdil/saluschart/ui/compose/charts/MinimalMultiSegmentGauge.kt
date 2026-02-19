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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

data class MinimalGaugeSegment(
    val fraction: Float, // portion of the bar, sum should be ~1.0
    val color: Color
)

@Composable
fun MinimalMultiSegmentGauge(
    modifier: Modifier = Modifier,
    segments: List<MinimalGaugeSegment>,

    // 0..1 position along the gauge
    markerRatio: Float,

    // sizing
    barHeight: Dp = 14.dp,
    cornerRadius: Dp = 999.dp,

    // marker capsule
    markerWidth: Dp = 44.dp,
    markerHeight: Dp = 22.dp,
    markerColor: Color = Color(0xFF7BE64C),
    markerShadowAlpha: Float = 0.10f,
) {
    if (segments.isEmpty()) return

    val p = markerRatio.coerceIn(0f, 1f)

    Canvas(modifier = modifier) {
        val h = barHeight.toPx()
        val r = min(cornerRadius.toPx(), h / 2f)

        val barLeft = 0f
        val barTop = (size.height - h) / 2f
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

        val requestedMW = markerWidth.toPx()
        val mH = markerHeight.toPx()

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
    }
}

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

