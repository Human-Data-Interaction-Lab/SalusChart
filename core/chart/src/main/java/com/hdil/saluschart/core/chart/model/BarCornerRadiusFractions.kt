package com.hdil.saluschart.core.chart.model

import androidx.compose.runtime.Immutable

/**
 * Defines per-corner radius fractions for a bar.
 *
 * Each value represents the fraction (0f..1f) of the bar’s maximum possible
 * corner radius applied to that specific corner.
 *
 * This allows asymmetric rounding, for example:
 * - Only top corners rounded (typical vertical bar chart)
 * - Only start corners rounded (horizontal bar chart)
 * - Fully rounded rectangle
 *
 * Values are interpreted as proportions, not absolute pixels.
 *
 * @param topStart Fraction for the top-start corner (0f = sharp, 1f = fully rounded)
 * @param topEnd Fraction for the top-end corner
 * @param bottomStart Fraction for the bottom-start corner
 * @param bottomEnd Fraction for the bottom-end corner
 */

@Immutable
data class BarCornerRadiusFractions(
    val topStart: Float = 0f,
    val topEnd: Float = 0f,
    val bottomStart: Float = 0f,
    val bottomEnd: Float = 0f
)