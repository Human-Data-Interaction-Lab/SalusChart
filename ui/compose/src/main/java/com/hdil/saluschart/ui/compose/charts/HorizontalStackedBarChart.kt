package com.hdil.saluschart.ui.compose.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import kotlin.math.abs
import kotlin.math.max

data class HorizontalStackedBarRow(
    val title: String,
    val unit: String,
    val total: Float,
    val segments: List<Float>,
    val segmentLabels: List<String> = emptyList(),
    val trackMax: Float? = null
)

private data class TooltipUiState(
    val mark: ChartMark,
    val anchorInRootPx: Offset,
    val dotColor: Color
)

@Composable
fun HorizontalStackedBarChartList(
    modifier: Modifier = Modifier,
    title: String,
    datePeriodText: String? = null,
    rows: List<HorizontalStackedBarRow>,
    colors: List<Color> = listOf(
        Color(0xFF20C95A),
        Color(0xFF0FA958),
        Color(0xFFFF8A3D)
    ),
    barTrackColor: Color = Color(0xFFF1F1F1),
    onRowClick: ((Int, Int?, Float) -> Unit)? = null
) {
    val density = LocalDensity.current

    // Root (overlay) geometry in WINDOW coords
    var rootTopLeftOnScreen by remember { mutableStateOf(Offset.Zero) }
    var rootSize by remember { mutableStateOf(IntSize.Zero) }

    // Tooltip
    var tooltip by remember { mutableStateOf<TooltipUiState?>(null) }
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipMeasuredOnce by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .onSizeChanged { rootSize = it }
            .onGloballyPositioned { coords ->
                rootTopLeftOnScreen = coords.localToWindow(Offset.Zero)
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            datePeriodText?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))

            rows.forEachIndexed { rowIndex, row ->
                Column(modifier = Modifier.padding(vertical = 12.dp)) {

                    // Header
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(row.title, fontWeight = FontWeight.SemiBold)
                        Text("${formatExact2(row.total)} ${row.unit}", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(10.dp))

                    // Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(barTrackColor)
                    ) {
                        val segSum = row.segments.sum().coerceAtLeast(0f)
                        val base = if (row.total > 0f) row.total else segSum
                        val trackMax = (row.trackMax ?: base).coerceAtLeast(1f)

                        // total fill ratio (can be < 1)
                        val fillRatio = (base / trackMax).coerceIn(0f, 1f)

                        Row(Modifier.fillMaxSize()) {
                            // Left portion is the "filled region", right portion remains grey
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(fillRatio.coerceAtLeast(0.0001f), fill = true)
                            ) {
                                // inside filled region: stacked segments
                                Row(Modifier.fillMaxSize()) {
                                    row.segments.forEachIndexed { segIndex, segValue ->
                                        if (segValue <= 0f) return@forEachIndexed

                                        val segLabel = row.segmentLabels.getOrNull(segIndex) ?: row.title
                                        val segColor = colors.getOrElse(segIndex) { Color.Gray }

                                        // Segment top-left in WINDOW coords
                                        var segTopLeftOnScreen by remember(rowIndex, segIndex) { mutableStateOf(Offset.Zero) }

                                        val weightInFilled = (segValue / base.coerceAtLeast(1f))
                                            .coerceAtLeast(0.0001f)

                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(weightInFilled, fill = true)
                                                .background(segColor)
                                                .onGloballyPositioned { coords ->
                                                    segTopLeftOnScreen = coords.localToWindow(Offset.Zero)
                                                }
                                                .pointerInput(rowIndex, segIndex, segValue) {
                                                    detectTapGestures { localTap ->
                                                        onRowClick?.invoke(rowIndex, segIndex, segValue)

                                                        // Convert: segment-local tap -> WINDOW -> ROOT(overlay) local
                                                        val tapOnScreen = segTopLeftOnScreen + localTap
                                                        val tapInRoot = tapOnScreen - rootTopLeftOnScreen

                                                        // toggle
                                                        val same = tooltip?.mark?.let { m ->
                                                            m.x.toInt() == rowIndex && m.label == segLabel && abs(m.y.toFloat() - segValue) < 0.0001f
                                                        } == true

                                                        tooltipMeasuredOnce = false // prevent visible jump
                                                        tooltip = if (same) {
                                                            null
                                                        } else {
                                                            TooltipUiState(
                                                                mark = ChartMark(
                                                                    x = rowIndex.toDouble(),
                                                                    y = segValue.toDouble(),
                                                                    label = segLabel
                                                                ),
                                                                anchorInRootPx = tapInRoot,
                                                                dotColor = segColor
                                                            )
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                }
                            }

                            // Grey remainder (unfilled)
                            val remainder = (1f - fillRatio).coerceAtLeast(0f)
                            if (remainder > 0.0001f) {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(remainder, fill = true)
                                )
                            }
                        }
                    }

                    // Breakdown (optional)
                    if (row.segmentLabels.isNotEmpty() && row.segmentLabels.size == row.segments.size) {
                        Spacer(Modifier.height(10.dp))
                        row.segmentLabels.zip(row.segments).forEach { (label, value) ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = Color.Gray, fontSize = 12.sp)
                                Text("${formatExact2(value)} ${row.unit}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        tooltip?.let { t ->
            val rootW = rootSize.width.toFloat().coerceAtLeast(1f)
            val rootH = rootSize.height.toFloat().coerceAtLeast(1f)

            val tipW = tooltipSize.width.toFloat().takeIf { it > 0f } ?: with(density) { 160.dp.toPx() }
            val tipH = tooltipSize.height.toFloat().takeIf { it > 0f } ?: with(density) { 72.dp.toPx() }

            val gapPx = with(density) { 8.dp.toPx() }

            val preferRight = t.anchorInRootPx.x + tipW + gapPx <= rootW
            var xPx = if (preferRight) t.anchorInRootPx.x + gapPx else t.anchorInRootPx.x - tipW - gapPx
            xPx = xPx.coerceIn(0f, max(0f, rootW - tipW))

            val preferAbove = t.anchorInRootPx.y - tipH - gapPx >= 0f
            var yPx = if (preferAbove) t.anchorInRootPx.y - tipH - gapPx else t.anchorInRootPx.y + gapPx
            yPx = yPx.coerceIn(0f, max(0f, rootH - tipH))

            val alpha by animateFloatAsState(
                targetValue = if (tooltipMeasuredOnce) 1f else 0f,
                label = "tooltipAlpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(999f)
            ) {
                ChartTooltip(
                    ChartMark = t.mark,
                    unit = rows.getOrNull(t.mark.x.toInt())?.unit ?: "",
                    color = t.dotColor,
                    modifier = Modifier
                        .offset(
                            x = with(density) { xPx.toDp() },
                            y = with(density) { yPx.toDp() }
                        )
                        .onSizeChanged {
                            tooltipSize = it
                            tooltipMeasuredOnce = true
                        }
                        .graphicsLayer { this.alpha = alpha }
                )
            }
        }
    }
}

private fun formatExact2(v: Float): String = "%,.2f".format(v)
