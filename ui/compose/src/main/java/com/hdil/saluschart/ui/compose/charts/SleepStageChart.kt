package com.hdil.saluschart.ui.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.chartDraw.YAxisPosition
import com.hdil.saluschart.data.model.model.SleepSession
import com.hdil.saluschart.data.model.model.SleepStage
import com.hdil.saluschart.data.model.model.SleepStageType
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

/**
 * Apple-Health-style Sleep Stage chart.
 */
@Composable
fun SleepStageChart(
    modifier: Modifier = Modifier,
    sleepSession: SleepSession,
    title: String = "Sleep Stage Analysis",
    showLabels: Boolean = true,
    showXAxis: Boolean = true,
    onStageClick: ((Int, String) -> Unit)? = null,
    barHeightRatio: Float = 0.5f,
    yAxisPosition: YAxisPosition = YAxisPosition.LEFT,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    showTitle: Boolean = true,
    showYAxis: Boolean = true,
    showStartEndLabels: Boolean = true,
    xLabelAutoSkip: Boolean = true,
    yAxisFixedWidth: Dp = 48.dp
) {
    if (sleepSession.stages.isEmpty()) return

    // Selected pill (for tooltip)
    var selectedVisual by remember { mutableStateOf<SelectedStageVisual?>(null) }

    // 1) Merge consecutive stages of same type.
    val mergedStages = remember(sleepSession.stages) {
        mergeConsecutiveStages(sleepSession.stages)
    }

    // Stage order bottom → top (Deep, Core, REM, Awake)
    val stageLabels = listOf("Deep", "Core", "REM", "Awake")

    fun stageIndex(type: SleepStageType): Int = when (type) {
        SleepStageType.DEEP -> 0
        SleepStageType.LIGHT -> 1 // Core
        SleepStageType.REM -> 2
        SleepStageType.AWAKE -> 3
        SleepStageType.UNKNOWN -> 1
    }

    val minX = mergedStages.minOf { it.startTime.toEpochMilli().toDouble() }
    val maxX = max(
        minX + 1.0,
        mergedStages.maxOf { it.endTime.toEpochMilli().toDouble() }
    )

    // 2) X-axis ticks (hours) – domain starts at first stage time, small padding at end
    val zone = ZoneId.systemDefault()
    val minInstant = Instant.ofEpochMilli(minX.toLong())
    val maxInstantRaw = Instant.ofEpochMilli(maxX.toLong())

    val padMinutes = 30L
    val domainMinInstant = minInstant
    val domainMaxInstant = maxInstantRaw.plusSeconds(padMinutes * 60)

    val domainMinX = domainMinInstant.toEpochMilli().toDouble()
    val domainMaxX = domainMaxInstant.toEpochMilli().toDouble()

    // 5 ticks → 4 segments (0%, 25%, 50%, 75%, 100% of domain)
    val tickInstants: List<Instant> =
        (0..4).map { i ->
            val ratio = i / 4.0
            val millis = domainMinX + ratio * (domainMaxX - domainMinX)
            Instant.ofEpochMilli(millis.toLong())
        }

    val tickFormatter = DateTimeFormatter.ofPattern("h a").withZone(zone)

    Column(
        modifier = modifier.padding(contentPadding)
    ) {
        if (showTitle) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(20.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val density = LocalDensity.current

            val leftInsetPx = if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                with(density) { yAxisFixedWidth.toPx() }
            } else {
                0f
            }

            val rightInsetPx = if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
                with(density) { (yAxisFixedWidth + 8.dp).toPx() }
            } else {
                0f
            }

            val extendDown = 0f

            // 1) BACKGROUND GRID
            Canvas(
                modifier = Modifier.matchParentSize()
            ) {
                val stageCount = stageLabels.size
                val rowHeight = size.height / stageCount.toFloat()

                val gridColor = Color.Black.copy(alpha = 0.22f)
                val horizontalStroke = 1.6f
                val verticalStroke = 1.4f
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                // Horizontal lines across the whole width
                for (i in 0 until stageCount) {
                    val y = size.height - i * rowHeight
                    drawLine(
                        color = gridColor.copy(alpha = if (i == 0) 0.30f else 0.18f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = horizontalStroke
                    )
                }

                // Vertical grid: solid left border, solid right border, 3 dashed lines
                val chartLeft = leftInsetPx
                val chartRight = size.width - rightInsetPx
                val chartWidthPx = chartRight - chartLeft

                // solid left border (outside labels)
                drawLine(
                    color = gridColor.copy(alpha = 0.30f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = verticalStroke
                )

                // solid right border
                drawLine(
                    color = gridColor.copy(alpha = 0.30f),
                    start = Offset(chartRight, 0f),
                    end = Offset(chartRight, size.height),
                    strokeWidth = verticalStroke
                )

                // dashed internal lines
                if (tickInstants.size > 2) {
                    for (i in 1 until tickInstants.lastIndex) {
                        val tMillis = tickInstants[i].toEpochMilli().toDouble()
                        val ratio = ((tMillis - domainMinX) / (domainMaxX - domainMinX)).toFloat()
                        val x = chartLeft + ratio * chartWidthPx

                        drawLine(
                            color = gridColor.copy(alpha = 0.18f),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height + extendDown),
                            strokeWidth = verticalStroke,
                            pathEffect = dashEffect
                        )
                    }
                }
            }

            // 2) FOREGROUND CONTENT: labels + chart
            Row(
                modifier = Modifier.matchParentSize()
            ) {
                // LEFT Y-AXIS LABELS
                if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
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
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                            }
                        }
                    }
                }

                // MAIN CHART AREA (pills + connectors + tooltip)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        // Tap handler – calculate hit in the same coord system as Canvas
                        .pointerInput(mergedStages, domainMinX, domainMaxX) {
                            detectTapGestures { tapOffset ->
                                val chartWidth = size.width.toFloat()
                                val chartHeight = size.height.toFloat()
                                val stageCount = stageLabels.size
                                val rowHeight = chartHeight / stageCount.toFloat()
                                val capsuleHeight = rowHeight * 0.55f

                                fun toX(ms: Double): Float {
                                    val ratio =
                                        ((ms - domainMinX) / (domainMaxX - domainMinX)).toFloat()
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

                                    if (tapOffset.x in startX..endX &&
                                        tapOffset.y in top..bottom
                                    ) {
                                        hit = SelectedStageVisual(
                                            stage = stage,
                                            stageIdx = idx,
                                            midX = midX,
                                            centerY = centerY,
                                            capsuleHeight = capsuleHeight
                                        )
                                        break
                                    }
                                }

                                selectedVisual = if (hit != null &&
                                    selectedVisual?.stage == hit.stage
                                ) {
                                    null // tap again to deselect
                                } else {
                                    hit
                                }
                            }
                        }
                ) {
                    val densityLocal = LocalDensity.current

                    // ---------- CAPSULES + CONNECTORS ----------
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val chartLeft = 0f
                        val chartRight = size.width
                        val chartTop = 0f
                        val chartBottom = size.height

                        val chartWidth = chartRight - chartLeft
                        val chartHeight = chartBottom - chartTop

                        val stageCount = stageLabels.size
                        val rowHeight = chartHeight / stageCount.toFloat()

                        val capsuleHeight = rowHeight * 0.55f
                        val minCapsuleWidth = capsuleHeight * 0.1f
                        val connectorWidth = capsuleHeight * 0.1f

                        fun toX(ms: Double): Float {
                            val ratio =
                                ((ms - domainMinX) / (domainMaxX - domainMinX)).toFloat()
                            return chartLeft + ratio * chartWidth
                        }

                        fun centerYForStageIndex(index: Int): Float {
                            val fromBottom = index + 0.5f
                            return chartBottom - fromBottom * rowHeight
                        }

                        val pillGeoms = mergedStages.map { stage ->
                            val idx = stageIndex(stage.stage)

                            val startMs = stage.startTime.toEpochMilli().toDouble()
                            val endMs = stage.endTime.toEpochMilli().toDouble()

                            val startX = toX(startMs)
                            val endX = toX(endMs)
                            val rawWidth = (endX - startX).coerceAtLeast(2f)
                            val width = rawWidth.coerceAtLeast(minCapsuleWidth)
                            val midX = (startX + endX) / 2f
                            val centerY = centerYForStageIndex(idx)

                            PillGeom(
                                stage = stage,
                                stageIdx = idx,
                                midX = midX,
                                centerY = centerY,
                                startX = startX,
                                endX = endX,
                                width = width
                            )
                        }

                        // Base colors for stages
                        val awakeColor = Color(0xFFFF4D4F)
                        val remColor = Color(0xFF00D4FF)
                        val coreColor = Color(0xFF0099FF)
                        val deepColor = Color(0xFF3A2B96)

                        fun colorForStage(type: SleepStageType): Color =
                            when (type) {
                                SleepStageType.AWAKE -> awakeColor
                                SleepStageType.REM -> remColor
                                SleepStageType.LIGHT -> coreColor
                                SleepStageType.DEEP -> deepColor
                                SleepStageType.UNKNOWN -> coreColor
                            }

                        // CONNECTORS
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

                            val transitionTimeMs =
                                current.endTime.toEpochMilli().toDouble()
                            val x = toX(transitionTimeMs)

                            fun stageTypeForIndex(idx: Int): SleepStageType =
                                when (idx) {
                                    3 -> SleepStageType.AWAKE
                                    2 -> SleepStageType.REM
                                    1 -> SleepStageType.LIGHT
                                    else -> SleepStageType.DEEP
                                }

                            val topStage = stageTypeForIndex(topIdx)
                            val bottomStage = stageTypeForIndex(bottomIdx)

                            val (topColor, bottomColor) = when {
                                topStage == SleepStageType.AWAKE &&
                                        (bottomStage == SleepStageType.REM ||
                                                bottomStage == SleepStageType.LIGHT) ->
                                    awakeColor to if (bottomStage == SleepStageType.REM) remColor else coreColor

                                topStage == SleepStageType.REM &&
                                        bottomStage == SleepStageType.LIGHT ->
                                    remColor to coreColor

                                topStage == SleepStageType.LIGHT &&
                                        bottomStage == SleepStageType.DEEP ->
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

                        // CAPSULES
                        pillGeoms.forEach { geom ->
                            val stage = geom.stage
                            val baseColor = colorForStage(stage.stage)

                            val capsuleHeight = rowHeight * 0.55f
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
                                    baseColor.copy(alpha = 0.0f),
                                ),
                                startY = haloTop,
                                endY = haloTop + haloHeight,
                            )

                            drawRoundRect(
                                brush = haloBrush,
                                topLeft = Offset(haloLeft, haloTop),
                                size = Size(haloWidth, haloHeight),
                                cornerRadius = CornerRadius(
                                    (capsuleHeight / 6f) + haloPad,
                                    (capsuleHeight / 6f) + haloPad
                                )
                            )

                            drawRoundRect(
                                color = baseColor,
                                topLeft = Offset(capsuleStartX, top),
                                size = Size(geom.width, capsuleHeight),
                                cornerRadius = CornerRadius(
                                    x = capsuleHeight / 6f,
                                    y = capsuleHeight / 6f
                                )
                            )
                        }
                    }

                    // ---------- TOOLTIP OVERLAY ----------
                    if (selectedVisual != null) {
                        val visual = selectedVisual!!
                        val stage = visual.stage

                        var hostSize by remember { mutableStateOf(IntSize.Zero) }
                        var tipSize by remember { mutableStateOf(IntSize.Zero) }

                        val stageColor = when (stage.stage) {
                            SleepStageType.AWAKE -> Color(0xFFFF4D4F)
                            SleepStageType.REM -> Color(0xFF00D4FF)
                            SleepStageType.LIGHT -> Color(0xFF0099FF)
                            SleepStageType.DEEP -> Color(0xFF3A2B96)
                            SleepStageType.UNKNOWN -> Color(0xFF0099FF)
                        }

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .onSizeChanged { hostSize = it }
                        ) {
                            val padPx = with(densityLocal) { 8.dp.toPx() }

                            val anchorX = visual.midX
                            val anchorY = visual.centerY - visual.capsuleHeight / 2f

                            val desiredX = anchorX + padPx
                            val desiredYAbove = anchorY - padPx - tipSize.height

                            val yPlaced =
                                if (desiredYAbove < 0f) anchorY + padPx else desiredYAbove

                            val maxX =
                                (hostSize.width - tipSize.width).coerceAtLeast(0)
                            val maxY =
                                (hostSize.height - tipSize.height).coerceAtLeast(0)

                            val xClamped = desiredX.coerceIn(0f, maxX.toFloat())
                            val yClamped = yPlaced.coerceIn(0f, maxY.toFloat())

                            SleepStageTooltip(
                                stage = stage,
                                color = stageColor,
                                modifier = Modifier
                                    .offset {
                                        IntOffset(xClamped.toInt(), yClamped.toInt())
                                    }
                                    .onSizeChanged { tipSize = it }
                            )
                        }
                    }
                }

                // RIGHT Y-AXIS
                if (showYAxis && yAxisPosition == YAxisPosition.RIGHT) {
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
                                    style = MaterialTheme.typography.bodySmall,
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

        // X-axis labels
        if (showXAxis && tickInstants.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showYAxis && yAxisPosition == YAxisPosition.LEFT) {
                    Spacer(Modifier.width(yAxisFixedWidth))
                }
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    XAxisLabels(
                        tickInstants = tickInstants,
                        tickFormatter = tickFormatter
                    )
                }
            }
        }
    }
}

@Composable
private fun SleepStageTooltip(
    stage: SleepStage,
    color: Color,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("h:mm a").withZone(zone)
    }

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

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.1f),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}


// ===== X axis labels =====

@Composable
private fun XAxisLabels(
    tickInstants: List<Instant>,
    tickFormatter: DateTimeFormatter
) {
    val tickCount = tickInstants.size

    val labelTickIndices: List<Int> =
        if (tickCount >= 2) (0 until tickCount - 1).toList() else (0 until tickCount).toList()

    Layout(
        content = {
            labelTickIndices.forEach { idx ->
                Text(
                    text = tickFormatter.format(tickInstants[idx]),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        if (measurables.isEmpty()) {
            layout(constraints.maxWidth, 0) {}
        } else {
            val looseConstraints = constraints.copy(
                minWidth = 0,
                maxWidth = Constraints.Infinity
            )
            val placeables = measurables.map { it.measure(looseConstraints) }

            val width = constraints.maxWidth
            val height = placeables.maxOf { it.height }

            val segmentWidth =
                if (tickCount > 1) width.toFloat() / (tickCount - 1) else 0f

            layout(width, height) {
                labelTickIndices.forEachIndexed { labelIndex, tickIndex ->
                    val p = placeables[labelIndex]
                    val tickX = segmentWidth * tickIndex
                    val x = if (tickIndex == 0) {
                        tickX.toInt()
                    } else {
                        (tickX - p.width / 2f).toInt()
                    }
                    p.placeRelative(
                        x.coerceIn(0, width - p.width),
                        height - p.height
                    )
                }
            }
        }
    }
}

// ===== Helpers =====

/** Merge consecutive SleepStage entries with the same type into one block. */
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
