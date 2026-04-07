package com.hdil.saluschart.core.chart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hdil.saluschart.core.chart.chartDraw.LineStyle
import com.hdil.saluschart.core.chart.chartDraw.ReferenceLineType

/**
 * Configuration for a single reference line or zone drawn on a chart.
 *
 * The [type] field determines what is rendered:
 * - [ReferenceLineType.AVERAGE]   — horizontal line at the dataset's mean Y value (auto-computed)
 * - [ReferenceLineType.TREND]     — diagonal trend line via linear regression (auto-computed)
 * - [ReferenceLineType.THRESHOLD] — horizontal line at a user-specified [y] value
 * - [ReferenceLineType.ZONE]      — shaded horizontal band from [y] (lower) to [yEnd] (upper)
 *
 * @property type  Which kind of reference line to draw.
 * @property y     For [ReferenceLineType.THRESHOLD]: the exact Y value of the line.
 *                 For [ReferenceLineType.ZONE]: the lower bound of the band.
 *                 Ignored for AVERAGE and TREND (the value is computed from chart data).
 * @property yEnd  Upper bound for [ReferenceLineType.ZONE]. Ignored for all other types.
 * @property color Line or band color.
 * @property strokeWidth Line thickness (not used for ZONE bands).
 * @property style Line dash style.
 * @property label Optional label text displayed near the line.
 *                 For AVERAGE and TREND, the auto-computed value is formatted via [labelFormat]
 *                 when no explicit [label] is provided.
 * @property labelFormat Format string used to display the auto-computed value for AVERAGE/TREND
 *                       (e.g. `"Avg: %.0f"`). Ignored when [label] is set.
 * @property showLabel Whether to show the value label next to the line.
 * @property interactive If true, tapping the line toggles label visibility (AVERAGE/THRESHOLD).
 * @property onClick Optional click callback invoked when the line is tapped.
 */
@Immutable
data class ReferenceLineSpec(
    val type: ReferenceLineType = ReferenceLineType.THRESHOLD,
    val y: Double = 0.0,
    val yEnd: Double? = null,
    val color: Color = Color.Red,
    val strokeWidth: Dp = 2.dp,
    val style: LineStyle = LineStyle.DASHED,
    val label: String? = null,
    val labelFormat: String = "%.0f",
    val showLabel: Boolean = false,
    val interactive: Boolean = false,
    val onClick: (() -> Unit)? = null,
)
