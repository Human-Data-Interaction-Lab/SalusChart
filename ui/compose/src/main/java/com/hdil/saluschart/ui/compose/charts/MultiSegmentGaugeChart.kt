package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class GaugeSegment(
    val start: Float,
    val end: Float,
    val color: Color
)

@Composable
fun MultiSegmentGaugeChart(
    modifier: Modifier = Modifier,

    // Card
    cornerRadius: Dp = 26.dp,
    cardPaddingH: Dp = 26.dp,
    cardPaddingV: Dp = 22.dp,

    // Content
    title: String,
    value: Float?,

    // Range & Segments
    minValue: Float,
    maxValue: Float,
    segments: List<GaugeSegment>,

    // Labels under bar (boundary ticks)
    tickValues: List<Float>,

    // Optional hints
    leftHint: String? = null,
    rightHint: String? = null,

    // Optional message for no data state
    noDataMessage: String? = null,

    // Styling
    trackColor: Color = Color(0xFFF2F2F2),
    textGray: Color = Color(0xFF9B9B9B),
    titleColor: Color = Color.Black,

    // Bar sizing
    barHeight: Dp = 28.dp,
    barHorizontalPadding: Dp = 28.dp,

    // Marker sizing
    markerOuterRadiusFactor: Float = 0.34f,
    markerHexRadiusFactor: Float = 0.68f,
    markerHexStrokeWidth: Dp = 2.dp,

    // Value label
    valueFontSizeSp: Float = 12f,
    valueFontWeight: FontWeight = FontWeight.SemiBold,
    valueToMarkerGap: Dp = 12.dp,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val lo = min(minValue, maxValue)
    val hi = max(minValue, maxValue)

    fun ratio(v: Float): Float {
        val denom = (hi - lo)
        val t = if (denom == 0f) 0f else (v - lo) / denom
        return t.coerceIn(0f, 1f)
    }

    fun segmentForValue(v: Float): GaugeSegment? {
        val vv = v.coerceIn(lo, hi)
        return segments.firstOrNull { seg ->
            val a = min(seg.start, seg.end)
            val b = max(seg.start, seg.end)
            vv >= a && vv <= b
        } ?: segments.firstOrNull()
    }

    // Value label color: slightly darker version of segment where marker is
    val valueColor: Color = remember(value, segments) {
        if (value == null) Color(0xFF6F6F6F)
        else darken(segmentForValue(value)?.color ?: Color(0xFF7ADB2A), 0.82f)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = cardPaddingH, vertical = cardPaddingV),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            // Value label + bar in a single layout so label aligns exactly above marker
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = barHorizontalPadding)
            ) {
                val barW = maxWidth

                val labelStyle = TextStyle(
                    fontSize = valueFontSizeSp.sp,
                    fontWeight = valueFontWeight
                )

                // How much vertical space for label area (sp -> dp via density)
                val labelAreaH: Dp = if (value != null) {
                    with(density) { valueFontSizeSp.sp.toDp() } + valueToMarkerGap
                } else 0.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(labelAreaH + barHeight)
                ) {
                    // Place value label at the marker X (clamped so it doesn't clip)
                    if (value != null) {
                        val label = format0(value)
                        val layout = textMeasurer.measure(label, style = labelStyle)
                        val textWdp = with(density) { layout.size.width.toDp() }

                        val xDp = barW * ratio(value)
                        val leftDp = (xDp - textWdp / 2f).coerceIn(0.dp, barW - textWdp)

                        Text(
                            text = label,
                            color = valueColor,
                            fontSize = valueFontSizeSp.sp,
                            fontWeight = valueFontWeight,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = leftDp, y = 0.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(999.dp))
                            .background(trackColor)
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            fun vToX(v: Float): Float = ratio(v) * w

                            // Segments: rectangles (square boundaries) but outer bar is clipped
                            segments.forEach { seg ->
                                val s0 = seg.start.coerceIn(lo, hi)
                                val s1 = seg.end.coerceIn(lo, hi)
                                val left = vToX(min(s0, s1))
                                val right = vToX(max(s0, s1))
                                val segW = (right - left).coerceAtLeast(0f)

                                drawRect(
                                    color = seg.color,
                                    topLeft = Offset(left, 0f),
                                    size = Size(segW, h)
                                )
                            }

                            // Marker: white circle + transparent hex window + white hex + inner white hex
                            if (value != null) {
                                val vx = vToX(value.coerceIn(lo, hi))
                                val cy = h / 2f

                                val outerR = h * markerOuterRadiusFactor
                                val hexR = outerR * markerHexRadiusFactor
                                val innerHexR = hexR * 0.62f

                                val hexPath = regularPolygonPath(
                                    centerX = vx,
                                    centerY = cy,
                                    radius = hexR,
                                    sides = 6,
                                    rotationRad = (-Math.PI / 6.0).toFloat()
                                )
                                val innerHexPath = regularPolygonPath(
                                    centerX = vx,
                                    centerY = cy,
                                    radius = innerHexR,
                                    sides = 6,
                                    rotationRad = (-Math.PI / 6.0).toFloat()
                                )

                                // Use a layer so BlendMode.Clear works reliably
                                drawIntoCanvas { canvas ->
                                    val paint = androidx.compose.ui.graphics.Paint()
                                    val layerRect = androidx.compose.ui.geometry.Rect(
                                        left = vx - outerR - 6.dp.toPx(),
                                        top = cy - outerR - 6.dp.toPx(),
                                        right = vx + outerR + 6.dp.toPx(),
                                        bottom = cy + outerR + 6.dp.toPx()
                                    )
                                    canvas.saveLayer(layerRect, paint)

                                    // Outer white circle
                                    drawCircle(
                                        color = Color.White,
                                        radius = outerR,
                                        center = Offset(vx, cy)
                                    )

                                    // Transparent hex cut-out (bar shows through)
                                    drawPath(
                                        path = hexPath,
                                        color = Color.Transparent,
                                        blendMode = BlendMode.Clear
                                    )

                                    // White hex outline
                                    drawPath(
                                        path = hexPath,
                                        color = Color.White,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = markerHexStrokeWidth.toPx()
                                        )
                                    )

                                    // Subtle inner white hex (looks like Samsung)
                                    drawPath(
                                        path = innerHexPath,
                                        color = Color.White.copy(alpha = 0.60f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = (markerHexStrokeWidth.toPx() * 0.85f)
                                        )
                                    )

                                    canvas.restore()
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Optional hints row
            if (leftHint != null || rightHint != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = barHorizontalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = leftHint ?: "",
                        color = textGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = rightHint ?: "",
                        color = textGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            TickRowNoOverlap(
                ticks = tickValues,
                minValue = lo,
                maxValue = hi,
                textColor = textGray,
                horizontalPadding = barHorizontalPadding,
                highlightValue = value
            )

            if (value == null && !noDataMessage.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFEDEDED))
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = noDataMessage,
                    color = Color(0xFF4A4A4A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TickRowNoOverlap(
    ticks: List<Float>,
    minValue: Float,
    maxValue: Float,
    textColor: Color,
    horizontalPadding: Dp,
    highlightValue: Float?
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    ) {
        val w = maxWidth
        val gap = 6.dp

        fun ratio(v: Float): Float {
            val lo = min(minValue, maxValue)
            val hi = max(minValue, maxValue)
            val denom = (hi - lo)
            val t = if (denom == 0f) 0f else (v - lo) / denom
            return t.coerceIn(0f, 1f)
        }

        val highlightTick = remember(ticks, highlightValue) {
            if (highlightValue == null || ticks.isEmpty()) null
            else ticks.minByOrNull { abs(it - highlightValue) }
        }

        val style = TextStyle(
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        val labels = remember(ticks) { ticks.map { format0(it) } }
        val measured = labels.map { textMeasurer.measure(it, style = style) }
        val widthsDp = measured.map { with(density) { it.size.width.toDp() } }

        val lefts = ticks.mapIndexed { i, t ->
            val cx = w * ratio(t)
            (cx - widthsDp[i] / 2f).coerceIn(0.dp, (w - widthsDp[i]).coerceAtLeast(0.dp))
        }.toMutableList()

        // Forward pass
        for (i in 1 until lefts.size) {
            val prevEnd = lefts[i - 1] + widthsDp[i - 1]
            if (lefts[i] < prevEnd + gap) lefts[i] = prevEnd + gap
        }
        // Backward pass
        for (i in lefts.lastIndex downTo 0) {
            val maxLeft = (w - widthsDp[i]).coerceAtLeast(0.dp)
            lefts[i] = lefts[i].coerceIn(0.dp, maxLeft)

            if (i > 0) {
                val prevEndMax = lefts[i] - gap
                val prevLeftMax = (prevEndMax - widthsDp[i - 1]).coerceAtLeast(0.dp)
                if (lefts[i - 1] > prevLeftMax) lefts[i - 1] = prevLeftMax
            }
        }

        Box(modifier = Modifier.height(18.dp).fillMaxWidth()) {
            labels.forEachIndexed { i, label ->
                val isHighlight = (highlightTick != null && format0(highlightTick) == label)
                Text(
                    text = label,
                    style = style.copy(
                        color = if (isHighlight) Color(0xFF7A7A7A) else textColor
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = lefts[i]),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun regularPolygonPath(
    centerX: Float,
    centerY: Float,
    radius: Float,
    sides: Int,
    rotationRad: Float = 0f
): Path {
    val path = Path()
    if (sides < 3) return path
    for (i in 0 until sides) {
        val a = (Math.PI * 2.0 * i / sides).toFloat() + rotationRad
        val x = centerX + radius * kotlin.math.cos(a)
        val y = centerY + radius * kotlin.math.sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun darken(c: Color, factor: Float): Color {
    return Color(
        red = (c.red * factor).coerceIn(0f, 1f),
        green = (c.green * factor).coerceIn(0f, 1f),
        blue = (c.blue * factor).coerceIn(0f, 1f),
        alpha = c.alpha
    )
}

private fun format0(v: Float): String {
    val i = v.toInt()
    return if (abs(v - i) < 0.0001f) i.toString() else "%.1f".format(v)
}
