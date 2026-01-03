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

        // Clip path so segment colors follow pill shape
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

        // Draw segments inside clipped pill
        val total = segments.sumOf { it.fraction.toDouble() }.toFloat().let { if (it <= 0f) 1f else it }
        var x = barLeft

        clipPath(clipPath) {
            segments.forEach { seg ->
                val w = (seg.fraction / total) * barW
                val wSafe = max(0f, w)
                if (wSafe > 0f) {
                    drawRect(
                        color = seg.color,
                        topLeft = Offset(x, barTop),
                        size = Size(wSafe, h)
                    )
                }
                x += wSafe
            }
        }

        // Marker capsule position (centered at markerRatio)
        val mW = markerWidth.toPx()
        val mH = markerHeight.toPx()
        val markerR = min(mH / 2f, mW / 2f)

        val cx = barLeft + p * barW
        val mLeft = (cx - mW / 2f).coerceIn(0f, size.width - mW)
        val mTop = barTop - (mH - h) / 2f

        // subtle shadow behind marker (optional)
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

