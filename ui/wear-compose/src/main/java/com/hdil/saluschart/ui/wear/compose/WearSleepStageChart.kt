package com.hdil.saluschart.ui.wear.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.chartDraw.TooltipContainer
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import com.hdil.saluschart.data.model.model.SleepStageType
import com.hdil.saluschart.ui.theme.LocalSalusChartColors
import com.hdil.saluschart.ui.theme.SalusChartColorScheme
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

@Composable
fun WearSleepStageChart(
    modifier: Modifier = Modifier,
    sleepSession: SleepSession,
    title: String = "Sleep Stage Analysis",
    showLabels: Boolean = true,
    showXAxis: Boolean = true,
    showXAxisLabels: Boolean = showXAxis,
    onStageClick: ((Int, String) -> Unit)? = null,
    barHeightRatio: Float = 0.5f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(
        horizontal = 12.dp,
        vertical = 8.dp
    ),
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showYAxisLabels: Boolean = showYAxis,
    showStartEndLabels: Boolean = true,
    xLabelAutoSkip: Boolean = true,
    yAxisFixedWidth: Dp = 36.dp
) {
    if (sleepSession.stages.isEmpty()) return

    var selectedVisual by remember { mutableStateOf<SelectedStageVisual?>(null) }
    val mergedStages = remember(sleepSession.stages) { mergeConsecutiveStages(sleepSession.stages) }

    val palette = LocalSalusChartColors.current.palette
    val deepColor = palette.getOrElse(0) { Color(0xFF3A2B96) }
    val coreColor = palette.getOrElse(1) { Color(0xFF0099FF) }
    val remColor = palette.getOrElse(2) { Color(0xFF00D4FF) }
    val awakeColor = palette.getOrElse(3) { Color(0xFFFF4D4F) }
    val stageLabels = listOf("Deep", "Core", "REM", "Awake")

    fun stageIndex(type: SleepStageType): Int = when (type) {
        SleepStageType.DEEP -> 0
        SleepStageType.LIGHT -> 1
        SleepStageType.REM -> 2
        SleepStageType.AWAKE -> 3
        SleepStageType.UNKNOWN -> 1
    }

    val minX = mergedStages.minOf { it.startTime.toEpochMilli().toDouble() }
    val maxX = max(minX + 1.0, mergedStages.maxOf { it.endTime.toEpochMilli().toDouble() })

    val zone = ZoneId.systemDefault()
    val minInstant = Instant.ofEpochMilli(minX.toLong())
    val maxInstantRaw = Instant.ofEpochMilli(maxX.toLong())
    val padMinutes = 30L
    val domainMinInstant = minInstant
    val domainMaxInstant = maxInstantRaw.plusSeconds(padMinutes * 60)
    val domainMinX = domainMinInstant.toEpochMilli().toDouble()
    val domainMaxX = domainMaxInstant.toEpochMilli().toDouble()
    val tickInstants = (0..4).map { i ->
        val ratio = i / 4.0
        val millis = domainMinX + ratio * (domainMaxX - domainMinX)
        Instant.ofEpochMilli(millis.toLong())
    }
    val tickFormatter = DateTimeFormatter.ofPattern("h a").withZone(zone)

    Column(modifier = modifier.padding(contentPadding)) {
        if (showTitle) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WearChartDefaults.CompactChartHeight)
        ) {
            val density = LocalDensity.current

            val leftInsetPx = if (showYAxisLabels && yAxisPosition == YAxisPosition.LEFT) {
                with(density) { yAxisFixedWidth.toPx() }
            } else 0f

            val rightInsetPx = if (showYAxisLabels && yAxisPosition == YAxisPosition.RIGHT) {
                with(density) { (yAxisFixedWidth + 8.dp).toPx() }
            } else 0f

            Canvas(modifier = Modifier.fillMaxSize()) {
                val stageCount = stageLabels.size
                val rowHeight = size.height / stageCount.toFloat()
                val gridColor = Color.Black.copy(alpha = 0.22f)
                val horizontalStroke = 1.2f
                val verticalStroke = 1.1f
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                for (i in 0 until stageCount) {
                    val y = size.height - i * rowHeight
                    drawLine(
                        color = gridColor.copy(alpha = if (i == 0) 0.28f else 0.16f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = horizontalStroke
                    )
                }

                val chartLeft = leftInsetPx
                val chartRight = size.width - rightInsetPx
                val chartWidthPx = chartRight - chartLeft

                drawLine(
                    color = gridColor.copy(alpha = 0.28f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = verticalStroke
                )

                drawLine(
                    color = gridColor.copy(alpha = 0.28f),
                    start = Offset(chartRight, 0f),
                    end = Offset(chartRight, size.height),
                    strokeWidth = verticalStroke
                )

                if (tickInstants.size > 2) {
                    for (i in 1 until tickInstants.lastIndex) {
                        val tMillis = tickInstants[i].toEpochMilli().toDouble()
                        val ratio = ((tMillis - domainMinX) / (domainMaxX - domainMinX)).toFloat()
                        val x = chartLeft + ratio * chartWidthPx
                        drawLine(
                            color = gridColor.copy(alpha = 0.18f),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = verticalStroke,
                            pathEffect = dashEffect
                        )
                    }
                }
            }

            Row(modifier = Modifier.matchParentSize()) {
                if (showYAxisLabels && yAxisPosition == YAxisPosition.LEFT) {
                    Column(
                        modifier = Modifier
                            .width(yAxisFixedWidth)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        stageLabels.reversed().forEach { label ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(top = 2.dp, start = 2.dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(mergedStages, domainMinX, domainMaxX) {
                            detectTapGestures { tapOffset ->
                                val chartWidth = size.width.toFloat()
                                val chartHeight = size.height.toFloat()
                                val stageCount = stageLabels.size
                                val rowHeight = chartHeight / stageCount.toFloat()
                                val capsuleHeight = rowHeight * barHeightRatio.coerceIn(0.3f, 0.8f)

                                fun toX(ms: Double): Float {
                                    val ratio = ((ms - domainMinX) / (domainMaxX - domainMinX)).toFloat()
                                    return ratio * chartWidth
                                }

                                fun centerYForStageIndex(index: Int): Float {
                                    val fromBottom = index + 0.5f
                                    return chartHeight - fromBottom * rowHeight
                                }

                                var hit: SelectedStageVisual? = null

                                for (stage in mergedStages) {
                                    val idx = stageIndex(stage.stage)
                                    val startMs = stage.startTime.toEpochMilli().toDouble()
                                    val endMs = stage.endTime.toEpochMilli().toDouble()
                                    val startX = toX(startMs)
                                    val endX = toX(endMs)
                                    val midX = (startX + endX) / 2f
                                    val centerY = centerYForStageIndex(idx)
                                    val top = centerY - capsuleHeight / 2f
                                    val bottom = centerY + capsuleHeight / 2f

                                    if (tapOffset.x in startX..endX && tapOffset.y in top..bottom) {
                                        hit = SelectedStageVisual(stage, idx, midX, centerY, capsuleHeight)
                                        onStageClick?.invoke(idx, stageLabel(stage.stage))
                                        break
                                    }
                                }

                                selectedVisual = if (hit != null && selectedVisual?.stage == hit.stage) null else hit
                            }
                        }
                ) {
                    val densityLocal = LocalDensity.current

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val chartWidth = size.width
                        val chartHeight = size.height
                        val stageCount = stageLabels.size
                        val rowHeight = chartHeight / stageCount.toFloat()
                        val capsuleHeight = rowHeight * barHeightRatio.coerceIn(0.3f, 0.8f)
                        val minCapsuleWidth = capsuleHeight * 0.1f
                        val connectorWidth = capsuleHeight * 0.1f

                        fun toX(ms: Double): Float {
                            val ratio = ((ms - domainMinX) / (domainMaxX - domainMinX)).toFloat()
                            return ratio * chartWidth
                        }

                        fun centerYForStageIndex(index: Int): Float {
                            val fromBottom = index + 0.5f
                            return chartHeight - fromBottom * rowHeight
                        }

                        val pillGeoms = mergedStages.map { stage ->
                            val idx = stageIndex(stage.stage)
                            val startX = toX(stage.startTime.toEpochMilli().toDouble())
                            val endX = toX(stage.endTime.toEpochMilli().toDouble())
                            val rawWidth = (endX - startX).coerceAtLeast(2f)
                            val width = rawWidth.coerceAtLeast(minCapsuleWidth)
                            val midX = (startX + endX) / 2f
                            val centerY = centerYForStageIndex(idx)
                            PillGeom(stage, idx, midX, centerY, startX, endX, width)
                        }

                        fun colorForStage(type: SleepStageType): Color = when (type) {
                            SleepStageType.AWAKE -> awakeColor
                            SleepStageType.REM -> remColor
                            SleepStageType.LIGHT -> coreColor
                            SleepStageType.DEEP -> deepColor
                            SleepStageType.UNKNOWN -> coreColor
                        }

                        for (i in 0 until mergedStages.size - 1) {
                            val current = mergedStages[i]
                            val next = mergedStages[i + 1]
                            val idxCurrent = stageIndex(current.stage)
                            val idxNext = stageIndex(next.stage)
                            if (idxCurrent == idxNext) continue

                            val topIdx = max(idxCurrent, idxNext)
                            val bottomIdx = min(idxCurrent, idxNext)
                            val topCenterY = centerYForStageIndex(topIdx)
                            val bottomCenterY = centerYForStageIndex(bottomIdx)
                            val wrapExtra = capsuleHeight * 0.15f
                            val topY = topCenterY - capsuleHeight / 2f - wrapExtra
                            val bottomY = bottomCenterY + capsuleHeight / 2f + wrapExtra
                            val height = bottomY - topY
                            if (height <= 0f) continue

                            val transitionTimeMs = current.endTime.toEpochMilli().toDouble()
                            val x = toX(transitionTimeMs)

                            fun stageTypeForIndex(idx: Int): SleepStageType = when (idx) {
                                3 -> SleepStageType.AWAKE
                                2 -> SleepStageType.REM
                                1 -> SleepStageType.LIGHT
                                else -> SleepStageType.DEEP
                            }

                            val topStage = stageTypeForIndex(topIdx)
                            val bottomStage = stageTypeForIndex(bottomIdx)
                            val (topColor, bottomColor) = when {
                                topStage == SleepStageType.AWAKE && (bottomStage == SleepStageType.REM || bottomStage == SleepStageType.LIGHT) ->
                                    awakeColor to if (bottomStage == SleepStageType.REM) remColor else coreColor
                                topStage == SleepStageType.REM && bottomStage == SleepStageType.LIGHT ->
                                    remColor to coreColor
                                topStage == SleepStageType.LIGHT && bottomStage == SleepStageType.DEEP ->
                                    coreColor to deepColor
                                else -> {
                                    val base = colorForStage(bottomStage)
                                    base to base
                                }
                            }

                            val connectorBrush = Brush.verticalGradient(
                                colors = listOf(
                                    topColor.copy(alpha = 0.0f),
                                    topColor.copy(alpha = 0.20f),
                                    bottomColor.copy(alpha = 0.20f),
                                    bottomColor.copy(alpha = 0.0f)
                                ),
                                startY = topY,
                                endY = bottomY
                            )

                            drawRoundRect(
                                brush = connectorBrush,
                                topLeft = Offset(x - connectorWidth / 2f, topY),
                                size = Size(connectorWidth, height),
                                cornerRadius = CornerRadius(connectorWidth / 2f)
                            )
                        }

                        pillGeoms.forEach { geom ->
                            val baseColor = colorForStage(geom.stage.stage)
                            val capsuleStartX = geom.midX - geom.width / 2f
                            val top = geom.centerY - capsuleHeight / 2f
                            val haloPad = capsuleHeight * 0.1f
                            val haloLeft = capsuleStartX - haloPad
                            val haloTop = top - haloPad
                            val haloWidth = geom.width + haloPad * 1.5f
                            val haloHeight = capsuleHeight + haloPad * 1.5f

                            val haloBrush = Brush.verticalGradient(
                                colors = listOf(
                                    baseColor.copy(alpha = 0.0f),
                                    baseColor.copy(alpha = 0.20f),
                                    baseColor.copy(alpha = 0.20f),
                                    baseColor.copy(alpha = 0.0f)
                                ),
                                startY = haloTop,
                                endY = haloTop + haloHeight
                            )

                            drawRoundRect(
                                brush = haloBrush,
                                topLeft = Offset(haloLeft, haloTop),
                                size = Size(haloWidth, haloHeight),
                                cornerRadius = CornerRadius((capsuleHeight / 6f) + haloPad, (capsuleHeight / 6f) + haloPad)
                            )

                            drawRoundRect(
                                color = baseColor,
                                topLeft = Offset(capsuleStartX, top),
                                size = Size(geom.width, capsuleHeight),
                                cornerRadius = CornerRadius(capsuleHeight / 6f, capsuleHeight / 6f)
                            )
                        }
                    }

                    if (selectedVisual != null) {
                        val visual = selectedVisual!!
                        val stage = visual.stage
                        var hostSize by remember { mutableStateOf(IntSize.Zero) }
                        var tipSize by remember { mutableStateOf(IntSize.Zero) }
                        var tooltipMeasuredOnce by remember { mutableStateOf(false) }
                        val tooltipAlpha by animateFloatAsState(
                            targetValue = if (tooltipMeasuredOnce) 1f else 0f,
                            label = "tooltipAlpha"
                        )

                        val stageColor = when (stage.stage) {
                            SleepStageType.AWAKE -> Color(0xFFFF4D4F)
                            SleepStageType.REM -> Color(0xFF00D4FF)
                            SleepStageType.LIGHT -> Color(0xFF0099FF)
                            SleepStageType.DEEP -> Color(0xFF3A2B96)
                            SleepStageType.UNKNOWN -> Color(0xFF0099FF)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .onSizeChanged { hostSize = it }
                        ) {
                            val padPx = with(densityLocal) { 8.dp.toPx() }
                            val anchorX = visual.midX
                            val anchorY = visual.centerY - visual.capsuleHeight / 2f
                            val desiredX = anchorX + padPx
                            val desiredYAbove = anchorY - padPx - tipSize.height
                            val yPlaced = if (desiredYAbove < 0f) anchorY + padPx else desiredYAbove
                            val maxX = (hostSize.width - tipSize.width).coerceAtLeast(0)
                            val maxY = (hostSize.height - tipSize.height).coerceAtLeast(0)
                            val xClamped = desiredX.coerceIn(0f, maxX.toFloat())
                            val yClamped = yPlaced.coerceIn(0f, maxY.toFloat())

                            WearSleepStageTooltip(
                                stage = stage,
                                color = stageColor,
                                modifier = Modifier
                                    .offset { IntOffset(xClamped.toInt(), yClamped.toInt()) }
                                    .onSizeChanged {
                                        tipSize = it
                                        tooltipMeasuredOnce = true
                                    }
                                    .graphicsLayer { alpha = tooltipAlpha }
                            )
                        }
                    }
                }

                if (showYAxisLabels && yAxisPosition == YAxisPosition.RIGHT) {
                    Column(
                        modifier = Modifier
                            .width(yAxisFixedWidth)
                            .fillMaxHeight()
                    ) {
                        Spacer(Modifier.weight(0.5f))
                        stageLabels.reversed().forEach { label ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                        Spacer(Modifier.weight(0.5f))
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }
        }

        if (showXAxisLabels && tickInstants.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                if (showYAxisLabels && yAxisPosition == YAxisPosition.LEFT) {
                    Spacer(Modifier.width(yAxisFixedWidth))
                }
                Box(modifier = Modifier.weight(1f)) {
                    WearSleepStageXAxisLabels(
                        tickInstants = tickInstants,
                        tickFormatter = tickFormatter
                    )
                }
            }
        }
    }
}

@Composable
private fun WearSleepStageTooltip(
    stage: SleepStage,
    color: Color,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a").withZone(zone) }
    val startStr = timeFormatter.format(stage.startTime)
    val endStr = timeFormatter.format(stage.endTime)
    val minutes = Duration.between(stage.startTime, stage.endTime).toMinutes()
    val durationText = when {
        minutes < 60 -> "${minutes} min"
        minutes % 60 == 0L -> "${minutes / 60} hr"
        else -> "${minutes / 60} hr ${minutes % 60} min"
    }
    val title = when (stage.stage) {
        SleepStageType.DEEP -> "Deep"
        SleepStageType.LIGHT -> "Core"
        SleepStageType.REM -> "REM"
        SleepStageType.AWAKE -> "Awake"
        SleepStageType.UNKNOWN -> "Unknown"
    }

    TooltipContainer(modifier = modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, shape = CircleShape)
            )
            Text(
                text = "$startStr – $endStr · $durationText",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun WearSleepStageXAxisLabels(
    tickInstants: List<Instant>,
    tickFormatter: DateTimeFormatter
) {
    val tickCount = tickInstants.size
    val labelTickIndices = if (tickCount >= 2) (0 until tickCount - 1).toList() else (0 until tickCount).toList()

    Layout(
        content = {
            labelTickIndices.forEach { idx ->
                Text(
                    text = tickFormatter.format(tickInstants[idx]),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Start
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        if (measurables.isEmpty()) {
            layout(constraints.maxWidth, 0) {}
        } else {
            val looseConstraints = constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
            val placeables = measurables.map { it.measure(looseConstraints) }
            val width = constraints.maxWidth
            val height = placeables.maxOf { it.height }
            val segmentWidth = if (tickCount > 1) width.toFloat() / (tickCount - 1) else 0f

            layout(width, height) {
                labelTickIndices.forEachIndexed { labelIndex, tickIndex ->
                    val p = placeables[labelIndex]
                    val tickX = segmentWidth * tickIndex
                    val x = if (tickIndex == 0) tickX.toInt() else (tickX - p.width / 2f).toInt()
                    p.placeRelative(x.coerceIn(0, width - p.width), height - p.height)
                }
            }
        }
    }
}

private fun mergeConsecutiveStages(stages: List<SleepStage>): List<SleepStage> {
    if (stages.isEmpty()) return emptyList()
    if (stages.size == 1) return stages

    val result = mutableListOf<SleepStage>()
    var current = stages.first()

    for (i in 1 until stages.size) {
        val next = stages[i]
        if (next.stage == current.stage && !next.startTime.isAfter(current.endTime)) {
            current = SleepStage(
                startTime = current.startTime,
                endTime = next.endTime,
                stage = current.stage
            )
        } else {
            result += current
            current = next
        }
    }
    result += current
    return result
}

private data class PillGeom(
    val stage: SleepStage,
    val stageIdx: Int,
    val midX: Float,
    val centerY: Float,
    val startX: Float,
    val endX: Float,
    val width: Float
)

private data class SelectedStageVisual(
    val stage: SleepStage,
    val stageIdx: Int,
    val midX: Float,
    val centerY: Float,
    val capsuleHeight: Float
)

private fun stageLabel(stage: SleepStageType): String = when (stage) {
    SleepStageType.DEEP -> "Deep"
    SleepStageType.LIGHT, SleepStageType.UNKNOWN -> "Core"
    SleepStageType.REM -> "REM"
    SleepStageType.AWAKE -> "Awake"
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WearSleepStageChartPreview() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSalusChartColors provides SalusChartColorScheme.Spring
    ) {
        androidx.compose.material3.MaterialTheme {
            WearSleepStageChart(
                sleepSession = SleepSession(
                    startTime = Instant.parse("2026-04-12T14:00:00Z"),
                    endTime = Instant.parse("2026-04-12T21:18:00Z"),
                    stages = listOf(
                        SleepStage(Instant.parse("2026-04-12T14:00:00Z"), Instant.parse("2026-04-12T15:00:00Z"), SleepStageType.LIGHT),
                        SleepStage(Instant.parse("2026-04-12T15:00:00Z"), Instant.parse("2026-04-12T16:20:00Z"), SleepStageType.DEEP),
                        SleepStage(Instant.parse("2026-04-12T16:20:00Z"), Instant.parse("2026-04-12T17:45:00Z"), SleepStageType.LIGHT),
                        SleepStage(Instant.parse("2026-04-12T17:45:00Z"), Instant.parse("2026-04-12T18:25:00Z"), SleepStageType.REM),
                        SleepStage(Instant.parse("2026-04-12T18:25:00Z"), Instant.parse("2026-04-12T18:35:00Z"), SleepStageType.AWAKE),
                        SleepStage(Instant.parse("2026-04-12T18:35:00Z"), Instant.parse("2026-04-12T21:18:00Z"), SleepStageType.LIGHT)
                    )
                ),
                showYAxis = true,
                showXAxis = true,
                showTitle = true
            )
        }
    }
}
