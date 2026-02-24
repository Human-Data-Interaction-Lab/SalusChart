package com.hdil.saluschart.core.chart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.chartDraw.LineStyle

/**
 * Configuration for a single horizontal reference line drawn at a specific Y value (data-space).
 *
 * This is typically used to highlight thresholds such as goals, averages, or clinically meaningful cutoffs.
 *
 * @property y Reference Y value in the chart's data coordinate space (not pixels).
 * @property color Line color.
 * @property strokeWidth Line thickness.
 * @property style Line style (solid/dashed/dotted, etc.).
 * @property label Optional label text shown near the line.
 * @property labelBackground Optional background color for the label pill/badge.
 * If null, the renderer may choose a derived background (e.g., based on [color]).
 */
@Immutable
data class ReferenceLineSpec(
    val y: Double,
    val color: Color = Color.Red,
    val strokeWidth: Dp = 2.dp,
    val style: LineStyle = LineStyle.DASHED,
    val label: String? = null,
    val labelBackground: Color? = null
)