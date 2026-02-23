package com.hdil.saluschart.ui.compose.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.ChartMark
import com.hdil.saluschart.core.chart.chartDraw.ChartDraw
import com.hdil.saluschart.core.chart.chartDraw.ChartLegend
import com.hdil.saluschart.core.chart.chartDraw.ChartTooltip
import com.hdil.saluschart.core.chart.chartDraw.LegendPosition
import com.hdil.saluschart.core.chart.chartMath.ChartMath
import com.hdil.saluschart.core.chart.chartMath.PieChartMath
import com.hdil.saluschart.ui.theme.ColorUtils

/**
 * Displays a pie (or donut) chart with optional legend, labels, and tap interaction.
 *
 * Interaction behavior:
 * - When [interactionsEnabled] is true, tapping a slice toggles selection and shows a tooltip.
 * - When [interactionsEnabled] is false, gesture detection is disabled and selection/tooltip are cleared.
 *
 * Legend layout:
 * - For [LegendPosition.LEFT]/[LegendPosition.RIGHT], the chart and legend are laid out in a row.
 * - For [LegendPosition.TOP]/[LegendPosition.BOTTOM], the chart and legend are laid out in a column.
 *
 * @param modifier Modifier applied to the chart container.
 * @param data Pie slice data. Each [ChartMark] uses its `y` value as the slice magnitude.
 * @param title Title displayed above the chart.
 * @param isDonut Whether to draw a donut chart (true) or a full pie chart (false).
 * @param colors Colors used for slices. Colors repeat if fewer than [data] size.
 * @param showLegend Whether to display the legend.
 * @param legendPosition Legend position relative to the chart.
 * @param showLabel Whether to draw labels on slices.
 * @param interactionsEnabled Enables or disables tap interaction (selection + tooltip).
 */
@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    data: List<ChartMark>,
    title: String = "Pie Chart Example",
    isDonut: Boolean = true,
    colors: List<Color> = ColorUtils.ColorUtils(data.size.coerceAtLeast(1)),
    showLegend: Boolean = false,
    legendPosition: LegendPosition = LegendPosition.BOTTOM,
    showLabel: Boolean = false,
    interactionsEnabled: Boolean = true,
) {
    if (data.isEmpty()) return

    // Selection + tooltip state
    var selectedSectionIndex by remember { mutableStateOf(-1) }
    var tooltipPosition by remember { mutableStateOf(IntOffset.Zero) }
    var showTooltip by remember { mutableStateOf(false) }

    // Clear any previous selection when interactions are disabled
    LaunchedEffect(interactionsEnabled) {
        if (!interactionsEnabled) {
            selectedSectionIndex = -1
            showTooltip = false
            tooltipPosition = IntOffset.Zero
        }
    }

    val animationScale by animateFloatAsState(
        targetValue = 1.05f,
        animationSpec = tween(300),
        label = "pieScale"
    )

    val isSideLegend = legendPosition == LegendPosition.LEFT || legendPosition == LegendPosition.RIGHT
    val legendBeforeChart = legendPosition == LegendPosition.LEFT || legendPosition == LegendPosition.TOP

    val interactionModifier =
        if (interactionsEnabled) {
            Modifier.pointerInput(data, isDonut) {
                detectTapGestures { offset ->
                    val canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                    val (center, radius) = ChartMath.Pie.computePieMetrics(canvasSize)
                    val sections = ChartMath.Pie.computePieAngles(data)

                    val clickedIndex = ChartMath.Pie.getClickedSectionIndex(
                        offset, center, radius, sections
                    )

                    if (clickedIndex < 0) {
                        selectedSectionIndex = -1
                        showTooltip = false
                        return@detectTapGestures
                    }

                    selectedSectionIndex =
                        if (selectedSectionIndex == clickedIndex) -1 else clickedIndex

                    if (selectedSectionIndex >= 0) {
                        val (startAngle, sweepAngle, _) = sections[selectedSectionIndex]
                        val midAngle = startAngle + sweepAngle / 2f

                        val labelPos = PieChartMath.calculateCenterPosition(
                            center = center,
                            radius = radius / 2f,
                            angleInDegrees = midAngle,
                            forToolTip = true
                        )

                        tooltipPosition = IntOffset(
                            x = labelPos.x.toInt(),
                            y = labelPos.y.toInt()
                        )
                        showTooltip = true
                    } else {
                        showTooltip = false
                    }
                }
            }
        } else {
            Modifier
        }

    val chartContent: @Composable (Modifier) -> Unit = { chartModifier ->
        Box(
            modifier = chartModifier,
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(interactionModifier)
            ) {
                val (center, radius) = ChartMath.Pie.computePieMetrics(size)
                val sections = ChartMath.Pie.computePieAngles(data)

                sections.forEachIndexed { i, (startAngle, sweepAngle, _) ->
                    val color = colors[i % colors.size]
                    val isSelected = i == selectedSectionIndex
                    val alpha = if (selectedSectionIndex >= 0 && !isSelected) 0.3f else 1f
                    val scale = if (isSelected) animationScale else 1f

                    ChartDraw.Pie.drawPieSection(
                        drawScope = this,
                        center = center,
                        radius = radius,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        color = color,
                        isDonut = isDonut,
                        strokeWidth = 100f,
                        isSelected = isSelected,
                        animationScale = scale,
                        alpha = alpha
                    )
                }

                if (showLabel && sections.isNotEmpty()) {
                    ChartDraw.Pie.drawPieLabels(
                        drawScope = this,
                        center = center,
                        radius = radius,
                        data = data,
                        sections = sections
                    )
                }
            }

            if (showTooltip && selectedSectionIndex in data.indices) {
                ChartTooltip(
                    chartMark = data[selectedSectionIndex],
                    modifier = Modifier.offset { tooltipPosition },
                    color = colors[selectedSectionIndex % colors.size]
                )
            }
        }
    }

    val legendContent: @Composable () -> Unit = {
        if (showLegend) {
            ChartLegend(
                chartData = data,
                colors = colors,
                position = legendPosition
            )
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        if (isSideLegend) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (legendBeforeChart) legendContent()
                chartContent(Modifier.weight(1f).fillMaxHeight())
                if (!legendBeforeChart) legendContent()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (legendBeforeChart) {
                    legendContent()
                    Spacer(Modifier.height(16.dp))
                }

                chartContent(Modifier.weight(1f).fillMaxWidth())

                if (!legendBeforeChart) {
                    Spacer(Modifier.height(16.dp))
                    legendContent()
                }
            }
        }
    }
}