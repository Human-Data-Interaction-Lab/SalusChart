package com.hdil.saluschart.ui.compose.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.RangeChartMark
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.TooltipSpec
import kotlin.math.max
import kotlin.math.min

private fun hourDecimalToHm(
    value: Double,
    minX: Double,
    maxX: Double
): String {
    val range = maxX - minX

    // normalize into [minX, maxX)
    var v = value
    while (v < minX) v += range
    while (v >= maxX) v -= range

    val totalMinutes = kotlin.math.floor((v - minX) * 60.0).toInt().coerceAtLeast(0)
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return "%d:%02d".format(h, m)
}

/**
 * Horizontal Range Bar Chart (sleep-style).
 *
 * Interprets:
 *   mark.minPoint.y and mark.maxPoint.y as the horizontal axis values (e.g., hours in [0..8] or [0..24]).
 *
 * Transparent background. Good rows blue, bad rows grey.
 * 2 vertical "pillars" and dashed guide lines.
 * Includes bottom axis labels and tap tooltip.
 */
@Composable
fun HorizontalRangeBarChart(
    modifier: Modifier = Modifier,
    title: String? = null,
    data: List<RangeChartMark>,
    datePeriodText: String? = null,

    // Value axis range (ex: 0..8 or 0..24)
    minX: Double,
    maxX: Double,

    // Row labels (left)
    rowLabels: List<String> = data.mapIndexed { i, _ -> (i + 1).toString() },
    leftLabelWidth: Dp = 36.dp,
    leftLabelColor: Color = MaterialTheme.colorScheme.onBackground,

    // Layout
    rowHeight: Dp = 42.dp,
    rowSpacing: Dp = 12.dp,
    barThickness: Dp = 10.dp,
    barCornerRadius: Dp = 999.dp, // pill
    chartStartPadding: Dp = 18.dp,
    chartEndPadding: Dp = 22.dp,
    labelTextSizeSp: Float = 14f,

    // Good/bad styling
    goodColor: Color = Color(0xFF6E86FF),
    badColor: Color = Color(0xFFD6D6D6),
    isGood: (RangeChartMark) -> Boolean = { true },

    // Guides (vertical pillars + dashed lines)
    showGuides: Boolean = true,
    guideStartX: Double = minX,
    guideEndX: Double = maxX,
    pillarWidth: Dp = 30.dp,
    pillarFill: Color = Color(0xFF6E86FF).copy(alpha = 0.06f),
    dashColor: Color = Color(0xFF6E86FF).copy(alpha = 0.55f),
    dashWidth: Dp = 1.dp,
    dashOn: Float = 6f,
    dashOff: Float = 6f,

    // Bottom axis labels
    bottomStartLabel: String = "",
    bottomEndLabel: String = "",
    bottomLabelTopPadding: Dp = 10.dp,
    bottomLabelTextColor: Color = Color(0xFF6E86FF),

    // Tooltip
    enableTooltip: Boolean = true,
    unit: String = "시간",

    ) {
    if (data.isEmpty()) return
    require(maxX > minX) { "maxX must be > minX" }
    require(rowLabels.size >= data.size) { "rowLabels must have >= data.size" }

    val density = LocalDensity.current

    // Tooltip state
    var tooltipSpec by remember { mutableStateOf<TooltipSpec?>(null) }
    var tooltipUnit by remember { mutableStateOf("시간") }


    // Chart box width for tooltip edge clamping
    var chartBoxSize by remember { mutableStateOf(IntSize.Zero) }

    // Layout height
    val chartHeightDp = with(density) {
        val rowH = rowHeight.toPx()
        val sp = rowSpacing.toPx()
        val h = data.size * rowH + (data.size - 1).coerceAtLeast(0) * sp
        h.toDp()
    }

    Column(modifier = modifier) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
        }

        datePeriodText?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            )
        }

        // Main area (labels + chart)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeightDp),
            verticalAlignment = Alignment.Top
        ) {
            // Left labels (same row math as bars)
            Canvas(
                modifier = Modifier
                    .width(leftLabelWidth)
                    .fillMaxHeight()
            ) {
                val rowHpx = with(density) { rowHeight.toPx() }
                val rowSpPx = with(density) { rowSpacing.toPx() }
                val stride = rowHpx + rowSpPx

                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = leftLabelColor.toArgb()
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = with(density) { labelTextSizeSp.sp.toPx() }
                }

                data.indices.forEach { i ->
                    val label = rowLabels[i]
                    val top = i * stride
                    val centerY = top + rowHpx / 2f
                    val x = size.width / 2f
                    val baseline = centerY - (paint.descent() + paint.ascent()) / 2f
                    drawContext.canvas.nativeCanvas.drawText(label, x, baseline, paint)
                }
            }

            // Chart area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = chartStartPadding, end = chartEndPadding)
                    .onSizeChanged { chartBoxSize = it }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(enableTooltip, data) {
                            if (!enableTooltip) return@pointerInput
                            detectTapGestures(
                                onTap = { pos ->
                                    val rowHpx = with(density) { rowHeight.toPx() }
                                    val rowSpPx = with(density) { rowSpacing.toPx() }
                                    val stride = rowHpx + rowSpPx

                                    val idx = (pos.y / stride).toInt()
                                    if (idx !in data.indices) {
                                        tooltipSpec = null
                                        return@detectTapGestures
                                    }

                                    // toggle (tap same row closes)
                                    val already = tooltipSpec?.chartMark?.x?.toInt() == idx
                                    if (already) {
                                        tooltipSpec = null
                                        return@detectTapGestures
                                    }

                                    val mark = data[idx]
                                    val rowCenterY = idx * stride + rowHpx / 2f

                                    val start = mark.minPoint.y
                                    val end = mark.maxPoint.y

                                    val range = maxX - minX              // usually 24.0
                                    val raw = end - start
                                    val dur = if (raw >= 0) raw else raw + range   // wrap midnight

                                    // Convert to minutes
                                    val totalMinutes = kotlin.math.floor(dur * 60.0).toInt().coerceAtLeast(0)
                                    val hours = totalMinutes / 60
                                    val minutes = totalMinutes % 60

                                    tooltipUnit = if (hours == 0) {
                                        "${minutes}분"
                                    } else {
                                        if (minutes == 0) "시간" else "시간 ${minutes}분"
                                    }

                                    tooltipSpec = TooltipSpec(
                                        chartMark = ChartMark(
                                            x = idx.toDouble(),
                                            y = if (hours == 0) minutes.toDouble() else hours.toDouble(), // number line
                                            label = "${hourDecimalToHm(start, minX, maxX)}–${hourDecimalToHm(end, minX, maxX)}"
                                        ),
                                        offset = Offset(pos.x, rowCenterY)
                                    )
                                }
                            )
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    fun xToPx(x: Double): Float {
                        val t = ((x - minX) / (maxX - minX)).toFloat().coerceIn(0f, 1f)
                        return t * w
                    }

                    val rowHpx = with(density) { rowHeight.toPx() }
                    val rowSpPx = with(density) { rowSpacing.toPx() }
                    val barThPx = with(density) { barThickness.toPx() }
                    val rPx = with(density) { barCornerRadius.toPx() }

                    // Guides: two slim pillars + dashed lines
                    if (showGuides) {
                        val x1 = xToPx(guideStartX)
                        val x2 = xToPx(guideEndX)

                        val pillW = with(density) { pillarWidth.toPx() }
                        val padY = 6.dp.toPx()
                        val dash = PathEffect.dashPathEffect(floatArrayOf(dashOn, dashOff), 0f)

                        fun drawPillar(centerX: Float) {
                            val left = (centerX - pillW / 1.5f).coerceIn(0f, w)
                            val right = (centerX + pillW / 1.5f).coerceIn(0f, w)
                            val rect = Rect(left, padY, right, h - padY)

                            drawRoundRect(
                                color = pillarFill,
                                topLeft = Offset(rect.left, rect.top),
                                size = rect.size,
                                cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                            )

                            val cx = (rect.left + rect.right) / 2f
                            drawLine(
                                color = dashColor,
                                start = Offset(cx, rect.top),
                                end = Offset(cx, rect.bottom),
                                strokeWidth = with(density) { dashWidth.toPx() },
                                pathEffect = dash
                            )
                        }

                        drawPillar(x1)
                        drawPillar(x2)
                    }

                    // Bars
                    data.forEachIndexed { i, mark ->
                        val top = i * (rowHpx + rowSpPx)
                        val centerY = top + rowHpx / 2f

                        val a = mark.minPoint.y
                        val b = mark.maxPoint.y
                        val leftX = xToPx(min(a, b))
                        val rightX = xToPx(max(a, b))

                        val c = if (isGood(mark)) goodColor else badColor

                        drawRoundRect(
                            color = c,
                            topLeft = Offset(leftX, centerY - barThPx / 2f),
                            size = androidx.compose.ui.geometry.Size(
                                (rightX - leftX).coerceAtLeast(1f),
                                barThPx
                            ),
                            cornerRadius = CornerRadius(rPx, rPx)
                        )
                    }
                }

                // Tooltip overlay
                tooltipSpec?.let { spec ->
                    val parentWidthPx = chartBoxSize.width.toFloat()

                    // instant + smooth: estimated width
                    val estimatedWidthPx = with(density) { 160.dp.toPx() }
                    var measuredWidthPx by remember(spec) { mutableStateOf<Float?>(null) }
                    val tooltipWidthPx = measuredWidthPx ?: estimatedWidthPx
                    var isMeasured by remember(spec) { mutableStateOf(false) }

                    val anchorXPx = spec.offset.x
                    val anchorYPx = spec.offset.y
                    val gapPx = with(density) { 8.dp.toPx() }

                    val wouldOverflowRight = anchorXPx + tooltipWidthPx + gapPx > parentWidthPx
                    val targetXPx = if (wouldOverflowRight) {
                        anchorXPx - tooltipWidthPx - gapPx
                    } else {
                        anchorXPx + gapPx
                    }

                    val animatedX by animateFloatAsState(
                        targetValue = if (isMeasured) targetXPx else targetXPx, // value doesn't matter while hidden
                        label = "rangeTooltipX"
                    )

                    val visible = isMeasured

                    val alpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        label = "tooltipAlpha"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (visible) 1f else 0.98f,
                        label = "tooltipScale"
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .zIndex(999f)
                    ) {
                        ChartTooltip(
                            chartMark = spec.chartMark,
                            unit = tooltipUnit,
                            color = goodColor,
                            modifier = Modifier
                                .offset(
                                    x = with(density) { animatedX.toDp() },
                                    y = with(density) { anchorYPx.toDp() } - 80.dp
                                )
                                .graphicsLayer {
                                    this.alpha = alpha
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .onSizeChanged {
                                    measuredWidthPx = it.width.toFloat()
                                    isMeasured = true
                                }
                        )
                    }
                }
            }
        }

        // Bottom axis labels row (aligned with chart area)
        if (bottomStartLabel.isNotBlank() || bottomEndLabel.isNotBlank()) {
            Spacer(Modifier.height(bottomLabelTopPadding))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Spacer(Modifier.width(leftLabelWidth))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = chartStartPadding, end = chartEndPadding)
                        .heightIn(min = 40.dp)
                ) {
                    if (bottomStartLabel.isNotBlank()) {
                        Text(
                            text = bottomStartLabel,
                            color = bottomLabelTextColor,
                            modifier = Modifier.align(Alignment.TopStart),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                    if (bottomEndLabel.isNotBlank()) {
                        Text(
                            text = bottomEndLabel,
                            color = bottomLabelTextColor,
                            modifier = Modifier.align(Alignment.TopEnd),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
