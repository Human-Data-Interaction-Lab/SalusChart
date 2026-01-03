package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.hdil.saluschart.core.chart.ProgressChartMark
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun MiniActivityRings(
    modifier: Modifier = Modifier,
    rings: List<ProgressChartMark>,
    colors: List<Color>,
    strokeWidth: Float,               // px
    maxLaps: Int = 2,                 // show up to 200%
    trackAlpha: Float = 0.20f,
    gapRatio: Float = 0.14f,
    startAngle: Float = -90f,         // 12 o'clock
    shadowAlpha: Float = 0.10f,
    shadowExtraWidthRatio: Float = 0.45f
) {
    if (rings.isEmpty()) return

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)

        val maxRadius = (min(size.width, size.height) / 2f) - strokeWidth / 2f
        if (maxRadius <= 0f) return@Canvas

        val gap = strokeWidth * gapRatio
        val ringStep = strokeWidth + gap

        fun lighten(c: Color, f: Float) = Color(
            (c.red + (1f - c.red) * f).coerceIn(0f, 1f),
            (c.green + (1f - c.green) * f).coerceIn(0f, 1f),
            (c.blue + (1f - c.blue) * f).coerceIn(0f, 1f),
            c.alpha
        )

        fun darken(c: Color, f: Float) = Color(
            (c.red * (1f - f)).coerceIn(0f, 1f),
            (c.green * (1f - f)).coerceIn(0f, 1f),
            (c.blue * (1f - f)).coerceIn(0f, 1f),
            c.alpha
        )

        fun pointOnCircle(radius: Float, angleDeg: Float): Offset {
            val rad = (angleDeg * PI / 180.0).toFloat()
            return Offset(
                x = center.x + radius * cos(rad),
                y = center.y + radius * sin(rad)
            )
        }

        fun luminance(c: Color): Float =
            (0.2126f * c.red + 0.7152f * c.green + 0.0722f * c.blue).coerceIn(0f, 1f)

        fun arcGradient(
            base: Color,
            radius: Float,
            start: Float,
            sweep: Float,
            tailDark: Boolean
        ): Brush {
            val startPt = pointOnCircle(radius, start)
            val endPt = pointOnCircle(radius, start + sweep)

            val sweepClamped = sweep.coerceIn(0f, 360f)
            val t = (sweepClamped / 360f) // 0..1

            val lum = luminance(base)
            val darkBase = 0.01f + 0.02f * t          // max ~0.06
            val darkAmt = (darkBase * (0.35f + 0.65f * lum)).coerceIn(0.0f, 0.03f)

            val headAmt = (0.28f + 0.06f * t).coerceIn(0.28f, 0.36f)

            val tailColor = if (tailDark) darken(base, darkAmt) else base
            val midColor = base
            val headColor = lighten(base, headAmt)

            val dx = endPt.x - startPt.x
            val dy = endPt.y - startPt.y
            val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
            val ux = dx / len
            val uy = dy / len

            val minExtend = strokeWidth * 1.6f
            val maxExtend = strokeWidth * 3.0f
            val extend = (maxExtend - (maxExtend - minExtend) * t)

            val extendedStart = Offset(startPt.x - ux * extend, startPt.y - uy * extend)
            val extendedEnd = Offset(endPt.x + ux * extend, endPt.y + uy * extend)

            return Brush.linearGradient(
                colors = listOf(tailColor, midColor, headColor),
                start = extendedStart,
                end = extendedEnd
            )
        }

        fun drawArcShadow(
            topLeft: Offset,
            arcSize: Size,
            start: Float,
            sweep: Float,
            width: Float
        ) {
            val shadowW = width + (width * shadowExtraWidthRatio)
            drawArc(
                color = Color.Black.copy(alpha = shadowAlpha),
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = shadowW, cap = StrokeCap.Butt)
            )
        }

        fun drawEndCapHalfOutline(
            ringRadius: Float,
            headEndAngle: Float,
            strokeWidthPx: Float
        ) {
            val capCenter = pointOnCircle(ringRadius, headEndAngle)

            val capR = strokeWidthPx / 2f
            val capTopLeft = Offset(capCenter.x - capR, capCenter.y - capR)
            val capSize = Size(capR * 2f, capR * 2f)

            val outlineStroke = (strokeWidthPx * 0.16f).coerceAtLeast(1.0f)
            val outlineAlpha = 0.55f

            val outlineStart = headEndAngle + 360f
            val outlineSweep = 160f

            drawArc(
                color = Color.DarkGray.copy(alpha = outlineAlpha),
                startAngle = outlineStart,
                sweepAngle = outlineSweep,
                useCenter = false,
                topLeft = capTopLeft,
                size = capSize,
                style = Stroke(width = outlineStroke, cap = StrokeCap.Round)
            )
        }

        rings.forEachIndexed { i, mark ->
            val radius = maxRadius - i * ringStep
            if (radius <= 0f) return@forEachIndexed

            val d = radius * 2f
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(d, d)

            val baseColor = colors.getOrElse(i) { Color.White }
            val trackColor = baseColor.copy(alpha = trackAlpha)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 359.9f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            val raw = if (mark.max > 0.0) (mark.current / mark.max) else 0.0
            if (raw <= 0.0) return@forEachIndexed

            val progress = raw.coerceIn(0.0, maxLaps.toDouble()).toFloat()
            if (progress <= 0f) return@forEachIndexed

            if (progress <= 1f) {
                val sweep = progress * 360f
                val brush = arcGradient(
                    base = baseColor,
                    radius = radius,
                    start = startAngle,
                    sweep = sweep,
                    tailDark = true
                )

                drawArcShadow(topLeft, arcSize, startAngle, sweep, strokeWidth)

                drawArc(
                    brush = brush,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                drawArcShadow(topLeft, arcSize, startAngle, 359.9f, strokeWidth)

                drawArc(
                    color = baseColor,
                    startAngle = startAngle,
                    sweepAngle = 359.9f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                val extra = (progress - 1f).coerceIn(0f, 1f)
                if (extra > 0f) {
                    val extraSweep = extra * 360f
                    val extraBrush = arcGradient(
                        base = baseColor,
                        radius = radius,
                        start = startAngle,
                        sweep = extraSweep,
                        tailDark = false
                    )

                    drawArc(
                        brush = extraBrush,
                        startAngle = startAngle,
                        sweepAngle = extraSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val headEndAngle = startAngle + extraSweep
                    drawEndCapHalfOutline(
                        ringRadius = radius,
                        headEndAngle = headEndAngle,
                        strokeWidthPx = strokeWidth
                    )
                }
            }
        }
    }
}
