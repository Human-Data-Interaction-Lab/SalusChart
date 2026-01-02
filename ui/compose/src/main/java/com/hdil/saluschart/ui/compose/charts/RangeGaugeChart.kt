package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun RangeGaugeChart(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,

    minValue: Float,
    maxValue: Float,
    rangeStart: Float,
    rangeEnd: Float,
    recentValue: Float,

    unit: String = "bpm",
    recentLabel: String = "최근기록 오후 3:40",

    trackColor: Color = Color(0xFFF2F2F2),
    rangeColor: Color = Color(0xFFFF8A3D),
    textGray: Color = Color(0xFF9B9B9B),

    barHeight: Dp = 28.dp,
    titleFontSize: Int = 20,
    bottomFontSize: Int = 14,
    recentValueFontSize: Int = 16,
    subtitleFontSize: Int = 12,
    markerDotCount: Int = 7
) {
    val density = LocalDensity.current

    val lo = min(minValue, maxValue)
    val hi = max(minValue, maxValue)

    val rs = rangeStart.coerceIn(lo, hi)
    val re = rangeEnd.coerceIn(lo, hi)
    val r0 = min(rs, re)
    val r1 = max(rs, re)

    val rv = recentValue.coerceIn(lo, hi)

    fun ratioOf(v: Float): Float {
        val denom = (hi - lo)
        val t = if (denom == 0f) 0f else (v - lo) / denom
        return t.coerceIn(0f, 1f)
    }

    val titleText = "${trim0(r0)}–${trim0(r1)} $unit"
    val markerT = ratioOf(rv) // ✅ recentValue 위치 비율 (0..1)

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
                .padding(horizontal = 18.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titleText,
                fontSize = titleFontSize.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val fullW = maxWidth
                val textHalf = 12.dp // "75" 대충 절반 폭

                Box(modifier = Modifier.fillMaxWidth()) {
                    // Gauge bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(RoundedCornerShape(999.dp))
                            .background(trackColor)
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            fun vToX(v: Float): Float = ratioOf(v) * w

                            val leftX = vToX(r0)
                            val rightX = vToX(r1)
                            val segW = (rightX - leftX).coerceAtLeast(0f)

                            drawRoundRect(
                                color = rangeColor,
                                topLeft = Offset(leftX, 0f),
                                size = Size(segW, h),
                                cornerRadius = CornerRadius(h / 2f, h / 2f)
                            )

                            val markerX = vToX(rv)
                            val dotRadius = 1.dp.toPx()
                            val gap = (h - dotRadius * 2f) / (markerDotCount + 1)

                            repeat(markerDotCount) { i ->
                                val cy = gap * (i + 1) + dotRadius
                                drawCircle(
                                    color = Color.White,
                                    radius = dotRadius,
                                    center = Offset(markerX, cy)
                                )
                            }
                        }
                    }

                    // ✅ 75 aligned to marker
                    Text(
                        text = trim0(rv),
                        color = rangeColor,
                        fontSize = recentValueFontSize.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(
                                x = (fullW * markerT) - textHalf,
                                y = (barHeight + 6.dp) // bar 아래 살짝
                            )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ✅ min/max는 좌우로만
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trim0(lo),
                    color = textGray,
                    fontSize = bottomFontSize.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = trim0(hi),
                    color = textGray,
                    fontSize = bottomFontSize.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = recentLabel,
                color = rangeColor,
                fontSize = subtitleFontSize.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun trim0(v: Float): String {
    val i = v.toInt()
    return if (kotlin.math.abs(v - i) < 0.0001f)
        i.toString()
    else "%.1f".format(v)
}
