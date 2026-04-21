package com.hdil.saluschart.ui.wear.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.core.chart.ChartMark
import kotlin.math.max

@Composable
internal fun wearResolvedPalette(colors: List<Color>): List<Color> =
    colors.ifEmpty { WearChartDefaults.palette() }

internal fun ChartMark.colorOr(fallback: Color): Color =
    color?.let { Color(it) } ?: fallback

internal fun List<ChartMark>.safeMinY(): Double = minOfOrNull { it.y } ?: 0.0

internal fun List<ChartMark>.safeMaxY(): Double = maxOfOrNull { it.y } ?: 0.0

internal fun normalizeY(
    value: Double,
    min: Double,
    max: Double
): Float {
    if (max <= min) return 0.5f
    return ((value - min) / (max - min)).toFloat().coerceIn(0f, 1f)
}

internal fun positiveOrOne(value: Double): Double = max(value, 1.0)
