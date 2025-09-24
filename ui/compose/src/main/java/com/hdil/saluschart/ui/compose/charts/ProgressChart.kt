package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartType
import com.hdil.saluschart.core.chart.ProgressChartPoint
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.ui.theme.ColorUtils
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * 프로그레스 차트를 표시하는 컴포저블 함수입니다.
 * 
 * @param modifier 모디파이어
 * @param data 프로그레스 차트에 표시할 데이터 리스트
 * @param title 차트 제목
 * @param isDonut 도넛 차트로 표시할지 여부 (true: 도넛, false: 바)
 * @param colors 각 프로그레스 인스턴스에 사용할 색상 목록
 * @param strokeWidth 도넛 차트의 링 두께 (도넛 모드일 때만)
 * @param barHeight 바 차트의 각 바 높이 (바 모드일 때만)
 * @param showLabels 라벨을 표시할지 여부
 * @param showValues 값을 표시할지 여부
 * @param showCenterInfo 중앙 정보를 표시할지 여부 (도넛 모드일 때만)
 * @param centerTitle 중앙 제목 텍스트 (도넛 모드일 때만)
 * @param centerSubtitle 중앙 부제목 텍스트 (도넛 모드일 때만)
 * @param chartType 차트 타입 (툴팁 위치 결정용)
 */
@Composable
fun ProgressChart(
    modifier: Modifier = Modifier,
    data: List<ProgressChartPoint>,
    title: String = "Progress Chart",
    isDonut: Boolean = true,
    isPercentage: Boolean = true,
    colors: List<Color> = ColorUtils.ColorUtils(data.size.coerceAtLeast(1)),
    strokeWidth: Float = 80f,
    barHeight: Float = 36f,
    showLabels: Boolean = true,
    showValues: Boolean = true,
    showCenterInfo: Boolean = true,
    centerTitle: String = "Activity",
    centerSubtitle: String = "Progress",
    chartType: ChartType = ChartType.PROGRESS, // 차트 타입 (툴팁 위치 결정용)
    barSpacing: Float = 16f,
    topPadding: Float = 8f,
    bottomPadding: Float = 8f,
    showLegend: Boolean = true,
    legendTitle: String? = null,
    legendColorBoxSize: Dp = 10.dp,
    legendTextSize: TextUnit = 12.sp,
    legendSpacing: Dp = 8.dp,
    legendPadding: Dp = 8.dp
) {
    if (data.isEmpty()) return
    val legendLabels = data.mapIndexed { i, p -> p.label ?: "항목 ${i + 1}" }
    // Tap state for donut interaction
    var tappedIndex by remember { mutableStateOf<Int?>(null) }
    var tapOffset   by remember { mutableStateOf<Offset?>(null) }

    Column(
        modifier = modifier
            .padding(8.dp, vertical = 12.dp)
            .pointerInput(tappedIndex) {
                if (tappedIndex != null) {
                    detectTapGestures {
                        tappedIndex = null
                        tapOffset = null
                    }
                }
            }
    ) {
    Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(20.dp))

        // Only for BAR mode we constrain height
        val canvasModifier = if (isDonut) {
            Modifier
                .fillMaxWidth()
                .height(260.dp)
        } else {
            // Convert px -> dp
            val contentHeightPx =
                topPadding +
                        bottomPadding +
                        data.size * barHeight +
                        (data.size - 1).coerceAtLeast(0) * barSpacing

            val contentHeightDp = with(LocalDensity.current) { contentHeightPx.toDp() }
            Modifier
                .fillMaxWidth()
                .height(contentHeightDp)
        }

        var boxSize by remember { mutableStateOf(IntSize.Zero) }

        Box(
            canvasModifier
                .padding(start = 24.dp)
                .onGloballyPositioned { boxSize = it.size }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(data, isDonut, strokeWidth) {
                        detectTapGestures { pos ->
                            if (!isDonut) return@detectTapGestures

                            // Convert IntSize -> Size (Float)
                            val canvasSize = Size(this.size.width.toFloat(), this.size.height.toFloat())

                            val (center, _, ringRadii) =
                                ChartMath.Progress.computeProgressDonutMetrics(
                                    size = canvasSize,
                                    data = data,
                                    strokeWidth = strokeWidth
                                )

                            val d = hypot(pos.x - center.x, pos.y - center.y)
                            val half = strokeWidth / 2f
                            val hitIndex = ringRadii.indexOfFirst { r -> d in (r - half)..(r + half) }

                            if (hitIndex == -1) {
                                tappedIndex = null
                                tapOffset = null
                                return@detectTapGestures
                            }

                            // (optional) angle check – safe to keep or remove
                            val angleDeg = ((Math.toDegrees(
                                atan2(pos.y - center.y, pos.x - center.x).toDouble()
                            ) + 360.0) % 360.0).toFloat()

                            // Use the same start angle you use to draw arcs
                            val startAt = -90f

                            // Current point & raw progress (allow >100%)
                            val pt = data[hitIndex]
                            val raw = if (pt.max > 0f) pt.current / pt.max else pt.progress

                            // If no progress at all, dismiss
                            if (raw <= 0f) {
                                tappedIndex = null
                                tapOffset = null
                                return@detectTapGestures
                            }

                            // Decompose into full laps + residual (exactly how you draw)
                            val laps = kotlin.math.floor(raw).toInt().coerceAtLeast(0)
                            val residualDeg = ((raw - laps).coerceIn(0.0, 1.0)) * 360.0

                            // hit-test helper for circular sweeps
                            fun containsAngle(start: Float, sweep: Float, target: Float): Boolean {
                                val s = ((start % 360f) + 360f) % 360f
                                val e = ((s + sweep) % 360f + 360f) % 360f
                                return if (sweep >= 0f) {
                                    if (s <= e) target in s..e else target >= s || target <= e
                                } else {
                                    if (e <= s) target in e..s else target >= e || target <= s
                                }
                            }

                            // Accept the tap if it falls on ANY of the drawn progress sweeps
                            var inside = false

                            // 1) Any completed laps (you draw a ~360° arc for them)
                            if (laps >= 1) {
                                // Use the same sweep you use for the lap (e.g., 359.6f) so the seam is hidden
                                inside = inside || containsAngle(startAt, 359.6f, angleDeg)
                                // If you ever support 200%+, you could repeat this check, but since each lap
                                // starts at the same startAt, one check is sufficient visually.
                            }

                            // 2) Residual (remaining) sweep on top
                            if (residualDeg > 0f) {
                                inside = inside || containsAngle(startAt, residualDeg.toFloat(), angleDeg)
                            }

                            if (inside) {
                                tappedIndex = hitIndex
                                tapOffset = pos
                            } else {
                                tappedIndex = null
                                tapOffset = null
                            }

                        }
                    }
            ) {
                // 프로그레스 마크 그리기 (도넛 또는 바)
                ChartDraw.Progress.drawProgressMarks(
                    drawScope = this,
                    data = data,
                    size = size,
                    colors = colors,
                    isDonut = isDonut,
                    strokeWidth = strokeWidth,
                    barHeight = barHeight,
                    barSpacing = barSpacing,
                    topPadding = topPadding,
                    cornerRadius = barHeight / 2f
                )
                
                // 중앙 정보 표시 (도넛 모드일 때만)
                if (isDonut && showCenterInfo) {
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    ChartDraw.Progress.drawProgressCenterInfo(
                        drawScope = this,
                        center = center,
                        title = centerTitle,
                        subtitle = centerSubtitle
                    )
                }
                
                // 라벨 표시
                if (showLabels) {
                    ChartDraw.Progress.drawProgressLabels(
                        drawScope = this,
                        data = data,
                        size = size,
                        isDonut = isDonut,
                        strokeWidth = strokeWidth,
                        barHeight = barHeight,
                        barSpacing = barSpacing,
                        topPadding = topPadding
                    )
                }
                
                // 값 표시
                if (showValues) {
                    ChartDraw.Progress.drawProgressValues(
                        drawScope = this,
                        data = data,
                        size = size,
                        isDonut = isDonut,
                        strokeWidth = strokeWidth,
                        barHeight = barHeight,
                        isPercentage = isPercentage,
                        barSpacing = barSpacing,
                        topPadding = topPadding
                    )
                }
            }
            // Tooltip overlay
            tappedIndex?.let { i ->
                tapOffset?.let { pos ->
                    val density = LocalDensity.current

                    val tipWidthDp = 180.dp     // expected tooltip width
                    val tipHeightDp = 72.dp     // expected tooltip height
                    val marginDp = 12.dp

                    val tipW = with(density) { tipWidthDp.toPx() }
                    val tipH = with(density) { tipHeightDp.toPx() }
                    val margin = with(density) { marginDp.toPx() }

                    // Base position (above finger)
                    var px = pos.x
                    var py = pos.y - 80f

                    // Clamp horizontally
                    val maxX = (boxSize.width - tipW - margin).coerceAtLeast(margin)
                    px = px.coerceIn(margin, maxX)

                    // Clamp vertically
                    val maxY = (boxSize.height - tipH - margin).coerceAtLeast(margin)
                    py = py.coerceIn(margin, maxY)

                    val xDp = with(density) { px.toDp() }
                    val yDp = with(density) { py.toDp() }

                    // Build value like "1200 KJ / 2000 KJ"
                    val point = data[i]
                    val unitSuffix = buildString {
                        append(" ")
                        point.unit?.let { append(it).append(" ") }   // "KJ "
                        append("/ ")
                        append(point.max.toInt())
                        point.unit?.let { append(" ").append(it) }   // " 2000 KJ"
                    }

                    // Use a proxy ChartPoint so tooltip shows 'current' (not ratio)
                    val tooltipPoint = com.hdil.saluschart.core.chart.ChartPoint(
                        x = i.toDouble(),
                        y = point.current,
                        label = point.label
                    )

                    ChartTooltip(
                        chartPoint = tooltipPoint,
                        unit = unitSuffix,
                        modifier = Modifier.offset(x = xDp, y = yDp),
                        color = colors[i]
                    )
                }
            }
        }

        // Legend
        if (showLegend) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ChartLegend(
                    modifier = Modifier.padding(vertical = legendPadding),
                    labels = legendLabels,
                    colors = colors,
                    position = LegendPosition.BOTTOM,
                    title = legendTitle,
                    colorBoxSize = legendColorBoxSize,
                    textSize = legendTextSize,
                    spacing = legendSpacing
                )
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}
